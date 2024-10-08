package fish.focus.uvms.docker.validation.spatial;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.junit.Ignore;
import org.junit.Test;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import fish.focus.schema.movement.v1.MovementPoint;
import fish.focus.uvms.docker.validation.common.AbstractRest;
import fish.focus.uvms.docker.validation.spatial.dto.AreaExtendedIdentifierType;
import fish.focus.uvms.docker.validation.spatial.dto.InputToSegmentCategoryType;
import fish.focus.uvms.spatial.model.exception.SpatialModelMarshallException;
import fish.focus.uvms.spatial.model.schemas.AllAreaTypesRequest;
import fish.focus.uvms.spatial.model.schemas.Area;
import fish.focus.uvms.spatial.model.schemas.AreaByCodeRequest;
import fish.focus.uvms.spatial.model.schemas.AreaByCodeResponse;
import fish.focus.uvms.spatial.model.schemas.AreaByLocationSpatialRQ;
import fish.focus.uvms.spatial.model.schemas.AreaSimpleType;
import fish.focus.uvms.spatial.model.schemas.AreaType;
import fish.focus.uvms.spatial.model.schemas.ClosestAreaSpatialRQ;
import fish.focus.uvms.spatial.model.schemas.ClosestLocationSpatialRQ;
import fish.focus.uvms.spatial.model.schemas.GeometryByPortCodeRequest;
import fish.focus.uvms.spatial.model.schemas.GeometryByPortCodeResponse;
import fish.focus.uvms.spatial.model.schemas.Location;
import fish.focus.uvms.spatial.model.schemas.LocationType;
import fish.focus.uvms.spatial.model.schemas.PingRQ;
import fish.focus.uvms.spatial.model.schemas.PingRS;
import fish.focus.uvms.spatial.model.schemas.PointType;
import fish.focus.uvms.spatial.model.schemas.SpatialEnrichmentRQ;
import fish.focus.uvms.spatial.model.schemas.SpatialEnrichmentRS;
import fish.focus.uvms.spatial.model.schemas.SpatialModuleMethod;
import fish.focus.uvms.spatial.model.schemas.UnitType;

public class SpatialRestIT extends AbstractRest {
    private Integer crs = 4326;
    private Double latitude = 57.715523;
    private Double longitude = 11.973965;

    private static Geometry getGeometryFromWKTSrring(String wkt) {
        try {
            WKTReader reader = new WKTReader();
            Geometry geom = reader.read(wkt);
            geom.setSRID(4326);
            return geom;
        } catch (ParseException e) {
            throw new IllegalArgumentException("Inputstring " + wkt + " causes a parse exception.", e);
        }
    }

    @Test
    public void getAreaByLocation() {
        PointType point = new PointType();
        point.setLatitude(latitude);
        point.setLongitude(longitude);
        point.setCrs(crs);

        AreaByLocationSpatialRQ areaByLocationSpatialRQ = new AreaByLocationSpatialRQ();
        areaByLocationSpatialRQ.setPoint(point);
        areaByLocationSpatialRQ.setMethod(SpatialModuleMethod.GET_AREA_BY_LOCATION);

        Response ret = getWebTarget()
                .path("spatial/spatialnonsecure/json/getAreaByLocation")
                .request(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .post(Entity.json(areaByLocationSpatialRQ), Response.class);

        assertEquals(200, ret.getStatus());

        List<AreaExtendedIdentifierType> list = ret.readEntity(new GenericType<List<AreaExtendedIdentifierType>>() {
        });
        List<String> control = list.stream().map(AreaExtendedIdentifierType::getName).collect(Collectors.toList());
        assertTrue(control.contains("Göteborg-Lundbyhamnen"));
    }

    @Test
    public void getAreaTypes() {
        PointType point = new PointType();
        point.setLatitude(latitude);
        point.setLongitude(longitude);
        point.setCrs(crs);
        AllAreaTypesRequest request = createAllAreaTypesRequest();

        Response ret = getWebTarget()
                .path("spatial/spatialnonsecure/json/getAreaTypes")
                .request(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .post(Entity.json(request), Response.class);

        assertEquals(200, ret.getStatus());

        List<String> list = ret.readEntity(new GenericType<List<String>>() {
        });
        assertTrue(list.contains("EEZ"));
    }

    @Test
    @Ignore
    public void getClosestAreaWith4kPoints() throws Exception {
        double[] points = TestPoints.testpoints;
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 8000; i = i + 2) {
            PointType point = new PointType();
            point.setLatitude(points[i]);
            point.setLongitude(points[i + 1]);
            point.setCrs(crs);
            ClosestAreaSpatialRQ request = createClosestAreaRequest(point,
                    UnitType.METERS, Collections.singletonList(AreaType.EEZ));

            Response ret = getWebTarget()
                    .path("spatial/spatialnonsecure/json/getClosestArea")
                    .request(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .post(Entity.json(request), Response.class);

            assertEquals(200, ret.getStatus());
            List<Area> list = ret.readEntity(new GenericType<List<Area>>() {
            });

            builder.append("Latitude: " + point.getLatitude() + " Longitude: " + point.getLongitude());
            for (Area aeit : list) {
                builder.append(" " + aeit.getAreaType().value() + ": " + aeit.getCode());
            }
            builder.append("\r\n");

            if (i % 100 == 0) {
                System.out.println("Number: " + i);
            }
        }
        System.out.println(builder.toString());
    }

    @Test
    public void getClosestLocation() throws Exception {
        PointType point = new PointType();
        point.setLatitude(latitude);
        point.setLongitude(longitude);
        point.setCrs(crs);
        ClosestLocationSpatialRQ request = createClosestLocationRequest(point,
                UnitType.METERS, Collections.singletonList(LocationType.PORT));

        Response ret = getWebTarget()
                .path("spatial/spatialnonsecure/json/getClosestLocation")
                .request(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .post(Entity.json(request), Response.class);

        assertEquals(200, ret.getStatus());

        String json = ret.readEntity(String.class);
        assertTrue(json.contains("Göteborg-Ringökajen"));
    }

    @Test
    public void getEnrichment() throws Exception {
        PointType point = new PointType();
        // This magical int (4326) is the World Geodetic System 1984, aka EPSG:4326.
        // See: https://en.wikipedia.org/wiki/World_Geodetic_System or http://spatialreference.org/ref/epsg/wgs-84/
        point.setCrs(4326);
        point.setLatitude(latitude);
        point.setLongitude(longitude);

        List<LocationType> locationTypes = Collections.singletonList(LocationType.PORT);
        List<AreaType> areaTypes = Collections.singletonList(AreaType.COUNTRY);
        SpatialEnrichmentRQ request = createSpatialEnrichmentRequest(point, UnitType.NAUTICAL_MILES, locationTypes, areaTypes);

        Response ret = getWebTarget()
                .path("spatial/spatialnonsecure/json/getEnrichment")
                .request(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .post(Entity.json(request), Response.class);

        assertEquals(200, ret.getStatus());
        SpatialEnrichmentRS enrichmentRS = ret.readEntity(new GenericType<SpatialEnrichmentRS>() {
        });

        List<Location> list = enrichmentRS.getClosestLocations().getClosestLocations();
        List<String> control = list.stream().map(Location::getName).collect(Collectors.toList());
        assertTrue(control.contains("Göteborg-Ringökajen"));
    }

    @Test
    public void ping() {
        PingRQ request = new PingRQ();

        Response response = getWebTarget()
                .path("spatial/spatialnonsecure/json/ping")
                .request(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .post(Entity.json(request), Response.class);

        PingRS pingRS = response.readEntity(new GenericType<PingRS>() {
        });
        assertEquals("pong", pingRS.getResponse());
    }

    @Test
    public void getAreaByCode() {
        AreaByCodeRequest request = createAreaByCodeRequest();

        Response ret = getWebTarget()
                .path("spatial/spatialnonsecure/json/getAreaByCode")
                .request(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .post(Entity.json(request), Response.class);

        assertEquals(200, ret.getStatus());

        AreaByCodeResponse response = ret.readEntity(new GenericType<AreaByCodeResponse>() {
        });

        List<AreaSimpleType> resultList = response.getAreaSimples();
        assertNotNull(resultList);
        assertTrue(resultList.size() > 0);

        AreaSimpleType line = resultList.get(0);
        String wkt = line.getWkt();
        assertNotNull(wkt);
        assertTrue(wkt.contains("POLYGON"));
    }

    @Test
    public void getGeometryByPortCode() throws Exception {
        GeometryByPortCodeRequest request = createToGeometryByPortCodeRequest("AOLAD");

        Response ret = getWebTarget()
                .path("spatial/spatialnonsecure/json/getGeometryByPortCode")
                .request(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .post(Entity.json(request), Response.class);

        assertEquals(200, ret.getStatus());
        GeometryByPortCodeResponse rs = ret.readEntity(new GenericType<GeometryByPortCodeResponse>() {
        });
        String geometry = rs.getPortGeometry();
        assertNotNull(geometry);
        assertTrue(geometry.contains("MULTIPOINT"));
    }

    @Test
    public void getEnrichmentMulti() throws Exception {
        PointType point = new PointType();
        point.setCrs(4326);
        point.setLatitude(latitude);
        point.setLongitude(longitude);

        List<LocationType> locationTypes = Collections.singletonList(LocationType.PORT);
        List<AreaType> areaTypes = Collections.singletonList(AreaType.COUNTRY);

        List<SpatialEnrichmentRQ> requests = new ArrayList<>();
        int batchSize = 10;
        for (int i = 0; i < batchSize; i++) {
            Double lat = point.getLatitude();
            lat = lat + 0.001;
            point.setLatitude(lat);
            SpatialEnrichmentRQ request = createSpatialEnrichmentRequest(point, UnitType.NAUTICAL_MILES, locationTypes, areaTypes);
            requests.add(request);
        }

        for (int i = 0; i < batchSize; i++) {
            SpatialEnrichmentRQ request = requests.get(i);
            Response ret = getWebTarget()
                    .path("spatial/spatialnonsecure/json/getEnrichment")
                    .request(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .post(Entity.json(request), Response.class);

            assertEquals(200, ret.getStatus());
        }
    }

    @Test
    public void testSegmentCategoriFromHelsingborgToLidkoping() {
        List<InputToSegmentCategoryType> movementList = new ArrayList<>();
        String[] pointArray = HelsingborgToLidkoping.helsingborgToLidköping;
        Instant start = Instant.now().minusSeconds(60 * pointArray.length);

        for (int i = 0; i < pointArray.length - 1; i++) {
            movementList.clear();

            InputToSegmentCategoryType move = new InputToSegmentCategoryType();
            Point p = (Point) getGeometryFromWKTSrring(pointArray[i]);
            MovementPoint pos = new MovementPoint();
            pos.setLongitude(p.getX());
            pos.setLatitude(p.getY());
            move.setPosition(pos);
            move.setPositionTime(Date.from(start.plusSeconds(i * 60)));

            movementList.add(move);

            move = new InputToSegmentCategoryType();
            p = (Point) getGeometryFromWKTSrring(pointArray[i + 1]);
            pos = new MovementPoint();
            pos.setLongitude(p.getX());
            pos.setLatitude(p.getY());
            move.setPosition(pos);
            move.setPositionTime(Date.from(start.plusSeconds((i + 1) * 60)));

            movementList.add(move);

            Response response = getWebTarget()
                    .path("spatial/spatialnonsecure/json/getSegmentCategoryType")
                    .request(MediaType.APPLICATION_JSON)
                    .post(Entity.json(movementList), Response.class);

            assertEquals(200, response.getStatus());
        }
    }

    @Test
    public void getAllNonUserAreasTest() throws Exception {
        Response response = getWebTarget()
                .path("spatial/rest/area/allNonUserAreas")
                .request(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getValidJwtToken())
                .get(Response.class);

        //System.out.println(response.readEntity(String.class));
        assertEquals(200, response.getStatus());
    }

    @Test
    public void getAllLayersTest() throws Exception {
        Response response = getWebTarget()
                .path("spatial/rest/area/layers")
                .request(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getValidJwtToken())
                .get(Response.class);

        //System.out.println(response.readEntity(String.class));
        assertEquals(200, response.getStatus());
    }

    @Test
    public void getAAreaTypesAreasTest() throws Exception {
        Response response = getWebTarget()
                .path("spatial/rest/area/getAreaLayer/EEZ")
                .request(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getValidJwtToken())
                .get(Response.class);

        //System.out.println(response.readEntity(String.class));
        assertEquals(200, response.getStatus());
    }

    private AreaByCodeRequest createAreaByCodeRequest() {

        AreaByCodeRequest request = new AreaByCodeRequest();
        List<AreaSimpleType> list = new ArrayList<>();
        AreaSimpleType ast = new AreaSimpleType();

        ast.setAreaCode("DOM");
        ast.setAreaType("eez");
        //ast.setWkt("");

        list.add(ast);
        request.setAreaSimples(list);

        return request;
    }

    private AllAreaTypesRequest createAllAreaTypesRequest() {
        AllAreaTypesRequest allAreaTypesRequest = new AllAreaTypesRequest();
        return allAreaTypesRequest;
    }

    private GeometryByPortCodeRequest createToGeometryByPortCodeRequest(String portCode) {
        GeometryByPortCodeRequest request = new GeometryByPortCodeRequest();
        request.setPortCode(portCode);
        request.setMethod(SpatialModuleMethod.GET_GEOMETRY_BY_PORT_CODE);
        return request;
    }

    private SpatialEnrichmentRQ createSpatialEnrichmentRequest(PointType point, UnitType unit, List<LocationType> locationTypes, List<AreaType> areaTypes) throws SpatialModelMarshallException {

        SpatialEnrichmentRQ request = new SpatialEnrichmentRQ();
        request.setMethod(SpatialModuleMethod.GET_ENRICHMENT);
        request.setPoint(point);
        request.setUnit(unit);
        SpatialEnrichmentRQ.LocationTypes loc = new SpatialEnrichmentRQ.LocationTypes();
        if (locationTypes != null) {
            loc.getLocationTypes().addAll(locationTypes);
        }
        request.setLocationTypes(loc);
        SpatialEnrichmentRQ.AreaTypes area = new SpatialEnrichmentRQ.AreaTypes();
        if (areaTypes != null) {
            area.getAreaTypes().addAll(areaTypes);
        }
        request.setAreaTypes(area);
        return request;
    }

    private ClosestAreaSpatialRQ createClosestAreaRequest(PointType point, UnitType unit, List<AreaType> areaTypes) throws SpatialModelMarshallException {
        ClosestAreaSpatialRQ request = new ClosestAreaSpatialRQ();
        request.setMethod(SpatialModuleMethod.GET_CLOSEST_AREA);
        request.setPoint(point);
        request.setUnit(unit);
        ClosestAreaSpatialRQ.AreaTypes area = new ClosestAreaSpatialRQ.AreaTypes();
        if (areaTypes != null) {
            area.getAreaTypes().addAll(areaTypes);
        }
        request.setAreaTypes(area);
        return request;
    }

    private ClosestLocationSpatialRQ createClosestLocationRequest(PointType point, UnitType unit, List<LocationType> locationTypes) throws SpatialModelMarshallException {
        ClosestLocationSpatialRQ request = new ClosestLocationSpatialRQ();
        request.setMethod(SpatialModuleMethod.GET_CLOSEST_LOCATION);
        request.setPoint(point);
        request.setUnit(unit);
        ClosestLocationSpatialRQ.LocationTypes loc = new ClosestLocationSpatialRQ.LocationTypes();
        if (locationTypes != null) {
            loc.getLocationTypes().addAll(locationTypes);
        }
        request.setLocationTypes(loc);
        return request;
    }
}
