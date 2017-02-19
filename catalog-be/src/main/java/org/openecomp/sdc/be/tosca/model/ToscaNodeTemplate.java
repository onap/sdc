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

public class ToscaNodeTemplate {
	private String type;
	private ToscaMetadata metadata;
	private Map<String, Object> properties;
	private List<Map<String, ToscaTemplateRequirement>> requirements;
	private Map<String, ToscaTemplateCapability> capabilities;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Map<String, Object> getProperties() {
		return properties;
	}

	public void setProperties(Map<String, Object> properties) {
		this.properties = properties;
	}

	public List<Map<String, ToscaTemplateRequirement>> getRequirements() {
		return requirements;
	}

	public void setRequirements(List<Map<String, ToscaTemplateRequirement>> requirements) {
		this.requirements = requirements;
	}

	public Map<String, ToscaTemplateCapability> getCapabilities() {
		return capabilities;
	}

	public void setCapabilities(Map<String, ToscaTemplateCapability> capabilities) {
		this.capabilities = capabilities;
	}

	public ToscaMetadata getMetadata() {
		return metadata;
	}

	public void setMetadata(ToscaMetadata metadata) {
		this.metadata = metadata;
	}

}
