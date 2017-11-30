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

import java.io.StringReader;
import java.util.Map;

import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;

public class JsonValidator implements PropertyTypeValidator {

	private static JsonValidator jsonValidator = new JsonValidator();

	private static Logger log = LoggerFactory.getLogger(JsonValidator.class.getName());

	private static JsonParser jsonParser = new JsonParser();

	public static JsonValidator getInstance() {
		return jsonValidator;
	}

	@Override
	public boolean isValid(String value, String innerType, Map<String, DataTypeDefinition> allDataTypes) {

		if (value == null || value.isEmpty()) {
			return true;
		}
		try {
			StringReader reader = new StringReader(value);
			JsonReader jsonReader = new JsonReader(reader);
			jsonReader.setLenient(true);
			jsonParser.parse(jsonReader);
		} catch (JsonSyntaxException e) {
			log.debug("Error parsing JSON property", e);
			return false;
		}
		return true;

	}

	@Override
	public boolean isValid(String value, String innerType) {
		return isValid(value, innerType, null);
	}
}
