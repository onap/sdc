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

public class GroupDataDefinition implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1565606165279109427L;

	private String name;

	// the id is unique per group instance on graph.
	private String uniqueId;

	private String type;

	// version should be changed when there is a change to the group's metadata
	// or to the groups members
	// (not necessarily when the VF version is changed). This field cannot be
	// updated by user
	private String version;

	// this id is constant and does not changed (also not when changing
	// version). This field cannot be updated by user
	private String invariantUUID;

	// the group UUID should be changed when one of the artifacts/component
	// instances has been changed.
	private String groupUUID;

	private String description;

	private Integer propertyValueCounter = 0;

	public GroupDataDefinition() {

	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUniqueId() {
		return uniqueId;
	}

	public void setUniqueId(String uniqueId) {
		this.uniqueId = uniqueId;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getInvariantUUID() {
		return invariantUUID;
	}

	public void setInvariantUUID(String invariantUUID) {
		this.invariantUUID = invariantUUID;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Integer getPropertyValueCounter() {
		return propertyValueCounter;
	}

	public void setPropertyValueCounter(Integer propertyValueCounter) {
		this.propertyValueCounter = propertyValueCounter;
	}

	public String getGroupUUID() {
		return groupUUID;
	}

	public void setGroupUUID(String groupUUID) {
		this.groupUUID = groupUUID;
	}

	public GroupDataDefinition(GroupDataDefinition other) {
		this.name = other.name;
		this.uniqueId = other.uniqueId;
		this.type = other.type;
		this.version = other.version;
		this.invariantUUID = other.invariantUUID;
		this.description = other.description;
		this.propertyValueCounter = other.propertyValueCounter;
		this.groupUUID = other.groupUUID;
	}

	@Override
	public String toString() {
		return "GroupDataDefinition [name=" + name + ", uniqueId=" + uniqueId + ", type=" + type + ", version="
				+ version + ", invariantUUID=" + invariantUUID + ", description=" + description
				+ ", propertyValueCounter=" + propertyValueCounter + ", groupUUID=" + groupUUID + "]";
	}
}
