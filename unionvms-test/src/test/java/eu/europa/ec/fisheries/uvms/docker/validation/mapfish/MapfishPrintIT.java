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
package eu.europa.ec.fisheries.uvms.docker.validation.mapfish;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import org.hamcrest.CoreMatchers;
import org.junit.Test;
import eu.europa.ec.fisheries.uvms.docker.validation.common.AbstractRest;

public class MapfishPrintIT extends AbstractRest {

    @Test
    public void mapFishPrintSanityTest() {
        Client client = ClientBuilder.newClient();
        String response = client.target("http://localhost:28080/mapfish-print/print/default/report.gif")
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getValidJwtToken())
                .post(Entity.json(getPayload()), String.class);
        
        assertThat(response, CoreMatchers.is(CoreMatchers.notNullValue()));
    }
    
    private String getPayload() {
        return "{\"layout\":\"A4 portrait\",\"attributes\":{\"title\":\"asdf\",\"subtitle\":\"asdf\",\"description\":\"asdf\",\"map\":{\"projection\":\"EPSG:3857\",\"bbox\":[1166138.9804931511,7909625.047912957,1282143.976370553,8056789.280432565],\"dpi\":72,\"rotation\":0,\"layers\":[{\"type\":\"grid\",\"gridType\":\"points\",\"numberOfLines\":[5,5],\"opacity\":1,\"renderAsSvg\":true,\"labelProjection\":\"EPSG:3857\",\"labelFomat\":\"%1.2f%s\",\"font\":{\"name\":[\"Arial\"],\"size\":8,\"style\":\"BOLD\"},\"labelColor\":\"#000000\",\"gridColor\":\"#000000\",\"haloRadius\":3},{\"type\":\"geojson\",\"style\":{\"version\":2,\"graphicWidth\":18,\"graphicOpacity\":1,\"graphicFormat\":\"image/png\",\"type\":\"point\",\"[countryCode = 'SWE']\":{\"symbolizers\":[{\"rotation\":\"[reportedCourse]\",\"externalGraphic\":\"http://liaswf05t:28080/unionvms//spatial/image/position/815FB3\"}]}},\"geojson\":{\"type\":\"FeatureCollection\",\"features\":[{\"type\":\"Feature\",\"geometry\":{\"type\":\"Point\",\"coordinates\":[1199912.791260696,7943617.1302092895]},\"properties\":{\"positionTime\":\"2019-03-20T02:14:00\",\"connectionId\":\"a6e3c961-18a7-4275-8665-e0afc49563ad\",\"reportedCourse\":52,\"movementType\":\"POS\",\"reportedSpeed\":2.5999999046325684,\"cfr\":\"APA000A13712\",\"countryCode\":\"SWE\",\"calculatedSpeed\":1.976749300956726,\"ircs\":\"SAPA\",\"name\":\"APA\",\"movementGuid\":\"6bdf8215-175c-49ad-9971-65ce9cf47660\",\"externalMarking\":\"AP-123\",\"source\":\"NAF\",\"isVisible\":true}},{\"type\":\"Feature\",\"geometry\":{\"type\":\"Point\",\"coordinates\":[1189226.1201445416,7958919.473795642]},\"properties\":{\"positionTime\":\"2019-03-19T20:14:00\",\"connectionId\":\"a6e3c961-18a7-4275-8665-e0afc49563ad\",\"reportedCourse\":265,\"movementType\":\"POS\",\"reportedSpeed\":4.400000095367432,\"cfr\":\"APA000A13712\",\"countryCode\":\"SWE\",\"calculatedSpeed\":7.173510551452637,\"ircs\":\"SAPA\",\"name\":\"APA\",\"movementGuid\":\"feacb10b-2013-43df-aef3-2abccb8c961d\",\"externalMarking\":\"AP-123\",\"source\":\"NAF\",\"isVisible\":true}},{\"type\":\"Feature\",\"geometry\":{\"type\":\"Point\",\"coordinates\":[1182769.5896785317,7962067.64819542]},\"properties\":{\"positionTime\":\"2019-03-14T05:08:00\",\"connectionId\":\"a6e3c961-18a7-4275-8665-e0afc49563ad\",\"reportedCourse\":176,\"movementType\":\"POS\",\"reportedSpeed\":1.399999976158142,\"cfr\":\"APA000A13712\",\"countryCode\":\"SWE\",\"calculatedSpeed\":3.4913017749786377,\"ircs\":\"SAPA\",\"name\":\"APA\",\"movementGuid\":\"4f28c47e-d798-4dcf-ac1b-85b517189c2f\",\"externalMarking\":\"AP-123\",\"source\":\"NAF\",\"isVisible\":true}},{\"type\":\"Feature\",\"geometry\":{\"type\":\"Point\",\"coordinates\":[1192677.024359133,7969208.391672196]},\"properties\":{\"positionTime\":\"2019-03-14T04:08:00\",\"connectionId\":\"a6e3c961-18a7-4275-8665-e0afc49563ad\",\"reportedCourse\":254,\"movementType\":\"POS\",\"reportedSpeed\":8.199999809265137,\"cfr\":\"APA000A13712\",\"countryCode\":\"SWE\",\"calculatedSpeed\":7.804006576538086,\"ircs\":\"SAPA\",\"name\":\"APA\",\"movementGuid\":\"4b4f10ba-5910-46ff-a003-ec19fa65ad13\",\"externalMarking\":\"AP-123\",\"source\":\"NAF\",\"isVisible\":true}},{\"type\":\"Feature\",\"geometry\":{\"type\":\"Point\",\"coordinates\":[1177871.5320836278,7969628.646610979]},\"properties\":{\"positionTime\":\"2019-03-19T09:14:00\",\"connectionId\":\"a6e3c961-18a7-4275-8665-e0afc49563ad\",\"reportedCourse\":207,\"movementType\":\"POS\",\"reportedSpeed\":9.300000190734863,\"cfr\":\"APA000A13712\",\"countryCode\":\"SWE\",\"calculatedSpeed\":8.97059440612793,\"ircs\":\"SAPA\",\"name\":\"APA\",\"movementGuid\":\"fee6cd2a-4d9b-4544-93fb-2f1a94ea98cb\",\"externalMarking\":\"AP-123\",\"source\":\"NAF\",\"isVisible\":true}},{\"type\":\"Feature\",\"geometry\":{\"type\":\"Point\",\"coordinates\":[1174420.6278690363,7970679.386720702]},\"properties\":{\"positionTime\":\"2019-03-18T04:12:00\",\"connectionId\":\"a6e3c961-18a7-4275-8665-e0afc49563ad\",\"reportedCourse\":161,\"movementType\":\"POS\",\"reportedSpeed\":3.200000047683716,\"cfr\":\"APA000A13712\",\"countryCode\":\"SWE\",\"calculatedSpeed\":2.673588514328003,\"ircs\":\"SAPA\",\"name\":\"APA\",\"movementGuid\":\"a7804d32-3288-4300-8c81-c649ce04f515\",\"externalMarking\":\"AP-123\",\"source\":\"NAF\",\"isVisible\":true}},{\"type\":\"Feature\",\"geometry\":{\"type\":\"Point\",\"coordinates\":[1202473.139548941,7975724.984169319]},\"properties\":{\"positionTime\":\"2019-03-18T23:14:00\",\"connectionId\":\"a6e3c961-18a7-4275-8665-e0afc49563ad\",\"reportedCourse\":346,\"movementType\":\"POS\",\"reportedSpeed\":2.200000047683716,\"cfr\":\"APA000A13712\",\"countryCode\":\"SWE\",\"calculatedSpeed\":1.7783987522125244,\"ircs\":\"SAPA\",\"name\":\"APA\",\"movementGuid\":\"c62481c9-7011-43a4-932c-cabb2e60db64\",\"externalMarking\":\"AP-123\",\"source\":\"NAF\",\"isVisible\":true}},{\"type\":\"Feature\",\"geometry\":{\"type\":\"Point\",\"coordinates\":[1180209.2413902865,7978038.681845785]},\"properties\":{\"positionTime\":\"2019-03-18T03:12:00\",\"connectionId\":\"a6e3c961-18a7-4275-8665-e0afc49563ad\",\"reportedCourse\":252,\"movementType\":\"POS\",\"reportedSpeed\":6.900000095367432,\"cfr\":\"APA000A13712\",\"countryCode\":\"SWE\",\"calculatedSpeed\":7.349081039428711,\"ircs\":\"SAPA\",\"name\":\"APA\",\"movementGuid\":\"091d35b6-2d6d-4f87-b10b-fb76ba72ac43\",\"externalMarking\":\"AP-123\",\"source\":\"NAF\",\"isVisible\":true}},{\"type\":\"Feature\",\"geometry\":{\"type\":\"Point\",\"coordinates\":[1193233.6218130994,7975935.290898095]},\"properties\":{\"positionTime\":\"2019-03-18T20:14:00\",\"connectionId\":\"a6e3c961-18a7-4275-8665-e0afc49563ad\",\"reportedCourse\":123,\"movementType\":\"POS\",\"reportedSpeed\":2.4000000953674316,\"cfr\":\"APA000A13712\",\"countryCode\":\"SWE\",\"calculatedSpeed\":1.9170116186141968,\"ircs\":\"SAPA\",\"name\":\"APA\",\"movementGuid\":\"bf4336c3-822a-4ae7-8bd3-e0ddce938471\",\"externalMarking\":\"AP-123\",\"source\":\"NAF\",\"isVisible\":true}},{\"type\":\"Feature\",\"geometry\":{\"type\":\"Point\",\"coordinates\":[1190005.3565800944,7981826.269461431]},\"properties\":{\"positionTime\":\"2019-03-18T19:14:00\",\"connectionId\":\"a6e3c961-18a7-4275-8665-e0afc49563ad\",\"reportedCourse\":152,\"movementType\":\"POS\",\"reportedSpeed\":2.799999952316284,\"cfr\":\"APA000A13712\",\"countryCode\":\"SWE\",\"calculatedSpeed\":1.2810806035995483,\"ircs\":\"SAPA\",\"name\":\"APA\",\"movementGuid\":\"1d456925-74fc-4901-9ed0-c39d4117eee8\",\"externalMarking\":\"AP-123\",\"source\":\"NAF\",\"isVisible\":true}},{\"type\":\"Feature\",\"geometry\":{\"type\":\"Point\",\"coordinates\":[1190895.912506441,7998261.24798685]},\"properties\":{\"positionTime\":\"2019-03-19T08:14:00\",\"connectionId\":\"a6e3c961-18a7-4275-8665-e0afc49563ad\",\"reportedCourse\":162,\"movementType\":\"POS\",\"reportedSpeed\":9.199999809265137,\"cfr\":\"APA000A13712\",\"countryCode\":\"SWE\",\"calculatedSpeed\":7.666028022766113,\"ircs\":\"SAPA\",\"name\":\"APA\",\"movementGuid\":\"1e921d76-8a17-4ea4-8075-e0e2a18e077e\",\"externalMarking\":\"AP-123\",\"source\":\"NAF\",\"isVisible\":true}},{\"type\":\"Feature\",\"geometry\":{\"type\":\"Point\",\"coordinates\":[1195793.9701013449,8010294.212084854]},\"properties\":{\"positionTime\":\"2019-03-19T04:14:00\",\"connectionId\":\"a6e3c961-18a7-4275-8665-e0afc49563ad\",\"reportedCourse\":3,\"movementType\":\"POS\",\"reportedSpeed\":1,\"cfr\":\"APA000A13712\",\"countryCode\":\"SWE\",\"calculatedSpeed\":2.113063097000122,\"ircs\":\"SAPA\",\"name\":\"APA\",\"movementGuid\":\"9ba1a250-f5dd-4a58-9eb1-c811466e14ba\",\"externalMarking\":\"AP-123\",\"source\":\"NAF\",\"isVisible\":true}},{\"type\":\"Feature\",\"geometry\":{\"type\":\"Point\",\"coordinates\":[1195014.7336657918,8002903.264593392]},\"properties\":{\"positionTime\":\"2019-03-19T03:14:00\",\"connectionId\":\"a6e3c961-18a7-4275-8665-e0afc49563ad\",\"reportedCourse\":11,\"movementType\":\"POS\",\"reportedSpeed\":1.600000023841858,\"cfr\":\"APA000A13712\",\"countryCode\":\"SWE\",\"calculatedSpeed\":1.9718239307403564,\"ircs\":\"SAPA\",\"name\":\"APA\",\"movementGuid\":\"eb346f1c-856f-4141-afa7-1d5a1f2f222e\",\"externalMarking\":\"AP-123\",\"source\":\"NAF\",\"isVisible\":true}},{\"type\":\"Feature\",\"geometry\":{\"type\":\"Point\",\"coordinates\":[1196573.2065368977,7996152.188762831]},\"properties\":{\"positionTime\":\"2019-03-19T02:14:00\",\"connectionId\":\"a6e3c961-18a7-4275-8665-e0afc49563ad\",\"reportedCourse\":350,\"movementType\":\"POS\",\"reportedSpeed\":3.4000000953674316,\"cfr\":\"APA000A13712\",\"countryCode\":\"SWE\",\"calculatedSpeed\":2.0042810440063477,\"ircs\":\"SAPA\",\"name\":\"APA\",\"movementGuid\":\"1c63c9d8-6af2-4ef1-bfa7-5a8ab4937ae9\",\"externalMarking\":\"AP-123\",\"source\":\"NAF\",\"isVisible\":true}},{\"type\":\"Feature\",\"geometry\":{\"type\":\"Point\",\"coordinates\":[1198576.9573711767,7989407.176518726]},\"properties\":{\"positionTime\":\"2019-03-19T01:14:00\",\"connectionId\":\"a6e3c961-18a7-4275-8665-e0afc49563ad\",\"reportedCourse\":333,\"movementType\":\"POS\",\"reportedSpeed\":1.600000023841858,\"cfr\":\"APA000A13712\",\"countryCode\":\"SWE\",\"calculatedSpeed\":1.9956134557724,\"ircs\":\"SAPA\",\"name\":\"APA\",\"movementGuid\":\"03cb4c5d-cc59-449a-9a80-0dd05f483adc\",\"externalMarking\":\"AP-123\",\"source\":\"NAF\",\"isVisible\":true}},{\"type\":\"Feature\",\"geometry\":{\"type\":\"Point\",\"coordinates\":[1200469.3887146623,7982668.214878469]},\"properties\":{\"positionTime\":\"2019-03-19T00:14:00\",\"connectionId\":\"a6e3c961-18a7-4275-8665-e0afc49563ad\",\"reportedCourse\":338,\"movementType\":\"POS\",\"reportedSpeed\":1.2000000476837158,\"cfr\":\"APA000A13712\",\"countryCode\":\"SWE\",\"calculatedSpeed\":2.062192440032959,\"ircs\":\"SAPA\",\"name\":\"APA\",\"movementGuid\":\"ff66cce8-82d3-472a-8c2d-29f10f9ac036\",\"externalMarking\":\"AP-123\",\"source\":\"NAF\",\"isVisible\":true}},{\"type\":\"Feature\",\"geometry\":{\"type\":\"Point\",\"coordinates\":[1194346.8167210321,8017692.446047904]},\"properties\":{\"positionTime\":\"2019-03-19T05:14:00\",\"connectionId\":\"a6e3c961-18a7-4275-8665-e0afc49563ad\",\"reportedCourse\":322,\"movementType\":\"POS\",\"reportedSpeed\":1.399999976158142,\"cfr\":\"APA000A13712\",\"countryCode\":\"SWE\",\"calculatedSpeed\":2.1412413120269775,\"ircs\":\"SAPA\",\"name\":\"APA\",\"movementGuid\":\"046f1bbf-1cb6-4a74-8f4a-9f9ca5e6bb1a\",\"externalMarking\":\"AP-123\",\"source\":\"NAF\",\"isVisible\":true}},{\"type\":\"Feature\",\"geometry\":{\"type\":\"Point\",\"coordinates\":[1217501.2708060332,7950740.379030037]},\"properties\":{\"positionTime\":\"2019-03-18T12:14:00\",\"connectionId\":\"a6e3c961-18a7-4275-8665-e0afc49563ad\",\"reportedCourse\":5,\"movementType\":\"POS\",\"reportedSpeed\":2.4000000953674316,\"cfr\":\"APA000A13712\",\"countryCode\":\"SWE\",\"calculatedSpeed\":1.5105003118515015,\"ircs\":\"SAPA\",\"name\":\"APA\",\"movementGuid\":\"8c052845-d4e1-4c91-9770-7bbe16159379\",\"externalMarking\":\"AP-123\",\"source\":\"NAF\",\"isVisible\":true}},{\"type\":\"Feature\",\"geometry\":{\"type\":\"Point\",\"coordinates\":[1217946.5487692063,7955143.402388411]},\"properties\":{\"positionTime\":\"2019-03-20T05:16:00\",\"connectionId\":\"a6e3c961-18a7-4275-8665-e0afc49563ad\",\"reportedCourse\":42,\"movementType\":\"POS\",\"reportedSpeed\":2.5999999046325684,\"cfr\":\"APA000A13712\",\"countryCode\":\"SWE\",\"calculatedSpeed\":2.070084571838379,\"ircs\":\"SAPA\",\"name\":\"APA\",\"movementGuid\":\"d684f6de-490a-407d-af5e-2a7191cb1507\",\"externalMarking\":\"AP-123\",\"source\":\"NAF\",\"isVisible\":true}},{\"type\":\"Feature\",\"geometry\":{\"type\":\"Point\",\"coordinates\":[1222399.3284009371,7960808.220352243]},\"properties\":{\"positionTime\":\"2019-03-20T06:16:00\",\"connectionId\":\"a6e3c961-18a7-4275-8665-e0afc49563ad\",\"reportedCourse\":53,\"movementType\":\"POS\",\"reportedSpeed\":2.200000047683716,\"cfr\":\"APA000A13712\",\"countryCode\":\"SWE\",\"calculatedSpeed\":2.06195068359375,\"ircs\":\"SAPA\",\"name\":\"APA\",\"movementGuid\":\"1216a51e-b768-42b0-9382-9d2b8a46aa82\",\"externalMarking\":\"AP-123\",\"source\":\"NAF\",\"isVisible\":true}},{\"type\":\"Feature\",\"geometry\":{\"type\":\"Point\",\"coordinates\":[1225738.9131247352,7964587.136594687]},\"properties\":{\"positionTime\":\"2019-03-20T07:16:00\",\"connectionId\":\"a6e3c961-18a7-4275-8665-e0afc49563ad\",\"reportedCourse\":66,\"movementType\":\"POS\",\"reportedSpeed\":3.200000047683716,\"cfr\":\"APA000A13712\",\"countryCode\":\"SWE\",\"calculatedSpeed\":1.4422757625579834,\"ircs\":\"SAPA\",\"name\":\"APA\",\"movementGuid\":\"5848dd86-c857-4197-aa54-cb1bc73febfa\",\"externalMarking\":\"AP-123\",\"source\":\"NAF\",\"isVisible\":true}},{\"type\":\"Feature\",\"geometry\":{\"type\":\"Point\",\"coordinates\":[1206591.9607082924,7974042.7420996]},\"properties\":{\"positionTime\":\"2019-03-18T14:14:00\",\"connectionId\":\"a6e3c961-18a7-4275-8665-e0afc49563ad\",\"reportedCourse\":31,\"movementType\":\"POS\",\"reportedSpeed\":3.4000000953674316,\"cfr\":\"APA000A13712\",\"countryCode\":\"SWE\",\"calculatedSpeed\":2.2940759658813477,\"ircs\":\"SAPA\",\"name\":\"APA\",\"movementGuid\":\"fd975a2e-3ec8-451a-90df-bffc0267ccf2\",\"externalMarking\":\"AP-123\",\"source\":\"NAF\",\"isVisible\":true}},{\"type\":\"Feature\",\"geometry\":{\"type\":\"Point\",\"coordinates\":[1205590.0852911528,7980353.091907442]},\"properties\":{\"positionTime\":\"2019-03-18T15:14:00\",\"connectionId\":\"a6e3c961-18a7-4275-8665-e0afc49563ad\",\"reportedCourse\":323,\"movementType\":\"POS\",\"reportedSpeed\":0.4000000059604645,\"cfr\":\"APA000A13712\",\"countryCode\":\"SWE\",\"calculatedSpeed\":1.823773980140686,\"ircs\":\"SAPA\",\"name\":\"APA\",\"movementGuid\":\"3c29d8df-42ae-41f5-8ed8-08f6314e3016\",\"externalMarking\":\"AP-123\",\"source\":\"NAF\",\"isVisible\":true}},{\"type\":\"Feature\",\"geometry\":{\"type\":\"Point\",\"coordinates\":[1250006.562117669,7998894.081282029]},\"properties\":{\"positionTime\":\"2019-03-18T00:12:00\",\"connectionId\":\"a6e3c961-18a7-4275-8665-e0afc49563ad\",\"reportedCourse\":259,\"movementType\":\"POS\",\"reportedSpeed\":6.800000190734863,\"cfr\":\"APA000A13712\",\"countryCode\":\"SWE\",\"calculatedSpeed\":5.994480133056641,\"ircs\":\"SAPA\",\"name\":\"APA\",\"movementGuid\":\"0aa229c6-949b-45d6-934d-090c0bc6e104\",\"externalMarking\":\"AP-123\",\"source\":\"NAF\",\"isVisible\":true}},{\"type\":\"Feature\",\"geometry\":{\"type\":\"Point\",\"coordinates\":[1268819.5560617323,8008392.987482539]},\"properties\":{\"positionTime\":\"2019-03-17T23:12:00\",\"connectionId\":\"a6e3c961-18a7-4275-8665-e0afc49563ad\",\"reportedCourse\":205,\"movementType\":\"POS\",\"reportedSpeed\":7.099999904632568,\"cfr\":\"APA000A13712\",\"countryCode\":\"SWE\",\"calculatedSpeed\":1.3834619522094727,\"ircs\":\"SAPA\",\"name\":\"APA\",\"movementGuid\":\"e9077714-234d-4f28-9313-02ef7c33a4e3\",\"externalMarking\":\"AP-123\",\"source\":\"NAF\",\"isVisible\":true}}]}},{\"type\":\"geojson\",\"style\":{\"version\":2,\"strokeColor\":\"#F7580D\",\"strokeWidth\":2,\"fillColor\":\"#ffffff\",\"fillOpacity\":0.3,\"fontWeight\":\"BOLD\",\"fontColor\":\"#000000\",\"fontSize\":8,\"fontFamily\":\"Arial\",\"labelAlign\":\"cm\",\"labelXOffset\":-1,\"labelYOffset\":-1,\"[radius = 10]\":{\"symbolizers\":[{\"type\":\"point\",\"pointRadius\":10},{\"type\":\"text\",\"label\":\"[printLabel]\"}]},\"[radius = 11]\":{\"symbolizers\":[{\"type\":\"point\",\"pointRadius\":11},{\"type\":\"text\",\"label\":\"[printLabel]\"}]},\"[radius = 12]\":{\"symbolizers\":[{\"type\":\"point\",\"pointRadius\":12},{\"type\":\"text\",\"label\":\"[printLabel]\"}]},\"[radius = 22]\":{\"symbolizers\":[{\"type\":\"point\",\"pointRadius\":22},{\"type\":\"text\",\"label\":\"[printLabel]\"}]}},\"geojson\":{\"type\":\"FeatureCollection\",\"features\":[{\"type\":\"Feature\",\"geometry\":{\"type\":\"Point\",\"coordinates\":[1193493.3672916172,7941662.924947891]},\"properties\":{\"printLabel\":3,\"radius\":10}},{\"type\":\"Feature\",\"geometry\":{\"type\":\"Point\",\"coordinates\":[1185441.2574575702,7942988.984080368]},\"properties\":{\"printLabel\":3,\"radius\":10}},{\"type\":\"Feature\",\"geometry\":{\"type\":\"Point\",\"coordinates\":[1196795.8455184842,7949063.725708537]},\"properties\":{\"printLabel\":2,\"radius\":10}},{\"type\":\"Feature\",\"geometry\":{\"type\":\"Point\",\"coordinates\":[1176257.399467125,7953256.22008072]},\"properties\":{\"printLabel\":2,\"radius\":10}},{\"type\":\"Feature\",\"geometry\":{\"type\":\"Point\",\"coordinates\":[1179652.6439363202,7946549.463148285]},\"properties\":{\"printLabel\":2,\"radius\":10}},{\"type\":\"Feature\",\"geometry\":{\"type\":\"Point\",\"coordinates\":[1184439.3820404308,7955562.8840109855]},\"properties\":{\"printLabel\":2,\"radius\":10}},{\"type\":\"Feature\",\"geometry\":{\"type\":\"Point\",\"coordinates\":[1189504.4188715247,7950740.39070327]},\"properties\":{\"printLabel\":2,\"radius\":10}},{\"type\":\"Feature\",\"geometry\":{\"type\":\"Point\",\"coordinates\":[1176275.9527155908,7960318.668279909]},\"properties\":{\"printLabel\":3,\"radius\":10}},{\"type\":\"Feature\",\"geometry\":{\"type\":\"Point\",\"coordinates\":[1197742.061190227,7970679.389657565]},\"properties\":{\"printLabel\":2,\"radius\":10}},{\"type\":\"Feature\",\"geometry\":{\"type\":\"Point\",\"coordinates\":[1188780.8421813685,8024039.6765549015]},\"properties\":{\"printLabel\":2,\"radius\":10}},{\"type\":\"Feature\",\"geometry\":{\"type\":\"Point\",\"coordinates\":[1204309.91114703,7950635.577256218]},\"properties\":{\"printLabel\":2,\"radius\":10}},{\"type\":\"Feature\",\"geometry\":{\"type\":\"Point\",\"coordinates\":[1205144.8073279797,7945502.183072884]},\"properties\":{\"printLabel\":2,\"radius\":10}},{\"type\":\"Feature\",\"geometry\":{\"type\":\"Point\",\"coordinates\":[1215219.221244771,7959234.355596179]},\"properties\":{\"printLabel\":2,\"radius\":10}},{\"type\":\"Feature\",\"geometry\":{\"type\":\"Point\",\"coordinates\":[1211490.0183031964,7953885.135777889]},\"properties\":{\"printLabel\":2,\"radius\":10}},{\"type\":\"Feature\",\"geometry\":{\"type\":\"Point\",\"coordinates\":[1212380.5742295426,7950006.810381113]},\"properties\":{\"printLabel\":2,\"radius\":10}},{\"type\":\"Feature\",\"geometry\":{\"type\":\"Point\",\"coordinates\":[1204365.570892427,7967632.768742185]},\"properties\":{\"printLabel\":2,\"radius\":10}},{\"type\":\"Feature\",\"geometry\":{\"type\":\"Point\",\"coordinates\":[1217270.7466938486,7970905.394524668]},\"properties\":{\"printLabel\":24,\"radius\":11}},{\"type\":\"Feature\",\"geometry\":{\"type\":\"Point\",\"coordinates\":[1216870.9241894162,7974154.055806698]},\"properties\":{\"printLabel\":24,\"radius\":11}},{\"type\":\"Feature\",\"geometry\":{\"type\":\"Point\",\"coordinates\":[1217134.8844819872,7979434.1707520755]},\"properties\":{\"printLabel\":46,\"radius\":12}},{\"type\":\"Feature\",\"geometry\":{\"type\":\"Point\",\"coordinates\":[1216932.9848055337,7976965.915883891]},\"properties\":{\"printLabel\":20,\"radius\":11}},{\"type\":\"Feature\",\"geometry\":{\"type\":\"Point\",\"coordinates\":[1220996.702816942,7983870.128312575]},\"properties\":{\"printLabel\":11,\"radius\":10}},{\"type\":\"Feature\",\"geometry\":{\"type\":\"Point\",\"coordinates\":[1203753.3136930638,7986036.940183016]},\"properties\":{\"printLabel\":2,\"radius\":10}},{\"type\":\"Feature\",\"geometry\":{\"type\":\"Point\",\"coordinates\":[1226084.0035461944,7987932.516519994]},\"properties\":{\"printLabel\":3,\"radius\":10}},{\"type\":\"Feature\",\"geometry\":{\"type\":\"Point\",\"coordinates\":[1228281.4502944537,7990110.983235711]},\"properties\":{\"printLabel\":10,\"radius\":10}},{\"type\":\"Feature\",\"geometry\":{\"type\":\"Point\",\"coordinates\":[1232209.3585270944,7993293.68673259]},\"properties\":{\"printLabel\":12,\"radius\":10}},{\"type\":\"Feature\",\"geometry\":{\"type\":\"Point\",\"coordinates\":[1236445.8241483069,7997193.318196466]},\"properties\":{\"printLabel\":11,\"radius\":10}},{\"type\":\"Feature\",\"geometry\":{\"type\":\"Point\",\"coordinates\":[1240276.125427429,8001033.506926834]},\"properties\":{\"printLabel\":10,\"radius\":10}},{\"type\":\"Feature\",\"geometry\":{\"type\":\"Point\",\"coordinates\":[1244204.342880877,8003944.636113561]},\"properties\":{\"printLabel\":9,\"radius\":10}},{\"type\":\"Feature\",\"geometry\":{\"type\":\"Point\",\"coordinates\":[1248582.6002979383,8007222.645136988]},\"properties\":{\"printLabel\":12,\"radius\":10}},{\"type\":\"Feature\",\"geometry\":{\"type\":\"Point\",\"coordinates\":[1253347.2600363751,8010471.724511019]},\"properties\":{\"printLabel\":10,\"radius\":10}},{\"type\":\"Feature\",\"geometry\":{\"type\":\"Point\",\"coordinates\":[1257299.2256478595,8013438.220664665]},\"properties\":{\"printLabel\":9,\"radius\":10}},{\"type\":\"Feature\",\"geometry\":{\"type\":\"Point\",\"coordinates\":[1259794.3283456676,8017005.225604458]},\"properties\":{\"printLabel\":8,\"radius\":10}},{\"type\":\"Feature\",\"geometry\":{\"type\":\"Point\",\"coordinates\":[1262524.944855058,8020940.469040518]},\"properties\":{\"printLabel\":11,\"radius\":10}},{\"type\":\"Feature\",\"geometry\":{\"type\":\"Point\",\"coordinates\":[1267246.240591854,8022859.094602816]},\"properties\":{\"printLabel\":9,\"radius\":10}},{\"type\":\"Feature\",\"geometry\":{\"type\":\"Point\",\"coordinates\":[1270782.1249383178,8012818.471412689]},\"properties\":{\"printLabel\":356,\"radius\":22}},{\"type\":\"Feature\",\"geometry\":{\"type\":\"Point\",\"coordinates\":[1270898.4475522966,8016624.603085392]},\"properties\":{\"printLabel\":8,\"radius\":10}},{\"type\":\"Feature\",\"geometry\":{\"type\":\"Point\",\"coordinates\":[1271835.3022668592,8020730.853783828]},\"properties\":{\"printLabel\":11,\"radius\":10}},{\"type\":\"Feature\",\"geometry\":{\"type\":\"Point\",\"coordinates\":[1273449.5633907097,8024876.592811834]},\"properties\":{\"printLabel\":63,\"radius\":12}}]}},{\"baseURL\":\"http://tile.openstreetmap.org\",\"type\":\"OSM\",\"resolutions\":[156543.03390625,78271.516953125,39135.7584765625,19567.87923828125,9783.939619140625,4891.9698095703125,2445.9849047851562,1222.9924523925781,611.4962261962891,305.74811309814453,152.87405654907226,76.43702827453613,38.218514137268066,19.109257068634033,9.554628534317017,4.777314267158508,2.388657133579254,1.194328566789627,0.5971642833948135],\"opacity\":1,\"maxExtent\":[-20037508.34,-20037508.34,20037508.34,20037508.34],\"tileSize\":[256,256],\"imageExtension\":\"png\"}]},\"legend\":{\"name\":\"\",\"classes\":[{\"name\":\"Positions\",\"icons\":[\"http://liaswf05t:28080/unionvms//spatial/image/legend/3d456026-8f29-4fe8-89a6-d8edd6e8e672\"]}]},\"datasource\":[{\"displayName\":\"\",\"table\":{\"columns\":[\"layer\",\"copyright\"],\"data\":[[\"Positions\",\"VMS Positions from UnionVMS.\"],[\"OpenStreetMap\",\"© OpenStreetMap contributors.\"]]}}],\"copyrightTitle\":\"COPYRIGHT\",\"legendTitle\":\"LEGEND\"}}";
    }
}
