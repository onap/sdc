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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.openecomp.sdc.be.components.kafka.DistributionNotificationPublisher;
import org.openecomp.sdc.be.components.kafka.DistributionNotificationPublisher.PublishResult;
import org.openecomp.sdc.be.config.DistributionEngineConfiguration;
import org.openecomp.sdc.be.config.DistributionEngineConfiguration.DistributionDeleteTopicConfig;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.common.util.ThreadLocalsHolder;

public class DistributionDeleteNotificationSenderTest {
    private DistributionNotificationPublisher publisher;
    private DistributionEngineConfiguration distributionConfig;
    private DistributionDeleteNotificationSender deleteNotificationSender;
    private static final String TOPIC_NAME = "delete_topic";

    @Before
    public void setUp() {
        publisher = Mockito.mock(DistributionNotificationPublisher.class);
        distributionConfig = Mockito.mock(DistributionEngineConfiguration.class);
        deleteNotificationSender = new DistributionDeleteNotificationSender(publisher, distributionConfig);
    }

    @Test
    public void testSendNotificationForDeleteService() {
        INotificationData notificationData = new NotificationDataImpl();
        stubDeleteTopicTimeout(10);
        when(publisher.publish(TOPIC_NAME, notificationData, 10L)).thenReturn(PublishResult.OK);

        ActionStatus result = deleteNotificationSender.sendNotificationForDeleteService(
                TOPIC_NAME, ThreadLocalsHolder.getUuid(), notificationData);

        assertEquals(ActionStatus.OK, result);
        verify(publisher, times(1)).publish(eq(TOPIC_NAME), eq(notificationData), eq(10L));
    }

    @Test
    public void testSendNotificationForDeleteService_WhenKafkaNotificationFails() {
        INotificationData notificationData = new NotificationDataImpl();
        stubDeleteTopicTimeout(10);
        when(publisher.publish(TOPIC_NAME, notificationData, 10L)).thenReturn(PublishResult.FAILED);

        ActionStatus result = deleteNotificationSender.sendNotificationForDeleteService(
                TOPIC_NAME, ThreadLocalsHolder.getUuid(), notificationData);

        assertEquals(ActionStatus.GENERAL_ERROR, result);
        verify(publisher, times(1)).publish(eq(TOPIC_NAME), eq(notificationData), eq(10L));
    }

    private void stubDeleteTopicTimeout(int seconds) {
        DistributionDeleteTopicConfig deleteTopicConfig = mock(DistributionEngineConfiguration.DistributionDeleteTopicConfig.class);
        when(distributionConfig.getDistributionDeleteTopic()).thenReturn(deleteTopicConfig);
        when(deleteTopicConfig.getMaxWaitingAfterSendingSeconds()).thenReturn(seconds);
    }
}
