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

import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.openecomp.sdc.be.datatypes.enums.ConstraintType;
import org.openecomp.sdc.be.model.PropertyConstraint;
import org.openecomp.sdc.be.model.tosca.ToscaType;
import org.openecomp.sdc.be.model.tosca.constraints.exception.ConstraintFunctionalException;
import org.openecomp.sdc.be.model.tosca.constraints.exception.ConstraintValueDoNotMatchPropertyTypeException;
import org.openecomp.sdc.be.model.tosca.constraints.exception.ConstraintViolationException;
import org.openecomp.sdc.be.model.tosca.constraints.exception.PropertyConstraintException;

@Getter
@Setter
@AllArgsConstructor
public class GreaterOrEqualConstraint extends AbstractComparablePropertyConstraint {

    @NotNull
    private Object greaterOrEqual;

    @Override
    public void initialize(ToscaType propertyType) throws ConstraintValueDoNotMatchPropertyTypeException {
        initialize(String.valueOf(greaterOrEqual), propertyType);
    }

    @Override
    public ConstraintType getConstraintType() {
        return ConstraintType.GREATER_OR_EQUAL;
    }

    @Override
    public void validateValueOnUpdate(PropertyConstraint newConstraint) throws PropertyConstraintException {
    }

    @Override
    protected void doValidate(Object propertyValue) throws ConstraintViolationException {
        if (getComparable().compareTo(propertyValue) > 0) {
            throw new ConstraintViolationException(propertyValue + " <= " + greaterOrEqual);
        }
    }

    @Override
    public String getErrorMessage(ToscaType toscaType, ConstraintFunctionalException e, String propertyName) {
        return getErrorMessage(toscaType, e, propertyName, "%s property value must be greater than or equal to %s", String.valueOf(greaterOrEqual));
    }

    @Override
    public boolean validateValueType(String propertyType) throws ConstraintValueDoNotMatchPropertyTypeException {
        ToscaType toscaType = ToscaType.getToscaType(propertyType);
        if (toscaType == null) {
            throw new ConstraintValueDoNotMatchPropertyTypeException(
                    "greaterOrEqual constraint has invalid values <" + greaterOrEqual.toString() + "> property type is <" + propertyType + ">");
        }
        if (greaterOrEqual == null) {
            throw new ConstraintValueDoNotMatchPropertyTypeException(
                    "greaterOrEqual constraint has invalid value <> property type is <" + propertyType + ">");
        }
        return toscaType.isValueTypeValid(greaterOrEqual);
    }

    @Override
    public String getConstraintValueAsString() {
        return String.valueOf(greaterOrEqual);
    }

    @Override
    public void changeConstraintValueTypeTo(String propertyType) throws ConstraintValueDoNotMatchPropertyTypeException {
        ToscaType toscaType = ToscaType.getToscaType(propertyType);
        try {
            greaterOrEqual = toscaType.convert(String.valueOf(greaterOrEqual));
        } catch (Exception e) {
            throw new ConstraintValueDoNotMatchPropertyTypeException(
                "lessThan constraint has invalid values <" + greaterOrEqual.toString() + "> property type is <" + propertyType + ">");
        }
    }
}
