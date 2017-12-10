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

import java.util.HashMap;
import java.util.Map;

import org.yaml.snakeyaml.TypeDescription;

public class ToscaSubstitutionMappingsDefinition {

	private String node_type;
	private Map<String, Object> requirements = new HashMap<>();
	private Map<String, Object> capabilities = new HashMap<>();

	public ToscaSubstitutionMappingsDefinition() {
		super();
	}
	
	public String getNode_type() {
		return node_type;
	}

	public void setNode_type(String node_type) {
		this.node_type = node_type;
	}

	public Map<String, Object> getRequirements() {
		return requirements;
	}

	public void setRequirements(Map<String, Object> requirements) {
		this.requirements = requirements;
	}

	public Map<String, Object> getCapabilities() {
		return capabilities;
	}

	public void setCapabilities(Map<String, Object> capabilities) {
		this.capabilities = capabilities;
	}

//	gets Type description for Yaml snake
	public static TypeDescription getTypeDescription(){
        TypeDescription typeDescription = new TypeDescription(ToscaSubstitutionMappingsDefinition.class);
        typeDescription.putMapPropertyType("requirements", String.class, Object.class);
        typeDescription.putMapPropertyType("capabilities", String.class, Object.class);
    	return typeDescription;
	}
	
	
}
