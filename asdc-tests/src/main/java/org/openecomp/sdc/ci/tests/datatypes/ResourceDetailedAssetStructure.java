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

public class ResourceDetailedAssetStructure extends ResourceAssetStructure {

	private String lastUpdaterFullName;
	private String toscaResourceName;
	private List<ResourceInstanceAssetStructure> resources;
	private List<ArtifactAssetStructure> artifacts;

	public ResourceDetailedAssetStructure() {
		super();
	}

	public ResourceDetailedAssetStructure(String lastUpdaterFullName, String toscaResourceName,
			List<ResourceInstanceAssetStructure> resources, List<ArtifactAssetStructure> artifacts) {
		super();
		this.lastUpdaterFullName = lastUpdaterFullName;
		this.toscaResourceName = toscaResourceName;
		this.resources = resources;
		this.artifacts = artifacts;
	}

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

	public List<ResourceInstanceAssetStructure> getResources() {
		return resources;
	}

	public void setResources(List<ResourceInstanceAssetStructure> resources) {
		this.resources = resources;
	}

	public List<ArtifactAssetStructure> getArtifacts() {
		return artifacts;
	}

	public void setArtifacts(List<ArtifactAssetStructure> artifacts) {
		this.artifacts = artifacts;
	}

	@Override
	public String toString() {
		return "ResourceDetailedAssetStructure [lastUpdaterFullName=" + lastUpdaterFullName + ", toscaResourceName="
				+ toscaResourceName + ", resources=" + resources + ", artifacts=" + artifacts + ", toString()="
				+ super.toString() + ", getSubCategory()=" + getSubCategory() + ", getResourceType()="
				+ getResourceType() + ", getUuid()=" + getUuid() + ", getInvariantUUID()=" + getInvariantUUID()
				+ ", getName()=" + getName() + ", getVersion()=" + getVersion() + ", getToscaModelURL()="
				+ getToscaModelURL() + ", getCategory()=" + getCategory() + ", getLifecycleState()="
				+ getLifecycleState() + ", getLastUpdaterUserId()=" + getLastUpdaterUserId() + ", getClass()="
				+ getClass() + ", hashCode()=" + hashCode() + "]";
	}

}
