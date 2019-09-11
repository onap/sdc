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

package org.openecomp.core.converter.pnfd.model;

import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Represents a type of a transformation block
 */
@AllArgsConstructor
@Getter
public enum TransformationBlock {
    INPUT("input"),
    GET_INPUT_FUNCTION("getInputFunction"),
    NODE_TEMPLATE("nodeTemplate"),
    CUSTOM_NODE_TYPE("customNodeType"),
    NODE_TYPE("nodeType");

    private final String name;

    /**
     * Parse a String to the related {@link TransformationBlock}.
     * @param name  the {@link TransformationBlock} name
     * @return
     *  The {@link TransformationBlock} representing the given name.
     */
    public static Optional<TransformationBlock> parse(final String name) {
        for (final TransformationBlock transformationBlock : values()) {
            if (transformationBlock.getName().equals(name)) {
                return Optional.of(transformationBlock);
            }
        }

        return Optional.empty();
    }
}
