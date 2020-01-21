/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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
 * Modifications copyright (c) 2019 Nokia
 * ================================================================================
 */
package org.openecomp.sdc.be.components.impl;

import fj.data.Either;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.sdc.be.components.impl.exceptions.ByActionStatusComponentException;
import org.openecomp.sdc.be.components.impl.exceptions.ComponentException;
import org.openecomp.sdc.be.components.utils.PolicyDefinitionBuilder;
import org.openecomp.sdc.be.components.utils.PropertyDataDefinitionBuilder;
import org.openecomp.sdc.be.components.utils.ResourceBuilder;
import org.openecomp.sdc.be.components.validation.UserValidations;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.jsongraph.JanusGraphDao;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.ComponentParametersView;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.PolicyDefinition;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.exception.ResponseFormat;

import javax.ws.rs.core.Response;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PolicyPropertiesBusinessLogicTest {

    private static final String POLICY_ID = "policy1";
    private static final String RESOURCE_ID = "resourceId";
    private static final String USER_ID = "userId";
    public static final String NO_PROPS_POLICY = "policy2";
    @InjectMocks
    private PolicyBusinessLogic testInstance;

    @Mock
    private UserValidations userValidations;

    @Mock
    private JanusGraphDao janusGraphDao;

    @Mock
    private ToscaOperationFacade toscaOperationFacade;

    @Mock
    private ComponentsUtils componentsUtils;

    private final ComponentTypeEnum COMPONENT_TYPE = ComponentTypeEnum.RESOURCE;

    private ComponentParametersView componentFilter;
    private Resource resource;
    private PropertyDefinition prop1, prop2;

    @Before
    public void setUp() throws Exception {
        testInstance.setUserValidations(userValidations);
        testInstance.setJanusGraphDao(janusGraphDao);
        testInstance.setToscaOperationFacade(toscaOperationFacade);
        testInstance.setComponentsUtils(componentsUtils);

        componentFilter = new ComponentParametersView(true);
        componentFilter.setIgnorePolicies(false);
        componentFilter.setIgnoreUsers(false);

        prop1 = new PropertyDataDefinitionBuilder().setUniqueId("prop1").build();
        prop2 = new PropertyDataDefinitionBuilder().setUniqueId("prop1").build();

        PolicyDefinition policy1 = PolicyDefinitionBuilder.create()
                .setUniqueId(POLICY_ID)
                .setProperties(prop1, prop2)
                .build();

        PolicyDefinition policy2 = PolicyDefinitionBuilder.create()
                .setUniqueId(NO_PROPS_POLICY)
                .build();
        resource = new ResourceBuilder()
                .setUniqueId(RESOURCE_ID)
                .setComponentType(COMPONENT_TYPE)
                .setLifeCycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT)
                .setLastUpdaterUserId(USER_ID)
                .addPolicy(policy1)
                .addPolicy(policy2)
                .build();
    }

    @After
    public void tearDown() {
        verify(janusGraphDao).commit();
    }

    @Test
    public void getPolicyProperties_userIdIsNull() {
        String userId = null;
        ComponentException forbiddenException = new ByActionStatusComponentException(ActionStatus.AUTH_FAILED);
        when(userValidations.validateUserExists(eq(userId))).thenThrow(forbiddenException);
        try{
            testInstance.getPolicyProperties(ComponentTypeEnum.RESOURCE, RESOURCE_ID, POLICY_ID, null);
        } catch(ByActionStatusComponentException e){
            assertThat(e.getActionStatus()).isEqualTo(ActionStatus.AUTH_FAILED);
        }
    }

    @Test(expected = ComponentException.class)
    public void getPolicyProperties_componentNotFound() {
        when(userValidations.validateUserExists(eq(USER_ID))).thenReturn(new User());
        ArgumentCaptor<ComponentParametersView> filterCaptor = ArgumentCaptor.forClass(ComponentParametersView.class);
        when(toscaOperationFacade.getToscaElement(eq(RESOURCE_ID), filterCaptor.capture())).thenReturn(Either.right(StorageOperationStatus.NOT_FOUND));
        when(componentsUtils.convertFromStorageResponse(StorageOperationStatus.NOT_FOUND, ComponentTypeEnum.RESOURCE)).thenCallRealMethod();
        ResponseFormat notFoundResponse = new ResponseFormat(Response.Status.NOT_FOUND.getStatusCode());
        testInstance.getPolicyProperties(ComponentTypeEnum.RESOURCE, RESOURCE_ID, POLICY_ID, USER_ID);
    }

    @Test(expected = ComponentException.class)
    public void getPolicyProperties_policyNotExist() {
        doPolicyValidations();
        ResponseFormat notFoundResponse = new ResponseFormat(Response.Status.NOT_FOUND.getStatusCode());
        testInstance.getPolicyProperties(ComponentTypeEnum.RESOURCE, RESOURCE_ID, "nonExistingPolicy", USER_ID);
    }

    @Test
    public void getPolicyProperties_noPropertiesOnPolicy() {
        doPolicyValidations();
        List<PropertyDataDefinition> policyProperties = testInstance.getPolicyProperties(ComponentTypeEnum.RESOURCE, RESOURCE_ID, NO_PROPS_POLICY, USER_ID);
        assertThat(policyProperties).isNull();
    }

    @Test
    public void getPolicyProperties() {
        doPolicyValidations();
        List<PropertyDataDefinition> policyProperties = testInstance.getPolicyProperties(ComponentTypeEnum.RESOURCE, RESOURCE_ID, POLICY_ID, USER_ID);
        assertThat(policyProperties)
                .usingElementComparatorOnFields("uniqueId")
                .containsExactly(prop1, prop2);
    }

    private void doPolicyValidations() {
        when(userValidations.validateUserExists(eq(USER_ID))).thenReturn(new User());
        ArgumentCaptor<ComponentParametersView> filterCaptor = ArgumentCaptor.forClass(ComponentParametersView.class);
        when(toscaOperationFacade.getToscaElement(eq(RESOURCE_ID), filterCaptor.capture())).thenReturn(Either.left(resource));
    }
}
