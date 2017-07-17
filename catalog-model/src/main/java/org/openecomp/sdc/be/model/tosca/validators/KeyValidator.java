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

import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.common.util.ValidationUtils;

public class KeyValidator implements PropertyTypeValidator {

	public static final int STRING_MAXIMUM_LENGTH = 100;

	private static KeyValidator keyValidator = new KeyValidator();

	public static KeyValidator getInstance() {
		return keyValidator;
	}

	private KeyValidator() {

	}

	@Override
	public boolean isValid(String value, String innerType, Map<String, DataTypeDefinition> allDataTypes) {

		if (value == null || true == value.isEmpty()) {
			return false;
		}

		if (value.length() > STRING_MAXIMUM_LENGTH) {
			return false;
		}
		String converted = ValidationUtils.removeNoneUtf8Chars(value);
		return ValidationUtils.validateIsEnglish(converted);
	}

	@Override
	public boolean isValid(String value, String innerType) {
		return isValid(value, innerType, null);
	}

}
