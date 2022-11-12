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
package org.openecomp.sdc.be.ui.model;

import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.openecomp.sdc.be.datatypes.components.ResourceMetadataDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.category.CategoryDefinition;

@Getter
@Setter
@NoArgsConstructor
public class UiResourceMetadata extends UiComponentMetadata {

    private String vendorName;
    private String vendorRelease;
    private String resourceVendorModelNumber;
    private ResourceTypeEnum resourceType = ResourceTypeEnum.VFC;
    private Boolean isAbstract;
    private String cost;
    private String licenseType;
    private String toscaResourceName;
    private List<String> derivedFrom;
    private Map<String, String> categorySpecificMetadata;
    private String csarVersionId;
    private String tenant;

    public UiResourceMetadata(List<CategoryDefinition> categories, List<String> derivedFrom, ResourceMetadataDataDefinition metadata) {
        super(categories, metadata);
        this.vendorName = metadata.getVendorName();
        this.vendorRelease = metadata.getVendorRelease();
        this.resourceVendorModelNumber = metadata.getResourceVendorModelNumber();
        this.resourceType = metadata.getResourceType();
        this.cost = metadata.getCost();
        this.licenseType = metadata.getLicenseType();
        this.toscaResourceName = metadata.getToscaResourceName();
        this.derivedFrom = derivedFrom;
        this.categorySpecificMetadata = metadata.getCategorySpecificMetadata();
        this.csarVersionId = metadata.getCsarVersionId();
        this.tenant = metadata.getTenant();
    }
}
