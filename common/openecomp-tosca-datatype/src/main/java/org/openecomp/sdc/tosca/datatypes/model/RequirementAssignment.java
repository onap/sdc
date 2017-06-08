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


import java.util.Arrays;

public class RequirementAssignment {

  private String capability;
  private String node;
  private String relationship;
  private NodeFilter node_filter;
  private Object[] occurrences;

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

  public NodeFilter getNode_filter() {
    return node_filter;
  }

  public void setNode_filter(NodeFilter node_filter) {
    this.node_filter = node_filter;
  }

  public Object[] getOccurrences() {
    return occurrences;
  }

  public void setOccurrences(Object[] occurrences) {
    this.occurrences = occurrences;
  }
}
