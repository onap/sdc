/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2021 Nordix Foundation
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================
 */
package org.openecomp.sdc.be.tosca.model;

import java.util.LinkedHashMap;
import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.openecomp.sdc.be.utils.TypeUtils.ToscaTagNamesEnum;

/**
 * Represents a TOSCA Attribute Definition (see TOSCA 1.3, Section 3.6.12 Attribute definition)
 */
@NoArgsConstructor
public class ToscaAttribute {

    //should be default, but it is a reserved word in Java
    private Object defaultValue;
    @Getter
    @Setter
    private Object value;
    @Getter
    @Setter
    private String type;
    @Getter
    @Setter
    private String description;
    @Getter
    @Setter
    private ToscaSchemaDefinition keySchema;
    @Getter
    @Setter
    private ToscaSchemaDefinition entrySchema;
    @Getter
    @Setter
    private String status;

    public Object getDefault() {
        return defaultValue;
    }

    public void setDefault(final Object defaultValue) {
        this.defaultValue = defaultValue;
    }

    /**
     * Converts this object to Map, ignoring null fields. The entries of the map are based on the TOSCA Attribute Definition.
     *
     * @return a Map representing the TOSCA Attribute Definition.
     */
    public Map<String, Object> asToscaMap() {
        final Map<String, Object> toscaAttributeAsMap = new LinkedHashMap<>();
        if (getType() != null) {
            toscaAttributeAsMap.put(ToscaTagNamesEnum.TYPE.getElementName(), getType());
        }
        if (getDescription() != null) {
            toscaAttributeAsMap.put(ToscaTagNamesEnum.DESCRIPTION.getElementName(), getDescription());
        }
        if (getKeySchema() != null) {
            toscaAttributeAsMap.put(ToscaTagNamesEnum.KEY_SCHEMA.getElementName(), getKeySchema());
        }
        if (getEntrySchema() != null) {
            toscaAttributeAsMap.put(ToscaTagNamesEnum.ENTRY_SCHEMA.getElementName(), getEntrySchema());
        }
        if (getDefault() != null) {
            toscaAttributeAsMap.put(ToscaTagNamesEnum.DEFAULT.getElementName(), getDefault());
        }
        if (getStatus() != null) {
            toscaAttributeAsMap.put(ToscaTagNamesEnum.STATUS.getElementName(), getStatus());
        }
        return toscaAttributeAsMap;
    }
}
