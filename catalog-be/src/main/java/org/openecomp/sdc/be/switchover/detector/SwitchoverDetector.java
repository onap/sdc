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

package org.openecomp.sdc.be.switchover.detector;

import com.google.common.annotations.VisibleForTesting;
import java.net.InetAddress;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.config.Configuration.SwitchoverDetectorConfig;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.common.http.client.api.HttpRequest;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.springframework.stereotype.Component;


@Component("switchover-detector")
public class SwitchoverDetector {

    private static final String SWITCHOVER_DETECTOR_LOG_CONTEXT = "switchover.detector";

    private SwitchoverDetectorConfig switchoverDetectorConfig;

    private Properties authHeader = null;

    private long detectorInterval = 60;

    private int maxBeQueryAttempts = 3;

    private int maxFeQueryAttempts = 3;

    private Boolean beMatch = null;

    private Boolean feMatch = null;

    private static final Logger logger = Logger.getLogger(SwitchoverDetector.class);

    private volatile String siteMode = SwitchoverDetectorState.UNKNOWN.getState();

    private ScheduledFuture<?> scheduledFuture = null;

    ScheduledExecutorService switchoverDetectorScheduler =
            Executors.newSingleThreadScheduledExecutor(runnable -> new Thread(runnable, "Switchover-Detector-Task"));

    SwitchoverDetectorScheduledTask switchoverDetectorScheduledTask = null;

    public enum SwitchoverDetectorState {

        UNKNOWN("unknown"), ACTIVE("active"), STANDBY("standby");

        private String state;

        SwitchoverDetectorState(String state) {
            this.state = state;
        }

        public String getState() {
            return state;
        }
    }

    public enum SwitchoverDetectorGroup {

        BE_SET("beSet"), FE_SET("feSet");

        private String group;

        SwitchoverDetectorGroup(String group) {
            this.group = group;
        }

        public String getGroup() {
            return group;
        }
    }

    public String getSiteMode() {
        return siteMode;
    }

    public void setSiteMode(String mode) {
        this.siteMode = mode;
    }

    public class SwitchoverDetectorScheduledTask implements Runnable {

        private Boolean queryGss(String fqdn, String vip, int maxAttempts) {

            Boolean result = null;
            int attempts = 0;

            while (result == null && (++attempts < maxAttempts)) {
                try {
                    InetAddress inetAddress = InetAddress.getByName(fqdn);
                    result = inetAddress.getHostAddress().equals(vip);

                } catch (Exception e) {
                    String message = e.getMessage();
                    if (message == null) {
                        message = e.getClass().getName();
                    }
                    logger.debug("Error occurred during switchover detector query, Result is {}", message, e);
                }
            }
            if (null == result) {
                BeEcompErrorManager.getInstance().logFqdnResolveError(SWITCHOVER_DETECTOR_LOG_CONTEXT,
                        "host " + fqdn + " not resolved after " + attempts + " attempts");
            }
            return result;
        }

        private Boolean queryBe() {
            return queryGss(switchoverDetectorConfig.getgBeFqdn(), switchoverDetectorConfig.getBeVip(),
                    maxBeQueryAttempts);
        }

        private Boolean queryFe() {
            return queryGss(switchoverDetectorConfig.getgFeFqdn(), switchoverDetectorConfig.getFeVip(),
                    maxFeQueryAttempts);
        }

        private void initializeSiteMode() {
            while (siteMode.equals(SwitchoverDetectorState.UNKNOWN.getState())) {

                beMatch = queryBe();
                feMatch = queryFe();

                if (beMatch != null && beMatch.equals(feMatch)) {
                    if (beMatch) {
                        setSiteMode(SwitchoverDetectorState.ACTIVE.getState());
                    } else {
                        setSiteMode(SwitchoverDetectorState.STANDBY.getState());
                    }
                }
            }
        }

        @Override
        public void run() {
            logger.trace("Executing Switchover Detector Task - Start");

            initializeSiteMode();

            Boolean beRes = queryBe();
            Boolean feRes = queryFe();

            if (null == beRes || null == feRes) {
                return;
            }

            Boolean updateRequired = siteMode.equals(SwitchoverDetectorState.STANDBY.getState()) && (beRes || feRes)
                    && (!beMatch.equals(beRes) || !feMatch.equals(feRes));
            Boolean prevModeStandby = siteMode.equals(SwitchoverDetectorState.STANDBY.getState());

            updateSiteModeAndPriority(beRes && feRes, prevModeStandby, updateRequired);

            beMatch = beRes;
            feMatch = feRes;
        }

        ExecutorService switchoverDetectorExecutor =
                Executors.newSingleThreadExecutor(runnable -> new Thread(runnable, "Switchover-Detector-Thread"));

        private void updateSiteModeAndPriority(Boolean bothMatch, Boolean previousModeStandby, Boolean updateRequired) {
            if (bothMatch && previousModeStandby) {
                logger.trace("Site switch over was done. Site is now in active mode");
                setSiteMode(SwitchoverDetectorState.ACTIVE.getState());
                BeEcompErrorManager.getInstance().logSiteSwitchoverInfo(SWITCHOVER_DETECTOR_LOG_CONTEXT, siteMode);
            } else if (!bothMatch && !previousModeStandby) {
                logger.trace("Site switch over was done. Site is now in stand-by mode");
                setSiteMode(SwitchoverDetectorState.STANDBY.getState());
                BeEcompErrorManager.getInstance().logSiteSwitchoverInfo(SWITCHOVER_DETECTOR_LOG_CONTEXT, siteMode);
            }
            if (updateRequired) {
                changeSitePriority(SwitchoverDetectorGroup.BE_SET.getGroup());
                changeSitePriority(SwitchoverDetectorGroup.FE_SET.getGroup());
                publishNetwork();
            }
        }

        private void changeSitePriority(String groupToSet) {

            String url = switchoverDetectorConfig.getGroups().get(groupToSet).getChangePriorityUrl();
            String body = switchoverDetectorConfig.getGroups().get(groupToSet).getChangePriorityBody();

            try {
                HttpRequest.put(url, authHeader, new StringEntity(body, ContentType.APPLICATION_JSON));
            } catch (Exception e) {
                String message = e.getMessage();
                if (message == null) {
                    message = e.getClass().getName();
                }
                logger.debug("Error occurred during change site priority request, Result is {}", message, e);
            }

        }

        private void publishNetwork() {

            String url = switchoverDetectorConfig.getPublishNetworkUrl();
            String body = switchoverDetectorConfig.getPublishNetworkBody();
            try {
                HttpRequest.post(url, authHeader, new StringEntity(body, ContentType.APPLICATION_JSON));
            } catch (Exception e) {
                String message = e.getMessage();
                if (message == null) {
                    message = e.getClass().getName();
                }
                logger.debug("Error occurred during publish network request, Result is {}", message, e);
            }
        }

    }

    @VisibleForTesting
    void setSwitchoverDetectorConfig(SwitchoverDetectorConfig switchoverDetectorConfig) {
        this.switchoverDetectorConfig = switchoverDetectorConfig;
    }

    @PostConstruct
    private void init() {
        logger.info("Enter init method of SwitchoverDetector");

        switchoverDetectorConfig =
                ConfigurationManager.getConfigurationManager().getConfiguration().getSwitchoverDetector();

        if (!switchoverDetectorConfig.getEnabled()) {
            logger.info("switchover detector service is disabled");
            return;
        }

        Long detectorIntervalConfig = switchoverDetectorConfig.getInterval();
        if (detectorIntervalConfig != null) {
            detectorInterval = detectorIntervalConfig;
        }

        Integer maxAttempts = switchoverDetectorConfig.getBeResolveAttempts();
        if (maxAttempts != null) {
            maxBeQueryAttempts = maxAttempts;
        }
        maxAttempts = switchoverDetectorConfig.getFeResolveAttempts();
        if (maxAttempts != null) {
            maxFeQueryAttempts = maxAttempts;
        }

        logger.info("switchover detector service is enabled, interval is {} seconds", detectorInterval);

        this.switchoverDetectorScheduledTask = new SwitchoverDetectorScheduledTask();
        startSwitchoverDetectorTask();
        logger.trace("Exit init method of SwitchoverDetector");

    }

    @PreDestroy
    private void destroy() {

        if (scheduledFuture != null) {
            scheduledFuture.cancel(true);
            scheduledFuture = null;
        }

        if (switchoverDetectorScheduler != null) {
            switchoverDetectorScheduler.shutdown();
        }

    }

    /**
     * This method starts the switchover detector threads in the background.
     */
    public void startSwitchoverDetectorTask() {
        if (this.scheduledFuture == null) {
            this.scheduledFuture = this.switchoverDetectorScheduler
                    .scheduleAtFixedRate(switchoverDetectorScheduledTask, 0, detectorInterval, TimeUnit.SECONDS);
        }
    }

}
