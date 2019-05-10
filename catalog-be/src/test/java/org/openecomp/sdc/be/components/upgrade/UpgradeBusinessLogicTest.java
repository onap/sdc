/*
 * -
 *  * ============LICENSE_START=======================================================
 *  *  Copyright (C) 2019  Nordix Foundation.
 *  * ================================================================================
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *  *
 *  * SPDX-License-Identifier: Apache-2.0
 *  * ============LICENSE_END=========================================================
 *
 */

package org.openecomp.sdc.be.components.upgrade;

import fj.data.Either;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.be.components.impl.ComponentInstanceBusinessLogic;
import org.openecomp.sdc.be.components.lifecycle.LifecycleBusinessLogic;
import org.openecomp.sdc.be.components.lifecycle.LifecycleChangeInfoWithAction;
import org.openecomp.sdc.be.components.validation.UserValidations;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.jsongraph.TitanDao;
import org.openecomp.sdc.be.datatypes.components.ResourceMetadataDataDefinition;
import org.openecomp.sdc.be.datatypes.components.ServiceMetadataDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentDependency;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.ComponentParametersView;
import org.openecomp.sdc.be.model.LifeCycleTransitionEnum;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.ResourceMetadataDefinition;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.ServiceMetadataDefinition;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.jsontitan.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.jsontitan.operations.UpgradeOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.user.Role;
import org.openecomp.sdc.exception.ResponseFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;


public class UpgradeBusinessLogicTest {

    private static final String COMPONENT_ID = "componentId";
    private static final String USER_ID = "admin123";
    private static final String SERVICE_ID = "service01";
    private static final String RESOURCE_ID = "resource01";

    private User user;
    private Resource resource;
    private Service service;
    private ResourceMetadataDefinition resourceMetadataDefinition;
    private ResourceMetadataDataDefinition resourceMetadataDataDefinition;
    private ServiceMetadataDefinition serviceMetadataDefinition;
    private ServiceMetadataDataDefinition serviceMetadataDataDefinition;

    @Mock
    private LifecycleBusinessLogic lifecycleBusinessLogic;

    @Mock
    private ComponentInstanceBusinessLogic componentInstanceBusinessLogic;

    @Mock
    private UserValidations userValidations;

    @Mock
    private ToscaOperationFacade toscaOperationFacade;

    @Mock
    private ComponentsUtils componentsUtils;

    @Mock
    private UpgradeOperation upgradeOperation;

    @Mock
    private TitanDao titanDao;

    @InjectMocks
    private UpgradeBusinessLogic upgradeBusinessLogic;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        upgradeBusinessLogic = new UpgradeBusinessLogic(lifecycleBusinessLogic, componentInstanceBusinessLogic,
                userValidations, toscaOperationFacade, componentsUtils, upgradeOperation, titanDao);

        user = new User();
        user.setRole(Role.ADMIN.name());
        user.setUserId(USER_ID);

        resourceMetadataDataDefinition = new ResourceMetadataDataDefinition();
        resourceMetadataDefinition = new ResourceMetadataDefinition(resourceMetadataDataDefinition);
        serviceMetadataDataDefinition = new ServiceMetadataDataDefinition();
        serviceMetadataDefinition = new ServiceMetadataDefinition(serviceMetadataDataDefinition);

        resource = new Resource(resourceMetadataDefinition);
        service = new Service(serviceMetadataDefinition);

        when(userValidations.validateUserExists(eq(user.getUserId()), anyString(), anyBoolean()))
                .thenReturn(user);
    }

    @Test
    public void testAutomatedUpgrade_givenValidResource_returnsSuccessful() {

        resourceMetadataDataDefinition.setHighestVersion(true);
        resourceMetadataDataDefinition.setLifecycleState(LifecycleStateEnum.CERTIFIED.name());
        resourceMetadataDataDefinition.setComponentType(ComponentTypeEnum.RESOURCE);
        resourceMetadataDataDefinition.setResourceType(ResourceTypeEnum.VF);

        when(toscaOperationFacade.getToscaFullElement(anyString()))
                .thenReturn(Either.left(resource));

        UpgradeStatus automatedUpgradeStatus = upgradeBusinessLogic.automatedUpgrade(COMPONENT_ID, getRequests(), user.getUserId());
        assertEquals(ActionStatus.OK, automatedUpgradeStatus.getStatus());
    }

    @Test
    public void testAutomatedUpgrade_givenResourceIsNotVFType_returnsUnsuccessful() {
        resourceMetadataDataDefinition.setResourceType(ResourceTypeEnum.PNF);
        resourceMetadataDataDefinition.setHighestVersion(true);
        resourceMetadataDataDefinition.setLifecycleState(LifecycleStateEnum.CERTIFIED.name());
        when(toscaOperationFacade.getToscaFullElement(COMPONENT_ID))
                .thenReturn(Either.left(resource));

        UpgradeStatus automatedUpgradeStatus = upgradeBusinessLogic.automatedUpgrade(
                COMPONENT_ID, getRequests(), user.getUserId());

        assertEquals("Status should be GENERAL Error ", ActionStatus.GENERAL_ERROR,
                automatedUpgradeStatus.getStatus());
    }

    @Test
    public void testAutomatedUpgrade_givenResourceVersionIsNotHighest_thenReturnsError() {
        ResponseFormat responseFormat = new ResponseFormat();
        responseFormat.setStatus(001);

        resourceMetadataDataDefinition.setHighestVersion(false);

        when(toscaOperationFacade.getToscaFullElement(COMPONENT_ID))
                .thenReturn(Either.left(resource));
        when(componentsUtils.getResponseFormat(eq(ActionStatus.COMPONENT_IS_NOT_HIHGEST_CERTIFIED), any()))
                .thenReturn(responseFormat);

        UpgradeStatus status = upgradeBusinessLogic.automatedUpgrade(COMPONENT_ID, null, user.getUserId());

        assertEquals(responseFormat.getStatus(), status.getError().getStatus());
    }

    @Test
    public void testAutomatedUpgrade_givenInvalidResourceId_thenReturnsError() {

        String invalidResourceId = "invalidResourceId";
        ResponseFormat stubResponseFormat = new ResponseFormat();
        stubResponseFormat.setStatus(001);

        when(toscaOperationFacade.getToscaFullElement(invalidResourceId))
                .thenReturn(Either.right(StorageOperationStatus.GENERAL_ERROR));
        when(componentsUtils.convertFromStorageResponse(StorageOperationStatus.GENERAL_ERROR))
                .thenReturn(ActionStatus.RESOURCE_NOT_FOUND);

        when(componentsUtils.getResponseFormatByResource(eq(ActionStatus.RESOURCE_NOT_FOUND), anyString()))
                .thenReturn(stubResponseFormat);


        UpgradeStatus status = upgradeBusinessLogic.automatedUpgrade(invalidResourceId,
                null, user.getUserId());
        assertEquals(stubResponseFormat.getStatus(), status.getError().getStatus());
    }

    @Test
    public void testAutomatedUpgrade_givenResourceIsArchived_thenReturnsError() {
        ResponseFormat responseFormat = new ResponseFormat();
        responseFormat.setStatus(001);

        resourceMetadataDataDefinition.setHighestVersion(true);
        resourceMetadataDataDefinition.setLifecycleState(LifecycleStateEnum.CERTIFIED.name());
        resourceMetadataDataDefinition.setArchived(true);

        when(toscaOperationFacade.getToscaFullElement(COMPONENT_ID))
                .thenReturn(Either.left(resource));
        when(componentsUtils.getResponseFormat(eq(ActionStatus.COMPONENT_IS_ARCHIVED), any()))
                .thenReturn(responseFormat);

        UpgradeStatus status = upgradeBusinessLogic.automatedUpgrade(COMPONENT_ID, null, user.getUserId());

        assertEquals(responseFormat.getStatus(), status.getError().getStatus());
    }

    @Test
    public void testAutomatedUpgrade_givenService_thenReturnsSuccessful() {
        user.setRole(Role.TESTER.name());

        List<String> stubInstanceIdsList = new ArrayList<>();

        List<ComponentInstance> stubComponentInstanceList = new ArrayList<>();
        ComponentInstance componentInstance = new ComponentInstance();
        componentInstance.setToscaPresentationValue(JsonPresentationFields.CI_IS_PROXY, Boolean.TRUE);
        componentInstance.setSourceModelUid("sm1");
        stubComponentInstanceList.add(componentInstance);

        List<ComponentInstanceProperty> stubComponentPropertyList = new ArrayList<>();
        ComponentInstanceProperty componentInstanceProperty = new ComponentInstanceProperty();
        stubComponentPropertyList.add(componentInstanceProperty);

        Map<String, List<ComponentInstanceProperty>> stubComponentInstanceProperties = new HashMap<>();
        stubComponentInstanceProperties.put("prop1", stubComponentPropertyList);
        service.setComponentInstancesProperties(stubComponentInstanceProperties);
        service.setComponentInstances(stubComponentInstanceList);

        serviceMetadataDataDefinition.setHighestVersion(true);
        serviceMetadataDataDefinition.setLifecycleState(LifecycleStateEnum.CERTIFIED.name());
        serviceMetadataDataDefinition.setComponentType(ComponentTypeEnum.SERVICE);
        serviceMetadataDataDefinition.setUniqueId("sUniqueId1");
        serviceMetadataDataDefinition.setInvariantUUID("iid");
        serviceMetadataDataDefinition.setVersion("1.0");


        ServiceMetadataDataDefinition serviceMetadataDataDefinition2 = new ServiceMetadataDataDefinition();
        serviceMetadataDataDefinition2.setLifecycleState(LifecycleStateEnum.CERTIFIED.name());
        serviceMetadataDataDefinition2.setComponentType(ComponentTypeEnum.SERVICE);
        serviceMetadataDataDefinition2.setUniqueId("sUniqueId2");
        serviceMetadataDataDefinition2.setInvariantUUID("iid");
        serviceMetadataDataDefinition2.setVersion("2.0");

        Service service2 = new Service(new ServiceMetadataDefinition(serviceMetadataDataDefinition2));

        when(toscaOperationFacade.getToscaFullElement(anyString()))
               .thenReturn(Either.left(service));

        when(toscaOperationFacade.getToscaElement(RESOURCE_ID))
                .thenReturn(Either.left(service));
        when(toscaOperationFacade.getToscaElement(eq(componentInstance.getSourceModelUid()), any(ComponentParametersView.class)))
                .thenReturn(Either.left(service2));

        doReturn(Either.left(service)).when(lifecycleBusinessLogic).changeComponentState(
                any(ComponentTypeEnum.class), any(), any(User.class),
                any(LifeCycleTransitionEnum.class), any(LifecycleChangeInfoWithAction.class), anyBoolean(), anyBoolean());

        when(upgradeOperation.getInstanceIdFromAllottedEdge(any(), any()))
                .thenReturn(stubInstanceIdsList);
        when(toscaOperationFacade.updateComponentInstancePropsToComponent(any(Map.class), any()))
                .thenReturn(Either.left(stubComponentInstanceProperties));
        when(componentInstanceBusinessLogic.changeInstanceVersion(any(Component.class), any(ComponentInstance.class), any(ComponentInstance.class), any(User.class), any(ComponentTypeEnum.class)))
                .thenReturn(Either.left(componentInstance));

        UpgradeStatus status = upgradeBusinessLogic.automatedUpgrade(COMPONENT_ID, getRequests(), user.getUserId());
        Assert.assertEquals(ActionStatus.OK, status.getStatus());
    }

    @Test
    public void testGetComponentDependencies_givenValidComponentId_thenReturnsSuccessful() {
        String componentId = "componentId";
        List<ComponentDependency> stubComponentDependencies
                = new ArrayList<>();
        stubComponentDependencies.add(new ComponentDependency());

        when(upgradeOperation.getComponentDependencies(componentId))
                .thenReturn(Either.left(stubComponentDependencies));

        assertEquals("should contain one component dependency", 1,
                upgradeBusinessLogic.getComponentDependencies(componentId, user.getUserId()).left().value().size());
    }

    private List<UpgradeRequest> getRequests() {
        List<UpgradeRequest> stubRequests = new ArrayList<>();
        UpgradeRequest request1 = new UpgradeRequest();
        request1.setServiceId(SERVICE_ID);
        request1.setResourceId(RESOURCE_ID);
        stubRequests.add(request1);
        return stubRequests;
    }
}