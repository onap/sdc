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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.openecomp.sdc.be.components.distribution.engine.DistributionEngineClusterHealth;
import org.openecomp.sdc.be.components.distribution.engine.DmaapHealth;
import org.openecomp.sdc.be.components.impl.CassandraHealthCheck;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.config.Configuration;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.impl.EsHealthCheckDao;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphGenericDao;
import org.openecomp.sdc.be.switchover.detector.SwitchoverDetector;
import org.openecomp.sdc.common.api.HealthCheckInfo;
import org.openecomp.sdc.common.api.HealthCheckInfo.HealthCheckStatus;
import org.openecomp.sdc.common.http.client.api.HttpRequest;
import org.openecomp.sdc.common.http.client.api.HttpResponse;
import org.openecomp.sdc.common.http.config.HttpClientConfig;
import org.openecomp.sdc.common.http.config.Timeouts;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.common.util.HealthCheckUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;
import static org.apache.http.HttpStatus.SC_INTERNAL_SERVER_ERROR;
import static org.apache.http.HttpStatus.SC_OK;
import static org.openecomp.sdc.common.api.Constants.*;
import static org.openecomp.sdc.common.api.HealthCheckInfo.HealthCheckStatus.DOWN;
import static org.openecomp.sdc.common.api.HealthCheckInfo.HealthCheckStatus.UP;
import static org.openecomp.sdc.common.impl.ExternalConfiguration.getAppVersion;


@Component("healthCheckBusinessLogic")
public class HealthCheckBusinessLogic {

    protected static final String BE_HEALTH_LOG_CONTEXT = "be.healthcheck";
    private static final String BE_HEALTH_CHECK_STR = "beHealthCheck";
    private static final String COMPONENT_CHANGED_MESSAGE = "BE Component %s state changed from %s to %s";
    private static final Logger log = Logger.getLogger(HealthCheckBusinessLogic.class.getName());
    private static final HealthCheckUtil healthCheckUtil = new HealthCheckUtil();
    private final ScheduledExecutorService healthCheckScheduler = newSingleThreadScheduledExecutor((Runnable r) -> new Thread(r, "BE-Health-Check-Task"));
    private HealthCheckScheduledTask healthCheckScheduledTask = null;
    @Resource
    private JanusGraphGenericDao janusGraphGenericDao;
    @Resource
    private EsHealthCheckDao esHealthCheckDao;
    @Resource
    private DistributionEngineClusterHealth distributionEngineClusterHealth;
    @Resource
    private DmaapHealth dmaapHealth;
    @Resource
    private CassandraHealthCheck cassandraHealthCheck;
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
        return !healthCheckInfo.getHealthCheckStatus().equals(DOWN);
    }

    public Pair<Boolean, List<HealthCheckInfo>> getBeHealthCheckInfosStatus() {
        Configuration config = ConfigurationManager.getConfigurationManager().getConfiguration();
        return new ImmutablePair<>(healthCheckUtil.getAggregateStatus(prevBeHealthCheckInfos, config.getHealthStatusExclude()), prevBeHealthCheckInfos);
    }

    private List<HealthCheckInfo> getBeHealthCheckInfos() {

        log.trace("In getBeHealthCheckInfos");

        List<HealthCheckInfo> healthCheckInfos = new ArrayList<>();

        //Dmaap
        getDmaapHealthCheck(healthCheckInfos);

        // BE
        getBeHealthCheck(healthCheckInfos);

        // JanusGraph
        getJanusGraphHealthCheck(healthCheckInfos);
        // ES
        getEsHealthCheck(healthCheckInfos);

        // Distribution Engine
        getDistributionEngineCheck(healthCheckInfos);

        //Cassandra
        getCassandraHealthCheck(healthCheckInfos);

        // Amdocs
        getAmdocsHealthCheck(healthCheckInfos);

        //DCAE
        getDcaeHealthCheck(healthCheckInfos);

        return healthCheckInfos;
    }

    private List<HealthCheckInfo> getEsHealthCheck(List<HealthCheckInfo> healthCheckInfos) {

        // ES health check and version
        String appVersion = getAppVersion();
        HealthCheckStatus healthCheckStatus;
        String description;

        try {
            healthCheckStatus = esHealthCheckDao.getClusterHealthStatus();
        } catch (Exception e) {
            healthCheckStatus = DOWN;
            description = "ES cluster error: " + e.getMessage();
            healthCheckInfos.add(new HealthCheckInfo(HC_COMPONENT_ES, healthCheckStatus, appVersion, description));
            log.error(description, e);
            return healthCheckInfos;
        }
        if (healthCheckStatus.equals(DOWN)) {
            description = "ES cluster is down";
        } else {
            description = "OK";
        }
        healthCheckInfos.add(new HealthCheckInfo(HC_COMPONENT_ES, healthCheckStatus, appVersion, description));
        return healthCheckInfos;
    }

    private List<HealthCheckInfo> getBeHealthCheck(List<HealthCheckInfo> healthCheckInfos) {
        String appVersion = getAppVersion();
        String description = "OK";
        healthCheckInfos.add(new HealthCheckInfo(HC_COMPONENT_BE, UP, appVersion, description));
        return healthCheckInfos;
    }

    private List<HealthCheckInfo> getDmaapHealthCheck(List<HealthCheckInfo> healthCheckInfos) {
        if(ConfigurationManager.getConfigurationManager().getConfiguration().getDmaapConsumerConfiguration().isActive()){
            String appVersion = getAppVersion();
            dmaapHealth.getHealthCheckInfo().setVersion(appVersion);
            healthCheckInfos.add(dmaapHealth.getHealthCheckInfo());
        } else {
          log.debug("Dmaap health check disabled");
        }

        return healthCheckInfos;
    }


    public List<HealthCheckInfo> getJanusGraphHealthCheck(List<HealthCheckInfo> healthCheckInfos) {
        // JanusGraph health check and version
        String description;
        boolean isJanusGraphUp;

        try {
            isJanusGraphUp = janusGraphGenericDao.isGraphOpen();
        } catch (Exception e) {
            description = "JanusGraph error: ";
            healthCheckInfos.add(new HealthCheckInfo(HC_COMPONENT_TITAN, DOWN, null, description));
            log.error(description, e);
            return healthCheckInfos;
        }
        if (isJanusGraphUp) {
            description = "OK";
            healthCheckInfos.add(new HealthCheckInfo(HC_COMPONENT_TITAN, UP, null, description));
        } else {
            description = "JanusGraph graph is down";
            healthCheckInfos.add(new HealthCheckInfo(HC_COMPONENT_TITAN, DOWN, null, description));
        }
        return healthCheckInfos;
    }

    private List<HealthCheckInfo> getCassandraHealthCheck(List<HealthCheckInfo> healthCheckInfos) {

        String description;
        boolean isCassandraUp = false;

        try {
            isCassandraUp = cassandraHealthCheck.getCassandraStatus();
        } catch (Exception e) {
            description = "Cassandra error: " + e.getMessage();
            log.error(description, e);
        }
        if (isCassandraUp) {
            description = "OK";
            healthCheckInfos.add(new HealthCheckInfo(HC_COMPONENT_CASSANDRA, UP, null, description));
        } else {
            description = "Cassandra is down";
            healthCheckInfos.add(new HealthCheckInfo(HC_COMPONENT_CASSANDRA, DOWN, null, description));
        }
        return healthCheckInfos;

    }

    private void getDistributionEngineCheck(List<HealthCheckInfo> healthCheckInfos) {

        HealthCheckInfo healthCheckInfo = distributionEngineClusterHealth.getHealthCheckInfo();

        healthCheckInfos.add(healthCheckInfo);

    }

    private List<HealthCheckInfo> getAmdocsHealthCheck(List<HealthCheckInfo> healthCheckInfos) {
        HealthCheckInfo beHealthCheckInfo = getHostedComponentsBeHealthCheck(HC_COMPONENT_ON_BOARDING, buildOnBoardingHealthCheckUrl());
        healthCheckInfos.add(beHealthCheckInfo);
        return healthCheckInfos;
    }

    private List<HealthCheckInfo> getDcaeHealthCheck(List<HealthCheckInfo> healthCheckInfos) {
        HealthCheckInfo beHealthCheckInfo = getHostedComponentsBeHealthCheck(HC_COMPONENT_DCAE, buildDcaeHealthCheckUrl());
        healthCheckInfos.add(beHealthCheckInfo);
        return healthCheckInfos;
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

                if (statusCode == SC_OK || statusCode == SC_INTERNAL_SERVER_ERROR) {
                    String response = httpResponse.getResponse();
                    log.trace("{} Health Check response: {}", componentName, response);
                    ObjectMapper mapper = new ObjectMapper();
                    Map<String, Object> healthCheckMap = mapper.readValue(response, new TypeReference<Map<String, Object>>() {
                    });
                    version = healthCheckMap.get("sdcVersion") != null ? healthCheckMap.get("sdcVersion").toString() : null;
                    if (healthCheckMap.containsKey("componentsInfo")) {
                        componentsInfo = mapper.convertValue(healthCheckMap.get("componentsInfo"), new TypeReference<List<HealthCheckInfo>>() {
                        });
                    }

                    if (!componentsInfo.isEmpty()) {
                        aggDescription = healthCheckUtil.getAggregateDescription(componentsInfo, null);
                    } else {
                        componentsInfo.add(new HealthCheckInfo(HC_COMPONENT_BE, DOWN, null, null));
                    }
                } else {
                    log.trace("{} Health Check Response code: {}", componentName, statusCode);
                }

                if (statusCode != SC_OK) {
                    healthCheckStatus = DOWN;
                    description = aggDescription.length() > 0
                            ? aggDescription
                            : componentName + " is Down, specific reason unknown";//No inner component returned DOWN, but the status of HC is still DOWN.
                    if (componentsInfo.isEmpty()) {
                        componentsInfo.add(new HealthCheckInfo(HC_COMPONENT_BE, DOWN, null, description));
                    }
                } else {
                    healthCheckStatus = UP;
                    description = "OK";
                }

            } catch (Exception e) {
                log.error("{} unexpected response: ", componentName, e);
                healthCheckStatus = DOWN;
                description = componentName + " unexpected response: " + e.getMessage();
                if (componentsInfo != null && componentsInfo.isEmpty()) {
                    componentsInfo.add(new HealthCheckInfo(HC_COMPONENT_BE, DOWN, null, description));
                }
            }
        } else {
            healthCheckStatus = DOWN;
            description = componentName + " health check Configuration is missing";
            componentsInfo.add(new HealthCheckInfo(HC_COMPONENT_BE, DOWN, null, description));
        }

        return new HealthCheckInfo(componentName, healthCheckStatus, version, description, componentsInfo);
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

            Map<String, HealthCheckStatus> currentValues = beHealthCheckInfos.stream().collect(Collectors.toMap(HealthCheckInfo::getHealthCheckComponent, HealthCheckInfo::getHealthCheckStatus));
            Map<String, HealthCheckStatus> prevValues = prevBeHealthCheckInfos.stream().collect(Collectors.toMap(HealthCheckInfo::getHealthCheckComponent, HealthCheckInfo::getHealthCheckStatus));

            if (currentValues != null && prevValues != null) {
                int currentSize = currentValues.size();
                int prevSize = prevValues.size();

                if (currentSize != prevSize) {

                    result = true; //extra/missing component

                    Map<String, HealthCheckStatus> notPresent = null;
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

                } else {

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
                }
            }

        } else if (beHealthCheckInfos == null && prevBeHealthCheckInfos == null) {
            result = false;
        } else {
            logAlarm(format(COMPONENT_CHANGED_MESSAGE, "", prevBeHealthCheckInfos == null ? "null" : "true", prevBeHealthCheckInfos == null ? "true" : "null"));
            result = true;
        }

        return result;
    }

    private String buildOnBoardingHealthCheckUrl() {

        Configuration.OnboardingConfig onboardingConfig = ConfigurationManager.getConfigurationManager().getConfiguration().getOnboarding();

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

    private String buildDcaeHealthCheckUrl() {

        Configuration.DcaeConfig dcaeConfig = ConfigurationManager.getConfigurationManager().getConfiguration().getDcae();

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

    public class HealthCheckScheduledTask implements Runnable {
        @Override
        public void run() {
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
