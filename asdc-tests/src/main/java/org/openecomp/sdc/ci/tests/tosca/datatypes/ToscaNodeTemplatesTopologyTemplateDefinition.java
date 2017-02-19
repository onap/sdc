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

package org.openecomp.sdc.ci.tests.tosca.datatypes;

import java.util.List;

public class ToscaNodeTemplatesTopologyTemplateDefinition {

	String name;
	String type;
	List<ToscaPropertiesNodeTemplatesDefinition> properties;
	List<ToscaRequirementsNodeTemplatesDefinition> requirements;
	List<ToscaCapabilitiesNodeTemplatesDefinition> capabilities;

	public ToscaNodeTemplatesTopologyTemplateDefinition() {
		super();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public List<ToscaPropertiesNodeTemplatesDefinition> getProperties() {
		return properties;
	}

	public void setProperties(List<ToscaPropertiesNodeTemplatesDefinition> properties) {
		this.properties = properties;
	}

	public List<ToscaRequirementsNodeTemplatesDefinition> getRequirements() {
		return requirements;
	}

	public void setRequirements(List<ToscaRequirementsNodeTemplatesDefinition> requirements) {
		this.requirements = requirements;
	}

	public List<ToscaCapabilitiesNodeTemplatesDefinition> getCapabilities() {
		return capabilities;
	}

	public void setCapabilities(List<ToscaCapabilitiesNodeTemplatesDefinition> capabilities) {
		this.capabilities = capabilities;
	}

	@Override
	public String toString() {
		return "ToscaNodeTemplatesTopologyTemplateDefinition [name=" + name + ", type=" + type + ", properties="
				+ properties + ", requirements=" + requirements + ", capabilities=" + capabilities + "]";
	}

}
