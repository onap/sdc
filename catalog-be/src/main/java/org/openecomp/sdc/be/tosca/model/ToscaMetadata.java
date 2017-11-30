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

package org.openecomp.sdc.be.tosca.model;

public class ToscaMetadata implements IToscaMetadata {
	private String invariantUUID;
	private String UUID;
	private String customizationUUID;
	private String version;
	private String name;
	private String description;
	private String type;
	private String category;
	private String subcategory;
	private String resourceVendor;
	private String resourceVendorRelease;
	private String resourceVendorModelNumber;
	private String serviceType;
	private String serviceRole;
	private Boolean serviceEcompNaming;
	private Boolean ecompGeneratedNaming;
	private String namingPolicy;
	private String sourceModelInvariant;
	private String environmentContext;
	private String sourceModelName;
	private String sourceModelUuid;
	
	
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	public String getInvariantUUID() {
		return invariantUUID;
	}

	@Override
	public void setInvariantUUID(String invariantUUID) {
		this.invariantUUID = invariantUUID;
	}

	public String getUUID() {
		return UUID;
	}

	@Override
	public void setUUID(String uUID) {
		UUID = uUID;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
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

	public String getResourceVendor() {
		return resourceVendor;
	}

	public void setResourceVendor(String resourceVendor) {
		this.resourceVendor = resourceVendor;
	}

	public String getResourceVendorRelease() {
		return resourceVendorRelease;
	}

	public void setResourceVendorRelease(String resourceVendorRelease) {
		this.resourceVendorRelease = resourceVendorRelease;
	}
	
	public String getResourceVendorModelNumber() {
		return resourceVendorModelNumber;
	}
	
	public void setResourceVendorModelNumber(String resourceVendorModelNumber) {
		this.resourceVendorModelNumber = resourceVendorModelNumber;
	}
	
	public String getServiceType() {
		return serviceType;
	}
	
	public void setServiceType(String serviceType) {
		this.serviceType = serviceType;
	}
	
	public String getServiceRole() {
		return serviceRole;
	}
	
	public void setServiceRole(String serviceRole) {
		this.serviceRole = serviceRole;
	}

	public Boolean isEcompGeneratedNaming() {
		return ecompGeneratedNaming;
	}

	public void setEcompGeneratedNaming(Boolean ecompGeneratedNaming) {
		this.ecompGeneratedNaming = ecompGeneratedNaming;
	}

	public String isNamingPolicy() {
		return namingPolicy;
	}

	public void setNamingPolicy(String namingPolicy) {
		this.namingPolicy = namingPolicy;
	}

	public Boolean getServiceEcompNaming() {
		return serviceEcompNaming;
	}

	public void setServiceEcompNaming(Boolean serviceEcompNaming) {
		this.serviceEcompNaming = serviceEcompNaming;
	}

	public String getVersion() {
		return version;
	}

	@Override
	public void setVersion(String version) {
		this.version = version;
	}

	public String getCustomizationUUID() {
		return customizationUUID;
	}

	public void setCustomizationUUID(String customizationUUID) {
		this.customizationUUID = customizationUUID;
	}

	public String getSourceModelInvariant() {
		return sourceModelInvariant;
	}

	public void setSourceModelInvariant(String sourceModelInvariant) {
		this.sourceModelInvariant = sourceModelInvariant;
	}

	public String getSourceModelName() {
		return sourceModelName;
	}

	public void setSourceModelName(String sourceModelName) {
		this.sourceModelName = sourceModelName;
	}

	public String getSourceModelUuid() {
		return sourceModelUuid;
	}

	public void setSourceModelUuid(String sourceModelUuid) {
		this.sourceModelUuid = sourceModelUuid;
	}
	
	

	public String getEnvironmentContext() {
		return environmentContext;
	}

	public void setEnvironmentContext(String environmentContext) {
		this.environmentContext = environmentContext;
	}
}
