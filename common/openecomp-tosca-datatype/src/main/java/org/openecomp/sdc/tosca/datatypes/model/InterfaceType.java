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

import org.apache.commons.collections4.MapUtils;

import java.util.HashMap;
import java.util.Map;

public class InterfaceType {
  private String derived_from;
  private String version;
  private Map<String, String> metadata;
  private String description;
  private Map<String, PropertyDefinition> inputs;
  private Map<String, OperationDefinition> operations;

  public Map<String, PropertyDefinition> getInputs() {
    return inputs;
  }

  public void setInputs(
      Map<String, PropertyDefinition> inputs) {
    this.inputs = inputs;
  }

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

  public Map<String, String> getMetadata() {
    return metadata;
  }

  public void setMetadata(Map<String, String> metadata) {
    this.metadata = metadata;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Map<String, OperationDefinition> getOperations() {
    return operations;
  }

  public void setOperations(
      Map<String, OperationDefinition> operations) {
    this.operations = operations;
  }

  public void addOperation(String operationName, OperationDefinition operationDefinition) {
    if(MapUtils.isEmpty(this.operations)){
      this.operations = new HashMap<>();
    }
    this.operations.put(operationName, operationDefinition);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof InterfaceType)) {
      return false;
    }

    InterfaceType that = (InterfaceType) o;

    if (getDerived_from() != null ? !getDerived_from().equals(that.getDerived_from())
        : that.getDerived_from() != null) {
      return false;
    }
    if (getVersion() != null ? !getVersion().equals(that.getVersion())
        : that.getVersion() != null) {
      return false;
    }
    if (getMetadata() != null ? !getMetadata().equals(that.getMetadata())
        : that.getMetadata() != null) {
      return false;
    }
    if (getDescription() != null ? !getDescription().equals(that.getDescription())
        : that.getDescription() != null) {
      return false;
    }
    if (getInputs() != null ? !getInputs().equals(that.getInputs()) : that.getInputs() != null) {
      return false;
    }
    return getOperations() != null ? getOperations().equals(that.getOperations())
        : that.getOperations() == null;
  }

  @Override
  public int hashCode() {
    int result = getDerived_from() != null ? getDerived_from().hashCode() : 0;
    result = 31 * result + (getVersion() != null ? getVersion().hashCode() : 0);
    result = 31 * result + (getMetadata() != null ? getMetadata().hashCode() : 0);
    result = 31 * result + (getDescription() != null ? getDescription().hashCode() : 0);
    result = 31 * result + (getInputs() != null ? getInputs().hashCode() : 0);
    result = 31 * result + (getOperations() != null ? getOperations().hashCode() : 0);
    return result;
  }
}
