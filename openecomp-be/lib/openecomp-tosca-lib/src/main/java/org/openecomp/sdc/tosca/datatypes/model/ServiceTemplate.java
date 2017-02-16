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

import java.util.Map;

public class ServiceTemplate implements Template {

  private String tosca_definitions_version;
  private Metadata metadata;
  private String description;
  private Map<String, Import> imports;
  private Map<String, ArtifactType> artifact_types;
  private Map<String, DataType> data_types;
  private Map<String, CapabilityType> capability_types;
  private Map<String, InterfaceType> interface_types;
  private Map<String, RelationshipType> relationship_types;
  private Map<String, NodeType> node_types;
  private Map<String, GroupType> group_types;
  private Map<String, PolicyType> policy_types;
  private TopologyTemplate topology_template;

  public String getTosca_definitions_version() {
    return tosca_definitions_version;
  }

  public void setTosca_definitions_version(String toscaDefinitionsVersion) {
    this.tosca_definitions_version = toscaDefinitionsVersion;
  }

  public Metadata getMetadata() {
    return metadata;
  }

  public void setMetadata(Metadata metadata) {
    this.metadata = metadata;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Map<String, Import> getImports() {
    return imports;
  }

  public void setImports(Map<String, Import> imports) {
    this.imports = imports;
  }

  public Map<String, ArtifactType> getArtifact_types() {
    return artifact_types;
  }

  public void setArtifact_types(Map<String, ArtifactType> artifactTypes) {
    this.artifact_types = artifactTypes;
  }

  public Map<String, DataType> getData_types() {
    return data_types;
  }

  public void setData_types(Map<String, DataType> dataTypes) {
    this.data_types = dataTypes;
  }

  public Map<String, CapabilityType> getCapability_types() {
    return capability_types;
  }

  public void setCapability_types(Map<String, CapabilityType> capabilityTypes) {
    this.capability_types = capabilityTypes;
  }

  public Map<String, RelationshipType> getRelationship_types() {
    return relationship_types;
  }

  public void setRelationship_types(Map<String, RelationshipType> relationshipTypes) {
    this.relationship_types = relationshipTypes;
  }

  public Map<String, NodeType> getNode_types() {
    return node_types;
  }

  public void setNode_types(Map<String, NodeType> nodeTypes) {
    this.node_types = nodeTypes;
  }

  public Map<String, GroupType> getGroup_types() {
    return group_types;
  }

  public void setGroup_types(Map<String, GroupType> groupTypes) {
    this.group_types = groupTypes;
  }

  public Map<String, InterfaceType> getInterface_types() {
    return interface_types;
  }

  public void setInterface_types(Map<String, InterfaceType> interfaceTypes) {
    this.interface_types = interfaceTypes;
  }

  public Map<String, PolicyType> getPolicy_types() {
    return policy_types;
  }

  public void setPolicy_types(Map<String, PolicyType> policyTypes) {
    this.policy_types = policyTypes;
  }

  public TopologyTemplate getTopology_template() {
    return topology_template;
  }

  public void setTopology_template(TopologyTemplate topologyTemplate) {
    this.topology_template = topologyTemplate;
  }
}
