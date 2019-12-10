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
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class ServiceMetadataDataDefinition extends ComponentMetadataDataDefinition {
    private static final String EMPTY_STR = "";

    @Getter
    @Setter
    private String distributionStatus;
    @Getter
    @Setter
    private String serviceType = EMPTY_STR;
    @Getter
    @Setter
    private String serviceRole = EMPTY_STR;
    @Getter
    @Setter
    private String instantiationType = EMPTY_STR;
    @Getter
    @Setter
    private Boolean ecompGeneratedNaming = true;
    @Getter
    @Setter
    private String namingPolicy = EMPTY_STR;
    @Getter
    @Setter
    private String environmentContext;


    public ServiceMetadataDataDefinition(ServiceMetadataDataDefinition other) {
        super(other);
        serviceType = other.getServiceType();
        serviceRole = other.getServiceRole();
    }



    @Override
    public String getActualComponentType() {

        return componentType != null ? componentType.getValue() : "";
    }

}
