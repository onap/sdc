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
import org.openecomp.sdc.be.components.validation.RequirementValidation;
import org.openecomp.sdc.be.components.validation.UserValidations;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.jsongraph.JanusGraphDao;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.CapabilityRequirementRelationship;
import org.openecomp.sdc.be.model.ComponentParametersView;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.RelationshipInfo;
import org.openecomp.sdc.be.model.RequirementCapabilityRelDef;
import org.openecomp.sdc.be.model.RequirementDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.RequirementOperation;
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

public class RequirementBusinessLogicTest {
    private final String componentId = "resourceId1";
    private final String requirementId = "uniqueId1";

    private final JanusGraphDao mockJanusGraphDao = Mockito.mock(JanusGraphDao.class);
    private final UserBusinessLogic mockUserAdmin = Mockito.mock(UserBusinessLogic.class);
    private final ToscaOperationFacade toscaOperationFacade = Mockito.mock(ToscaOperationFacade.class);
    private final UserValidations userValidations = Mockito.mock(UserValidations.class);
    private final RequirementOperation requirementOperation = Mockito.mock(RequirementOperation.class);
    private final RequirementValidation requirementValidation = Mockito.mock(RequirementValidation.class);

    private final GraphLockOperation graphLockOperation = Mockito.mock(GraphLockOperation.class);
    private User user = null;

    @InjectMocks
    private RequirementBusinessLogic requirementsBusinessLogicMock = new RequirementBusinessLogic();

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        ExternalConfiguration.setAppName("catalog-be");

        // init Configuration
        String appConfigDir = "src/test/resources/config/catalog-be";
        ConfigurationSource configurationSource = new FSConfigurationSource(ExternalConfiguration
                .getChangeListener(), appConfigDir);
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
        when(requirementValidation.validateRequirements(anyCollection(), anyObject(), anyBoolean()))
                .thenReturn(Either.left(true));
        when(requirementOperation.addRequirement(anyString(), anyObject()))
                .thenReturn(Either.left(createMockRequirementListToReturn(createRequirement(
                        "reqName", "capType", "node", "source1",
                        "0", "10"))));

        when(requirementOperation.updateRequirement(anyString(), anyObject()))
                .thenReturn(Either.left(createMockRequirementListToReturn(createRequirement(
                        "reqName", "capType", "node", "source1",
                "0", "10"))));
        when(requirementOperation.deleteRequirements( anyObject(), anyString()))
                .thenReturn(StorageOperationStatus.OK);
        when(mockJanusGraphDao.commit()).thenReturn(JanusGraphOperationStatus.OK);

        requirementsBusinessLogicMock = new RequirementBusinessLogic();

        requirementsBusinessLogicMock.setComponentsUtils(componentsUtils);
        requirementsBusinessLogicMock.setUserAdmin(mockUserAdmin);
        requirementsBusinessLogicMock.setGraphLockOperation(graphLockOperation);
        requirementsBusinessLogicMock.setJanusGraphGenericDao(mockJanusGraphDao);
        requirementsBusinessLogicMock.setToscaOperationFacade(toscaOperationFacade);
        requirementsBusinessLogicMock.setUserValidations(userValidations);
        requirementsBusinessLogicMock.setRequirementOperation(requirementOperation);
        requirementsBusinessLogicMock.setRequirementValidation(requirementValidation);
    }

    @Test
    public void shouldPassCreateRequirementsFirstTimeInComponentForHappyScenario(){
        List<RequirementDefinition> requirementDefinitions = createMockRequirementListToReturn(
                createRequirement("reqName", "reqDesc", "capType", "source1",
                "0", "10"));
        Resource resource = createComponent(false);
        resource.setComponentType(ComponentTypeEnum.RESOURCE);
        validateUserRoles(Role.ADMIN, Role.DESIGNER);
        when(toscaOperationFacade.getToscaElement(anyString(), any(ComponentParametersView.class)))
                .thenReturn(Either.left(resource));
        Either<List<RequirementDefinition>, ResponseFormat> requirements = requirementsBusinessLogicMock
                .createRequirements(componentId, requirementDefinitions, user,
                         "createRequirements", true);
        Assert.assertTrue(requirements.isLeft());
        Assert.assertTrue(requirements.left().value().stream().anyMatch(requirementDefinition ->
                requirementDefinition.getName().equals("reqName")));
    }

    @Test
    public void shouldPassCreateRequirementsForHappyScenario(){
        List<RequirementDefinition> requirementDefinitions = createMockRequirementListToReturn(
                createRequirement("reqName2", "capType", "node", "source1",
                "0", "10"));
        Resource resource = createComponent(true);
        resource.setComponentType(ComponentTypeEnum.RESOURCE);
        validateUserRoles(Role.ADMIN, Role.DESIGNER);
        when(toscaOperationFacade.getToscaElement(anyString(), any(ComponentParametersView.class)))
                .thenReturn(Either.left(resource));
        Either<List<RequirementDefinition>, ResponseFormat> requirements = requirementsBusinessLogicMock
                .createRequirements(componentId, requirementDefinitions, user,
                         "createRequirements", true);

        Assert.assertTrue(requirements.isLeft());
        Assert.assertTrue(requirements.left().value().stream().anyMatch(requirementDefinition ->
                requirementDefinition.getName().equals("reqName2")));
    }

    @Test
    public void shouldPassUpdateRequirementsForHappyScenario(){

        List<RequirementDefinition> requirementDefinitions = createMockRequirementListToReturn(
                createRequirement("reqName", "capType", "node", "source1",
                "6", "11"));
        Resource resource = createComponent(true);
        resource.setComponentType(ComponentTypeEnum.RESOURCE);
        validateUserRoles(Role.ADMIN, Role.DESIGNER);
        when(toscaOperationFacade.getToscaElement(anyString(), any(ComponentParametersView.class)))
                .thenReturn(Either.left(resource));
        when(toscaOperationFacade.getParentComponents(anyString()))
                .thenReturn(Either.right(StorageOperationStatus.NOT_FOUND));
        Either<List<RequirementDefinition>, ResponseFormat> capabilities = requirementsBusinessLogicMock
                .updateRequirements(componentId, requirementDefinitions, user,
                         "updateRequirements", true);
        Assert.assertTrue(capabilities.isLeft());
        Assert.assertTrue(capabilities.left().value().stream().anyMatch(requirementDefinition ->
                requirementDefinition.getMaxOccurrences().equals("11")));
    }

    @Test
    public void shouldPassDeleteRequirementsForHappyScenario(){
        Resource resource = createComponent(true);
        resource.setComponentType(ComponentTypeEnum.RESOURCE);
        validateUserRoles(Role.ADMIN, Role.DESIGNER);
        when(toscaOperationFacade.getToscaElement(anyString(), any(ComponentParametersView.class)))
                .thenReturn(Either.left(resource));
        when(toscaOperationFacade.getParentComponents(anyString()))
                .thenReturn(Either.right(StorageOperationStatus.NOT_FOUND));
        Either<RequirementDefinition, ResponseFormat> deleteRequirementEither
                = requirementsBusinessLogicMock.deleteRequirement(componentId, requirementId, user, true);
        Assert.assertTrue(deleteRequirementEither.isLeft());

    }

    @Test
    public void shouldPassUpdateRequirementCapabilityUpdateWhenCapabilityNotExist(){

        List<RequirementDefinition> requirementDefinitions
                = createMockRequirementListToReturn(createRequirement(
                        "reqName", "capTypeUpdate", "node", "source1",
                "6", "11"));
        Resource resource = createComponent(true);
        resource.setComponentType(ComponentTypeEnum.RESOURCE);
        validateUserRoles(Role.ADMIN, Role.DESIGNER);
        when(toscaOperationFacade.getToscaElement(anyString(), any(ComponentParametersView.class)))
                .thenReturn(Either.left(resource));
        when(toscaOperationFacade.getParentComponents(anyString()))
                .thenReturn(Either.right(StorageOperationStatus.NOT_FOUND));
        Either<List<RequirementDefinition>, ResponseFormat> updateRequirements
                = requirementsBusinessLogicMock.updateRequirements(componentId, requirementDefinitions,
                user,  "updateRequirements", true);
        Assert.assertTrue(updateRequirements.isLeft());
        Assert.assertTrue(updateRequirements.left().value().stream().anyMatch(requirementDefinition ->
                requirementDefinition.getMaxOccurrences().equals("11")));
    }

    @Test
    public void shouldPassUpdateRequirementTypeWhenCapabilityExist(){

        List<RequirementDefinition> requirementDefinitions = createMockRequirementListToReturn(
                createRequirement("reqName", "capTypeUpdate1", "node",
                        "source1","6", "11"));
        Resource resource = createComponent(true);
        resource.setComponentType(ComponentTypeEnum.RESOURCE);

        RequirementDefinition requirement = createRequirement("reqName",
                "capTypeUpdate1", "node", "source1",
                "6", "11");
        requirement.setUniqueId("unique2");
        List<RequirementDefinition> requirementDefinitions1 = createMockRequirementListToReturn(requirement);
        Map<String, List<RequirementDefinition>> requirementMap = new HashMap<>();
        requirementMap.put("capTypeUpdate1", requirementDefinitions1);
        resource.getRequirements().putAll(requirementMap);

        validateUserRoles(Role.ADMIN, Role.DESIGNER);
        when(toscaOperationFacade.getToscaElement(anyString(), any(ComponentParametersView.class)))
                .thenReturn(Either.left(resource));
        when(toscaOperationFacade.getParentComponents(anyString()))
                .thenReturn(Either.right(StorageOperationStatus.NOT_FOUND));
        Either<List<RequirementDefinition>, ResponseFormat> capabilities
                = requirementsBusinessLogicMock.updateRequirements(componentId, requirementDefinitions,
                user,  "updateRequirements", true);
        Assert.assertTrue(capabilities.isLeft());
        Assert.assertTrue(capabilities.left().value().stream().anyMatch(capabilityDefinition ->
                capabilityDefinition.getMaxOccurrences().equals("11")));
    }

    @Test
    public void shouldFailUpdateRequirementWhenOperationFailedInJanusGraph(){
        List<RequirementDefinition> requirementDefinitions = createMockRequirementListToReturn(
                createRequirement("reqName", "capType", "node", "source1",
                "6", "11"));
        Resource resource = createComponent(true);
        resource.setComponentType(ComponentTypeEnum.RESOURCE);
        validateUserRoles(Role.ADMIN, Role.DESIGNER);
        when(requirementOperation.addRequirement(anyString(), anyObject()))
                .thenReturn(Either.right(StorageOperationStatus.GENERAL_ERROR));
        when(requirementOperation.updateRequirement(anyString(), anyObject()))
                .thenReturn(Either.right(StorageOperationStatus.GENERAL_ERROR));
        when(toscaOperationFacade.getToscaElement(anyString(), any(ComponentParametersView.class)))
                .thenReturn(Either.left(resource));
        Either<List<RequirementDefinition>, ResponseFormat> capabilities = requirementsBusinessLogicMock
                .updateRequirements(componentId, requirementDefinitions, user,
                        "updateRequirements", true);

        Assert.assertTrue(capabilities.isRight());
    }

    
    @Test
    public void shouldFailDeleteRequirementWhenOperationFailedInJanusGraph(){
        Resource resource = createComponent(true);
        resource.setComponentType(ComponentTypeEnum.RESOURCE);
        validateUserRoles(Role.ADMIN, Role.DESIGNER);
        when(toscaOperationFacade.getToscaElement(anyString(), any(ComponentParametersView.class)))
                .thenReturn(Either.left(resource));
        when(toscaOperationFacade.getParentComponents(anyString()))
                .thenReturn(Either.right(StorageOperationStatus.NOT_FOUND));
        when(requirementOperation.deleteRequirements(anyObject(), anyString()))
                .thenReturn(StorageOperationStatus.GENERAL_ERROR);
        Either<RequirementDefinition, ResponseFormat> deleteRequirementEither
                = requirementsBusinessLogicMock.deleteRequirement(componentId, requirementId, user, true);
        Assert.assertTrue(deleteRequirementEither.isRight());
    }

    @Test
    public void shouldFailDeleteRequirementWhenRequirementUsedInServiceComposition(){
        Resource resource = createComponent(true);
        resource.setComponentType(ComponentTypeEnum.RESOURCE);
        validateUserRoles(Role.ADMIN, Role.DESIGNER);
        when(toscaOperationFacade.getToscaElement(anyString(), any(ComponentParametersView.class)))
                .thenReturn(Either.left(resource));
        when(toscaOperationFacade.getParentComponents(anyString()))
                .thenReturn(Either.left(Collections.singletonList(createParentService())));
        Either<RequirementDefinition, ResponseFormat> deleteRequirementEither
                = requirementsBusinessLogicMock.deleteRequirement(componentId, requirementId, user, true);
        Assert.assertTrue(deleteRequirementEither.isRight());
    }

    @Test
    public void shouldPassGetRequirementsForHappyScenario(){
        Resource resource = createComponent(true);
        resource.setComponentType(ComponentTypeEnum.RESOURCE);
        validateUserRoles(Role.ADMIN, Role.DESIGNER);
        when(toscaOperationFacade.getToscaElement(anyString(), any(ComponentParametersView.class)))
                .thenReturn(Either.left(resource));
        when(toscaOperationFacade.getParentComponents(anyString()))
                .thenReturn(Either.right(StorageOperationStatus.NOT_FOUND));
        Either<RequirementDefinition, ResponseFormat> getRequirementEither
                = requirementsBusinessLogicMock.getRequirement(componentId, requirementId, user, true);
        Assert.assertTrue(getRequirementEither.isLeft());

    }

    private Resource createComponent(boolean needRequirements) {
        Resource resource = new Resource();
        resource.setName("Resource1");
        resource.addCategory("Network Layer 2-3", "Router");
        resource.setDescription("My short description");
        List<String> tgs = new ArrayList<>();
        tgs.add("test");
        tgs.add(resource.getName());
        resource.setTags(tgs);

        if(needRequirements) {
            List<RequirementDefinition> requirementDefinitions = createMockRequirementListToReturn(
                    createRequirement("reqName", "capType", "node", "source1",
                    "0", "10"));
            Map<String, List<RequirementDefinition>> requirementsMap = new HashMap<>();
            requirementsMap.put("capType", requirementDefinitions);
            resource.setRequirements(requirementsMap);
        }
        resource.setName(resource.getName());
        resource.setVersion("0.1");
        resource.setUniqueId(resource.getName().toLowerCase() + ":" + resource.getVersion());
        resource.setCreatorUserId(user.getUserId());
        resource.setCreatorFullName(user.getFirstName() + " " + user.getLastName());
        resource.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
        return resource;
    }

    private List<RequirementDefinition> createMockRequirementListToReturn(RequirementDefinition requirementDefinition) {
        List<RequirementDefinition> requirementDefinitions = new ArrayList<>();
        requirementDefinitions.add(requirementDefinition);
        return requirementDefinitions;
    }

    private void validateUserRoles(Role... roles) {
        List<Role> listOfRoles = Stream.of(roles).collect(Collectors.toList());
    }

    private RequirementDefinition createRequirement(String name, String capability, String node,
                                                    String relationship, String minOccurrences,
                                                    String maxOccurrences) {
        RequirementDefinition requirementDefinition = new RequirementDefinition();
        requirementDefinition.setName(name);
        requirementDefinition.setCapability(capability);
        requirementDefinition.setNode(node);
        requirementDefinition.setRelationship(relationship);
        requirementDefinition.setMaxOccurrences(maxOccurrences);
        requirementDefinition.setMinOccurrences(minOccurrences);
        requirementDefinition.setUniqueId(requirementId);

        return requirementDefinition;
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
        relation.setCapabilityUid("capabilityId");
        relation.setRequirementUid(requirementId);
        capabilityRequirementRelationship.setRelation(relation);

        relationships.add(capabilityRequirementRelationship);
        relationDef.setRelationships(relationships);
        resourceInstancesRelations.add(relationDef);

        service.setComponentInstancesRelations(resourceInstancesRelations);

        return service;
    }

}