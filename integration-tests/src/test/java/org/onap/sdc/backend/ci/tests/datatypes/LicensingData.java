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
import java.util.List;

public class LicensingData {
    private String licenseAgreement;
    private List<String> featureGroups = null;

    public LicensingData() {
    }

    public LicensingData(String licenseAgreement, List<String> featureGroups) {
        this.licenseAgreement = licenseAgreement;
        this.featureGroups = featureGroups;
    }

    public String getLicenseAgreement() {
        return licenseAgreement;
    }

    public void setLicenseAgreement(String licenseAgreement) {
        this.licenseAgreement = licenseAgreement;
    }

    public List<String> getFeatureGroups() {
        return featureGroups;
    }

    public void setFeatureGroups(List<String> featureGroups) {
        this.featureGroups = featureGroups;
    }

    @Override
    public String toString() {
        return "LicensingData{" +
                "licenseAgreement='" + licenseAgreement + '\'' +
                ", featureGroups=" + featureGroups +
                '}';
    }
}
