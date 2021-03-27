/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2020 Nordix Foundation
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
package org.openecomp.sdc.be.datatypes.enums;

import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Represents the type of a node filter constraint.
 */
@Getter
@AllArgsConstructor
public enum NodeFilterConstraintType {
    PROPERTIES(NodeFilterConstraintType.PROPERTIES_PARAM_NAME), CAPABILITIES(NodeFilterConstraintType.CAPABILITIES_PARAM_NAME);
    // Those values are needed as constants for Swagger allowedValues param
    public static final String PROPERTIES_PARAM_NAME = "properties";
    public static final String CAPABILITIES_PARAM_NAME = "capabilities";
    private final String type;

    /**
     * Parse a String to the related {@link NodeFilterConstraintType}.
     *
     * @param type the {@link NodeFilterConstraintType} type
     * @return The {@link NodeFilterConstraintType} representing the given type.
     */
    public static Optional<NodeFilterConstraintType> parse(final String type) {
        for (final NodeFilterConstraintType nodeFilterConstraintType : values()) {
            if (nodeFilterConstraintType.getType().equals(type)) {
                return Optional.of(nodeFilterConstraintType);
            }
        }
        return Optional.empty();
    }
}
