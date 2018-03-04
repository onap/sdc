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

package org.openecomp.sdc.fe.servlets;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import org.openecomp.sdc.common.api.HealthCheckInfo;
import org.openecomp.sdc.common.api.HealthCheckWrapper;
import org.openecomp.sdc.common.config.EcompErrorEnum;
import org.openecomp.sdc.common.http.client.api.HttpResponse;
import org.openecomp.sdc.common.http.config.HttpClientConfig;
import org.openecomp.sdc.common.http.config.Timeouts;
import org.openecomp.sdc.common.util.HealthCheckUtil;
import org.openecomp.sdc.fe.config.Configuration;
import org.openecomp.sdc.fe.config.ConfigurationManager;
import org.openecomp.sdc.fe.config.FeEcompErrorManager;
import org.slf4j.Logger;

import javax.servlet.ServletContext;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static java.util.Arrays.asList;
import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;
import static org.apache.http.HttpStatus.SC_INTERNAL_SERVER_ERROR;
import static org.apache.http.HttpStatus.SC_OK;
import static org.openecomp.sdc.common.api.Constants.*;
import static org.openecomp.sdc.common.api.HealthCheckInfo.HealthCheckStatus.*;
import static org.openecomp.sdc.common.http.client.api.HttpRequest.get;
import static org.openecomp.sdc.common.impl.ExternalConfiguration.getAppVersion;
import static org.slf4j.LoggerFactory.getLogger;

public class HealthCheckService {

    private static final String URL = "%s://%s:%s/sdc2/rest/healthCheck";
    private static Logger healthLogger = getLogger("asdc.fe.healthcheck");
    private static Logger log = getLogger(HealthCheckService.class.getName());
    private final List<String> healthCheckFeComponents = asList(HC_COMPONENT_ON_BOARDING, HC_COMPONENT_DCAE);
    private static final HealthCheckUtil healthCheckUtil = new HealthCheckUtil();
    private static final String DEBUG_CONTEXT = "HEALTH_FE";
    /**
     * This executor will execute the health check task.
     */
    ScheduledExecutorService healthCheckExecutor = newSingleThreadScheduledExecutor((Runnable r) -> new Thread(r, "FE-Health-Check-Thread"));

    public void setTask(HealthCheckScheduledTask task) {
        this.task = task;
    }

    private HealthCheckScheduledTask task ;
    private HealthStatus lastHealthStatus = new HealthStatus(500, "{}");
    private ServletContext context;

    public HealthCheckService(ServletContext context) {
        this.context = context;
        this.task = new HealthCheckScheduledTask();
    }

    public void start(int interval) {
        this.healthCheckExecutor.scheduleAtFixedRate( getTask() , 0, interval, TimeUnit.SECONDS);
    }

    /**
     * To be used by the HealthCheckServlet
     *
     * @return
     */
    public Response getFeHealth() {
        return this.buildResponse(lastHealthStatus.statusCode, lastHealthStatus.body);
    }

    private Response buildResponse(int status, String jsonResponse) {
        healthLogger.trace("FE and BE health check status: {}", jsonResponse);
        return Response.status(status).entity(jsonResponse).build();
    }

    public HealthStatus getLastHealthStatus() {
        return lastHealthStatus;
    }
    public HealthCheckScheduledTask getTask() {
        return task;
    }

    //immutable
    protected static class HealthStatus {

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

    protected class HealthCheckScheduledTask implements Runnable {
        @Override
        public void run() {
            healthLogger.trace("Executing FE Health Check Task - Start");
            HealthStatus currentHealth = checkHealth();
            int currentHealthStatus = currentHealth.statusCode;
            healthLogger.trace("Executing FE Health Check Task - Status = {}", currentHealthStatus);

            // In case health status was changed, issue alarm/recovery
            if (currentHealthStatus != lastHealthStatus.statusCode) {
                log.trace("FE Health State Changed to {}. Issuing alarm / recovery alarm...", currentHealthStatus);
                logFeAlarm(currentHealthStatus);
            }

            // Anyway, update latest response
            lastHealthStatus = currentHealth;
        }

        private List<HealthCheckInfo> addHostedComponentsFeHealthCheck(String baseComponent) {
            Configuration config = getConfig();

            String healthCheckUrl = null;
            switch (baseComponent) {
                case HC_COMPONENT_ON_BOARDING:
                    healthCheckUrl = buildOnboardingHealthCheckUrl(config);
                    break;
                case HC_COMPONENT_DCAE:
                    healthCheckUrl = buildDcaeHealthCheckUrl(config);
                    break;
                default:
                    log.debug("Unsupported base component {}", baseComponent);
            }

            StringBuilder description = new StringBuilder("");
            int connectTimeoutMs = 3000;
            int readTimeoutMs = config.getHealthCheckSocketTimeoutInMs(5000);

            if (healthCheckUrl != null) {
                ObjectMapper mapper = new ObjectMapper();
                try {
                    HttpResponse<String> response = get(healthCheckUrl, new HttpClientConfig(new Timeouts(connectTimeoutMs, readTimeoutMs)));
                    int beStatus = response.getStatusCode();
                    if (beStatus == SC_OK || beStatus == SC_INTERNAL_SERVER_ERROR) {
                        String beJsonResponse = response.getResponse();
                        return convertResponse(beJsonResponse, mapper, baseComponent, description, beStatus);
                    } else {
                        description.append("Response code: " + beStatus);
                        log.trace("{} Health Check Response code: {}", baseComponent, beStatus);
                    }
                } catch (Exception e) {
                    log.error("{} Unexpected response ", baseComponent, e);
                    description.append(baseComponent + " Unexpected response: " + e.getMessage());
                }
            } else {
                description.append(baseComponent + " health check Configuration is missing");
            }

            return asList(new HealthCheckInfo(HC_COMPONENT_FE, DOWN, null, description.toString()));
        }

        private void logFeAlarm(int lastFeStatus) {
            switch (lastFeStatus) {
                case 200:
                    FeEcompErrorManager.getInstance().processEcompError(DEBUG_CONTEXT, EcompErrorEnum.FeHealthCheckRecovery, "FE Health Recovered");
                    FeEcompErrorManager.getInstance().logFeHealthCheckRecovery("FE Health Recovered");
                    break;
                case 500:
                    FeEcompErrorManager.getInstance().processEcompError(DEBUG_CONTEXT, EcompErrorEnum.FeHealthCheckError, "Connection with ASDC-BE is probably down");
                    FeEcompErrorManager.getInstance().logFeHealthCheckError("Connection with ASDC-BE is probably down");
                    break;
                default:
                    break;
            }
        }

        protected HealthStatus checkHealth() {
            HttpResponse<String> response;
            try {
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                Configuration config = getConfig();
                String redirectedUrl = String.format(URL, config.getBeProtocol(), config.getBeHost(),
                        HTTPS.equals(config.getBeProtocol()) ? config.getBeSslPort() : config.getBeHttpPort());

                int connectTimeoutMs = 3000;
                int readTimeoutMs = config.getHealthCheckSocketTimeoutInMs(5000);

                HealthCheckWrapper feAggHealthCheck;
                try {
                    response = get(redirectedUrl, new HttpClientConfig(new Timeouts(connectTimeoutMs, readTimeoutMs)));
                    log.debug("HC call to BE - status code is {}", response.getStatusCode());
                    String beJsonResponse = response.getResponse();
                    feAggHealthCheck = getFeHealthCheckInfos(gson, beJsonResponse);
                } catch (Exception e) {
                    log.debug("Health Check error when trying to connect to BE or external FE. Error: {}", e.getMessage());
                    log.error("Health Check error when trying to connect to BE or external FE.", e);
                    String beDowneResponse = gson.toJson(getBeDownCheckInfos());
                    return new HealthStatus(SC_INTERNAL_SERVER_ERROR, beDowneResponse);
                }

                //Getting aggregate FE status
                boolean aggregateFeStatus = (response != null && response.getStatusCode() == SC_INTERNAL_SERVER_ERROR) ? false : healthCheckUtil.getAggregateStatus(feAggHealthCheck.getComponentsInfo(), config.getHealthStatusExclude());
                return new HealthStatus(aggregateFeStatus ? SC_OK : SC_INTERNAL_SERVER_ERROR, gson.toJson(feAggHealthCheck));
            } catch (Exception e) {
                FeEcompErrorManager.getInstance().processEcompError(DEBUG_CONTEXT,EcompErrorEnum.FeHealthCheckGeneralError, "Unexpected FE Health check error");
                FeEcompErrorManager.getInstance().logFeHealthCheckGeneralError("Unexpected FE Health check error");
                log.error("Unexpected FE health check error {}", e.getMessage());
                return new HealthStatus(SC_INTERNAL_SERVER_ERROR, e.getMessage());
            }
        }

        protected Configuration getConfig(){
            return ((ConfigurationManager) context.getAttribute(CONFIGURATION_MANAGER_ATTR))
                    .getConfiguration();
        }

        protected HealthCheckWrapper getFeHealthCheckInfos(Gson gson, String responseString) {
            Configuration config = getConfig();
            Type wrapperType = new TypeToken<HealthCheckWrapper>() {
            }.getType();
            HealthCheckWrapper healthCheckWrapper = gson.fromJson(responseString, wrapperType);
            String appVersion = getAppVersion();
            String description = "OK";
            healthCheckWrapper.getComponentsInfo()
                    .add(new HealthCheckInfo(HC_COMPONENT_FE, UP, appVersion, description));

            //add hosted components fe component
            for (String component : healthCheckFeComponents) {
                List<HealthCheckInfo> feComponentsInfo = addHostedComponentsFeHealthCheck(component);
                HealthCheckInfo baseComponentHCInfo = healthCheckWrapper.getComponentsInfo().stream().filter(c -> c.getHealthCheckComponent().equals(component)).findFirst().orElse(null);
                if (baseComponentHCInfo != null) {
                    if (baseComponentHCInfo.getComponentsInfo() == null) {
                        baseComponentHCInfo.setComponentsInfo(new ArrayList<>());
                    }
                    baseComponentHCInfo.getComponentsInfo().addAll(feComponentsInfo);
                    boolean status = healthCheckUtil.getAggregateStatus(baseComponentHCInfo.getComponentsInfo() ,config.getHealthStatusExclude());
                    baseComponentHCInfo.setHealthCheckStatus(status ? UP : DOWN);

                    String componentsDesc = healthCheckUtil.getAggregateDescription(baseComponentHCInfo.getComponentsInfo(), baseComponentHCInfo.getDescription());
                    if (componentsDesc.length() > 0) { //aggregated description contains all the internal components desc
                        baseComponentHCInfo.setDescription(componentsDesc);
                    }
                } else {
                    log.error("{} not exists in HealthCheck info", component);
                }
            }
            return healthCheckWrapper;
        }

        private HealthCheckWrapper getBeDownCheckInfos() {
            List<HealthCheckInfo> healthCheckInfos = new ArrayList<>();
            healthCheckInfos.add(new HealthCheckInfo(HC_COMPONENT_FE, UP,
                    getAppVersion(), "OK"));
            healthCheckInfos.add(new HealthCheckInfo(HC_COMPONENT_BE, DOWN, null, null));
            healthCheckInfos.add(new HealthCheckInfo(HC_COMPONENT_TITAN, UNKNOWN, null, null));
            healthCheckInfos.add(new HealthCheckInfo(HC_COMPONENT_CASSANDRA, UNKNOWN, null, null));
            healthCheckInfos.add(new HealthCheckInfo(HC_COMPONENT_DISTRIBUTION_ENGINE, UNKNOWN, null, null));
            healthCheckInfos.add(new HealthCheckInfo(HC_COMPONENT_ON_BOARDING, UNKNOWN, null, null));
            healthCheckInfos.add(new HealthCheckInfo(HC_COMPONENT_DCAE, UNKNOWN, null, null));
            return new HealthCheckWrapper(healthCheckInfos, "UNKNOWN", "UNKNOWN");
        }

        private String buildOnboardingHealthCheckUrl(Configuration config) {

            Configuration.OnboardingConfig onboardingConfig = config.getOnboarding();

            if (onboardingConfig != null) {
                String protocol = onboardingConfig.getProtocol();
                String host = onboardingConfig.getHost();
                Integer port = onboardingConfig.getPort();
                String uri = onboardingConfig.getHealthCheckUri();

                return protocol + "://" + host + ":" + port + uri;
            }

            log.error("onboarding health check configuration is missing.");
            return null;
        }

        private String buildDcaeHealthCheckUrl(Configuration config) {

            Configuration.DcaeConfig dcaeConfig = config.getDcae();

            if (dcaeConfig != null) {
                String protocol = dcaeConfig.getProtocol();
                String host = dcaeConfig.getHost();
                Integer port = dcaeConfig.getPort();
                String uri = dcaeConfig.getHealthCheckUri();

                return protocol + "://" + host + ":" + port + uri;
            }

            log.error("dcae health check configuration is missing.");
            return null;
        }

        private List<HealthCheckInfo> convertResponse(String beJsonResponse, ObjectMapper mapper, String baseComponent, StringBuilder description, int beStatus) {
            try {
                Map<String, Object> healthCheckMap = mapper.readValue(beJsonResponse, new TypeReference<Map<String, Object>>() {
                });
                if (healthCheckMap.containsKey("componentsInfo")) {
                    return mapper.convertValue(healthCheckMap.get("componentsInfo"), new TypeReference<List<HealthCheckInfo>>() {
                    });
                } else {
                    description.append("Internal components are missing");
                }
            } catch (JsonSyntaxException | IOException e) {
                log.error("{} Unexpected response body ", baseComponent, e);
                description.append(baseComponent + " Unexpected response body. Response code: " + beStatus);
            }
            return new ArrayList<>();
        }
    }

}
