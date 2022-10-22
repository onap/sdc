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
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.openecomp.sdc.be.datatypes.enums.JsonPresentationFieldsExtractor;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;

@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
@ToString(callSuper = true)
public class ResourceMetadataDataDefinition extends ComponentMetadataDataDefinition {

    private String vendorName;
    private String vendorRelease;
    private String resourceVendorModelNumber;
    private ResourceTypeEnum resourceType = ResourceTypeEnum.VFC;
    private Boolean isAbstract;
    private String cost;
    private String licenseType;
    private String toscaResourceName;
    private String csarVersionId;
    private String tenant;

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
        this.csarVersionId = other.getCsarVersionId();
        this.tenant = other.getTenant();
    }

    public ResourceMetadataDataDefinition(JsonPresentationFieldsExtractor extractor) {
        super(extractor);
        this.vendorName = extractor.getVendorName();
        this.vendorRelease = extractor.getVendorRelease();
        this.resourceVendorModelNumber = extractor.getResourceVendorModelNumber();
        this.isAbstract = extractor.isAbstract();
        this.resourceType = extractor.getResourceType();
        this.toscaResourceName = extractor.getToscaResourceName();
        this.csarVersionId = extractor.getCsarVersionId();
        this.tenant = extractor.getTenant();
    }

    public Boolean isAbstract() {
        return getIsAbstract();
    }

    public void setAbstract(final Boolean isAbstract) {
        setIsAbstract(isAbstract);;
    }

    @Override
    public String getActualComponentType() {
        return getResourceType().getValue();
    }
}
