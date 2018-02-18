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

import org.apache.commons.collections.MapUtils;

import java.util.HashMap;
import java.util.Map;

public class InterfaceDefinition {

  private String type;
  private Map<String, PropertyDefinition> inputs;
  private Map<String, OperationDefinition> operations;

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public Map<String, PropertyDefinition> getInputs() {
    return inputs;
  }

  public void setInputs(
      Map<String, PropertyDefinition> inputs) {
    this.inputs = inputs;
  }

  public Map<String, OperationDefinition> getOperations() {
    return operations;
  }

  public void setOperations(
      Map<String, OperationDefinition> operations) {
    this.operations = operations;
  }

  public void addOperation(String operationName, OperationDefinition operationDefinition) {
    if (MapUtils.isEmpty(this.operations)) {
      this.operations = new HashMap<>();
    }
    this.operations.put(operationName, operationDefinition);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof InterfaceDefinition)) {
      return false;
    }

    InterfaceDefinition that = (InterfaceDefinition) o;

    if (getType() != null ? !getType().equals(that.getType()) : that.getType() != null) {
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
    int result = getType() != null ? getType().hashCode() : 0;
    result = 31 * result + (getInputs() != null ? getInputs().hashCode() : 0);
    result = 31 * result + (getOperations() != null ? getOperations().hashCode() : 0);
    return result;
  }
}
