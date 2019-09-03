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

/**
 * Represents a type of a transformation block
 */
public enum TransformationBlock {
    INPUT("input"),
    NODE_TEMPLATE("nodeTemplate"),
    GET_INPUT_FUNCTION("getInputFunction");

    private final String name;

    TransformationBlock(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

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
