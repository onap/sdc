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

package org.openecomp.sdc.be.tosca;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doReturn;
import static org.openecomp.sdc.be.tosca.PropertyConvertor.PropertyType.PROPERTY;

import fj.data.Either;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import mockit.Deencapsulation;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Triple;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.be.components.BeConfDependentTest;
import org.openecomp.sdc.be.components.utils.PropertyDataDefinitionBuilder;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.datatypes.elements.ArtifactDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ForwardingPathDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ForwardingPathElementDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ListDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.OperationDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.RequirementDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ToscaArtifactDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.OriginTypeEnum;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.CapabilityDefinition;
import org.openecomp.sdc.be.model.CapabilityRequirementRelationship;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.ComponentInstanceInput;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.ComponentParametersView;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.GroupDefinition;
import org.openecomp.sdc.be.model.GroupInstance;
import org.openecomp.sdc.be.model.InputDefinition;
import org.openecomp.sdc.be.model.InterfaceDefinition;
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
import org.openecomp.sdc.be.tosca.model.SubstitutionMapping;
import org.openecomp.sdc.be.tosca.model.ToscaCapability;
import org.openecomp.sdc.be.tosca.model.ToscaMetadata;
import org.openecomp.sdc.be.tosca.model.ToscaNodeTemplate;
import org.openecomp.sdc.be.tosca.model.ToscaNodeType;
import org.openecomp.sdc.be.tosca.model.ToscaProperty;
import org.openecomp.sdc.be.tosca.model.ToscaTemplate;
import org.openecomp.sdc.be.tosca.model.ToscaTemplateArtifact;
import org.openecomp.sdc.be.tosca.model.ToscaTemplateRequirement;
import org.openecomp.sdc.be.tosca.model.ToscaTopolgyTemplate;
import org.openecomp.sdc.be.tosca.utils.InputConverter;

public class ToscaExportHandlerTest extends BeConfDependentTest {

    private static final String COMPONENT_PROPERTY_NAME = "prop1";
    private static final String COMPONENT_PROPERTY_TYPE = "string";
    private static final String COMPONENT_INPUT_NAME = "input1";
    private static final String COMPONENT_INPUT_TYPE = "integer";
    private static final String RESOURCE_NAME = "resource";
    private static final String TOSCA_VERSION = "tosca_simple_yaml_1_1";
    private static final Map<String, DataTypeDefinition> DATA_TYPES = new HashMap<>();

    @InjectMocks
    private ToscaExportHandler testSubject;

    @Mock
    private ApplicationDataTypeCache dataTypeCache;

    @Mock
    private ToscaOperationFacade toscaOperationFacade;

    @Mock
    private CapabilityRequirementConverter capabiltyRequirementConvertor;

    @Mock
    private InputConverter inputConverter;

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

    @Before
    public void setUpMock() throws Exception {
        MockitoAnnotations.initMocks(this);
		doReturn(new ToscaProperty()).when(propertyConvertor).convertProperty(any(), any(), eq(PROPERTY));
        doReturn(new HashMap<String, Object>()).when(interfacesOperationsConverter)
                .getInterfacesMap(any(), isNull(), anyMap(), anyMap(), anyBoolean(), anyBoolean());
    }

    private Resource getNewResource() {
        Resource resource = new Resource();
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
    public void testExportComponent() throws Exception {
        Component component = getNewResource();
        Either<ToscaRepresentation, ToscaError> result;

        Mockito.when(dataTypeCache.getAll()).thenReturn(Either.left(new HashMap<>()));
        Mockito
            .when(capabiltyRequirementConvertor.convertRequirements(any(Map.class), any(Resource.class),
                any(ToscaNodeType.class))).thenReturn(Either.left(new ToscaNodeType()));
        Mockito.when(interfaceLifecycleOperation.getAllInterfaceLifecycleTypes())
            .thenReturn(Either.left(Collections.emptyMap()));

        // default test when component is Resource
        result = testSubject.exportComponent(component);
        Assert.assertNotNull(result);

        component = getNewService();
        Mockito
            .when(capabiltyRequirementConvertor.convertRequirements(any(Map.class), any(Service.class),
                any(ToscaNodeType.class))).thenReturn(Either.left(new ToscaNodeType()));
        Mockito.when(dataTypeCache.getAll()).thenReturn(Either.right(JanusGraphOperationStatus.NOT_FOUND));

        // default test when component is Service
        result = testSubject.exportComponent(component);
        Assert.assertNotNull(result);
    }

    @Test
    public void testExportComponentInterface() throws Exception {
        Component component = getNewResource();
        Either<ToscaRepresentation, ToscaError> result;

        ((Resource) component).setInterfaces(new HashMap<>());

        Mockito.when(dataTypeCache.getAll()).thenReturn(Either.right(JanusGraphOperationStatus.NOT_FOUND));
        Mockito.when(interfaceLifecycleOperation.getAllInterfaceLifecycleTypes())
            .thenReturn(Either.left(Collections.emptyMap()));
        // default test when convertInterfaceNodeType is right
        result = testSubject.exportComponentInterface(component, false);
        Assert.assertNotNull(result);

        Mockito.when(dataTypeCache.getAll()).thenReturn(Either.left(new HashMap<>()));
        Mockito
            .when(capabiltyRequirementConvertor.convertRequirements(any(Map.class), any(Resource.class),
                any(ToscaNodeType.class))).thenReturn(Either.left(new ToscaNodeType()));

        // default test when convertInterfaceNodeType is left
        result = testSubject.exportComponentInterface(component, false);
        Assert.assertNotNull(result);
    }

    @Test
    public void testConvertInterfaceNodeTypeProperties() throws Exception {

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

        Mockito.when(interfaceLifecycleOperation.getAllInterfaceLifecycleTypes())
            .thenReturn(Either.left(Collections.emptyMap()));
        Mockito.when(dataTypeCache.getAll()).thenReturn(Either.left(new HashMap<>()));
        // when convertRequirements is called, make it return the same value as 3rd (index=2) argument.
        Mockito
            .when(capabiltyRequirementConvertor.convertRequirements(any(Map.class), any(Resource.class),
                any(ToscaNodeType.class))).thenAnswer(i -> Either.left(i.getArgument(2)));

        Either<ToscaTemplate, ToscaError> result = (Either<ToscaTemplate, ToscaError>) Deencapsulation
            .invoke(testSubject, "convertInterfaceNodeType", new HashMap<String, Component>(), component,
                new ToscaTemplate(TOSCA_VERSION), new HashMap<String, ToscaNodeType>(), false);
        Assert.assertNotNull(result);
        assertThat(result.isLeft(), is(true));
        Map<String, ToscaNodeType> nodeTypeMap = result.left().value().getNode_types();
        assertThat(nodeTypeMap.size(), is(1));
        ToscaNodeType toscaNodeType = nodeTypeMap.values().iterator().next();
        Map<String, ToscaProperty> propertyMap = toscaNodeType.getProperties();
        // Check if inputs and properties in component are merged properly
        assertThat(propertyMap.size(), is(2));
        assertThat(propertyMap.containsKey(COMPONENT_INPUT_NAME), is(true));
        assertThat(propertyMap.containsKey(COMPONENT_PROPERTY_NAME), is(true));
    }

    @Test
    public void testCreateToscaRepresentation() throws Exception {
        ToscaTemplate toscaTemplate = new ToscaTemplate("");
        ToscaRepresentation result;

        // default test
        result = testSubject.createToscaRepresentation(toscaTemplate);
        Assert.assertNotNull(result);
    }

    @Test
    public void testGetDependencies() throws Exception {

        Component component = new Resource();
        Either<ToscaTemplate, ToscaError> result;

        // default test
        result = testSubject.getDependencies(component);
        Assert.assertNotNull(result);
    }

    @Test
    public void testConvertToscaTemplate() throws Exception {

        Component component = getNewResource();
        ToscaTemplate toscaNode = new ToscaTemplate("");
        Either<ToscaTemplate, ToscaError> result;
        List<ComponentInstance> resourceInstances = new ArrayList<>();
        ComponentInstance instance = new ComponentInstance();

        instance.setOriginType(OriginTypeEnum.SERVICE);
        instance.setSourceModelUid("targetModelUid");
        resourceInstances.add(instance);

        component.setComponentInstances(resourceInstances);

        Mockito.when(dataTypeCache.getAll()).thenReturn(Either.left(new HashMap<>()));
        Mockito.when(capabiltyRequirementConvertor.getOriginComponent(any(Map.class),
            any(ComponentInstance.class))).thenReturn(Either.right(false));

        // default test
        result = Deencapsulation.invoke(testSubject, "convertToscaTemplate", component, toscaNode);
        Assert.assertNotNull(result);
    }

    @Test
    public void testConvertToscaTemplateWhenComponentContainsGroup() {
        Component component = getNewResource();
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

        Mockito.when(capabiltyRequirementConvertor.convertSubstitutionMappingCapabilities(any(Map.class),
            any(Component.class))).thenReturn(Either.left(substitutionMappingMap));

        Mockito.when(capabiltyRequirementConvertor.convertSubstitutionMappingRequirements(any(Map.class),
            any(Component.class), any(SubstitutionMapping.class)))
            .thenReturn(Either.left(new SubstitutionMapping()));

        Mockito.when(dataTypeCache.getAll()).thenReturn(Either.left(new HashMap<>()));

        Mockito.when(inputConverter.convertInputs(any(List.class), any(Map.class)))
            .thenReturn(new HashMap<>());

        Mockito.when(groupExportParser.getGroups(component))
            .thenReturn(null);

        // test component contains group
        result = Deencapsulation.invoke(testSubject, "convertToscaTemplate", component, toscaNode);
        Assert.assertNotNull(result);
    }

    @Test
    public void testConvertToscaTemplateWhenComponentIsService() throws Exception {
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

        Mockito
            .when(capabiltyRequirementConvertor.convertSubstitutionMappingCapabilities(anyMap(), any(Component.class)))
            .thenReturn(Either.left(substitutionMappingMap));

        Mockito.when(capabiltyRequirementConvertor
            .convertSubstitutionMappingRequirements(anyMap(), any(Component.class), any(SubstitutionMapping.class)))
            .thenReturn(Either.left(new SubstitutionMapping()));

        Mockito.when(dataTypeCache.getAll()).thenReturn(Either.left(new HashMap<>()));

        Mockito.when(inputConverter.convertInputs(anyList(), anyMap())).thenReturn(new HashMap<>());
        // test component contains group
        result = Deencapsulation.invoke(testSubject, "convertToscaTemplate", component, toscaNode);
        Assert.assertNotNull(result);
    }

    @Test
    public void testConvertMetadata_1() throws Exception {

        Component component = getNewResource();
        boolean isInstance = true;
        ComponentInstance componentInstance = new ComponentInstance();
        componentInstance.setOriginType(OriginTypeEnum.ServiceProxy);
        componentInstance.setSourceModelInvariant("targetModelInvariant");

        ToscaMetadata result;

        // default test
        result = Deencapsulation.invoke(testSubject, "convertMetadata", component, isInstance, componentInstance);
        Assert.assertNotNull(result);
    }

    @Test
    public void testFillImports() throws Exception {

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

        Mockito.when(toscaOperationFacade.getToscaFullElement(any(String.class)))
            .thenReturn(Either.left(component));

        // default test
        result = Deencapsulation.invoke(testSubject, "fillImports", component, toscaTemplate);
        Assert.assertNotNull(result);
    }

    @Test
    public void testCreateDependency() throws Exception {

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

        Mockito.when(toscaOperationFacade.getToscaFullElement(eq("name"))).thenReturn(Either.left(component));

        Mockito.when(toscaOperationFacade.getToscaFullElement(eq("modelName")))
            .thenReturn(Either.left(new Service()));

        // default test
        Deencapsulation.invoke(testSubject, "createDependency", componentCache, imports, dependecies, ci);
    }

    @Test
    public void testGetInterfaceFilename() throws Exception {
        String artifactName = "artifact.name";
        String result;

        // default test
        result = ToscaExportHandler.getInterfaceFilename(artifactName);
        Assert.assertNotNull(result);
    }

    @Test
    public void testConvertNodeType() throws Exception {
        Component component = new Resource();
        ToscaTemplate toscaNode = new ToscaTemplate("");
        Map<String, ToscaNodeType> nodeTypes = new HashMap<>();
        Either<ToscaTemplate, ToscaError> result;

        Mockito.when(dataTypeCache.getAll()).thenReturn(Either.right(JanusGraphOperationStatus.ALREADY_EXIST));
        Mockito.when(interfaceLifecycleOperation.getAllInterfaceLifecycleTypes())
            .thenReturn(Either.left(Collections.emptyMap()));
        // default test
        result = Deencapsulation
            .invoke(testSubject, "convertNodeType", new HashMap<>(), component, toscaNode, nodeTypes);
        Assert.assertNotNull(result);
    }

    @Test
    public void testConvertInterfaceNodeType() throws Exception {
        Component component = getNewResource();
        ToscaTemplate toscaNode = new ToscaTemplate("");
        Map<String, ToscaNodeType> nodeTypes = new HashMap<>();
        Either<ToscaTemplate, ToscaError> result;
        List<InputDefinition> inputs = new ArrayList<>();
        inputs.add(new InputDefinition());
        component.setInputs(inputs);

        Mockito.when(dataTypeCache.getAll()).thenReturn(Either.left(new HashMap<>()));
        Mockito.when(interfaceLifecycleOperation.getAllInterfaceLifecycleTypes())
            .thenReturn(Either.left(Collections.emptyMap()));

        Mockito
            .when(capabiltyRequirementConvertor.convertRequirements(any(Map.class), any(Resource.class),
                any(ToscaNodeType.class))).thenReturn(Either.left(new ToscaNodeType()));

        // default test
        result = Deencapsulation.invoke(testSubject, "convertInterfaceNodeType", new HashMap<>(), component, toscaNode
            , nodeTypes, false);
        Assert.assertNotNull(result);
    }

    @Test
    public void testConvertReqCapAndTypeName() throws Exception {
        Component component = new Resource();
        ToscaTemplate toscaNode = new ToscaTemplate("");
        Map<String, ToscaNodeType> nodeTypes = new HashMap();
        ToscaNodeType toscaNodeType = new ToscaNodeType();
        Map<String, DataTypeDefinition> dataTypes = new HashMap<>();
        Either<ToscaTemplate, ToscaError> result;

        Mockito.when(
            capabiltyRequirementConvertor
                .convertCapabilities(any(Map.class), any(Resource.class), any(Map.class)))
            .thenReturn(new HashMap<>());

        Mockito
            .when(capabiltyRequirementConvertor.convertRequirements(any(Map.class), any(Resource.class),
                any(ToscaNodeType.class))).thenReturn(Either.left(new ToscaNodeType()));

        // default test
        result = Deencapsulation
            .invoke(testSubject, "convertReqCapAndTypeName", new HashMap<>(), component, toscaNode, nodeTypes,
                toscaNodeType, dataTypes);
        Assert.assertNotNull(result);

        component = new Service();

        Mockito
            .when(capabiltyRequirementConvertor.convertRequirements(any(Map.class), any(Service.class),
                any(ToscaNodeType.class))).thenReturn(Either.left(new ToscaNodeType()));

        // test when component is service
        result = Deencapsulation
            .invoke(testSubject, "convertReqCapAndTypeName", new HashMap<>(), component, toscaNode, nodeTypes,
                toscaNodeType, dataTypes);
        Assert.assertNotNull(result);
    }

    @Test
    public void testConvertNodeTemplates() throws Exception {
        Component component = getNewResource();
        List<ComponentInstance> componentInstances = new ArrayList<>();
        Map<String, List<ComponentInstanceProperty>> componentInstancesProperties = new HashMap<>();
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
        List<GroupInstance> groupInstances = new ArrayList<>();
        GroupInstance groupInst = new GroupInstance();
        List<String> artifacts = new ArrayList<>();
        artifacts.add("artifact");
        groupInst.setArtifacts(artifacts);
        groupInst.setType("type");
        groupInstances.add(groupInst);
        instance.setGroupInstances(groupInstances);
        componentInstances.add(instance);

        component.setComponentInstancesInputs(componentInstancesInputs);
        component.setInvariantUUID("uuid");
        component.setUUID("uuid");
        component.setDescription("desc");

        componentCache.put("uid", component);

        componentInstancesProperties.put("id", new ArrayList<>());
        componentInstancesInputs.put("id", new ArrayList<>());

        Mockito.when(capabiltyRequirementConvertor.getOriginComponent(any(Map.class),
            any(ComponentInstance.class))).thenReturn(Either.left(component));

        Mockito.when(capabiltyRequirementConvertor.convertComponentInstanceCapabilities(
            any(ComponentInstance.class), any(Map.class), any(ToscaNodeTemplate.class)))
            .thenReturn(Either.left(new ToscaNodeTemplate()));

        // default test
        result = Deencapsulation.invoke(testSubject, "convertNodeTemplates", component, componentInstances,
            componentInstancesProperties, new HashMap<>(), componentCache, dataTypes, topologyTemplate);
        Assert.assertNotNull(result);
    }

    @Test
    public void testConvertNodeTemplatesWhenComponentIsService() throws Exception {
        Component component = getNewService();
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
        List<GroupInstance> groupInstances = new ArrayList<>();
        GroupInstance groupInst = new GroupInstance();
        List<String> artifacts = new ArrayList<>();
        artifacts.add("artifact");
        groupInst.setArtifacts(artifacts);
        groupInst.setType("type");
        groupInstances.add(groupInst);
        instance.setGroupInstances(groupInstances);
        componentInstances.add(instance);

        component.setComponentInstancesInputs(componentInstancesInputs);
        component.setInvariantUUID("uuid");
        component.setUUID("uuid");
        component.setDescription("desc");

        Map<String, ForwardingPathDataDefinition> forwardingPaths = new HashMap<>();
        ForwardingPathDataDefinition path = new ForwardingPathDataDefinition();
        ListDataDefinition<ForwardingPathElementDataDefinition> list = new ListDataDefinition<>();
        path.setPathElements(list);
        forwardingPaths.put("key", path);

        ((Service) component).setForwardingPaths(forwardingPaths);

        componentCache.put("uid", component);

        componentInstancesProperties.put("id", new ArrayList<>());
        componentInstancesInterfaces.put("id", new ArrayList<>());
        componentInstancesInputs.put("id", new ArrayList<>());

        Mockito.when(capabiltyRequirementConvertor.getOriginComponent(any(Map.class),
            any(ComponentInstance.class))).thenReturn(Either.left(component));

        Mockito.when(capabiltyRequirementConvertor.convertComponentInstanceCapabilities(
            any(ComponentInstance.class), any(Map.class), any(ToscaNodeTemplate.class)))
            .thenReturn(Either.left(new ToscaNodeTemplate()));

        // default test
        result = Deencapsulation.invoke(testSubject, "convertNodeTemplates", component, componentInstances,
            componentInstancesProperties, componentInstancesInterfaces, componentCache, dataTypes, topologyTemplate);
        Assert.assertNotNull(result);
    }

    @Test
    public void testConvertNodeTemplatesWhenConvertComponentInstanceCapabilitiesIsRight() throws Exception {
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

        component.setComponentInstancesInputs(componentInstancesInputs);
        component.setInvariantUUID("uuid");
        component.setUUID("uuid");
        component.setDescription("desc");

        componentCache.put("uid", component);

        Mockito.when(capabiltyRequirementConvertor.getOriginComponent(any(Map.class),
            any(ComponentInstance.class))).thenReturn(Either.left(component));

        Mockito.when(capabiltyRequirementConvertor.convertComponentInstanceCapabilities(
            any(ComponentInstance.class), any(Map.class), any(ToscaNodeTemplate.class)))
            .thenReturn(Either.right(ToscaError.GENERAL_ERROR));

        // default test
        result = Deencapsulation.invoke(testSubject, "convertNodeTemplates", component, componentInstances,
            componentInstancesProperties, componentInstancesInterfaces, componentCache, dataTypes, topologyTemplate);
        Assert.assertNotNull(result);
    }

    @Test
    public void testConvetNodeTemplateWhenGetOriginComponentIsRight() throws Exception {
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

        component.setComponentInstancesInputs(componentInstancesInputs);
        component.setInvariantUUID("uuid");
        component.setUUID("uuid");
        component.setDescription("desc");

        componentCache.put("uid", component);

        Mockito.when(capabiltyRequirementConvertor.getOriginComponent(any(Map.class),
            any(ComponentInstance.class))).thenReturn(Either.right(false));

        // default test
        result = Deencapsulation.invoke(testSubject, "convertNodeTemplates", component, componentInstances,
            componentInstancesProperties, componentInstancesInterfaces, componentCache, dataTypes, topologyTemplate);
        Assert.assertNotNull(result);
    }

    @Test
    public void testConvertNodeTemplatesWhenConvertComponentInstanceRequirmentsIsRight() {
        Component component = new Resource();
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
        reldef.setToNode("node");
        List<CapabilityRequirementRelationship> relationships = new ArrayList<>();
        CapabilityRequirementRelationship relationship = new CapabilityRequirementRelationship();
        relationship.setRelation(new RelationshipInfo());
        relationships.add(relationship);
        reldef.setRelationships(relationships);
        resourceInstancesRelations.add(reldef);
        component.setComponentInstancesRelations(resourceInstancesRelations);

        ComponentInstance instance = new ComponentInstance();
        instance.setUniqueId("id");
        componentInstances.add(instance);

        component.setComponentInstancesInputs(componentInstancesInputs);
        component.setComponentInstances(componentInstances);

        Mockito.when(capabiltyRequirementConvertor.getOriginComponent(any(Map.class),
            any(ComponentInstance.class))).thenReturn(Either.left(component));

        // default test
        result = Deencapsulation.invoke(testSubject, "convertNodeTemplates", component, componentInstances,
            componentInstancesProperties, componentInstancesInterfaces, componentCache, dataTypes, topologyTemplate);
        Assert.assertNotNull(result);
    }

    @Test
    public void testAddComponentInstanceInputs() throws Exception {

        Map<String, DataTypeDefinition> dataTypes = new HashMap<>();
        Map<String, List<ComponentInstanceInput>> componentInstancesInputs = new HashMap<>();
        ComponentInstance componentInstance = new ComponentInstance();
        String instanceUniqueId = "id";
        Map<String, Object> props = new HashMap<>();

        List<ComponentInstanceInput> componentInstanceInputs = new ArrayList<>();
        componentInstanceInputs.add(new ComponentInstanceInput());

        componentInstancesInputs.put(instanceUniqueId, componentInstanceInputs);

        // default test
        Deencapsulation.invoke(testSubject, "addComponentInstanceInputs", dataTypes, componentInstancesInputs,
            instanceUniqueId, props);
    }

    @Test
    public void testAddPropertiesOfComponentInstance() throws Exception {
        Map<String, List<ComponentInstanceProperty>> componentInstancesProperties = new HashMap<>();
        Map<String, DataTypeDefinition> dataTypes = new HashMap<>();
        ComponentInstance componentInstance = new ComponentInstance();
        String instanceUniqueId = "id";
        Map<String, Object> props = new HashMap<>();

        ComponentInstanceProperty cip = new ComponentInstanceProperty();
        cip.setInstanceUniqueId("id");

        List<ComponentInstanceProperty> list = new ArrayList<>();
        list.add(cip);

        componentInstancesProperties.put("id", list);

        // default test
        Deencapsulation.invoke(testSubject, "addPropertiesOfComponentInstance", componentInstancesProperties, dataTypes,
            instanceUniqueId, props);
    }

    @Test
    public void testAddPropertiesOfParentComponent() throws Exception {
        Map<String, DataTypeDefinition> dataTypes = new HashMap<>();
        ComponentInstance componentInstance = new ComponentInstance();
        Component componentOfInstance = new Resource();
        Map<String, Object> props = new HashMap<>();

        List<PropertyDefinition> properties = new ArrayList<>();
        properties.add(new PropertyDefinition());

        ((Resource) componentOfInstance).setProperties(properties);

        // default test
        Deencapsulation.invoke(testSubject, "addPropertiesOfParentComponent", dataTypes,
            componentOfInstance, props);
    }

    @Test
    public void testCreateNodeType() throws Exception {

        Component component = new Resource();
        List<String> array = new ArrayList<>();
        array.add("value");
        ((Resource) component).setDerivedFrom(array);
        ToscaNodeType result;

        // test when component is resource
        result = Deencapsulation.invoke(testSubject, "createNodeType", component);
        Assert.assertNotNull(result);

        component = new Service();
        // test when component is service
        result = Deencapsulation.invoke(testSubject, "createNodeType", component);
        Assert.assertNotNull(result);
    }

    @Test
    public void testCreateProxyInterfaceTypesComponentNotFound() throws Exception {
        Component container = new Service();
        Either<Map<String, ToscaNodeType>, ToscaError> result;
        List<ComponentInstance> componentInstances = new ArrayList<>();
        ComponentInstance instance = new ComponentInstance();
        instance.setOriginType(OriginTypeEnum.ServiceProxy);
        instance.setSourceModelUid("targetModelUid");
        instance.setToscaComponentName("toscaComponentName");

        componentInstances.add(instance);
        container.setComponentInstances(componentInstances);
        Mockito.when(toscaOperationFacade.getToscaElement(any(String.class),
            any(ComponentParametersView.class)))
            .thenReturn(Either.right(StorageOperationStatus.BAD_REQUEST));
        result = Deencapsulation.invoke(testSubject, "createProxyInterfaceTypes", container);
        Assert.assertTrue(result.isRight());
    }

    @Test
    public void testCreateProxyInterfaceTypesWhenInterfaceLifecycleFetchFailed() {
        Component container = new Service();
        Either<Map<String, ToscaNodeType>, ToscaError> result;
        List<ComponentInstance> componentInstances = new ArrayList<>();
        ComponentInstance instance = new ComponentInstance();
        instance.setOriginType(OriginTypeEnum.ServiceProxy);
        instance.setSourceModelUid("targetModelUid");
        instance.setToscaComponentName("toscaComponentName");
        componentInstances.add(instance);
        container.setComponentInstances(componentInstances);

        Mockito.when(toscaOperationFacade.getToscaElement(any(String.class),
            any(ComponentParametersView.class)))
            .thenReturn(Either.left(new Resource()));
        Mockito.when(interfaceLifecycleOperation.getAllInterfaceLifecycleTypes())
            .thenReturn(Either.right(StorageOperationStatus.BAD_REQUEST));
        result = Deencapsulation.invoke(testSubject, "createProxyInterfaceTypes", container);
        Assert.assertTrue(result.isRight());
    }

    @Test
    public void testCreateProxyInterfaceTypesPositive() {
        Component container = new Service();
        Either<Map<String, ToscaNodeType>, ToscaError> result;
        List<ComponentInstance> componentInstances = new ArrayList<>();
        ComponentInstance instance = new ComponentInstance();
        instance.setOriginType(OriginTypeEnum.ServiceProxy);
        instance.setSourceModelUid("targetModelUid");
        instance.setToscaComponentName("toscaComponentName");
        componentInstances.add(instance);
        container.setComponentInstances(componentInstances);

        Mockito.when(interfaceLifecycleOperation.getAllInterfaceLifecycleTypes())
            .thenReturn(Either.left(Collections.emptyMap()));

        Component proxyResource = new Resource();
        Map<String, InterfaceDefinition> proxyInterfaces = new HashMap<>();
        proxyInterfaces.put("Local", new InterfaceDefinition("Local", "desc", new HashMap<>()));
        proxyResource.setInterfaces(proxyInterfaces);
        Mockito.when(toscaOperationFacade.getToscaElement(any(String.class),
            any(ComponentParametersView.class)))
            .thenReturn(Either.left(proxyResource));

        result = Deencapsulation.invoke(testSubject, "createProxyInterfaceTypes", container);
        Assert.assertNotNull(result);
        Assert.assertTrue(result.isLeft());
        Assert.assertEquals(1, result.left().value().size());
    }

    @Test
    public void testCreateProxyNodeTypes() throws Exception {
        Map<String, Component> componentCache = new HashMap<>();
        Component container = new Resource();
        Either<Map<String, ToscaNodeType>, ToscaError> result;
        List<ComponentInstance> componentInstances = new ArrayList<>();
        ComponentInstance instance = new ComponentInstance();
        instance.setOriginType(OriginTypeEnum.ServiceProxy);
        instance.setSourceModelUid("targetModelUid");

        componentInstances.add(instance);
        container.setComponentInstances(componentInstances);

        Mockito.when(toscaOperationFacade.getLatestByName("serviceProxy"))
            .thenReturn(Either.right(StorageOperationStatus.BAD_REQUEST));

        // test when getLatestByName return is right
        result = Deencapsulation.invoke(testSubject, "createProxyNodeTypes", componentCache, container);
        Assert.assertNotNull(result);
    }

    @Test
    public void testCreateProxyNodeTypesWhenGetLatestByNameReturnValue() {
        Map<String, Component> componentCache = new HashMap<>();
        Component container = new Resource();
        Either<Map<String, ToscaNodeType>, ToscaError> result;
        List<ComponentInstance> componentInstances = new ArrayList<>();
        ComponentInstance instance = new ComponentInstance();
        instance.setOriginType(OriginTypeEnum.ServiceProxy);
        instance.setSourceModelUid("targetModelUid");

        componentInstances.add(instance);
        container.setComponentInstances(componentInstances);

        Mockito.when(toscaOperationFacade.getLatestByName("serviceProxy")).thenReturn(Either.left(new Resource()));

        ComponentParametersView parameterView = new ComponentParametersView();
        parameterView.disableAll();
        parameterView.setIgnoreCategories(false);

        Mockito.when(toscaOperationFacade.getToscaElement(any(String.class),
            any(ComponentParametersView.class)))
            .thenReturn(Either.right(StorageOperationStatus.BAD_REQUEST));

        Mockito.when(dataTypeCache.getAll()).thenReturn(Either.left(new HashMap<>()));

        // test when getLatestByName is left
        result = Deencapsulation.invoke(testSubject, "createProxyNodeTypes", componentCache, container);
        Assert.assertNotNull(result);
    }

    @Test
    public void testCreateProxyNodeType() throws Exception {
        Map<String, Component> componentCache = new HashMap<>();
        Component origComponent = new Resource();
        Component proxyComponent = new Resource();
        ComponentInstance instance = new ComponentInstance();
        ToscaNodeType result;

        Mockito.when(dataTypeCache.getAll()).thenReturn(Either.left(new HashMap<>()));

        // default test
        result = Deencapsulation.invoke(testSubject, "createProxyNodeType", componentCache, origComponent,
            proxyComponent, instance);
        Assert.assertNotNull(result);
    }

    @Test
    public void testConvertComponentInstanceRequirements() throws Exception {
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
        Assert.assertNotNull(result);

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
        Assert.assertNotNull(result);
    }

    @Test
    public void testAddRequirement() throws Exception {
        ComponentInstance fromInstance = new ComponentInstance();
        Component fromOriginComponent = new Resource();
        List<ComponentInstance> instancesList = new ArrayList<>();
        RequirementCapabilityRelDef rel = new RequirementCapabilityRelDef();
        List<Map<String, ToscaTemplateRequirement>> toscaRequirements = new ArrayList<>();
        Map<String, Component> componentCache = new HashMap<>();
        boolean result;

        List<CapabilityRequirementRelationship> relationships = new ArrayList<>();
        CapabilityRequirementRelationship cap = new CapabilityRequirementRelationship();
        cap.setRequirement(new RequirementDataDefinition());
        RelationshipInfo relation = new RelationshipInfo();
        relation.setRequirementUid("Uid");
        relation.setRequirement("requirment");
        relation.setCapability("cap");
        relation.setCapabilityOwnerId("id1");
        cap.setRelation(relation);
        relationships.add(cap);
        rel.setRelationships(relationships);
        rel.setToNode("name");
        fromInstance.setUniqueId("name");
        fromInstance.setComponentUid("string");
        instancesList.add(fromInstance);
        Map<String, List<RequirementDefinition>> requirements = new HashMap<>();
        fromOriginComponent.setRequirements(requirements);

        // default test
        result = Deencapsulation.invoke(testSubject, "addRequirement", fromInstance, fromOriginComponent, instancesList,
            rel, toscaRequirements, componentCache);
        Assert.assertNotNull(result);
    }

    @Test
    public void testAddRequirmentsWhenFindRequirmentsReturnsValue() {

        ComponentInstance fromInstance = new ComponentInstance();
        Component fromOriginComponent = new Resource();
        List<ComponentInstance> instancesList = new ArrayList<>();
        RequirementCapabilityRelDef rel = new RequirementCapabilityRelDef();
        List<Map<String, ToscaTemplateRequirement>> toscaRequirements = new ArrayList<>();
        Map<String, Component> componentCache = new HashMap<>();
        boolean result;

        List<CapabilityRequirementRelationship> relationships = new ArrayList<>();
        CapabilityRequirementRelationship cap = new CapabilityRequirementRelationship();
        cap.setRequirement(new RequirementDataDefinition());
        RelationshipInfo relation = new RelationshipInfo();
        relation.setRequirementUid("Uid");
        relation.setRequirement("requirment");
        relation.setCapability("cap");
        relation.setCapabilityOwnerId("id1");
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

        Mockito.when(toscaOperationFacade.getToscaElement(any(String.class),
            any(ComponentParametersView.class)))
            .thenReturn(Either.right(StorageOperationStatus.BAD_REQUEST));

        // default test
        result = Deencapsulation.invoke(testSubject, "addRequirement", fromInstance, fromOriginComponent, instancesList,
            rel, toscaRequirements, componentCache);
        Assert.assertNotNull(result);
    }

    @Test
    public void testAddRequirmentsWhenCapabilityBelongsToRelation() {
        ComponentInstance fromInstance = new ComponentInstance();
        Component fromOriginComponent = new Resource();
        List<ComponentInstance> instancesList = new ArrayList<>();
        RequirementCapabilityRelDef rel = new RequirementCapabilityRelDef();
        List<Map<String, ToscaTemplateRequirement>> toscaRequirements = new ArrayList<>();
        Map<String, Component> componentCache = new HashMap<>();
        boolean result;

        List<CapabilityRequirementRelationship> relationships = new ArrayList<>();
        CapabilityRequirementRelationship cap = new CapabilityRequirementRelationship();
        cap.setRequirement(new RequirementDataDefinition());
        RelationshipInfo relation = new RelationshipInfo();
        relation.setRequirementUid("Uid");
        relation.setRequirement("requirment");
        relation.setCapability("cap");
        relation.setCapabilityOwnerId("id1");
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
        capdef.setName("name");
        capdef.setType("type");
        caps.add(capdef);
        capabilities.put("cap", caps);

        fromOriginComponent.setCapabilities(capabilities);

        Mockito.when(toscaOperationFacade.getToscaElement(any(String.class),
            any(ComponentParametersView.class))).thenReturn(Either.left(fromOriginComponent));

        // default test
        result = Deencapsulation.invoke(testSubject, "addRequirement", fromInstance, fromOriginComponent, instancesList,
            rel, toscaRequirements, componentCache);
        Assert.assertNotNull(result);
    }

    @Test
    public void testAddRequirmentsWithBuildAndAddRequirements() {
        ComponentInstance fromInstance = new ComponentInstance();
        Component fromOriginComponent = new Resource();
        List<ComponentInstance> instancesList = new ArrayList<>();
        RequirementCapabilityRelDef rel = new RequirementCapabilityRelDef();
        List<Map<String, ToscaTemplateRequirement>> toscaRequirements = new ArrayList<>();
        Map<String, Component> componentCache = new HashMap<>();
        boolean result;

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

        Mockito.when(toscaOperationFacade.getToscaElement(any(String.class),
            any(ComponentParametersView.class))).thenReturn(Either.left(fromOriginComponent));

        Mockito.when(
            capabiltyRequirementConvertor
                .buildSubstitutedName(anyMap(), any(Component.class), anyList(), anyString(), anyString()))
            .thenReturn(Either.right(false));

        // default test
        result = Deencapsulation.invoke(testSubject, "addRequirement", fromInstance, fromOriginComponent, instancesList,
            rel, toscaRequirements, componentCache);
        Assert.assertNotNull(result);
    }

    @Test
    public void testBuildAndAddRequirement() throws Exception {
        List<Map<String, ToscaTemplateRequirement>> toscaRequirements = new ArrayList<>();
        Component fromOriginComponent = new Resource();
        Component toOriginComponent = new Resource();
        CapabilityDefinition capability = new CapabilityDefinition();
        RequirementDefinition requirement = new RequirementDefinition();
        RelationshipInfo reqAndRelationshipPair = new RelationshipInfo();
        ComponentInstance toInstance = new ComponentInstance();
        Map<String, Component> componentCache = new HashMap<>();
        boolean result;
        capability.setPath(new ArrayList<>());
        capability.setPreviousName("before cap");
        reqAndRelationshipPair.setCapability("cap");
        requirement.setPath(new ArrayList<>());
        requirement.setPreviousName("before req");
        reqAndRelationshipPair.setRequirement("req");

        Mockito.when(
            capabiltyRequirementConvertor
                .buildSubstitutedName(anyMap(), eq(toOriginComponent), anyList(), eq("cap"), anyString()))
            .thenReturn(Either.left("buildCapNameRes"));

        Mockito.when(
            capabiltyRequirementConvertor
                .buildSubstitutedName(anyMap(), eq(fromOriginComponent), anyList(), eq("req"), anyString()))
            .thenReturn(Either.right(false));

        // default test
        result = Deencapsulation.invoke(testSubject, "buildAndAddRequirement", toscaRequirements, fromOriginComponent,
            toOriginComponent, capability, requirement, reqAndRelationshipPair, toInstance, componentCache);
        Assert.assertNotNull(result);
    }

    @Test
    public void testBuildAndAddRequirementBuildSubtitutedNameReturnsValueTwice() {
        List<Map<String, ToscaTemplateRequirement>> toscaRequirements = new ArrayList<>();
        Component fromOriginComponent = new Resource();
        Component toOriginComponent = new Resource();
        CapabilityDefinition capability = new CapabilityDefinition();
        RequirementDefinition requirement = new RequirementDefinition();
        RelationshipInfo reqAndRelationshipPair = new RelationshipInfo();
        ComponentInstance toInstance = new ComponentInstance();
        Map<String, Component> componentCache = new HashMap<>();
        boolean result;
        capability.setPath(new ArrayList<>());
        reqAndRelationshipPair.setCapability("cap");
        requirement.setPath(new ArrayList<>());
        reqAndRelationshipPair.setRequirement("req");

        Mockito.when(capabiltyRequirementConvertor.buildSubstitutedName(any(), any(), any(), any(), any()))
            .thenReturn(Either.left("buildCapNameRes"));

        // default test
        result = Deencapsulation.invoke(testSubject, "buildAndAddRequirement", toscaRequirements, fromOriginComponent,
            toOriginComponent, capability, requirement, reqAndRelationshipPair, toInstance, componentCache);
        Assert.assertNotNull(result);
        Assert.assertTrue(result);
    }

    @Test
    public void testIsRequirementBelongToRelation() throws Exception {

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
        Assert.assertNotNull(result);
    }

    @Test
    public void testIsRequirementBelongToRelationWithNonAtomicComponent() {

        Component originComponent = new Service();
        RelationshipInfo reqAndRelationshipPair = new RelationshipInfo();
        RequirementDefinition requirement = new RequirementDefinition();
        String fromInstanceId = "";
        boolean result;

        // default test return true
        result = Deencapsulation.invoke(testSubject, "isRequirementBelongToRelation", originComponent,
            reqAndRelationshipPair, requirement, fromInstanceId);
        Assert.assertNotNull(result);
    }

    @Test
    public void testIsRequirementBelongToOwner() throws Exception {

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
        Assert.assertNotNull(result);
    }

    @Test
    public void testIsCvfc() throws Exception {

        Component component = new Resource();
        boolean result;

        component = new Service();

        result = Deencapsulation.invoke(testSubject, "isCvfc", component);
        Assert.assertNotNull(result);
    }

    @Test
    public void testConvertCapabilities() throws Exception {
        Component component = new Resource();
        SubstitutionMapping substitutionMappings = new SubstitutionMapping();
        Map<String, Component> componentCache = new HashMap<>();
        Either<SubstitutionMapping, ToscaError> result;

        Mockito.when(capabiltyRequirementConvertor.convertSubstitutionMappingCapabilities(componentCache, component))
            .thenReturn(Either.right(ToscaError.NODE_TYPE_CAPABILITY_ERROR));

        // default test return isRight
        result = Deencapsulation.invoke(testSubject, "convertCapabilities", component, substitutionMappings,
            componentCache);
        Assert.assertNotNull(result);
    }

    @Test
    public void testConvertCapabilities_1() throws Exception {
        Component component = new Resource();
        ToscaNodeType nodeType = new ToscaNodeType();
        Map<String, DataTypeDefinition> dataTypes = new HashMap<>();
        Either<ToscaNodeType, ToscaError> result;

        Map<String, ToscaCapability> capabilities = new HashMap<>();
        capabilities.put("key", new ToscaCapability());

        // default test
        result = Deencapsulation
            .invoke(testSubject, "convertCapabilities", new HashMap<>(), component, nodeType, dataTypes);
        Assert.assertNotNull(result);
    }

    @Test
    public void testConvertToNodeTemplateArtifacts() throws Exception {
        Map<String, ToscaArtifactDataDefinition> container = new HashMap<>();
        ToscaArtifactDataDefinition art = new ToscaArtifactDataDefinition();
        art.setFile("test_file");
        art.setType("test_type");
        Map<String, ToscaTemplateArtifact> result;
        container.put("test_art", art);
        result = Deencapsulation.invoke(testSubject, "convertToNodeTemplateArtifacts", container);
        Assert.assertNotNull(result);
        Assert.assertTrue(MapUtils.isNotEmpty(result));
        Assert.assertTrue(result.get("test_art").getFile().equals("test_file"));
        Assert.assertTrue(result.get("test_art").getType().equals("test_type"));
    }

    @Test
    public void testGetProxyNodeTypeInterfacesNoInterfaces() {
        Component service = new Service();
        Optional<Map<String, Object>> proxyNodeTypeInterfaces =
                testSubject.getProxyNodeTypeInterfaces(service, DATA_TYPES);
        Assert.assertFalse(proxyNodeTypeInterfaces.isPresent());
    }

    @Test
    public void testGetProxyNodeTypeInterfaces() {
        Component service = getTestComponent();
        Optional<Map<String, Object>> proxyNodeTypeInterfaces =
                testSubject.getProxyNodeTypeInterfaces(service, DATA_TYPES);
        Assert.assertTrue(proxyNodeTypeInterfaces.isPresent());
        Map<String, Object> componentInterfaces = proxyNodeTypeInterfaces.get();
        Assert.assertNotNull(componentInterfaces);
    }


    @Test
    public void testGetProxyNodeTypePropertiesComponentNull() {
        Optional<Map<String, ToscaProperty>> proxyNodeTypeProperties =
                testSubject.getProxyNodeTypeProperties(null, DATA_TYPES);
        Assert.assertFalse(proxyNodeTypeProperties.isPresent());
    }

    @Test
    public void testGetProxyNodeTypePropertiesNoProperties() {
        Component service = new Service();
        Optional<Map<String, ToscaProperty>> proxyNodeTypeProperties =
                testSubject.getProxyNodeTypeProperties(service, DATA_TYPES);
        Assert.assertFalse(proxyNodeTypeProperties.isPresent());
    }

    @Test
    public void testGetProxyNodeTypeProperties() {
        Component service = getTestComponent();
        service.setProperties(Arrays.asList(createMockProperty("componentPropStr", "Default String Prop"),
                createMockProperty("componentPropInt", null)));
        Optional<Map<String, ToscaProperty>> proxyNodeTypeProperties =
                testSubject.getProxyNodeTypeProperties(service, DATA_TYPES);
        Assert.assertTrue(proxyNodeTypeProperties.isPresent());
        Map<String, ToscaProperty> componentProperties = proxyNodeTypeProperties.get();
        Assert.assertNotNull(componentProperties);
        Assert.assertEquals(2, componentProperties.size());
    }

    @Test
    public void testAddInputsToPropertiesNoInputs() {
        Component service = getTestComponent();
        service.setProperties(Arrays.asList(createMockProperty("componentPropStr", "Default String Prop"),
                createMockProperty("componentPropInt", null)));
        Optional<Map<String, ToscaProperty>> proxyNodeTypePropertiesResult =
                testSubject.getProxyNodeTypeProperties(service, DATA_TYPES);

        Assert.assertTrue(proxyNodeTypePropertiesResult.isPresent());
        Map<String, ToscaProperty> proxyNodeTypeProperties = proxyNodeTypePropertiesResult.get();
        testSubject.addInputsToProperties(DATA_TYPES, null, proxyNodeTypeProperties);
        Assert.assertNotNull(proxyNodeTypeProperties);
        Assert.assertEquals(2, proxyNodeTypeProperties.size());
        testSubject.addInputsToProperties(DATA_TYPES, new ArrayList<>(), proxyNodeTypeProperties);
        Assert.assertEquals(2, proxyNodeTypeProperties.size());
    }

    @Test
    public void testAddInputsToPropertiesWithInputs() {
        Component service = getTestComponent();
        service.setProperties(Arrays.asList(createMockProperty("componentPropStr", "Default String Prop"),
                createMockProperty("componentPropInt", null)));
        service.setInputs(Arrays.asList(createMockInput("componentInputStr1",
                "Default String Input1"), createMockInput("componentInputStr2", "Default String Input2")));
        Optional<Map<String, ToscaProperty>> proxyNodeTypePropertiesResult =
                testSubject.getProxyNodeTypeProperties(service, DATA_TYPES);

        Assert.assertTrue(proxyNodeTypePropertiesResult.isPresent());
        Map<String, ToscaProperty> proxyNodeTypeProperties = proxyNodeTypePropertiesResult.get();
        testSubject.addInputsToProperties(DATA_TYPES, service.getInputs(), proxyNodeTypeProperties);
        Assert.assertNotNull(proxyNodeTypeProperties);
        Assert.assertEquals(4, proxyNodeTypeProperties.size());
    }

    @Test
    public void testAddInputsToPropertiesOnlyInputs() {
        Component service = getTestComponent();
        service.setInputs(Arrays.asList(createMockInput("componentInputStr1",
                "Default String Input1"), createMockInput("componentInputStr2", "Default String Input2")));
        Optional<Map<String, ToscaProperty>> proxyNodeTypePropertiesResult =
                testSubject.getProxyNodeTypeProperties(service, DATA_TYPES);

        Assert.assertTrue(proxyNodeTypePropertiesResult.isPresent());
        Map<String, ToscaProperty> proxyNodeTypeProperties = proxyNodeTypePropertiesResult.get();
        testSubject.addInputsToProperties(DATA_TYPES, service.getInputs(), proxyNodeTypeProperties);
        Assert.assertNotNull(proxyNodeTypeProperties);
        Assert.assertEquals(2, proxyNodeTypeProperties.size());
    }

    @Test
    public void testOperationImplementationInProxyNodeTypeNotPresent() {
        Component service = getTestComponent();
        InterfaceDefinition interfaceDefinition =
                service.getInterfaces().get("normalizedServiceComponentName-interface");
        interfaceDefinition.setOperations(new HashMap<>());
        final OperationDataDefinition operation = new OperationDataDefinition();
        operation.setName("start");
        operation.setDescription("op description");
        final ArtifactDataDefinition implementation = new ArtifactDataDefinition();
        implementation.setArtifactName("createBPMN.bpmn");
        operation.setImplementation(implementation);
        interfaceDefinition.getOperations().put(operation.getName(), operation);
        service.getInterfaces().put("normalizedServiceComponentName-interface", interfaceDefinition);
        service.setInputs(Arrays.asList(createMockInput("componentInputStr1",
                "Default String Input1"), createMockInput("componentInputStr2", "Default String Input2")));
        Optional<Map<String, Object>> proxyNodeTypeInterfaces =
                testSubject.getProxyNodeTypeInterfaces(service, DATA_TYPES);
        Assert.assertTrue(proxyNodeTypeInterfaces.isPresent());
        Map<String, Object> componentInterfaces = proxyNodeTypeInterfaces.get();
        Assert.assertNotNull(componentInterfaces);
    }

    private Component getTestComponent() {
        Component component = new Service();
        component.setNormalizedName("normalizedServiceComponentName");
        InterfaceDefinition addedInterface = new InterfaceDefinition();
        addedInterface.setType("com.some.service.or.other.serviceName");
        final String interfaceType = "normalizedServiceComponentName-interface";
        component.setInterfaces(new HashMap<>());
        component.getInterfaces().put(interfaceType, addedInterface);
        return component;
    }

    private PropertyDefinition createMockProperty(String propertyName, String defaultValue){
        PropertyDefinition propertyDefinition = new PropertyDefinition();
        propertyDefinition.setName(propertyName);
        propertyDefinition.setType("string");
        propertyDefinition.setDefaultValue(defaultValue);
        return propertyDefinition;
    }

    private InputDefinition createMockInput(String inputName, String defaultValue){
        InputDefinition inputDefinition = new InputDefinition();
        inputDefinition.setName(inputName);
        inputDefinition.setType("string");
        inputDefinition.setDefaultValue(defaultValue);
        return inputDefinition;
    }

}
