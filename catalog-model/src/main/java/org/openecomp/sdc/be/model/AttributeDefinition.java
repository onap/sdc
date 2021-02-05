/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2020, Nordix Foundation. All rights reserved.
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
package org.openecomp.sdc.be.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.onap.sdc.tosca.datatypes.model.EntrySchema;
import org.openecomp.sdc.be.datatypes.elements.AttributeDataDefinition;

@Getter
@Setter
@ToString
public class AttributeDefinition extends AttributeDataDefinition implements IOperationParameter, IComplexDefaultValue {

    // All names are according to TOSCA spec from
    // https://docs.oasis-open.org/tosca/TOSCA-Simple-Profile-YAML/v1.3/os/TOSCA-Simple-Profile-YAML-v1.3-os.html#DEFN_ELEMENT_ATTRIBUTE_DEFN
    private String type;
    private String description;
    private Object _default;
    private String status;
    private EntrySchema entry_schema;

    public AttributeDefinition() {
        toscaPresentation = null;
    }

    public AttributeDefinition(final AttributeDataDefinition attributeDataDefinition) {
        super(attributeDataDefinition);
    }

    public AttributeDefinition(final AttributeDefinition attributeDefinition) {
        super(attributeDefinition);
        this.type = attributeDefinition.getType();
        this.description = attributeDefinition.getDescription();
        this._default = attributeDefinition.get_default();
        this.status = attributeDefinition.getStatus();
        this.entry_schema = attributeDefinition.getEntry_schema();
        this.toscaPresentation = attributeDefinition.toscaPresentation;
    }

    @Override
    public String getDefaultValue() {
        return _default == null ? null : String.valueOf(_default);
    }

    @Override
    public void setDefaultValue(final String value) {
        this._default = value;
    }

    @Override
    public boolean isDefinition() {
        return false;
    }
}
