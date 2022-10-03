/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.openecomp.sdc.be.servlets;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import fj.data.Either;
import java.util.Collections;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.TestProperties;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openecomp.sdc.be.components.impl.ComponentBusinessLogicProvider;
import org.openecomp.sdc.be.components.impl.ResourceBusinessLogic;
import org.openecomp.sdc.be.components.utils.PolicyDefinitionBuilder;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.PolicyDefinition;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.ui.model.UiComponentDataTransfer;
import org.openecomp.sdc.be.user.UserBusinessLogic;
import org.openecomp.sdc.common.api.Constants;

class ComponentServletTest extends JerseySpringBaseTest {

    private static final String USER_ID = "userId";
    private static final String RESOURCE_ID = "resourceId";
    private ResourceBusinessLogic resourceBusinessLogic;
    private PolicyDefinition policy1, policy2;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        policy1 = buildPolicy("p1");
        policy2 = buildPolicy("p2");
    }

    @AfterEach
    void after() throws Exception {
        super.tearDown();
    }

    @Override
    protected ResourceConfig configure() {
        forceSet(TestProperties.CONTAINER_PORT, "0");
        resourceBusinessLogic = mock(ResourceBusinessLogic.class);
        UserBusinessLogic userBusinessLogic = mock(UserBusinessLogic.class);
        ComponentsUtils componentsUtils = mock(ComponentsUtils.class);
        ComponentServlet componentServlet = new ComponentServlet(componentsUtils,
            new ComponentBusinessLogicProvider(resourceBusinessLogic, null, null));
        return super.configure().register(componentServlet);
    }

    @Test
    void filterDataByParam_getPolicies_returnOnlyNameTargetsAndIdFields() {
        UiComponentDataTransfer dataTransfer = buildDataTransferWithPolicies();
        when(resourceBusinessLogic.getComponentDataFilteredByParams(eq(RESOURCE_ID.toLowerCase()), any(User.class),
            eq(Collections.singletonList("policies")))).thenReturn(Either.left(dataTransfer));
        UiComponentDataTransfer uiComponentDataTransfer = buildGetPolicyTypesCall().get(UiComponentDataTransfer.class);
        assertThat(uiComponentDataTransfer.getPolicies())
            .usingElementComparatorOnFields("name", "uniqueId", "targets")
            .containsExactlyInAnyOrder(policy1, policy2)
            .extracting("properties")//properties is not returned in the response
            .containsExactly(null, null);
    }

    @Test
    void filterDataByParam_getPolicies_policyTypeNameFieldShouldReturnAsType() {
        UiComponentDataTransfer dataTransfer = buildDataTransferWithPolicies();
        when(resourceBusinessLogic.getComponentDataFilteredByParams(eq(RESOURCE_ID.toLowerCase()), any(User.class),
            eq(Collections.singletonList("policies")))).thenReturn(Either.left(dataTransfer));
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
