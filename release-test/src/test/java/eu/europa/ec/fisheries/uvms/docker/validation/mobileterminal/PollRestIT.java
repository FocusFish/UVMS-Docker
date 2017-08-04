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
package eu.europa.ec.fisheries.uvms.docker.validation.mobileterminal;

import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.junit.Ignore;
import org.junit.Test;

import eu.europa.ec.fisheries.schema.mobileterminal.polltypes.v1.PollAttribute;
import eu.europa.ec.fisheries.schema.mobileterminal.polltypes.v1.PollAttributeType;
import eu.europa.ec.fisheries.schema.mobileterminal.polltypes.v1.PollListQuery;
import eu.europa.ec.fisheries.schema.mobileterminal.polltypes.v1.PollMobileTerminal;
import eu.europa.ec.fisheries.schema.mobileterminal.polltypes.v1.PollRequestType;
import eu.europa.ec.fisheries.schema.mobileterminal.polltypes.v1.PollSearchCriteria;
import eu.europa.ec.fisheries.schema.mobileterminal.polltypes.v1.PollType;
import eu.europa.ec.fisheries.schema.mobileterminal.polltypes.v1.PollableQuery;
import eu.europa.ec.fisheries.schema.mobileterminal.types.v1.ComChannelType;
import eu.europa.ec.fisheries.schema.mobileterminal.types.v1.ListPagination;
import eu.europa.ec.fisheries.schema.mobileterminal.types.v1.MobileTerminalAssignQuery;
import eu.europa.ec.fisheries.schema.mobileterminal.types.v1.MobileTerminalAttribute;
import eu.europa.ec.fisheries.schema.mobileterminal.types.v1.MobileTerminalType;
import eu.europa.ec.fisheries.wsdl.asset.types.Asset;

/**
 * The Class PollRestIT.
 */

public class PollRestIT extends AbstractMobileTerminalTest {

	/**
	 * Gets the areas test.
	 *
	 * @return the areas test
	 * @throws Exception
	 *             the exception
	 */
	@Test
	public void getRunningProgramPollsTest() throws Exception {
		final HttpResponse response = Request.Get(BASE_URL + "mobileterminal/rest/poll/running")
				.setHeader("Content-Type", "application/json").setHeader("Authorization", getValidJwtToken()).execute()
				.returnResponse();
		List dataList = checkSuccessResponseReturnType(response, List.class);
	}

	/**
	 * Creates the poll test.
	 *
	 * @throws Exception
	 *             the exception
	 */
	@Test
	@Ignore
	public void createPollTest() throws Exception {
		Asset testAsset = createTestAsset();
		MobileTerminalType createdMobileTerminalType = createMobileTerminalType();
		
		{
			MobileTerminalAssignQuery mobileTerminalAssignQuery = new MobileTerminalAssignQuery();
			mobileTerminalAssignQuery.setMobileTerminalId(createdMobileTerminalType.getMobileTerminalId());
			mobileTerminalAssignQuery.setConnectId(testAsset.getAssetId().getGuid());
		 	// Assign first
			final HttpResponse response = Request
					.Post(BASE_URL + "mobileterminal/rest/mobileterminal/assign?comment=comment")
					.setHeader("Content-Type", "application/json").setHeader("Authorization", getValidJwtToken())
					.bodyByteArray(writeValueAsString(mobileTerminalAssignQuery).getBytes()).execute()
					.returnResponse();
	
			Map<String, Object> dataMap = checkSuccessResponseReturnMap(response);
		}
		
		String comChannelId = createdMobileTerminalType.getChannels().get(0).getGuid();

		PollRequestType pollRequestType = new PollRequestType();
		pollRequestType.setPollType(PollType.MANUAL_POLL);
		pollRequestType.setUserName("vms_admin_com");
		pollRequestType.setComment("Manual poll created by test");


		PollMobileTerminal pollMobileTerminal = new PollMobileTerminal();
		pollMobileTerminal.setComChannelId(comChannelId);
		pollMobileTerminal.setConnectId(testAsset.getAssetId().getGuid());
		pollMobileTerminal.setMobileTerminalId(createdMobileTerminalType.getMobileTerminalId().getGuid());

		List<MobileTerminalAttribute> mobileTerminalAttributes = createdMobileTerminalType.getAttributes();
		for (MobileTerminalAttribute mobileTerminalAttribute : mobileTerminalAttributes) {
			
			String type = mobileTerminalAttribute.getType();
			String value = mobileTerminalAttribute.getValue();
			PollAttribute pollAttribute = new PollAttribute();

			try {
				PollAttributeType pollAttributeType = PollAttributeType.valueOf(type);
				pollAttribute.setKey(pollAttributeType);
				pollAttribute.setValue(value);
				pollRequestType.getAttributes().add(pollAttribute);
			} catch (RuntimeException rte) {
				// ignore 
			}
		}

		pollRequestType.getMobileTerminals().add(pollMobileTerminal);

		final HttpResponse response = Request.Post(BASE_URL + "mobileterminal/rest/poll")
				.setHeader("Content-Type", "application/json").setHeader("Authorization", getValidJwtToken())
				.bodyByteArray(writeValueAsString(pollRequestType).getBytes()).execute().returnResponse();

		Map<String, Object> dataMap = checkSuccessResponseReturnMap(response);
	}

	/**
	 * Start program poll test.
	 *
	 * @throws Exception
	 *             the exception
	 */
	@Test
	@Ignore
	public void startProgramPollTest() throws Exception {
		final HttpResponse response = Request.Put(BASE_URL + "mobileterminal/rest/poll/start/{id}")
				.setHeader("Content-Type", "application/json").setHeader("Authorization", getValidJwtToken()).execute()
				.returnResponse();
		Map<String, Object> dataMap = checkSuccessResponseReturnMap(response);
	}

	/**
	 * Stop program poll test.
	 *
	 * @throws Exception
	 *             the exception
	 */
	@Test
	@Ignore
	public void stopProgramPollTest() throws Exception {
		final HttpResponse response = Request.Put(BASE_URL + "mobileterminal/rest/poll/stop/{id}")
				.setHeader("Content-Type", "application/json").setHeader("Authorization", getValidJwtToken()).execute()
				.returnResponse();
		Map<String, Object> dataMap = checkSuccessResponseReturnMap(response);
	}

	/**
	 * Inactivate program poll test.
	 *
	 * @throws Exception
	 *             the exception
	 */
	@Test
	@Ignore
	public void inactivateProgramPollTest() throws Exception {
		final HttpResponse response = Request.Put(BASE_URL + "mobileterminal/rest/poll/inactivate/{id}")
				.setHeader("Content-Type", "application/json").setHeader("Authorization", getValidJwtToken()).execute()
				.returnResponse();
		Map<String, Object> dataMap = checkSuccessResponseReturnMap(response);
	}

	/**
	 * Gets the poll by search criteria test.
	 *
	 * @return the poll by search criteria test
	 * @throws Exception
	 *             the exception
	 */
	@Test
	public void getPollBySearchCriteriaTest() throws Exception {
		PollListQuery pollListQuery = new PollListQuery();
		ListPagination pagination = new ListPagination();
		pollListQuery.setPagination(pagination);
		pagination.setListSize(100);
		pagination.setPage(1);

		PollSearchCriteria pollSearchCriteria = new PollSearchCriteria();
		pollListQuery.setPollSearchCriteria(pollSearchCriteria);
		pollSearchCriteria.setIsDynamic(true);

		final HttpResponse response = Request.Post(BASE_URL + "mobileterminal/rest/poll/list")
				.setHeader("Content-Type", "application/json").setHeader("Authorization", getValidJwtToken())
				.bodyByteArray(writeValueAsString(pollListQuery).getBytes()).execute().returnResponse();

		Map<String, Object> dataMap = checkSuccessResponseReturnMap(response);
	}

	/**
	 * Gets the pollable channels test.
	 *
	 * @return the pollable channels test
	 * @throws Exception
	 *             the exception
	 */
	@Test
	public void getPollableChannelsTest() throws Exception {
		PollableQuery pollableQuery = new PollableQuery();
		ListPagination listPagination = new ListPagination();
		listPagination.setListSize(100);
		listPagination.setPage(1);
		pollableQuery.setPagination(listPagination);
		pollableQuery.getConnectIdList().add("connectId");

		final HttpResponse response = Request.Post(BASE_URL + "mobileterminal/rest/poll/pollable")
				.setHeader("Content-Type", "application/json").setHeader("Authorization", getValidJwtToken())
				.bodyByteArray(writeValueAsString(pollableQuery).getBytes()).execute().returnResponse();

		Map<String, Object> dataMap = checkSuccessResponseReturnMap(response);
	}

}