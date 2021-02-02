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
package org.openecomp.sdc.be.datatypes.elements;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections.CollectionUtils;
import org.onap.sdc.tosca.datatypes.model.EntrySchema;
import org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields;
import org.openecomp.sdc.be.datatypes.tosca.ToscaDataDefinition;

@Getter
@Setter
public class AttributeDataDefinition extends ToscaDataDefinition implements Serializable {

    private transient List<GetOutputValueDataDefinition> getOutputValues;
    private String outputId;
    private String value;

    public AttributeDataDefinition() {
    }

    public AttributeDataDefinition(final AttributeDataDefinition attributeDataDefinition) {
        super();
        this.setUniqueId(attributeDataDefinition.getUniqueId());
        this.setOwnerId(attributeDataDefinition.getOwnerId());
        this.setName(attributeDataDefinition.getName());
        this.setType(attributeDataDefinition.getType());
        this.setDescription(attributeDataDefinition.getDescription());
        this.set_default(attributeDataDefinition.get_default());
        this.setStatus(attributeDataDefinition.getStatus());
        this.setEntry_schema(attributeDataDefinition.getEntry_schema());
        this.outputId = attributeDataDefinition.getOutputId();
        this.value = attributeDataDefinition.getValue();
        if (CollectionUtils.isNotEmpty(attributeDataDefinition.getGetOutputValues())) {
            this.getOutputValues = new ArrayList<>(attributeDataDefinition.getGetOutputValues());
        }
    }

    public String getUniqueId() {
        return (String) getToscaPresentationValue(JsonPresentationFields.UNIQUE_ID);
    }

    public void setUniqueId(String uniqueId) {
        setToscaPresentationValue(JsonPresentationFields.UNIQUE_ID, uniqueId);
    }

    @Override
    public String getOwnerId() {
        return (String) getToscaPresentationValue(JsonPresentationFields.OWNER_ID);
    }

    @Override
    public void setOwnerId(String ownerId) {
        setToscaPresentationValue(JsonPresentationFields.OWNER_ID, ownerId);
    }

    public String getName() {
        return (String) getToscaPresentationValue(JsonPresentationFields.NAME);
    }

    public void setName(String name) {
        setToscaPresentationValue(JsonPresentationFields.NAME, name);
    }

    @Override
    public String getType() {
        return (String) getToscaPresentationValue(JsonPresentationFields.TYPE);
    }

    public String getDescription() {
        return (String) getToscaPresentationValue(JsonPresentationFields.DESCRIPTION);
    }

    public void setDescription(String description) {
        setToscaPresentationValue(JsonPresentationFields.DESCRIPTION, description);
    }

    public Object get_default() {
        return getToscaPresentationValue(JsonPresentationFields.DEFAULT);
    }

    public void set_default(Object _default) {
        setToscaPresentationValue(JsonPresentationFields.DEFAULT, _default);
    }

    public String getStatus() {
        return (String) getToscaPresentationValue(JsonPresentationFields.STATUS);
    }

    public void setStatus(String status) {
        setToscaPresentationValue(JsonPresentationFields.STATUS, status);
    }

    public EntrySchema getEntry_schema() {
        return (EntrySchema) getToscaPresentationValue(JsonPresentationFields.ENTRY_SCHEMA);
    }

    public void setEntry_schema(final EntrySchema entrySchema) {
        setToscaPresentationValue(JsonPresentationFields.ENTRY_SCHEMA, entrySchema);
    }

    public SchemaDefinition getSchema() {
        return null;
    }

}
