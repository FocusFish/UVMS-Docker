package fish.focus.uvms.docker.validation.user;

import fish.focus.uvms.docker.validation.common.AbstractRest;
import fish.focus.uvms.docker.validation.common.AuthenticationResponse;
import fish.focus.uvms.docker.validation.user.dto.Policy;
import org.junit.Test;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.junit.Assert.assertThrows;

public class UserPolicyIT extends AbstractRest {

    @Test
    public void givenLoggedInUserVmsAdminCom_whenAccessingPolicies_thenSuccess() {
        final AuthenticationResponse authData = UserHelper.authenticate("vms_admin_com", "password");
        String jwtToken = authData.getJwtoken();

        List<Policy> response = getWebTarget()
                .path("usm-administration/rest/policies")
                .queryParam("name", "ldap.enabled")
                .queryParam("subject", "Authentication")
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, jwtToken)
                .get(new GenericType<List<Policy>>() {});

        assertThat(response, is(not(empty())));
    }

    @Test
    public void givenLoggedInUserVmsUserCom_whenAccessingPolicies_thenUnauthorized() {
        final AuthenticationResponse authData = UserHelper.authenticate("vms_user_com", "password");
        String jwtToken = authData.getJwtoken();

        assertThrows(WebApplicationException.class, () -> getWebTarget()
                .path("usm-administration/rest/policies")
                .queryParam("name", "ldap.enabled")
                .queryParam("subject", "Authentication")
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, jwtToken)
                .get(new GenericType<List<Policy>>() {})
        );
    }
}
