/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2021 Nordix Foundation
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================
 */

package org.onap.sdc.frontend.ci.tests.datatypes;

import java.util.List;
import java.util.UUID;
import lombok.Data;
import org.onap.sdc.backend.ci.tests.datatypes.enums.ServiceInstantiationType;

@Data
public class ServiceCreateData {

    private String name;
    private String category;
    private String etsiVersion;
    private List<String> tagList;
    private String description;
    private String contactId;
    private Boolean hasGeneratedNaming;
    private String namingPolicy;
    private String serviceType;
    private String serviceRole;
    private String serviceFunction;
    private ServiceInstantiationType instantiationType;

    public void setRandomName(final String prefix) {
        final String randomPart = UUID.randomUUID().toString().split("-")[0];
        this.name = String.format("%s%s", prefix == null ? "" : prefix, randomPart);
    }
}
