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

package org.openecomp.sdc.be.model.tosca.validators;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class IntegerValidatorTest {

    private static final IntegerValidator validator = IntegerValidator.getInstance();

    @Test
    void testIntegerValidatorDecimal() {
        assertTrue(validator.isValid(null, null));
        assertTrue(validator.isValid("", null));
        assertTrue(validator.isValid("0", null));
        assertTrue(validator.isValid("+0", null));
        assertTrue(validator.isValid("-0", null));
        assertTrue(validator.isValid("+65465", null));
        assertTrue(validator.isValid("-65465", null));
        assertTrue(validator.isValid("9223372036854775807", null));
        assertTrue(validator.isValid("92233720368547758079223372036854775807", null));
        assertTrue(validator.isValid("-9223372036854775808", null));
        assertTrue(validator.isValid("-92233720368547758089223372036854775808", null));
    }

    @Test
    void testIntegerValidatorHexa() {
        assertTrue(validator.isValid("-0xadc", null));
        assertTrue(validator.isValid("+0xadf", null));
        assertTrue(validator.isValid("0x7FFFFFFFFFFFFFFF", null));
        assertTrue(validator.isValid("0x7FFFFFFFFFFFFFFFFFFFFFFFFFFFFFF", null));
        assertTrue(validator.isValid("-0x8000000000000000", null));
        assertTrue(validator.isValid("-0x8000000000000000000000000000000", null));
    }

    @Test
    void testIntegerValidatorOctal() {
        assertTrue(validator.isValid("+0o545435", null));
        assertTrue(validator.isValid("-0o545435", null));
        assertTrue(validator.isValid("0o777777777777777777777", null));
        assertTrue(validator.isValid("0o777777777777777777777777777777777777777777", null));
        assertTrue(validator.isValid("-0o1000000000000000000000", null));
        assertTrue(validator.isValid("-0o1000000000000000000000000000000000000000000", null));
    }

    @Test
    void testIntegerValidatorIncorrect() {
        assertFalse(validator.isValid("-2.147483649", null));
        assertFalse(validator.isValid("dsfasf342342", null));
    }
}
