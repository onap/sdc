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

import com.fasterxml.jackson.annotation.JsonCreator;
import java.io.Serializable;
import org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields;
import org.openecomp.sdc.be.datatypes.tosca.ToscaDataDefinition;

public class OperationDataDefinition extends ToscaDataDefinition implements Serializable {


	@JsonCreator
	public OperationDataDefinition() {
		super();
	}

	public OperationDataDefinition(String description) {
		super();
		setDescription(description);
	}

	public OperationDataDefinition(OperationDataDefinition p) {
		setDescription(p.getDescription());
		setImplementation(p.getImplementation());
		setInputs(p.getInputs());
		setOutputs(p.getOutputs());
		setName(p.getName());
		setUniqueId(p.getUniqueId());
	}


	public String getUniqueId() {
		return (String) getToscaPresentationValue(JsonPresentationFields.UNIQUE_ID);
	}

	public void setUniqueId(String uniqueId) {
		setToscaPresentationValue(JsonPresentationFields.UNIQUE_ID, uniqueId);
	}


	public Long getCreationDate() {
		return (Long) getToscaPresentationValue(JsonPresentationFields.CREATION_DATE);
	}

	public void setCreationDate(Long creationDate) {
		setToscaPresentationValue(JsonPresentationFields.CREATION_DATE, creationDate);
	}

	public Long getLastUpdateDate() {
		return (Long) getToscaPresentationValue(JsonPresentationFields.LAST_UPDATE_DATE);
	}

	public void setLastUpdateDate(Long lastUpdateDate) {
		setToscaPresentationValue(JsonPresentationFields.LAST_UPDATE_DATE, lastUpdateDate);
	}

	public String getDescription() {
		return (String) getToscaPresentationValue(JsonPresentationFields.DESCRIPTION);
	}

	public void setDescription(String description) {
		setToscaPresentationValue(JsonPresentationFields.DESCRIPTION, description);
	}

	public ArtifactDataDefinition getImplementation() {
		return (ArtifactDataDefinition) getToscaPresentationValue(JsonPresentationFields.OPERATION_IMPLEMENTATION);
	}

	public void setImplementation(ArtifactDataDefinition implementation) {
		setToscaPresentationValue(JsonPresentationFields.OPERATION_IMPLEMENTATION, implementation);
	}

	public ListDataDefinition<OperationInputDefinition> getInputs() {
		return (ListDataDefinition<OperationInputDefinition>)
			getToscaPresentationValue(JsonPresentationFields.OPERATION_INPUT);
	}

	public void setInputs(ListDataDefinition<OperationInputDefinition> inputs) {
		setToscaPresentationValue(JsonPresentationFields.OPERATION_INPUT,inputs);
	}

	public ListDataDefinition<OperationOutputDefinition> getOutputs() {
		return (ListDataDefinition<OperationOutputDefinition>)
				getToscaPresentationValue(JsonPresentationFields.OPERATION_OUTPUT);
	}

	public void setOutputs(ListDataDefinition<OperationOutputDefinition> outputs) {
		setToscaPresentationValue(JsonPresentationFields.OPERATION_OUTPUT, outputs);
	}
	public String getName() {
		return (String) getToscaPresentationValue(JsonPresentationFields.NAME);
	}

	public void setName(String name) {
		setToscaPresentationValue(JsonPresentationFields.NAME, name);
	}
}
