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

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.tosca.ToscaPropertyType;
import org.openecomp.sdc.common.util.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

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

	private static MapValidator mapValidator = new MapValidator();

	private static Logger log = LoggerFactory.getLogger(MapValidator.class.getName());
	Gson gson = new Gson();

	private static DataTypeValidatorConverter dataTypeValidatorConverter = DataTypeValidatorConverter.getInstance();

	private static JsonParser jsonParser = new JsonParser();

	public static MapValidator getInstance() {
		return mapValidator;
	}

	@Override
	public boolean isValid(String value, String innerType, Map<String, DataTypeDefinition> allDataTypes) {

		if (value == null || value == "") {
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
			log.debug("Finish to validate value {} of map with inner type {}. result is {}",value,innerType,isValid);
			return isValid;

		}

		try {
			JsonElement jsonObject = jsonParser.parse(value);
			JsonObject asJsonObject = jsonObject.getAsJsonObject();
			Set<Entry<String, JsonElement>> entrySet = asJsonObject.entrySet();
			for (Entry<String, JsonElement> entry : entrySet) {
				String currentKey = entry.getKey();
				JsonElement jsonValue = entry.getValue();

				String element = JsonUtils.toString(jsonValue);

				if (!innerValidator.isValid(element, null, allDataTypes)
						|| !keyValidator.isValid(entry.getKey(), null, allDataTypes)) {
					log.debug("validation of key : {}, element : {} failed", currentKey, entry.getValue());
					return false;
				}
			}

			return true;
		} catch (JsonSyntaxException e) {
			log.debug("Failed to parse json : {}", value, e);
			BeEcompErrorManager.getInstance().logBeInvalidJsonInput("Map Validator");
		}

		return false;

	}

	private boolean validateComplexInnerType(String value, String innerType,
			Map<String, DataTypeDefinition> allDataTypes) {

		DataTypeDefinition innerDataTypeDefinition = allDataTypes.get(innerType);
		if (innerDataTypeDefinition == null) {
			log.debug("Data type {} cannot be found in our data types.", innerType);
			return false;
		}

		try {
			JsonElement jsonObject = jsonParser.parse(value);
			JsonObject asJsonObject = jsonObject.getAsJsonObject();
			Set<Entry<String, JsonElement>> entrySet = asJsonObject.entrySet();
			for (Entry<String, JsonElement> entry : entrySet) {
				String currentKey = entry.getKey();
				JsonElement currentValue = entry.getValue();

				if (currentValue != null) {
					String element = JsonUtils.toString(currentValue);
					boolean isValid = dataTypeValidatorConverter.isValid(element, innerDataTypeDefinition,
							allDataTypes);
					if (!isValid) {
						log.debug("Cannot parse value {} from type {} of key {}",currentValue,innerType,currentKey);
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
