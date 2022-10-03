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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import org.apache.http.HttpStatus;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.TestProperties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openecomp.sdc.be.components.impl.PolicyTypeBusinessLogic;
import org.openecomp.sdc.be.components.utils.PolicyTypeBuilder;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.PolicyTypeDefinition;
import org.openecomp.sdc.be.user.UserBusinessLogic;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.exception.ResponseFormat;

class PolicyTypesEndpointTest extends JerseySpringBaseTest {

    public static final String USER_ID = "userId";
    public static final String COMPONENT_TYPE = "VF";
    private PolicyTypeBusinessLogic policyTypeBusinessLogic;
    private ComponentsUtils componentUtils;

    @BeforeEach
    public void before() throws Exception {
        super.setUp();
    }

    @AfterEach
    void after() throws Exception {
        super.tearDown();
    }

    @Override
    protected ResourceConfig configure() {
        forceSet(TestProperties.CONTAINER_PORT, "0");
        policyTypeBusinessLogic = mock(PolicyTypeBusinessLogic.class);
        componentUtils = mock(ComponentsUtils.class);
        UserBusinessLogic userBusinessLogic = mock(UserBusinessLogic.class);
        ComponentsUtils componentsUtils = mock(ComponentsUtils.class);
        return super.configure()
            .register(new PolicyTypesEndpoint(componentsUtils, policyTypeBusinessLogic));
    }

    @Test
    void getPolicyTypes() {
        List<PolicyTypeDefinition> policyTypes = buildPolicyTypesList();
        when(policyTypeBusinessLogic.getAllPolicyTypes(USER_ID, COMPONENT_TYPE, null)).thenReturn(policyTypes);
        when(componentUtils.getResponseFormat(ActionStatus.OK)).thenReturn(new ResponseFormat(HttpStatus.SC_OK));
        List<PolicyTypeDefinition> fetchedPolicyTypes = buildGetPolicyTypesCall()
            .get(new GenericType<List<PolicyTypeDefinition>>() {
            });
        verifyPolicyTypesList(policyTypes, fetchedPolicyTypes);
    }

    @Test
    void getPolicyTypes_whenNoInternalComponent_passNullAsComponentType() {
        List<PolicyTypeDefinition> policyTypes = buildPolicyTypesList();
        when(policyTypeBusinessLogic.getAllPolicyTypes(USER_ID, null, null)).thenReturn(policyTypes);
        when(componentUtils.getResponseFormat(ActionStatus.OK)).thenReturn(new ResponseFormat(HttpStatus.SC_OK));
        List<PolicyTypeDefinition> fetchedPolicyTypes = buildGetPolicyTypesCallNoInternalComponent()
            .get(new GenericType<List<PolicyTypeDefinition>>() {
            });
        verifyPolicyTypesList(policyTypes, fetchedPolicyTypes);
    }


    private void verifyPolicyTypesList(List<PolicyTypeDefinition> policyTypes,
                                       List<PolicyTypeDefinition> fetchedPolicyTypes) {
        assertThat(fetchedPolicyTypes)
            .usingElementComparatorOnFields("version", "type", "uniqueId", "name", "icon")
            .isEqualTo(policyTypes);
        assertThat(fetchedPolicyTypes)
            .extracting("derivedFrom")//derivedFrom is not on the PolicyTypeMixin and should not return in response
            .containsOnly((String) null);
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
        PolicyTypeDefinition type1 = new PolicyTypeBuilder()
            .setDerivedFrom("root")
            .setType("type1")
            .setUniqueId("id1")
            .setVersion("1.0")
            .setName("type1name")
            .setIcon("type1Icon")
            .build();
        PolicyTypeDefinition type2 = new PolicyTypeBuilder()
            .setDerivedFrom("type1")
            .setType("type2")
            .setUniqueId("id2")
            .setVersion("1.0")
            .setName("type2name")
            .setIcon("type2con")
            .build();
        PolicyTypeDefinition type3 = new PolicyTypeBuilder()
            .setDerivedFrom("root")
            .setType("type3")
            .setUniqueId("id3")
            .setVersion("1.0")
            .setName("type3name")
            .setIcon("type3con")
            .build();
        return asList(type1, type2, type3);
    }


}
