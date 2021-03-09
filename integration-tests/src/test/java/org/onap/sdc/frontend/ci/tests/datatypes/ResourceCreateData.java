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

/**
 * Represents the necessary data to create a resource (VF, VFC or similar)
 */
@Data
public class ResourceCreateData {

    private String name;
    private String category;
    private List<String> tagList;
    private String description;
    private String contactId;
    private String vendorName;
    private String vendorRelease;
    private String vendorModelNumber;

    public void setRandomName(final String prefix) {
        final String randomPart = UUID.randomUUID().toString().split("-")[0];
        this.name = String.format("%s%s", prefix == null ? "" : prefix, randomPart);
    }
}
