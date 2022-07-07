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
 * Modifications copyright (c) 2020, Nordix Foundation
 * ================================================================================
 */

package org.openecomp.sdc.be.tosca;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum.VF;
import static org.openecomp.sdc.be.tosca.PropertyConvertor.PropertyType.PROPERTY;

import fj.data.Either;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import mockit.Deencapsulation;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Triple;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.be.components.utils.PropertyDataDefinitionBuilder;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.RequirementDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ToscaArtifactDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.OriginTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.exception.ToscaExportException;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.AttributeDefinition;
import org.openecomp.sdc.be.model.CapabilityDefinition;
import org.openecomp.sdc.be.model.CapabilityRequirementRelationship;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.ComponentInstanceAttribute;
import org.openecomp.sdc.be.model.ComponentInstanceInput;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.ComponentParametersView;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.GroupDefinition;
import org.openecomp.sdc.be.model.GroupInstance;
import org.openecomp.sdc.be.model.InputDefinition;
import org.openecomp.sdc.be.model.InterfaceDefinition;
import org.openecomp.sdc.be.model.OutputDefinition;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.RelationshipInfo;
import org.openecomp.sdc.be.model.RequirementCapabilityRelDef;
import org.openecomp.sdc.be.model.RequirementDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.cache.ApplicationDataTypeCache;
import org.openecomp.sdc.be.model.category.CategoryDefinition;
import org.openecomp.sdc.be.model.category.SubCategoryDefinition;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.InterfaceLifecycleOperation;
import org.openecomp.sdc.be.tosca.exception.ToscaConversionException;
import org.openecomp.sdc.be.tosca.model.ToscaCapability;
import org.openecomp.sdc.be.tosca.model.ToscaNodeTemplate;
import org.openecomp.sdc.be.tosca.model.ToscaNodeType;
import org.openecomp.sdc.be.tosca.model.ToscaProperty;
import org.openecomp.sdc.be.tosca.model.ToscaTemplate;
import org.openecomp.sdc.be.tosca.model.ToscaTemplateArtifact;
import org.openecomp.sdc.be.tosca.model.ToscaTemplateRequirement;
import org.openecomp.sdc.be.tosca.model.ToscaTopolgyTemplate;
import org.openecomp.sdc.be.tosca.utils.InputConverter;
import org.openecomp.sdc.be.tosca.utils.OutputConverter;
import org.openecomp.sdc.common.test.BaseConfDependent;

class ToscaExportHandlerTest extends BaseConfDependent {

    private static final String COMPONENT_PROPERTY_NAME = "prop1";
    private static final String COMPONENT_PROPERTY_TYPE = "string";
    private static final String COMPONENT_INPUT_NAME = "input1";
    private static final String COMPONENT_OUTPUT_NAME = "output1";
    private static final String COMPONENT_INPUT_TYPE = "integer";
    private static final String COMPONENT_OUTPUT_TYPE = "integer";
    private static final String RESOURCE_NAME = "resource";
    private static final String TOSCA_VERSION = "tosca_simple_yaml_1_1";
    private static final Map<String, DataTypeDefinition> DATA_TYPES = new HashMap<>();

    @InjectMocks
    private ToscaExportHandler testSubject;

    @Mock
    private ApplicationDataTypeCache applicationDataTypeCache;

    @Mock
    private ToscaOperationFacade toscaOperationFacade;

    @Mock
    private CapabilityRequirementConverter capabilityRequirementConverter;

    @Mock
    private InputConverter inputConverter;

    @Mock
    private OutputConverter outputConverter;

    @Mock
    private GroupExportParser groupExportParser;

    @Mock
    private PropertyConvertor propertyConvertor;

    @Mock
    private GroupExportParserImpl groupExportParserImpl;

    @Mock
    private InterfaceLifecycleOperation interfaceLifecycleOperation;

    @Mock
    private InterfacesOperationsConverter interfacesOperationsConverter;

    @Mock
    private PolicyExportParser policyExportParser;

    @Mock
    private AttributeConverter attributeConverter;

    @BeforeAll
    public static void setupBeforeClass() {
        componentName = "catalog-be";
        confPath = "src/test/resources/config";
        setUp();
    }

    @BeforeEach
    void setUpMock() {
        MockitoAnnotations.openMocks(this);
        doReturn(new ToscaProperty()).when(propertyConvertor).convertProperty(any(), any(), eq(PROPERTY));
        doReturn(new HashMap<String, Object>()).when(interfacesOperationsConverter)
            .getInterfacesMap(any(), isNull(), anyMap(), anyMap(), anyBoolean(), anyBoolean());
    }

    private Resource getNewResource() {
        Resource resource = new Resource();
        resource.setUniqueId("resourceUniqueId");
        List<CategoryDefinition> categories = new ArrayList<>();
        CategoryDefinition category = new CategoryDefinition();
        List<SubCategoryDefinition> subcategories = new ArrayList<>();
        SubCategoryDefinition subcategory = new SubCategoryDefinition();
        List<DataTypeDefinition> dataTypes = new ArrayList<>();
        DataTypeDefinition dataType = new DataTypeDefinition();
        dataType.setName("dataTypeName");
        dataType.setDerivedFromName("tosca.datatypes.Root");
        PropertyDataDefinition propData = new PropertyDataDefinitionBuilder()
            .setName("property")
            .setType("type")
            .build();
        List<PropertyDataDefinition> propDataList = Arrays.asList(propData);
        dataType.setPropertiesData(propDataList);
        List<PropertyDefinition> propList = propDataList.stream().map(PropertyDefinition::new)
            .collect(Collectors.toList());
        dataType.setProperties(propList);
        dataTypes.add(dataType);

        subcategory.setName("name");
        subcategories.add(subcategory);
        category.setName("name");
        category.setSubcategories(subcategories);
        categories.add(category);

        final List<PropertyDefinition> properties = new ArrayList<>();
        properties.add(new PropertyDefinition());
        resource.setProperties(properties);
        Map<String, InterfaceDefinition> proxyInterfaces = new HashMap<>();
        proxyInterfaces.put("Local", new InterfaceDefinition("Local", "desc", new HashMap<>()));
        resource.setInterfaces(proxyInterfaces);

        resource.setCategories(categories);
        resource.setVersion("version");
        resource.setVendorName("vendorName");
        resource.setVendorRelease("vendorRelease");
        resource.setResourceVendorModelNumber("resourceVendorModelNumber");
        resource.setDataTypes(dataTypes);

        return resource;
    }

    private Service getNewService() {
        Service service = new Service();
        List<CategoryDefinition> categories = new ArrayList<>();
        CategoryDefinition category = new CategoryDefinition();
        List<SubCategoryDefinition> subcategories = new ArrayList<>();
        SubCategoryDefinition subcategory = new SubCategoryDefinition();

        subcategory.setName("name");
        subcategories.add(subcategory);
        category.setName("name");
        category.setSubcategories(subcategories);
        categories.add(category);

        service.setCategories(categories);
        service.setComponentType(ComponentTypeEnum.SERVICE);
        service.setServiceType("serviceType");
        service.setServiceRole("serviceRole");
        service.setEnvironmentContext("environmentContext");

        return service;
    }

    @Test
    void testExportComponent() {
        Component component = getNewResource();
        Either<ToscaRepresentation, ToscaError> result;

        when(applicationDataTypeCache.getAll(null)).thenReturn(Either.left(new HashMap<>()));
        when(capabilityRequirementConverter.convertRequirements(anyMap(), any(Resource.class),
            any(ToscaNodeType.class))).thenReturn(Either.left(new ToscaNodeType()));
        when(interfaceLifecycleOperation.getAllInterfaceLifecycleTypes(any()))
            .thenReturn(Either.left(Collections.emptyMap()));

        // default test when component is Resource
        result = testSubject.exportComponent(component);
        assertNotNull(result);

        component = getNewService();
        when(capabilityRequirementConverter.convertRequirements(anyMap(), any(Service.class),
            any(ToscaNodeType.class))).thenReturn(Either.left(new ToscaNodeType()));
        when(applicationDataTypeCache.getAll(null)).thenReturn(Either.right(JanusGraphOperationStatus.NOT_FOUND));

        // default test when component is Service
        result = testSubject.exportComponent(component);
        assertNotNull(result);
    }

    @Test
    void testExportComponentInterface() {
        Resource component = getNewResource();
        Either<ToscaRepresentation, ToscaError> result;

        component.setInterfaces(new HashMap<>());

        when(applicationDataTypeCache.getAll(null)).thenReturn(Either.right(JanusGraphOperationStatus.NOT_FOUND));
        when(interfaceLifecycleOperation.getAllInterfaceLifecycleTypes(any()))
            .thenReturn(Either.left(Collections.emptyMap()));
        // default test when convertInterfaceNodeType is right
        result = testSubject.exportComponentInterface(component, false);
        assertNotNull(result);

        when(applicationDataTypeCache.getAll(null)).thenReturn(Either.left(new HashMap<>()));
        when(capabilityRequirementConverter.convertRequirements(anyMap(), any(Resource.class),
            any(ToscaNodeType.class))).thenReturn(Either.left(new ToscaNodeType()));

        // default test when convertInterfaceNodeType is left
        result = testSubject.exportComponentInterface(component, false);
        assertNotNull(result);
    }

    @Test
    void testConvertInterfaceNodeTypeProperties() {

        Resource component = getNewResource();

        component.setInterfaces(new HashMap<>());
        InputDefinition input = new InputDefinition();
        input.setName(COMPONENT_INPUT_NAME);
        input.setType(COMPONENT_INPUT_TYPE);
        component.setInputs(Collections.singletonList(input));
        PropertyDefinition property = new PropertyDefinition();
        property.setName(COMPONENT_PROPERTY_NAME);
        property.setType(COMPONENT_PROPERTY_TYPE);
        component.setProperties(Collections.singletonList(property));
        component.setName(RESOURCE_NAME);
        component.setToscaResourceName(RESOURCE_NAME);

        when(interfaceLifecycleOperation.getAllInterfaceLifecycleTypes(any()))
            .thenReturn(Either.left(Collections.emptyMap()));
        when(applicationDataTypeCache.getAll(null)).thenReturn(Either.left(new HashMap<>()));
        // when convertRequirements is called, make it return the same value as 3rd (index=2) argument.
        when(capabilityRequirementConverter.convertRequirements(anyMap(), any(Resource.class),
            any(ToscaNodeType.class))).thenAnswer(i -> Either.left(i.getArgument(2)));

        final var result = testSubject.convertInterfaceNodeType(new HashMap<String, Component>(), component,
            new ToscaTemplate(TOSCA_VERSION), new HashMap<String, ToscaNodeType>(), false);
        assertNotNull(result);
        assertTrue(result.isLeft());
        Map<String, ToscaNodeType> nodeTypeMap = result.left().value().getNode_types();
        assertNotNull(nodeTypeMap);
        assertEquals(1, nodeTypeMap.size());
        ToscaNodeType toscaNodeType = nodeTypeMap.values().iterator().next();
        assertNotNull(toscaNodeType);
        Map<String, ToscaProperty> propertyMap = toscaNodeType.getProperties();
        // Check if inputs and properties in component are merged properly
        assertNotNull(propertyMap);
        assertEquals(1, propertyMap.size());
        assertFalse(propertyMap.containsKey(COMPONENT_INPUT_NAME));
        assertTrue(propertyMap.containsKey(COMPONENT_PROPERTY_NAME));
    }

    @Test
    void testGetDependencies() {

        Component component = new Resource();
        Either<ToscaTemplate, ToscaError> result;

        // default test
        result = testSubject.getDependencies(component);
        assertNotNull(result);
    }

    @Test
    void testSetImports() {
        Resource resource = new Resource();
        resource.setResourceType(ResourceTypeEnum.PNF);

        resource.setName("TestResourceName");
        Map<String, ArtifactDefinition> artifactList = new HashMap<>();
        ArtifactDefinition artifact = new ArtifactDefinition();
        artifact.setArtifactName("name.name2");
        artifactList.put("assettoscatemplate", artifact);
        resource.setArtifacts(artifactList);
        resource.setToscaArtifacts(artifactList);
        ToscaTemplate toscaTemplate = new ToscaTemplate("");

        ComponentInstance ci = new ComponentInstance();
        ci.setComponentUid("name");
        ci.setOriginType(OriginTypeEnum.PNF);
        ci.setSourceModelUid("modelName");
        List<ComponentInstance> componentInstanceList = new LinkedList<>();
        componentInstanceList.add(ci);
        resource.setComponentInstances(componentInstanceList);

        when(toscaOperationFacade.getToscaFullElement("name")).thenReturn(Either.left(resource));

        Either<ImmutablePair<ToscaTemplate, Map<String, Component>>, ToscaError> result;
        result = Deencapsulation.invoke(testSubject, "fillImports", resource, toscaTemplate);

        verify(toscaOperationFacade, times(1)).getToscaFullElement("name");
        assertTrue(result.isLeft());
        ToscaTemplate toscaTemplateRes = result.left().value().left;
        assertEquals(8, toscaTemplateRes.getImports().size());
        assertNotNull(toscaTemplateRes.getImports().get(6).get("resource-TestResourceName-interface"));
        assertNotNull(toscaTemplateRes.getImports().get(7).get("resource-TestResourceName"));
        assertEquals(1, toscaTemplateRes.getDependencies().size());
        assertEquals("name.name2", toscaTemplateRes.getDependencies().get(0).getLeft());
    }

    @Test
    void testConvertToscaTemplate() throws Exception {

        final Component component = getNewResource();
        final ToscaTemplate toscaNode = new ToscaTemplate("");
        Either<ToscaTemplate, ToscaError> result;
        final List<ComponentInstance> resourceInstances = new ArrayList<>();
        final ComponentInstance instance = new ComponentInstance();

        instance.setOriginType(OriginTypeEnum.SERVICE);
        instance.setSourceModelUid("targetModelUid");
        resourceInstances.add(instance);

        component.setComponentInstances(resourceInstances);

        when(applicationDataTypeCache.getAll(null)).thenReturn(Either.left(new HashMap<>()));
        when(capabilityRequirementConverter.getOriginComponent(anyMap(), any(ComponentInstance.class))).thenReturn(Either.right(false));

        final Map<String, ToscaProperty> map = new HashMap<>();
        map.put("mock", new ToscaProperty());
        doReturn(map).when(outputConverter).convert(any(), any());

        // default test
        result = Deencapsulation.invoke(testSubject, "convertToscaTemplate", component, toscaNode);
        assertNotNull(result);
    }

    @Test
    void testConvertToscaTemplateWhenComponentContainsGroup() throws ToscaConversionException {
        Component component = getNewResource();
        ToscaTemplate toscaNode = new ToscaTemplate("");
        component.setComponentInstances(new ArrayList<>());

        List<GroupDefinition> groups = new ArrayList<>();
        GroupDefinition group = new GroupDefinition();
        List<String> artifacts = new ArrayList<>();
        artifacts.add("artifact");
        group.setType("org.openecomp.groups.VfModule");
        group.setArtifacts(artifacts);
        groups.add(group);
        component.setGroups(groups);

        final var input = new InputDefinition();
        input.setName(COMPONENT_INPUT_NAME);
        input.setType(COMPONENT_INPUT_TYPE);
        component.setInputs(Collections.singletonList(input));

        final var output = new OutputDefinition();
        output.setName(COMPONENT_OUTPUT_NAME);
        output.setType(COMPONENT_OUTPUT_TYPE);
        component.setOutputs(Collections.singletonList(output));

        Map<String, String[]> substitutionMappingMap = new HashMap<>();
        String[] array = {"value1", "value2"};
        substitutionMappingMap.put("key", array);

        when(capabilityRequirementConverter.convertSubstitutionMappingCapabilities(anyMap(),
            any(Component.class))).thenReturn(Either.left(substitutionMappingMap));

        when(capabilityRequirementConverter.convertSubstitutionMappingRequirements(any(Component.class), anyMap()))
            .thenReturn(Either.left(Collections.emptyMap()));

        when(applicationDataTypeCache.getAll(null)).thenReturn(Either.left(new HashMap<>()));

        when(inputConverter.convertInputs(anyList(), anyMap())).thenReturn(new HashMap<>());

        when(groupExportParser.getGroups(component)).thenReturn(null);

        final Map<String, ToscaProperty> map = new HashMap<>();
        map.put("mock", new ToscaProperty());
        doReturn(map).when(outputConverter).convert(any(), any());

        // test component contains group
        final var result = Deencapsulation.invoke(testSubject, "convertToscaTemplate", component, toscaNode);
        assertNotNull(result);
    }

    @Test
    void testConvertToscaTemplateWhenComponentIsService() throws Exception {
        Component component = getNewService();
        ToscaTemplate toscaNode = new ToscaTemplate("");
        Either<ToscaTemplate, ToscaError> result;
        component.setComponentInstances(new ArrayList<>());

        List<GroupDefinition> groups = new ArrayList<>();
        GroupDefinition group = new GroupDefinition();
        List<String> artifacts = new ArrayList<>();
        artifacts.add("artifact");
        group.setType("org.openecomp.groups.VfModule");
        group.setArtifacts(artifacts);
        groups.add(group);
        component.setGroups(groups);

        Map<String, String[]> substitutionMappingMap = new HashMap<>();
        String[] array = {"value1", "value2"};
        substitutionMappingMap.put("key", array);

        when(capabilityRequirementConverter.convertSubstitutionMappingCapabilities(anyMap(), any(Component.class)))
            .thenReturn(Either.left(substitutionMappingMap));

        when(capabilityRequirementConverter
            .convertSubstitutionMappingRequirements(any(Component.class), anyMap()))
            .thenReturn(Either.left(Collections.emptyMap()));

        when(applicationDataTypeCache.getAll(null)).thenReturn(Either.left(new HashMap<>()));

        when(inputConverter.convertInputs(anyList(), anyMap())).thenReturn(new HashMap<>());
        final Map<String, ToscaProperty> map = new HashMap<>();
        map.put("mock", new ToscaProperty());
        doReturn(map).when(outputConverter).convert(any(), any());

        // test component contains group
        result = Deencapsulation.invoke(testSubject, "convertToscaTemplate", component, toscaNode);
        assertNotNull(result);
    }

    @Test
    void testConvertMetadata_1() {

        Component component = getNewResource();
        boolean isInstance = true;
        ComponentInstance componentInstance = new ComponentInstance();
        componentInstance.setOriginType(OriginTypeEnum.ServiceProxy);
        componentInstance.setSourceModelInvariant("targetModelInvariant");

        // default test
        Map<String, String> result = Deencapsulation.invoke(testSubject, "convertMetadata", component, isInstance, componentInstance);
        assertNotNull(result);
    }

    @Test
    void testFillImports() {

        Component component = getNewService();
        ToscaTemplate toscaTemplate = new ToscaTemplate("");
        Either<ImmutablePair<ToscaTemplate, Map<String, Component>>, ToscaError> result;

        ComponentInstance instance = new ComponentInstance();
        List<ComponentInstance> resourceInstances = new ArrayList<>();
        instance.setComponentUid("name");
        resourceInstances.add(instance);
        component.setComponentInstances(resourceInstances);
        Map<String, ArtifactDefinition> toscaArtifacts = new HashMap<>();
        ArtifactDefinition artifact = new ArtifactDefinition();
        artifact.setArtifactName("name.name2");
        toscaArtifacts.put("assettoscatemplate", artifact);
        component.setToscaArtifacts(toscaArtifacts);

        when(toscaOperationFacade.getToscaFullElement(any(String.class)))
            .thenReturn(Either.left(component));

        Resource baseType = getNewResource();
        Map<String, ArtifactDefinition> baseTypeToscaArtifacts = new HashMap<>();
        ArtifactDefinition baseTypeArtifact = new ArtifactDefinition();
        baseTypeArtifact.setArtifactName("typeA");
        baseTypeToscaArtifacts.put("assettoscatemplate", baseTypeArtifact);
        baseType.setToscaArtifacts(baseTypeToscaArtifacts);

        component.setDerivedFromGenericType("org.typeA");
        component.setDerivedFromGenericVersion("1.0");
        when(toscaOperationFacade.getByToscaResourceNameAndVersion("org.typeA", "1.0", null)).thenReturn(Either.left(baseType));

        // default test
        result = Deencapsulation.invoke(testSubject, "fillImports", component, toscaTemplate);
        assertNotNull(result);
    }

    @Test
    void testCreateDependency() {

        Map<String, Component> componentCache = new HashMap<>();
        List<Map<String, Map<String, String>>> imports = new ArrayList<>();
        List<Triple<String, String, Component>> dependecies = new ArrayList<>();
        ComponentInstance ci = new ComponentInstance();
        Component component = getNewResource();

        Map<String, ArtifactDefinition> toscaArtifacts = new HashMap<>();
        ArtifactDefinition artifact = new ArtifactDefinition();
        artifact.setArtifactName("name.name2");
        toscaArtifacts.put("assettoscatemplate", artifact);
        component.setToscaArtifacts(toscaArtifacts);
        ci.setComponentUid("name");
        ci.setOriginType(OriginTypeEnum.ServiceProxy);
        ci.setSourceModelUid("modelName");

        when(toscaOperationFacade.getToscaFullElement("name")).thenReturn(Either.left(component));

        when(toscaOperationFacade.getToscaFullElement("modelName")).thenReturn(Either.left(new Service()));

        // default test
        Deencapsulation.invoke(testSubject, "createDependency", componentCache, imports, dependecies, ci);
        assertFalse(componentCache.isEmpty());
    }

    @Test
    void testGetInterfaceFilename() {
        String artifactName = "artifact.name";
        String result;

        // default test
        result = ToscaExportHandler.getInterfaceFilename(artifactName);
        assertNotNull(result);
    }

    @Test
    void testConvertNodeType() {
        Component component = new Resource();
        ToscaTemplate toscaNode = new ToscaTemplate("");
        Map<String, ToscaNodeType> nodeTypes = new HashMap<>();
        Either<ToscaTemplate, ToscaError> result;

        when(applicationDataTypeCache.getAll(null)).thenReturn(Either.right(JanusGraphOperationStatus.ALREADY_EXIST));
        when(interfaceLifecycleOperation.getAllInterfaceLifecycleTypes(any()))
            .thenReturn(Either.left(Collections.emptyMap()));
        // default test
        result = Deencapsulation
            .invoke(testSubject, "convertNodeType", new HashMap<>(), component, toscaNode, nodeTypes);
        assertNotNull(result);
    }

    @Test
    void testConvertInterfaceNodeType() {
        Component component = getNewResource();
        ToscaTemplate toscaNode = new ToscaTemplate("");
        Map<String, ToscaNodeType> nodeTypes = new HashMap<>();
        Either<ToscaTemplate, ToscaError> result;
        List<InputDefinition> inputs = new ArrayList<>();
        inputs.add(new InputDefinition());
        component.setInputs(inputs);

        when(applicationDataTypeCache.getAll(null)).thenReturn(Either.left(new HashMap<>()));
        when(interfaceLifecycleOperation.getAllInterfaceLifecycleTypes(any()))
            .thenReturn(Either.left(Collections.emptyMap()));

        when(capabilityRequirementConverter.convertRequirements(anyMap(), any(Resource.class),
            any(ToscaNodeType.class))).thenReturn(Either.left(new ToscaNodeType()));

        // default test
        result = Deencapsulation.invoke(testSubject, "convertInterfaceNodeType", new HashMap<>(), component, toscaNode
            , nodeTypes, false);
        assertNotNull(result);
    }

    @Test
    void testConvertReqCapAndTypeName() {
        Component component = new Resource();
        ToscaTemplate toscaNode = new ToscaTemplate("");
        Map<String, ToscaNodeType> nodeTypes = new HashMap<>();
        ToscaNodeType toscaNodeType = new ToscaNodeType();
        Map<String, DataTypeDefinition> dataTypes = new HashMap<>();
        Either<ToscaTemplate, ToscaError> result;

        when(
            capabilityRequirementConverter
                .convertCapabilities(anyMap(), any(Resource.class), anyMap()))
            .thenReturn(new HashMap<>());

        when(capabilityRequirementConverter.convertRequirements(anyMap(), any(Resource.class),
            any(ToscaNodeType.class))).thenReturn(Either.left(new ToscaNodeType()));

        // default test
        result = Deencapsulation
            .invoke(testSubject, "convertReqCapAndTypeName", new HashMap<>(), component, toscaNode, nodeTypes,
                toscaNodeType, dataTypes);
        assertNotNull(result);

        component = new Service();

        when(capabilityRequirementConverter.convertRequirements(anyMap(), any(Service.class),
            any(ToscaNodeType.class))).thenReturn(Either.left(new ToscaNodeType()));

        // test when component is service
        result = Deencapsulation
            .invoke(testSubject, "convertReqCapAndTypeName", new HashMap<>(), component, toscaNode, nodeTypes,
                toscaNodeType, dataTypes);
        assertNotNull(result);
    }

    @Test
    void testConvertNodeTemplatesWhenComponentIsService() {
        final Component component = getNewService();
        final List<ComponentInstance> componentInstances = new ArrayList<>();
        final Map<String, List<ComponentInstanceProperty>> componentInstancesProperties = new HashMap<>();
        final Map<String, Component> componentCache = new HashMap<>();
        final Map<String, DataTypeDefinition> dataTypes = new HashMap<>();
        final ToscaTopolgyTemplate topologyTemplate = new ToscaTopolgyTemplate();
        final Either<Map<String, ToscaNodeTemplate>, ToscaError> result;
        final Map<String, List<ComponentInstanceInput>> componentInstancesInputs = new HashMap<>();
        final List<ComponentInstanceInput> inputs = new ArrayList<>();
        final ComponentInstanceInput componentInstanceInput = new ComponentInstanceInput();
        componentInstanceInput.setUniqueId("uuid");
        inputs.add(componentInstanceInput);
        componentInstancesInputs.put("uuid", inputs);
        final List<RequirementCapabilityRelDef> resourceInstancesRelations = new ArrayList<>();
        final RequirementCapabilityRelDef reldef = new RequirementCapabilityRelDef();
        reldef.setFromNode("node");
        resourceInstancesRelations.add(reldef);
        component.setComponentInstancesRelations(resourceInstancesRelations);

        final ComponentInstance instance = new ComponentInstance();
        instance.setUniqueId("id");
        instance.setComponentUid("uid");
        instance.setOriginType(OriginTypeEnum.ServiceProxy);
        final List<GroupInstance> groupInstances = new ArrayList<>();
        final GroupInstance groupInst = new GroupInstance();
        final List<String> artifacts = new ArrayList<>();
        artifacts.add("artifact");
        groupInst.setArtifacts(artifacts);
        groupInst.setType("type");
        groupInstances.add(groupInst);
        instance.setGroupInstances(groupInstances);

        final List<PropertyDefinition> properties = new ArrayList<>();
        properties.add(new PropertyDefinition());
        instance.setProperties(properties);

        instance.setUniqueId("uuid");
        instance.setDescription("desc");
        instance.setSourceModelUid("sourceModelUid");

        componentInstances.add(instance);

        component.setComponentInstances(componentInstances);

        component.setComponentInstancesInputs(componentInstancesInputs);
        component.setInvariantUUID("uuid");
        component.setUUID("uuid");
        component.setDescription("desc");
        component.setUniqueId("uid");

        componentCache.put("uid", component);

        final List<ComponentInstanceProperty> componentInstanceProperties = new ArrayList<>();
        componentInstanceProperties.add(new ComponentInstanceProperty());

        componentInstancesProperties.put("uuid", componentInstanceProperties);
        component.setComponentInstancesProperties(componentInstancesProperties);

        final Map<String, List<ComponentInstanceAttribute>> componentInstancesAttributes = new HashMap<>();
        final List<ComponentInstanceAttribute> componentInstanceAttributes = new ArrayList<>();
        final ComponentInstanceAttribute componentInstanceAttribute = new ComponentInstanceAttribute();
        componentInstanceAttribute.setDefaultValue("def value");
        componentInstanceAttributes.add(componentInstanceAttribute);

        componentInstancesAttributes.put("uuid", componentInstanceAttributes);
        component.setComponentInstancesAttributes(componentInstancesAttributes);

        ComponentInstanceProperty cip = new ComponentInstanceProperty();
        cip.setInstanceUniqueId("id");

        List<ComponentInstanceProperty> list = new ArrayList<>();
        list.add(cip);

        componentInstancesProperties.put("id", list);
        component.setComponentInstancesProperties(componentInstancesProperties);

        when(capabilityRequirementConverter.getOriginComponent(anyMap(), any(ComponentInstance.class))).thenReturn(Either.left(component));
        when(capabilityRequirementConverter
            .convertComponentInstanceCapabilities(any(ComponentInstance.class), anyMap(), any(ToscaNodeTemplate.class)))
            .thenReturn(Either.left(new ToscaNodeTemplate()));
        when(interfaceLifecycleOperation.getAllInterfaceLifecycleTypes(any())).thenReturn(Either.left(Collections.emptyMap()));
        when(applicationDataTypeCache.getAll(null)).thenReturn(Either.left(new HashMap<>()));
        when(capabilityRequirementConverter.convertRequirements(anyMap(), any(Resource.class), any(ToscaNodeType.class)))
            .thenReturn(Either.left(new ToscaNodeType()));
        when(toscaOperationFacade.getToscaFullElement("uid")).thenReturn(Either.left(component));
        when(toscaOperationFacade.getToscaFullElement("sourceModelUid")).thenReturn(Either.left(component));
        when(toscaOperationFacade.getLatestByName("serviceProxy", null)).thenReturn(Either.left(new Resource()));
        when(toscaOperationFacade.getToscaElement(any(String.class), any(ComponentParametersView.class))).thenReturn(Either.left(getNewResource()));

        final Map<String, String[]> substitutionMappingMap = new HashMap<>();
        final String[] array = {"value1", "value2"};
        substitutionMappingMap.put("key", array);
        when(capabilityRequirementConverter.convertSubstitutionMappingCapabilities(anyMap(), any(Component.class)))
            .thenReturn(Either.left(substitutionMappingMap));

        when(capabilityRequirementConverter
            .convertSubstitutionMappingRequirements(any(Component.class), anyMap()))
            .thenReturn(Either.left(Collections.emptyMap()));

        // default test
        final Either<ToscaRepresentation, ToscaError> toscaRepresentationToscaErrorEither = testSubject.exportComponent(component);
        assertNotNull(toscaRepresentationToscaErrorEither);

    }

    @Test
    void testConvertNodeTemplatesWhenComponentIsResource() {
        final Resource component = getNewResource();
        component.setResourceType(VF);
        final List<ComponentInstance> componentInstances = new ArrayList<>();
        final Map<String, List<ComponentInstanceProperty>> componentInstancesProperties = new HashMap<>();
        final Map<String, Component> componentCache = new HashMap<>();
        final Map<String, DataTypeDefinition> dataTypes = new HashMap<>();
        final ToscaTopolgyTemplate topologyTemplate = new ToscaTopolgyTemplate();
        final Either<Map<String, ToscaNodeTemplate>, ToscaError> result;
        final Map<String, List<ComponentInstanceInput>> componentInstancesInputs = new HashMap<>();
        final List<ComponentInstanceInput> inputs = new ArrayList<>();
        final ComponentInstanceInput componentInstanceInput = new ComponentInstanceInput();
        componentInstanceInput.setUniqueId("uuid");
        inputs.add(componentInstanceInput);
        componentInstancesInputs.put("uuid", inputs);
        final List<RequirementCapabilityRelDef> resourceInstancesRelations = new ArrayList<>();
        final RequirementCapabilityRelDef reldef = new RequirementCapabilityRelDef();
        reldef.setFromNode("node");
        resourceInstancesRelations.add(reldef);
        component.setComponentInstancesRelations(resourceInstancesRelations);

        final ComponentInstance instance = new ComponentInstance();
        instance.setUniqueId("id");
        instance.setComponentUid("uid");
        instance.setOriginType(OriginTypeEnum.VFC);
        final List<GroupInstance> groupInstances = new ArrayList<>();
        final GroupInstance groupInst = new GroupInstance();
        final List<String> artifacts = new ArrayList<>();
        artifacts.add("artifact");
        groupInst.setArtifacts(artifacts);
        groupInst.setType("type");
        groupInstances.add(groupInst);
        instance.setGroupInstances(groupInstances);

        final List<PropertyDefinition> properties = new ArrayList<>();
        properties.add(new PropertyDefinition());
        instance.setProperties(properties);
        component.setProperties(properties);

        instance.setUniqueId("uuid");
        instance.setDescription("desc");
        instance.setSourceModelUid("sourceModelUid");
        final Map<String, ArtifactDefinition> artifactList = new HashMap<>();
        final ArtifactDefinition artifact = new ArtifactDefinition();
        artifact.setArtifactName("name.name2");
        artifactList.put("assettoscatemplate", artifact);
        instance.setArtifacts(artifactList);

        Map<String, ToscaArtifactDataDefinition> toscaArtifactDataDefinitionMap = new HashMap<>();
        toscaArtifactDataDefinitionMap.put("assettoscatemplate", new ToscaArtifactDataDefinition());
        instance.setToscaArtifacts(toscaArtifactDataDefinitionMap);

        componentInstances.add(instance);

        component.setComponentInstances(componentInstances);

        component.setComponentInstancesInputs(componentInstancesInputs);
        component.setInvariantUUID("uuid");
        component.setUUID("uuid");
        component.setDescription("desc");
        component.setUniqueId("uid");

        componentCache.put("uid", component);

        final List<ComponentInstanceProperty> componentInstanceProperties = new ArrayList<>();
        componentInstanceProperties.add(new ComponentInstanceProperty());

        componentInstancesProperties.put("uuid", componentInstanceProperties);
        component.setComponentInstancesProperties(componentInstancesProperties);

        final Map<String, List<ComponentInstanceAttribute>> componentInstancesAttributes = new HashMap<>();
        final List<ComponentInstanceAttribute> componentInstanceAttributes = new ArrayList<>();
        final ComponentInstanceAttribute componentInstanceAttribute = new ComponentInstanceAttribute();
        componentInstanceAttribute.setDefaultValue("def value");
        componentInstanceAttributes.add(componentInstanceAttribute);

        componentInstancesAttributes.put("uuid", componentInstanceAttributes);
        component.setComponentInstancesAttributes(componentInstancesAttributes);

        component.setArtifacts(artifactList);
        component.setToscaArtifacts(artifactList);

        final List<AttributeDefinition> attributes = new ArrayList<>();
        final var attribute = new AttributeDefinition();
        attribute.setName("mock");
        attributes.add(attribute);
        component.setAttributes(attributes);

        List<ComponentInstanceInput> componentInstanceInputs = new ArrayList<>();
        componentInstanceInputs.add(new ComponentInstanceInput());

        componentInstancesInputs.put("id", componentInstanceInputs);
        component.setComponentInstancesInputs(componentInstancesInputs);

        when(capabilityRequirementConverter.getOriginComponent(anyMap(), any(ComponentInstance.class))).thenReturn(Either.left(component));
        when(capabilityRequirementConverter
            .convertComponentInstanceCapabilities(any(ComponentInstance.class), anyMap(), any(ToscaNodeTemplate.class)))
            .thenReturn(Either.left(new ToscaNodeTemplate()));
        when(interfaceLifecycleOperation.getAllInterfaceLifecycleTypes(any())).thenReturn(Either.left(Collections.emptyMap()));
        when(applicationDataTypeCache.getAll(null)).thenReturn(Either.left(new HashMap<>()));
        when(capabilityRequirementConverter.convertRequirements(anyMap(), any(Resource.class), any(ToscaNodeType.class)))
            .thenReturn(Either.left(new ToscaNodeType()));
        when(toscaOperationFacade.getToscaFullElement("uid")).thenReturn(Either.left(component));
        when(toscaOperationFacade.getToscaFullElement("sourceModelUid")).thenReturn(Either.left(component));
        when(toscaOperationFacade.getLatestByName("serviceProxy", null)).thenReturn(Either.left(new Resource()));

        Resource newResource = getNewResource();
        InputDefinition input = new InputDefinition();
        input.setName(COMPONENT_INPUT_NAME);
        input.setType(COMPONENT_INPUT_TYPE);
        newResource.setInputs(Collections.singletonList(input));

        when(toscaOperationFacade.getToscaElement(any(String.class), any(ComponentParametersView.class))).thenReturn(Either.left(newResource));

        final Map<String, String[]> substitutionMappingMap = new HashMap<>();
        final String[] array = {"value1", "value2"};
        substitutionMappingMap.put("key", array);
        when(capabilityRequirementConverter.convertSubstitutionMappingCapabilities(anyMap(), any(Component.class)))
            .thenReturn(Either.left(substitutionMappingMap));

        when(capabilityRequirementConverter
            .convertSubstitutionMappingRequirements(any(Component.class), anyMap()))
            .thenReturn(Either.left(Collections.emptyMap()));

        // default test
        final Either<ToscaRepresentation, ToscaError> toscaRepresentationToscaErrorEither = testSubject.exportComponent(newResource);
        assertNotNull(toscaRepresentationToscaErrorEither);

    }

    @Test
    void testConvertNodeTemplatesWhenConvertComponentInstanceCapabilitiesIsRight() {
        Component component = getNewResource();
        List<ComponentInstance> componentInstances = new ArrayList<>();
        Map<String, List<ComponentInstanceProperty>> componentInstancesProperties = new HashMap<>();
        Map<String, List<ComponentInstanceProperty>> componentInstancesInterfaces = new HashMap<>();
        Map<String, Component> componentCache = new HashMap<>();
        Map<String, DataTypeDefinition> dataTypes = new HashMap<>();
        ToscaTopolgyTemplate topologyTemplate = new ToscaTopolgyTemplate();
        Either<Map<String, ToscaNodeTemplate>, ToscaError> result;
        Map<String, List<ComponentInstanceInput>> componentInstancesInputs = new HashMap<>();
        List<ComponentInstanceInput> inputs = new ArrayList<>();
        inputs.add(new ComponentInstanceInput());
        componentInstancesInputs.put("key", inputs);
        List<RequirementCapabilityRelDef> resourceInstancesRelations = new ArrayList<>();
        RequirementCapabilityRelDef reldef = new RequirementCapabilityRelDef();
        reldef.setFromNode("node");
        resourceInstancesRelations.add(reldef);
        component.setComponentInstancesRelations(resourceInstancesRelations);

        ComponentInstance instance = new ComponentInstance();
        instance.setUniqueId("id");
        instance.setComponentUid("uid");
        instance.setOriginType(OriginTypeEnum.ServiceProxy);
        componentInstances.add(instance);
        component.setComponentInstances(componentInstances);

        component.setComponentInstancesInputs(componentInstancesInputs);
        component.setInvariantUUID("uuid");
        component.setUUID("uuid");
        component.setDescription("desc");

        componentCache.put("uid", component);

        when(capabilityRequirementConverter.getOriginComponent(anyMap(), any(ComponentInstance.class))).thenReturn(Either.left(component));
        when(capabilityRequirementConverter
            .convertComponentInstanceCapabilities(any(ComponentInstance.class), anyMap(), any(ToscaNodeTemplate.class)))
            .thenReturn(Either.right(ToscaError.GENERAL_ERROR));
        when(interfaceLifecycleOperation.getAllInterfaceLifecycleTypes(any())).thenReturn(Either.left(Collections.emptyMap()));
        when(applicationDataTypeCache.getAll(null)).thenReturn(Either.left(new HashMap<>()));
        when(capabilityRequirementConverter.convertRequirements(anyMap(), any(Resource.class),
            any(ToscaNodeType.class))).thenReturn(Either.left(new ToscaNodeType()));

        // default test
        final Either<ToscaRepresentation, ToscaError> toscaRepresentationToscaErrorEither = testSubject.exportComponent(component);
        assertNotNull(toscaRepresentationToscaErrorEither);
    }

    @Test
    void testConvetNodeTemplateWhenGetOriginComponentIsRight() {
        Component component = getNewResource();
        List<ComponentInstance> componentInstances = new ArrayList<>();
        Map<String, List<ComponentInstanceProperty>> componentInstancesProperties = new HashMap<>();
        Map<String, List<ComponentInstanceProperty>> componentInstancesInterfaces = new HashMap<>();
        Map<String, Component> componentCache = new HashMap<>();
        Map<String, DataTypeDefinition> dataTypes = new HashMap<>();
        ToscaTopolgyTemplate topologyTemplate = new ToscaTopolgyTemplate();
        Either<Map<String, ToscaNodeTemplate>, ToscaError> result;
        Map<String, List<ComponentInstanceInput>> componentInstancesInputs = new HashMap<>();
        List<ComponentInstanceInput> inputs = new ArrayList<>();
        inputs.add(new ComponentInstanceInput());
        componentInstancesInputs.put("key", inputs);
        List<RequirementCapabilityRelDef> resourceInstancesRelations = new ArrayList<>();
        RequirementCapabilityRelDef reldef = new RequirementCapabilityRelDef();
        reldef.setFromNode("id");
        resourceInstancesRelations.add(reldef);
        component.setComponentInstancesRelations(resourceInstancesRelations);

        ComponentInstance instance = new ComponentInstance();
        instance.setUniqueId("id");
        instance.setComponentUid("uid");
        instance.setOriginType(OriginTypeEnum.ServiceProxy);
        componentInstances.add(instance);
        component.setComponentInstances(componentInstances);

        component.setComponentInstancesInputs(componentInstancesInputs);
        component.setInvariantUUID("uuid");
        component.setUUID("uuid");
        component.setDescription("desc");

        final List<AttributeDefinition> attributes = new ArrayList<>();
        final var attribute = new AttributeDefinition();
        attribute.setName("mock");
        attributes.add(attribute);
        component.setAttributes(attributes);

        componentCache.put("uid", component);

        when(capabilityRequirementConverter.getOriginComponent(anyMap(), any(ComponentInstance.class))).thenReturn(Either.right(false));
        when(interfaceLifecycleOperation.getAllInterfaceLifecycleTypes(any())).thenReturn(Either.left(Collections.emptyMap()));
        when(applicationDataTypeCache.getAll(null)).thenReturn(Either.left(new HashMap<>()));
        when(capabilityRequirementConverter.convertRequirements(anyMap(), any(Resource.class),
            any(ToscaNodeType.class))).thenReturn(Either.left(new ToscaNodeType()));

        // default test
        final Either<ToscaRepresentation, ToscaError> toscaRepresentationToscaErrorEither = testSubject.exportComponent(component);
        assertNotNull(toscaRepresentationToscaErrorEither);
    }

    @Test
    void testConvertNodeTemplatesWhenConvertComponentInstanceRequirmentsIsRight() {
        Resource component = getNewResource();
        component.setResourceType(VF);
        List<ComponentInstance> componentInstances = new ArrayList<>();
        Map<String, List<ComponentInstanceProperty>> componentInstancesProperties = new HashMap<>();
        Map<String, List<ComponentInstanceProperty>> componentInstancesInterfaces = new HashMap<>();
        Map<String, Component> componentCache = new HashMap<>();
        Map<String, DataTypeDefinition> dataTypes = new HashMap<>();
        ToscaTopolgyTemplate topologyTemplate = new ToscaTopolgyTemplate();
        Map<String, List<ComponentInstanceInput>> componentInstancesInputs = new HashMap<>();
        List<ComponentInstanceInput> inputs = new ArrayList<>();
        inputs.add(new ComponentInstanceInput());
        componentInstancesInputs.put("key", inputs);
        List<RequirementCapabilityRelDef> resourceInstancesRelations = new ArrayList<>();
        RequirementCapabilityRelDef reldef = new RequirementCapabilityRelDef();
        reldef.setFromNode("id");
        reldef.setToNode("node");
        List<CapabilityRequirementRelationship> relationships = new ArrayList<>();
        CapabilityRequirementRelationship relationship = new CapabilityRequirementRelationship();
        relationship.setRelation(new RelationshipInfo());
        relationships.add(relationship);
        reldef.setRelationships(relationships);
        resourceInstancesRelations.add(reldef);
        component.setComponentInstancesRelations(resourceInstancesRelations);

        Map<String, ArtifactDefinition> artifactList = new HashMap<>();
        ArtifactDefinition artifact = new ArtifactDefinition();
        artifact.setArtifactName("name.name2");
        artifactList.put("assettoscatemplate", artifact);
        component.setArtifacts(artifactList);
        component.setToscaArtifacts(artifactList);

        ComponentInstance instance = new ComponentInstance();
        instance.setUniqueId("id");
        instance.setComponentUid("id");
        instance.setOriginType(OriginTypeEnum.VF);
        componentInstances.add(instance);
        component.setComponentInstances(componentInstances);

        component.setComponentInstancesInputs(componentInstancesInputs);
        component.setComponentInstances(componentInstances);

        doReturn(Either.left(component)).when(toscaOperationFacade).getToscaFullElement("id");
        when(capabilityRequirementConverter.getOriginComponent(anyMap(), any(ComponentInstance.class))).thenReturn(Either.left(component));
        when(interfaceLifecycleOperation.getAllInterfaceLifecycleTypes(any())).thenReturn(Either.left(Collections.emptyMap()));
        when(applicationDataTypeCache.getAll(null)).thenReturn(Either.left(new HashMap<>()));
        when(capabilityRequirementConverter.convertRequirements(anyMap(), any(Resource.class),
            any(ToscaNodeType.class))).thenReturn(Either.left(new ToscaNodeType()));
        when(toscaOperationFacade.getToscaElement(any(String.class), any(ComponentParametersView.class)))
            .thenReturn(Either.right(StorageOperationStatus.BAD_REQUEST));

        // default test
        final Either<ToscaRepresentation, ToscaError> result = testSubject.exportComponent(component);
        assertNotNull(result);
    }

    @Test
    void testCreateNodeType() {

        Component component = new Resource();
        List<String> array = new ArrayList<>();
        array.add("value");
        ((Resource) component).setDerivedFrom(array);
        ToscaNodeType result;

        // test when component is resource
        result = Deencapsulation.invoke(testSubject, "createNodeType", component);
        assertNotNull(result);

        component = new Service();
        // test when component is service
        result = Deencapsulation.invoke(testSubject, "createNodeType", component);
        assertNotNull(result);
    }

    @Test
    void testCreateProxyInterfaceTypesComponentNotFound() {
        Component container = new Service();
        Either<Map<String, ToscaNodeType>, ToscaError> result;
        List<ComponentInstance> componentInstances = new ArrayList<>();
        ComponentInstance instance = new ComponentInstance();
        instance.setOriginType(OriginTypeEnum.ServiceProxy);
        instance.setSourceModelUid("targetModelUid");
        instance.setToscaComponentName("toscaComponentName");

        componentInstances.add(instance);
        container.setComponentInstances(componentInstances);
        when(toscaOperationFacade.getToscaElement(any(String.class),
            any(ComponentParametersView.class)))
            .thenReturn(Either.right(StorageOperationStatus.BAD_REQUEST));
        result = Deencapsulation.invoke(testSubject, "createProxyInterfaceTypes", container);
        assertTrue(result.isRight());
    }

    @Test
    void testCreateProxyInterfaceTypesWhenInterfaceLifecycleFetchFailed() {
        Component container = new Service();
        Either<Map<String, ToscaNodeType>, ToscaError> result;
        List<ComponentInstance> componentInstances = new ArrayList<>();
        ComponentInstance instance = new ComponentInstance();
        instance.setOriginType(OriginTypeEnum.ServiceProxy);
        instance.setSourceModelUid("targetModelUid");
        instance.setToscaComponentName("toscaComponentName");
        componentInstances.add(instance);
        container.setComponentInstances(componentInstances);

        when(toscaOperationFacade.getToscaElement(any(String.class),
            any(ComponentParametersView.class)))
            .thenReturn(Either.left(new Resource()));
        when(interfaceLifecycleOperation.getAllInterfaceLifecycleTypes(any()))
            .thenReturn(Either.right(StorageOperationStatus.BAD_REQUEST));
        result = Deencapsulation.invoke(testSubject, "createProxyInterfaceTypes", container);
        assertTrue(result.isRight());
    }

    @Test
    void testCreateProxyInterfaceTypesPositive() {
        Component container = new Service();
        Either<Map<String, ToscaNodeType>, ToscaError> result;
        List<ComponentInstance> componentInstances = new ArrayList<>();
        ComponentInstance instance = new ComponentInstance();
        instance.setOriginType(OriginTypeEnum.ServiceProxy);
        instance.setSourceModelUid("targetModelUid");
        instance.setToscaComponentName("toscaComponentName");
        componentInstances.add(instance);
        container.setComponentInstances(componentInstances);

        when(interfaceLifecycleOperation.getAllInterfaceLifecycleTypes(any()))
            .thenReturn(Either.left(Collections.emptyMap()));

        Component proxyResource = new Resource();
        Map<String, InterfaceDefinition> proxyInterfaces = new HashMap<>();
        proxyInterfaces.put("Local", new InterfaceDefinition("Local", "desc", new HashMap<>()));
        proxyResource.setInterfaces(proxyInterfaces);
        when(toscaOperationFacade.getToscaElement(any(String.class),
            any(ComponentParametersView.class)))
            .thenReturn(Either.left(proxyResource));

        result = Deencapsulation.invoke(testSubject, "createProxyInterfaceTypes", container);
        assertNotNull(result);
        assertTrue(result.isLeft());
        assertEquals(1, result.left().value().size());
    }

    @Test
    void testCreateProxyNodeTypes() {
        Map<String, Component> componentCache = new HashMap<>();
        Component container = new Resource();
        Either<Map<String, ToscaNodeType>, ToscaError> result;
        List<ComponentInstance> componentInstances = new ArrayList<>();
        ComponentInstance instance = new ComponentInstance();
        instance.setOriginType(OriginTypeEnum.ServiceProxy);
        instance.setSourceModelUid("targetModelUid");

        componentInstances.add(instance);
        container.setComponentInstances(componentInstances);

        when(toscaOperationFacade.getLatestByName("serviceProxy", null))
            .thenReturn(Either.right(StorageOperationStatus.BAD_REQUEST));

        // test when getLatestByName return is right
        result = Deencapsulation.invoke(testSubject, "createProxyNodeTypes", componentCache, container);
        assertNotNull(result);
    }

    @Test
    void testCreateServiceSubstitutionNodeTypes() {
        Map<String, Component> componentCache = new HashMap<>();

        Component referencedService = getNewService();
        referencedService.setInvariantUUID("uuid");
        referencedService.setUUID("uuid");
        referencedService.setUniqueId("targetModelUid");
        referencedService.setDescription("desc");
        componentCache.put("targetModelUid", referencedService);

        Component containerService = new Service();
        List<ComponentInstance> componentInstances = new ArrayList<>();
        ComponentInstance instance = new ComponentInstance();
        instance.setOriginType(OriginTypeEnum.ServiceSubstitution);
        instance.setSourceModelUid("targetModelUid");

        componentInstances.add(instance);
        containerService.setComponentInstances(componentInstances);

        when(interfaceLifecycleOperation.getAllInterfaceLifecycleTypes(any()))
            .thenReturn(Either.left(Collections.emptyMap()));
        when(applicationDataTypeCache.getAll(null)).thenReturn(Either.left(new HashMap<>()));
        when(capabilityRequirementConverter.convertRequirements(anyMap(), any(Service.class),
            any(ToscaNodeType.class))).thenReturn(Either.left(new ToscaNodeType()));

        ToscaTemplate toscaNode = new ToscaTemplate("1_1");

        Deencapsulation.invoke(testSubject, "createServiceSubstitutionNodeTypes", componentCache, containerService, toscaNode);
        assertNotNull(toscaNode.getNode_types());
    }

    @Test
    void testCreateProxyNodeTypesWhenGetLatestByNameReturnValue() {
        Map<String, Component> componentCache = new HashMap<>();
        Component container = new Resource();
        Either<Map<String, ToscaNodeType>, ToscaError> result;
        List<ComponentInstance> componentInstances = new ArrayList<>();
        ComponentInstance instance = new ComponentInstance();
        instance.setOriginType(OriginTypeEnum.ServiceProxy);
        instance.setSourceModelUid("targetModelUid");

        componentInstances.add(instance);
        container.setComponentInstances(componentInstances);

        when(toscaOperationFacade.getLatestByName("serviceProxy", null)).thenReturn(Either.left(new Resource()));

        ComponentParametersView parameterView = new ComponentParametersView();
        parameterView.disableAll();
        parameterView.setIgnoreCategories(false);

        when(toscaOperationFacade.getToscaElement(any(String.class),
            any(ComponentParametersView.class)))
            .thenReturn(Either.right(StorageOperationStatus.BAD_REQUEST));

        when(applicationDataTypeCache.getAll(null)).thenReturn(Either.left(new HashMap<>()));

        // test when getLatestByName is left
        result = Deencapsulation.invoke(testSubject, "createProxyNodeTypes", componentCache, container);
        assertNotNull(result);
    }

    @Test
    void testCreateProxyNodeType() {
        Map<String, Component> componentCache = new HashMap<>();
        Component origComponent = new Resource();
        Component proxyComponent = new Resource();
        ComponentInstance instance = new ComponentInstance();
        ToscaNodeType result;

        when(applicationDataTypeCache.getAll(null)).thenReturn(Either.left(new HashMap<>()));

        // default test
        result = Deencapsulation.invoke(testSubject, "createProxyNodeType", componentCache, origComponent,
            proxyComponent, instance);
        assertNotNull(result);
    }

    @Test
    void testConvertComponentInstanceRequirements() {
        Component component = new Resource();
        ComponentInstance componentInstance = new ComponentInstance();
        List<RequirementCapabilityRelDef> relations = new ArrayList<>();
        ToscaNodeTemplate nodeTypeTemplate = new ToscaNodeTemplate();
        Component originComponent = new Resource();
        Map<String, Component> componentCache = new HashMap<>();
        Either<ToscaNodeTemplate, ToscaError> result;

        // default test
        result = Deencapsulation.invoke(testSubject, "convertComponentInstanceRequirements", component,
            componentInstance, relations, nodeTypeTemplate, originComponent, componentCache);
        assertNotNull(result);

        RequirementCapabilityRelDef reldef = new RequirementCapabilityRelDef();
        reldef.setFromNode("name");
        reldef.setToNode("name1");
        List<CapabilityRequirementRelationship> relationships = new ArrayList<>();
        CapabilityRequirementRelationship cap = new CapabilityRequirementRelationship();
        cap.setRelation(new RelationshipInfo());
        relationships.add(cap);
        reldef.setRelationships(relationships);
        relations.add(reldef);
        componentInstance.setUniqueId("name");

        List<ComponentInstance> instances = new ArrayList<>();
        instances.add(componentInstance);
        component.setComponentInstances(instances);

        // test when filteredRElations ins't empty
        result = Deencapsulation.invoke(testSubject, "convertComponentInstanceRequirements", component,
            componentInstance, relations, nodeTypeTemplate, originComponent, componentCache);
        assertNotNull(result);
    }

    @Test
    void buildRequirementFailure() {
        final Component fromOriginComponent = new Resource();
        final ComponentInstance fromInstance = new ComponentInstance();
        final String fromInstanceUid = "fromInstanceUid";
        fromInstance.setUniqueId(fromInstanceUid);
        fromInstance.setComponentUid("componentUid");
        final RequirementCapabilityRelDef relationshipDefinition = new RequirementCapabilityRelDef();
        relationshipDefinition.setToNode("wrongNodeUid");
        final List<CapabilityRequirementRelationship> relationshipList = new ArrayList<>();
        final CapabilityRequirementRelationship relationship = new CapabilityRequirementRelationship();
        relationship.setRequirement(new RequirementDataDefinition());
        relationshipList.add(relationship);
        relationshipDefinition.setRelationships(relationshipList);
        final List<ComponentInstance> instancesList = new ArrayList<>();
        instancesList.add(fromInstance);
        String expectedError = String
            .format("Failed to find a relation from the node %s to the node %s", fromInstance.getName(),
                relationshipDefinition.getToNode());
        assertThrows(ToscaExportException.class, () ->
            Deencapsulation.invoke(testSubject, "buildRequirement", fromInstance, fromOriginComponent,
                instancesList, relationshipDefinition, new HashMap<>()), expectedError);

        try {
            Deencapsulation.invoke(testSubject, "buildRequirement", fromInstance, fromOriginComponent,
                instancesList, relationshipDefinition, new HashMap<>());
        } catch (Exception e) {
            assertTrue(e instanceof ToscaExportException);
            assertEquals(expectedError, e.getMessage());
        }

        final RelationshipInfo relation = new RelationshipInfo();
        final String requirementUid = "Uid";
        relation.setRequirementUid(requirementUid);
        final String requirementName = "requirementName";
        relation.setRequirement(requirementName);
        final String capabilityName = "capabilityName";
        relation.setCapability(capabilityName);
        final String capabilityOwnerId = "capabilityOwnerId";
        relation.setCapabilityOwnerId(capabilityOwnerId);
        relationship.setRelation(relation);

        final Map<String, List<RequirementDefinition>> requirementMap = new HashMap<>();
        final RequirementDefinition requirementDefinition = new RequirementDefinition();
        requirementMap.put(requirementUid, Collections.singletonList(requirementDefinition));
        fromOriginComponent.setRequirements(requirementMap);
        relationshipDefinition.setToNode(fromInstanceUid);

        expectedError = String
            .format("Failed to find a requirement with uniqueId %s on a component with uniqueId %s",
                relation.getRequirementUid(), fromOriginComponent.getUniqueId());

        assertThrows(ToscaExportException.class, () ->
            Deencapsulation.invoke(testSubject, "buildRequirement", fromInstance, fromOriginComponent,
                instancesList, relationshipDefinition, new HashMap<>()), expectedError);

        requirementDefinition.setName(requirementName);

        when(toscaOperationFacade.getToscaElement(any(String.class), any(ComponentParametersView.class)))
            .thenReturn(Either.right(StorageOperationStatus.NOT_FOUND));

        expectedError = String.format(
            "Failed to build substituted name for the requirement %s. "
                + "Failed to get an origin component with uniqueId %s",
            requirementName, fromInstance.getActualComponentUid());
        assertThrows(ToscaExportException.class, () -> Deencapsulation
            .invoke(testSubject, "buildRequirement", fromInstance, fromOriginComponent, instancesList,
                relationshipDefinition, new HashMap<>()), expectedError);

        final Component toOriginComponent = new Resource();
        final Map<String, List<CapabilityDefinition>> capabilityMap = new HashMap<>();
        final CapabilityDefinition capabilityDefinition = new CapabilityDefinition();

        capabilityDefinition.setName(capabilityName);
        capabilityDefinition.setOwnerId(capabilityOwnerId);
        capabilityDefinition.setType("aType");
        final String capabilityPreviousName = "capabilityPreviousName";
        capabilityDefinition.setPreviousName(capabilityPreviousName);
        capabilityMap.put(capabilityName, Collections.singletonList(capabilityDefinition));
        toOriginComponent.setCapabilities(capabilityMap);
        when(toscaOperationFacade.getToscaElement(any(String.class), any(ComponentParametersView.class)))
            .thenReturn(Either.left(toOriginComponent));

        requirementDefinition.setCapability(capabilityName);
        relation.setCapability("wrong");
        final String requirementPreviousName = "requirementPreviousName";
        requirementDefinition.setPreviousName(requirementPreviousName);
        requirementDefinition.setPath(new ArrayList<>());

        expectedError = String
            .format("Failed to find a capability with name %s on a component with uniqueId %s",
                relation.getCapability(), fromOriginComponent.getUniqueId());

        assertThrows(ToscaExportException.class, () -> Deencapsulation
                .invoke(testSubject, "buildRequirement", fromInstance, fromOriginComponent, instancesList,
                    relationshipDefinition, new HashMap<>()),
            expectedError);
    }

    @Test
    void testBuildRequirement() {
        final ComponentInstance fromInstance = new ComponentInstance();
        fromInstance.setUniqueId("name");
        fromInstance.setComponentUid("string");
        final List<ComponentInstance> instancesList = new ArrayList<>();

        final Map<String, Component> componentCache = new HashMap<>();
        final List<CapabilityRequirementRelationship> relationshipList = new ArrayList<>();
        final CapabilityRequirementRelationship relationship = new CapabilityRequirementRelationship();
        relationship.setRequirement(new RequirementDataDefinition());
        final RelationshipInfo relation = new RelationshipInfo();
        final String requirementUid = "Uid";
        relation.setRequirementUid(requirementUid);
        final String requirementName = "requirementName";
        relation.setRequirement(requirementName);
        final String capabilityName = "capabilityName";
        relation.setCapability(capabilityName);
        final String capabilityOwnerId = "capabilityOwnerId";
        relation.setCapabilityOwnerId(capabilityOwnerId);
        relationship.setRelation(relation);
        relationshipList.add(relationship);
        final RequirementCapabilityRelDef relationshipDefinition = new RequirementCapabilityRelDef();
        relationshipDefinition.setRelationships(relationshipList);
        relationshipDefinition.setToNode("name");
        instancesList.add(fromInstance);
        final RequirementDefinition requirementDefinition = new RequirementDefinition();
        requirementDefinition.setName(requirementName);
        requirementDefinition.setCapability(capabilityName);
        final String requirementPreviousName = "requirementPreviousName";
        requirementDefinition.setPreviousName(requirementPreviousName);
        requirementDefinition.setPath(new ArrayList<>());
        final Map<String, List<RequirementDefinition>> requirementMap = new HashMap<>();
        requirementMap.put(requirementUid, Collections.singletonList(requirementDefinition));
        final Component fromOriginComponent = new Resource();
        fromOriginComponent.setRequirements(requirementMap);

        final Map<String, List<CapabilityDefinition>> capabilityMap = new HashMap<>();
        final CapabilityDefinition capabilityDefinition = new CapabilityDefinition();
        capabilityDefinition.setName(capabilityName);
        capabilityDefinition.setOwnerId(capabilityOwnerId);
        final String capabilityPreviousName = "capabilityPreviousName";
        capabilityDefinition.setPreviousName(capabilityPreviousName);
        capabilityMap.put(capabilityName, Collections.singletonList(capabilityDefinition));
        final Component toOriginComponent = new Resource();
        toOriginComponent.setCapabilities(capabilityMap);

        when(toscaOperationFacade.getToscaElement(any(String.class), any(ComponentParametersView.class)))
            .thenReturn(Either.left(toOriginComponent));
        final String builtCapabilityName = "builtCapabilityName";
        when(
            capabilityRequirementConverter
                .buildSubstitutedName(anyMap(), eq(toOriginComponent), anyList(), eq(capabilityName), eq(
                    capabilityPreviousName), any()))
            .thenReturn(Either.left(builtCapabilityName));

        final String builtRequirementName = "builtRequirementName";
        when(
            capabilityRequirementConverter
                .buildSubstitutedName(anyMap(), eq(fromOriginComponent), anyList(), eq(requirementName), eq(
                    requirementPreviousName), any()))
            .thenReturn(Either.left(builtRequirementName));

        final Map<String, ToscaTemplateRequirement> actualRequirementMap =
            Deencapsulation.invoke(testSubject, "buildRequirement", fromInstance, fromOriginComponent,
                instancesList, relationshipDefinition, componentCache);
        assertNotNull(actualRequirementMap);
        assertFalse(actualRequirementMap.isEmpty());
        assertTrue(actualRequirementMap.containsKey(builtRequirementName));
        final ToscaTemplateRequirement actualToscaTemplateRequirement = actualRequirementMap.get(builtRequirementName);
        assertNotNull(actualToscaTemplateRequirement);
        assertEquals(builtCapabilityName, actualToscaTemplateRequirement.getCapability());

        //to toOriginComponent not found
        when(toscaOperationFacade.getToscaElement(any(String.class), any(ComponentParametersView.class)))
            .thenReturn(Either.right(StorageOperationStatus.NOT_FOUND));

        assertThrows(ToscaExportException.class, () -> Deencapsulation.invoke(testSubject, "buildRequirement", fromInstance, fromOriginComponent,
            instancesList, relationshipDefinition, componentCache));
    }

    @Test
    void testAddRequirmentsWithBuildAndAddRequirements() {
        ComponentInstance fromInstance = new ComponentInstance();
        Component fromOriginComponent = new Resource();
        List<ComponentInstance> instancesList = new ArrayList<>();
        RequirementCapabilityRelDef rel = new RequirementCapabilityRelDef();
        List<Map<String, ToscaTemplateRequirement>> toscaRequirements = new ArrayList<>();
        Map<String, Component> componentCache = new HashMap<>();

        List<CapabilityRequirementRelationship> relationships = new ArrayList<>();
        CapabilityRequirementRelationship cap = new CapabilityRequirementRelationship();
        cap.setRequirement(new RequirementDataDefinition());
        RelationshipInfo relation = new RelationshipInfo();
        relation.setRequirementUid("Uid");
        relation.setRequirement("requirment");
        relation.setCapability("cap");
        relation.setCapabilityOwnerId("id");
        cap.setRelation(relation);
        relationships.add(cap);
        rel.setRelationships(relationships);
        rel.setToNode("name");
        fromInstance.setUniqueId("name");
        fromInstance.setComponentUid("string");
        instancesList.add(fromInstance);
        Map<String, List<RequirementDefinition>> requirements = new HashMap<>();

        List<RequirementDefinition> defs = new ArrayList<>();
        RequirementDefinition def = new RequirementDefinition();
        def.setName("requirment");
        def.setCapability("cap");
        defs.add(def);
        requirements.put("key", defs);
        fromOriginComponent.setRequirements(requirements);

        Map<String, List<CapabilityDefinition>> capabilities = new HashMap<>();
        List<CapabilityDefinition> caps = new ArrayList<>();
        CapabilityDefinition capdef = new CapabilityDefinition();
        capdef.setOwnerId("id");
        capdef.setName("cap");
        capdef.setPreviousName("before cap");
        capdef.setType("type");
        caps.add(capdef);
        capabilities.put("cap", caps);
        fromOriginComponent.setCapabilities(capabilities);

        when(toscaOperationFacade.getToscaElement(any(String.class),
            any(ComponentParametersView.class))).thenReturn(Either.left(fromOriginComponent));

        when(
            capabilityRequirementConverter
                .buildSubstitutedName(anyMap(), any(Component.class), anyList(), anyString(), anyString(), any()))
            .thenReturn(Either.right(false));

        final String expectedErrorMsg =
            String.format("Failed to build a substituted capability name for the capability "
                    + "with name %s on a component with uniqueId %s",
                cap.getRequirement(), fromOriginComponent.getUniqueId());

        assertThrows(ToscaExportException.class, () ->
            Deencapsulation.invoke(testSubject, "buildRequirement", fromInstance, fromOriginComponent, instancesList,
                rel, componentCache), expectedErrorMsg);
    }

    @Test
    void testBuildAndAddRequirement() {
        Component fromOriginComponent = new Resource();
        Component toOriginComponent = new Resource();
        CapabilityDefinition capability = new CapabilityDefinition();
        RequirementDefinition requirement = new RequirementDefinition();
        RelationshipInfo reqAndRelationshipPair = new RelationshipInfo();
        ComponentInstance toInstance = new ComponentInstance();
        Map<String, Component> componentCache = new HashMap<>();
        final CapabilityRequirementRelationship capabilityRequirementRelationship = new CapabilityRequirementRelationship();
        capabilityRequirementRelationship.setRelation(reqAndRelationshipPair);
        capability.setPath(new ArrayList<>());
        capability.setPreviousName("before cap");
        reqAndRelationshipPair.setCapability("cap");
        requirement.setPath(new ArrayList<>());
        requirement.setPreviousName("before req");
        reqAndRelationshipPair.setRequirement("req");

        when(
            capabilityRequirementConverter
                .buildSubstitutedName(anyMap(), eq(toOriginComponent), anyList(), eq("cap"), anyString(), any()))
            .thenReturn(Either.left("buildCapNameRes"));

        when(
            capabilityRequirementConverter
                .buildSubstitutedName(anyMap(), eq(fromOriginComponent), anyList(), eq("req"), anyString(), any()))
            .thenReturn(Either.left("buildReqNameRes"));

        // default test
        final Map<String, ToscaTemplateRequirement> requirementMap =
            Deencapsulation.invoke(testSubject, "buildRequirement", fromOriginComponent, toOriginComponent,
                capability, requirement, capabilityRequirementRelationship, toInstance, componentCache);
        assertNotNull(requirementMap);
        assertFalse(requirementMap.isEmpty());
        assertTrue(requirementMap.containsKey("buildReqNameRes"));
        final ToscaTemplateRequirement actualToscaTemplateRequirement = requirementMap.get("buildReqNameRes");
        assertNotNull(actualToscaTemplateRequirement);
        assertEquals("buildCapNameRes", actualToscaTemplateRequirement.getCapability());
    }

    @Test
    void testBuildRequirementBuildSubstitutedNameReturnsValueTwice() {
        final Component fromOriginComponent = new Resource();
        final Component toOriginComponent = new Resource();
        final CapabilityDefinition capability = new CapabilityDefinition();
        final RequirementDefinition requirement = new RequirementDefinition();
        final RelationshipInfo relationship = new RelationshipInfo();
        final CapabilityRequirementRelationship capabilityRequirementRelationship = new CapabilityRequirementRelationship();
        capabilityRequirementRelationship.setRelation(relationship);
        ComponentInstance toInstance = new ComponentInstance();
        Map<String, Component> componentCache = new HashMap<>();
        capability.setPath(new ArrayList<>());
        relationship.setCapability("cap");
        requirement.setPath(new ArrayList<>());
        relationship.setRequirement("req");

        final String builtCapabilityOrRequirementName = "builtCapabilityOrRequirementName";
        when(capabilityRequirementConverter.buildSubstitutedName(anyMap(), eq(fromOriginComponent), anyList(), eq("cap"), any(), any()))
            .thenReturn(Either.left(builtCapabilityOrRequirementName));
        when(capabilityRequirementConverter.buildSubstitutedName(anyMap(), eq(toOriginComponent), anyList(), eq("req"), any(), any()))
            .thenReturn(Either.left(builtCapabilityOrRequirementName));

        final Map<String, ToscaTemplateRequirement> requirementMap = Deencapsulation
            .invoke(testSubject, "buildRequirement", fromOriginComponent, toOriginComponent, capability, requirement,
                capabilityRequirementRelationship, toInstance, componentCache);
        assertNotNull(requirementMap);
        assertFalse(requirementMap.isEmpty());
        assertTrue(requirementMap.containsKey(builtCapabilityOrRequirementName));
        final ToscaTemplateRequirement actualToscaTemplateRequirement = requirementMap.get(builtCapabilityOrRequirementName);
        assertNotNull(actualToscaTemplateRequirement);
        assertEquals(builtCapabilityOrRequirementName, actualToscaTemplateRequirement.getCapability());
    }

    @Test
    void testIsRequirementBelongToRelation() {

        Component originComponent = new Resource();
        RelationshipInfo reqAndRelationshipPair = new RelationshipInfo();
        RequirementDefinition requirement = new RequirementDefinition();
        String fromInstanceId = "";
        boolean result;

        requirement.setName("name");
        reqAndRelationshipPair.setRequirement("name1");

        // test return false
        result = Deencapsulation.invoke(testSubject, "isRequirementBelongToRelation", originComponent,
            reqAndRelationshipPair, requirement, fromInstanceId);
        assertFalse(result);
    }

    @Test
    void testIsRequirementBelongToRelationWithNonAtomicComponent() {

        Component originComponent = new Service();
        RelationshipInfo reqAndRelationshipPair = new RelationshipInfo();
        RequirementDefinition requirement = new RequirementDefinition();
        String fromInstanceId = "";
        boolean result;

        // default test return true
        result = Deencapsulation.invoke(testSubject, "isRequirementBelongToRelation", originComponent,
            reqAndRelationshipPair, requirement, fromInstanceId);
        assertTrue(result);
    }

    @Test
    void testIsRequirementBelongToOwner() {

        RelationshipInfo reqAndRelationshipPair = new RelationshipInfo();
        RequirementDefinition requirement = new RequirementDefinition();
        String fromInstanceId = "";
        Component originComponent = new Resource();
        boolean result;

        requirement.setOwnerId("owner1");
        reqAndRelationshipPair.setRequirementOwnerId("owner");

        // default test
        result = Deencapsulation.invoke(testSubject, "isRequirementBelongToOwner", reqAndRelationshipPair, requirement,
            fromInstanceId, originComponent);
        assertFalse(result);
    }

    @Test
    void testIsCvfc() {

        Component component = new Service();
        boolean result;

        result = Deencapsulation.invoke(testSubject, "isCvfc", component);
        assertFalse(result);
    }

    @Test
    void testConvertCapabilities() {
        final Component component = new Resource();
        final Map<String, Component> componentCache = new HashMap<>();

        final Either<Map<String, String[]>, ToscaError> expectedResult = Either.right(ToscaError.NODE_TYPE_CAPABILITY_ERROR);
        when(capabilityRequirementConverter.convertSubstitutionMappingCapabilities(componentCache, component))
            .thenReturn(expectedResult);

        // default test return isRight
        final Either<Map<String, String[]>, ToscaError> actualResult =
            Deencapsulation.invoke(testSubject, "convertSubstitutionMappingCapabilities", component, componentCache);
        assertNotNull(actualResult);
        assertEquals(expectedResult, actualResult);
    }

    @Test
    void testConvertCapabilities_1() {
        Component component = new Resource();
        ToscaNodeType nodeType = new ToscaNodeType();
        Map<String, DataTypeDefinition> dataTypes = new HashMap<>();
        Either<ToscaNodeType, ToscaError> result;

        Map<String, ToscaCapability> capabilities = new HashMap<>();
        capabilities.put("key", new ToscaCapability());

        // default test
        result = Deencapsulation
            .invoke(testSubject, "convertCapabilities", new HashMap<>(), component, nodeType, dataTypes);
        assertNotNull(result);
    }

    @Test
    void testConvertToNodeTemplateArtifacts() {
        Map<String, ToscaArtifactDataDefinition> container = new HashMap<>();
        ToscaArtifactDataDefinition art = new ToscaArtifactDataDefinition();
        art.setFile("test_file");
        art.setType("test_type");
        Map<String, ToscaTemplateArtifact> result;
        container.put("test_art", art);
        result = Deencapsulation.invoke(testSubject, "convertToNodeTemplateArtifacts", container);
        assertNotNull(result);
        assertTrue(MapUtils.isNotEmpty(result));
        assertEquals("test_file", result.get("test_art").getFile());
        assertEquals("test_type", result.get("test_art").getType());
    }

    private Component getTestComponent() {
        Component component = new Service();
        component.setUniqueId("serviceUniqueId");
        component.setNormalizedName("normalizedServiceComponentName");
        InterfaceDefinition addedInterface = new InterfaceDefinition();
        addedInterface.setType("com.some.service.or.other.serviceName");
        final String interfaceType = "normalizedServiceComponentName-interface";
        component.setInterfaces(new HashMap<>());
        component.getInterfaces().put(interfaceType, addedInterface);
        return component;
    }

    private PropertyDefinition createMockProperty(String propertyName, String defaultValue) {
        PropertyDefinition propertyDefinition = new PropertyDefinition();
        propertyDefinition.setName(propertyName);
        propertyDefinition.setType("string");
        propertyDefinition.setDefaultValue(defaultValue);
        return propertyDefinition;
    }

    private InputDefinition createMockInput(String inputName, String defaultValue) {
        InputDefinition inputDefinition = new InputDefinition();
        inputDefinition.setName(inputName);
        inputDefinition.setType("string");
        inputDefinition.setDefaultValue(defaultValue);
        return inputDefinition;
    }

}
