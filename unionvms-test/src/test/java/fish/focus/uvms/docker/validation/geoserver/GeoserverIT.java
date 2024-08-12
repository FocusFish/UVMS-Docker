package fish.focus.uvms.docker.validation.geoserver;

import fish.focus.uvms.docker.validation.common.AbstractRest;
import org.junit.Test;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.Response.Status.*;
import static org.junit.Assert.assertEquals;

public class GeoserverIT extends AbstractRest {

    @Test
    public void verifyProtectedUvmsLayersTest() {
        Response response = geoServerWebTarget()
                .path("uvms/wms")
                .queryParam("service", "WMS")
                .queryParam("version", "1.1.0")
                .queryParam("request", "GetMap")
                .queryParam("layers", "uvms:port")
                .queryParam("styles", "")
                .queryParam("bbox", "-180.0,-90.0,180.0,90.0")
                .queryParam("width", "768")
                .queryParam("height", "384")
                .queryParam("srs", "EPSG:4326")
                .queryParam("format", "application/openlayers")
                .request(MediaType.APPLICATION_JSON)
                .get();

        assertEquals(FORBIDDEN.getStatusCode(), response.getStatus());
    }

    @Test
    public void verifyProtectedUvmsLayersAccessWithJwtHeaderTest() {
        Response response = geoServerWebTarget()
                .path("uvms/wms")
                .queryParam("service", "WMS")
                .queryParam("version", "1.1.0")
                .queryParam("request", "GetMap")
                .queryParam("layers", "uvms:port")
                .queryParam("styles", "")
                .queryParam("bbox", "-180.0,-90.0,180.0,90.0")
                .queryParam("width", "768")
                .queryParam("height", "384")
                .queryParam("srs", "EPSG:4326")
                .queryParam("format", "application/openlayers")
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getValidJwtToken())
                .get();

        assertEquals(OK.getStatusCode(), response.getStatus());
        assertEquals("SAMEORIGIN", response.getHeaderString("X-Frame-Options"));
    }

    @Test
    public void verifyAccessControlAllowOriginTest() {
        Response response = geoServerWebTarget()
                .path("web/")
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getValidJwtToken())
                .header("Origin", "http://www.example.com")
                .get();

        if (response.getStatus() == FOUND.getStatusCode()) {
            // From 2.25 local and CI differs in responses.
            // (or possibly earlier since > 2.19.6 were never tested until 2.25 was introduced)
            // Locally, the first request returns 302 while on CI it returns 200.
            var request = ClientBuilder.newClient()
                    .target(response.getLocation())
                    .request(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, getValidJwtToken())
                    .header("Origin", "http://www.example.com");

            for (var cookie : response.getCookies().values()) {
                request.cookie(cookie);
            }

            response = request.get();
        }

        assertEquals(OK.getStatusCode(), response.getStatus());
        assertEquals("SAMEORIGIN", response.getHeaderString("X-Frame-Options"));
        assertEquals("http://www.example.com", response.getHeaderString("Access-Control-Allow-Origin"));
        assertEquals("true", response.getHeaderString("Access-Control-Allow-Credentials"));
    }

    @Test
    public void verifyOptionsAllowTest() {
        Response response = geoServerWebTarget()
                .path("web")
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getValidJwtToken())
                .header("Origin", "http://www.example.com")
                .header("Access-Control-Request-Method", "POST")
                .header("Access-Control-Request-Headers", "X-Requested-With,Content-Type,Accept,Authorization")
                .options();

        assertEquals(OK.getStatusCode(), response.getStatus());
        assertEquals("http://www.example.com", response.getHeaderString("Access-Control-Allow-Origin"));
        assertEquals("true", response.getHeaderString("Access-Control-Allow-Credentials"));
        assertEquals("GET,POST,PUT,DELETE,HEAD,OPTIONS", response.getHeaderString("Access-Control-Allow-Methods"));
        assertEquals("X-Requested-With,Content-Type,Accept,Authorization", response.getHeaderString("Access-Control-Allow-Headers"));
    }

    private WebTarget geoServerWebTarget() {
        Client client = ClientBuilder.newClient();
        return client.target("http://localhost:28080/geoserver/");
    }
}
