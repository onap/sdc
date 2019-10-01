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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.ClassUtils;
import org.onap.sdc.tosca.datatypes.model.ScalarUnitValidator;

@AllArgsConstructor
@Getter
public enum DefinedHeatParameterTypes {
    NUMBER("number"),
    STRING("string"),
    COMMA_DELIMITED_LIST("comma_delimited_list"),
    JSON("json"),
    BOOLEAN("boolean");

    private static Map<String, DefinedHeatParameterTypes> stringToDefinedType;

    static {
        stringToDefinedType = new HashMap<>();
        for (final DefinedHeatParameterTypes definedHeatParameterType : DefinedHeatParameterTypes.values()) {
            stringToDefinedType.put(definedHeatParameterType.type, definedHeatParameterType);
        }
    }

    private String type;

    public static DefinedHeatParameterTypes findByHeatResource(final String type) {
        return stringToDefinedType.get(type);
    }

    /**
     * Is value is from given type boolean.
     *
     * @param value         the value
     * @param parameterType the parameter type
     * @return the boolean
     */
    public static boolean isValueIsFromGivenType(final Object value, final String parameterType) {
        final DefinedHeatParameterTypes definedType = findByHeatResource(parameterType);

        if (Objects.nonNull(definedType)) {
            switch (definedType) {
                case NUMBER:
                    if (isValueScalarUnit(value, ToscaScalarUnitSize.class) ||
                        isValueScalarUnit(value, ToscaScalarUnitTime.class) ||
                        isValueScalarUnit(value, ToscaScalarUnitFrequency.class)) {
                        return isValueString(value);
                    }
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

    public static boolean isNovaServerEnvValueIsFromRightType(final Object value) {
        return isValueIsFromGivenType(value, COMMA_DELIMITED_LIST.getType())
                || isValueIsFromGivenType(value, STRING.getType());
    }

    private static boolean isValueCommaDelimitedList(final Object value) {
        return value instanceof List
                || String.valueOf(value).contains(",")
                || isValueIsFromGivenType(value, DefinedHeatParameterTypes.STRING.type);
    }

    private static boolean isValueString(final Object value) {
        return value instanceof String
                || ClassUtils.isPrimitiveOrWrapper(value.getClass());
    }

    private static boolean isValueJson(final Object value) {
        return (value instanceof Map) || (value instanceof List);
    }

    public static boolean isEmptyValueInEnv(final Object value) {
        return Objects.isNull(value);
    }

    public static <E extends Enum<E>> boolean isValueScalarUnit(final Object value, final Class<E> enumClass) {
        final ScalarUnitValidator scalarUnitValidator = ScalarUnitValidator.getInstance();
        final String stringToValidate = String.valueOf(value);
        return scalarUnitValidator.isScalarUnit(stringToValidate) && Arrays.stream(StringUtils.split(stringToValidate))
            .anyMatch(strValue -> Arrays.stream(enumClass.getEnumConstants())
                .anyMatch(scalarUnit -> scalarUnit.name().equalsIgnoreCase(strValue)));
    }

}
