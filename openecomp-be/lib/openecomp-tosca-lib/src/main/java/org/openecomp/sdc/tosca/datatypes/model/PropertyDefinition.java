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

import org.openecomp.sdc.tosca.services.DataModelUtil;

import java.util.List;

public class PropertyDefinition {

  private String type;
  private String description;
  private Boolean required;
  private Object _default;
  private Status status;
  private List<Constraint> constraints;
  private EntrySchema entry_schema;

  public PropertyDefinition() {
    status = Status.SUPPORTED;
    required = true;
  }

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

  public Boolean getRequired() {
    return required;
  }

  public void setRequired(Boolean required) {
    this.required = required;
  }

  public Object get_default() {
    return _default;
  }

  public void set_default(Object defaultValue) {
    this._default = defaultValue;
  }

  public Status getStatus() {
    return status;
  }

  public void setStatus(Status status) {
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

  public void setEntry_schema(EntrySchema entrySchema) {
    this.entry_schema = entrySchema;
  }

  @Override
  public PropertyDefinition clone() {
    PropertyDefinition propertyDefinition = new PropertyDefinition();
    propertyDefinition.setType(this.getType());
    propertyDefinition.setDescription(this.getDescription());
    propertyDefinition.setRequired(this.getRequired());
    propertyDefinition.set_default(this.get_default());
    propertyDefinition.setStatus(this.getStatus());
    propertyDefinition.setEntry_schema(this.getEntry_schema().clone());
    propertyDefinition.setConstraints(DataModelUtil.cloneConstraints(this.getConstraints()));
    return propertyDefinition;
  }


}
