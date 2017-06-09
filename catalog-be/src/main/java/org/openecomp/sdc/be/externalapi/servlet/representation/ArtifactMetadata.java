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

public class ArtifactMetadata {
	private String artifactName;
	private String artifactType;
	private String artifactURL;
	private String artifactDescription;
	private Integer artifactTimeout;
	private String artifactChecksum;
	private String artifactUUID;
	private String artifactVersion;
	private String generatedFromUUID;
	private String artifactLabel;
	private String artifactGroupType;
	
	public String getArtifactName() {
		return artifactName;
	}

	public void setArtifactName(String artifactName) {
		this.artifactName = artifactName;
	}

	public String getArtifactType() {
		return artifactType;
	}

	public void setArtifactType(String artifactType) {
		this.artifactType = artifactType;
	}

	public String getArtifactURL() {
		return artifactURL;
	}

	public void setArtifactURL(String artifactURL) {
		this.artifactURL = artifactURL;
	}

	public String getArtifactDescription() {
		return artifactDescription;
	}

	public void setArtifactDescription(String artifactDescription) {
		this.artifactDescription = artifactDescription;
	}

	public Integer getArtifactTimeout() {
		return artifactTimeout;
	}

	public void setArtifactTimeout(Integer artifactTimeout) {
		this.artifactTimeout = artifactTimeout;
	}

	public String getArtifactChecksum() {
		return artifactChecksum;
	}

	public void setArtifactChecksum(String artifactChecksum) {
		this.artifactChecksum = artifactChecksum;
	}

	public String getArtifactUUID() {
		return artifactUUID;
	}

	public void setArtifactUUID(String artifactUUID) {
		this.artifactUUID = artifactUUID;
	}

	public String getArtifactVersion() {
		return artifactVersion;
	}

	public void setArtifactVersion(String artifactVersion) {
		this.artifactVersion = artifactVersion;
	}

	public String getGeneratedFromUUID() {
		return generatedFromUUID;
	}

	public void setGeneratedFromUUID(String generatedFromUUID) {
		this.generatedFromUUID = generatedFromUUID;
	}

	public String getArtifactLabel() {
		return artifactLabel;
	}

	public void setArtifactLabel(String artifactLabel) {
		this.artifactLabel = artifactLabel;
	}

	public String getArtifactGroupType() {
		return artifactGroupType;
	}

	public void setArtifactGroupType(String artifactGroupType) {
		this.artifactGroupType = artifactGroupType;
	}

}
