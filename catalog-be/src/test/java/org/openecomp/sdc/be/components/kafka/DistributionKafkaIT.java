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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openecomp.sdc.be.components.distribution.engine.DistributionStatusListener;
import org.openecomp.sdc.be.components.distribution.engine.DistributionStatusNotificationHandler;
import org.openecomp.sdc.be.components.distribution.engine.DistributionTopics;
import org.openecomp.sdc.be.components.distribution.engine.NotificationDataImpl;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.config.DistributionEngineConfiguration;
import org.openecomp.sdc.be.config.DistributionEngineConfiguration.DistributionNotificationTopicConfig;
import org.openecomp.sdc.be.config.DistributionEngineConfiguration.DistributionStatusTopicConfig;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.openecomp.sdc.common.impl.FSConfigurationSource;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.testcontainers.kafka.KafkaContainer;

class DistributionKafkaIT {

    private static final String STATUS_TOPIC = "SDC-DISTR-STATUS-TOPIC-AUTO";
    private static final String CONSUMER_GROUP = "it-" + UUID.randomUUID();
    private static final KafkaContainer KAFKA = new KafkaContainer("apache/kafka:3.7.0");
    private static final DistributionStatusNotificationHandler handler = mock(DistributionStatusNotificationHandler.class);
    private static AnnotationConfigApplicationContext context;
    private static DistributionNotificationPublisher publisher;
    private static KafkaClusterProbe probe;

    @BeforeAll
    static void startBrokerAndListener() throws Exception {
        new ConfigurationManager(new FSConfigurationSource(ExternalConfiguration.getChangeListener(), "src/test/resources/config/catalog-be"));
        KAFKA.start();
        KafkaCommonConfig kafkaCommonConfig = new KafkaCommonConfig(distributionEngineConfiguration(), Map.of("SECURITY_PROTOCOL", "PLAINTEXT"));
        // auto.offset.reset=latest: a topic created by the first send could be assigned after that record, which is then never read
        try (Admin admin = Admin.create(Map.of(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, KAFKA.getBootstrapServers()))) {
            admin.createTopics(List.of(new NewTopic(STATUS_TOPIC, 1, (short) 1))).all().get(30, TimeUnit.SECONDS);
        }
        context = new AnnotationConfigApplicationContext();
        context.registerBean(KafkaCommonConfig.class, () -> kafkaCommonConfig);
        context.registerBean("distributionTopics", DistributionTopics.class, () -> new DistributionTopics(distributionEngineConfiguration()));
        context.registerBean(DistributionStatusNotificationHandler.class, () -> handler);
        context.register(KafkaConfig.class, DistributionStatusListener.class, DistributionNotificationPublisher.class, KafkaClusterProbe.class);
        context.refresh();
        MessageListenerContainer container = context.getBean(KafkaListenerEndpointRegistry.class)
            .getListenerContainer(DistributionStatusListener.LISTENER_ID);
        container.start();
        waitForAssignment(container);
        publisher = context.getBean(DistributionNotificationPublisher.class);
        probe = context.getBean(KafkaClusterProbe.class);
    }

    @AfterAll
    static void stop() {
        if (context != null) {
            context.close();
        }
        KAFKA.stop();
    }

    private static DistributionEngineConfiguration distributionEngineConfiguration() {
        DistributionEngineConfiguration de = new DistributionEngineConfiguration();
        de.setKafkaBootStrapServers(KAFKA.getBootstrapServers());
        de.setEnvironments(List.of("AUTO"));
        de.setDistributionStatusTopicName("SDC-DISTR-STATUS-TOPIC");
        DistributionStatusTopicConfig status = new DistributionStatusTopicConfig();
        status.setConsumerId("sdc-it");
        status.setConsumerGroup(CONSUMER_GROUP);
        de.setDistributionStatusTopic(status);
        DistributionNotificationTopicConfig notification = new DistributionNotificationTopicConfig();
        notification.setMaxWaitingAfterSendingSeconds(5);
        de.setDistributionNotificationTopic(notification);
        return de;
    }

    private static void waitForAssignment(MessageListenerContainer container) throws InterruptedException {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(30);
        while (container.getAssignedPartitions() == null || container.getAssignedPartitions().isEmpty()) {
            if (System.nanoTime() > deadline) {
                throw new IllegalStateException("status topic partition was never assigned");
            }
            Thread.sleep(100);
        }
    }

    private static NotificationDataImpl statusNotification(String distributionId) {
        NotificationDataImpl data = new NotificationDataImpl();
        data.setDistributionID(distributionId);
        return data;
    }

    @Test
    void statusRecordReachesTheHandlerWithoutPollingDelay() {
        long t0 = System.nanoTime();
        publisher.publish(STATUS_TOPIC, statusNotification("d1"), 5);
        verify(handler, timeout(5_000)).handle(eq(STATUS_TOPIC), contains("\"d1\""));
        assertTrue(Duration.ofNanos(System.nanoTime() - t0).toSeconds() < 5);
    }

    @Test
    void consumedRecordsAreCommittedForTheConfiguredGroup() throws Exception {
        publisher.publish(STATUS_TOPIC, statusNotification("d3"), 5);
        verify(handler, timeout(5_000)).handle(eq(STATUS_TOPIC), contains("\"d3\""));
        TopicPartition partition = new TopicPartition(STATUS_TOPIC, 0);
        try (Admin admin = Admin.create(Map.of(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, KAFKA.getBootstrapServers()))) {
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
            OffsetAndMetadata committed = null;
            while (committed == null && System.nanoTime() < deadline) {
                committed = admin.listConsumerGroupOffsets(CONSUMER_GROUP).partitionsToOffsetAndMetadata()
                    .get(5, TimeUnit.SECONDS).get(partition);
                if (committed == null) {
                    Thread.sleep(100);
                }
            }
            assertNotNull(committed, "no offset committed for group " + CONSUMER_GROUP);
            assertTrue(committed.offset() > 0);
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    void poisonRecordIsSkipped() throws Exception {
        doThrow(new RuntimeException("boom")).when(handler).handle(anyString(), contains("poison"));
        context.getBean(KafkaTemplate.class).send(STATUS_TOPIC, "poison").get(5, TimeUnit.SECONDS);
        publisher.publish(STATUS_TOPIC, statusNotification("d2"), 5);
        verify(handler, timeout(5_000)).handle(anyString(), contains("\"d2\""));
    }

    @Test
    void probeTracksBrokerAvailability() {
        assertTrue(probe.isClusterReachable(Duration.ofSeconds(5)));
    }
}
