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

import org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields;
import org.openecomp.sdc.be.datatypes.tosca.ToscaDataDefinition;

public class CapabilityTypeDataDefinition extends ToscaDataDefinition implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3242123405337612089L;



	// private String derivedFrom;

	public CapabilityTypeDataDefinition(CapabilityTypeDataDefinition cdt) {
		super();
		this.setUniqueId(cdt.getUniqueId());
		this.setDescription ( cdt.getDescription());
		this.setType(cdt.getType());
		this.setValidSourceTypes( cdt.getValidSourceTypes());
		this.setVersion( cdt.getVersion());
		this.setCreationTime (cdt.getCreationTime());
		this.setModificationTime (cdt.getModificationTime());		
		
	}

	public CapabilityTypeDataDefinition() {
		super();
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

	public String getType() {
		return (String) getToscaPresentationValue(JsonPresentationFields.TYPE);
	}

	public void setType(String type) {
		setToscaPresentationValue(JsonPresentationFields.TYPE, type);
	}

	public List<String> getValidSourceTypes() {
		return (List<String> ) getToscaPresentationValue(JsonPresentationFields.VALID_SOURCE_TYPE);
	}

	public void setValidSourceTypes(List<String> validSourceTypes) {
		setToscaPresentationValue(JsonPresentationFields.VALID_SOURCE_TYPE, validSourceTypes);
	}

	public String getVersion() {
		return (String) getToscaPresentationValue(JsonPresentationFields.VERSION);
	}

	public void setVersion(String version) {
		setToscaPresentationValue(JsonPresentationFields.VERSION, version);
	}

	public Long getCreationTime() {
		return (Long) getToscaPresentationValue(JsonPresentationFields.CREATION_TIME);
	}

	public void setCreationTime(Long creationTime) {
		setToscaPresentationValue(JsonPresentationFields.CREATION_TIME, creationTime);
	}

	public Long getModificationTime() {
		return (Long) getToscaPresentationValue(JsonPresentationFields.MODIFICATION_TIME);
	}

	public void setModificationTime(Long modificationTime) {
		setToscaPresentationValue(JsonPresentationFields.MODIFICATION_TIME, modificationTime);
	}

	@Override
	public String toString() {
		String uniqueId = getUniqueId();
		String description = getDescription();
		String type = getType();
		List<String> validSourceTypes = getValidSourceTypes();
		String version = getVersion();
		Long creationTime = getCreationTime();
		Long modificationTime = getModificationTime();
		
		return "CapabilityTypeDataDefinition [uniqueId=" + uniqueId + ", description=" + description + ", type=" + type
				+ ", validSourceTypes=" + validSourceTypes + ", version=" + version + ", creationTime=" + creationTime
				+ ", modificationTime=" + modificationTime + "]";
	}

}
