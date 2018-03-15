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
import java.util.Objects;

public class InterfaceDefinitionTemplate extends InterfaceDefinition {

  private Map<String, Object> inputs;

  public Map<String, Object> getInputs() {
    return inputs;
  }

  public void setInputs(
      Map<String, Object> inputs) {
    this.inputs = inputs;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof InterfaceDefinitionTemplate)) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    InterfaceDefinitionTemplate that = (InterfaceDefinitionTemplate) o;
    return Objects.equals(inputs, that.inputs);
  }

  @Override
  public int hashCode() {

    return Objects.hash(super.hashCode(), inputs);
  }
}
