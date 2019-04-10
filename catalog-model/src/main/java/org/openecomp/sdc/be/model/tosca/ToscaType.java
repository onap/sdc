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

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.openecomp.sdc.be.model.tosca.constraints.ConstraintUtil;
import org.openecomp.sdc.be.model.tosca.constraints.exception.ConstraintValueDoNotMatchPropertyTypeException;

/**
 * The primitive type that TOSCA YAML supports.
 * 
 * @author mkv
 */
public enum ToscaType {
	STRING("string"),
	INTEGER("integer"),
	FLOAT("float"),
	BOOLEAN("boolean"),
	TIMESTAMP("timestamp"),
	VERSION("version"),
	LIST("list"),
	MAP("map"),
	SCALAR_UNIT("scalar-unit"),
	SCALAR_UNIT_SIZE("scalar-unit.size"),
	SCALAR_UNIT_TIME("scalar-unit.time"),
	SCALAR_UNIT_FREQUENCY("scalar-unit.frequency");

	private String type;

	ToscaType(String type) {
		this.type = type;
	}

	public String getType() {
		return type;
	}

	public ToscaType getToscaType(String typeName) {
		if (typeName == null) {
			return null;
		}

		for (ToscaType type : ToscaType.values()) {
			if (type.getType().equals(typeName)) {
				return type;
			}
		}
		return null;
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
			case SCALAR_UNIT:
			case SCALAR_UNIT_SIZE:
			case SCALAR_UNIT_TIME:
			case SCALAR_UNIT_FREQUENCY:
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
			case LIST:
				return isList(value);
			case MAP:
				return isMap(value);
			default:
				return false;
		}
	}

	private boolean isList(String value) {
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			objectMapper.readValue(value,
					new TypeReference<List<Object>>() {
					});

		} catch (IOException e) {
			return false;
		}

		return true;
	}

	private boolean isMap(String value) {
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			objectMapper.readValue(value,
					new TypeReference<Map<String, Object>>() {
					});

		} catch (IOException e) {
			return false;
		}

		return true;
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
			case SCALAR_UNIT:
			case SCALAR_UNIT_SIZE:
			case SCALAR_UNIT_TIME:
			case SCALAR_UNIT_FREQUENCY:
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
			case LIST:
				try {
					return ConstraintUtil.parseToCollection(value, new TypeReference<List<Object>>() {});
				} catch (ConstraintValueDoNotMatchPropertyTypeException e) {
					throw new IllegalArgumentException("Value must be a valid timestamp", e);
				}
			case MAP:
				try {
					return ConstraintUtil.parseToCollection(value, new TypeReference<Map<String, Object>>() {});
				} catch (ConstraintValueDoNotMatchPropertyTypeException e) {
					throw new IllegalArgumentException("Value must be a valid timestamp", e);
				}
			default:
				return null;
		}
	}

	public static boolean isPrimitiveType(String dataTypeName) {

		if (!ToscaPropertyType.MAP.getType().equals(dataTypeName) && !ToscaPropertyType.LIST.getType()
				.equals(dataTypeName)) {

			return isValidType(dataTypeName) != null;
		}

		return false;
	}

	public static ToscaType isValidType(String typeName) {
		if (typeName == null) {
			return null;
		}

		for (ToscaType type : ToscaType.values()) {
			if (type.getType().equals(typeName)) {
				return type;
			}
		}
		return null;
	}

	@Override
    public String toString() {
        return name().toLowerCase();
    }

	public static boolean isCollectionType(String type) {
		return ToscaPropertyType.MAP.getType().equals(type)
				|| ToscaPropertyType.LIST.getType().equals(type);
	}
}
