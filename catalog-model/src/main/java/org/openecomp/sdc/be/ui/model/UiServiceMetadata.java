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
import lombok.Setter;
import org.openecomp.sdc.be.datatypes.components.ServiceMetadataDataDefinition;
import org.openecomp.sdc.be.model.category.CategoryDefinition;

@Getter
@Setter
public class UiServiceMetadata extends UiComponentMetadata {

    private String distributionStatus;
    private Boolean ecompGeneratedNaming;
    private String namingPolicy;
    private String serviceType;
    private String serviceRole;
    private String environmentContext;
    private String instantiationType;
    private String serviceFunction;
    private Map<String, String> categorySpecificMetadata;

    public UiServiceMetadata(List<CategoryDefinition> categories, ServiceMetadataDataDefinition metadata) {
        super(categories, metadata);
        this.distributionStatus = metadata.getDistributionStatus();
        this.ecompGeneratedNaming = metadata.isEcompGeneratedNaming();
        this.namingPolicy = metadata.getNamingPolicy();
        this.serviceType = metadata.getServiceType();
        this.serviceRole = metadata.getServiceRole();
        this.environmentContext = metadata.getEnvironmentContext();
        this.instantiationType = metadata.getInstantiationType();
        this.serviceFunction = metadata.getServiceFunction();
        this.categorySpecificMetadata = metadata.getCategorySpecificMetadata();
    }
}
