/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2020 Nordix Foundation
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

package org.openecomp.sdc.be.model.converter;

import static org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields.PROPERTY;

import java.util.Map;
import java.util.Optional;
import org.openecomp.sdc.be.datatypes.elements.SchemaDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Converter to create {@link SchemaDefinition} instances from other data sources.
 */
@Component("schemaDefinitionConverter")
public class SchemaDefinitionConverter {

    private PropertyDataConverter propertyDataConverter;

    /**
     * Parses a json object as map to a {@link SchemaDefinition} instance.
     *
     * @param schemaJsonAsMapObj the json map representing the {@link SchemaDefinition}
     * @return an instance of {@link SchemaDefinition}
     */
    public Optional<SchemaDefinition> parseTo(final Object schemaJsonAsMapObj) {
        if (schemaJsonAsMapObj instanceof Map) {
            final Map<String, Object> schemaMap = (Map<String, Object>) schemaJsonAsMapObj;
            final Object schemaPropertyObj = schemaMap.get(PROPERTY.getPresentation());
            if (schemaPropertyObj instanceof Map) {
                final SchemaDefinition schema = new SchemaDefinition();
                schema.setProperty(propertyDataConverter.createPropertyData((Map<String, Object>) schemaPropertyObj));
                return Optional.of(schema);
            }
        }
        return Optional.empty();
    }

    //circular dependency
    @Autowired
    public void setPropertyDataConverter(final PropertyDataConverter propertyDataConverter) {
        this.propertyDataConverter = propertyDataConverter;
    }
}
