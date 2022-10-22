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

import lombok.Data;
import lombok.NoArgsConstructor;
import org.openecomp.sdc.be.datatypes.enums.JsonPresentationFieldsExtractor;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;

@Data
@NoArgsConstructor
public class ServiceMetadataDataDefinition extends ComponentMetadataDataDefinition {

    private static final String EMPTY_STR = "";
    private String distributionStatus;
    private String serviceType = EMPTY_STR;
    private String serviceRole = EMPTY_STR;
    private String instantiationType = EMPTY_STR;
    private String serviceFunction = EMPTY_STR;
    private Boolean ecompGeneratedNaming = true;
    private String namingPolicy = EMPTY_STR;
    private String environmentContext;
    private String serviceVendorModelNumber;
    public Boolean isAbstract;
    private ResourceTypeEnum importServiceType = ResourceTypeEnum.SERVICE;
    private String toscaServiceName;
    private String vendorName;
    private String tenant;

    private String vendorRelease;

    public ServiceMetadataDataDefinition(JsonPresentationFieldsExtractor extractor) {
        super(extractor);
        serviceType = extractor.getServiceType();
        serviceRole = extractor.getServiceRole();
        serviceFunction = extractor.getServiceFunction();
    }

    public Boolean isEcompGeneratedNaming() {
        return getEcompGeneratedNaming();
    }

    @Override
    public String getActualComponentType() {
        return componentType != null ? componentType.getValue() : "";
    }
}
