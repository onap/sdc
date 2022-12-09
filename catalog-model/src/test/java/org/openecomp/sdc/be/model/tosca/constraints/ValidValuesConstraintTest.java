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

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.openecomp.sdc.be.model.tosca.constraints.exception.ConstraintValueDoNotMatchPropertyTypeException;

class ValidValuesConstraintTest {

    private ValidValuesConstraint createStringTestSubject() {
        List<Object> validValues = new ArrayList<>();
        validValues.add("test1");
        validValues.add("test2");
        validValues.add("test3");
        validValues.add("test4");
        return new ValidValuesConstraint(validValues);
    }

    private ValidValuesConstraint createIntegerTestSubject() {
        List<Object> validValues = new ArrayList<>();
        validValues.add(1);
        validValues.add(2);
        validValues.add(3);
        validValues.add(4);
        return new ValidValuesConstraint(validValues);
    }

    @Test
    void testGetValidValues() {
        ValidValuesConstraint testSubject = createStringTestSubject();
        List<Object> result = testSubject.getValidValues();

        assertFalse(result.isEmpty());
        assertEquals("test1", result.get(0));
        assertEquals("test4", result.get(3));
    }

    @Test
    void testSetValidValues() {
        ValidValuesConstraint testSubject = createStringTestSubject();
        List<Object> validValues = new ArrayList<>();
        validValues.add("test5");
        validValues.add("test6");
        validValues.add("test7");
        validValues.add("test8");
        testSubject.setValidValues(validValues);

        List<Object> result = testSubject.getValidValues();

        assertEquals(4, result.size());
        assertEquals("test5", result.get(0));
        assertEquals("test8", result.get(3));
    }

    @Test
    void testValidateValueTypeStringTrue() throws ConstraintValueDoNotMatchPropertyTypeException {
        ValidValuesConstraint testSubject = createStringTestSubject();
        assertTrue(testSubject.validateValueType("string"));
    }

    @Test
    void testValidateValueTypeStringFalse() throws ConstraintValueDoNotMatchPropertyTypeException {
        ValidValuesConstraint testSubject = createStringTestSubject();
        assertFalse(testSubject.validateValueType("integer"));
    }

    @Test
    void testValidateValueTypeIntegerTrue() throws ConstraintValueDoNotMatchPropertyTypeException {
        ValidValuesConstraint testSubject = createIntegerTestSubject();
        assertTrue(testSubject.validateValueType("integer"));
    }

    @Test
    void testValidateValueTypeIntegerFalse() throws ConstraintValueDoNotMatchPropertyTypeException {
        ValidValuesConstraint testSubject = createIntegerTestSubject();
        assertFalse(testSubject.validateValueType("string"));
    }

    @Test
    void testChangeStringConstraintValueTypeToIntegerThrow() {
        String propertyType = "integer";
        ValidValuesConstraint testSubject = createStringTestSubject();
        Exception exception = assertThrows(ConstraintValueDoNotMatchPropertyTypeException.class, () -> {
            testSubject.changeConstraintValueTypeTo(propertyType);
        });

        String expectedMessage =
            "validValues constraint has invalid values <" + testSubject.getValidValues() + "> property type is <" + propertyType + ">";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void testChangeIntegerConstraintValueTypeToString() throws ConstraintValueDoNotMatchPropertyTypeException {
        ValidValuesConstraint testSubject = createIntegerTestSubject();

        testSubject.changeConstraintValueTypeTo("string");
        List<Object> result = testSubject.getValidValues();

        result.forEach(value -> assertTrue(value instanceof String));
    }
}
