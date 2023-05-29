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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.openecomp.sdc.be.datatypes.elements.SchemaDefinition;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.tosca.ToscaType;
import org.openecomp.sdc.be.model.tosca.constraints.exception.ConstraintValueDoNotMatchPropertyTypeException;
import org.openecomp.sdc.be.model.tosca.constraints.exception.ConstraintViolationException;

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

    @Test
    void testValidateConstraintWithoutDefaultValue() throws ConstraintValueDoNotMatchPropertyTypeException {
        PropertyDefinition prop = new PropertyDefinition();
        ValidValuesConstraint constraint = createIntegerTestSubject();
        SchemaDefinition schemaDefinition = new SchemaDefinition();
        constraint.initialize(ToscaType.INTEGER, schemaDefinition);

        prop.setType(ToscaType.INTEGER.getType());

        assertDoesNotThrow(() -> {
            constraint.validate(prop);
        });
    }

    @Test
    void testValidateConstraintWithDefaultValue() throws ConstraintValueDoNotMatchPropertyTypeException {
        PropertyDefinition prop = new PropertyDefinition();
        ValidValuesConstraint constraint = createIntegerTestSubject();
        SchemaDefinition schemaDefinition = new SchemaDefinition();
        constraint.initialize(ToscaType.INTEGER, schemaDefinition);

        prop.setDefaultValue("2");
        prop.setType(ToscaType.INTEGER.getType());

        assertDoesNotThrow(() -> {
            constraint.validate(prop);
        });
    }

    @Test
    void testValidateConstraintWithIncorrectDefaultValue() throws ConstraintValueDoNotMatchPropertyTypeException {
        PropertyDefinition prop = new PropertyDefinition();
        ValidValuesConstraint constraint = createIntegerTestSubject();
        SchemaDefinition schemaDefinition = new SchemaDefinition();
        constraint.initialize(ToscaType.INTEGER, schemaDefinition);

        prop.setDefaultValue("1000");
        prop.setType(ToscaType.INTEGER.getType());

        assertThrows(ConstraintViolationException.class, () -> {
            constraint.validate(prop);
        });
    }

    @Test
    void testValidateConstraintWithInvalidDefaultValue() throws ConstraintValueDoNotMatchPropertyTypeException {
        PropertyDefinition prop = new PropertyDefinition();
        ValidValuesConstraint constraint = createIntegerTestSubject();
        SchemaDefinition schemaDefinition = new SchemaDefinition();
        constraint.initialize(ToscaType.INTEGER, schemaDefinition);

        prop.setDefaultValue("A String");
        prop.setType(ToscaType.INTEGER.getType());

        assertThrows(NumberFormatException.class, () -> {
            constraint.validate(prop);
        });
    }

    @Test
    void testValidateConstraintWithNullProp() {
        ValidValuesConstraint constraint = createIntegerTestSubject();

        assertThrows(NullPointerException.class, () -> {
            constraint.validate(null);
        });
    }

    @Test
    void testValidateConstraintWithListProp() throws ConstraintValueDoNotMatchPropertyTypeException {
        PropertyDefinition prop = new PropertyDefinition();
        PropertyDefinition intProp = new PropertyDefinition();
        ValidValuesConstraint constraint = createIntegerTestSubject();
        SchemaDefinition schemaDefinition = new SchemaDefinition();
        constraint.initialize(ToscaType.INTEGER, schemaDefinition);

        prop.setDefaultValue("[\"2\"]");
        prop.setType(ToscaType.LIST.getType());

        intProp.setType(ToscaType.INTEGER.getType());
        schemaDefinition.setProperty(intProp);
        prop.setSchema(schemaDefinition);

        assertDoesNotThrow(() -> {
            constraint.validate(prop);
        });
    }

    @Test
    void testValidateConstraintWithMapProp() throws ConstraintValueDoNotMatchPropertyTypeException {
        PropertyDefinition prop = new PropertyDefinition();
        PropertyDefinition intProp = new PropertyDefinition();
        ValidValuesConstraint constraint = createIntegerTestSubject();
        SchemaDefinition schemaDefinition = new SchemaDefinition();
        constraint.initialize(ToscaType.INTEGER, schemaDefinition);

        prop.setDefaultValue("{\"key\": \"2\"}");
        prop.setType(ToscaType.MAP.getType());

        intProp.setType(ToscaType.INTEGER.getType());
        schemaDefinition.setProperty(intProp);
        prop.setSchema(schemaDefinition);

        assertDoesNotThrow(() -> {
            constraint.validate(prop);
        });
    }
}
