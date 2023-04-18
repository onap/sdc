/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

import java.util.List;

import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.openecomp.sdc.be.datatypes.enums.ConstraintType;
import org.openecomp.sdc.be.model.PropertyConstraint;
import org.openecomp.sdc.be.model.tosca.ToscaType;
import org.openecomp.sdc.be.model.tosca.constraints.exception.ConstraintFunctionalException;
import org.openecomp.sdc.be.model.tosca.constraints.exception.ConstraintValueDoNotMatchPropertyTypeException;
import org.openecomp.sdc.be.model.tosca.constraints.exception.ConstraintViolationException;
import org.openecomp.sdc.be.model.tosca.constraints.exception.PropertyConstraintException;

@SuppressWarnings("serial")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class EqualConstraint extends AbstractComparablePropertyConstraint {

    @Getter
    @Setter
    @NotNull
    @EqualsAndHashCode.Include
    private Object equal;
    private Object typed;

    public EqualConstraint(Object equal) {
        super();
        this.equal = equal;
    }

    @Override
    public void initialize(ToscaType propertyType) throws ConstraintValueDoNotMatchPropertyTypeException {
        if (propertyType.isValidValue(String.valueOf(equal))) {
            typed = propertyType.convert(String.valueOf(equal));
            if (ConstraintUtil.isComparableType(propertyType) || ToscaType.BOOLEAN.equals(propertyType)) {
                initialize(String.valueOf(equal), propertyType);
            }
        } else {
            throw new ConstraintValueDoNotMatchPropertyTypeException(
                "constraintValue constraint has invalid value <" + equal + "> property type is <" + propertyType.toString() + ">");
        }
    }

    @Override
    public ConstraintType getConstraintType() {
        return ConstraintType.EQUAL;
    }

    @Override
    public void validateValueOnUpdate(PropertyConstraint newConstraint) throws PropertyConstraintException {
    }

    private void fail(Object propertyValue) throws ConstraintViolationException {
        throw new ConstraintViolationException(
            "Equal constraint violation, the reference is <" + equal + "> but the value to compare is <" + propertyValue + ">");
    }

    @Override
    public String getErrorMessage(ToscaType toscaType, ConstraintFunctionalException e, String propertyName) {
        return getErrorMessage(toscaType, e, propertyName, "'%s' value must be %s", String.valueOf(equal));
    }

    @Override
    protected void doValidate(Object propertyValue) throws ConstraintViolationException {
        if (propertyValue == null) {
            if (typed != null) {
                fail(null);
            }
        } else if (typed == null) {
            fail(propertyValue);
        } else if (!typed.equals(propertyValue)) {
            fail(propertyValue);
        }
    }

    @Override
    public String toString() {
        return String.valueOf(equal);
    }

    @Override
    public void validate(Object propertyValue) throws ConstraintViolationException {
        if (propertyValue == null) {
            throw new ConstraintViolationException("Value to check is null");
        }
        if (isComparableValue(propertyValue)) {
            super.validate(propertyValue);
        }
        doValidate(propertyValue);
    }
    
    private boolean isComparableValue(Object propertyValue) {
        return Comparable.class.isAssignableFrom(propertyValue.getClass());
    }

    public boolean validateValueType(String propertyType) throws ConstraintValueDoNotMatchPropertyTypeException {
        ToscaType toscaType = ToscaType.getToscaType(propertyType);
        if (toscaType == null) {
            throw new ConstraintValueDoNotMatchPropertyTypeException(
                "equal constraint has invalid values <" + equal.toString() + "> property type is <" + propertyType + ">");
        }
        if (equal == null) {
            throw new ConstraintValueDoNotMatchPropertyTypeException(
                "equal constraint has invalid value <> property type is <" + propertyType + ">");
        }
        return toscaType.isValueTypeValid(equal);
    }

    public void changeConstraintValueTypeTo(String propertyType) throws ConstraintValueDoNotMatchPropertyTypeException {
        ToscaType toscaType = ToscaType.getToscaType(propertyType);
        try {
            equal = toscaType.convert(String.valueOf(equal));
        } catch (Exception e) {
            throw new ConstraintValueDoNotMatchPropertyTypeException(
                "equal constraint has invalid values <" + equal.toString() + "> property type is <" + propertyType + ">");
        }
    }
}
