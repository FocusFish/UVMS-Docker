package fish.focus.uvms.docker.validation.config;

import fish.focus.schema.config.module.v1.PushModuleSettingMessage;
import fish.focus.schema.config.types.v1.SettingType;
import fish.focus.uvms.docker.validation.common.AbstractHelper;
import fish.focus.uvms.docker.validation.common.TopicListener;
import org.hamcrest.CoreMatchers;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

public class ConfigRestHelper extends AbstractHelper {

    public static void setLocalFlagStateToSwe() throws IOException, Exception {
        String settingKey = "flux_local_nation_code";
        String settingValue = "SWE";

        List<SettingType> dtoList = getWebTarget()
                .path("config/rest/globals")
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getValidJwtToken())
                .get(new GenericType<List<SettingType>>() {
                });

        SettingType flux_local_nation_code = dtoList.stream().filter(setting -> setting.getKey().equals(settingKey)).findAny().get();
        flux_local_nation_code.setValue(settingValue);

        try (TopicListener topicListener = new TopicListener(TopicListener.CONFIG_STATUS, null)) {
            Response response = getWebTarget()
                    .path("config/rest/settings")
                    .path(flux_local_nation_code.getId().toString())
                    .request(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, getValidJwtToken())
                    .put(Entity.json(flux_local_nation_code), Response.class);
            assertEquals(200, response.getStatus());

            PushModuleSettingMessage configMessage = topicListener.listenOnEventBusForSpecificMessage(PushModuleSettingMessage.class);
            SettingType setting = configMessage.getSetting();
            assertThat(setting.getKey(), CoreMatchers.is(settingKey));
            assertThat(setting.getValue(), CoreMatchers.is(settingValue));
        }
    }
}
