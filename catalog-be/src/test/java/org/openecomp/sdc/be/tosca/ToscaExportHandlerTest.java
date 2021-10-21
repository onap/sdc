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

import fj.data.Either;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.be.components.BeConfDependentTest;
import org.openecomp.sdc.be.components.utils.PropertyDataDefinitionBuilder;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.datatypes.elements.ArtifactDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.OperationDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ToscaArtifactDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.OriginTypeEnum;
import org.openecomp.sdc.be.model.*;
import org.openecomp.sdc.be.model.cache.ApplicationDataTypeCache;
import org.openecomp.sdc.be.model.category.CategoryDefinition;
import org.openecomp.sdc.be.model.category.SubCategoryDefinition;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.InterfaceLifecycleOperation;
import org.openecomp.sdc.be.tosca.model.*;
import org.openecomp.sdc.be.tosca.utils.InputConverter;
import org.openecomp.sdc.be.tosca.utils.OutputConverter;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum.VF;
import static org.openecomp.sdc.be.tosca.PropertyConvertor.PropertyType.PROPERTY;

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

    @Before
    public void setUpMock() {
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

        when(applicationDataTypeCache.getAll(null)).thenReturn(Either.left(new HashMap<>()));
        when(capabilityRequirementConverter.convertRequirements(any(Map.class), any(Resource.class),
            any(ToscaNodeType.class))).thenReturn(Either.left(new ToscaNodeType()));
        when(interfaceLifecycleOperation.getAllInterfaceLifecycleTypes(any()))
            .thenReturn(Either.left(Collections.emptyMap()));

        // default test when component is Resource
        result = testSubject.exportComponent(component);
        Assert.assertNotNull(result);

        component = getNewService();
        when(capabilityRequirementConverter.convertRequirements(any(Map.class), any(Service.class),
            any(ToscaNodeType.class))).thenReturn(Either.left(new ToscaNodeType()));
        when(applicationDataTypeCache.getAll(null)).thenReturn(Either.right(JanusGraphOperationStatus.NOT_FOUND));

        // default test when component is Service
        result = testSubject.exportComponent(component);
        Assert.assertNotNull(result);
    }

    @Test
    public void testExportComponentInterface() throws Exception {
        Component component = getNewResource();
        Either<ToscaRepresentation, ToscaError> result;

        ((Resource) component).setInterfaces(new HashMap<>());

        when(applicationDataTypeCache.getAll(null)).thenReturn(Either.right(JanusGraphOperationStatus.NOT_FOUND));
        when(interfaceLifecycleOperation.getAllInterfaceLifecycleTypes(any()))
            .thenReturn(Either.left(Collections.emptyMap()));
        // default test when convertInterfaceNodeType is right
        result = testSubject.exportComponentInterface(component, false);
        Assert.assertNotNull(result);

        when(applicationDataTypeCache.getAll(null)).thenReturn(Either.left(new HashMap<>()));
        when(capabilityRequirementConverter.convertRequirements(any(Map.class), any(Resource.class),
            any(ToscaNodeType.class))).thenReturn(Either.left(new ToscaNodeType()));

        // default test when convertInterfaceNodeType is left
        result = testSubject.exportComponentInterface(component, false);
        Assert.assertNotNull(result);
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
    public void testGetInterfaceFilename() throws Exception {
        String artifactName = "artifact.name";
        String result;

        // default test
        result = ToscaExportHandler.getInterfaceFilename(artifactName);
        Assert.assertNotNull(result);
    }

    @Test
    public void testConvertNodeTemplatesWhenComponentIsService() throws Exception {
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

        when(capabilityRequirementConverter.getOriginComponent(any(Map.class), any(ComponentInstance.class))).thenReturn(Either.left(component));
        when(capabilityRequirementConverter
            .convertComponentInstanceCapabilities(any(ComponentInstance.class), any(Map.class), any(ToscaNodeTemplate.class)))
            .thenReturn(Either.left(new ToscaNodeTemplate()));
        when(interfaceLifecycleOperation.getAllInterfaceLifecycleTypes(any())).thenReturn(Either.left(Collections.emptyMap()));
        when(applicationDataTypeCache.getAll(null)).thenReturn(Either.left(new HashMap<>()));
        when(capabilityRequirementConverter.convertRequirements(any(Map.class), any(Resource.class), any(ToscaNodeType.class)))
            .thenReturn(Either.left(new ToscaNodeType()));
        when(toscaOperationFacade.getToscaFullElement("uid")).thenReturn(Either.left(component));
        when(toscaOperationFacade.getToscaFullElement("sourceModelUid")).thenReturn(Either.left(component));
        when(toscaOperationFacade.getLatestByName("serviceProxy", null)).thenReturn(Either.left(new Resource()));
        when(toscaOperationFacade.getToscaElement(any(String.class), any(ComponentParametersView.class))).thenReturn(Either.left(new Resource()));

        final Map<String, String[]> substitutionMappingMap = new HashMap<>();
        final String[] array = {"value1", "value2"};
        substitutionMappingMap.put("key", array);
        when(capabilityRequirementConverter.convertSubstitutionMappingCapabilities(anyMap(), any(Component.class)))
            .thenReturn(Either.left(substitutionMappingMap));

        when(capabilityRequirementConverter
            .convertSubstitutionMappingRequirements(any(Map.class), any(Component.class), any(SubstitutionMapping.class)))
            .thenReturn(Either.left(new SubstitutionMapping()));

        // default test
        final Either<ToscaRepresentation, ToscaError> toscaRepresentationToscaErrorEither = testSubject.exportComponent(component);
        assertNotNull(toscaRepresentationToscaErrorEither);

    }

    @Test
    public void testConvertNodeTemplatesWhenComponentIsResource() throws Exception {
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

        when(capabilityRequirementConverter.getOriginComponent(any(Map.class), any(ComponentInstance.class))).thenReturn(Either.left(component));
        when(capabilityRequirementConverter
            .convertComponentInstanceCapabilities(any(ComponentInstance.class), any(Map.class), any(ToscaNodeTemplate.class)))
            .thenReturn(Either.left(new ToscaNodeTemplate()));
        when(interfaceLifecycleOperation.getAllInterfaceLifecycleTypes(any())).thenReturn(Either.left(Collections.emptyMap()));
        when(applicationDataTypeCache.getAll(null)).thenReturn(Either.left(new HashMap<>()));
        when(capabilityRequirementConverter.convertRequirements(any(Map.class), any(Resource.class), any(ToscaNodeType.class)))
            .thenReturn(Either.left(new ToscaNodeType()));
        when(toscaOperationFacade.getToscaFullElement("uid")).thenReturn(Either.left(component));
        when(toscaOperationFacade.getToscaFullElement("sourceModelUid")).thenReturn(Either.left(component));
        when(toscaOperationFacade.getLatestByName("serviceProxy", null)).thenReturn(Either.left(new Resource()));
        when(toscaOperationFacade.getToscaElement(any(String.class), any(ComponentParametersView.class))).thenReturn(Either.left(new Resource()));

        final Map<String, String[]> substitutionMappingMap = new HashMap<>();
        final String[] array = {"value1", "value2"};
        substitutionMappingMap.put("key", array);
        when(capabilityRequirementConverter.convertSubstitutionMappingCapabilities(anyMap(), any(Component.class)))
            .thenReturn(Either.left(substitutionMappingMap));

        when(capabilityRequirementConverter
            .convertSubstitutionMappingRequirements(any(Map.class), any(Component.class), any(SubstitutionMapping.class)))
            .thenReturn(Either.left(new SubstitutionMapping()));

        // default test
        final Either<ToscaRepresentation, ToscaError> toscaRepresentationToscaErrorEither = testSubject.exportComponent(component);
        assertNotNull(toscaRepresentationToscaErrorEither);

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
        component.setComponentInstances(componentInstances);

        component.setComponentInstancesInputs(componentInstancesInputs);
        component.setInvariantUUID("uuid");
        component.setUUID("uuid");
        component.setDescription("desc");

        componentCache.put("uid", component);

        when(capabilityRequirementConverter.getOriginComponent(any(Map.class), any(ComponentInstance.class))).thenReturn(Either.left(component));
        when(capabilityRequirementConverter
            .convertComponentInstanceCapabilities(any(ComponentInstance.class), any(Map.class), any(ToscaNodeTemplate.class)))
            .thenReturn(Either.right(ToscaError.GENERAL_ERROR));
        when(interfaceLifecycleOperation.getAllInterfaceLifecycleTypes(any())).thenReturn(Either.left(Collections.emptyMap()));
        when(applicationDataTypeCache.getAll(null)).thenReturn(Either.left(new HashMap<>()));
        when(capabilityRequirementConverter.convertRequirements(any(Map.class), any(Resource.class),
            any(ToscaNodeType.class))).thenReturn(Either.left(new ToscaNodeType()));

        // default test
        final Either<ToscaRepresentation, ToscaError> toscaRepresentationToscaErrorEither = testSubject.exportComponent(component);
        assertNotNull(toscaRepresentationToscaErrorEither);
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

        when(capabilityRequirementConverter.getOriginComponent(any(Map.class), any(ComponentInstance.class))).thenReturn(Either.right(false));
        when(interfaceLifecycleOperation.getAllInterfaceLifecycleTypes(any())).thenReturn(Either.left(Collections.emptyMap()));
        when(applicationDataTypeCache.getAll(null)).thenReturn(Either.left(new HashMap<>()));
        when(capabilityRequirementConverter.convertRequirements(any(Map.class), any(Resource.class),
            any(ToscaNodeType.class))).thenReturn(Either.left(new ToscaNodeType()));

        // default test
        final Either<ToscaRepresentation, ToscaError> toscaRepresentationToscaErrorEither = testSubject.exportComponent(component);
        assertNotNull(toscaRepresentationToscaErrorEither);
    }

    @Test
    public void testConvertNodeTemplatesWhenConvertComponentInstanceRequirmentsIsRight() {
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
        when(capabilityRequirementConverter.getOriginComponent(any(Map.class), any(ComponentInstance.class))).thenReturn(Either.left(component));
        when(interfaceLifecycleOperation.getAllInterfaceLifecycleTypes(any())).thenReturn(Either.left(Collections.emptyMap()));
        when(applicationDataTypeCache.getAll(null)).thenReturn(Either.left(new HashMap<>()));
        when(capabilityRequirementConverter.convertRequirements(any(Map.class), any(Resource.class),
            any(ToscaNodeType.class))).thenReturn(Either.left(new ToscaNodeType()));
        when(toscaOperationFacade.getToscaElement(any(String.class), any(ComponentParametersView.class)))
            .thenReturn(Either.right(StorageOperationStatus.BAD_REQUEST));

        // default test
        final Either<ToscaRepresentation, ToscaError> result = testSubject.exportComponent(component);
        assertNotNull(result);
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
