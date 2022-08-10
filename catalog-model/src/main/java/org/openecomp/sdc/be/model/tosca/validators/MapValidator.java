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
package org.openecomp.sdc.be.model.tosca.validators;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.tosca.ToscaPropertyType;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.common.util.JsonUtils;

/*
 * Property Type Map correct usage:
 * null key null value = Yaml reader error
valid key null value = key & value deleted
duplicated keys = last key is taken
mismatch between inner type and values type = returned mismatch in data type
validators and converters works the same as before

Types:
when written line by line :
                    key1 : val1
                    key2 : val2
key1 and val does not need "    " , even if val1 is a string.
when written as one line : {"key1":val1 , "key2":val2}
Keys always need " " around them.
*/
public class MapValidator implements PropertyTypeValidator {

    private static final Logger log = Logger.getLogger(MapValidator.class.getName());
    private static final MapValidator mapValidator = new MapValidator();
    private static final DataTypeValidatorConverter dataTypeValidatorConverter = DataTypeValidatorConverter.getInstance();

    public static MapValidator getInstance() {
        return mapValidator;
    }

    @Override
    public boolean isValid(String value, String innerType, Map<String, DataTypeDefinition> allDataTypes) {
        if (StringUtils.isEmpty(value)) {
            return true;
        }
        if (innerType == null) {
            return false;
        }
        PropertyTypeValidator innerValidator;
        PropertyTypeValidator keyValidator = ToscaPropertyType.KEY.getValidator();
        ToscaPropertyType innerToscaType = ToscaPropertyType.isValidType(innerType);
        if (innerToscaType != null) {
            switch (innerToscaType) {
                case STRING:
                    innerValidator = ToscaPropertyType.STRING.getValidator();
                    break;
                case INTEGER:
                    innerValidator = ToscaPropertyType.INTEGER.getValidator();
                    break;
                case FLOAT:
                    innerValidator = ToscaPropertyType.FLOAT.getValidator();
                    break;
                case BOOLEAN:
                    innerValidator = ToscaPropertyType.BOOLEAN.getValidator();
                    break;
                case JSON:
                    innerValidator = ToscaPropertyType.JSON.getValidator();
                    break;
                default:
                    log.debug("inner Tosca Type is unknown. {}", innerToscaType);
                    return false;
            }
        } else {
            log.debug("inner Tosca Type is: {}", innerType);
            boolean isValid = validateComplexInnerType(value, innerType, allDataTypes);
            log.debug("Finish to validate value {} of map with inner type {}. result is {}", value, innerType, isValid);
            return isValid;
        }
        try {
            JsonElement jsonObject = JsonParser.parseString(value);
            if (!jsonObject.isJsonObject()) {
                return false;
            }
            JsonObject valueAsJson = jsonObject.getAsJsonObject();
            return validateJsonObject(allDataTypes, innerValidator, keyValidator, valueAsJson);
        } catch (JsonSyntaxException e) {
            log.debug("Failed to parse json : {}", value, e);
            BeEcompErrorManager.getInstance().logBeInvalidJsonInput("Map Validator");
        }
        return false;
    }

    private boolean validateJsonObject(Map<String, DataTypeDefinition> allDataTypes, PropertyTypeValidator innerValidator,
                                       PropertyTypeValidator keyValidator, JsonObject asJsonObject) {
        Set<Entry<String, JsonElement>> entrySet = asJsonObject.entrySet();
        for (Entry<String, JsonElement> entry : entrySet) {
            String currentKey = entry.getKey();
            JsonElement jsonValue = entry.getValue();
            String element = JsonUtils.toString(jsonValue);
            if (!innerValidator.isValid(element, null, allDataTypes) || !keyValidator.isValid(entry.getKey(), null, allDataTypes)) {
                log.debug("validation of key : {}, element : {} failed", currentKey, entry.getValue());
                return false;
            }
        }
        return true;
    }

    private boolean validateComplexInnerType(String value, String innerType, Map<String, DataTypeDefinition> allDataTypes) {
        DataTypeDefinition innerDataTypeDefinition = allDataTypes.get(innerType);
        if (innerDataTypeDefinition == null) {
            log.debug("Data type {} cannot be found in our data types.", innerType);
            return false;
        }
        try {
            JsonElement jsonObject = JsonParser.parseString(value);
            JsonObject asJsonObject = jsonObject.getAsJsonObject();
            Set<Entry<String, JsonElement>> entrySet = asJsonObject.entrySet();
            for (Entry<String, JsonElement> entry : entrySet) {
                String currentKey = entry.getKey();
                JsonElement currentValue = entry.getValue();
                if (currentValue != null) {
                    String element = JsonUtils.toString(currentValue);
                    boolean isValid = dataTypeValidatorConverter.isValid(element, innerDataTypeDefinition, allDataTypes);
                    if (!isValid) {
                        log.debug("Cannot parse value {} from type {} of key {}", currentValue, innerType, currentKey);
                        return false;
                    }
                }
            }
        } catch (Exception e) {
            log.debug("Cannot parse value {} of map from inner type {}", value, innerType, e);
            return false;
        }
        return true;
    }

    @Override
    public boolean isValid(String value, String innerType) {
        return isValid(value, innerType, null);
    }
}
