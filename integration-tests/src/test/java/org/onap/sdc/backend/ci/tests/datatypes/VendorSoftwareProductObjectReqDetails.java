/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

public class VendorSoftwareProductObjectReqDetails {

    private String name;
    private String description;
    private String category;
    private String subCategory;
    private String vendorId;
    private String vendorName;
//    private LicensingVersion licensingVersion;
    private String licensingVersion;
    private LicensingData licensingData;
    private String onboardingMethod;
    private String networkPackageName;
    private String onboardingOrigin;
    private String icon;


    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public VendorSoftwareProductObjectReqDetails() {
    }

    public VendorSoftwareProductObjectReqDetails(String name, String description, String category, String subCategory, String vendorId, String vendorName, String licensingVersion, LicensingData licensingData, String onboardingMethod, String networkPackageName, String onboardingOrigin, String icon) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.subCategory = subCategory;
        this.vendorId = vendorId;
        this.vendorName = vendorName;
        this.licensingVersion = licensingVersion;
        this.licensingData = licensingData;
        this.onboardingMethod = onboardingMethod;
        this.networkPackageName = networkPackageName;
        this.onboardingOrigin = onboardingOrigin;
        this.icon = icon;
    }

    public String getLicensingVersion() {
        return licensingVersion;
    }

    public void setLicensingVersion(String licensingVersion) {
        this.licensingVersion = licensingVersion;
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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getSubCategory() {
        return subCategory;
    }

    public void setSubCategory(String subCategory) {
        this.subCategory = subCategory;
    }

    public String getVendorId() {
        return vendorId;
    }

    public void setVendorId(String vendorId) {
        this.vendorId = vendorId;
    }

    public String getVendorName() {
        return vendorName;
    }

    public void setVendorName(String vendorName) {
        this.vendorName = vendorName;
    }

//    public LicensingVersion getLicensingVersion() {
//        return licensingVersion;
//    }
//
//    public void setLicensingVersion(LicensingVersion licensingVersion) {
//        this.licensingVersion = licensingVersion;
//    }

    public LicensingData getLicensingData() {
        return licensingData;
    }

    public void setLicensingData(LicensingData licensingData) {
        this.licensingData = licensingData;
    }

    public String getOnboardingMethod() {
        return onboardingMethod;
    }

    public void setOnboardingMethod(String onboardingMethod) {
        this.onboardingMethod = onboardingMethod;
    }

    public String getNetworkPackageName() {
        return networkPackageName;
    }

    public void setNetworkPackageName(String networkPackageName) {
        this.networkPackageName = networkPackageName;
    }

    public String getOnboardingOrigin() {
        return onboardingOrigin;
    }

    public void setOnboardingOrigin(String onboardingOrigin) {
        this.onboardingOrigin = onboardingOrigin;
    }

    @Override
    public String toString() {
        return "VendorSoftwareProductObjectReqDetails{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", category='" + category + '\'' +
                ", subCategory='" + subCategory + '\'' +
                ", vendorId='" + vendorId + '\'' +
                ", vendorName='" + vendorName + '\'' +
                ", licensingVersion=" + licensingVersion +
                ", licensingData=" + licensingData +
                ", onboardingMethod='" + onboardingMethod + '\'' +
                ", networkPackageName='" + networkPackageName + '\'' +
                ", onboardingOrigin='" + onboardingOrigin + '\'' +
                '}';
    }
}
