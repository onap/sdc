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

package org.onap.sdc.backend.ci.tests.tosca.datatypes;

import org.yaml.snakeyaml.TypeDescription;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

//	spec page 102

public class ToscaGroupsTopologyTemplateDefinition extends ToscaServiceGroupsMetadataDefinition implements Serializable {

	public static final long serialVersionUID = -6373752349967949120L;
	public String type; // required
	public String description;
	//	private Map<String, String> properties;
	public ToscaGroupPropertyDefinition properties;
	public Map<String, String> targets; // required
	public Map<String, Object> interfaces;
	public List<String> members;
	//	private Map<String, String> metadata;
//	private ToscaServiceGroupsMetadataDefinition metadata;

	public ToscaGroupsTopologyTemplateDefinition() {
		super();
	}

//	public ToscaServiceGroupsMetadataDefinition getMetadata() {
//		return metadata;
//	}
//
	public void setMetadata(ToscaServiceGroupsMetadataDefinition metadata) {
		this.vfModuleModelCustomizationUUID = metadata.vfModuleModelCustomizationUUID;
		this.vfModuleModelInvariantUUID = metadata.vfModuleModelInvariantUUID;
		this.vfModuleModelName = metadata.vfModuleModelName;
		this.vfModuleModelUUID = metadata.vfModuleModelUUID;
		this.vfModuleModelVersion = metadata.vfModuleModelVersion;
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

//	public Map<String, String> getProperties() {
//		return properties;
//	}
//
//	public void setProperties(Map<String, String> properties) {
//		this.properties = properties;
//	}

	public ToscaGroupPropertyDefinition getProperties() {
		return properties;
	}

	public void setProperties(ToscaGroupPropertyDefinition properties) {
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

//	@Override
//	public String toString() {
//		return "ToscaGroupsTopologyTemplateDefinition [type=" + type + ", description=" + description + ", properties=" + properties + ", targets=" + targets + ", interfaces=" + interfaces + ", members=" + members + ", metadata=" + metadata
//				+ "]";
//	}


	@Override
	public String toString() {
		return "ToscaGroupsTopologyTemplateDefinition{" +
				"type='" + type + '\'' +
				", description='" + description + '\'' +
				", properties=" + properties +
				", targets=" + targets +
				", interfaces=" + interfaces +
				", members=" + members +
				'}';
	}


	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof ToscaGroupsTopologyTemplateDefinition)) return false;
		if (!super.equals(o)) return false;

		ToscaGroupsTopologyTemplateDefinition that = (ToscaGroupsTopologyTemplateDefinition) o;

//		if (type != null ? !type.equals(that.type) : that.type != null) return false;
//		if (description != null ? !description.equals(that.description) : that.description != null) return false;
//		if (properties != null ? !properties.equals(that.properties) : that.properties != null) return false;
//		if (targets != null ? !targets.equals(that.targets) : that.targets != null) return false;
//		if (interfaces != null ? !interfaces.equals(that.interfaces) : that.interfaces != null) return false;
//		return members != null ? members.equals(that.members) : that.members == null;

		return (properties != null ? properties.equals(that.properties) : false);
	}

	//gets Type description for Yaml snake
	public static TypeDescription getTypeDescription(){
		TypeDescription typeDescription = new TypeDescription(ToscaGroupsTopologyTemplateDefinition.class);
//        typeDescription.putMapPropertyType("properties", String.class, Object.class);
//		typeDescription.putListPropertyType("properties", ToscaGroupPropertyDefinition.class);
		typeDescription.putMapPropertyType("interfaces", String.class, Object.class);
		typeDescription.putMapPropertyType("targets", String.class, Object.class);
//        typeDescription.putMapPropertyType("metadata", String.class, String.class);
		typeDescription.putMapPropertyType("metadata", String.class, String.class);
		typeDescription.putListPropertyType("members", String.class);
		return typeDescription;
	}

}
