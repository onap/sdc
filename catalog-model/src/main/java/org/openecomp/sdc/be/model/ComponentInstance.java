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
import java.util.Map;

import org.openecomp.sdc.be.datatypes.elements.ComponentInstanceDataDefinition;

public class ComponentInstance extends ComponentInstanceDataDefinition implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6721465693884621223L;

	private String icon;

	private String componentName;
	private String componentVersion;
	private String toscaComponentName;
	private Map<String, List<CapabilityDefinition>> capabilities;
	private Map<String, List<RequirementDefinition>> requirements;
	private Map<String, ArtifactDefinition> deploymentArtifacts;

	public ComponentInstance() {
		super();
	}

	public ComponentInstance(ComponentInstanceDataDefinition r) {
		super(r);
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public String getComponentName() {
		return componentName;
	}

	public void setComponentName(String resourceName) {
		this.componentName = resourceName;
	}

	public String getComponentVersion() {
		return componentVersion;
	}

	public String getToscaComponentName() {
		return toscaComponentName;
	}

	public void setToscaComponentName(String toscaComponentName) {
		this.toscaComponentName = toscaComponentName;
	}

	public void setComponentVersion(String resourceVersion) {
		this.componentVersion = resourceVersion;
	}

	public Map<String, List<CapabilityDefinition>> getCapabilities() {
		return capabilities;
	}

	public void setCapabilities(Map<String, List<CapabilityDefinition>> capabilities) {
		this.capabilities = capabilities;
	}

	public Map<String, List<RequirementDefinition>> getRequirements() {
		return requirements;
	}

	public void setRequirements(Map<String, List<RequirementDefinition>> requirements) {
		this.requirements = requirements;
	}

	public Map<String, ArtifactDefinition> getDeploymentArtifacts() {
		return deploymentArtifacts;
	}

	public void setDeploymentArtifacts(Map<String, ArtifactDefinition> deploymentArtifacts) {
		this.deploymentArtifacts = deploymentArtifacts;
	}

}
