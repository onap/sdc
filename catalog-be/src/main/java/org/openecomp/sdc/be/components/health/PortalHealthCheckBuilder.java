/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2020 AT&T Intellectual Property. All rights reserved.
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

import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.lang.StringUtils;
import org.onap.portalsdk.core.onboarding.exception.CipherUtilException;
import org.onap.portalsdk.core.onboarding.util.PortalApiProperties;
import org.openecomp.sdc.be.config.Configuration;
import org.openecomp.sdc.be.config.Configuration.EcompPortalConfig;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.ecomp.PortalPropertiesEnum;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.api.HealthCheckInfo;
import org.openecomp.sdc.common.api.HealthCheckInfo.HealthCheckStatus;
import org.openecomp.sdc.common.http.client.api.HttpExecuteException;
import org.openecomp.sdc.common.http.client.api.HttpRequest;
import org.openecomp.sdc.common.http.client.api.HttpResponse;
import org.openecomp.sdc.common.http.config.HttpClientConfig;
import org.openecomp.sdc.common.http.config.Timeouts;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import java.security.InvalidParameterException;
import java.util.Base64;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static org.apache.http.HttpStatus.SC_OK;
import static org.onap.portalsdk.core.onboarding.util.CipherUtil.decryptPKC;
import static org.openecomp.sdc.common.api.Constants.HC_COMPONENT_ECOMP_PORTAL;
import static org.openecomp.sdc.common.api.HealthCheckInfo.HealthCheckStatus.DOWN;
import static org.openecomp.sdc.common.api.HealthCheckInfo.HealthCheckStatus.UP;

@Component("portalHealthCheckBusinessLogic")
public class PortalHealthCheckBuilder {

    private static final Logger log = Logger.getLogger(HealthCheckBusinessLogic.class.getName());
    private static final String PORTAL_NOT_AVAILABLE = HC_COMPONENT_ECOMP_PORTAL + " is not available";
    private static final String PROPERTY_NOT_SET = "Property is not found %s";
    private static final String CONFIG_IS_MISSING = HC_COMPONENT_ECOMP_PORTAL + " health check configuration is missing";
    private static final String PORTAL_ERROR = HC_COMPONENT_ECOMP_PORTAL + " responded with %s status code";
    private String decryptedPortalUser;
    private String decryptedPortalPass;
    private EcompPortalConfig configuration = null ;
    private long healthCheckReadTimeout = 20;
    private long reconnectInterval = 5;
    private HealthCheckScheduledTask healthCheckScheduledTask = null ;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private ScheduledFuture<?> scheduledFuture = null;
    private HealthCheckInfo healthCheckInfo = new HealthCheckInfo
            (HC_COMPONENT_ECOMP_PORTAL, HealthCheckStatus.DOWN, null, CONFIG_IS_MISSING, null);

    @VisibleForTesting
    PortalHealthCheckBuilder init(EcompPortalConfig configuration) throws CipherUtilException {
        log.trace("Enter init method of Portal healthcheck");
        decryptedPortalUser = decryptPKC
                (getPortalProperty(PortalPropertiesEnum.USER.value()));
        decryptedPortalPass = decryptPKC
                (getPortalProperty(PortalPropertiesEnum.PASSWORD.value()));
        synchronized (PortalHealthCheckBuilder.class){
            if (configuration != null) {
                Integer pollingInterval = configuration.getPollingInterval();
                if (pollingInterval != null && pollingInterval != 0) {
                    reconnectInterval = pollingInterval;
                }
                Integer healthCheckReadTimeoutConfig = configuration.getTimeoutMs();
                if (healthCheckReadTimeoutConfig != null) {
                    this.healthCheckReadTimeout = healthCheckReadTimeoutConfig;
                }
                this.healthCheckScheduledTask = new HealthCheckScheduledTask(configuration);
                startHealthCheckTask(true);
            }
            else {
                log.error("ECOMP Portal health check configuration is missing.");
            }
        }
        log.trace("Exit init method of Portal healthcheck");
        return this;
    }

    @PostConstruct
    public PortalHealthCheckBuilder init() throws CipherUtilException {
        return init(ConfigurationManager.getConfigurationManager().getConfiguration().getEcompPortal());
    }


    @PreDestroy
    protected void destroy() {
        if (scheduledFuture != null) {
            scheduledFuture.cancel(true);
            scheduledFuture = null;
        }
        if (scheduler != null) {
            scheduler.shutdown();
        }
    }

    /**
     * Start health check task.
     *
     * @param startTask
     */
    private void startHealthCheckTask(boolean startTask) {
        synchronized (PortalHealthCheckBuilder.class){
            if (startTask && this.scheduledFuture == null) {
                this.scheduledFuture = this.scheduler.scheduleAtFixedRate(this.healthCheckScheduledTask , 0, reconnectInterval, TimeUnit.SECONDS);
            }
        }
    }

    @VisibleForTesting
    void runTask() {
        healthCheckScheduledTask.run();
    }

    public HealthCheckInfo getHealthCheckInfo() {
        return healthCheckInfo;
    }

    /**
     * Health Check Task Scheduler - infinite check.
     */
    public class HealthCheckScheduledTask implements Runnable {
        private final EcompPortalConfig config;
        String healthCheckUrl = buildPortalHealthCheckUrl();
        HealthCheckStatus healthCheckStatus = DOWN;
        String componentName = HC_COMPONENT_ECOMP_PORTAL;
        String description;
        final int timeout = 3000;

        HealthCheckScheduledTask(final EcompPortalConfig config){
            this.config = config;
        }
        @Override
        public void run() {
            if (healthCheckUrl != null) {
                try {
                    int statusCode = getStatusCode(healthCheckUrl, timeout);
                    log.trace("{} Health Check response code: {}", componentName, statusCode);
                    if (statusCode != SC_OK) {
                        description = String.format(PORTAL_ERROR, statusCode);
                    } else {
                        healthCheckStatus = UP;
                        description = "OK";
                    }
                } catch (Exception e) {
                    log.error("{} is not available: ", componentName, e.getMessage());
                    description = PORTAL_NOT_AVAILABLE;
                }
            } else {
                description = CONFIG_IS_MISSING;
            }

            healthCheckInfo.setHealthCheckStatus(healthCheckStatus);
            healthCheckInfo.setDescription(description);
        }
    }

    private static String getPortalProperty(String key) {
        String value = PortalApiProperties.getProperty(key);
        if (StringUtils.isEmpty(value)) {
            throw new InvalidParameterException(String.format(PROPERTY_NOT_SET, key));
        }
        return value;
    }

    String buildPortalHealthCheckUrl() {
        final String hcUrl = "%s://%s:%s%s";
        Configuration.EcompPortalConfig configuration = ConfigurationManager.getConfigurationManager().getConfiguration().getEcompPortal();
        if (configuration != null) {
            return String.format(hcUrl, configuration.getProtocol(), configuration.getHost(),
                    configuration.getPort(), configuration.getHealthCheckUri());
        }
        log.error("ECOMP Portal health check configuration is missing.");
        return null;
    }

    private Properties createHeaders(){
        Properties headers = new Properties();
        String encodedBasicAuthCred = Base64.getEncoder()
                .encodeToString((decryptedPortalUser + ":" +
                        decryptedPortalPass)
                        .getBytes());
        headers.put(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON);
        headers.put(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
        headers.put(Constants.X_TRANSACTION_ID_HEADER, UUID.randomUUID().toString());
        headers.put("Authorization", "Basic " + encodedBasicAuthCred);
        headers.put("cache-control", "no-cache");
        headers.put("uebkey", PortalApiProperties.getProperty("ueb_app_key"));
        return headers;
    }

    int getStatusCode(String healthCheckUrl, int timeout) throws HttpExecuteException {
        HttpResponse<String> httpResponse = HttpRequest.get(healthCheckUrl, createHeaders(), new HttpClientConfig(new Timeouts(timeout, timeout)));
        return httpResponse.getStatusCode();
    }

    @VisibleForTesting
    public EcompPortalConfig getConfiguration() {
        return ConfigurationManager.getConfigurationManager().getConfiguration().getEcompPortal();
    }

}
