package fish.focus.uvms.docker.validation.activity;

import java.util.List;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import fish.focus.uvms.activity.service.dto.FishingActivityReportDTO;
import fish.focus.uvms.activity.service.search.FishingActivityQueryWithStringMaps;
import fish.focus.uvms.commons.rest.dto.PaginatedResponse;
import fish.focus.uvms.docker.validation.common.AbstractHelper;

public class ActivityTestHelper extends AbstractHelper {

    private ActivityTestHelper() {}

    public static List<FishingActivityReportDTO> getFaList(FishingActivityQueryWithStringMaps query) {
        PaginatedResponse<FishingActivityReportDTO> response = getWebTarget()
            .path("activity/rest/fa/list")
            .request(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, getValidJwtToken("vms_admin_com", "password"))
            .header("roleName", "AdminAll")
            .header("scopeName", "All Reports")
            .post(Entity.json(query), new GenericType<PaginatedResponse<FishingActivityReportDTO>>() {});
        return response.getResultList();

    }
}
