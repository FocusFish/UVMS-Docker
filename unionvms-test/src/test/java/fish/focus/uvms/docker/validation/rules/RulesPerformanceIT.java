package fish.focus.uvms.docker.validation.rules;

import fish.focus.schema.exchange.module.v1.ProcessedMovementResponse;
import fish.focus.schema.exchange.movement.v1.MovementRefTypeType;
import fish.focus.schema.movementrules.asset.v1.AssetId;
import fish.focus.schema.movementrules.asset.v1.AssetIdList;
import fish.focus.schema.movementrules.asset.v1.AssetIdType;
import fish.focus.schema.movementrules.asset.v1.AssetType;
import fish.focus.schema.movementrules.exchange.v1.PluginType;
import fish.focus.schema.movementrules.module.v1.RulesModuleMethod;
import fish.focus.schema.movementrules.module.v1.SetMovementReportRequest;
import fish.focus.schema.movementrules.movement.v1.*;
import fish.focus.uvms.asset.client.model.AssetDTO;
import fish.focus.uvms.docker.validation.asset.AssetTestHelper;
import fish.focus.uvms.docker.validation.common.MessageHelper;
import fish.focus.uvms.docker.validation.mobileterminal.MobileTerminalTestHelper;
import fish.focus.uvms.docker.validation.mobileterminal.dto.MobileTerminalDto;
import fish.focus.uvms.docker.validation.movement.LatLong;
import fish.focus.uvms.docker.validation.movement.MovementHelper;
import fish.focus.uvms.movementrules.model.mapper.JAXBMarshaller;
import org.apache.activemq.artemis.api.core.TransportConfiguration;
import org.apache.activemq.artemis.api.jms.ActiveMQJMSClient;
import org.apache.activemq.artemis.api.jms.JMSFactoryType;
import org.apache.activemq.artemis.core.remoting.impl.netty.NettyConnectorFactory;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import javax.jms.Queue;
import javax.jms.*;
import javax.xml.bind.JAXBException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

public class RulesPerformanceIT {

    private static final String MOVEMENTRULES_QUEUE = "UVMSMovementRulesEvent";
    private static final String RESPONSE_QUEUE = "IntegrationTestsResponseQueue";
    private static MovementHelper movementHelper;
    private static MessageHelper messageHelper;
    private final ConnectionFactory connectionFactory;

    public RulesPerformanceIT() {
        Map<String, Object> params = new HashMap<>();
        params.put("host", "localhost");
        params.put("port", 5445);
        TransportConfiguration transportConfiguration = new TransportConfiguration(NettyConnectorFactory.class.getName(), params);
        connectionFactory = ActiveMQJMSClient.createConnectionFactoryWithoutHA(JMSFactoryType.CF, transportConfiguration);
    }

    @BeforeClass
    public static void setup() throws JMSException {
        movementHelper = new MovementHelper();
        messageHelper = new MessageHelper();
    }

    @AfterClass
    public static void cleanup() {
        movementHelper.close();
    }

    private static RawMovementType createBasicMovement(AssetId assetId, String assetName, LatLong pos) {
        RawMovementType movement = new RawMovementType();
        movement.setAssetId(assetId);
        movement.setAssetName(assetName);
        movement.setFlagState("SWE");
        movement.setDateRecieved(new Date());
        movement.setMovementType(MovementTypeType.POS);
        movement.setPluginName("PLUGIN");
        movement.setPluginType("SATELLITE_RECEIVER");
        MovementPoint movementPoint = new MovementPoint();
        movementPoint.setLatitude(pos.latitude);
        movementPoint.setLongitude(pos.longitude);
        movement.setPosition(movementPoint);
        movement.setPositionTime(new Date());
        movement.setReportedCourse(pos.bearing);
        movement.setReportedSpeed(pos.speed);
        movement.setSource(MovementSourceType.INMARSAT_C);
        movement.setComChannelType(MovementComChannelType.NAF);
        return movement;
    }

    private static String createSetMovementReportRequest(PluginType type, RawMovementType rawMovementType) throws JAXBException {
        SetMovementReportRequest request = new SetMovementReportRequest();
        request.setMethod(RulesModuleMethod.SET_MOVEMENT_REPORT);
        request.setType(type);
        request.setUsername("PerformanceTester");
        request.setRequest(rawMovementType);
        return JAXBMarshaller.marshallJaxBObjectToString(request);
    }

    private static String humanReadableFormat(Duration duration) {
        return duration.toString()
                .substring(2)
                .replaceAll("(\\d[HMS])(?!$)", "$1 ")
                .toLowerCase();
    }

    @Test
    @Ignore("Needs a special version of rules that respond on the test queue to work!")
    public void createRouteTestTitanic1000PositionsSync() throws Exception {
        AssetDTO testAsset = AssetTestHelper.createTestAsset();
        MobileTerminalDto mobileTerminal = MobileTerminalTestHelper.createMobileTerminal();
        MobileTerminalTestHelper.assignMobileTerminal(testAsset, mobileTerminal);
        // 0.1F = 654 pos    0.01 = 6543     0.07 = 934   0.06 = 1090
        List<LatLong> route = movementHelper.createRuttCobhNewYork(1000, 0.06f);

        AssetId assetId = new AssetId();
        assetId.setAssetType(AssetType.VESSEL);
        AssetIdList assetIdList = new AssetIdList();
        assetIdList.setIdType(AssetIdType.IRCS);
        assetIdList.setValue(testAsset.getIrcs());
        assetId.getAssetIdList().add(assetIdList);

        assetIdList = new AssetIdList();
        assetIdList.setIdType(AssetIdType.CFR);
        assetIdList.setValue(testAsset.getCfr());
        assetId.getAssetIdList().add(assetIdList);

        int i = 0;
        Instant b4 = Instant.now();
        Instant lastIteration = Instant.now();
        List<Duration> averageDurations = new ArrayList<>();

        for (LatLong pos : route) {
            RawMovementType move = createBasicMovement(assetId, testAsset.getName(), pos);
            String request = createSetMovementReportRequest(PluginType.FLUX, move);

            String corrId = sendMessageToRules(request, RulesModuleMethod.SET_MOVEMENT_REPORT.value());

            Message message = messageHelper.listenForResponseOnQueue("PerformanceTester", "IntegrationTestsResponseQueue");

            ProcessedMovementResponse movementResponse = JAXBMarshaller.unmarshallTextMessage((TextMessage) message, ProcessedMovementResponse.class);
            if (movementResponse.getMovementRefType().getType().equals(MovementRefTypeType.ALARM)) {
                System.out.println("Alarm: " + i + ", ");
            }
            i++;
            if ((i % 10) == 0) {
                System.out.println("Created movement number: " + i + " Time so far: " + humanReadableFormat(Duration.between(b4, Instant.now()))
                        + " Time since last 10: " + humanReadableFormat(Duration.between(lastIteration, Instant.now())));
                averageDurations.add(Duration.between(lastIteration, Instant.now()));
                lastIteration = Instant.now();
            }
        }
        averageDurations.forEach(dur -> System.out.print(humanReadableFormat(dur) + ", "));
        System.out.println();
    }

    @Test
    @Ignore("Needs a special version of rules that respond on the test queue to work!")
    public void createRouteTestTitanic1000PositionsAsync() throws Exception {
        // 0.1F = 654 pos    0.01 = 6543     0.07 = 934   0.06 = 1090
        List<LatLong> route = movementHelper.createRuttCobhNewYork(1000, 0.06f);
        sendRouteToRulesOnXShipsAsync(1, route);
    }

    @Test
    @Ignore("Needs a special version of rules that respond on the test queue to work!")
    public void createRouteTestTitanic10ships100PositionsEachAsync() throws Exception {
        // 0.1F = 654 pos    0.01 = 6543     0.07 = 934   0.06 = 1090
        List<LatLong> route = movementHelper.createRuttCobhNewYork(1000, 0.06f);
        sendRouteToRulesOnXShipsAsync(10, route);
    }

    @Test
    @Ignore("Needs a special version of rules that respond on the test queue to work!")
    public void createRouteTestTitanic10ships600PositionsEachAsync() throws Exception {
        // 0.1F = 654 pos    0.01 = 6543     0.07 = 934   0.06 = 1090
        List<LatLong> route = movementHelper.createRuttCobhNewYork(6000, 0.01f);
        sendRouteToRulesOnXShipsAsync(10, route);
    }

    @Test
    @Ignore("Needs a special version of rules that respond on the test queue to work!")
    public void createRouteTestTitanic10ships6000PositionsEachAsync() throws Exception {
        // 0.1F = 654 pos    0.01 = 6543     0.07 = 934   0.06 = 1090
        List<LatLong> route = movementHelper.createRuttCobhNewYork(60000, 0.001f);
        sendRouteToRulesOnXShipsAsync(10, route);
    }

    @Test
    @Ignore("Needs a special version of rules that respond on the test queue to work!")
    public void createRouteTestTitanic60ships100PositionsEachAsync() throws Exception {
        // 0.1F = 654 pos    0.01 = 6543     0.07 = 934   0.06 = 1090
        List<LatLong> route = movementHelper.createRuttCobhNewYork(6000, 0.01f);
        sendRouteToRulesOnXShipsAsync(60, route);
    }

    // Needs a special version of rules that respond on the test queue to work! See ExchangeServiceBean in Movement-Rules
    private void sendRouteToRulesOnXShipsAsync(int nrOfShips, List<LatLong> route) throws Exception {
        List<AssetId> assetList = new ArrayList<>();
        List<String> nameList = new ArrayList<>();

        System.out.println("Start creating assets");
        for (int i = 0; i < nrOfShips; i++) {
            AssetDTO testAsset = AssetTestHelper.createTestAsset();
            MobileTerminalDto mobileTerminal = MobileTerminalTestHelper.createMobileTerminal();
            MobileTerminalTestHelper.assignMobileTerminal(testAsset, mobileTerminal);

            AssetId assetId = new AssetId();
            assetId.setAssetType(AssetType.VESSEL);
            AssetIdList assetIdList = new AssetIdList();
            assetIdList.setIdType(AssetIdType.IRCS);
            assetIdList.setValue(testAsset.getIrcs());
            assetId.getAssetIdList().add(assetIdList);

            assetIdList = new AssetIdList();
            assetIdList.setIdType(AssetIdType.CFR);
            assetIdList.setValue(testAsset.getCfr());
            assetId.getAssetIdList().add(assetIdList);

            assetList.add(assetId);
            nameList.add(testAsset.getName());
        }

        System.out.println("Done with creating assets");

        int i = 0;
        Instant b4 = Instant.now();
        Instant lastIteration = Instant.now();
        List<Duration> averageDurations = new ArrayList<>();
        List<String> corrList = new ArrayList<>();

        for (LatLong pos : route) {
            AssetId assetId = assetList.get(i % nrOfShips);

            RawMovementType move = createBasicMovement(assetId, nameList.get(i % nrOfShips), pos);
            String request = createSetMovementReportRequest(PluginType.FLUX, move);

            String corrId = sendMessageToRules(request, RulesModuleMethod.SET_MOVEMENT_REPORT.value());
            corrList.add(corrId);

            i++;
            if ((i % 10) == 0) {
                System.out.println("Created movement number: " + i + " Time so far: " + humanReadableFormat(Duration.between(b4, Instant.now()))
                        + " Time since last 10: " + humanReadableFormat(Duration.between(lastIteration, Instant.now())));
                averageDurations.add(Duration.between(lastIteration, Instant.now()));
                lastIteration = Instant.now();
            }
        }

        Instant middle = Instant.now();
        i = 0;
        for (String corr : corrList) {
            Message message = messageHelper.listenForResponseOnQueue("PerformanceTester", "IntegrationTestsResponseQueue");
            ProcessedMovementResponse movementResponse = JAXBMarshaller.unmarshallTextMessage((TextMessage) message, ProcessedMovementResponse.class);
            if (movementResponse.getMovementRefType().getType().equals(MovementRefTypeType.ALARM)) {
                System.out.println("Alarm: " + i + ", ");
            }
            i++;

            if ((i % 10) == 0) {
                System.out.println("Recieved movement number: " + i + " Time so far: " + humanReadableFormat(Duration.between(b4, Instant.now()))
                        + " Time since last 10: " + humanReadableFormat(Duration.between(lastIteration, Instant.now())));
                averageDurations.add(Duration.between(lastIteration, Instant.now()));
                lastIteration = Instant.now();
            }
        }

        averageDurations.forEach(dur -> System.out.print(humanReadableFormat(dur) + ", "));
        System.out.println();
    }

    private String sendMessageToRules(String text, String requestType) throws Exception {
        try (Connection connection = connectionFactory.createConnection("test", "test")) {
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Queue responseQueue = session.createQueue(RESPONSE_QUEUE);
            Queue assetQueue = session.createQueue(MOVEMENTRULES_QUEUE);

            TextMessage message = session.createTextMessage();
            message.setJMSReplyTo(responseQueue);
            message.setText(text);
            message.setStringProperty("FUNCTION", requestType);

            session.createProducer(assetQueue).send(message);

            return message.getJMSMessageID();
        }
    }
}
