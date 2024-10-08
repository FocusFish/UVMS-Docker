/*
﻿Developed with the contribution of the European Commission - Directorate General for Maritime Affairs and Fisheries
© European Union, 2015-2016.

This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can
redistribute it and/or modify it under the terms of the GNU General Public License as published by the
Free Software Foundation, either version 3 of the License, or any later version. The IFDM Suite is distributed in
the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details. You should have received a
copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */
package fish.focus.uvms.docker.validation.system.vms;

import fish.focus.schema.exchange.movement.v1.MovementTypeType;
import fish.focus.schema.movement.search.v1.ListCriteria;
import fish.focus.schema.movement.search.v1.MovementQuery;
import fish.focus.schema.movement.search.v1.SearchKey;
import fish.focus.schema.movement.v1.MovementType;
import fish.focus.schema.movementrules.customrule.v1.*;
import fish.focus.uvms.asset.client.model.AssetDTO;
import fish.focus.uvms.docker.validation.asset.AssetTestHelper;
import fish.focus.uvms.docker.validation.common.AbstractRest;
import fish.focus.uvms.docker.validation.common.TopicListener;
import fish.focus.uvms.docker.validation.exchange.ExchangeHelper;
import fish.focus.uvms.docker.validation.exchange.dto.ExchangeLogDto;
import fish.focus.uvms.docker.validation.movement.LatLong;
import fish.focus.uvms.docker.validation.movement.MovementHelper;
import fish.focus.uvms.docker.validation.movement.model.AlarmReport;
import fish.focus.uvms.docker.validation.system.helper.*;
import fish.focus.uvms.docker.validation.user.UserHelper;
import fish.focus.uvms.docker.validation.user.dto.Channel;
import fish.focus.uvms.docker.validation.user.dto.EndPoint;
import fish.focus.uvms.docker.validation.user.dto.Organisation;
import fish.focus.uvms.movement.model.dto.MovementDto;
import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Test;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class NAFSystemIT extends AbstractRest {

    public static int ENDPOINT_PORT = 29001;

    @After
    public void removeCustomRules() {
        CustomRuleHelper.removeCustomRulesByDefaultUser();
    }

    @Test
    public void sendPositionToNorwayAndVerifyMandatoryFields() throws IOException, Exception {
        Organisation organisation = createOrganisationNorway();

        CustomRuleType flagStateRule = CustomRuleBuilder.getBuilder()
                .setName("Area NOR => Send to NOR")
                .rule(CriteriaType.AREA, SubCriteriaType.AREA_CODE,
                        ConditionType.EQ, "NOR")
                .action(ActionType.SEND_REPORT, VMSSystemHelper.NAF_NAME, organisation.getName())
                .build();

        CustomRuleType createdCustomRule = CustomRuleHelper.createCustomRule(flagStateRule);
        assertNotNull(createdCustomRule);

        AssetDTO asset = AssetTestHelper.createTestAsset();
        LatLong position = new LatLong(58.973, 5.781, Date.from(Instant.now().minusMillis(10 * 60 * 1000)));
        position.speed = 5;

        try (NafEndpoint nafEndpoint = new NafEndpoint(ENDPOINT_PORT)) {
            NAFHelper.sendPositionToNAFPlugin(position, asset);
            nafEndpoint.getMessage(10000);
        }

        position = new LatLong(58.973, 5.781, Date.from(Instant.now()));
        position.speed = 5;

        String message;
        try (NafEndpoint nafEndpoint = new NafEndpoint(ENDPOINT_PORT)) {
            NAFHelper.sendPositionToNAFPlugin(position, asset);
            message = nafEndpoint.getMessage(10000);
        }

        assertThat(NAFHelper.readCodeValue("AD", message), is(organisation.getNation()));
        assertThat(NAFHelper.readCodeValue("FR", message), is("UNK"));
        assertThat(NAFHelper.readCodeValue("TM", message), is(MovementTypeType.POS.toString()));
        assertThat(NAFHelper.readCodeValue("RC", message), is(asset.getIrcs()));
        assertThat(NAFHelper.readCodeDoubleValue("LT", message), is(position.latitude));
        assertThat(NAFHelper.readCodeDoubleValue("LG", message), is(position.longitude));
        assertThat(NAFHelper.readCodeValue("SP", message), is(String.valueOf((int) position.speed * 10)));
        assertThat(NAFHelper.readCodeValue("CO", message), is(String.valueOf((int) position.bearing)));
        ZonedDateTime positionTime = ZonedDateTime.ofInstant(position.positionTime.toInstant(), ZoneId.of("UTC"));
        assertThat(NAFHelper.readCodeValue("DA", message), is(positionTime.format(DateTimeFormatter.ofPattern("yyyyMMdd"))));
        assertThat(NAFHelper.readCodeValue("TI", message), is(positionTime.format(DateTimeFormatter.ofPattern("HHmm"))));
    }

    @Test
    public void sendPositionToOrganisationWithTwoEndpoints() throws IOException, Exception {
        Organisation organisation = createOrganisationNorway();
        EndPoint endpoint = new EndPoint();
        endpoint.setName("FLUX");
        endpoint.setUri("URI");
        endpoint.setStatus("E");
        endpoint.setOrganisationName(organisation.getName());
        EndPoint createdEndpoint = UserHelper.createEndpoint(endpoint);
        Channel channel = new Channel();
        channel.setDataflow("FLUX");
        channel.setService("FLUX");
        channel.setPriority(1);
        channel.setEndpointId(createdEndpoint.getEndpointId());
        UserHelper.createChannel(channel);

        CustomRuleType flagStateRule = CustomRuleBuilder.getBuilder()
                .setName("Area NOR => Send to NOR")
                .rule(CriteriaType.AREA, SubCriteriaType.AREA_CODE,
                        ConditionType.EQ, "NOR")
                .action(ActionType.SEND_REPORT, VMSSystemHelper.NAF_NAME, organisation.getName())
                .build();

        CustomRuleType createdCustomRule = CustomRuleHelper.createCustomRule(flagStateRule);
        assertNotNull(createdCustomRule);

        AssetDTO asset = AssetTestHelper.createTestAsset();
        LatLong position = new LatLong(58.973, 5.781, Date.from(Instant.now().minusMillis(10 * 60 * 1000)));
        position.speed = 5;
        try (TopicListener topicListener = new TopicListener(TopicListener.EVENT_STREAM, "event = 'Movement'")) {
            NAFHelper.sendPositionToNAFPlugin(position, asset);
            topicListener.listenOnEventBus(); // First position for an asset creates ENT, ignore this
        }

        try (NafEndpoint nafEndpoint = new NafEndpoint(ENDPOINT_PORT)) {
            NAFHelper.sendPositionToNAFPlugin(position, asset);
            nafEndpoint.getMessage(10000);
        }

        position = new LatLong(58.973, 5.781, Date.from(Instant.now()));
        position.speed = 5;

        String message;
        try (NafEndpoint nafEndpoint = new NafEndpoint(ENDPOINT_PORT)) {
            NAFHelper.sendPositionToNAFPlugin(position, asset);
            message = nafEndpoint.getMessage(10000);
        }

        assertThat(NAFHelper.readCodeValue("AD", message), is(organisation.getNation()));
        assertThat(NAFHelper.readCodeValue("FR", message), is("UNK"));
        assertThat(NAFHelper.readCodeValue("TM", message), is(MovementTypeType.POS.toString()));
        assertThat(NAFHelper.readCodeValue("RC", message), is(asset.getIrcs()));
        assertThat(NAFHelper.readCodeDoubleValue("LT", message), is(position.latitude));
        assertThat(NAFHelper.readCodeDoubleValue("LG", message), is(position.longitude));
        assertThat(NAFHelper.readCodeValue("SP", message), is(String.valueOf((int) position.speed * 10)));
        assertThat(NAFHelper.readCodeValue("CO", message), is(String.valueOf((int) position.bearing)));
        ZonedDateTime positionTime = ZonedDateTime.ofInstant(position.positionTime.toInstant(), ZoneId.of("UTC"));
        assertThat(NAFHelper.readCodeValue("DA", message), is(positionTime.format(DateTimeFormatter.ofPattern("yyyyMMdd"))));
        assertThat(NAFHelper.readCodeValue("TI", message), is(positionTime.format(DateTimeFormatter.ofPattern("HHmm"))));
    }

    @Test
    public void sendEntryReportToNorwayAndVerifyMandatoryFields() throws IOException, Exception {
        Organisation organisation = createOrganisationNorway();

        CustomRuleType flagStateRule = CustomRuleBuilder.getBuilder()
                .setName("Enter NOR => Send to NOR")
                .rule(CriteriaType.AREA, SubCriteriaType.AREA_CODE_VMS_ENT,
                        ConditionType.EQ, "NOR")
                .action(ActionType.SEND_ENTRY_REPORT, VMSSystemHelper.NAF_NAME, organisation.getName())
                .build();

        CustomRuleType createdCustomRule = CustomRuleHelper.createCustomRule(flagStateRule);
        assertNotNull(createdCustomRule);

        AssetDTO asset = AssetTestHelper.createTestAsset();
        LatLong swePosition = new LatLong(57.716673, 11.973996, Date.from(Instant.now().minusMillis(10 * 60 * 1000)));
        swePosition.speed = 5;
        LatLong norPosition = new LatLong(58.973, 5.781, Date.from(Instant.now()));
        norPosition.speed = 5;

        String message;
        try (NafEndpoint nafEndpoint = new NafEndpoint(ENDPOINT_PORT);
             TopicListener topicListener = new TopicListener(TopicListener.EVENT_STREAM, "event = 'Movement'")) {
            NAFHelper.sendPositionToNAFPlugin(swePosition, asset);
            topicListener.listenOnEventBus();
            NAFHelper.sendPositionToNAFPlugin(norPosition, asset);
            message = nafEndpoint.getMessage(10000);
        }

        assertThat(NAFHelper.readCodeValue("AD", message), is(organisation.getNation()));
        assertThat(NAFHelper.readCodeValue("FR", message), is("UNK"));
        assertThat(NAFHelper.readCodeValue("TM", message), is(MovementTypeType.ENT.toString()));
        assertThat(NAFHelper.readCodeValue("RC", message), is(asset.getIrcs()));
        assertThat(NAFHelper.readCodeDoubleValue("LT", message), is(norPosition.latitude));
        assertThat(NAFHelper.readCodeDoubleValue("LG", message), is(norPosition.longitude));
        assertThat(NAFHelper.readCodeValue("SP", message), is(String.valueOf((int) norPosition.speed * 10)));
        assertThat(NAFHelper.readCodeValue("CO", message), is(String.valueOf((int) norPosition.bearing)));
        ZonedDateTime positionTime = ZonedDateTime.ofInstant(norPosition.positionTime.toInstant(), ZoneId.of("UTC"));
        assertThat(NAFHelper.readCodeValue("DA", message), is(positionTime.format(DateTimeFormatter.ofPattern("yyyyMMdd"))));
        assertThat(NAFHelper.readCodeValue("TI", message), is(positionTime.format(DateTimeFormatter.ofPattern("HHmm"))));
    }

    @Test
    public void sendExitReportToNorwayAndVerifyMandatoryFields() throws IOException, Exception {
        Organisation organisation = createOrganisationNorway();

        CustomRuleType flagStateRule = CustomRuleBuilder.getBuilder()
                .setName("Exit NOR => Send to NOR")
                .rule(CriteriaType.AREA, SubCriteriaType.AREA_CODE_VMS_EXT,
                        ConditionType.EQ, "NOR")
                .action(ActionType.SEND_EXIT_REPORT, VMSSystemHelper.NAF_NAME, organisation.getName())
                .build();

        CustomRuleType createdCustomRule = CustomRuleHelper.createCustomRule(flagStateRule);
        assertNotNull(createdCustomRule);

        AssetDTO asset = AssetTestHelper.createTestAsset();
        LatLong norPosition = new LatLong(58.973, 5.781, Date.from(Instant.now().minusMillis(10 * 60 * 1000)));
        norPosition.speed = 5;
        LatLong swePosition = new LatLong(57.716673, 11.973996, Date.from(Instant.now()));
        swePosition.speed = 5;

        String message;
        try (NafEndpoint nafEndpoint = new NafEndpoint(ENDPOINT_PORT);
             TopicListener topicListener = new TopicListener(TopicListener.EVENT_STREAM, "event = 'Movement'")) {
            NAFHelper.sendPositionToNAFPlugin(norPosition, asset);
            topicListener.listenOnEventBus();
            NAFHelper.sendPositionToNAFPlugin(swePosition, asset);
            message = nafEndpoint.getMessage(10000);
        }

        assertThat(NAFHelper.readCodeValue("AD", message), is(organisation.getNation()));
        assertThat(NAFHelper.readCodeValue("FR", message), is("UNK"));
        assertThat(NAFHelper.readCodeValue("TM", message), is(MovementTypeType.EXI.toString()));
        assertThat(NAFHelper.readCodeValue("RC", message), is(asset.getIrcs()));
        assertThat(NAFHelper.isCodePresent("LT", message), is(false));
        assertThat(NAFHelper.isCodePresent("LG", message), is(false));
        assertThat(NAFHelper.isCodePresent("SP", message), is(false));
        assertThat(NAFHelper.isCodePresent("CO", message), is(false));
        ZonedDateTime positionTime = ZonedDateTime.ofInstant(swePosition.positionTime.toInstant(), ZoneId.of("UTC"));
        assertThat(NAFHelper.readCodeValue("DA", message), is(positionTime.format(DateTimeFormatter.ofPattern("yyyyMMdd"))));
        assertThat(NAFHelper.readCodeValue("TI", message), is(positionTime.format(DateTimeFormatter.ofPattern("HHmm"))));
    }

    @Test
    public void exitReportTest() throws Exception {
        AssetDTO asset = AssetTestHelper.createBasicAsset();
        asset.setFlagStateCode("NOR");
        asset = AssetTestHelper.createAsset(asset);

        try (TopicListener topicListener = new TopicListener(TopicListener.EVENT_STREAM, "event = 'Movement'")) {
            LatLong entryPosition = new LatLong(58.509, 10.212, Date.from(Instant.now().minus(4, ChronoUnit.HOURS).truncatedTo(ChronoUnit.MINUTES)));
            NAFHelper.sendEntryReportToNAFPlugin(entryPosition, asset);
            topicListener.listenOnEventBus();
            LatLong normalPosition1 = new LatLong(58.474, 10.455, Date.from(Instant.now().minus(3, ChronoUnit.HOURS).truncatedTo(ChronoUnit.MINUTES)));
            NAFHelper.sendPositionToNAFPlugin(normalPosition1, asset);
            topicListener.listenOnEventBus();
            LatLong normalPosition2 = new LatLong(58.594, 10.400, Date.from(Instant.now().minus(2, ChronoUnit.HOURS).truncatedTo(ChronoUnit.MINUTES)));
            NAFHelper.sendPositionToNAFPlugin(normalPosition2, asset);
            topicListener.listenOnEventBus();
            LatLong exitPosition = new LatLong(58.655, 10.344, Date.from(Instant.now().minus(1, ChronoUnit.HOURS).truncatedTo(ChronoUnit.MINUTES)));
            NAFHelper.sendExitReportToNAFPlugin(exitPosition, asset);
            topicListener.listenOnEventBus();

            MovementQuery query = MovementHelper.getBasicMovementQuery();
            ListCriteria criteria = new ListCriteria();
            criteria.setKey(SearchKey.CONNECT_ID);
            criteria.setValue(asset.getId().toString());
            query.getMovementSearchCriteria().add(criteria);

            List<MovementType> movements = MovementHelper.getListByQuery(query);

            assertThat(movements.size(), is(4));
            movements.sort(Comparator.comparing(MovementType::getPositionTime));
            assertThat(movements.get(0).getConnectId(), is(asset.getId().toString()));
            assertThat(movements.get(0).getPosition().getLongitude(), is(entryPosition.longitude));
            assertThat(movements.get(0).getPosition().getLatitude(), is(entryPosition.latitude));
            assertThat(movements.get(0).getPositionTime(), is(entryPosition.positionTime));

            assertThat(movements.get(1).getConnectId(), is(asset.getId().toString()));
            assertThat(movements.get(1).getPosition().getLongitude(), is(normalPosition1.longitude));
            assertThat(movements.get(1).getPosition().getLatitude(), is(normalPosition1.latitude));
            assertThat(movements.get(1).getPositionTime(), is(normalPosition1.positionTime));

            assertThat(movements.get(2).getConnectId(), is(asset.getId().toString()));
            assertThat(movements.get(2).getPosition().getLongitude(), is(normalPosition2.longitude));
            assertThat(movements.get(2).getPosition().getLatitude(), is(normalPosition2.latitude));
            assertThat(movements.get(2).getPositionTime(), is(normalPosition2.positionTime));

            assertThat(movements.get(3).getConnectId(), is(asset.getId().toString()));
            assertThat(movements.get(3).getPosition().getLongitude(), is(normalPosition2.longitude));
            assertThat(movements.get(3).getPosition().getLatitude(), is(normalPosition2.latitude));
            assertThat(movements.get(3).getPositionTime(), is(exitPosition.positionTime));
        }
    }

    @Test
    public void singleExitReportTest() throws Exception {
        Instant now = Instant.now();
        AssetDTO asset = AssetTestHelper.createBasicAsset();
        asset.setFlagStateCode("NOR");
        asset = AssetTestHelper.createAsset(asset);

        LatLong exitPosition = new LatLong(58.655, 10.344, Date.from(now.minus(1, ChronoUnit.HOURS).truncatedTo(ChronoUnit.MINUTES)));
        NAFHelper.sendExitReportToNAFPlugin(exitPosition, asset);
        SanityRuleHelper.pollAlarmReportCreated();
        AlarmReport alarm = SanityRuleHelper.getLatestOpenAlarmReportSince(now.atZone(ZoneId.of("UTC")));
        assertThat(alarm.getAssetGuid(), is(asset.getId().toString()));
        assertTrue(alarm.getAlarmItemList().stream().filter(a -> a.getRuleName().equals("VMS Exit report without previous VMS movement")).findAny().isPresent());
    }

    @Test
    public void incomingOrginalMessageTest() throws IOException, Exception {
        AssetDTO asset = AssetTestHelper.createTestAsset();
        CustomRuleType flagStateRule = CustomRuleBuilder.getBuilder()
                .rule(CriteriaType.ASSET, SubCriteriaType.ASSET_IRCS,
                        ConditionType.EQ, asset.getIrcs())
                .action(ActionType.SEND_REPORT, VMSSystemHelper.NAF_NAME, "TEST")
                .build();

        CustomRuleType createdCustomRule = CustomRuleHelper.createCustomRule(flagStateRule);
        assertNotNull(createdCustomRule);

        LatLong position = new LatLong(58.973, 5.781, Date.from(Instant.now()));
        position.speed = 5;

        try (NafEndpoint nafEndpoint = new NafEndpoint(ENDPOINT_PORT)) {
            NAFHelper.sendPositionToNAFPlugin(position, asset);
            nafEndpoint.getMessage(10000);
        }

        List<MovementDto> movements = MovementHelper.getLatestMovements(Arrays.asList(asset.getId().toString()));
        ExchangeLogDto exchangeLog = ExchangeHelper.getIncomingExchangeLogByTypeGUID(movements.get(0).getId().toString());
        String incomingMessage = ExchangeHelper.getExchangeLogMessage(exchangeLog.getId());
        assertThat(incomingMessage, CoreMatchers.startsWith("//SR//"));
        assertThat(incomingMessage, CoreMatchers.endsWith("//ER//"));
        assertThat(incomingMessage, CoreMatchers.containsString("//RC/" + asset.getIrcs()));
    }

    @Test
    public void outgoingOriginalMessageTest() throws IOException, Exception {
        AssetDTO asset = AssetTestHelper.createTestAsset();
        CustomRuleType flagStateRule = CustomRuleBuilder.getBuilder()
                .rule(CriteriaType.ASSET, SubCriteriaType.ASSET_IRCS,
                        ConditionType.EQ, asset.getIrcs())
                .action(ActionType.SEND_REPORT, VMSSystemHelper.NAF_NAME, "TEST")
                .build();

        CustomRuleType createdCustomRule = CustomRuleHelper.createCustomRule(flagStateRule);
        assertNotNull(createdCustomRule);

        LatLong position = new LatLong(58.973, 5.781, Date.from(Instant.now()));
        position.speed = 5;

        try (NafEndpoint nafEndpoint = new NafEndpoint(ENDPOINT_PORT)) {
            NAFHelper.sendPositionToNAFPlugin(position, asset);
            nafEndpoint.getMessage(10000);
        }

        List<MovementDto> movements = MovementHelper.getLatestMovements(Arrays.asList(asset.getId().toString()));
        ExchangeLogDto exchangeLog = ExchangeHelper.getOutgoingExchangeLogByTypeGUID(movements.get(0).getId().toString());
        String outgoingMessage = ExchangeHelper.getExchangeLogMessage(exchangeLog.getId());
        assertThat(outgoingMessage, CoreMatchers.startsWith("//SR//"));
        assertThat(outgoingMessage, CoreMatchers.endsWith("//ER//"));
        assertThat(outgoingMessage, CoreMatchers.containsString("//RC/" + asset.getIrcs()));
    }

    private Organisation createOrganisationNorway() throws SocketException {
        Organisation organisation = UserHelper.getBasicOrganisation();
        organisation.setNation("NOR");
        UserHelper.createOrganisation(organisation);
        EndPoint endpoint = new EndPoint();
        endpoint.setName("NAF");
        endpoint.setUri("http://" + getDockerHostIp() + ":" + ENDPOINT_PORT + "/naf/message/#MESSAGE#");
        endpoint.setStatus("E");
        endpoint.setOrganisationName(organisation.getName());
        EndPoint createdEndpoint = UserHelper.createEndpoint(endpoint);
        Channel channel = new Channel();
        channel.setDataflow("NAF");
        channel.setService("NAF");
        channel.setPriority(1);
        channel.setEndpointId(createdEndpoint.getEndpointId());
        UserHelper.createChannel(channel);
        return organisation;
    }

    // Find docker host machine ip. Replace this with 'host.docker.internal' when supported on Linux.
    private String getDockerHostIp() throws SocketException {
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            NetworkInterface e = interfaces.nextElement();
            Enumeration<InetAddress> inetAddresses = e.getInetAddresses();
            while (inetAddresses.hasMoreElements()) {
                InetAddress inetAddress = inetAddresses.nextElement();
                if (inetAddress.getHostAddress().startsWith("172")) {
                    return inetAddress.getHostAddress();
                }
            }
        }
        return "host.docker.internal";
    }
}
