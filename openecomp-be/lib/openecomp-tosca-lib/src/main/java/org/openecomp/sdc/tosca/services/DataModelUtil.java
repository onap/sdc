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

import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.tosca.datatypes.ToscaFunctions;
import org.openecomp.sdc.tosca.datatypes.model.AttributeDefinition;
import org.openecomp.sdc.tosca.datatypes.model.Constraint;
import org.openecomp.sdc.tosca.datatypes.model.EntrySchema;
import org.openecomp.sdc.tosca.datatypes.model.GroupDefinition;
import org.openecomp.sdc.tosca.datatypes.model.Metadata;
import org.openecomp.sdc.tosca.datatypes.model.NodeTemplate;
import org.openecomp.sdc.tosca.datatypes.model.NodeType;
import org.openecomp.sdc.tosca.datatypes.model.PolicyDefinition;
import org.openecomp.sdc.tosca.datatypes.model.PropertyDefinition;
import org.openecomp.sdc.tosca.datatypes.model.RelationshipTemplate;
import org.openecomp.sdc.tosca.datatypes.model.RequirementAssignment;
import org.openecomp.sdc.tosca.datatypes.model.RequirementDefinition;
import org.openecomp.sdc.tosca.datatypes.model.ServiceTemplate;
import org.openecomp.sdc.tosca.datatypes.model.Status;
import org.openecomp.sdc.tosca.datatypes.model.SubstitutionMapping;
import org.openecomp.sdc.tosca.datatypes.model.TopologyTemplate;
import org.openecomp.sdc.tosca.datatypes.model.heatextend.ParameterDefinitionExt;
import org.openecomp.sdc.tosca.errors.InvalidAddActionNullEntityErrorBuilder;
import org.openecomp.sdc.tosca.errors.InvalidRequirementAssignmentErrorBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * The type Data model util.
 */
public class DataModelUtil {

  /**
   * Add substitution mapping.
   *
   * @param serviceTemplate     the service template
   * @param substitutionMapping the substitution mapping
   */
  public static void addSubstitutionMapping(ServiceTemplate serviceTemplate,
                                            SubstitutionMapping substitutionMapping) {
    if (serviceTemplate == null) {
      throw new CoreException(
          new InvalidAddActionNullEntityErrorBuilder("Substitution Mapping", "Service Template")
              .build());
    }

    if (serviceTemplate.getTopology_template() == null) {
      serviceTemplate.setTopology_template(new TopologyTemplate());
    }
    serviceTemplate.getTopology_template().setSubstitution_mappings(substitutionMapping);
  }

  /**
   * Add substitution mapping req.
   *
   * @param serviceTemplate                    the service template
   * @param substitutionMappingRequirementId   the substitution mapping requirement id
   * @param substitutionMappingRequirementList the substitution mapping requirement list
   */
  public static void addSubstitutionMappingReq(ServiceTemplate serviceTemplate,
                                               String substitutionMappingRequirementId,
                                               List<String> substitutionMappingRequirementList) {
    if (serviceTemplate == null) {
      throw new CoreException(
          new InvalidAddActionNullEntityErrorBuilder("Substitution Mapping Requirements",
              "Service Template").build());
    }

    if (serviceTemplate.getTopology_template() == null) {
      serviceTemplate.setTopology_template(new TopologyTemplate());
    }
    if (serviceTemplate.getTopology_template().getSubstitution_mappings() == null) {
      serviceTemplate.getTopology_template().setSubstitution_mappings(new SubstitutionMapping());
    }
    if (serviceTemplate.getTopology_template().getSubstitution_mappings().getRequirements()
        == null) {
      serviceTemplate.getTopology_template().getSubstitution_mappings()
          .setRequirements(new HashMap<>());
    }

    serviceTemplate.getTopology_template().getSubstitution_mappings().getRequirements()
        .put(substitutionMappingRequirementId, substitutionMappingRequirementList);
  }

  /**
   * Add node template.
   *
   * @param serviceTemplate the service template
   * @param nodeTemplateId  the node template id
   * @param nodeTemplate    the node template
   */
  public static void addNodeTemplate(ServiceTemplate serviceTemplate, String nodeTemplateId,
                                     NodeTemplate nodeTemplate) {
    if (serviceTemplate == null) {
      throw new CoreException(
          new InvalidAddActionNullEntityErrorBuilder("Node Template", "Service Template").build());
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
   * Add policy definition.
   *
   * @param serviceTemplate  the service template
   * @param policyId         the policy id
   * @param policyDefinition the policy definition
   */
  public static void addPolicyDefinition(ServiceTemplate serviceTemplate, String policyId,
                                         PolicyDefinition policyDefinition) {
    if (serviceTemplate == null) {
      throw new CoreException(
          new InvalidAddActionNullEntityErrorBuilder("Policy Definition", "Service Template")
              .build());
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
  public static void addNodeType(ServiceTemplate serviceTemplate, String nodeTypeId,
                                 NodeType nodeType) {
    if (serviceTemplate == null) {
      throw new CoreException(
          new InvalidAddActionNullEntityErrorBuilder("Node Type", "Service Template").build());
    }
    if (serviceTemplate.getNode_types() == null) {
      serviceTemplate.setNode_types(new HashMap<>());
    }
    serviceTemplate.getNode_types().put(nodeTypeId, nodeType);
  }

  /**
   * Add relationship template.
   *
   * @param serviceTemplate        the service template
   * @param relationshipTemplateId the relationship template id
   * @param relationshipTemplate   the relationship template
   */
  public static void addRelationshipTemplate(ServiceTemplate serviceTemplate,
                                             String relationshipTemplateId,
                                             RelationshipTemplate relationshipTemplate) {
    if (serviceTemplate == null) {
      throw new CoreException(
          new InvalidAddActionNullEntityErrorBuilder("Relationship Template", "Service Template")
              .build());
    }
    if (serviceTemplate.getTopology_template() == null) {
      serviceTemplate.setTopology_template(new TopologyTemplate());
    }
    if (serviceTemplate.getTopology_template().getRelationship_templates() == null) {
      serviceTemplate.getTopology_template().setRelationship_templates(new HashMap<>());
    }
    serviceTemplate.getTopology_template().getRelationship_templates()
        .put(relationshipTemplateId, relationshipTemplate);
  }

  /**
   * Add requirement assignment.
   *
   * @param nodeTemplate          the node template
   * @param requirementId         the requirement id
   * @param requirementAssignment the requirement assignment
   */
  public static void addRequirementAssignment(NodeTemplate nodeTemplate, String requirementId,
                                              RequirementAssignment requirementAssignment) {
    if (nodeTemplate == null) {
      throw new CoreException(
          new InvalidAddActionNullEntityErrorBuilder("Requirement Assignment", "Node Template")
              .build());
    }
    if (requirementAssignment.getNode() == null) {
      throw new CoreException(new InvalidRequirementAssignmentErrorBuilder(requirementId).build());
    }

    if (nodeTemplate.getRequirements() == null) {
      nodeTemplate.setRequirements(new ArrayList<>());
    }
    Map<String, RequirementAssignment> requirement = new HashMap<>();
    requirement.put(requirementId, requirementAssignment);
    nodeTemplate.getRequirements().add(requirement);
  }

  /**
   * Gets node template.
   *
   * @param serviceTemplate the service template
   * @param nodeTemplateId  the node template id
   * @return the node template
   */
  public static NodeTemplate getNodeTemplate(ServiceTemplate serviceTemplate,
                                             String nodeTemplateId) {
    if (serviceTemplate == null
        || serviceTemplate.getTopology_template() == null
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
   * Add group definition to topology template.
   *
   * @param serviceTemplate the service template
   * @param groupName       the group name
   * @param group           the group
   */
  public static void addGroupDefinitionToTopologyTemplate(ServiceTemplate serviceTemplate,
                                                          String groupName, GroupDefinition group) {
    if (serviceTemplate == null) {
      throw new CoreException(
          new InvalidAddActionNullEntityErrorBuilder("Group Definition", "Service Template")
              .build());
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
   * Create property definition property definition.
   *
   * @param type        the type
   * @param description the description
   * @param required    the required
   * @param constraints the constraints
   * @param status      the status
   * @param entrySchema the entry schema
   * @param defaultVal  the default val
   * @return the property definition
   */
  public static PropertyDefinition createPropertyDefinition(String type, String description,
                                                            boolean required,
                                                            List<Constraint> constraints,
                                                            Status status,
                                                            EntrySchema entrySchema,
                                                            Object defaultVal) {
    PropertyDefinition propDef = new PropertyDefinition();
    propDef.setType(type);
    propDef.setDescription(description);
    propDef.setRequired(required);
    propDef.setConstraints(constraints);
    if (status != null) {
      propDef.setStatus(status);
    }
    propDef.setEntry_schema(entrySchema);
    propDef.set_default(defaultVal);

    return propDef;
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
  public static RequirementDefinition createRequirement(String capability, String node,
                                                        String relationship, Object[] occurrences) {
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
   * Create attribute definition attribute definition.
   *
   * @param type        the type
   * @param description the description
   * @param status      the status
   * @param entrySchema the entry schema
   * @param defaultVal  the default val
   * @return the attribute definition
   */
  public static AttributeDefinition createAttributeDefinition(String type, String description,
                                                              Status status,
                                                              EntrySchema entrySchema,
                                                              Object defaultVal) {
    AttributeDefinition attributeDef = new AttributeDefinition();
    attributeDef.setType(type);

    if (description != null) {
      attributeDef.setDescription(description);
    }
    if (status != null) {
      attributeDef.setStatus(status);
    }
    attributeDef.setEntry_schema(entrySchema);
    attributeDef.set_default(defaultVal);

    return attributeDef;
  }

  /**
   * Create valid values constraint constraint.
   *
   * @param values the values
   * @return the constraint
   */
  public static Constraint createValidValuesConstraint(Object... values) {
    Constraint validValues = new Constraint();
    for (Object value : values) {
      validValues.addValidValue(value);
    }
    return validValues;
  }

  /**
   * Create metadata metadata.
   *
   * @param templateName    the template name
   * @param templateVersion the template version
   * @param templateAuthor  the template author
   * @return the metadata
   */
  public static Metadata createMetadata(String templateName, String templateVersion,
                                        String templateAuthor) {
    Metadata metadata = new Metadata();
    metadata.setTemplate_name(templateName);
    metadata.setTemplate_version(templateVersion);
    metadata.setTemplate_author(templateAuthor);

    return metadata;
  }

  /**
   * Create entry schema entry schema.
   *
   * @param type        the type
   * @param description the description
   * @param constraints the constraints
   * @return the entry schema
   */
  public static EntrySchema createEntrySchema(String type, String description,
                                              List<Constraint> constraints) {
    EntrySchema entrySchema = new EntrySchema();
    entrySchema.setType(type);
    entrySchema.setDescription(description);
    entrySchema.setConstraints(constraints);
    return entrySchema;
  }

  /**
   * Create valid values constraints list list.
   *
   * @param values the values
   * @return the list
   */
  public static List<Constraint> createValidValuesConstraintsList(String... values) {
    List<Constraint> constraints;
    Constraint validValues;
    constraints = new ArrayList<>();
    validValues = DataModelUtil.createValidValuesConstraint(values);
    constraints.add(validValues);
    return constraints;
  }

  /**
   * Create greater or equal constrain constraint.
   *
   * @param value the value
   * @return the constraint
   */
  public static Constraint createGreaterOrEqualConstrain(Object value) {

    Constraint constraint = new Constraint();
    constraint.setGreater_or_equal(value);
    return constraint;
  }

  /**
   * Gets constrain list.
   *
   * @param constrains the constrains
   * @return the constrain list
   */
  public static List<Constraint> getConstrainList(Constraint... constrains) {
    return Arrays.asList(constrains);

  }

  /**
   * Create get input property value from list parameter map.
   *
   * @param inputPropertyListName the input property list name
   * @param indexInTheList        the index in the list
   * @param nestedPropertyName    the nested property name
   * @return the map
   */
  public static Map createGetInputPropertyValueFromListParameter(String inputPropertyListName,
                                                                 int indexInTheList,
                                                                 String... nestedPropertyName) {
    List propertyList = new ArrayList<>();
    propertyList.add(inputPropertyListName);
    propertyList.add(indexInTheList);
    if (nestedPropertyName != null) {
      Collections.addAll(propertyList, nestedPropertyName);
    }
    Map getInputProperty = new HashMap<>();
    getInputProperty.put(ToscaFunctions.GET_INPUT.getDisplayName(), propertyList);
    return getInputProperty;
  }

  /**
   * Convert property def to parameter def parameter definition ext.
   *
   * @param propertyDefinition the property definition
   * @return the parameter definition ext
   */
  public static ParameterDefinitionExt convertPropertyDefToParameterDef(
      PropertyDefinition propertyDefinition) {
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
    parameterDefinition.setEntry_schema(propertyDefinition.getEntry_schema());
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
  public static ParameterDefinitionExt convertAttributeDefToParameterDef(
      AttributeDefinition attributeDefinition, Map<String, List> outputValue) {
    if (attributeDefinition == null) {
      return null;
    }
    ParameterDefinitionExt parameterDefinition = new ParameterDefinitionExt();
    parameterDefinition.setDescription(attributeDefinition.getDescription());
    parameterDefinition.setValue(outputValue);
    return parameterDefinition;
  }

  /**
   * Clone constraints list.
   *
   * @param constraints the constraints
   * @return the list
   */
  public static List<Constraint> cloneConstraints(List<Constraint> constraints) {
    if (constraints == null) {
      return null;
    }
    return constraints.stream().map(Constraint::clone).collect(Collectors.toList());
  }

  /**
   * Clone valid source types list.
   *
   * @param validSourceTypes the valid source types
   * @return the list
   */
  public static List<String> cloneValidSourceTypes(List<String> validSourceTypes) {
    if (validSourceTypes == null) {
      return null;
    }
    return validSourceTypes.stream().collect(Collectors.toList());
  }

  /**
   * Clone property definitions map.
   *
   * @param propertyDefinitions the property definitions
   * @return the map
   */
  public static Map<String, PropertyDefinition> clonePropertyDefinitions(
      Map<String, PropertyDefinition> propertyDefinitions) {
    if (propertyDefinitions == null) {
      return null;
    }
    Map<String, PropertyDefinition> clonedProperties = new HashMap<>();
    for (String propertyKey : propertyDefinitions.keySet()) {
      clonedProperties.put(propertyKey, propertyDefinitions.get(propertyKey).clone());
    }
    return clonedProperties;
  }

  /**
   * Clone attribute definitions map.
   *
   * @param attributeDefinitions the attribute definitions
   * @return the map
   */
  public static Map<String, AttributeDefinition> cloneAttributeDefinitions(
      Map<String, AttributeDefinition> attributeDefinitions) {
    if (attributeDefinitions == null) {
      return null;
    }
    Map<String, AttributeDefinition> clonedAttributeDefinitions = new HashMap<>();
    for (String attributeKey : attributeDefinitions.keySet()) {
      clonedAttributeDefinitions.put(attributeKey, attributeDefinitions.get(attributeKey).clone());
    }
    return clonedAttributeDefinitions;
  }
}
