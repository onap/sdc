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

package org.openecomp.sdc.be.model.tosca;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Locale;

/**
 * The primitive type that TOSCA YAML supports.
 * 
 * @author mkv
 */
public enum ToscaType {
	STRING, INTEGER, FLOAT, BOOLEAN, TIMESTAMP, VERSION;

	// private static final Pattern TIMESTAMP_REGEX = Pattern
	// .compile("[0-9][0-9][0-9][0-9]-[0-9][0-9]-[0-9][0-9]|[0-9][0-9][0-9][0-9]-[0-9][0-9]?-[0-9][0-9]?([Tt]|[
	// \\t]+)[0-9][0-9]?:[0-9][0-9]:[0-9][0-9](\\.[0-9]*)?(([ \\t]*)Z|([
	// \\t]*)[-+][0-9][0-9]?(:[0-9][0-9])?)?");

	public static ToscaType fromYamlTypeName(String typeName) {
		if (typeName == null) {
			return null;
		}
		try {
			return ToscaType.valueOf(typeName.toUpperCase());
		} catch (IllegalArgumentException e) {
			return null;
		}
	}

	public boolean isValidValue(String value) {
		switch (this) {
		case BOOLEAN:
			return value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false");
		case FLOAT:
			return isFloat(value);
		case INTEGER:
			return isInteger(value);
		case STRING:
			return true;
		case TIMESTAMP:
			try {
				DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, Locale.US).parse(value);
				return true;
			} catch (ParseException e) {
				return false;
			}
		case VERSION:
			return VersionUtil.isValid(value);
		default:
			return false;
		}
	}

	private boolean isFloat(String value) {
		try {
			Float.valueOf(value);
		} catch (NumberFormatException e) {
			return false;
		}
		return true;
	}

	private boolean isInteger(String value) {
		try {
			Long.valueOf(value);
		} catch (NumberFormatException e) {
			return false;
		}
		return true;
	}

	public Object convert(String value) {
		switch (this) {
		case STRING:
			return value;
		case BOOLEAN:
			return Boolean.valueOf(value);
		case FLOAT:
			return Double.valueOf(value);
		case INTEGER:
			return Long.valueOf(value);
		case TIMESTAMP:
			try {
				return DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, Locale.US).parse(value);
			} catch (ParseException e) {
				throw new IllegalArgumentException("Value must be a valid timestamp", e);
			}
		case VERSION:
			return VersionUtil.parseVersion(value);
		default:
			return null;
		}
	}

	@Override
	public String toString() {
		return name().toLowerCase();
	}
}
