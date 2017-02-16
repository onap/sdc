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

public abstract class AbstractStringPropertyConstraint extends AbstractPropertyConstraint implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6857605164938136232L;

	protected abstract void doValidate(String propertyValue) throws ConstraintViolationException;

	@Override
	public void validate(Object propertyValue) throws ConstraintViolationException {
		if (propertyValue == null) {
			throw new ConstraintViolationException("Value to validate is null");
		}
		if (!(propertyValue instanceof String)) {
			throw new ConstraintViolationException("This constraint can only be applied on String value");
		}
		doValidate((String) propertyValue);
	}

	@Override
	public void initialize(ToscaType propertyType) throws ConstraintValueDoNotMatchPropertyTypeException {
		ConstraintUtil.checkStringType(propertyType);
	}
}
