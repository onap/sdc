/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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
package org.openecomp.sdcrests.health.types;

public class HealthInfoDto {

    private MonitoredModules healthCheckComponent;
    private HealthCheckStatus healthStatus;
    private String version;
    private String description;

    public HealthInfoDto() {
    }

    public HealthInfoDto(MonitoredModules healthCheckComponent, HealthCheckStatus healthStatus, String version, String description) {
        this.healthCheckComponent = healthCheckComponent;
        this.healthStatus = healthStatus;
        this.version = version;
        this.description = description;
    }

    public MonitoredModules getHealthCheckComponent() {
        return healthCheckComponent;
    }

    public void setHealthCheckComponent(MonitoredModules healthCheckComponent) {
        this.healthCheckComponent = healthCheckComponent;
    }

    public HealthCheckStatus getHealthStatus() {
        return healthStatus;
    }

    public void setHealthStatus(HealthCheckStatus healthStatus) {
        this.healthStatus = healthStatus;
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

    @Override
    public String toString() {
        return "HealthInfo{" + "healthCheckComponent='" + healthCheckComponent + '\'' + ", healthStatus=" + healthStatus + ", version='" + version
            + '\'' + ", description='" + description + '\'' + '}';
    }
}
