/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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
package org.openecomp.sdc.be.model.tosca.converters;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.jetbrains.annotations.Nullable;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.tosca.ToscaPropertyType;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.common.util.JsonUtils;

public class ToscaListValueConverter extends ToscaValueBaseConverter implements ToscaValueConverter {

    private static final Logger log = Logger.getLogger(ToscaListValueConverter.class.getName());
    private static final ToscaListValueConverter listConverter = new ToscaListValueConverter();

    private ToscaListValueConverter() {
    }

    public static ToscaListValueConverter getInstance() {
        return listConverter;
    }

    @Override
    public Object convertToToscaValue(String value, String innerType, Map<String, DataTypeDefinition> dataTypes) {
        if (value == null) {
            return null;
        }
        try {
            ToscaPropertyType innerToscaType = ToscaPropertyType.isValidType(innerType);
            ToscaValueConverter innerConverter = null;
            boolean isScalar = true;
            if (innerToscaType != null) {
                innerConverter = innerToscaType.getValueConverter();
            } else {
                DataTypeDefinition dataTypeDefinition = dataTypes.get(innerType);
                if (dataTypeDefinition != null) {
                    ToscaPropertyType toscaPropertyType = null;
                    if ((toscaPropertyType = isScalarType(dataTypeDefinition)) != null) {
                        innerConverter = toscaPropertyType.getValueConverter();
                    } else {
                        isScalar = false;
                        innerConverter = ToscaMapValueConverter.getInstance();
                    }
                } else {
                    log.debug("inner Tosca Type is null");
                    return value;
                }
            }
            JsonElement jsonElement;
            jsonElement = parseToJson(value);
            if (jsonElement == null || jsonElement.isJsonNull()) {
                log.debug("convertToToscaValue json element is null");
                return null;
            }
            if (!jsonElement.isJsonArray()) {
                // get_input all array like get_input: qrouter_names
                return handleComplexJsonValue(jsonElement);
            }
            JsonArray asJsonArray = jsonElement.getAsJsonArray();
            ArrayList<Object> toscaList = new ArrayList<>();
            final boolean isScalarF = isScalar;
            final ToscaValueConverter innerConverterFinal = innerConverter;
            asJsonArray.forEach(e -> {
                Object convertedValue = null;
                if (isScalarF) {
                    if (e.isJsonPrimitive()) {
                        String jsonAsString = e.getAsString();
                        log.debug("try to convert scalar value {}", jsonAsString);
                        convertedValue = innerConverterFinal.convertToToscaValue(jsonAsString, innerType, dataTypes);
                    } else {
                        convertedValue = handleComplexJsonValue(e);
                    }
                } else {
                    JsonObject asJsonObject = e.getAsJsonObject();
                    Set<Entry<String, JsonElement>> entrySet = asJsonObject.entrySet();
                    DataTypeDefinition dataTypeDefinition = dataTypes.get(innerType);
                    Map<String, PropertyDefinition> allProperties = getAllProperties(dataTypeDefinition);
                    Map<String, Object> toscaObjectPresentation = new HashMap<>();
                    for (Entry<String, JsonElement> entry : entrySet) {
                        String propName = entry.getKey();
                        JsonElement elementValue = entry.getValue();
                        PropertyDefinition propertyDefinition = allProperties.get(propName);
                        if (propertyDefinition == null) {
                            log.debug("The property {} was not found under data type {}", propName, dataTypeDefinition.getName());
                            continue;
                        }
                        String type = propertyDefinition.getType();
                        ToscaPropertyType propertyType = ToscaPropertyType.isValidType(type);
                        Object convValue;
                        if (propertyType != null) {
                            if (elementValue.isJsonPrimitive()) {
                                ToscaValueConverter valueConverter = propertyType.getValueConverter();
                                convValue = valueConverter.convertToToscaValue(elementValue.getAsString(), type, dataTypes);
                            } else {
                                if (JsonUtils.isEmptyJson(elementValue)) {
                                    convValue = null;
                                } else {
                                    if (ToscaPropertyType.MAP == propertyType || ToscaPropertyType.LIST == propertyType) {
                                        ToscaValueConverter valueConverter = propertyType.getValueConverter();
                                        String json = gson.toJson(elementValue);
                                        String innerTypeRecursive = propertyDefinition.getSchema().getProperty().getType();
                                        convValue = valueConverter.convertToToscaValue(json, innerTypeRecursive, dataTypes);
                                    } else {
                                        convValue = handleComplexJsonValue(elementValue);
                                    }
                                }
                            }
                        } else {
                            String json = gson.toJson(elementValue);
                            convValue = convertToToscaValue(json, type, dataTypes);
                        }
                        toscaObjectPresentation.put(propName, convValue);
                    }
                    convertedValue = toscaObjectPresentation;
                }
                toscaList.add(convertedValue);
            });
            return toscaList;
        } catch (JsonParseException e) {
            log.debug("Failed to parse json : {}", value, e);
            BeEcompErrorManager.getInstance().logBeInvalidJsonInput("List Converter");
            return null;
        }
    }

    private JsonElement parseToJson(final String value) {
        try {
            final StringReader reader = new StringReader(value);
            final JsonReader jsonReader = new JsonReader(reader);
            jsonReader.setLenient(true);
            return JsonParser.parseReader(jsonReader);
        } catch (final JsonSyntaxException e) {
            log.debug("convertToToscaValue failed to parse json value :", e);
            return null;
        }
    }
}
