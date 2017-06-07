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

package org.openecomp.sdc.be.model;

import java.util.Map;

import org.openecomp.sdc.be.datatypes.elements.ArtifactDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.OperationDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;

/**
 * Defines an operation available to manage particular aspects of the Node Type.
 * 
 * @author esofer
 */
public class Operation extends OperationDataDefinition implements IOperationParameter {



	private boolean definition;

	/**
	 * <p>
	 * Jackson DeSerialization workaround constructor to create an operation
	 * with no arguments.
	 * </p>
	 * 
	 * @param emptyString
	 *            The empty string provided by jackson.
	 */
	public Operation() {
		super();
	}

	public Operation(OperationDataDefinition p) {
		super(p);
	}

	public Operation(ArtifactDataDefinition implementation, String description,
			Map<String, PropertyDataDefinition> inputs) {
		super(description);
		setImplementation(implementation);
		setInputs(inputs);
	}

	@Override
	public boolean isDefinition() {
		return false;
	}

	public void setDefinition(boolean definition) {
		this.definition = definition;
	}

	@Override
	public String toString() {
		return "Operation [definition=" + definition + "]";
	}

	public ArtifactDefinition getImplementationArtifact(){
		if ( getImplementation() != null ){
			return new ArtifactDefinition(getImplementation());
		}
		return null;
	}
}
