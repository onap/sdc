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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.openecomp.sdc.be.datatypes.elements.GroupDataDefinition;

public class GroupDefinition extends GroupDataDefinition implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -852613634651112247L;

	// map of componentInstances <name: uniqueId>
	private Map<String, String> members;

	// properties (properties should be defined in the group type, the
	// properties here are actually the value for the properties)
	private List<GroupProperty> properties;

	// artifacts - list of artifact uid. All artifacts in the group must already
	// be uploaded to the VF
	private List<String> artifacts;

	private List<String> artifactsUuid;

	// The unique id of the type of this group
	private String typeUid;

	public GroupDefinition() {
		super();
	}

	public GroupDefinition(GroupDataDefinition other) {
		super(other);
	}

	public GroupDefinition(GroupDefinition other) {
		this.setName(other.getName());
		this.setUniqueId(other.getUniqueId());
		this.setType(other.getType());
		this.setVersion(other.getVersion());
		this.setInvariantUUID(other.getInvariantUUID());
		this.setGroupUUID(other.getGroupUUID());
		this.setDescription(other.getDescription());
		if (other.members != null) {
			this.members = new HashMap<String, String>(other.getMembers());
		}
		if (other.properties != null) {
			this.properties = other.properties.stream().map(p -> new GroupProperty(p)).collect(Collectors.toList());
		}
		if (other.artifacts != null) {
			this.artifacts = new ArrayList<String>(other.getArtifacts());
		}

		if (other.artifactsUuid != null) {
			this.artifactsUuid = new ArrayList<String>(other.getArtifactsUuid());
		}
		this.setTypeUid(other.typeUid);
	}

	public Map<String, String> getMembers() {
		return members;
	}

	public void setMembers(Map<String, String> members) {
		this.members = members;
	}

	public List<GroupProperty> getProperties() {
		return properties;
	}

	public void setProperties(List<GroupProperty> properties) {
		this.properties = properties;
	}

	public List<String> getArtifacts() {
		return artifacts;
	}

	public void setArtifacts(List<String> artifacts) {
		this.artifacts = artifacts;
	}

	public String getTypeUid() {
		return typeUid;
	}

	public void setTypeUid(String typeUid) {
		this.typeUid = typeUid;
	}

	public List<String> getArtifactsUuid() {
		return artifactsUuid;
	}

	public void setArtifactsUuid(List<String> artifactsUuid) {
		this.artifactsUuid = artifactsUuid;
	}

	@Override
	public String toString() {
		return "GroupDefinition [" + super.toString() + "members=" + members + ", properties=" + properties
				+ ", artifacts=" + artifacts + ", artifactsUUID=" + artifactsUuid + ", typeUid=" + typeUid + "]";
	}

}
