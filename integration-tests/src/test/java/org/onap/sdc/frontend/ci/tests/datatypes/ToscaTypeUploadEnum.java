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

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ToscaTypeUploadEnum {

    DATA("datatypes", "dataTypes", "data-types", "dataTypesZip"),
    CAPABILITY("capability", "capabilityTypes", "capability-types", "capabilityTypeZip"),
    RELATIONSHIP("relationship", "relationshipTypes", "relationship-types", "relationshipTypeZip"),
    INTERFACE("interfaceLifecycle", "interfaceLifecycleTypes", "interface-lifecycle-types", "interfaceLifecycleTypeZip"),
    GROUP("grouptypes", "groupTypes", "group-types", "groupTypesZip", true),
    POLICY("policytypes", "policyTypes", "policy-types", "policyTypesZip", true);

    ToscaTypeUploadEnum(final String urlSuffix, final String modelParam, final String directory, final String zipParam) {
        this.urlSuffix = urlSuffix;
        this.modelParam = modelParam;
        this.directory = directory;
        this.zipParam = zipParam;
        this.metadata = false;
    }

    private String urlSuffix;
    private String modelParam;
    private String directory;
    private String zipParam;
    private boolean metadata;
}
