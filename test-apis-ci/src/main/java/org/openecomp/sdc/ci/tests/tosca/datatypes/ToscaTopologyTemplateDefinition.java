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
import java.util.stream.Collectors;

import org.yaml.snakeyaml.TypeDescription;

//	spec page 104	
public class ToscaTopologyTemplateDefinition {

	String description;
//	Map<String, Map<String, ToscaInputsDefinition>> inputs;
	Map<String,ToscaNodeTemplatesTopologyTemplateDefinition> node_templates = new HashMap<>();
//	Map<String,ToscaRelationshipTemplatesTopologyTemplateDefinition> relationship_templates;
	Map<String,ToscaGroupsTopologyTemplateDefinition> groups = new HashMap<>();
	Map<String, ToscaInputsTopologyTemplateDefinition> inputs = new HashMap<>();
//	Map<String,ToscaPoliciesTopologyTemplateDefinition> policies;
//	Map<String,ToscaOutputsTopologyTemplateDefinition> outputs;
	ToscaSubstitutionMappingsDefinition substitution_mappings;
	
	public ToscaTopologyTemplateDefinition() {
		super();
	}
	
	public ToscaTopologyTemplateDefinition(ToscaTopologyTemplateDefinition definition) {
		this.description = definition.description;
		this.node_templates = new HashMap<>(definition.node_templates);
		this.groups = new HashMap<String,ToscaGroupsTopologyTemplateDefinition>(definition.groups);
		this.inputs = definition.inputs.entrySet().stream().collect(Collectors.toMap(e -> e.getKey(), e -> new ToscaInputsTopologyTemplateDefinition(e.getValue())));
		this.substitution_mappings = definition.substitution_mappings;
	}
	
	public ToscaTopologyTemplateDefinition(String description, Map<String, ToscaNodeTemplatesTopologyTemplateDefinition> node_templates, Map<String, ToscaGroupsTopologyTemplateDefinition> groups,
		Map<String, ToscaInputsTopologyTemplateDefinition> inputs, ToscaSubstitutionMappingsDefinition substitution_mappings) {
	super();
	this.description = description;
	this.node_templates = node_templates;
	this.groups = groups;
	this.inputs = inputs;
	this.substitution_mappings = substitution_mappings;
}

	public Map<String, ToscaInputsTopologyTemplateDefinition> getInputs() {
		return inputs;
	}

	public void setInputs(Map<String, ToscaInputsTopologyTemplateDefinition> inputs) {
		this.inputs = inputs;
	}

	public void addInputs(Map<String, ToscaInputsTopologyTemplateDefinition> inputs) {
		this.inputs.putAll(inputs);
	}
	
	public ToscaSubstitutionMappingsDefinition getSubstitution_mappings() {
		return substitution_mappings;
	}
	public void setSubstitution_mappings(ToscaSubstitutionMappingsDefinition substitution_mappings) {
		this.substitution_mappings = substitution_mappings;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}


	public Map<String, ToscaNodeTemplatesTopologyTemplateDefinition> getNode_templates() {
		return node_templates;
	}

	public void setNode_templates(Map<String, ToscaNodeTemplatesTopologyTemplateDefinition> node_templates) {
		this.node_templates = node_templates;
	}

	public Map<String, ToscaGroupsTopologyTemplateDefinition> getGroups() {
		return groups;
	}

	public void setGroups(Map<String, ToscaGroupsTopologyTemplateDefinition> groups) {
		this.groups = groups;
	}

	
	//gets Type description for Yaml snake
	public static TypeDescription getTypeDescription(){
        TypeDescription typeDescription = new TypeDescription(ToscaTopologyTemplateDefinition.class);
        typeDescription.putMapPropertyType("inputs", String.class, ToscaInputsTopologyTemplateDefinition.class);
        typeDescription.putMapPropertyType("node_templates", String.class, ToscaNodeTemplatesTopologyTemplateDefinition.class);
        typeDescription.putMapPropertyType("groups", String.class, ToscaGroupsTopologyTemplateDefinition.class);
    	return typeDescription;
	}
	
	
}
