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

public class ResourceAssetStructure extends AssetStructure {

	private String subCategory;
	private String resourceType;

	public ResourceAssetStructure() {
		super();
	}

	public ResourceAssetStructure(String uuid, String invariantUUID, String name, String version, String toscaModelURL,
			String category, String lifecycleState, String lastUpdaterUserId) {
		super(uuid, invariantUUID, name, version, toscaModelURL, category, lifecycleState, lastUpdaterUserId);
	}

	@Override
	public String toString() {
		return "ResourceAssetStructure [subCategory=" + subCategory + ", resourceType=" + resourceType + "]";
	}

	public String getSubCategory() {
		return subCategory;
	}

	public void setSubCategory(String subCategory) {
		this.subCategory = subCategory;
	}

	public String getResourceType() {
		return resourceType;
	}

	public void setResourceType(String resourceType) {
		this.resourceType = resourceType;
	}

}
