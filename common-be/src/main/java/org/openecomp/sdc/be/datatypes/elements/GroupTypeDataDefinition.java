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
import java.util.List;
import java.util.Map;

public class GroupTypeDataDefinition implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7001817818907172496L;

	private String uniqueId;

	private String type;

	private String version;

	private String derivedFrom;

	private List<String> members;

	private Map<String, String> metadata;

	private String description;

	/**
	 * Timestamp of data type creation
	 */
	private Long creationTime;

	/**
	 * Timestamp of the data type last update
	 */
	private Long modificationTime;

	private boolean highestVersion;

	public GroupTypeDataDefinition() {

	}

	public GroupTypeDataDefinition(GroupTypeDataDefinition p) {
		this.uniqueId = p.uniqueId;
		this.type = p.type;
		this.version = p.version;
		this.members = p.members;
		this.metadata = p.metadata;
		// this.derivedFromName = p.derivedFromName;
		this.description = p.description;
		this.creationTime = p.creationTime;
		this.modificationTime = p.modificationTime;
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

	public List<String> getMembers() {
		return members;
	}

	public void setMembers(List<String> members) {
		this.members = members;
	}

	public Map<String, String> getMetadata() {
		return metadata;
	}

	public void setMetadata(Map<String, String> metadata) {
		this.metadata = metadata;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getUniqueId() {
		return uniqueId;
	}

	public void setUniqueId(String uniqueId) {
		this.uniqueId = uniqueId;
	}

	public Long getCreationTime() {
		return creationTime;
	}

	public void setCreationTime(Long creationTime) {
		this.creationTime = creationTime;
	}

	public Long getModificationTime() {
		return modificationTime;
	}

	public void setModificationTime(Long modificationTime) {
		this.modificationTime = modificationTime;
	}

	@Override
	public String toString() {
		return "GroupTypeDataDefinition [uniqueId=" + uniqueId + ", type=" + type + ", version=" + version
				+ ", members=" + members + ", metadata=" + metadata + ", description=" + description + ", creationTime="
				+ creationTime + ", modificationTime=" + modificationTime + "]";
	}

	public String getDerivedFrom() {
		return derivedFrom;
	}

	public void setDerivedFrom(String derivedFrom) {
		this.derivedFrom = derivedFrom;
	}

	public boolean isHighestVersion() {
		return highestVersion;
	}

	public void setHighestVersion(boolean isLatestVersion) {
		this.highestVersion = isLatestVersion;
	}

}
