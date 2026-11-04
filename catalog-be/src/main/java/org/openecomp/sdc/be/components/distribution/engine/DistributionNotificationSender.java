/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2026 Deutsche Telekom AG. All rights reserved.
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

import org.openecomp.sdc.be.components.kafka.DistributionNotificationPublisher;
import org.openecomp.sdc.be.components.kafka.DistributionNotificationPublisher.PublishResult;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.config.DistributionEngineConfiguration;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.springframework.stereotype.Component;

@Component("distributionNotificationSender")
public class DistributionNotificationSender {

    protected static final String DISTRIBUTION_NOTIFICATION_SENDING = "distributionNotificationSending";
    private static final Logger logger = Logger.getLogger(DistributionNotificationSender.class.getName());
    private final ComponentsUtils componentUtils;
    private final DistributionNotificationPublisher publisher;

    private final DistributionEngineConfiguration deConfiguration = ConfigurationManager.getConfigurationManager().getDistributionEngineConfiguration();

    public DistributionNotificationSender(ComponentsUtils componentUtils, DistributionNotificationPublisher publisher) {
        this.componentUtils = componentUtils;
        this.publisher = publisher;
    }

    public ActionStatus sendNotification(String topicName, String distributionId, EnvironmentMessageBusData messageBusData,
                                         INotificationData notificationData, Service service, User modifier) {
        long startTime = System.currentTimeMillis();
        PublishResult status = publisher.publish(topicName, notificationData,
            deConfiguration.getDistributionNotificationTopic().getMaxWaitingAfterSendingSeconds());
        logger.info("After publishing service {} of version {}. Status is {}", service.getName(), service.getVersion(), status.httpCode());
        auditDistributionNotification(
            new AuditDistributionNotificationBuilder().setTopicName(topicName).setDistributionId(distributionId).setStatus(status).setService(service)
                .setEnvId(messageBusData.getEnvId()).setModifier(modifier).setWorkloadContext(notificationData.getWorkloadContext())
                .setTenant(messageBusData.getTenant()));
        long endTime = System.currentTimeMillis();
        if (logger.isDebugEnabled()) {
            logger.debug("After building and publishing artifacts object. Total took {} milliseconds", endTime - startTime);
        }
        return status == PublishResult.OK ? ActionStatus.OK : ActionStatus.GENERAL_ERROR;
    }

    private void auditDistributionNotification(AuditDistributionNotificationBuilder builder) {
        if (this.componentUtils != null) {
            String httpCodeStr = String.valueOf(builder.getStatus().httpCode());
            String desc = builder.getStatus().description();
            this.componentUtils.auditDistributionNotification(builder.getService().getUUID(), builder.getService().getName(), "Service",
                builder.getService().getVersion(), builder.getModifier(), builder.getEnvId(), builder.getService().getLifecycleState().name(),
                builder.getTopicName(), builder.getDistributionId(), desc, httpCodeStr, builder.getWorkloadContext(), builder.getTenant());
        }
    }
}
