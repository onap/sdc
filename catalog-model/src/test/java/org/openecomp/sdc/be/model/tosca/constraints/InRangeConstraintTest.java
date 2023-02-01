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

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.openecomp.sdc.be.model.tosca.constraints.exception.ConstraintValueDoNotMatchPropertyTypeException;

class InRangeConstraintTest {

    private InRangeConstraint createStringTestSubject() {
        return new InRangeConstraint(Arrays.asList("test1", "test10"));
    }

    private InRangeConstraint createIntegerTestSubject() {
        return new InRangeConstraint(Arrays.asList(1, 10));
    }

    @Test
    void testGetInRange() {
        InRangeConstraint testSubject = createStringTestSubject();
        List<Object> result = testSubject.getInRange();

        assertFalse(result.isEmpty());
        assertEquals("test1", result.get(0));
        assertEquals("test10", result.get(1));
    }

    @Test
    void testSetInRange() {
        InRangeConstraint testSubject = createStringTestSubject();
        testSubject.setInRange(Arrays.asList("test21", "test30"));
        List<Object> result = testSubject.getInRange();

        assertEquals(2, result.size());
        assertEquals("test21", result.get(0));
        assertEquals("test30", result.get(1));
    }

    @Test
    void testValidateValueTypeStringTrue() throws ConstraintValueDoNotMatchPropertyTypeException {
        InRangeConstraint testSubject = createStringTestSubject();
        Boolean validTypes = testSubject.validateValueType("string");

        assertTrue(validTypes);
    }

    @Test
    void testValidateValueTypeStringFalse() throws ConstraintValueDoNotMatchPropertyTypeException {
        InRangeConstraint testSubject = createStringTestSubject();
        Boolean validTypes = testSubject.validateValueType("integer");

        assertFalse(validTypes);
    }

    @Test
    void testValidateValueTypeIntegerTrue() throws ConstraintValueDoNotMatchPropertyTypeException {
        InRangeConstraint testSubject = createIntegerTestSubject();
        Boolean validTypes = testSubject.validateValueType("integer");

        assertTrue(validTypes);
    }

    @Test
    void testValidateValueTypeIntegerFalse() throws ConstraintValueDoNotMatchPropertyTypeException {
        InRangeConstraint testSubject = createIntegerTestSubject();
        Boolean validTypes = testSubject.validateValueType("string");

        assertFalse(validTypes);
    }

    @Test
    void testChangeStringConstraintValueTypeToIntegerThrow() {
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
    void testChangeIntegerConstraintValueTypeToString() throws ConstraintValueDoNotMatchPropertyTypeException {
        InRangeConstraint testSubject = createIntegerTestSubject();
        testSubject.changeConstraintValueTypeTo("string");
        List<Object> result = testSubject.getInRange();

        result.forEach(value -> assertTrue(value instanceof String));
    }
}
