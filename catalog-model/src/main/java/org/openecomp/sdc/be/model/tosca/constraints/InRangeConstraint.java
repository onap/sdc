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

import com.google.common.collect.Lists;

import java.util.List;

import org.openecomp.sdc.be.model.tosca.ToscaType;
import org.openecomp.sdc.be.model.tosca.constraints.exception.ConstraintFunctionalException;
import org.openecomp.sdc.be.model.tosca.constraints.exception.ConstraintValueDoNotMatchPropertyTypeException;
import org.openecomp.sdc.be.model.tosca.constraints.exception.ConstraintViolationException;

import javax.validation.constraints.NotNull;

public class InRangeConstraint extends AbstractPropertyConstraint {

    private List<String> inRange;

    private Comparable min;
    private Comparable max;

    public InRangeConstraint(List<String> inRange) {
        this.inRange = inRange;
    }

    public InRangeConstraint() { }

    @Override
    public void initialize(ToscaType propertyType) throws ConstraintValueDoNotMatchPropertyTypeException {
        // Perform verification that the property type is supported for
        // comparison
        ConstraintUtil.checkComparableType(propertyType);
        if (inRange == null || inRange.size() != 2) {
            throw new ConstraintValueDoNotMatchPropertyTypeException("In range constraint must have two elements.");
        }
        String minRawText = inRange.get(0);
        String maxRawText = inRange.get(1);
        if (!propertyType.isValidValue(minRawText)) {
            throw new ConstraintValueDoNotMatchPropertyTypeException("Invalid min value for in range constraint ["
                    + minRawText + "] as it does not follow the property type [" + propertyType + "]");
        }
        if (!propertyType.isValidValue(maxRawText)) {
            throw new ConstraintValueDoNotMatchPropertyTypeException("Invalid max value for in range constraint ["
                    + maxRawText + "] as it does not follow the property type [" + propertyType + "]");
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
            throw new ConstraintViolationException("Value to check is not comparable to range type, value type ["
                    + propertyValue.getClass() + "], range type [" + min.getClass() + "]");
        }
        if (min.compareTo(propertyValue) > 0 || max.compareTo(propertyValue) < 0) {
            throw new ConstraintViolationException("The value [" + propertyValue + "] is out of range " + inRange);
        }
    }

    @NotNull
    public String getRangeMinValue() {
        if (inRange != null) {
            return inRange.get(0);
        } else {
            return null;
        }
    }

    public void setRangeMinValue(String minValue) {
        if (inRange == null) {
            inRange = Lists.newArrayList(minValue, "");
        } else {
            inRange.set(0, minValue);
        }
    }

    @NotNull
    public String getRangeMaxValue() {
        if (inRange != null) {
            return inRange.get(1);
        } else {
            return null;
        }
    }

    public void setRangeMaxValue(String maxValue) {
        if (inRange == null) {
            inRange = Lists.newArrayList("", maxValue);
        } else {
            inRange.set(1, maxValue);
        }
    }

    @Override
    public String getErrorMessage(ToscaType toscaType, ConstraintFunctionalException e, String propertyName) {
        return getErrorMessage(toscaType, e, propertyName, "%f property value must be between >= [%s] and <= [%s]",
                String.valueOf(min), String.valueOf(max));
    }

}
