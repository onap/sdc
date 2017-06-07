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

package org.openecomp.sdc.be.tosca.model;

public class VfModuleToscaMetadata implements IToscaMetadata {

	private String vfModuleModelName;
	private String vfModuleModelInvariantUUID;
	private String vfModuleModelUUID;
	private String vfModuleModelVersion;
	private String vfModuleModelCustomizationUUID;

	@Override
	public void setName(String name) {
		vfModuleModelName = name;
	}

	@Override
	public void setInvariantUUID(String invariantUUID) {
		vfModuleModelInvariantUUID = invariantUUID;
	}

	@Override
	public void setUUID(String uUID) {
		vfModuleModelUUID = uUID;
	}

	@Override
	public void setVersion(String version) {
		vfModuleModelVersion = version;
	}

	public String getVfModuleModelName() {
		return vfModuleModelName;
	}

	public String getVfModuleModelInvariantUUID() {
		return vfModuleModelInvariantUUID;
	}

	public String getVfModuleModelUUID() {
		return vfModuleModelUUID;
	}

	public String getVfModuleModelVersion() {
		return vfModuleModelVersion;
	}

	public String getVfModuleModelCustomizationUUID() {
		return vfModuleModelCustomizationUUID;
	}

	public void setCustomizationUUID(String customizationUUID) {
		this.vfModuleModelCustomizationUUID = customizationUUID;
	}

}
