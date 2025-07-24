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
package org.openecomp.sdcrests.health.rest.services;

import java.util.Arrays;
import java.util.Collection;
import javax.inject.Named;
import org.openecomp.sdc.common.session.SessionContextProviderFactory;
import org.openecomp.sdc.health.HealthCheckManager;
import org.openecomp.sdc.health.HealthCheckManagerFactory;
import org.openecomp.sdc.health.data.HealthCheckResult;
import org.openecomp.sdc.health.data.HealthCheckStatus;
import org.openecomp.sdc.health.data.HealthInfo;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Named
@Service("healthCheck")
@Scope(value = "prototype")
public class HealthCheckImpl implements org.openecomp.sdcrests.health.rest.HealthCheck {

    private static final Logger logger = LoggerFactory.getLogger(HealthCheckImpl.class);
    private HealthCheckManager healthCheckManager;

    public HealthCheckImpl() {
        try {
            healthCheckManager = HealthCheckManagerFactory.getInstance().createInterface();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    public ResponseEntity checkHealth() {
        HealthCheckResult healthCheckResult = new HealthCheckResult();
        SessionContextProviderFactory.getInstance().createInterface().create("public", "dox");
        try {
            Collection<HealthInfo> healthInfos = healthCheckManager.checkHealth();
            healthCheckResult.setComponentsInfo(healthInfos);
            boolean someIsDown = healthInfos.stream().anyMatch(healthInfo -> healthInfo.getHealthCheckStatus().equals(HealthCheckStatus.DOWN));
            healthInfos.stream().filter(healthInfo -> healthInfo.getHealthCheckComponent().equals(org.openecomp.sdc.health.data.MonitoredModules.BE))
                    .findFirst().ifPresent(healthInfo -> healthCheckResult.setSdcVersion(healthInfo.getVersion()));
            if (someIsDown) {
                return new ResponseEntity<>(healthCheckResult, HttpStatus.INTERNAL_SERVER_ERROR);
            }
            return ResponseEntity.ok(healthCheckResult);
        } catch (Exception ex) {
            logger.error("Health check failed", ex);
            HealthInfo healthInfo = new HealthInfo(org.openecomp.sdc.health.data.MonitoredModules.BE, HealthCheckStatus.DOWN, "",
                    "Failed to perform Health Check");
            Collection<HealthInfo> healthInfos = Arrays.asList(healthInfo);
            healthCheckResult.setComponentsInfo(healthInfos);
            return new ResponseEntity<>(healthCheckResult, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
