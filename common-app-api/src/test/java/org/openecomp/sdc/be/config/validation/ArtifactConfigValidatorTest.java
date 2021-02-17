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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openecomp.sdc.be.config.ArtifactConfiguration;
import org.openecomp.sdc.be.config.Configuration;
import org.openecomp.sdc.be.config.exception.MissingBaseArtifactConfigException;
import org.openecomp.sdc.common.api.ArtifactTypeEnum;

public class ArtifactConfigValidatorTest {

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();
    public ArtifactConfigValidator artifactConfigValidator;

    @Test
    public void testValidate() {
        final Configuration configuration = new Configuration();
        //not base artifacts, should validate
        artifactConfigValidator = new ArtifactConfigValidator(configuration, Collections.emptySet());
        artifactConfigValidator.validate();
        final HashSet<ArtifactTypeEnum> baseArtifactTypes = new HashSet<>(Arrays.asList(ArtifactTypeEnum.AAI_SERVICE_MODEL, ArtifactTypeEnum.BPEL));
        //with base artifacts, but no artifact configured, should validate
        artifactConfigValidator = new ArtifactConfigValidator(configuration, baseArtifactTypes);
        artifactConfigValidator.validate();
        final List<ArtifactConfiguration> artifactConfigurationList = new ArrayList<>();
        final ArtifactConfiguration artifactConfiguration1 = new ArtifactConfiguration();
        artifactConfiguration1.setType(ArtifactTypeEnum.AAI_SERVICE_MODEL.getType());
        artifactConfigurationList.add(artifactConfiguration1);
        final ArtifactConfiguration artifactConfiguration2 = new ArtifactConfiguration();
        artifactConfiguration2.setType(ArtifactTypeEnum.BPEL.getType());
        artifactConfigurationList.add(artifactConfiguration2);
        configuration.setArtifacts(artifactConfigurationList);
        //with base artifacts and corresponding configuration, should validate
        artifactConfigValidator = new ArtifactConfigValidator(configuration, baseArtifactTypes);
        artifactConfigValidator.validate();
        //with base artifacts and missing one configuration, should not validate
        configuration.setArtifacts(Collections.singletonList(artifactConfiguration1));
        exceptionRule.expect(MissingBaseArtifactConfigException.class);
        exceptionRule.expectMessage(String.format("Missing configuration for Artifact Type(s): %s", ArtifactTypeEnum.BPEL.getType()));
        artifactConfigValidator = new ArtifactConfigValidator(configuration, baseArtifactTypes);
        artifactConfigValidator.validate();
    }
}
