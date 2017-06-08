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

import java.util.List;
import java.util.Map;

public class NodeType {

  private String derived_from;
  private String version;
  private String description;
  private Map<String, PropertyDefinition> properties;
  private Map<String, AttributeDefinition> attributes;
  private List<Map<String, RequirementDefinition>> requirements;
  private Map<String, CapabilityDefinition> capabilities;
  private Map<String, Object> interfaces;
  private Map<String, ArtifactDefinition> artifacts;


  public String getDerived_from() {
    return derived_from;
  }

  public void setDerived_from(String derived_from) {
    this.derived_from = derived_from;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Map<String, PropertyDefinition> getProperties() {
    return properties;
  }

  public void setProperties(Map<String, PropertyDefinition> properties) {
    this.properties = properties;
  }

  public Map<String, AttributeDefinition> getAttributes() {
    return attributes;
  }

  public void setAttributes(Map<String, AttributeDefinition> attributes) {
    this.attributes = attributes;
  }

  public List<Map<String, RequirementDefinition>> getRequirements() {
    return requirements;
  }

  public void setRequirements(List<Map<String, RequirementDefinition>> requirements) {
    this.requirements = requirements;
  }

  public Map<String, CapabilityDefinition> getCapabilities() {
    return capabilities;
  }

  public void setCapabilities(Map<String, CapabilityDefinition> capabilities) {
    this.capabilities = capabilities;
  }

  public Map<String, Object> getInterfaces() {
    return interfaces;
  }

  public void setInterfaces(Map<String, Object> interfaces) {
    this.interfaces = interfaces;
  }

  public Map<String, ArtifactDefinition> getArtifacts() {
    return artifacts;
  }

  public void setArtifacts(Map<String, ArtifactDefinition> artifacts) {
    this.artifacts = artifacts;
  }

  @Override
  public NodeType clone() {
    NodeType clone = new NodeType();
    clone.setCapabilities(this.getCapabilities());
    clone.setDerived_from(this.getDerived_from());
    clone.setProperties(this.getProperties());
    clone.setRequirements(this.getRequirements());
    clone.setDescription(this.getDescription());
    clone.setAttributes(this.getAttributes());
    clone.setInterfaces(this.getInterfaces());
    clone.setVersion(this.getVersion());
    clone.setArtifacts(this.getArtifacts());
    return clone;
  }
}
