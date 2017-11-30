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
	private Map<String, Object> attributes;
	private List<Map<String, Object>> requirements;
	private Object capabilities;
//	private List<Map<String, Object>> capabilities;
//	private Map<String, ToscaPropertiesNodeTemplatesDefinition> properties;
//	private Map<String, ToscaRequirementsNodeTemplatesDefinition> requirements;
//	private Map<String, ToscaCapabilitiesNodeTemplatesDefinition> capabilities;
	private Map<String, Object> interfaces;
	private Map<String, Object> artifacts;
	private Map<String, Object> node_filter;
	private String copy;
	private Map<String, String> metadata;


	public ToscaNodeTemplatesTopologyTemplateDefinition() {
		super();
	}
	
	public Object getCapabilities() {
		return capabilities;
	}
	
	public void setCapabilities(Object capabilities) {
		this.capabilities = capabilities;
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

	public Map<String, Object> getAttributes() {
		return attributes;
	}

	public void setAttributes(Map<String, Object> attributes) {
		this.attributes = attributes;
	}

	public List<Map<String, Object>> getRequirements() {
		return requirements;
	}

	public void setRequirements(List<Map<String, Object>> requirements) {
		this.requirements = requirements;
	}

	public Map<String, Object> getInterfaces() {
		return interfaces;
	}

	public void setInterfaces(Map<String, Object> interfaces) {
		this.interfaces = interfaces;
	}

	public Map<String, Object> getArtifacts() {
		return artifacts;
	}

	public void setArtifacts(Map<String, Object> artifacts) {
		this.artifacts = artifacts;
	}

	public Map<String, Object> getNode_filter() {
		return node_filter;
	}

	public void setNode_filter(Map<String, Object> node_filter) {
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
//        typeDescription.putMapPropertyType("capabilities",String.class, Object.class);
//        typeDescription.putListPropertyType("capabilities", Object.class);
    	typeDescription.putMapPropertyType("attributes", String.class, Object.class);
    	typeDescription.putMapPropertyType("interfaces", String.class, Object.class);
    	typeDescription.putMapPropertyType("artifacts", String.class, Object.class);
    	typeDescription.putMapPropertyType("node_filter", String.class, Object.class);
    	typeDescription.putMapPropertyType("metadata", String.class, String.class);
    	return typeDescription;
	}
	
	
}
