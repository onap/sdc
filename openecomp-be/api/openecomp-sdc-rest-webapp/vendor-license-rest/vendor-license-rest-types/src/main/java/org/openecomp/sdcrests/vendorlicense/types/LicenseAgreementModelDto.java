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
package org.openecomp.sdcrests.vendorlicense.types;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Set;

@Schema(description = "LicenseAgreementModel")
public class LicenseAgreementModelDto extends LicenseAgreementDescriptorDto {

    private String id;
    private Set<FeatureGroupEntityDto> featureGroups;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Set<FeatureGroupEntityDto> getFeatureGroups() {
        return featureGroups;
    }

    public void setFeatureGroups(Set<FeatureGroupEntityDto> featureGroups) {
        this.featureGroups = featureGroups;
    }
}
