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

import org.openecomp.sdc.tosca.services.DataModelCloneUtil;

import java.util.Objects;

public class PropertyDefinition extends DefinitionOfDataType {

  public PropertyDefinition() {
    this.status = Status.SUPPORTED;
    this.required = true;
  }

  @Override
  public PropertyDefinition clone() {
    PropertyDefinition propertyDefinition = new PropertyDefinition();
    propertyDefinition.setType(this.getType());
    propertyDefinition.setDescription(this.getDescription());
    propertyDefinition.setRequired(this.getRequired());
    propertyDefinition.set_default(this.get_default());
    propertyDefinition.setStatus(this.getStatus());
    propertyDefinition.setEntry_schema(
        Objects.isNull(this.getEntry_schema()) ? null : this.getEntry_schema().clone());
    propertyDefinition.setConstraints(DataModelCloneUtil.cloneConstraints(this.getConstraints()));
    return propertyDefinition;
  }


}
