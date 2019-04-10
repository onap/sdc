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

package org.openecomp.sdc.be.model.tosca.constraints;

import java.util.Arrays;
import java.util.List;

public enum ConstraintType {

	EQUAL("equal", "equal"),

	IN_RANGE("inRange","in_range"),

    GREATER_THAN("greaterThan", "greater_than"),

    GREATER_OR_EQUAL("greaterOrEqual", "greater_or_equal"),

    LESS_OR_EQUAL("lessOrEqual", "less_or_equal"),

    LENGTH("length", "length"),

    MIN_LENGTH("minLength", "min_length"),

    MAX_LENGTH("maxLength", "max_length"),

    VALID_VALUES("validValues", "valid_values"),

    LESS_THAN("lessThan", "less_than"),

    SCHEMA("schema", "schema");

    List<String> types;

    private ConstraintType(String... types) {
        this.types = Arrays.asList(types);
    }

    public List<String> getTypes() {
        return types;
    }

    public static ConstraintType getByType(String type) {
        for (ConstraintType inst : ConstraintType.values()) {
            if (inst.getTypes().contains(type)) {
                return inst;
            }
        }
        return null;
    }
}
