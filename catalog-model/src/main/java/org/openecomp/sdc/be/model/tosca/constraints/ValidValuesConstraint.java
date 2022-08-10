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

import static java.util.stream.Collectors.toList;

import com.google.common.collect.Sets;
import java.util.List;
import java.util.Set;
import javax.validation.constraints.NotNull;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.enums.ConstraintType;
import org.openecomp.sdc.be.model.PropertyConstraint;
import org.openecomp.sdc.be.model.tosca.ToscaType;
import org.openecomp.sdc.be.model.tosca.constraints.exception.ConstraintFunctionalException;
import org.openecomp.sdc.be.model.tosca.constraints.exception.ConstraintValueDoNotMatchPropertyTypeException;
import org.openecomp.sdc.be.model.tosca.constraints.exception.ConstraintViolationException;
import org.openecomp.sdc.be.model.tosca.constraints.exception.PropertyConstraintException;

public class ValidValuesConstraint extends AbstractPropertyConstraint {

    private static final String PROPERTY_TYPE_IS = "> property type is <";
    @NotNull
    private List<String> validValues;
    private Set<Object> validValuesTyped;

    public ValidValuesConstraint(List<String> validValues) {
        this.validValues = validValues;
    }

    public ValidValuesConstraint() {
    }

    @Override
    public void initialize(ToscaType propertyType) throws ConstraintValueDoNotMatchPropertyTypeException {
        validValuesTyped = Sets.newHashSet();
        if (validValues == null) {
            throw new ConstraintValueDoNotMatchPropertyTypeException(
                "validValues constraint has invalid value <> property type is <" + propertyType.toString() + ">");
        }
        for (String value : validValues) {
            if (!propertyType.isValidValue(value)) {
                throw new ConstraintValueDoNotMatchPropertyTypeException(
                    "validValues constraint has invalid value <" + value + PROPERTY_TYPE_IS + propertyType.toString() + ">");
            } else {
                validValuesTyped.add(propertyType.convert(value));
            }
        }
    }

    public void validateType(String propertyType) throws ConstraintValueDoNotMatchPropertyTypeException {
        ToscaType toscaType = ToscaType.getToscaType(propertyType);
        if (toscaType == null) {
            throw new ConstraintValueDoNotMatchPropertyTypeException(
                "validValues constraint has invalid values <" + validValues.toString() + PROPERTY_TYPE_IS + propertyType + ">");
        }
        if (validValues == null) {
            throw new ConstraintValueDoNotMatchPropertyTypeException(
                "validValues constraint has invalid value <> property type is <" + propertyType + ">");
        }
        for (String value : validValues) {
            if (!toscaType.isValidValue(value)) {
                throw new ConstraintValueDoNotMatchPropertyTypeException(
                    "validValues constraint has invalid value <" + value + PROPERTY_TYPE_IS + propertyType + ">");
            }
        }
    }

    @Override
    public void validateValueOnUpdate(PropertyConstraint newConstraint) throws PropertyConstraintException {
        if (newConstraint.getConstraintType() == getConstraintType()) {
            if (!((ValidValuesConstraint) newConstraint).getValidValues().containsAll(validValues)) {
                throw new PropertyConstraintException("Deletion of exists value is not permitted", null, null,
                    ActionStatus.CANNOT_DELETE_VALID_VALUES, getConstraintType().name(),
                    validValues.stream().filter(v -> !((ValidValuesConstraint) newConstraint).getValidValues().contains(v)).collect(toList())
                        .toString());
            }
        }
    }

    @Override
    public void validate(Object propertyValue) throws ConstraintViolationException {
        if (propertyValue == null) {
            throw new ConstraintViolationException("Value to validate is null");
        }
        if (!validValuesTyped.contains(propertyValue)) {
            throw new ConstraintViolationException("The value is not in the list of valid values");
        }
    }

    public List<String> getValidValues() {
        return validValues;
    }

    public void setValidValues(List<String> validValues) {
        this.validValues = validValues;
    }

    @Override
    public ConstraintType getConstraintType() {
        return ConstraintType.VALID_VALUES;
    }

    @Override
    public String getErrorMessage(ToscaType toscaType, ConstraintFunctionalException e, String propertyName) {
        return getErrorMessage(toscaType, e, propertyName, "%s valid value must be one of the following: [%s]", String.join(",", validValues));
    }
}
