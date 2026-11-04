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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.openecomp.sdc.be.components.distribution.engine.DistributionStatusListener;
import org.openecomp.sdc.be.components.distribution.engine.DistributionStatusNotificationHandler;
import org.openecomp.sdc.be.components.distribution.engine.DistributionTopics;
import org.openecomp.sdc.be.config.DistributionEngineConfiguration;
import org.openecomp.sdc.be.config.DistributionEngineConfiguration.DistributionStatusTopicConfig;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.MessageListenerContainer;

class KafkaConfigTest {

    @Test
    @SuppressWarnings("unchecked")
    void authenticationFailuresAreRetriedInsteadOfStoppingTheContainer() {
        Duration interval = new KafkaConfig().kafkaListenerContainerFactory(mock(ConsumerFactory.class))
            .getContainerProperties().getAuthExceptionRetryInterval();
        assertEquals(KafkaConfig.AUTH_EXCEPTION_RETRY_INTERVAL, interval);
        assertEquals(Duration.ofSeconds(10), interval);
    }

    @Test
    void contextStartsWithoutConfiguredEnvironments() {
        DistributionEngineConfiguration de = new DistributionEngineConfiguration();
        de.setKafkaBootStrapServers("localhost:1");
        de.setDistributionStatusTopicName("SDC-DISTR-STATUS-TOPIC");
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.registerBean(KafkaCommonConfig.class, () -> new KafkaCommonConfig(de, Map.of("SECURITY_PROTOCOL", "PLAINTEXT")));
            context.registerBean("distributionTopics", DistributionTopics.class, () -> new DistributionTopics(de));
            context.registerBean(DistributionStatusNotificationHandler.class, () -> mock(DistributionStatusNotificationHandler.class));
            context.register(KafkaConfig.class, DistributionStatusListener.class);
            context.refresh();
            MessageListenerContainer container = context.getBean(KafkaListenerEndpointRegistry.class)
                .getListenerContainer(DistributionStatusListener.LISTENER_ID);
            assertNull(container);
        }
    }

    @Test
    void configuredEnvironmentsRegisterTheStatusListener() {
        DistributionEngineConfiguration de = new DistributionEngineConfiguration();
        de.setKafkaBootStrapServers("localhost:1");
        de.setDistributionStatusTopicName("SDC-DISTR-STATUS-TOPIC");
        de.setEnvironments(List.of("AUTO"));
        DistributionStatusTopicConfig status = new DistributionStatusTopicConfig();
        status.setConsumerId("sdc-id");
        status.setConsumerGroup("sdc-group");
        de.setDistributionStatusTopic(status);
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.registerBean(KafkaCommonConfig.class, () -> new KafkaCommonConfig(de, Map.of("SECURITY_PROTOCOL", "PLAINTEXT")));
            context.registerBean("distributionTopics", DistributionTopics.class, () -> new DistributionTopics(de));
            context.registerBean(DistributionStatusNotificationHandler.class, () -> mock(DistributionStatusNotificationHandler.class));
            context.register(KafkaConfig.class, DistributionStatusListener.class);
            context.refresh();
            MessageListenerContainer container = context.getBean(KafkaListenerEndpointRegistry.class)
                .getListenerContainer(DistributionStatusListener.LISTENER_ID);
            assertEquals(List.of("SDC-DISTR-STATUS-TOPIC-AUTO"),
                List.of(container.getContainerProperties().getTopics()));
            assertEquals("sdc-group", container.getGroupId());
            assertFalse(container.isRunning());
        }
    }
}
