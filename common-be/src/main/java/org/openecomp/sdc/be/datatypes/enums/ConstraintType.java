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
package org.openecomp.sdc.be.datatypes.enums;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.Getter;

@Getter
public enum ConstraintType {
    EQUAL("equal"),
    IN_RANGE("in_range", "inRange"),
    GREATER_THAN("greater_than", "greaterThan"),
    GREATER_OR_EQUAL("greater_or_equal", "greaterOrEqual"),
    LESS_OR_EQUAL("less_or_equal", "lessOrEqual"),
    LENGTH("length"),
    MIN_LENGTH("min_length", "minLength"),
    MAX_LENGTH("max_length", "maxLength"),
    VALID_VALUES("valid_values", "validValues"),
    LESS_THAN("less_than", "lessThan"),
    SCHEMA("schema");

    private static final Set<ConstraintType> comparableConstraints = Set.of(ConstraintType.GREATER_THAN, ConstraintType.LESS_THAN);
    private final String type;
    private final List<String> typeAlias;


    ConstraintType(final String type, final String... typeAliases) {
        this.type = type;
        if (typeAliases == null) {
            this.typeAlias = Collections.emptyList();
        } else {
            this.typeAlias = Arrays.asList(typeAliases);
        }
    }

    public static Optional<ConstraintType> findByType(final String type) {
        if (type == null) {
            return Optional.empty();
        }
        return Arrays.stream(ConstraintType.values())
            .filter(constraintType -> constraintType.getType().equals(type) || constraintType.getTypeAlias().contains(type))
            .findFirst();
    }

    public boolean isComparable() {
        return comparableConstraints.contains(this);
    }

}
