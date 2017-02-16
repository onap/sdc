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

package org.openecomp.sdc.be.tosca.model;

import java.util.List;
import java.util.Map;

public class ToscaNodeType {
	public ToscaNodeType() {
	}

	private ToscaMetadata metadata;
	private String derived_from;
	private String description;

	private Map<String, ToscaProperty> properties;
	private Map<String, ToscaCapability> capabilities;

	private List<Map<String, ToscaRequirement>> requirements;

	public Map<String, ToscaProperty> getProperties() {
		return properties;
	}

	public void setProperties(Map<String, ToscaProperty> properties) {
		this.properties = properties;
	}

	public Map<String, ToscaCapability> getCapabilities() {
		return capabilities;
	}

	public void setCapabilities(Map<String, ToscaCapability> capabilities) {
		this.capabilities = capabilities;
	}

	public List<Map<String, ToscaRequirement>> getRequirements() {
		return requirements;
	}

	public void setRequirements(List<Map<String, ToscaRequirement>> requirements) {
		this.requirements = requirements;
	}

	public String getDerived_from() {
		return derived_from;
	}

	public void setDerived_from(String derived_from) {
		this.derived_from = derived_from;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public ToscaMetadata getMetadata() {
		return metadata;
	}

	public void setMetadata(ToscaMetadata metadata) {
		this.metadata = metadata;
	}

}
