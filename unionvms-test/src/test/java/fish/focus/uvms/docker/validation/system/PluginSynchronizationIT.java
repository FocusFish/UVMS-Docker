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
package fish.focus.uvms.docker.validation.system;

import fish.focus.schema.exchange.plugin.types.v1.PluginType;
import fish.focus.schema.exchange.registry.v1.ExchangeRegistryMethod;
import fish.focus.schema.exchange.registry.v1.RegisterServiceRequest;
import fish.focus.schema.exchange.registry.v1.UnregisterServiceRequest;
import fish.focus.schema.exchange.service.v1.CapabilityListType;
import fish.focus.schema.exchange.service.v1.ServiceType;
import fish.focus.schema.exchange.service.v1.SettingListType;
import fish.focus.uvms.docker.validation.common.AbstractRest;
import fish.focus.uvms.docker.validation.common.MessageHelper;
import fish.focus.uvms.docker.validation.common.TopicListener;
import fish.focus.uvms.docker.validation.mobileterminal.MobileTerminalTestHelper;
import fish.focus.uvms.docker.validation.mobileterminal.dto.MobileTerminalDto;
import fish.focus.uvms.exchange.model.constant.ExchangeModelConstants;
import fish.focus.uvms.exchange.model.mapper.JAXBMarshaller;
import fish.focus.uvms.mobileterminal.model.constants.MobileTerminalTypeEnum;
import org.junit.Test;

import javax.jms.Message;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

public class PluginSynchronizationIT extends AbstractRest {

    @Test
    public void registerServiceTest() throws Exception {
        RegisterServiceRequest registerRequest = new RegisterServiceRequest();
        ServiceType serviceType = createServiceType();
        registerRequest.setService(serviceType);
        registerRequest.setCapabilityList(new CapabilityListType());
        registerRequest.setSettingList(new SettingListType());

        try (MessageHelper messageHelper = new MessageHelper();
             TopicListener listener = new TopicListener(TopicListener.EVENT_STREAM, "event='Service Registered'")) {
            messageHelper.sendToEventBus(JAXBMarshaller.marshallJaxBObjectToString(registerRequest), ExchangeModelConstants.EXCHANGE_REGISTER_SERVICE, ExchangeRegistryMethod.REGISTER_SERVICE.toString());
            listener.listenOnEventBus();
            TimeUnit.SECONDS.sleep(1);
        }

        MobileTerminalDto mobileTerminal = MobileTerminalTestHelper.createBasicMobileTerminal();
        mobileTerminal.getPlugin().setPluginServiceName(serviceType.getServiceClassName());
        MobileTerminalDto createdMobileTerminal = MobileTerminalTestHelper.persistMobileTerminal(mobileTerminal);
        assertThat(createdMobileTerminal, is(notNullValue()));
        assertThat(createdMobileTerminal.getPlugin().getPluginServiceName(), is(serviceType.getServiceClassName()));
    }

    @Test
    public void unregisterServiceEventTest() throws Exception {
        RegisterServiceRequest registerRequest = new RegisterServiceRequest();
        ServiceType serviceType = createServiceType();
        registerRequest.setService(serviceType);
        registerRequest.setCapabilityList(new CapabilityListType());
        registerRequest.setSettingList(new SettingListType());

        UnregisterServiceRequest unregisterRequest = new UnregisterServiceRequest();
        unregisterRequest.setService(serviceType);

        try (MessageHelper messageHelper = new MessageHelper();
             TopicListener listener = new TopicListener(TopicListener.EVENT_STREAM, "event='Service Registered' OR event='Service Unregistered'")) {
            // register
            messageHelper.sendToEventBus(JAXBMarshaller.marshallJaxBObjectToString(registerRequest), ExchangeModelConstants.EXCHANGE_REGISTER_SERVICE, ExchangeRegistryMethod.REGISTER_SERVICE.toString());
            listener.listenOnEventBus();
            // unregister
            messageHelper.sendToEventBus(JAXBMarshaller.marshallJaxBObjectToString(unregisterRequest), ExchangeModelConstants.EXCHANGE_REGISTER_SERVICE, ExchangeRegistryMethod.UNREGISTER_SERVICE.toString());
            Message message = listener.listenOnEventBus();
            assertThat(message.getStringProperty("event"), is("Service Unregistered"));
        }
    }

    private ServiceType createServiceType() {
        String uuid = UUID.randomUUID().toString();
        String serviceName = "validation.system.PluginSynchronizationIT." + uuid;
        ServiceType serviceType = new ServiceType();
        serviceType.setDescription("Description");
        serviceType.setName(uuid);
        serviceType.setServiceClassName(serviceName);
        serviceType.setServiceResponseMessageName(serviceName + "RESPONSE");
        serviceType.setPluginType(PluginType.SATELLITE_RECEIVER);
        serviceType.setSatelliteType(MobileTerminalTypeEnum.INMARSAT_C.toString());
        return serviceType;
    }
}
