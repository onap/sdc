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

import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.openecomp.sdc.be.components.kafka.KafkaHandler;
import org.openecomp.sdc.be.config.DistributionEngineConfiguration;
import org.openecomp.sdc.be.config.DistributionEngineConfiguration.DistributionDeleteTopicConfig;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.distribution.api.client.CambriaOperationStatus;
import org.openecomp.sdc.common.util.ThreadLocalsHolder;

public class DistributionDeleteNotificationSenderTest {
    private KafkaHandler kafkaHandler;
    private DistributionEngineConfiguration distributionConfig;
    private DistributionDeleteNotificationSender deleteNotificationSender;
    private CambriaHandler cambriaHandler;
    private static final String TOPIC_NAME = "delete_topic";

    @Before
    public void setUp() {
        kafkaHandler = Mockito.mock(KafkaHandler.class);
        distributionConfig = Mockito.mock(DistributionEngineConfiguration.class);
        cambriaHandler = Mockito.mock(CambriaHandler.class);
        deleteNotificationSender = new DistributionDeleteNotificationSender(kafkaHandler, distributionConfig,
                cambriaHandler);
    }

    @Test
    public void testSendNotificationForDeleteService_WhenKafkaIsInactive() {
        when(kafkaHandler.isKafkaActive()).thenReturn(false);
        DistributionDeleteTopicConfig deleteTopicConfig = mock(
                DistributionEngineConfiguration.DistributionDeleteTopicConfig.class);
        when(distributionConfig.getDistributionDeleteTopic()).thenReturn(deleteTopicConfig);
        when(deleteTopicConfig.getMaxWaitingAfterSendingSeconds()).thenReturn(10);

        EnvironmentMessageBusData messageBusData = mock(EnvironmentMessageBusData.class);
        List<String> serverList = Collections.singletonList("uebEndpoint");
        when(messageBusData.getUebPublicKey()).thenReturn("uebPublicKey");
        when(messageBusData.getUebPrivateKey()).thenReturn("uebPrivateKey");
        when(messageBusData.getDmaaPuebEndpoints()).thenReturn(serverList);

        INotificationData notificationData = new NotificationDataImpl();

        when(cambriaHandler.sendNotificationAndClose(TOPIC_NAME, "uebPublicKey", "uebPrivateKey", serverList,
                notificationData, 10)).thenReturn(new CambriaErrorResponse(CambriaOperationStatus.OK, 200));

        ActionStatus result = deleteNotificationSender.sendNotificationForDeleteService(
                TOPIC_NAME, ThreadLocalsHolder.getUuid(), messageBusData, notificationData);

        assertEquals(ActionStatus.OK, result);
        verify(cambriaHandler, times(1)).sendNotificationAndClose(eq(TOPIC_NAME), eq("uebPublicKey"),
                eq("uebPrivateKey"), eq(serverList), eq(notificationData), eq(10L));
    }

    @Test
    public void testSendNotificationForDeleteService_WhenKafkaIsActive() {
        INotificationData notificationData = new NotificationDataImpl();
        when(kafkaHandler.isKafkaActive()).thenReturn(true);
        when(kafkaHandler.sendNotification(TOPIC_NAME, notificationData))
                .thenReturn(new CambriaErrorResponse(CambriaOperationStatus.OK, 200));

        ActionStatus result = deleteNotificationSender.sendNotificationForDeleteService(
                TOPIC_NAME, ThreadLocalsHolder.getUuid(), null, notificationData);

        assertEquals(ActionStatus.OK, result);
        verify(kafkaHandler, times(1)).sendNotification(eq(TOPIC_NAME), eq(notificationData));
    }

    @Test
    public void testSendNotificationForDeleteService_WhenKafkaNotificationFails() {
        INotificationData notificationData = new NotificationDataImpl();
        when(kafkaHandler.isKafkaActive()).thenReturn(true);
        when(kafkaHandler.sendNotification(TOPIC_NAME, notificationData))
                .thenReturn(new CambriaErrorResponse(CambriaOperationStatus.INTERNAL_SERVER_ERROR, 200));

        ActionStatus result = deleteNotificationSender.sendNotificationForDeleteService(
                TOPIC_NAME, ThreadLocalsHolder.getUuid(), null, notificationData);

        assertEquals(ActionStatus.GENERAL_ERROR, result);
        verify(kafkaHandler, times(1)).sendNotification(eq(TOPIC_NAME), eq(notificationData));
    }
}
