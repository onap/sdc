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
import org.openecomp.sdc.be.model.tosca.constraints.exception.ConstraintValueDoNotMatchPropertyTypeException;
import org.openecomp.sdc.be.model.tosca.constraints.exception.ConstraintViolationException;

@SuppressWarnings("rawtypes")
public abstract class AbstractComparablePropertyConstraint extends AbstractPropertyConstraint implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2002627754053326321L;

	private Comparable comparable;

	protected Comparable getComparable() {
		return comparable;
	}

	protected void initialize(String rawTextValue, ToscaType propertyType)
			throws ConstraintValueDoNotMatchPropertyTypeException {
		// Perform verification that the property type is supported for
		// comparison
		ConstraintUtil.checkComparableType(propertyType);
		// Check if the text value is valid for the property type
		if (propertyType.isValidValue(rawTextValue)) {
			// Convert the raw text value to a comparable value
			comparable = ConstraintUtil.convertToComparable(propertyType, rawTextValue);
		} else {
			// Invalid value throw exception
			throw new ConstraintValueDoNotMatchPropertyTypeException(
					"The value [" + rawTextValue + "] is not valid for the type [" + propertyType + "]");
		}
	}

	protected abstract void doValidate(Object propertyValue) throws ConstraintViolationException;

	@Override
	public void validate(Object propertyValue) throws ConstraintViolationException {
		if (propertyValue == null) {
			throw new ConstraintViolationException("Value to check is null");
		}
		if (!(comparable.getClass().isAssignableFrom(propertyValue.getClass()))) {
			throw new ConstraintViolationException("Value to check is not comparable to reference type, value type ["
					+ propertyValue.getClass() + "], reference type [" + comparable.getClass() + "]");
		}
		doValidate(propertyValue);
	}
}
