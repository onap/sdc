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

public class AttributeDefinition {

  private String type;
  private String description;
  private Object _default;
  private Status status;
  private EntrySchema entry_schema;

  public AttributeDefinition() {
    status = Status.SUPPORTED;
  }

  public EntrySchema getEntry_schema() {
    return entry_schema;
  }

  public void setEntry_schema(EntrySchema entrySchema) {
    this.entry_schema = entrySchema;
  }

  public Status getStatus() {
    return status;
  }

  public void setStatus(Status status) {
    this.status = status;
  }

  public Object get_default() {
    return _default;
  }

  public void set_default(Object defaultValue) {
    this._default = defaultValue;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  @Override
  public AttributeDefinition clone() {
    AttributeDefinition attributeDefinition = new AttributeDefinition();
    attributeDefinition.setType(this.getType());
    attributeDefinition.setDescription(this.getDescription());
    attributeDefinition.set_default(this.get_default());
    attributeDefinition.setStatus(this.getStatus());
    attributeDefinition.setEntry_schema(this.getEntry_schema().clone());
    return attributeDefinition;
  }
}
