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
import org.openecomp.sdc.be.datatypes.elements.InputsValueDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.OperationDataDefinition;

/**
 * Defines an operation available to manage particular aspects of the Node Type.
 * 
 * @author esofer
 */
public class Operation extends OperationDataDefinition implements IOperationParameter {

	/** Implementation artifact for the interface. */
	private ArtifactDefinition implementation;

	/**
	 * This OPTIONAL property contains a list of one or more input parameter
	 * definitions.
	 */
	// @JsonDeserialize(contentUsing = OperationParameterDeserializer.class)
	private Map<String, PropertyValueDefinition> inputs;

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
		// TODO Auto-generated constructor stub
	}

	public Operation(OperationDataDefinition p) {
		super(p);
		// TODO Auto-generated constructor stub
	}

	public Operation(ArtifactDataDefinition implementation, String description,
			Map<String, InputsValueDataDefinition> inputs) {
		super(description);

	}

	@Override
	public boolean isDefinition() {
		// TODO Auto-generated method stub
		return false;
	}

	public void setDefinition(boolean definition) {
		this.definition = definition;
	}

	public ArtifactDefinition getImplementation() {
		return implementation;
	}

	public void setImplementation(ArtifactDefinition implementation) {
		this.implementation = implementation;
	}

	public Map<String, PropertyValueDefinition> getInputs() {
		return inputs;
	}

	public void setInputs(Map<String, PropertyValueDefinition> inputs) {
		this.inputs = inputs;
	}

	@Override
	public String toString() {
		return "Operation [implementation=" + implementation + ", inputs=" + inputs + ", definition=" + definition
				+ "]";
	}

}
