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
 * Represents the capability of the component or component instance
 */
public class CapabilityDataDefinition extends ToscaDataDefinition implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 7544984873506188920L;
	public static final String MIN_OCCURRENCES = "1";
	public static final String MAX_OCCURRENCES = "UNBOUNDED";

	/**
	 * The default constructor initializing limits of the occurrences
	 */
	public CapabilityDataDefinition() {
		super();
		this.setMinOccurrences(  MIN_OCCURRENCES );
		this.setMaxOccurrences(  MAX_OCCURRENCES);
		this.setLeftOccurrences(  MAX_OCCURRENCES);
	}
	/**
	 * Deep copy constructor
	 * @param other
	 */
	public CapabilityDataDefinition(CapabilityDataDefinition other) {
		super();		
		this.setUniqueId(other.getUniqueId());
		this.setType(other.getType());		
		this.setDescription (  other.getDescription());
		this.setName(  other.getName());
		this.setParentName(  other.getParentName());
		
		if(other.getValidSourceTypes() == null)
			this.setValidSourceTypes(Lists.newArrayList());
		else
			this.setValidSourceTypes(Lists.newArrayList(other.getValidSourceTypes()));
		
		if(other.getCapabilitySources() == null)
			this.setCapabilitySources(Lists.newArrayList());
		else
			this.setCapabilitySources(Lists.newArrayList(other.getCapabilitySources()));
		
		this.setOwnerId( other.getOwnerId());
		this.setOwnerName( other.getOwnerName());
		this.setMinOccurrences(  other.getMinOccurrences());
		this.setMaxOccurrences(  other.getMaxOccurrences());
		this.setLeftOccurrences(other.getLeftOccurrences());
		
		if(other.getPath() == null)
			this.setPath(Lists.newArrayList());
		else
			this.setPath(Lists.newArrayList(other.getPath()));
		
		this.setSource(other.getSource());
		
	}

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

	public String getMaxOccurrences() {
		return (String) getToscaPresentationValue(JsonPresentationFields.MAX_OCCURRENCES);
	}

	public void setMaxOccurrences(String maxOccurrences) {
		setToscaPresentationValue(JsonPresentationFields.MAX_OCCURRENCES, maxOccurrences);
	}
	public String getLeftOccurrences() {
		return (String) getToscaPresentationValue(JsonPresentationFields.LEFT_OCCURRENCES);
	}

	public void setLeftOccurrences(String leftOccurrences) {
		setToscaPresentationValue(JsonPresentationFields.LEFT_OCCURRENCES, leftOccurrences);
	}
	
	public String getUniqueId() {
		return (String) getToscaPresentationValue(JsonPresentationFields.UNIQUE_ID);
	}

	public void setUniqueId(String uniqueId) {
		setToscaPresentationValue(JsonPresentationFields.UNIQUE_ID, uniqueId);
	}

	public String getDescription() {
		return (String) getToscaPresentationValue(JsonPresentationFields.DESCRIPTION);
	}

	public void setDescription(String description) {
		setToscaPresentationValue(JsonPresentationFields.DESCRIPTION, description);
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

	public String getType() {
		return (String) getToscaPresentationValue(JsonPresentationFields.TYPE);
	}

	public void setType(String type) {
		setToscaPresentationValue(JsonPresentationFields.TYPE, type);
	}

	@SuppressWarnings("unchecked")
	public List<String> getValidSourceTypes() {
		return (List<String>) getToscaPresentationValue(JsonPresentationFields.VALID_SOURCE_TYPE);
	}

	public void setValidSourceTypes(List<String> validSourceTypes) {
		setToscaPresentationValue(JsonPresentationFields.VALID_SOURCE_TYPE, validSourceTypes);
	}

	@SuppressWarnings("unchecked")
	public List<String> getCapabilitySources() {
		return (List<String>) getToscaPresentationValue(JsonPresentationFields.CAPABILITY_SOURCES);
	}
	
	public void setCapabilitySources(List<String> capabilitySources) {
		setToscaPresentationValue(JsonPresentationFields.CAPABILITY_SOURCES, capabilitySources);
	}

	public void setPath(List<String> path){
		setToscaPresentationValue(JsonPresentationFields.PATH, path);
	}
	@SuppressWarnings("unchecked")
	public List<String> getPath() {
		return (List<String>) getToscaPresentationValue(JsonPresentationFields.PATH);
	}
	public void setSource(String source){
		setToscaPresentationValue(JsonPresentationFields.SOURCE, source);
	}
	public String getSource() {
		return (String) getToscaPresentationValue(JsonPresentationFields.SOURCE);
	}
	
	/**
	 * Adds the element to the path avoiding duplication
	 * @param elementInPath
	 */
	public void addToPath(String elementInPath){
		List<String> path = getPath();
		if ( path == null ){
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
		
		String uniqueId = this.getUniqueId();		
		String description = this.getDescription();
		String name = this.getName();
		String type = this.getType();
		List<String> validSourceTypes = this.getValidSourceTypes();
		List<String> capabilitySources = this.getCapabilitySources();
		List<String> path = this.getPath();
		
		String ownerId = this.getOwnerId();
		String ownerName = this.getOwnerName();
		String minOccurrences = this.getMinOccurrences();
		String maxOccurrences = this.getMaxOccurrences();
		String leftOccurrences = getLeftOccurrences();
		String source = getSource();
		
		result = prime * result + ((capabilitySources == null) ? 0 : capabilitySources.hashCode());
		result = prime * result + ((description == null) ? 0 : description.hashCode());
		result = prime * result + ((maxOccurrences == null) ? 0 : maxOccurrences.hashCode());
		result = prime * result + ((minOccurrences == null) ? 0 : minOccurrences.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((ownerId == null) ? 0 : ownerId.hashCode());
		result = prime * result + ((ownerName == null) ? 0 : ownerName.hashCode());
	
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		result = prime * result + ((uniqueId == null) ? 0 : uniqueId.hashCode());
		result = prime * result + ((validSourceTypes == null) ? 0 : validSourceTypes.hashCode());
		result = prime * result + ((leftOccurrences == null) ? 0 : leftOccurrences.hashCode());
		result = prime * result + ((path == null) ? 0 : path.hashCode());
		result = prime * result + ((source == null) ? 0 : source.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		
		String uniqueId = this.getUniqueId();		
		String description = this.getDescription();
		String name = this.getName();
		String type = this.getType();
		List<String> validSourceTypes = this.getValidSourceTypes();
		List<String> capabilitySources = this.getCapabilitySources();
		String ownerId = this.getOwnerId();
		String ownerName = this.getOwnerName();
		String minOccurrences = this.getMinOccurrences();
		String maxOccurrences = this.getMaxOccurrences();
		String leftOccurrences = getLeftOccurrences();
		List<String> path = this.getPath();
		String source = getSource();
		
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CapabilityDataDefinition other = (CapabilityDataDefinition) obj;
		if (capabilitySources == null) {
			if (other.getCapabilitySources() != null)
				return false;
		} else if (!capabilitySources.equals(other.getCapabilitySources()))
			return false;
		if (description == null) {
			if (other.getDescription() != null)
				return false;
		} else if (!description.equals(other.getDescription()))
			return false;
		if (maxOccurrences == null) {
			if (other.getMaxOccurrences() != null)
				return false;
		} else if (!maxOccurrences.equals(other.getMaxOccurrences()))
			return false;
		if (minOccurrences == null) {
			if (other.getMinOccurrences() != null)
				return false;
		} else if (!minOccurrences.equals(other.getMinOccurrences()))
			return false;
		if (name == null) {
			if (other.getName() != null)
				return false;
		} else if (!name.equals(other.getName()))
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
		if (type == null) {
			if (other.getType() != null)
				return false;
		} else if (!type.equals(other.getType()))
			return false;
		if (uniqueId == null) {
			if (other.getUniqueId() != null)
				return false;
		} else if (!uniqueId.equals(other.getUniqueId()))
			return false;
		if (validSourceTypes == null) {
			if (other.getValidSourceTypes() != null)
				return false;
		} else if (!validSourceTypes.equals(other.getValidSourceTypes()))
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
		String uniqueId = this.getUniqueId();		
		String description = this.getDescription();
		String name = this.getName();
		String type = this.getType();
		List<String> validSourceTypes = this.getValidSourceTypes();
		List<String> capabilitySources = this.getCapabilitySources();
		List<String> path = this.getPath();
		String ownerId = this.getOwnerId();
		String ownerName = this.getOwnerName();
		String minOccurrences = this.getMinOccurrences();
		String maxOccurrences = this.getMaxOccurrences();
		String source = this.getSource();
		
		
		return "CapabilityDefinition [uniqueId=" + uniqueId + ", description=" + description + ", name=" + name
				+ ", type=" + type + ", validSourceTypes=" + validSourceTypes + ", capabilitySources="
				+ capabilitySources + ", ownerId=" + ownerId + ", ownerName=" + ownerName
				+ ", minOccurrences=" + minOccurrences + ", maxOccurrences=" + maxOccurrences + ", path=" + path+ ", source=" + source + "]";
	}

}
