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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.openecomp.sdc.be.datatypes.elements.InterfaceDataDefinition;

/**
 * Definition of the operations that can be performed on (instances of) a Node
 * Type.
 * 
 * @author esofer
 */
public class InterfaceDefinition extends InterfaceDataDefinition implements IOperationParameter, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8220887972866354746L;

	/**
	 * Defines an operation available to manage particular aspects of the Node
	 * Type.
	 */
	private Map<String, Operation> operations = new HashMap<String, Operation>();

	private boolean definition;

	public InterfaceDefinition() {
		super();
		// TODO Auto-generated constructor stub
	}

	public InterfaceDefinition(String type, String description, Map<String, Operation> operations) {
		super(type, description);
		this.operations = operations;

	}

	public InterfaceDefinition(InterfaceDataDefinition p) {
		super(p);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean isDefinition() {
		// TODO Auto-generated method stub
		return false;
	}

	public void setDefinition(boolean definition) {
		this.definition = definition;
	}

	public Map<String, Operation> getOperations() {
		return operations;
	}

	public void setOperations(Map<String, Operation> operations) {
		this.operations = operations;
	}

	@Override
	public String toString() {
		return "InterfaceDefinition [operations=" + operations + ", definition=" + definition + "]";
	}

}
