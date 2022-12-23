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

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import org.openecomp.sdc.be.datatypes.enums.ConstraintType;
import org.openecomp.sdc.be.model.PropertyConstraint;
import org.openecomp.sdc.be.model.tosca.ToscaType;
import org.openecomp.sdc.be.model.tosca.constraints.exception.ConstraintFunctionalException;
import org.openecomp.sdc.be.model.tosca.constraints.exception.ConstraintValueDoNotMatchPropertyTypeException;
import org.openecomp.sdc.be.model.tosca.constraints.exception.ConstraintViolationException;
import org.openecomp.sdc.be.model.tosca.constraints.exception.PropertyConstraintException;

@NoArgsConstructor
@Getter
@Setter
public class InRangeConstraint extends AbstractPropertyConstraint {

    @NonNull
    private List<Object> inRange;
    @JsonIgnore
    private Comparable min;
    @JsonIgnore
    private Comparable max;

    public InRangeConstraint(List<Object> inRange) {
        this.inRange = inRange;
    }

    @Override
    public void initialize(ToscaType propertyType) throws ConstraintValueDoNotMatchPropertyTypeException {
        // Perform verification that the property type is supported for comparison
        ConstraintUtil.checkComparableType(propertyType);
        if (inRange == null || inRange.size() != 2) {
            throw new ConstraintValueDoNotMatchPropertyTypeException("In range constraint must have two elements.");
        }
        String minRawText = String.valueOf(inRange.get(0));
        String maxRawText = String.valueOf(inRange.get(1));
        if (!propertyType.isValidValue(minRawText)) {
            throw new ConstraintValueDoNotMatchPropertyTypeException(
                "Invalid min value for in range constraint [" + minRawText + "] as it does not follow the property type [" + propertyType + "]");
        }
        if (!propertyType.isValidValue(maxRawText)) {
            throw new ConstraintValueDoNotMatchPropertyTypeException(
                "Invalid max value for in range constraint [" + maxRawText + "] as it does not follow the property type [" + propertyType + "]");
        }
        min = ConstraintUtil.convertToComparable(propertyType, minRawText);
        max = ConstraintUtil.convertToComparable(propertyType, maxRawText);
    }

    @Override
    public void validate(Object propertyValue) throws ConstraintViolationException {
        if (propertyValue == null) {
            throw new ConstraintViolationException("Value to check is null");
        }
        if (!(min.getClass().isAssignableFrom(propertyValue.getClass()))) {
            throw new ConstraintViolationException(
                "Value to check is not comparable to range type, value type [" + propertyValue.getClass() + "], range type [" + min.getClass() + "]");
        }
        if (min.compareTo(propertyValue) > 0 || max.compareTo(propertyValue) < 0) {
            throw new ConstraintViolationException("The value [" + propertyValue + "] is out of range " + inRange);
        }
    }

    @Override
    public ConstraintType getConstraintType() {
        return ConstraintType.IN_RANGE;
    }

    @Override
    public void validateValueOnUpdate(PropertyConstraint newConstraint) throws PropertyConstraintException {
    }

    @Override
    public String getErrorMessage(ToscaType toscaType, ConstraintFunctionalException e, String propertyName) {
        return getErrorMessage(toscaType, e, propertyName, "%s property value must be in a range of %s", String.valueOf(min),
            String.valueOf(max));
    }

    public boolean validateValueType(String propertyType) throws ConstraintValueDoNotMatchPropertyTypeException {
        ToscaType toscaType = ToscaType.getToscaType(propertyType);
        if (toscaType == null) {
            throw new ConstraintValueDoNotMatchPropertyTypeException(
                "inRange constraint has invalid values <" + inRange + "> property type is <" + propertyType + ">");
        }
        if (inRange == null) {
            throw new ConstraintValueDoNotMatchPropertyTypeException(
                "inRange constraint has invalid value <> property type is <" + propertyType + ">");
        }
        for (Object value : inRange) {
            if (!toscaType.isValueTypeValid(value)) {
                return false;
            }
        }
        return true;
    }

    public void changeConstraintValueTypeTo(String propertyType) throws ConstraintValueDoNotMatchPropertyTypeException {
        ToscaType toscaType = ToscaType.getToscaType(propertyType);
        try {
            inRange.replaceAll(obj -> toscaType.convert(String.valueOf(obj)));
        } catch (Exception e) {
            throw new ConstraintValueDoNotMatchPropertyTypeException(
                "inRange constraint has invalid values <" + inRange + "> property type is <" + propertyType + ">");
        }
    }
}
