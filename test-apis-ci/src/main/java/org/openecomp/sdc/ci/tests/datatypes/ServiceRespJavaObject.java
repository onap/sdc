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

import java.util.ArrayList;
import java.util.List;

public class ServiceRespJavaObject {

	String category;
	String creatorUserId;
	String creatorFullName;
	String lastUpdaterUserId;
	String lastUpdaterFullName;
	String serviceName;
	String version;
	String creationDate;
	String icon;
	String name;
	String description;
	ArrayList<String> tags;
	String uniqueId;
	String lastUpdateDate;
	String contactId;
	String vendorName;
	String vendorRelease;
	String lifecycleState;
	String highestVersion;
	ArrayList<String> artifacts;
	ArrayList<String> ResourceInstances;
	ArrayList<String> ResourceInstancesRelations;

	public ServiceRespJavaObject() {
		super();
	}

	public ServiceRespJavaObject(String category, String creatorUserId, String creatorFullName,
			String lastUpdaterUserId, String lastUpdaterFullName, String serviceName, String version,
			String creationDate, String icon, String name, String description, ArrayList<String> tags, String uniqueId,
			String lastUpdateDate, String contactId, String vendorName, String vendorRelease, String lifecycleState,
			String highestVersion, ArrayList<String> artifacts, ArrayList<String> resourceInstances,
			ArrayList<String> resourceInstancesRelations) {
		super();
		this.category = category;
		this.creatorUserId = creatorUserId;
		this.creatorFullName = creatorFullName;
		this.lastUpdaterUserId = lastUpdaterUserId;
		this.lastUpdaterFullName = lastUpdaterFullName;
		this.serviceName = serviceName;
		this.version = version;
		this.creationDate = creationDate;
		this.icon = icon;
		this.name = name;
		this.description = description;
		this.tags = tags;
		this.uniqueId = uniqueId;
		this.lastUpdateDate = lastUpdateDate;
		this.contactId = contactId;
		this.vendorName = vendorName;
		this.vendorRelease = vendorRelease;
		this.lifecycleState = lifecycleState;
		this.highestVersion = highestVersion;
		this.artifacts = artifacts;
		ResourceInstances = resourceInstances;
		ResourceInstancesRelations = resourceInstancesRelations;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
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

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(String creationDate) {
		this.creationDate = creationDate;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public ArrayList<String> getTags() {
		return tags;
	}

	public void setTags(ArrayList<String> tags) {
		this.tags = tags;
	}

	public String getUniqueId() {
		return uniqueId;
	}

	public void setUniqueId(String uniqueId) {
		this.uniqueId = uniqueId;
	}

	public String getLastUpdateDate() {
		return lastUpdateDate;
	}

	public void setLastUpdateDate(String lastUpdateDate) {
		this.lastUpdateDate = lastUpdateDate;
	}

	public String getContactId() {
		return contactId;
	}

	public void setContactId(String contactId) {
		this.contactId = contactId;
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

	public String getLifecycleState() {
		return lifecycleState;
	}

	public void setLifecycleState(String lifecycleState) {
		this.lifecycleState = lifecycleState;
	}

	public String getHighestVersion() {
		return highestVersion;
	}

	public void setHighestVersion(String highest) {
		this.highestVersion = highest;
	}

	public ArrayList<String> getArtifacts() {
		return artifacts;
	}

	public void setArtifacts(ArrayList<String> artifacts) {
		this.artifacts = artifacts;
	}

	public ArrayList<String> getResourceInstances() {
		return ResourceInstances;
	}

	public void setResourceInstances(ArrayList<String> resourceInstances) {
		ResourceInstances = resourceInstances;
	}

	public ArrayList<String> getResourceInstancesRelations() {
		return ResourceInstancesRelations;
	}

	public void setResourceInstancesRelations(ArrayList<String> resourceInstancesRelations) {
		ResourceInstancesRelations = resourceInstancesRelations;
	}

	@Override
	public String toString() {
		return "ServiceRespJavaObject [category=" + category + ", creatorUserId=" + creatorUserId + ", creatorFullName="
				+ creatorFullName + ", lastUpdaterUserId=" + lastUpdaterUserId + ", lastUpdaterFullName="
				+ lastUpdaterFullName + ", serviceName=" + serviceName + ", version=" + version + ", creationDate="
				+ creationDate + ", icon=" + icon + ", name=" + name + ", description=" + description + ", tags=" + tags
				+ ", uniqueId=" + uniqueId + ", lastUpdateDate=" + lastUpdateDate + ", contactId=" + contactId
				+ ", vendorName=" + vendorName + ", vendorRelease=" + vendorRelease + ", lifecycleState="
				+ lifecycleState + ", lifecycleState=" + lifecycleState + ", artifacts=" + artifacts
				+ ", ResourceInstances=" + ResourceInstances + ", ResourceInstancesRelations="
				+ ResourceInstancesRelations + "]";
	}

}
