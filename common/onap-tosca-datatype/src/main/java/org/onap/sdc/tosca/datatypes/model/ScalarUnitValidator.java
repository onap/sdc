/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================
 */

package org.onap.sdc.tosca.datatypes.model;

import java.util.Arrays;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;

/**
 * This Class is responsible for validating if a given value is a valid Tosca Scalar Unit Type.
 */
public class ScalarUnitValidator {

    private static ScalarUnitValidator scalarUnitValidator = new ScalarUnitValidator();

    /**
     *  Tosca Scalar Unit Types structure.
     */
    private final Pattern pattern = Pattern.compile("\\d+\\s*[a-zA-Z]{1,3}");

    private ScalarUnitValidator() {
    }

    public static ScalarUnitValidator getInstance() {
        return scalarUnitValidator;
    }

    /**
     * Validates if the given String matches with the Tosca Scalar Unit Types structure.
     *
     * @param value String to be validated.
     * @return an {@Boolean}
     */
    public boolean isScalarUnit(final String value) {
        if (value == null || value.isEmpty()) {
            return true;
        }

        return pattern.matcher(value).matches();
    }

    /**
     * Validates if the given String has a Recognized Tosca unit.
     *
     * @param value String to be validated
     * @param enumClass Enum that represents a Tosca Scalar Unit Type.
     * @param <E>
     * @return an Enum that represents the Tosca Scalar Unit Type.
     */
    public <E extends Enum<E>> boolean isValueScalarUnit(final Object value, final Class<E> enumClass) {
        final String stringToValidate = String.valueOf(value);
        return isScalarUnit(stringToValidate) && Arrays.stream(StringUtils.split(stringToValidate))
            .anyMatch(strValue -> Arrays.stream(enumClass.getEnumConstants())
                .anyMatch(scalarUnit ->
                    scalarUnit.name().equalsIgnoreCase(strValue) || strValue.endsWith(scalarUnit.name())));
    }
}
