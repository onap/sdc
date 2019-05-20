/*
 * Copyright © 2016-2018 European Support Limited
 *
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
 */

package org.openecomp.sdc.be.components.impl;

import fj.data.Either;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.be.auditing.impl.AuditingManager;
import org.openecomp.sdc.be.components.validation.CapabilitiesValidation;
import org.openecomp.sdc.be.components.validation.UserValidations;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.dao.jsongraph.JanusGraphDao;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.CapabilityDefinition;
import org.openecomp.sdc.be.model.CapabilityRequirementRelationship;
import org.openecomp.sdc.be.model.ComponentParametersView;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.RelationshipInfo;
import org.openecomp.sdc.be.model.RequirementCapabilityRelDef;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.CapabilitiesOperation;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.GraphLockOperation;
import org.openecomp.sdc.be.user.Role;
import org.openecomp.sdc.be.user.UserBusinessLogic;
import org.openecomp.sdc.common.api.ConfigurationSource;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.openecomp.sdc.common.impl.FSConfigurationSource;
import org.openecomp.sdc.exception.ResponseFormat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyObject;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class CapabilitiesBusinessLogicTest {
    private final String componentId = "resourceId1";
    private final String capabilityId = "uniqueId1";

    private final JanusGraphDao mockJanusGraphDao = Mockito.mock(JanusGraphDao.class);
    private final UserBusinessLogic mockUserAdmin = Mockito.mock(UserBusinessLogic.class);
    private final ToscaOperationFacade toscaOperationFacade = Mockito.mock(ToscaOperationFacade.class);
    private final UserValidations userValidations = Mockito.mock(UserValidations.class);
    private final CapabilitiesOperation capabilitiesOperation = Mockito.mock(CapabilitiesOperation.class);
    private final CapabilitiesValidation capabilitiesValidation = Mockito.mock(CapabilitiesValidation.class);

    private final GraphLockOperation graphLockOperation = Mockito.mock(GraphLockOperation.class);
    private User user = null;

    @InjectMocks
    private CapabilitiesBusinessLogic capabilitiesBusinessLogicMock = new CapabilitiesBusinessLogic();

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        ExternalConfiguration.setAppName("catalog-be");

        // init Configuration
        String appConfigDir = "src/test/resources/config/catalog-be";
        ConfigurationSource configurationSource = new FSConfigurationSource(ExternalConfiguration.getChangeListener(), appConfigDir);
        ConfigurationManager configurationManager = new ConfigurationManager(configurationSource);

        ComponentsUtils componentsUtils = new ComponentsUtils(Mockito.mock(AuditingManager.class));

        // User data and management
        user = new User();
        user.setUserId("jh0003");
        user.setFirstName("Jimmi");
        user.setLastName("Hendrix");
        user.setRole(Role.ADMIN.name());

        Either<User, ActionStatus> eitherGetUser = Either.left(user);
        when(mockUserAdmin.getUser("jh0003", false)).thenReturn(eitherGetUser);
        when(graphLockOperation.lockComponent(Mockito.anyString(), eq(NodeTypeEnum.Resource)))
                .thenReturn(StorageOperationStatus.OK);

        //CapabilityOperation
        when(capabilitiesValidation.validateCapabilities(anyCollection(), anyObject(), anyBoolean())
        ).thenReturn(Either.left(true));
        when(capabilitiesOperation.addCapabilities(anyString(), anyObject()))
                .thenReturn(Either.left(createMockCapabilityListToReturn(
                        createCapability("capName", "capDesc", "capType", "source1",
                "0", "10"))));

        when(capabilitiesOperation.updateCapabilities(anyString(), anyObject()))
                .thenReturn(Either.left(createMockCapabilityListToReturn(
                        createCapability("capName", "capDesc", "capType", "source1",
                "0", "10"))));
        when(capabilitiesOperation.deleteCapabilities( anyObject(), anyString()))
                .thenReturn(StorageOperationStatus.OK);
        when(mockJanusGraphDao.commit()).thenReturn(JanusGraphOperationStatus.OK);

        capabilitiesBusinessLogicMock = new CapabilitiesBusinessLogic();
        capabilitiesBusinessLogicMock.setComponentsUtils(componentsUtils);
        capabilitiesBusinessLogicMock.setUserAdmin(mockUserAdmin);
        capabilitiesBusinessLogicMock.setGraphLockOperation(graphLockOperation);
        capabilitiesBusinessLogicMock.setJanusGraphGenericDao(mockJanusGraphDao);
        capabilitiesBusinessLogicMock.setToscaOperationFacade(toscaOperationFacade);
        capabilitiesBusinessLogicMock.setUserValidations(userValidations);
        capabilitiesBusinessLogicMock.setCapabilitiesOperation(capabilitiesOperation);
        capabilitiesBusinessLogicMock.setCapabilitiesValidation(capabilitiesValidation);
    }

    @Test
    public void shouldPassCreateCapabilitiesFirstTimeInComponentForHappyScenario(){
        List<CapabilityDefinition> capabilityDefinitions = createMockCapabilityListToReturn(
                createCapability("capName", "capDesc", "capType", "source1",
                "0", "10"));
        Resource resource = createComponent(false);
        resource.setComponentType(ComponentTypeEnum.RESOURCE);
        validateUserRoles(Role.ADMIN, Role.DESIGNER);
        when(toscaOperationFacade.getToscaElement(anyString(), any(ComponentParametersView.class)))
                .thenReturn(Either.left(resource));
        Either<List<CapabilityDefinition>, ResponseFormat> capabilities = capabilitiesBusinessLogicMock
                .createCapabilities(componentId, capabilityDefinitions, user,
                         "createCapabilities", true);
        Assert.assertTrue(capabilities.isLeft());
        Assert.assertTrue(capabilities.left().value().stream().anyMatch(capabilityDefinition ->
                capabilityDefinition.getName().equals("capName")));
    }

    @Test
    public void shouldPassCreateCapabilitiesForHappyScenario(){
        List<CapabilityDefinition> capabilityDefinitions = createMockCapabilityListToReturn(
                createCapability("capName2", "capDesc", "capType", "source1",
                "0", "10"));
        Resource resource = createComponent(true);
        resource.setComponentType(ComponentTypeEnum.RESOURCE);
        validateUserRoles(Role.ADMIN, Role.DESIGNER);
        when(toscaOperationFacade.getToscaElement(anyString(), any(ComponentParametersView.class)))
                .thenReturn(Either.left(resource));
        Either<List<CapabilityDefinition>, ResponseFormat> capabilities = capabilitiesBusinessLogicMock
                .createCapabilities(componentId, capabilityDefinitions, user,
                        "createCapabilities", false);

        Assert.assertTrue(capabilities.isLeft());
        Assert.assertTrue(capabilities.left().value().stream().anyMatch(capabilityDefinition ->
                capabilityDefinition.getName().equals("capName2")));
    }

    @Test
    public void shouldFailCreateCapabilitiesWhenOperationFailedInJanusGraph(){
        List<CapabilityDefinition> capabilityDefinitions = createMockCapabilityListToReturn(
                createCapability("capName2", "capDesc", "capType", "source1",
                "0", "10"));
        Resource resource = createComponent(true);
        resource.setComponentType(ComponentTypeEnum.RESOURCE);
        validateUserRoles(Role.ADMIN, Role.DESIGNER);
        when(capabilitiesOperation.addCapabilities(anyString(), anyObject()))
                .thenReturn(Either.right(StorageOperationStatus.GENERAL_ERROR));
        when(capabilitiesOperation.updateCapabilities(anyString(), anyObject()))
                .thenReturn(Either.right(StorageOperationStatus.GENERAL_ERROR));
        when(toscaOperationFacade.getToscaElement(anyString(), any(ComponentParametersView.class)))
                .thenReturn(Either.left(resource));
        Either<List<CapabilityDefinition>, ResponseFormat> capabilities = capabilitiesBusinessLogicMock
                .createCapabilities(componentId, capabilityDefinitions, user,
                         "createCapabilities", true);

        Assert.assertTrue(capabilities.isRight());
    }
    @Test
    public void shouldPassUpdateCapabilitiesForHappyScenario(){

        List<CapabilityDefinition> capabilityDefinitions = createMockCapabilityListToReturn(
                createCapability("capName", "capDesc updated", "capType", "source1",
                "6", "11"));
        Resource resource = createComponent(true);
        resource.setComponentType(ComponentTypeEnum.RESOURCE);
        validateUserRoles(Role.ADMIN, Role.DESIGNER);
        when(toscaOperationFacade.getToscaElement(anyString(), any(ComponentParametersView.class)))
                .thenReturn(Either.left(resource));
        when(toscaOperationFacade.getParentComponents(anyString()))
                .thenReturn(Either.right(StorageOperationStatus.NOT_FOUND));
        Either<List<CapabilityDefinition>, ResponseFormat> capabilities = capabilitiesBusinessLogicMock
                .updateCapabilities(componentId, capabilityDefinitions, user
                        , "updateCapabilities", true);
        Assert.assertTrue(capabilities.isLeft());
        Assert.assertTrue(capabilities.left().value().stream().anyMatch(capabilityDefinition ->
                capabilityDefinition.getMaxOccurrences().equals("11")));
    }

    @Test
    public void shouldPassUpdateCapabilityTypeUpdateWhenTypeIsNotAvailable(){

        List<CapabilityDefinition> capabilityDefinitions = createMockCapabilityListToReturn(
                createCapability("capName", "capDesc updated", "capTypeUpdate", "source1",
                "6", "11"));
        Resource resource = createComponent(true);
        resource.setComponentType(ComponentTypeEnum.RESOURCE);
        validateUserRoles(Role.ADMIN, Role.DESIGNER);
        when(toscaOperationFacade.getToscaElement(anyString(), any(ComponentParametersView.class)))
                .thenReturn(Either.left(resource));
        when(toscaOperationFacade.getParentComponents(anyString()))
                .thenReturn(Either.right(StorageOperationStatus.NOT_FOUND));
        Either<List<CapabilityDefinition>, ResponseFormat> capabilities = capabilitiesBusinessLogicMock
                .updateCapabilities(componentId, capabilityDefinitions, user,
                        "updateCapabilities",true);
        Assert.assertTrue(capabilities.isLeft());
        Assert.assertTrue(capabilities.left().value().stream().anyMatch(capabilityDefinition ->
                capabilityDefinition.getMaxOccurrences().equals("11")));
    }

    @Test
    public void shouldPassUpdateCapabilityTypeUpdateWhenTypeIsAvailable(){

        List<CapabilityDefinition> capabilityDefinitions = createMockCapabilityListToReturn(
                createCapability("capName", "capDesc updated", "capTypeUpdate1", "source1",
                "6", "11"));
        Resource resource = createComponent(true);
        resource.setComponentType(ComponentTypeEnum.RESOURCE);

        CapabilityDefinition capability = createCapability("capName", "capDesc", "capTypeUpdate1",
                "source1", "0", "10");
        capability.setUniqueId("unique2");
        List<CapabilityDefinition> capabilityDefinitions1 = createMockCapabilityListToReturn(capability);
        Map<String, List<CapabilityDefinition>> capabilityMap = new HashMap<>();
        capabilityMap.put("capTypeUpdate1", capabilityDefinitions1);
        resource.getCapabilities().putAll(capabilityMap);

        validateUserRoles(Role.ADMIN, Role.DESIGNER);
        when(toscaOperationFacade.getToscaElement(anyString(), any(ComponentParametersView.class)))
                .thenReturn(Either.left(resource));
        when(toscaOperationFacade.getParentComponents(anyString()))
                .thenReturn(Either.right(StorageOperationStatus.NOT_FOUND));
        Either<List<CapabilityDefinition>, ResponseFormat> capabilities = capabilitiesBusinessLogicMock
                .updateCapabilities(componentId, capabilityDefinitions, user,
                        "updateCapabilities",true);
        Assert.assertTrue(capabilities.isLeft());
        Assert.assertTrue(capabilities.left().value().stream().anyMatch(capabilityDefinition ->
                capabilityDefinition.getMaxOccurrences().equals("11")));
    }

    @Test
    public void shouldFailUpdateCapabilitiesWhenOperaitonFailedInJanusGraph(){
        List<CapabilityDefinition> capabilityDefinitions = createMockCapabilityListToReturn(
                createCapability("capName2", "capDesc", "capType", "source1",
                "0", "10"));
        Resource resource = createComponent(true);
        resource.setComponentType(ComponentTypeEnum.RESOURCE);
        validateUserRoles(Role.ADMIN, Role.DESIGNER);
        when(capabilitiesOperation.addCapabilities(anyString(), anyObject()))
                .thenReturn(Either.right(StorageOperationStatus.GENERAL_ERROR));
        when(capabilitiesOperation.updateCapabilities(anyString(), anyObject()))
                .thenReturn(Either.right(StorageOperationStatus.GENERAL_ERROR));
        when(toscaOperationFacade.getToscaElement(anyString(), any(ComponentParametersView.class)))
                .thenReturn(Either.left(resource));
        Either<List<CapabilityDefinition>, ResponseFormat> capabilities = capabilitiesBusinessLogicMock
                .updateCapabilities(componentId, capabilityDefinitions, user,
                        "updateCapabilities", true);

        Assert.assertTrue(capabilities.isRight());
    }

    @Test
    public void shouldPassDeleteCapabilitiesForHappyScenario(){
        Resource resource = createComponent(true);
        resource.setComponentType(ComponentTypeEnum.RESOURCE);
        validateUserRoles(Role.ADMIN, Role.DESIGNER);
        when(toscaOperationFacade.getToscaElement(anyString(), any(ComponentParametersView.class)))
                .thenReturn(Either.left(resource));
        when(toscaOperationFacade.getParentComponents(anyString()))
                .thenReturn(Either.right(StorageOperationStatus.NOT_FOUND));
        Either<CapabilityDefinition, ResponseFormat> deleteCapabilityEither =
                capabilitiesBusinessLogicMock.deleteCapability(componentId, capabilityId, user, true);
        Assert.assertTrue(deleteCapabilityEither.isLeft());

    }

    @Test
    public void shouldFailDeleteCapabilitiesWhenOperationFailedInJanusGraph(){
        Resource resource = createComponent(true);
        resource.setComponentType(ComponentTypeEnum.RESOURCE);
        validateUserRoles(Role.ADMIN, Role.DESIGNER);
        when(toscaOperationFacade.getToscaElement(anyString(), any(ComponentParametersView.class)))
                .thenReturn(Either.left(resource));
        when(toscaOperationFacade.getParentComponents(anyString()))
                .thenReturn(Either.right(StorageOperationStatus.NOT_FOUND));
        when(capabilitiesOperation.deleteCapabilities(anyObject(), anyString()))
                .thenReturn(StorageOperationStatus.GENERAL_ERROR);
        Either<CapabilityDefinition, ResponseFormat> deleteCapabilityEither
                = capabilitiesBusinessLogicMock.deleteCapability(componentId, capabilityId, user, true);
        Assert.assertTrue(deleteCapabilityEither.isRight());
    }

    @Test
    public void shouldFailDeleteCapabilitiesWhenCapabilityUsedInServiceComposition(){
        Resource resource = createComponent(true);
        resource.setComponentType(ComponentTypeEnum.RESOURCE);
        validateUserRoles(Role.ADMIN, Role.DESIGNER);
        when(toscaOperationFacade.getToscaElement(anyString(), any(ComponentParametersView.class)))
                .thenReturn(Either.left(resource));
        when(toscaOperationFacade.getParentComponents(anyString()))
                .thenReturn(Either.left(Collections.singletonList(createParentService())));
        Either<CapabilityDefinition, ResponseFormat> deleteCapabilityEither
                = capabilitiesBusinessLogicMock.deleteCapability(componentId, capabilityId, user, true);
        Assert.assertTrue(deleteCapabilityEither.isRight());
    }

    @Test
    public void shouldPassGetCapabilitiesForHappyScenario(){
        Resource resource = createComponent(true);
        resource.setComponentType(ComponentTypeEnum.RESOURCE);
        validateUserRoles(Role.ADMIN, Role.DESIGNER);
        when(toscaOperationFacade.getToscaElement(anyString(), any(ComponentParametersView.class)))
                .thenReturn(Either.left(resource));
        when(toscaOperationFacade.getParentComponents(anyString()))
                .thenReturn(Either.right(StorageOperationStatus.NOT_FOUND));
        Either<CapabilityDefinition, ResponseFormat> getCapabilityEither
                = capabilitiesBusinessLogicMock.getCapability(componentId, capabilityId, user, true);
        Assert.assertTrue(getCapabilityEither.isLeft());

    }

    @Test
    public void shouldFailGetCapabilitiesWhenCapabilityNotExist(){
        Resource resource = createComponent(true);
        resource.setComponentType(ComponentTypeEnum.RESOURCE);
        validateUserRoles(Role.ADMIN, Role.DESIGNER);
        when(toscaOperationFacade.getToscaElement(anyString(), any(ComponentParametersView.class)))
                .thenReturn(Either.left(resource));
        when(toscaOperationFacade.getParentComponents(anyString()))
                .thenReturn(Either.right(StorageOperationStatus.NOT_FOUND));
        Either<CapabilityDefinition, ResponseFormat> getCapabilityEither
                = capabilitiesBusinessLogicMock.getCapability(componentId, "capId1", user, true);
        Assert.assertTrue(getCapabilityEither.isRight());

    }

    private Resource createComponent(boolean needCapability) {
        Resource resource = new Resource();
        resource.setName("Resource1");
        resource.addCategory("Network Layer 2-3", "Router");
        resource.setDescription("My short description");
        List<String> tgs = new ArrayList<>();
        tgs.add("test");
        tgs.add(resource.getName());
        resource.setTags(tgs);

        if(needCapability) {
            List<CapabilityDefinition> capabilityDefinitions = createMockCapabilityListToReturn(
                    createCapability("capName", "capDesc", "capType", "source1",
                    "0", "10"));
            Map<String, List<CapabilityDefinition>> capabilityMap = new HashMap<>();
            capabilityMap.put("capType", capabilityDefinitions);
            resource.setCapabilities(capabilityMap);
        }
            resource.setName(resource.getName());
            resource.setVersion("0.1");
            resource.setUniqueId(resource.getName().toLowerCase() + ":" + resource.getVersion());
            resource.setCreatorUserId(user.getUserId());
            resource.setCreatorFullName(user.getFirstName() + " " + user.getLastName());
            resource.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
        return resource;
    }

    private List<CapabilityDefinition> createMockCapabilityListToReturn(CapabilityDefinition capabilityDefinition) {
        List<CapabilityDefinition> capabilityDefinitions = new ArrayList<>();
        capabilityDefinitions.add(capabilityDefinition);
        return capabilityDefinitions;
    }

    private void validateUserRoles(Role... roles) {
        List<Role> listOfRoles = Stream.of(roles).collect(Collectors.toList());
    }

    private CapabilityDefinition createCapability(String name, String description, String type,
                                                  String validSourceTypes, String minOccurrences,
                                                  String maxOccurrences) {
        CapabilityDefinition capabilityDefinition = new CapabilityDefinition();
        capabilityDefinition.setName(name);
        capabilityDefinition.setDescription(description);
        capabilityDefinition.setType(type);
        capabilityDefinition.setValidSourceTypes(Collections.singletonList(validSourceTypes));
        capabilityDefinition.setMaxOccurrences(maxOccurrences);
        capabilityDefinition.setMinOccurrences(minOccurrences);
        capabilityDefinition.setUniqueId(capabilityId);

        return capabilityDefinition;
    }

    private Service createParentService() {
        Service service = new Service();
        service.setUniqueId("serviceUniqueId");

        List<RequirementCapabilityRelDef> resourceInstancesRelations = new ArrayList<>();
        RequirementCapabilityRelDef relationDef = new RequirementCapabilityRelDef();
        relationDef.setFromNode("fromNode");
        relationDef.setToNode("toNode");

        List<CapabilityRequirementRelationship> relationships = new ArrayList<>();
        CapabilityRequirementRelationship capabilityRequirementRelationship = new CapabilityRequirementRelationship();

        RelationshipInfo relation = new RelationshipInfo();
        relation.setCapabilityUid(capabilityId);
        relation.setRequirementUid("reqUniqueId1");
        capabilityRequirementRelationship.setRelation(relation);

        relationships.add(capabilityRequirementRelationship);
        relationDef.setRelationships(relationships);
        resourceInstancesRelations.add(relationDef);

        service.setComponentInstancesRelations(resourceInstancesRelations);

        return service;
    }

}