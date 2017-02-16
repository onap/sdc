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

/**
 * Exception thrown when the user defines invalid custom property constraint
 * 
 * @author mkv
 * 
 */
public class InvalidPropertyConstraintImplementationException extends ConstraintTechnicalException {

	private static final long serialVersionUID = 2797550944328544706L;

	public InvalidPropertyConstraintImplementationException(String message, Throwable cause) {
		super(message, cause);
	}

	public InvalidPropertyConstraintImplementationException(String message) {
		super(message);
	}
}
