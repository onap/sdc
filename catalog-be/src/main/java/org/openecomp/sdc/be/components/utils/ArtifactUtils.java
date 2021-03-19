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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.HashMap;
import java.util.Map;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.common.api.ArtifactGroupTypeEnum;
import org.openecomp.sdc.common.api.Constants;

public final class ArtifactUtils {

    private ArtifactUtils() {
    }

    public static String buildJsonStringForCsarVfcArtifact(ArtifactDefinition artifact) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Map<String, Object> json = new HashMap<>();
        String artifactName = artifact.getArtifactName();
        json.put(Constants.ARTIFACT_NAME, artifactName);
        json.put(Constants.ARTIFACT_LABEL, artifact.getArtifactLabel());
        json.put(Constants.ARTIFACT_TYPE, artifact.getArtifactType());
        json.put(Constants.ARTIFACT_GROUP_TYPE, ArtifactGroupTypeEnum.DEPLOYMENT.getType());
        json.put(Constants.ARTIFACT_DESCRIPTION, artifact.getDescription());
        json.put(Constants.ARTIFACT_PAYLOAD_DATA, artifact.getPayloadData());
        json.put(Constants.ARTIFACT_DISPLAY_NAME, artifact.getArtifactDisplayName());
        return gson.toJson(json);
    }
}
