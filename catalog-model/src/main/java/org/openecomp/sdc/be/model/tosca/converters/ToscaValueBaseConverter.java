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

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.tosca.ToscaPropertyType;
import org.openecomp.sdc.common.log.wrappers.Logger;

public class ToscaValueBaseConverter {

    private static final Logger log = Logger.getLogger(ToscaValueBaseConverter.class.getName());
    protected Gson gson = new Gson();

    /**
     * checks is received Object empty or equals null or not It is relevant only if received Object is instance of String, Map or List class.
     *
     * @param convertedValue
     * @return
     */
    public static boolean isEmptyObjectValue(Object convertedValue) {
        if ((convertedValue == null) || (convertedValue instanceof String && ((String) convertedValue).isEmpty()) || (convertedValue instanceof Map
            && ((Map) convertedValue).isEmpty()) || (convertedValue instanceof List && ((List) convertedValue).isEmpty())) {
            return true;
        }
        return false;
    }

    protected Map<String, PropertyDefinition> getAllProperties(DataTypeDefinition dataTypeDefinition) {
        Map<String, PropertyDefinition> allParentsProps = new HashMap<>();
        while (dataTypeDefinition != null) {
            List<PropertyDefinition> currentParentsProps = dataTypeDefinition.getProperties();
            if (currentParentsProps != null) {
                currentParentsProps.stream().forEach(p -> allParentsProps.put(p.getName(), p));
            }
            dataTypeDefinition = dataTypeDefinition.getDerivedFrom();
        }
        return allParentsProps;
    }

    public ToscaPropertyType isScalarType(DataTypeDefinition dataTypeDef) {
        ToscaPropertyType result = null;
        DataTypeDefinition dataType = dataTypeDef;
        while (dataType != null) {
            String name = dataType.getName();
            ToscaPropertyType typeIfScalar = ToscaPropertyType.getTypeIfScalar(name);
            if (typeIfScalar != null) {
                result = typeIfScalar;
                break;
            }
            dataType = dataType.getDerivedFrom();
        }
        return result;
    }

    public Object handleComplexJsonValue(final JsonElement jsonElement) {
        if (jsonElement.isJsonNull()) {
            return null;
        }
        if (jsonElement.isJsonObject()) {
            return handleJsonObject(jsonElement);
        }
        if (jsonElement.isJsonArray()) {
            return handleJsonArray(jsonElement);
        }
        if (jsonElement.isJsonPrimitive()) {
            return json2JavaPrimitive(jsonElement.getAsJsonPrimitive());
        }
        log.debug("JSON type '{}' not supported", jsonElement);
        return null;
    }

    private Map<String, Object> handleJsonObject(final JsonElement jsonElement) {
        final Map<String, Object> jsonObjectAsMap = new HashMap<>();
        final JsonObject jsonObject = jsonElement.getAsJsonObject();
        for (final Entry<String, JsonElement> entry : jsonObject.entrySet()) {
            jsonObjectAsMap.put(entry.getKey(), handleComplexJsonValue(entry.getValue()));
        }
        return jsonObjectAsMap;
    }

    private List<Object> handleJsonArray(final JsonElement entry) {
        final List<Object> jsonAsArray = new ArrayList<>();
        final JsonArray jsonArray = entry.getAsJsonArray();
        for (final JsonElement jsonElement : jsonArray) {
            jsonAsArray.add(handleComplexJsonValue(jsonElement));
        }
        return jsonAsArray;
    }

    public Object json2JavaPrimitive(final JsonPrimitive jsonPrimitive) {
        if (jsonPrimitive.isBoolean()) {
            return jsonPrimitive.getAsBoolean();
        }
        if (jsonPrimitive.isString()) {
            return jsonPrimitive.getAsString();
        }
        if (jsonPrimitive.isNumber()) {
            if (jsonPrimitive.getAsString().contains(".")) {
                return jsonPrimitive.getAsDouble();
            }
            return jsonPrimitive.getAsInt();
        }
        throw new IllegalStateException(String.format("JSON primitive not supported: %s", jsonPrimitive));
    }
}
