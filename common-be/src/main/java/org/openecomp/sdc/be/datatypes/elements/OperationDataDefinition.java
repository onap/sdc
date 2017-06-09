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

import java.io.Serializable;
import java.util.Map;

import org.openecomp.sdc.be.datatypes.tosca.ToscaDataDefinition;

public class OperationDataDefinition extends ToscaDataDefinition implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1951516966187326915L;

	private String uniqueId;

	/**
	 * Timestamp of the resource (artifact) creation
	 */
	private Long creationDate;

	/**
	 * Timestamp of the last resource (artifact) creation
	 */
	private Long lastUpdateDate;

	/** Description of the operation. */
	private String description;
	/** Implementation artifact for the interface. */
	private ArtifactDataDefinition implementation;

	/**
	 * This OPTIONAL property contains a list of one or more input parameter
	 * definitions.
	 */
	// @JsonDeserialize(contentUsing = OperationParameterDeserializer.class)
	private Map<String, PropertyDataDefinition> inputs;
	public OperationDataDefinition() {
		super();
	}

	public OperationDataDefinition(String description) {
		super();
		this.description = description;
	}

	public OperationDataDefinition(OperationDataDefinition p) {
		this.uniqueId = p.uniqueId;
		this.description = p.description;
		this.implementation = p.implementation;
		this.inputs = p.inputs;
	}

	public String getUniqueId() {
		return uniqueId;
	}

	public void setUniqueId(String uniqueId) {
		this.uniqueId = uniqueId;
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
	public ArtifactDataDefinition getImplementation() {
		return implementation;
	}

	public void setImplementation(ArtifactDataDefinition implementation) {
		this.implementation = implementation;
	}

	public Map<String, PropertyDataDefinition> getInputs() {
		return inputs;
	}

	public void setInputs(Map<String, PropertyDataDefinition> inputs) {
		this.inputs = inputs;
	}
}
