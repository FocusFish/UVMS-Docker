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
package fish.focus.uvms.docker.validation.system.vms;

import fish.focus.schema.exchange.movement.v1.MovementType;
import fish.focus.schema.exchange.movement.v1.RecipientInfoType;
import fish.focus.schema.exchange.plugin.v1.SetReportRequest;
import fish.focus.schema.movementrules.customrule.v1.*;
import fish.focus.uvms.asset.client.model.AssetDTO;
import fish.focus.uvms.docker.validation.asset.AssetTestHelper;
import fish.focus.uvms.docker.validation.common.AbstractRest;
import fish.focus.uvms.docker.validation.common.MessageHelper;
import fish.focus.uvms.docker.validation.common.TopicListener;
import fish.focus.uvms.docker.validation.mobileterminal.MobileTerminalTestHelper;
import fish.focus.uvms.docker.validation.mobileterminal.dto.MobileTerminalDto;
import fish.focus.uvms.docker.validation.movement.LatLong;
import fish.focus.uvms.docker.validation.system.helper.CustomRuleBuilder;
import fish.focus.uvms.docker.validation.system.helper.CustomRuleHelper;
import fish.focus.uvms.docker.validation.system.helper.FLUXHelper;
import fish.focus.uvms.docker.validation.system.helper.VMSSystemHelper;
import fish.focus.uvms.docker.validation.user.UserHelper;
import fish.focus.uvms.docker.validation.user.dto.Channel;
import fish.focus.uvms.docker.validation.user.dto.EndPoint;
import fish.focus.uvms.docker.validation.user.dto.Organisation;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.jms.JMSException;
import java.time.Instant;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;

public class VMSSystemIT extends AbstractRest {

    private static MessageHelper messageHelper;

    @BeforeClass
    public static void setup() throws JMSException {
        messageHelper = new MessageHelper();
    }

    @AfterClass
    public static void cleanup() {
        messageHelper.close();
    }

    @After
    public void removeCustomRules() {
        CustomRuleHelper.removeCustomRulesByDefaultUser();
    }

    @Test
    public void sendFlagStateToFLUXDNKTest() throws Exception {
        Instant timestamp = Instant.now();

        AssetDTO asset = AssetTestHelper.createTestAsset();
        MobileTerminalDto mobileTerminal = MobileTerminalTestHelper.createMobileTerminal();
        MobileTerminalTestHelper.assignMobileTerminal(asset, mobileTerminal);

        String fluxEndpoint = "DNK";

        CustomRuleType flagStateRule = CustomRuleBuilder.getBuilder()
                .setName("Flag state => FLUX DNK")
                .rule(CriteriaType.ASSET, SubCriteriaType.FLAG_STATE,
                        ConditionType.EQ, asset.getFlagStateCode())
                .action(ActionType.SEND_REPORT, VMSSystemHelper.FLUX_NAME, fluxEndpoint)
                .action(ActionType.CREATE_TICKET, "Dummy ticket")
                .build();

        CustomRuleType createdCustomRule = CustomRuleHelper.createCustomRule(flagStateRule);
        assertNotNull(createdCustomRule);

        LatLong position = new LatLong(11d, 56d, new Date());

        senPositionToFluxAndVerifyMessageContent(timestamp, asset, fluxEndpoint, createdCustomRule, position);
    }

    @Test
    public void sendFlagStateAndAreaDNKToFLUXDNKTest() throws Exception {
        Instant timestamp = Instant.now();

        AssetDTO asset = AssetTestHelper.createTestAsset();
        MobileTerminalDto mobileTerminal = MobileTerminalTestHelper.createMobileTerminal();
        MobileTerminalTestHelper.assignMobileTerminal(asset, mobileTerminal);

        String areaCode = "DNK";
        String fluxEndpoint = "DNK";

        CustomRuleType flagStateAndAreaRule = CustomRuleBuilder.getBuilder()
                .setName("Flag state && Area => FLUX DNK")
                .rule(CriteriaType.ASSET, SubCriteriaType.FLAG_STATE,
                        ConditionType.EQ, asset.getFlagStateCode())
                .and(CriteriaType.AREA, SubCriteriaType.AREA_CODE,
                        ConditionType.EQ, areaCode)
                .action(ActionType.SEND_REPORT, VMSSystemHelper.FLUX_NAME, fluxEndpoint)
                .action(ActionType.CREATE_TICKET, "Dummy ticket")
                .build();

        CustomRuleType createdCustomRule = CustomRuleHelper.createCustomRule(flagStateAndAreaRule);
        assertNotNull(createdCustomRule);

        LatLong position = new LatLong(56d, 10.5, new Date());

        senPositionToFluxAndVerifyMessageContent(timestamp, asset, fluxEndpoint, createdCustomRule, position);
    }

    @Test
    public void sendFlagStateToFLUXDNKWithRuleIntervalTest() throws Exception {
        Instant timestamp = Instant.now();

        AssetDTO asset = AssetTestHelper.createTestAsset();
        MobileTerminalDto mobileTerminal = MobileTerminalTestHelper.createMobileTerminal();
        MobileTerminalTestHelper.assignMobileTerminal(asset, mobileTerminal);

        String fluxEndpoint = "DNK";

        Calendar calendarStart = Calendar.getInstance();
        calendarStart.add(Calendar.HOUR, -1);
        Date ruleIntervalStart = calendarStart.getTime();

        Calendar calendarEnd = Calendar.getInstance();
        calendarEnd.add(Calendar.HOUR, 1);
        Date ruleIntervalEnd = calendarEnd.getTime();

        CustomRuleType flagStateRuleWithInterval = CustomRuleBuilder.getBuilder()
                .setName("Flag state => FLUX DNK")
                .rule(CriteriaType.ASSET, SubCriteriaType.FLAG_STATE,
                        ConditionType.EQ, asset.getFlagStateCode())
                .interval(ruleIntervalStart, ruleIntervalEnd)
                .action(ActionType.SEND_REPORT, VMSSystemHelper.FLUX_NAME, fluxEndpoint)
                .action(ActionType.CREATE_TICKET, "Dummy ticket")
                .build();

        CustomRuleType createdCustomRule = CustomRuleHelper.createCustomRule(flagStateRuleWithInterval);
        assertNotNull(createdCustomRule);

        LatLong position = new LatLong(11d, 56d, new Date());

        senPositionToFluxAndVerifyMessageContent(timestamp, asset, fluxEndpoint, createdCustomRule, position);
    }

    @Test
    public void sendFlagStateToFLUXDNKWithPastValidRuleIntervalTest() throws Exception {
        Instant timestamp = Instant.now();

        AssetDTO asset = AssetTestHelper.createTestAsset();
        MobileTerminalDto mobileTerminal = MobileTerminalTestHelper.createMobileTerminal();
        MobileTerminalTestHelper.assignMobileTerminal(asset, mobileTerminal);

        String fluxEndpoint = "DNK";

        Calendar calendarStart = Calendar.getInstance();
        calendarStart.add(Calendar.HOUR, -2);
        Date ruleIntervalStart = calendarStart.getTime();

        Calendar calendarEnd = Calendar.getInstance();
        calendarEnd.add(Calendar.HOUR, -1);
        Date ruleIntervalEnd = calendarEnd.getTime();

        CustomRuleType flagStateRuleWithInterval = CustomRuleBuilder.getBuilder()
                .setName("Flag state => FLUX DNK")
                .rule(CriteriaType.ASSET, SubCriteriaType.FLAG_STATE,
                        ConditionType.EQ, asset.getFlagStateCode())
                .interval(ruleIntervalStart, ruleIntervalEnd)
                .action(ActionType.SEND_REPORT, VMSSystemHelper.FLUX_NAME, fluxEndpoint)
                .action(ActionType.CREATE_TICKET, "Dummy ticket")
                .build();

        CustomRuleType createdCustomRuleWithInterval = CustomRuleHelper.createCustomRule(flagStateRuleWithInterval);
        assertNotNull(createdCustomRuleWithInterval);

        CustomRuleType flagStateRuleWithoutInterval = CustomRuleBuilder.getBuilder()
                .setName("Flag state => FLUX DNK")
                .rule(CriteriaType.ASSET, SubCriteriaType.FLAG_STATE,
                        ConditionType.EQ, asset.getFlagStateCode())
                .action(ActionType.SEND_REPORT, VMSSystemHelper.FLUX_NAME, fluxEndpoint)
                .action(ActionType.CREATE_TICKET, "Dummy ticket")
                .build();

        CustomRuleType createdCustomRuleWithoutInterval = CustomRuleHelper.createCustomRule(flagStateRuleWithoutInterval);
        assertNotNull(createdCustomRuleWithoutInterval);

        LatLong position = new LatLong(11d, 56d, new Date());

        sendPositionToFluxAndVerifyMessage(asset, position);

        CustomRuleHelper.assertRuleNotTriggered(createdCustomRuleWithInterval);
        CustomRuleHelper.assertRuleTriggered(createdCustomRuleWithoutInterval, timestamp);
    }

    @Test
    public void sendFlagStateToFLUXDNKWithFutureValidRuleIntervalTest() throws Exception {
        Instant timestamp = Instant.now();

        AssetDTO asset = AssetTestHelper.createTestAsset();
        MobileTerminalDto mobileTerminal = MobileTerminalTestHelper.createMobileTerminal();
        MobileTerminalTestHelper.assignMobileTerminal(asset, mobileTerminal);

        String fluxEndpoint = "DNK";

        Calendar calendarStart = Calendar.getInstance();
        calendarStart.add(Calendar.HOUR, 1);
        Date ruleIntervalStart = calendarStart.getTime();

        Calendar calendarEnd = Calendar.getInstance();
        calendarEnd.add(Calendar.HOUR, 2);
        Date ruleIntervalEnd = calendarEnd.getTime();

        CustomRuleType flagStateRuleWithInterval = CustomRuleBuilder.getBuilder()
                .setName("Flag state => FLUX DNK")
                .rule(CriteriaType.ASSET, SubCriteriaType.FLAG_STATE,
                        ConditionType.EQ, asset.getFlagStateCode())
                .interval(ruleIntervalStart, ruleIntervalEnd)
                .action(ActionType.SEND_REPORT, VMSSystemHelper.FLUX_NAME, fluxEndpoint)
                .action(ActionType.CREATE_TICKET, "Dummy ticket")
                .build();

        CustomRuleType createdCustomRuleWithInterval = CustomRuleHelper.createCustomRule(flagStateRuleWithInterval);
        assertNotNull(createdCustomRuleWithInterval);

        CustomRuleType flagStateRuleWithoutInterval = CustomRuleBuilder.getBuilder()
                .setName("Flag state => FLUX DNK")
                .rule(CriteriaType.ASSET, SubCriteriaType.FLAG_STATE,
                        ConditionType.EQ, asset.getFlagStateCode())
                .action(ActionType.SEND_REPORT, VMSSystemHelper.FLUX_NAME, fluxEndpoint)
                .action(ActionType.CREATE_TICKET, "Dummy ticket")
                .build();

        CustomRuleType createdCustomRuleWithoutInterval = CustomRuleHelper.createCustomRule(flagStateRuleWithoutInterval);
        assertNotNull(createdCustomRuleWithoutInterval);

        LatLong position = new LatLong(11d, 56d, new Date());

        sendPositionToFluxAndVerifyMessage(asset, position);

        CustomRuleHelper.assertRuleNotTriggered(createdCustomRuleWithInterval);
        CustomRuleHelper.assertRuleTriggered(createdCustomRuleWithoutInterval, timestamp);
    }

    @Test
    public void sendToExistingNafEndpointTest() throws Exception {
        String nation = generateARandomStringWithMaxLength(9);
        String uri = "Test URI" + generateARandomStringWithMaxLength(10);
        String name = "NAF";

        Organisation organisation = UserHelper.getBasicOrganisation();
        organisation.setNation(nation);
        UserHelper.createOrganisation(organisation);
        EndPoint endpoint = new EndPoint();
        endpoint.setName(name);
        endpoint.setUri(uri);
        endpoint.setStatus("E");
        endpoint.setOrganisationName(organisation.getName());
        EndPoint createdEndpoint = UserHelper.createEndpoint(endpoint);
        Channel channel = new Channel();
        channel.setDataflow("NAF");
        channel.setService("NAF");
        channel.setPriority(1);
        channel.setEndpointId(createdEndpoint.getEndpointId());
        UserHelper.createChannel(channel);

        SetReportRequest report = VMSSystemHelper.triggerBasicRuleAndSendToNAF(organisation.getName());
        assertThat(report, is(notNullValue()));
        assertThat(report.getReport(), is(notNullValue()));

        List<RecipientInfoType> recipientInfo = report.getReport().getRecipientInfo();

        assertThat(recipientInfo.size(), is(1));
        RecipientInfoType recipientInfoType = recipientInfo.get(0);
        assertThat(recipientInfoType.getKey(), is(name));
        assertThat(recipientInfoType.getValue(), is(uri));
    }

    @Test
    public void sendToExistingFLUXEndpointTest() throws Exception {
        String nation = generateARandomStringWithMaxLength(9);
        String uri = "FLUX:" + generateARandomStringWithMaxLength(10);
        String name = "FLUX";

        Organisation organisation = UserHelper.getBasicOrganisation();
        organisation.setNation(nation);
        UserHelper.createOrganisation(organisation);
        EndPoint endpoint = new EndPoint();
        endpoint.setName(name);
        endpoint.setUri(uri);
        endpoint.setStatus("E");
        endpoint.setOrganisationName(organisation.getName());
        EndPoint createdEndpoint = UserHelper.createEndpoint(endpoint);
        Channel channel = new Channel();
        channel.setDataflow("FLUXVesselPositionMessage");
        channel.setService("FLUX");
        channel.setPriority(1);
        channel.setEndpointId(createdEndpoint.getEndpointId());
        UserHelper.createChannel(channel);

        SetReportRequest report = VMSSystemHelper.triggerBasicRuleAndSendToFlux(organisation.getName());
        assertThat(report, is(notNullValue()));
        assertThat(report.getReport(), is(notNullValue()));
        assertThat(report.getReport().getRecipient(), is(uri));

        List<RecipientInfoType> recipientInfo = report.getReport().getRecipientInfo();
        assertThat(recipientInfo.size(), is(1));
    }

    @Test
    public void sendToOrganisatioWithTwoEndpointsTest() throws Exception {
        String nation = generateARandomStringWithMaxLength(9);
        String uri = "Test URI" + generateARandomStringWithMaxLength(10);

        Organisation organisation = UserHelper.getBasicOrganisation();
        organisation.setNation(nation);
        UserHelper.createOrganisation(organisation);
        EndPoint endpoint = new EndPoint();
        endpoint.setName("FLUX");
        endpoint.setUri(uri);
        endpoint.setStatus("E");
        endpoint.setOrganisationName(organisation.getName());
        EndPoint createdEndpoint = UserHelper.createEndpoint(endpoint);
        Channel channel = new Channel();
        channel.setDataflow("FLUXVesselPositionMsg");
        channel.setService("FLUX");
        channel.setPriority(1);
        channel.setEndpointId(createdEndpoint.getEndpointId());
        UserHelper.createChannel(channel);
        EndPoint endpoint2 = new EndPoint();
        endpoint2.setName("NAF");
        endpoint2.setUri(uri);
        endpoint2.setStatus("E");
        endpoint2.setOrganisationName(organisation.getName());
        EndPoint createdEndpoint2 = UserHelper.createEndpoint(endpoint2);
        Channel channel2 = new Channel();
        channel2.setDataflow("NAF");
        channel2.setService("NAF");
        channel2.setPriority(1);
        channel2.setEndpointId(createdEndpoint2.getEndpointId());
        UserHelper.createChannel(channel2);

        SetReportRequest report = VMSSystemHelper.triggerBasicRuleAndSendToFlux(organisation.getName());
        assertThat(report, is(notNullValue()));
        assertThat(report.getReport(), is(notNullValue()));
        assertThat(report.getReport().getRecipient(), is(nation));

        List<RecipientInfoType> recipientInfo = report.getReport().getRecipientInfo();
        assertThat(recipientInfo.size(), is(2));
    }

    private void sendPositionToFluxAndVerifyMessage(AssetDTO asset, LatLong position) throws Exception {
        try (TopicListener topicListener = new TopicListener(VMSSystemHelper.FLUX_SELECTOR)) {
            FLUXHelper.sendPositionToFluxPlugin(asset, position);
            CustomRuleHelper.pollTicketCreated();

            SetReportRequest setReportRequest = topicListener.listenOnEventBusForSpecificMessage(SetReportRequest.class);
            assertThat(setReportRequest, is(notNullValue()));
        }
    }

    private void senPositionToFluxAndVerifyMessageContent(Instant timestamp, AssetDTO asset, String fluxEndpoint,
                                                          CustomRuleType createdCustomRule, LatLong position) throws Exception {

        try (TopicListener topicListener = new TopicListener(VMSSystemHelper.FLUX_SELECTOR)) {
            FLUXHelper.sendPositionToFluxPlugin(asset, position);
            CustomRuleHelper.pollTicketCreated();
            CustomRuleHelper.assertRuleTriggered(createdCustomRule, timestamp);

            SetReportRequest setReportRequest = topicListener.listenOnEventBusForSpecificMessage(SetReportRequest.class);

            assertThat(setReportRequest.getReport().getRecipient(), is(fluxEndpoint));

            MovementType movement = setReportRequest.getReport().getMovement();
            assertThat(movement.getAssetName(), is(asset.getName()));
            assertThat(movement.getIrcs(), is(asset.getIrcs()));
            assertThat(movement.getPosition().getLatitude(), is(position.latitude));
            assertThat(movement.getPosition().getLongitude(), is(position.longitude));
        }
    }
}
