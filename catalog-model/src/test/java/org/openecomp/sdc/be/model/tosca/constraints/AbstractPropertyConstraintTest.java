/*-
 * ============LICENSE_START=======================================================
 * ONAP SDC
 * ================================================================================
 * Copyright (C) 2019 Samsung. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END============================================
 * ===================================================================
 */

package org.openecomp.sdc.be.model.tosca.constraints;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.openecomp.sdc.be.datatypes.enums.ConstraintType;
import org.openecomp.sdc.be.model.PropertyConstraint;
import org.openecomp.sdc.be.model.tosca.ToscaType;
import org.openecomp.sdc.be.model.tosca.constraints.exception.ConstraintFunctionalException;
import org.openecomp.sdc.be.model.tosca.constraints.exception.ConstraintViolationException;
import org.openecomp.sdc.be.model.tosca.constraints.exception.PropertyConstraintException;
import org.openecomp.sdc.be.model.tosca.version.Version;

public class AbstractPropertyConstraintTest {

    private final TestPropertyConstraint constraint = new TestPropertyConstraint();

    @Test
    public void testValidateBoolean() throws ConstraintViolationException {
        // given
        final String value = "faLsE";

        // when
        constraint.validate(ToscaType.BOOLEAN, value);

        // then
        assertEquals(Boolean.valueOf(value), constraint.last);
    }

    @Test
    public void testValidateFloat() throws ConstraintViolationException {
        // given
        final String value = "123.456";

        // when
        constraint.validate(ToscaType.FLOAT, value);

        // then
        assertEquals(Float.valueOf(value), constraint.last);
    }

    @Test
    public void testValidateVersion() throws ConstraintViolationException {
        // given
        final String value = "12.34.1002-Release7";

        // when
        constraint.validate(ToscaType.VERSION, value);

        // then
        assertEquals(new Version(value), constraint.last);
    }

    @Test
    public void testGetErrorMessageIsInstanceOf() {
        // given
        final ConstraintFunctionalException exception = new ConstraintViolationException("exc");

        // when
        final String message = constraint.getErrorMessage(ToscaType.SCALAR_UNIT, exception,
                "area", "Message: %s --> %s", "prop1", "prop2");

        // then
        assertEquals("Message: area --> [prop1, prop2]", message);
    }

    @Test
    public void testGetErrorMessageOtherType() {
        // given
        final ConstraintFunctionalException exception = new ConstraintFunctionalException("exc");

        // when
        final String message = constraint.getErrorMessage(ToscaType.SCALAR_UNIT_FREQUENCY, exception,
                "freq", null);

        // then
        assertEquals("Unsupported value provided for freq property supported value type is scalar-unit.frequency.",
                message);
    }
}

class TestPropertyConstraint extends AbstractPropertyConstraint {

    Object last;

    @Override
    public void validate(Object propertyValue) {
        last = propertyValue;
    }

    @Override
    public ConstraintType getConstraintType() {
        return null;
    }

    @Override
    public void validateValueOnUpdate(PropertyConstraint newConstraint) throws PropertyConstraintException {

    }

    @Override
    public String getErrorMessage(ToscaType toscaType, ConstraintFunctionalException exception, String propertyName) {
        return null;
    }
}
