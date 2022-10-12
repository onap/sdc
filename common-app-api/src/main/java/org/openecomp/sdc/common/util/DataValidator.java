/*
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2022 Nordix Foundation. All rights reserved.
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

package org.openecomp.sdc.common.util;

import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

/**
 * Provides mechanism to validate input for Constraint Violations. For more info see {@link ValidatorFactory}, {@link ConstraintViolation},
 * {@link Validator}.
 */
public class DataValidator {

    private final ValidatorFactory validatorFactory;

    public DataValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
    }

    private <E> Set<ConstraintViolation<E>> getConstraintViolations(E classToValid) {
        final Validator validator = validatorFactory.getValidator();
        return validator.validate(classToValid);
    }

    /**
     * Validates input for Constraint Violations
     *
     * @param classToValid - class to validate
     * @param <E>
     * @return true if input is valid, false if not
     */
    public <E> boolean isValid(E classToValid) {
        return getConstraintViolations(classToValid).isEmpty();
    }

}
