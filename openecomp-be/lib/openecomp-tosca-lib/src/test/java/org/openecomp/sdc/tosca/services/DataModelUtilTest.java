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

package org.openecomp.sdc.tosca.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.collections4.MapUtils;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.onap.sdc.tosca.datatypes.model.AttributeDefinition;
import org.onap.sdc.tosca.datatypes.model.CapabilityDefinition;
import org.onap.sdc.tosca.datatypes.model.EntrySchema;
import org.onap.sdc.tosca.datatypes.model.GroupDefinition;
import org.onap.sdc.tosca.datatypes.model.Import;
import org.onap.sdc.tosca.datatypes.model.NodeTemplate;
import org.onap.sdc.tosca.datatypes.model.NodeType;
import org.onap.sdc.tosca.datatypes.model.ParameterDefinition;
import org.onap.sdc.tosca.datatypes.model.PolicyDefinition;
import org.onap.sdc.tosca.datatypes.model.PropertyDefinition;
import org.onap.sdc.tosca.datatypes.model.RelationshipTemplate;
import org.onap.sdc.tosca.datatypes.model.RequirementAssignment;
import org.onap.sdc.tosca.datatypes.model.RequirementDefinition;
import org.onap.sdc.tosca.datatypes.model.ServiceTemplate;
import org.onap.sdc.tosca.datatypes.model.SubstitutionMapping;
import org.onap.sdc.tosca.datatypes.model.TopologyTemplate;
import org.onap.sdc.tosca.datatypes.model.heatextend.ParameterDefinitionExt;
import org.openecomp.sdc.errors.CoreException;
import org.openecomp.sdc.tosca.datatypes.ToscaFunctions;

/**
 * @author shiria
 * @since September 15, 2016.
 */
@RunWith(MockitoJUnitRunner.class)
public class DataModelUtilTest {

    private static final String REQUIREMENT_ID = "requirementId";
    private static final String REQUIREMENT_DEFINITION_ID = "requirementDefinitionId";
    private static final String NODE_TEMPLATE_ID = "nodeTemplateId";
    private static final String NODE_TYPE_ID = "nodeTypeId";
    private static final String CAPABILITY_ID = "capabilityId";
    private static final String PROPERTY_ID = "propertyId";
    private static final String NODE_TYPE_KEY = "nodeTypeKey";
    private static final String TEMPLATE_NAME = "templateName";
    private static final String OUTPUT_ID = "outputId";
    private static final String REQUIREMENT_KEY = "requirementKey";
    private static final String NODE_ID = "nodeId";
    private static final String PARAMETER_ID = "parameterId";
    private static final String ENTRY_ID = "entryId";
    private static final String PROPERTY_DEF_TYPE = "propertyDefType";

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testAddSubstitutionMappingTopolgyTemplateNull() {
        ServiceTemplate serviceTemplate = new ServiceTemplate();
        DataModelUtil.addSubstitutionMapping(serviceTemplate, new SubstitutionMapping());

        Assert.assertNotNull(serviceTemplate.getTopology_template());
        Assert.assertNotNull(serviceTemplate.getTopology_template().getSubstitution_mappings());
    }

    @Test
    public void testAddSubstitutionMapping() throws Exception {
        thrown.expect(CoreException.class);
        thrown.expectMessage(
            "Invalid action, can't add 'Substitution Mapping' to 'Service Template', 'Service Template' entity is NULL.");
        DataModelUtil.addSubstitutionMapping(null, new SubstitutionMapping());
    }

    @Test
    public void testGetDirectivesNodeTemplateNull() {
        assertTrue(DataModelUtil.getDirectives(null).isEmpty());
    }

    @Test
    public void testGetDirectivesWhenDirectivesNull() {
        assertTrue(DataModelUtil.getDirectives(new NodeTemplate()).isEmpty());
    }

    @Test
    public void testGetDirectives() {
        NodeTemplate nodeTemplate = new NodeTemplate();
        nodeTemplate.setDirectives(Collections.singletonList("directive"));

        Assert.assertEquals(1, DataModelUtil.getDirectives(nodeTemplate).size());
    }

    @Test
    public void testAddSubstitutionMappingReq() {
        ServiceTemplate serviceTemplate = new ServiceTemplate();
        DataModelUtil.addSubstitutionMappingReq(serviceTemplate,
            REQUIREMENT_ID, Collections.singletonList("requirement"));

        Assert.assertNotNull(serviceTemplate.getTopology_template().getSubstitution_mappings().getRequirements());
        Assert.assertEquals(1,
            serviceTemplate.getTopology_template().getSubstitution_mappings().getRequirements().size());
    }

    @Test
    public void testAddSubstitutionMappingReqServiceTemplateNull() {
        thrown.expect(CoreException.class);
        thrown.expectMessage(
            "Invalid action, can't add 'Substitution Mapping Requirements' to 'Service Template', 'Service Template' entity is NULL.");
        DataModelUtil.addSubstitutionMappingReq(null, REQUIREMENT_ID, Collections.emptyList());
    }

    @Test
    public void testAddSubstitutionMappingCapabilityServiceTemplateNull() {
        thrown.expect(CoreException.class);
        thrown.expectMessage(
            "Invalid action, can't add 'Substitution Mapping Capabilities' to 'Service Template', 'Service Template' entity is NULL.");
        DataModelUtil.addSubstitutionMappingCapability(null, CAPABILITY_ID, Collections.emptyList());
    }

    @Test
    public void testAddSubstitutionMappingCapability() {
        ServiceTemplate serviceTemplate = new ServiceTemplate();
        DataModelUtil.addSubstitutionMappingCapability(serviceTemplate,
            CAPABILITY_ID, Collections.singletonList("requirement"));

        Assert.assertNotNull(serviceTemplate.getTopology_template().getSubstitution_mappings().getCapabilities());
        Assert.assertEquals(1,
            serviceTemplate.getTopology_template().getSubstitution_mappings().getCapabilities().size());
    }

    @Test
    public void testGetNodeTemplatesNull() {
        ServiceTemplate serviceTemplate = new ServiceTemplate();
        serviceTemplate.setTopology_template(new TopologyTemplate());

        Map<String, NodeTemplate> nodeTemplateMap = DataModelUtil.getNodeTemplates(serviceTemplate);

        Assert.assertNotNull(nodeTemplateMap);
        assertTrue(MapUtils.isEmpty(nodeTemplateMap));
    }

    @Test
    public void testGetNodeTemplates() {
        ServiceTemplate serviceTemplate = new ServiceTemplate();
        serviceTemplate.setTopology_template(new TopologyTemplate());

        Map<String, NodeTemplate> nodeTemplateMap = Stream.of(new AbstractMap.SimpleEntry<>("nodeTemplate1", new
            NodeTemplate())).collect(
            Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue));

        serviceTemplate.getTopology_template().setNode_templates(nodeTemplateMap);

        nodeTemplateMap = DataModelUtil.getNodeTemplates(serviceTemplate);

        Assert.assertNotNull(nodeTemplateMap);
        Assert.assertEquals(1, nodeTemplateMap.size());
    }

    @Test
    public void testGetGroupsNull() {
        ServiceTemplate serviceTemplate = new ServiceTemplate();
        serviceTemplate.setTopology_template(new TopologyTemplate());

        Map<String, GroupDefinition> nodeTemplateMap = DataModelUtil.getGroups(serviceTemplate);

        Assert.assertNotNull(nodeTemplateMap);
        assertTrue(MapUtils.isEmpty(nodeTemplateMap));
    }

    @Test
    public void testGetGroups() {
        ServiceTemplate serviceTemplate = new ServiceTemplate();
        serviceTemplate.setTopology_template(new TopologyTemplate());

        Map<String, GroupDefinition> nodeTemplateMap = Stream.of(new AbstractMap.SimpleEntry<>("group1", new
            GroupDefinition())).collect(
            Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue));

        serviceTemplate.getTopology_template().setGroups(nodeTemplateMap);

        nodeTemplateMap = DataModelUtil.getGroups(serviceTemplate);

        Assert.assertNotNull(nodeTemplateMap);
        Assert.assertEquals(1, nodeTemplateMap.size());
    }

    @Test
    public void testAddNodeTemplateServiceTemplateNull() {
        thrown.expect(CoreException.class);
        thrown.expectMessage(
            "Invalid action, can't add 'Node Template' to 'Service Template', 'Service Template' entity is NULL.");
        DataModelUtil.addNodeTemplate(null, "123", new NodeTemplate());
    }

    @Test
    public void testAddNodeTemplate() {
        ServiceTemplate serviceTemplate = new ServiceTemplate();
        DataModelUtil.addNodeTemplate(serviceTemplate, NODE_TEMPLATE_ID, new NodeTemplate());

        assertEquals(1, serviceTemplate.getTopology_template().getNode_templates().size());
    }

    @Test
    public void testAddNodeTypeCapabilitiesDefCapabilitiesNull() {
        NodeType nodeType = new NodeType();
        DataModelUtil.addNodeTypeCapabilitiesDef(nodeType, null);

        assertNull(nodeType.getCapabilities());
    }

    @Test(expected = CoreException.class)
    public void testAddNodeTypeCapabilitiesDefThrowsException() {
        Map<String, CapabilityDefinition> capabilityDefinitionMap =
            Stream.of(new AbstractMap.SimpleEntry<>(CAPABILITY_ID, new
                CapabilityDefinition())).collect(
                Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue));

        DataModelUtil.addNodeTypeCapabilitiesDef(null, capabilityDefinitionMap);
    }

    @Test
    public void testAddNodeTypeCapabilitiesDef() {
        NodeType nodeType = new NodeType();
        Map<String, CapabilityDefinition> capabilityDefinitionMap =
            Stream.of(new AbstractMap.SimpleEntry<>(CAPABILITY_ID, new
                CapabilityDefinition())).collect(
                Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue));

        DataModelUtil.addNodeTypeCapabilitiesDef(nodeType, capabilityDefinitionMap);

        Assert.assertEquals(1, nodeType.getCapabilities().size());
    }

    @Test
    public void testSetNodeTypeCapabilitiesDefCapabilitiesNull() {
        NodeType nodeType = new NodeType();
        DataModelUtil.setNodeTypeCapabilitiesDef(nodeType, null);

        assertNull(nodeType.getCapabilities());
    }

    @Test(expected = CoreException.class)
    public void testSetNodeTypeCapabilitiesDefThrowsException() {
        Map<String, CapabilityDefinition> capabilityDefinitionMap =
            Stream.of(new AbstractMap.SimpleEntry<>(CAPABILITY_ID,
                new CapabilityDefinition())).collect(
                Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue));

        DataModelUtil.setNodeTypeCapabilitiesDef(null, capabilityDefinitionMap);
    }

    @Test
    public void testSetNodeTypeCapabilitiesDef() {
        NodeType nodeType = new NodeType();
        Map<String, CapabilityDefinition> capabilityDefinitionMap =
            Stream.of(new AbstractMap.SimpleEntry<>(CAPABILITY_ID, new
                CapabilityDefinition())).collect(
                Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue));

        DataModelUtil.setNodeTypeCapabilitiesDef(nodeType, capabilityDefinitionMap);

        Assert.assertEquals(1, nodeType.getCapabilities().size());
    }

    @Test
    public void testGetCapabilityDefinitionCapabilityDefinitionIdNull() {
        NodeType nodeType = new NodeType();
        nodeType.setCapabilities(new HashMap<>());
        assertFalse(DataModelUtil.getCapabilityDefinition(nodeType, null).isPresent());
    }

    @Test
    public void testGetCapabilityDefinition() {
        NodeType nodeType = new NodeType();
        nodeType.setCapabilities(Collections.singletonMap("capabilityDefinitionId", new CapabilityDefinition()));
        assertTrue(DataModelUtil.getCapabilityDefinition(nodeType, "capabilityDefinitionId").isPresent());
    }

    @Test
    public void testAddPolicyDefinitionThrowsException() {
        thrown.expect(CoreException.class);
        thrown.expectMessage(
            "Invalid action, can't add 'Policy Definition' to 'Service Template', 'Service Template' entity is NULL.");
        DataModelUtil.addPolicyDefinition(null, "policyId", new PolicyDefinition());
    }

    @Test
    public void testAddPolicyDefinition() {
        ServiceTemplate serviceTemplate = new ServiceTemplate();
        DataModelUtil.addPolicyDefinition(serviceTemplate, "policyId", new PolicyDefinition());
        assertEquals(1, serviceTemplate.getTopology_template().getPolicies().size());
    }

    @Test
    public void testAddNodeTypeThrowsException() {
        thrown.expect(CoreException.class);
        thrown.expectMessage(
            "Invalid action, can't add 'Node Type' to 'Service Template', 'Service Template' entity is NULL.");
        DataModelUtil.addNodeType(null, NODE_TYPE_ID, new NodeType());
    }

    @Test
    public void testAddNodeType() {
        ServiceTemplate serviceTemplate = new ServiceTemplate();
        DataModelUtil.addNodeType(serviceTemplate, NODE_TYPE_ID, new NodeType());

        assertEquals(1, serviceTemplate.getNode_types().size());
    }

    @Test
    public void testAddRelationshipTemplateThrowsException() {
        thrown.expect(CoreException.class);
        thrown.expectMessage(
            "Invalid action, can't add 'Relationship Template' to 'Service Template', 'Service Template' entity is NULL.");
        DataModelUtil.addRelationshipTemplate(null, "relationshipTemplateId", new RelationshipTemplate());
    }

    @Test
    public void testAddRelationshipTemplate() {
        ServiceTemplate serviceTemplate = new ServiceTemplate();
        DataModelUtil.addRelationshipTemplate(serviceTemplate, "relationshipTemplateId", new RelationshipTemplate());

        assertEquals(1, serviceTemplate.getTopology_template().getRelationship_templates().size());
    }

    @Test
    public void testAddRequirementAssignmentThrowsException() {
        thrown.expect(CoreException.class);
        thrown.expectMessage(
            "Invalid action, can't add 'Requirement Assignment' to 'Node Template', 'Node Template' entity is NULL.");
        DataModelUtil.addRequirementAssignment(null, REQUIREMENT_ID, new RequirementAssignment());
    }

    @Test(expected = CoreException.class)
    public void testAddRequirementAssignmentNodeNotAssigned() {
        DataModelUtil.addRequirementAssignment(new NodeTemplate(), REQUIREMENT_ID, new RequirementAssignment());
    }

    @Test
    public void testAddRequirementAssignment() {
        NodeTemplate nodeTemplate = new NodeTemplate();
        RequirementAssignment requirementAssignment = new RequirementAssignment();
        requirementAssignment.setNode("node");
        DataModelUtil.addRequirementAssignment(nodeTemplate, REQUIREMENT_ID, requirementAssignment);

        assertEquals(1, nodeTemplate.getRequirements().size());
    }

    @Test
    public void testCreateAttachmentRequirementAssignment() {
        assertNotNull(DataModelUtil.createAttachmentRequirementAssignment("node"));
    }

    @Test
    public void testGetNodeTemplate() {
        ServiceTemplate serviceTemplate = new ServiceTemplate();
        serviceTemplate.setTopology_template(new TopologyTemplate());

        assertNull(DataModelUtil.getNodeTemplate(serviceTemplate, NODE_TEMPLATE_ID));
    }

    @Test
    public void testGetNodeType() {
        assertNull(DataModelUtil.getNodeType(new ServiceTemplate(), NODE_TYPE_ID));
    }

    @Test
    public void testGetRequirementDefinitionRequirementIdIsNull() {
        assertFalse(DataModelUtil.getRequirementDefinition(new NodeType(), null).isPresent());
    }

    @Test
    public void testGetRequirementDefinitionListIsEmpty() {
        NodeType nodeType = new NodeType();

        nodeType.setRequirements(Collections.emptyList());
        assertFalse(DataModelUtil.getRequirementDefinition(new NodeType(), REQUIREMENT_DEFINITION_ID).isPresent());
    }

    @Test
    public void testGetRequirementDefinitionWrongKey() {
        Map<String, RequirementDefinition> requirementDefinitionMap =
            Stream.of(new AbstractMap.SimpleEntry<>(REQUIREMENT_ID, new RequirementDefinition()))
                .collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue));

        assertFalse(DataModelUtil.getRequirementDefinition(Collections.singletonList(requirementDefinitionMap),
            "wrongKey").isPresent());
    }

    @Test
    public void testAddGroupToTopologyTemplateThrowsException() {
        thrown.expect(CoreException.class);
        thrown.expectMessage(
            "Invalid action, can't add 'Group Definition' to 'Service Template', 'Service Template' entity is NULL.");
        DataModelUtil.addGroupDefinitionToTopologyTemplate(null, "groupId", new GroupDefinition());
    }

    @Test
    public void testAddGroupToTopologyTemplate() {
        ServiceTemplate serviceTemplate = new ServiceTemplate();

        DataModelUtil.addGroupDefinitionToTopologyTemplate(serviceTemplate, "groupId", new GroupDefinition());

        assertEquals(1, serviceTemplate.getTopology_template().getGroups().size());
    }

    @Test
    public void testAddRequirementToList() {
        Map<String, RequirementDefinition> requirementDefinitionMap = new HashMap<>();
        requirementDefinitionMap.put("requirmentDefinitionId", new RequirementDefinition());

        List<Map<String, RequirementDefinition>> mapList = new ArrayList<>();

        DataModelUtil.addRequirementToList(mapList, requirementDefinitionMap);

        assertEquals(1, mapList.size());
    }

    @Test
    public void testAddGroupMemberGroupNotExist() {
        ServiceTemplate serviceTemplate = new ServiceTemplate();
        serviceTemplate.setTopology_template(new TopologyTemplate());
        DataModelUtil.addGroupMember(serviceTemplate, "groupName", "memberId");

        assertNull(serviceTemplate.getTopology_template().getGroups());
    }

    @Test
    public void testAddGroupMemberGroup() {
        ServiceTemplate serviceTemplate = new ServiceTemplate();
        serviceTemplate.setTopology_template(new TopologyTemplate());
        Map<String, GroupDefinition> groupDefinitionMap =
            Stream.of(new AbstractMap.SimpleEntry<>("groupName", new GroupDefinition()))
                .collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue));

        serviceTemplate.getTopology_template().setGroups(groupDefinitionMap);

        DataModelUtil.addGroupMember(serviceTemplate, "groupName", "memberId");

        assertEquals(1, serviceTemplate.getTopology_template().getGroups().size());
    }

    @Test
    public void testCreateParameterDefinition() {
        assertNotNull(DataModelUtil
            .createParameterDefinition("parameterType", "description", true, Collections.emptyList(), new
                EntrySchema(), null));
    }

    @Test
    public void testCreateRequirement() {
        assertNotNull(DataModelUtil.createRequirement("capability", "node", "relationShip", new Object[1]));
    }

    @Test
    public void testCreateEntrySchema() {
        assertNull(DataModelUtil.createEntrySchema(null, null, null));
    }

    @Test
    public void testCreateGetInputPropertyValueFromListParameter() {
        Map inputPropertyMap = DataModelUtil.createGetInputPropertyValueFromListParameter("inputPropertyList", 1,
            "nestedPropertyName");

        assertNotNull(inputPropertyMap.get(ToscaFunctions.GET_INPUT.getFunctionName()));
    }

    @Test
    public void testConvertPropertyDefToParameterDefNull() {
        assertNull(DataModelUtil.convertPropertyDefToParameterDef(null));
    }

    @Test
    public void testConvertPropertyDefToParameterDef() {
        PropertyDefinition propertyDefinition = new PropertyDefinition();
        propertyDefinition.setType(PROPERTY_DEF_TYPE);

        ParameterDefinitionExt parameterDefinitionExt =
            DataModelUtil.convertPropertyDefToParameterDef(propertyDefinition);
        assertNotNull(parameterDefinitionExt);
        assertEquals(PROPERTY_DEF_TYPE, parameterDefinitionExt.getType());
    }

    @Test
    public void testConvertAttributeDefToParameterDefAttDefNull() {
        assertNull(DataModelUtil.convertAttributeDefToParameterDef(null, null));
    }

    @Test
    public void testConvertAttributeDefToParameterDef() {
        ParameterDefinitionExt parameterDefinitionExt =
            DataModelUtil.convertAttributeDefToParameterDef(new AttributeDefinition(), Collections.emptyMap());

        assertTrue(MapUtils.isEmpty((Map) parameterDefinitionExt.getValue()));
    }

    @Test
    public void testIsNodeTemplateTrue() {
        ServiceTemplate serviceTemplate = new ServiceTemplate();
        TopologyTemplate topologyTemplate = new TopologyTemplate();

        serviceTemplate.setTopology_template(topologyTemplate);

        Map<String, NodeTemplate> nodeTemplateMap =
            Stream.of(new AbstractMap.SimpleEntry<>(ENTRY_ID, new NodeTemplate()))
                .collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue));
        topologyTemplate.setNode_templates(nodeTemplateMap);

        assertTrue(DataModelUtil.isNodeTemplate(ENTRY_ID, serviceTemplate));
    }

    @Test
    public void testIsNodeTemplateEntryMissing() {
        ServiceTemplate serviceTemplate = new ServiceTemplate();
        TopologyTemplate topologyTemplate = new TopologyTemplate();

        serviceTemplate.setTopology_template(topologyTemplate);
        topologyTemplate.setNode_templates(Collections.emptyMap());

        assertFalse(DataModelUtil.isNodeTemplate(ENTRY_ID, serviceTemplate));
    }

    @Test(expected = CoreException.class)
    public void testAddInputParameterToTopologyTemplateServiceTemplateNull() {
        DataModelUtil.addInputParameterToTopologyTemplate(null, null, null);
    }

    @Test
    public void testAddInputParameterToTopologyTemplate() {
        ServiceTemplate serviceTemplate = new ServiceTemplate();
        DataModelUtil.addInputParameterToTopologyTemplate(serviceTemplate, PARAMETER_ID, new ParameterDefinition());

        assertEquals(1, serviceTemplate.getTopology_template().getInputs().size());
    }

    @Test(expected = CoreException.class)
    public void testAddOutputParameterToTopologyTemplateServiceTemplateNull() {
        DataModelUtil.addOutputParameterToTopologyTemplate(null, null, null);
    }

    @Test
    public void testAddOutputParameterToTopologyTemplate() {
        ServiceTemplate serviceTemplate = new ServiceTemplate();
        DataModelUtil.addOutputParameterToTopologyTemplate(serviceTemplate, PARAMETER_ID, new ParameterDefinition());

        assertEquals(1, serviceTemplate.getTopology_template().getOutputs().size());
    }

    @Test
    public void testGetNodeTemplateRequirementsNodeTemplateNull() {
        assertNull(DataModelUtil.getNodeTemplateRequirements(null));
    }

    @Test
    public void testGetNodeTemplateRequirementsNodeTemplateRequirementsNull() {
        assertTrue(MapUtils.isEmpty(DataModelUtil.getNodeTemplateRequirements(new NodeTemplate())));
    }

    @Test
    public void testGetNodeTemplateRequirementListNodeTemplateRequirementListNull() {
        assertNull(DataModelUtil.getNodeTemplateRequirementList(new NodeTemplate()));
    }

    @Test
    public void testGetNodeTemplateRequirementList() {
        Map<String, RequirementAssignment> requirementAssignmentMap =
            Stream.of(new AbstractMap.SimpleEntry<>(REQUIREMENT_ID, new RequirementAssignment()))
                .collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue));

        List<Map<String, RequirementAssignment>> requirementList = new ArrayList<>();
        requirementList.add(requirementAssignmentMap);

        NodeTemplate nodeTemplate = new NodeTemplate();
        nodeTemplate.setRequirements(requirementList);

        assertEquals(requirementList.size(), DataModelUtil.getNodeTemplateRequirementList(nodeTemplate).size());
    }

    @Test
    public void testGetRequirementAssignmentRequirementAssignmentListEmpty() {
        assertFalse(DataModelUtil.getRequirementAssignment(null, null).isPresent());
    }

    @Test
    public void testGetRequirementAssignmentRequirementAssignmentListDoseNotContainsKeyPassed() {
        assertFalse(DataModelUtil.getRequirementAssignment(
            Collections.singletonList(new HashMap<>()), REQUIREMENT_KEY).isPresent());
    }

    @Test
    public void testGetRequirementAssignmentRequirementAssignmentListContainsKeyPassed() {
        Map<String, RequirementAssignment> requirementAssignmentMap =
            Stream.of(new AbstractMap.SimpleEntry<>(REQUIREMENT_KEY, new RequirementAssignment()))
                .collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue));

        assertTrue(DataModelUtil.getRequirementAssignment(
            Collections.singletonList(requirementAssignmentMap), REQUIREMENT_KEY).isPresent());
    }

    @Test
    public void testRemoveRequirementsDefinition() {
        Map<String, RequirementDefinition> requirementDefinitionMap =
            Stream.of(new AbstractMap.SimpleEntry<>(REQUIREMENT_KEY, new RequirementDefinition()))
                .collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue));

        List<Map<String, RequirementDefinition>> requirementList =
            Stream.of(requirementDefinitionMap).collect(Collectors.toList());
        DataModelUtil.removeRequirementsDefinition(requirementList, REQUIREMENT_KEY);

        assertTrue(requirementList.isEmpty());
    }

    @Test
    public void testRemoveRequirementsAssignment() {
        Map<String, RequirementAssignment> requirementDefinitionMap =
            Stream.of(new AbstractMap.SimpleEntry<>(REQUIREMENT_KEY, new RequirementAssignment()))
                .collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue));

        List<Map<String, RequirementAssignment>> requirementList =
            Stream.of(requirementDefinitionMap).collect(Collectors.toList());
        DataModelUtil.removeRequirementsAssignment(requirementList, REQUIREMENT_KEY);

        assertTrue(requirementList.isEmpty());
    }

    @Test
    public void testRemoveRequirementAssignmentNodeTemplate() {

        RequirementAssignment requirementAssignment = new RequirementAssignment();
        requirementAssignment.setNode(NODE_ID);
        requirementAssignment.setCapability(CAPABILITY_ID);
        requirementAssignment.setRelationship("relationshipId");
        Map<String, RequirementAssignment> requirementDefinitionMap =
            Stream.of(new AbstractMap.SimpleEntry<>(REQUIREMENT_KEY, requirementAssignment))
                .collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue));

        List<Map<String, RequirementAssignment>> requirementList =
            Stream.of(requirementDefinitionMap).collect(Collectors.toList());
        NodeTemplate nodeTemplate = new NodeTemplate();
        nodeTemplate.setRequirements(requirementList);

        RequirementAssignment requirementAssignment1 = new RequirementAssignment();
        requirementAssignment1.setNode(NODE_ID);
        requirementAssignment1.setCapability(CAPABILITY_ID);
        requirementAssignment1.setRelationship("relationshipId");
        DataModelUtil.removeRequirementAssignment(nodeTemplate, REQUIREMENT_KEY, requirementAssignment);

        assertTrue(nodeTemplate.getRequirements().isEmpty());
    }

    @Test
    public void testGetNamespaceSuffixNull() {
        assertNull(DataModelUtil.getNamespaceSuffix(null));
    }

    @Test
    public void testGetNamespaceSuffix() {
        assertEquals("suffix", DataModelUtil.getNamespaceSuffix("name.suffix"));
    }

    @Test
    public void testIsImportAddedToServiceTemplateImportIdExists() {
        Map<String, Import> importMap = Stream.of(new AbstractMap.SimpleEntry<>("imp1", new Import())).collect
            (Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue));

        assertTrue(DataModelUtil.isImportAddedToServiceTemplate(Collections.singletonList(importMap), "imp1"));
    }

    @Test
    public void testIsImportAddedToServiceTemplateImportIdNotExists() {
        Map<String, Import> importMap = Stream.of(new AbstractMap.SimpleEntry<>("imp1", new Import())).collect
            (Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue));

        assertFalse(DataModelUtil.isImportAddedToServiceTemplate(Collections.singletonList(importMap), "imp2"));
    }

    @Test
    public void testGetOutputParameterOutputIsNull() {
        ServiceTemplate serviceTemplate = new ServiceTemplate();
        serviceTemplate.setTopology_template(new TopologyTemplate());

        assertNull(DataModelUtil.getOuputParameter(serviceTemplate, OUTPUT_ID));
    }

    @Test
    public void testGetOutputParameter() {
        ServiceTemplate serviceTemplate = new ServiceTemplate();
        serviceTemplate.setTopology_template(new TopologyTemplate());

        Map<String, ParameterDefinition> outputMap = Stream.of(new AbstractMap.SimpleEntry<>(OUTPUT_ID, new
            ParameterDefinition())).collect
            (Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue));
        serviceTemplate.getTopology_template().setOutputs(outputMap);

        assertNotNull(DataModelUtil.getOuputParameter(serviceTemplate, OUTPUT_ID));
    }

    @Test
    public void testGetInputParametersNull() {
        ServiceTemplate serviceTemplate = new ServiceTemplate();
        serviceTemplate.setTopology_template(new TopologyTemplate());

        assertNull(DataModelUtil.getInputParameters(serviceTemplate));
    }

    @Test
    public void testGetInputParameters() {
        ServiceTemplate serviceTemplate = new ServiceTemplate();
        serviceTemplate.setTopology_template(new TopologyTemplate());

        serviceTemplate.getTopology_template().setInputs(new HashMap<>());

        assertNotNull(DataModelUtil.getInputParameters(serviceTemplate));
    }

    @Test
    public void testGetRelationshipTemplatesNull() {
        ServiceTemplate serviceTemplate = new ServiceTemplate();
        serviceTemplate.setTopology_template(new TopologyTemplate());

        assertNull(DataModelUtil.getInputParameters(serviceTemplate));
    }

    @Test
    public void testGetRelationshipTemplates() {
        ServiceTemplate serviceTemplate = new ServiceTemplate();
        serviceTemplate.setTopology_template(new TopologyTemplate());

        serviceTemplate.getTopology_template().setRelationship_templates(new HashMap<>());

        assertNotNull(DataModelUtil.getRelationshipTemplates(serviceTemplate));
    }

    @Test
    public void testGetPropertyValuePropertyNotPresent() {
        NodeTemplate nodeTemplate = new NodeTemplate();
        Map<String, Object> objectMap = Stream.of(new AbstractMap.SimpleEntry<>(PROPERTY_ID, new PropertyDefinition()))
            .collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue));

        nodeTemplate.setProperties(objectMap);
        assertNull(DataModelUtil.getPropertyValue(nodeTemplate, "wrongId"));
    }

    @Test
    public void testGetPropertyValue() {
        NodeTemplate nodeTemplate = new NodeTemplate();
        Map<String, Object> objectMap = Stream.of(new AbstractMap.SimpleEntry<>(PROPERTY_ID, new PropertyDefinition()))
            .collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue));

        nodeTemplate.setProperties(objectMap);
        assertNotNull(DataModelUtil.getPropertyValue(nodeTemplate, PROPERTY_ID));
    }

    @Test
    public void testGetNodeTemplatePropertiesPresent() {
        ServiceTemplate serviceTemplate = new ServiceTemplate();
        serviceTemplate.setTopology_template(new TopologyTemplate());
        NodeTemplate nodeTemplate = new NodeTemplate();
        nodeTemplate.setProperties(new HashMap<>());
        Map<String, NodeTemplate> nodeTemplateMap =
            Stream.of(new AbstractMap.SimpleEntry<>(NODE_TEMPLATE_ID, nodeTemplate))
                .collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue));

        serviceTemplate.getTopology_template().setNode_templates(nodeTemplateMap);

        assertNotNull(DataModelUtil.getNodeTemplateProperties(serviceTemplate, NODE_TEMPLATE_ID));
    }

    @Test
    public void testGetNodeTemplatePropertiesNotPresent() {
        ServiceTemplate serviceTemplate = new ServiceTemplate();
        serviceTemplate.setTopology_template(new TopologyTemplate());
        NodeTemplate nodeTemplate = new NodeTemplate();
        nodeTemplate.setProperties(new HashMap<>());

        serviceTemplate.getTopology_template().setNode_templates(new HashMap<>());

        assertNull(DataModelUtil.getNodeTemplateProperties(serviceTemplate, NODE_TEMPLATE_ID));
    }

    @Test
    public void testGetSubstitutionMappingsNullCheck() {
        ServiceTemplate serviceTemplate = new ServiceTemplate();
        serviceTemplate.setTopology_template(new TopologyTemplate());
        assertNull(DataModelUtil.getSubstitutionMappings(serviceTemplate));
    }

    @Test
    public void testGetClonedObject() {
        RequirementAssignment requirementAssignment = new RequirementAssignment();
        requirementAssignment.setNode(NODE_ID);

        Object obj = DataModelUtil.getClonedObject(requirementAssignment, requirementAssignment.getClass());

        assertTrue(obj instanceof RequirementAssignment);
        RequirementAssignment assignment = (RequirementAssignment) obj;
        assertNotSame(assignment, requirementAssignment);
    }

    @Test
    public void testGetCloneObject() {
        RequirementAssignment requirementAssignment = new RequirementAssignment();
        requirementAssignment.setNode(NODE_ID);

        assertNotSame(DataModelUtil.getClonedObject(requirementAssignment), requirementAssignment);
    }

    @Test
    public void testGetCloneObjectSerializableObject() {
        List<String> stringList = new ArrayList<>();

        assertNotSame(DataModelUtil.getClonedObject(stringList), stringList);
    }

    @Test
    public void testAddSubstitutionFilteringProperty() {
        NodeTemplate nodeTemplate = new NodeTemplate();
        nodeTemplate.setProperties(new HashMap<>());

        DataModelUtil.addSubstitutionFilteringProperty(TEMPLATE_NAME, nodeTemplate, 5);

        assertNotNull(nodeTemplate.getProperties().get(ToscaConstants.SERVICE_TEMPLATE_FILTER_PROPERTY_NAME));
    }

    @Test
    public void testAddBindingReqFromPortToCompute() {
        NodeTemplate nodeTemplate = new NodeTemplate();

        DataModelUtil.addBindingReqFromPortToCompute("computeNodeId", nodeTemplate);

        assertNotNull(nodeTemplate.getRequirements());
        assertEquals(1, nodeTemplate.getRequirements().size());
    }

    @Test
    public void testCreateSubstitutionTemplateSubMappingCapabilityAndRequirementNull() {
        Map<String, Map<String, List<String>>> mapping = new HashMap<>();

        Map<String, List<String>> capabilityStringList = new HashMap<>();
        capabilityStringList.put(CAPABILITY_ID, Collections.singletonList("test"));

        Map<String, List<String>> requirementStringList = new HashMap<>();
        capabilityStringList.put(REQUIREMENT_ID, Collections.singletonList("test"));

        mapping.put(ToscaConstants.CAPABILITY, capabilityStringList);
        mapping.put(ToscaConstants.REQUIREMENT, requirementStringList);

        NodeType nodeType = new NodeType();

        Map<String, CapabilityDefinition> capabilityMap = new HashMap<>();
        capabilityMap.put(CAPABILITY_ID, new CapabilityDefinition());

        Map<String, RequirementDefinition> requirementMap = new HashMap<>();
        requirementMap.put(REQUIREMENT_ID, new RequirementDefinition());

        nodeType.setRequirements(Collections.singletonList(requirementMap));
        nodeType.setCapabilities(capabilityMap);

        assertNotNull(DataModelUtil.createSubstitutionTemplateSubMapping(NODE_TYPE_KEY, nodeType, mapping));
    }

    @Test
    public void testCreateSubstitutionTemplateSubMapping() {
        Map<String, Map<String, List<String>>> mapping = new HashMap<>();
        mapping.put(ToscaConstants.CAPABILITY, null);
        mapping.put(ToscaConstants.REQUIREMENT, null);

        assertNotNull(DataModelUtil.createSubstitutionTemplateSubMapping(NODE_TYPE_KEY, new NodeType(), mapping));
    }

    @Test
    public void testAddNodeTemplateCapability() {
        NodeTemplate nodeTemplate = new NodeTemplate();

        DataModelUtil.addNodeTemplateCapability(nodeTemplate, CAPABILITY_ID, null, null);
        assertNotNull(nodeTemplate.getCapabilities());
    }

    @Test
    public void testAddSubstitutionNodeTypeRequirements() {

        NodeType nodeType = new NodeType();

        Map<String, RequirementDefinition> requirementDefinitionMap = new HashMap<>();
        requirementDefinitionMap.put(REQUIREMENT_ID, new RequirementDefinition());

        DataModelUtil.addSubstitutionNodeTypeRequirements(
            nodeType, Collections.singletonList(requirementDefinitionMap), TEMPLATE_NAME);

        assertEquals(1, nodeType.getRequirements().size());
        assertNotNull(nodeType.getRequirements().get(0).get("requirementId_templateName"));
    }

    @Test
    public void testIsNodeTemplateSectionMissingFromServiceTemplateNodeTemplateMissing() {
        ServiceTemplate serviceTemplate = new ServiceTemplate();
        serviceTemplate.setTopology_template(new TopologyTemplate());

        assertTrue(DataModelUtil.isNodeTemplateSectionMissingFromServiceTemplate(serviceTemplate));
    }

    @Test
    public void testIsNodeTemplateSectionMissingFromServiceTemplateTopologyTemplateMissing() {
        ServiceTemplate serviceTemplate = new ServiceTemplate();

        assertTrue(DataModelUtil.isNodeTemplateSectionMissingFromServiceTemplate(serviceTemplate));
    }

    @Test
    public void testIsNodeTemplateSectionMissingFromServiceTemplate() {
        ServiceTemplate serviceTemplate = new ServiceTemplate();
        serviceTemplate.setTopology_template(new TopologyTemplate());

        Map<String, NodeTemplate> nodeTemplateMap = new HashMap<>();
        nodeTemplateMap.put(NODE_TEMPLATE_ID, new NodeTemplate());
        serviceTemplate.getTopology_template().setNode_templates(nodeTemplateMap);

        assertFalse(DataModelUtil.isNodeTemplateSectionMissingFromServiceTemplate(serviceTemplate));
    }

    @Test
    public void testGetRelationshipTemplate() {
        RelationshipTemplate relationshipTemplate = new RelationshipTemplate();
        String testingRelationshipType = "testingRelationshipType";
        relationshipTemplate.setType(testingRelationshipType);
        TopologyTemplate topologyTemplate = new TopologyTemplate();
        topologyTemplate.setRelationship_templates(new HashMap<>());
        String relationId = "rtest";
        topologyTemplate.getRelationship_templates().put(relationId, relationshipTemplate);
        ServiceTemplate serviceTemplate = new ServiceTemplate();
        serviceTemplate.setTopology_template(topologyTemplate);

        Optional<RelationshipTemplate> relationshipTemplateOut =
            DataModelUtil.getRelationshipTemplate(serviceTemplate, relationId);
        Assert.assertNotNull(relationshipTemplateOut);
        Assert.assertEquals(true, relationshipTemplateOut.isPresent());
        Assert.assertEquals(testingRelationshipType, relationshipTemplateOut.get().getType());
    }

    @Test
    public void testGetEmptyRelationshipTemplate() {
        ServiceTemplate serviceTemplate = new ServiceTemplate();
        String relationId = "rtest";
        Optional<RelationshipTemplate> relationshipTemplateOut =
            DataModelUtil.getRelationshipTemplate(serviceTemplate, relationId);
        Assert.assertNotNull(relationshipTemplateOut);
        Assert.assertEquals(false, relationshipTemplateOut.isPresent());
    }

    @Test
    public void testAddNodeTemplateProperty() {
        NodeTemplate nodeTemplate = new NodeTemplate();

        DataModelUtil.addNodeTemplateProperty(nodeTemplate, PROPERTY_ID, null);

        assertNotNull(nodeTemplate.getProperties());
    }
}
