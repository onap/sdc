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

public class ServiceDetailedAssetStructure extends ServiceAssetStructure {

	String lastUpdaterFullName;
	List<ResourceInstanceAssetStructure> resources;
	List<ArtifactAssetStructure> artifacts;

	public ServiceDetailedAssetStructure() {
		super();
	}

	public ServiceDetailedAssetStructure(String uuid, String invariantUUID, String name, String version,
			String toscaModelURL, String category, String lifecycleState, String lastUpdaterUserId) {
		super(uuid, invariantUUID, name, version, toscaModelURL, category, lifecycleState, lastUpdaterUserId);
	}

	public ServiceDetailedAssetStructure(String lastUpdaterFullName, List<ResourceInstanceAssetStructure> resources,
			List<ArtifactAssetStructure> artifacts) {
		super();
		this.lastUpdaterFullName = lastUpdaterFullName;
		this.resources = resources;
		this.artifacts = artifacts;
	}

	public String getLastUpdaterFullName() {
		return lastUpdaterFullName;
	}

	public void setLastUpdaterFullName(String lastUpdaterFullName) {
		this.lastUpdaterFullName = lastUpdaterFullName;
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
		return "ServiceDetailedAssetStructure [lastUpdaterFullName=" + lastUpdaterFullName + ", resources=" + resources
				+ ", artifacts=" + artifacts + "]";
	}

}
