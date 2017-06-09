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
import java.util.Map;

import org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields;
import org.openecomp.sdc.be.datatypes.tosca.ToscaDataDefinition;

public class GroupInstanceDataDefinition extends ToscaDataDefinition implements Serializable {
	
	private static final long serialVersionUID = -4231181556686542208L;
	
	public GroupInstanceDataDefinition() {
		super();
	}
	public GroupInstanceDataDefinition(Map<String, Object> gi) {
		super(gi);
	}
	public GroupInstanceDataDefinition(GroupInstanceDataDefinition other) {
		this.setUniqueId(other.getUniqueId());
		this.setName(other.getName());
		this.setGroupUid(other.getGroupUid());
		this.setCreationTime(other.getCreationTime());
		this.setModificationTime(other.getModificationTime());		
		this.setPosX(other.getPosX());
		this.setPosY(other.getPosY());
		this.setPropertyValueCounter(other.getPropertyValueCounter());
		this.setNormalizedName(other.getNormalizedName());		
		this.setCustomizationUUID(other.getCustomizationUUID());
		this.setGroupName(other.getGroupName());
		this.setInvariantUUID(other.getInvariantUUID());
		this.setType(other.getType());
		this.setGroupUUID(other.getGroupUUID());
		this.setVersion(other.getVersion());
		this.setDescription(other.getDescription());
		if(other.getArtifacts() != null)
			this.setArtifacts(new ArrayList<>(other.getArtifacts()));
		if(other.getArtifactsUuid() != null)
			this.setArtifactsUuid(new ArrayList<>(other.getArtifactsUuid()));
		if(other.getGroupInstanceArtifacts() != null)
			this.setGroupInstanceArtifacts(new ArrayList<>(other.getGroupInstanceArtifacts()));
		if(other.getGroupInstanceArtifactsUuid() != null)
			this.setGroupInstanceArtifactsUuid(new ArrayList<>(other.getGroupInstanceArtifactsUuid()));
		if(other.getProperties() != null)
			this.setProperties(new ArrayList<>(other.getProperties()));
	}

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

	public String getNormalizedName() {
		return (String) getToscaPresentationValue(JsonPresentationFields.NORMALIZED_NAME);
	}

	public void setNormalizedName(String normalizedName) {
		setToscaPresentationValue(JsonPresentationFields.NORMALIZED_NAME, normalizedName);
	}

	public String getGroupUid() {
		return (String) getToscaPresentationValue(JsonPresentationFields.GROUP_UNIQUE_ID);
	}

	public void setGroupUid(String groupUid) {
		setToscaPresentationValue(JsonPresentationFields.GROUP_UNIQUE_ID, groupUid);
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

	public String getPosX() {
		return (String) getToscaPresentationValue(JsonPresentationFields.POS_X);
	}

	public void setPosX(String posX) {
		setToscaPresentationValue(JsonPresentationFields.POS_X, posX);
	}

	public String getPosY() {
		return (String) getToscaPresentationValue(JsonPresentationFields.POS_Y);
	}

	public void setPosY(String posY) {
		setToscaPresentationValue(JsonPresentationFields.POS_Y, posY);
	}

	public Integer getPropertyValueCounter() {
		return (Integer) getToscaPresentationValue(JsonPresentationFields.PROPERTY_VALUE_COUNTER);
	}

	public void setPropertyValueCounter(Integer propertyValueCounter) {
		setToscaPresentationValue(JsonPresentationFields.PROPERTY_VALUE_COUNTER, propertyValueCounter);
	}

	public String getCustomizationUUID() {
		return (String) getToscaPresentationValue(JsonPresentationFields.CUSTOMIZATION_UUID);
	}

	public void setCustomizationUUID(String customizationUUID) {
		setToscaPresentationValue(JsonPresentationFields.CUSTOMIZATION_UUID, customizationUUID);
	}

	public String getGroupName() {
		return (String) getToscaPresentationValue(JsonPresentationFields.GROUP_NAME);
	}

	public void setGroupName(String groupName) {
		setToscaPresentationValue(JsonPresentationFields.GROUP_NAME, groupName);
	}

	public String getInvariantUUID() {
		return (String) getToscaPresentationValue(JsonPresentationFields.INVARIANT_UUID);
	}

	public void setInvariantUUID(String invariantUUID) {
		setToscaPresentationValue(JsonPresentationFields.INVARIANT_UUID, invariantUUID);
	}

	public String getType() {
		return (String) getToscaPresentationValue(JsonPresentationFields.TYPE);
	}

	public void setType(String type) {
		setToscaPresentationValue(JsonPresentationFields.TYPE, type);
	}

	public String getGroupUUID() {
		return (String) getToscaPresentationValue(JsonPresentationFields.GROUP_UUID);
	}

	public void setGroupUUID(String groupUUID) {
		setToscaPresentationValue(JsonPresentationFields.GROUP_UUID, groupUUID);
	}

	public String getVersion() {
		return (String) getToscaPresentationValue(JsonPresentationFields.VERSION);
	}

	public void setVersion(String version) {
		setToscaPresentationValue(JsonPresentationFields.VERSION, version);
	}

	public String getDescription() {
		return (String) getToscaPresentationValue(JsonPresentationFields.DESCRIPTION);
	}

	public void setDescription(String description) {
		setToscaPresentationValue(JsonPresentationFields.DESCRIPTION, description);
	}

	@SuppressWarnings("unchecked")
	public List<String> getArtifacts() {
		return (List<String>) getToscaPresentationValue(JsonPresentationFields.GROUP_ARTIFACTS);
	}

	public void setArtifacts(List<String> artifacts) {
		setToscaPresentationValue(JsonPresentationFields.GROUP_ARTIFACTS, artifacts);
	}

	@SuppressWarnings("unchecked")
	public List<String> getArtifactsUuid() {
		return (List<String> ) getToscaPresentationValue(JsonPresentationFields.GROUP_ARTIFACTS_UUID);
	}

	public void setArtifactsUuid(List<String> artifactsUuid) {
		setToscaPresentationValue(JsonPresentationFields.GROUP_ARTIFACTS_UUID, artifactsUuid);
	}

	@SuppressWarnings("unchecked")
	public List<String> getGroupInstanceArtifacts() {
		return (List<String>) getToscaPresentationValue(JsonPresentationFields.GROUP_INSTANCE_ARTIFACTS);
	}

	public void setGroupInstanceArtifacts(List<String> groupInstanceArtifacts) {
		setToscaPresentationValue(JsonPresentationFields.GROUP_INSTANCE_ARTIFACTS, groupInstanceArtifacts);
	}

	@SuppressWarnings("unchecked")
	public List<String> getGroupInstanceArtifactsUuid() {
		return (List<String>) getToscaPresentationValue(JsonPresentationFields.GROUP_INSTANCE_ARTIFACTS_UUID);
	}

	public void setGroupInstanceArtifactsUuid(List<String> groupInstanceArtifactsUuid) {
		setToscaPresentationValue(JsonPresentationFields.GROUP_INSTANCE_ARTIFACTS_UUID, groupInstanceArtifactsUuid);
	}

	@SuppressWarnings("unchecked")
	public List<PropertyDataDefinition> getProperties() {
		return (List<PropertyDataDefinition>) getToscaPresentationValue(JsonPresentationFields.GROUP_INSTANCE_PROPERTIES);
	}

	public void setProperties(List<PropertyDataDefinition> properties) {
		setToscaPresentationValue(JsonPresentationFields.GROUP_INSTANCE_PROPERTIES, properties);
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}
}
