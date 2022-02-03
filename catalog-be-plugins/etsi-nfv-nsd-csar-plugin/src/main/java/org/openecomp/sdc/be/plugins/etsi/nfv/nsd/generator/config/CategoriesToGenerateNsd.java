/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2022 Nordix
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

package org.openecomp.sdc.be.plugins.etsi.nfv.nsd.generator.config;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
public enum CategoriesToGenerateNsd {
    ETSI_NS_COMPONENT_CATEGORY ("ETSI NFV Network Service"),
    ETSI_NS_COMPONENT_CATEGORY_FOR_ETSI_MODEL("ETSI NFV Network Service For ETSI Model");

    private final String categoryName;

    public static boolean hasCategoryName(String categoryNameToCheck) {
        return Arrays.stream(CategoriesToGenerateNsd.values()).anyMatch(CategoriesToGenerateNsd ->
                CategoriesToGenerateNsd.getCategoryName().equals(categoryNameToCheck));
    }

    public static String getCategories(){
        return Arrays.stream(CategoriesToGenerateNsd.values()).map(CategoriesToGenerateNsd::getCategoryName)
                .collect(Collectors.joining(", "));
    }
}
