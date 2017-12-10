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

package org.openecomp.sdc.be.model.tosca.constraints.exception;

import org.openecomp.sdc.be.model.tosca.constraints.ConstraintUtil.ConstraintInformation;

/**
 * Exception to be thrown when a constraint definition is invalid because the
 * specified value doesn't match the property type.
 * 
 * @author esofer
 */
public class ConstraintValueDoNotMatchPropertyTypeException extends ConstraintFunctionalException {

	private static final long serialVersionUID = 4342613849660957651L;

	public ConstraintValueDoNotMatchPropertyTypeException(String message) {
		super(message);
	}

	public ConstraintValueDoNotMatchPropertyTypeException(String message, Throwable cause) {
		super(message, cause);
	}

	public ConstraintValueDoNotMatchPropertyTypeException(String message, Throwable cause,
			ConstraintInformation constraintInformation) {
		super(message, cause);
		this.constraintInformation = constraintInformation;
	}
}
