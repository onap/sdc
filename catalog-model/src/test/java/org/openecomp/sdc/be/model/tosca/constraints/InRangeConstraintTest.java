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

public class InRangeConstraintTest {

    private InRangeConstraint createStringTestSubject() {
        List<Object> validValues = new ArrayList<>();
        validValues.add("test1");
        validValues.add("test10");
        return new InRangeConstraint(validValues);
    }

    private InRangeConstraint createIntegerTestSubject() {
        List<Object> validValues = new ArrayList<>();
        validValues.add(1);
        validValues.add(10);
        return new InRangeConstraint(validValues);
    }

    @Test
    public void testGetInRange() {
        InRangeConstraint testSubject = createStringTestSubject();
        List<Object> result = testSubject.getInRange();

        assertFalse(result.isEmpty());
        assertEquals("test1", result.get(0));
        assertEquals("test10", result.get(1));
    }

    @Test
    public void testSetInRange() {
        InRangeConstraint testSubject = createStringTestSubject();
        List<Object> validValues = new ArrayList<>();
        validValues.add("test21");
        validValues.add("test30");
        testSubject.setInRange(validValues);
        List<Object> result = testSubject.getInRange();

        assertEquals(2, result.size());
        assertEquals("test21", result.get(0));
        assertEquals("test30", result.get(1));
    }

    @Test
    public void testGetRangeMinValue() throws Exception {
        InRangeConstraint testSubject = createIntegerTestSubject();
        Object result = testSubject.getRangeMinValue();

        assertEquals(1, result);
    }

    @Test
    public void testSetRangeMinValue() throws Exception {
        InRangeConstraint testSubject = createIntegerTestSubject();
        testSubject.setRangeMinValue(21);

        Object result = testSubject.getRangeMinValue();

        assertEquals(21, result);
    }

    @Test
    public void testGetRangeMaxValue() throws Exception {
        InRangeConstraint testSubject = createIntegerTestSubject();
        Object result = testSubject.getRangeMaxValue();

        assertEquals(10, result);
    }

    @Test
    public void testSetRangeMaxValue() throws Exception {
        InRangeConstraint testSubject = createIntegerTestSubject();
        testSubject.setRangeMaxValue(30);

        Object result = testSubject.getRangeMaxValue();

        assertEquals(30, result);
    }

    @Test
    public void testValidateValueTypeStringTrue() throws ConstraintValueDoNotMatchPropertyTypeException {
        InRangeConstraint testSubject = createStringTestSubject();
        Boolean validTypes = testSubject.validateValueType("string");

        assertTrue(validTypes);
    }

    @Test
    public void testValidateValueTypeStringFalse() throws ConstraintValueDoNotMatchPropertyTypeException {
        InRangeConstraint testSubject = createStringTestSubject();
        Boolean validTypes = testSubject.validateValueType("integer");

        assertFalse(validTypes);
    }

    @Test
    public void testValidateValueTypeIntegerTrue() throws ConstraintValueDoNotMatchPropertyTypeException {
        InRangeConstraint testSubject = createIntegerTestSubject();
        Boolean validTypes = testSubject.validateValueType("integer");

        assertTrue(validTypes);
    }

    @Test
    public void testValidateValueTypeIntegerFalse() throws ConstraintValueDoNotMatchPropertyTypeException {
        InRangeConstraint testSubject = createIntegerTestSubject();
        Boolean validTypes = testSubject.validateValueType("string");

        assertFalse(validTypes);
    }

    @Test
    public void testChangeStringConstraintValueTypeToIntegerThrow() {
        String propertyType = "integer";
        InRangeConstraint testSubject = createStringTestSubject();
        Exception exception = assertThrows(ConstraintValueDoNotMatchPropertyTypeException.class, () -> {
            testSubject.changeConstraintValueTypeTo(propertyType);
        });
        String expectedMessage =
            "inRange constraint has invalid values <" + testSubject.getInRange() + "> property type is <" + propertyType + ">";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void testChangeIntegerConstraintValueTypeToString() throws ConstraintValueDoNotMatchPropertyTypeException {
        InRangeConstraint testSubject = createIntegerTestSubject();
        testSubject.changeConstraintValueTypeTo("string");
        List<Object> result = testSubject.getInRange();

        result.forEach(value -> assertTrue(value instanceof String));
    }
}
