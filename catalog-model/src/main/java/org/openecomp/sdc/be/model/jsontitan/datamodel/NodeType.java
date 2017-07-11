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

package org.openecomp.sdc.be.model.jsontitan.datamodel;

import java.util.List;
import java.util.Map;

import org.openecomp.sdc.be.datatypes.elements.InterfaceDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ListCapabilityDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ListRequirementDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.MapPropertiesDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;

public class NodeType extends ToscaElement{

	public NodeType() {
		super(ToscaElementTypeEnum.NodeType);
	}

	private List<String> derivedFrom;
	private List<String> derivedList;
	
	private Map<String, PropertyDataDefinition> attributes;
	private Map<String, ListCapabilityDataDefinition> capabilties;
	private Map<String, MapPropertiesDataDefinition> capabiltiesProperties;
	private Map<String, ListRequirementDataDefinition> requirements;
	private Map<String, InterfaceDataDefinition> interfaceArtifacts;
	
	
	// will be used in future
	// private Map<String, Map<String, Object>> other;


	public List<String> getDerivedList() {
		return derivedList;
	}

	public void setDerivedList(List<String> derivedList) {
		this.derivedList = derivedList;
	}

	public List<String> getDerivedFrom() {
		return derivedFrom;
	}

	public void setDerivedFrom(List<String> derivedFrom) {
		this.derivedFrom = derivedFrom;
	}

	public Map<String, PropertyDataDefinition> getAttributes() {
		return attributes;
	}

	public void setAttributes(Map<String, PropertyDataDefinition> attributes) {
		this.attributes = attributes;
	}

	public Map<String, ListCapabilityDataDefinition> getCapabilties() {
		return capabilties;
	}

	public void setCapabilties(Map<String, ListCapabilityDataDefinition> capabilties) {
		this.capabilties = capabilties;
	}

	public Map<String, ListRequirementDataDefinition> getRequirements() {
		return requirements;
	}

	public void setRequirements(Map<String, ListRequirementDataDefinition> requirements) {
		this.requirements = requirements;
	}

	public Map<String, MapPropertiesDataDefinition> getCapabiltiesProperties() {
		return capabiltiesProperties;
	}

	public void setCapabiltiesProperties(Map<String, MapPropertiesDataDefinition> capabiltiesProperties) {
		this.capabiltiesProperties = capabiltiesProperties;
	}

	public Map<String, InterfaceDataDefinition> getInterfaceArtifacts() {
		return interfaceArtifacts;
	}

	public void setInterfaceArtifacts(Map<String, InterfaceDataDefinition> interfaceArtifacts) {
		this.interfaceArtifacts = interfaceArtifacts;
	}

}
