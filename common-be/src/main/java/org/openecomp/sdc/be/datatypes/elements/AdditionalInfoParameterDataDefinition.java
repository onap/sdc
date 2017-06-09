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
import java.util.List;

import org.openecomp.sdc.be.datatypes.tosca.ToscaDataDefinition;

public class AdditionalInfoParameterDataDefinition extends ToscaDataDefinition implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -565365728516901670L;

	private String uniqueId;

	private Long creationTime;

	private Long modificationTime;

	private Integer lastCreatedCounter = 0;

	private List<AdditionalInfoParameterInfo> parameters;

	public AdditionalInfoParameterDataDefinition() {

	}

	public AdditionalInfoParameterDataDefinition(AdditionalInfoParameterDataDefinition p) {
		this.uniqueId = p.uniqueId;
		this.creationTime = p.creationTime;
		this.modificationTime = p.modificationTime;
		this.lastCreatedCounter = p.lastCreatedCounter;
		this.parameters = p.parameters;
	}

	public String getUniqueId() {
		return uniqueId;
	}

	public void setUniqueId(String uniqueId) {
		this.uniqueId = uniqueId;
	}

	public Long getCreationTime() {
		return creationTime;
	}

	public void setCreationTime(Long creationTime) {
		this.creationTime = creationTime;
	}

	public Long getModificationTime() {
		return modificationTime;
	}

	public void setModificationTime(Long modificationTime) {
		this.modificationTime = modificationTime;
	}

	public Integer getLastCreatedCounter() {
		return lastCreatedCounter;
	}

	public void setLastCreatedCounter(Integer lastCreatedCounter) {
		this.lastCreatedCounter = lastCreatedCounter;
	}
	public List<AdditionalInfoParameterInfo> getParameters() {
		return parameters;
	}

	public void setParameters(List<AdditionalInfoParameterInfo> parameters) {
		this.parameters = parameters;
	}
	@Override
	public String toString() {
		return "AdditionalInfoParameterDataDefinition [uniqueId=" + uniqueId + ", creationTime=" + creationTime
				+ ", modificationTime=" + modificationTime + ", lastCreatedCounter=" + lastCreatedCounter + "]";
	}

}
