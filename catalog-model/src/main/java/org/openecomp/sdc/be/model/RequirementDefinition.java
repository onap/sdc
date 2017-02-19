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

package org.openecomp.sdc.be.model;

import java.io.Serializable;

/**
 * Specifies the requirements that the Node Type exposes.
 */
public class RequirementDefinition implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8840549489409274532L;

	/**
	 * Unique id of the requirement
	 */
	private String uniqueId;

	private String name;

	/**
	 * specify the capability type
	 */
	private String capability;

	/**
	 * specify the node type(Optional by tosca)
	 */
	private String node;

	/**
	 * specify the relationship type(Optional by tosca)
	 */
	private String relationship;

	// specifies the resource instance holding this requirement
	private String ownerId;
	private String ownerName;

	private String minOccurrences;
	private String maxOccurrences;

	public RequirementDefinition() {
		super();
	}

	public RequirementDefinition(RequirementDefinition other) {
		this.uniqueId = other.uniqueId;
		this.name = other.name;
		this.capability = other.capability;
		this.node = other.node;
		this.relationship = other.relationship;
		this.ownerId = other.ownerId;
		this.ownerName = other.ownerName;
		this.minOccurrences = other.minOccurrences;
		this.maxOccurrences = other.maxOccurrences;

	}

	public String getUniqueId() {
		return uniqueId;
	}

	public void setUniqueId(String uniqueId) {
		this.uniqueId = uniqueId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCapability() {
		return capability;
	}

	public void setCapability(String capability) {
		this.capability = capability;
	}

	public String getNode() {
		return node;
	}

	public void setNode(String node) {
		this.node = node;
	}

	public String getRelationship() {
		return relationship;
	}

	public void setRelationship(String relationship) {
		this.relationship = relationship;
	}

	// public RequirementImplDef getRequirementImpl() {
	// return requirementImpl;
	// }
	//
	// public void setRequirementImpl(RequirementImplDef requirementImpl) {
	// this.requirementImpl = requirementImpl;
	// }

	public String getOwnerId() {
		return ownerId;
	}

	public void setOwnerId(String ownerId) {
		this.ownerId = ownerId;
	}

	public String getOwnerName() {
		return ownerName;
	}

	public void setOwnerName(String ownerName) {
		this.ownerName = ownerName;
	}

	public String getMinOccurrences() {
		return minOccurrences;
	}

	public void setMinOccurrences(String minOccurrences) {
		this.minOccurrences = minOccurrences;
	}

	public String getMaxOccurrences() {
		return maxOccurrences;
	}

	public void setMaxOccurrences(String maxOccurrences) {
		this.maxOccurrences = maxOccurrences;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((capability == null) ? 0 : capability.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((node == null) ? 0 : node.hashCode());
		result = prime * result + ((ownerId == null) ? 0 : ownerId.hashCode());
		result = prime * result + ((ownerName == null) ? 0 : ownerName.hashCode());
		result = prime * result + ((relationship == null) ? 0 : relationship.hashCode());
		result = prime * result + ((uniqueId == null) ? 0 : uniqueId.hashCode());
		result = prime * result + ((minOccurrences == null) ? 0 : minOccurrences.hashCode());
		result = prime * result + ((maxOccurrences == null) ? 0 : maxOccurrences.hashCode());
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
		RequirementDefinition other = (RequirementDefinition) obj;
		if (capability == null) {
			if (other.capability != null)
				return false;
		} else if (!capability.equals(other.capability))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (node == null) {
			if (other.node != null)
				return false;
		} else if (!node.equals(other.node))
			return false;
		if (ownerId == null) {
			if (other.ownerId != null)
				return false;
		} else if (!ownerId.equals(other.ownerId))
			return false;
		if (ownerName == null) {
			if (other.ownerName != null)
				return false;
		} else if (!ownerName.equals(other.ownerName))
			return false;
		if (relationship == null) {
			if (other.relationship != null)
				return false;
		} else if (!relationship.equals(other.relationship))
			return false;
		if (uniqueId == null) {
			if (other.uniqueId != null)
				return false;
		} else if (!uniqueId.equals(other.uniqueId))
			return false;
		if (minOccurrences == null) {
			if (other.minOccurrences != null)
				return false;
		} else if (!minOccurrences.equals(other.minOccurrences))
			return false;
		if (maxOccurrences == null) {
			if (other.maxOccurrences != null)
				return false;
		} else if (!maxOccurrences.equals(other.maxOccurrences))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "RequirementDefinition [uniqueId=" + uniqueId + ", name=" + name + ", capability=" + capability
				+ ", node=" + node + ", relationship=" + relationship + ", ownerId=" + ownerId + ", ownerName="
				+ ownerName + ", minOccurrences=" + minOccurrences + ", maxOccurrences=" + maxOccurrences + "]";
	}

}
