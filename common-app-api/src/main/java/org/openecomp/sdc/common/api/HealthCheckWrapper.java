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

import java.util.List;

public class HealthCheckWrapper {

	String sdcVersion;
	String siteMode;
	List<HealthCheckInfo> componentsInfo;

	public HealthCheckWrapper(List<HealthCheckInfo> componentsInfo, String sdcVersion, String siteMode) {
		super();
		this.componentsInfo = componentsInfo;
		this.sdcVersion = sdcVersion;
		this.siteMode = siteMode;
	}

	public List<HealthCheckInfo> getComponentsInfo() {
		return componentsInfo;
	}

	public void setComponentsInfo(List<HealthCheckInfo> componentsInfo) {
		this.componentsInfo = componentsInfo;
	}

	public String getSdcVersion() {
		return sdcVersion;
	}

	public void setSdcVersion(String sdcVersion) {
		this.sdcVersion = sdcVersion;
	}

	public String getSiteMode() {
		return siteMode;
	}

	public void setSiteMode(String siteMode) {
		this.siteMode = siteMode;
	}

}
