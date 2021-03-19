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

public class ToscaBooleanValidator implements PropertyTypeValidator {

    private static ToscaBooleanValidator booleanValidator = new ToscaBooleanValidator();
    private static String[] validValues = {"true", "on", "yes", "y", "false", "off", "no", "n"};

    private ToscaBooleanValidator() {
    }

    public static ToscaBooleanValidator getInstance() {
        return booleanValidator;
    }

    @Override
    public boolean isValid(String value, String innerType, Map<String, DataTypeDefinition> dataTypes) {
        if (value == null || value.isEmpty()) {
            return true;
        }
        return (Arrays.stream(validValues).filter(str -> str.equalsIgnoreCase(value)).toArray().length == 1);
    }

    @Override
    public boolean isValid(String value, String innerType) {
        return isValid(value, innerType, null);
    }
}
