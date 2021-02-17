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
package org.openecomp.sdc.be.config.validation;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.openecomp.sdc.be.config.ArtifactConfiguration;
import org.openecomp.sdc.be.config.Configuration;
import org.openecomp.sdc.be.config.exception.MissingBaseArtifactConfigException;
import org.openecomp.sdc.common.api.ArtifactTypeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Responsible for the validation of the artifact configuration
 */
public class ArtifactConfigValidator {

    private static final Logger LOGGER = LoggerFactory.getLogger(ArtifactConfigValidator.class);
    private final Configuration configuration;
    private final Set<ArtifactTypeEnum> baseArtifactSet;

    public ArtifactConfigValidator(final Configuration configuration, final Set<ArtifactTypeEnum> baseArtifactSet) {
        this.configuration = configuration;
        this.baseArtifactSet = baseArtifactSet;
    }

    /**
     * Validates the artifacts configuration
     */
    public void validate() {
        if (CollectionUtils.isEmpty(baseArtifactSet)) {
            return;
        }
        final List<ArtifactConfiguration> artifacts = configuration.getArtifacts();
        if (CollectionUtils.isEmpty(artifacts)) {
            LOGGER.warn("No configuration artifacts entry found. Ignoring artifacts validation.");
            return;
        }
        final Set<ArtifactTypeEnum> notConfiguredArtifactTypeSet = baseArtifactSet.stream().filter(artifactTypeEnum -> artifacts.stream()
            .noneMatch(artifactConfiguration -> artifactTypeEnum.getType().equals(artifactConfiguration.getType()))).collect(Collectors.toSet());
        if (!notConfiguredArtifactTypeSet.isEmpty()) {
            final String msg = buildErrorMessage(notConfiguredArtifactTypeSet);
            throw new MissingBaseArtifactConfigException(msg);
        }
    }

    private String buildErrorMessage(final Set<ArtifactTypeEnum> missingConfigArtifactTypeSet) {
        final String artifactTypeAsString = missingConfigArtifactTypeSet.stream().map(ArtifactTypeEnum::getType).collect(Collectors.joining(", "));
        return String.format("Missing configuration for Artifact Type(s): %s", artifactTypeAsString);
    }
}
