package fish.focus.uvms.docker.validation.webgateway;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

import org.hamcrest.CoreMatchers;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import fish.focus.uvms.docker.validation.common.AbstractRest;
import fish.focus.uvms.docker.validation.common.MessageHelper;
import fish.focus.uvms.docker.validation.movement.AuthorizationHeaderWebTarget;
import fish.focus.uvms.docker.validation.movement.MovementHelper;
import fish.focus.uvms.docker.validation.rules.CustomRulesTestHelper;

import javax.jms.JMSException;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.sse.SseEventSource;
import java.util.ArrayList;
import java.util.List;

public class WebGatewayIT extends AbstractRest {

    private static MovementHelper movementHelper;
    private static MessageHelper messageHelper;
    private static String eventStream = "";
    private static String eventNameStream = "";

    @BeforeClass
    public static void setup() throws JMSException {
        movementHelper = new MovementHelper();
        messageHelper = new MessageHelper();
    }

    @AfterClass
    public static void cleanup() {
        movementHelper.close();
        messageHelper.close();
    }

    public static SseEventSource getSseStream() {
        WebTarget target = getWebTarget().path("web-gateway/rest/sse/subscribe");
        AuthorizationHeaderWebTarget jwtTarget = new AuthorizationHeaderWebTarget(target, getValidJwtToken());
        return SseEventSource.
                target(jwtTarget).build();
    }

    @Before
    public void init() {
        eventStream = "";
        eventNameStream = "";
    }

    @Test(timeout = 25000)
    public void sseTest() throws Exception {
        List<String> sseMessages = new ArrayList<>();
        String movementGuid;

        try (SseEventSource source = getSseStream()) {
            source.register((inboundSseEvent) -> {
                sseMessages.add("Name: " + (inboundSseEvent.getName() == null ? null : inboundSseEvent.getName()) + " Data: " + inboundSseEvent.readData());
            });
            source.open();


            movementGuid = CustomRulesTestHelper.createRuleAndGetMovementGuid();


            while (sseMessages.size() < 5) {         // 2 welcoming messages and then 1 for creating the asset, 1 for creating the movement and then lastly one for creating the ticket
                Thread.sleep(100);
            }
        }
        assertThat(sseMessages.size(), CoreMatchers.is(5));
        assertTrue(sseMessages.stream().anyMatch(s -> s.contains("Updated Asset")));
        assertTrue(sseMessages.stream().anyMatch(s -> s.contains("Movement") && s.contains(movementGuid)));
        assertTrue(sseMessages.stream().anyMatch(s -> s.contains("Ticket") && s.contains(movementGuid)));
    }


}
