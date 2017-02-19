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

public class RelationshipTypeDataDefinition implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1104043358598884458L;

	private String uniqueId;

	private String description;

	/** Identifies the type of the capability. */
	private String type;

	private List<String> validSourceTypes;

	private String version;

	private Long creationTime;

	private Long modificationTime;

	// private String derivedFrom;

	public RelationshipTypeDataDefinition(RelationshipTypeDataDefinition cdt) {
		super();
		this.uniqueId = cdt.getUniqueId();
		this.description = cdt.getDescription();
		this.type = cdt.getType();
		this.validSourceTypes = cdt.getValidSourceTypes();
		this.version = cdt.getVersion();
		this.creationTime = cdt.getCreationTime();
		this.modificationTime = cdt.getModificationTime();
	}

	public RelationshipTypeDataDefinition() {

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

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
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
		return "RelationshipTypeDataDefinition [uniqueId=" + uniqueId + ", description=" + description + ", type="
				+ type + ", validSourceTypes=" + validSourceTypes + ", version=" + version + ", creationTime="
				+ creationTime + ", modificationTime=" + modificationTime + "]";
	}

}
