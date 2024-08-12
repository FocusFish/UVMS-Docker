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
package fish.focus.uvms.docker.validation.system.helper;

import fish.focus.schema.exchange.common.v1.AcknowledgeType;
import fish.focus.schema.exchange.common.v1.AcknowledgeTypeType;
import fish.focus.schema.exchange.common.v1.PollStatusAcknowledgeType;
import fish.focus.schema.exchange.plugin.v1.SetCommandRequest;
import fish.focus.schema.exchange.v1.ExchangeLogStatusTypeType;
import fish.focus.schema.mobileterminal.polltypes.v1.PollType;
import fish.focus.uvms.asset.client.model.AssetDTO;
import fish.focus.uvms.docker.validation.asset.AssetTestHelper;
import fish.focus.uvms.docker.validation.common.MessageHelper;
import fish.focus.uvms.docker.validation.common.TopicListener;
import fish.focus.uvms.docker.validation.mobileterminal.MobileTerminalTestHelper;
import fish.focus.uvms.docker.validation.mobileterminal.dto.MobileTerminalDto;
import fish.focus.uvms.exchange.model.mapper.ExchangePluginResponseMapper;

public class PollHelper {

    public static SetCommandRequest createPollAndReturnSetCommandRequest() throws Exception {
        AssetDTO testAsset = AssetTestHelper.createTestAsset();
        return createPollAndReturnSetCommandRequest(testAsset);
    }

    private static SetCommandRequest createPollAndReturnSetCommandRequest(AssetDTO testAsset) throws Exception {
        try (TopicListener topicListener = new TopicListener(VMSSystemHelper.INMARSAT_SELECTOR)) {
            MobileTerminalTestHelper.createPoll_Helper(testAsset, PollType.MANUAL_POLL);
            return topicListener.listenOnEventBusForSpecificMessage(SetCommandRequest.class);
        }
    }

    public static SetCommandRequest createPollAndReturnSetCommandRequest(AssetDTO testAsset, MobileTerminalDto mobileTerminal) throws Exception {
        try (TopicListener topicListener = new TopicListener(VMSSystemHelper.INMARSAT_SELECTOR)) {
            MobileTerminalTestHelper.createPollWithMT_Helper(testAsset, PollType.MANUAL_POLL, mobileTerminal);
            return topicListener.listenOnEventBusForSpecificMessage(SetCommandRequest.class);
        }
    }

    public static void ackPoll(String messageId, String pollId, ExchangeLogStatusTypeType status, String unsentMessageId) throws Exception {
        AcknowledgeType setCommandAck = ExchangePluginResponseMapper.mapToAcknowledgeType(messageId, AcknowledgeTypeType.OK);
        setCommandAck.setUnsentMessageGuid(unsentMessageId);
        PollStatusAcknowledgeType pollAck = new PollStatusAcknowledgeType();
        pollAck.setStatus(status);
        pollAck.setPollId(pollId);
        setCommandAck.setPollStatus(pollAck);
        String ackMessage = ExchangePluginResponseMapper.mapToSetPollStatusToSuccessfulResponse("Test", setCommandAck, pollId);

        try (MessageHelper messageHelper = new MessageHelper()) {
            messageHelper.sendMessage("UVMSExchangeEvent", ackMessage);
        }
        Thread.sleep(1000);
    }
}
