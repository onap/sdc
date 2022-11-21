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

public class EqualConstraintTest {

    private EqualConstraint createStringTestSubject() {
        return new EqualConstraint("test");
    }

    private EqualConstraint createIntegerTestSubject() {
        return new EqualConstraint(418);
    }

    @Test
    public void testGetEqual() {
        EqualConstraint testSubject = createStringTestSubject();
        Object result = testSubject.getEqual();

        assertEquals("test", result);
    }

    @Test
    public void testSetEqual() {
        EqualConstraint testSubject = createStringTestSubject();
        testSubject.setEqual("test2");
        Object result = testSubject.getEqual();

        assertEquals("test2", result);
    }

    @Test
    public void testValidateValueTypeStringTrue() throws ConstraintValueDoNotMatchPropertyTypeException {
        EqualConstraint testSubject = createStringTestSubject();
        Boolean validTypes = testSubject.validateValueType("string");
        assertTrue(validTypes);
    }

    @Test
    public void testValidateValueTypeStringFalse() throws ConstraintValueDoNotMatchPropertyTypeException {
        EqualConstraint testSubject = createStringTestSubject();
        Boolean validTypes = testSubject.validateValueType("integer");
        assertFalse(validTypes);
    }

    @Test
    public void testValidateValueTypeIntegerTrue() throws ConstraintValueDoNotMatchPropertyTypeException {
        EqualConstraint testSubject = createIntegerTestSubject();
        Boolean validTypes = testSubject.validateValueType("integer");
        assertTrue(validTypes);
    }

    @Test
    public void testValidateValueTypeIntegerFalse() throws ConstraintValueDoNotMatchPropertyTypeException {
        EqualConstraint testSubject = createIntegerTestSubject();
        Boolean validTypes = testSubject.validateValueType("string");
        assertFalse(validTypes);
    }

    @Test
    public void testChangeStringConstraintValueTypeToIntegerThrow() {
        String propertyType = "integer";
        EqualConstraint testSubject = createStringTestSubject();
        Exception exception = assertThrows(ConstraintValueDoNotMatchPropertyTypeException.class, () -> {
            testSubject.changeConstraintValueTypeTo(propertyType);
        });

        String expectedMessage =
            "equal constraint has invalid values <" + testSubject.getEqual() + "> property type is <" + propertyType + ">";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void testChangeIntegerConstraintValueTypeToString() throws ConstraintValueDoNotMatchPropertyTypeException {
        EqualConstraint testSubject = createIntegerTestSubject();

        testSubject.changeConstraintValueTypeTo("string");
        Object result = testSubject.getEqual();

        assertTrue(result instanceof String);
    }
}
