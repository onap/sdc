package org.openecomp.sdc.be.servlets;

import com.fasterxml.jackson.databind.DeserializationFeature;
import fj.data.Either;
import org.apache.http.HttpStatus;
import org.glassfish.jersey.jackson.internal.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import org.glassfish.jersey.jackson.internal.jackson.jaxrs.json.JacksonJsonProvider;
import org.glassfish.jersey.server.ResourceConfig;
import org.junit.Test;
import org.openecomp.sdc.be.components.impl.PolicyTypeBusinessLogic;
import org.openecomp.sdc.be.components.utils.PolicyTypeBuilder;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.PolicyTypeDefinition;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.exception.ResponseFormat;

import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PolicyTypesEndpointTest extends JerseySpringBaseTest {

    public static final String USER_ID = "userId";
    public static final String COMPONENT_TYPE = "VF";
    private PolicyTypeBusinessLogic policyTypeBusinessLogic;
    private ComponentsUtils componentUtils;

    @Override
    protected ResourceConfig configure() {
        policyTypeBusinessLogic = mock(PolicyTypeBusinessLogic.class);
        componentUtils = mock(ComponentsUtils.class);
        final JacksonJsonProvider jacksonJsonProvider = new JacksonJaxbJsonProvider().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return super.configure()
                .register(new PolicyTypesEndpoint(policyTypeBusinessLogic))
                .register(jacksonJsonProvider);
    }

    @Test
    public void getPolicyTypes() {
        List<PolicyTypeDefinition> policyTypes = buildPolicyTypesList();
        when(policyTypeBusinessLogic.getAllPolicyTypes(USER_ID, COMPONENT_TYPE)).thenReturn(Either.left(policyTypes));
        when(componentUtils.getResponseFormat(ActionStatus.OK)).thenReturn(new ResponseFormat(HttpStatus.SC_OK));
        List<PolicyTypeDefinition> fetchedPolicyTypes = buildGetPolicyTypesCall().get(new GenericType<List<PolicyTypeDefinition>>(){});
        verifyPolicyTypesList(policyTypes, fetchedPolicyTypes);
    }

    @Test
    public void getPolicyTypes_whenNoInternalComponent_passNullAsComponentType() {
        List<PolicyTypeDefinition> policyTypes = buildPolicyTypesList();
        when(policyTypeBusinessLogic.getAllPolicyTypes(USER_ID, null)).thenReturn(Either.left(policyTypes));
        when(componentUtils.getResponseFormat(ActionStatus.OK)).thenReturn(new ResponseFormat(HttpStatus.SC_OK));
        List<PolicyTypeDefinition> fetchedPolicyTypes = buildGetPolicyTypesCallNoInternalComponent().get(new GenericType<List<PolicyTypeDefinition>>(){});
        verifyPolicyTypesList(policyTypes, fetchedPolicyTypes);
    }

    @Test
    public void getPolicyTypes_error() {
        when(policyTypeBusinessLogic.getAllPolicyTypes(USER_ID, COMPONENT_TYPE)).thenReturn(Either.right(new ResponseFormat(HttpStatus.SC_INTERNAL_SERVER_ERROR)));
        Response response = buildGetPolicyTypesCall().get();
        assertThat(response.getStatus()).isEqualTo(500);
    }

    private void verifyPolicyTypesList(List<PolicyTypeDefinition> policyTypes, List<PolicyTypeDefinition> fetchedPolicyTypes) {
        assertThat(fetchedPolicyTypes)
                .usingElementComparatorOnFields("version", "type", "uniqueId")
                .isEqualTo(policyTypes);
        assertThat(fetchedPolicyTypes).extracting("derivedFrom")//derivedFrom is not on the PolicyTypeMixin and should not return in response
                .containsOnly((String)null);
    }

    private Invocation.Builder buildGetPolicyTypesCall() {
        return target("/v1/catalog/policyTypes")
                .queryParam("internalComponentType", COMPONENT_TYPE)
                .request(MediaType.APPLICATION_JSON)
                .header(Constants.USER_ID_HEADER, USER_ID);
    }

    private Invocation.Builder buildGetPolicyTypesCallNoInternalComponent() {
        return target("/v1/catalog/policyTypes")
                .request(MediaType.APPLICATION_JSON)
                .header(Constants.USER_ID_HEADER, USER_ID);
    }

    private List<PolicyTypeDefinition> buildPolicyTypesList() {
        PolicyTypeDefinition type1 = new PolicyTypeBuilder().setDerivedFrom("root").setType("type1").setUniqueId("id1").setVersion("1.0").build();
        PolicyTypeDefinition type2 = new PolicyTypeBuilder().setDerivedFrom("type1").setType("type2").setUniqueId("id2").setVersion("1.0").build();
        PolicyTypeDefinition type3 = new PolicyTypeBuilder().setDerivedFrom("root").setType("type3").setUniqueId("id3").setVersion("1.0").build();
        return asList(type1, type2, type3);
    }



}
