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

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.Resource;

import java.util.List;

@Getter
@Setter
@ToString(callSuper = false)
public class ResourceReqDetails extends ComponentReqDetails {

	private List<String> derivedFrom;
	private String vendorName;
	private String vendorRelease;
	@Setter(AccessLevel.NONE)
	private String componentType = "RESOURCE";
	private String csarVersionId;
	private Boolean isAbstract;
	private Boolean isHighestVersion;
	private String cost;
	private String licenseType;
	private String toscaResourceName;
	private String resourceVendorModelNumber;
	private String resourceType = ResourceTypeEnum.VFC.toString();

	public ResourceReqDetails() {
		super();
	}

	public ResourceReqDetails(List<String> derivedFrom, String vendorName, String vendorRelease, Boolean isAbstract, Boolean isHighestVersion, String cost, String licenseType, String toscaResourceName, String resourceVendorModelNumber,
			String resourceType) {
		super();
		this.derivedFrom = derivedFrom;
		this.vendorName = vendorName;
		this.vendorRelease = vendorRelease;
		this.isAbstract = isAbstract;
		this.isHighestVersion = isHighestVersion;
		this.cost = cost;
		this.licenseType = licenseType;
		this.toscaResourceName = toscaResourceName;
		this.resourceVendorModelNumber = resourceVendorModelNumber;
		this.resourceType = resourceType;
	}

	public ResourceReqDetails(Resource resource) {
		super();
		this.resourceType = resource.getResourceType().toString();
		this.name = resource.getName();
		this.description = resource.getDescription();
		this.tags = resource.getTags();
		this.derivedFrom = resource.getDerivedFrom();
		this.vendorName = resource.getVendorName();
		this.vendorRelease = resource.getVendorRelease();
		this.contactId = resource.getContactId();
		this.icon = resource.getIcon();
		this.toscaResourceName = resource.getToscaResourceName();
		this.uniqueId = resource.getUniqueId();
		this.creatorUserId = resource.getCreatorUserId();
		this.creatorFullName = resource.getCreatorFullName();
		this.lastUpdaterUserId = resource.getLastUpdaterUserId();
		this.lastUpdaterFullName = resource.getLastUpdaterFullName();
		this.lifecycleState = resource.getLifecycleState();
		this.version = resource.getVersion();
		this.UUID = resource.getUUID();
		this.categories = resource.getCategories();
		this.importedToscaChecksum = resource.getImportedToscaChecksum();
		this.resourceVendorModelNumber = resource.getResourceVendorModelNumber();

	}

	public ResourceReqDetails(String resourceName, String description, List<String> tags, String category,
			List<String> derivedFrom, String vendorName, String vendorRelease, String contactId, String icon) {
		this(resourceName, description, tags, category, derivedFrom, vendorName, vendorRelease, contactId, icon,
				ResourceTypeEnum.VFC.toString());
	}

	public ResourceReqDetails(String resourceName, String description, List<String> tags, String category,
			List<String> derivedFrom, String vendorName, String vendorRelease, String contactId, String icon,
			String resourceType) {
		super();
		this.resourceType = resourceType;
		this.name = resourceName;
		this.description = description;
		this.tags = tags;
		this.derivedFrom = derivedFrom;
		this.vendorName = vendorName;
		this.vendorRelease = vendorRelease;
		this.contactId = contactId;
		this.icon = icon;
		if (category != null) {
			String[] arr = category.split("/");
			if (arr.length == 2) {
				addCategoryChain(arr[0], arr[1]);
			}
		}
		this.toscaResourceName = resourceName;
	}

	public ResourceReqDetails(ResourceReqDetails originalResource, String version) {
		super();
		this.name = originalResource.getName();
		this.description = originalResource.getDescription();
		this.tags = originalResource.getTags();
		this.derivedFrom = originalResource.getDerivedFrom();
		this.vendorName = originalResource.getVendorName();
		this.vendorRelease = originalResource.getVendorRelease();
		this.contactId = originalResource.getContactId();
		this.icon = originalResource.getIcon();
		this.version = version;
		this.uniqueId = originalResource.getUniqueId();
		this.categories = originalResource.getCategories();
		this.toscaResourceName = originalResource.getToscaResourceName();
		this.resourceType = originalResource.getResourceType();
	}

	public ResourceReqDetails(String resourceName, List<String> derivedFrom, String vendorName, String vendorRelease,
			String resourceVersion, Boolean isAbstract, Boolean isHighestVersion, String cost, String licenseType,
			String resourceType) {
		super();
		this.name = resourceName;
		this.derivedFrom = derivedFrom;
		this.vendorName = vendorName;
		this.vendorRelease = vendorRelease;
		this.version = resourceVersion;
		this.isAbstract = isAbstract;
		this.isHighestVersion = isHighestVersion;
		this.cost = cost;
		this.licenseType = licenseType;
		this.resourceType = resourceType;
		this.toscaResourceName = resourceName;
	}

}