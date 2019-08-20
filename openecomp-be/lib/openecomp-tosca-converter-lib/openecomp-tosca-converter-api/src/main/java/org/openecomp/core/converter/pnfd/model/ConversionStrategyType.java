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
 * Represents a type of a conversion strategy
 */
public enum ConversionStrategyType {
    COPY("copy"),
    REPLACE("replace"),
    REPLACE_IN_LIST("replaceInList");

    private final String type;

    ConversionStrategyType(final String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    /**
     * Parse a String to the related {@link ConversionStrategyType}.
     * @param type  the {@link ConversionStrategyType} type
     * @return
     *  The {@link ConversionStrategyType} representing the given type.
     */
    public static Optional<ConversionStrategyType> parse(final String type) {
        for (final ConversionStrategyType conversionStrategyType : values()) {
            if (conversionStrategyType.getType().equals(type)) {
                return Optional.of(conversionStrategyType);
            }
        }

        return Optional.empty();
    }
}
