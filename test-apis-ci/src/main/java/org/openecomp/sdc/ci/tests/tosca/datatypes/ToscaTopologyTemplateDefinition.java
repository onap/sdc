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
import java.util.Map;

import org.yaml.snakeyaml.TypeDescription;

//	spec page 104	
public class ToscaTopologyTemplateDefinition {

	String description;
	Map<String, Object> inputs;
	Map<String,ToscaNodeTemplatesTopologyTemplateDefinition> node_templates;
//	Map<String,ToscaRelationshipTemplatesTopologyTemplateDefinition> relationship_templates;
	Map<String,ToscaGroupsTopologyTemplateDefinition> groups;
//	Map<String,ToscaPoliciesTopologyTemplateDefinition> policies;
//	Map<String,ToscaOutputsTopologyTemplateDefinition> outputs;
	ToscaSubstitutionMappingsDefinition substitution_mappings;
	
	public ToscaTopologyTemplateDefinition() {
		super();
		// TODO Auto-generated constructor stub
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

	public Map<String, Object> getInputs() {
		return inputs;
	}

	public void setInputs(Map<String, Object> inputs) {
		this.inputs = inputs;
	}

	public Map<String, ToscaNodeTemplatesTopologyTemplateDefinition> getNode_templates() {
		return node_templates;
	}

	public void setNode_templates(Map<String, ToscaNodeTemplatesTopologyTemplateDefinition> node_templates) {
		this.node_templates = node_templates;
	}

//	public Map<String, ToscaRelationshipTemplatesTopologyTemplateDefinition> getRelationship_templates() {
//		return relationship_templates;
//	}
//
//	public void setRelationship_templates(Map<String, ToscaRelationshipTemplatesTopologyTemplateDefinition> relationship_templates) {
//		this.relationship_templates = relationship_templates;
//	}

	public Map<String, ToscaGroupsTopologyTemplateDefinition> getGroups() {
		return groups;
	}

	public void setGroups(Map<String, ToscaGroupsTopologyTemplateDefinition> groups) {
		this.groups = groups;
	}
//
//	public Map<String, ToscaPoliciesTopologyTemplateDefinition> getPolicies() {
//		return policies;
//	}
//
//	public void setPolicies(Map<String, ToscaPoliciesTopologyTemplateDefinition> policies) {
//		this.policies = policies;
//	}
//
//	public Map<String, ToscaOutputsTopologyTemplateDefinition> getOutputs() {
//		return outputs;
//	}
//
//	public void setOutputs(Map<String, ToscaOutputsTopologyTemplateDefinition> outputs) {
//		this.outputs = outputs;
//	}
//
//	public Map<String, ToscaSubstitutionMappingsDefinition> getSubstitution_mappings() {
//		return substitution_mappings;
//	}
//
//	public void setSubstitution_mappings(Map<String, ToscaSubstitutionMappingsDefinition> substitution_mappings) {
//		this.substitution_mappings = substitution_mappings;
//	}

//	@Override
//	public String toString() {
//		return "ToscaTopologyTemplateDefinition [description=" + description + ", inputs=" + inputs + ", node_templates=" + node_templates + ", relationship_templates=" + relationship_templates + ", groups=" + groups + ", policies="
//				+ policies + ", outputs=" + outputs + ", substitution_mappings=" + substitution_mappings + "]";
//	}

	//gets Type description for Yaml snake
	public static TypeDescription getTypeDescription(){
        TypeDescription typeDescription = new TypeDescription(ToscaTopologyTemplateDefinition.class);
        typeDescription.putMapPropertyType("inputs", String.class, Object.class);
        typeDescription.putMapPropertyType("node_templates", String.class, ToscaNodeTemplatesTopologyTemplateDefinition.class);
        typeDescription.putMapPropertyType("groups", String.class, ToscaGroupsTopologyTemplateDefinition.class);
    	return typeDescription;
	}
	
	
}
