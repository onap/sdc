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

package org.onap.sdc.tosca.datatypes.model;

import java.util.List;
import java.util.Objects;
import org.onap.sdc.tosca.services.DataModelCloneUtil;

public class DefinitionOfDataType implements Cloneable {

  private String type;
  private String description;
  private Object value;
  private Boolean required;
  private Object _default;
  private String status;
  private List<Constraint> constraints;
  private EntrySchema entry_schema;

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

  public Object getValue() {
    return value;
  }

  public void setValue(Object value) {
    this.value = value;
  }

  public Boolean getRequired() {
    return required;
  }

  public void setRequired(Boolean required) {
    this.required = required;
  }

  public Object get_default() {
    return _default;
  }

  public void set_default(Object _default) {
    this._default = _default;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public List<Constraint> getConstraints() {
    return constraints;
  }

  public void setConstraints(List<Constraint> constraints) {
    this.constraints = constraints;
  }

  public EntrySchema getEntry_schema() {
    return entry_schema;
  }

  public void setEntry_schema(EntrySchema entry_schema) {
    this.entry_schema = entry_schema;
  }

  @Override
  public DefinitionOfDataType clone() {
    DefinitionOfDataType definitionOfDataType = new DefinitionOfDataType();
    definitionOfDataType.setType(this.getType());
    definitionOfDataType.setDescription(this.getDescription());
    definitionOfDataType.setRequired(this.getRequired());
    definitionOfDataType.set_default(this.get_default());
    definitionOfDataType.setStatus(this.getStatus());
    definitionOfDataType.setEntry_schema(
            Objects.isNull(this.getEntry_schema()) ? null : this.getEntry_schema().clone());
    definitionOfDataType.setConstraints(DataModelCloneUtil.cloneConstraints(this.getConstraints()));
    return definitionOfDataType;
  }

}
