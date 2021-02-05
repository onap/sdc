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

import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.onap.sdc.tosca.datatypes.model.EntrySchema;
import org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields;
import org.openecomp.sdc.be.datatypes.tosca.ToscaDataDefinition;

@Getter
@Setter
@NoArgsConstructor
public class AttributeDataDefinition extends ToscaDataDefinition {

    private List<GetOutputValueDataDefinition> getOutputValues;
    private String outputId;
    private String value;
    private String outputPath;
    private String instanceUniqueId;
    private String attributeId;

    public AttributeDataDefinition(final AttributeDataDefinition attributeDataDefinition) {
        super();
        this.setUniqueId(attributeDataDefinition.getUniqueId());
        this.setOwnerId(attributeDataDefinition.getOwnerId());
        this.setName(attributeDataDefinition.getName());
        this.setType(attributeDataDefinition.getType());
        this.setDescription(attributeDataDefinition.getDescription());
        this.set_default(attributeDataDefinition.get_default());
        this.setValue(attributeDataDefinition.getValue());
        this.setStatus(attributeDataDefinition.getStatus());
        this.setEntry_schema(attributeDataDefinition.getEntry_schema());
        this.setSchema(attributeDataDefinition.getSchema());
        this.setOutputPath(attributeDataDefinition.getOutputPath());
        this.setInstanceUniqueId(attributeDataDefinition.getInstanceUniqueId());
        this.setAttributeId(attributeDataDefinition.getAttributeId());
    }

    public String getUniqueId() {
        return (String) getToscaPresentationValue(JsonPresentationFields.UNIQUE_ID);
    }

    public void setUniqueId(final String uniqueId) {
        setToscaPresentationValue(JsonPresentationFields.UNIQUE_ID, uniqueId);
    }

    @Override
    public String getOwnerId() {
        return (String) getToscaPresentationValue(JsonPresentationFields.OWNER_ID);
    }

    @Override
    public void setOwnerId(final String ownerId) {
        setToscaPresentationValue(JsonPresentationFields.OWNER_ID, ownerId);
    }

    public String getName() {
        return (String) getToscaPresentationValue(JsonPresentationFields.NAME);
    }

    public void setName(final String name) {
        setToscaPresentationValue(JsonPresentationFields.NAME, name);
    }

    @Override
    public String getType() {
        return (String) getToscaPresentationValue(JsonPresentationFields.TYPE);
    }

    @Override
    public void setType(final String type) {
        setToscaPresentationValue(JsonPresentationFields.TYPE, type);
    }

    public String getDescription() {
        return (String) getToscaPresentationValue(JsonPresentationFields.DESCRIPTION);
    }

    public void setDescription(final String description) {
        setToscaPresentationValue(JsonPresentationFields.DESCRIPTION, description);
    }

    public Object get_default() {
        return getToscaPresentationValue(JsonPresentationFields.DEFAULT);
    }

    public void set_default(final Object _default) {
        setToscaPresentationValue(JsonPresentationFields.DEFAULT, _default);
    }

    public String getStatus() {
        return (String) getToscaPresentationValue(JsonPresentationFields.STATUS);
    }

    public void setStatus(final String status) {
        setToscaPresentationValue(JsonPresentationFields.STATUS, status);
    }

    public EntrySchema getEntry_schema() {
        return (EntrySchema) getToscaPresentationValue(JsonPresentationFields.SCHEMA);
    }

    public void setEntry_schema(final EntrySchema entrySchema) {
        setToscaPresentationValue(JsonPresentationFields.SCHEMA, entrySchema);
    }

    public SchemaDefinition getSchema() {
        return (SchemaDefinition) getToscaPresentationValue(JsonPresentationFields.SCHEMA);
    }

    public String getParentUniqueId() {
        return getOwnerId();
    }

}
