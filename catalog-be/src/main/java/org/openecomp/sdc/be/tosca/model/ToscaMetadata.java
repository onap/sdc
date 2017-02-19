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
	private String version;
	private String name;
	private String description;
	private String type;
	private String category;
	private String subcategory;
	private String resourceVendor;
	private String resourceVendorRelease;
	private Boolean serviceEcompNaming;
	private Boolean serviceHoming;

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

	public Boolean isServiceEcompNaming() {
		return serviceEcompNaming;
	}

	public void setServiceEcompNaming(Boolean serviceEcompNaming) {
		this.serviceEcompNaming = serviceEcompNaming;
	}

	public Boolean isServiceHoming() {
		return serviceHoming;
	}

	public void setServiceHoming(Boolean serviceHoming) {
		this.serviceHoming = serviceHoming;
	}

	public String getVersion() {
		return version;
	}

	@Override
	public void setVersion(String version) {
		this.version = version;
	}

}
