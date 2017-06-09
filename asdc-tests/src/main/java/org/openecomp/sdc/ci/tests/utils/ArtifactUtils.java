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

package org.openecomp.sdc.ci.tests.utils;

import org.apache.commons.codec.binary.Base64;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.ci.tests.datatypes.ArtifactReqDetails;
import org.openecomp.sdc.common.api.ArtifactGroupTypeEnum;

public class ArtifactUtils {
	/**
	 * Converts ArtifactReqDetails to ArtifactDefinition
	 * @param artifactReq
	 * @return
	 */
	public static ArtifactDefinition convertArtifactReqToDefinition(ArtifactReqDetails artifactReq) {
		ArtifactDefinition artifact = new ArtifactDefinition();
		artifact.setArtifactLabel(artifactReq.getArtifactLabel());
		artifact.setArtifactDisplayName(artifactReq.getArtifactDisplayName());
		artifact.setArtifactGroupType(ArtifactGroupTypeEnum.findType(artifactReq.getArtifactGroupType()));
		artifact.setArtifactType(artifactReq.getArtifactType().toUpperCase());
		artifact.setArtifactName(artifactReq.getArtifactName());
		artifact.setDescription(artifactReq.getDescription());
		artifact.setUniqueId(artifactReq.getUniqueId());
		artifact.setTimeout(artifactReq.getTimeout());
		artifact.setEsId(artifactReq.getUniqueId());

		return artifact;
	}
	/**
	 * Converts ArtifactDefinition to ArtifactReqDetails
	 * @param artifactDef
	 * @return
	 */
	public static ArtifactReqDetails convertArtifactDefinitionToArtifactReqDetails( ArtifactDefinition artifactDef) {
		ArtifactReqDetails artifactReq = new ArtifactReqDetails();
		artifactReq.setArtifactLabel(artifactDef.getArtifactLabel());
		artifactReq.setArtifactDisplayName(artifactDef.getArtifactDisplayName());
		artifactReq.setArtifactGroupType(artifactDef.getArtifactGroupType().getType());
		artifactReq.setArtifactType(artifactDef.getArtifactType().toUpperCase());
		artifactReq.setArtifactName(artifactDef.getArtifactName());
		artifactReq.setDescription(artifactDef.getDescription());
		artifactReq.setUniqueId(artifactDef.getUniqueId());
		artifactReq.setTimeout(artifactDef.getTimeout());
		artifactReq.setPayload(Base64.encodeBase64String(artifactDef.getPayloadData()));
		return artifactReq;
	}
}
