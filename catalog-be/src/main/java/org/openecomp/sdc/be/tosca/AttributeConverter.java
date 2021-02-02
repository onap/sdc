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

package org.openecomp.sdc.be.tosca;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import java.io.StringReader;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.onap.sdc.tosca.datatypes.model.EntrySchema;
import org.openecomp.sdc.be.model.AttributeDefinition;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.tosca.ToscaPropertyType;
import org.openecomp.sdc.be.model.tosca.converters.DataTypePropertyConverter;
import org.openecomp.sdc.be.model.tosca.converters.ToscaMapValueConverter;
import org.openecomp.sdc.be.tosca.model.ToscaAttribute;
import org.openecomp.sdc.be.tosca.model.ToscaSchemaDefinition;
import org.openecomp.sdc.common.log.enums.EcompLoggerErrorCode;
import org.openecomp.sdc.common.log.wrappers.Logger;

/**
 * Handles conversions between attribute objects.
 */
public class AttributeConverter {

    private static final Logger LOGGER = Logger.getLogger(AttributeConverter.class);
    private final Map<String, DataTypeDefinition> dataTypes;
    private final ToscaMapValueConverter toscaMapValueConverter;

    /**
     * Creates an {@link AttributeConverter} with all the required data types.
     *
     * @param dataTypes all the data types required for the conversion
     */
    public AttributeConverter(final Map<String, DataTypeDefinition> dataTypes) {
        this.dataTypes = dataTypes;
        toscaMapValueConverter = ToscaMapValueConverter.getInstance();
    }

    /**
     * Converts and {@link AttributeDefinition} to a {@link ToscaAttribute}.
     *
     * @param attributeDefinition the attribute definition to be converted
     * @return the {@link ToscaAttribute} instance based on the the given {@link AttributeDefinition} instance
     */
    public ToscaAttribute convert(final AttributeDefinition attributeDefinition) {
        final ToscaAttribute toscaAttribute = new ToscaAttribute();
        LOGGER.trace("Converting attribute '{}' from type '{}' with default value '{}'",
            attributeDefinition.getName(), attributeDefinition.getType(), attributeDefinition.getDefaultValue());
        toscaAttribute.setEntrySchema(convert(attributeDefinition.getEntry_schema()));
        toscaAttribute.setType(attributeDefinition.getType());
        toscaAttribute.setDescription(attributeDefinition.getDescription());
        toscaAttribute.setStatus(attributeDefinition.getStatus());
        final Object defaultValue = convertToToscaObject(attributeDefinition.getName(), attributeDefinition.getType(),
            attributeDefinition.getDefaultValue(), attributeDefinition.getEntry_schema(), false);
        if (defaultValue != null) {
            toscaAttribute.setDefault(defaultValue);
        }

        return toscaAttribute;
    }

    private ToscaSchemaDefinition convert(final EntrySchema entrySchema) {
        if (entrySchema == null) {
            return null;
        }

        final ToscaSchemaDefinition toscaSchemaDefinition = new ToscaSchemaDefinition();
        toscaSchemaDefinition.setType(entrySchema.getType());
        toscaSchemaDefinition.setDescription(entrySchema.getDescription());
        return toscaSchemaDefinition;
    }

    private Object convertToToscaObject(final String name, final String attributeType, String value,
                                       final EntrySchema schemaDefinition, final boolean preserveEmptyValue) {
        final String innerType = schemaDefinition == null ? attributeType : schemaDefinition.getType();
        LOGGER.trace("Converting attribute '{}' of type '{}', value '{}', innerType '{}'",
            name, attributeType, value, innerType);
        if (StringUtils.isEmpty(value)) {
            value = getTypeDefaultValue(attributeType);
            if (StringUtils.isEmpty(value)) {
                return null;
            }
        }

        try {
            boolean isScalar = true;

            ToscaPropertyType predefinedType = ToscaPropertyType.isValidType(attributeType);
            if (predefinedType == null) {
                //not predefined, search in existing data types
                final DataTypeDefinition dataTypeDefinition = dataTypes.get(attributeType);
                predefinedType = toscaMapValueConverter.isScalarType(dataTypeDefinition);
                if (predefinedType == null) {
                    isScalar = false;
                }
            } else {
                isScalar = ToscaPropertyType.getTypeIfScalar(predefinedType.getType()) != null;
            }

            //if it has a converter
            if (predefinedType != null && predefinedType.getValueConverter() != null) {
                LOGGER.trace("It's well defined type. convert it");
                return predefinedType.getValueConverter().convertToToscaValue(value, innerType, dataTypes);
            }
            final JsonElement valueAsJson = parseToJson(value);
            //no converter but scalar
            if (isScalar) {
                return toscaMapValueConverter.handleComplexJsonValue(valueAsJson);
            }

            //if it is a data type
            return toscaMapValueConverter.convertDataTypeToToscaObject(
                innerType, dataTypes, null, false, valueAsJson, preserveEmptyValue);

        } catch (final JsonParseException e) {
            LOGGER.error(EcompLoggerErrorCode.SCHEMA_ERROR, "Attribute Converter",
                "Failed to parse json value :", e);
            return null;
        } catch (final Exception e) {
            LOGGER.error(EcompLoggerErrorCode.UNKNOWN_ERROR, "Attribute Converter",
                "Unexpected error occurred while converting attribute value to TOSCA", e);
            return null;
        }

    }

    private JsonElement parseToJson(final String value) throws JsonParseException {
        final StringReader reader = new StringReader(value);
        final JsonReader jsonReader = new JsonReader(reader);
        jsonReader.setLenient(true);
        return new JsonParser().parse(jsonReader);
    }

    private String getTypeDefaultValue(final String attributeType) {
        return DataTypePropertyConverter.getInstance().getDataTypePropertiesDefaultValuesRec(attributeType, dataTypes);
    }

}
