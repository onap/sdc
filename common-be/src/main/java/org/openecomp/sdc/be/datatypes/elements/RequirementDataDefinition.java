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

package org.openecomp.sdc.be.datatypes.elements;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields;
import org.openecomp.sdc.be.datatypes.tosca.ToscaDataDefinition;

import com.google.common.collect.Lists;
/**
 * Represents the requirement of the component or component instance
 */
public class RequirementDataDefinition extends ToscaDataDefinition implements Serializable {
	/**
	 * 
	 */
	public static final String MIN_OCCURRENCES = "1";
	public static final String MAX_OCCURRENCES = "UNBOUNDED";
	public static final String MAX_DEFAULT_OCCURRENCES = "1";

	private static final long serialVersionUID = -8840549489409274532L;
	/**
	 * The default constructor initializing limits of the occurrences
	 */
	public RequirementDataDefinition() {
		super();
		this.setMinOccurrences(  MIN_OCCURRENCES );
		this.setMaxOccurrences(  MAX_OCCURRENCES);
		this.setLeftOccurrences(  MAX_OCCURRENCES);
	}
	/**
	 * Deep copy constructor
	 * @param other
	 */
	public RequirementDataDefinition(RequirementDataDefinition other) {
		this.setUniqueId(other.getUniqueId());
		this.setName(other.getName());
		this.setParentName(other.getParentName());
		this.setCapability(other.getCapability());
		this.setNode(other.getNode());
		this.setRelationship(other.getRelationship());
		this.setOwnerId(other.getOwnerId());
		this.setOwnerName(other.getOwnerName());
		this.setMinOccurrences(other.getMinOccurrences());
		this.setMaxOccurrences(other.getMaxOccurrences());
		this.setLeftOccurrences(other.getLeftOccurrences());
		if(other.getPath() == null)
			this.setPath(Lists.newArrayList());
		else
			this.setPath(Lists.newArrayList(other.getPath()));
		this.setSource(other.getSource());
	}

	/**
	 * Unique id of the requirement
	 */
	public String getUniqueId() {
		return (String) getToscaPresentationValue(JsonPresentationFields.UNIQUE_ID);
	}

	public void setUniqueId(String uniqueId) {
		setToscaPresentationValue(JsonPresentationFields.UNIQUE_ID, uniqueId);
	}

	public String getName() {
		return (String) getToscaPresentationValue(JsonPresentationFields.NAME);
	}

	public void setName(String name) {
		setToscaPresentationValue(JsonPresentationFields.NAME, name);
	}
	
	public String getParentName() {
		return (String) getToscaPresentationValue(JsonPresentationFields.PARENT_NAME);
	}
	
	public void setParentName(String parentName) {
		setToscaPresentationValue(JsonPresentationFields.PARENT_NAME, parentName);
	}

	/**
	 * specify the capability type
	 */

	public String getCapability() {
		return (String) getToscaPresentationValue(JsonPresentationFields.CAPAPILITY);
	}

	public void setCapability(String capability) {
		setToscaPresentationValue(JsonPresentationFields.CAPAPILITY, capability);
	}

	/**
	 * specify the node type(Optional by tosca)
	 */
	public String getNode() {
		return (String) getToscaPresentationValue(JsonPresentationFields.NODE);
	}

	public void setNode(String node) {
		setToscaPresentationValue(JsonPresentationFields.NODE, node);
	}

	/**
	 * specify the relationship type(Optional by tosca)
	 */
	public String getRelationship() {
		return (String) getToscaPresentationValue(JsonPresentationFields.RELATIONSHIP);
	}

	public void setRelationship(String relationship) {
		setToscaPresentationValue(JsonPresentationFields.RELATIONSHIP, relationship);
	}

	/**
	 *  specifies the resource instance holding this requirement
	 */
	@Override
	public String getOwnerId() {
		return (String) getToscaPresentationValue(JsonPresentationFields.OWNER_ID);
	}
	
	@Override
	public void setOwnerId(String ownerId) {
		setToscaPresentationValue(JsonPresentationFields.OWNER_ID, ownerId);
	}

	public String getOwnerName() {
		return (String) getToscaPresentationValue(JsonPresentationFields.OWNER_NAME);
	}

	public void setOwnerName(String ownerName) {
		setToscaPresentationValue(JsonPresentationFields.OWNER_NAME, ownerName);
	}

	public String getMinOccurrences() {
		return (String) getToscaPresentationValue(JsonPresentationFields.MIN_OCCURRENCES);
	}

	public void setMinOccurrences(String minOccurrences) {
		setToscaPresentationValue(JsonPresentationFields.MIN_OCCURRENCES, minOccurrences);
	}

	public String getLeftOccurrences() {
		return (String) getToscaPresentationValue(JsonPresentationFields.LEFT_OCCURRENCES);
	}

	public void setLeftOccurrences(String leftOccurrences) {
		setToscaPresentationValue(JsonPresentationFields.LEFT_OCCURRENCES, leftOccurrences);
	}

	public String getMaxOccurrences() {
		return (String) getToscaPresentationValue(JsonPresentationFields.MAX_OCCURRENCES);
	}

	public void setMaxOccurrences(String maxOccurrences) {
		setToscaPresentationValue(JsonPresentationFields.MAX_OCCURRENCES, maxOccurrences);
	}

	public void setPath(List<String> path) {
		setToscaPresentationValue(JsonPresentationFields.PATH, path);
	}

	@SuppressWarnings({ "unchecked" })
	public List<String> getPath() {
		return (List<String>) getToscaPresentationValue(JsonPresentationFields.PATH);
	}

	public void setSource(String source) {
		setToscaPresentationValue(JsonPresentationFields.SOURCE, source);
	}

	public String getSource() {
		return (String) getToscaPresentationValue(JsonPresentationFields.SOURCE);
	}

	/**
	 * Adds the element to the path avoiding duplication
	 * @param elementInPath
	 */
	public void addToPath(String elementInPath) {
		List<String> path = getPath();
		if (path == null) {
			path = new ArrayList<>();
		}
		if(!path.contains(elementInPath)){
			path.add(elementInPath);
		}
		setPath(path);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		String name = getName();
		String uniqueId = getUniqueId();
		String capability = getCapability();
		String node = getNode();
		String relationship = getRelationship();
		String ownerId = getOwnerId();
		String ownerName = getOwnerName();
		String minOccurrences = getMinOccurrences();
		String maxOccurrences = getMaxOccurrences();
		String leftOccurrences = getLeftOccurrences();

		List<String> path = this.getPath();
		String source = getSource();

		result = prime * result + ((capability == null) ? 0 : capability.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((node == null) ? 0 : node.hashCode());
		result = prime * result + ((ownerId == null) ? 0 : ownerId.hashCode());
		result = prime * result + ((ownerName == null) ? 0 : ownerName.hashCode());
		result = prime * result + ((relationship == null) ? 0 : relationship.hashCode());
		result = prime * result + ((uniqueId == null) ? 0 : uniqueId.hashCode());
		result = prime * result + ((minOccurrences == null) ? 0 : minOccurrences.hashCode());
		result = prime * result + ((maxOccurrences == null) ? 0 : maxOccurrences.hashCode());
		result = prime * result + ((leftOccurrences == null) ? 0 : leftOccurrences.hashCode());
		result = prime * result + ((path == null) ? 0 : path.hashCode());
		result = prime * result + ((source == null) ? 0 : source.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RequirementDataDefinition other = (RequirementDataDefinition) obj;

		String name = getName();
		String uniqueId = getUniqueId();
		String capability = getCapability();
		String node = getNode();
		String relationship = getRelationship();
		String ownerId = getOwnerId();
		String ownerName = getOwnerName();
		String minOccurrences = getMinOccurrences();
		String maxOccurrences = getMaxOccurrences();
		String leftOccurrences = getLeftOccurrences();
		List<String> path = this.getPath();
		String source = getSource();

		if (capability == null) {
			if (other.getCapability() != null)
				return false;
		} else if (!capability.equals(other.getCapability()))
			return false;
		if (name == null) {
			if (other.getName() != null)
				return false;
		} else if (!name.equals(other.getName()))
			return false;
		if (node == null) {
			if (other.getNode() != null)
				return false;
		} else if (!node.equals(other.getNode()))
			return false;
		if (ownerId == null) {
			if (other.getOwnerId() != null)
				return false;
		} else if (!ownerId.equals(other.getOwnerId()))
			return false;
		if (ownerName == null) {
			if (other.getOwnerName() != null)
				return false;
		} else if (!ownerName.equals(other.getOwnerName()))
			return false;
		if (relationship == null) {
			if (other.getRelationship() != null)
				return false;
		} else if (!relationship.equals(other.getRelationship()))
			return false;
		if (uniqueId == null) {
			if (other.getUniqueId() != null)
				return false;
		} else if (!uniqueId.equals(other.getUniqueId()))
			return false;
		if (minOccurrences == null) {
			if (other.getMinOccurrences() != null)
				return false;
		} else if (!minOccurrences.equals(other.getMinOccurrences()))
			return false;
		if (maxOccurrences == null) {
			if (other.getMaxOccurrences() != null)
				return false;
		} else if (!maxOccurrences.equals(other.getMaxOccurrences()))
			return false;
		if (leftOccurrences == null) {
			if (other.getLeftOccurrences() != null)
				return false;
		} else if (!leftOccurrences.equals(other.getLeftOccurrences()))
			return false;
		if (path == null) {
			if (other.getPath() != null)
				return false;
		} else if (!path.equals(other.getPath()))
			return false;
		if (source == null) {
			if (other.getSource() != null)
				return false;
		} else if (!source.equals(other.getSource()))
			return false;
		return true;
	}

	@Override
	public String toString() {
		String name = getName();
		String uniqueId = getUniqueId();
		String capability = getCapability();
		String node = getNode();
		String relationship = getRelationship();
		String ownerId = getOwnerId();
		String ownerName = getOwnerName();
		String minOccurrences = getMinOccurrences();
		String maxOccurrences = getMaxOccurrences();
		String leftOccurrences = getLeftOccurrences();
		List<String> path = this.getPath();
		String source = getSource();

		return "RequirementDefinition [uniqueId=" + uniqueId + ", name=" + name + ", capability=" + capability + ", node=" + node + ", relationship=" + relationship + ", ownerId=" + ownerId + ", ownerName=" + ownerName + ", minOccurrences="
				+ minOccurrences + ", maxOccurrences=" + maxOccurrences + ",leftOccurrences=" + leftOccurrences + ", path=" + path + ", source=" + source + "]";
	}

}
