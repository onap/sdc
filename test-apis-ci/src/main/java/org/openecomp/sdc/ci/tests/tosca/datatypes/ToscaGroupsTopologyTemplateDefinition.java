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

//	spec page 102
public class ToscaGroupsTopologyTemplateDefinition {

	private String type; // required
	private String description;
	private Map<String, Object> properties;
	private Map<String, String> targets; // required
	private Map<String, Object> interfaces;
	private List<String> members;
//	private Map<String, String> metadata;
	private ToscaServiceGroupsMetadataDefinition metadata;

	public ToscaGroupsTopologyTemplateDefinition() {
		super();
	}

	public ToscaServiceGroupsMetadataDefinition getMetadata() {
		return metadata;
	}

	public void setMetadata(ToscaServiceGroupsMetadataDefinition metadata) {
		this.metadata = metadata;
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

	public Map<String, Object> getInterfaces() {
		return interfaces;
	}

	public void setInterfaces(Map<String, Object> interfaces) {
		this.interfaces = interfaces;
	}

	public List<String> getMembers() {
		return members;
	}

	public void setMembers(List<String> members) {
		this.members = members;
	}

//	public Map<String, String> getMetadata() {
//		return metadata;
//	}
//
//	public void setMetadata(Map<String, String> metadata) {
//		this.metadata = metadata;
//	}

	@Override
	public String toString() {
		return "ToscaGroupsTopologyTemplateDefinition [type=" + type + ", description=" + description + ", properties=" + properties + ", targets=" + targets + ", interfaces=" + interfaces + ", members=" + members + ", metadata=" + metadata
				+ "]";
	}

	//gets Type description for Yaml snake
	public static TypeDescription getTypeDescription(){
        TypeDescription typeDescription = new TypeDescription(ToscaGroupsTopologyTemplateDefinition.class);
        typeDescription.putMapPropertyType("properties", String.class, Object.class);
        typeDescription.putMapPropertyType("interfaces", String.class, Object.class);
        typeDescription.putMapPropertyType("targets", String.class, Object.class);
//        typeDescription.putMapPropertyType("metadata", String.class, String.class);
        typeDescription.putMapPropertyType("metadata", String.class, String.class);
        typeDescription.putListPropertyType("members", String.class);
    	return typeDescription;
	}

}
