package fish.focus.uvms.docker.validation.webgateway;

import fish.focus.uvms.asset.client.model.AssetDTO;
import fish.focus.uvms.asset.remote.dto.search.SearchBranch;
import fish.focus.uvms.asset.remote.dto.search.SearchFields;
import fish.focus.uvms.docker.validation.asset.AssetJMSHelper;
import fish.focus.uvms.docker.validation.asset.AssetTestHelper;
import fish.focus.uvms.docker.validation.common.AbstractRest;
import fish.focus.uvms.docker.validation.movement.LatLong;
import fish.focus.uvms.docker.validation.movement.MovementHelper;
import fish.focus.uvms.docker.validation.movement.model.IncomingMovement;
import fish.focus.uvms.docker.validation.webgateway.dto.TracksByAssetSearchRequestDto;
import fish.focus.uvms.movement.model.dto.MovementDto;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.jms.JMSException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.util.Date;

public class ReportIT extends AbstractRest {

    private static MovementHelper movementHelper;
    private static AssetJMSHelper jmsHelper;

    @BeforeClass
    public static void setup() throws JMSException {
        movementHelper = new MovementHelper();
        jmsHelper = new AssetJMSHelper();
    }

    @Test
    public void getAssetListTest() throws Exception {
        AssetDTO testAsset = AssetTestHelper.createTestAsset();

        LatLong latLong = new LatLong(16.9, 32.6333333, new Date());
        IncomingMovement createMovementRequest = movementHelper.createIncomingMovement(testAsset, latLong);
        MovementDto createMovementResponse = movementHelper.createMovement(createMovementRequest);

        SearchBranch assetQuery = AssetTestHelper.getBasicAssetSearchBranch();
        assetQuery.addNewSearchLeaf(SearchFields.GUID, testAsset.getId().toString());

        TracksByAssetSearchRequestDto report1 = new TracksByAssetSearchRequestDto();
        report1.setAssetQuery(assetQuery);

        Response response = getWebTarget()
                .path("web-gateway/rest/reports/tracksByAssetSearch")
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getValidJwtToken())
                .post(Entity.json(report1), Response.class);
        assertEquals(200, response.getStatus());

        String returnString = response.readEntity(String.class);
        assertTrue(returnString.contains(testAsset.getId().toString()));
        assertTrue(returnString.contains(createMovementResponse.getId().toString()));
    }



}
