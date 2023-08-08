/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2022 Nordix Foundation
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
package org.openecomp.sdc.be.tosca.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.LinkedList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.openecomp.sdc.be.datatypes.enums.ConstraintType;

/**
 * Represents a TOSCA in_range constraint
 */
@Getter
@Setter
@AllArgsConstructor
public class ToscaPropertyConstraintInRange implements ToscaPropertyConstraint {

    private List<Object> inRange;
    private static final ConstraintType CONSTRAINT_TYPE = ConstraintType.IN_RANGE;

    @Override
    public String getEntryToscaName(final String attributeName) {
        if ("inRange".equals(attributeName)) {
            return CONSTRAINT_TYPE.getType();
        }
        return attributeName;
    }

    @Override
    @JsonIgnore
    public ConstraintType getConstraintType() {
        return CONSTRAINT_TYPE;
    }
}
