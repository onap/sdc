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

import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.fe.config.Configuration;
import org.openecomp.sdc.fe.config.ConfigurationManager;

import javax.servlet.ServletContext;
import javax.ws.rs.core.Response;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class HealthCheckService {

    private static final Logger healthLogger = Logger.getLogger("asdc.fe.healthcheck");
    /**
     * This executor will execute the health check task.
     */
    private ScheduledExecutorService healthCheckExecutor =
            Executors.newSingleThreadScheduledExecutor((Runnable r) -> new Thread(r, "FE-Health-Check-Thread"));

    private final HealthCheckScheduledTask task ;


    public HealthCheckService(ServletContext context) {
        this.context = context;
        this.task = new HealthCheckScheduledTask(this);
    }

    public Configuration getConfig(){
        return ((ConfigurationManager) context.getAttribute(Constants.CONFIGURATION_MANAGER_ATTR))
                .getConfiguration();
    }

    void setLastHealthStatus(HealthStatus lastHealthStatus) {
        this.lastHealthStatus = lastHealthStatus;
    }

    private HealthStatus lastHealthStatus = new HealthStatus(500, "{}");
    private ServletContext context;

    public void start(int interval) {
        this.healthCheckExecutor.scheduleAtFixedRate( getTask() , 0, interval, TimeUnit.SECONDS);
    }

    /**
     * To be used by the HealthCheckServlet
     *
     * @return
     */
    public Response getFeHealth() {
        return this.buildResponse(lastHealthStatus);
    }

    private Response buildResponse(HealthStatus healthStatus) {
        healthLogger.trace("FE and BE health check status: {}", healthStatus.getBody());
        return Response.status(healthStatus.getStatusCode()).entity(healthStatus.getBody()).build();
    }

    public HealthStatus getLastHealthStatus() {
        return lastHealthStatus;
    }
    public HealthCheckScheduledTask getTask() {
        return task;
    }

    //immutable
    static class HealthStatus {

        public void setBody(String body) {
            this.body = body;
        }

        public void setStatusCode(int statusCode) {
            this.statusCode = statusCode;
        }

        private String body;
        private int statusCode;

        public HealthStatus(int code, String body) {
            this.body = body;
            this.statusCode = code;
        }

        public int getStatusCode() {
            return statusCode;
        }

        public String getBody() {
            return body;
        }
    }



}
