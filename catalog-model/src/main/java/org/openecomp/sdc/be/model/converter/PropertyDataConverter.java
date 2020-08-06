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

import static org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields.DEFAULT_VALUE;
import static org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields.DEFINITION;
import static org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields.DESCRIPTION;
import static org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields.HIDDEN;
import static org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields.IMMUTABLE;
import static org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields.INPUT_ID;
import static org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields.INPUT_PATH;
import static org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields.INSTANCE_UNIQUE_ID;
import static org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields.LABEL;
import static org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields.NAME;
import static org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields.PARENT_PROPERTY_TYPE;
import static org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields.PASSWORD;
import static org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields.PROPERTY_CONSTRAINTS;
import static org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields.PROPERTY_ID;
import static org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields.REQUIRED;
import static org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields.SCHEMA;
import static org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields.STATUS;
import static org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields.SUB_PROPERTY_INPUT_PATH;
import static org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields.TYPE;
import static org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields.UNIQUE_ID;
import static org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields.VALUE;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Converter to create {@link PropertyDataDefinition} instances from other data sources.
 */
@Component("propertyDataConverter")
public class PropertyDataConverter {

    private SchemaDefinitionConverter schemaDefinitionConverter;

    /**
     * Parses all property data JSON entry map the given list to {@link PropertyDataDefinition}.
     *
     * @param propertiesDataList the list of property data JSON entry map
     * @return a list with all property data JSON entry map parsed as a PropertyDataDefinition
     */
    public List<PropertyDataDefinition> parseProperties(final List<Map<String, Object>> propertiesDataList) {
        if (CollectionUtils.isEmpty(propertiesDataList)) {
            return Collections.emptyList();
        }

        return propertiesDataList.stream()
            .map(this::createPropertyData)
            .collect(Collectors.toList());
    }

    /**
     * Creates a {@link PropertyDataDefinition} from JSON entry map.
     *
     * @param propertyToscaMap the JSON entry map representing a {@link PropertyDataDefinition}
     * @return a new instance of {@link PropertyDataDefinition}
     */
    public PropertyDataDefinition createPropertyData(final Map<String, Object> propertyToscaMap) {
        final PropertyDataDefinition propertyData = new PropertyDataDefinition();
        setPropertyValue(UNIQUE_ID, String.class, propertyToscaMap, propertyData::setUniqueId);
        setPropertyValue(TYPE, String.class, propertyToscaMap, propertyData::setType);
        setPropertyValue(REQUIRED, Boolean.class, propertyToscaMap, propertyData::setRequired);
        setPropertyValue(NAME, String.class, propertyToscaMap, propertyData::setName);
        setPropertyValue(VALUE, String.class, propertyToscaMap, propertyData::setValue);
        setPropertyValue(LABEL, String.class, propertyToscaMap, propertyData::setLabel);
        setPropertyValue(HIDDEN, Boolean.class, propertyToscaMap, propertyData::setHidden);
        setPropertyValue(IMMUTABLE, Boolean.class, propertyToscaMap, propertyData::setImmutable);
        setPropertyValue(PASSWORD, Boolean.class, propertyToscaMap, propertyData::setPassword);
        setPropertyValue(DEFINITION, Boolean.class, propertyToscaMap, propertyData::setDefinition);
        setPropertyValue(DESCRIPTION, String.class, propertyToscaMap, propertyData::setDescription);
        setPropertyValue(DEFAULT_VALUE, String.class, propertyToscaMap, propertyData::setDefaultValue);
        setPropertyValue(INPUT_PATH, String.class, propertyToscaMap, propertyData::setInputPath);
        setPropertyValue(STATUS, String.class, propertyToscaMap, propertyData::setStatus);
        setPropertyValue(INPUT_ID, String.class, propertyToscaMap, propertyData::setInputId);
        setPropertyValue(INSTANCE_UNIQUE_ID, String.class, propertyToscaMap, propertyData::setInstanceUniqueId);
        setPropertyValue(PROPERTY_ID, String.class, propertyToscaMap, propertyData::setPropertyId);
        setPropertyValue(PARENT_PROPERTY_TYPE, String.class, propertyToscaMap, propertyData::setParentPropertyType);
        setPropertyValue(SUB_PROPERTY_INPUT_PATH,
            String.class, propertyToscaMap, propertyData::setSubPropertyInputPath);
        setSchema(propertyToscaMap, propertyData);
        setPropertyConstraints(propertyToscaMap, propertyData);

        return propertyData;
    }

    private <T> void setPropertyValue(final JsonPresentationFields propertyField, final Class<T> propertyClass,
                                      final Map<String, Object> propertyToscaMap, final Consumer<T> setter) {
        final Object valueObj = propertyToscaMap.get(propertyField.getPresentation());
        if (propertyClass.isInstance(valueObj)) {
            setter.accept((T) valueObj);
        }
    }

    private void setPropertyConstraints(final Map<String, Object> propertyValue,
                                        final PropertyDataDefinition property) {
        final String propertyConstraintsEntry = PROPERTY_CONSTRAINTS.getPresentation();
        if (!propertyValue.containsKey(propertyConstraintsEntry)) {
            return;
        }
        final Object constraintListObj = propertyValue.get(propertyConstraintsEntry);
        if (constraintListObj instanceof List) {
            property.setPropertyConstraints((List<String>) constraintListObj);
        }
    }

    private void setSchema(final Map<String, Object> propertyValue, final PropertyDataDefinition propertyDefinition) {
        final Object schemaObj = propertyValue.get(SCHEMA.getPresentation());
        if (schemaObj == null) {
            return;
        }
        schemaDefinitionConverter.parseTo(schemaObj).ifPresent(propertyDefinition::setSchema);
    }

    //circular dependency
    @Autowired
    public void setSchemaDefinitionConverter(final SchemaDefinitionConverter schemaDefinitionConverter) {
        this.schemaDefinitionConverter = schemaDefinitionConverter;
    }

}
