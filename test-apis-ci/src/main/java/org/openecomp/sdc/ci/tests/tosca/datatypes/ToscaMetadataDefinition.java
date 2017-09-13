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

package org.openecomp.sdc.ci.tests.tosca.datatypes;

import org.yaml.snakeyaml.TypeDescription;

public class ToscaMetadataDefinition {

	private String invariantUUID;
	private String UUID;
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
	private String serviceEcompNaming;
	private String ecompGeneratedNaming;
	private String namingPolicy;
	
	public ToscaMetadataDefinition(String invariantUUID, String uUID, String name, String description, String type, String category, String subcategory, String resourceVendor, String resourceVendorRelease, String resourceVendorModelNumber,
			String serviceType, String serviceRole, String serviceEcompNaming, String ecompGeneratedNaming, String namingPolicy) {
		super();
		this.invariantUUID = invariantUUID;
		UUID = uUID;
		this.name = name;
		this.description = description;
		this.type = type;
		this.category = category;
		this.subcategory = subcategory;
		this.resourceVendor = resourceVendor;
		this.resourceVendorRelease = resourceVendorRelease;
		this.resourceVendorModelNumber = resourceVendorModelNumber;
		this.serviceType = serviceType;
		this.serviceRole = serviceRole;
		this.serviceEcompNaming = serviceEcompNaming;
		this.ecompGeneratedNaming = ecompGeneratedNaming;
		this.namingPolicy = namingPolicy;
	}

	public String getInvariantUUID() {
		return invariantUUID;
	}

	public void setInvariantUUID(String invariantUUID) {
		this.invariantUUID = invariantUUID;
	}

	public String getUUID() {
		return UUID;
	}

	public void setUUID(String uUID) {
		UUID = uUID;
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

	public String getServiceEcompNaming() {
		return serviceEcompNaming;
	}

	public void setServiceEcompNaming(String serviceEcompNaming) {
		this.serviceEcompNaming = serviceEcompNaming;
	}

	public String getEcompGeneratedNaming() {
		return ecompGeneratedNaming;
	}

	public void setEcompGeneratedNaming(String ecompGeneratedNaming) {
		this.ecompGeneratedNaming = ecompGeneratedNaming;
	}

	public String getNamingPolicy() {
		return namingPolicy;
	}

	public void setNamingPolicy(String namingPolicy) {
		this.namingPolicy = namingPolicy;
	}

	@Override
	public String toString() {
		return "ToscaMetadataDefinition [invariantUUID=" + invariantUUID + ", UUID=" + UUID + ", name=" + name + ", description=" + description + ", type=" + type + ", category=" + category + ", subcategory=" + subcategory
				+ ", resourceVendor=" + resourceVendor + ", resourceVendorRelease=" + resourceVendorRelease + ", resourceVendorModelNumber=" + resourceVendorModelNumber + ", serviceType=" + serviceType + ", serviceRole=" + serviceRole
				+ ", serviceEcompNaming=" + serviceEcompNaming + ", ecompGeneratedNaming=" + ecompGeneratedNaming + ", namingPolicy=" + namingPolicy + "]";
	}
	
	//gets Type description for Yaml snake
	public static TypeDescription getTypeDescription(){
        TypeDescription typeDescription = new TypeDescription(ToscaMetadataDefinition.class);
    	return typeDescription;
	}
	
}
