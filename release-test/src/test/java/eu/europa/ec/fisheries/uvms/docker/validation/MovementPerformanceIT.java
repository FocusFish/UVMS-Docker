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
package eu.europa.ec.fisheries.uvms.docker.validation;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.fluent.Request;
import org.databene.contiperf.PerfTest;
import org.databene.contiperf.Required;
import org.databene.contiperf.junit.ContiPerfRule;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.europa.ec.fisheries.schema.movement.asset.v1.VesselType;
import eu.europa.ec.fisheries.schema.movement.v1.MovementPoint;
import eu.europa.ec.fisheries.schema.movement.v1.TempMovementStateEnum;
import eu.europa.ec.fisheries.schema.movement.v1.TempMovementType;

/**
 * The Class MovementPerformanceIT.
 */
public class MovementPerformanceIT extends AbstractRestServiceTest {

	/** The i. */
	@Rule
	public ContiPerfRule i = new ContiPerfRule();

	/** The Constant BASE_URL. */
	private static final String BASE_URL = "http://localhost:28080/";
	
	/** The valid jwt token. */
	private static String validJwtToken;

	/**
	 * Aquire jwt token for test.
	 *
	 * @throws Exception the exception
	 */
	@BeforeClass
	public static void aquireJwtTokenForTest() throws Exception {
		HttpResponse response = Request.Post(BASE_URL + "usm-administration/rest/authenticate")
				.setHeader("Content-Type", "application/json")
				.bodyByteArray("{\"userName\":\"vms_admin_com\",\"password\":\"password\"}".getBytes()).execute()
				.returnResponse();

		assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
		final Map<String, Object> data = getJsonMap(response);
		assertEquals(true, data.get("authenticated"));
		validJwtToken = (String) data.get("jwtoken");

	}

	/**
	 * Creates the temp test.
	 *
	 * @throws Exception the exception
	 */
	@Test
	@PerfTest(threads = 4, duration = 10000, warmUp = 1000)
	@Required(max = 2500, average = 1500, percentile95 = 2000, throughput = 4)
	public void createTempTest() throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		HttpResponse response = Request.Post(BASE_URL + "movement/rest/tempmovement")
				.setHeader("Content-Type", "application/json").setHeader("Authorization",validJwtToken)
				.bodyByteArray(mapper.writeValueAsString(createTempMovement()).getBytes()).execute().returnResponse();

		assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
		final Map<String, Object> data = getJsonMap(response);
		assertFalse(data.isEmpty());
	}

	/**
	 * Creates the temp movement.
	 *
	 * @return the temp movement type
	 */
	private static TempMovementType createTempMovement() {
		VesselType vesselType = new VesselType();
		vesselType.setCfr("T");
		vesselType.setExtMarking("T");
		vesselType.setFlagState("T");
		vesselType.setIrcs("T");
		vesselType.setName("T");

		MovementPoint movementPoint = new MovementPoint();
		movementPoint.setAltitude(0.0);
		movementPoint.setLatitude(0.0);
		movementPoint.setLongitude(0.0);

		Date d = Calendar.getInstance().getTime();
		final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

		TempMovementType tempMovementType = new TempMovementType();
		tempMovementType.setAsset(vesselType);
		tempMovementType.setCourse(0.0);
		tempMovementType.setPosition(movementPoint);
		tempMovementType.setSpeed(0.0);
		tempMovementType.setState(TempMovementStateEnum.SENT);
		tempMovementType.setTime(sdf.format(d));
		return tempMovementType;
	}

}
