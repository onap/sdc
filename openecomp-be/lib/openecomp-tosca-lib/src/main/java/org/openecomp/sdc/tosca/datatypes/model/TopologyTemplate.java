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

package org.openecomp.sdc.tosca.datatypes.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class TopologyTemplate {

  private String description;
  private Map<String, ParameterDefinition> inputs;
  private Map<String, NodeTemplate> node_templates;
  private Map<String, RelationshipTemplate> relationship_templates;
  private Map<String, GroupDefinition> groups;
  private Map<String, ParameterDefinition> outputs;
  private SubstitutionMapping substitution_mappings;
  private Map<String, PolicyDefinition> policies;

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Map<String, ParameterDefinition> getInputs() {
    return inputs;
  }

  public void setInputs(Map<String, ParameterDefinition> inputs) {
    this.inputs = inputs;
  }

  public Map<String, NodeTemplate> getNode_templates() {
    return node_templates;
  }

  public void setNode_templates(Map<String, NodeTemplate> nodeTemplates) {
    this.node_templates = nodeTemplates;
  }

  public Map<String, RelationshipTemplate> getRelationship_templates() {
    return relationship_templates;
  }

  public void setRelationship_templates(Map<String, RelationshipTemplate> relationshipTemplates) {
    this.relationship_templates = relationshipTemplates;
  }

  public Map<String, GroupDefinition> getGroups() {
    return groups;
  }

  public void setGroups(Map<String, GroupDefinition> groups) {
    this.groups = groups;
  }

  /**
   * Add group.
   *
   * @param groupKey        the group key
   * @param groupDefinition the group definition
   */
  public void addGroup(String groupKey, GroupDefinition groupDefinition) {
    if (Objects.isNull(this.groups)) {
      this.groups = new HashMap<>();
    }
    this.groups.put(groupKey, groupDefinition);
  }

  public Map<String, ParameterDefinition> getOutputs() {
    return outputs;
  }

  public void setOutputs(Map<String, ParameterDefinition> outputs) {
    this.outputs = outputs;
  }

  public SubstitutionMapping getSubstitution_mappings() {
    return substitution_mappings;
  }

  public void setSubstitution_mappings(SubstitutionMapping substitutionMapping) {
    this.substitution_mappings = substitutionMapping;
  }

  public Map<String, PolicyDefinition> getPolicies() {
    return policies;
  }

  public void setPolicies(Map<String, PolicyDefinition> policies) {
    this.policies = policies;
  }
}
