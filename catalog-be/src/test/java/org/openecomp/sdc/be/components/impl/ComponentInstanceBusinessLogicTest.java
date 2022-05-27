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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum.RESOURCE_PARAM_NAME;

import fj.data.Either;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import mockit.Deencapsulation;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.be.components.impl.exceptions.ByActionStatusComponentException;
import org.openecomp.sdc.be.components.impl.exceptions.ByResponseFormatComponentException;
import org.openecomp.sdc.be.components.impl.exceptions.ComponentException;
import org.openecomp.sdc.be.components.impl.exceptions.ToscaGetFunctionExceptionSupplier;
import org.openecomp.sdc.be.components.validation.UserValidations;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphDao;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.dao.jsongraph.types.JsonParseFlagEnum;
import org.openecomp.sdc.be.datatypes.elements.CapabilityDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ForwardingPathDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ForwardingPathElementDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.GetInputValueDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.GetPolicyValueDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ListDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.RequirementDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.SchemaDefinition;
import org.openecomp.sdc.be.datatypes.elements.ToscaGetFunctionDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.OriginTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.PropertySource;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.datatypes.tosca.ToscaGetFunctionType;
import org.openecomp.sdc.be.exception.BusinessException;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.CapabilityDefinition;
import org.openecomp.sdc.be.model.CapabilityRequirementRelationship;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.ComponentInstanceAttribute;
import org.openecomp.sdc.be.model.ComponentInstanceInput;
import org.openecomp.sdc.be.model.ComponentInstancePropInput;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.ComponentParametersView;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.InputDefinition;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.PolicyDefinition;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.RelationshipImpl;
import org.openecomp.sdc.be.model.RelationshipInfo;
import org.openecomp.sdc.be.model.RequirementCapabilityRelDef;
import org.openecomp.sdc.be.model.RequirementDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.cache.ApplicationDataTypeCache;
import org.openecomp.sdc.be.model.jsonjanusgraph.config.ContainerInstanceTypesData;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ForwardingPathOperation;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.exception.OperationException;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.GraphLockOperation;
import org.openecomp.sdc.be.model.operations.impl.PropertyOperation;
import org.openecomp.sdc.common.api.ArtifactGroupTypeEnum;
import org.openecomp.sdc.common.api.ArtifactTypeEnum;
import org.openecomp.sdc.common.api.ConfigurationSource;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.openecomp.sdc.common.impl.FSConfigurationSource;
import org.openecomp.sdc.common.util.ValidationUtils;
import org.openecomp.sdc.exception.ResponseFormat;

/**
 * The test suite designed for test functionality of ComponentInstanceBusinessLogic class
 */
class ComponentInstanceBusinessLogicTest {

    private final static String USER_ID = "jh0003";
    private final static String COMPONENT_ID = "componentId";
    private final static String ORIGIN_COMPONENT_ID = "originComponentId";
    private final static String ORIGIN_COMPONENT_VERSION = "1.0";
    private final static String TO_INSTANCE_ID = "toInstanceId";
    private final static String TO_INSTANCE_NAME = "toInstanceName";
    private final static String COMPONENT_INSTANCE_ID = "componentInstanceId";
    private final static String COMPONENT_INSTANCE_NAME = "componentInstanceName";
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
    private final static String INPUT_ID = "inputId";
    private final static String ICON_NAME = "icon";

    private ComponentInstanceBusinessLogic componentInstanceBusinessLogic;
    @Mock
    private ComponentInstancePropInput componentInstancePropInput;
    @Mock
    private ArtifactsBusinessLogic artifactsBusinessLogic;
    @Mock
    private ComponentsUtils componentsUtils;
    @Mock
    private ToscaOperationFacade toscaOperationFacade;
    @Mock
    private ForwardingPathOperation forwardingPathOperation;
    @Mock
    private User user;
    @Mock
    private UserValidations userValidations;
    @Mock
    private GraphLockOperation graphLockOperation;
    @Mock
    private JanusGraphDao janusGraphDao;
    @Mock
    private ApplicationDataTypeCache applicationDataTypeCache;
    @Mock
    private PropertyOperation propertyOperation;
    @Mock
    private ContainerInstanceTypesData containerInstanceTypeData;
    @Mock
    private CompositionBusinessLogic compositionBusinessLogic;

    private Component service;
    private Component resource;
    private ComponentInstance toInstance;
    private ComponentInstance fromInstance;
    private RequirementCapabilityRelDef relation;
    private List<ComponentInstanceProperty> ciPropertyList;
    private List<ComponentInstanceInput> ciInputList;

    @BeforeAll
    static void beforeAll() {
        initConfig();
    }

    private static void initConfig() {
        final ConfigurationSource configurationSource = new FSConfigurationSource(
            ExternalConfiguration.getChangeListener(),
            "src/test/resources/config/catalog-be"
        );
        new ConfigurationManager(configurationSource);
    }

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
        componentInstanceBusinessLogic = new ComponentInstanceBusinessLogic(null, null, null, null, null, null, null, artifactsBusinessLogic, null,
            null, forwardingPathOperation, null, null);
        componentInstanceBusinessLogic.setComponentsUtils(componentsUtils);
        componentInstanceBusinessLogic.setToscaOperationFacade(toscaOperationFacade);
        componentInstanceBusinessLogic.setUserValidations(userValidations);
        componentInstanceBusinessLogic.setGraphLockOperation(graphLockOperation);
        componentInstanceBusinessLogic.setJanusGraphDao(janusGraphDao);
        componentInstanceBusinessLogic.setApplicationDataTypeCache(applicationDataTypeCache);
        componentInstanceBusinessLogic.setPropertyOperation(propertyOperation);
        componentInstanceBusinessLogic.setContainerInstanceTypesData(containerInstanceTypeData);
        componentInstanceBusinessLogic.setCompositionBusinessLogic(compositionBusinessLogic);

        stubMethods();
        createComponents();
    }

    @Test
    void testGetRelationByIdSuccess() {
        getServiceRelationByIdSuccess(service);
        getServiceRelationByIdSuccess(resource);
    }

    @Test
    void testGetRelationByIdUserValidationFailure() {
        getServiceRelationByIdUserValidationFailure(service);
        getServiceRelationByIdUserValidationFailure(resource);
    }

    @Test
    void testGetRelationByIdComponentNotFoundFailure() {
        getRelationByIdComponentNotFoundFailure(service);
        getRelationByIdComponentNotFoundFailure(resource);
    }

    @Test
    void testForwardingPathOnVersionChange() {
        getforwardingPathOnVersionChange();
    }

    @Test
    void testIsCloudSpecificArtifact() {
        assertThat(componentInstanceBusinessLogic.isCloudSpecificArtifact(ARTIFACT_1)).isTrue();
        assertThat(componentInstanceBusinessLogic.isCloudSpecificArtifact(ARTIFACT_2)).isTrue();
        assertThat(componentInstanceBusinessLogic.isCloudSpecificArtifact(ARTIFACT_3)).isTrue();
        assertThat(componentInstanceBusinessLogic.isCloudSpecificArtifact(ARTIFACT_4)).isFalse();
        assertThat(componentInstanceBusinessLogic.isCloudSpecificArtifact(ARTIFACT_5)).isFalse();
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
        when(toscaOperationFacade.getToscaFullElement(new_Comp_UID)).thenReturn(Either.left(component2));

        Either<Set<String>, ResponseFormat> resultOp = componentInstanceBusinessLogic
            .forwardingPathOnVersionChange(containerComponentParam,
                containerComponentID, componentInstanceID,
                newComponentInstance);
        assertEquals(1, resultOp.left().value().size());
        assertEquals("FP-ID-1", resultOp.left().value().iterator().next());

    }

    @Test
    void testCreateOrUpdatePropertiesValues2() {
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

        when(toscaOperationFacade.getToscaElement(containerComponentID, JsonParseFlagEnum.ParseAll))
            .thenReturn(Either.left(component));
        when(graphLockOperation.lockComponent(containerComponentID, NodeTypeEnum.ResourceInstance))
            .thenReturn(StorageOperationStatus.OK);
        when(componentsUtils.getAllDataTypes(applicationDataTypeCache, component.getModel())).thenReturn(types);
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
    void testToscaGetFunctionValidation() {
        final String userId = "userId";
        final String containerComponentId = "containerComponentId";
        final String containerComponentName = "containerComponentName";
        final String resourceInstanceId = "resourceInstanceId";
        final String inputName = "myInputToGet";
        final String inputId = String.format("%s.%s", containerComponentId, inputName);
        final String schemaType = "string";
        //creating instance list of string property with get_input value
        final ComponentInstanceProperty propertyGetInput = new ComponentInstanceProperty();
        propertyGetInput.setName("getInputProperty");
        propertyGetInput.setPropertyId(String.format("%s.%s", containerComponentId, "getInputProperty"));
        propertyGetInput.setValue(String.format("get_input: [\"%s\"]", inputName));
        propertyGetInput.setType("list");
        final SchemaDefinition listStringPropertySchema = createSchema(schemaType);
        propertyGetInput.setSchema(listStringPropertySchema);
        propertyGetInput.setToscaGetFunction(
            createGetToscaFunction(inputName, inputId, List.of(propertyGetInput.getName()), PropertySource.SELF, ToscaGetFunctionType.GET_INPUT,
                containerComponentId, containerComponentName)
        );
        //creating instance map of string property with get_input value to a second level property:
        // get_input: ["property1", "subProperty1", "subProperty2"]
        final String getPropertyPropertyName = "getPropertyProperty";
        final List<String> containerPropertyPath = List.of("property1", "subProperty1", "subProperty2");
        final String containerPropertyId = String.format("%s.%s", containerComponentId, containerPropertyPath.get(0));
        final String mapToscaType = "map";
        final ComponentInstanceProperty propertyGetProperty = createComponentInstanceProperty(
            String.format("%s.%s", containerComponentId, getPropertyPropertyName),
            getPropertyPropertyName,
            mapToscaType,
            "string",
            String.format("\"get_property\": [\"%s\", \"%s\"]", PropertySource.SELF, String.join("\", \"", containerPropertyPath)),
            createGetToscaFunction(containerPropertyPath.get(containerPropertyPath.size() - 1), containerPropertyId,
                containerPropertyPath, PropertySource.SELF, ToscaGetFunctionType.GET_PROPERTY, containerComponentId, containerComponentName)
        );

        final ComponentInstanceProperty getPropertyOnInstanceProperty = createComponentInstanceProperty(
            String.format("%s.%s", containerComponentId, "getPropertyOnInstanceProperty"),
            "getPropertyOnInstanceProperty",
            "string",
            null,
            String.format("\"get_property\": [\"%s\", \"%s\"]", PropertySource.INSTANCE, String.join("\", \"", containerPropertyPath)),
            createGetToscaFunction(containerPropertyPath.get(containerPropertyPath.size() - 1), containerPropertyId,
                containerPropertyPath, PropertySource.INSTANCE, ToscaGetFunctionType.GET_PROPERTY, containerComponentId, containerComponentName)
        );

        //creating component that has the instance properties
        final Component component = new Service();
        component.setUniqueId(containerComponentId);
        component.setName(containerComponentName);
        component.setLastUpdaterUserId(userId);
        component.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
        //adding instance properties to the component
        final List<ComponentInstanceProperty> resourceInstanceProperties = List.of(propertyGetInput, propertyGetProperty);
        final Map<String, List<ComponentInstanceProperty>> componentInstanceProps = new HashMap<>();
        componentInstanceProps.put(resourceInstanceId, resourceInstanceProperties);
        component.setComponentInstancesProperties(componentInstanceProps);

        //creating component input that will be gotten by the get_input instance property
        final var inputDefinition = new InputDefinition();
        inputDefinition.setName(inputName);
        inputDefinition.setUniqueId(inputId);
        inputDefinition.setType(propertyGetInput.getType());
        inputDefinition.setSchema(listStringPropertySchema);
        component.setInputs(List.of(inputDefinition));

        //creating component property that contains the sub property that will be gotten by the get_property instance property
        final var propertyDefinition = new PropertyDefinition();
        propertyDefinition.setName(containerPropertyPath.get(0));
        propertyDefinition.setUniqueId(containerPropertyId);
        final String property1Type = "property1.datatype";
        propertyDefinition.setType(property1Type);
        component.setProperties(List.of(propertyDefinition));
        //creating resource instance to be added to the component
        final ComponentInstance resourceInstance = createComponentInstance("resourceInstance");
        resourceInstance.setUniqueId(resourceInstanceId);
        component.setComponentInstances(List.of(resourceInstance));

        mockComponentForToscaGetFunctionValidation(component);

        //creating data types for "map", and sub properties
        final Map<String, DataTypeDefinition> allDataTypesMap = new HashMap<>();
        allDataTypesMap.put(mapToscaType, new DataTypeDefinition());

        final String subProperty1Type = "subProperty1.datatype";
        allDataTypesMap.put(property1Type, createDataType(property1Type, Map.of(containerPropertyPath.get(1), subProperty1Type)));

        final var subProperty2Property = new PropertyDefinition();
        subProperty2Property.setName(containerPropertyPath.get(2));
        subProperty2Property.setType(propertyGetProperty.getType());
        subProperty2Property.setSchema(propertyGetProperty.getSchema());
        allDataTypesMap.put(subProperty1Type, createDataType(subProperty1Type, List.of(subProperty2Property)));

        when(applicationDataTypeCache.getAll(component.getModel())).thenReturn(Either.left(allDataTypesMap));
        //when
        final Either<List<ComponentInstanceProperty>, ResponseFormat> actualResponseFormat = componentInstanceBusinessLogic
            .createOrUpdatePropertiesValues(
                ComponentTypeEnum.RESOURCE_INSTANCE, containerComponentId, resourceInstanceId, resourceInstanceProperties, userId);
        //then
        assertTrue(actualResponseFormat.isLeft());
        assertThat(actualResponseFormat.left().value()).isEqualTo(resourceInstanceProperties);
    }

    @Test
    void testToscaGetPropertyOnInstanceValidation() {
        final String userId = "userId";
        final String containerComponentId = "containerComponentId";
        final String containerComponentName = "containerComponentName";
        final String instanceUniqueId = String.format("%s.%s", containerComponentId, "instanceId");

        final List<String> parentPropertyPath = List.of("property1");
        final String containerPropertyId = String.format("%s.%s", containerComponentId, parentPropertyPath.get(0));
        final ComponentInstanceProperty getPropertyOnInstanceProperty = createComponentInstanceProperty(
            String.format("%s.%s", containerComponentId, "getPropertyOnInstanceProperty"),
            "getPropertyOnInstanceProperty",
            "string",
            null,
            String.format("\"get_property\": [\"%s\", \"%s\"]", PropertySource.INSTANCE, parentPropertyPath.get(0)),
            createGetToscaFunction(parentPropertyPath.get(0), containerPropertyId, parentPropertyPath, PropertySource.INSTANCE,
                ToscaGetFunctionType.GET_PROPERTY, instanceUniqueId, containerComponentName)
        );

        //creating component that has the instance properties
        final Component component = new Service();
        component.setUniqueId(containerComponentId);
        component.setName(containerComponentName);
        component.setLastUpdaterUserId(userId);
        component.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
        //adding instance properties to the component
        final List<ComponentInstanceProperty> resourceInstanceProperties = List.of(getPropertyOnInstanceProperty);
        final Map<String, List<ComponentInstanceProperty>> componentInstanceProps = new HashMap<>();
        componentInstanceProps.put(instanceUniqueId, resourceInstanceProperties);
        component.setComponentInstancesProperties(componentInstanceProps);

        //creating resource property that will be get
        final var propertyDefinition = new PropertyDefinition();
        propertyDefinition.setName(parentPropertyPath.get(0));
        propertyDefinition.setUniqueId(containerPropertyId);
        final String property1Type = "string";
        propertyDefinition.setType(property1Type);
        //creating resource instance to be added to the component
        final ComponentInstance resourceInstance = createComponentInstance("resourceInstance");
        resourceInstance.setUniqueId(instanceUniqueId);
        resourceInstance.setProperties(List.of(propertyDefinition));
        component.setComponentInstances(List.of(resourceInstance));

        mockComponentForToscaGetFunctionValidation(component);

        //when
        final Either<List<ComponentInstanceProperty>, ResponseFormat> actualResponseFormat = componentInstanceBusinessLogic
            .createOrUpdatePropertiesValues(
                ComponentTypeEnum.RESOURCE_INSTANCE, containerComponentId, instanceUniqueId, resourceInstanceProperties, userId);
        //then
        assertTrue(actualResponseFormat.isLeft());
        assertThat(actualResponseFormat.left().value()).isEqualTo(resourceInstanceProperties);
    }

    private DataTypeDefinition createDataType(final String name, final Map<String, String> propertyNameAndTypeMap) {
        final var dataTypeDefinition = new DataTypeDefinition();
        dataTypeDefinition.setName(name);
        if (MapUtils.isNotEmpty(propertyNameAndTypeMap)) {
            for (final Entry<String, String> propertyEntry : propertyNameAndTypeMap.entrySet()) {
                final var propertyDefinition = new PropertyDefinition();
                propertyDefinition.setName(propertyEntry.getKey());
                propertyDefinition.setType(propertyEntry.getValue());
                dataTypeDefinition.setProperties(List.of(propertyDefinition));
            }
        }
        return dataTypeDefinition;
    }

    private DataTypeDefinition createDataType(final String name, final List<PropertyDefinition> propertyList) {
        final var dataTypeDefinition = new DataTypeDefinition();
        dataTypeDefinition.setName(name);
        if (CollectionUtils.isNotEmpty(propertyList)) {
            dataTypeDefinition.setProperties(propertyList);
        }
        return dataTypeDefinition;
    }

    private ComponentInstanceProperty createComponentInstanceProperty(final String uniqueId, final String name, final String type,
                                                                      final String schemaType, final String value,
                                                                      final ToscaGetFunctionDataDefinition toscaGetFunction) {
        final var componentInstanceProperty = new ComponentInstanceProperty();
        componentInstanceProperty.setName(name);
        componentInstanceProperty.setUniqueId(uniqueId);
        componentInstanceProperty.setType(type);
        componentInstanceProperty.setValue(value);
        if (schemaType != null) {
            final SchemaDefinition schemaDefinition = createSchema(schemaType);
            componentInstanceProperty.setSchema(schemaDefinition);
        }
        if (toscaGetFunction != null) {
            componentInstanceProperty.setToscaGetFunction(toscaGetFunction);
        }

        return componentInstanceProperty;
    }

    @Test
    void testToscaGetFunctionValidation_schemaDivergeTest() {
        final String userId = "userId";
        final String containerComponentId = "containerComponentId";
        final String containerComponentName = "containerComponentName";
        final String resourceInstanceId = "resourceInstanceId";
        final String inputName = "myInputToGet";
        final String inputId = String.format("%s.%s", containerComponentId, inputName);
        final String propertyName = "getInputProperty";
        final String propertyId = String.format("%s.%s", containerComponentId, propertyName);
        final String propertyType = "list";
        final List<ComponentInstanceProperty> properties = new ArrayList<>();
        final ComponentInstanceProperty propertyGetInput = createComponentInstanceProperty(
            propertyId,
            "getInputProperty",
            propertyType,
            "string",
            String.format("get_input: [\"%s\"]", inputName),
            createGetToscaFunction(inputName, inputId, List.of(propertyName), PropertySource.SELF, ToscaGetFunctionType.GET_INPUT,
                containerComponentId, containerComponentName)
        );
        properties.add(propertyGetInput);

        final Component component = new Service();
        component.setUniqueId(containerComponentId);
        component.setName(containerComponentName);
        component.setLastUpdaterUserId(userId);
        component.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);

        var inputDefinition = new InputDefinition();
        inputDefinition.setName(inputName);
        inputDefinition.setUniqueId(inputId);
        inputDefinition.setType(propertyType);
        inputDefinition.setSchema(createSchema("integer"));
        component.setInputs(List.of(inputDefinition));

        final Map<String, List<ComponentInstanceProperty>> componentInstanceProps = new HashMap<>();
        componentInstanceProps.put(resourceInstanceId, properties);
        component.setComponentInstancesProperties(componentInstanceProps);

        final ComponentInstance resourceInstance = createComponentInstance("componentInstance1");
        resourceInstance.setUniqueId(resourceInstanceId);
        component.setComponentInstances(List.of(resourceInstance));

        mockComponentForToscaGetFunctionValidation(component);
        //when
        final Either<List<ComponentInstanceProperty>, ResponseFormat> responseFormatEither =
            componentInstanceBusinessLogic
                .createOrUpdatePropertiesValues(ComponentTypeEnum.RESOURCE_INSTANCE, containerComponentId, resourceInstanceId, properties, userId);
        //then
        assertTrue(responseFormatEither.isRight(), "Expecting an error");
        final ResponseFormat actualResponse = responseFormatEither.right().value();
        final ResponseFormat expectedResponse =
            ToscaGetFunctionExceptionSupplier
                .propertySchemaDiverge(propertyGetInput.getToscaGetFunction().getFunctionType(), inputDefinition.getSchemaType(),
                    propertyGetInput.getSchemaType())
                .get().getResponseFormat();
        assertEquals(expectedResponse.getFormattedMessage(), actualResponse.getFormattedMessage());
        assertEquals(expectedResponse.getStatus(), actualResponse.getStatus());
    }

    @Test
    void testToscaGetFunctionValidation_propertyTypeDivergeTest() {
        final String userId = "userId";
        final String containerComponentId = "containerComponentId";
        final String containerComponentName = "containerComponentName";
        final String resourceInstanceId = "resourceInstanceId";
        final String inputName = "myInputToGet";
        final String inputId = String.format("%s.%s", containerComponentId, inputName);
        final String propertyName = "getInputProperty";
        final String propertyId = String.format("%s.%s", containerComponentId, propertyName);
        final String propertyType = "string";
        final List<ComponentInstanceProperty> properties = new ArrayList<>();
        final ComponentInstanceProperty propertyGetInput = createComponentInstanceProperty(
            propertyId,
            "getInputProperty",
            propertyType,
            "string",
            String.format("get_input: [\"%s\"]", inputName),
            createGetToscaFunction(inputName, inputId, List.of(propertyName), PropertySource.SELF, ToscaGetFunctionType.GET_INPUT,
                containerComponentId, containerComponentName)
        );
        properties.add(propertyGetInput);

        final Component component = new Service();
        component.setName(containerComponentName);
        component.setUniqueId(containerComponentId);
        component.setLastUpdaterUserId(userId);
        component.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);

        var inputDefinition = new InputDefinition();
        inputDefinition.setName(inputName);
        inputDefinition.setUniqueId(inputId);
        inputDefinition.setType("integer");
        component.setInputs(List.of(inputDefinition));

        final Map<String, List<ComponentInstanceProperty>> componentInstanceProps = new HashMap<>();
        componentInstanceProps.put(resourceInstanceId, properties);
        component.setComponentInstancesProperties(componentInstanceProps);

        final ComponentInstance resourceInstance = createComponentInstance("componentInstance1");
        resourceInstance.setUniqueId(resourceInstanceId);
        component.setComponentInstances(List.of(resourceInstance));

        mockComponentForToscaGetFunctionValidation(component);
        //when
        final Either<List<ComponentInstanceProperty>, ResponseFormat> responseFormatEither =
            componentInstanceBusinessLogic
                .createOrUpdatePropertiesValues(ComponentTypeEnum.RESOURCE_INSTANCE, containerComponentId, resourceInstanceId, properties, userId);
        //then
        assertTrue(responseFormatEither.isRight(), "Expecting an error");
        final ResponseFormat actualResponse = responseFormatEither.right().value();
        final ResponseFormat expectedResponse =
            ToscaGetFunctionExceptionSupplier
                .propertyTypeDiverge(propertyGetInput.getToscaGetFunction().getFunctionType(), inputDefinition.getType(), propertyGetInput.getType())
                .get().getResponseFormat();
        assertEquals(expectedResponse.getFormattedMessage(), actualResponse.getFormattedMessage());
        assertEquals(expectedResponse.getStatus(), actualResponse.getStatus());
    }

    @Test
    void testToscaGetFunctionValidation_toscaFunctionNotSupportedTest() {
        final String userId = "userId";
        final String containerComponentId = "containerComponentId";
        final String containerComponentName = "containerComponentName";
        final String resourceInstanceId = "resourceInstanceId";
        final List<ComponentInstanceProperty> properties = new ArrayList<>();
        final ComponentInstanceProperty propertyGetInput = new ComponentInstanceProperty();
        propertyGetInput.setName("anyName");
        final var toscaGetFunction = new ToscaGetFunctionDataDefinition();
        toscaGetFunction.setFunctionType(ToscaGetFunctionType.GET_ATTRIBUTE);
        propertyGetInput.setToscaGetFunction(toscaGetFunction);
        properties.add(propertyGetInput);

        final Component component = new Service();
        component.setName(containerComponentName);
        component.setUniqueId(containerComponentId);
        component.setLastUpdaterUserId(userId);
        component.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);

        final Map<String, List<ComponentInstanceProperty>> componentInstanceProps = new HashMap<>();
        componentInstanceProps.put(resourceInstanceId, properties);
        component.setComponentInstancesProperties(componentInstanceProps);

        final ComponentInstance resourceInstance = createComponentInstance("componentInstance1");
        resourceInstance.setUniqueId(resourceInstanceId);
        component.setComponentInstances(List.of(resourceInstance));

        mockComponentForToscaGetFunctionValidation(component);
        //when
        final Either<List<ComponentInstanceProperty>, ResponseFormat> responseFormatEither =
            componentInstanceBusinessLogic
                .createOrUpdatePropertiesValues(ComponentTypeEnum.RESOURCE_INSTANCE, containerComponentId, resourceInstanceId, properties, userId);
        //then
        assertTrue(responseFormatEither.isRight(), "Expecting an error");
        final ResponseFormat actualResponse = responseFormatEither.right().value();
        final ResponseFormat expectedResponse =
            ToscaGetFunctionExceptionSupplier.functionNotSupported(toscaGetFunction.getFunctionType()).get().getResponseFormat();
        assertEquals(expectedResponse.getFormattedMessage(), actualResponse.getFormattedMessage());
        assertEquals(expectedResponse.getStatus(), actualResponse.getStatus());
    }

    @Test
    void testToscaGetFunctionValidation_propertyNotFoundTest() {
        final String userId = "userId";
        final String containerComponentId = "containerComponentId";
        final String containerComponentName = "containerComponentName";
        final String resourceInstanceId = "resourceInstanceId";
        final String inputName = "myInputToGet";
        final String inputId = String.format("%s.%s", containerComponentId, inputName);
        final String propertyName = "getInputProperty";
        final String propertyId = String.format("%s.%s", containerComponentId, propertyName);
        final String propertyType = "string";
        final List<ComponentInstanceProperty> properties = new ArrayList<>();
        final ComponentInstanceProperty propertyGetInput = createComponentInstanceProperty(
            propertyId,
            "getInputProperty",
            propertyType,
            "string",
            String.format("get_input: [\"%s\"]", inputName),
            createGetToscaFunction(inputName, inputId, List.of(propertyName), PropertySource.SELF, ToscaGetFunctionType.GET_INPUT,
                containerComponentId, containerComponentName)
        );
        properties.add(propertyGetInput);

        final Component component = new Service();
        component.setName(containerComponentName);
        component.setUniqueId(containerComponentId);
        component.setLastUpdaterUserId(userId);
        component.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);

        final Map<String, List<ComponentInstanceProperty>> componentInstanceProps = new HashMap<>();
        componentInstanceProps.put(resourceInstanceId, properties);
        component.setComponentInstancesProperties(componentInstanceProps);

        final ComponentInstance resourceInstance = createComponentInstance("componentInstance1");
        resourceInstance.setUniqueId(resourceInstanceId);
        component.setComponentInstances(List.of(resourceInstance));

        mockComponentForToscaGetFunctionValidation(component);
        //when
        final Either<List<ComponentInstanceProperty>, ResponseFormat> responseFormatEither =
            componentInstanceBusinessLogic
                .createOrUpdatePropertiesValues(ComponentTypeEnum.RESOURCE_INSTANCE, containerComponentId, resourceInstanceId, properties, userId);
        //then
        assertTrue(responseFormatEither.isRight(), "Expecting an error");
        final ResponseFormat actualResponse = responseFormatEither.right().value();
        final ResponseFormat expectedResponse =
            ToscaGetFunctionExceptionSupplier
                .propertyNotFoundOnTarget(inputName, PropertySource.SELF, ToscaGetFunctionType.GET_INPUT)
                .get().getResponseFormat();
        assertEquals(expectedResponse.getFormattedMessage(), actualResponse.getFormattedMessage());
        assertEquals(expectedResponse.getStatus(), actualResponse.getStatus());
    }

    @Test
    void testCreateOrUpdatePropertiesValuesPropertyNotExists() {
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

        when(toscaOperationFacade.getToscaElement(containerComponentID, JsonParseFlagEnum.ParseAll))
            .thenReturn(Either.left(component));
        when(graphLockOperation.lockComponent(containerComponentID, NodeTypeEnum.ResourceInstance))
            .thenReturn(StorageOperationStatus.OK);
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
    void testCreateOrUpdatePropertiesValuesValidationFailure() {
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

        when(toscaOperationFacade.getToscaElement(containerComponentID, JsonParseFlagEnum.ParseAll))
            .thenReturn(Either.left(component));
        when(graphLockOperation.lockComponent(containerComponentID, NodeTypeEnum.ResourceInstance))
            .thenReturn(StorageOperationStatus.OK);
        when(componentsUtils.getAllDataTypes(applicationDataTypeCache, component.getModel())).thenReturn(types);
        when(propertyOperation.validateAndUpdatePropertyValue(property.getType(), "newVal", true, null, types))
            .thenReturn(Either.right(false));
        when(componentsUtils.convertFromStorageResponse(StorageOperationStatus.BAD_REQUEST))
            .thenReturn(ActionStatus.INVALID_CONTENT);

        final Either<List<ComponentInstanceProperty>, ResponseFormat> response = componentInstanceBusinessLogic.createOrUpdatePropertiesValues(
            ComponentTypeEnum.RESOURCE_INSTANCE, containerComponentID, resourceInstanceId, properties, "userId");
        assertThat(response.isRight()).as("Response should be an error").isTrue();
        final ResponseFormat responseFormat = response.right().value();
        assertThat(responseFormat.getStatus()).as("Response status should be as expected").isEqualTo(400);
        assertThat(responseFormat.getMessageId()).as("Error message id should be as expected").isEqualTo("SVC4000");
    }

    @Test
    void testCreateOrUpdatePropertiesValuesMissingFieldFailure() {
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
    void testDeleteForwardingPathsWhenComponentinstanceDeleted() {

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
        when(toscaOperationFacade.getToscaElement(containerComponentID)).thenReturn(Either.left(component));
        when(forwardingPathOperation.deleteForwardingPath(any(Service.class), anySet()))
            .thenReturn(Either.left(new HashSet<>()));
        final ComponentInstance ci = new ComponentInstance();
        ci.setName(componentInstanceID);
        ComponentInstance responseFormatEither = componentInstanceBusinessLogic
            .deleteForwardingPathsRelatedTobeDeletedComponentInstance(
                containerComponentID, containerComponentType, ci);
        assertFalse(responseFormatEither.isEmpty());
    }

    @Test
    void testAddComponentInstanceDeploymentArtifacts() {

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

        componentInstanceBusinessLogic.setToscaOperationFacade(toscaOperationFacade);
        when(toscaOperationFacade.addDeploymentArtifactsToInstance(containerComponent.getUniqueId(), componentInstance,
            finalDeploymentArtifacts)).thenReturn(StorageOperationStatus.OK);
        when(toscaOperationFacade
            .addGroupInstancesToComponentInstance(containerComponent, componentInstance, new ArrayList<>(),
                new HashMap<>()))
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
        artifactDefinition.setArtifactName("artifactName");
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

    private void getServiceRelationByIdSuccess(Component component) {
        Either<Component, StorageOperationStatus> getComponentRes = Either.left(component);
        when(toscaOperationFacade.getToscaElement(eq(COMPONENT_ID), any(ComponentParametersView.class)))
            .thenReturn(getComponentRes);
        Either<RequirementCapabilityRelDef, ResponseFormat> response = componentInstanceBusinessLogic
            .getRelationById(COMPONENT_ID,
                RELATION_ID, USER_ID,
                component.getComponentType());
        assertThat(response.isLeft()).isTrue();
    }

    private void getServiceRelationByIdUserValidationFailure(Component component) {
        doThrow(new ByActionStatusComponentException(ActionStatus.USER_NOT_FOUND)).when(userValidations).validateUserExists(USER_ID);
        try {
            componentInstanceBusinessLogic
                .getRelationById(COMPONENT_ID, RELATION_ID, USER_ID, component.getComponentType());
        } catch (ByActionStatusComponentException e) {
            assertSame(ActionStatus.USER_NOT_FOUND, e.getActionStatus());
        }
    }

    private void getRelationByIdComponentNotFoundFailure(Component component) {
        Either<Component, StorageOperationStatus> getComponentRes = Either.right(StorageOperationStatus.NOT_FOUND);
        when(toscaOperationFacade.getToscaElement(eq(COMPONENT_ID), any(ComponentParametersView.class)))
            .thenReturn(getComponentRes);

        Either<RequirementCapabilityRelDef, ResponseFormat> response = componentInstanceBusinessLogic
            .getRelationById(COMPONENT_ID,
                RELATION_ID, USER_ID,
                component.getComponentType());
        assertThat(response.isRight()).isTrue();
    }

    private void stubMethods() {
        Mockito.lenient().when(userValidations.validateUserExists(eq(USER_ID))).thenReturn(user);
        Mockito.lenient().when(componentsUtils
            .convertFromStorageResponse(eq(StorageOperationStatus.GENERAL_ERROR), any(ComponentTypeEnum.class)))
            .thenReturn(ActionStatus.GENERAL_ERROR);
    }

    private void createComponents() {
        createRelation();
        createInstances();
        createProperties();
        createInputs();
        createService();
        resource = createResource();
    }

    private Resource createResource() {
        final Resource resource = new Resource();
        resource.setUniqueId(COMPONENT_ID);
        resource.setComponentInstancesRelations(Lists.newArrayList(relation));
        resource.setComponentInstances(Lists.newArrayList(toInstance, fromInstance));
        resource.setCapabilities(toInstance.getCapabilities());
        resource.setRequirements(fromInstance.getRequirements());
        resource.setComponentType(ComponentTypeEnum.RESOURCE);
        resource.setState(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
        return resource;
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
        service.setLastUpdaterUserId(USER_ID);

        Map<String, List<ComponentInstanceProperty>> ciPropertyMap = new HashMap<>();
        ciPropertyMap.put(TO_INSTANCE_ID, ciPropertyList);
        service.setComponentInstancesProperties(ciPropertyMap);

        Map<String, List<ComponentInstanceInput>> ciInputMap = new HashMap<>();
        ciInputMap.put(TO_INSTANCE_ID, ciInputList);
        service.setComponentInstancesInputs(ciInputMap);
    }

    private void createInstances() {
        toInstance = new ComponentInstance();
        toInstance.setUniqueId(TO_INSTANCE_ID);
        toInstance.setName(TO_INSTANCE_NAME);

        fromInstance = new ComponentInstance();
        fromInstance.setUniqueId(FROM_INSTANCE_ID);

        CapabilityDataDefinition capability = new CapabilityDataDefinition();
        capability.setOwnerId(CAPABILITY_OWNER_ID);
        capability.setUniqueId(CAPABILITY_UID);
        capability.setName(CAPABILITY_NAME);

        Map<String, List<CapabilityDefinition>> capabilities = new HashMap<>();
        final CapabilityDefinition capabilityDefinition = new CapabilityDefinition(capability);
        final ArrayList<ComponentInstanceProperty> properties = new ArrayList<>();
        properties.add(componentInstancePropInput);
        capabilityDefinition.setProperties(properties);
        capabilities.put(capability.getName(), Lists.newArrayList(capabilityDefinition));

        RequirementDataDefinition requirement = new RequirementDataDefinition();
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

    private void createProperties() {
        // Create GetInputValueData
        GetInputValueDataDefinition inputValueDef = new GetInputValueDataDefinition();
        inputValueDef.setInputId(INPUT_ID);
        List<GetInputValueDataDefinition> inputValueDefList = new ArrayList<>();
        inputValueDefList.add(inputValueDef);
        // Create ComponentInstanceProperty
        ComponentInstanceProperty ciProperty = new ComponentInstanceProperty();
        ciProperty.setGetInputValues(inputValueDefList);
        ciProperty.setName(PROP_NAME);
        // Create ComponentInstanceProperty list
        ciPropertyList = new ArrayList<>();
        ciPropertyList.add(ciProperty);
    }

    private void createInputs() {
        // Create GetInputValueData
        GetInputValueDataDefinition inputValueDef = new GetInputValueDataDefinition();
        inputValueDef.setInputId(INPUT_ID);
        List<GetInputValueDataDefinition> inputValueDefList = new ArrayList<>();
        inputValueDefList.add(inputValueDef);
        // Create ComponentInstanceInput
        ComponentInstanceInput ciInput = new ComponentInstanceInput();
        ciInput.setUniqueId(INPUT_ID);
        ciInput.setName(PROP_NAME);
        ciInput.setGetInputValues(inputValueDefList);
        // Create ComponentInstanceInput list
        ciInputList = new ArrayList<>();
        ciInputList.add(ciInput);
    }

    private ComponentInstanceBusinessLogic createTestSubject() {
        return componentInstanceBusinessLogic;
    }

    @Test
    void testChangeServiceProxyVersion() {
        ComponentInstanceBusinessLogic componentInstanceBusinessLogic;

        Either<ComponentInstance, ResponseFormat> result;

        // default test
        componentInstanceBusinessLogic = createTestSubject();
        result = componentInstanceBusinessLogic.changeServiceProxyVersion();
        assertNotNull(result);
    }

    @Test
    void testCreateServiceProxy() {
        ComponentInstanceBusinessLogic testSubject;
        Either<ComponentInstance, ResponseFormat> result;

        // default test
        testSubject = createTestSubject();
        result = testSubject.createServiceProxy();
        assertNotNull(result);
    }

    @Test
    void testDeleteServiceProxy() {
        ComponentInstanceBusinessLogic testSubject;

        Either<ComponentInstance, ResponseFormat> result;

        // default test
        testSubject = createTestSubject();
        result = testSubject.deleteServiceProxy();
        assertNotNull(result);
    }

    @Test
    void testGetComponentInstanceInputsByInputIdEmpty() {
        Component component = new Service();
        String inputId = "";
        List<ComponentInstanceInput> result;

        result = componentInstanceBusinessLogic.getComponentInstanceInputsByInputId(component, inputId);
        assertNotNull(result);
        assertThat(result.isEmpty()).isTrue();
    }

    @Test
    void testGetComponentInstanceInputsByInputIdPresent() {
        List<ComponentInstanceInput> result;

        result = componentInstanceBusinessLogic.getComponentInstanceInputsByInputId(service, INPUT_ID);
        assertNotNull(result);
        assertThat(result.isEmpty()).isFalse();
        assertThat(result.size()).isOne();
        ComponentInstanceInput resultInput = result.get(0);
        assertThat(resultInput.getComponentInstanceId()).isEqualTo(TO_INSTANCE_ID);
        assertThat(resultInput.getComponentInstanceName()).isEqualTo(TO_INSTANCE_NAME);
    }

    @Test
    void testGetComponentInstancePropertiesByInputIdEmpty() {
        Component component = new Service();
        String inputId = "";
        List<ComponentInstanceProperty> result;

        result = componentInstanceBusinessLogic.getComponentInstancePropertiesByInputId(component, inputId);
        assertNotNull(result);
        assertThat(result.isEmpty()).isTrue();
    }

    @Test
    void testGetComponentInstancePropertiesByInputIdPresent() {
        List<ComponentInstanceProperty> result;

        result = componentInstanceBusinessLogic.getComponentInstancePropertiesByInputId(service, INPUT_ID);
        assertNotNull(result);
        assertThat(result.size()).isOne();
        ComponentInstanceProperty resultProperty = result.get(0);
        assertThat(resultProperty.getComponentInstanceId()).isEqualTo(TO_INSTANCE_ID);
        assertThat(resultProperty.getComponentInstanceName()).isEqualTo(TO_INSTANCE_NAME);
    }

    @Test
    void testGetRelationById() {
        ComponentInstanceBusinessLogic testSubject;
        String componentId = "";
        String relationId = "";
        String userId = user.getUserId();
        ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.RESOURCE_INSTANCE;
        Either<RequirementCapabilityRelDef, ResponseFormat> result;

        // default test
        testSubject = createTestSubject();
        result = testSubject.getRelationById(componentId, relationId, userId, componentTypeEnum);
        assertNotNull(result);
    }

    @Test
    void testValidateParent() {
        ComponentInstanceBusinessLogic testSubject;
        resource = createResource();
        String nodeTemplateId = "";
        boolean result;

        // default test
        testSubject = createTestSubject();
        result = Deencapsulation.invoke(testSubject, "validateParent", new Object[]{resource, nodeTemplateId});
        assertFalse(result);
    }

    @Test
    void testGetComponentType() {
        ComponentInstanceBusinessLogic testSubject;
        ComponentTypeEnum result;

        // default test
        testSubject = createTestSubject();
        result = Deencapsulation.invoke(testSubject, "getComponentType", new Object[]{ComponentTypeEnum.class});
        assertNotNull(result);
    }

    @Test
    void testGetNewGroupName() {
        ComponentInstanceBusinessLogic testSubject;
        String oldPrefix = "";
        String newNormailzedPrefix = "";
        String qualifiedGroupInstanceName = "";
        String result;

        // test 1
        testSubject = createTestSubject();
        result = Deencapsulation.invoke(testSubject, "getNewGroupName",
            new Object[]{oldPrefix, newNormailzedPrefix, qualifiedGroupInstanceName});
        assertNotNull(result);
    }

    @Test
    void testUpdateComponentInstanceMetadata_3() {
        ComponentInstanceBusinessLogic testSubject;
        createInstances();
        ComponentInstance result;

        // default test
        testSubject = createTestSubject();
        result = Deencapsulation
            .invoke(testSubject, "updateComponentInstanceMetadata", new Object[]{toInstance, toInstance});
        assertNotNull(result);
    }

    @Test
    void testFindRelation() {
        ComponentInstanceBusinessLogic testSubject;
        String relationId = "";
        List<RequirementCapabilityRelDef> requirementCapabilityRelations = new ArrayList<>();
        RequirementCapabilityRelDef result;

        // default test
        testSubject = createTestSubject();
        result = Deencapsulation.invoke(testSubject, "findRelation",
            new Object[]{relationId, requirementCapabilityRelations});
        assertNull(result);
    }

    @Test
    void testCreateOrUpdatePropertiesValues() {
        ComponentInstanceBusinessLogic testSubject;
        ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.RESOURCE;
        resource = createResource();
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
        assertNotNull(result);

        componentTypeEnum = null;
        result = testSubject
            .createOrUpdatePropertiesValues(componentTypeEnum, componentId, resourceInstanceId, properties,
                userId);
        assertNotNull(result);

        result = testSubject
            .createOrUpdatePropertiesValues(componentTypeEnum, componentId, resourceInstanceId, properties,
                userId);
        assertNotNull(result);
    }

    @Test
    void testUpdateCapabilityPropertyOnContainerComponent() {
        ComponentInstanceBusinessLogic testSubject;
        ComponentInstanceProperty property = new ComponentInstanceProperty();
        String newValue = "";
        resource = createResource();
        createInstances();
        String capabilityType = "";
        String capabilityName = "";
        ResponseFormat result;

        // default test
        testSubject = createTestSubject();
        result = Deencapsulation.invoke(testSubject, "updateCapabilityPropertyOnContainerComponent",
            new Object[]{property, newValue, resource, toInstance, capabilityType, capabilityName});
        assertNull(result);
    }

    @Test
    void testCreateOrUpdateInstanceInputValues() {
        ComponentInstanceBusinessLogic testSubject;
        ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.RESOURCE;
        resource = createResource();
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
        assertNotNull(result);
        componentTypeEnum = null;
        result = testSubject
            .createOrUpdateInstanceInputValues(componentTypeEnum, componentId, resourceInstanceId, inputs,
                userId);
        assertNotNull(result);

        result = testSubject
            .createOrUpdateInstanceInputValues(componentTypeEnum, componentId, resourceInstanceId, inputs,
                userId);
        assertNotNull(result);
    }

    @Test
    void testCreateOrUpdateGroupInstancePropertyValue() {
        ComponentInstanceBusinessLogic testSubject;
        ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.RESOURCE;
        resource = createResource();
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
        assertNotNull(result);
        componentTypeEnum = null;
        result = testSubject
            .createOrUpdateGroupInstancePropertyValue(componentTypeEnum, componentId, resourceInstanceId,
                groupInstanceId, property, userId);
        assertNotNull(result);

        result = testSubject
            .createOrUpdateGroupInstancePropertyValue(componentTypeEnum, componentId, resourceInstanceId,
                groupInstanceId, property, userId);
        assertNotNull(result);
    }

    @Test
    void testDeletePropertyValue() {
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
        assertNotNull(result);
        componentTypeEnum = null;
        result = testSubject.deletePropertyValue(componentTypeEnum, serviceId, resourceInstanceId, propertyValueId,
            userId);
        assertNotNull(result);

        result = testSubject.deletePropertyValue(componentTypeEnum, serviceId, resourceInstanceId, propertyValueId,
            userId);
        assertNotNull(result);
    }

    @Test
    void testGetComponentParametersViewForForwardingPath() {
        ComponentInstanceBusinessLogic testSubject;
        ComponentParametersView result;

        // default test
        testSubject = createTestSubject();
        result = Deencapsulation.invoke(testSubject, "getComponentParametersViewForForwardingPath");
        assertNotNull(result);
    }

    @Test
    void testGetResourceInstanceById() {
        ComponentInstanceBusinessLogic testSubject;
        resource = createResource();
        String instanceId = "";
        Either<ComponentInstance, StorageOperationStatus> result;

        // default test
        testSubject = createTestSubject();
        result = Deencapsulation.invoke(testSubject, "getResourceInstanceById", new Object[]{resource, instanceId});
        assertNotNull(result);
    }

    @Test
    void testUpdateInstanceCapabilityProperties_1() {
        ComponentInstanceBusinessLogic testSubject;
        ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.RESOURCE;
        resource = createResource();
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
        assertNotNull(result);
        when(toscaOperationFacade.getToscaFullElement(containerComponentId)).thenReturn(Either.left(resource));
        result = testSubject.updateInstanceCapabilityProperties(componentTypeEnum, containerComponentId,
            componentInstanceUniqueId, capabilityType, capabilityName, properties, userId);
        assertNotNull(result);
    }

    @Test
    void testUpdateInstanceRequirement() {
        ComponentInstanceBusinessLogic testSubject;
        ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.RESOURCE;
        createComponents();
        String userId = "userId";
        resource.setLastUpdaterUserId(userId);
        String containerComponentId = resource.getUniqueId();
        String componentInstanceUniqueId = TO_INSTANCE_ID;
        String capabilityType = "";
        String capabilityName = "";
        RequirementDefinition requirementDef = new RequirementDefinition();

        Either<RequirementDefinition, ResponseFormat> result;

        when(toscaOperationFacade.getToscaFullElement(containerComponentId)).thenReturn(Either.left(resource));
        testSubject = createTestSubject();
        when(toscaOperationFacade.updateComponentInstanceRequirement(containerComponentId, TO_INSTANCE_ID, requirementDef)).thenReturn(StorageOperationStatus.OK);
        when(toscaOperationFacade.updateComponentInstanceMetadataOfTopologyTemplate(resource)).thenReturn(Either.left(resource));
        when(graphLockOperation.unlockComponent(Mockito.anyString(), eq(NodeTypeEnum.Resource)))
            .thenReturn(StorageOperationStatus.OK);
        when(graphLockOperation.lockComponent(Mockito.anyString(), eq(NodeTypeEnum.Resource)))
            .thenReturn(StorageOperationStatus.OK);

        result = testSubject.updateInstanceRequirement(componentTypeEnum, containerComponentId,
            componentInstanceUniqueId, requirementDef, userId);
        assertEquals(requirementDef, result.left().value());

    }

    @Test
    void testCopyComponentInstanceWrongUserId() {

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
        assertNotNull(result);

        service.setLastUpdaterUserId(oldLastUpdatedUserId);
        assertThat(result.isRight()).isTrue();
    }

    @Test
    void testCopyComponentInstanceComponentWrongState() {
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
        result = componentInstanceBusinessLogic
            .copyComponentInstance(inputComponentInstance, containerComponentId, componentInstanceId, USER_ID);
        assertNotNull(result);
        service.setLastUpdaterUserId(oldServiceLastUpdatedUserId);
        assertThat(result.isRight()).isTrue();
    }

    @Test
    void testCopyComponentInstance() {
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

        result = componentInstanceBusinessLogic
            .copyComponentInstance(inputComponentInstance, containerComponentId, componentInstanceId,
                USER_ID);
        assertNotNull(result);

        service.setLastUpdaterUserId(oldServiceLastUpdatedUserId);
        resource.setLifecycleState(oldResourceLifeCycle);

        assertThat(result.isLeft()).isFalse();
    }

    @Test
    void testCreateOrUpdateAttributeValueForCopyPaste() {
        ComponentInstance serviceComponentInstance = createComponetInstanceFromComponent(service);
        ComponentInstanceAttribute attribute = new ComponentInstanceAttribute();
        attribute.setType("string");
        attribute.setUniqueId("testCreateOrUpdateAttributeValueForCopyPaste");
        LifecycleStateEnum oldLifeCycleState = service.getLifecycleState();
        String oldLastUpdatedUserId = service.getLastUpdaterUserId();
        service.setLastUpdaterUserId(USER_ID);
        service.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);

        Map<String, List<ComponentInstanceAttribute>> instAttrsMap = new HashMap<>();
        List<ComponentInstanceAttribute> instAttrsList = new ArrayList<>();
        ComponentInstanceAttribute prop = new ComponentInstanceAttribute();
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

        Either<ComponentInstanceAttribute, ResponseFormat> result = Deencapsulation
            .invoke(componentInstanceBusinessLogic,
                "createOrUpdateAttributeValueForCopyPaste",
                ComponentTypeEnum.SERVICE,
                serviceComponentInstance
                    .getUniqueId(),
                toInstance.getUniqueId(), attribute,
                USER_ID);
        assertNotNull(result);

        service.setLastUpdaterUserId(oldLastUpdatedUserId);
        service.setLifecycleState(oldLifeCycleState);

        assertThat(result.isLeft()).isTrue();
        ComponentInstanceAttribute resultProp = result.left().value();
        assertEquals(1, resultProp.getPath().size());
        assertEquals(resultProp.getPath().get(0), toInstance.getUniqueId());
    }

    @Test
    void testUpdateComponentInstanceProperty() {

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
        assertNotNull(result);
        assertThat(result.isLeft()).isTrue();
    }

    @Test
    void testGetInputListDefaultValue() {
        Component component = service;
        String inputId = "dummy_id";
        String defaultValue = "dummy_default_value";
        List<InputDefinition> newInputs = new ArrayList<>();
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
    void testBatchDeleteComponentInstanceFailureWrongType() {
        Map<String, List<String>> result;
        List<String> componentInstanceIdList = new ArrayList<>();
        String containerComponentParam = "WRONG_TYPE";
        String containerComponentId = "containerComponentId";
        String componentInstanceId = "componentInstanceId";
        componentInstanceIdList.add(componentInstanceId);
        Map<String, List<String>> deleteErrorMap = new HashMap<>();
        List<String> deleteErrorIds = new ArrayList<>();
        deleteErrorIds.add(componentInstanceId);
        deleteErrorMap.put("deleteFailedIds", deleteErrorIds);
        Either<Component, StorageOperationStatus> cont = Either.left(service);
        when(componentsUtils.convertFromStorageResponse(StorageOperationStatus.NOT_FOUND, null))
            .thenReturn(ActionStatus.GENERAL_ERROR);
        when(toscaOperationFacade.getToscaElement(any(String.class), any(ComponentParametersView.class)))
            .thenReturn(cont);

        try {
            result = componentInstanceBusinessLogic
                .batchDeleteComponentInstance(containerComponentParam, containerComponentId, componentInstanceIdList,
                    USER_ID);
            assertNotNull(result);
            assertEquals(deleteErrorMap, result);
        } catch (ComponentException e) {
            assertEquals(e.getActionStatus().toString(), StorageOperationStatus.GENERAL_ERROR.toString());
        }
    }

    @Test
    void testBatchDeleteComponentInstanceFailureCompIds() {
        String containerComponentParam = ComponentTypeEnum.SERVICE_PARAM_NAME;
        String containerComponentId = "containerComponentId";
        String componentInstanceId = "componentInstanceId";
        List<String> componentInstanceIdList = new ArrayList<>();
        componentInstanceIdList.add(componentInstanceId);
        Map<String, List<String>> deleteErrorMap = new HashMap<>();
        List<String> deleteErrorIds = new ArrayList<>();
        deleteErrorIds.add(componentInstanceId);
        deleteErrorMap.put("deleteFailedIds", deleteErrorIds);

        Either<Component, StorageOperationStatus> err = Either.right(StorageOperationStatus.GENERAL_ERROR);
        when(toscaOperationFacade.getToscaElement(eq(containerComponentId), any(ComponentParametersView.class)))
            .thenReturn(err);

        try {
            Map<String, List<String>> result = componentInstanceBusinessLogic.batchDeleteComponentInstance(
                containerComponentParam, containerComponentId, componentInstanceIdList, USER_ID);
            assertNotNull(result);
            assertEquals(deleteErrorMap, result);
        } catch (ComponentException e) {
            assertEquals(e.getActionStatus().toString(), StorageOperationStatus.GENERAL_ERROR.toString());
        }
    }

    @Test
    void testBatchDeleteComponentInstanceSuccess() {
        Map<String, List<String>> result;
        String containerComponentParam = ComponentTypeEnum.SERVICE_PARAM_NAME;
        LifecycleStateEnum oldLifeCycleState = service.getLifecycleState();
        String oldLastUpdatedUserId = service.getLastUpdaterUserId();
        service.setLastUpdaterUserId(USER_ID);
        service.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
        String containerComponentId = service.getUniqueId();
        String componentInstanceId = TO_INSTANCE_ID;
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
                componentInstanceIdList, USER_ID);
        assertNotNull(result);

        service.setLastUpdaterUserId(oldLastUpdatedUserId);
        service.setLifecycleState(oldLifeCycleState);
        assertEquals(deleteErrorMap, result);
    }

    @Test
    void testDissociateRIFromRIFailDissociate() {

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
        when(componentsUtils.convertFromStorageResponseForResourceInstance(StorageOperationStatus.OK, true))
            .thenReturn(ActionStatus.GENERAL_ERROR);
        when(toscaOperationFacade.dissociateResourceInstances(componentId, ref)).thenReturn(resultEither);

        try {
            result = componentInstanceBusinessLogic
                .batchDissociateRIFromRI(componentId, USER_ID, requirementDefList, componentTypeEnum);
            assertNotNull(result);
            assertEquals(new ArrayList<>(), result);
        } catch (ComponentException e) {
            assertEquals(e.getActionStatus().toString(), StorageOperationStatus.GENERAL_ERROR.toString());
        }

        service.setLastUpdaterUserId(oldLastUpdatedUserId);
        service.setLifecycleState(oldLifeCycleState);

    }

    @Test
    void testDissociateRIFromRISuccess() {

        List<RequirementCapabilityRelDef> result;
        RequirementCapabilityRelDef ref = new RequirementCapabilityRelDef();
        List<RequirementCapabilityRelDef> requirementDefList = new ArrayList<>();
        requirementDefList.add(ref);
        ComponentTypeEnum componentTypeEnum = service.getComponentType();
        String componentId = service.getUniqueId();
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
            .batchDissociateRIFromRI(componentId, USER_ID, requirementDefList, componentTypeEnum);
        assertNotNull(result);

        service.setLastUpdaterUserId(oldLastUpdatedUserId);
        service.setLifecycleState(oldLifeCycleState);

        assertEquals(requirementDefList, result);
    }

    @Test
    void testGetComponentInstancePropertyByPolicyId_success() {
        Optional<ComponentInstanceProperty> propertyCandidate =
            getComponentInstanceProperty(PROP_NAME);

        assertThat(propertyCandidate).isPresent();
        assertEquals(PROP_NAME, propertyCandidate.get().getName());
    }

    @Test
    void testGetComponentInstancePropertyByPolicyId_failure() {
        Optional<ComponentInstanceProperty> propertyCandidate =
            getComponentInstanceProperty(NON_EXIST_NAME);

        assertEquals(propertyCandidate, Optional.empty());
    }

    private Optional<ComponentInstanceProperty> getComponentInstanceProperty(String propertyName) {
        ComponentInstanceProperty componentInstanceProperty = new ComponentInstanceProperty();
        componentInstanceProperty.setName(propertyName);

        PolicyDefinition policyDefinition = getPolicyDefinition();
        componentInstanceProperty.setGetPolicyValues(policyDefinition.getGetPolicyValues());

        service.setComponentInstancesProperties(
            Collections.singletonMap(COMPONENT_INSTANCE_ID, Collections.singletonList(componentInstanceProperty)));

        return componentInstanceBusinessLogic.getComponentInstancePropertyByPolicyId(service, policyDefinition);
    }

    private PolicyDefinition getPolicyDefinition() {
        PolicyDefinition policyDefinition = new PolicyDefinition();
        policyDefinition.setInstanceUniqueId(COMPONENT_INSTANCE_ID);
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

    // Prepare ComponentInstance & Resource objects used in createComponentInstance() tests

    private Pair<ComponentInstance, Resource> prepareResourcesForCreateComponentInstanceTest() {
        ComponentInstance instanceToBeCreated = new ComponentInstance();
        instanceToBeCreated.setName(COMPONENT_INSTANCE_NAME);
        instanceToBeCreated.setUniqueId(COMPONENT_INSTANCE_ID);
        instanceToBeCreated.setComponentUid(ORIGIN_COMPONENT_ID);
        instanceToBeCreated.setOriginType(OriginTypeEnum.VF);

        Resource originComponent = new Resource();
        originComponent.setLifecycleState(LifecycleStateEnum.CERTIFIED);
        originComponent.setResourceType(ResourceTypeEnum.VF);
        originComponent.setVersion(ORIGIN_COMPONENT_VERSION);
        originComponent.setIcon(ICON_NAME);

        return Pair.of(instanceToBeCreated, originComponent);
    }
    // Common part for testing component instance name validation

    private void testCreateComponentInstanceNameValidationFailure(String ciName) {
        ComponentInstance ci = new ComponentInstance();
        ci.setName(ciName);

        // Stub for getting component
        when(toscaOperationFacade.getToscaElement(eq(COMPONENT_ID), any(ComponentParametersView.class)))
            .thenReturn(Either.left(service));

        // Expecting ByActionStatusComponentException
        ByActionStatusComponentException e = assertThrows(ByActionStatusComponentException.class, () -> {
            componentInstanceBusinessLogic.createComponentInstance(ComponentTypeEnum.SERVICE_PARAM_NAME, COMPONENT_ID, USER_ID, ci);
        });
        assertEquals(ActionStatus.INVALID_COMPONENT_NAME, e.getActionStatus());
    }
    @TestFactory
    Iterable<DynamicTest> testCreateComponentInstanceNameValidationFailureFactory() {
        String longName = String.join("", Collections.nCopies(ValidationUtils.COMPONENT_NAME_MAX_LENGTH + 1, "x"));
        String invalidName = "componentInstance#name";
        return Arrays.asList(
            dynamicTest("instance name is empty", () ->
                testCreateComponentInstanceNameValidationFailure("")),
            dynamicTest("instance name is too long", () ->
                testCreateComponentInstanceNameValidationFailure(longName)),
            dynamicTest("instance name includes invalid character", () ->
                testCreateComponentInstanceNameValidationFailure(invalidName))
        );
    }

    @Test
    void testCreateComponentInstanceFailToGetComponent() {
        ComponentInstance ci = prepareResourcesForCreateComponentInstanceTest().getLeft();

        // Stub for getting component
        when(toscaOperationFacade.getToscaElement(eq(COMPONENT_ID), any(ComponentParametersView.class)))
            .thenReturn(Either.left(service));
        when(toscaOperationFacade.getToscaFullElement(ORIGIN_COMPONENT_ID))
            .thenReturn(Either.right(StorageOperationStatus.NOT_FOUND));
        when(componentsUtils.convertFromStorageResponse(StorageOperationStatus.NOT_FOUND, ComponentTypeEnum.RESOURCE))
            .thenReturn(ActionStatus.RESOURCE_NOT_FOUND);

        ByActionStatusComponentException e = assertThrows(ByActionStatusComponentException.class, () -> {
            componentInstanceBusinessLogic.createComponentInstance(ComponentTypeEnum.SERVICE_PARAM_NAME, COMPONENT_ID, USER_ID, ci);
        });
        assertThat(e.getActionStatus()).isEqualTo(ActionStatus.RESOURCE_NOT_FOUND);
    }

    @Test
    void testCreateComponentInstanceFailureInvalidState() {
        Pair<ComponentInstance, Resource> p = prepareResourcesForCreateComponentInstanceTest();
        ComponentInstance ci = p.getLeft();
        Resource originComponent = p.getRight();
        originComponent.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);

        // Stub for getting component
        when(toscaOperationFacade.getToscaElement(eq(COMPONENT_ID), any(ComponentParametersView.class)))
            .thenReturn(Either.left(service));
        when(toscaOperationFacade.getToscaFullElement(ORIGIN_COMPONENT_ID))
            .thenReturn(Either.left(originComponent));

        ByActionStatusComponentException e = assertThrows(ByActionStatusComponentException.class, () -> {
            componentInstanceBusinessLogic.createComponentInstance(ComponentTypeEnum.SERVICE_PARAM_NAME, COMPONENT_ID, USER_ID, ci);
        });
        assertThat(e.getActionStatus()).isEqualTo(ActionStatus.CONTAINER_CANNOT_CONTAIN_COMPONENT_IN_STATE);
    }

    @Test
    void testCreateComponentInstanceFailureArchived() {
        Pair<ComponentInstance, Resource> p = prepareResourcesForCreateComponentInstanceTest();
        ComponentInstance ci = p.getLeft();
        Resource originComponent = p.getRight();
        originComponent.setArchived(Boolean.TRUE);

        // Stub for getting component
        when(toscaOperationFacade.getToscaElement(eq(COMPONENT_ID), any(ComponentParametersView.class)))
            .thenReturn(Either.left(service));
        when(toscaOperationFacade.getToscaFullElement(ORIGIN_COMPONENT_ID))
            .thenReturn(Either.left(originComponent));

        ByActionStatusComponentException e = assertThrows(ByActionStatusComponentException.class, () -> {
            componentInstanceBusinessLogic.createComponentInstance(ComponentTypeEnum.SERVICE_PARAM_NAME, COMPONENT_ID, USER_ID, ci);
        });
        assertThat(e.getActionStatus()).isEqualTo(ActionStatus.COMPONENT_IS_ARCHIVED);
    }

    @Test
    void testCreateComponentInstanceFailureInvalidOriginType() {
        Pair<ComponentInstance, Resource> p = prepareResourcesForCreateComponentInstanceTest();
        ComponentInstance ci = p.getLeft();
        Resource originComponent = p.getRight();
        ci.setOriginType(OriginTypeEnum.VFC); // Set different type from origin

        // Stub for getting component
        when(toscaOperationFacade.getToscaElement(eq(COMPONENT_ID), any(ComponentParametersView.class)))
            .thenReturn(Either.left(service));
        when(toscaOperationFacade.getToscaFullElement(ORIGIN_COMPONENT_ID))
            .thenReturn(Either.left(originComponent));

        final ByActionStatusComponentException e = assertThrows(ByActionStatusComponentException.class, () -> {
            componentInstanceBusinessLogic.createComponentInstance(ComponentTypeEnum.SERVICE_PARAM_NAME, COMPONENT_ID, USER_ID, ci);
        });
        assertThat(e.getActionStatus()).isEqualTo(ActionStatus.INVALID_CONTENT);
    }

    @Test
    void testCreateComponentInstanceFailureCannotContainInstance() {
        final Pair<ComponentInstance, Resource> p = prepareResourcesForCreateComponentInstanceTest();
        final ComponentInstance ci = p.getLeft();
        final Resource originComponent = p.getRight();

        // Stub for getting component
        when(toscaOperationFacade.getToscaElement(eq(COMPONENT_ID), any(ComponentParametersView.class)))
            .thenReturn(Either.left(service));
        when(toscaOperationFacade.getToscaFullElement(ORIGIN_COMPONENT_ID))
            .thenReturn(Either.left(originComponent));
        // Assume services cannot contain VF resource
        when(containerInstanceTypeData.isAllowedForServiceComponent(ResourceTypeEnum.VF, null))
            .thenReturn(false);

        ByActionStatusComponentException actualException = assertThrows(ByActionStatusComponentException.class, () -> {
            componentInstanceBusinessLogic.createComponentInstance(ComponentTypeEnum.SERVICE_PARAM_NAME, COMPONENT_ID, USER_ID, ci);
        });
        assertThat(actualException.getActionStatus()).isEqualTo(ActionStatus.CONTAINER_CANNOT_CONTAIN_INSTANCE);
        verify(containerInstanceTypeData, times(1)).isAllowedForServiceComponent(ResourceTypeEnum.VF, null);

        //given
        final Resource resource = createResource();
        resource.setResourceType(ResourceTypeEnum.VF);
        resource.setLastUpdaterUserId(USER_ID);
        //when
        when(toscaOperationFacade.getToscaElement(eq(COMPONENT_ID), any(ComponentParametersView.class)))
            .thenReturn(Either.left(resource));
        when(toscaOperationFacade.getToscaFullElement(ORIGIN_COMPONENT_ID))
            .thenReturn(Either.left(originComponent));
        when(containerInstanceTypeData.isAllowedForResourceComponent(ResourceTypeEnum.VF, ResourceTypeEnum.VF))
            .thenReturn(false);
        actualException = assertThrows(ByActionStatusComponentException.class, () -> {
            componentInstanceBusinessLogic.createComponentInstance(RESOURCE_PARAM_NAME, COMPONENT_ID, USER_ID, ci);
        });
        //then
        assertThat(actualException.getActionStatus()).isEqualTo(ActionStatus.CONTAINER_CANNOT_CONTAIN_INSTANCE);
    }

    @Test
    void testCreateComponentInstanceFailureAddToGraph() {
        final Pair<ComponentInstance, Resource> p = prepareResourcesForCreateComponentInstanceTest();
        final ComponentInstance ci = p.getLeft();
        final Resource originComponent = p.getRight();

        // TODO Refactor createComponentInstance() method and reduce these mocks
        //      not to target the internal details too much
        when(toscaOperationFacade.getToscaElement(eq(COMPONENT_ID), any(ComponentParametersView.class)))
            .thenReturn(Either.left(service));
        when(toscaOperationFacade.getToscaFullElement(ORIGIN_COMPONENT_ID))
            .thenReturn(Either.left(originComponent));
        when(containerInstanceTypeData.isAllowedForServiceComponent(ResourceTypeEnum.VF, null))
            .thenReturn(true);
        Mockito.doNothing().when(compositionBusinessLogic).validateAndSetDefaultCoordinates(ci);
        when(graphLockOperation.lockComponent(COMPONENT_ID, NodeTypeEnum.Service))
            .thenReturn(StorageOperationStatus.OK);
        when(toscaOperationFacade.addComponentInstanceToTopologyTemplate(service, originComponent, ci, false, user))
            .thenReturn(Either.right(StorageOperationStatus.BAD_REQUEST));
        when(componentsUtils.convertFromStorageResponseForResourceInstance(StorageOperationStatus.BAD_REQUEST, true))
            .thenReturn(ActionStatus.INVALID_CONTENT);
        when(componentsUtils.getResponseFormatForResourceInstance(ActionStatus.INVALID_CONTENT, "", null))
            .thenReturn(new ResponseFormat());
        when(janusGraphDao.rollback()).thenReturn(JanusGraphOperationStatus.OK);
        when(graphLockOperation.unlockComponent(COMPONENT_ID, NodeTypeEnum.Service))
            .thenReturn(StorageOperationStatus.OK);

        assertThrows(ByResponseFormatComponentException.class, () -> {
            componentInstanceBusinessLogic.createComponentInstance(ComponentTypeEnum.SERVICE_PARAM_NAME, COMPONENT_ID, USER_ID, ci);
        });
        verify(containerInstanceTypeData, times(1))
            .isAllowedForServiceComponent(ResourceTypeEnum.VF, null);
        verify(compositionBusinessLogic, times(1)).validateAndSetDefaultCoordinates(ci);
        verify(toscaOperationFacade, times(1))
            .addComponentInstanceToTopologyTemplate(service, originComponent, ci, false, user);
        verify(graphLockOperation, times(1)).unlockComponent(COMPONENT_ID, NodeTypeEnum.Service);
    }

    @Test
    void testCreateComponentInstanceSuccess() {
        final Pair<ComponentInstance, Resource> p = prepareResourcesForCreateComponentInstanceTest();
        final ComponentInstance instanceToBeCreated = p.getLeft();
        final Resource originComponent = p.getRight();

        final Service updatedService = new Service();
        updatedService.setComponentInstances(Collections.singletonList(instanceToBeCreated));
        updatedService.setUniqueId(service.getUniqueId());

        // TODO Refactor createComponentInstance() method and reduce these mocks
        //      not to target the internal details too much
        when(toscaOperationFacade.getToscaElement(eq(COMPONENT_ID), any(ComponentParametersView.class)))
            .thenReturn(Either.left(service));
        when(toscaOperationFacade.getToscaFullElement(ORIGIN_COMPONENT_ID))
            .thenReturn(Either.left(originComponent));
        when(containerInstanceTypeData.isAllowedForServiceComponent(ResourceTypeEnum.VF, null))
            .thenReturn(true);
        Mockito.doNothing().when(compositionBusinessLogic).validateAndSetDefaultCoordinates(instanceToBeCreated);
        when(graphLockOperation.lockComponent(COMPONENT_ID, NodeTypeEnum.Service))
            .thenReturn(StorageOperationStatus.OK);
        when(toscaOperationFacade.addComponentInstanceToTopologyTemplate(service, originComponent, instanceToBeCreated, false, user))
            .thenReturn(Either.left(new ImmutablePair<>(updatedService, COMPONENT_INSTANCE_ID)));
        when(artifactsBusinessLogic.getArtifacts(
            ORIGIN_COMPONENT_ID, NodeTypeEnum.Resource, ArtifactGroupTypeEnum.DEPLOYMENT, null))
            .thenReturn(Either.left(new HashMap<>()));
        when(toscaOperationFacade
            .addInformationalArtifactsToInstance(service.getUniqueId(), instanceToBeCreated, originComponent.getArtifacts()))
            .thenReturn(StorageOperationStatus.OK);
        when(janusGraphDao.commit()).thenReturn(JanusGraphOperationStatus.OK);
        when(graphLockOperation.unlockComponent(COMPONENT_ID, NodeTypeEnum.Service))
            .thenReturn(StorageOperationStatus.OK);

        final ComponentInstance result = componentInstanceBusinessLogic.createComponentInstance(
            ComponentTypeEnum.SERVICE_PARAM_NAME, COMPONENT_ID, USER_ID, instanceToBeCreated);
        assertThat(result).isEqualTo(instanceToBeCreated);
        assertThat(instanceToBeCreated.getComponentVersion()).isEqualTo(originComponent.getVersion());
        assertThat(instanceToBeCreated.getIcon()).isEqualTo(originComponent.getIcon());
        verify(containerInstanceTypeData, times(1))
            .isAllowedForServiceComponent(ResourceTypeEnum.VF, null);
        verify(compositionBusinessLogic, times(1)).validateAndSetDefaultCoordinates(instanceToBeCreated);
        verify(toscaOperationFacade, times(1))
            .addComponentInstanceToTopologyTemplate(service, originComponent, instanceToBeCreated, false, user);
        // Check graph db change was committed
        verify(janusGraphDao, times(1)).commit();
    }
    
    @Test
    void testCreateComponentInstanceServiceSubstitutionSuccess() {
        ComponentInstance instanceToBeCreated = createServiceSubstitutionComponentInstance();
        Service originService = createServiceSubstitutionOriginService();
        Component serviceBaseComponent = createServiceSubstitutionServiceDerivedFromComponent();

        Service updatedService = new Service();
        updatedService.setComponentInstances(Collections.singletonList(instanceToBeCreated));
        updatedService.setUniqueId(service.getUniqueId());
        
        when(toscaOperationFacade.getToscaElement(eq(COMPONENT_ID), any(ComponentParametersView.class)))
            .thenReturn(Either.left(service));
        when(toscaOperationFacade.getToscaFullElement(ORIGIN_COMPONENT_ID))
            .thenReturn(Either.left(originService));
        when(toscaOperationFacade.getLatestByToscaResourceName(eq(originService.getDerivedFromGenericType()), isNull()))
            .thenReturn(Either.left(serviceBaseComponent));
        when(toscaOperationFacade.getToscaElement(eq(ORIGIN_COMPONENT_ID), any(ComponentParametersView.class)))
            .thenReturn(Either.left(originService));
        Mockito.doNothing().when(compositionBusinessLogic).validateAndSetDefaultCoordinates(instanceToBeCreated);
        when(graphLockOperation.lockComponent(COMPONENT_ID, NodeTypeEnum.Service))
            .thenReturn(StorageOperationStatus.OK);
        when(toscaOperationFacade.addComponentInstanceToTopologyTemplate(service, serviceBaseComponent, instanceToBeCreated, false, user))
            .thenReturn(Either.left(new ImmutablePair<>(updatedService, COMPONENT_INSTANCE_ID)));
        when(artifactsBusinessLogic.getArtifacts(
                "baseComponentId", NodeTypeEnum.Resource, ArtifactGroupTypeEnum.DEPLOYMENT, null))
            .thenReturn(Either.left(new HashMap<>()));
        when(toscaOperationFacade
            .addInformationalArtifactsToInstance(service.getUniqueId(), instanceToBeCreated, originService.getArtifacts()))
            .thenReturn(StorageOperationStatus.OK);
        when(janusGraphDao.commit()).thenReturn(JanusGraphOperationStatus.OK);
        when(graphLockOperation.unlockComponent(COMPONENT_ID, NodeTypeEnum.Service))
            .thenReturn(StorageOperationStatus.OK);

        ComponentInstance result = componentInstanceBusinessLogic.createComponentInstance(
            ComponentTypeEnum.SERVICE_PARAM_NAME, COMPONENT_ID, USER_ID, instanceToBeCreated);
        assertThat(result).isEqualTo(instanceToBeCreated);
        assertThat(instanceToBeCreated.getComponentVersion()).isEqualTo(originService.getVersion());
        assertThat(instanceToBeCreated.getIcon()).isEqualTo(originService.getIcon());
        verify(compositionBusinessLogic, times(1)).validateAndSetDefaultCoordinates(instanceToBeCreated);
        verify(toscaOperationFacade, times(1))
            .addComponentInstanceToTopologyTemplate(service, serviceBaseComponent, instanceToBeCreated, false, user);
        // Check graph db change was committed
        verify(janusGraphDao, times(1)).commit();
    }

    @Test
    void testGetComponentInstanceAttributesById_success() {
        final ComponentInstanceAttribute componentInstanceAttribute = new ComponentInstanceAttribute();
        componentInstanceAttribute.setComponentInstanceId(TO_INSTANCE_ID);

        final HashMap<String, List<ComponentInstanceAttribute>> map = new HashMap<>();
        map.put(TO_INSTANCE_ID, Arrays.asList(componentInstanceAttribute));
        resource.setComponentInstancesAttributes(map);

        final Either<Component, StorageOperationStatus> leftServiceOp = Either.left(resource);
        doReturn(leftServiceOp).when(toscaOperationFacade).getToscaElement(COMPONENT_ID);

        final List<ComponentInstanceAttribute> result = componentInstanceBusinessLogic
            .getComponentInstanceAttributesById(RESOURCE_PARAM_NAME, COMPONENT_ID, TO_INSTANCE_ID, USER_ID);
        assertThat(result).isNotNull().isNotEmpty();
        verify(toscaOperationFacade, times(1)).getToscaElement(COMPONENT_ID);
    }

    @Test
    void testGetComponentInstanceAttributesById_fail_missing_ComponentInstancesAttributes() {
        final Either<Component, StorageOperationStatus> leftServiceOp = Either.left(resource);
        doReturn(leftServiceOp).when(toscaOperationFacade).getToscaElement(COMPONENT_ID);

        final List<ComponentInstanceAttribute> result = componentInstanceBusinessLogic
            .getComponentInstanceAttributesById(RESOURCE_PARAM_NAME, COMPONENT_ID, TO_INSTANCE_ID, USER_ID);
        assertThat(result).isNotNull().isEmpty();
        verify(toscaOperationFacade, times(1)).getToscaElement(COMPONENT_ID);
    }

    @Test
    void testGetComponentInstanceAttributesById_fail_getToscaElement() {
        final ComponentInstanceAttribute componentInstanceAttribute = new ComponentInstanceAttribute();
        componentInstanceAttribute.setComponentInstanceId(TO_INSTANCE_ID);

        final HashMap<String, List<ComponentInstanceAttribute>> map = new HashMap<>();
        map.put(TO_INSTANCE_ID, Arrays.asList(componentInstanceAttribute));
        resource.setComponentInstancesAttributes(map);

        final Either<Object, StorageOperationStatus> right = Either.right(StorageOperationStatus.BAD_REQUEST);
        doReturn(right).when(toscaOperationFacade).getToscaElement(COMPONENT_ID);
        doReturn(ActionStatus.BAD_REQUEST_MISSING_RESOURCE).when(componentsUtils).convertFromStorageResponse(StorageOperationStatus.BAD_REQUEST);

        assertThrows(ByActionStatusComponentException.class, () -> {
            final List<ComponentInstanceAttribute> result = componentInstanceBusinessLogic
                .getComponentInstanceAttributesById(RESOURCE_PARAM_NAME, COMPONENT_ID, TO_INSTANCE_ID, USER_ID);

        });

    }

    @Test
    void testGetComponentInstanceAttributesById_fail_getResourceInstanceById() {
        final ComponentInstanceAttribute componentInstanceAttribute = new ComponentInstanceAttribute();
        componentInstanceAttribute.setComponentInstanceId(TO_INSTANCE_ID);

        final HashMap<String, List<ComponentInstanceAttribute>> map = new HashMap<>();
        map.put(TO_INSTANCE_ID, Arrays.asList(componentInstanceAttribute));
        resource.setComponentInstancesAttributes(map);

        final Either<Component, StorageOperationStatus> leftServiceOp = Either.left(resource);
        doReturn(leftServiceOp).when(toscaOperationFacade).getToscaElement(COMPONENT_ID);
        doReturn(ActionStatus.RESOURCE_INSTANCE_NOT_FOUND_ON_SERVICE).when(componentsUtils).convertFromStorageResponse(StorageOperationStatus.PARENT_RESOURCE_NOT_FOUND);

        assertThrows(ByActionStatusComponentException.class, () -> {
            final List<ComponentInstanceAttribute> result = componentInstanceBusinessLogic
                .getComponentInstanceAttributesById(RESOURCE_PARAM_NAME, COMPONENT_ID, "", USER_ID);

        });

    }

    @Test
    void updateInstanceCapabilitySuccessTest() {
        var containerComponentId = "containerComponentId";
        var componentInstanceUniqueId = "componentInstanceUniqueId";
        var capabilityDefinition = new CapabilityDefinition();
        capabilityDefinition.setUniqueId("uniqueId");

        final Component component = new Service();
        component.setUniqueId(containerComponentId);
        component.setLastUpdaterUserId(USER_ID);
        component.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);

        var componentInstance = new ComponentInstance();
        componentInstance.setUniqueId(componentInstanceUniqueId);
        component.setComponentInstances(Collections.singletonList(componentInstance));

        when(toscaOperationFacade.getToscaFullElement(containerComponentId))
            .thenReturn(Either.left(component));
        when(toscaOperationFacade.updateComponentInstanceCapability(containerComponentId, componentInstanceUniqueId, capabilityDefinition))
            .thenReturn(capabilityDefinition);
        when(toscaOperationFacade.updateComponentInstanceMetadataOfTopologyTemplate(component))
            .thenReturn(Either.left(component));
        when(graphLockOperation.lockComponent(containerComponentId, NodeTypeEnum.Service))
            .thenReturn(StorageOperationStatus.OK);

        final Either<CapabilityDefinition, ResponseFormat> resultEither = componentInstanceBusinessLogic
            .updateInstanceCapability(ComponentTypeEnum.SERVICE, containerComponentId, componentInstanceUniqueId, capabilityDefinition, USER_ID);
        assertTrue(resultEither.isLeft());
        final CapabilityDefinition actualCapabilityDefinition = resultEither.left().value();
        assertNotEquals(capabilityDefinition, actualCapabilityDefinition);
        assertEquals(capabilityDefinition.getUniqueId(), actualCapabilityDefinition.getUniqueId());
    }

    @Test
    void updateInstanceCapabilityNoContainerComponentTypeTest() {
        var responseFormat = new ResponseFormat();
        when(componentsUtils.getResponseFormat(ActionStatus.NOT_ALLOWED)).thenReturn(responseFormat);
        final Either<CapabilityDefinition, ResponseFormat> resultEither = componentInstanceBusinessLogic
            .updateInstanceCapability(null, "containerComponentId", "componentInstanceUniqueId", new CapabilityDefinition(), USER_ID);
        assertTrue(resultEither.isRight(), "Either return should be right");
        final ResponseFormat actualResponseFormat = resultEither.right().value();
        assertEquals(responseFormat, actualResponseFormat);
    }

    @Test
    void updateInstanceCapabilityContainerComponentNotFoundTest() {
        var containerComponentId = "containerComponentId";
        when(toscaOperationFacade.getToscaFullElement(containerComponentId)).thenReturn(Either.right(null));
        var responseFormat = new ResponseFormat();
        when(componentsUtils.getResponseFormat(ActionStatus.COMPONENT_NOT_FOUND, containerComponentId)).thenReturn(responseFormat);
        final Either<CapabilityDefinition, ResponseFormat> resultEither = componentInstanceBusinessLogic
            .updateInstanceCapability(ComponentTypeEnum.SERVICE, "containerComponentId", "componentInstanceUniqueId", new CapabilityDefinition(), USER_ID);
        assertTrue(resultEither.isRight(), "Either return should be right");
        final ResponseFormat actualResponseFormat = resultEither.right().value();
        assertEquals(responseFormat, actualResponseFormat);
    }

    @Test
    void updateInstanceCapabilityCannotWorkOnComponentTest() {
        var containerComponentId = "containerComponentId";
        var componentInstanceUniqueId = "componentInstanceUniqueId";

        final Component component = new Service();
        component.setUniqueId(containerComponentId);
        component.setLastUpdaterUserId("anotherUse");
        component.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);

        var expectedResponseFormat = new ResponseFormat();

        when(toscaOperationFacade.getToscaFullElement(containerComponentId))
            .thenReturn(Either.left(component));
        when(componentsUtils.getResponseFormat(ActionStatus.RESTRICTED_OPERATION))
            .thenReturn(expectedResponseFormat);

        final Either<CapabilityDefinition, ResponseFormat> resultEither = componentInstanceBusinessLogic
            .updateInstanceCapability(ComponentTypeEnum.SERVICE, containerComponentId, componentInstanceUniqueId, new CapabilityDefinition(), USER_ID);
        assertTrue(resultEither.isRight(), "Either return should be right");
        final ResponseFormat actualResponseFormat = resultEither.right().value();
        assertEquals(expectedResponseFormat, actualResponseFormat);
    }

    @Test
    void updateInstanceCapabilityResourceInstanceNotFoundTest() {
        var containerComponentId = "containerComponentId";
        var componentInstanceUniqueId = "componentInstanceUniqueId";

        final Component component = new Service();
        component.setUniqueId(containerComponentId);
        component.setLastUpdaterUserId(USER_ID);
        component.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);

        var expectedResponseFormat = new ResponseFormat();

        when(toscaOperationFacade.getToscaFullElement(containerComponentId))
            .thenReturn(Either.left(component));
        when(componentsUtils.getResponseFormat(ActionStatus.RESOURCE_INSTANCE_NOT_FOUND_ON_SERVICE, componentInstanceUniqueId, containerComponentId))
            .thenReturn(expectedResponseFormat);

        final Either<CapabilityDefinition, ResponseFormat> resultEither = componentInstanceBusinessLogic
            .updateInstanceCapability(ComponentTypeEnum.SERVICE, containerComponentId, componentInstanceUniqueId, new CapabilityDefinition(), USER_ID);
        assertTrue(resultEither.isRight(), "Either return should be right");
        final ResponseFormat actualResponseFormat = resultEither.right().value();
        assertEquals(expectedResponseFormat, actualResponseFormat);
    }

    @Test
    void updateInstanceCapabilityUpdateMetadataFailTest() {
        var containerComponentId = "containerComponentId";
        var componentInstanceUniqueId = "componentInstanceUniqueId";
        var capabilityDefinition = new CapabilityDefinition();
        capabilityDefinition.setUniqueId("uniqueId");

        final Component component = new Service();
        component.setUniqueId(containerComponentId);
        component.setLastUpdaterUserId(USER_ID);
        component.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);

        var componentInstance = new ComponentInstance();
        componentInstance.setUniqueId(componentInstanceUniqueId);
        component.setComponentInstances(Collections.singletonList(componentInstance));

        var expectedResponseFormat = new ResponseFormat();

        when(toscaOperationFacade.getToscaFullElement(containerComponentId))
            .thenReturn(Either.left(component));
        when(graphLockOperation.lockComponent(containerComponentId, NodeTypeEnum.Service))
            .thenReturn(StorageOperationStatus.OK);
        when(toscaOperationFacade.updateComponentInstanceCapability(containerComponentId, componentInstanceUniqueId, capabilityDefinition))
            .thenReturn(capabilityDefinition);
        when(toscaOperationFacade.updateComponentInstanceMetadataOfTopologyTemplate(component))
            .thenReturn(Either.right(StorageOperationStatus.GENERAL_ERROR));
        when(componentsUtils.convertFromStorageResponse(StorageOperationStatus.GENERAL_ERROR, ComponentTypeEnum.SERVICE))
            .thenReturn(ActionStatus.GENERAL_ERROR);
        when(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR))
            .thenReturn(expectedResponseFormat);

        final Either<CapabilityDefinition, ResponseFormat> resultEither = componentInstanceBusinessLogic
            .updateInstanceCapability(ComponentTypeEnum.SERVICE, containerComponentId, componentInstanceUniqueId, capabilityDefinition, USER_ID);
        assertTrue(resultEither.isRight(), "Either return should be right");
        final ResponseFormat actualResponseFormat = resultEither.right().value();
        assertEquals(expectedResponseFormat, actualResponseFormat);
    }

    @Test
    void updateInstanceCapabilityBusinessExceptionHandlingTest() {
        var containerComponentId = "containerComponentId";
        var componentInstanceUniqueId = "componentInstanceUniqueId";
        var capabilityDefinition = new CapabilityDefinition();
        capabilityDefinition.setUniqueId("uniqueId");

        final Component component = new Service();
        component.setUniqueId(containerComponentId);
        component.setLastUpdaterUserId(USER_ID);
        component.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);

        var componentInstance = new ComponentInstance();
        componentInstance.setUniqueId(componentInstanceUniqueId);
        component.setComponentInstances(Collections.singletonList(componentInstance));


        when(toscaOperationFacade.getToscaFullElement(containerComponentId))
            .thenReturn(Either.left(component));
        when(graphLockOperation.lockComponent(containerComponentId, NodeTypeEnum.Service))
            .thenReturn(StorageOperationStatus.OK);
        when(toscaOperationFacade.updateComponentInstanceCapability(containerComponentId, componentInstanceUniqueId, capabilityDefinition))
            .thenThrow(new OperationException(ActionStatus.GENERAL_ERROR));

        final BusinessException businessException = assertThrows(BusinessException.class, () -> {
            componentInstanceBusinessLogic
                .updateInstanceCapability(ComponentTypeEnum.SERVICE, containerComponentId, componentInstanceUniqueId, capabilityDefinition, USER_ID);
        });
        assertTrue(businessException instanceof OperationException);
        assertEquals(ActionStatus.GENERAL_ERROR, ((OperationException) businessException).getActionStatus());
    }

    @Test
    void updateInstanceCapabilityUnknownExceptionHandlingTest() {
        var containerComponentId = "containerComponentId";
        var componentInstanceUniqueId = "componentInstanceUniqueId";
        var capabilityDefinition = new CapabilityDefinition();
        capabilityDefinition.setUniqueId("uniqueId");

        final Component component = new Service();
        component.setUniqueId(containerComponentId);
        component.setLastUpdaterUserId(USER_ID);
        component.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);

        var componentInstance = new ComponentInstance();
        componentInstance.setUniqueId(componentInstanceUniqueId);
        component.setComponentInstances(Collections.singletonList(componentInstance));

        var expectedResponseFormat = new ResponseFormat();

        when(toscaOperationFacade.getToscaFullElement(containerComponentId))
            .thenReturn(Either.left(component));
        when(graphLockOperation.lockComponent(containerComponentId, NodeTypeEnum.Service))
            .thenReturn(StorageOperationStatus.OK);
        when(toscaOperationFacade.updateComponentInstanceCapability(containerComponentId, componentInstanceUniqueId, capabilityDefinition))
            .thenThrow(new RuntimeException());
        when(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR))
            .thenReturn(expectedResponseFormat);

        final Exception exception = assertThrows(BusinessException.class, () ->
            componentInstanceBusinessLogic
                .updateInstanceCapability(ComponentTypeEnum.SERVICE, containerComponentId, componentInstanceUniqueId, capabilityDefinition, USER_ID));
        assertTrue(exception instanceof ByResponseFormatComponentException);
        final ByResponseFormatComponentException actualException = (ByResponseFormatComponentException) exception;
        assertEquals(expectedResponseFormat, actualException.getResponseFormat());
    }

    private ComponentInstance createServiceSubstitutionComponentInstance() {
        final ComponentInstance instanceToBeCreated = new ComponentInstance();
        instanceToBeCreated.setName(COMPONENT_INSTANCE_NAME);
        instanceToBeCreated.setUniqueId(COMPONENT_INSTANCE_ID);
        instanceToBeCreated.setComponentUid(ORIGIN_COMPONENT_ID);
        instanceToBeCreated.setOriginType(OriginTypeEnum.ServiceSubstitution);

        return instanceToBeCreated;
    }
    
    private Service createServiceSubstitutionOriginService() {
        final Service originComponent = new Service();
        originComponent.setLifecycleState(LifecycleStateEnum.CERTIFIED);
        originComponent.setVersion(ORIGIN_COMPONENT_VERSION);
        originComponent.setIcon(ICON_NAME);
        originComponent.setDerivedFromGenericType("org.openecomp.resource.abstract.nodes.service");
        originComponent.setName("myService");
        return originComponent;
    }
    
    private Component createServiceSubstitutionServiceDerivedFromComponent() {
        final Resource component = new Resource();
        component.setLifecycleState(LifecycleStateEnum.CERTIFIED);
        component.setVersion(ORIGIN_COMPONENT_VERSION);
        component.setIcon(ICON_NAME);
        component.setToscaResourceName("org.openecomp.resource.abstract.nodes.service");
        component.setUniqueId("baseComponentId");
        return component;
    }

    private void mockComponentForToscaGetFunctionValidation(final Component component) {
        when(toscaOperationFacade.getToscaElement(component.getUniqueId(), JsonParseFlagEnum.ParseAll))
            .thenReturn(Either.left(component));
        when(graphLockOperation.lockComponent(component.getUniqueId(), NodeTypeEnum.ResourceInstance))
            .thenReturn(StorageOperationStatus.OK);
        when(toscaOperationFacade.updateComponentInstanceMetadataOfTopologyTemplate(component))
            .thenReturn(Either.left(component));
        when(janusGraphDao.commit()).thenReturn(JanusGraphOperationStatus.OK);
        when(graphLockOperation.unlockComponent(component.getUniqueId(), NodeTypeEnum.ResourceInstance))
            .thenReturn(StorageOperationStatus.OK);
    }

    private ToscaGetFunctionDataDefinition createGetToscaFunction(final String propertyName, final String propertyUniqueId,
                                                                  final List<String> propertyPathFromSource,
                                                                  final PropertySource propertySource, final ToscaGetFunctionType functionType,
                                                                  final String sourceUniqueId,
                                                                  final String sourceName) {
        final var toscaGetFunction = new ToscaGetFunctionDataDefinition();
        toscaGetFunction.setFunctionType(functionType);
        toscaGetFunction.setPropertyUniqueId(propertyUniqueId);
        toscaGetFunction.setPropertyName(propertyName);
        toscaGetFunction.setPropertyPathFromSource(propertyPathFromSource);
        toscaGetFunction.setPropertySource(propertySource);
        toscaGetFunction.setSourceName(sourceName);
        toscaGetFunction.setSourceUniqueId(sourceUniqueId);
        return toscaGetFunction;
    }

    private SchemaDefinition createSchema(final String schemaType) {
        final var schemaDefinition = new SchemaDefinition();
        final var schemaProperty = new PropertyDefinition();
        schemaProperty.setType(schemaType);
        schemaDefinition.setProperty(schemaProperty);
        return schemaDefinition;
    }

}

