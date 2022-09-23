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

package org.openecomp.sdc.be.servlets.utils;

import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import org.springframework.stereotype.Component;

@Component
public class DataValidator {

    private static volatile ValidatorFactory VALIDATOR_FACTORY;

    public DataValidator() {
        if (VALIDATOR_FACTORY == null) {
            synchronized (DataValidator.class) {
                if (VALIDATOR_FACTORY == null) {
                    VALIDATOR_FACTORY = Validation.buildDefaultValidatorFactory();
                }
            }
        }
    }

    public <E> Set<ConstraintViolation<E>> getConstraintViolations(E classToValid) {
        Validator validator = VALIDATOR_FACTORY.getValidator();
        return validator.validate(classToValid);
    }

    public <E> boolean isValid(E classToValid) {
        return getConstraintViolations(classToValid).isEmpty();
    }

}
