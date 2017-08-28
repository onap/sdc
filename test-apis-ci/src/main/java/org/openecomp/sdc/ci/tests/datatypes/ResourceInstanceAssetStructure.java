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

import java.util.List;

public class ResourceInstanceAssetStructure {

	String resourceInstanceName;
	String resourceName;
	String resourceInvariantUUID;
	String resourceVersion;
	String resourceType;
	String resourceUUID;
	List<ArtifactAssetStructure> artifacts;

	public ResourceInstanceAssetStructure() {
		super();
	}

	public ResourceInstanceAssetStructure(String resourceInstanceName, String resourceName,
			String resourceInvariantUUID, String resourceVersion, String resourceType, String resourceUUID,
			List<ArtifactAssetStructure> artifacts) {
		super();
		this.resourceInstanceName = resourceInstanceName;
		this.resourceName = resourceName;
		this.resourceInvariantUUID = resourceInvariantUUID;
		this.resourceVersion = resourceVersion;
		this.resourceType = resourceType;
		this.resourceUUID = resourceUUID;
		this.artifacts = artifacts;
	}

	public String getResourceInstanceName() {
		return resourceInstanceName;
	}

	public void setResourceInstanceName(String resourceInstanceName) {
		this.resourceInstanceName = resourceInstanceName;
	}

	public String getResourceName() {
		return resourceName;
	}

	public void setResourceName(String resourceName) {
		this.resourceName = resourceName;
	}

	public String getResourceInvariantUUID() {
		return resourceInvariantUUID;
	}

	public void setResourceInvariantUUID(String resourceInvariantUUID) {
		this.resourceInvariantUUID = resourceInvariantUUID;
	}

	public String getResourceVersion() {
		return resourceVersion;
	}

	public void setResourceVersion(String resourceVersion) {
		this.resourceVersion = resourceVersion;
	}

	public String getResourceType() {
		return resourceType;
	}

	public void setResourceType(String resourceType) {
		this.resourceType = resourceType;
	}

	public String getResourceUUID() {
		return resourceUUID;
	}

	public void setResourceUUID(String resourceUUID) {
		this.resourceUUID = resourceUUID;
	}

	public List<ArtifactAssetStructure> getArtifacts() {
		return artifacts;
	}

	public void setArtifacts(List<ArtifactAssetStructure> artifacts) {
		this.artifacts = artifacts;
	}

	@Override
	public String toString() {
		return "ResourceInstanceAssetStructure [resourceInstanceName=" + resourceInstanceName + ", resourceName="
				+ resourceName + ", resourceInvariantUUID=" + resourceInvariantUUID + ", resourceVersion="
				+ resourceVersion + ", resourceType=" + resourceType + ", resourceUUID=" + resourceUUID + ", artifacts="
				+ artifacts + "]";
	}

}
