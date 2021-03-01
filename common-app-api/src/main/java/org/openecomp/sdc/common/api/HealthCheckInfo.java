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

public class HealthCheckInfo {
	
	private String healthCheckComponent;
	private HealthCheckStatus healthCheckStatus;
	private String version;
	private String description;
	private List<HealthCheckInfo> componentsInfo;

	public void setHealthCheckComponent(String healthCheckComponent) {
		this.healthCheckComponent = healthCheckComponent;
	}

	public HealthCheckInfo(String healthCheckComponent, HealthCheckStatus healthCheckStatus,
			String version, String description) {
		super();
		this.healthCheckComponent = healthCheckComponent;
		this.healthCheckStatus = healthCheckStatus;
		this.version = version;
		this.description = description;
	}

	public HealthCheckInfo(String healthCheckComponent, HealthCheckStatus healthCheckStatus,
						   String version, String description, List<HealthCheckInfo> componentsInfo) {
		super();
		this.healthCheckComponent = healthCheckComponent;
		this.healthCheckStatus = healthCheckStatus;
		this.version = version;
		this.description = description;
		this.componentsInfo = componentsInfo;
}

	public HealthCheckInfo() {
		super();
	}

	public String getHealthCheckComponent() {
		return healthCheckComponent;
	}

	public HealthCheckStatus getHealthCheckStatus() {
		return healthCheckStatus;
	}

	public void setHealthCheckStatus(HealthCheckStatus healthCheckStatus) {
		this.healthCheckStatus = healthCheckStatus;
	}

	public List<HealthCheckInfo> getComponentsInfo() {
		return componentsInfo;
	}

	public void setComponentsInfo(List<HealthCheckInfo> componentsInfo) {
		this.componentsInfo = componentsInfo;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public enum HealthCheckStatus {
		UP, DOWN, UNKNOWN;
	}

	@Override
	public String toString() {
		return "HealthCheckInfo [healthCheckComponent=" + healthCheckComponent + ", healthCheckStatus="
				+ healthCheckStatus + ", version=" + version + ", description=" + description + ", componentsInfo="
				+ componentsInfo + "]";
	}
	
}
