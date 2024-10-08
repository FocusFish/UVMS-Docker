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
package fish.focus.uvms.docker.validation.system.rules;

import fish.focus.schema.exchange.common.v1.CommandTypeType;
import fish.focus.schema.exchange.common.v1.KeyValueType;
import fish.focus.schema.exchange.movement.v1.MovementType;
import fish.focus.schema.exchange.movement.v1.MovementTypeType;
import fish.focus.schema.exchange.plugin.v1.SetCommandRequest;
import fish.focus.schema.exchange.plugin.v1.SetReportRequest;
import fish.focus.schema.movementrules.customrule.v1.*;
import fish.focus.uvms.asset.client.model.AssetDTO;
import fish.focus.uvms.docker.validation.asset.AssetTestHelper;
import fish.focus.uvms.docker.validation.common.AbstractRest;
import fish.focus.uvms.docker.validation.common.MessageHelper;
import fish.focus.uvms.docker.validation.common.TopicListener;
import fish.focus.uvms.docker.validation.mobileterminal.MobileTerminalTestHelper;
import fish.focus.uvms.docker.validation.mobileterminal.dto.ChannelDto;
import fish.focus.uvms.docker.validation.mobileterminal.dto.MobileTerminalDto;
import fish.focus.uvms.docker.validation.movement.LatLong;
import fish.focus.uvms.docker.validation.movement.ManualMovementRestHelper;
import fish.focus.uvms.docker.validation.movement.model.ManualMovementDto;
import fish.focus.uvms.docker.validation.system.helper.*;
import fish.focus.uvms.exchange.model.mapper.JAXBMarshaller;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.jms.TextMessage;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;

public class RulesAlarmIT extends AbstractRest {

    private static MessageHelper messageHelper;

    @AfterClass
    public static void cleanup() {
        messageHelper.close();
    }

    @BeforeClass
    public static void registerEmailPluginIfNotExisting() throws Exception {
        messageHelper = new MessageHelper();
        VMSSystemHelper.registerEmailPluginIfNotExisting();
    }

    @After
    public void removeCustomRules() {
        CustomRuleHelper.removeCustomRulesByDefaultUser();
    }

    @Test
    public void sendEmailIfReportedSpeedIsGreaterThan10knotsTest() throws Exception {
        Instant timestamp = Instant.now();
        AssetDTO asset = AssetTestHelper.createTestAsset();
        String email = System.currentTimeMillis() + "@mail.com";

        CustomRuleType speedRule = CustomRuleBuilder.getBuilder()
                .setName("Speed > 10 knots => Send email")
                .rule(CriteriaType.POSITION, SubCriteriaType.REPORTED_SPEED,
                        ConditionType.GT, "10")
                .action(ActionType.EMAIL, email)
                .action(ActionType.CREATE_TICKET, "Dummy ticket")
                .build();

        CustomRuleType createdSpeedRule = CustomRuleHelper.createCustomRule(speedRule);
        assertNotNull(createdSpeedRule);

        LatLong position = new LatLong(11d, 56d, new Date());
        position.speed = 10.5;
        sendPositionToFluxAndVerifyEmailAndRuleName(asset, email, createdSpeedRule, position);

        CustomRuleHelper.assertRuleTriggered(createdSpeedRule, timestamp);
    }

    @Test
    public void sendEmailIfReportedSpeedIslessThan10knotsTest() throws Exception {
        Instant timestamp = Instant.now();
        AssetDTO asset = AssetTestHelper.createTestAsset();
        String email = System.currentTimeMillis() + "@mail.com";

        CustomRuleType speedRule = CustomRuleBuilder.getBuilder()
                .setName("Speed < 10 knots => Send email")
                .rule(CriteriaType.POSITION, SubCriteriaType.REPORTED_SPEED,
                        ConditionType.LT, "10")
                .action(ActionType.EMAIL, email)
                .action(ActionType.CREATE_TICKET, "Dummy ticket")
                .build();

        CustomRuleType createdSpeedRule = CustomRuleHelper.createCustomRule(speedRule);
        assertNotNull(createdSpeedRule);

        LatLong position = new LatLong(11d, 56d, new Date());
        position.speed = 9.5;
        sendPositionToFluxAndVerifyEmailAndRuleName(asset, email, createdSpeedRule, position);
        CustomRuleHelper.assertRuleTriggered(createdSpeedRule, timestamp);
    }

    @Test
    public void doNotTriggerRuleIfReportedSpeedIsLessThan10knotsTest() throws Exception {
        Instant timestamp = Instant.now();
        AssetDTO asset = AssetTestHelper.createTestAsset();

        CustomRuleType speedRule = CustomRuleBuilder.getBuilder()
                .setName("Speed > 10 knots => Send email")
                .rule(CriteriaType.POSITION, SubCriteriaType.REPORTED_SPEED,
                        ConditionType.GT, "10")
                .action(ActionType.EMAIL, "test@mail.com")
                .action(ActionType.CREATE_TICKET, "Dummy ticket")
                .build();

        CustomRuleType createdSpeedRule = CustomRuleHelper.createCustomRule(speedRule);
        assertNotNull(createdSpeedRule);

        String email = System.currentTimeMillis() + "@mail.com";
        CustomRuleType fsRule = CustomRuleBuilder.getBuilder()
                .setName("Flag state => Send email")
                .rule(CriteriaType.ASSET, SubCriteriaType.FLAG_STATE,
                        ConditionType.EQ, asset.getFlagStateCode())
                .action(ActionType.EMAIL, email)
                .action(ActionType.CREATE_TICKET, "Dummy ticket")
                .build();

        CustomRuleType createdFsRule = CustomRuleHelper.createCustomRule(fsRule);
        assertNotNull(createdFsRule);

        LatLong position = new LatLong(11d, 56d, new Date());
        position.speed = 9.5;
        sendPositionToFluxAndVerifyEmailAndRuleName(asset, email, createdFsRule, position);
        CustomRuleHelper.assertRuleNotTriggered(createdSpeedRule);
        CustomRuleHelper.assertRuleTriggered(createdFsRule, timestamp);
    }

    @Test
    public void doNotTriggerRuleIfReportedSpeedIsGreaterThan10knotsTest() throws Exception {
        Instant timestamp = Instant.now();
        AssetDTO asset = AssetTestHelper.createTestAsset();

        CustomRuleType speedRule = CustomRuleBuilder.getBuilder()
                .setName("Speed < 10 knots => Send email")
                .rule(CriteriaType.POSITION, SubCriteriaType.REPORTED_SPEED,
                        ConditionType.LT, "10")
                .action(ActionType.EMAIL, "test@mail.com")
                .action(ActionType.CREATE_TICKET, "Dummy ticket")
                .build();

        CustomRuleType createdSpeedRule = CustomRuleHelper.createCustomRule(speedRule);
        assertNotNull(createdSpeedRule);

        String email = System.currentTimeMillis() + "@mail.com";
        CustomRuleType fsRule = CustomRuleBuilder.getBuilder()
                .setName("Flag state => Send email")
                .rule(CriteriaType.ASSET, SubCriteriaType.FLAG_STATE,
                        ConditionType.EQ, asset.getFlagStateCode())
                .action(ActionType.EMAIL, email)
                .action(ActionType.CREATE_TICKET, "Dummy ticket")
                .build();

        CustomRuleType createdFsRule = CustomRuleHelper.createCustomRule(fsRule);
        assertNotNull(createdFsRule);

        LatLong position = new LatLong(11d, 56d, new Date());
        position.speed = 10.5;
        sendPositionToFluxAndVerifyEmailAndRuleName(asset, email, createdFsRule, position);
        CustomRuleHelper.assertRuleNotTriggered(createdSpeedRule);
        CustomRuleHelper.assertRuleTriggered(createdFsRule, timestamp);
    }

    @Test
    public void sendEmailIfReportedSpeedIsGreaterThanOrEqual10knotsTest() throws Exception {
        Instant timestamp = Instant.now();
        AssetDTO asset = AssetTestHelper.createTestAsset();
        String email = System.currentTimeMillis() + "@mail.com";

        CustomRuleType speedRule = CustomRuleBuilder.getBuilder()
                .setName("Speed >= 10 knots => Send email")
                .rule(CriteriaType.POSITION, SubCriteriaType.REPORTED_SPEED,
                        ConditionType.GE, "10")
                .action(ActionType.EMAIL, email)
                .action(ActionType.CREATE_TICKET, "Dummy ticket")
                .build();

        CustomRuleType createdSpeedRule = CustomRuleHelper.createCustomRule(speedRule);
        assertNotNull(createdSpeedRule);

        LatLong position = new LatLong(11d, 56d, new Date());
        position.speed = 10;
        sendPositionToFluxAndVerifyEmailAndRuleName(asset, email, createdSpeedRule, position);
        CustomRuleHelper.assertRuleTriggered(createdSpeedRule, timestamp);
    }

    @Test
    public void sendEmailIfReportedSpeedIsLessThanOrEqual10knotsTest() throws Exception {
        Instant timestamp = Instant.now();
        AssetDTO asset = AssetTestHelper.createTestAsset();
        String email = System.currentTimeMillis() + "@mail.com";

        CustomRuleType speedRule = CustomRuleBuilder.getBuilder()
                .setName("Speed <= 10 knots => Send email")
                .rule(CriteriaType.POSITION, SubCriteriaType.REPORTED_SPEED,
                        ConditionType.LE, "10")
                .action(ActionType.EMAIL, email)
                .action(ActionType.CREATE_TICKET, "Dummy ticket")
                .build();

        CustomRuleType createdSpeedRule = CustomRuleHelper.createCustomRule(speedRule);
        assertNotNull(createdSpeedRule);

        LatLong position = new LatLong(11d, 56d, new Date());
        position.speed = 10;
        sendPositionToFluxAndVerifyEmailAndRuleName(asset, email, createdSpeedRule, position);
        CustomRuleHelper.assertRuleTriggered(createdSpeedRule, timestamp);
    }

    @Test
    public void sendEmailIfReportedSpeedIsGreaterThan10knotsAndAreaIsDNKTest() throws Exception {
        Instant timestamp = Instant.now();
        AssetDTO asset = AssetTestHelper.createTestAsset();
        String email = System.currentTimeMillis() + "@mail.com";

        CustomRuleType speedAndAreaRule = CustomRuleBuilder.getBuilder()
                .setName("Sp > 10 && area = DNK => Send email")
                .rule(CriteriaType.POSITION, SubCriteriaType.REPORTED_SPEED,
                        ConditionType.GT, "10")
                .and(CriteriaType.AREA, SubCriteriaType.AREA_CODE,
                        ConditionType.EQ, "DNK")
                .action(ActionType.EMAIL, email)
                .action(ActionType.CREATE_TICKET, "Dummy ticket")
                .build();

        CustomRuleType createdSpeedAndAreaRule = CustomRuleHelper.createCustomRule(speedAndAreaRule);
        assertNotNull(createdSpeedAndAreaRule);

        LatLong position = new LatLong(56d, 10.5, new Date());
        position.speed = 10.5;
        sendPositionToFluxAndVerifyEmailAndRuleName(asset, email, createdSpeedAndAreaRule, position);
        CustomRuleHelper.assertRuleTriggered(createdSpeedAndAreaRule, timestamp);
    }

    @Test
    public void sendEmailIfReportedSpeedIsLessThan10knotsAndAreaIsDNKTest() throws Exception {
        Instant timestamp = Instant.now();
        AssetDTO asset = AssetTestHelper.createTestAsset();
        String email = System.currentTimeMillis() + "@mail.com";

        CustomRuleType speedAndAreaRule = CustomRuleBuilder.getBuilder()
                .setName("Sp < 10 && area = DNK => Send email")
                .rule(CriteriaType.POSITION, SubCriteriaType.REPORTED_SPEED,
                        ConditionType.LT, "10")
                .and(CriteriaType.AREA, SubCriteriaType.AREA_CODE,
                        ConditionType.EQ, "DNK")
                .action(ActionType.EMAIL, email)
                .action(ActionType.CREATE_TICKET, "Dummy ticket")
                .build();

        CustomRuleType createdSpeedAndAreaRule = CustomRuleHelper.createCustomRule(speedAndAreaRule);
        assertNotNull(createdSpeedAndAreaRule);

        LatLong position = new LatLong(56d, 10.5, new Date());
        position.speed = 9.5;
        sendPositionToFluxAndVerifyEmailAndRuleName(asset, email, createdSpeedAndAreaRule, position);
        CustomRuleHelper.assertRuleTriggered(createdSpeedAndAreaRule, timestamp);
    }

    @Test
    public void sendEmailIfAreaCodeIsDEUTest() throws Exception {
        Instant timestamp = Instant.now();
        AssetDTO asset = AssetTestHelper.createTestAsset();
        String email = System.currentTimeMillis() + "@mail.com";

        CustomRuleType areaRule = CustomRuleBuilder.getBuilder()
                .setName("Area = DEU => Send email")
                .rule(CriteriaType.AREA, SubCriteriaType.AREA_CODE,
                        ConditionType.EQ, "DEU")
                .action(ActionType.EMAIL, email)
                .action(ActionType.CREATE_TICKET, "Dummy ticket")
                .build();

        CustomRuleType createdAreaRule = CustomRuleHelper.createCustomRule(areaRule);
        assertNotNull(createdAreaRule);

        LatLong position = new LatLong(54.528352, 12.877972, new Date());
        sendPositionToFluxAndVerifyEmailAndRuleName(asset, email, createdAreaRule, position);
        CustomRuleHelper.assertRuleTriggered(createdAreaRule, timestamp);
    }

    @Test
    public void sendEmailIfAssetIRCSMatchesTest() throws Exception {
        Instant timestamp = Instant.now();
        AssetDTO asset = AssetTestHelper.createTestAsset();
        String email = System.currentTimeMillis() + "@mail.com";

        CustomRuleType ircsRule = CustomRuleBuilder.getBuilder()
                .setName("IRCS => Send email")
                .rule(CriteriaType.ASSET, SubCriteriaType.ASSET_IRCS,
                        ConditionType.EQ, asset.getIrcs())
                .action(ActionType.EMAIL, email)
                .action(ActionType.CREATE_TICKET, "Dummy ticket")
                .build();

        CustomRuleType createdIrcsRule = CustomRuleHelper.createCustomRule(ircsRule);
        assertNotNull(createdIrcsRule);

        LatLong position = new LatLong(1d, 1d, new Date());
        sendPositionToFluxAndVerifyEmailAndRuleName(asset, email, createdIrcsRule, position);
        CustomRuleHelper.assertRuleTriggered(createdIrcsRule, timestamp);
    }

    @Test
    public void sendEmailIfIrcsDisjunctionMatchesTest() throws Exception {
        Instant timestamp = Instant.now();
        AssetDTO asset1 = AssetTestHelper.createTestAsset();
        AssetDTO asset2 = AssetTestHelper.createTestAsset();
        String email = System.currentTimeMillis() + "@mail.com";

        CustomRuleType customRule = CustomRuleBuilder.getBuilder()
                .rule(CriteriaType.ASSET, SubCriteriaType.ASSET_IRCS,
                        ConditionType.EQ, asset1.getIrcs())
                .or(CriteriaType.ASSET, SubCriteriaType.ASSET_IRCS,
                        ConditionType.EQ, asset2.getIrcs())
                .action(ActionType.EMAIL, email)
                .action(ActionType.CREATE_TICKET, "Dummy ticket")
                .build();

        CustomRuleType createdCustomRule = CustomRuleHelper.createCustomRule(customRule);
        assertNotNull(createdCustomRule);

        LatLong position = new LatLong(1d, 1d, new Date());
        sendPositionToFluxAndVerifyEmailAndRuleName(asset1, email, createdCustomRule, position);

        CustomRuleHelper.assertRuleTriggered(createdCustomRule, timestamp);

        timestamp = Instant.now();

        LatLong position2 = new LatLong(2d, 2d, new Date());
        sendPositionToFluxAndVerifyEmailAndRuleName(asset2, email, createdCustomRule, position2);
        CustomRuleHelper.assertRuleTriggered(createdCustomRule, timestamp);
    }

    @Test
    public void sendEmailIfIrcsCfrConjunctionMatchesTest() throws Exception {
        Instant timestamp = Instant.now();
        AssetDTO asset = AssetTestHelper.createTestAsset();
        String email = System.currentTimeMillis() + "@mail.com";

        CustomRuleType customRule = CustomRuleBuilder.getBuilder()
                .rule(CriteriaType.ASSET, SubCriteriaType.ASSET_IRCS,
                        ConditionType.EQ, asset.getIrcs())
                .and(CriteriaType.ASSET, SubCriteriaType.ASSET_CFR,
                        ConditionType.EQ, asset.getCfr())
                .action(ActionType.EMAIL, email)
                .action(ActionType.CREATE_TICKET, "Dummy ticket")
                .build();

        CustomRuleType createdCustomRule = CustomRuleHelper.createCustomRule(customRule);
        assertNotNull(createdCustomRule);

        LatLong position = new LatLong(1d, 1d, new Date());
        sendPositionToFluxAndVerifyEmailAndRuleName(asset, email, createdCustomRule, position);
        CustomRuleHelper.assertRuleTriggered(createdCustomRule, timestamp);
    }

    @Test
    public void doNotTriggerRuleIfIrcsCfrConjunctionNotMatchesTest() throws Exception {
        Instant timestamp = Instant.now();
        AssetDTO asset = AssetTestHelper.createTestAsset();

        CustomRuleType customRule = CustomRuleBuilder.getBuilder()
                .rule(CriteriaType.ASSET, SubCriteriaType.ASSET_IRCS,
                        ConditionType.EQ, asset.getIrcs())
                .and(CriteriaType.ASSET, SubCriteriaType.ASSET_CFR,
                        ConditionType.EQ, "MOCKCFR")
                .action(ActionType.EMAIL, "test@mail.com")
                .action(ActionType.CREATE_TICKET, "Dummy ticket")
                .build();

        CustomRuleType createdCustomRule = CustomRuleHelper.createCustomRule(customRule);
        assertNotNull(createdCustomRule);

        String email = System.currentTimeMillis() + "@mail.com";
        CustomRuleType fsRule = CustomRuleBuilder.getBuilder()
                .rule(CriteriaType.ASSET, SubCriteriaType.FLAG_STATE,
                        ConditionType.EQ, asset.getFlagStateCode())
                .action(ActionType.EMAIL, email)
                .action(ActionType.CREATE_TICKET, "Dummy ticket")
                .build();

        CustomRuleType createdFsRule = CustomRuleHelper.createCustomRule(fsRule);
        assertNotNull(createdFsRule);

        LatLong position = new LatLong(1d, 1d, new Date());
        sendPositionToFluxAndVerifyEmailAndRuleName(asset, email, createdFsRule, position);
        CustomRuleHelper.assertRuleNotTriggered(createdCustomRule);
        CustomRuleHelper.assertRuleTriggered(createdFsRule, timestamp);
    }

    @Test
    public void sendEmailIfAssetCFRMatchesTest() throws Exception {
        Instant timestamp = Instant.now();
        AssetDTO asset = AssetTestHelper.createTestAsset();
        String email = System.currentTimeMillis() + "@mail.com";

        CustomRuleType cfrRule = CustomRuleBuilder.getBuilder()
                .setName("CFR => Send email")
                .rule(CriteriaType.ASSET, SubCriteriaType.ASSET_CFR,
                        ConditionType.EQ, asset.getCfr())
                .action(ActionType.EMAIL, email)
                .action(ActionType.CREATE_TICKET, "Dummy ticket")
                .build();

        CustomRuleType createdCfrRule = CustomRuleHelper.createCustomRule(cfrRule);
        assertNotNull(createdCfrRule);

        LatLong position = new LatLong(1d, 1d, new Date());
        sendPositionToFluxAndVerifyEmailAndRuleName(asset, email, createdCfrRule, position);
        CustomRuleHelper.assertRuleTriggered(createdCfrRule, timestamp);
    }

    @Test
    public void sendEmailIfAssetNameMatchesTest() throws Exception {
        Instant timestamp = Instant.now();
        AssetDTO asset = AssetTestHelper.createTestAsset();
        String email = System.currentTimeMillis() + "@mail.com";

        CustomRuleType assetNameRule = CustomRuleBuilder.getBuilder()
                .setName("Asset name => Send email")
                .rule(CriteriaType.ASSET, SubCriteriaType.ASSET_NAME,
                        ConditionType.EQ, asset.getName())
                .action(ActionType.EMAIL, email)
                .action(ActionType.CREATE_TICKET, "Dummy ticket")
                .build();

        CustomRuleType createdAssetNameRule = CustomRuleHelper.createCustomRule(assetNameRule);
        assertNotNull(createdAssetNameRule);

        LatLong position = new LatLong(1d, 1d, new Date());
        sendPositionToFluxAndVerifyEmailAndRuleName(asset, email, createdAssetNameRule, position);
        CustomRuleHelper.assertRuleTriggered(createdAssetNameRule, timestamp);
    }

    @Test
    public void sendEmailIfLatitudeIsGreaterThan10Test() throws Exception {
        Instant timestamp = Instant.now();
        AssetDTO asset = AssetTestHelper.createTestAsset();
        String email = System.currentTimeMillis() + "@mail.com";

        CustomRuleType customRule = CustomRuleBuilder.getBuilder()
                .rule(CriteriaType.POSITION, SubCriteriaType.LATITUDE,
                        ConditionType.GT, "10")
                .action(ActionType.EMAIL, email)
                .action(ActionType.CREATE_TICKET, "Dummy ticket")
                .build();

        CustomRuleType createdCustomRule = CustomRuleHelper.createCustomRule(customRule);
        assertNotNull(createdCustomRule);

        LatLong position = new LatLong(11d, 1d, new Date());
        sendPositionToFluxAndVerifyEmailAndRuleName(asset, email, createdCustomRule, position);
        CustomRuleHelper.assertRuleTriggered(createdCustomRule, timestamp);
    }

    @Test
    public void sendEmailIfLongitudeIsGreaterThan10Test() throws Exception {
        Instant timestamp = Instant.now();
        AssetDTO asset = AssetTestHelper.createTestAsset();
        String email = System.currentTimeMillis() + "@mail.com";

        CustomRuleType customRule = CustomRuleBuilder.getBuilder()
                .rule(CriteriaType.POSITION, SubCriteriaType.LONGITUDE,
                        ConditionType.GT, "10")
                .action(ActionType.EMAIL, email)
                .action(ActionType.CREATE_TICKET, "Dummy ticket")
                .build();

        CustomRuleType createdCustomRule = CustomRuleHelper.createCustomRule(customRule);
        assertNotNull(createdCustomRule);

        LatLong position = new LatLong(1d, 11d, new Date());
        sendPositionToFluxAndVerifyEmailAndRuleName(asset, email, createdCustomRule, position);
        CustomRuleHelper.assertRuleTriggered(createdCustomRule, timestamp);
    }

    @Test
    public void sendEmailIfPositionReportTimeIsGreaterOrEqualTest() throws Exception {
        Instant timestamp = Instant.now();
        AssetDTO asset = AssetTestHelper.createTestAsset();
        String email = System.currentTimeMillis() + "@mail.com";

        CustomRuleType customRule = CustomRuleBuilder.getBuilder()
                .rule(CriteriaType.POSITION, SubCriteriaType.POSITION_REPORT_TIME,
                        ConditionType.GE, "" + timestamp.toEpochMilli())
                .action(ActionType.EMAIL, email)
                .action(ActionType.CREATE_TICKET, "Dummy ticket")
                .build();

        CustomRuleType createdCustomRule = CustomRuleHelper.createCustomRule(customRule);
        assertNotNull(createdCustomRule);

        LatLong position = new LatLong(1d, 1d, new Date());
        sendPositionToFluxAndVerifyEmailAndRuleName(asset, email, createdCustomRule, position);
        CustomRuleHelper.assertRuleTriggered(createdCustomRule, timestamp);
    }

    @Test
    public void triggerAreaEntryRule() throws Exception {
        Instant timestamp = Instant.now();
        AssetDTO asset = AssetTestHelper.createTestAsset();
        LatLong positionSwe = new LatLong(57.670176, 11.799626, new Date());
        try (TopicListener topicListener = new TopicListener(TopicListener.EVENT_STREAM, "event = 'Movement'")) {
            FLUXHelper.sendPositionToFluxPlugin(asset, positionSwe);
            topicListener.listenOnEventBus();
        }
        String fluxEndpoint = "DNK";

        CustomRuleType areaEntryRule = CustomRuleBuilder.getBuilder()
                .setName("Area entry DNK")
                .rule(CriteriaType.AREA, SubCriteriaType.AREA_CODE_ENT,
                        ConditionType.EQ, "DNK")
                .action(ActionType.SEND_REPORT, VMSSystemHelper.FLUX_NAME, fluxEndpoint)
                .action(ActionType.CREATE_TICKET, "Dummy ticket")
                .build();

        CustomRuleType createdAreaRule = CustomRuleHelper.createCustomRule(areaEntryRule);
        assertNotNull(createdAreaRule);

        LatLong positionDnk = new LatLong(56d, 10.5, new Date());
        sendPositionToFluxAndVerifyAssetInfo(asset, fluxEndpoint, positionDnk);
        CustomRuleHelper.assertRuleTriggered(createdAreaRule, timestamp);
    }

    @Test
    public void doNotTriggerAreaEntryRule() throws Exception {
        Instant timestamp = Instant.now();
        AssetDTO asset = AssetTestHelper.createTestAsset();
        LatLong positionSwe = new LatLong(57.670176, 11.799626, new Date());
        try (TopicListener topicListener = new TopicListener(TopicListener.EVENT_STREAM, "event = 'Movement'")) {
            FLUXHelper.sendPositionToFluxPlugin(asset, positionSwe);
            topicListener.listenOnEventBus();
        }
        String fluxEndpoint = "DNK";

        Thread.sleep(1500);     //since sometimes we are fast enough to create the rule b4 the previous position as cleared MR

        CustomRuleType areaEntryRule = CustomRuleBuilder.getBuilder()
                .setName("Area entry SWE")
                .rule(CriteriaType.AREA, SubCriteriaType.AREA_CODE_ENT,
                        ConditionType.EQ, "SWE")
                .action(ActionType.SEND_REPORT, VMSSystemHelper.FLUX_NAME, fluxEndpoint)
                .action(ActionType.CREATE_TICKET, "Dummy ticket")
                .build();

        CustomRuleType createdAreaRule = CustomRuleHelper.createCustomRule(areaEntryRule);
        assertNotNull(createdAreaRule);

        String email = System.currentTimeMillis() + "@mail.com";
        CustomRuleType fsRule = CustomRuleBuilder.getBuilder()
                .setName("Flag state => Send email")
                .rule(CriteriaType.ASSET, SubCriteriaType.FLAG_STATE,
                        ConditionType.EQ, asset.getFlagStateCode())
                .action(ActionType.EMAIL, email)
                .action(ActionType.CREATE_TICKET, "Dummy ticket")
                .build();

        CustomRuleType createdFsRule = CustomRuleHelper.createCustomRule(fsRule);
        assertNotNull(createdFsRule);

        LatLong positionSwe2 = new LatLong(57.670176, 11.799626, new Date());

        sendPositionToFluxAndVerifyEmailAndRuleName(asset, email, createdFsRule, positionSwe2);

        CustomRuleHelper.assertRuleNotTriggered(createdAreaRule);
        CustomRuleHelper.assertRuleTriggered(createdFsRule, timestamp);
    }

    @Test
    public void triggerAreaExitRule() throws Exception {
        Instant timestamp = Instant.now();
        AssetDTO asset = AssetTestHelper.createTestAsset();
        LatLong positionSwe = new LatLong(57.670176, 11.799626, new Date());
        try (TopicListener topicListener = new TopicListener(TopicListener.EVENT_STREAM, "event = 'Movement'")) {
            FLUXHelper.sendPositionToFluxPlugin(asset, positionSwe);
            topicListener.listenOnEventBus();
        }
        String fluxEndpoint = "DNK";

        CustomRuleType areaEntryRule = CustomRuleBuilder.getBuilder()
                .setName("Area exit SWE")
                .rule(CriteriaType.AREA, SubCriteriaType.AREA_CODE_EXT,
                        ConditionType.EQ, "SWE")
                .action(ActionType.SEND_REPORT, VMSSystemHelper.FLUX_NAME, fluxEndpoint)
                .action(ActionType.CREATE_TICKET, "Dummy ticket")
                .build();

        CustomRuleType createdAreaRule = CustomRuleHelper.createCustomRule(areaEntryRule);
        assertNotNull(createdAreaRule);

        LatLong positionDnk = new LatLong(56d, 10.5, new Date());
        sendPositionToFluxAndVerifyAssetInfo(asset, fluxEndpoint, positionDnk);
        CustomRuleHelper.assertRuleTriggered(createdAreaRule, timestamp);
    }

    @Test
    public void triggerAreaVMSEntryRuleFirstPosition() throws Exception {
        Instant timestamp = Instant.now();
        AssetDTO asset = AssetTestHelper.createTestAsset();
        String fluxEndpoint = "DNK";

        CustomRuleType areaEntryRule = CustomRuleBuilder.getBuilder()
                .setName("Area entry DNK")
                .rule(CriteriaType.AREA, SubCriteriaType.AREA_CODE_VMS_ENT,
                        ConditionType.EQ, "DNK")
                .action(ActionType.SEND_REPORT, VMSSystemHelper.FLUX_NAME, fluxEndpoint)
                .action(ActionType.CREATE_TICKET, "Dummy ticket")
                .build();

        CustomRuleType createdAreaRule = CustomRuleHelper.createCustomRule(areaEntryRule);
        assertNotNull(createdAreaRule);

        LatLong positionDnk = new LatLong(56d, 10.5, new Date());
        sendPositionToFluxAndVerifyAssetInfo(asset, fluxEndpoint, positionDnk);
        CustomRuleHelper.assertRuleTriggered(createdAreaRule, timestamp);
    }

    @Test
    public void triggerAreaVMSEntryRule() throws Exception {
        Instant timestamp = Instant.now();
        AssetDTO asset = AssetTestHelper.createTestAsset();
        LatLong positionSwe = new LatLong(57.670176, 11.799626, new Date());
        try (TopicListener topicListener = new TopicListener(TopicListener.EVENT_STREAM, "event = 'Movement'")) {
            FLUXHelper.sendPositionToFluxPlugin(asset, positionSwe);
            topicListener.listenOnEventBus();
        }
        String fluxEndpoint = "DNK";

        CustomRuleType areaEntryRule = CustomRuleBuilder.getBuilder()
                .setName("Area entry DNK")
                .rule(CriteriaType.AREA, SubCriteriaType.AREA_CODE_VMS_ENT,
                        ConditionType.EQ, "DNK")
                .action(ActionType.SEND_REPORT, VMSSystemHelper.FLUX_NAME, fluxEndpoint)
                .action(ActionType.CREATE_TICKET, "Dummy ticket")
                .build();

        CustomRuleType createdAreaRule = CustomRuleHelper.createCustomRule(areaEntryRule);
        assertNotNull(createdAreaRule);

        LatLong positionDnk = new LatLong(56d, 10.5, new Date());
        sendPositionToFluxAndVerifyAssetInfo(asset, fluxEndpoint, positionDnk);
        CustomRuleHelper.assertRuleTriggered(createdAreaRule, timestamp);
    }


    @Test
    public void triggerAreaEntryRuleAndAreaVMSEntryRule() throws Exception {
        Instant timestamp = Instant.now();
        AssetDTO asset = AssetTestHelper.createTestAsset();
        LatLong positionSwe = new LatLong(57.670176, 11.799626, new Date());
        try (TopicListener topicListener = new TopicListener(TopicListener.EVENT_STREAM, "event = 'Movement'")) {
            FLUXHelper.sendPositionToFluxPlugin(asset, positionSwe);
            topicListener.listenOnEventBus();
        }
        String fluxEndpoint = "DNK";

        CustomRuleType areaEntryRule = CustomRuleBuilder.getBuilder()
                .setName("Area entry DNK")
                .rule(CriteriaType.AREA, SubCriteriaType.AREA_CODE_ENT,
                        ConditionType.EQ, "DNK")
                .action(ActionType.SEND_REPORT, VMSSystemHelper.FLUX_NAME, fluxEndpoint)
                .action(ActionType.CREATE_TICKET, "Dummy ticket")
                .build();

        CustomRuleType createdAreaRule = CustomRuleHelper.createCustomRule(areaEntryRule);
        assertNotNull(createdAreaRule);

        LatLong positionDnk = new LatLong(56d, 10.5, new Date());
        SetReportRequest fluxReport = sendAisPositionAndGetFluxReport(asset, fluxEndpoint, positionDnk);
        MovementType position = fluxReport.getReport().getMovement();
        assertThat(position.getAssetName(), is(asset.getName()));
        assertThat(position.getPosition().getLatitude(), is(positionDnk.latitude));
        assertThat(position.getPosition().getLongitude(), is(positionDnk.longitude));

        CustomRuleHelper.assertRuleTriggered(createdAreaRule, timestamp);

        CustomRuleType areaVMSEntryRule = CustomRuleBuilder.getBuilder()
                .setName("Area VMS entry DNK")
                .rule(CriteriaType.AREA, SubCriteriaType.AREA_CODE_VMS_ENT,
                        ConditionType.EQ, "DNK")
                .action(ActionType.SEND_REPORT, VMSSystemHelper.FLUX_NAME, fluxEndpoint)
                .action(ActionType.CREATE_TICKET, "Dummy ticket")
                .build();

        CustomRuleType createdVMSAreaRule = CustomRuleHelper.createCustomRule(areaVMSEntryRule);
        assertNotNull(createdVMSAreaRule);

        LatLong vmsPositionDnk = new LatLong(56d, 10.5, new Date());
        sendPositionToFluxAndVerifyAssetInfo(asset, fluxEndpoint, vmsPositionDnk);
        CustomRuleHelper.assertRuleTriggered(createdVMSAreaRule, timestamp);
    }

    @Test
    public void triggerAreaEntryRuleWithAisPosition() throws Exception {
        Instant timestamp = Instant.now();
        AssetDTO asset = AssetTestHelper.createTestAsset();
        LatLong positionSwe = new LatLong(57.670176, 11.799626, new Date());
        try (TopicListener topicListener = new TopicListener(TopicListener.EVENT_STREAM, "event = 'Movement'")) {
            FLUXHelper.sendPositionToFluxPlugin(asset, positionSwe);
            topicListener.listenOnEventBus();
        }
        String fluxEndpoint = "DNK";

        CustomRuleType areaEntryRule = CustomRuleBuilder.getBuilder()
                .setName("Area entry DNK")
                .rule(CriteriaType.AREA, SubCriteriaType.AREA_CODE_ENT,
                        ConditionType.EQ, "DNK")
                .action(ActionType.SEND_REPORT, VMSSystemHelper.FLUX_NAME, fluxEndpoint)
                .action(ActionType.CREATE_TICKET, "Dummy ticket")
                .build();

        CustomRuleType createdAreaRule = CustomRuleHelper.createCustomRule(areaEntryRule);
        assertNotNull(createdAreaRule);

        LatLong positionDnk = new LatLong(56d, 10.5, new Date());
        SetReportRequest fluxReport = sendAisPositionAndGetFluxReport(asset, fluxEndpoint, positionDnk);
        MovementType position = fluxReport.getReport().getMovement();
        assertThat(position.getAssetName(), is(asset.getName()));
        assertThat(position.getPosition().getLatitude(), is(positionDnk.latitude));
        assertThat(position.getPosition().getLongitude(), is(positionDnk.longitude));

        CustomRuleHelper.assertRuleTriggered(createdAreaRule, timestamp);
    }

    @Test
    public void triggerAreaVMSEntryRuleWithInmarsatPositions() throws Exception {
        Instant timestamp = Instant.now();
        AssetDTO asset = AssetTestHelper.createTestAsset();
        MobileTerminalDto mobileTerminal = MobileTerminalTestHelper.createMobileTerminal();
        MobileTerminalTestHelper.assignMobileTerminal(asset, mobileTerminal);
        LatLong positionSwe = new LatLong(57.670176, 11.799626, new Date());
        try (TopicListener topicListener = new TopicListener(TopicListener.EVENT_STREAM, "event = 'Movement'")) {
            InmarsatPluginMock.sendInmarsatPosition(mobileTerminal, positionSwe);
            topicListener.listenOnEventBus();
        }
        String fluxEndpoint = "DNK";

        CustomRuleType areaEntryRule = CustomRuleBuilder.getBuilder()
                .setName("Area entry DNK")
                .rule(CriteriaType.AREA, SubCriteriaType.AREA_CODE_VMS_ENT,
                        ConditionType.EQ, "DNK")
                .action(ActionType.SEND_REPORT, VMSSystemHelper.FLUX_NAME, fluxEndpoint)
                .action(ActionType.CREATE_TICKET, "Dummy ticket")
                .build();

        CustomRuleType createdAreaRule = CustomRuleHelper.createCustomRule(areaEntryRule);
        assertNotNull(createdAreaRule);

        LatLong positionDnk = new LatLong(56d, 10.5, new Date());
        try (TopicListener topicListener = new TopicListener(VMSSystemHelper.FLUX_SELECTOR)) {
            InmarsatPluginMock.sendInmarsatPosition(mobileTerminal, positionDnk);
            CustomRuleHelper.pollTicketCreated();

            SetReportRequest setReportRequest = topicListener.listenOnEventBusForSpecificMessage(SetReportRequest.class);
            assertThat(setReportRequest.getReport().getRecipient(), is(fluxEndpoint));
            MovementType position = setReportRequest.getReport().getMovement();
            assertThat(position.getAssetName(), is(asset.getName()));
            assertThat(position.getPosition().getLatitude(), is(positionDnk.latitude));
            assertThat(position.getPosition().getLongitude(), is(positionDnk.longitude));
        }
        CustomRuleHelper.assertRuleTriggered(createdAreaRule, timestamp);
    }

    @Test
    public void triggerAreaVMSEntryRuleWithManualPosition() throws Exception {
        Instant timestamp = Instant.now();
        AssetDTO asset = AssetTestHelper.createTestAsset();
        MobileTerminalDto mobileTerminal = MobileTerminalTestHelper.createMobileTerminal();
        MobileTerminalTestHelper.assignMobileTerminal(asset, mobileTerminal);
        LatLong positionSwe = new LatLong(57.670176, 11.799626, new Date());
        try (TopicListener topicListener = new TopicListener(TopicListener.EVENT_STREAM, "event = 'Movement'")) {
            InmarsatPluginMock.sendInmarsatPosition(mobileTerminal, positionSwe);
            topicListener.listenOnEventBus();
        }
        String fluxEndpoint = "DNK";

        CustomRuleType areaEntryRule = CustomRuleBuilder.getBuilder()
                .setName("Area entry DNK")
                .rule(CriteriaType.AREA, SubCriteriaType.AREA_CODE_VMS_ENT,
                        ConditionType.EQ, "DNK")
                .action(ActionType.SEND_REPORT, VMSSystemHelper.FLUX_NAME, fluxEndpoint)
                .action(ActionType.CREATE_TICKET, "Dummy ticket")
                .build();

        CustomRuleType createdAreaRule = CustomRuleHelper.createCustomRule(areaEntryRule);
        assertNotNull(createdAreaRule);

        LatLong positionDnk = new LatLong(56d, 10.5, new Date());
        try (TopicListener topicListener = new TopicListener(VMSSystemHelper.FLUX_SELECTOR)) {
            ManualMovementDto manualMovement = ManualMovementRestHelper.mapToManualMovement(positionDnk, asset);
            ManualMovementRestHelper.sendManualMovement(manualMovement);
            CustomRuleHelper.pollTicketCreated();

            SetReportRequest setReportRequest = topicListener.listenOnEventBusForSpecificMessage(SetReportRequest.class);
            assertThat(setReportRequest.getReport().getRecipient(), is(fluxEndpoint));
            MovementType position = setReportRequest.getReport().getMovement();
            assertThat(position.getAssetName(), is(asset.getName()));
            assertThat(position.getPosition().getLatitude(), is(positionDnk.latitude));
            assertThat(position.getPosition().getLongitude(), is(positionDnk.longitude));
            assertThat(position.getMovementType(), is(MovementTypeType.MAN));
        }
        CustomRuleHelper.assertRuleTriggered(createdAreaRule, timestamp);
    }

    @Test
    public void doNotTriggerAreaVMSEntryRuleWithExtraManualPosition() throws Exception {
        Instant timestamp = Instant.now();
        AssetDTO asset = AssetTestHelper.createTestAsset();
        MobileTerminalDto mobileTerminal = MobileTerminalTestHelper.createMobileTerminal();
        MobileTerminalTestHelper.assignMobileTerminal(asset, mobileTerminal);
        LatLong positionSwe = new LatLong(57.670176, 11.799626, new Date());
        try (TopicListener topicListener = new TopicListener(TopicListener.EVENT_STREAM, "event = 'Movement'")) {
            InmarsatPluginMock.sendInmarsatPosition(mobileTerminal, positionSwe);
            topicListener.listenOnEventBus();
        }
        String fluxEndpoint = "DNK";

        CustomRuleType areaEntryRule = CustomRuleBuilder.getBuilder()
                .setName("Area entry DNK")
                .rule(CriteriaType.AREA, SubCriteriaType.AREA_CODE_VMS_ENT, ConditionType.EQ, "DNK")
                .and(CriteriaType.POSITION, SubCriteriaType.SOURCE, ConditionType.EQ, "FLUX")
                .action(ActionType.SEND_REPORT, VMSSystemHelper.FLUX_NAME, fluxEndpoint)
                .action(ActionType.CREATE_TICKET, "Dummy ticket")
                .build();

        CustomRuleType createdAreaRule = CustomRuleHelper.createCustomRule(areaEntryRule);
        assertNotNull(createdAreaRule);

        LatLong positionDnk = new LatLong(56d, 10.5, new Date());
        sendPositionToFluxAndVerifyAssetInfo(asset, fluxEndpoint, positionDnk);
        CustomRuleHelper.assertRuleTriggered(createdAreaRule, timestamp);

        CustomRuleType areaEntryManualRule = CustomRuleBuilder.getBuilder()
                .setName("Area entry DNK")
                .rule(CriteriaType.AREA, SubCriteriaType.AREA_CODE_VMS_ENT, ConditionType.EQ, "DNK")
                .and(CriteriaType.POSITION, SubCriteriaType.SOURCE, ConditionType.EQ, "MANUAL")
                .action(ActionType.SEND_REPORT, VMSSystemHelper.FLUX_NAME, fluxEndpoint)
                .action(ActionType.CREATE_TICKET, "Dummy ticket")
                .build();

        CustomRuleType createdAreaManualRule = CustomRuleHelper.createCustomRule(areaEntryManualRule);
        assertNotNull(createdAreaManualRule);

        CustomRuleType extraRule = CustomRuleBuilder.getBuilder()
                .rule(CriteriaType.ASSET, SubCriteriaType.FLAG_STATE, ConditionType.EQ, "SWE")
                .action(ActionType.SEND_REPORT, VMSSystemHelper.FLUX_NAME, fluxEndpoint)
                .action(ActionType.CREATE_TICKET, "Dummy ticket")
                .build();

        CustomRuleType createdExtraRule = CustomRuleHelper.createCustomRule(extraRule);
        assertNotNull(createdExtraRule);

        LatLong manualPositionDnk = new LatLong(56d, 10.5, new Date());
        try (TopicListener topicListener = new TopicListener(VMSSystemHelper.FLUX_SELECTOR)) {
            ManualMovementDto manualMovement = ManualMovementRestHelper.mapToManualMovement(manualPositionDnk, asset);
            ManualMovementRestHelper.sendManualMovement(manualMovement);
            CustomRuleHelper.pollTicketCreated();

            SetReportRequest setReportRequest = topicListener.listenOnEventBusForSpecificMessage(SetReportRequest.class);
            assertThat(setReportRequest.getReport().getRecipient(), is(fluxEndpoint));
            MovementType position = setReportRequest.getReport().getMovement();
            assertThat(position.getAssetName(), is(asset.getName()));
            assertThat(position.getPosition().getLatitude(), is(manualPositionDnk.latitude));
            assertThat(position.getPosition().getLongitude(), is(manualPositionDnk.longitude));
            assertThat(position.getMovementType(), is(MovementTypeType.MAN));
        }

        CustomRuleHelper.assertRuleNotTriggered(createdAreaManualRule);
        CustomRuleHelper.assertRuleTriggered(createdExtraRule, timestamp);
    }

    @Test
    public void triggerAreaVMSExitRule() throws Exception {
        Instant timestamp = Instant.now();
        AssetDTO asset = AssetTestHelper.createTestAsset();
        LatLong positionSwe = new LatLong(57.670176, 11.799626, new Date());
        try (TopicListener topicListener = new TopicListener(TopicListener.EVENT_STREAM, "event = 'Movement'")) {
            FLUXHelper.sendPositionToFluxPlugin(asset, positionSwe);
            topicListener.listenOnEventBus();
        }
        String fluxEndpoint = "DNK";

        CustomRuleType areaEntryRule = CustomRuleBuilder.getBuilder()
                .setName("Area exit SWE")
                .rule(CriteriaType.AREA, SubCriteriaType.AREA_CODE_VMS_EXT,
                        ConditionType.EQ, "SWE")
                .action(ActionType.SEND_REPORT, VMSSystemHelper.FLUX_NAME, fluxEndpoint)
                .action(ActionType.CREATE_TICKET, "Dummy ticket")
                .build();

        CustomRuleType createdAreaRule = CustomRuleHelper.createCustomRule(areaEntryRule);
        assertNotNull(createdAreaRule);

        LatLong positionDnk = new LatLong(56d, 10.5, new Date());
        sendPositionToFluxAndVerifyAssetInfo(asset, fluxEndpoint, positionDnk);
        CustomRuleHelper.assertRuleTriggered(createdAreaRule, timestamp);
    }

    @Test
    public void triggerAreaExitRuleAndAreaVMSExitRule() throws Exception {
        Instant timestamp = Instant.now();
        AssetDTO asset = AssetTestHelper.createTestAsset();
        LatLong positionSwe = new LatLong(57.670176, 11.799626, new Date());
        try (TopicListener topicListener = new TopicListener(TopicListener.EVENT_STREAM, "event = 'Movement'")) {
            FLUXHelper.sendPositionToFluxPlugin(asset, positionSwe);
            topicListener.listenOnEventBus();
        }
        String fluxEndpoint = "DNK";

        CustomRuleType areaExitRule = CustomRuleBuilder.getBuilder()
                .setName("Area Exit")
                .rule(CriteriaType.AREA, SubCriteriaType.AREA_CODE_EXT,
                        ConditionType.EQ, "SWE")
                .action(ActionType.SEND_REPORT, VMSSystemHelper.FLUX_NAME, fluxEndpoint)
                .action(ActionType.CREATE_TICKET, "Dummy ticket")
                .build();

        CustomRuleType createdAreaExitRule = CustomRuleHelper.createCustomRule(areaExitRule);
        assertNotNull(createdAreaExitRule);

        LatLong aisPositionDnk = new LatLong(56d, 10.5, new Date());
        SetReportRequest fluxReport = sendAisPositionAndGetFluxReport(asset, fluxEndpoint, aisPositionDnk);
        MovementType position = fluxReport.getReport().getMovement();
        assertThat(position.getAssetName(), is(asset.getName()));
        assertThat(position.getPosition().getLatitude(), is(aisPositionDnk.latitude));
        assertThat(position.getPosition().getLongitude(), is(aisPositionDnk.longitude));

        CustomRuleHelper.assertRuleTriggered(createdAreaExitRule, timestamp);

        CustomRuleType areaVMSExitRule = CustomRuleBuilder.getBuilder()
                .setName("Area exit SWE")
                .rule(CriteriaType.AREA, SubCriteriaType.AREA_CODE_VMS_EXT,
                        ConditionType.EQ, "SWE")
                .action(ActionType.SEND_REPORT, VMSSystemHelper.FLUX_NAME, fluxEndpoint)
                .action(ActionType.CREATE_TICKET, "Dummy ticket")
                .build();

        CustomRuleType createdAreaVMSExitRule = CustomRuleHelper.createCustomRule(areaVMSExitRule);
        assertNotNull(createdAreaVMSExitRule);

        LatLong vmsPositionDnk = new LatLong(56d, 10.5, new Date());
        sendPositionToFluxAndVerifyAssetInfo(asset, fluxEndpoint, vmsPositionDnk);
        CustomRuleHelper.assertRuleTriggered(createdAreaVMSExitRule, timestamp);
    }

    @Test
    public void createPollIfReportedSpeedIsGreaterThan10knotsTest() throws Exception {
        Instant timestamp = Instant.now();
        AssetDTO asset = AssetTestHelper.createTestAsset();
        MobileTerminalDto mobileTerminal = MobileTerminalTestHelper.createMobileTerminal();
        MobileTerminalTestHelper.assignMobileTerminal(asset, mobileTerminal);

        CustomRuleType speedRule = CustomRuleBuilder.getBuilder()
                .setName("Send poll")
                .rule(CriteriaType.POSITION, SubCriteriaType.REPORTED_SPEED,
                        ConditionType.GT, "10")
                .action(ActionType.MANUAL_POLL, "Not needed")
                .action(ActionType.CREATE_TICKET, "Dummy ticket")
                .build();

        CustomRuleType createdSpeedRule = CustomRuleHelper.createCustomRule(speedRule);
        assertNotNull(createdSpeedRule);

        TextMessage message;
        try (TopicListener topicListener = new TopicListener(VMSSystemHelper.INMARSAT_SELECTOR)) {

            LatLong position = new LatLong(11d, 56d, new Date());
            position.speed = 10.5;

            InmarsatPluginMock.sendInmarsatPosition(mobileTerminal, position);
            message = (TextMessage) topicListener.listenOnEventBus();
        }

        assertThat(message, is(notNullValue()));
        SetCommandRequest response = JAXBMarshaller.unmarshallString(message.getText(), SetCommandRequest.class);

        assertThat(response.getCommand().getCommand(), is(CommandTypeType.POLL));

        List<KeyValueType> pollReceiverValues = response.getCommand().getPoll().getPollReceiver();
        Map<String, String> receiverValuesMap = new HashMap<>();
        for (KeyValueType keyValueType : pollReceiverValues) {
            receiverValuesMap.put(keyValueType.getKey(), keyValueType.getValue());
        }
        assertThat(receiverValuesMap.get("SATELLITE_NUMBER"), is(mobileTerminal.getSatelliteNumber()));

        ChannelDto channel = mobileTerminal.getChannels().iterator().next();
        assertThat(receiverValuesMap.get("DNID"), is(channel.getDnid()));
        assertThat(receiverValuesMap.get("MEMBER_NUMBER"), is(channel.getMemberNumber()));

        TimeUnit.SECONDS.sleep(3);
        CustomRuleHelper.assertRuleTriggered(createdSpeedRule, timestamp);
    }

    @Test
    public void sendEmailIfVesselTypeIsTestTypeTest() throws Exception {
        Instant timestamp = Instant.now();
        String type = "test type";
        AssetDTO asset = AssetTestHelper.createBasicAsset();
        asset.setVesselType(type);
        asset = AssetTestHelper.createAsset(asset);
        String email = System.currentTimeMillis() + "@mail.com";

        CustomRuleType speedRule = CustomRuleBuilder.getBuilder()
                .setName("Vessel type == 'test type' => email")
                .rule(CriteriaType.ASSET, SubCriteriaType.ASSET_TYPE,
                        ConditionType.EQ, type)
                .action(ActionType.EMAIL, email)
                .action(ActionType.CREATE_TICKET, "Dummy ticket")
                .build();

        CustomRuleType createdSpeedRule = CustomRuleHelper.createCustomRule(speedRule);
        assertNotNull(createdSpeedRule);

        LatLong position = new LatLong(11d, 56d, new Date());
        position.speed = 10.5;
        sendPositionToFluxAndVerifyEmailAndRuleName(asset, email, createdSpeedRule, position);

        CustomRuleHelper.assertRuleTriggered(createdSpeedRule, timestamp);
    }

    private void sendPositionToFluxAndVerifyEmailAndRuleName(AssetDTO asset, String email,
                                                             CustomRuleType createdSpeedRule, LatLong position) throws Exception {

        try (TopicListener topicListener = new TopicListener(VMSSystemHelper.emailSelector)) {
            FLUXHelper.sendPositionToFluxPlugin(asset, position);
            CustomRuleHelper.pollTicketCreated();

            SetCommandRequest setCommandRequest = topicListener.listenOnEventBusForSpecificMessage(SetCommandRequest.class);
            assertThat(setCommandRequest.getCommand().getEmail().getTo(), is(email));
            assertThat(setCommandRequest.getCommand().getFwdRule(), is(createdSpeedRule.getName()));
        }
    }

    private void sendPositionToFluxAndVerifyAssetInfo(AssetDTO asset, String fluxEndpoint, LatLong positionDnk) throws Exception {
        try (TopicListener topicListener = new TopicListener(VMSSystemHelper.FLUX_SELECTOR)) {
            FLUXHelper.sendPositionToFluxPlugin(asset, positionDnk);
            CustomRuleHelper.pollTicketCreated();

            SetReportRequest setReportRequest = topicListener.listenOnEventBusForSpecificMessage(SetReportRequest.class);
            assertThat(setReportRequest.getReport().getRecipient(), is(fluxEndpoint));

            MovementType movement = setReportRequest.getReport().getMovement();
            assertThat(movement.getAssetName(), is(asset.getName()));
            assertThat(movement.getIrcs(), is(asset.getIrcs()));
        }
    }

    private SetReportRequest sendAisPositionAndGetFluxReport(AssetDTO asset, String fluxEndpoint, LatLong position) throws Exception {
        try (TopicListener topicListener = new TopicListener(VMSSystemHelper.FLUX_SELECTOR)) {
            AisPluginMock.sendAisPosition(asset, position);
            CustomRuleHelper.pollTicketCreated();

            SetReportRequest setReportRequest = topicListener.listenOnEventBusForSpecificMessage(SetReportRequest.class);
            assertThat(setReportRequest.getReport().getRecipient(), is(fluxEndpoint));
            return setReportRequest;
        }
    }
}
