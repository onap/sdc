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

import org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields;
import org.openecomp.sdc.be.datatypes.tosca.ToscaDataDefinition;

import java.util.List;

public class RelationshipInstDataDefinition extends ToscaDataDefinition {

	public RelationshipInstDataDefinition(RelationshipInstDataDefinition cdt) {
		this.setUniqueId(cdt.getUniqueId());
		this.setDescription(cdt.getDescription());
		this.setType(cdt.getType());
		this.setValidSourceTypes(cdt.getValidSourceTypes());
		this.setValidTargetTypes(cdt.getValidTargetTypes());
		this.setVersion(cdt.getVersion());
		this.setOriginUI(cdt.isOriginUI());
		this.setCreationTime(cdt.getCreationTime());
		this.setModificationTime(cdt.getModificationTime());
		this.setCapabilityOwnerId(cdt.getCapabilityOwnerId());
		this.setRequirementOwnerId(cdt.getRequirementOwnerId());
		this.setCapabilityId(cdt.getCapabilityId());
		this.setRequirementId(cdt.getRequirementId());
		this.setFromId(cdt.getFromId());
		this.setToId(cdt.getToId());
		this.setRequirement(cdt.getRequirement());
		this.setCapability(cdt.getCapability());
	}

	public void setRequirement(String requirement) {
		setToscaPresentationValue(JsonPresentationFields.REQUIREMENT, requirement);
	}

	public String getRequirement() {
		return (String) getToscaPresentationValue(JsonPresentationFields.REQUIREMENT);
	}
	public void setCapability(String capability) {
		setToscaPresentationValue(JsonPresentationFields.CAPABILITY, capability);
	}

	public String getCapability() {
		return (String) getToscaPresentationValue(JsonPresentationFields.CAPABILITY);
	}
	public void setToId(Object toId) {
		setToscaPresentationValue(JsonPresentationFields.TO_ID, toId);
	}

	public void setFromId(Object fromId) {
		setToscaPresentationValue(JsonPresentationFields.FROM_ID, fromId);
	}

	public String getToId() {
		return (String) getToscaPresentationValue(JsonPresentationFields.TO_ID);
	}

	public String getFromId() {
		return (String) getToscaPresentationValue(JsonPresentationFields.FROM_ID);
	}

	public void setRequirementId(Object requirementId) {
		setToscaPresentationValue(JsonPresentationFields.REQUIREMENT_ID, requirementId);
	}

	public void setCapabilityId(Object capabilityId) {
		setToscaPresentationValue(JsonPresentationFields.CAPABILITY_ID, capabilityId);
	}

	public String getRequirementId() {
		return (String) getToscaPresentationValue(JsonPresentationFields.REQUIREMENT_ID);
	}

	public String getCapabilityId() {
		return (String) getToscaPresentationValue(JsonPresentationFields.CAPABILITY_ID);
	}

	public void setRequirementOwnerId(Object requirementOwnerId) {
		setToscaPresentationValue(JsonPresentationFields.REQUIREMENT_OWNER_ID, requirementOwnerId);
	}

	public String getRequirementOwnerId() {
		return (String) getToscaPresentationValue(JsonPresentationFields.REQUIREMENT_OWNER_ID);
	}

	public void setCapabilityOwnerId(Object capabilityOwnerId) {
		setToscaPresentationValue(JsonPresentationFields.CAPABILTY_OWNER_ID, capabilityOwnerId);
	}

	public String getCapabilityOwnerId() {
		return (String) getToscaPresentationValue(JsonPresentationFields.CAPABILTY_OWNER_ID);
	}

	public RelationshipInstDataDefinition() {
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

	/** Identifies the type of the capability. */
	public String getType() {
		return (String) getToscaPresentationValue(JsonPresentationFields.TYPE);
	}

	public void setType(String type) {
		setToscaPresentationValue(JsonPresentationFields.TYPE, type);
	}

	public List<String> getValidSourceTypes() {
		return (List<String>) getToscaPresentationValue(JsonPresentationFields.VALID_SOURCE_TYPES);
	}

	public void setValidSourceTypes(List<String> validSourceTypes) {
		setToscaPresentationValue(JsonPresentationFields.VALID_SOURCE_TYPES, validSourceTypes);
	}

	public List<String> getValidTargetTypes() {
		return (List<String>) getToscaPresentationValue(JsonPresentationFields.VALID_TARGET_TYPES);
	}

	public void setValidTargetTypes(List<String> validTargetTypes) {
		setToscaPresentationValue(JsonPresentationFields.VALID_TARGET_TYPES, validTargetTypes);
	}

	public String getVersion() {
		return (String) getToscaPresentationValue(JsonPresentationFields.VERSION);
	}

	public void setVersion(String version) {
		setToscaPresentationValue(JsonPresentationFields.VERSION, version);
	}
	
	public Boolean isOriginUI() {
	    return (Boolean) getToscaPresentationValue(JsonPresentationFields.ORIGIN_UI);
	}
	
	public void setOriginUI(Boolean originUI) {
	    setToscaPresentationValue(JsonPresentationFields.ORIGIN_UI, originUI);
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
		Boolean originUI = isOriginUI();
		Long creationTime = getCreationTime();
		Long modificationTime = getModificationTime();

		return "RelationshipTypeDataDefinition [uniqueId=" + uniqueId + ", description=" + description + ", type=" + type + ", validSourceTypes=" + validSourceTypes + 
		        ", version=" + version + ", originUI=" + originUI + ", creationTime=" + creationTime + ", modificationTime=" + modificationTime + "]";
	}

}
