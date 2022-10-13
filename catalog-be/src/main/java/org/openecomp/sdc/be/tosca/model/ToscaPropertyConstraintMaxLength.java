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

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.openecomp.sdc.be.datatypes.enums.ConstraintType;

/**
 * Represents a TOSCA max_length constraint
 */
@Getter
@Setter
@AllArgsConstructor
public class ToscaPropertyConstraintMaxLength implements ToscaPropertyConstraint {

    private Integer maxLength;
    private static final ConstraintType CONSTRAINT_TYPE = ConstraintType.MAX_LENGTH;

    @Override
    public String getEntryToscaName(final String attributeName) {
        if ("maxLength".equals(attributeName)) {
            return CONSTRAINT_TYPE.getType();
        }
        return attributeName;
    }

    @Override
    public ConstraintType getConstraintType() {
        return CONSTRAINT_TYPE;
    }
}
