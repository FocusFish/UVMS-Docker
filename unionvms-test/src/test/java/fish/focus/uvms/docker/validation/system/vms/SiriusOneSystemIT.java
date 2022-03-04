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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.junit.Test;
import fish.focus.schema.movement.v1.MovementSourceType;
import fish.focus.uvms.asset.client.model.AssetDTO;
import fish.focus.uvms.docker.validation.asset.AssetTestHelper;
import fish.focus.uvms.docker.validation.common.TopicListener;
import fish.focus.uvms.docker.validation.mobileterminal.MobileTerminalTestHelper;
import fish.focus.uvms.docker.validation.mobileterminal.dto.MobileTerminalDto;
import fish.focus.uvms.docker.validation.mobileterminal.dto.MobileTerminalPluginDto;
import fish.focus.uvms.docker.validation.movement.LatLong;
import fish.focus.uvms.docker.validation.movement.MovementHelper;
import fish.focus.uvms.docker.validation.system.helper.FLUXHelper;
import fish.focus.uvms.docker.validation.system.helper.SiriusOnePluginMock;
import fish.focus.uvms.mobileterminal.model.constants.MobileTerminalTypeEnum;
import fish.focus.uvms.movement.model.dto.MovementDto;

public class SiriusOneSystemIT {

    @Test
    public void siriousOnePositionTest() throws Exception {
        AssetDTO asset = AssetTestHelper.createTestAsset();
        
        MobileTerminalDto terminal = MobileTerminalTestHelper.createBasicMobileTerminal();
        terminal.setMobileTerminalType(MobileTerminalTypeEnum.IRIDIUM.toString());

        MobileTerminalPluginDto plugin = new MobileTerminalPluginDto();
        plugin.setPluginServiceName("fish.focus.uvms.plugins.iridium.siriusone");
        terminal.setPlugin(plugin);
        
        MobileTerminalDto mobileTerminal = MobileTerminalTestHelper.persistMobileTerminal(terminal);
        MobileTerminalTestHelper.assignMobileTerminal(asset, mobileTerminal);
        
        LatLong position = new LatLong(11d, 56d, new Date());
        try (TopicListener topicListener = new TopicListener(TopicListener.EVENT_STREAM, "event = 'Movement'")) {
            SiriusOnePluginMock.sendSiriusOnePosition(mobileTerminal, position);
            topicListener.listenOnEventBus();
        }
        
        List<MovementDto> latestMovements = MovementHelper.getLatestMovements(Arrays.asList(asset.getId().toString()));
        
        assertThat(latestMovements.size(), is(1));
        MovementDto movement = latestMovements.get(0);
        assertThat(movement.getSource(), is(MovementSourceType.IRIDIUM));
        assertThat(movement.getLocation().getLatitude(), is(position.latitude));
        assertThat(movement.getLocation().getLongitude(), is(position.longitude));
        assertThat(movement.getTimestamp(), is(position.positionTime.toInstant()));
    }
}
