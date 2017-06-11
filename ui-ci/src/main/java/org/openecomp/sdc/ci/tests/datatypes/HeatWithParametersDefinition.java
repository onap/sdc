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

package org.openecomp.sdc.ci.tests.datatypes;

import java.util.List;

import org.openecomp.sdc.be.datatypes.elements.HeatParameterDataDefinition;

public class HeatWithParametersDefinition {

	private String heatLabel;
	private String heatEnvLabel;
	private String heatArtifactType;
	private String heatEnvArtifactType;
	private String heatArtifactDisplayName;
	private List<HeatParameterDataDefinition> heatParameterDefinition;
	
	public HeatWithParametersDefinition(String heatLabel, String heatEnvLabel, String heatArtifactType, String heatEnvArtifactType, String heatArtifactDisplayName, List<HeatParameterDataDefinition> heatParameterDefinition) {
		super();
		this.heatLabel = heatLabel;
		this.heatEnvLabel = heatEnvLabel;
		this.heatArtifactType = heatArtifactType;
		this.heatEnvArtifactType = heatEnvArtifactType;
		this.heatArtifactDisplayName = heatArtifactDisplayName;
		this.heatParameterDefinition = heatParameterDefinition;
	}

	public String getHeatArtifactDisplayName() {
		return heatArtifactDisplayName;
	}

	public void setHeatArtifactDisplayName(String heatArtifactDisplayName) {
		this.heatArtifactDisplayName = heatArtifactDisplayName;
	}

	public String getHeatLabel() {
		return heatLabel;
	}

	public void setHeatLabel(String heatLabel) {
		this.heatLabel = heatLabel;
	}

	public String getHeatEnvLabel() {
		return heatEnvLabel;
	}

	public void setHeatEnvLabel(String heatEnvLabel) {
		this.heatEnvLabel = heatEnvLabel;
	}

	public String getHeatArtifactType() {
		return heatArtifactType;
	}

	public void setHeatArtifactType(String heatArtifactType) {
		this.heatArtifactType = heatArtifactType;
	}

	public String getHeatEnvArtifactType() {
		return heatEnvArtifactType;
	}

	public void setHeatEnvArtifactType(String heatEnvArtifactType) {
		this.heatEnvArtifactType = heatEnvArtifactType;
	}

	public List<HeatParameterDataDefinition> getHeatParameterDefinition() {
		return heatParameterDefinition;
	}

	public void setHeatParameterDefinition(List<HeatParameterDataDefinition> heatParameterDefinition) {
		this.heatParameterDefinition = heatParameterDefinition;
	}

	@Override
	public String toString() {
		return "HeatWithParametersDefinition [heatLabel=" + heatLabel + ", heatEnvLabel=" + heatEnvLabel + ", heatArtifactType=" + heatArtifactType + ", heatEnvArtifactType=" + heatEnvArtifactType + ", heatArtifactDisplayName="
				+ heatArtifactDisplayName + ", heatParameterDefinition=" + heatParameterDefinition + "]";
	}

	
	
}
