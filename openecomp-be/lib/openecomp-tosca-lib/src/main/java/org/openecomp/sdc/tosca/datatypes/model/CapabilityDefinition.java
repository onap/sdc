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

import org.openecomp.sdc.tosca.services.DataModelUtil;
import org.openecomp.sdc.tosca.services.ToscaConstants;

import java.util.List;
import java.util.Map;

public class CapabilityDefinition {

  private String type;
  private String description;
  private Map<String, PropertyDefinition> properties;
  private Map<String, AttributeDefinition> attributes;
  private List<String> valid_source_types;
  private Object[] occurrences;


  /**
   * Instantiates a new Capability definition.
   */
  public CapabilityDefinition() {
    occurrences = new Object[2];
    occurrences[0] = 1;
    occurrences[1] = ToscaConstants.UNBOUNDED;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
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

  public List<String> getValid_source_types() {
    return valid_source_types;
  }

  public void setValid_source_types(List<String> validSourceTypes) {
    this.valid_source_types = validSourceTypes;
  }

  public Object[] getOccurrences() {
    return occurrences;
  }

  public void setOccurrences(Object[] occurrences) {
    this.occurrences = occurrences;
  }

  @Override
  public CapabilityDefinition clone() {
    CapabilityDefinition capabilityDefinition = new CapabilityDefinition();
    capabilityDefinition
        .setOccurrences(new Object[]{this.getOccurrences()[0], this.getOccurrences()[1]});
    capabilityDefinition
        .setProperties(DataModelUtil.clonePropertyDefinitions(this.getProperties()));
    capabilityDefinition.setType(this.getType());
    capabilityDefinition
        .setAttributes(DataModelUtil.cloneAttributeDefinitions(this.getAttributes()));
    capabilityDefinition.setDescription(this.getDescription());
    capabilityDefinition
        .setValid_source_types(DataModelUtil.cloneValidSourceTypes(this.getValid_source_types()));
    return capabilityDefinition;

  }

}
