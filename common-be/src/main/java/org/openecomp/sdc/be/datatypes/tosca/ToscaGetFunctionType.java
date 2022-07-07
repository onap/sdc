/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2021 Nordix Foundation
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

package org.openecomp.sdc.be.datatypes.tosca;

import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.openecomp.sdc.be.datatypes.elements.ToscaFunctionType;

@AllArgsConstructor
@Getter
public enum ToscaGetFunctionType {
    GET_INPUT("get_input", "input"),
    GET_PROPERTY("get_property", "property"),
    GET_ATTRIBUTE("get_attribute", "attribute");

    private final String functionName;
    private final String propertyType;

    /**
     * Converts a {@link ToscaFunctionType} to a {@link ToscaGetFunctionType}
     * @param toscaFunctionType the tosca function type to convert
     * @return the respective {@link ToscaGetFunctionType}
     */
    public static Optional<ToscaGetFunctionType> fromToscaFunctionType(final ToscaFunctionType toscaFunctionType) {
        switch (toscaFunctionType) {
            case GET_INPUT:
                return Optional.of(GET_INPUT);
            case GET_PROPERTY:
                return Optional.of(GET_PROPERTY);
            case GET_ATTRIBUTE:
                return Optional.of(GET_ATTRIBUTE);
            default:
                return Optional.empty();
        }
    }

}
