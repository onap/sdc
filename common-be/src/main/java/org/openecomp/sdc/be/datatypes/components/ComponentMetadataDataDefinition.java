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

package org.openecomp.sdc.be.datatypes.components;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.tosca.ToscaDataDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ComponentMetadataDataDefinition extends ToscaDataDefinition implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9114770126086263552L;
	private static final Logger log = LoggerFactory.getLogger(ComponentMetadataDataDefinition.class.getName());
	
	private String uniqueId;

	private String name; // archiveName

	private String version; // archiveVersion

	private Boolean isHighestVersion;

	private Long creationDate;

	private Long lastUpdateDate;

	private String description;

	private String state;

	private List<String> tags;

	private String conformanceLevel;

	private String icon;

	private String UUID;

	private String normalizedName;

	private String systemName;

	private String contactId;

	private Map<String, String> allVersions;

	private Boolean isDeleted;

	private String projectCode;

	private String csarUUID;

	private String csarVersion;

	private String importedToscaChecksum;

	private String invariantUUID;

	protected ComponentTypeEnum componentType;

	// USER
	private String creatorUserId;

	private String creatorFullName;

	private String lastUpdaterUserId;

	private String lastUpdaterFullName;

	public Boolean getIsHighestVersion() {
		return isHighestVersion;
	}

	public void setIsHighestVersion(Boolean isHighestVersion) {
		this.isHighestVersion = isHighestVersion;
	}

	public ComponentTypeEnum getComponentType() {
		return componentType;
	}

	public void setComponentType(ComponentTypeEnum componentType) {
		this.componentType = componentType;
	}

	public String getCreatorUserId() {
		return creatorUserId;
	}

	public void setCreatorUserId(String creatorUserId) {
		this.creatorUserId = creatorUserId;
	}

	public String getCreatorFullName() {
		return creatorFullName;
	}

	public void setCreatorFullName(String creatorFullName) {
		this.creatorFullName = creatorFullName;
	}

	public String getLastUpdaterUserId() {
		return lastUpdaterUserId;
	}

	public void setLastUpdaterUserId(String lastUpdaterUserId) {
		this.lastUpdaterUserId = lastUpdaterUserId;
	}

	public String getLastUpdaterFullName() {
		return lastUpdaterFullName;
	}

	public void setLastUpdaterFullName(String lastUpdaterFullName) {
		this.lastUpdaterFullName = lastUpdaterFullName;
	}

	public Boolean getIsDeleted() {
		return isDeleted;
	}

	public ComponentMetadataDataDefinition() {

	}

	public ComponentMetadataDataDefinition(ComponentMetadataDataDefinition other) {
		this.uniqueId = other.getUniqueId();
		this.name = other.getName();
		this.version = other.getVersion();
		this.isHighestVersion = other.isHighestVersion();
		this.creationDate = other.getCreationDate();
		this.lastUpdateDate = other.getLastUpdateDate();
		this.description = other.getDescription();
		this.state = other.getState();
		this.tags = new ArrayList<>(other.getTags());
		this.icon = other.getIcon();
		this.contactId = other.getContactId();
		this.UUID = other.getUUID();
		this.normalizedName = other.getNormalizedName();
		this.systemName = other.getSystemName();
		this.allVersions = new HashMap<>(other.getAllVersions());
		this.isDeleted = other.isDeleted();
		this.projectCode = other.getProjectCode();
		this.csarUUID = other.getCsarUUID();
		this.csarVersion = other.csarVersion;
		this.importedToscaChecksum = other.getImportedToscaChecksum();
		this.invariantUUID = other.getInvariantUUID();

	}

	public String getUniqueId() {
		return uniqueId;
	}

	public void setUniqueId(String uniqueId) {
		if ( this.uniqueId != null && !this.uniqueId.equals( uniqueId ) )
			log.warn("uniqueId changed more then once -> OLD : {} , NEW: {} ", this.uniqueId ,uniqueId );
		this.uniqueId = uniqueId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public Boolean isHighestVersion() {
		return isHighestVersion;
	}

	public void setHighestVersion(Boolean isHighestVersion) {
		this.isHighestVersion = isHighestVersion;
	}

	public Long getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Long creationDate) {
		this.creationDate = creationDate;
	}

	public Long getLastUpdateDate() {
		return lastUpdateDate;
	}

	public void setLastUpdateDate(Long lastUpdateDate) {
		this.lastUpdateDate = lastUpdateDate;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public List<String> getTags() {
		return tags;
	}

	public void setTags(List<String> tags) {
		this.tags = tags;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public String getContactId() {
		return contactId;
	}

	public void setContactId(String contactId) {
		this.contactId = contactId;
	}

	public String getUUID() {
		return UUID;
	}

	public void setUUID(String UUID) {
		if ( this.UUID != null && !this.UUID.equals( UUID ) )
			log.warn("UUID changed more then once -> OLD : {} , NEW: {} ", this.UUID, UUID );
		this.UUID = UUID;
	}

	public String getNormalizedName() {
		return normalizedName;
	}

	public void setNormalizedName(String normalizedName) {
		this.normalizedName = normalizedName;
	}

	public String getSystemName() {
		return systemName;
	}

	public void setSystemName(String systemName) {
		this.systemName = systemName;
	}

	public Map<String, String> getAllVersions() {
		return allVersions;
	}

	public void setAllVersions(Map<String, String> allVersions) {
		this.allVersions = allVersions;
	}

	public String getInvariantUUID() {
		return invariantUUID;
	}

	public void setInvariantUUID(String invariantUUID) {
		if ( this.invariantUUID != null && !this.invariantUUID.equals( invariantUUID ) )
			log.warn("InvariantUUID changed more then once -> OLD : {} , NEW: {} ", this.invariantUUID ,invariantUUID );
		this.invariantUUID = invariantUUID;
	}

	public Boolean isDeleted() {
		return isDeleted;
	}

	public void setIsDeleted(Boolean isDeleted) {
		this.isDeleted = isDeleted;
	}

	public String getProjectCode() {
		return projectCode;
	}

	public void setProjectCode(String projectCode) {
		this.projectCode = projectCode;
	}

	public String getCsarUUID() {
		return csarUUID;
	}

	public void setCsarUUID(String csarUUID) {
		this.csarUUID = csarUUID;
	}

	public String getCsarVersion() {
		return csarVersion;
	}

	public void setCsarVersion(String csarVersion) {
		this.csarVersion = csarVersion;
	}

	public String getImportedToscaChecksum() {
		return importedToscaChecksum;
	}

	public void setImportedToscaChecksum(String importedToscaChecksum) {
		this.importedToscaChecksum = importedToscaChecksum;
	}
	
	public String getConformanceLevel() {
		return conformanceLevel;
	}

	public void setConformanceLevel(String conformanceLevel) {
		this.conformanceLevel = conformanceLevel;
	}

	public String getLifecycleState() {
		return state;
	}

	public void setLifecycleState(String state) {
		this.state = state;
	}
	
	@Override
	public String toString() {
		return "ComponentMetadataDataDefinition [uniqueId=" + uniqueId + ", name=" + name + ", version=" + version
				+ ", isHighestVersion=" + isHighestVersion + ", creationDate=" + creationDate + ", lastUpdateDate="
				+ lastUpdateDate + ", description=" + description + ", state=" + state + ", tags=" + tags
				+ ", conformanceLevel=" + conformanceLevel + ", icon=" + icon + ", UUID=" + UUID + ", normalizedName="
				+ normalizedName + ", systemName=" + systemName + ", contactId=" + contactId + ", allVersions="
				+ allVersions + ", isDeleted=" + isDeleted + ", projectCode=" + projectCode + ", csarUUID=" + csarUUID
				+ ", csarVersion=" + csarVersion + ", importedToscaChecksum=" + importedToscaChecksum
				+ ", invariantUUID=" + invariantUUID + ", componentType=" + componentType + ", creatorUserId="
				+ creatorUserId + ", creatorFullName=" + creatorFullName + ", lastUpdaterUserId=" + lastUpdaterUserId
				+ ", lastUpdaterFullName=" + lastUpdaterFullName + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((UUID == null) ? 0 : UUID.hashCode());
		result = prime * result + ((allVersions == null) ? 0 : allVersions.hashCode());
		result = prime * result + ((componentType == null) ? 0 : componentType.hashCode());
		result = prime * result + ((conformanceLevel == null) ? 0 : conformanceLevel.hashCode());
		result = prime * result + ((contactId == null) ? 0 : contactId.hashCode());
		result = prime * result + ((creationDate == null) ? 0 : creationDate.hashCode());
		result = prime * result + ((creatorFullName == null) ? 0 : creatorFullName.hashCode());
		result = prime * result + ((creatorUserId == null) ? 0 : creatorUserId.hashCode());
		result = prime * result + ((csarUUID == null) ? 0 : csarUUID.hashCode());
		result = prime * result + ((csarVersion == null) ? 0 : csarVersion.hashCode());
		result = prime * result + ((description == null) ? 0 : description.hashCode());
		result = prime * result + ((icon == null) ? 0 : icon.hashCode());
		result = prime * result + ((importedToscaChecksum == null) ? 0 : importedToscaChecksum.hashCode());
		result = prime * result + ((invariantUUID == null) ? 0 : invariantUUID.hashCode());
		result = prime * result + ((isDeleted == null) ? 0 : isDeleted.hashCode());
		result = prime * result + ((isHighestVersion == null) ? 0 : isHighestVersion.hashCode());
		result = prime * result + ((lastUpdateDate == null) ? 0 : lastUpdateDate.hashCode());
		result = prime * result + ((lastUpdaterFullName == null) ? 0 : lastUpdaterFullName.hashCode());
		result = prime * result + ((lastUpdaterUserId == null) ? 0 : lastUpdaterUserId.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((normalizedName == null) ? 0 : normalizedName.hashCode());
		result = prime * result + ((projectCode == null) ? 0 : projectCode.hashCode());
		result = prime * result + ((state == null) ? 0 : state.hashCode());
		result = prime * result + ((systemName == null) ? 0 : systemName.hashCode());
		result = prime * result + ((tags == null) ? 0 : tags.hashCode());
		result = prime * result + ((uniqueId == null) ? 0 : uniqueId.hashCode());
		result = prime * result + ((version == null) ? 0 : version.hashCode());
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
		ComponentMetadataDataDefinition other = (ComponentMetadataDataDefinition) obj;
		if (UUID == null) {
			if (other.UUID != null)
				return false;
		} else if (!UUID.equals(other.UUID))
			return false;
		if (allVersions == null) {
			if (other.allVersions != null)
				return false;
		} else if (!allVersions.equals(other.allVersions))
			return false;
		if (componentType != other.componentType)
			return false;
		if (conformanceLevel == null) {
			if (other.conformanceLevel != null)
				return false;
		} else if (!conformanceLevel.equals(other.conformanceLevel))
			return false;
		if (contactId == null) {
			if (other.contactId != null)
				return false;
		} else if (!contactId.equals(other.contactId))
			return false;
		if (creationDate == null) {
			if (other.creationDate != null)
				return false;
		} else if (!creationDate.equals(other.creationDate))
			return false;
		if (creatorFullName == null) {
			if (other.creatorFullName != null)
				return false;
		} else if (!creatorFullName.equals(other.creatorFullName))
			return false;
		if (creatorUserId == null) {
			if (other.creatorUserId != null)
				return false;
		} else if (!creatorUserId.equals(other.creatorUserId))
			return false;
		if (csarUUID == null) {
			if (other.csarUUID != null)
				return false;
		} else if (!csarUUID.equals(other.csarUUID))
			return false;
		if (csarVersion == null) {
			if (other.csarVersion != null)
				return false;
		} else if (!csarVersion.equals(other.csarVersion))
			return false;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		if (icon == null) {
			if (other.icon != null)
				return false;
		} else if (!icon.equals(other.icon))
			return false;
		if (importedToscaChecksum == null) {
			if (other.importedToscaChecksum != null)
				return false;
		} else if (!importedToscaChecksum.equals(other.importedToscaChecksum))
			return false;
		if (invariantUUID == null) {
			if (other.invariantUUID != null)
				return false;
		} else if (!invariantUUID.equals(other.invariantUUID))
			return false;
		if (isDeleted == null) {
			if (other.isDeleted != null)
				return false;
		} else if (!isDeleted.equals(other.isDeleted))
			return false;
		if (isHighestVersion == null) {
			if (other.isHighestVersion != null)
				return false;
		} else if (!isHighestVersion.equals(other.isHighestVersion))
			return false;
		if (lastUpdateDate == null) {
			if (other.lastUpdateDate != null)
				return false;
		} else if (!lastUpdateDate.equals(other.lastUpdateDate))
			return false;
		if (lastUpdaterFullName == null) {
			if (other.lastUpdaterFullName != null)
				return false;
		} else if (!lastUpdaterFullName.equals(other.lastUpdaterFullName))
			return false;
		if (lastUpdaterUserId == null) {
			if (other.lastUpdaterUserId != null)
				return false;
		} else if (!lastUpdaterUserId.equals(other.lastUpdaterUserId))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (normalizedName == null) {
			if (other.normalizedName != null)
				return false;
		} else if (!normalizedName.equals(other.normalizedName))
			return false;
		if (projectCode == null) {
			if (other.projectCode != null)
				return false;
		} else if (!projectCode.equals(other.projectCode))
			return false;
		if (state == null) {
			if (other.state != null)
				return false;
		} else if (!state.equals(other.state))
			return false;
		if (systemName == null) {
			if (other.systemName != null)
				return false;
		} else if (!systemName.equals(other.systemName))
			return false;
		if (tags == null) {
			if (other.tags != null)
				return false;
		} else if (!tags.equals(other.tags))
			return false;
		if (uniqueId == null) {
			if (other.uniqueId != null)
				return false;
		} else if (!uniqueId.equals(other.uniqueId))
			return false;
		if (version == null) {
			if (other.version != null)
				return false;
		} else if (!version.equals(other.version))
			return false;
		return true;
	}

}
