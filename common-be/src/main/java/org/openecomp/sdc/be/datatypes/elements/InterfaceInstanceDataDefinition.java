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

package org.openecomp.sdc.be.datatypes.elements;

import org.apache.commons.collections.MapUtils;
import org.openecomp.sdc.be.datatypes.tosca.ToscaDataDefinition;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class InterfaceInstanceDataDefinition extends ToscaDataDefinition implements Serializable {
  protected Map<String, Object> inputs;
  protected Map<String, OperationInstance> operations;

  public InterfaceInstanceDataDefinition(
      InterfaceInstanceDataDefinition inter) {
    this.toscaPresentation = null;
    this.inputs = inter.inputs == null? new HashMap():new HashMap<>(inter.inputs);
    this.operations = new HashMap<>(inter.operations);
  }

  public InterfaceInstanceDataDefinition(){
    this.toscaPresentation = null;
  }

  public Map<String, Object> getInputs() {
    return this.inputs;
  }

  public void setInputs(
      Map<String, Object> inputs) {
    this.inputs = inputs;
  }

  public Map<String, OperationInstance> getOperations() {
    return operations;
  }

  public void addInstanceOperation(String operationName, OperationInstance operation) {
    if(MapUtils.isEmpty(this.operations)) {
      this.operations = new HashMap<>();
    }

    this.operations.put(operationName, operation);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof InterfaceInstanceDataDefinition)) {
      return false;
    }
    InterfaceInstanceDataDefinition that = (InterfaceInstanceDataDefinition) o;
    return Objects.equals(inputs, that.inputs);
  }

  @Override
  public int hashCode() {

    return Objects.hash(inputs);
  }

}
