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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openecomp.sdc.be.model.User;

@ExtendWith(MockitoExtension.class)
class DataValidatorTest {

    private static final ValidatorFactory VALIDATOR_FACTORY = Validation.buildDefaultValidatorFactory();
    @InjectMocks
    private DataValidator dataValidator;

    @Test
    void getConstraintViolationsSecureString() {
        SecureString secureString = new SecureString("<script>alert(“XSS”);</script>");
        Validator validator = VALIDATOR_FACTORY.getValidator();
        Set<ConstraintViolation<SecureString>> expectedConstraintViolations = validator.validate(secureString);
        Set<ConstraintViolation<SecureString>> actualConstraintViolations = dataValidator.getConstraintViolations(secureString);
        assertEquals(expectedConstraintViolations, actualConstraintViolations);
    }

    @Test
    void isValidSecureString() {
        SecureString secureString = new SecureString("<script>alert(“XSS”);</script>");
        assertFalse(dataValidator.isValid(secureString));
    }

    @Test
    void getConstraintViolationsEPUser() {
        User user = new User();
        user.setEmail("“><script>alert(“XSS”)</script>");
        user.setUserId("<IMG SRC=”javascript:alert(‘XSS’);”>");
        user.setFirstName("<IMG SRC=javascript:alert(‘XSS’)> ");
        Validator validator = VALIDATOR_FACTORY.getValidator();
        Set<ConstraintViolation<User>> expectedConstraintViolations = validator.validate(user);
        Set<ConstraintViolation<User>> actualConstraintViolations = dataValidator.getConstraintViolations(user);
        assertEquals(expectedConstraintViolations, actualConstraintViolations);
    }

    @Test
    void isValidEPUser() {
        User user = new User();
        user.setEmail("“><script>alert(“XSS”)</script>");
        user.setUserId("<IMG SRC=”javascript:alert(‘XSS’);”>");
        user.setFirstName("<IMG SRC=javascript:alert(‘XSS’)> ");
        assertFalse(dataValidator.isValid(user));
    }

}
