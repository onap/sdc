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
 */

package org.openecomp.sdc.be.datamodel.utils;

import java.util.Base64;
import org.openecomp.sdc.be.info.ArtifactTemplateInfo;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.common.api.ArtifactGroupTypeEnum;
import org.openecomp.sdc.common.api.ArtifactTypeEnum;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.util.ValidationUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class ArtifactUtils {

    public static ArtifactDefinition findMasterArtifact(Map<String, ArtifactDefinition> deplymentArtifact, List<ArtifactDefinition> artifacts, List<String> artifactsList) {
        for (String artifactUid : artifactsList) {
            for (Entry<String, ArtifactDefinition> entry : deplymentArtifact.entrySet()) {
                ArtifactDefinition artifact = entry.getValue();
                if (artifactUid.equalsIgnoreCase(artifact.getUniqueId())) {
                    artifacts.add(artifact);
                }

            }
        }
        if (artifacts.size() == 1) {
            return artifacts.get(0);
        }
        ArtifactDefinition masterArtifact = null;
        for (ArtifactDefinition artifactInfo : artifacts) {
            String atrifactType = artifactInfo.getArtifactType();
            if (atrifactType.equalsIgnoreCase(ArtifactTypeEnum.HEAT_VOL.getType()) || atrifactType.equalsIgnoreCase(ArtifactTypeEnum.HEAT_NET.getType())) {
                masterArtifact = artifactInfo;
                continue;
            }
            if (atrifactType.equalsIgnoreCase(ArtifactTypeEnum.HEAT.getType())) {
                masterArtifact = artifactInfo;
                break;
            }
        }
        return masterArtifact;
    }

    public static Map<String, Object> buildJsonForUpdateArtifact(String artifactId, String artifactName, String artifactType,
                                                                 ArtifactGroupTypeEnum artifactGroupType, String label, String displayName, String description,
                                                                 byte[] artifactContentent, List<ArtifactTemplateInfo> updatedRequiredArtifacts, boolean isFromCsar) {

        Map<String, Object> json = new HashMap<>();
        if (artifactId != null && !artifactId.isEmpty())
            json.put(Constants.ARTIFACT_ID, artifactId);

        json.put(Constants.ARTIFACT_NAME, artifactName);
        json.put(Constants.ARTIFACT_TYPE, artifactType);
        json.put(Constants.ARTIFACT_DESCRIPTION, description);
        json.put(Constants.IS_FROM_CSAR, isFromCsar);

        String encodedPayload = Base64.getEncoder().encodeToString(artifactContentent);

        json.put(Constants.ARTIFACT_PAYLOAD_DATA, encodedPayload);
        json.put(Constants.ARTIFACT_DISPLAY_NAME, displayName);
        json.put(Constants.ARTIFACT_LABEL, label);
        json.put(Constants.ARTIFACT_GROUP_TYPE, artifactGroupType.getType());
        json.put(Constants.REQUIRED_ARTIFACTS,
                (updatedRequiredArtifacts == null || updatedRequiredArtifacts.isEmpty()) ? new ArrayList<>()
                        : updatedRequiredArtifacts.stream()
                                                  .filter(e -> e.getType().equals(ArtifactTypeEnum.HEAT_ARTIFACT.getType())
                                        || e.getType().equals(ArtifactTypeEnum.HEAT_NESTED.getType()))
                                                  .map(ArtifactTemplateInfo::getFileName).collect(Collectors.toList()));
        return json;
    }

    public static Map<String, Object> buildJsonForArtifact(ArtifactTemplateInfo artifactTemplateInfo,
			byte[] artifactContentent, int atrifactLabelCounter, boolean isFromcsar) {

        Map<String, Object> json = new HashMap<>();
        String artifactName = artifactTemplateInfo.getFileName();

        json.put(Constants.ARTIFACT_NAME, artifactTemplateInfo.getFileName());
        json.put(Constants.ARTIFACT_TYPE, artifactTemplateInfo.getType());
        json.put(Constants.ARTIFACT_DESCRIPTION, "created from csar");
		json.put(Constants.IS_FROM_CSAR, isFromcsar);

        String encodedPayload = Base64.getEncoder().encodeToString(artifactContentent);

        json.put(Constants.ARTIFACT_PAYLOAD_DATA, encodedPayload);
        String displayName = artifactName;
        if (artifactName.lastIndexOf(".") > 0)
            displayName = artifactName.substring(0, artifactName.lastIndexOf("."));
        json.put(Constants.ARTIFACT_DISPLAY_NAME, displayName);
        String label = ValidationUtils.normalizeArtifactLabel(artifactTemplateInfo.getType() + atrifactLabelCounter);
        json.put(Constants.ARTIFACT_LABEL, label);
        json.put(Constants.ARTIFACT_GROUP_TYPE, ArtifactGroupTypeEnum.DEPLOYMENT.getType());
        List<ArtifactTemplateInfo> requiredArtifacts = artifactTemplateInfo.getRelatedArtifactsInfo();
        json.put(Constants.REQUIRED_ARTIFACTS,
                (requiredArtifacts == null || requiredArtifacts.isEmpty()) ? new ArrayList<>()
                        : requiredArtifacts.stream()
                                           .filter(e -> e.getType().equals(ArtifactTypeEnum.HEAT_ARTIFACT.getType())
                                        || e.getType().equals(ArtifactTypeEnum.HEAT_NESTED.getType()))
                                           .map(ArtifactTemplateInfo::getFileName).collect(Collectors.toList()));
        return json;
    }

    public static ArtifactDefinition findArtifactInList(List<ArtifactDefinition> createdArtifacts, String artifactId) {
        for (ArtifactDefinition artifact : createdArtifacts) {
            if (artifact.getUniqueId().equals(artifactId)) {
                return artifact;
            }
        }
        return null;
    }
}
