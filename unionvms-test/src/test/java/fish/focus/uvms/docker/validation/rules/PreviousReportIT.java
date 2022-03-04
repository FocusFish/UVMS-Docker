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
package fish.focus.uvms.docker.validation.rules;

import static org.junit.Assert.assertTrue;
import java.io.IOException;
import java.util.List;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import org.junit.BeforeClass;
import org.junit.Test;
import fish.focus.uvms.asset.client.model.AssetDTO;
import fish.focus.uvms.docker.validation.asset.AssetTestHelper;
import fish.focus.uvms.docker.validation.common.AbstractRest;
import fish.focus.uvms.docker.validation.config.ConfigRestHelper;
import fish.focus.uvms.docker.validation.mobileterminal.MobileTerminalTestHelper;
import fish.focus.uvms.docker.validation.mobileterminal.dto.MobileTerminalDto;
import fish.focus.uvms.docker.validation.rules.dto.PreviousReportDto;
import fish.focus.uvms.docker.validation.system.helper.VMSSystemHelper;

public class PreviousReportIT extends AbstractRest {

    @BeforeClass
    public static void beforeClass() throws Exception {
        ConfigRestHelper.setLocalFlagStateToSwe();
    }

    @Test
    public void createPreviousReportWhenSatellitePosition() throws IOException, Exception {
        AssetDTO asset = AssetTestHelper.createAsset(AssetTestHelper.createBasicAsset());
        MobileTerminalDto mt = MobileTerminalTestHelper.createMobileTerminal();
        MobileTerminalTestHelper.assignMobileTerminal(asset, mt);

        VMSSystemHelper.triggerBasicRuleWithSatellitePosition(mt);

        List<PreviousReportDto> previousReports = MovementRulesTestHelper.getPreviousReports();

        assertTrue(previousReports.stream().anyMatch(r -> r.getAssetGuid().equals(asset.getId().toString())));
    }

    @Test
    public void dontCreatePreviousReportWhenFLUXPosition() throws IOException, Exception {
        AssetDTO asset = AssetTestHelper.createAsset(AssetTestHelper.createBasicAsset());
        MobileTerminalDto mt = MobileTerminalTestHelper.createMobileTerminal();
        MobileTerminalTestHelper.assignMobileTerminal(asset, mt);

        VMSSystemHelper.triggerBasicRuleAndSendToNAF(asset, generateARandomStringWithMaxLength(10));

        List<PreviousReportDto> previousReports = MovementRulesTestHelper.getPreviousReports();

        assertTrue(previousReports.stream().noneMatch(r -> r.getAssetGuid().equals(asset.getId().toString())));
    }
}