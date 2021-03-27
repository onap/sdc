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

import lombok.EqualsAndHashCode;
import org.openecomp.sdc.be.datatypes.enums.JsonPresentationFieldsExtractor;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;

@EqualsAndHashCode(callSuper = true)
public class ResourceMetadataDataDefinition extends ComponentMetadataDataDefinition {

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

    public ResourceMetadataDataDefinition(JsonPresentationFieldsExtractor extractor) {
        super(extractor);
        this.vendorName = extractor.getVendorName();
        this.vendorRelease = extractor.getVendorRelease();
        this.resourceVendorModelNumber = extractor.getResourceVendorModelNumber();
        this.isAbstract = extractor.isAbstract();
        this.resourceType = extractor.getResourceType();
        this.toscaResourceName = extractor.getToscaResourceName();
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
        return "ResourceMetadataDataDefinition [vendorName=" + vendorName + ", vendorRelease=" + vendorRelease + ", resourceVendorModelNumber="
            + resourceVendorModelNumber + ", resourceType=" + resourceType + ", isAbstract=" + isAbstract + super.toString() + "]";
    }

    @Override
    public String getActualComponentType() {
        return getResourceType().getValue();
    }
}
