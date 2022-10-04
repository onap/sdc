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
package org.openecomp.sdc.be.components.health;

import static java.lang.String.format;
import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;
import static org.apache.http.HttpStatus.SC_INTERNAL_SERVER_ERROR;
import static org.apache.http.HttpStatus.SC_OK;
import static org.openecomp.sdc.common.api.Constants.HC_COMPONENT_BE;
import static org.openecomp.sdc.common.api.Constants.HC_COMPONENT_CASSANDRA;
import static org.openecomp.sdc.common.api.Constants.HC_COMPONENT_DMAAP_PRODUCER;
import static org.openecomp.sdc.common.api.Constants.HC_COMPONENT_ECOMP_PORTAL;
import static org.openecomp.sdc.common.api.Constants.HC_COMPONENT_JANUSGRAPH;
import static org.openecomp.sdc.common.api.Constants.HC_COMPONENT_ON_BOARDING;
import static org.openecomp.sdc.common.api.HealthCheckInfo.HealthCheckStatus.DOWN;
import static org.openecomp.sdc.common.api.HealthCheckInfo.HealthCheckStatus.UP;
import static org.openecomp.sdc.common.impl.ExternalConfiguration.getAppVersion;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.openecomp.sdc.be.catalog.impl.DmaapProducerHealth;
import org.openecomp.sdc.be.components.distribution.engine.DistributionEngineClusterHealth;
import org.openecomp.sdc.be.components.distribution.engine.DmaapHealth;
import org.openecomp.sdc.be.components.impl.CADIHealthCheck;
import org.openecomp.sdc.be.components.impl.CassandraHealthCheck;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.config.Configuration;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphGenericDao;
import org.openecomp.sdc.be.switchover.detector.SwitchoverDetector;
import org.openecomp.sdc.common.api.HealthCheckInfo;
import org.openecomp.sdc.common.api.HealthCheckInfo.HealthCheckStatus;
import org.openecomp.sdc.common.http.client.api.HttpRequest;
import org.openecomp.sdc.common.http.client.api.HttpResponse;
import org.openecomp.sdc.common.http.config.HttpClientConfig;
import org.openecomp.sdc.common.http.config.Timeouts;
import org.openecomp.sdc.common.log.elements.LogFieldsMdcHandler;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.common.util.HealthCheckUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("healthCheckBusinessLogic")
public class HealthCheckBusinessLogic {

    private static String hcUrl = "%s://%s:%s%s";
    private static final String BE_HEALTH_CHECK_STR = "beHealthCheck";
    private static final String LOG_PARTNER_NAME = "SDC.BE";
    private static final String COMPONENT_CHANGED_MESSAGE = "BE Component %s state changed from %s to %s";
    private static final Logger log = Logger.getLogger(HealthCheckBusinessLogic.class.getName());
    private static final HealthCheckUtil healthCheckUtil = new HealthCheckUtil();
    private final ScheduledExecutorService healthCheckScheduler = newSingleThreadScheduledExecutor(
        (Runnable r) -> new Thread(r, "BE-Health-Check-Task"));
    private HealthCheckScheduledTask healthCheckScheduledTask = null;
    private static LogFieldsMdcHandler mdcFieldsHandler = new LogFieldsMdcHandler();
    @Resource
    private JanusGraphGenericDao janusGraphGenericDao;
    @Resource
    private DistributionEngineClusterHealth distributionEngineClusterHealth;
    @Resource
    private DmaapHealth dmaapHealth;
    @Resource
    private DmaapProducerHealth dmaapProducerHealth;
    @Resource
    private CassandraHealthCheck cassandraHealthCheck;
    @Resource
    private PortalHealthCheckBuilder portalHealthCheck;
    @Autowired
    private SwitchoverDetector switchoverDetector;
    private volatile List<HealthCheckInfo> prevBeHealthCheckInfos = null;
    private ScheduledFuture<?> scheduledFuture = null;

    @PostConstruct
    public void init() {
        prevBeHealthCheckInfos = getBeHealthCheckInfos();
        log.debug("After initializing prevBeHealthCheckInfos: {}", prevBeHealthCheckInfos);
        healthCheckScheduledTask = new HealthCheckScheduledTask();
        if (this.scheduledFuture == null) {
            this.scheduledFuture = this.healthCheckScheduler.scheduleAtFixedRate(healthCheckScheduledTask, 0, 3, TimeUnit.SECONDS);
        }
    }

    public boolean isDistributionEngineUp() {
        HealthCheckInfo healthCheckInfo = distributionEngineClusterHealth.getHealthCheckInfo();
        return healthCheckInfo.getHealthCheckStatus() != DOWN;
    }

    public Pair<Boolean, List<HealthCheckInfo>> getBeHealthCheckInfosStatus() {
        Configuration config = ConfigurationManager.getConfigurationManager().getConfiguration();
        return new ImmutablePair<>(healthCheckUtil.getAggregateStatus(prevBeHealthCheckInfos, config.getHealthStatusExclude()),
            prevBeHealthCheckInfos);
    }

    private List<HealthCheckInfo> getBeHealthCheckInfos() {
        log.trace("In getBeHealthCheckInfos");
        List<HealthCheckInfo> healthCheckInfos = new ArrayList<>();
        //Dmaap
        HealthCheckInfo info;
        if ((info = getDmaapHealthCheck()) != null) {
            healthCheckInfos.add(info);
        }
        //DmaapProducer
        healthCheckInfos.add(getDmaapProducerHealthCheck());
        // BE
        healthCheckInfos.add(new HealthCheckInfo(HC_COMPONENT_BE, UP, getAppVersion(), "OK"));
        // JanusGraph
        healthCheckInfos.add(getJanusGraphHealthCheck());
        // Distribution Engine
        healthCheckInfos.add(distributionEngineClusterHealth.getHealthCheckInfo());
        //Cassandra
        healthCheckInfos.add(getCassandraHealthCheck());
        // Amdocs
        healthCheckInfos.add(getHostedComponentsBeHealthCheck(HC_COMPONENT_ON_BOARDING, buildOnBoardingHealthCheckUrl()));
        //ECOMP Portal
        healthCheckInfos.add(portalHealthCheck.getHealthCheckInfo());
        //CADI
        healthCheckInfos.add(CADIHealthCheck.getCADIHealthCheckInstance().getCADIStatus());
        return healthCheckInfos;
    }

    private HealthCheckInfo getDmaapHealthCheck() {
        HealthCheckInfo healthCheckInfo = null;
        if (ConfigurationManager.getConfigurationManager().getConfiguration().getDmaapConsumerConfiguration().isActive()) {
            String appVersion = getAppVersion();
            dmaapHealth.getHealthCheckInfo().setVersion(appVersion);
            healthCheckInfo = dmaapHealth.getHealthCheckInfo();
        } else {
            log.debug("Dmaap health check disabled");
        }
        return healthCheckInfo;
    }

    private HealthCheckInfo getDmaapProducerHealthCheck() {
        if (ConfigurationManager.getConfigurationManager().getConfiguration().getDmaapConsumerConfiguration().isActive() && !Boolean.parseBoolean(System.getenv().getOrDefault("USE_KAFKA", "false"))) {
            String appVersion = getAppVersion();
            dmaapProducerHealth.getHealthCheckInfo().setVersion(appVersion);
            return dmaapProducerHealth.getHealthCheckInfo();
        } else {
            String description = "Dmaap health check disabled";
            log.debug(description);
            return new HealthCheckInfo(HC_COMPONENT_DMAAP_PRODUCER, DOWN, null, description);
        }
    }

    public HealthCheckInfo getJanusGraphHealthCheck() {
        // JanusGraph health check and version
        String description;
        boolean isJanusGraphUp;
        HealthCheckInfo healthCheckInfo = new HealthCheckInfo(HC_COMPONENT_JANUSGRAPH, DOWN, null, null);
        try {
            isJanusGraphUp = janusGraphGenericDao.isGraphOpen();
        } catch (Exception e) {
            description = "JanusGraph error: " + e.getMessage();
            healthCheckInfo.setDescription(description);
            log.error(description);
            return healthCheckInfo;
        }
        if (isJanusGraphUp) {
            description = "OK";
            healthCheckInfo.setDescription(description);
            healthCheckInfo.setHealthCheckStatus(HealthCheckInfo.HealthCheckStatus.UP);
        } else {
            description = "JanusGraph graph is down";
            healthCheckInfo.setDescription(description);
        }
        return healthCheckInfo;
    }

    private HealthCheckInfo getCassandraHealthCheck() {
        String description;
        boolean isCassandraUp = false;
        HealthCheckInfo healthCheckInfo = new HealthCheckInfo(HC_COMPONENT_CASSANDRA, DOWN, null, null);
        try {
            isCassandraUp = cassandraHealthCheck.getCassandraStatus();
        } catch (Exception e) {
            description = "Cassandra error: " + e.getMessage();
            log.error(description, e);
        }
        if (isCassandraUp) {
            description = "OK";
            healthCheckInfo.setHealthCheckStatus(HealthCheckStatus.UP);
            healthCheckInfo.setDescription(description);
        } else {
            description = "Cassandra is down";
            healthCheckInfo.setDescription(description);
        }
        return healthCheckInfo;
    }

    private HealthCheckInfo getHostedComponentsBeHealthCheck(String componentName, String healthCheckUrl) {
        HealthCheckStatus healthCheckStatus;
        String description;
        String version = null;
        List<HealthCheckInfo> componentsInfo = new ArrayList<>();
        final int timeout = 3000;
        if (healthCheckUrl != null) {
            try {
                HttpResponse<String> httpResponse = HttpRequest.get(healthCheckUrl, new HttpClientConfig(new Timeouts(timeout, timeout)));
                int statusCode = httpResponse.getStatusCode();
                String aggDescription = "";
                if ((statusCode == SC_OK || statusCode == SC_INTERNAL_SERVER_ERROR) && !componentName.equals(HC_COMPONENT_ECOMP_PORTAL)) {
                    String response = httpResponse.getResponse();
                    log.trace("{} Health Check response: {}", componentName, response);
                    ObjectMapper mapper = new ObjectMapper();
                    Map<String, Object> healthCheckMap = mapper.readValue(response, new TypeReference<Map<String, Object>>() {
                    });
                    version = getVersion(healthCheckMap);
                    if (healthCheckMap.containsKey("componentsInfo")) {
                        componentsInfo = mapper.convertValue(healthCheckMap.get("componentsInfo"), new TypeReference<List<HealthCheckInfo>>() {
                        });
                    }
                    aggDescription = getAggDescription(componentsInfo, aggDescription);
                } else {
                    log.trace("{} Health Check Response code: {}", componentName, statusCode);
                }
                if (statusCode != SC_OK) {
                    healthCheckStatus = DOWN;
                    description = getDescription(componentName, aggDescription);
                    setDescriptionToObject(description, componentsInfo);
                } else {
                    healthCheckStatus = UP;
                    description = "OK";
                }
            } catch (Exception e) {
                log.error("{} unexpected response: ", componentName, e);
                healthCheckStatus = DOWN;
                description = componentName + " unexpected response: " + e.getMessage();
                addToHealthCheckInfoObject(description, componentsInfo);
            }
        } else {
            healthCheckStatus = DOWN;
            description = componentName + " health check Configuration is missing";
            componentsInfo.add(new HealthCheckInfo(HC_COMPONENT_BE, DOWN, null, description));
        }
        return new HealthCheckInfo(componentName, healthCheckStatus, version, description, componentsInfo);
    }

    private void addToHealthCheckInfoObject(String description, List<HealthCheckInfo> componentsInfo) {
        if (componentsInfo != null && componentsInfo.isEmpty()) {
            componentsInfo.add(new HealthCheckInfo(HC_COMPONENT_BE, DOWN, null, description));
        }
    }

    private void setDescriptionToObject(String description, List<HealthCheckInfo> componentsInfo) {
        if (componentsInfo.isEmpty()) {
            componentsInfo.add(new HealthCheckInfo(HC_COMPONENT_BE, DOWN, null, description));
        }
    }

    private String getDescription(String componentName, String aggDescription) {
        String description;
        description = aggDescription.length() > 0 ? aggDescription
            : componentName + " is Down, specific reason unknown";//No inner component returned DOWN, but the status of HC is still DOWN.
        return description;
    }

    private String getVersion(Map<String, Object> healthCheckMap) {
        return healthCheckMap.get("sdcVersion") != null ? healthCheckMap.get("sdcVersion").toString() : null;
    }

    private String getAggDescription(List<HealthCheckInfo> componentsInfo, String aggDescription) {
        if (!componentsInfo.isEmpty()) {
            aggDescription = healthCheckUtil.getAggregateDescription(componentsInfo);
        } else {
            componentsInfo.add(new HealthCheckInfo(HC_COMPONENT_BE, DOWN, null, null));
        }
        return aggDescription;
    }

    @PreDestroy
    protected void destroy() {
        if (scheduledFuture != null) {
            scheduledFuture.cancel(true);
            scheduledFuture = null;
        }
        if (healthCheckScheduler != null) {
            healthCheckScheduler.shutdown();
        }
    }

    private void logAlarm(String componentChangedMsg) {
        BeEcompErrorManager.getInstance().logBeHealthCheckRecovery(componentChangedMsg);
    }

    public String getSiteMode() {
        return switchoverDetector.getSiteMode();
    }

    public boolean anyStatusChanged(List<HealthCheckInfo> beHealthCheckInfos, List<HealthCheckInfo> prevBeHealthCheckInfos) {
        boolean result = false;
        if (beHealthCheckInfos != null && prevBeHealthCheckInfos != null) {
            Map<String, HealthCheckStatus> currentValues = beHealthCheckInfos.stream()
                .collect(Collectors.toMap(HealthCheckInfo::getHealthCheckComponent, HealthCheckInfo::getHealthCheckStatus));
            Map<String, HealthCheckStatus> prevValues = prevBeHealthCheckInfos.stream()
                .collect(Collectors.toMap(HealthCheckInfo::getHealthCheckComponent, HealthCheckInfo::getHealthCheckStatus));
            if (currentValues != null && prevValues != null) {
                int currentSize = currentValues.size();
                int prevSize = prevValues.size();
                if (currentSize != prevSize) {
                    result = true; //extra/missing component
                    updateHealthCheckStatusMap(currentValues, prevValues);
                } else {
                    result = isHealthStatusChanged(result, currentValues, prevValues);
                }
            }
        } else if (beHealthCheckInfos == null && prevBeHealthCheckInfos == null) {
            result = false;
        } else {
            writeLogAlarm(prevBeHealthCheckInfos);
            result = true;
        }
        return result;
    }

    private void writeLogAlarm(List<HealthCheckInfo> prevBeHealthCheckInfos) {
        logAlarm(format(COMPONENT_CHANGED_MESSAGE, "", prevBeHealthCheckInfos == null ? "null" : "true",
            prevBeHealthCheckInfos == null ? "true" : "null"));
    }

    private boolean isHealthStatusChanged(boolean result, Map<String, HealthCheckStatus> currentValues, Map<String, HealthCheckStatus> prevValues) {
        for (Entry<String, HealthCheckStatus> entry : currentValues.entrySet()) {
            String key = entry.getKey();
            HealthCheckStatus value = entry.getValue();
            if (!prevValues.containsKey(key)) {
                result = true; //component missing
                logAlarm(format(COMPONENT_CHANGED_MESSAGE, key, prevValues.get(key), currentValues.get(key)));
                break;
            }
            HealthCheckStatus prevHealthCheckStatus = prevValues.get(key);
            if (value != prevHealthCheckStatus) {
                result = true; //component status changed
                logAlarm(format(COMPONENT_CHANGED_MESSAGE, key, prevValues.get(key), currentValues.get(key)));
                break;
            }
        }
        return result;
    }

    private void updateHealthCheckStatusMap(Map<String, HealthCheckStatus> currentValues, Map<String, HealthCheckStatus> prevValues) {
        Map<String, HealthCheckStatus> notPresent;
        if (currentValues.keySet().containsAll(prevValues.keySet())) {
            notPresent = new HashMap<>(currentValues);
            notPresent.keySet().removeAll(prevValues.keySet());
        } else {
            notPresent = new HashMap<>(prevValues);
            notPresent.keySet().removeAll(currentValues.keySet());
        }
        for (String component : notPresent.keySet()) {
            logAlarm(format(COMPONENT_CHANGED_MESSAGE, component, prevValues.get(component), currentValues.get(component)));
        }
    }

    private String buildOnBoardingHealthCheckUrl() {
        Configuration.OnboardingConfig onboardingConfig = ConfigurationManager.getConfigurationManager().getConfiguration().getOnboarding();
        if (onboardingConfig != null) {
            return String.format(hcUrl, onboardingConfig.getProtocol(), onboardingConfig.getHost(), onboardingConfig.getPort(),
                onboardingConfig.getHealthCheckUri());
        }
        log.error("Onboarding health check configuration is missing.");
        return null;
    }

    public class HealthCheckScheduledTask implements Runnable {

        @Override
        public void run() {
            mdcFieldsHandler.addInfoForErrorAndDebugLogging(LOG_PARTNER_NAME);
            Configuration config = ConfigurationManager.getConfigurationManager().getConfiguration();
            log.trace("Executing BE Health Check Task");
            List<HealthCheckInfo> currentBeHealthCheckInfos = getBeHealthCheckInfos();
            boolean healthStatus = healthCheckUtil.getAggregateStatus(currentBeHealthCheckInfos, config.getHealthStatusExclude());
            boolean prevHealthStatus = healthCheckUtil.getAggregateStatus(prevBeHealthCheckInfos, config.getHealthStatusExclude());
            boolean anyStatusChanged = anyStatusChanged(currentBeHealthCheckInfos, prevBeHealthCheckInfos);
            if (prevHealthStatus != healthStatus || anyStatusChanged) {
                log.trace("BE Health State Changed to {}. Issuing alarm / recovery alarm...", healthStatus);
                prevBeHealthCheckInfos = currentBeHealthCheckInfos;
                logAlarm(healthStatus);
            }
        }

        private void logAlarm(boolean prevHealthState) {
            if (prevHealthState) {
                BeEcompErrorManager.getInstance().logBeHealthCheckRecovery(BE_HEALTH_CHECK_STR);
            } else {
                BeEcompErrorManager.getInstance().logBeHealthCheckError(BE_HEALTH_CHECK_STR);
            }
        }
    }
}
