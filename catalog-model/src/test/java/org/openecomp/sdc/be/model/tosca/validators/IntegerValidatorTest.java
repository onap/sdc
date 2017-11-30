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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class IntegerValidatorTest {
	private static IntegerValidator validator = IntegerValidator.getInstance();

	@Test
	public void testIntegerValidatorDecimal() {
		assertTrue(validator.isValid(null, null));
		assertTrue(validator.isValid("", null));
		assertTrue(validator.isValid("0", null));
		assertTrue(validator.isValid("+0", null));
		assertTrue(validator.isValid("-0", null));
		assertTrue(validator.isValid("+65465", null));
		assertTrue(validator.isValid("-65465", null));
		assertTrue(validator.isValid("2147483647", null));
		assertFalse(validator.isValid("2147483648", null));
		assertTrue(validator.isValid("-2147483648", null));
		assertFalse(validator.isValid("-2147483649", null));
	}

	@Test
	public void testIntegerValidatorHexa() {
		assertTrue(validator.isValid("-0xadc", null));
		assertTrue(validator.isValid("+0xadf", null));
		assertTrue(validator.isValid("0x7FFFFFFF", null));
		assertFalse(validator.isValid("0x80000000", null));
		assertTrue(validator.isValid("-0x80000000", null));
		assertFalse(validator.isValid("-0x80000001", null));
	}

	public void testIntegerValidatorOctal() {
		assertTrue(validator.isValid("0o545435", null));
		assertTrue(validator.isValid("-0o545435", null));
		assertTrue(validator.isValid("0o17777777777", null));
		assertFalse(validator.isValid("0o20000000000", null));
		assertTrue(validator.isValid("-0o20000000000", null));
		assertFalse(validator.isValid("-0o20000000001", null));
	}

	@Test
	public void testIntegerValidatorIncorrect() {
		assertFalse(validator.isValid("-2.147483649", null));
		assertFalse(validator.isValid("dsfasf342342", null));
	}
}
