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
package org.openecomp.sdc.be.tosca.model;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.openecomp.sdc.be.datatypes.enums.ConstraintType;

/**
 * Represents a tosca valid_values constraint
 */
@Getter
@Setter
@AllArgsConstructor
public class ToscaPropertyConstraintValidValues implements ToscaPropertyConstraint {

    private List<String> validValues;

    /**
     * Get the TOSCA entry name of an attribute in this class.
     *
     * @param attributeName the class attribute name
     * @return the TOSCA entry represented by the attribute
     */
    public static String getEntryToscaName(final String attributeName) {
        if ("validValues".equals(attributeName)) {
            return "valid_values";
        }
        return attributeName;
    }

    @Override
    public ConstraintType getConstraintType() {
        return ConstraintType.VALID_VALUES;
    }
}
