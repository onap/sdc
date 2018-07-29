package org.openecomp.sdc.be.servlets;

import fj.data.Either;
import org.glassfish.jersey.server.ResourceConfig;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.openecomp.sdc.be.components.impl.ComponentBusinessLogicProvider;
import org.openecomp.sdc.be.components.impl.ResourceBusinessLogic;
import org.openecomp.sdc.be.components.utils.PolicyDefinitionBuilder;
import org.openecomp.sdc.be.model.PolicyDefinition;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.ui.model.UiComponentDataTransfer;
import org.openecomp.sdc.common.api.Constants;

import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collections;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ComponentServletTest extends JerseySpringBaseTest{

    private static final String USER_ID = "userId";
    private static final String RESOURCE_ID = "resourceId";
    private ResourceBusinessLogic resourceBusinessLogic;
    private PolicyDefinition policy1, policy2;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        policy1 = buildPolicy("p1");
        policy2 = buildPolicy("p2");
    }

    @Override
    protected ResourceConfig configure() {
        resourceBusinessLogic = mock(ResourceBusinessLogic.class);
        ComponentServlet componentServlet = new ComponentServlet(new ComponentBusinessLogicProvider(resourceBusinessLogic, null, null));
        return super.configure().register(componentServlet);
    }

    @Test
    public void filterDataByParam_getPolicies_returnOnlyNameTargetsAndIdFields() {
        UiComponentDataTransfer dataTransfer = buildDataTransferWithPolicies();
        when(resourceBusinessLogic.getComponentDataFilteredByParams(eq(RESOURCE_ID.toLowerCase()), any(User.class), eq(Collections.singletonList("policies")))).thenReturn(Either.left(dataTransfer));
        UiComponentDataTransfer uiComponentDataTransfer = buildGetPolicyTypesCall().get(UiComponentDataTransfer.class);
        assertThat(uiComponentDataTransfer.getPolicies())
                .usingElementComparatorOnFields("name", "uniqueId", "targets")
                .containsExactlyInAnyOrder(policy1, policy2)
                .extracting("properties")//properties is not returned in the response
                .containsExactly(null, null);
    }

    @Test
    public void filterDataByParam_getPolicies_policyTypeNameFieldShouldReturnAsType() {
        UiComponentDataTransfer dataTransfer = buildDataTransferWithPolicies();
        when(resourceBusinessLogic.getComponentDataFilteredByParams(eq(RESOURCE_ID.toLowerCase()), any(User.class), eq(Collections.singletonList("policies")))).thenReturn(Either.left(dataTransfer));
        Response uiComponentDataTransfer = buildGetPolicyTypesCall().get();
        verifyPolicyTypeFieldUsingJsonResponse(uiComponentDataTransfer);
    }

    private void verifyPolicyTypeFieldUsingJsonResponse(Response uiComponentDataTransfer) {
        JSONObject json = new JSONObject(uiComponentDataTransfer.readEntity(String.class));
        JSONArray policies = json.getJSONArray("policies");
        for (int i = 0; i < policies.length(); i++) {
            JSONObject policy = policies.getJSONObject(i);
            String policyId = policy.get("uniqueId").toString();
            assertThat(policy.get("type")).isEqualTo("type" + policyId);
        }
    }

    private UiComponentDataTransfer buildDataTransferWithPolicies() {
        UiComponentDataTransfer res = new UiComponentDataTransfer();
        res.setPolicies(asList(policy1, policy2));
        return res;
    }

    private PolicyDefinition buildPolicy(String id) {
        return PolicyDefinitionBuilder.create()
                .setUniqueId(id)
                .setName("name" + id)
                .setType("type" + id)
                .addGroupTarget("group1")
                .addGroupTarget("group2")
                .addComponentInstanceTarget("inst1")
                .addComponentInstanceTarget("inst2")
                .addProperty("prop1")
                .build();
    }

    private Invocation.Builder buildGetPolicyTypesCall() {
        return target("/v1/catalog/resources/{id}/filteredDataByParams")
                .queryParam("include", "policies")
                .resolveTemplate("id", RESOURCE_ID)
                .request(MediaType.APPLICATION_JSON)
                .header(Constants.USER_ID_HEADER, USER_ID);
    }
}