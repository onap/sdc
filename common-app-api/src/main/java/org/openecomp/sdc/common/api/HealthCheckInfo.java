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

public class HealthCheckInfo {

	private HealthCheckComponent healthCheckComponent;
	private HealthCheckStatus healthCheckStatus;
	private String version;
	private String description;

	public HealthCheckInfo(HealthCheckComponent healthCheckComponent, HealthCheckStatus healthCheckStatus,
			String version, String description) {
		super();
		this.healthCheckComponent = healthCheckComponent;
		this.healthCheckStatus = healthCheckStatus;
		this.version = version;
		this.description = description;
	}

	public HealthCheckInfo() {
		super();
	}

	public HealthCheckComponent getHealthCheckComponent() {
		return healthCheckComponent;
	}

	public HealthCheckStatus getHealthCheckStatus() {
		return healthCheckStatus;
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

	public enum HealthCheckComponent {
		FE, BE, TITAN, ES, DE;
	}

	public enum HealthCheckStatus {
		UP, DOWN, UNKNOWN;
	}

	@Override
	public String toString() {
		return "HealthCheckInfo [healthCheckComponent=" + healthCheckComponent + ", healthCheckStatus="
				+ healthCheckStatus + ", version=" + version + ", description=" + description + "]";
	}
}
