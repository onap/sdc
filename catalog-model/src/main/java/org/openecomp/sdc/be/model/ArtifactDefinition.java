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

import org.openecomp.sdc.be.datatypes.elements.ArtifactDataDefinition;

public class ArtifactDefinition extends ArtifactDataDefinition implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8323923665449071631L;

	/**
	 * Base64 encoded Artifact file data
	 */
	private byte[] payloadData;

	private List<HeatParameterDefinition> heatParameters;

	private String generatedFromId;

	public byte[] getPayloadData() {
		return payloadData;
	}

	public void setPayload(byte[] payloadData) {
		this.payloadData = payloadData;
	}

	public void setPayloadData(String payloadData) {
		if (payloadData != null) {
			this.payloadData = payloadData.getBytes();
		}
	}

	public ArtifactDefinition() {
		super();
	}

	public ArtifactDefinition(ArtifactDataDefinition a) {
		super(a);

	}

	public ArtifactDefinition(ArtifactDataDefinition a, String payloadData) {
		super(a);
		setPayloadData(payloadData);
	}

	public List<HeatParameterDefinition> getHeatParameters() {
		return heatParameters;
	}

	public void setHeatParameters(List<HeatParameterDefinition> properties) {
		this.heatParameters = properties;
	}

	public String getGeneratedFromId() {
		return generatedFromId;
	}

	public void setGeneratedFromId(String generatedFromId) {
		this.generatedFromId = generatedFromId;
	}

	public boolean checkEsIdExist() {
		if ((getEsId() != null) && (!getEsId().trim().isEmpty())) {
			return true;
		}
		return false;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((generatedFromId == null) ? 0 : generatedFromId.hashCode());
		result = prime * result + ((heatParameters == null) ? 0 : heatParameters.hashCode());
		result = prime * result + ((payloadData == null) ? 0 : payloadData.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		ArtifactDefinition other = (ArtifactDefinition) obj;
		if (generatedFromId == null) {
			if (other.generatedFromId != null)
				return false;
		} else if (!generatedFromId.equals(other.generatedFromId))
			return false;
		if (heatParameters == null) {
			if (other.heatParameters != null)
				return false;
		} else if (heatParameters.size() != other.heatParameters.size())
			return false;
		else {
			for (HeatParameterDefinition heatParam : heatParameters) {
				if (!other.heatParameters.contains(heatParam)) {
					return false;
				}
			}
		}
		if (payloadData == null) {
			if (other.payloadData != null)
				return false;
		} else if (!payloadData.equals(other.payloadData))
			return false;
		return true;
	}
}
