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
import java.util.Optional;
import java.util.stream.Collectors;
import org.openecomp.sdc.common.api.ArtifactGroupTypeEnum;

/**
 * Singleton that manages the artifact type configuration
 */
public class ArtifactConfigManager {

    public static final ArtifactConfigManager INSTANCE = new ArtifactConfigManager();

    private ArtifactConfigManager() {
    }

    public static ArtifactConfigManager getInstance() {
        return INSTANCE;
    }

    /**
     * Find an artifact configuration by artifact type.
     *
     * @param type the artifact type
     * @return the artifact configuration if the type exists
     */
    public Optional<ArtifactConfiguration> find(final String type) {
        final List<ArtifactConfiguration> artifactConfigurationList = getConfiguration();
        return artifactConfigurationList.stream().filter(artifactConfiguration -> artifactConfiguration.getType().equals(type)).findFirst();
    }

    /**
     * Find an artifact configuration by artifact type, that supports the artifact category/group and component type.
     *
     * @param type          the artifact type
     * @param artifactGroup the artifact category/group
     * @param componentType the component type
     * @return the artifact configuration if it matches the provided filter
     */
    public Optional<ArtifactConfiguration> find(final String type, final ArtifactGroupTypeEnum artifactGroup, final ComponentType componentType) {
        final ArtifactConfiguration artifactConfiguration = find(type).orElse(null);
        if (artifactConfiguration == null) {
            return Optional.empty();
        }
        final boolean hasCategory = artifactConfiguration.hasSupport(artifactGroup);
        if (!hasCategory) {
            return Optional.empty();
        }
        final boolean hasComponentType = artifactConfiguration.hasSupport(componentType);
        if (!hasComponentType) {
            return Optional.empty();
        }
        return Optional.of(artifactConfiguration);
    }

    /**
     * Find all artifact configuration that supports an artifact category/group and a component type.
     *
     * @param artifactGroup the artifact group/category
     * @param componentType the component type
     * @return the artifact configurations that matches the filter
     */
    public List<ArtifactConfiguration> findAll(final ArtifactGroupTypeEnum artifactGroup, final ComponentType componentType) {
        final List<ArtifactConfiguration> artifactConfigurationList = ConfigurationManager.getConfigurationManager().getConfiguration()
            .getArtifacts();
        return artifactConfigurationList.stream()
            .filter(artifactConfiguration1 -> artifactConfiguration1.hasSupport(artifactGroup) && artifactConfiguration1.hasSupport(componentType))
            .collect(Collectors.toList());
    }

    /**
     * Find all artifact configuration that supports an artifact category/group.
     *
     * @param artifactGroup the artifact category/group
     * @return the artifact configurations that matches the filter
     */
    public List<ArtifactConfiguration> findAll(final ArtifactGroupTypeEnum artifactGroup) {
        final List<ArtifactConfiguration> artifactConfigurationList = ConfigurationManager.getConfigurationManager().getConfiguration()
            .getArtifacts();
        return artifactConfigurationList.stream().filter(artifactConfiguration1 -> artifactConfiguration1.hasSupport(artifactGroup))
            .collect(Collectors.toList());
    }

    /**
     * Find all artifact configuration that supports a component type.
     *
     * @param componentType the component type
     * @return the artifact configurations that matches the filter
     */
    public List<ArtifactConfiguration> findAll(final ComponentType componentType) {
        final List<ArtifactConfiguration> artifactConfigurationList = ConfigurationManager.getConfigurationManager().getConfiguration()
            .getArtifacts();
        return artifactConfigurationList.stream().filter(artifactConfiguration1 -> artifactConfiguration1.hasSupport(componentType))
            .collect(Collectors.toList());
    }

    /**
     * Gets the artifact configuration list.
     *
     * @return the artifact configuration list.
     */
    public List<ArtifactConfiguration> getConfiguration() {
        return ConfigurationManager.getConfigurationManager().getConfiguration().getArtifacts();
    }
}
