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

package org.openecomp.sdc.be.components.distribution.engine;

import java.util.List;

import org.openecomp.sdc.be.model.ComponentInstance;

public class JsonContainerResourceInstance {
	private String resourceInstanceName, resourceName, resourceVersion, resoucreType, resourceUUID, resourceInvariantUUID, resourceCustomizationUUID, category, subcategory;
	private List<ArtifactInfoImpl> artifacts;

	public JsonContainerResourceInstance(ComponentInstance resourceInstance, String resourceType, List<ArtifactInfoImpl> artifacts) {
		super();
		this.resourceInstanceName = resourceInstance.getName();
		this.resourceName = resourceInstance.getComponentName();
		this.resourceVersion = resourceInstance.getComponentVersion();
		this.resoucreType = resourceType;
		this.resourceUUID = resourceInstance.getComponentUid();
		this.artifacts = artifacts;
		this.resourceCustomizationUUID = resourceInstance.getCustomizationUUID();
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

	public String getResourceVersion() {
		return resourceVersion;
	}

	public void setResourceVersion(String resourceVersion) {
		this.resourceVersion = resourceVersion;
	}

	public String getResoucreType() {
		return resoucreType;
	}

	public void setResoucreType(String resoucreType) {
		this.resoucreType = resoucreType;
	}

	public String getResourceUUID() {
		return resourceUUID;
	}

	public void setResourceUUID(String resourceUUID) {
		this.resourceUUID = resourceUUID;
	}

	public List<ArtifactInfoImpl> getArtifacts() {
		return artifacts;
	}

	public void setArtifacts(List<ArtifactInfoImpl> artifacts) {
		this.artifacts = artifacts;
	}

	public String getResourceInvariantUUID() {
		return resourceInvariantUUID;
	}

	public void setResourceInvariantUUID(String resourceInvariantUUID) {
		this.resourceInvariantUUID = resourceInvariantUUID;
	}

	public String getResourceCustomizationUUID() {
		return resourceCustomizationUUID;
	}

	public void setResourceCustomizationUUID(String customizationUUID) {
		this.resourceCustomizationUUID = customizationUUID;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getSubcategory() {
		return subcategory;
	}

	public void setSubcategory(String subcategory) {
		this.subcategory = subcategory;
	}
}
