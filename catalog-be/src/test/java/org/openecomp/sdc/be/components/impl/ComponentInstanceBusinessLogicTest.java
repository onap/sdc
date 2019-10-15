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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.sdc.be.components.impl.exceptions.ByActionStatusComponentException;
import org.openecomp.sdc.be.components.impl.exceptions.ComponentException;
import org.openecomp.sdc.be.components.validation.UserValidations;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.dao.jsongraph.JanusGraphDao;
import org.openecomp.sdc.be.dao.jsongraph.types.JsonParseFlagEnum;
import org.openecomp.sdc.be.datatypes.elements.CapabilityDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ForwardingPathDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ForwardingPathElementDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.GetPolicyValueDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ListDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.RequirementDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.SchemaDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.impl.ServletUtils;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.CapabilityDefinition;
import org.openecomp.sdc.be.model.CapabilityRequirementRelationship;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.ComponentInstanceInput;
import org.openecomp.sdc.be.model.ComponentInstancePropInput;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.ComponentParametersView;
import org.openecomp.sdc.be.model.DataTypeDefinition;
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
import org.openecomp.sdc.be.model.cache.ApplicationDataTypeCache;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ForwardingPathOperation;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.GraphLockOperation;
import org.openecomp.sdc.be.model.operations.impl.PropertyOperation;
import org.openecomp.sdc.be.user.UserBusinessLogic;
import org.openecomp.sdc.common.api.ArtifactGroupTypeEnum;
import org.openecomp.sdc.common.api.ArtifactTypeEnum;
import org.openecomp.sdc.common.api.ConfigurationSource;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.openecomp.sdc.common.impl.FSConfigurationSource;
import org.openecomp.sdc.exception.ResponseFormat;

/**
 * The test suite designed for test functionality of ComponentInstanceBusinessLogic class
 */

@RunWith(MockitoJUnitRunner.class)
public class ComponentInstanceBusinessLogicTest {

    private final static String USER_ID = "jh0003";
    private final static String COMPONENT_ID = "componentId";
    private final static String ORIGIN_COMPONENT_ID = "originComponentId";
    private final static String COMPONENT_INST_ID = "componentInstId";
    private final static String TO_INSTANCE_ID = "toInstanceId";
    private final static String TO_INSTANCE_NAME = "toInstanceName";
    private final static String COMPONENT_INSTANCE_ID = "componentInstanceId";
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

    static ConfigurationSource configurationSource = new FSConfigurationSource(
        ExternalConfiguration.getChangeListener(),
        "src/test/resources/config/catalog-be");
    static ConfigurationManager configurationManager = new ConfigurationManager(configurationSource);

    @InjectMocks
    private static ComponentInstanceBusinessLogic componentInstanceBusinessLogic;
    @Mock
    private ComponentInstancePropInput componentInstancePropInput;
    @Mock
    ArtifactsBusinessLogic artifactsBusinessLogic;
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
    GraphLockOperation graphLockOperation;
    @Mock
    private JanusGraphDao janusGraphDao;
    @Mock
    ApplicationDataTypeCache dataTypeCache;
    @Mock
    PropertyOperation propertyOperation;
    @Mock
    ApplicationDataTypeCache applicationDataTypeCache;

    private Component service;
    private Component resource;
    private ComponentInstance toInstance;
    private ComponentInstance fromInstance;
    private CapabilityDataDefinition capability;
    private RequirementDataDefinition requirement;
    private RequirementCapabilityRelDef relation;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(componentInstanceBusinessLogic);
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

    private void getforwardingPathOnVersionChange() {
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
    public void testCreateOrUpdatePropertiesValues2() {
        String containerComponentID = "containerId";
        String resourceInstanceId = "resourceId";
        String componentInstanceID = "componentInstance";
        List<ComponentInstanceProperty> properties = new ArrayList<>();
        ComponentInstanceProperty property = new ComponentInstanceProperty();
        property.setName("property");
        property.setValue("newVal");
        property.setType("string");
        properties.add(property);

        List<ComponentInstanceProperty> origProperties = new ArrayList<>();
        ComponentInstanceProperty origProperty = new ComponentInstanceProperty();
        origProperty.setName("property");
        origProperty.setValue("value");
        origProperty.setType("string");
        origProperties.add(origProperty);

        Map<String, DataTypeDefinition> types = new HashMap<>();
        DataTypeDefinition dataTypeDef = new DataTypeDefinition();
        types.put("string", dataTypeDef);

        Component component = new Service();
        component.setLastUpdaterUserId("userId");
        component.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
        Map<String, List<ComponentInstanceProperty>> componentInstanceProps = new HashMap<>();
        componentInstanceProps.put("resourceId", origProperties);
        component.setComponentInstancesProperties(componentInstanceProps);
        ComponentInstance ci = createComponentInstance("ci1");
        ci.setUniqueId("resourceId");
        component.setComponentInstances(Arrays.asList(ci, createComponentInstance("ci2"),
            createComponentInstance(componentInstanceID)));
        HashMap<String, DataTypeDefinition> dataTypeDefinitionHashMap = new HashMap<>();
        DataTypeDefinition dataTypeDefinition = new DataTypeDefinition();
        dataTypeDefinition.setName("string");
        dataTypeDefinitionHashMap.put("string", dataTypeDefinition);

        //when(userValidations.validateUserExists(user.getUserId(), false)).thenReturn(user);
        when(toscaOperationFacade.getToscaElement(containerComponentID, JsonParseFlagEnum.ParseAll))
            .thenReturn(Either.left(component));
        when(graphLockOperation.lockComponent(containerComponentID, NodeTypeEnum.ResourceInstance))
            .thenReturn(StorageOperationStatus.OK);
        when(dataTypeCache.getAll()).thenReturn(Either.left(types));
        when(propertyOperation.validateAndUpdatePropertyValue(property.getType(), "newVal", true, null, types))
            .thenReturn(Either.left("newVal"));
        when(propertyOperation.validateAndUpdateRules("string", property.getRules(),
            null, types, true)).thenReturn(ImmutablePair.of("string", null));
        when(toscaOperationFacade.updateComponentInstanceProperty(component, ci.getUniqueId(),
            origProperty)).thenReturn(StorageOperationStatus.OK);
        origProperties.get(0).setValue("newVal");
        when(toscaOperationFacade.updateComponentInstanceMetadataOfTopologyTemplate(component))
            .thenReturn(Either.left(component));
        when(janusGraphDao.commit()).thenReturn(JanusGraphOperationStatus.OK);
        when(graphLockOperation.unlockComponent(containerComponentID, NodeTypeEnum.ResourceInstance))
            .thenReturn(StorageOperationStatus.OK);

        Either<List<ComponentInstanceProperty>, ResponseFormat> responseFormatEither = componentInstanceBusinessLogic
            .createOrUpdatePropertiesValues(
                ComponentTypeEnum.RESOURCE_INSTANCE, containerComponentID, resourceInstanceId, properties, "userId");
        assertThat(responseFormatEither.left().value()).isEqualTo(properties);
    }

    @Test
    public void testCreateOrUpdatePropertiesValuesPropertyNotExists() {
        String containerComponentID = "containerId";
        String resourceInstanceId = "resourceId";
        String componentInstanceID = "componentInstance";
        List<ComponentInstanceProperty> properties = new ArrayList<>();
        ComponentInstanceProperty property = new ComponentInstanceProperty();
        property.setName("property");
        property.setValue("newVal");
        property.setType("string");
        properties.add(property);

        List<ComponentInstanceProperty> origProperties = new ArrayList<>();

        Map<String, DataTypeDefinition> types = new HashMap<>();
        DataTypeDefinition dataTypeDef = new DataTypeDefinition();
        types.put("string", dataTypeDef);

        Component component = new Service();
        component.setLastUpdaterUserId("userId");
        component.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
        Map<String, List<ComponentInstanceProperty>> componentInstanceProps = new HashMap<>();
        componentInstanceProps.put("resourceId", origProperties);
        component.setComponentInstancesProperties(componentInstanceProps);
        ComponentInstance ci = createComponentInstance("ci1");
        ci.setUniqueId("resourceId");
        component.setComponentInstances(Arrays.asList(ci, createComponentInstance("ci2"),
            createComponentInstance(componentInstanceID)));
        HashMap<String, DataTypeDefinition> dataTypeDefinitionHashMap = new HashMap<>();
        DataTypeDefinition dataTypeDefinition = new DataTypeDefinition();
        dataTypeDefinition.setName("string");
        dataTypeDefinitionHashMap.put("string", dataTypeDefinition);

        //when(userValidations.validateUserExists(user.getUserId(), false)).thenReturn(user);
        when(toscaOperationFacade.getToscaElement(containerComponentID, JsonParseFlagEnum.ParseAll))
            .thenReturn(Either.left(component));
        when(graphLockOperation.lockComponent(containerComponentID, NodeTypeEnum.ResourceInstance))
            .thenReturn(StorageOperationStatus.OK);
        //when(dataTypeCache.getAll()).thenReturn(Either.left(types));
        //when (janusGraphDao.commit()).thenReturn(JanusGraphOperationStatus.OK);
        when(graphLockOperation.unlockComponent(containerComponentID, NodeTypeEnum.ResourceInstance))
            .thenReturn(StorageOperationStatus.OK);

        try {
            componentInstanceBusinessLogic.createOrUpdatePropertiesValues(
                ComponentTypeEnum.RESOURCE_INSTANCE, containerComponentID, resourceInstanceId, properties, "userId");
        } catch (ComponentException e) {
            assertThat(e.getActionStatus()).isEqualTo(ActionStatus.PROPERTY_NOT_FOUND);
        }

    }

    @Test
    public void testCreateOrUpdatePropertiesValuesValidationFailure() {
        String containerComponentID = "containerId";
        String resourceInstanceId = "resourceId";
        String componentInstanceID = "componentInstance";
        List<ComponentInstanceProperty> properties = new ArrayList<>();
        ComponentInstanceProperty property = new ComponentInstanceProperty();
        property.setName("property");
        property.setValue("newVal");
        property.setType("string");
        properties.add(property);

        List<ComponentInstanceProperty> origProperties = new ArrayList<>();
        ComponentInstanceProperty origProperty = new ComponentInstanceProperty();
        origProperty.setName("property");
        origProperty.setValue("value");
        origProperty.setType("string");
        origProperties.add(origProperty);

        Map<String, DataTypeDefinition> types = new HashMap<>();
        DataTypeDefinition dataTypeDef = new DataTypeDefinition();
        types.put("string", dataTypeDef);

        Component component = new Service();
        component.setLastUpdaterUserId("userId");
        component.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
        Map<String, List<ComponentInstanceProperty>> componentInstanceProps = new HashMap<>();
        componentInstanceProps.put("resourceId", origProperties);
        component.setComponentInstancesProperties(componentInstanceProps);
        ComponentInstance ci = createComponentInstance("ci1");
        ci.setUniqueId("resourceId");
        component.setComponentInstances(Arrays.asList(ci, createComponentInstance("ci2"),
            createComponentInstance(componentInstanceID)));
        HashMap<String, DataTypeDefinition> dataTypeDefinitionHashMap = new HashMap<>();
        DataTypeDefinition dataTypeDefinition = new DataTypeDefinition();
        dataTypeDefinition.setName("string");
        dataTypeDefinitionHashMap.put("string", dataTypeDefinition);

        //when(userValidations.validateUserExists(user.getUserId(), false)).thenReturn(user);
        when(toscaOperationFacade.getToscaElement(containerComponentID, JsonParseFlagEnum.ParseAll))
            .thenReturn(Either.left(component));
        when(graphLockOperation.lockComponent(containerComponentID, NodeTypeEnum.ResourceInstance))
            .thenReturn(StorageOperationStatus.OK);
        when(dataTypeCache.getAll()).thenReturn(Either.left(types));
        when(propertyOperation.validateAndUpdatePropertyValue(property.getType(), "newVal", true, null, types))
            .thenReturn(Either.right(false));
        when(componentsUtils.convertFromStorageResponse(StorageOperationStatus.BAD_REQUEST))
            .thenReturn(ActionStatus.INVALID_CONTENT);

        try {
            componentInstanceBusinessLogic.createOrUpdatePropertiesValues(
                ComponentTypeEnum.RESOURCE_INSTANCE, containerComponentID, resourceInstanceId, properties, "userId");
        } catch (ComponentException e) {
            assertThat(e.getActionStatus()).isEqualTo(ActionStatus.INVALID_CONTENT);
            return;
        }
        fail();
    }

    @Test
    public void testCreateOrUpdatePropertiesValuesMissingFieldFailure() {
        String containerComponentID = "containerId";
        String resourceInstanceId = "resourceId";
        String componentInstanceID = "componentInstance";
        List<ComponentInstanceProperty> properties = new ArrayList<>();
        ComponentInstanceProperty property = new ComponentInstanceProperty();
        property.setValue("newVal");
        property.setType("string");
        properties.add(property);

        List<ComponentInstanceProperty> origProperties = new ArrayList<>();
        ComponentInstanceProperty origProperty = new ComponentInstanceProperty();
        origProperty.setName("property");
        origProperty.setValue("value");
        origProperty.setType("string");
        origProperties.add(origProperty);

        Component component = new Service();
        component.setLastUpdaterUserId("userId");
        component.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
        Map<String, List<ComponentInstanceProperty>> componentInstanceProps = new HashMap<>();
        componentInstanceProps.put("resourceId", origProperties);
        component.setComponentInstancesProperties(componentInstanceProps);
        ComponentInstance ci = createComponentInstance("ci1");
        ci.setUniqueId("resourceId");
        component.setComponentInstances(Arrays.asList(ci, createComponentInstance("ci2"),
            createComponentInstance(componentInstanceID)));

        HashMap<String, DataTypeDefinition> dataTypeDefinitionHashMap = new HashMap<>();
        DataTypeDefinition dataTypeDefinition = new DataTypeDefinition();
        dataTypeDefinition.setName("string");
        dataTypeDefinitionHashMap.put("string", dataTypeDefinition);

        //when(userValidations.validateUserExists(user.getUserId(), false)).thenReturn(user);
        when(toscaOperationFacade.getToscaElement(containerComponentID, JsonParseFlagEnum.ParseAll))
            .thenReturn(Either.left(component));
        when(graphLockOperation.lockComponent(containerComponentID, NodeTypeEnum.ResourceInstance))
            .thenReturn(StorageOperationStatus.OK);

        try {
            componentInstanceBusinessLogic.createOrUpdatePropertiesValues(
                ComponentTypeEnum.RESOURCE_INSTANCE, containerComponentID, resourceInstanceId, properties, "userId");
        } catch (ComponentException e) {
            assertThat(e.getActionStatus()).isEqualTo(ActionStatus.MISSING_PROPERTY_NAME);
        }
    }

    @Test
    public void testDeleteForwardingPathsWhenComponentinstanceDeleted() {

        ComponentTypeEnum containerComponentType = ComponentTypeEnum.findByParamName("services");
        String containerComponentID = "Service-comp";
        String componentInstanceID = "NodeA1";
        Service component = new Service();
        component
            .setComponentInstances(Arrays.asList(createComponentInstance("NodeA2"), createComponentInstance("NodeB2"),
                createComponentInstance(componentInstanceID)));

        component.addForwardingPath(createPath("path1", componentInstanceID, "NodeB1", "1"));
        component.addForwardingPath(createPath("Path2", "NodeA2", "NodeB2", "2"));
        when(toscaOperationFacade.getToscaElement(eq(containerComponentID), any(ComponentParametersView.class)))
            .thenReturn(Either.left(component));
        when(toscaOperationFacade.getToscaElement(eq(containerComponentID))).thenReturn(Either.left(component));
        when(forwardingPathOperation.deleteForwardingPath(any(Service.class), anySet()))
            .thenReturn(Either.left(new HashSet<>()));
        final ComponentInstance ci = new ComponentInstance();
        ci.setName(componentInstanceID);
        ComponentInstance responseFormatEither = componentInstanceBusinessLogic
            .deleteForwardingPathsRelatedTobeDeletedComponentInstance(
                containerComponentID, containerComponentType, ci);
        assertThat(!responseFormatEither.isEmpty()).isEqualTo(true);

    }

    @Test
    public void testAddComponentInstanceDeploymentArtifacts() {

        Component containerComponent = new Service();
        ComponentInstance componentInstance = new ComponentInstance();
        componentInstance.setUniqueId(COMPONENT_INSTANCE_ID);
        Component originComponent = fillOriginComponent(new Resource());

        Map<String, ArtifactDefinition> artifacts = new HashMap<>();
        ArtifactDefinition deploymentArtifact1 = getArtifact("deploymentArtifact1", ArtifactTypeEnum.HEAT.getType());
        artifacts.put(deploymentArtifact1.getArtifactLabel(), deploymentArtifact1);
        ArtifactDefinition deploymentArtifact2 = getArtifact("deploymentArtifact2",
            ArtifactTypeEnum.HEAT_ENV.getType());
        artifacts.put(deploymentArtifact2.getArtifactLabel(), deploymentArtifact2);
        ArtifactDefinition deploymentArtifact3 = getArtifact("deploymentArtifact3",
            ArtifactTypeEnum.HEAT_VOL.getType());
        artifacts.put(deploymentArtifact3.getArtifactLabel(), deploymentArtifact3);
        ArtifactDefinition heatEnvPlaceHolder = getArtifact("deploymentArtifact4", ArtifactTypeEnum.HEAT_ENV.getType());
        ArtifactDefinition heatEnvPlaceHolder2 = getArtifact("deploymentArtifact5",
            ArtifactTypeEnum.HEAT_ENV.getType());

        Either<Map<String, ArtifactDefinition>, StorageOperationStatus> getResourceDeploymentArtifacts = Either
            .left(artifacts);

        Map<String, ArtifactDefinition> finalDeploymentArtifacts = new HashMap<>();
        finalDeploymentArtifacts.put(deploymentArtifact1.getArtifactLabel(), deploymentArtifact1);
        finalDeploymentArtifacts.put(deploymentArtifact3.getArtifactLabel(), deploymentArtifact3);
        finalDeploymentArtifacts.put(heatEnvPlaceHolder.getArtifactLabel(), heatEnvPlaceHolder);
        finalDeploymentArtifacts.put(heatEnvPlaceHolder2.getArtifactLabel(), heatEnvPlaceHolder2);

        when(artifactsBusinessLogic.getArtifacts(componentInstance.getComponentUid(), NodeTypeEnum.Resource,
            ArtifactGroupTypeEnum.DEPLOYMENT, null)).thenReturn(getResourceDeploymentArtifacts);
        when(artifactsBusinessLogic.createHeatEnvPlaceHolder(new ArrayList<>(),
            deploymentArtifact1, ArtifactsBusinessLogic.HEAT_ENV_NAME, componentInstance.getUniqueId(),
            NodeTypeEnum.ResourceInstance, componentInstance.getName(), user, containerComponent,
            null)).thenReturn(heatEnvPlaceHolder);
        when(artifactsBusinessLogic.createHeatEnvPlaceHolder(new ArrayList<>(),
            deploymentArtifact3, ArtifactsBusinessLogic.HEAT_ENV_NAME, componentInstance.getUniqueId(),
            NodeTypeEnum.ResourceInstance, componentInstance.getName(), user, containerComponent,
            null)).thenReturn(heatEnvPlaceHolder2);

        when(toscaOperationFacade.addDeploymentArtifactsToInstance(containerComponent.getUniqueId(), componentInstance,
            finalDeploymentArtifacts)).thenReturn(StorageOperationStatus.OK);
        when(toscaOperationFacade
            .addGroupInstancesToComponentInstance(containerComponent, componentInstance, null, new HashMap<>()))
            .thenReturn(StorageOperationStatus.OK);
        when(toscaOperationFacade
            .addInformationalArtifactsToInstance(containerComponent.getUniqueId(), componentInstance, null))
            .thenReturn(StorageOperationStatus.OK);

        ActionStatus status = componentInstanceBusinessLogic.addComponentInstanceArtifacts(containerComponent,
            componentInstance, originComponent, user, null);

        assertThat(status).isEqualTo(ActionStatus.OK);

    }

    private Component fillOriginComponent(Resource originComponent) {
        originComponent.setUniqueId("resourceId");
        originComponent.setUniqueId(ORIGIN_COMPONENT_ID);
        originComponent.setComponentInstances(Lists.newArrayList(toInstance, fromInstance));
        originComponent.setComponentType(ComponentTypeEnum.RESOURCE);
        originComponent.setState(LifecycleStateEnum.CERTIFIED);
        return originComponent;
    }

    private ArtifactDefinition getArtifact(String artifactLabel, String artifactType) {
        ArtifactDefinition artifactDefinition = new ArtifactDefinition();
        artifactDefinition.setArtifactLabel(artifactLabel);
        artifactDefinition.setArtifactGroupType(ArtifactGroupTypeEnum.DEPLOYMENT);
        artifactDefinition.setEsId("esId" + artifactLabel);
        artifactDefinition.setArtifactType(artifactType);
        return artifactDefinition;
    }

    private ComponentInstance createComponentInstance(String path1) {
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
        when(userValidations.validateUserExists(eq(USER_ID)))
            .thenThrow(new ByActionStatusComponentException(ActionStatus.USER_NOT_FOUND));
        try {
            componentInstanceBusinessLogic
                .getRelationById(COMPONENT_ID, RELATION_ID, USER_ID, component.getComponentType());
        } catch (ByActionStatusComponentException e) {
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
        when(userValidations.validateUserExists(eq(USER_ID))).thenReturn(user);
        when(componentsUtils
            .convertFromStorageResponse(eq(StorageOperationStatus.GENERAL_ERROR), any(ComponentTypeEnum.class)))
            .thenReturn(ActionStatus.GENERAL_ERROR);
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
        toInstance.setName(TO_INSTANCE_NAME);

        fromInstance = new ComponentInstance();
        fromInstance.setUniqueId(FROM_INSTANCE_ID);

        capability = new CapabilityDataDefinition();
        capability.setOwnerId(CAPABILITY_OWNER_ID);
        capability.setUniqueId(CAPABILITY_UID);
        capability.setName(CAPABILITY_NAME);

        Map<String, List<CapabilityDefinition>> capabilities = new HashMap<>();
        final CapabilityDefinition capabilityDefinition = new CapabilityDefinition(capability);
        final ArrayList<ComponentInstanceProperty> properties = new ArrayList<>();
        properties.add(componentInstancePropInput);
        capabilityDefinition.setProperties(properties);
        capabilities.put(capability.getName(), Lists.newArrayList(capabilityDefinition));

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
        Assert.assertNotNull(result);
    }

    @Test
    public void testCreateServiceProxy() {
        ComponentInstanceBusinessLogic testSubject;
        Either<ComponentInstance, ResponseFormat> result;

        // default test
        testSubject = createTestSubject();
        result = testSubject.createServiceProxy();
        Assert.assertNotNull(result);
    }

    @Test
    public void testDeleteServiceProxy() {
        ComponentInstanceBusinessLogic testSubject;

        Either<ComponentInstance, ResponseFormat> result;

        // default test
        testSubject = createTestSubject();
        result = testSubject.deleteServiceProxy();
        Assert.assertNotNull(result);
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
        Assert.assertNotNull(result);
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
        Assert.assertNotNull(result);
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
        Assert.assertNotNull(result);
    }

    @Test
    public void testValidateParent() {
        ComponentInstanceBusinessLogic testSubject;
        createResource();
        String nodeTemplateId = "";
        boolean result;

        // default test
        testSubject = createTestSubject();
        result = Deencapsulation.invoke(testSubject, "validateParent", new Object[]{resource, nodeTemplateId});
        Assert.assertNotNull(result);
    }

    @Test
    public void testGetComponentType() {
        ComponentInstanceBusinessLogic testSubject;
        ComponentTypeEnum result;

        // default test
        testSubject = createTestSubject();
        result = Deencapsulation.invoke(testSubject, "getComponentType", new Object[]{ComponentTypeEnum.class});
        Assert.assertNotNull(result);
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
            new Object[]{oldPrefix, newNormailzedPrefix, qualifiedGroupInstanceName});
        Assert.assertNotNull(result);
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
            .invoke(testSubject, "updateComponentInstanceMetadata", new Object[]{toInstance, toInstance});
        Assert.assertNotNull(result);
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
            new Object[]{relationId, requirementCapabilityRelations});
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
        Assert.assertNotNull(result);

        componentTypeEnum = null;
        result = testSubject
            .createOrUpdatePropertiesValues(componentTypeEnum, componentId, resourceInstanceId, properties,
                userId);
        Assert.assertNotNull(result);

        result = testSubject
            .createOrUpdatePropertiesValues(componentTypeEnum, componentId, resourceInstanceId, properties,
                userId);
        Assert.assertNotNull(result);
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
            new Object[]{property, newValue, resource, toInstance, capabilityType, capabilityName});
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
        Assert.assertNotNull(result);
        componentTypeEnum = null;
        result = testSubject
            .createOrUpdateInstanceInputValues(componentTypeEnum, componentId, resourceInstanceId, inputs,
                userId);
        Assert.assertNotNull(result);

        result = testSubject
            .createOrUpdateInstanceInputValues(componentTypeEnum, componentId, resourceInstanceId, inputs,
                userId);
        Assert.assertNotNull(result);
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
        Assert.assertNotNull(result);
        componentTypeEnum = null;
        result = testSubject
            .createOrUpdateGroupInstancePropertyValue(componentTypeEnum, componentId, resourceInstanceId,
                groupInstanceId, property, userId);
        Assert.assertNotNull(result);

        result = testSubject
            .createOrUpdateGroupInstancePropertyValue(componentTypeEnum, componentId, resourceInstanceId,
                groupInstanceId, property, userId);
        Assert.assertNotNull(result);
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
        Assert.assertNotNull(result);
        componentTypeEnum = null;
        result = testSubject.deletePropertyValue(componentTypeEnum, serviceId, resourceInstanceId, propertyValueId,
            userId);
        Assert.assertNotNull(result);

        result = testSubject.deletePropertyValue(componentTypeEnum, serviceId, resourceInstanceId, propertyValueId,
            userId);
        Assert.assertNotNull(result);
    }

    @Test
    public void testGetComponentParametersViewForForwardingPath() throws Exception {
        ComponentInstanceBusinessLogic testSubject;
        ComponentParametersView result;

        // default test
        testSubject = createTestSubject();
        result = Deencapsulation.invoke(testSubject, "getComponentParametersViewForForwardingPath");
        Assert.assertNotNull(result);
    }

    @Test
    public void testGetResourceInstanceById() throws Exception {
        ComponentInstanceBusinessLogic testSubject;
        createResource();
        String instanceId = "";
        Either<ComponentInstance, StorageOperationStatus> result;

        // default test
        testSubject = createTestSubject();
        result = Deencapsulation.invoke(testSubject, "getResourceInstanceById", new Object[]{resource, instanceId});
        Assert.assertNotNull(result);
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
        Assert.assertNotNull(result);
        when(toscaOperationFacade.getToscaFullElement(containerComponentId)).thenReturn(Either.left(resource));
        result = testSubject.updateInstanceCapabilityProperties(componentTypeEnum, containerComponentId,
            componentInstanceUniqueId, capabilityType, capabilityName, properties, userId);
        Assert.assertNotNull(result);
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
        when(janusGraphDao.rollback()).thenReturn(JanusGraphOperationStatus.OK);
        when(graphLockOperation.unlockComponent(Mockito.anyString(), eq(NodeTypeEnum.Service)))
            .thenReturn(StorageOperationStatus.OK);
        when(graphLockOperation.lockComponent(Mockito.anyString(), eq(NodeTypeEnum.Service)))
            .thenReturn(StorageOperationStatus.OK);

        result = componentInstanceBusinessLogic
            .copyComponentInstance(inputComponentInstance, containerComponentId, componentInstanceId,
                USER_ID);
        Assert.assertNotNull(result);

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
        when(janusGraphDao.rollback()).thenReturn(JanusGraphOperationStatus.OK);
        when(graphLockOperation.unlockComponent(Mockito.anyString(), eq(NodeTypeEnum.Service)))
            .thenReturn(StorageOperationStatus.OK);
        when(graphLockOperation.lockComponent(Mockito.anyString(), eq(NodeTypeEnum.Service)))
            .thenReturn(StorageOperationStatus.OK);
        Either<Component, StorageOperationStatus> getComponentRes = Either.left(resource);
        result = componentInstanceBusinessLogic
            .copyComponentInstance(inputComponentInstance, containerComponentId, componentInstanceId, USER_ID);
        Assert.assertNotNull(result);
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
        ImmutablePair<Component, String> pair = new ImmutablePair<>(resource, TO_INSTANCE_ID);
        Either<ImmutablePair<Component, String>, StorageOperationStatus> result2 = Either.left(pair);
        Either<Map<String, ArtifactDefinition>, StorageOperationStatus> getResourceDeploymentArtifacts = Either
            .left(new HashMap<String, ArtifactDefinition>());
        StorageOperationStatus artStatus = StorageOperationStatus.OK;

        result = componentInstanceBusinessLogic
            .copyComponentInstance(inputComponentInstance, containerComponentId, componentInstanceId,
                USER_ID);
        Assert.assertNotNull(result);

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
        Assert.assertNotNull(result);

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
        Assert.assertNotNull(result);
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
        Either<Component, StorageOperationStatus> cont = Either.left(service);
        when(componentsUtils.convertFromStorageResponse(eq(StorageOperationStatus.NOT_FOUND), eq(null)))
            .thenReturn(ActionStatus.GENERAL_ERROR);
        when(toscaOperationFacade.getToscaElement(any(String.class), any(ComponentParametersView.class)))
            .thenReturn(cont);

        try {
            result = componentInstanceBusinessLogic
                .batchDeleteComponentInstance(containerComponentParam, containerComponentId, componentInstanceIdList,
                    userId);
            Assert.assertNotNull(result);
            assertEquals(deleteErrorMap, result);
        } catch (ComponentException e) {
            assertEquals(e.getActionStatus().toString(), StorageOperationStatus.GENERAL_ERROR.toString());
        }
    }

    @Test
    public void testBatchDeleteComponentInstanceFailureCompIds() {
        Map<String, List<String>> result = new HashMap<>();
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

        try {
            result = componentInstanceBusinessLogic
                .batchDeleteComponentInstance(containerComponentParam, containerComponentId, componentInstanceIdList,
                    userId);
            Assert.assertNotNull(result);
            assertEquals(deleteErrorMap, result);
        } catch (ComponentException e) {
            assertEquals(e.getActionStatus().toString(), StorageOperationStatus.GENERAL_ERROR.toString());
        }
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
        when(janusGraphDao.commit()).thenReturn(JanusGraphOperationStatus.OK);

        result = componentInstanceBusinessLogic
            .batchDeleteComponentInstance(containerComponentParam, containerComponentId,
                componentInstanceIdList, userId);
        Assert.assertNotNull(result);

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
        when(componentsUtils.convertFromStorageResponseForResourceInstance(eq(StorageOperationStatus.OK), eq(true)))
            .thenReturn(ActionStatus.GENERAL_ERROR);
        when(toscaOperationFacade.dissociateResourceInstances(componentId, ref)).thenReturn(resultEither);

        try {
            result = componentInstanceBusinessLogic
                .batchDissociateRIFromRI(componentId, userId, requirementDefList, componentTypeEnum);
            Assert.assertNotNull(result);
            assertEquals(new ArrayList<>(), result);
        } catch (ComponentException e) {
            assertEquals(e.getActionStatus().toString(), StorageOperationStatus.GENERAL_ERROR.toString());
        }

        service.setLastUpdaterUserId(oldLastUpdatedUserId);
        service.setLifecycleState(oldLifeCycleState);

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
        Assert.assertNotNull(result);

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
