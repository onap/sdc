/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.be.model.tosca.constraints;

import org.junit.jupiter.api.Test;
import org.openecomp.sdc.be.model.tosca.constraints.exception.ConstraintValueDoNotMatchPropertyTypeException;

import static org.junit.jupiter.api.Assertions.*;

public class GreaterOrEqualConstraintTest {

	private GreaterOrEqualConstraint createStringTestSubject() {
		return new GreaterOrEqualConstraint("test");
	}

	private GreaterOrEqualConstraint createIntegerTestSubject() {
		return new GreaterOrEqualConstraint(418);
	}

	@Test
	public void testGetGreaterOrEqualThan() {
		GreaterOrEqualConstraint testSubject = createStringTestSubject();
		Object result = testSubject.getGreaterOrEqual();

		assertEquals("test", result);
	}

	@Test
	public void testSetGreaterOrEqual() {
		GreaterOrEqualConstraint testSubject = createStringTestSubject();
		testSubject.setGreaterOrEqual("test2");
		Object result = testSubject.getGreaterOrEqual();

		assertEquals("test2", result);
	}

	@Test
	public void testValidateValueTypeStringTrue() throws ConstraintValueDoNotMatchPropertyTypeException {
		GreaterOrEqualConstraint testSubject = createStringTestSubject();
		Boolean validTypes = testSubject.validateValueType("string");
		assertTrue(validTypes);
	}

	@Test
	public void testValidateValueTypeStringFalse() throws ConstraintValueDoNotMatchPropertyTypeException {
		GreaterOrEqualConstraint testSubject = createStringTestSubject();
		Boolean validTypes = testSubject.validateValueType("integer");
		assertFalse(validTypes);
	}

	@Test
	public void testValidateValueTypeIntegerTrue() throws ConstraintValueDoNotMatchPropertyTypeException {
		GreaterOrEqualConstraint testSubject = createIntegerTestSubject();
		Boolean validTypes = testSubject.validateValueType("integer");
		assertTrue(validTypes);
	}

	@Test
	public void testValidateValueTypeIntegerFalse() throws ConstraintValueDoNotMatchPropertyTypeException {
		GreaterOrEqualConstraint testSubject = createIntegerTestSubject();
		Boolean validTypes = testSubject.validateValueType("string");
		assertFalse(validTypes);
	}

	@Test
	public void testChangeStringConstraintValueTypeToIntegerThrow() {
		String propertyType = "integer";
		GreaterOrEqualConstraint testSubject = createStringTestSubject();
		Exception exception = assertThrows(ConstraintValueDoNotMatchPropertyTypeException.class, () -> {
			testSubject.changeConstraintValueTypeTo(propertyType);
		});

		String expectedMessage =
				"greaterOrEqual constraint has invalid values <" + testSubject.getGreaterOrEqual() + "> property type is <" + propertyType + ">";
		String actualMessage = exception.getMessage();

		assertTrue(actualMessage.contains(expectedMessage));
	}

	@Test
	public void testChangeIntegerConstraintValueTypeToString() throws ConstraintValueDoNotMatchPropertyTypeException {
		GreaterOrEqualConstraint testSubject = createIntegerTestSubject();

		testSubject.changeConstraintValueTypeTo("string");
		Object result = testSubject.getGreaterOrEqual();

		assertTrue(result instanceof String);
	}
}
