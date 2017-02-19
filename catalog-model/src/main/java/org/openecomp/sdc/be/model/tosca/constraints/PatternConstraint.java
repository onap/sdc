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
import java.util.regex.Pattern;

import javax.validation.constraints.NotNull;

import org.openecomp.sdc.be.model.tosca.constraints.exception.ConstraintViolationException;

//import com.fasterxml.jackson.annotation.JsonIgnore;

public class PatternConstraint extends AbstractStringPropertyConstraint implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8708185294968697107L;

	@NotNull
	private String pattern;

	// @JsonIgnore
	private Pattern compiledPattern;

	public void setPattern(String pattern) {
		this.pattern = pattern;
		this.compiledPattern = Pattern.compile(this.pattern);
	}

	@Override
	protected void doValidate(String propertyValue) throws ConstraintViolationException {
		if (!compiledPattern.matcher(propertyValue).matches()) {
			throw new ConstraintViolationException("The value do not match pattern " + pattern);
		}
	}
}
