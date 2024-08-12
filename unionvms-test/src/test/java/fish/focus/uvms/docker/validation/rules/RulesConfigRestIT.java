/*

Developed with the contribution of the European Commission - Directorate General for Maritime Affairs and Fisheries
� European Union, 2017.

This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can
redistribute it and/or modify it under the terms of the GNU General Public License as published by the
Free Software Foundation, either version 3 of the License, or any later version. The IFDM Suite is distributed in
the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details. You should have received a
copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.

*/
package fish.focus.uvms.docker.validation.rules;

import fish.focus.schema.movementrules.ticket.v1.TicketStatusType;
import fish.focus.uvms.docker.validation.common.AbstractRest;
import org.junit.Test;

import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class RulesConfigRestIT extends AbstractRest {

    @Test
    public void getConfigTest() {
        Response response = getWebTarget()
                .path("movement-rules/rest/config")
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getValidJwtToken())
                .get();

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        Map data = response.readEntity(Map.class);
        assertFalse(data.isEmpty());
    }

    @Test
    public void getTicketStatusesTest() {
        List<TicketStatusType> response = getWebTarget()
                .path("movement-rules/rest/config")
                .path("ticketstatus")
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getValidJwtToken())
                .get(new GenericType<List<TicketStatusType>>() {
                });

        assertFalse(response.isEmpty());
    }
}
