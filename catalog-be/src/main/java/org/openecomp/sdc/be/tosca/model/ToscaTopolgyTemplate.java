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

import java.util.HashMap;
import java.util.Map;

public class ToscaTopolgyTemplate {
	private Map<String, ToscaProperty> inputs;
	private Map<String, ToscaNodeTemplate> node_templates;
	private Map<String, ToscaGroupTemplate> groups;
	private SubstitutionMapping substitution_mappings;

	public Map<String, ToscaNodeTemplate> getNode_templates() {
		return node_templates;
	}

	public void setNode_templates(Map<String, ToscaNodeTemplate> node_templates) {
		this.node_templates = node_templates;
	}

	public Map<String, ToscaGroupTemplate> getGroups() {
		return groups;
	}

	public void addGroups(Map<String, ToscaGroupTemplate> groups) {
		if ( this.groups == null ){
			this.groups = new HashMap<>();
		}
		this.groups.putAll(groups);
	}

	public SubstitutionMapping getSubstitution_mappings() {
		return substitution_mappings;
	}

	public void setSubstitution_mappings(SubstitutionMapping substitution_mapping) {
		this.substitution_mappings = substitution_mapping;
	}

	public Map<String, ToscaProperty> getInputs() {
		return inputs;
	}

	public void setInputs(Map<String, ToscaProperty> inputs) {
		this.inputs = inputs;
	}

}
