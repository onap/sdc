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
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FilterKeyEnum {
    RESOURCE_TYPE("resourceType"), SUB_CATEGORY("subCategory"), CATEGORY("category"), DISTRIBUTION_STATUS("distributionStatus"), NAME_FRAGMENT(
        "nameFragment");
    private static final int NUMBER_OF_RESOURCES_FILTERED = 3;
    private static final int NUMBER_OF_SERVICES_FILTERED = 4;
    private final String name;

    public static List<String> getAllFilters() {
        return Arrays.stream(FilterKeyEnum.values()).map(FilterKeyEnum::getName).collect(Collectors.toList());
    }

    public static List<String> getValidFiltersByAssetType(ComponentTypeEnum assetType) {
        if (assetType == null) {
            return null;
        }
        switch (assetType) {
            case RESOURCE:
                return getAllFilters().subList(0, NUMBER_OF_RESOURCES_FILTERED);
            case SERVICE:
                return getAllFilters().subList(2, NUMBER_OF_SERVICES_FILTERED);
            default:
                return null;
        }
    }
}
