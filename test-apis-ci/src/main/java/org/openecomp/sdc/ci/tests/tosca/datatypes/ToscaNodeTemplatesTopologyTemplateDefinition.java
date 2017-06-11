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

public class ToscaNodeTemplatesTopologyTemplateDefinition {

	private String name;
	private String type;
	private String description;
	private String [] directives;
	private Map<String, Object> properties;
	private List<Map<String, Object>> attributes;
	private List<Map<String, Object>> requirements;
	private List<Map<String, Object>> capabilities;
	private List<Map<String, Object>> interfaces;
	private List<Map<String, Object>> artifacts;
	private List<Map<String, Object>> node_filter;
	private String copy;
	private Map<String, String> metadata;

	
//	private Map<String, ToscaPropertiesNodeTemplatesDefinition> properties;
//	private Map<String, ToscaRequirementsNodeTemplatesDefinition> requirements;
//	private Map<String, ToscaCapabilitiesNodeTemplatesDefinition> capabilities;

	public ToscaNodeTemplatesTopologyTemplateDefinition() {
		super();
	}
	
	public Map<String, String> getMetadata() {
		return metadata;
	}

	public void setMetadata(Map<String, String> metadata) {
		this.metadata = metadata;
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

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String[] getDirectives() {
		return directives;
	}

	public void setDirectives(String[] directives) {
		this.directives = directives;
	}


	public Map<String, Object> getProperties() {
		return properties;
	}

	public void setProperties(Map<String, Object> properties) {
		this.properties = properties;
	}

	public List<Map<String, Object>> getAttributes() {
		return attributes;
	}

	public void setAttributes(List<Map<String, Object>> attributes) {
		this.attributes = attributes;
	}

	public List<Map<String, Object>> getRequirements() {
		return requirements;
	}

	public void setRequirements(List<Map<String, Object>> requirements) {
		this.requirements = requirements;
	}

	public List<Map<String, Object>> getCapabilities() {
		return capabilities;
	}

	public void setCapabilities(List<Map<String, Object>> capabilities) {
		this.capabilities = capabilities;
	}

	public List<Map<String, Object>> getInterfaces() {
		return interfaces;
	}

	public void setInterfaces(List<Map<String, Object>> interfaces) {
		this.interfaces = interfaces;
	}

	public List<Map<String, Object>> getArtifacts() {
		return artifacts;
	}

	public void setArtifacts(List<Map<String, Object>> artifacts) {
		this.artifacts = artifacts;
	}

	public List<Map<String, Object>> getNode_filter() {
		return node_filter;
	}

	public void setNode_filter(List<Map<String, Object>> node_filter) {
		this.node_filter = node_filter;
	}

	public String getCopy() {
		return copy;
	}

	public void setCopy(String copy) {
		this.copy = copy;
	}
	
	//gets Type description for Yaml snake
	public static TypeDescription getTypeDescription(){
        TypeDescription typeDescription = new TypeDescription(ToscaNodeTemplatesTopologyTemplateDefinition.class);
        typeDescription.putMapPropertyType("properties", String.class, Object.class);
        typeDescription.putListPropertyType("requirements", Map.class);
        typeDescription.putListPropertyType("capabilities", Map.class);
    	typeDescription.putListPropertyType("attributes", Map.class);
    	typeDescription.putListPropertyType("interfaces", Map.class);
    	typeDescription.putListPropertyType("artifacts", Map.class);
    	typeDescription.putListPropertyType("node_filter", Map.class);
    	typeDescription.putMapPropertyType("metadata", String.class, String.class);
    	return typeDescription;
	}
	
	
}
