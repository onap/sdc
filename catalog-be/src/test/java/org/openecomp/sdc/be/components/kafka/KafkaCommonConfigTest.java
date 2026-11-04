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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.SaslConfigs;
import org.junit.jupiter.api.Test;
import org.openecomp.sdc.be.config.DistributionEngineConfiguration;
import org.openecomp.sdc.be.config.DistributionEngineConfiguration.DistributionDeleteTopicConfig;
import org.openecomp.sdc.be.config.DistributionEngineConfiguration.DistributionNotificationTopicConfig;
import org.openecomp.sdc.be.config.DistributionEngineConfiguration.DistributionStatusTopicConfig;

class KafkaCommonConfigTest {

    private static DistributionEngineConfiguration minimalConfig() {
        DistributionEngineConfiguration de = new DistributionEngineConfiguration();
        de.setKafkaBootStrapServers("kafka:9092");
        DistributionStatusTopicConfig status = new DistributionStatusTopicConfig();
        status.setConsumerId("sdc-id");
        status.setConsumerGroup("sdc");
        de.setDistributionStatusTopic(status);
        DistributionNotificationTopicConfig notif = new DistributionNotificationTopicConfig();
        notif.setMaxWaitingAfterSendingSeconds(5);
        de.setDistributionNotificationTopic(notif);
        return de;
    }

    private static DistributionEngineConfiguration withDeleteTimeout(int seconds) {
        DistributionEngineConfiguration de = minimalConfig();
        DistributionDeleteTopicConfig delete = new DistributionDeleteTopicConfig();
        delete.setMaxWaitingAfterSendingSeconds(seconds);
        de.setDistributionDeleteTopic(delete);
        return de;
    }

    @Test
    void maxBlockMsFollowsSendTimeout() {
        Properties p = new KafkaCommonConfig(withDeleteTimeout(5), Map.of("SASL_JAAS_CONFIG", "x")).getProducerProperties();
        assertEquals("5000", p.get(ProducerConfig.MAX_BLOCK_MS_CONFIG));
    }

    @Test
    void maxBlockMsCoversTheLongerDeleteTimeout() {
        Properties p = new KafkaCommonConfig(withDeleteTimeout(12), Map.of("SASL_JAAS_CONFIG", "x")).getProducerProperties();
        assertEquals("12000", p.get(ProducerConfig.MAX_BLOCK_MS_CONFIG));
    }

    @Test
    void maxBlockMsToleratesMissingDeleteTopicConfig() {
        Properties p = new KafkaCommonConfig(minimalConfig(), Map.of("SASL_JAAS_CONFIG", "x")).getProducerProperties();
        assertEquals("5000", p.get(ProducerConfig.MAX_BLOCK_MS_CONFIG));
    }

    @Test
    void missingJaasDoesNotThrowAndIsReported() {
        KafkaCommonConfig c = new KafkaCommonConfig(minimalConfig(), Map.of());
        assertDoesNotThrow(c::getConsumerProperties);
        assertDoesNotThrow(c::getProducerProperties);
        assertDoesNotThrow(c::getAdminProperties);
        assertTrue(c.configurationError().isPresent());
        assertFalse(c.getConsumerProperties().containsKey(SaslConfigs.SASL_JAAS_CONFIG));
    }

    @Test
    void plaintextNeedsNoJaas() {
        KafkaCommonConfig c = new KafkaCommonConfig(minimalConfig(), Map.of("SECURITY_PROTOCOL", "PLAINTEXT"));
        assertTrue(c.configurationError().isEmpty());
        assertEquals("PLAINTEXT", c.getConsumerProperties().get(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG));
    }

    @Test
    void consumerCommitsAreLeftToTheContainer() {
        KafkaCommonConfig c = new KafkaCommonConfig(minimalConfig(), Map.of("SASL_JAAS_CONFIG", "x"));
        assertEquals("false", c.getConsumerProperties().get(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG));
    }

    @Test
    void adminClientGetsItsOwnClientId() {
        Properties p = new KafkaCommonConfig(minimalConfig(), Map.of("SASL_JAAS_CONFIG", "x")).getAdminProperties();
        assertTrue(((String) p.get(CommonClientConfigs.CLIENT_ID_CONFIG)).startsWith("sdc-id-admin-"));
        assertEquals("kafka:9092", p.get(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG));
    }

    @Test
    void absentDistributionEngineConfigurationIsReportedNotThrown() {
        KafkaCommonConfig c = new KafkaCommonConfig(null, Map.of("SASL_JAAS_CONFIG", "x"));
        assertDoesNotThrow(c::getConsumerProperties);
        assertDoesNotThrow(c::getProducerProperties);
        assertEquals(Optional.of("kafkaBootStrapServers not configured"), c.configurationError());
    }
}
