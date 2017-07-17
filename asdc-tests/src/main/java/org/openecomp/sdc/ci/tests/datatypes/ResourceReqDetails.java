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

import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.Resource;

public class ResourceReqDetails extends ComponentReqDetails {
	List<String> derivedFrom;
	String vendorName;
	String vendorRelease;
	String resourceVendorModelNumber;

	// Unsettable/unupdatable fields

	Boolean isAbstract;
	Boolean isHighestVersion;
	String cost;
	String licenseType;
	String toscaResourceName;

	private String resourceType = ResourceTypeEnum.VFC.toString(); // Default
																	// value

	public ResourceReqDetails() {
		super();
	}

	public ResourceReqDetails(Resource resource) {
		super();
		this.resourceType = resource.getResourceType().toString();
		this.name = resource.getName();
		this.description = resource.getDescription();
		this.tags = resource.getTags();
		// this.category = resource.getCategories();
		this.derivedFrom = resource.getDerivedFrom();
		this.vendorName = resource.getVendorName();
		this.vendorRelease = resource.getVendorRelease();
		this.resourceVendorModelNumber = resource.getResourceVendorModelNumber();
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

	}

	public ResourceReqDetails(String resourceName, String description, List<String> tags, String category,
			List<String> derivedFrom, String vendorName, String vendorRelease, String contactId, String icon) {
		this(resourceName, description, tags, category, derivedFrom, vendorName, vendorRelease, contactId, icon,
				ResourceTypeEnum.VFC.toString());
	}

	// new
	public ResourceReqDetails(String resourceName, String description, List<String> tags, String category,
			List<String> derivedFrom, String vendorName, String vendorRelease, String contactId, String icon,
			String resourceType) {
		super();
		this.resourceType = resourceType;
		this.name = resourceName;
		this.description = description;
		this.tags = tags;
		// this.category = category;
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
		// this.category = originalResource.getCategory();
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

	public String getToscaResourceName() {
		return toscaResourceName;
	}

	public void setToscaResourceName(String toscaResourceName) {
		this.toscaResourceName = toscaResourceName;
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

	// Unupdatable fields - to check that they are not updated
	public void setIsAbstract(Boolean isAbstract) {
		this.isAbstract = isAbstract;
	}

	public void setIsHighestVersion(Boolean isHighestVersion) {
		this.isHighestVersion = isHighestVersion;
	}

	public Boolean getIsAbstract() {
		return isAbstract;
	}

	public Boolean getIsHighestVersion() {
		return isHighestVersion;
	}

	public String getResourceType() {
		return resourceType;
	}

	public void setResourceType(String resourceType) {
		this.resourceType = resourceType;
	}

	@Override
	public String toString() {
		return "ResourceReqDetails [name=" + name + ", derivedFrom=" + derivedFrom + ", vendorName=" + vendorName
				+ ", vendorRelease=" + vendorRelease + ", version=" + version + ", isAbstract=" + isAbstract
				+ ", isHighestVersion=" + isHighestVersion + ", cost=" + cost + ", licenseType=" + licenseType
				+ ", resourceType=" + resourceType + "]";
	}

}
