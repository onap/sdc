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

import java.util.regex.Pattern;

/**
 * This Class is responsible for validating if a given value is a valid Tosca Scalar Unit Type.
 */
public class ScalarUnitValidator {

    private static ScalarUnitValidator scalarUnitValidator = new ScalarUnitValidator();

    private ScalarUnitValidator() {
    }

    public static ScalarUnitValidator getInstance() {
        return scalarUnitValidator;
    }

    /**
     *  Tosca Scalar Unit Types structure.
     */
    private final Pattern pattern = Pattern.compile("\\d+\\ [a-zA-Z]{1,3}");

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
}
