/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2022 Nordix Foundation
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================
 */

package org.openecomp.sdc.be.model.tosca.constraints;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.openecomp.sdc.be.model.tosca.constraints.exception.ConstraintValueDoNotMatchPropertyTypeException;

public class LessThanConstraintTest {

    private LessThanConstraint createStringTestSubject() {
        return new LessThanConstraint("test");
    }

    private LessThanConstraint createIntegerTestSubject() {
        return new LessThanConstraint(418);
    }

    @Test
    public void testGetLessThan() {
        LessThanConstraint testSubject = createStringTestSubject();
        Object result = testSubject.getLessThan();

        assertEquals("test", result);
    }

    @Test
    public void testSetLessThan() {
        LessThanConstraint testSubject = createStringTestSubject();
        testSubject.setLessThan("test2");
        Object result = testSubject.getLessThan();

        assertEquals("test2", result);
    }

    @Test
    public void testValidateValueTypeStringTrue() throws ConstraintValueDoNotMatchPropertyTypeException {
        LessThanConstraint testSubject = createStringTestSubject();
        Boolean validTypes = testSubject.validateValueType("string");
        assertTrue(validTypes);
    }

    @Test
    public void testValidateValueTypeStringFalse() throws ConstraintValueDoNotMatchPropertyTypeException {
        LessThanConstraint testSubject = createStringTestSubject();
        Boolean validTypes = testSubject.validateValueType("integer");
        assertFalse(validTypes);
    }

    @Test
    public void testValidateValueTypeIntegerTrue() throws ConstraintValueDoNotMatchPropertyTypeException {
        LessThanConstraint testSubject = createIntegerTestSubject();
        Boolean validTypes = testSubject.validateValueType("integer");
        assertTrue(validTypes);
    }

    @Test
    public void testValidateValueTypeIntegerFalse() throws ConstraintValueDoNotMatchPropertyTypeException {
        LessThanConstraint testSubject = createIntegerTestSubject();
        Boolean validTypes = testSubject.validateValueType("string");
        assertFalse(validTypes);
    }

    @Test
    public void testChangeStringConstraintValueTypeToIntegerThrow() {
        String propertyType = "integer";
        LessThanConstraint testSubject = createStringTestSubject();
        Exception exception = assertThrows(ConstraintValueDoNotMatchPropertyTypeException.class, () -> {
            testSubject.changeConstraintValueTypeTo(propertyType);
        });

        String expectedMessage =
            "lessThan constraint has invalid values <" + testSubject.getLessThan() + "> property type is <" + propertyType + ">";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void testChangeIntegerConstraintValueTypeToString() throws ConstraintValueDoNotMatchPropertyTypeException {
        LessThanConstraint testSubject = createIntegerTestSubject();

        testSubject.changeConstraintValueTypeTo("string");
        Object result = testSubject.getLessThan();

        assertTrue(result instanceof String);
    }
}
