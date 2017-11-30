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

package org.openecomp.sdc.be.info;

import org.openecomp.sdc.be.model.ArtifactDefinition;

public class ArtifactDefinitionInfo {

	private String uniqueId;
	/** Specifies the display name of the artifact. */
	private String artifactName;
	private String artifactDisplayName;
	private String artifactVersion;
	private String artifactUUID;

	public ArtifactDefinitionInfo(ArtifactDefinition artifactDefinition) {
		uniqueId = artifactDefinition.getUniqueId();
		artifactName = artifactDefinition.getArtifactName();
		artifactDisplayName = artifactDefinition.getArtifactDisplayName();
		artifactVersion = artifactDefinition.getArtifactVersion();
		artifactUUID = artifactDefinition.getArtifactUUID();

	}

	public String getUniqueId() {
		return uniqueId;
	}

	public void setUniqueId(String uniqueId) {
		this.uniqueId = uniqueId;
	}

	public String getArtifactName() {
		return artifactName;
	}

	public void setArtifactName(String artifactName) {
		this.artifactName = artifactName;
	}

	public String getArtifactDisplayName() {
		return artifactDisplayName;
	}

	public void setArtifactDisplayName(String artifactDisplayName) {
		this.artifactDisplayName = artifactDisplayName;
	}

	public String getArtifactVersion() {
		return artifactVersion;
	}

	public void setArtifactVersion(String artifactVersion) {
		this.artifactVersion = artifactVersion;
	}

	public String getArtifactUUID() {
		return artifactUUID;
	}

	public void setArtifactUUID(String artifactUUID) {
		this.artifactUUID = artifactUUID;
	}

}
