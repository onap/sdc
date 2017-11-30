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

package org.openecomp.sdc.be.datatypes.components;

import java.io.Serializable;

import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;

public class ResourceMetadataDataDefinition extends ComponentMetadataDataDefinition implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1142973528643758481L;

	private String vendorName;
	private String vendorRelease;
	private String resourceVendorModelNumber;
	private ResourceTypeEnum resourceType = ResourceTypeEnum.VFC; // ResourceType.VFC
																	// is
																	// default
	private Boolean isAbstract;
	private String cost;
	private String licenseType;
	private String toscaResourceName;

	public ResourceMetadataDataDefinition() {
		super();
		resourceVendorModelNumber = "";
	}

	public ResourceMetadataDataDefinition(ResourceMetadataDataDefinition other) {
		super(other);
		this.vendorName = other.getVendorName();
		this.vendorRelease = other.getVendorRelease();
		this.resourceVendorModelNumber = other.getResourceVendorModelNumber();
		this.isAbstract = other.isHighestVersion();
		this.resourceType = other.getResourceType();
		this.toscaResourceName = other.getToscaResourceName();
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

	public Boolean isAbstract() {
		return isAbstract;
	}

	public void setAbstract(Boolean isAbstract) {
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

	@Override
	public String toString() {
		return "ResourceMetadataDataDefinition [vendorName=" + vendorName + ", vendorRelease=" + vendorRelease
				+ ", resourceVendorModelNumber=" + resourceVendorModelNumber + ", resourceType=" + resourceType +
				", isAbstract=" + isAbstract + super.toString() + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((cost == null) ? 0 : cost.hashCode());
		result = prime * result + ((isAbstract == null) ? 0 : isAbstract.hashCode());
		result = prime * result + ((licenseType == null) ? 0 : licenseType.hashCode());
		result = prime * result + ((resourceType == null) ? 0 : resourceType.hashCode());
		result = prime * result + ((vendorName == null) ? 0 : vendorName.hashCode());
		result = prime * result + ((vendorRelease == null) ? 0 : vendorRelease.hashCode());
		result = prime * result + ((resourceVendorModelNumber == null)? 0 : resourceVendorModelNumber.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		ResourceMetadataDataDefinition other = (ResourceMetadataDataDefinition) obj;
		if (cost == null) {
			if (other.cost != null)
				return false;
		} else if (!cost.equals(other.cost))
			return false;
		if (isAbstract == null) {
			if (other.isAbstract != null)
				return false;
		} else if (!isAbstract.equals(other.isAbstract))
			return false;
		if (licenseType == null) {
			if (other.licenseType != null)
				return false;
		} else if (!licenseType.equals(other.licenseType))
			return false;
		if (resourceType != other.resourceType)
			return false;
		if (vendorName == null) {
			if (other.vendorName != null)
				return false;
		} else if (!vendorName.equals(other.vendorName))
			return false;
		if (vendorRelease == null) {
			if (other.vendorRelease != null)
				return false;
		}
		if (toscaResourceName == null) {
			if (other.toscaResourceName != null)
				return false;
		} else if (!vendorRelease.equals(other.vendorRelease))
			return false;
		if (resourceVendorModelNumber == null) {
			if (other.resourceVendorModelNumber != null)
				return false;
		} else if (!resourceVendorModelNumber.equals(other.resourceVendorModelNumber))
			return false;

		return super.equals(obj);
	}

}
