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

import java.util.Map;

public class ToscaPoliciesTopologyTemplateDefinition {

	public ToscaPoliciesTopologyTemplateDefinition() {
		super();
	}
	
	private String type; // required
	private String description;
	private Map<String, Object> properties;
	private Map<String, String> targets;
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public Map<String, Object> getProperties() {
		return properties;
	}
	public void setProperties(Map<String, Object> properties) {
		this.properties = properties;
	}
	public Map<String, String> getTargets() {
		return targets;
	}
	public void setTargets(Map<String, String> targets) {
		this.targets = targets;
	}
	@Override
	public String toString() {
		return "ToscaPoliciesTopologyTemplateDefinition [type=" + type + ", description=" + description + ", properties=" + properties + ", targets=" + targets + "]";
	}
	
	
	
}

