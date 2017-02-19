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

public class RequirementDefinition {

  private String capability;
  private String node;
  private String relationship;
  private Object[] occurrences;

  /**
   * Instantiates a new Requirement definition.
   */
  public RequirementDefinition() {
    occurrences = new Object[2];
    occurrences[0] = 1;
    occurrences[1] = 1;
  }

  public String getCapability() {
    return capability;
  }

  public void setCapability(String capability) {
    this.capability = capability;
  }

  public String getNode() {
    return node;
  }

  public void setNode(String node) {
    this.node = node;
  }

  public String getRelationship() {
    return relationship;
  }

  public void setRelationship(String relationship) {
    this.relationship = relationship;
  }

  public Object[] getOccurrences() {
    return occurrences;
  }

  public void setOccurrences(Object[] occurrences) {
    this.occurrences = occurrences;
  }

  @Override
  public RequirementDefinition clone() {
    RequirementDefinition requirementDefinition = new RequirementDefinition();
    requirementDefinition.setNode(this.getNode());
    requirementDefinition.setRelationship(this.getRelationship());
    requirementDefinition.setCapability(this.getCapability());
    requirementDefinition
        .setOccurrences(new Object[]{this.getOccurrences()[0], this.getOccurrences()[1]});
    return requirementDefinition;
  }

}
