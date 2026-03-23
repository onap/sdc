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

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
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

//    public LicensingVersion getLicensingVersion() {
//        return licensingVersion;
//    }
//
//    public void setLicensingVersion(LicensingVersion licensingVersion) {
//        this.licensingVersion = licensingVersion;
//    }

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
