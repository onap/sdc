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
package org.openecomp.sdc.be.tosca.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ToscaMetadata implements IToscaMetadata {

    private String invariantUUID;
    private String UUID;
    private String customizationUUID;
    private String version;
    private String name;
    private String description;
    private String type;
    private String category;
    private String subcategory;
    private String resourceVendor;
    private String resourceVendorRelease;
    private String resourceVendorModelNumber;
    private String serviceType;
    private String serviceRole;
    private String instantiationType;
    private String serviceEcompNaming;
    private String ecompGeneratedNaming;
    private String namingPolicy;
    private String sourceModelInvariant;
    private String environmentContext;
    private String sourceModelName;
    private String sourceModelUuid;
    private String serviceFunction;
    private String model;

    public Boolean isEcompGeneratedNaming() {
        return new Boolean(ecompGeneratedNaming);
    }

    public void setEcompGeneratedNaming(Boolean ecompGeneratedNaming) {
        this.ecompGeneratedNaming = ecompGeneratedNaming == null ? null : ecompGeneratedNaming.toString();
    }

    public Boolean getServiceEcompNaming() {
        return new Boolean(serviceEcompNaming);
    }

    public void setServiceEcompNaming(Boolean serviceEcompNaming) {
        this.serviceEcompNaming = serviceEcompNaming == null ? null : serviceEcompNaming.toString();
    }

}
