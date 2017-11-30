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

package org.openecomp.sdc.ci.tests.datatypes;

import java.util.List;

import org.openecomp.sdc.be.model.category.CategoryDefinition;

public class ResourceRespJavaObject {
	String uniqueId;
	String name;
	String version;
	String creatorUserId;
	String creatorFullName;
	String lastUpdaterUserId;
	String lastUpdaterFullName;
	String description;
	String icon;
	List<String> tags;
	String isHighestVersion;
	String creationDate;
	String lastUpdateDate;
	// String category;
	String lifecycleState;
	List<String> derivedFrom;
	String vendorName;
	String vendorRelease;
	String contactId;
	String abstractt;
	String highestVersion;
	List<String> artifacts;
	List<String> interfaces;
	String uuid;
	String cost;
	String licenseType;
	String resourceType;
	List<CategoryDefinition> categories;

	public String getResourceType() {
		return resourceType;
	}

	public void setResourceType(String resourceType) {
		this.resourceType = resourceType;
	}

	public ResourceRespJavaObject(String uniqueId, String resourceName, String resourceVersion, String creatorUserId,
			String creatorFullName, String lastUpdaterUserId, String lastUpdaterFullName, String description,
			String icon, List<String> tags, String isHighestVersion, String creationDate, String lastUpdateDate,
			String category, String lifecycleState, List<String> derivedFrom, String vendorName, String vendorRelease,
			String contactId, String abstractt, String highestVersion, List<String> artifacts, List<String> interfaces,
			String uuid, String cost, String licenseType, String resourceType) {
		super();
		this.uniqueId = uniqueId;
		this.name = resourceName;
		this.version = resourceVersion;
		this.creatorUserId = creatorUserId;
		this.creatorFullName = creatorFullName;
		this.lastUpdaterUserId = lastUpdaterUserId;
		this.lastUpdaterFullName = lastUpdaterFullName;
		this.description = description;
		this.icon = icon;
		this.tags = tags;
		this.isHighestVersion = isHighestVersion;
		this.creationDate = creationDate;
		this.lastUpdateDate = lastUpdateDate;
		// this.category = category;
		this.lifecycleState = lifecycleState;
		this.derivedFrom = derivedFrom;
		this.vendorName = vendorName;
		this.vendorRelease = vendorRelease;
		this.contactId = contactId;
		this.abstractt = abstractt;
		this.highestVersion = highestVersion;
		this.artifacts = artifacts;
		this.interfaces = interfaces;
		this.uuid = uuid;
		this.cost = cost;
		this.licenseType = licenseType;
		this.resourceType = resourceType;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getCost() {
		return cost;
	}

	public void setCost(String cost) {
		this.cost = cost;
	}

	public String getLicenseType() {
		return licenseType;
	}

	public void setLicenseType(String licenseType) {
		this.licenseType = licenseType;
	}

	public String getUuid() {
		return uuid;
	}

	public String setUuid() {
		return uuid;
	}

	public List<String> getInterfaces() {
		return interfaces;
	}

	public void setInterfaces(List<String> interfaces) {
		this.interfaces = interfaces;
	}

	public List<String> getArtifacts() {
		return artifacts;
	}

	public void setArtifacts(List<String> artifacts) {
		this.artifacts = artifacts;
	}

	public ResourceRespJavaObject() {
		super();
	}

	// public ResourceRespJavaObject(String uniqueId, String resourceName,
	// String resourceVersion, String creatorUserId,
	// String creatorFullName, String lastUpdaterUserId,
	// String lastUpdaterFullName, String description, String icon,
	// List<String> tags, String isHighestVersion, String creationDate,
	// String lastUpdateDate, String category, String lifecycleState,
	// List<String> derivedFrom, String vendorName, String vendorRelease,
	// String contactId, String abstractt, String highestVersion) {
	// super();
	// this.uniqueId = uniqueId;
	// this.resourceName = resourceName;
	// this.resourceVersion = resourceVersion;
	// this.creatorUserId = creatorUserId;
	// this.creatorFullName = creatorFullName;
	// this.lastUpdaterUserId = lastUpdaterUserId;
	// this.lastUpdaterFullName = lastUpdaterFullName;
	// this.description = description;
	// this.icon = icon;
	// this.tags = tags;
	// this.isHighestVersion = isHighestVersion;
	// this.creationDate = creationDate;
	// this.lastUpdateDate = lastUpdateDate;
	// this.category = category;
	// this.lifecycleState = lifecycleState;
	// this.derivedFrom = derivedFrom;
	// this.vendorName = vendorName;
	// this.vendorRelease = vendorRelease;
	// this.contactId = contactId;
	// this.abstractt = abstractt;
	// this.highestVersion = highestVersion;
	// }
	public String getUniqueId() {
		return uniqueId;
	}

	public void setUniqueId(String uniqueId) {
		this.uniqueId = uniqueId;
	}

	public String getName() {
		return name;
	}

	public void setName(String resourceName) {
		this.name = resourceName;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String resourceVersion) {
		this.version = resourceVersion;
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

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public List<String> getTags() {
		return tags;
	}

	public void setTags(List<String> tags) {
		this.tags = tags;
	}

	public String getIsHighestVersion() {
		return isHighestVersion;
	}

	public void setIsHighestVersion(String isHighestVersion) {
		this.isHighestVersion = isHighestVersion;
	}

	public String getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(String creationDate) {
		this.creationDate = creationDate;
	}

	public String getLastUpdateDate() {
		return lastUpdateDate;
	}

	public void setLastUpdateDate(String lastUpdateDate) {
		this.lastUpdateDate = lastUpdateDate;
	}

	// public String getCategory() {
	// return category;
	// }
	// public void setCategory(String category) {
	// this.category = category;
	// }
	public String getLifecycleState() {
		return lifecycleState;
	}

	public void setLifecycleState(String lifecycleState) {
		this.lifecycleState = lifecycleState;
	}

	public List<String> getDerivedFrom() {
		return derivedFrom;
	}

	public void setDerivedFrom(List<String> derivedFrom) {
		this.derivedFrom = derivedFrom;
	}

	public String getVendorName() {
		return vendorName;
	}

	public void setVendorName(String vendorName) {
		this.vendorName = vendorName;
	}

	public String getVendorRelease() {
		return vendorRelease;
	}

	public void setVendorRelease(String vendorRelease) {
		this.vendorRelease = vendorRelease;
	}

	public String getContactId() {
		return contactId;
	}

	public void setContactId(String contactId) {
		this.contactId = contactId;
	}

	public String getAbstractt() {
		return abstractt;
	}

	public void setAbstractt(String abstractt) {
		this.abstractt = abstractt;
	}

	public String getHighestVersion() {
		return highestVersion;
	}

	public void setHighestVersion(String highestVersion) {
		this.highestVersion = highestVersion;
	}

	public List<CategoryDefinition> getCategories() {
		return categories;
	}

	public void setCategories(List<CategoryDefinition> categories) {
		this.categories = categories;
	}

	@Override
	public String toString() {
		return "ResourceRespJavaObject [uniqueId=" + uniqueId + ", resourceName=" + name + ", resourceVersion="
				+ version + ", creatorUserId=" + creatorUserId + ", creatorFullName=" + creatorFullName
				+ ", lastUpdaterUserId=" + lastUpdaterUserId + ", lastUpdaterFullName=" + lastUpdaterFullName
				+ ", description=" + description + ", icon=" + icon + ", tags=" + tags + ", isHighestVersion="
				+ isHighestVersion + ", creationDate=" + creationDate + ", lastUpdateDate=" + lastUpdateDate
				+ ", lifecycleState=" + lifecycleState + ", derivedFrom=" + derivedFrom + ", vendorName=" + vendorName
				+ ", vendorRelease=" + vendorRelease + ", contactId=" + contactId + ", abstractt=" + abstractt
				+ ", highestVersion=" + highestVersion + ", artifacts=" + artifacts + ", interfaces=" + interfaces
				+ ", uuid=" + uuid + ", cost=" + cost + ", licenseType=" + licenseType + ", resourceType="
				+ resourceType + "]";
	}

}
