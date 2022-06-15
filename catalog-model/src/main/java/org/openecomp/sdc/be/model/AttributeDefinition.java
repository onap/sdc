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

import lombok.NoArgsConstructor;
import org.openecomp.sdc.be.datatypes.elements.AttributeDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.SchemaDefinition;

@NoArgsConstructor
public class AttributeDefinition extends AttributeDataDefinition implements IOperationParameter, IComplexDefaultValue, ToscaPropertyData {

    public AttributeDefinition(final AttributeDataDefinition attributeDataDefinition) {
        super(attributeDataDefinition);
    }

    @Override
    public String getDefaultValue() {
        return get_default() == null ? null : String.valueOf(get_default());
    }

    @Override
    public void setDefaultValue(final String value) {
        set_default(value);
    }

    @Override
    public boolean isDefinition() {
        return false;
    }

    @Override
    public String toString() {
        return "AttributeDefinition{" + "name=" + getName() + "uniqueId=" + getUniqueId() + "ownerId=" + getOwnerId() + "type=" + getType()
            + "description=" + getDescription() + "default=" + getDefaultValue() + '}';
    }

    @Override
    public String getSchemaType() {
        final SchemaDefinition schema = getSchema();
        if (schema == null || schema.getProperty() == null) {
            return null;
        }
        return schema.getProperty().getType();
    }

}
