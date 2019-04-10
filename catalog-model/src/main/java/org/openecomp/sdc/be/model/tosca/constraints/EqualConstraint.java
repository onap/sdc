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

import java.io.Serializable;

import org.openecomp.sdc.be.model.tosca.ToscaType;
import org.openecomp.sdc.be.model.tosca.constraints.exception.ConstraintFunctionalException;
import org.openecomp.sdc.be.model.tosca.constraints.exception.ConstraintValueDoNotMatchPropertyTypeException;
import org.openecomp.sdc.be.model.tosca.constraints.exception.ConstraintViolationException;

import javax.validation.constraints.NotNull;

@SuppressWarnings("serial")
public class EqualConstraint extends AbstractPropertyConstraint implements Serializable {

	@NotNull
	private String constraintValue;
    private Object typed;

	public EqualConstraint(String constraintValue) {
		super();
		this.constraintValue = constraintValue;
	}

	public String getConstraintValue() {
		return constraintValue;
	}

	@Override
	public void initialize(ToscaType propertyType) throws ConstraintValueDoNotMatchPropertyTypeException {
		if (propertyType.isValidValue(constraintValue)) {
			typed = propertyType.convert(constraintValue);
		} else {
            throw new ConstraintValueDoNotMatchPropertyTypeException(
                    "constraintValue constraint has invalid value <" + constraintValue
                            + "> property type is <" + propertyType.toString() + ">");
		}
	}

    @Override
    public void validate(Object propertyValue) throws ConstraintViolationException {
        if (propertyValue == null) {
            if (typed != null) {
                fail(null);
            }
        } else if (typed == null) {
            fail(propertyValue);
        } else if (typed instanceof Comparable && typed != propertyValue) {
            fail(propertyValue);
        } else if (!typed.equals(propertyValue)) {
            fail(propertyValue);
        }
    }

	private void fail(Object propertyValue) throws ConstraintViolationException {
		throw new ConstraintViolationException("Equal constraint violation, the reference is <" + constraintValue
													   + "> but the value to compare is <" + propertyValue + ">");
	}

	@Override
	public String getErrorMessage(ToscaType toscaType, ConstraintFunctionalException e, String propertyName) {
	    return getErrorMessage(toscaType, e, propertyName, "%s property value must be %s", constraintValue);
    }
}
