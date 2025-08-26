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
package org.openecomp.sdc.fe.impl;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.servlet.ServletContext;

import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.fe.config.Configuration;
import org.openecomp.sdc.fe.config.ConfigurationManager;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class HealthCheckService {

    private static final Logger healthLogger = Logger.getLogger("asdc.fe.healthcheck");
    private final HealthCheckScheduledTask task;

    /**
     * This executor will execute the health check task.
     */
    private ScheduledExecutorService healthCheckExecutor = Executors
        .newSingleThreadScheduledExecutor(r -> new Thread(r, "FE-Health-Check-Thread"));

    private HealthStatus lastHealthStatus = new HealthStatus(500, "{}");
    private ServletContext context;

    public HealthCheckService(ServletContext context) {
        this.context = context;
        this.task = new HealthCheckScheduledTask(this);
    }

    public Configuration getConfig() {
        return ((ConfigurationManager) context.getAttribute(Constants.CONFIGURATION_MANAGER_ATTR)).getConfiguration();
    }

    public void start(int interval) {
        this.healthCheckExecutor.scheduleAtFixedRate(getTask(), 0, interval, TimeUnit.SECONDS);
    }

    /**
     * To be used by the HealthCheckServlet
     *
     * @return ResponseEntity representing FE health status
     */
    public ResponseEntity<String> getFeHealth() {
        return this.buildResponse(lastHealthStatus);
    }

    private ResponseEntity<String> buildResponse(HealthStatus healthStatus) {
        healthLogger.trace("FE and BE health check status: {}", healthStatus.getBody());
        return ResponseEntity.status(HttpStatus.valueOf(healthStatus.getStatusCode()))
                             .body(healthStatus.getBody());
    }

    public HealthStatus getLastHealthStatus() {
        return lastHealthStatus;
    }

    void setLastHealthStatus(HealthStatus lastHealthStatus) {
        this.lastHealthStatus = lastHealthStatus;
    }

    public HealthCheckScheduledTask getTask() {
        return task;
    }

    // Immutable inner class for status
    static class HealthStatus {

        private String body;
        private int statusCode;

        public HealthStatus(int code, String body) {
            this.body = body;
            this.statusCode = code;
        }

        public int getStatusCode() {
            return statusCode;
        }

        public void setStatusCode(int statusCode) {
            this.statusCode = statusCode;
        }

        public String getBody() {
            return body;
        }

        public void setBody(String body) {
            this.body = body;
        }
    }
}
