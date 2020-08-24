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

package org.onap.sdc.backend.ci.tests.tosca.datatypes;

import java.io.Serializable;

public class ToscaServiceGroupsMetadataDefinition implements Serializable {

	public static final long serialVersionUID = -6373756459974949120L;

	public String vfModuleModelName;
	public String vfModuleModelInvariantUUID;
	public String vfModuleModelCustomizationUUID;
	public String vfModuleModelUUID;
	public String vfModuleModelVersion;
	
	public ToscaServiceGroupsMetadataDefinition() {
		super();
	}

	public String getVfModuleModelName() {
		return vfModuleModelName;
	}

	public void setVfModuleModelName(String vfModuleModelName) {
		this.vfModuleModelName = vfModuleModelName;
	}

	public String getVfModuleModelInvariantUUID() {
		return vfModuleModelInvariantUUID;
	}

	public void setVfModuleModelInvariantUUID(String vfModuleModelInvariantUUID) {
		this.vfModuleModelInvariantUUID = vfModuleModelInvariantUUID;
	}

	public String getVfModuleModelCustomizationUUID() {
		return vfModuleModelCustomizationUUID;
	}

	public void setVfModuleModelCustomizationUUID(String vfModuleModelCustomizationUUID) {
		this.vfModuleModelCustomizationUUID = vfModuleModelCustomizationUUID;
	}

	public String getVfModuleModelUUID() {
		return vfModuleModelUUID;
	}

	public void setVfModuleModelUUID(String vfModuleModelUUID) {
		this.vfModuleModelUUID = vfModuleModelUUID;
	}

	public String getVfModuleModelVersion() {
		return vfModuleModelVersion;
	}

	public void setVfModuleModelVersion(String vfModuleModelVersion) {
		this.vfModuleModelVersion = vfModuleModelVersion;
	}

	@Override
	public String toString() {
		return "ToscaGroupsMetadataDefinition [vfModuleModelName=" + vfModuleModelName + ", vfModuleModelInvariantUUID=" + vfModuleModelInvariantUUID + ", vfModuleModelCustomizationUUID=" + vfModuleModelCustomizationUUID
				+ ", vfModuleModelUUID=" + vfModuleModelUUID + ", vfModuleModelVersion=" + vfModuleModelVersion + "]";
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof ToscaServiceGroupsMetadataDefinition)) return false;

		ToscaServiceGroupsMetadataDefinition that = (ToscaServiceGroupsMetadataDefinition) o;

		if (vfModuleModelName != null ? !vfModuleModelName.equals(that.vfModuleModelName) : that.vfModuleModelName != null)
			return false;
		if (!vfModuleModelInvariantUUID.equals(that.vfModuleModelInvariantUUID)) return false;
		if (vfModuleModelCustomizationUUID != null ? !vfModuleModelCustomizationUUID.equals(that.vfModuleModelCustomizationUUID) : that.vfModuleModelCustomizationUUID != null)
			return false;
		if (vfModuleModelUUID != null ? !vfModuleModelUUID.equals(that.vfModuleModelUUID) : that.vfModuleModelUUID != null)
			return false;
		return vfModuleModelVersion != null ? vfModuleModelVersion.equals(that.vfModuleModelVersion) : that.vfModuleModelVersion == null;
	}

	@Override
	public int hashCode() {
		return vfModuleModelInvariantUUID.hashCode();
	}
}
