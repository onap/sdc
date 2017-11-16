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

package org.openecomp.sdc.heat.datatypes;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.ClassUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public enum DefinedHeatParameterTypes {
  NUMBER("number"),
  STRING("string"),
  COMMA_DELIMITED_LIST("comma_delimited_list"),
  JSON("json"),
  BOOLEAN("boolean");

  private static Map<String, DefinedHeatParameterTypes> stringToDefinedType = new HashMap<>();

  static {
    stringToDefinedType = new HashMap<>();
    for (DefinedHeatParameterTypes definedHeatParameterType : DefinedHeatParameterTypes.values()) {
      stringToDefinedType.put(definedHeatParameterType.type, definedHeatParameterType);
    }
  }

  private String type;

  DefinedHeatParameterTypes(String type) {
    this.type = type;
  }

  public static DefinedHeatParameterTypes findByHeatResource(String type) {
    return stringToDefinedType.get(type);
  }

  /**
   * Is value is from given type boolean.
   *
   * @param value         the value
   * @param parameterType the parameter type
   * @return the boolean
   */
  public static boolean isValueIsFromGivenType(Object value, String parameterType) {
    DefinedHeatParameterTypes definedType = findByHeatResource(parameterType);

    if (Objects.nonNull(definedType)) {
      switch (definedType) {
        case NUMBER:
          return NumberUtils.isNumber(String.valueOf(value));

        case BOOLEAN:
          return HeatBoolean.isValueBoolean(value);

        case COMMA_DELIMITED_LIST:
          return isValueCommaDelimitedList(value);

        case JSON:
          return isValueJson(value);

        case STRING:
          return isValueString(value);
        default:
      }
    }

    return false;
  }

  public static boolean isNovaServerEnvValueIsFromRightType(Object value) {
    return isValueIsFromGivenType(value, COMMA_DELIMITED_LIST.getType())
        || isValueIsFromGivenType(value, STRING.getType());
  }

  private static boolean isValueCommaDelimitedList(Object value) {
    return String.valueOf(value).contains(",")
            || isValueIsFromGivenType(value, DefinedHeatParameterTypes.STRING.type);
  }

  private static boolean isValueString(Object value) {
    return value instanceof String
            || ClassUtils.isPrimitiveOrWrapper(value.getClass());
  }

  private static boolean isValueJson(Object value) {
    return (value instanceof Map) || (value instanceof List);
  }

  public static boolean isEmptyValueInEnv(Object value) {
    return Objects.isNull(value);
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }
}
