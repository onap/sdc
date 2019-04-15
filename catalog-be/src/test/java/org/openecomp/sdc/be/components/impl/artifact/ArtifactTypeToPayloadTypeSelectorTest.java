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
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.openecomp.sdc.common.api.ArtifactTypeEnum;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.openecomp.sdc.be.components.impl.artifact.ArtifactTypeToPayloadTypeSelector.getPayloadType;
import static org.openecomp.sdc.be.components.impl.artifact.PayloadTypeEnum.HEAT_ENV;
import static org.openecomp.sdc.be.components.impl.artifact.PayloadTypeEnum.HEAT_YAML;
import static org.openecomp.sdc.be.components.impl.artifact.PayloadTypeEnum.NOT_DEFINED;
import static org.openecomp.sdc.be.components.impl.artifact.PayloadTypeEnum.XML;

@RunWith(Parameterized.class)
public class ArtifactTypeToPayloadTypeSelectorTest {

    private static final String ANY_EXTENSION = "anyExtension";
    private static final String JSON = "json";
    private static final String YAML = "yaml";
    private static final String YML = "yml";
    private String artifactType;
    private String extension;
    private PayloadTypeEnum expectedPayloadTypeEnum;

    public ArtifactTypeToPayloadTypeSelectorTest(String artifactType, String extension, PayloadTypeEnum expectedPayloadTypeEnum) {
        this.artifactType = artifactType;
        this.extension = extension;
        this.expectedPayloadTypeEnum = expectedPayloadTypeEnum;
    }

    @Parameters(name = "{index}: {0}, {1}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                { ArtifactTypeEnum.HEAT.getType(), ANY_EXTENSION, HEAT_YAML },
                { ArtifactTypeEnum.HEAT.getType().toLowerCase(), ANY_EXTENSION, HEAT_YAML },
                { ArtifactTypeEnum.HEAT_VOL.getType(), ANY_EXTENSION, HEAT_YAML },
                { ArtifactTypeEnum.HEAT_VOL.getType().toLowerCase(), ANY_EXTENSION, HEAT_YAML },
                { ArtifactTypeEnum.HEAT_NET.getType(), ANY_EXTENSION, HEAT_YAML },
                { ArtifactTypeEnum.HEAT_NET.getType().toLowerCase(), ANY_EXTENSION, HEAT_YAML },
                { ArtifactTypeEnum.HEAT_ENV.getType(), ANY_EXTENSION, HEAT_ENV },
                { ArtifactTypeEnum.HEAT_ENV.getType().toLowerCase(), ANY_EXTENSION, HEAT_ENV },
                { ArtifactTypeEnum.YANG_XML.getType(), ANY_EXTENSION, XML },
                { ArtifactTypeEnum.YANG_XML.getType().toLowerCase(), ANY_EXTENSION, XML },
                { ArtifactTypeEnum.VNF_CATALOG.getType(), ANY_EXTENSION, XML },
                { ArtifactTypeEnum.VNF_CATALOG.getType().toLowerCase(), ANY_EXTENSION, XML },
                { ArtifactTypeEnum.VF_LICENSE.getType(), ANY_EXTENSION, XML },
                { ArtifactTypeEnum.VF_LICENSE.getType().toLowerCase(), ANY_EXTENSION, XML },
                { ArtifactTypeEnum.VENDOR_LICENSE.getType(), ANY_EXTENSION, XML },
                { ArtifactTypeEnum.VENDOR_LICENSE.getType().toLowerCase(), ANY_EXTENSION, XML },
                { ArtifactTypeEnum.MODEL_INVENTORY_PROFILE.getType(), ANY_EXTENSION, XML },
                { ArtifactTypeEnum.MODEL_INVENTORY_PROFILE.getType().toLowerCase(), ANY_EXTENSION, XML },
                { ArtifactTypeEnum.MODEL_QUERY_SPEC.getType(), ANY_EXTENSION, XML },
                { ArtifactTypeEnum.MODEL_QUERY_SPEC.getType().toLowerCase(), ANY_EXTENSION, XML },
                { ArtifactTypeEnum.UCPE_LAYER_2_CONFIGURATION.getType(), ANY_EXTENSION, XML },
                { ArtifactTypeEnum.UCPE_LAYER_2_CONFIGURATION.getType().toLowerCase(), ANY_EXTENSION, XML },
                { ArtifactTypeEnum.DCAE_INVENTORY_JSON.getType(), JSON, PayloadTypeEnum.JSON },
                { ArtifactTypeEnum.DCAE_INVENTORY_JSON.getType().toLowerCase(), JSON, PayloadTypeEnum.JSON },
                { ArtifactTypeEnum.DCAE_INVENTORY_TOSCA.getType(), YAML, PayloadTypeEnum.YAML },
                { ArtifactTypeEnum.DCAE_INVENTORY_TOSCA.getType().toLowerCase(), YAML, PayloadTypeEnum.YAML },
                { ArtifactTypeEnum.DCAE_INVENTORY_TOSCA.getType(), YML, PayloadTypeEnum.YAML },
                { ArtifactTypeEnum.DCAE_INVENTORY_TOSCA.getType().toLowerCase(), YML, PayloadTypeEnum.YAML },
                { ArtifactTypeEnum.VES_EVENTS.getType(), YAML, PayloadTypeEnum.YAML },
                { ArtifactTypeEnum.VES_EVENTS.getType().toLowerCase(), YAML, PayloadTypeEnum.YAML },
                { ArtifactTypeEnum.VES_EVENTS.getType(), YML, PayloadTypeEnum.YAML },
                { ArtifactTypeEnum.VES_EVENTS.getType().toLowerCase(), YML, PayloadTypeEnum.YAML },
                { ArtifactTypeEnum.LIFECYCLE_OPERATIONS.getType(), YAML, PayloadTypeEnum.YAML },
                { ArtifactTypeEnum.LIFECYCLE_OPERATIONS.getType().toLowerCase(), YAML, PayloadTypeEnum.YAML },
                { ArtifactTypeEnum.LIFECYCLE_OPERATIONS.getType(), YML, PayloadTypeEnum.YAML },
                { ArtifactTypeEnum.LIFECYCLE_OPERATIONS.getType().toLowerCase(), YML, PayloadTypeEnum.YAML },
                { ArtifactTypeEnum.PM_DICTIONARY.getType(), YAML, PayloadTypeEnum.YAML },
                { ArtifactTypeEnum.PM_DICTIONARY.getType().toLowerCase(), YAML, PayloadTypeEnum.YAML },
                { ArtifactTypeEnum.PM_DICTIONARY.getType(), YML, PayloadTypeEnum.YAML },
                { ArtifactTypeEnum.PM_DICTIONARY.getType().toLowerCase(), YML, PayloadTypeEnum.YAML },
                { ArtifactTypeEnum.ANSIBLE_PLAYBOOK.getType(), ANY_EXTENSION, NOT_DEFINED },
                { ArtifactTypeEnum.ANSIBLE_PLAYBOOK.getType().toLowerCase(), ANY_EXTENSION, NOT_DEFINED }
        });
    }

    @Test
    public void testCorrectPayloadTypeEnumSelectedForArtifactTypeAndExtension() {
        PayloadTypeEnum payloadType = getPayloadType(artifactType, extension);
        assertEquals(expectedPayloadTypeEnum, payloadType);
    }

}
