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
package org.openecomp.sdc.tosca.datatypes.model;

import org.apache.commons.collections4.MapUtils;

import java.util.HashMap;
import java.util.Map;

public class InterfaceDefinition {
  protected Map<String, OperationDefinition> operations;

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

    return getOperations() != null ? getOperations().equals(that.getOperations())
        : that.getOperations() == null;
  }

  @Override
  public int hashCode() {
    return getOperations() != null ? getOperations().hashCode() : 0;
  }
}
