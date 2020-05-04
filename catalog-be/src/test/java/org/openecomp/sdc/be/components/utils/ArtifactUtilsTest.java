/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
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
 * ============LICENSE_END=========================================================
 * Modifications copyright (c) 2019 Nokia
 * ================================================================================
 */

package org.openecomp.sdc.be.components.utils;

import static org.junit.Assert.assertEquals;
import static org.openecomp.sdc.be.components.utils.ArtifactUtils.buildJsonStringForCsarVfcArtifact;

import org.junit.Test;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.common.api.ArtifactGroupTypeEnum;

public class ArtifactUtilsTest {

    private static final String JSON =
        "{\n"
            + "  \"artifactType\": \"artifactType\",\n"
            + "  \"artifactDisplayName\": \"displayName\",\n"
            + "  \"artifactName\": \"artifactName\",\n"
            + "  \"artifactGroupType\": \"DEPLOYMENT\",\n"
            + "  \"description\": \"description\",\n"
            + "  \"payloadData\": [\n"
            + "    112,\n"
            + "    97,\n"
            + "    121,\n"
            + "    108,\n"
            + "    111,\n"
            + "    97,\n"
            + "    100,\n"
            + "    68,\n"
            + "    97,\n"
            + "    116,\n"
            + "    97\n"
            + "  ],\n"
            + "  \"artifactLabel\": \"label\"\n"
            + "}";

    @Test
    public void artifactDefinitionShouldBeDeserializedProperly()  {
        ArtifactDefinition ad = new ArtifactDefinition();
        ad.setArtifactName("artifactName");
        ad.setArtifactLabel("label");
        ad.setArtifactType("artifactType");
        ad.setDescription("description");
        ad.setPayloadData("payloadData");
        ad.setArtifactDisplayName("displayName");
        ad.setArtifactGroupType(ArtifactGroupTypeEnum.DEPLOYMENT);

        String actual = buildJsonStringForCsarVfcArtifact(ad);

        assertEquals(actual, JSON);
    }
}
