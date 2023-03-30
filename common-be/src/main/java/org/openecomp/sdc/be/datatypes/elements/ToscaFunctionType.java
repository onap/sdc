/*
 * -
 *  ============LICENSE_START=======================================================
 *  Copyright (C) 2022 Nordix Foundation.
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================
 */

package org.openecomp.sdc.be.datatypes.elements;

import java.util.Arrays;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

@AllArgsConstructor
@Getter
public enum ToscaFunctionType {

    GET_INPUT("get_input"),
    GET_PROPERTY("get_property"),
    GET_ATTRIBUTE("get_attribute"),
    CONCAT("concat"),
    YAML("yaml"),
    STRING("string");

    private final String name;

    public static Optional<ToscaFunctionType> findType(final String functionType) {
        if (StringUtils.isBlank(functionType)) {
            return Optional.empty();
        }
        return Arrays.stream(values()).filter(toscaFunctionType -> toscaFunctionType.getName().equalsIgnoreCase(functionType)).findFirst();
    }

    public static boolean isGetFunction(final ToscaFunctionType functionType) {
        return GET_INPUT.equals(functionType) || GET_PROPERTY.equals(functionType) || GET_ATTRIBUTE.equals(functionType);
    }

}
