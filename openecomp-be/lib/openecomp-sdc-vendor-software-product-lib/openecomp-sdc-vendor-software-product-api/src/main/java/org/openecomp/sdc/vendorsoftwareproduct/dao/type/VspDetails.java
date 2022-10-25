/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2021 Nordix Foundation.
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
package org.openecomp.sdc.vendorsoftwareproduct.dao.type;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdc.versioning.dao.types.VersionableEntity;

@Getter
@Setter
@NoArgsConstructor
public class VspDetails implements VersionableEntity {

    public static final String ENTITY_TYPE = "Vendor Software Product";
    private String id;
    private Version version;
    private String name;
    private String description;
    private String category;
    private String subCategory;
    private String icon;
    private String vendorName;
    private String vendorId;
    private Version vlmVersion;
    private String licenseType;
    private String licenseAgreement;
    private List<String> featureGroups;
    private String onboardingMethod;
    private List<String> modelIdList;
    private String tenant;

    public VspDetails(String id, Version version) {
        this.id = id;
        this.version = version;
    }

    @Override
    public String getEntityType() {
        return ENTITY_TYPE;
    }

    @Override
    public String getFirstClassCitizenId() {
        return getId();
    }

    public List<String> getModelIdList() {
        if (modelIdList == null) {
            return Collections.emptyList();
        }
        return new ArrayList<>(modelIdList);
    }

    @Override
    public String toString() {
        return String.format("Vsp id = '%s', Version = '%s', Name = '%s', Tenant = '%s', Category = '%s', Description = '%s', Vendor = '%s', Model = '%s'",
            this.id, this.version, this.name, this.tenant, this.category, this.description, this.vendorName, this.modelIdList);
    }
}
