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
package fish.focus.uvms.docker.validation.frontend;

import java.util.UUID;

import org.junit.Test;
import fish.focus.uvms.asset.client.model.AssetDTO;
import fish.focus.uvms.docker.validation.asset.AssetTestHelper;
import fish.focus.uvms.docker.validation.frontend.pages.AssetMobileTerminalPage;
import fish.focus.uvms.docker.validation.frontend.pages.AssetNotesPage;
import fish.focus.uvms.docker.validation.frontend.pages.AssetPage;
import fish.focus.uvms.docker.validation.frontend.pages.AssetSearchPage;
import fish.focus.uvms.docker.validation.frontend.pages.UnionVMS;
import fish.focus.uvms.docker.validation.mobileterminal.MobileTerminalTestHelper;
import fish.focus.uvms.docker.validation.mobileterminal.dto.MobileTerminalDto;

public class AssetIT {

    @Test
    public void searchAssetTest() {
        UnionVMS uvms = UnionVMS.login();
        AssetDTO asset = AssetTestHelper.createTestAsset();

        AssetSearchPage assetSearchPage = uvms.assetSearchPage();
        assetSearchPage.assertSearchResultSize(0);

        assetSearchPage.searchAsset(asset.getCfr());
        assetSearchPage.assertSearchResultSize(1);
        assetSearchPage.assertSearchResultAtPosition(0, asset);
    }

    @Test
    public void searchAssetAndClickOnResultTest() {
        UnionVMS uvms = UnionVMS.login();
        AssetDTO asset = AssetTestHelper.createTestAsset();

        AssetSearchPage assetSearchPage = uvms.assetSearchPage();
        assetSearchPage.assertSearchResultSize(0);

        assetSearchPage.searchAsset(asset.getCfr());
        assetSearchPage.assertSearchResultSize(1);
        assetSearchPage.assertSearchResultAtPosition(0, asset);

        AssetPage assetPage = assetSearchPage.clickOnResultRow(0);
        assetPage.assertFlagstate(asset.getFlagStateCode());
        assetPage.assertExternalMarking(asset.getExternalMarking());
        assetPage.assertCfr(asset.getCfr());
        assetPage.assertIrcs(asset.getIrcs());
        assetPage.assertImo(asset.getImo());
        assetPage.assertMmsi(asset.getMmsi());
    }

    @Test
    public void createNoteTest() {
        UnionVMS uvms = UnionVMS.login();

        AssetDTO asset = AssetTestHelper.createTestAsset();

        AssetPage assetPage = uvms.assetPage(asset.getId());
        AssetNotesPage assetNotesPage = assetPage.assetNotesPage();

        String note = "Test note: " + UUID.randomUUID().toString();
        assetNotesPage.assertNumberOfNotes(0);
        assetNotesPage.createNote(note);

        assetNotesPage.assertNumberOfNotes(1);
        assetNotesPage.assertNoteAtPosition(0, note);
    }

    @Test
    public void createMobileTerminalTest() {
        AssetDTO asset = AssetTestHelper.createTestAsset();
        MobileTerminalDto mobileTerminal = MobileTerminalTestHelper.createBasicMobileTerminal();

        UnionVMS uvms = UnionVMS.login();
        AssetPage assetPage = uvms.assetPage(asset.getId());
        AssetMobileTerminalPage assetMobileTerminalPage = assetPage.assetMobileTerminalPage();
        assetMobileTerminalPage.createMobileTerminal(mobileTerminal, "Comment " + UUID.randomUUID());
        assetMobileTerminalPage.assertSerialNumber(mobileTerminal.getSerialNo());
    }
}
