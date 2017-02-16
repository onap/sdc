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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.tosca.ToscaPropertyType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class ToscaValueBaseConverter {
	protected Gson gson = new Gson();
	private static Logger log = LoggerFactory.getLogger(ToscaValueBaseConverter.class.getName());

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

	public Object handleComplexJsonValue(JsonElement elementValue) {
		Object jsonValue = null;

		Map<String, Object> value = new HashMap<String, Object>();
		if ( elementValue.isJsonObject() ){
			JsonObject jsonOb = elementValue.getAsJsonObject();
			Set<Entry<String, JsonElement>> entrySet = jsonOb.entrySet();
			Iterator<Entry<String, JsonElement>> iteratorEntry = entrySet.iterator();
			while (iteratorEntry.hasNext()) {
				Entry<String, JsonElement> entry = iteratorEntry.next();
				if (entry.getValue().isJsonArray()) {
					List<Object> array = handleJsonArray(entry.getValue());
					value.put(entry.getKey(), array);
				} else {
					Object object;
					if (entry.getValue().isJsonPrimitive()) {
						object = json2JavaPrimitive(entry.getValue().getAsJsonPrimitive());
					} else {
						object = handleComplexJsonValue(entry.getValue());
					}
					value.put(entry.getKey(), object);
				}
			}
			jsonValue = value;
		}else{
			if ( elementValue.isJsonArray() ){
				jsonValue = handleJsonArray(elementValue);
			}else{
				log.debug("not supported json type {} ",elementValue);
			}
		}
		
		return jsonValue;
	}

	private List<Object> handleJsonArray(JsonElement entry) {
		List<Object> array = new ArrayList<>();
		JsonArray jsonArray = entry.getAsJsonArray();
		Iterator<JsonElement> iterator = jsonArray.iterator();
		while (iterator.hasNext()) {
			Object object;
			JsonElement element = iterator.next();
			if (element.isJsonPrimitive()) {
				object = json2JavaPrimitive(element.getAsJsonPrimitive());
			} else {
				object = handleComplexJsonValue(element);
			}
			array.add(object);
		}
		return array;
	}

	public Object json2JavaPrimitive(JsonPrimitive prim) {
		if (prim.isBoolean()) {
			return prim.getAsBoolean();
		} else if (prim.isString()) {
			return prim.getAsString();
		} else if (prim.isNumber()) {
			String strRepesentation = prim.getAsString();
			if (strRepesentation.contains(".")) {
				return prim.getAsDouble();
			} else {
				return prim.getAsInt();
			}
		} else {
			throw new IllegalStateException();
		}
	}
}
