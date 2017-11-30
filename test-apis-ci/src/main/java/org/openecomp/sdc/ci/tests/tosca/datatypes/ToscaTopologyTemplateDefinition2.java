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

//	spec page 104	
public class ToscaTopologyTemplateDefinition2 {

	String description;
	Map<String,Map<String, Object>> inputs;
	Map<String,Object> node_templates;
//	Map<String,ToscaRelationshipTemplatesTopologyTemplateDefinition> relationship_templates;
	Map<String,Object> groups;
//	Map<String,ToscaPoliciesTopologyTemplateDefinition> policies;
//	Map<String,ToscaOutputsTopologyTemplateDefinition> outputs;
//	Map<String,ToscaSubstitutionMappingsDefinition> substitution_mappings;
	
	public ToscaTopologyTemplateDefinition2() {
		super();
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}


	public Map<String, Map<String, Object>> getInputs() {
		return inputs;
	}

	public void setInputs(Map<String, Map<String, Object>> inputs) {
		this.inputs = inputs;
	}

	public Map<String, Object> getNode_templates() {
		return node_templates;
	}

	public void setNode_templates(Map<String, Object> node_templates) {
		this.node_templates = node_templates;
	}

	public Map<String, Object> getGroups() {
		return groups;
	}

	public void setGroups(Map<String, Object> groups) {
		this.groups = groups;
	}

	
	
	
}
