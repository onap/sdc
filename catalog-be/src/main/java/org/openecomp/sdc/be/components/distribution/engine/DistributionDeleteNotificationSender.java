/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright © 2025 Deutsche Telekom. All rights reserved.
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
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DistributionDeleteNotificationSender {
    private static final Logger logger = Logger.getLogger(DistributionDeleteNotificationSender.class.getName());

    private final DistributionNotificationPublisher publisher;

    private final DistributionEngineConfiguration deConfiguration;

    @Autowired
    public DistributionDeleteNotificationSender(DistributionNotificationPublisher publisher) {
        this(publisher, ConfigurationManager.getConfigurationManager().getDistributionEngineConfiguration());
    }

    public DistributionDeleteNotificationSender(DistributionNotificationPublisher publisher,
            DistributionEngineConfiguration deConfiguration) {
        this.publisher = publisher;
        this.deConfiguration = deConfiguration;
    }

    /**
     * This method is used to send the notification for the delete service.
     * 
     * @param topicName        name of the Kafka topic for delete service
     * @param distributionId   random UUID generated for the notification
     * @param notificationData the payload data to be sent in the Kafka notification
     * @return the action status of the notification
     */
    public ActionStatus sendNotificationForDeleteService(String topicName, String distributionId,
            INotificationData notificationData) {
        logger.debug("sending notification with topicName={}, distributionId={}",
                topicName,
                distributionId);

        PublishResult status = publisher.publish(topicName, notificationData,
                deConfiguration.getDistributionDeleteTopic().getMaxWaitingAfterSendingSeconds());

        logger.info("sending notification is completed for topicName={}, with status = {}",
                topicName, status);
        return status == PublishResult.OK ? ActionStatus.OK : ActionStatus.GENERAL_ERROR;
    }
}
