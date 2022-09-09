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

package org.openecomp.sdc.be.ui.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.openecomp.sdc.be.datatypes.elements.ToscaGetFunctionDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.FilterValueType;
import org.openecomp.sdc.be.ui.model.UIConstraint;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UIConstraintMapper {

    public static UIConstraint mapToLegacyConstraint(final UIConstraint uiConstraint) {
        final UIConstraint uiConstraint1 = new UIConstraint();
        uiConstraint1.setCapabilityName(uiConstraint.getCapabilityName());
        uiConstraint1.setServicePropertyName(uiConstraint.getServicePropertyName());
        uiConstraint1.setConstraintOperator(uiConstraint.getConstraintOperator());
        uiConstraint1.setSourceType(FilterValueType.STATIC.getName());
        uiConstraint1.setSourceName(FilterValueType.STATIC.getName());
        if (uiConstraint.getValue() instanceof ToscaGetFunctionDataDefinition) {
            uiConstraint1.setValue(((ToscaGetFunctionDataDefinition) uiConstraint.getValue()).getValue());
        } else {
            uiConstraint1.setValue(uiConstraint.getValue());
        }

        return uiConstraint1;
    }

}
