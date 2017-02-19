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

import java.util.Arrays;
import java.util.Map;

import org.openecomp.sdc.be.model.DataTypeDefinition;

public class BooleanValidator implements PropertyTypeValidator {

	private static BooleanValidator booleanValidator = new BooleanValidator();
	private static String[] validValues = { "true", "t", "on", "yes", "y", "1", "false", "f", "off", "no", "n", "0" };

	public static BooleanValidator getInstance() {
		return booleanValidator;
	}

	private BooleanValidator() {

	}

	@Override
	public boolean isValid(String value, String innerType, Map<String, DataTypeDefinition> allDataTypes) {

		if (value == null || true == value.isEmpty()) {
			return true;
		}

		return (Arrays.stream(validValues).filter(str -> str.equalsIgnoreCase(value)).toArray().length == 1);
	}

	@Override
	public boolean isValid(String value, String innerType) {
		return isValid(value, null, null);
	}
}
