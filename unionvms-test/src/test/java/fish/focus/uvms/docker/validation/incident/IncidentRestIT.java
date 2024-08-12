package fish.focus.uvms.docker.validation.incident;

import fish.focus.schema.movementrules.ticket.v1.TicketStatusType;
import fish.focus.uvms.asset.client.model.AssetDTO;
import fish.focus.uvms.docker.validation.asset.AssetTestHelper;
import fish.focus.uvms.docker.validation.common.AbstractRest;
import fish.focus.uvms.docker.validation.common.TopicListener;
import fish.focus.uvms.docker.validation.config.ConfigRestHelper;
import fish.focus.uvms.docker.validation.movement.LatLong;
import fish.focus.uvms.docker.validation.movement.MovementHelper;
import fish.focus.uvms.docker.validation.movement.model.IncomingMovement;
import fish.focus.uvms.docker.validation.system.helper.FLUXHelper;
import fish.focus.uvms.incident.model.dto.IncidentDto;
import fish.focus.uvms.incident.model.dto.IncidentLogDto;
import fish.focus.uvms.incident.model.dto.IncidentTicketDto;
import fish.focus.uvms.incident.model.dto.OpenAndRecentlyResolvedIncidentsDto;
import fish.focus.uvms.incident.model.dto.enums.MovementSourceType;
import fish.focus.uvms.incident.model.dto.enums.StatusEnum;
import fish.focus.uvms.movement.model.dto.MovementDto;
import org.junit.Test;

import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

public class IncidentRestIT extends AbstractRest {

    @Test
    public void createAssetNotSendingIncidentTest() throws Exception {
        AssetDTO asset = AssetTestHelper.createAsset(AssetTestHelper.createBasicAsset());
        IncidentTicketDto ticket = IncidentTestHelper.createTicket(asset.getId());
        IncidentDto dto = IncidentTestHelper.createAssetNotSendingIncident(ticket, IncidentTestHelper.INCIDENT_CREATE_EVENT);
        assertNotNull(dto);
    }

    @Test
    public void getAssetNotSendingListTest() throws Exception {
        OpenAndRecentlyResolvedIncidentsDto before = IncidentTestHelper.getAllOpenAndRecentlyResolvedIncidents();
        AssetDTO asset = AssetTestHelper.createAsset(AssetTestHelper.createBasicAsset());
        IncidentTicketDto ticket = IncidentTestHelper.createTicket(asset.getId());
        IncidentTestHelper.createAssetNotSendingIncident(ticket, IncidentTestHelper.INCIDENT_CREATE_EVENT);
        OpenAndRecentlyResolvedIncidentsDto after = IncidentTestHelper.getAllOpenAndRecentlyResolvedIncidents();
        assertEquals(before.getUnresolved().size() + 1, after.getUnresolved().size());
    }

    @Test
    public void updateAssetNotSendingStatusTest() throws Exception {
        AssetDTO asset = AssetTestHelper.createAsset(AssetTestHelper.createBasicAsset());
        IncidentTicketDto ticket = IncidentTestHelper.createTicket(asset.getId());
        IncidentDto created = IncidentTestHelper.createAssetNotSendingIncident(ticket, IncidentTestHelper.INCIDENT_CREATE_EVENT);

        assertNotEquals(StatusEnum.RESOLVED, created.getStatus());

        ticket.setType(null);
        ticket.setMovementId(UUID.randomUUID().toString());
        ticket.setMovementSource(MovementSourceType.FLUX);
        IncidentDto updated = IncidentTestHelper.createAssetNotSendingIncident(ticket, IncidentTestHelper.INCIDENT_UPDATE_EVENT);

        assertEquals(StatusEnum.RESOLVED, updated.getStatus());
    }

    @Test
    public void getAssetNotSendingEventChangesTest() throws Exception {
        AssetDTO asset = AssetTestHelper.createAsset(AssetTestHelper.createBasicAsset());
        IncidentTicketDto ticket = IncidentTestHelper.createTicket(asset.getId());
        IncidentDto incident = IncidentTestHelper.createAssetNotSendingIncident(ticket, IncidentTestHelper.INCIDENT_CREATE_EVENT);

        ticket.setStatus(TicketStatusType.CLOSED.toString());
        IncidentTestHelper.createAssetNotSendingIncident(ticket, IncidentTestHelper.INCIDENT_UPDATE_EVENT);

        Map<Long, IncidentLogDto> dtoList = getWebTarget()
                .path("incident/rest/incident/incidentLogForIncident")
                .path(String.valueOf(incident.getId()))
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getValidJwtToken())
                .get(new GenericType<Map<Long, IncidentLogDto>>() {
                });

        assertTrue(dtoList.size() > 0);
    }


    @Test
    public void movementReceivedWithAssetInParkedStatusButNoIncident() throws Exception {

        //Fix FlagState
        ConfigRestHelper.setLocalFlagStateToSwe();

        //Actual test
        AssetDTO basicAsset = AssetTestHelper.createBasicAsset();
        basicAsset.setParked(true);
        AssetDTO asset = AssetTestHelper.createAsset(basicAsset);

        try (TopicListener topicListener = new TopicListener(TopicListener.EVENT_STREAM, "event = 'Movement'")) {
            FLUXHelper.sendPositionToFluxPlugin(asset,
                    new LatLong(56d, 11d, new Date(System.currentTimeMillis() - 10000)));
            topicListener.listenOnEventBus();
        }
        Thread.sleep(1000);

        Map<String, IncidentDto> openTicketsForAsset = IncidentTestHelper.getOpenTicketsForAsset(asset.getId().toString());

        assertEquals(0, openTicketsForAsset.size());
    }

    @Test
    public void getIncidentsForAssetIdTest() throws Exception {
        AssetDTO asset = AssetTestHelper.createAsset(AssetTestHelper.createBasicAsset());
        MovementDto movement = null;
        try (MovementHelper movementHelper = new MovementHelper()) {
            IncomingMovement incomingMovement = movementHelper.createIncomingMovement(asset, new LatLong(56d, 11d, Date.from(Instant.now().minus(1, ChronoUnit.HOURS))));
            movement = movementHelper.createMovement(incomingMovement);
        }
        IncidentTicketDto ticket = IncidentTestHelper.createTicket(asset.getId());
        ticket.setMovementId(movement.getId().toString());
        IncidentDto incident = IncidentTestHelper.createAssetNotSendingIncident(ticket, IncidentTestHelper.INCIDENT_CREATE_EVENT);

        Map<String, IncidentDto> incidentMap = IncidentTestHelper.getOpenTicketsForAsset(asset.getId().toString());

        assertTrue(incidentMap.size() > 0);
        assertEquals(asset.getId(), incidentMap.get(incident.getId().toString()).getAssetId());
        assertEquals(movement.getId(), incidentMap.get(incident.getId().toString()).getLastKnownLocation().getId());
    }
}
