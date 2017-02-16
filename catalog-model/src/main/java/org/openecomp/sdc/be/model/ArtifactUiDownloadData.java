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

package org.openecomp.sdc.be.model;

public class ArtifactUiDownloadData {

	// POJO for UI artifact download, holding artifact filename and base64
	// content
	String artifactName;
	String base64Contents;

	public void setArtifactName(String artifactName) {
		this.artifactName = artifactName;
	}

	public void setBase64Contents(String base64Contents) {
		this.base64Contents = base64Contents;
	}

	public String getArtifactName() {
		return artifactName;
	}

	public String getBase64Contents() {
		return base64Contents;
	}
}
