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

package org.openecomp.sdc.be.components.impl.json;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.openecomp.sdc.be.components.impl.json.JsonUtils.buildJsonStringForCsarVfcArtifact;

import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.common.api.ArtifactGroupTypeEnum;

public class JsonUtilsTest {

    private static final String JSON_FILE_DIR = "src/test/resources/org/openecomp/sdc/be/components/impl/json/";

    @Test
    public void artifactDefinitionShouldBeDeserializedProperly() throws IOException {
        ArtifactDefinition ad = new ArtifactDefinition();
        ad.setArtifactName("artifactName");
        ad.setArtifactLabel("label");
        ad.setArtifactType("artifactType");
        ad.setDescription("description");
        ad.setPayloadData("payloadData");
        ad.setArtifactDisplayName("displayName");
        ad.setArtifactGroupType(ArtifactGroupTypeEnum.DEPLOYMENT);

        String expected = readJsonFile("artifact_definition.json");
        String actual = buildJsonStringForCsarVfcArtifact(ad);

        assertThat(actual, equalTo(expected));
    }

    private String readJsonFile(String fileName) throws IOException {
        return FileUtils.readFileToString(new File(JSON_FILE_DIR + fileName), "UTF8");
    }
}
