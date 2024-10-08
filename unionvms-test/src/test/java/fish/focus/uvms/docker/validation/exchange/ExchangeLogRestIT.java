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
package fish.focus.uvms.docker.validation.exchange;

import fish.focus.schema.exchange.common.v1.CommandType;
import fish.focus.schema.exchange.plugin.v1.SetCommandRequest;
import fish.focus.schema.exchange.v1.*;
import fish.focus.schema.mobileterminal.polltypes.v1.PollType;
import fish.focus.uvms.asset.client.model.AssetDTO;
import fish.focus.uvms.docker.validation.asset.AssetTestHelper;
import fish.focus.uvms.docker.validation.common.AbstractRest;
import fish.focus.uvms.docker.validation.exchange.dto.ListQueryResponse;
import fish.focus.uvms.docker.validation.mobileterminal.MobileTerminalTestHelper;
import fish.focus.uvms.docker.validation.mobileterminal.dto.CreatePollResultDto;
import fish.focus.uvms.docker.validation.system.helper.PollHelper;
import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class ExchangeLogRestIT extends AbstractRest {

    @Test
    public void getLogListByCriteriaTest() {
        ExchangeListQuery exchangeListQuery = new ExchangeListQuery();
        ExchangeListCriteria exchangeListCriteria = new ExchangeListCriteria();
        exchangeListCriteria.setIsDynamic(true);
        ExchangeListCriteriaPair exchangeListCriteriaPair = new ExchangeListCriteriaPair();
        exchangeListCriteriaPair.setKey(SearchField.STATUS);
        exchangeListCriteriaPair.setValue("SUCCESSFUL");
        exchangeListCriteria.getCriterias().add(exchangeListCriteriaPair);

        exchangeListQuery.setExchangeSearchCriteria(exchangeListCriteria);
        ExchangeListPagination exchangeListPagination = new ExchangeListPagination();
        exchangeListPagination.setPage(1);
        exchangeListPagination.setListSize(100);
        exchangeListQuery.setPagination(exchangeListPagination);

        ListQueryResponse listQueryResponse = getWebTarget()
                .path("exchange/rest/exchange/list")
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getValidJwtToken())
                .post(Entity.json(exchangeListQuery), ListQueryResponse.class);

        assertNotNull(listQueryResponse);
        assertFalse(listQueryResponse.getLogList().isEmpty());
    }

    @Test
    public void getPollStatusQueryTest() throws Exception {
        SetCommandRequest commandRequest = PollHelper.createPollAndReturnSetCommandRequest();
        CommandType command = commandRequest.getCommand();
        PollHelper.ackPoll(command.getPoll().getMessage(), command.getPoll().getPollId(),
                ExchangeLogStatusTypeType.SUCCESSFUL, command.getUnsentMessageGuid());

        Thread.sleep(1000);     // Needed to let the system work and catch up

        PollQuery pollQuery = new PollQuery();
        pollQuery.setStatus(ExchangeLogStatusTypeType.SUCCESSFUL);
        pollQuery.setStatusFromDate(formatDateAsUTC(Instant.now().minus(1, ChronoUnit.HOURS)));
        pollQuery.setStatusToDate(formatDateAsUTC(Instant.now().plus(1, ChronoUnit.HOURS)));

        List<ExchangeLogStatusType> exchangeLogStatusTypeList = getWebTarget()
                .path("exchange/rest/exchange/poll/")
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getValidJwtToken())
                .post(Entity.json(pollQuery), new GenericType<List<ExchangeLogStatusType>>() {
                });

        assertNotNull(exchangeLogStatusTypeList);
        assertFalse(exchangeLogStatusTypeList.isEmpty());
    }

    @Test
    public void getPollStatusRefGuidTest() {
        AssetDTO testAsset = AssetTestHelper.createTestAsset();
        CreatePollResultDto createPollResultDto = MobileTerminalTestHelper.createPoll_Helper(testAsset, PollType.MANUAL_POLL);
        List<String> sentPolls = createPollResultDto.getSentPolls();
        String uid = sentPolls.get(0);

        ExchangeLogStatusType exchangeLogStatusType = getWebTarget()
                .path("exchange/rest/exchange/poll/" + uid)
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getValidJwtToken())
                .get(ExchangeLogStatusType.class);

        assertNotNull(exchangeLogStatusType);
        assertNotNull(exchangeLogStatusType.getGuid());
    }

    @Test
    public void getExchangeLogByGuidTest() {
        AssetDTO testAsset = AssetTestHelper.createTestAsset();
        CreatePollResultDto createPollResultDto = MobileTerminalTestHelper.createPoll_Helper(testAsset, PollType.MANUAL_POLL);
        List<String> sentPolls = createPollResultDto.getSentPolls();
        String uid = sentPolls.get(0);

        ExchangeLogStatusType exchangeLogStatusType = getWebTarget()
                .path("exchange/rest/exchange/poll/" + uid)
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getValidJwtToken())
                .get(ExchangeLogStatusType.class);

        assertNotNull(exchangeLogStatusType);
        String guid = (String) exchangeLogStatusType.getGuid();

        ExchangeLogType exchangeLogType = getWebTarget()
                .path("exchange/rest/exchange/" + guid)
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getValidJwtToken())
                .get(ExchangeLogType.class);

        assertNotNull(exchangeLogType);
        assertEquals(LogType.SEND_POLL, exchangeLogType.getType());
    }

    private String formatDateAsUTC(Instant date) {
        return DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss Z").withZone(ZoneId.of("UTC")).format(date);
    }
}
