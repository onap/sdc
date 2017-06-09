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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import fj.data.Either;

public class ListConverter implements PropertyValueConverter {

	private static ListConverter listConverter = new ListConverter();
	private static Gson gson = GsonFactory.getGson();
	private static Logger log = LoggerFactory.getLogger(ListValidator.class.getName());

	DataTypeValidatorConverter dataTypeValidatorConverter = DataTypeValidatorConverter.getInstance();

	private static JsonParser jsonParser = new JsonParser();

	public static ListConverter getInstance() {
		return listConverter;
	}

	@Override
	public String convert(String value, String innerType, Map<String, DataTypeDefinition> dataTypes) {
		Either<String, Boolean> convertWithErrorResult = this.convertWithErrorResult(value, innerType, dataTypes);
		if (convertWithErrorResult.isRight()) {
			return null;
		}

		return convertWithErrorResult.left().value();
	}

	public Either<String, Boolean> convertWithErrorResult(String value, String innerType,
			Map<String, DataTypeDefinition> dataTypes) {
		if (value == null || innerType == null) {
			return Either.left(value);
		}

		PropertyValueConverter innerConverter;
		ToscaPropertyType innerToscaType = ToscaPropertyType.isValidType(innerType);

		if (innerToscaType != null) {
			PropertyValueConverter innerConverter1;
			switch (innerToscaType) {
			case STRING:
				innerConverter1 = ToscaPropertyType.STRING.getConverter();
				break;
			case INTEGER:
				innerConverter1 = ToscaPropertyType.INTEGER.getConverter();
				break;
			case FLOAT:
				innerConverter1 = ToscaPropertyType.FLOAT.getConverter();
				break;
			case BOOLEAN:
				innerConverter1 = ToscaPropertyType.BOOLEAN.getConverter();
				break;
			case JSON:
				innerConverter1 = ToscaPropertyType.JSON.getConverter();
				break;
			default:
				log.debug("inner Tosca Type is unknown");
				return Either.left(value);
			}
			innerConverter = innerConverter1;
		} else {
			log.debug("inner Tosca Type {} ia a complex data type.", innerType);

			Either<String, Boolean> validateComplexInnerType = convertComplexInnerType(value, innerType, dataTypes);

			return validateComplexInnerType;
		}

		try {
			ArrayList<String> newList = new ArrayList<String>();

			JsonArray jo = (JsonArray) jsonParser.parse(value);
			if(ToscaPropertyType.JSON == innerToscaType)
				return Either.left(value);
			int size = jo.size();
			for (int i = 0; i < size; i++) {
				JsonElement currentValue = jo.get(i);
				String element = JsonUtils.toString(currentValue);

				if (element == null || element.isEmpty()) {
					continue;
				}
				element = innerConverter.convert(element, null, dataTypes);
				newList.add(element);
			}

			switch (innerToscaType) {
			case STRING:
				value = gson.toJson(newList);
				break;
			case INTEGER:
				List<BigInteger> intList = new ArrayList<BigInteger>();

				for (String str : newList) {
					int base = 10;
					if (str.contains("0x")) {
						str = str.replaceFirst("0x", "");
						base = 16;
					}
					if (str.contains("0o")) {
						str = str.replaceFirst("0o", "");
						base = 8;
					}
					intList.add(new BigInteger(str, base));
				}
				value = gson.toJson(intList);
				break;
			case FLOAT:
				value = "[";
				for (String str : newList) {
					value += str + ",";
				}
				value = value.substring(0, value.length() - 1);
				value += "]";
				break;
			case BOOLEAN:
				List<Boolean> boolList = new ArrayList<Boolean>();
				for (String str : newList) {
					boolList.add(Boolean.valueOf(str));
				}
				value = gson.toJson(boolList);
				break;
			default:
				value = gson.toJson(newList);
				log.debug("inner Tosca Type unknown : {}", innerToscaType);
			}

		} catch (JsonParseException e) {
			log.debug("Failed to parse json : {}", value, e);
			BeEcompErrorManager.getInstance().logBeInvalidJsonInput("List Converter");
			return Either.right(false);
		}

		return Either.left(value);
	}

	private Either<String, Boolean> convertComplexInnerType(String value, String innerType,
			Map<String, DataTypeDefinition> allDataTypes) {

		DataTypeDefinition dataTypeDefinition = allDataTypes.get(innerType);
		if (dataTypeDefinition == null) {
			log.debug("Cannot find data type {}", innerType);
			return Either.right(false);
		}

		List<JsonElement> newList = new ArrayList<>();

		try {

			JsonArray jo = (JsonArray) jsonParser.parse(value);
			int size = jo.size();
			for (int i = 0; i < size; i++) {
				JsonElement currentValue = jo.get(i);

				if (currentValue != null) {

					String element = JsonUtils.toString(currentValue);

					ImmutablePair<JsonElement, Boolean> validateAndUpdate = dataTypeValidatorConverter
							.validateAndUpdate(element, dataTypeDefinition, allDataTypes);
					if (validateAndUpdate.right.booleanValue() == false) {
						log.debug("Cannot parse value {} from type {} in list position {}",currentValue,innerType,i);
						return Either.right(false);
					}
					JsonElement newValue = validateAndUpdate.left;
					newList.add(newValue);
				}
			}
		} catch (Exception e) {
			log.debug("Failed to parse the value {} of list parameter.", value);
			return Either.right(false);
		}
		value = gson.toJson(newList);
		return Either.left(value);
	}

}
