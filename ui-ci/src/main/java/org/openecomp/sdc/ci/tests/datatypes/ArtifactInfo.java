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

package org.openecomp.sdc.ci.tests.datatypes;

public class ArtifactInfo {

	private String filepath;
	private String filename;
	private String description;
	private String artifactType;
	private String artifactLabel;
	private String artifactVersion;

	public ArtifactInfo(String filepath, String filename, String description, String artifactLabel,
			String artifactType) {
		super();
		this.filepath = filepath;
		this.filename = filename;
		this.description = description;
		this.artifactType = artifactType;
		this.artifactLabel = artifactLabel;
	}
	
	public ArtifactInfo(String filepath, String filename, String description, String artifactLabel,
			String artifactType, String artifactVersion) {
		super();
		this.filepath = filepath;
		this.filename = filename;
		this.description = description;
		this.artifactType = artifactType;
		this.artifactLabel = artifactLabel;
		this.artifactVersion = artifactVersion;
	}

	public ArtifactInfo() {
		super();
	}
	
	public String getArtifactVersion() {
		return artifactVersion;
	}

	public void setArtifactVersion(String artifactVersion) {
		this.artifactVersion = artifactVersion;
	}

	public String getFilepath() {
		return filepath;
	}

	public void setFilepath(String filepath) {
		this.filepath = filepath;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getArtifactType() {
		return artifactType;
	}

	public void setArtifactType(String artifactType) {
		this.artifactType = artifactType;
	}

	public String getArtifactLabel() {
		return artifactLabel;
	}

	public void setArtifactLabel(String artifactLabel) {
		this.artifactLabel = artifactLabel;
	}

}
