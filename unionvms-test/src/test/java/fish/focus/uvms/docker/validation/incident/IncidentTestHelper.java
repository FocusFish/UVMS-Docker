package fish.focus.uvms.docker.validation.incident;

import fish.focus.schema.movementrules.ticket.v1.TicketStatusType;
import fish.focus.uvms.docker.validation.common.AbstractHelper;
import fish.focus.uvms.docker.validation.common.MessageHelper;
import fish.focus.uvms.incident.model.dto.IncidentDto;
import fish.focus.uvms.incident.model.dto.IncidentTicketDto;
import fish.focus.uvms.incident.model.dto.OpenAndRecentlyResolvedIncidentsDto;
import fish.focus.uvms.incident.model.dto.enums.IncidentType;

import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.locks.LockSupport;

public class IncidentTestHelper extends AbstractHelper {

    public static final String QUEUE_NAME = "IncidentEvent";
    public static final String INCIDENT_CREATE_EVENT = "Incident";
    public static final String INCIDENT_UPDATE_EVENT = "IncidentUpdate";

    public static IncidentDto createAssetNotSendingIncident(IncidentTicketDto ticket, String eventName) throws Exception {
        sendMessage(ticket, eventName);
        LockSupport.parkNanos(5000000000L);

        return getWebTarget()
                .path("incident/rest/incident/byTicketId")
                .path(ticket.getId().toString())
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getValidJwtToken())
                .get(IncidentDto.class);
    }

    public static OpenAndRecentlyResolvedIncidentsDto getAllOpenAndRecentlyResolvedIncidents() {
        return getWebTarget()
                .path("incident/rest/incident/allOpenIncidents")
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getValidJwtToken())
                .get(OpenAndRecentlyResolvedIncidentsDto.class);
    }

    public static void sendMessage(IncidentTicketDto ticket, String eventName) throws Exception {
        try (MessageHelper messageHelper = new MessageHelper()) {
            String asString = writeValueAsString(ticket);
            messageHelper.sendMessageWithProperty(QUEUE_NAME, asString, eventName);
        }
    }

    public static IncidentTicketDto createTicket(UUID assetId) {
        IncidentTicketDto ticket = new IncidentTicketDto();
        ticket.setId(UUID.randomUUID());
        ticket.setType(IncidentType.ASSET_NOT_SENDING);
        ticket.setAssetId(assetId.toString());
        ticket.setMovementId(UUID.randomUUID().toString());
        ticket.setMobTermId(UUID.randomUUID().toString());
        ticket.setPollId(UUID.randomUUID().toString());
        ticket.setRuleName("Asset not sending");
        ticket.setRuleGuid("Asset not sending");
        ticket.setUpdatedBy("UVMS");
        ticket.setStatus(TicketStatusType.POLL_PENDING.value());
        ticket.setTicketCount(1L);
        ticket.setCreatedDate(Instant.now());
        ticket.setUpdated(Instant.now());
        return ticket;
    }

    public static Map<String, IncidentDto> getOpenTicketsForAsset(String assetId) {
        return getWebTarget()
                .path("incident/rest/incident/incidentsForAssetId")
                .path(assetId)
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getValidJwtToken())
                .get(new GenericType<HashMap<String, IncidentDto>>() {
                });
    }
}
