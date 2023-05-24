/*-
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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
 */
package org.openecomp.sdc.be.datatypes.enums;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FilterKeyEnum {
    RESOURCE_TYPE("resourceType"),
    SUB_CATEGORY("subCategory"),
    CATEGORY("category"),
    VERSION("version"),
    METADATA("metadata"),
    DISTRIBUTION_STATUS("distributionStatus"),
    NAME_FRAGMENT("nameFragment");
    private final String name;

    private static final List<String> RESOURCES_FILTERED = Arrays.asList(
        RESOURCE_TYPE.getName(),
        SUB_CATEGORY.getName(),
        CATEGORY.getName());
    private static final List<String> SERVICES_FILTERED = Arrays.asList(
        CATEGORY.getName(),
        DISTRIBUTION_STATUS.getName(),
        VERSION.getName(),
        METADATA.getName());

    public static List<String> getValidFiltersByAssetType(ComponentTypeEnum assetType) {
        if (assetType == null) {
            return Collections.emptyList();
        }
        switch (assetType) {
            case RESOURCE:
                return RESOURCES_FILTERED;
            case SERVICE:
                return SERVICES_FILTERED;
            default:
                return Collections.emptyList();
        }
    }
}
