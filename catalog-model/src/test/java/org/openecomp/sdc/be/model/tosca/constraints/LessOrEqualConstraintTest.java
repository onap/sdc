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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.openecomp.sdc.be.model.tosca.constraints.exception.ConstraintValueDoNotMatchPropertyTypeException;

public class LessOrEqualConstraintTest {

	private LessOrEqualConstraint createStringTestSubject() {
		return new LessOrEqualConstraint("test");
	}

	private LessOrEqualConstraint createIntegerTestSubject() {
		return new LessOrEqualConstraint(418);
	}

	@Test
	public void testGetLessOrEqualThan() {
		LessOrEqualConstraint testSubject = createStringTestSubject();
		Object result = testSubject.getLessOrEqual();

		assertEquals("test", result);
	}

	@Test
	public void testSetLessOrEqual() {
		LessOrEqualConstraint testSubject = createStringTestSubject();
		testSubject.setLessOrEqual("test2");
		Object result = testSubject.getLessOrEqual();

		assertEquals("test2", result);
	}

	@Test
	public void testValidateValueTypeStringTrue() throws ConstraintValueDoNotMatchPropertyTypeException {
		LessOrEqualConstraint testSubject = createStringTestSubject();
		Boolean validTypes = testSubject.validateValueType("string");
		assertTrue(validTypes);
	}

	@Test
	public void testValidateValueTypeStringFalse() throws ConstraintValueDoNotMatchPropertyTypeException {
		LessOrEqualConstraint testSubject = createStringTestSubject();
		Boolean validTypes = testSubject.validateValueType("integer");
		assertFalse(validTypes);
	}

	@Test
	public void testValidateValueTypeIntegerTrue() throws ConstraintValueDoNotMatchPropertyTypeException {
		LessOrEqualConstraint testSubject = createIntegerTestSubject();
		Boolean validTypes = testSubject.validateValueType("integer");
		assertTrue(validTypes);
	}

	@Test
	public void testValidateValueTypeIntegerFalse() throws ConstraintValueDoNotMatchPropertyTypeException {
		LessOrEqualConstraint testSubject = createIntegerTestSubject();
		Boolean validTypes = testSubject.validateValueType("string");
		assertFalse(validTypes);
	}

	@Test
	public void testChangeStringConstraintValueTypeToIntegerThrow() {
		String propertyType = "integer";
		LessOrEqualConstraint testSubject = createStringTestSubject();
		Exception exception = assertThrows(ConstraintValueDoNotMatchPropertyTypeException.class, () -> {
			testSubject.changeConstraintValueTypeTo(propertyType);
		});

		String expectedMessage =
				"lessOrEqual constraint has invalid values <" + testSubject.getLessOrEqual() + "> property type is <" + propertyType + ">";
		String actualMessage = exception.getMessage();

		assertTrue(actualMessage.contains(expectedMessage));
	}

	@Test
	public void testChangeIntegerConstraintValueTypeToString() throws ConstraintValueDoNotMatchPropertyTypeException {
		LessOrEqualConstraint testSubject = createIntegerTestSubject();

		testSubject.changeConstraintValueTypeTo("string");
		Object result = testSubject.getLessOrEqual();

		assertTrue(result instanceof String);
	}
}
