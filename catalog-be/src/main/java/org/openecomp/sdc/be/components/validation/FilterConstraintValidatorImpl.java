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

package org.openecomp.sdc.be.components.validation;

import org.apache.commons.lang3.StringUtils;
import org.openecomp.sdc.be.components.impl.exceptions.ByActionStatusComponentException;
import org.openecomp.sdc.be.components.impl.exceptions.FilterConstraintExceptionSupplier;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.model.dto.FilterConstraintDto;
import org.openecomp.sdc.be.model.validation.FilterConstraintValidator;

@org.springframework.stereotype.Component
public class FilterConstraintValidatorImpl implements FilterConstraintValidator {

    @Override
    public void validate(final FilterConstraintDto filterConstraint) {
        if (filterConstraint == null) {
            throw FilterConstraintExceptionSupplier.filterConstraintNotProvided().get();
        }

        if (StringUtils.isBlank(filterConstraint.getPropertyName()) && StringUtils.isBlank(filterConstraint.getCapabilityName())) {
            throw FilterConstraintExceptionSupplier.missingField("propertyName or capabilityName").get();
        }

        if (filterConstraint.getValueType() == null) {
            throw FilterConstraintExceptionSupplier.missingField("valueType").get();
        }

        if (filterConstraint.getTargetType() == null) {
            throw FilterConstraintExceptionSupplier.missingField("targetType").get();
        }

        if (filterConstraint.getValue() == null) {
            throw FilterConstraintExceptionSupplier.missingField("value").get();
        }

        if (filterConstraint.getOperator() == null) {
            throw FilterConstraintExceptionSupplier.missingField("operator").get();
        }
    }
}
