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

package org.openecomp.sdc.be.ui.model;

import java.util.List;
import java.util.Map;

import org.openecomp.sdc.be.datatypes.components.ComponentMetadataDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.model.category.CategoryDefinition;

public abstract class UiComponentMetadata {

	private String uniqueId;

	private String name; // archiveName

	private String version; // archiveVersion

	private Boolean isHighestVersion;

	private Long creationDate;

	private Long lastUpdateDate;

	private String description;

	private String lifecycleState;

	private List<String> tags;

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

	private ComponentTypeEnum componentType;
	
	private List<CategoryDefinition> categories;
	
	private String creatorUserId;
	
	private String creatorFullName;
	
	private String lastUpdaterUserId;
	
	private String lastUpdaterFullName;

	public UiComponentMetadata(){}

	public UiComponentMetadata (List<CategoryDefinition> categories, ComponentMetadataDataDefinition metadata) {
	
		this.uniqueId = metadata.getUniqueId();
		this.name = metadata.getName(); // archiveName
		this.version = metadata.getVersion();
		this.isHighestVersion = metadata.isHighestVersion();
		this.creationDate = metadata.getCreationDate();
		this.lastUpdateDate = metadata.getLastUpdateDate();
		this.description = metadata.getDescription();
		this.lifecycleState = metadata.getState();
		this.tags = metadata.getTags();
		this.icon = metadata.getIcon();
		this.UUID = metadata.getUUID();
		this.normalizedName = metadata.getNormalizedName();
		this.systemName = metadata.getSystemName();
		this.contactId = metadata.getContactId();
		this.allVersions = metadata.getAllVersions();
		this.projectCode = metadata.getProjectCode();
		this.csarUUID = metadata.getCsarUUID();
		this.csarVersion = metadata.getCsarVersion();
		this.importedToscaChecksum = metadata.getImportedToscaChecksum();
		this.invariantUUID = metadata.getInvariantUUID();
		this.componentType = metadata.getComponentType();
		this.categories = categories;
		this.creatorUserId = metadata.getCreatorUserId();
		this.creatorFullName = metadata.getCreatorFullName();
		this.lastUpdaterFullName = metadata.getLastUpdaterFullName();
		this.lastUpdaterUserId = metadata.getLastUpdaterUserId();

	}
	
	
	
	public List<CategoryDefinition> getCategories() {
		return categories;
	}

	public void setCategories(List<CategoryDefinition> categories) {
		this.categories = categories;
	}

	
	public String getUniqueId() {
		return uniqueId;
	}

	public void setUniqueId(String uniqueId) {
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

	public Boolean getIsHighestVersion() {
		return isHighestVersion;
	}

	public void setIsHighestVersion(Boolean isHighestVersion) {
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

	public String getLifecycleState() {
		return lifecycleState;
	}

	public void setLifecycleState(String state) {
		this.lifecycleState = state;
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

	public String getUUID() {
		return UUID;
	}

	public void setUUID(String uUID) {
		UUID = uUID;
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

	public String getContactId() {
		return contactId;
	}

	public void setContactId(String contactId) {
		this.contactId = contactId;
	}

	public Map<String, String> getAllVersions() {
		return allVersions;
	}

	public void setAllVersions(Map<String, String> allVersions) {
		this.allVersions = allVersions;
	}

	public Boolean getIsDeleted() {
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

	public String getInvariantUUID() {
		return invariantUUID;
	}

	public void setInvariantUUID(String invariantUUID) {
		this.invariantUUID = invariantUUID;
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

}
