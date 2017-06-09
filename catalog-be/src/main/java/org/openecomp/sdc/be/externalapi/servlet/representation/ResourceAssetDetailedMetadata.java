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

package org.openecomp.sdc.be.externalapi.servlet.representation;

import java.util.List;

public class ResourceAssetDetailedMetadata extends ResourceAssetMetadata {

	private String lastUpdaterFullName;
	private String toscaResourceName;
	private List<ResourceInstanceMetadata> resources;
	private List<ArtifactMetadata> artifacts;
	private String description;

	public String getLastUpdaterFullName() {
		return lastUpdaterFullName;
	}

	public void setLastUpdaterFullName(String lastUpdaterFullName) {
		this.lastUpdaterFullName = lastUpdaterFullName;
	}

	public String getToscaResourceName() {
		return toscaResourceName;
	}

	public void setToscaResourceName(String toscaResourceName) {
		this.toscaResourceName = toscaResourceName;
	}

	public List<ResourceInstanceMetadata> getResources() {
		return resources;
	}

	public void setResources(List<ResourceInstanceMetadata> resources) {
		this.resources = resources;
	}

	public List<ArtifactMetadata> getArtifacts() {
		return artifacts;
	}

	public void setArtifacts(List<ArtifactMetadata> artifactMetaList) {
		this.artifacts = artifactMetaList;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
}
