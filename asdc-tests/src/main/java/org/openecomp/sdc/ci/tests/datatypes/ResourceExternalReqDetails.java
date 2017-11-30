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

public class ResourceExternalReqDetails extends ComponentReqDetails {
	String vendorName;
	String vendorRelease;
	String category;
	String subcategory;
	
	private String resourceType = ResourceTypeEnum.VFC.toString(); // Default
																	// value
	public ResourceExternalReqDetails() {
		super();
	}
	
	
	public ResourceExternalReqDetails(String resourceName, String description, List<String> tags,
			String vendorName, String vendorRelease, String contactId, String icon,
			String resourceType, String resourceCategory, String resourceSubcategory) {
		super();
		this.resourceType = resourceType;
		this.name = resourceName;
		this.description = description;
		this.tags = tags;
		this.vendorName = vendorName;
		this.vendorRelease = vendorRelease;
		this.contactId = contactId;
		this.icon = icon;
		this.category = resourceCategory;
		this.subcategory = resourceSubcategory;
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
	
	public String getResourceType() {
		return resourceType;
	}

	public void setResourceType(String resourceType) {
		this.resourceType = resourceType;
	}
	
	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getSubcategory() {
		return subcategory;
	}

	public void setSubcategory(String subcategory) {
		this.subcategory = subcategory;
	}
	

	@Override
	public String toString() {
		return "ResourceReqDetails [name=" + name + ", vendorName=" + vendorName
				+ ", vendorRelease=" + vendorRelease + ", version=" + version
				+ ", resourceType=" + resourceType + ", category=" + category + ", subcategory=" + subcategory +"]";
	}

}
