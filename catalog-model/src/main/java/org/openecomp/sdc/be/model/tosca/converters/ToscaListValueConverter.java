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

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.tosca.ToscaPropertyType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;

public class ToscaListValueConverter extends ToscaValueBaseConverter implements ToscaValueConverter {
	private static ToscaListValueConverter listConverter = new ToscaListValueConverter();
	private JsonParser jsonParser = new JsonParser();
	private static Logger log = LoggerFactory.getLogger(ToscaListValueConverter.class.getName());

	public static ToscaListValueConverter getInstance() {
		return listConverter;
	}

	private ToscaListValueConverter() {

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
			JsonElement jsonElement = null;
			try {
				StringReader reader = new StringReader(value);
				JsonReader jsonReader = new JsonReader(reader);
				jsonReader.setLenient(true);

				jsonElement = jsonParser.parse(jsonReader);
			} catch (JsonSyntaxException e) {
				log.debug("convertToToscaValue failed to parse json value :", e);
				return null;
			}
			if (jsonElement == null || true == jsonElement.isJsonNull()) {
				log.debug("convertToToscaValue json element is null");
				return null;
			}
			if (jsonElement.isJsonArray() == false) {
				// get_input all array like get_input: qrouter_names
				return handleComplexJsonValue(jsonElement);
			}
			JsonArray asJsonArray = jsonElement.getAsJsonArray();

			ArrayList<Object> toscaList = new ArrayList<Object>();
			final boolean isScalarF = isScalar;
			final ToscaValueConverter innerConverterFinal = innerConverter;
			asJsonArray.forEach(e -> {
				Object convertedValue = null;
				if (isScalarF) {
					log.debug("try to convert scalar value {}", e.getAsString());
					if (e.getAsString() == null) {
						convertedValue = null;
					} else {
						JsonElement singleElement = jsonParser.parse(e.getAsString());
						if (singleElement.isJsonPrimitive()) {
							convertedValue = innerConverterFinal.convertToToscaValue(e.getAsString(), innerType,
									dataTypes);
						} else {
							convertedValue = handleComplexJsonValue(singleElement);
						}
					}
				} else {
					JsonObject asJsonObject = e.getAsJsonObject();
					Set<Entry<String, JsonElement>> entrySet = asJsonObject.entrySet();

					DataTypeDefinition dataTypeDefinition = dataTypes.get(innerType);
					Map<String, PropertyDefinition> allProperties = getAllProperties(dataTypeDefinition);
					Map<String, Object> toscaObjectPresentation = new HashMap<>();
					// log.debug("try to convert datatype value {}",
					// e.getAsString());

					for (Entry<String, JsonElement> entry : entrySet) {
						String propName = entry.getKey();

						JsonElement elementValue = entry.getValue();
						PropertyDefinition propertyDefinition = allProperties.get(propName);
						if (propertyDefinition == null) {
							log.debug("The property {} was not found under data type {}", propName, dataTypeDefinition.getName());
							continue;
							// return null;
						}
						String type = propertyDefinition.getType();
						ToscaPropertyType propertyType = ToscaPropertyType.isValidType(type);
						Object convValue;
						if (propertyType != null) {
							if (elementValue.isJsonPrimitive()) {
								ToscaValueConverter valueConverter = propertyType.getValueConverter();
								convValue = valueConverter.convertToToscaValue(elementValue.getAsString(), type,
										dataTypes);
							} else {
								if (ToscaPropertyType.MAP.equals(type) || ToscaPropertyType.LIST.equals(propertyType)) {
									ToscaValueConverter valueConverter = propertyType.getValueConverter();
									String json = gson.toJson(elementValue);
									String innerTypeRecursive = propertyDefinition.getSchema().getProperty().getType();
									convValue = valueConverter.convertToToscaValue(json, innerTypeRecursive, dataTypes);
								} else {
									convValue = handleComplexJsonValue(elementValue);
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
		} catch (

		JsonParseException e) {
			log.debug("Failed to parse json : {}. {}", value, e);
			BeEcompErrorManager.getInstance().logBeInvalidJsonInput("List Converter");
			return null;
		}
	}

}
