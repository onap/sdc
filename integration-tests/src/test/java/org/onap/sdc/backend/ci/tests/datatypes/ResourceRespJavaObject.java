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

package org.onap.sdc.backend.ci.tests.datatypes;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.openecomp.sdc.be.model.category.CategoryDefinition;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
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

	// Legacy alias kept for backward compatibility
	public String setUuid() {
		return uuid;
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
