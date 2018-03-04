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

package org.openecomp.sdc.be.datatypes.elements;

import org.openecomp.sdc.be.datatypes.tosca.ToscaDataDefinition;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class InterfaceDataDefinition extends ToscaDataDefinition implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2208369368489725049L;

	private String type;

	private String description;

	private String uniqueId;

	/**
	 * Timestamp of the resource (artifact) creation
	 */
	private Long creationDate;

	/**
	 * Timestamp of the last resource (artifact) creation
	 */
	private Long lastUpdateDate;
	/**
	 * Defines an operation available to manage particular aspects of the Node
	 * Type.
	 */
	private Map<String, OperationDataDefinition> operations = new HashMap<String, OperationDataDefinition>();
	
	public InterfaceDataDefinition() {
		super();
	}

	public InterfaceDataDefinition(String type, String description) {
		super();
		this.type = type;
		this.description = description;

	}

	public InterfaceDataDefinition(InterfaceDataDefinition p) {
		this.uniqueId = p.uniqueId;
		this.type = p.type;
		this.description = p.description;

	}

	public String getUniqueId() {
		return uniqueId;
	}

	public void setUniqueId(String uniqueId) {
		this.uniqueId = uniqueId;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Long getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Long creationDate) {
		this.creationDate = creationDate;
	}

	public Long getLastUpdateDate() {
		return lastUpdateDate;
	}

	public void setLastUpdateDate(Long lastUpdateDate) {
		this.lastUpdateDate = lastUpdateDate;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	public Map<String, OperationDataDefinition> getOperations() {
		return operations;
	}

	public void setOperations(Map<String, OperationDataDefinition> operations) {
		this.operations = operations;
	}
}
