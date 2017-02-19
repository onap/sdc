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

package org.openecomp.sdc.common.api;

public class UploadArtifactInfo {

	public UploadArtifactInfo() {

	}

	public UploadArtifactInfo(String artifactName, String artifactPath, ArtifactTypeEnum artifactType,
			String artifactDescription) {
		super();
		this.artifactName = artifactName;
		this.artifactPath = artifactPath;
		this.artifactType = artifactType;
		this.artifactDescription = artifactDescription;
	}

	public UploadArtifactInfo(String artifactName, String artifactPath, ArtifactTypeEnum artifactType,
			String artifactDescription, String artifactData) {
		super();
		this.artifactName = artifactName;
		this.artifactPath = artifactPath;
		this.artifactType = artifactType;
		this.artifactDescription = artifactDescription;
		this.artifactData = artifactData;
	}

	private String artifactName;
	private String artifactPath;
	private ArtifactTypeEnum artifactType;
	private String artifactDescription;
	private String artifactData;

	public String getArtifactName() {
		return artifactName;
	}

	public void setArtifactName(String artifactName) {
		this.artifactName = artifactName;
	}

	public String getArtifactPath() {
		return artifactPath;
	}

	public void setArtifactPath(String artifactPath) {
		this.artifactPath = artifactPath;
	}

	public ArtifactTypeEnum getArtifactType() {
		return artifactType;
	}

	public void setArtifactType(ArtifactTypeEnum artifactType) {
		this.artifactType = artifactType;
	}

	public String getArtifactDescription() {
		return artifactDescription;
	}

	public void setArtifactDescription(String artifactDescription) {
		this.artifactDescription = artifactDescription;
	}

	public String getArtifactData() {
		return artifactData;
	}

	public void setArtifactData(String artifactData) {
		this.artifactData = artifactData;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((artifactData == null) ? 0 : artifactData.hashCode());
		result = prime * result + ((artifactDescription == null) ? 0 : artifactDescription.hashCode());
		result = prime * result + ((artifactName == null) ? 0 : artifactName.hashCode());
		result = prime * result + ((artifactPath == null) ? 0 : artifactPath.hashCode());
		result = prime * result + ((artifactType == null) ? 0 : artifactType.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		UploadArtifactInfo other = (UploadArtifactInfo) obj;
		if (artifactData == null) {
			if (other.artifactData != null)
				return false;
		} else if (!artifactData.equals(other.artifactData))
			return false;
		if (artifactDescription == null) {
			if (other.artifactDescription != null)
				return false;
		} else if (!artifactDescription.equals(other.artifactDescription))
			return false;
		if (artifactName == null) {
			if (other.artifactName != null)
				return false;
		} else if (!artifactName.equals(other.artifactName))
			return false;
		if (artifactPath == null) {
			if (other.artifactPath != null)
				return false;
		} else if (!artifactPath.equals(other.artifactPath))
			return false;
		if (artifactType != other.artifactType)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "UploadArtifactInfo [artifactName=" + artifactName + ", artifactPath=" + artifactPath + ", artifactType="
				+ artifactType + ", artifactDescription=" + artifactDescription + ", artifactData=" + artifactData
				+ "]";
	}

}
