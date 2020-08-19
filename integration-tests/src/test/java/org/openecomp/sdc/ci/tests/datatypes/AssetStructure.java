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

public class AssetStructure {

	private String uuid;
	private String invariantUUID;
	private String name;
	private String version;
	private String toscaModelURL;
	private String category;
	private String lifecycleState;
	private String lastUpdaterUserId;

	public AssetStructure() {
		super();
	}

	public AssetStructure(String uuid, String invariantUUID, String name, String version, String toscaModelURL,
			String category, String lifecycleState, String lastUpdaterUserId) {
		super();
		this.uuid = uuid;
		this.invariantUUID = invariantUUID;
		this.name = name;
		this.version = version;
		this.toscaModelURL = toscaModelURL;
		this.category = category;
		this.lifecycleState = lifecycleState;
		this.lastUpdaterUserId = lastUpdaterUserId;
	}

	@Override
	public String toString() {
		return "AssetStructure [uuid=" + uuid + ", invariantUUID=" + invariantUUID + ", name=" + name + ", version="
				+ version + ", toscaModelURL=" + toscaModelURL + ", category=" + category + ", lifecycleState="
				+ lifecycleState + ", lastUpdaterUserId=" + lastUpdaterUserId + "]";
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getInvariantUUID() {
		return invariantUUID;
	}

	public void setInvariantUUID(String invariantUUID) {
		this.invariantUUID = invariantUUID;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getToscaModelURL() {
		return toscaModelURL;
	}

	public void setToscaModelURL(String toscaModelURL) {
		this.toscaModelURL = toscaModelURL;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getLifecycleState() {
		return lifecycleState;
	}

	public void setLifecycleState(String lifecycleState) {
		this.lifecycleState = lifecycleState;
	}

	public String getLastUpdaterUserId() {
		return lastUpdaterUserId;
	}

	public void setLastUpdaterUserId(String lastUpdaterUserId) {
		this.lastUpdaterUserId = lastUpdaterUserId;
	}

}
