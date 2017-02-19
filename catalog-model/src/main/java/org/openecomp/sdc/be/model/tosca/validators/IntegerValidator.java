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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openecomp.sdc.be.model.DataTypeDefinition;

public class IntegerValidator implements PropertyTypeValidator {

	private static IntegerValidator integerValidator = new IntegerValidator();

	private IntegerValidator() {
	}

	public static IntegerValidator getInstance() {
		return integerValidator;
	}

	private class PatternBase {
		public PatternBase(Pattern pattern, Integer base) {
			this.pattern = pattern;
			this.base = base;
		}

		Pattern pattern;
		Integer base;
	}

	private PatternBase base8Pattern = new PatternBase(Pattern.compile("([-+])?0o([0-7]+)"), 8);
	private PatternBase base10Pattern = new PatternBase(Pattern.compile("([-+])?(0|[1-9][0-9]*)"), 10);
	private PatternBase base16Pattern = new PatternBase(Pattern.compile("([-+])?0x([0-9a-fA-F]+)"), 16);

	private PatternBase[] patterns = { base10Pattern, base8Pattern, base16Pattern };

	@Override
	public boolean isValid(String value, String innerType, Map<String, DataTypeDefinition> allDataTypes) {

		if (value == null || true == value.isEmpty()) {
			return true;
		}

		for (PatternBase patternBase : patterns) {
			Matcher matcher = patternBase.pattern.matcher(value);
			Long parsed = null;
			if (matcher.matches()) {
				try {
					parsed = Long.parseLong(matcher.group(2), patternBase.base);
					if (matcher.group(1) != null && matcher.group(1).compareTo("-") == 0) {
						parsed *= -1;
					}
					return (Integer.MIN_VALUE <= parsed && parsed <= (Integer.MAX_VALUE)) ? true : false;
				} catch (NumberFormatException e) {
					return false;
				}
			}
		}
		return false;
	}

	@Override
	public boolean isValid(String value, String innerType) {
		return isValid(value, innerType, null);
	}
}
