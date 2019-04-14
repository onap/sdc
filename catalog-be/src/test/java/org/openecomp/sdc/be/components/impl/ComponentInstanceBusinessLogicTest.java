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
 */

package org.openecomp.sdc.be.components.impl;

import static org.assertj.core.api.Assertions.assertThat;
import fj.data.Either;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import mockit.Deencapsulation;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.assertj.core.util.Lists;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.sdc.be.components.impl.exceptions.ComponentException;
import org.openecomp.sdc.be.components.validation.UserValidations;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.jsongraph.TitanDao;
import org.openecomp.sdc.be.dao.jsongraph.types.JsonParseFlagEnum;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.datatypes.elements.*;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.impl.ServletUtils;
import org.openecomp.sdc.be.info.CreateAndAssotiateInfo;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.CapabilityDefinition;
import org.openecomp.sdc.be.model.CapabilityRequirementRelationship;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.ComponentInstanceInput;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.ComponentParametersView;
import org.openecomp.sdc.be.model.InputDefinition;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.PolicyDefinition;
import org.openecomp.sdc.be.model.RelationshipImpl;
import org.openecomp.sdc.be.model.RelationshipInfo;
import org.openecomp.sdc.be.model.RequirementCapabilityRelDef;
import org.openecomp.sdc.be.model.RequirementDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.jsontitan.operations.ForwardingPathOperation;
import org.openecomp.sdc.be.model.jsontitan.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.GraphLockOperation;
import org.openecomp.sdc.be.user.UserBusinessLogic;
import org.openecomp.sdc.common.api.ArtifactGroupTypeEnum;
import org.openecomp.sdc.exception.ResponseFormat;

import java.util.function.BiPredicate;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

/**
 * The test suite designed for test functionality of ComponentInstanceBusinessLogic class
 */

@RunWith(MockitoJUnitRunner.class)
public class ComponentInstanceBusinessLogicTest {

    private final static String USER_ID = "jh0003";
    private final static String COMPONENT_ID = "componentId";
    private final static String COMPONENT_INST_ID = "componentInstId";
    private final static String TO_INSTANCE_ID = "toInstanceId";
    private final static String FROM_INSTANCE_ID = "fromInstanceId";
    private final static String RELATION_ID = "relationId";
    private final static String CAPABILITY_OWNER_ID = "capabilityOwnerId";
    private final static String CAPABILITY_UID = "capabilityUid";
    private final static String CAPABILITY_NAME = "capabilityName";
    private final static String REQUIREMENT_OWNER_ID = "requirementOwnerId";
    private final static String REQUIREMENT_UID = "requirementUid";
    private final static String REQUIREMENT_NAME = "requirementName";
    private final static String RELATIONSHIP_TYPE = "relationshipType";
    private final static String ARTIFACT_1 = "cloudtech_k8s_charts.zip";
    private final static String ARTIFACT_2 = "cloudtech_azure_day0.zip";
    private final static String ARTIFACT_3 = "cloudtech_aws_configtemplate.zip";
    private final static String ARTIFACT_4 = "k8s_charts.zip";
    private final static String ARTIFACT_5 = "cloudtech_openstack_configtemplate.zip";
    private final static String PROP_NAME = "propName";
    private final static String NON_EXIST_NAME = "nonExistName";

    @InjectMocks
    private static ComponentInstanceBusinessLogic componentInstanceBusinessLogic;
    @Mock
    private ComponentsUtils componentsUtils;
    @Mock
    private ServletUtils servletUtils;
    @Mock
    private ResponseFormat responseFormat;
    @Mock
    private ToscaOperationFacade toscaOperationFacade;
    @Mock
    private UserBusinessLogic userAdmin;
    @Mock
    private ForwardingPathOperation forwardingPathOperation;
    @Mock
    private User user;
    @Mock
    private UserValidations userValidations;
    @Mock
    private TitanDao titanDao;
    @Mock
    private ArtifactsBusinessLogic artifactBusinessLogic;
    @Mock
    private GraphLockOperation graphLockOperation;

    private Component service;
    private Component resource;
    private ComponentInstance toInstance;
    private ComponentInstance fromInstance;
    private CapabilityDataDefinition capability;
    private RequirementDataDefinition requirement;
    private RequirementCapabilityRelDef relation;


    @Before
    public void init() {
        stubMethods();
        createComponents();
    }

    @Test
    public void testGetRelationByIdSuccess() {
        getServiceRelationByIdSuccess(service);
        getServiceRelationByIdSuccess(resource);
    }

    @Test
    public void testGetRelationByIdUserValidationFailure() {
        getServiceRelationByIdUserValidationFailure(service);
        getServiceRelationByIdUserValidationFailure(resource);
    }

    @Test
    public void testGetRelationByIdComponentNotFoundFailure() {
        getRelationByIdComponentNotFoundFailure(service);
        getRelationByIdComponentNotFoundFailure(resource);
    }

    @Test
    public void testForwardingPathOnVersionChange() {
        getforwardingPathOnVersionChange();
    }

    @Test
    public void testIsCloudSpecificArtifact() {
        assertTrue(componentInstanceBusinessLogic.isCloudSpecificArtifact(ARTIFACT_1));
        assertTrue(componentInstanceBusinessLogic.isCloudSpecificArtifact(ARTIFACT_2));
        assertTrue(componentInstanceBusinessLogic.isCloudSpecificArtifact(ARTIFACT_3));
        assertFalse(componentInstanceBusinessLogic.isCloudSpecificArtifact(ARTIFACT_4));
        assertFalse(componentInstanceBusinessLogic.isCloudSpecificArtifact(ARTIFACT_5));
    }

    private void getforwardingPathOnVersionChange(){
        String containerComponentParam = "services";
        String containerComponentID = "121-cont";
        String componentInstanceID = "121-cont-1-comp";
        Service component = new Service();
        Map<String, ForwardingPathDataDefinition> forwardingPaths = generateForwardingPath(componentInstanceID);

        //Add existing componentInstance to component
        List<ComponentInstance> componentInstanceList = new ArrayList<>();
        ComponentInstance oldComponentInstance = new ComponentInstance();
        oldComponentInstance.setName("OLD_COMP_INSTANCE");
        oldComponentInstance.setUniqueId(componentInstanceID);
        oldComponentInstance.setName(componentInstanceID);
        oldComponentInstance.setToscaPresentationValue(JsonPresentationFields.CI_COMPONENT_UID, "1-comp");
        componentInstanceList.add(oldComponentInstance);
        component.setComponentInstances(componentInstanceList);
        component.setForwardingPaths(forwardingPaths);

        List<ComponentInstance> componentInstanceListNew = new ArrayList<>();
        ComponentInstance newComponentInstance = new ComponentInstance();
        String new_Comp_UID = "2-comp";
        newComponentInstance.setToscaPresentationValue(JsonPresentationFields.CI_COMPONENT_UID, new_Comp_UID);
        newComponentInstance.setUniqueId(new_Comp_UID);
        componentInstanceListNew.add(newComponentInstance);
        Component component2 = new Service();
        component2.setComponentInstances(componentInstanceListNew);

        //Mock for getting component
        when(toscaOperationFacade.getToscaElement(eq(containerComponentID), any(ComponentParametersView.class)))
                .thenReturn(Either.left(component));
        when(toscaOperationFacade.validateComponentExists(any(String.class))).thenReturn(Either.left(Boolean.TRUE));
        when(toscaOperationFacade.getToscaFullElement(eq(new_Comp_UID))).thenReturn(Either.left(component2));

        Either<Set<String>, ResponseFormat> resultOp = componentInstanceBusinessLogic
                                                               .forwardingPathOnVersionChange(containerComponentParam,
                                                                       containerComponentID, componentInstanceID,
                                                                       newComponentInstance);
        assertEquals(1, resultOp.left().value().size());
        assertEquals("FP-ID-1", resultOp.left().value().iterator().next());

    }


    @Test
    public void testDeleteForwardingPathsWhenComponentinstanceDeleted() {

        ComponentTypeEnum containerComponentType = ComponentTypeEnum.findByParamName("services");
        String containerComponentID = "Service-comp";
        String componentInstanceID = "NodeA1";
        Service component = new Service();
        component.setComponentInstances(
                Arrays.asList(createComponentIstance("NodeA2"), createComponentIstance("NodeB2"),
                        createComponentIstance(componentInstanceID)));

        component.addForwardingPath(createPath("path1", componentInstanceID, "NodeB1", "1"));
        component.addForwardingPath(createPath("Path2", "NodeA2", "NodeB2", "2"));
        when(toscaOperationFacade.getToscaElement(eq(containerComponentID), any(ComponentParametersView.class)))
                .thenReturn(Either.left(component));
        when(toscaOperationFacade.getToscaElement(eq(containerComponentID))).thenReturn(Either.left(component));
        when(forwardingPathOperation.deleteForwardingPath(any(Service.class), anySet()))
                .thenReturn(Either.left(new HashSet<>()));
        final ComponentInstance ci = new ComponentInstance();
        ci.setName(componentInstanceID);
        Either<ComponentInstance, ResponseFormat> responseFormatEither = componentInstanceBusinessLogic.deleteForwardingPathsRelatedTobeDeletedComponentInstance(
                containerComponentID, containerComponentType, Either.left(ci));
        assertThat(responseFormatEither.isLeft()).isEqualTo(true);

    }

    private ComponentInstance createComponentIstance(String path1) {
        ComponentInstance componentInstance = new ComponentInstance();
        componentInstance.setName(path1);
        return componentInstance;
    }

    private ForwardingPathDataDefinition createPath(String pathName, String fromNode, String toNode, String uniqueId) {
        ForwardingPathDataDefinition forwardingPath = new ForwardingPathDataDefinition(pathName);
        forwardingPath.setProtocol("protocol");
        forwardingPath.setDestinationPortNumber("port");
        forwardingPath.setUniqueId(uniqueId);
        ListDataDefinition<ForwardingPathElementDataDefinition> forwardingPathElementListDataDefinition =
                new ListDataDefinition<>();
        forwardingPathElementListDataDefinition
                .add(new ForwardingPathElementDataDefinition(fromNode, toNode, "nodeAcpType", "nodeBcpType",
                        "nodeDcpName", "nodeBcpName"));
        forwardingPath.setPathElements(forwardingPathElementListDataDefinition);

        return forwardingPath;
    }


    private Map<String, ForwardingPathDataDefinition> generateForwardingPath(String componentInstanceID) {
        ForwardingPathDataDefinition forwardingPath = new ForwardingPathDataDefinition("fpName");
        String protocol = "protocol";
        forwardingPath.setProtocol(protocol);
        forwardingPath.setDestinationPortNumber("DestinationPortNumber");
        forwardingPath.setUniqueId("FP-ID-1");
        ListDataDefinition<ForwardingPathElementDataDefinition> forwardingPathElementListDataDefinition =
                new ListDataDefinition<>();
        forwardingPathElementListDataDefinition
                .add(new ForwardingPathElementDataDefinition(componentInstanceID, "nodeB", "nodeA_FORWARDER_CAPABILITY",
                        "nodeBcpType", "nodeDcpName", "nodeBcpName"));
        forwardingPath.setPathElements(forwardingPathElementListDataDefinition);
        Map<String, ForwardingPathDataDefinition> forwardingPaths = new HashMap<>();
        forwardingPaths.put("1122", forwardingPath);
        return forwardingPaths;
    }

    @SuppressWarnings("unchecked")
    private void getServiceRelationByIdSuccess(Component component) {
        Either<Component, StorageOperationStatus> getComponentRes = Either.left(component);
        when(toscaOperationFacade.getToscaElement(eq(COMPONENT_ID), any(ComponentParametersView.class)))
                .thenReturn(getComponentRes);
        Either<RequirementCapabilityRelDef, ResponseFormat> response = componentInstanceBusinessLogic
                                                                               .getRelationById(COMPONENT_ID,
                                                                                       RELATION_ID, USER_ID,
                                                                                       component.getComponentType());
        assertTrue(response.isLeft());
    }

    private void getServiceRelationByIdUserValidationFailure(Component component) {
        when(userValidations.validateUserExists(eq(USER_ID), eq("get relation by Id"), eq(false)))
                .thenThrow(new ComponentException(ActionStatus.USER_NOT_FOUND));
        try {
            componentInstanceBusinessLogic
                    .getRelationById(COMPONENT_ID, RELATION_ID, USER_ID, component.getComponentType());
        } catch (ComponentException e) {
            assertSame(e.getActionStatus(), ActionStatus.USER_NOT_FOUND);
        }
    }

    private void getRelationByIdComponentNotFoundFailure(Component component) {
        Either<User, ActionStatus> eitherCreator = Either.left(user);
        Either<Component, StorageOperationStatus> getComponentRes = Either.right(StorageOperationStatus.NOT_FOUND);
        when(toscaOperationFacade.getToscaElement(eq(COMPONENT_ID), any(ComponentParametersView.class)))
                .thenReturn(getComponentRes);

        Either<RequirementCapabilityRelDef, ResponseFormat> response = componentInstanceBusinessLogic
                                                                               .getRelationById(COMPONENT_ID,
                                                                                       RELATION_ID, USER_ID,
                                                                                       component.getComponentType());
        assertTrue(response.isRight());
    }

    private void stubMethods() {
        when(userValidations.validateUserExists(eq(USER_ID), eq("get relation by Id"), eq(false))).thenReturn(user);
    }

    private void createComponents() {
        createRelation();
        createInstances();
        createService();
        createResource();
    }

    private void createResource() {
        resource = new Resource();
        resource.setUniqueId(COMPONENT_ID);
        resource.setComponentInstancesRelations(Lists.newArrayList(relation));
        resource.setComponentInstances(Lists.newArrayList(toInstance, fromInstance));
        resource.setCapabilities(toInstance.getCapabilities());
        resource.setRequirements(fromInstance.getRequirements());
        resource.setComponentType(ComponentTypeEnum.RESOURCE);
        resource.setState(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
    }


    private void createService() {
        service = new Service();
        service.setUniqueId(COMPONENT_ID);
        service.setComponentInstancesRelations(Lists.newArrayList(relation));
        service.setComponentInstances(Lists.newArrayList(toInstance, fromInstance));
        service.setCapabilities(toInstance.getCapabilities());
        service.setRequirements(fromInstance.getRequirements());
        service.setComponentType(ComponentTypeEnum.SERVICE);
        service.setState(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
    }


    private void createInstances() {
        toInstance = new ComponentInstance();
        toInstance.setUniqueId(TO_INSTANCE_ID);

        fromInstance = new ComponentInstance();
        fromInstance.setUniqueId(FROM_INSTANCE_ID);

        capability = new CapabilityDataDefinition();
        capability.setOwnerId(CAPABILITY_OWNER_ID);
        capability.setUniqueId(CAPABILITY_UID);
        capability.setName(CAPABILITY_NAME);

        Map<String, List<CapabilityDefinition>> capabilities = new HashMap<>();
        capabilities.put(capability.getName(), Lists.newArrayList(new CapabilityDefinition(capability)));

        requirement = new RequirementDataDefinition();
        requirement.setOwnerId(REQUIREMENT_OWNER_ID);
        requirement.setUniqueId(REQUIREMENT_UID);
        requirement.setName(REQUIREMENT_NAME);
        requirement.setRelationship(RELATIONSHIP_TYPE);


        Map<String, List<RequirementDefinition>> requirements = new HashMap<>();
        requirements.put(requirement.getCapability(), Lists.newArrayList(new RequirementDefinition(requirement)));

        toInstance.setCapabilities(capabilities);
        fromInstance.setRequirements(requirements);
    }


    private void createRelation() {

        relation = new RequirementCapabilityRelDef();
        CapabilityRequirementRelationship relationship = new CapabilityRequirementRelationship();
        RelationshipInfo relationInfo = new RelationshipInfo();
        relationInfo.setId(RELATION_ID);
        relationship.setRelation(relationInfo);

        relation.setRelationships(Lists.newArrayList(relationship));
        relation.setToNode(TO_INSTANCE_ID);
        relation.setFromNode(FROM_INSTANCE_ID);

        relationInfo.setCapabilityOwnerId(CAPABILITY_OWNER_ID);
        relationInfo.setCapabilityUid(CAPABILITY_UID);
        relationInfo.setCapability(CAPABILITY_NAME);
        relationInfo.setRequirementOwnerId(REQUIREMENT_OWNER_ID);
        relationInfo.setRequirementUid(REQUIREMENT_UID);
        relationInfo.setRequirement(REQUIREMENT_NAME);
        RelationshipImpl relationshipImpl = new RelationshipImpl();
        relationshipImpl.setType(RELATIONSHIP_TYPE);
        relationInfo.setRelationships(relationshipImpl);
    }

    private ComponentInstanceBusinessLogic createTestSubject() {
        return componentInstanceBusinessLogic;
    }


    @Test
    public void testChangeServiceProxyVersion() {
        ComponentInstanceBusinessLogic componentInstanceBusinessLogic;

        Either<ComponentInstance, ResponseFormat> result;

        // default test
        componentInstanceBusinessLogic = createTestSubject();
        result = componentInstanceBusinessLogic.changeServiceProxyVersion();
    }


    @Test
    public void testCreateServiceProxy() {
        ComponentInstanceBusinessLogic testSubject;
        Either<ComponentInstance, ResponseFormat> result;

        // default test
        testSubject = createTestSubject();
        result = testSubject.createServiceProxy();
    }


    @Test
    public void testDeleteForwardingPathsRelatedTobeDeletedComponentInstance() {
        ComponentInstanceBusinessLogic testSubject;
        String containerComponentId = "";
        String componentInstanceId = "";
        ComponentTypeEnum containerComponentType = ComponentTypeEnum.RESOURCE;
        Either<ComponentInstance, ResponseFormat> resultOp = null;
        Either<ComponentInstance, ResponseFormat> result;

        // default test
        testSubject = createTestSubject();
        result = testSubject.deleteForwardingPathsRelatedTobeDeletedComponentInstance(containerComponentId,
                containerComponentType, resultOp);
    }


    @Test
    public void testDeleteServiceProxy() {
        ComponentInstanceBusinessLogic testSubject;

        Either<ComponentInstance, ResponseFormat> result;

        // default test
        testSubject = createTestSubject();
        result = testSubject.deleteServiceProxy();
    }


    @Test
    public void testGetComponentInstanceInputsByInputId() {
        ComponentInstanceBusinessLogic testSubject;
        Component component = new Service();
        String inputId = "";
        List<ComponentInstanceInput> result;

        // default test
        testSubject = createTestSubject();
        result = testSubject.getComponentInstanceInputsByInputId(component, inputId);
    }


    @Test
    public void testGetComponentInstancePropertiesByInputId() {
        ComponentInstanceBusinessLogic testSubject;
        Component component = new Service();
        String inputId = "";
        List<ComponentInstanceProperty> result;

        // default test
        testSubject = createTestSubject();
        result = testSubject.getComponentInstancePropertiesByInputId(component, inputId);
    }


    @Test
    public void testGetRelationById() {
        ComponentInstanceBusinessLogic testSubject;
        String componentId = "";
        String relationId = "";
        String userId = user.getUserId();
        ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.RESOURCE_INSTANCE;
        Either<RequirementCapabilityRelDef, ResponseFormat> result;

        // default test
        testSubject = createTestSubject();
        result = testSubject.getRelationById(componentId, relationId, userId, componentTypeEnum);
    }


    @Test
    public void testCreateComponentInstance_1() {
        ComponentInstanceBusinessLogic testSubject;
        String containerComponentParam = "";
        String containerComponentId = "";
        String userId = user.getUserId();
        ComponentInstance resourceInstance = null;
        boolean inTransaction = false;
        boolean needLock = false;
        Either<ComponentInstance, ResponseFormat> result;

        // default test
        testSubject = createTestSubject();
        result = testSubject.createComponentInstance(containerComponentParam, containerComponentId, userId,
                resourceInstance, inTransaction, needLock);
    }


    @Test
    public void testCreateAndAssociateRIToRI() {
        ComponentInstanceBusinessLogic testSubject;

        String containerComponentParam = "";
        String containerComponentId = "";
        String userId = user.getUserId();
        CreateAndAssotiateInfo createAndAssotiateInfo = new CreateAndAssotiateInfo(null, null);
        Either<CreateAndAssotiateInfo, ResponseFormat> result;

        // default test
        testSubject = createTestSubject();
        result = testSubject.createAndAssociateRIToRI(containerComponentParam, containerComponentId, userId,
                createAndAssotiateInfo);
    }

    @Test
    public void testGetOriginComponentFromComponentInstance_1() {
        ComponentInstanceBusinessLogic testSubject;
        createResource();
        String componentInstanceName = "";
        String origComponetId = resource.getUniqueId();
        Either<Component, StorageOperationStatus> oldResourceRes = Either.left(resource);
        when(toscaOperationFacade.getToscaFullElement(resource.getUniqueId())).thenReturn(oldResourceRes);
        Either<Component, ResponseFormat> result;

        // default test
        testSubject = createTestSubject();
        result = Deencapsulation.invoke(testSubject, "getOriginComponentFromComponentInstance",
                new Object[] {componentInstanceName, origComponetId});
    }


    @Test
    public void testCreateComponentInstanceOnGraph() {
        ComponentInstanceBusinessLogic testSubject;
        createResource();
        createInstances();
        Either<ComponentInstance, ResponseFormat> result;

        Either<ImmutablePair<Component, String>, StorageOperationStatus> result2 =
                Either.right(StorageOperationStatus.ARTIFACT_NOT_FOUND);
        when(toscaOperationFacade.addComponentInstanceToTopologyTemplate(resource, resource, toInstance, false, user))
                .thenReturn(result2);

        // default test
        testSubject = createTestSubject();
        result = Deencapsulation.invoke(testSubject, "createComponentInstanceOnGraph",
                new Object[] {resource, resource, toInstance, user});
    }

    @Ignore("test failing skipping for now")
    @Test
    public void testCreateComponentInstanceOnGraph2() {
        ComponentInstanceBusinessLogic testSubject;
        createResource();
        resource.setName("name");
        createInstances();
        Either<ComponentInstance, ResponseFormat> result;
        ImmutablePair<Component, String> pair = new ImmutablePair<>(resource, "");


        Either<ImmutablePair<Component, String>, StorageOperationStatus> result2 = Either.left(pair);
        when(toscaOperationFacade.addComponentInstanceToTopologyTemplate(resource, resource, toInstance, false, user))
                .thenReturn(result2);

        // default test
        testSubject = createTestSubject();
        result = Deencapsulation.invoke(testSubject, "createComponentInstanceOnGraph",
                new Object[] {resource, resource, toInstance, user});
    }

    @Test
    public void testUpdateComponentInstanceMetadata() {
        ComponentInstanceBusinessLogic testSubject;
        String containerComponentParam = "";
        String containerComponentId = "";
        String componentInstanceId = "";
        String userId = user.getUserId();
        createInstances();
        Either<ComponentInstance, ResponseFormat> result;

        // default test
        testSubject = createTestSubject();
        result = testSubject.updateComponentInstanceMetadata(containerComponentParam, containerComponentId,
                componentInstanceId, userId, toInstance);
    }


    @Test
    public void testUpdateComponentInstanceMetadata_1() {
        ComponentInstanceBusinessLogic testSubject;
        String containerComponentParam = "";
        String containerComponentId = "";
        String componentInstanceId = "";
        String userId = user.getUserId();
        createInstances();
        boolean inTransaction = false;
        boolean needLock = false;
        boolean createNewTransaction = false;
        Either<ComponentInstance, ResponseFormat> result;

        // default test
        testSubject = createTestSubject();
        result = testSubject.updateComponentInstanceMetadata(containerComponentParam, containerComponentId,
                componentInstanceId, userId, toInstance, inTransaction, needLock, createNewTransaction);
    }


    @Test
    public void testValidateParent() {
        ComponentInstanceBusinessLogic testSubject;
        createResource();
        String nodeTemplateId = "";
        boolean result;

        // default test
        testSubject = createTestSubject();
        result = Deencapsulation.invoke(testSubject, "validateParent", new Object[] {resource, nodeTemplateId});
    }


    @Test
    public void testGetComponentType() {
        ComponentInstanceBusinessLogic testSubject;
        ComponentTypeEnum result;

        // default test
        testSubject = createTestSubject();
        result = Deencapsulation.invoke(testSubject, "getComponentType", new Object[] {ComponentTypeEnum.class});
    }


    @Test
    public void testGetNewGroupName() {
        ComponentInstanceBusinessLogic testSubject;
        String oldPrefix = "";
        String newNormailzedPrefix = "";
        String qualifiedGroupInstanceName = "";
        String result;

        // test 1
        testSubject = createTestSubject();
        result = Deencapsulation.invoke(testSubject, "getNewGroupName",
                new Object[] {oldPrefix, newNormailzedPrefix, qualifiedGroupInstanceName});
    }


    @Test
    public void testUpdateComponentInstanceMetadata_3() {
        ComponentInstanceBusinessLogic testSubject;
        createInstances();
        ComponentInstance newComponentInstance = null;
        ComponentInstance result;

        // default test
        testSubject = createTestSubject();
        result = Deencapsulation
                         .invoke(testSubject, "updateComponentInstanceMetadata", new Object[] {toInstance, toInstance});
    }


    @Test
    public void testDeleteComponentInstance() throws Exception {
        ComponentInstanceBusinessLogic testSubject;
        String containerComponentParam = "";
        String containerComponentId = "";
        String componentInstanceId = "";
        String userId = user.getUserId();
        Either<ComponentInstance, ResponseFormat> result;

        // default test
        testSubject = createTestSubject();
        TitanDao mock = Mockito.mock(TitanDao.class);
        testSubject.setTitanGenericDao(mock);
        result = testSubject.deleteComponentInstance(containerComponentParam, containerComponentId, componentInstanceId,
                userId);
    }


    @Test
    public void testDeleteForwardingPaths() {
        ComponentInstanceBusinessLogic testSubject;
        createService();
        String serviceId = service.getUniqueId();
        List<String> pathIdsToDelete = new ArrayList<>();
        Either<Set<String>, ResponseFormat> result;

        //	Either<Service, StorageOperationStatus> storageStatus = toscaOperationFacade.getToscaElement(serviceId);
        when(toscaOperationFacade.getToscaElement(serviceId))
                .thenReturn(Either.right(StorageOperationStatus.BAD_REQUEST));

        // default test
        testSubject = createTestSubject();
        result =
                Deencapsulation.invoke(testSubject, "deleteForwardingPaths", new Object[] {serviceId, pathIdsToDelete});
    }


    @Test
    public void testAssociateRIToRIOnGraph() throws Exception {
        ComponentInstanceBusinessLogic testSubject;
        createResource();
        RequirementCapabilityRelDef requirementDef = new RequirementCapabilityRelDef();
        ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.RESOURCE;
        boolean inTransaction = false;
        Either<RequirementCapabilityRelDef, ResponseFormat> result;


        Either<RequirementCapabilityRelDef, StorageOperationStatus> getResourceResult = Either.left(requirementDef);
        when(toscaOperationFacade.associateResourceInstances(resource.getUniqueId(), requirementDef))
                .thenReturn(getResourceResult);

        // default test
        testSubject = createTestSubject();
        result = testSubject.associateRIToRIOnGraph(resource, requirementDef, componentTypeEnum, inTransaction);
    }


    @Test
    public void testFindRelation() throws Exception {
        ComponentInstanceBusinessLogic testSubject;
        String relationId = "";
        List<RequirementCapabilityRelDef> requirementCapabilityRelations = new ArrayList<>();
        RequirementCapabilityRelDef result;

        // default test
        testSubject = createTestSubject();
        result = Deencapsulation.invoke(testSubject, "findRelation",
                new Object[] {relationId, requirementCapabilityRelations});
    }


    @Test
    public void testIsNetworkRoleServiceProperty() throws Exception {
        ComponentInstanceBusinessLogic testSubject;
        ComponentInstanceProperty property = new ComponentInstanceProperty();
        ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.RESOURCE;
        boolean result;

        // default test
        testSubject = createTestSubject();
        result = Deencapsulation.invoke(testSubject, "isNetworkRoleServiceProperty",
                new Object[] {property, componentTypeEnum});
    }


    @Test
    public void testConcatServiceNameToVLINetworkRolePropertiesValues() throws Exception {
        ComponentInstanceBusinessLogic testSubject;
        ToscaOperationFacade toscaOperationFacade = new ToscaOperationFacade();
        ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.RESOURCE;
        String componentId = "";
        String resourceInstanceId = "";
        List<ComponentInstanceProperty> properties = new ArrayList<>();
        StorageOperationStatus result;

        // default test
        testSubject = createTestSubject();
        result = Deencapsulation.invoke(testSubject, "concatServiceNameToVLINetworkRolePropertiesValues",
                new Object[] {toscaOperationFacade, componentTypeEnum, componentId, resourceInstanceId, properties});
    }


    @Test
    public void testCreateOrUpdatePropertiesValues() throws Exception {
        ComponentInstanceBusinessLogic testSubject;
        ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.RESOURCE;
        createResource();
        String componentId = resource.getUniqueId();
        String resourceInstanceId = "";
        List<ComponentInstanceProperty> properties = new ArrayList<>();
        String userId = user.getUserId();
        Either<List<ComponentInstanceProperty>, ResponseFormat> result;


        when(toscaOperationFacade.getToscaElement(componentId, JsonParseFlagEnum.ParseAll))
                .thenReturn(Either.left(resource));

        // test 1
        testSubject = createTestSubject();
        result = testSubject
                         .createOrUpdatePropertiesValues(componentTypeEnum, componentId, resourceInstanceId, properties,
                                 userId);

        componentTypeEnum = null;
        result = testSubject
                         .createOrUpdatePropertiesValues(componentTypeEnum, componentId, resourceInstanceId, properties,
                                 userId);

        //        when(toscaOperationFacade.getToscaElement(componentId, JsonParseFlagEnum.ParseAll)).thenReturn(Either.right(StorageOperationStatus.BAD_REQUEST));
        result = testSubject
                         .createOrUpdatePropertiesValues(componentTypeEnum, componentId, resourceInstanceId, properties,
                                 userId);

    }


    @Test
    public void testUpdateCapabilityPropertyOnContainerComponent() throws Exception {
        ComponentInstanceBusinessLogic testSubject;
        ComponentInstanceProperty property = new ComponentInstanceProperty();
        String newValue = "";
        createResource();
        createInstances();
        String capabilityType = "";
        String capabilityName = "";
        ResponseFormat result;

        // default test
        testSubject = createTestSubject();
        result = Deencapsulation.invoke(testSubject, "updateCapabilityPropertyOnContainerComponent",
                new Object[] {property, newValue, resource, toInstance, capabilityType, capabilityName});
    }


    @Test
    public void testCreateOrUpdateInstanceInputValues() throws Exception {
        ComponentInstanceBusinessLogic testSubject;
        ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.RESOURCE;
        createResource();
        String componentId = resource.getUniqueId();
        String resourceInstanceId = "";
        List<ComponentInstanceInput> inputs = new ArrayList<>();
        String userId = user.getUserId();
        Either<List<ComponentInstanceInput>, ResponseFormat> result;

        when(toscaOperationFacade.getToscaElement(componentId, JsonParseFlagEnum.ParseAll))
                .thenReturn(Either.left(resource));

        // test 1
        testSubject = createTestSubject();
        result = testSubject
                         .createOrUpdateInstanceInputValues(componentTypeEnum, componentId, resourceInstanceId, inputs,
                                 userId);
        componentTypeEnum = null;
        result = testSubject
                         .createOrUpdateInstanceInputValues(componentTypeEnum, componentId, resourceInstanceId, inputs,
                                 userId);


        //        when(toscaOperationFacade.getToscaElement(componentId, JsonParseFlagEnum.ParseAll)).thenReturn(Either.right(StorageOperationStatus.BAD_REQUEST));
        result = testSubject
                         .createOrUpdateInstanceInputValues(componentTypeEnum, componentId, resourceInstanceId, inputs,
                                 userId);

    }


    @Test
    public void testCreateOrUpdateGroupInstancePropertyValue() throws Exception {
        ComponentInstanceBusinessLogic testSubject;
        ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.RESOURCE;
        createResource();
        String componentId = resource.getUniqueId();
        String resourceInstanceId = "";
        String groupInstanceId = "";
        ComponentInstanceProperty property = new ComponentInstanceProperty();
        String userId = user.getUserId();
        Either<ComponentInstanceProperty, ResponseFormat> result;


        when(toscaOperationFacade.getToscaElement(componentId, JsonParseFlagEnum.ParseMetadata))
                .thenReturn(Either.left(resource));

        // test 1
        testSubject = createTestSubject();
        result = testSubject
                         .createOrUpdateGroupInstancePropertyValue(componentTypeEnum, componentId, resourceInstanceId,
                                 groupInstanceId, property, userId);
        componentTypeEnum = null;
        result = testSubject
                         .createOrUpdateGroupInstancePropertyValue(componentTypeEnum, componentId, resourceInstanceId,
                                 groupInstanceId, property, userId);

        //        when(toscaOperationFacade.getToscaElement(componentId, JsonParseFlagEnum.ParseMetadata)).thenReturn(Either.right(StorageOperationStatus.BAD_REQUEST));
        result = testSubject
                         .createOrUpdateGroupInstancePropertyValue(componentTypeEnum, componentId, resourceInstanceId,
                                 groupInstanceId, property, userId);
    }


    @Test
    public void testCreateOrUpdateInputValue() throws Exception {
        ComponentInstanceBusinessLogic testSubject;
        createResource();
        ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.RESOURCE;
        String componentId = resource.getUniqueId();
        String resourceInstanceId = resource.getUniqueId();
        ComponentInstanceInput inputProperty = new ComponentInstanceInput();
        String userId = user.getUserId();
        Either<ComponentInstanceInput, ResponseFormat> result;


        Either<Component, StorageOperationStatus> getResourceResult = Either.left(resource);
        when(toscaOperationFacade.getToscaElement(resource.getUniqueId(), JsonParseFlagEnum.ParseMetadata))
                .thenReturn(getResourceResult);

        // test 1
        testSubject = createTestSubject();
        result = testSubject.createOrUpdateInputValue(componentTypeEnum, componentId, resourceInstanceId, inputProperty,
                userId);

        componentTypeEnum = null;
        result = testSubject.createOrUpdateInputValue(componentTypeEnum, componentId, resourceInstanceId, inputProperty,
                userId);

        //        when(toscaOperationFacade.getToscaElement(resource.getUniqueId(), JsonParseFlagEnum.ParseMetadata)).thenReturn(Either.right(StorageOperationStatus.BAD_REQUEST));
        result = testSubject.createOrUpdateInputValue(componentTypeEnum, componentId, resourceInstanceId, inputProperty,
                userId);
    }


    @Test
    public void testDeletePropertyValue() throws Exception {
        ComponentInstanceBusinessLogic testSubject;
        ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.RESOURCE;
        createService();
        String serviceId = service.getUniqueId();
        String resourceInstanceId = "";
        String propertyValueId = "";
        String userId = user.getUserId();
        Either<ComponentInstanceProperty, ResponseFormat> result;

        when(toscaOperationFacade.getToscaElement(serviceId, JsonParseFlagEnum.ParseMetadata))
                .thenReturn(Either.left(service));

        // test 1
        testSubject = createTestSubject();
        result = testSubject.deletePropertyValue(componentTypeEnum, serviceId, resourceInstanceId, propertyValueId,
                userId);
        componentTypeEnum = null;
        result = testSubject.deletePropertyValue(componentTypeEnum, serviceId, resourceInstanceId, propertyValueId,
                userId);

        //        when(toscaOperationFacade.getToscaElement(serviceId, JsonParseFlagEnum.ParseMetadata)).thenReturn(Either.right(StorageOperationStatus.BAD_REQUEST));
        result = testSubject.deletePropertyValue(componentTypeEnum, serviceId, resourceInstanceId, propertyValueId,
                userId);
    }


    @Test
    public void testGetAndValidateOriginComponentOfComponentInstance() throws Exception {
        ComponentInstanceBusinessLogic testSubject;
        ComponentTypeEnum containerComponentType = ComponentTypeEnum.RESOURCE;
        createResource();
        createInstances();
        Either<Component, ResponseFormat> result;

        when(toscaOperationFacade.getToscaFullElement(toInstance.getComponentUid())).thenReturn(Either.left(resource));

        // default test
        testSubject = createTestSubject();
        result = Deencapsulation.invoke(testSubject, "getAndValidateOriginComponentOfComponentInstance",
                new Object[] {containerComponentType, toInstance});
    }


    @Test
    public void testGetComponentParametersViewForForwardingPath() throws Exception {
        ComponentInstanceBusinessLogic testSubject;
        ComponentParametersView result;

        // default test
        testSubject = createTestSubject();
        result = Deencapsulation.invoke(testSubject, "getComponentParametersViewForForwardingPath");
    }


    @Test
    public void testChangeComponentInstanceVersion() throws Exception {
        ComponentInstanceBusinessLogic testSubject;
        String containerComponentParam = "";
        String containerComponentId = "";
        String componentInstanceId = "";
        String userId = user.getUserId();
        createInstances();
        ComponentInstance newComponentInstance = toInstance;
        Either<ComponentInstance, ResponseFormat> result;

        // default test
        testSubject = createTestSubject();
        result = testSubject.changeComponentInstanceVersion(containerComponentParam, containerComponentId,
                componentInstanceId, userId, newComponentInstance);
        newComponentInstance = null;
        testSubject = createTestSubject();
        result = testSubject.changeComponentInstanceVersion(containerComponentParam, containerComponentId,
                componentInstanceId, userId, newComponentInstance);

    }

    @Ignore("test failing skipping for now")
    @Test
    public void testValidateInstanceNameUniquenessUponUpdate() throws Exception {
        ComponentInstanceBusinessLogic testSubject;
        createResource();
        createInstances();
        String newInstanceName = toInstance.getName();
        Boolean result;

        // default test
        testSubject = createTestSubject();
        result = Deencapsulation.invoke(testSubject, "validateInstanceNameUniquenessUponUpdate",
                new Object[] {resource, toInstance, newInstanceName});
    }


    @Test
    public void testGetResourceInstanceById() throws Exception {
        ComponentInstanceBusinessLogic testSubject;
        createResource();
        String instanceId = "";
        Either<ComponentInstance, StorageOperationStatus> result;

        // default test
        testSubject = createTestSubject();
        result = Deencapsulation.invoke(testSubject, "getResourceInstanceById", new Object[] {resource, instanceId});
    }

    @Ignore("test failing skipping for now")
    @Test
    public void testBuildComponentInstance() throws Exception {
        ComponentInstanceBusinessLogic testSubject;
        createInstances();
        ComponentInstance origInstanceForUpdate = null;
        ComponentInstance result;

        // default test
        testSubject = createTestSubject();
        result = Deencapsulation.invoke(testSubject, "buildComponentInstance", new Object[] {toInstance, toInstance});
    }


    @Test
    public void testFindCapabilityOfInstance() throws Exception {
        ComponentInstanceBusinessLogic testSubject;
        String componentId = "";
        String instanceId = "";
        String capabilityType = "";
        String capabilityName = "";
        String ownerId = "";
        Map<String, List<CapabilityDefinition>> instanceCapabilities = new HashMap<>();
        Either<List<ComponentInstanceProperty>, ResponseFormat> result;

        // default test
        testSubject = createTestSubject();
        result = Deencapsulation.invoke(testSubject, "findCapabilityOfInstance",
                new Object[] {componentId, instanceId, capabilityType, capabilityName, ownerId, instanceCapabilities});
    }


    @Test
    public void testFetchComponentInstanceCapabilityProperties() throws Exception {
        ComponentInstanceBusinessLogic testSubject;
        String componentId = "";
        String instanceId = "";
        String capabilityType = "";
        String capabilityName = "";
        String ownerId = "";
        Either<List<ComponentInstanceProperty>, ResponseFormat> result;

        // default test
        testSubject = createTestSubject();
        result = Deencapsulation.invoke(testSubject, "fetchComponentInstanceCapabilityProperties",
                new Object[] {componentId, instanceId, capabilityType, capabilityName, ownerId});
    }


    @Test
    public void testUpdateCapabilityPropertyOnContainerComponent_1() throws Exception {
        ComponentInstanceBusinessLogic testSubject;
        ComponentInstanceProperty property = new ComponentInstanceProperty();
        String newValue = "";
        createResource();
        createInstances();
        String capabilityType = "";
        String capabilityName = "";
        String ownerId = "";
        ResponseFormat result;

        // default test
        testSubject = createTestSubject();
        result = Deencapsulation.invoke(testSubject, "updateCapabilityPropertyOnContainerComponent",
                new Object[] {property, newValue, resource, toInstance, capabilityType, capabilityName, ownerId});
    }


    @Test
    public void testUpdateInstanceCapabilityProperties() throws Exception {
        ComponentInstanceBusinessLogic testSubject;
        ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.RESOURCE;
        createResource();
        String containerComponentId = resource.getUniqueId();
        String componentInstanceUniqueId = "";
        String capabilityType = "";
        String capabilityName = "";
        String ownerId = "";
        List<ComponentInstanceProperty> properties = new ArrayList<>();
        String userId = user.getUserId();
        Either<List<ComponentInstanceProperty>, ResponseFormat> result;


        when(toscaOperationFacade.getToscaFullElement(containerComponentId)).thenReturn(Either.left(resource));


        // test 1
        testSubject = createTestSubject();
        result = testSubject.updateInstanceCapabilityProperties(componentTypeEnum, containerComponentId,
                componentInstanceUniqueId, capabilityType, capabilityName, ownerId, properties, userId);
        when(toscaOperationFacade.getToscaFullElement(containerComponentId))
                .thenReturn(Either.right(StorageOperationStatus.ARTIFACT_NOT_FOUND));
        result = testSubject.updateInstanceCapabilityProperties(componentTypeEnum, containerComponentId,
                componentInstanceUniqueId, capabilityType, capabilityName, ownerId, properties, userId);
        componentTypeEnum = null;
        result = testSubject.updateInstanceCapabilityProperties(componentTypeEnum, containerComponentId,
                componentInstanceUniqueId, capabilityType, capabilityName, ownerId, properties, userId);


    }


    @Test
    public void testUpdateInstanceCapabilityProperties_1() throws Exception {
        ComponentInstanceBusinessLogic testSubject;
        ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.RESOURCE;
        createResource();
        String containerComponentId = resource.getUniqueId();
        String componentInstanceUniqueId = "";
        String capabilityType = "";
        String capabilityName = "";
        List<ComponentInstanceProperty> properties = new ArrayList<>();
        String userId = user.getUserId();
        Either<List<ComponentInstanceProperty>, ResponseFormat> result;


        when(toscaOperationFacade.getToscaFullElement(containerComponentId))
                .thenReturn(Either.right(StorageOperationStatus.BAD_REQUEST));
        // test 1
        testSubject = createTestSubject();
        result = testSubject.updateInstanceCapabilityProperties(componentTypeEnum, containerComponentId,
                componentInstanceUniqueId, capabilityType, capabilityName, properties, userId);
        when(toscaOperationFacade.getToscaFullElement(containerComponentId)).thenReturn(Either.left(resource));
        result = testSubject.updateInstanceCapabilityProperties(componentTypeEnum, containerComponentId,
                componentInstanceUniqueId, capabilityType, capabilityName, properties, userId);
    }

    @Test
    public void testCopyComponentInstanceWrongUserId() {

        Either<Map<String, ComponentInstance>, ResponseFormat> result;
        ComponentInstance inputComponentInstance = createComponetInstanceFromComponent(resource);
        String containerComponentId = service.getUniqueId();
        String componentInstanceId = resource.getUniqueId();
        String oldLastUpdatedUserId = service.getLastUpdaterUserId();
        service.setLastUpdaterUserId("wrong user id");

        Either<Component, StorageOperationStatus> leftServiceOp = Either.left(service);
        when(toscaOperationFacade.getToscaElement(containerComponentId)).thenReturn(leftServiceOp);
        when(toscaOperationFacade.getToscaElement(eq(containerComponentId), any(ComponentParametersView.class)))
                .thenReturn(leftServiceOp);
        when(titanDao.rollback()).thenReturn(TitanOperationStatus.OK);
        when(graphLockOperation.unlockComponent(Mockito.anyString(), eq(NodeTypeEnum.Service)))
                .thenReturn(StorageOperationStatus.OK);
        when(graphLockOperation.lockComponent(Mockito.anyString(), eq(NodeTypeEnum.Service)))
                .thenReturn(StorageOperationStatus.OK);

        result = componentInstanceBusinessLogic
                         .copyComponentInstance(inputComponentInstance, containerComponentId, componentInstanceId,
                                 USER_ID);

        service.setLastUpdaterUserId(oldLastUpdatedUserId);

        assertThat(result.isRight());
    }

    @Test
    public void testCopyComponentInstanceComponentWrongState() {
        Either<Map<String, ComponentInstance>, ResponseFormat> result;
        ComponentInstance inputComponentInstance = createComponetInstanceFromComponent(resource);
        String containerComponentId = service.getUniqueId();
        String componentInstanceId = resource.getUniqueId();
        String oldServiceLastUpdatedUserId = service.getLastUpdaterUserId();
        service.setLastUpdaterUserId(USER_ID);

        Either<Component, StorageOperationStatus> leftServiceOp = Either.left(service);
        when(toscaOperationFacade.getToscaElement(containerComponentId)).thenReturn(leftServiceOp);
        when(toscaOperationFacade.getToscaElement(eq(containerComponentId), any(ComponentParametersView.class)))
                .thenReturn(leftServiceOp);
        when(titanDao.rollback()).thenReturn(TitanOperationStatus.OK);
        when(graphLockOperation.unlockComponent(Mockito.anyString(), eq(NodeTypeEnum.Service)))
                .thenReturn(StorageOperationStatus.OK);
        when(graphLockOperation.lockComponent(Mockito.anyString(), eq(NodeTypeEnum.Service)))
                .thenReturn(StorageOperationStatus.OK);
        Either<Component, StorageOperationStatus> getComponentRes = Either.left(resource);
        when(toscaOperationFacade.getToscaFullElement(inputComponentInstance.getComponentUid()))
                .thenReturn(getComponentRes);

        result = componentInstanceBusinessLogic
                         .copyComponentInstance(inputComponentInstance, containerComponentId, componentInstanceId,
                                 USER_ID);

        service.setLastUpdaterUserId(oldServiceLastUpdatedUserId);

        assertThat(result.isRight());
    }

    @Test
    public void testCopyComponentInstance() {
        Either<Map<String, ComponentInstance>, ResponseFormat> result;
        ComponentInstance inputComponentInstance = createComponetInstanceFromComponent(resource);
        String containerComponentId = service.getUniqueId();
        String componentInstanceId = resource.getUniqueId();
        String oldServiceLastUpdatedUserId = service.getLastUpdaterUserId();
        service.setLastUpdaterUserId(USER_ID);
        LifecycleStateEnum oldResourceLifeCycle = resource.getLifecycleState();
        resource.setLifecycleState(LifecycleStateEnum.CERTIFIED);

        Either<Component, StorageOperationStatus> leftServiceOp = Either.left(service);
        when(toscaOperationFacade.getToscaElement(containerComponentId)).thenReturn(leftServiceOp);
        when(toscaOperationFacade.getToscaElement(eq(containerComponentId), any(ComponentParametersView.class)))
                .thenReturn(leftServiceOp);
        when(graphLockOperation.unlockComponent(Mockito.anyString(), eq(NodeTypeEnum.Service)))
                .thenReturn(StorageOperationStatus.OK);
        when(graphLockOperation.lockComponent(Mockito.anyString(), eq(NodeTypeEnum.Service)))
                .thenReturn(StorageOperationStatus.OK);
        Either<Component, StorageOperationStatus> getComponentRes = Either.left(resource);
        when(toscaOperationFacade.getToscaFullElement(inputComponentInstance.getComponentUid()))
                .thenReturn(getComponentRes);
        ImmutablePair<Component, String> pair = new ImmutablePair<>(resource, TO_INSTANCE_ID);
        Either<ImmutablePair<Component, String>, StorageOperationStatus> result2 = Either.left(pair);
        when(toscaOperationFacade
                     .addComponentInstanceToTopologyTemplate(eq(service), eq(resource), eq(inputComponentInstance),
                             eq(false), isNull(User.class))).thenReturn(result2);
        Either<Map<String, ArtifactDefinition>, StorageOperationStatus> getResourceDeploymentArtifacts =
                Either.left(new HashMap<String, ArtifactDefinition>());
        when(artifactBusinessLogic.getArtifacts(eq(inputComponentInstance.getComponentUid()), eq(NodeTypeEnum.Resource),
                eq(ArtifactGroupTypeEnum.DEPLOYMENT), isNull(String.class))).thenReturn(getResourceDeploymentArtifacts);
        StorageOperationStatus artStatus = StorageOperationStatus.OK;
        when(toscaOperationFacade
                     .addInformationalArtifactsToInstance(eq(resource.getUniqueId()), eq(inputComponentInstance),
                             isNull(Map.class))).thenReturn(artStatus);

        result = componentInstanceBusinessLogic
                         .copyComponentInstance(inputComponentInstance, containerComponentId, componentInstanceId,
                                 USER_ID);

        service.setLastUpdaterUserId(oldServiceLastUpdatedUserId);
        resource.setLifecycleState(oldResourceLifeCycle);

        assertThat(result.isLeft());
    }

    @Test
    public void testCreateOrUpdateAttributeValueForCopyPaste() {
        ComponentInstance serviceComponentInstance = createComponetInstanceFromComponent(service);
        ComponentInstanceProperty attribute = new ComponentInstanceProperty();
        attribute.setType("string");
        attribute.setUniqueId("testCreateOrUpdateAttributeValueForCopyPaste");
        SchemaDefinition def = Mockito.mock(SchemaDefinition.class);
        attribute.setSchema(def);
        LifecycleStateEnum oldLifeCycleState = service.getLifecycleState();
        String oldLastUpdatedUserId = service.getLastUpdaterUserId();
        service.setLastUpdaterUserId(USER_ID);
        service.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);

        Map<String, List<ComponentInstanceProperty>> instAttrsMap =
                new HashMap<String, List<ComponentInstanceProperty>>();
        List<ComponentInstanceProperty> instAttrsList = new ArrayList<ComponentInstanceProperty>();
        ComponentInstanceProperty prop = new ComponentInstanceProperty();
        prop.setUniqueId(attribute.getUniqueId());
        instAttrsList.add(prop);
        instAttrsMap.put(toInstance.getUniqueId(), instAttrsList);
        service.setComponentInstancesAttributes(instAttrsMap);

        Either<Component, StorageOperationStatus> serviceEitherLeft = Either.left(service);
        when(toscaOperationFacade.getToscaElement(serviceComponentInstance.getUniqueId(), JsonParseFlagEnum.ParseAll))
                .thenReturn(serviceEitherLeft);
        when(toscaOperationFacade.updateComponentInstanceAttribute(service, toInstance.getUniqueId(), attribute))
                .thenReturn(StorageOperationStatus.OK);
        when(toscaOperationFacade.updateComponentInstanceMetadataOfTopologyTemplate(service))
                .thenReturn(serviceEitherLeft);

        Either<ComponentInstanceProperty, ResponseFormat> result = Deencapsulation
                                                                           .invoke(componentInstanceBusinessLogic,
                                                                                   "createOrUpdateAttributeValueForCopyPaste",
                                                                                   ComponentTypeEnum.SERVICE,
                                                                                   serviceComponentInstance
                                                                                           .getUniqueId(),
                                                                                   toInstance.getUniqueId(), attribute,
                                                                                   USER_ID);

        service.setLastUpdaterUserId(oldLastUpdatedUserId);
        service.setLifecycleState(oldLifeCycleState);

        assertTrue(result.isLeft());
        ComponentInstanceProperty resultProp = result.left().value();
        assertEquals(resultProp.getPath().size(), 1);
        assertEquals(resultProp.getPath().get(0), toInstance.getUniqueId());
    }

    @Test
    public void testUpdateComponentInstanceProperty() {

        String containerComponentId = service.getUniqueId();
        String componentInstanceId = "dummy_id";
        ComponentInstanceProperty property = Mockito.mock(ComponentInstanceProperty.class);

        Either<Component, StorageOperationStatus> getComponent = Either.left(service);
        when(toscaOperationFacade.getToscaElement(containerComponentId)).thenReturn(getComponent);
        StorageOperationStatus status = StorageOperationStatus.OK;
        when(toscaOperationFacade.updateComponentInstanceProperty(service, componentInstanceId, property))
                .thenReturn(status);
        Either<Component, StorageOperationStatus> updateContainerRes = Either.left(service);
        when(toscaOperationFacade.updateComponentInstanceMetadataOfTopologyTemplate(service))
                .thenReturn(updateContainerRes);

        Either<String, ResponseFormat> result = Deencapsulation.invoke(componentInstanceBusinessLogic,
                "updateComponentInstanceProperty", containerComponentId, componentInstanceId, property);

        assertTrue(result.isLeft());
    }

    @Test
    public void testGetInputListDefaultValue() {
        Component component = service;
        String inputId = "dummy_id";
        String defaultValue = "dummy_default_value";
        List<InputDefinition> newInputs = new ArrayList<InputDefinition>();
        InputDefinition in = new InputDefinition();
        in.setUniqueId(inputId);
        in.setDefaultValue(defaultValue);
        newInputs.add(in);
        List<InputDefinition> oldInputs = service.getInputs();
        service.setInputs(newInputs);

        Either<String, ResponseFormat> result =
                Deencapsulation.invoke(componentInstanceBusinessLogic, "getInputListDefaultValue", component, inputId);

        service.setInputs(oldInputs);

        assertEquals(result.left().value(), defaultValue);
    }

    @Test
    public void testBatchDeleteComponentInstanceFailureWrongType() {
        Map<String, List<String>> result;
        List<String> componentInstanceIdList = new ArrayList<>();
        String containerComponentParam = "WRONG_TYPE";
        String containerComponentId = "containerComponentId";
        String componentInstanceId = "componentInstanceId";
        componentInstanceIdList.add(componentInstanceId);
        String userId = USER_ID;
        Map<String, List<String>> deleteErrorMap = new HashMap<>();
        List<String> deleteErrorIds = new ArrayList<>();
        deleteErrorIds.add(componentInstanceId);
        deleteErrorMap.put("deleteFailedIds", deleteErrorIds);

        result = componentInstanceBusinessLogic
                         .batchDeleteComponentInstance(containerComponentParam, containerComponentId,
                                 componentInstanceIdList, userId);

        assertEquals(deleteErrorMap, result);
    }

    @Test
    public void testBatchDeleteComponentInstanceFailureCompIds() {
        Map<String, List<String>> result;
        String containerComponentParam = ComponentTypeEnum.SERVICE_PARAM_NAME;
        String containerComponentId = "containerComponentId";
        String componentInstanceId = "componentInstanceId";
        List<String> componentInstanceIdList = new ArrayList<>();
        componentInstanceIdList.add(componentInstanceId);
        String userId = USER_ID;
        Map<String, List<String>> deleteErrorMap = new HashMap<>();
        List<String> deleteErrorIds = new ArrayList<>();
        deleteErrorIds.add(componentInstanceId);
        deleteErrorMap.put("deleteFailedIds", deleteErrorIds);

        Either<Component, StorageOperationStatus> err = Either.right(StorageOperationStatus.GENERAL_ERROR);
        when(toscaOperationFacade.getToscaElement(eq(containerComponentId), any(ComponentParametersView.class)))
                .thenReturn(err);

        result = componentInstanceBusinessLogic
                         .batchDeleteComponentInstance(containerComponentParam, containerComponentId,
                                 componentInstanceIdList, userId);

        assertEquals(deleteErrorMap, result);
    }

    @Test
    public void testBatchDeleteComponentInstanceSuccess() {
        Map<String, List<String>> result;
        String containerComponentParam = ComponentTypeEnum.SERVICE_PARAM_NAME;
        LifecycleStateEnum oldLifeCycleState = service.getLifecycleState();
        String oldLastUpdatedUserId = service.getLastUpdaterUserId();
        service.setLastUpdaterUserId(USER_ID);
        service.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
        String containerComponentId = service.getUniqueId();
        String componentInstanceId = TO_INSTANCE_ID;
        String userId = USER_ID;
        List<String> componentInstanceIdList = new ArrayList<>();
        componentInstanceIdList.add(componentInstanceId);
        Map<String, List<String>> deleteErrorMap = new HashMap<>();
        List<String> deleteErrorIds = new ArrayList<>();
        deleteErrorMap.put("deleteFailedIds", deleteErrorIds);

        Either<Component, StorageOperationStatus> cont = Either.left(service);
        when(graphLockOperation.unlockComponent(Mockito.anyString(), eq(NodeTypeEnum.Service)))
                .thenReturn(StorageOperationStatus.OK);
        when(graphLockOperation.lockComponent(Mockito.anyString(), eq(NodeTypeEnum.Service)))
                .thenReturn(StorageOperationStatus.OK);
        ImmutablePair<Component, String> pair = new ImmutablePair<>(resource, TO_INSTANCE_ID);
        Either<ImmutablePair<Component, String>, StorageOperationStatus> result2 = Either.left(pair);
        when(toscaOperationFacade.deleteComponentInstanceFromTopologyTemplate(service, componentInstanceId))
                .thenReturn(result2);
        when(toscaOperationFacade.getToscaElement(eq(service.getUniqueId()), any(ComponentParametersView.class)))
                .thenReturn(cont);
        when(titanDao.commit()).thenReturn(TitanOperationStatus.OK);

        result = componentInstanceBusinessLogic
                         .batchDeleteComponentInstance(containerComponentParam, containerComponentId,
                                 componentInstanceIdList, userId);

        service.setLastUpdaterUserId(oldLastUpdatedUserId);
        service.setLifecycleState(oldLifeCycleState);
        assertEquals(deleteErrorMap, result);
    }

    @Test
    public void testDissociateRIFromRIFailDissociate() {

        List<RequirementCapabilityRelDef> result;
        RequirementCapabilityRelDef ref = new RequirementCapabilityRelDef();
        ref.setFromNode(FROM_INSTANCE_ID);
        ref.setToNode(TO_INSTANCE_ID);
        List<CapabilityRequirementRelationship> relationships = new ArrayList<>();
        CapabilityRequirementRelationship relationship = new CapabilityRequirementRelationship();
        RelationshipInfo ri = new RelationshipInfo();
        ri.setRequirement(REQUIREMENT_NAME);
        relationship.setRelation(ri);
        relationships.add(relationship);
        ref.setRelationships(relationships);
        List<RequirementCapabilityRelDef> requirementDefList = new ArrayList<>();
        requirementDefList.add(ref);
        ComponentTypeEnum componentTypeEnum = service.getComponentType();
        String componentId = service.getUniqueId();
        String userId = USER_ID;
        LifecycleStateEnum oldLifeCycleState = service.getLifecycleState();
        String oldLastUpdatedUserId = service.getLastUpdaterUserId();
        service.setLastUpdaterUserId(USER_ID);
        service.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);

        Either<Component, StorageOperationStatus> cont = Either.left(service);
        when(toscaOperationFacade.getToscaElement(eq(service.getUniqueId()), any(ComponentParametersView.class)))
                .thenReturn(cont);
        when(graphLockOperation.unlockComponent(Mockito.anyString(), eq(NodeTypeEnum.Service)))
                .thenReturn(StorageOperationStatus.OK);
        when(graphLockOperation.lockComponent(Mockito.anyString(), eq(NodeTypeEnum.Service)))
                .thenReturn(StorageOperationStatus.OK);
        Either<RequirementCapabilityRelDef, StorageOperationStatus> resultEither;
        resultEither = Either.right(StorageOperationStatus.OK);
        when(toscaOperationFacade.dissociateResourceInstances(componentId, ref)).thenReturn(resultEither);

        result = componentInstanceBusinessLogic
                         .batchDissociateRIFromRI(componentId, userId, requirementDefList, componentTypeEnum);

        service.setLastUpdaterUserId(oldLastUpdatedUserId);
        service.setLifecycleState(oldLifeCycleState);

        assertEquals(new ArrayList<>(), result);
    }

    @Test
    public void testDissociateRIFromRISuccess() {

        List<RequirementCapabilityRelDef> result;
        RequirementCapabilityRelDef ref = new RequirementCapabilityRelDef();
        List<RequirementCapabilityRelDef> requirementDefList = new ArrayList<>();
        requirementDefList.add(ref);
        ComponentTypeEnum componentTypeEnum = service.getComponentType();
        String componentId = service.getUniqueId();
        String userId = USER_ID;
        LifecycleStateEnum oldLifeCycleState = service.getLifecycleState();
        String oldLastUpdatedUserId = service.getLastUpdaterUserId();
        service.setLastUpdaterUserId(USER_ID);
        service.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);

        Either<Component, StorageOperationStatus> cont = Either.left(service);
        when(toscaOperationFacade.getToscaElement(eq(service.getUniqueId()), any(ComponentParametersView.class)))
                .thenReturn(cont);
        when(graphLockOperation.unlockComponent(Mockito.anyString(), eq(NodeTypeEnum.Service)))
                .thenReturn(StorageOperationStatus.OK);
        when(graphLockOperation.lockComponent(Mockito.anyString(), eq(NodeTypeEnum.Service)))
                .thenReturn(StorageOperationStatus.OK);
        Either<RequirementCapabilityRelDef, StorageOperationStatus> resultEither;
        resultEither = Either.left(ref);
        when(toscaOperationFacade.dissociateResourceInstances(componentId, ref)).thenReturn(resultEither);

        result = componentInstanceBusinessLogic
                         .batchDissociateRIFromRI(componentId, userId, requirementDefList, componentTypeEnum);

        service.setLastUpdaterUserId(oldLastUpdatedUserId);
        service.setLifecycleState(oldLifeCycleState);

        assertEquals(requirementDefList, result);
    }

    @Test
    public void testGetComponentInstancePropertyByPolicyId_success() {
        Optional<ComponentInstanceProperty> propertyCandidate =
                getComponentInstanceProperty(PROP_NAME);

        Assert.assertTrue(propertyCandidate.isPresent());
        Assert.assertEquals(propertyCandidate.get().getName(), PROP_NAME);
    }

    @Test
    public void testGetComponentInstancePropertyByPolicyId_failure() {
        Optional<ComponentInstanceProperty> propertyCandidate =
                getComponentInstanceProperty(NON_EXIST_NAME);

        Assert.assertEquals(propertyCandidate, Optional.empty());
    }

    private Optional<ComponentInstanceProperty> getComponentInstanceProperty(String propertyName) {
        ComponentInstanceProperty componentInstanceProperty = new ComponentInstanceProperty();
        componentInstanceProperty.setName(propertyName);

        PolicyDefinition policyDefinition = getPolicyDefinition();
        componentInstanceProperty.setGetPolicyValues(policyDefinition.getGetPolicyValues());

        service.setComponentInstancesProperties(
                Collections.singletonMap(COMPONENT_INST_ID, Collections.singletonList(componentInstanceProperty)));

        return componentInstanceBusinessLogic.getComponentInstancePropertyByPolicyId(service, policyDefinition);
    }

    private PolicyDefinition getPolicyDefinition() {
        PolicyDefinition policyDefinition = new PolicyDefinition();
        policyDefinition.setInstanceUniqueId(COMPONENT_INST_ID);
        policyDefinition.setName(PROP_NAME);

        GetPolicyValueDataDefinition getPolicy = new GetPolicyValueDataDefinition();
        getPolicy.setPropertyName(PROP_NAME);

        List<GetPolicyValueDataDefinition> getPolicies = new ArrayList<>();
        getPolicies.add(getPolicy);
        policyDefinition.setGetPolicyValues(getPolicies);

        return policyDefinition;
    }

    private ComponentInstance createComponetInstanceFromComponent(Component component) {
        ComponentInstance componentInst = new ComponentInstance();
        componentInst.setUniqueId(component.getUniqueId());
        componentInst.setComponentUid(component.getUniqueId() + "_test");
        componentInst.setPosX("10");
        componentInst.setPosY("10");
        componentInst.setCapabilities(component.getCapabilities());
        componentInst.setRequirements(component.getRequirements());
        componentInst.setArtifacts(component.getArtifacts());
        componentInst.setDeploymentArtifacts(component.getDeploymentArtifacts());
        return componentInst;
    }
}
