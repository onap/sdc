/*
 * -
 *  * ============LICENSE_START=======================================================
 *  *  Copyright (C) 2019 Nordix Foundation.
 *  * ================================================================================
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *  *
 *  * SPDX-License-Identifier: Apache-2.0
 *  * ============LICENSE_END=========================================================
 *
 */

package org.openecomp.sdc.be.components.impl.artifact;

import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openecomp.sdc.common.api.ArtifactTypeEnum;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.openecomp.sdc.be.components.impl.artifact.ArtifactTypeToPayloadTypeSelector.getPayloadType;
import static org.openecomp.sdc.be.components.impl.artifact.PayloadTypeEnum.HEAT_ENV;
import static org.openecomp.sdc.be.components.impl.artifact.PayloadTypeEnum.HEAT_YAML;
import static org.openecomp.sdc.be.components.impl.artifact.PayloadTypeEnum.NOT_DEFINED;
import static org.openecomp.sdc.be.components.impl.artifact.PayloadTypeEnum.XML;

@ExtendWith(MockitoExtension.class)
public class ArtifactTypeToPayloadTypeSelectorTest {

    private static final String ANY_EXTENSION = "anyExtension";
    private static final String JSON = "json";
    private static final String YAML = "yaml";
    private static final String YML = "yml";

    public static Stream<Arguments> data() {
        return Stream.of(
                Arguments.of(ArtifactTypeEnum.HEAT.getType(), ANY_EXTENSION, HEAT_YAML),
                Arguments.of(ArtifactTypeEnum.HEAT.getType().toLowerCase(), ANY_EXTENSION, HEAT_YAML),
                Arguments.of(ArtifactTypeEnum.HEAT_VOL.getType(), ANY_EXTENSION, HEAT_YAML),
                Arguments.of(ArtifactTypeEnum.HEAT_VOL.getType().toLowerCase(), ANY_EXTENSION, HEAT_YAML),
                Arguments.of(ArtifactTypeEnum.HEAT_NET.getType(), ANY_EXTENSION, HEAT_YAML),
                Arguments.of(ArtifactTypeEnum.HEAT_NET.getType().toLowerCase(), ANY_EXTENSION, HEAT_YAML),
                Arguments.of(ArtifactTypeEnum.HEAT_ENV.getType(), ANY_EXTENSION, HEAT_ENV),
                Arguments.of(ArtifactTypeEnum.HEAT_ENV.getType().toLowerCase(), ANY_EXTENSION, HEAT_ENV),
                Arguments.of(ArtifactTypeEnum.YANG_XML.getType(), ANY_EXTENSION, XML),
                Arguments.of(ArtifactTypeEnum.YANG_XML.getType().toLowerCase(), ANY_EXTENSION, XML),
                Arguments.of(ArtifactTypeEnum.VNF_CATALOG.getType(), ANY_EXTENSION, XML),
                Arguments.of(ArtifactTypeEnum.VNF_CATALOG.getType().toLowerCase(), ANY_EXTENSION, XML),
                Arguments.of(ArtifactTypeEnum.VF_LICENSE.getType(), ANY_EXTENSION, XML),
                Arguments.of(ArtifactTypeEnum.VF_LICENSE.getType().toLowerCase(), ANY_EXTENSION, XML),
                Arguments.of(ArtifactTypeEnum.VENDOR_LICENSE.getType(), ANY_EXTENSION, XML),
                Arguments.of(ArtifactTypeEnum.VENDOR_LICENSE.getType().toLowerCase(), ANY_EXTENSION, XML),
                Arguments.of(ArtifactTypeEnum.MODEL_INVENTORY_PROFILE.getType(), ANY_EXTENSION, XML),
                Arguments.of(ArtifactTypeEnum.MODEL_INVENTORY_PROFILE.getType().toLowerCase(), ANY_EXTENSION, XML),
                Arguments.of(ArtifactTypeEnum.MODEL_QUERY_SPEC.getType(), ANY_EXTENSION, XML),
                Arguments.of(ArtifactTypeEnum.UCPE_LAYER_2_CONFIGURATION.getType(), ANY_EXTENSION, XML),
                Arguments.of(ArtifactTypeEnum.UCPE_LAYER_2_CONFIGURATION.getType().toLowerCase(), ANY_EXTENSION, XML),
                Arguments.of(ArtifactTypeEnum.DCAE_INVENTORY_JSON.getType(), JSON, PayloadTypeEnum.JSON),
                Arguments.of(ArtifactTypeEnum.DCAE_INVENTORY_JSON.getType().toLowerCase(), JSON, PayloadTypeEnum.JSON),
                Arguments.of(ArtifactTypeEnum.DCAE_INVENTORY_TOSCA.getType(), YAML, PayloadTypeEnum.YAML),
                Arguments.of(ArtifactTypeEnum.DCAE_INVENTORY_TOSCA.getType().toLowerCase(), YAML, PayloadTypeEnum.YAML),
                Arguments.of(ArtifactTypeEnum.DCAE_INVENTORY_TOSCA.getType(), YML, PayloadTypeEnum.YAML),
                Arguments.of(ArtifactTypeEnum.DCAE_INVENTORY_TOSCA.getType().toLowerCase(), YML, PayloadTypeEnum.YAML),
                Arguments.of(ArtifactTypeEnum.VES_EVENTS.getType(), YAML, PayloadTypeEnum.YAML),
                Arguments.of(ArtifactTypeEnum.VES_EVENTS.getType().toLowerCase(), YAML, PayloadTypeEnum.YAML),
                Arguments.of(ArtifactTypeEnum.VES_EVENTS.getType(), YML, PayloadTypeEnum.YAML),
                Arguments.of(ArtifactTypeEnum.VES_EVENTS.getType().toLowerCase(), YML, PayloadTypeEnum.YAML),
                Arguments.of(ArtifactTypeEnum.LIFECYCLE_OPERATIONS.getType(), YAML, PayloadTypeEnum.YAML),
                Arguments.of(ArtifactTypeEnum.LIFECYCLE_OPERATIONS.getType().toLowerCase(), YAML, PayloadTypeEnum.YAML),
                Arguments.of(ArtifactTypeEnum.LIFECYCLE_OPERATIONS.getType(), YML, PayloadTypeEnum.YAML),
                Arguments.of(ArtifactTypeEnum.LIFECYCLE_OPERATIONS.getType().toLowerCase(), YML, PayloadTypeEnum.YAML),
                Arguments.of(ArtifactTypeEnum.PM_DICTIONARY.getType(), YAML, PayloadTypeEnum.YAML),
                Arguments.of(ArtifactTypeEnum.PM_DICTIONARY.getType().toLowerCase(), YAML, PayloadTypeEnum.YAML ),
                Arguments.of(ArtifactTypeEnum.PM_DICTIONARY.getType(), YML, PayloadTypeEnum.YAML),
                Arguments.of(ArtifactTypeEnum.PM_DICTIONARY.getType().toLowerCase(), YML, PayloadTypeEnum.YAML),
                Arguments.of(ArtifactTypeEnum.ANSIBLE_PLAYBOOK.getType(), ANY_EXTENSION, NOT_DEFINED),
                Arguments.of(ArtifactTypeEnum.ANSIBLE_PLAYBOOK.getType().toLowerCase(), ANY_EXTENSION, NOT_DEFINED)
        );
    }

    @ParameterizedTest
    @MethodSource("data")
    public void testCorrectPayloadTypeEnumSelectedForArtifactTypeAndExtension(String artifactType, String extension, PayloadTypeEnum expectedPayloadTypeEnum) {
        PayloadTypeEnum payloadType = getPayloadType(artifactType, extension);
        assertEquals(expectedPayloadTypeEnum, payloadType);
    }
}



