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

import java.util.Arrays;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Represents a valid property for a PNFD transformation block.
 */
@Getter
@AllArgsConstructor
public enum TransformationPropertyType {
    NODE_NAME_PREFIX("nodeNamePrefix");

    private final String type;

    public static Optional<TransformationPropertyType> parse(final String type) {
        return Arrays.stream(values())
            .filter(transformationPropertyType -> transformationPropertyType.getType().equals(type))
            .findFirst();
    }
}
