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
import java.util.List;

import org.openecomp.sdc.be.datatypes.elements.AdditionalInfoParameterDataDefinition;

public class AdditionalInformationDefinition extends AdditionalInfoParameterDataDefinition implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5266684455492488001L;

	private String parentUniqueId;

	private List<AdditionalInfoParameterInfo> parameters;

	public AdditionalInformationDefinition() {
		super();
	}

	public AdditionalInformationDefinition(AdditionalInfoParameterDataDefinition p, String parentUniqueId,
			List<AdditionalInfoParameterInfo> parameters) {
		super(p);
		this.parentUniqueId = parentUniqueId;
		this.parameters = parameters;
	}

	public AdditionalInformationDefinition(AdditionalInformationDefinition pd) {
		this.setUniqueId(pd.getUniqueId());
		this.setCreationTime(pd.getCreationTime());
		this.setModificationTime(pd.getModificationTime());
		this.parentUniqueId = pd.parentUniqueId;
		this.parameters = pd.parameters;
	}

	public String getParentUniqueId() {
		return parentUniqueId;
	}

	public void setParentUniqueId(String parentUniqueId) {
		this.parentUniqueId = parentUniqueId;
	}

	public List<AdditionalInfoParameterInfo> getParameters() {
		return parameters;
	}

	public void setParameters(List<AdditionalInfoParameterInfo> parameters) {
		this.parameters = parameters;
	}

	@Override
	public String toString() {
		return "AdditionalInformationDefinition [parameters=" + parameters + ", parentUniqueId=" + parentUniqueId + " "
				+ super.toString() + "]";
	}

}
