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
package org.openecomp.sdc.be.components.kafka;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.kafka.common.KafkaException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openecomp.sdc.be.components.distribution.engine.NotificationDataImpl;
import org.openecomp.sdc.be.components.kafka.DistributionNotificationPublisher.PublishResult;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.openecomp.sdc.common.impl.FSConfigurationSource;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.SettableListenableFuture;

class DistributionNotificationPublisherTest {

    @SuppressWarnings("unchecked")
    private final KafkaTemplate<String, String> template = mock(KafkaTemplate.class);
    private final DistributionNotificationPublisher publisher = new DistributionNotificationPublisher(template);

    @BeforeAll
    static void loadConfiguration() {
        new ConfigurationManager(new FSConfigurationSource(ExternalConfiguration.getChangeListener(), "src/test/resources/config/catalog-be"));
    }

    @Test
    void ackedSendIsOkAndUsesTheFixedKey() {
        SettableListenableFuture<SendResult<String, String>> f = new SettableListenableFuture<>();
        f.set(null);
        when(template.send(anyString(), anyString(), anyString())).thenReturn(f);
        assertEquals(PublishResult.OK, publisher.publish("T", new NotificationDataImpl(), 5));
        verify(template).send(eq("T"), eq("PartitionKey"), anyString());
    }

    @Test
    void brokerErrorIsFailed() {
        SettableListenableFuture<SendResult<String, String>> f = new SettableListenableFuture<>();
        f.setException(new KafkaException("down"));
        when(template.send(anyString(), anyString(), anyString())).thenReturn(f);
        assertEquals(PublishResult.FAILED, publisher.publish("T", new NotificationDataImpl(), 5));
    }

    @Test
    void noAckWithinTimeoutIsFailed() {
        when(template.send(anyString(), anyString(), anyString())).thenReturn(new SettableListenableFuture<>());
        assertEquals(PublishResult.FAILED, publisher.publish("T", new NotificationDataImpl(), 0));
    }

    @Test
    void sendThrowingSynchronouslyIsFailed() {
        when(template.send(anyString(), anyString(), anyString())).thenThrow(new KafkaException("max.block.ms"));
        assertEquals(PublishResult.FAILED, publisher.publish("T", new NotificationDataImpl(), 5));
    }

    @Test
    void resultsCarryTheAuditedHttpCodeAndDescription() {
        assertEquals(200, PublishResult.OK.httpCode());
        assertEquals("OK", PublishResult.OK.description());
        assertEquals(500, PublishResult.FAILED.httpCode());
        assertEquals("Error: failed to publish to Kafka", PublishResult.FAILED.description());
    }

    @Test
    void sendOnAClosedProducerIsFailed() {
        when(template.send(anyString(), anyString(), anyString())).thenThrow(new IllegalStateException("Cannot perform operation after producer has been closed"));
        assertEquals(PublishResult.FAILED, publisher.publish("T", new NotificationDataImpl(), 5));
    }
}
