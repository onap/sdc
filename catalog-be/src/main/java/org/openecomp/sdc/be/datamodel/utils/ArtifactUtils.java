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

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.common.api.ArtifactTypeEnum;

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
}
