/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2020 Nordix Foundation
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
package org.openecomp.sdc.be.config;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.collections.CollectionUtils;
import org.openecomp.sdc.common.api.ArtifactGroupTypeEnum;

/**
 * Represents an artifact type configuration
 */
@Getter
@Setter
@ToString
public class ArtifactConfiguration {

    private String type;
    private List<ArtifactGroupTypeEnum> categories;
    private List<ComponentType> componentTypes;
    private List<String> acceptedTypes;
    private List<String> resourceTypes;

    /**
     * Checks if the configuration supports a component type.
     *
     * @param componentType the component type
     * @return {@code true} if the component type is supported, {@code false} otherwise
     */
    public boolean hasSupport(final ComponentType componentType) {
        if (CollectionUtils.isEmpty(componentTypes)) {
            return false;
        }
        return componentTypes.contains(componentType);
    }

    /**
     * Checks if the configuration supports an artifact group/category.
     *
     * @param groupType the artifact category/group type
     * @return {@code true} if the artifact group/category is supported, {@code false} otherwise
     */
    public boolean hasSupport(final ArtifactGroupTypeEnum groupType) {
        if (CollectionUtils.isEmpty(categories)) {
            return false;
        }
        return categories.contains(groupType);
    }
}
