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

import org.openecomp.sdc.be.datatypes.components.ResourceMetadataDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.category.CategoryDefinition;

public class UiResourceMetadata extends UiComponentMetadata {
	
	private String vendorName;
	private String vendorRelease;
	private String resourceVendorModelNumber;
	private ResourceTypeEnum resourceType = ResourceTypeEnum.VFC;
	private Boolean isAbstract;
	private String cost;
	private String licenseType;
	private String toscaResourceName;
	private List<String> derivedFrom;
	
	
	public UiResourceMetadata(List<CategoryDefinition> categories, List<String> derivedFrom, ResourceMetadataDataDefinition metadata) {
		super(categories, metadata);
		this.vendorName = metadata.getVendorName();
		this.vendorRelease = metadata.getVendorRelease();
		this.resourceVendorModelNumber = metadata.getResourceVendorModelNumber();
		this.resourceType = metadata.getResourceType();
		this.cost = metadata.getCost();
		this.licenseType = metadata.getLicenseType();
		this.toscaResourceName = metadata.getToscaResourceName();
		this.derivedFrom = derivedFrom;
	}
	
	public UiResourceMetadata(){}
	
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
	
	public String getResourceVendorModelNumber() {
		return resourceVendorModelNumber;
	}

	public void setResourceVendorModelNumber(String resourceVendorModelNumber) {
		this.resourceVendorModelNumber = resourceVendorModelNumber;
	}

	public ResourceTypeEnum getResourceType() {
		return resourceType;
	}

	public void setResourceType(ResourceTypeEnum resourceType) {
		this.resourceType = resourceType;
	}

	public Boolean getIsAbstract() {
		return isAbstract;
	}

	public void setIsAbstract(Boolean isAbstract) {
		this.isAbstract = isAbstract;
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

	public String getToscaResourceName() {
		return toscaResourceName;
	}

	public void setToscaResourceName(String toscaResourceName) {
		this.toscaResourceName = toscaResourceName;
	}



}



