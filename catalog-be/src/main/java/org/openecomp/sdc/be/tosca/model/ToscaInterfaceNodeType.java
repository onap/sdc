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

package org.openecomp.sdc.be.tosca.model;

import java.util.Map;
import java.util.Objects;

/**
 * @author KATYR
 * @since March 26, 2018
 */

public class ToscaInterfaceNodeType {

  private String derived_from;
  private String description;
  private Map<String, Object> operations;


  public String getDerived_from() {
    return derived_from;
  }

  public void setDerived_from(String derived_from) {
    this.derived_from = derived_from;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ToscaInterfaceNodeType that = (ToscaInterfaceNodeType) o;
    return Objects.equals(derived_from, that.derived_from) &&
        Objects.equals(description, that.description);
  }

  @Override
  public int hashCode() {

    return Objects.hash(derived_from, description);
  }

  public Map<String, Object> getOperations() {
    return operations;
  }

  public void setOperations(Map<String, Object> operations) {
    this.operations = operations;
  }
}
