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

import fish.focus.schema.exchange.module.v1.GetServiceListResponse;
import fish.focus.schema.exchange.plugin.types.v1.PluginType;
import fish.focus.schema.exchange.plugin.v1.SetCommandRequest;
import fish.focus.schema.exchange.plugin.v1.SetReportRequest;
import fish.focus.schema.exchange.service.v1.CapabilityListType;
import fish.focus.schema.exchange.service.v1.ServiceResponseType;
import fish.focus.schema.exchange.service.v1.ServiceType;
import fish.focus.schema.exchange.service.v1.SettingListType;
import fish.focus.schema.movementrules.customrule.v1.*;
import fish.focus.uvms.asset.client.model.AssetDTO;
import fish.focus.uvms.commons.message.api.MessageConstants;
import fish.focus.uvms.docker.validation.asset.AssetTestHelper;
import fish.focus.uvms.docker.validation.common.MessageHelper;
import fish.focus.uvms.docker.validation.common.TopicListener;
import fish.focus.uvms.docker.validation.mobileterminal.dto.MobileTerminalDto;
import fish.focus.uvms.docker.validation.movement.LatLong;
import fish.focus.uvms.exchange.model.constant.ExchangeModelConstants;
import fish.focus.uvms.exchange.model.mapper.ExchangeModuleRequestMapper;
import fish.focus.uvms.exchange.model.mapper.JAXBMarshaller;

import javax.jms.Message;
import javax.jms.TextMessage;
import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertNotNull;

public class VMSSystemHelper {

    private static final String SERVICE_NAME = "fish.focus.uvms.docker.validation.system.rules.EMAIL";
    private static final long TIMEOUT = 10000;

    public static final String FLUX_SELECTOR = "ServiceName='fish.focus.uvms.plugins.flux.movement'";
    public static final String NAF_SELECTOR = "ServiceName='fish.focus.uvms.plugins.naf'";
    public static final String INMARSAT_SELECTOR = "ServiceName='fish.focus.uvms.plugins.inmarsat'";
    public static String emailSelector = "ServiceName='" + SERVICE_NAME + "'";
    public static String emailPluginName = "TEST EMAIL";
    public static String FLUX_NAME = "fish.focus.uvms.plugins.flux.movement";
    public static String NAF_NAME = "fish.focus.uvms.plugins.naf";
    public static String REST_NAME = "fish.focus.uvms.plugins.rest.movement";
    
    public static SetReportRequest triggerBasicRuleAndSendToFlux(String fluxEndpoint) throws Exception {
        return triggerBasicRuleWithAction(ActionType.SEND_REPORT, FLUX_NAME, fluxEndpoint, SetReportRequest.class, FLUX_SELECTOR, true);
    }

    public static SetReportRequest triggerBasicRuleAndCreateTicket(String fluxEndpoint) throws Exception {
        return triggerBasicRuleWithAction(ActionType.SEND_REPORT, FLUX_NAME, fluxEndpoint, SetReportRequest.class, FLUX_SELECTOR, true);
    }

    public static SetReportRequest triggerBasicRuleAndSendToNAF(String nation) throws Exception {
        return triggerBasicRuleWithAction(ActionType.SEND_REPORT, NAF_NAME, nation, SetReportRequest.class, NAF_SELECTOR, true);
    }

    public static SetReportRequest triggerBasicRuleAndSendToNAF(AssetDTO asset, String nation) throws Exception {
        return triggerBasicRuleWithAction(asset, ActionType.SEND_REPORT, NAF_NAME, nation, SetReportRequest.class, NAF_SELECTOR, true);
    }

    public static SetCommandRequest triggerBasicRuleAndSendEmail(String email) throws Exception {
        return triggerBasicRuleWithAction(ActionType.EMAIL, email, SetCommandRequest.class, emailSelector, true);
    }
    
    private static <T> T triggerBasicRuleWithAction(ActionType actionType, String actionValue, Class<T> expectedType, String selector, boolean createTicket) throws Exception {
        return triggerBasicRuleWithAction(actionType, null, actionValue, expectedType, selector, createTicket);
    }

    private static <T> T triggerBasicRuleWithAction(ActionType actionType, String target, String actionValue, Class<T> expectedType, String selector, boolean createTicket) throws Exception {
        AssetDTO asset = AssetTestHelper.createTestAsset();
        return triggerBasicRuleWithAction(asset, actionType, target, actionValue, expectedType, selector, createTicket);
    }
    
    private static <T> T triggerBasicRuleWithAction(AssetDTO asset, ActionType actionType, String target, String actionValue, Class<T> expectedType, String selector, boolean createTicket) throws Exception {
        try {
            Instant timestamp = Instant.now();

            CustomRuleType flagStateRule = CustomRuleBuilder.getBuilder()
                    .setName("Flag state => FLUX DNK")
                    .rule(CriteriaType.ASSET, SubCriteriaType.FLAG_STATE, 
                            ConditionType.EQ, asset.getFlagStateCode())
                    .action(actionType, target, actionValue)
                    .build();

            if(createTicket){
                CustomRuleActionType ticketAction = new CustomRuleActionType();
                ticketAction.setAction(ActionType.CREATE_TICKET);
                ticketAction.setOrder("99");
                flagStateRule.getActions().add(ticketAction);
            }

            CustomRuleType createdCustomRule = CustomRuleHelper.createCustomRule(flagStateRule);
            assertNotNull(createdCustomRule);
    
            LatLong position = new LatLong(11d, 56d, new Date());

            T reportRequest;
            try (TopicListener topicListener = new TopicListener(selector)) {
                FLUXHelper.sendPositionToFluxPlugin(asset, position);
                CustomRuleHelper.pollTicketCreated();
                reportRequest = topicListener.listenOnEventBusForSpecificMessage(expectedType);
            }
            CustomRuleHelper.assertRuleTriggered(createdCustomRule, timestamp);
            return reportRequest;
        } finally {
            CustomRuleHelper.removeCustomRulesByDefaultUser();
        }
    }
    
    public static void registerEmailPluginIfNotExisting() throws Exception {
        try (MessageHelper messageHelper = new MessageHelper()) {
            String exchangeRequest = ExchangeModuleRequestMapper.createGetServiceListRequest(Collections.singletonList(PluginType.EMAIL));
            Message exchangeResponse = messageHelper.getMessageResponse(MessageConstants.QUEUE_EXCHANGE_EVENT_NAME, exchangeRequest);
            GetServiceListResponse emailServices = JAXBMarshaller.unmarshallTextMessage((TextMessage) exchangeResponse, GetServiceListResponse.class);
            List<ServiceResponseType> service = emailServices.getService();
            if (!service.isEmpty()) {
                emailSelector = "ServiceName='" + service.get(0).getServiceClassName() + "'";
            } else {
                ServiceType serviceType = new ServiceType();
                serviceType.setName(emailPluginName);
                serviceType.setPluginType(PluginType.EMAIL);
                serviceType.setServiceClassName(SERVICE_NAME);
                serviceType.setServiceResponseMessageName(SERVICE_NAME);
                String registerRequest = ExchangeModuleRequestMapper.createRegisterServiceRequest(serviceType, new CapabilityListType(), new SettingListType());
                messageHelper.sendToEventBus(registerRequest, ExchangeModelConstants.EXCHANGE_REGISTER_SERVICE);
                
                // Clear topic
                messageHelper.listenOnEventBus(emailSelector, TIMEOUT);
                messageHelper.listenOnEventBus(emailSelector, TIMEOUT);
            }
        }
    }

    public static void triggerBasicRuleWithSatellitePosition(MobileTerminalDto mobileTerminal) throws IOException, Exception {
        try {
            Instant timestamp = Instant.now();
            CustomRuleType sourceRule = CustomRuleBuilder.getBuilder()
                    .setName("Source inmarsat => FLUX DNK")
                    .rule(CriteriaType.POSITION, SubCriteriaType.SOURCE,
                            ConditionType.EQ, mobileTerminal.getMobileTerminalType())
                    .action(ActionType.SEND_REPORT, FLUX_NAME, "DNK")
                    .build();
            CustomRuleType createdCustomRule = CustomRuleHelper.createCustomRule(sourceRule);

            LatLong position = new LatLong(11d, 56d, Date.from(Instant.now().truncatedTo(ChronoUnit.MINUTES)));

            try (TopicListener topicListener = new TopicListener(FLUX_SELECTOR)) {
                InmarsatPluginMock.sendInmarsatPosition(mobileTerminal, position);
                topicListener.listenOnEventBusForSpecificMessage(SetReportRequest.class);
            }
            CustomRuleHelper.assertRuleTriggered(createdCustomRule, timestamp);
        } finally {
            CustomRuleHelper.removeCustomRulesByDefaultUser();
        }
    }
}
