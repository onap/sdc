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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.operations.impl.PropertyOperation;
import org.openecomp.sdc.be.model.tosca.ToscaPropertyType;
import org.openecomp.sdc.be.model.tosca.converters.PropertyValueConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;

public class DataTypeValidatorConverter {

	private static DataTypeValidatorConverter dataTypeValidatorConverter = new DataTypeValidatorConverter();

	public static DataTypeValidatorConverter getInstance() {
		return dataTypeValidatorConverter;
	}

	private DataTypeValidatorConverter() {

	}

	private static Logger log = LoggerFactory.getLogger(DataTypeValidatorConverter.class.getName());

	JsonParser jsonParser = new JsonParser();

	Gson gson = new Gson();

	ImmutablePair<JsonElement, Boolean> falseResult = new ImmutablePair<JsonElement, Boolean>(null, false);
	ImmutablePair<JsonElement, Boolean> trueEmptyResult = new ImmutablePair<JsonElement, Boolean>(null, true);

	ImmutablePair<String, Boolean> trueStringEmptyResult = new ImmutablePair<String, Boolean>(null, true);
	ImmutablePair<String, Boolean> falseStringEmptyResult = new ImmutablePair<String, Boolean>(null, true);

	private ToscaPropertyType isDataTypeDerviedFromScalarType(DataTypeDefinition dataTypeDef) {

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

	private ImmutablePair<JsonElement, Boolean> validateAndUpdate(JsonElement jsonElement, DataTypeDefinition dataTypeDefinition, Map<String, DataTypeDefinition> allDataTypes) {

		Map<String, PropertyDefinition> allProperties = getAllProperties(dataTypeDefinition);

		ToscaPropertyType toscaPropertyType = null;
		if ((toscaPropertyType = isDataTypeDerviedFromScalarType(dataTypeDefinition)) != null) {

			PropertyTypeValidator validator = toscaPropertyType.getValidator();
			PropertyValueConverter converter = toscaPropertyType.getConverter();
			if (jsonElement == null || true == jsonElement.isJsonNull()) {
				boolean valid = validator.isValid(null, null, allDataTypes);
				if (!valid) {
					log.trace("Failed in validation of property {} from type {}",  dataTypeDefinition.getName(), dataTypeDefinition.getName());
					return falseResult;
				}
				return new ImmutablePair<JsonElement, Boolean>(jsonElement, true);

			} else {
				if (jsonElement.isJsonPrimitive()) {
					String value = null;
					if (jsonElement != null) {
						if (jsonElement.toString().isEmpty()) {
							value = "";
						} else {
							value = jsonElement.toString();
						}
					}
					boolean valid = validator.isValid(value, null, null);
					if (!valid) {
						log.trace("Failed in validation of property {} from type {}. Json primitive value is {}", dataTypeDefinition.getName(), dataTypeDefinition.getName(), value);
						return falseResult;
					}

					String convertedValue = converter.convert(value, null, allDataTypes);
					JsonElement element = null;
					try {
						element = jsonParser.parse(convertedValue);
					} catch (JsonSyntaxException e) {
						log.debug("Failed to parse value {} of property {} {}", convertedValue, dataTypeDefinition.getName(), e);
						return falseResult;
					}

					return new ImmutablePair<JsonElement, Boolean>(element, true);

				} else {
					// MAP, LIST, OTHER types cannot be applied data type
					// definition scalar type. We currently cannot derived from
					// map/list. (cannot add the entry schema to it)
					log.debug("We cannot derive from list/map. Thus, the value cannot be not primitive since the data type {} is scalar one", dataTypeDefinition.getName());

					return falseResult;
				}
			}
		} else {

			if (jsonElement == null || jsonElement.isJsonNull()) {

				return new ImmutablePair<JsonElement, Boolean>(jsonElement, true);

			} else {

				if (jsonElement.isJsonObject()) {

					JsonObject buildJsonObject = new JsonObject();

					JsonObject asJsonObject = jsonElement.getAsJsonObject();
					Set<Entry<String, JsonElement>> entrySet = asJsonObject.entrySet();

					for (Entry<String, JsonElement> entry : entrySet) {
						String propName = entry.getKey();

						JsonElement elementValue = entry.getValue();

						PropertyDefinition propertyDefinition = allProperties.get(propName);
						if (propertyDefinition == null) {
							log.debug("The property {} was not found under data type {}" ,propName, dataTypeDefinition.getName());
							return falseResult;
						}
						String type = propertyDefinition.getType();
						boolean isScalarType = ToscaPropertyType.isScalarType(type);

						if (isScalarType) {
							ToscaPropertyType propertyType = ToscaPropertyType.isValidType(type);
							if (propertyType == null) {
								log.debug("cannot find the {} under default tosca property types", type);
								return falseResult;
							}
							PropertyTypeValidator validator = propertyType.getValidator();
							String innerType = null;
							if (propertyType == ToscaPropertyType.LIST || propertyType == ToscaPropertyType.MAP) {
								if (propertyDefinition.getSchema() != null && propertyDefinition.getSchema().getProperty() != null) {
									innerType = propertyDefinition.getSchema().getProperty().getType();
									if (innerType == null) {
										log.debug("Property type {} must have inner type in its declaration.", propertyType);
										return falseResult;
									}
								}
							}

							String value = null;
							if (elementValue != null) {
								if (elementValue.isJsonPrimitive() && elementValue.getAsString().isEmpty()) {
									value = "";
								} else {
									value = elementValue.toString();
								}
							}

							boolean isValid = validator.isValid(value, innerType, allDataTypes);
							if (false == isValid) {
								log.debug("Failed to validate the value {} from type {}", value, propertyType);
								return falseResult;
							}

							PropertyValueConverter converter = propertyType.getConverter();
							String convertedValue = converter.convert(value, innerType, allDataTypes);

							JsonElement element = null;
							if (convertedValue != null) {
								if (convertedValue.isEmpty()) {
									element = new JsonPrimitive("");
								} else {
									try {
										element = jsonParser.parse(convertedValue);
									} catch (JsonSyntaxException e) {
										log.debug("Failed to parse value {} of type {}", convertedValue, propertyType, e);
										return falseResult;
									}
								}
							}
							buildJsonObject.add(propName, element);

						} else {

							DataTypeDefinition typeDefinition = allDataTypes.get(type);
							if (typeDefinition == null) {
								log.debug("The data type {} cannot be found in the given data type list.", type);
								return falseResult;
							}

							ImmutablePair<JsonElement, Boolean> isValid = validateAndUpdate(elementValue, typeDefinition, allDataTypes);

							if (!isValid.getRight().booleanValue()) {
								log.debug("Failed in validation of value {} from type {}", (elementValue != null ? elementValue.toString() : null), typeDefinition.getName());
								return falseResult;
							}

							buildJsonObject.add(propName, isValid.getLeft());
						}

					}

					return new ImmutablePair<JsonElement, Boolean>(buildJsonObject, true);
				} else {
					log.debug("The value {} of type {} should be json object", (jsonElement != null ? jsonElement.toString() : null), dataTypeDefinition.getName());
					return falseResult;
				}

			}
		}

	}

	public ImmutablePair<JsonElement, Boolean> validateAndUpdate(String value, DataTypeDefinition dataTypeDefinition, Map<String, DataTypeDefinition> allDataTypes) {

		ImmutablePair<JsonElement, Boolean> result = falseResult;

		if (value == null || value.isEmpty()) {
			return trueEmptyResult;
		}

		JsonElement jsonElement = null;
		try {
			jsonElement = jsonParser.parse(value);
		} catch (JsonSyntaxException e) {
			return falseResult;
		}

		result = validateAndUpdate(jsonElement, dataTypeDefinition, allDataTypes);

		return result;
	}

	private Map<String, PropertyDefinition> getAllProperties(DataTypeDefinition dataTypeDefinition) {

		Map<String, PropertyDefinition> allParentsProps = new HashMap<String, PropertyDefinition>();

		while (dataTypeDefinition != null) {

			List<PropertyDefinition> currentParentsProps = dataTypeDefinition.getProperties();
			if (currentParentsProps != null) {
				currentParentsProps.stream().forEach(p -> allParentsProps.put(p.getName(), p));
			}

			dataTypeDefinition = dataTypeDefinition.getDerivedFrom();
		}

		return allParentsProps;
	}

	private String getValueFromJsonElement(JsonElement jsonElement) {
		String value = null;

		if (jsonElement == null || jsonElement.isJsonNull()) {
			value = PropertyOperation.EMPTY_VALUE;
		} else {
			if (jsonElement.toString().isEmpty()) {
				value = "";
			} else {
				value = jsonElement.toString();
			}
		}

		return value;
	}

	public boolean isValid(String value, DataTypeDefinition dataTypeDefinition, Map<String, DataTypeDefinition> allDataTypes) {

		boolean result = false;

		if (value == null || value.isEmpty()) {
			return true;
		}

		JsonElement jsonElement = null;
		try {
			jsonElement = jsonParser.parse(value);
		} catch (JsonSyntaxException e) {
			log.debug("Failed to parse the value {} from type {}", value, dataTypeDefinition, e);
			return false;
		}

		result = isValid(jsonElement, dataTypeDefinition, allDataTypes);

		return result;
	}

	private boolean isValid(JsonElement jsonElement, DataTypeDefinition dataTypeDefinition, Map<String, DataTypeDefinition> allDataTypes) {

		Map<String, PropertyDefinition> allProperties = getAllProperties(dataTypeDefinition);

		ToscaPropertyType toscaPropertyType = null;
		if ((toscaPropertyType = isDataTypeDerviedFromScalarType(dataTypeDefinition)) != null) {

			PropertyTypeValidator validator = toscaPropertyType.getValidator();
			if (jsonElement == null || true == jsonElement.isJsonNull()) {
				boolean valid = validator.isValid(null, null, allDataTypes);
				if (false == valid) {
					log.trace("Failed in validation of property {} from type {}", dataTypeDefinition.getName(), dataTypeDefinition.getName());
					return false;
				}

				return true;

			} else {
				if (true == jsonElement.isJsonPrimitive()) {
					String value = null;
					if (jsonElement != null) {
						if (jsonElement.toString().isEmpty()) {
							value = "";
						} else {
							value = jsonElement.toString();
						}
					}
					boolean valid = validator.isValid(value, null, allDataTypes);
					if (false == valid) {
						log.trace("Failed in validation of property {} from type {}. Json primitive value is {}", dataTypeDefinition.getName(), dataTypeDefinition.getName(), value);
						return false;
					}

					return true;

				} else {
					// MAP, LIST, OTHER types cannot be applied data type
					// definition scalar type. We currently cannot derived from
					// map/list. (cannot add the entry schema to it)
					log.debug("We cannot derive from list/map. Thus, the value cannot be not primitive since the data type {} is scalar one", dataTypeDefinition.getName());

					return false;
				}
			}
		} else {

			if (jsonElement == null || jsonElement.isJsonNull()) {

				return true;

			} else {

				if (jsonElement.isJsonObject()) {

					JsonObject asJsonObject = jsonElement.getAsJsonObject();
					Set<Entry<String, JsonElement>> entrySet = asJsonObject.entrySet();

					for (Entry<String, JsonElement> entry : entrySet) {
						String propName = entry.getKey();

						JsonElement elementValue = entry.getValue();

						PropertyDefinition propertyDefinition = allProperties.get(propName);
						if (propertyDefinition == null) {
							log.debug("The property {} was not found under data type {}", propName, dataTypeDefinition.getName());
							return false;
						}
						String type = propertyDefinition.getType();
						boolean isScalarType = ToscaPropertyType.isScalarType(type);

						if (true == isScalarType) {
							ToscaPropertyType propertyType = ToscaPropertyType.isValidType(type);
							if (propertyType == null) {
								log.debug("cannot find the {} under default tosca property types", type);
								return false;
							}
							PropertyTypeValidator validator = propertyType.getValidator();
							String innerType = null;
							if (propertyType == ToscaPropertyType.LIST || propertyType == ToscaPropertyType.MAP) {
								if (propertyDefinition.getSchema() != null && propertyDefinition.getSchema().getProperty() != null) {
									innerType = propertyDefinition.getSchema().getProperty().getType();
									if (innerType == null) {
										log.debug("Property type {} must have inner type in its declaration.", propertyType);
										return false;
									}
								}
							}

							String value = null;
							if (elementValue != null) {
								if (elementValue.isJsonPrimitive() && elementValue.getAsString().isEmpty()) {
									value = "";
								} else {
									value = elementValue.toString();
								}
							}

							boolean isValid = validator.isValid(value, innerType, allDataTypes);
							if (false == isValid) {
								log.debug("Failed to validate the value {} from type {}", value, propertyType);
								return false;
							}

						} else {

							DataTypeDefinition typeDefinition = allDataTypes.get(type);
							if (typeDefinition == null) {
								log.debug("The data type {} cannot be found in the given data type list.", type);
								return false;
							}

							boolean isValid = isValid(elementValue, typeDefinition, allDataTypes);

							if (false == isValid) {
								log.debug("Failed in validation of value {} from type {}", (elementValue != null ? elementValue.toString() : null), typeDefinition.getName());
								return false;
							}

						}

					}

					return true;
				} else {
					log.debug("The value {} of type {} should be json object", (jsonElement != null ? jsonElement.toString() : null), dataTypeDefinition.getName());
					return false;
				}

			}
		}

	}
}
