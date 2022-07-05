/*

 * Copyright (c) 2018 Huawei Intellectual Property.

 *

 * Licensed under the Apache License, Version 2.0 (the "License");

 * you may not use this file except in compliance with the License.

 * You may obtain a copy of the License at

 *

 *     http://www.apache.org/licenses/LICENSE-2.0

 *

 * Unless required by applicable law or agreed to in writing, software

 * distributed under the License is distributed on an "AS IS" BASIS,

 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.

 * See the License for the specific language governing permissions and

 * limitations under the License.

 */
package org.openecomp.sdc.be.model.jsonjanusgraph.utils;

import java.util.HashMap;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;

import org.mockito.junit.jupiter.MockitoExtension;
import org.openecomp.sdc.be.dao.jsongraph.types.VertexTypeEnum;
import org.openecomp.sdc.be.datatypes.elements.ArtifactDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.AttributeDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.CapabilityDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ComponentInstanceDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ListCapabilityDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.MapAttributesDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.MapCapabilityProperty;
import org.openecomp.sdc.be.datatypes.elements.MapPropertiesDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.RelationshipInstDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.AttributeDefinition;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.CapabilityDefinition;
import org.openecomp.sdc.be.model.CapabilityRequirementRelationship;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.OutputDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.RelationshipInfo;
import org.openecomp.sdc.be.model.RequirementCapabilityRelDef;
import org.openecomp.sdc.be.model.RelationshipTypeDefinition;
import org.openecomp.sdc.be.model.RelationshipImpl;

import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.Component;


import org.openecomp.sdc.be.model.jsonjanusgraph.datamodel.TopologyTemplate;
import org.openecomp.sdc.be.model.jsonjanusgraph.datamodel.NodeType;
import org.openecomp.sdc.be.model.jsonjanusgraph.datamodel.ToscaElementTypeEnum;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.openecomp.sdc.be.ui.model.OperationUi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;


@ExtendWith(MockitoExtension.class)
public class ModelConverterTest {
    @InjectMocks
    private ModelConverter test;

    @Test
    public void testConvertToToscaElementService()
    {
        Service service = new Service();
        service.setComponentType(ComponentTypeEnum.SERVICE);
        TopologyTemplate template = test.convertToToscaElement(service);
        assertEquals(ToscaElementTypeEnum.TOPOLOGY_TEMPLATE, template.getToscaType());
    }

    @Test
    public void testConvertToToscaElementResource()
    {
        Resource resource = new Resource();
        resource.setComponentType(ComponentTypeEnum.RESOURCE);
        NodeType nodeType = test.convertToToscaElement(resource);
        assertEquals(ToscaElementTypeEnum.NODE_TYPE, nodeType.getToscaType());
    }

    @Test
    public void testConvertFromToscaElementService()
    {
        TopologyTemplate topologyTemplate = new TopologyTemplate();
        topologyTemplate.setComponentType(ComponentTypeEnum.SERVICE);
        Component component = test.convertFromToscaElement(topologyTemplate);
        assertEquals(ToscaElementTypeEnum.TOPOLOGY_TEMPLATE.getValue(), component.getToscaType());

        topologyTemplate.setComponentType(ComponentTypeEnum.PRODUCT);
        component = test.convertFromToscaElement(topologyTemplate);
        assertEquals(ToscaElementTypeEnum.TOPOLOGY_TEMPLATE.getValue(), component.getToscaType());

        NodeType nodeType = new NodeType();
        nodeType.setComponentType(ComponentTypeEnum.RESOURCE);
        topologyTemplate.setToscaType(ToscaElementTypeEnum.NODE_TYPE);
        component = test.convertFromToscaElement(nodeType);
        assertEquals(ToscaElementTypeEnum.NODE_TYPE.getValue(), component.getToscaType());

        topologyTemplate.setComponentType(ComponentTypeEnum.RESOURCE_INSTANCE);
        assertNull(test.convertFromToscaElement(topologyTemplate));
    }

    @Test
    public void testConvertFromToscaElementServiceWithSelfCapabilities()
    {
        TopologyTemplate topologyTemplate = new TopologyTemplate();

        Map<String, MapPropertiesDataDefinition> capabilitiesProperties = CapabilityTestUtils
                .createCapPropsForTopologyTemplate(topologyTemplate);

        topologyTemplate.setCapabilitiesProperties(capabilitiesProperties);

        topologyTemplate.setComponentType(ComponentTypeEnum.SERVICE);
        Component component = test.convertFromToscaElement(topologyTemplate);
        assertEquals(ToscaElementTypeEnum.TOPOLOGY_TEMPLATE.getValue(), component.getToscaType());
    }

    @Test
    public void testConvertFromToscaElementResource()
    {
        TopologyTemplate topologyTemplate = new TopologyTemplate();
        topologyTemplate.setComponentType(ComponentTypeEnum.RESOURCE);
        Component component = test.convertFromToscaElement(topologyTemplate);
        assertEquals(ToscaElementTypeEnum.TOPOLOGY_TEMPLATE.getValue(), component.getToscaType());
    }

    @Test
    public void testConvertFromToscaElementResourceType() {
        TopologyTemplate topologyTemplate = new TopologyTemplate();
        topologyTemplate.setComponentType(ComponentTypeEnum.RESOURCE);
        topologyTemplate.setResourceType(ResourceTypeEnum.PNF);
        Resource resource = test.convertFromToscaElement(topologyTemplate);
        assertEquals(ResourceTypeEnum.PNF, resource.getResourceType());
    }

    @Test
    public void testConvertFromToscaElementResourceOutputs() {
        final TopologyTemplate topologyTemplate = new TopologyTemplate();
        topologyTemplate.setComponentType(ComponentTypeEnum.RESOURCE);
        final OutputDefinition outputDefinition = new OutputDefinition();
        final Map<String, AttributeDataDefinition> map = new HashMap<>();
        map.put("mock", outputDefinition);
        topologyTemplate.setOutputs(map);
        final Resource resource = test.convertFromToscaElement(topologyTemplate);
        assertNotNull(resource.getOutputs());
        assertFalse(resource.getOutputs().isEmpty());
    }

    @Test
    public void testConvertFromToscaElementResourceComponentInstancesOutputs() {
        final TopologyTemplate topologyTemplate = new TopologyTemplate();
        topologyTemplate.setComponentType(ComponentTypeEnum.RESOURCE);

        final Map<String, MapAttributesDataDefinition> instOutputs = new HashMap<>();
        final MapAttributesDataDefinition mapAttributesDataDefinition = new MapAttributesDataDefinition();
        final AttributeDefinition attributeDefinition = new AttributeDefinition();
        final Map<String, AttributeDataDefinition> mapToscaDataDefinition = new HashMap<>();
        mapToscaDataDefinition.put("mock", attributeDefinition);
        mapAttributesDataDefinition.setMapToscaDataDefinition(mapToscaDataDefinition);
        instOutputs.put("mock", mapAttributesDataDefinition);
        topologyTemplate.setInstOutputs(instOutputs);

        final Map<String, ComponentInstanceDataDefinition> componentInstanceDataDefinitionMap = new HashMap<>();
        componentInstanceDataDefinitionMap.put("mock", new ComponentInstance());
        topologyTemplate.setComponentInstances(componentInstanceDataDefinitionMap);

        final Resource resource = test.convertFromToscaElement(topologyTemplate);
        assertNotNull(resource);
        assertNotNull(resource.getComponentInstancesOutputs());
        assertFalse(resource.getComponentInstancesOutputs().isEmpty());
    }

    @Test
    public void testIsAtomicComponent() {
        Resource component = new Resource();
        component.setComponentType(ComponentTypeEnum.SERVICE);
        assertFalse(test.isAtomicComponent(component));

        component.setComponentType(ComponentTypeEnum.RESOURCE);
        assertTrue(test.isAtomicComponent(component));

        ResourceTypeEnum resourceType = null;
        assertFalse(test.isAtomicComponent(resourceType));
    }

    @Test
    public void testGetVertexType()
    {
        VertexTypeEnum result;
        Resource component = new Resource();
        component.setComponentType(ComponentTypeEnum.SERVICE);
        assertEquals("topology_template", test.getVertexType(component).getName());
        component.setComponentType(ComponentTypeEnum.RESOURCE);
        assertEquals("node_type", test.getVertexType(component).getName());

        assertEquals(VertexTypeEnum.TOPOLOGY_TEMPLATE, test.getVertexType("Service"));
        assertEquals(VertexTypeEnum.NODE_TYPE, test.getVertexType("VFC"));
    }

    @Test
    public void testConvertRelation()
    {
        RelationshipTypeDefinition relationshipDef = new RelationshipTypeDefinition();
        relationshipDef.setFromId("formId");
        relationshipDef.setToId("toId");
        relationshipDef.setOriginUI(true);
        RequirementCapabilityRelDef result = ModelConverter.convertRelation(relationshipDef);

        assertEquals("formId", result.getFromNode());
        assertEquals("toId", result.getToNode());
        assertEquals(true, result.isOriginUI());
        assertEquals(1, result.getRelationships().size());
    }

    @Test
    public void testConvertRelationToToscaRelation()
    {
        RequirementCapabilityRelDef reqCap = new RequirementCapabilityRelDef();
        reqCap.setOriginUI(true);
        reqCap.setFromNode("fromNode");
        reqCap.setToNode("toNode");
        List<CapabilityRequirementRelationship> list = new LinkedList<>();
        CapabilityRequirementRelationship relationship = new CapabilityRequirementRelationship();
        RelationshipInfo info = new RelationshipInfo();
        info.setCapabilityOwnerId("capOwnerId");
        info.setId("id");
        info.setCapabilityUid("capUid");
        info.setRequirementOwnerId("reqOwnerId");
        info.setRequirementUid("reqUid");
        info.setRequirement("req");
        info.setCapability("cap");
        RelationshipImpl relationshipImpl = new RelationshipImpl();
        relationshipImpl.setType("type");
        info.setRelationships(relationshipImpl);
        relationship.setRelation(info);
        list.add(relationship);
        reqCap.setRelationships(list);

        List<RelationshipInstDataDefinition> result = ModelConverter.convertRelationToToscaRelation(reqCap);
        assertEquals(1, result.size());
        assertEquals("capOwnerId", result.get(0).getCapabilityOwnerId());
        assertEquals("id", result.get(0).getUniqueId());
        assertEquals("capUid", result.get(0).getCapabilityId());
        assertEquals("reqOwnerId", result.get(0).getRequirementOwnerId());
        assertEquals("reqUid", result.get(0).getRequirementId());
        assertEquals("req", result.get(0).getRequirement());
        assertEquals("cap", result.get(0).getCapability());
        assertEquals("type", result.get(0).getType());
        assertEquals(true, result.get(0).isOriginUI());
        assertEquals("fromNode", result.get(0).getFromId());
        assertEquals("toNode", result.get(0).getToId());
    }

    @Test
    public void testConvertRelationTemplateToToscaRelation()
    {
        RequirementCapabilityRelDef reqCap = new RequirementCapabilityRelDef();
        reqCap.setOriginUI(true);
        reqCap.setFromNode("fromNode");
        reqCap.setToNode("toNode");
        List<CapabilityRequirementRelationship> list = new LinkedList<>();
        CapabilityRequirementRelationship relationship = new CapabilityRequirementRelationship();
        RelationshipInfo info = new RelationshipInfo();
        info.setCapabilityOwnerId("capOwnerId");
        info.setId("id");
        info.setCapabilityUid("capUid");
        info.setRequirementOwnerId("reqOwnerId");
        info.setRequirementUid("reqUid");
        info.setRequirement("req");
        info.setCapability("cap");
        RelationshipImpl relationshipImpl = new RelationshipImpl();
        relationshipImpl.setType("type");
        info.setRelationships(relationshipImpl);
        relationship.setRelation(info);
        OperationUi operationUi = new OperationUi();
        operationUi.setInterfaceType("tosca.interfaces.relationship.Configure");
        operationUi.setOperationType("add_source");
        final ArtifactDataDefinition artifactDataDefinition = new ArtifactDataDefinition();
        artifactDataDefinition.setArtifactName("impl");
        operationUi.setImplementation(artifactDataDefinition);
        relationship.setOperations(List.of(operationUi));
        list.add(relationship);
        reqCap.setRelationships(list);

        List<RelationshipInstDataDefinition> result = ModelConverter.convertRelationToToscaRelation(reqCap);
        assertEquals(1, result.size());
        assertEquals(false, result.get(0).getInterfaces().isEmpty());
        assertEquals(false, result.get(0).getInterfaces().getListToscaDataDefinition().get(0).getOperations().isEmpty());
    }

    @Test
    public void testConvertToMapOfMapCapabilityPropertiesonvertRelation()
    {
        Map<String, List<CapabilityDefinition>> capabilities = new HashMap<>();
        MapCapabilityProperty result = ModelConverter.convertToMapOfMapCapabilityProperties(capabilities, "ownerId", true);
        assertNotNull(result);
        assertEquals(0, result.getMapToscaDataDefinition().size());

        List<CapabilityDefinition> list = new LinkedList<>();
        CapabilityDefinition capDef = new CapabilityDefinition();
        List<ComponentInstanceProperty> properties = new LinkedList<>();
        ComponentInstanceProperty property = new ComponentInstanceProperty();
        properties.add(property);
        capDef.setProperties(properties);
        list.add(capDef);
        capabilities.put("test", list);
        result = ModelConverter.convertToMapOfMapCapabilityProperties(capabilities, "ownerId", true);
        assertEquals(1, result.getMapToscaDataDefinition().size());
    }

    @Test
    public void testBuildCapabilityPropertyKey()
    {
        CapabilityDefinition capDef = new CapabilityDefinition();
        capDef.setOwnerId("owner");
        String result = ModelConverter.buildCapabilityPropertyKey(true,"type","name", "capId", capDef);

        assertEquals("capId#owner#type#name", result);
    }

    @Test
    public void testConvertToMapOfMapCapabiltyProperties()
    {
        Map<String, List<CapabilityDefinition>> capabilities = new HashMap<>();
        List<CapabilityDefinition> list = new LinkedList<>();
        CapabilityDefinition capDef = new CapabilityDefinition();
        List<ComponentInstanceProperty> properties = new LinkedList<>();
        ComponentInstanceProperty property = new ComponentInstanceProperty();
        properties.add(property);
        capDef.setProperties(properties);
        list.add(capDef);
        capabilities.put("test", list);

        MapCapabilityProperty result = ModelConverter.convertToMapOfMapCapabiltyProperties(capabilities, "ownerId", true);

        assertEquals(1, result.getMapToscaDataDefinition().size());
        assertNotNull(result.getMapToscaDataDefinition().get("ownerId#ownerId#test#null"));
    }

    @Test
    public void testGetCapabilitiesMapFromMapObject()
    {
        assertNull(ModelConverter.getCapabilitiesMapFromMapObject(null, null));

        Map<String, ListCapabilityDataDefinition> toscaCapabilities = new HashMap<>();
        Map<String, MapPropertiesDataDefinition> toscaCapPropMap = new HashMap<>();
        ListCapabilityDataDefinition dataDefList = new ListCapabilityDataDefinition();
        List<CapabilityDataDefinition> capDataDefList = new LinkedList<>();
        CapabilityDataDefinition capDataDef = new CapabilityDataDefinition();
        capDataDef.setName("test");
        capDataDefList.add(capDataDef);
        dataDefList.setListToscaDataDefinition(capDataDefList);
        MapPropertiesDataDefinition dataDefMap = new MapPropertiesDataDefinition();
        Map<String, PropertyDataDefinition> propDataMap = new HashMap<>();
        PropertyDataDefinition propertyDataDefinition = new PropertyDataDefinition();
        propDataMap.put("propMap", propertyDataDefinition);
        dataDefMap.setMapToscaDataDefinition(propDataMap);
        toscaCapabilities.put("prop", dataDefList);
        toscaCapPropMap.put("prop#test", dataDefMap);

        Map<String, List<CapabilityDefinition>> result = ModelConverter.getCapabilitiesMapFromMapObject(toscaCapabilities, toscaCapPropMap);
        assertEquals(1, result.size());
    }
}
