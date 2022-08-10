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
import org.openecomp.sdc.be.model.tosca.constraints.exception.ConstraintValueDoNotMatchPropertyTypeException;
import org.openecomp.sdc.be.model.tosca.constraints.exception.ConstraintViolationException;
import org.openecomp.sdc.be.model.tosca.constraints.exception.PropertyConstraintException;

public class AbstractStringPropertyConstraintTest {

    private static final String PROPERTY_VALUE = "propValue";

    private final TestStringPropertyConstraint constraint = new TestStringPropertyConstraint();

    @Test
    public void testInitializeSuccess() throws ConstraintValueDoNotMatchPropertyTypeException {
        // when
        constraint.initialize(ToscaType.STRING);
    }

    @Test(expected = ConstraintValueDoNotMatchPropertyTypeException.class)
    public void testInitializeFailure() throws ConstraintValueDoNotMatchPropertyTypeException {
        // when
        constraint.initialize(ToscaType.TIMESTAMP);
    }

    @Test
    public void testValidateSuccess() throws ConstraintViolationException {
        // when
        constraint.validate(PROPERTY_VALUE);

        // then
        assertEquals(PROPERTY_VALUE, constraint.last);
    }

    @Test(expected = ConstraintViolationException.class)
    public void testValidateNull() throws ConstraintViolationException {
        // when
        constraint.validate(null);
    }

    @Test(expected = ConstraintViolationException.class)
    public void testValidateNotString() throws ConstraintViolationException {
        // when
        constraint.validate(Integer.valueOf(123));
    }
}

class TestStringPropertyConstraint extends AbstractStringPropertyConstraint {

    String last;

    @Override
    protected void doValidate(String propertyValue) {
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