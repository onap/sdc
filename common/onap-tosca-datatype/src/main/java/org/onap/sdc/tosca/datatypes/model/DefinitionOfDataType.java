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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.onap.sdc.tosca.services.DataModelCloneUtil;

@Getter
@Setter
@NoArgsConstructor
public class DefinitionOfDataType implements Cloneable {

    private String type;
    private String description;
    private String tenant;

    private Object value;
    private Boolean required;
    private Object _default;
    private String status;
    private List<Constraint> constraints;
    private EntrySchema entry_schema;

    @Override
    public DefinitionOfDataType clone() {
        DefinitionOfDataType definitionOfDataType = new DefinitionOfDataType();
        definitionOfDataType.setType(this.getType());
        definitionOfDataType.setDescription(this.getDescription());
        definitionOfDataType.setRequired(this.getRequired());
        definitionOfDataType.set_default(this.get_default());
        definitionOfDataType.setStatus(this.getStatus());
        definitionOfDataType.setEntry_schema(Objects.isNull(this.getEntry_schema()) ? null : this.getEntry_schema().clone());
        definitionOfDataType.setConstraints(DataModelCloneUtil.cloneConstraints(this.getConstraints()));
        return definitionOfDataType;
    }
}
