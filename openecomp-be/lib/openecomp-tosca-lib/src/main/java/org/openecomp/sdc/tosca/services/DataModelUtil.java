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
package org.openecomp.sdc.tosca.services;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.onap.sdc.tosca.datatypes.model.AttributeDefinition;
import org.onap.sdc.tosca.datatypes.model.CapabilityAssignment;
import org.onap.sdc.tosca.datatypes.model.CapabilityDefinition;
import org.onap.sdc.tosca.datatypes.model.Constraint;
import org.onap.sdc.tosca.datatypes.model.DataType;
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
import org.onap.sdc.tosca.services.ToscaExtensionYamlUtil;
import org.onap.sdc.tosca.services.YamlUtil;
import org.openecomp.core.utilities.CommonMethods;
import org.openecomp.sdc.errors.CoreException;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.tosca.datatypes.ToscaCapabilityType;
import org.openecomp.sdc.tosca.datatypes.ToscaFunctions;
import org.openecomp.sdc.tosca.datatypes.ToscaRelationshipType;
import org.openecomp.sdc.tosca.errors.InvalidAddActionNullEntityErrorBuilder;
import org.openecomp.sdc.tosca.errors.InvalidRequirementAssignmentErrorBuilder;
import org.openecomp.sdc.tosca.services.impl.ToscaAnalyzerServiceImpl;

/**
 * The type Data model util.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DataModelUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataModelUtil.class);
    private static final String SERVICE_TEMPLATE = "Service Template";
    private static final String NODE_TYPE = "Node Type";

    /**
     * Add substitution mapping.
     *
     * @param serviceTemplate     the service template
     * @param substitutionMapping the substitution mapping
     */
    public static void addSubstitutionMapping(ServiceTemplate serviceTemplate, SubstitutionMapping substitutionMapping) {
        if (serviceTemplate == null) {
            throw new CoreException(new InvalidAddActionNullEntityErrorBuilder("Substitution Mapping", SERVICE_TEMPLATE).build());
        }
        if (serviceTemplate.getTopology_template() == null) {
            serviceTemplate.setTopology_template(new TopologyTemplate());
        }
        serviceTemplate.getTopology_template().setSubstitution_mappings(substitutionMapping);
    }

    /**
     * Gets node template directives.
     *
     * @param nodeTemplate the node template
     * @return the directives
     */
    public static List<String> getDirectives(NodeTemplate nodeTemplate) {
        if (Objects.isNull(nodeTemplate) || Objects.isNull(nodeTemplate.getDirectives())) {
            return Collections.emptyList();
        }
        return nodeTemplate.getDirectives();
    }

    /**
     * Add substitution mapping req.
     *
     * @param serviceTemplate                    the service template
     * @param substitutionMappingRequirementId   the substitution mapping requirement id
     * @param substitutionMappingRequirementList the substitution mapping requirement list
     */
    public static void addSubstitutionMappingReq(ServiceTemplate serviceTemplate, String substitutionMappingRequirementId,
                                                 List<String> substitutionMappingRequirementList) {
        if (serviceTemplate == null) {
            throw new CoreException(new InvalidAddActionNullEntityErrorBuilder("Substitution Mapping Requirements", SERVICE_TEMPLATE).build());
        }
        if (serviceTemplate.getTopology_template() == null) {
            serviceTemplate.setTopology_template(new TopologyTemplate());
        }
        if (serviceTemplate.getTopology_template().getSubstitution_mappings() == null) {
            serviceTemplate.getTopology_template().setSubstitution_mappings(new SubstitutionMapping());
        }
        if (serviceTemplate.getTopology_template().getSubstitution_mappings().getRequirements() == null) {
            serviceTemplate.getTopology_template().getSubstitution_mappings().setRequirements(new HashMap<>());
        }
        serviceTemplate.getTopology_template().getSubstitution_mappings().getRequirements()
            .put(substitutionMappingRequirementId, substitutionMappingRequirementList);
    }

    /**
     * Add substitution mapping capability.
     *
     * @param serviceTemplate                   the service template
     * @param substitutionMappingCapabilityId   the substitution mapping capability id
     * @param substitutionMappingCapabilityList the substitution mapping capability list
     */
    public static void addSubstitutionMappingCapability(ServiceTemplate serviceTemplate, String substitutionMappingCapabilityId,
                                                        List<String> substitutionMappingCapabilityList) {
        if (serviceTemplate == null) {
            throw new CoreException(new InvalidAddActionNullEntityErrorBuilder("Substitution Mapping Capabilities", SERVICE_TEMPLATE).build());
        }
        if (serviceTemplate.getTopology_template() == null) {
            serviceTemplate.setTopology_template(new TopologyTemplate());
        }
        if (serviceTemplate.getTopology_template().getSubstitution_mappings() == null) {
            serviceTemplate.getTopology_template().setSubstitution_mappings(new SubstitutionMapping());
        }
        if (serviceTemplate.getTopology_template().getSubstitution_mappings().getCapabilities() == null) {
            serviceTemplate.getTopology_template().getSubstitution_mappings().setCapabilities(new HashMap<>());
        }
        serviceTemplate.getTopology_template().getSubstitution_mappings().getCapabilities()
            .putIfAbsent(substitutionMappingCapabilityId, substitutionMappingCapabilityList);
    }

    /**
     * Gets node templates from the service template.
     *
     * @param serviceTemplate the service template
     * @return the service template node templates and empty map if not present
     */
    public static Map<String, NodeTemplate> getNodeTemplates(ServiceTemplate serviceTemplate) {
        if (Objects.isNull(serviceTemplate) || Objects.isNull(serviceTemplate.getTopology_template()) || MapUtils
            .isEmpty(serviceTemplate.getTopology_template().getNode_templates())) {
            return new HashMap<>();
        }
        return serviceTemplate.getTopology_template().getNode_templates();
    }

    /**
     * Gets groups from the service template.
     *
     * @param serviceTemplate the service template
     * @return the service template groups and empty map if not present
     */
    public static Map<String, GroupDefinition> getGroups(ServiceTemplate serviceTemplate) {
        if (Objects.isNull(serviceTemplate) || Objects.isNull(serviceTemplate.getTopology_template()) || MapUtils
            .isEmpty(serviceTemplate.getTopology_template().getGroups())) {
            return new HashMap<>();
        }
        return serviceTemplate.getTopology_template().getGroups();
    }

    /**
     * Add node template.
     *
     * @param serviceTemplate the service template
     * @param nodeTemplateId  the node template id
     * @param nodeTemplate    the node template
     */
    public static void addNodeTemplate(ServiceTemplate serviceTemplate, String nodeTemplateId, NodeTemplate nodeTemplate) {
        if (serviceTemplate == null) {
            throw new CoreException(new InvalidAddActionNullEntityErrorBuilder("Node Template", SERVICE_TEMPLATE).build());
        }
        TopologyTemplate topologyTemplate = serviceTemplate.getTopology_template();
        if (Objects.isNull(topologyTemplate)) {
            topologyTemplate = new TopologyTemplate();
            serviceTemplate.setTopology_template(topologyTemplate);
        }
        if (topologyTemplate.getNode_templates() == null) {
            topologyTemplate.setNode_templates(new HashMap<>());
        }
        topologyTemplate.getNode_templates().put(nodeTemplateId, nodeTemplate);
    }

    /**
     * Add capabilities def to node type.
     *
     * @param nodeType     the node type
     * @param capabilities the capability definitions
     */
    public static void addNodeTypeCapabilitiesDef(NodeType nodeType, Map<String, CapabilityDefinition> capabilities) {
        if (MapUtils.isEmpty(capabilities)) {
            return;
        }
        if (nodeType == null) {
            throw new CoreException(new InvalidAddActionNullEntityErrorBuilder("Capability Definition", NODE_TYPE).build());
        }
        if (MapUtils.isEmpty(nodeType.getCapabilities())) {
            nodeType.setCapabilities(new HashMap<>());
        }
        for (Map.Entry<String, CapabilityDefinition> entry : capabilities.entrySet()) {
            nodeType.getCapabilities().put(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Set capabilities def to node type.
     *
     * @param nodeType     the node type
     * @param capabilities the capability definitions
     */
    public static void setNodeTypeCapabilitiesDef(NodeType nodeType, Map<String, CapabilityDefinition> capabilities) {
        if (MapUtils.isEmpty(capabilities)) {
            return;
        }
        if (nodeType == null) {
            throw new CoreException(new InvalidAddActionNullEntityErrorBuilder("Capability Definition", NODE_TYPE).build());
        }
        if (MapUtils.isEmpty(nodeType.getCapabilities())) {
            nodeType.setCapabilities(new HashMap<>());
        }
        if (MapUtils.isNotEmpty(capabilities)) {
            nodeType.setCapabilities(new HashMap<>());
            for (Map.Entry<String, CapabilityDefinition> entry : capabilities.entrySet()) {
                nodeType.getCapabilities().put(entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * Add policy definition.
     *
     * @param serviceTemplate  the service template
     * @param policyId         the policy id
     * @param policyDefinition the policy definition
     */
    public static void addPolicyDefinition(ServiceTemplate serviceTemplate, String policyId, PolicyDefinition policyDefinition) {
        if (serviceTemplate == null) {
            throw new CoreException(new InvalidAddActionNullEntityErrorBuilder("Policy Definition", SERVICE_TEMPLATE).build());
        }
        TopologyTemplate topologyTemplate = serviceTemplate.getTopology_template();
        if (Objects.isNull(topologyTemplate)) {
            topologyTemplate = new TopologyTemplate();
            serviceTemplate.setTopology_template(topologyTemplate);
        }
        if (topologyTemplate.getPolicies() == null) {
            topologyTemplate.setPolicies(new HashMap<>());
        }
        topologyTemplate.getPolicies().put(policyId, policyDefinition);
    }

    /**
     * Add node type.
     *
     * @param serviceTemplate the service template
     * @param nodeTypeId      the node type id
     * @param nodeType        the node type
     */
    public static void addNodeType(ServiceTemplate serviceTemplate, String nodeTypeId, NodeType nodeType) {
        if (serviceTemplate == null) {
            throw new CoreException(new InvalidAddActionNullEntityErrorBuilder(NODE_TYPE, SERVICE_TEMPLATE).build());
        }
        if (serviceTemplate.getNode_types() == null) {
            serviceTemplate.setNode_types(new HashMap<>());
        }
        serviceTemplate.getNode_types().put(nodeTypeId, nodeType);
    }

    public static void addDataType(final ServiceTemplate serviceTemplate, final String key, final DataType nodeTypeValue) {
        if (serviceTemplate == null) {
            throw new CoreException(new InvalidAddActionNullEntityErrorBuilder("Data Type", SERVICE_TEMPLATE).build());
        }
        if (serviceTemplate.getData_types() == null) {
            serviceTemplate.setData_types(new HashMap<>());
        }
        serviceTemplate.getData_types().put(key, nodeTypeValue);
    }

    /**
     * Add relationship template.
     *
     * @param serviceTemplate        the service template
     * @param relationshipTemplateId the relationship template id
     * @param relationshipTemplate   the relationship template
     */
    public static void addRelationshipTemplate(ServiceTemplate serviceTemplate, String relationshipTemplateId,
                                               RelationshipTemplate relationshipTemplate) {
        if (serviceTemplate == null) {
            throw new CoreException(new InvalidAddActionNullEntityErrorBuilder("Relationship Template", SERVICE_TEMPLATE).build());
        }
        if (serviceTemplate.getTopology_template() == null) {
            serviceTemplate.setTopology_template(new TopologyTemplate());
        }
        if (serviceTemplate.getTopology_template().getRelationship_templates() == null) {
            serviceTemplate.getTopology_template().setRelationship_templates(new HashMap<>());
        }
        serviceTemplate.getTopology_template().getRelationship_templates().put(relationshipTemplateId, relationshipTemplate);
    }

    /**
     * Add requirement assignment.
     *
     * @param nodeTemplate          the node template
     * @param requirementId         the requirement id
     * @param requirementAssignment the requirement assignment
     */
    public static void addRequirementAssignment(NodeTemplate nodeTemplate, String requirementId, RequirementAssignment requirementAssignment) {
        if (nodeTemplate == null) {
            throw new CoreException(new InvalidAddActionNullEntityErrorBuilder("Requirement Assignment", "Node Template").build());
        }
        if (requirementAssignment.getNode() == null) {
            throw new CoreException(new InvalidRequirementAssignmentErrorBuilder(requirementId).build());
        }
        Map<String, RequirementAssignment> requirement = new HashMap<>();
        requirement.put(requirementId, requirementAssignment);
        nodeTemplate.addRequirements(requirement);
    }

    /**
     * Creates a new requirement assignment object for attachment requirement.
     *
     * @param node the node
     * @return the attachment requirement assignment object
     */
    public static RequirementAssignment createAttachmentRequirementAssignment(String node) {
        RequirementAssignment requirement = new RequirementAssignment();
        requirement.setCapability(ToscaCapabilityType.NATIVE_ATTACHMENT);
        requirement.setNode(node);
        requirement.setRelationship(ToscaRelationshipType.ATTACHES_TO);
        return requirement;
    }

    /**
     * Gets node template.
     *
     * @param serviceTemplate the service template
     * @param nodeTemplateId  the node template id
     * @return the node template
     */
    public static NodeTemplate getNodeTemplate(ServiceTemplate serviceTemplate, String nodeTemplateId) {
        if (serviceTemplate == null || serviceTemplate.getTopology_template() == null
            || serviceTemplate.getTopology_template().getNode_templates() == null) {
            return null;
        }
        return serviceTemplate.getTopology_template().getNode_templates().get(nodeTemplateId);
    }

    /**
     * Gets node type.
     *
     * @param serviceTemplate the service template
     * @param nodeTypeId      the node type id
     * @return the node type
     */
    public static NodeType getNodeType(ServiceTemplate serviceTemplate, String nodeTypeId) {
        if (serviceTemplate == null || serviceTemplate.getNode_types() == null) {
            return null;
        }
        return serviceTemplate.getNode_types().get(nodeTypeId);
    }

    /**
     * Gets requirement definition.
     *
     * @param nodeType                the node type
     * @param requirementDefinitionId the requirement definition id
     * @return the requirement definition
     */
    public static Optional<RequirementDefinition> getRequirementDefinition(NodeType nodeType, String requirementDefinitionId) {
        if (nodeType == null || nodeType.getRequirements() == null || requirementDefinitionId == null) {
            return Optional.empty();
        }
        return getRequirementDefinition(nodeType.getRequirements(), requirementDefinitionId);
    }

    /**
     * get requirement definition from requirement definition list by req key.
     *
     * @param requirementsDefinitionList requirement definition list
     * @param requirementKey             requirement key
     */
    public static Optional<RequirementDefinition> getRequirementDefinition(List<Map<String, RequirementDefinition>> requirementsDefinitionList,
                                                                           String requirementKey) {
        if (CollectionUtils.isEmpty(requirementsDefinitionList)) {
            return Optional.empty();
        }
        ToscaExtensionYamlUtil toscaExtensionYamlUtil = new ToscaExtensionYamlUtil();
        for (Map<String, RequirementDefinition> requirementMap : requirementsDefinitionList) {
            if (requirementMap.containsKey(requirementKey)) {
                RequirementDefinition requirementDefinition = toscaExtensionYamlUtil
                    .yamlToObject(toscaExtensionYamlUtil.objectToYaml(requirementMap.get(requirementKey)), RequirementDefinition.class);
                return Optional.of(requirementDefinition);
            }
        }
        return Optional.empty();
    }

    /**
     * Gets capability definition.
     *
     * @param nodeType               the node type
     * @param capabilityDefinitionId the capability definition id
     * @return the capability definition
     */
    public static Optional<CapabilityDefinition> getCapabilityDefinition(NodeType nodeType, String capabilityDefinitionId) {
        if (nodeType == null || nodeType.getCapabilities() == null || capabilityDefinitionId == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(nodeType.getCapabilities().get(capabilityDefinitionId));
    }

    /**
     * Add group definition to topology template.
     *
     * @param serviceTemplate the service template
     * @param groupName       the group name
     * @param group           the group
     */
    public static void addGroupDefinitionToTopologyTemplate(ServiceTemplate serviceTemplate, String groupName, GroupDefinition group) {
        if (serviceTemplate == null) {
            throw new CoreException(new InvalidAddActionNullEntityErrorBuilder("Group Definition", SERVICE_TEMPLATE).build());
        }
        TopologyTemplate topologyTemplate = serviceTemplate.getTopology_template();
        if (Objects.isNull(topologyTemplate)) {
            topologyTemplate = new TopologyTemplate();
            serviceTemplate.setTopology_template(topologyTemplate);
        }
        if (topologyTemplate.getGroups() == null) {
            topologyTemplate.setGroups(new HashMap<>());
        }
        if (serviceTemplate.getTopology_template().getGroups() == null) {
            Map<String, GroupDefinition> groups = new HashMap<>();
            serviceTemplate.getTopology_template().setGroups(groups);
        }
        serviceTemplate.getTopology_template().getGroups().put(groupName, group);
    }

    /**
     * Adds a group member to an existing group in the service template.
     *
     * @param serviceTemplate the service template
     * @param groupName       the group name
     * @param groupMemberId   the group member id
     */
    public static void addGroupMember(ServiceTemplate serviceTemplate, String groupName, String groupMemberId) {
        TopologyTemplate topologyTemplate = serviceTemplate.getTopology_template();
        if (Objects.isNull(topologyTemplate) || topologyTemplate.getGroups() == null || topologyTemplate.getGroups().get(groupName) == null) {
            return;
        }
        GroupDefinition groupDefinition = topologyTemplate.getGroups().get(groupName);
        if (CollectionUtils.isEmpty(groupDefinition.getMembers())) {
            groupDefinition.setMembers(new ArrayList<>());
        }
        if (!groupDefinition.getMembers().contains(groupMemberId)) {
            groupDefinition.getMembers().add(groupMemberId);
        }
    }

    /**
     * Create parameter definition property definition.
     *
     * @param type        the type
     * @param description the description
     * @param required    the required
     * @param constraints the constraints
     * @param entrySchema the entry schema
     * @param defaultVal  the default val
     * @return the property definition
     */
    public static ParameterDefinition createParameterDefinition(String type, String description, Boolean required, List<Constraint> constraints,
                                                                EntrySchema entrySchema, Object defaultVal) {
        ParameterDefinition paramDef = new ParameterDefinition();
        paramDef.setType(type);
        paramDef.setDescription(description);
        paramDef.setRequired(required);
        paramDef.setConstraints(constraints);
        paramDef.setEntry_schema(entrySchema == null ? null : entrySchema.clone());
        paramDef.set_default(defaultVal);
        return paramDef;
    }

    /**
     * Create requirement requirement definition.
     *
     * @param capability   the capability
     * @param node         the node
     * @param relationship the relationship
     * @param occurrences  the occurrences
     * @return the requirement definition
     */
    public static RequirementDefinition createRequirement(String capability, String node, String relationship, Object[] occurrences) {
        RequirementDefinition requirementDefinition = new RequirementDefinition();
        requirementDefinition.setCapability(capability);
        requirementDefinition.setNode(node);
        requirementDefinition.setRelationship(relationship);
        if (occurrences != null) {
            requirementDefinition.setOccurrences(occurrences);
        }
        return requirementDefinition;
    }

    /**
     * Create entry schema entry schema.
     *
     * @param type        the type
     * @param description the description
     * @param constraints the constraints
     * @return the entry schema
     */
    public static EntrySchema createEntrySchema(String type, String description, List<Constraint> constraints) {
        if (Objects.isNull(type) && Objects.isNull(description) && CollectionUtils.isEmpty(constraints)) {
            return null;
        }
        EntrySchema entrySchema = new EntrySchema();
        entrySchema.setType(type);
        entrySchema.setDescription(description);
        entrySchema.setConstraints(constraints);
        return entrySchema;
    }

    /**
     * Create get input property value from list parameter map.
     *
     * @param inputPropertyListName the input property list name
     * @param indexInTheList        the index in the list
     * @param nestedPropertyName    the nested property name
     * @return the map
     */
    public static Map createGetInputPropertyValueFromListParameter(String inputPropertyListName, int indexInTheList, String... nestedPropertyName) {
        List<Object> propertyList = new ArrayList<>();
        propertyList.add(inputPropertyListName);
        propertyList.add(indexInTheList);
        if (nestedPropertyName != null) {
            Collections.addAll(propertyList, nestedPropertyName);
        }
        Map<String, Object> getInputProperty = new HashMap<>();
        getInputProperty.put(ToscaFunctions.GET_INPUT.getFunctionName(), propertyList);
        return getInputProperty;
    }

    /**
     * Convert property def to parameter def parameter definition ext.
     *
     * @param propertyDefinition the property definition
     * @return the parameter definition ext
     */
    public static ParameterDefinitionExt convertPropertyDefToParameterDef(PropertyDefinition propertyDefinition) {
        if (propertyDefinition == null) {
            return null;
        }
        ParameterDefinitionExt parameterDefinition = new ParameterDefinitionExt();
        parameterDefinition.setType(propertyDefinition.getType());
        parameterDefinition.setDescription(propertyDefinition.getDescription());
        parameterDefinition.setRequired(propertyDefinition.getRequired());
        parameterDefinition.set_default(propertyDefinition.get_default());
        parameterDefinition.setStatus(propertyDefinition.getStatus());
        parameterDefinition.setConstraints(propertyDefinition.getConstraints());
        parameterDefinition
            .setEntry_schema(Objects.isNull(propertyDefinition.getEntry_schema()) ? null : propertyDefinition.getEntry_schema().clone());
        parameterDefinition.setHidden(false);
        parameterDefinition.setImmutable(false);
        return parameterDefinition;
    }

    /**
     * Convert attribute def to parameter def parameter definition ext.
     *
     * @param attributeDefinition the attribute definition
     * @param outputValue         the output value
     * @return the parameter definition ext
     */
    public static ParameterDefinitionExt convertAttributeDefToParameterDef(AttributeDefinition attributeDefinition, Map<String, List> outputValue) {
        if (attributeDefinition == null) {
            return null;
        }
        ParameterDefinitionExt parameterDefinition = new ParameterDefinitionExt();
        parameterDefinition.setDescription(attributeDefinition.getDescription());
        parameterDefinition.setValue(outputValue);
        return parameterDefinition;
    }

    public static boolean isNodeTemplate(String entryId, ServiceTemplate serviceTemplate) {
        return serviceTemplate.getTopology_template().getNode_templates() != null
            && serviceTemplate.getTopology_template().getNode_templates().get(entryId) != null;
    }

    /**
     * Add Input parameter.
     *
     * @param serviceTemplate       the service template
     * @param parameterDefinitionId the parameter definition id
     * @param parameterDefinition   the parameter definition
     */
    public static void addInputParameterToTopologyTemplate(ServiceTemplate serviceTemplate, String parameterDefinitionId,
                                                           ParameterDefinition parameterDefinition) {
        if (Objects.isNull(serviceTemplate)) {
            throw new CoreException(new InvalidAddActionNullEntityErrorBuilder("Topology Template Input Parameter", SERVICE_TEMPLATE).build());
        }
        TopologyTemplate topologyTemplate = serviceTemplate.getTopology_template();
        if (Objects.isNull(topologyTemplate)) {
            topologyTemplate = new TopologyTemplate();
            serviceTemplate.setTopology_template(topologyTemplate);
        }
        if (topologyTemplate.getInputs() == null) {
            topologyTemplate.setInputs(new HashMap<>());
        }
        topologyTemplate.getInputs().put(parameterDefinitionId, parameterDefinition);
    }

    /**
     * Add Output parameter.
     *
     * @param serviceTemplate       the service template
     * @param parameterDefinitionId the parameter definition id
     * @param parameterDefinition   the parameter definition
     */
    public static void addOutputParameterToTopologyTemplate(ServiceTemplate serviceTemplate, String parameterDefinitionId,
                                                            ParameterDefinition parameterDefinition) {
        if (Objects.isNull(serviceTemplate)) {
            throw new CoreException(new InvalidAddActionNullEntityErrorBuilder("Topology Template Output Parameter", SERVICE_TEMPLATE).build());
        }
        TopologyTemplate topologyTemplate = serviceTemplate.getTopology_template();
        if (Objects.isNull(topologyTemplate)) {
            topologyTemplate = new TopologyTemplate();
            serviceTemplate.setTopology_template(topologyTemplate);
        }
        if (topologyTemplate.getOutputs() == null) {
            topologyTemplate.setOutputs(new HashMap<>());
        }
        topologyTemplate.getOutputs().put(parameterDefinitionId, parameterDefinition);
    }

    /**
     * Add requirement def to requirement def list.
     *
     * @param requirementList requirement list
     * @param requirementDef  added requirement def
     */
    public static void addRequirementToList(List<Map<String, RequirementDefinition>> requirementList,
                                            Map<String, RequirementDefinition> requirementDef) {
        if (requirementDef == null) {
            return;
        }
        if (requirementList == null) {
            requirementList = new ArrayList<>();
        }
        for (Map.Entry<String, RequirementDefinition> entry : requirementDef.entrySet()) {
            CommonMethods.mergeEntryInList(entry.getKey(), entry.getValue(), requirementList);
        }
    }

    /**
     * get node template requirement.
     *
     * @param nodeTemplate node template
     */
    public static Map<String, RequirementAssignment> getNodeTemplateRequirements(NodeTemplate nodeTemplate) {
        if (Objects.isNull(nodeTemplate)) {
            return null;
        }
        List<Map<String, RequirementAssignment>> templateRequirements = nodeTemplate.getRequirements();
        Map<String, RequirementAssignment> nodeTemplateRequirementsAssignment = new HashMap<>();
        if (CollectionUtils.isEmpty(templateRequirements)) {
            return nodeTemplateRequirementsAssignment;
        }
        ToscaExtensionYamlUtil toscaExtensionYamlUtil = new ToscaExtensionYamlUtil();
        for (Map<String, RequirementAssignment> requirementAssignmentMap : templateRequirements) {
            for (Map.Entry<String, RequirementAssignment> requirementEntry : requirementAssignmentMap.entrySet()) {
                RequirementAssignment requirementAssignment = (toscaExtensionYamlUtil
                    .yamlToObject(toscaExtensionYamlUtil.objectToYaml(requirementEntry.getValue()), RequirementAssignment.class));
                nodeTemplateRequirementsAssignment.put(requirementEntry.getKey(), requirementAssignment);
            }
        }
        return nodeTemplateRequirementsAssignment;
    }

    /**
     * Gets the list of requirements for the node template.
     *
     * @param nodeTemplate the node template
     * @return the node template requirement list and null if the node has no requirements
     */
    public static List<Map<String, RequirementAssignment>> getNodeTemplateRequirementList(NodeTemplate nodeTemplate) {
        ToscaExtensionYamlUtil toscaExtensionYamlUtil = new ToscaExtensionYamlUtil();
        //Creating concrete objects
        List<Map<String, RequirementAssignment>> requirements = nodeTemplate.getRequirements();
        List<Map<String, RequirementAssignment>> concreteRequirementList = null;
        if (requirements != null) {
            concreteRequirementList = new ArrayList<>();
            ListIterator<Map<String, RequirementAssignment>> reqListIterator = requirements.listIterator();
            while (reqListIterator.hasNext()) {
                Map<String, RequirementAssignment> requirement = reqListIterator.next();
                Map<String, RequirementAssignment> concreteRequirement = new HashMap<>();
                for (Map.Entry<String, RequirementAssignment> reqEntry : requirement.entrySet()) {
                    RequirementAssignment requirementAssignment = (toscaExtensionYamlUtil
                        .yamlToObject(toscaExtensionYamlUtil.objectToYaml(reqEntry.getValue()), RequirementAssignment.class));
                    concreteRequirement.put(reqEntry.getKey(), requirementAssignment);
                    concreteRequirementList.add(concreteRequirement);
                    reqListIterator.remove();
                }
            }
            requirements.clear();
            requirements.addAll(concreteRequirementList);
            nodeTemplate.setRequirements(requirements);
        }
        return concreteRequirementList;
    }

    /**
     * get requirement assignment from requirement assignment list by req key.
     *
     * @param requirementsAssignmentList requirement definition list
     * @param requirementKey             requirement key
     */
    public static Optional<List<RequirementAssignment>> getRequirementAssignment(List<Map<String, RequirementAssignment>> requirementsAssignmentList,
                                                                                 String requirementKey) {
        if (CollectionUtils.isEmpty(requirementsAssignmentList)) {
            return Optional.empty();
        }
        List<RequirementAssignment> matchRequirementAssignmentList = new ArrayList<>();
        for (Map<String, RequirementAssignment> requirementMap : requirementsAssignmentList) {
            if (requirementMap.containsKey(requirementKey)) {
                ToscaExtensionYamlUtil toscaExtensionYamlUtil = new ToscaExtensionYamlUtil();
                RequirementAssignment requirementAssignment = (toscaExtensionYamlUtil
                    .yamlToObject(toscaExtensionYamlUtil.objectToYaml(requirementMap.get(requirementKey)), RequirementAssignment.class));
                matchRequirementAssignmentList.add(requirementAssignment);
            }
        }
        if (CollectionUtils.isEmpty(matchRequirementAssignmentList)) {
            return Optional.empty();
        }
        return Optional.of(matchRequirementAssignmentList);
    }

    /**
     * remove requirement definition from requirement definition list by req key.
     *
     * @param requirementsDefinitionList requirement definition list
     * @param requirementKey             requirement key
     */
    public static void removeRequirementsDefinition(List<Map<String, RequirementDefinition>> requirementsDefinitionList, String requirementKey) {
        if (requirementsDefinitionList == null) {
            return;
        }
        List<Map<String, RequirementDefinition>> mapToBeRemoved = new ArrayList<>();
        for (Map<String, RequirementDefinition> reqMap : requirementsDefinitionList) {
            reqMap.remove(requirementKey);
            if (reqMap.isEmpty()) {
                mapToBeRemoved.add(reqMap);
            }
        }
        for (Map<String, RequirementDefinition> removeMap : mapToBeRemoved) {
            requirementsDefinitionList.remove(removeMap);
        }
    }

    /**
     * remove requirement assignment from requirement definition list by req key.
     *
     * @param requirementsAssignmentList requirement Assignment list
     * @param requirementKey             requirement key
     */
    public static void removeRequirementsAssignment(List<Map<String, RequirementAssignment>> requirementsAssignmentList, String requirementKey) {
        if (requirementsAssignmentList == null) {
            return;
        }
        List<Map<String, RequirementAssignment>> mapToBeRemoved = new ArrayList<>();
        for (Map<String, RequirementAssignment> reqMap : requirementsAssignmentList) {
            reqMap.remove(requirementKey);
            if (reqMap.isEmpty()) {
                mapToBeRemoved.add(reqMap);
            }
        }
        for (Map<String, RequirementAssignment> removeMap : mapToBeRemoved) {
            requirementsAssignmentList.remove(removeMap);
        }
    }

    /**
     * Remove requirement assignment.
     *
     * @param nodeTemplate                     the node template
     * @param requirementKey                   the requirement key
     * @param requirementAssignmentToBeDeleted the requirement assignment to be deleted
     */
    public static void removeRequirementAssignment(NodeTemplate nodeTemplate, String requirementKey,
                                                   RequirementAssignment requirementAssignmentToBeDeleted) {
        ToscaAnalyzerService toscaAnalyzerService = new ToscaAnalyzerServiceImpl();
        List<Map<String, RequirementAssignment>> nodeTemplateRequirements = nodeTemplate.getRequirements();
        if (nodeTemplateRequirements == null) {
            return;
        }
        ListIterator<Map<String, RequirementAssignment>> iter = nodeTemplateRequirements.listIterator();
        while (iter.hasNext()) {
            Map<String, RequirementAssignment> reqMap = iter.next();
            RequirementAssignment requirementAssignment = reqMap.get(requirementKey);
            if (requirementAssignment != null) {
                boolean isDesiredRequirementAssignment = toscaAnalyzerService
                    .isDesiredRequirementAssignment(requirementAssignment, requirementAssignmentToBeDeleted.getCapability(),
                        requirementAssignmentToBeDeleted.getNode(), requirementAssignmentToBeDeleted.getRelationship());
                if (isDesiredRequirementAssignment) {
                    iter.remove();
                }
            }
        }
    }

    /**
     * Return the suffix of the input namespace For an exampale - for abc.sdf.vsrx, return vsrx
     *
     * @param namespace namespace
     * @return String namespace suffix
     */
    public static String getNamespaceSuffix(String namespace) {
        if (namespace == null) {
            return null;
        }
        String delimiterChar = ".";
        if (namespace.contains(delimiterChar)) {
            return namespace.substring(namespace.lastIndexOf(delimiterChar) + 1);
        }
        return namespace;
    }

    /**
     * Return true if the input import exist in the input imports list.
     *
     * @param imports  namespace
     * @param importId namespace
     * @return true if exist, false if not exist
     */
    public static boolean isImportAddedToServiceTemplate(List<Map<String, Import>> imports, String importId) {
        for (Map<String, Import> anImport : imports) {
            if (anImport.containsKey(importId)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get output parameter according to the input outputParameterId.
     *
     * @param serviceTemplate   service template
     * @param outputParameterId output parameter id
     * @return ParameterDefinition - output parameter
     */
    public static ParameterDefinition getOuputParameter(ServiceTemplate serviceTemplate, String outputParameterId) {
        if (serviceTemplate == null || serviceTemplate.getTopology_template() == null
            || serviceTemplate.getTopology_template().getOutputs() == null) {
            return null;
        }
        return serviceTemplate.getTopology_template().getOutputs().get(outputParameterId);
    }

    /**
     * Gets input parameters in a service template.
     *
     * @param serviceTemplate the service template
     * @return the input parameters
     */
    public static Map<String, ParameterDefinition> getInputParameters(ServiceTemplate serviceTemplate) {
        if (serviceTemplate == null || serviceTemplate.getTopology_template() == null || serviceTemplate.getTopology_template().getInputs() == null) {
            return null;
        }
        return serviceTemplate.getTopology_template().getInputs();
    }

    /**
     * Gets relationship templates in a service template.
     *
     * @param serviceTemplate the service template
     * @return the relationship template
     */
    public static Map<String, RelationshipTemplate> getRelationshipTemplates(ServiceTemplate serviceTemplate) {
        if (serviceTemplate == null || serviceTemplate.getTopology_template() == null
            || serviceTemplate.getTopology_template().getRelationship_templates() == null) {
            return null;
        }
        return serviceTemplate.getTopology_template().getRelationship_templates();
    }

    /**
     * Get property value according to the input propertyId.
     *
     * @param nodeTemplate node template
     * @param propertyId   property id
     * @return Object        property Value
     */
    public static Object getPropertyValue(NodeTemplate nodeTemplate, String propertyId) {
        if (nodeTemplate == null || nodeTemplate.getProperties() == null) {
            return null;
        }
        return nodeTemplate.getProperties().get(propertyId);
    }

    /**
     * Get node template properties according to the input node template id.
     *
     * @param serviceTemplate service template
     * @param nodeTemplateId  node template id
     * @return node template properties
     */
    public static Map<String, Object> getNodeTemplateProperties(ServiceTemplate serviceTemplate, String nodeTemplateId) {
        if (serviceTemplate == null || serviceTemplate.getTopology_template() == null
            || serviceTemplate.getTopology_template().getNode_templates() == null
            || serviceTemplate.getTopology_template().getNode_templates().get(nodeTemplateId) == null) {
            return null;
        }
        return serviceTemplate.getTopology_template().getNode_templates().get(nodeTemplateId).getProperties();
    }

    /**
     * Adds a property to a node template.
     *
     * @param nodeTemplate  the node template
     * @param propertyKey   the property key
     * @param propertyValue the property value
     */
    public static void addNodeTemplateProperty(NodeTemplate nodeTemplate, String propertyKey, Object propertyValue) {
        if (Objects.isNull(nodeTemplate)) {
            return;
        }
        if (MapUtils.isEmpty(nodeTemplate.getProperties())) {
            nodeTemplate.setProperties(new HashMap<>());
        }
        nodeTemplate.getProperties().put(propertyKey, propertyValue);
    }

    /**
     * Gets substitution mappings in a service template.
     *
     * @param serviceTemplate the service template
     * @return the substitution mappings
     */
    public static SubstitutionMapping getSubstitutionMappings(ServiceTemplate serviceTemplate) {
        if (serviceTemplate == null || serviceTemplate.getTopology_template() == null
            || serviceTemplate.getTopology_template().getSubstitution_mappings() == null) {
            return null;
        }
        return serviceTemplate.getTopology_template().getSubstitution_mappings();
    }

    /**
     * Compare two requirement assignment objects for equality.
     *
     * @param first  the first requirement assignment object
     * @param second the second  requirement assignment object
     * @return true if objects are equal and false otherwise
     */
    public static boolean compareRequirementAssignment(RequirementAssignment first, RequirementAssignment second) {
        return (first.getCapability().equals(second.getCapability()) && first.getNode().equals(second.getNode()) && first.getRelationship()
            .equals(second.getRelationship()));
    }

    /**
     * Gets a deep copy clone of the input object.
     *
     * @param <T>         the type parameter
     * @param objectValue the object value
     * @param clazz       the clazz
     * @return the cloned object
     */
    public static <T> Object getClonedObject(Object objectValue, Class<T> clazz) {
        YamlUtil yamlUtil = new ToscaExtensionYamlUtil();
        Object clonedObjectValue;
        String objectToYaml = yamlUtil.objectToYaml(objectValue);
        clonedObjectValue = yamlUtil.yamlToObject(objectToYaml, clazz);
        return clonedObjectValue;
    }

    /**
     * Gets a deep copy clone of the input object.
     *
     * @param obj the object to be cloned
     * @return the cloned object
     */
    public static Object getClonedObject(Object obj) {
        Object clonedObjectValue;
        try {
            //Serialize object
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(obj);
            //Deserialize object
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
            ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
            clonedObjectValue = objectInputStream.readObject();
        } catch (NotSerializableException ex) {
            LOGGER.debug(ex.getMessage(), ex);
            return getClonedObject(obj, obj.getClass());
        } catch (IOException | ClassNotFoundException ex) {
            LOGGER.debug(ex.getMessage(), ex);
            return null;
        }
        return clonedObjectValue;
    }

    /**
     * Add substitution filtering property.
     *
     * @param templateName the substitution service template name
     * @param nodeTemplate the node template
     * @param count        the count
     */
    public static void addSubstitutionFilteringProperty(String templateName, NodeTemplate nodeTemplate, int count) {
        Map<String, Object> serviceTemplateFilterPropertyValue = new HashMap<>();
        Map<String, Object> properties = nodeTemplate.getProperties();
        serviceTemplateFilterPropertyValue.put(ToscaConstants.SUBSTITUTE_SERVICE_TEMPLATE_PROPERTY_NAME, templateName);
        serviceTemplateFilterPropertyValue.put(ToscaConstants.COUNT_PROPERTY_NAME, count);
        properties.put(ToscaConstants.SERVICE_TEMPLATE_FILTER_PROPERTY_NAME, serviceTemplateFilterPropertyValue);
        nodeTemplate.setProperties(properties);
    }

    /**
     * Adding binding requirement from port node template to compute node template.
     *
     * @param computeNodeTemplateId compute node template id
     * @param portNodeTemplate      port node template
     */
    public static void addBindingReqFromPortToCompute(String computeNodeTemplateId, NodeTemplate portNodeTemplate) {
        RequirementAssignment requirementAssignment = new RequirementAssignment();
        requirementAssignment.setCapability(ToscaCapabilityType.NATIVE_NETWORK_BINDABLE);
        requirementAssignment.setRelationship(ToscaRelationshipType.NATIVE_NETWORK_BINDS_TO);
        requirementAssignment.setNode(computeNodeTemplateId);
        addRequirementAssignment(portNodeTemplate, ToscaConstants.BINDING_REQUIREMENT_ID, requirementAssignment);
    }

    /**
     * Create substitution template substitution mapping.
     *
     * @param nodeTypeKey          the node type key
     * @param substitutionNodeType the substitution node type
     * @param mapping              the mapping
     * @return the substitution mapping
     */
    public static SubstitutionMapping createSubstitutionTemplateSubMapping(String nodeTypeKey, NodeType substitutionNodeType,
                                                                           Map<String, Map<String, List<String>>> mapping) {
        SubstitutionMapping substitutionMapping = new SubstitutionMapping();
        substitutionMapping.setNode_type(nodeTypeKey);
        substitutionMapping.setCapabilities(manageCapabilityMapping(substitutionNodeType.getCapabilities(), mapping.get(ToscaConstants.CAPABILITY)));
        substitutionMapping
            .setRequirements(manageRequirementMapping(substitutionNodeType.getRequirements(), mapping.get(ToscaConstants.REQUIREMENT)));
        return substitutionMapping;
    }

    /**
     * Add node template capability.
     *
     * @param nodeTemplate         the node template
     * @param capabilityId         the capability id
     * @param capabilityProperties the capability properties
     * @param capabilityAttributes the capability attributes
     */
    public static void addNodeTemplateCapability(NodeTemplate nodeTemplate, String capabilityId, Map<String, Object> capabilityProperties,
                                                 Map<String, Object> capabilityAttributes) {
        Map<String, CapabilityAssignment> capabilities = nodeTemplate.getCapabilities();
        if (Objects.isNull(capabilities)) {
            capabilities = new HashMap<>();
        }
        CapabilityAssignment capabilityAssignment = new CapabilityAssignment();
        capabilityAssignment.setProperties(capabilityProperties);
        capabilityAssignment.setAttributes(capabilityAttributes);
        capabilities.put(capabilityId, capabilityAssignment);
        nodeTemplate.setCapabilities(capabilities);
    }

    private static Map<String, List<String>> manageRequirementMapping(List<Map<String, RequirementDefinition>> requirementList,
                                                                      Map<String, List<String>> requirementSubstitutionMapping) {
        if (requirementList == null) {
            return null;
        }
        Map<String, List<String>> requirementMapping = new HashMap<>();
        String requirementKey;
        List<String> requirementMap;
        for (Map<String, RequirementDefinition> requirementDefMap : requirementList) {
            for (Map.Entry<String, RequirementDefinition> entry : requirementDefMap.entrySet()) {
                requirementKey = entry.getKey();
                requirementMap = requirementSubstitutionMapping.get(requirementKey);
                requirementMapping.put(requirementKey, requirementMap);
            }
        }
        return requirementMapping;
    }

    private static Map<String, List<String>> manageCapabilityMapping(Map<String, CapabilityDefinition> capabilities,
                                                                     Map<String, List<String>> capabilitySubstitutionMapping) {
        if (capabilities == null) {
            return null;
        }
        Map<String, List<String>> capabilityMapping = new HashMap<>();
        String capabilityKey;
        List<String> capabilityMap;
        for (Map.Entry<String, CapabilityDefinition> entry : capabilities.entrySet()) {
            capabilityKey = entry.getKey();
            capabilityMap = capabilitySubstitutionMapping.get(capabilityKey);
            capabilityMapping.put(capabilityKey, capabilityMap);
        }
        return capabilityMapping;
    }

    public static void addSubstitutionNodeTypeRequirements(NodeType substitutionNodeType, List<Map<String, RequirementDefinition>> requirementsList,
                                                           String templateName) {
        if (CollectionUtils.isEmpty(requirementsList)) {
            return;
        }
        if (substitutionNodeType.getRequirements() == null) {
            substitutionNodeType.setRequirements(new ArrayList<>());
        }
        for (Map<String, RequirementDefinition> requirementDef : requirementsList) {
            for (Map.Entry<String, RequirementDefinition> entry : requirementDef.entrySet()) {
                Map<String, RequirementDefinition> requirementMap = new HashMap<>();
                requirementMap.put(entry.getKey() + "_" + templateName, entry.getValue().clone());
                substitutionNodeType.getRequirements().add(requirementMap);
            }
        }
    }

    public static boolean isNodeTemplateSectionMissingFromServiceTemplate(ServiceTemplate serviceTemplate) {
        return Objects.isNull(serviceTemplate.getTopology_template()) || MapUtils.isEmpty(serviceTemplate.getTopology_template().getNode_templates());
    }

    /**
     * Gets relationship template in a service template according to the relationship id.
     *
     * @param serviceTemplate the service template
     * @param relationshipId  the relationship id
     * @return the relationship template
     */
    public static Optional<RelationshipTemplate> getRelationshipTemplate(ServiceTemplate serviceTemplate, String relationshipId) {
        if (serviceTemplate == null || serviceTemplate.getTopology_template() == null
            || serviceTemplate.getTopology_template().getRelationship_templates() == null
            || serviceTemplate.getTopology_template().getRelationship_templates().get(relationshipId) == null) {
            return Optional.empty();
        }
        return Optional.of(serviceTemplate.getTopology_template().getRelationship_templates().get(relationshipId));
    }
}
