/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2026 Deutsche Telekom AG. All rights reserved.
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
package org.openecomp.sdc.be.components.distribution.engine;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import org.apache.commons.lang3.StringUtils;
import org.openecomp.sdc.be.components.distribution.engine.report.DistributionCompleteReporter;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.common.log.wrappers.LoggerSdcAudit;
import org.springframework.stereotype.Component;

@Component
public class DistributionStatusNotificationHandler {

    static final String DISTRIBUTION_STATUS_POLLING = "distributionEngineStatusPolling";
    private static final String PARTNER_NAME = "UNKNOWN";
    private static final Logger logger = Logger.getLogger(DistributionStatusNotificationHandler.class.getName());
    private final LoggerSdcAudit audit = new LoggerSdcAudit(DistributionStatusNotificationHandler.class);
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final ComponentsUtils componentUtils;
    private final DistributionCompleteReporter distributionCompleteReporter;

    public DistributionStatusNotificationHandler(ComponentsUtils componentUtils, DistributionCompleteReporter distributionCompleteReporter) {
        this.componentUtils = componentUtils;
        this.distributionCompleteReporter = distributionCompleteReporter;
    }

    /**
     * Never throws: invalid or unprocessable messages are logged, raise an alarm and are dropped.
     */
    public void handle(String topic, String message) {
        logger.trace("received message {}", message);
        try {
            handleMessage(topic, message);
        } catch (RuntimeException e) {
            logger.error("failed to handle message {} from topic {}", message, topic, e);
            BeEcompErrorManager.getInstance().logBeUebSystemError(DISTRIBUTION_STATUS_POLLING,
                "failed to handle message " + message + " from topic " + topic + " error: " + e);
        }
    }

    private void handleMessage(String topic, String message) {
        DistributionStatusNotification notification;
        try {
            notification = gson.fromJson(message, DistributionStatusNotification.class);
        } catch (JsonParseException e) {
            skipInvalidMessage(topic, message, "not a valid distribution status notification: " + e.getMessage());
            return;
        }
        if (notification == null || StringUtils.isBlank(notification.getDistributionID())) {
            skipInvalidMessage(topic, message, "missing distributionID");
            return;
        }
        if (notification.getStatus() == null) {
            skipInvalidMessage(topic, message, "missing or unknown status");
            return;
        }
        audit.startAuditFetchLog(PARTNER_NAME, DistributionStatusNotificationHandler.class.getName());
        handleDistributionNotificationMsg(topic, notification);
    }

    private void skipInvalidMessage(String topic, String message, String reason) {
        logger.warn("skipping invalid message {} from topic {}: {}", message, topic, reason);
        BeEcompErrorManager.getInstance().logBeUebSystemError(DISTRIBUTION_STATUS_POLLING,
            "skipping invalid message " + message + " from topic " + topic + ": " + reason);
    }

    private void handleDistributionNotificationMsg(String topic, DistributionStatusNotification notification) {
        componentUtils.auditDistributionStatusNotification(notification.getDistributionID(), notification.getConsumerID(), topic,
            notification.getArtifactURL(), String.valueOf(notification.getTimestamp()), notification.getStatus().name(),
            notification.getErrorReason(), audit);
        if (notification.isDistributionCompleteNotification()) {
            distributionCompleteReporter.reportDistributionComplete(notification);
        }
    }
}
