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

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.tosca.ToscaPropertyType;
import org.openecomp.sdc.be.model.tosca.validators.DataTypeValidatorConverter;
import org.openecomp.sdc.be.model.tosca.validators.ListValidator;
import org.openecomp.sdc.common.util.GsonFactory;
import org.openecomp.sdc.common.util.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import fj.data.Either;

public class MapConverter implements PropertyValueConverter {

	private static MapConverter mapConverter = new MapConverter();
	private static Gson gson = GsonFactory.getGson();
	private static Logger log = LoggerFactory.getLogger(ListValidator.class.getName());

	DataTypeValidatorConverter dataTypeValidatorConverter = DataTypeValidatorConverter.getInstance();

	private static JsonParser jsonParser = new JsonParser();

	public static MapConverter getInstance() {
		return mapConverter;
	}

	public String convert(String value, String innerType, Map<String, DataTypeDefinition> dataTypes) {

		Either<String, Boolean> convertWithErrorResult = this.convertWithErrorResult(value, innerType, dataTypes);
		if (convertWithErrorResult.isRight()) {
			return null;
		}

		return convertWithErrorResult.left().value();
	}

	public Either<String, Boolean> convertWithErrorResult(String value, String innerType,
			Map<String, DataTypeDefinition> dataTypes) {

		if (value == null || value == "" || innerType == null) {
			return Either.left(value);
		}

		PropertyValueConverter innerConverter;
		PropertyValueConverter keyConverter = ToscaPropertyType.STRING.getConverter();
		ToscaPropertyType innerToscaType = ToscaPropertyType.isValidType(innerType);

		if (innerToscaType != null) {
			switch (innerToscaType) {
			case STRING:
				innerConverter = ToscaPropertyType.STRING.getConverter();
				break;
			case INTEGER:
				innerConverter = ToscaPropertyType.INTEGER.getConverter();
				break;
			case FLOAT:
				innerConverter = ToscaPropertyType.FLOAT.getConverter();
				break;
			case BOOLEAN:
				innerConverter = ToscaPropertyType.BOOLEAN.getConverter();
				break;
			case JSON:
				innerConverter = ToscaPropertyType.JSON.getConverter();
				break;
			default:
				log.debug("inner Tosca Type is unknown");
				return Either.left(value);
			}

		} else {

			log.debug("inner Tosca Type {} ia a complex data type.", innerType);

			Either<String, Boolean> validateComplexInnerType = convertComplexInnerType(value, innerType, keyConverter,
					dataTypes);

			return validateComplexInnerType;

		}

		try {
			Map<String, String> newMap = new HashMap<String, String>();

			JsonElement jsonObject = jsonParser.parse(value);
			JsonObject asJsonObject = jsonObject.getAsJsonObject();
			Set<Entry<String, JsonElement>> entrySet = asJsonObject.entrySet();
			for (Entry<String, JsonElement> entry : entrySet) {
				String key = entry.getKey();
				JsonElement jsonValue = entry.getValue();

				key = keyConverter.convert(entry.getKey(), null, dataTypes);

				String element = JsonUtils.toString(jsonValue);

				String val = innerConverter.convert(element, null, dataTypes);
				newMap.put(key, val);
			}

			String objVal;
			switch (innerToscaType) {
			case STRING:
				value = gson.toJson(newMap);
				break;
			case INTEGER:
				String key = null;
				Map<String, Integer> intMap = new HashMap<String, Integer>();
				for (Map.Entry<String, String> entry : newMap.entrySet()) {
					objVal = entry.getValue();
					key = entry.getKey();
					if (objVal != null) {
						intMap.put(key, Integer.valueOf(objVal.toString()));
					} else {
						intMap.put(key, null);
					}

				}
				value = gson.toJson(intMap);
				break;
			case FLOAT:
				value = "{";
				for (Map.Entry<String, String> entry : newMap.entrySet()) {
					objVal = entry.getValue();
					if (objVal == null) {
						objVal = "null";
					}
					key = entry.getKey();
					value += "\"" + key + "\":" + objVal.toString() + ",";
				}
				value = value.substring(0, value.length() - 1);
				value += "}";
				break;
			case BOOLEAN:
				Map<String, Boolean> boolMap = new HashMap<String, Boolean>();
				for (Map.Entry<String, String> entry : newMap.entrySet()) {
					objVal = entry.getValue();
					key = entry.getKey();
					if (objVal != null) {
						boolMap.put(key, Boolean.valueOf(objVal.toString()));
					} else {
						boolMap.put(key, null);
					}
				}
				value = gson.toJson(boolMap);
				break;
			default:
				value = gson.toJson(newMap);
				log.debug("inner Tosca Type unknown : {}", innerToscaType);
			}
		} catch (JsonParseException e) {
			log.debug("Failed to parse json : {}. {}", value, e);
			BeEcompErrorManager.getInstance().logBeInvalidJsonInput("Map Converter");
			return Either.right(false);
		}

		return Either.left(value);

	}

	/**
	 * convert the json value of map when the inner type is a complex data type
	 * 
	 * @param value
	 * @param innerType
	 * @param keyConverter
	 * @param allDataTypes
	 * @return
	 */
	private Either<String, Boolean> convertComplexInnerType(String value, String innerType,
			PropertyValueConverter keyConverter, Map<String, DataTypeDefinition> allDataTypes) {

		DataTypeDefinition dataTypeDefinition = allDataTypes.get(innerType);
		if (dataTypeDefinition == null) {
			log.debug("Cannot find data type {}", innerType);
			return Either.right(false);
		}

		Map<String, JsonElement> newMap = new HashMap<String, JsonElement>();

		try {

			JsonElement jsonObject = jsonParser.parse(value);
			JsonObject asJsonObject = jsonObject.getAsJsonObject();
			Set<Entry<String, JsonElement>> entrySet = asJsonObject.entrySet();
			for (Entry<String, JsonElement> entry : entrySet) {
				String currentKey = keyConverter.convert(entry.getKey(), null, allDataTypes);

				JsonElement currentValue = entry.getValue();

				if (currentValue != null) {

					String element = JsonUtils.toString(currentValue);

					ImmutablePair<JsonElement, Boolean> validateAndUpdate = dataTypeValidatorConverter
							.validateAndUpdate(element, dataTypeDefinition, allDataTypes);
					if (validateAndUpdate.right.booleanValue() == false) {
						log.debug("Cannot parse value {} from type {} of key {}", currentValue, innerType, currentKey);
						return Either.right(false);
					}
					JsonElement newValue = validateAndUpdate.left;
					newMap.put(currentKey, newValue);
				} else {
					newMap.put(currentKey, null);
				}
			}

		} catch (Exception e) {
			log.debug("Cannot parse value {} of map from inner type {}", value, innerType);
			return Either.right(false);
		}

		value = gson.toJson(newMap);
		return Either.left(value);
	}

}
