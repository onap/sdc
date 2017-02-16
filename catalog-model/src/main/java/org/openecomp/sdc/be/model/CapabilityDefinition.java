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
import java.util.ArrayList;
import java.util.List;

/**
 * Specifies the capabilities that the Node Type exposes.
 */
public class CapabilityDefinition implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3871825415338268030L;

	private String uniqueId;

	private String description;

	private String name;

	/** Identifies the type of the capability. */
	private String type;

	private List<String> validSourceTypes;

	private List<String> capabilitySources;
	/**
	 * The properties field contains all properties defined for
	 * CapabilityDefinition
	 */
	private List<ComponentInstanceProperty> properties;

	// specifies the resource instance holding this requirement
	private String ownerId;
	private String ownerName;
	private String minOccurrences;
	private String maxOccurrences;

	public CapabilityDefinition() {
		super();
	}

	public CapabilityDefinition(CapabilityDefinition other) {
		this.uniqueId = other.uniqueId;
		this.description = other.description;
		this.name = other.name;
		this.type = other.type;
		if (other.validSourceTypes != null) {
			this.validSourceTypes = new ArrayList<>(other.validSourceTypes);
		}
		if (other.capabilitySources != null) {
			this.capabilitySources = new ArrayList<>(other.capabilitySources);
		}
		if (other.properties != null) {
			this.properties = new ArrayList<>(other.properties);
		}
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

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
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

	public List<String> getValidSourceTypes() {
		return validSourceTypes;
	}

	public void setValidSourceTypes(List<String> validSourceTypes) {
		this.validSourceTypes = validSourceTypes;
	}

	public List<String> getCapabilitySources() {
		return capabilitySources;
	}

	public List<ComponentInstanceProperty> getProperties() {
		return properties;
	}

	public void setProperties(List<ComponentInstanceProperty> properties) {
		this.properties = properties;
	}

	public void setCapabilitySources(List<String> capabilitySources) {
		this.capabilitySources = capabilitySources;
	}

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
		result = prime * result + ((capabilitySources == null) ? 0 : capabilitySources.hashCode());
		result = prime * result + ((description == null) ? 0 : description.hashCode());
		result = prime * result + ((maxOccurrences == null) ? 0 : maxOccurrences.hashCode());
		result = prime * result + ((minOccurrences == null) ? 0 : minOccurrences.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((ownerId == null) ? 0 : ownerId.hashCode());
		result = prime * result + ((ownerName == null) ? 0 : ownerName.hashCode());
		result = prime * result + ((properties == null) ? 0 : properties.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		result = prime * result + ((uniqueId == null) ? 0 : uniqueId.hashCode());
		result = prime * result + ((validSourceTypes == null) ? 0 : validSourceTypes.hashCode());
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
		CapabilityDefinition other = (CapabilityDefinition) obj;
		if (capabilitySources == null) {
			if (other.capabilitySources != null)
				return false;
		} else if (!capabilitySources.equals(other.capabilitySources))
			return false;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		if (maxOccurrences == null) {
			if (other.maxOccurrences != null)
				return false;
		} else if (!maxOccurrences.equals(other.maxOccurrences))
			return false;
		if (minOccurrences == null) {
			if (other.minOccurrences != null)
				return false;
		} else if (!minOccurrences.equals(other.minOccurrences))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
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
		if (properties == null) {
			if (other.properties != null)
				return false;
		} else if (!properties.equals(other.properties))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		if (uniqueId == null) {
			if (other.uniqueId != null)
				return false;
		} else if (!uniqueId.equals(other.uniqueId))
			return false;
		if (validSourceTypes == null) {
			if (other.validSourceTypes != null)
				return false;
		} else if (!validSourceTypes.equals(other.validSourceTypes))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "CapabilityDefinition [uniqueId=" + uniqueId + ", description=" + description + ", name=" + name
				+ ", type=" + type + ", validSourceTypes=" + validSourceTypes + ", capabilitySources="
				+ capabilitySources + ", properties=" + properties + ", ownerId=" + ownerId + ", ownerName=" + ownerName
				+ ", minOccurrences=" + minOccurrences + ", maxOccurrences=" + maxOccurrences + "]";
	}

}
