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
import java.util.Map;
import java.util.stream.Collectors;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.openecomp.sdc.be.datatypes.elements.InterfaceDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.OperationDataDefinition;

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

	

	private boolean definition;

	public InterfaceDefinition() {
		super();
	}

	public InterfaceDefinition(String type, String description, Map<String, Operation> operations) {
		super(type, description);
		setOperationsMap(operations);
	}

	public InterfaceDefinition(InterfaceDataDefinition p) {
		super(p);
	}

	@Override
	public boolean isDefinition() {
		// TODO Auto-generated method stub
		return false;
	}

	public void setDefinition(boolean definition) {
		this.definition = definition;
	}
	@JsonIgnore
	public Map<String, Operation> getOperationsMap() {
		Map<String, Operation> convertedOperation = getOperations().entrySet().stream().collect(Collectors.toMap(e -> e.getKey(), e -> new Operation(e.getValue())));
		return convertedOperation;
	}
	@JsonIgnore
	public void setOperationsMap(Map<String, Operation> operations) {
		Map<String, OperationDataDefinition> convertedOperation = operations.entrySet().stream().collect(Collectors.toMap(e -> e.getKey(), e -> new OperationDataDefinition(e.getValue())));
		setOperations(convertedOperation);
	}

	@Override
	public String toString() {
		return "InterfaceDefinition [definition=" + definition + "]";
	}

}
