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

import java.util.Map;
import java.util.Objects;

public class InterfaceDefinitionType extends InterfaceDefinition {

  private String type;
  private Map<String, PropertyDefinition> inputs;

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

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof InterfaceDefinitionType)) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    InterfaceDefinitionType that = (InterfaceDefinitionType) o;
    return Objects.equals(type, that.type) &&
        Objects.equals(inputs, that.inputs);
  }

  @Override
  public int hashCode() {

    return Objects.hash(super.hashCode(), type, inputs);
  }
}
