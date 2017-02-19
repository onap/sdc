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

public class ResourceArtifactInfoImpl extends ArtifactInfoImpl implements IResourceArtifactInfo {

	private String resourceName;
	private String resourceVersion;
	private String resourceUUID;

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

	public String getResourceUUID() {
		return resourceUUID;
	}

	public void setResourceUUID(String resourceUUID) {
		this.resourceUUID = resourceUUID;
	}

	@Override
	public String toString() {
		return "ResourceArtifactInfoImpl [resourceName=" + resourceName + ", resourceVersion=" + resourceVersion + ", resourceUUID=" + resourceUUID + super.toString() + "]";
	}

}
