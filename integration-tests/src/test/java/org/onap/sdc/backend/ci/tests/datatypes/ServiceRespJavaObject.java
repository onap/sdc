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

import java.util.ArrayList;

@Getter
@Setter
@NoArgsConstructor
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
