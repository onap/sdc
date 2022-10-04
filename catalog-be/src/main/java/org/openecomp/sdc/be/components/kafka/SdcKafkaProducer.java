/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2022 Nordix Foundation. All rights reserved.
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

import com.google.common.annotations.VisibleForTesting;
import java.util.Properties;
import java.util.UUID;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.config.SaslConfigs;
import org.openecomp.sdc.be.config.DistributionEngineConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class that provides a KafkaProducer to communicate with a kafka cluster
 */
public class SdcKafkaProducer {
    private static final Logger log = LoggerFactory.getLogger(SdcKafkaProducer.class.getName());

    private KafkaProducer<String, String> kafkaProducer;

    /**
     * Constructor setting up the KafkaProducer from a predefined set of configurations
     */
    public SdcKafkaProducer(DistributionEngineConfiguration deConfiguration) {
        log.info("Create SdcKafkaProducer via constructor");
        Properties properties = new Properties();

        properties.put(ProducerConfig.CLIENT_ID_CONFIG, deConfiguration.getDistributionStatusTopic().getConsumerId() + "-producer-" + UUID.randomUUID());
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,  "org.apache.kafka.common.serialization.StringSerializer");
        properties.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, deConfiguration.getKafkaBootStrapServers());
        properties.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_PLAINTEXT");
        properties.put(SaslConfigs.SASL_JAAS_CONFIG, getKafkaSaslJaasConfig());
        properties.put(SaslConfigs.SASL_MECHANISM, "SCRAM-SHA-512");
        kafkaProducer = new KafkaProducer<>(properties);
    }

    /**
     *
     * @param kafkaProducer Setting a KafkaProducer to use within the sdcKafkaProducer class
     */
    @VisibleForTesting
    SdcKafkaProducer(KafkaProducer kafkaProducer) {
        this.kafkaProducer = kafkaProducer;
    }

    /**
     * @return The Sasl Jaas Configuration
     */
    private static String getKafkaSaslJaasConfig() throws KafkaException {
        String saslJaasConfFromEnv = System.getenv("SASL_JAAS_CONFIG");
        if(saslJaasConfFromEnv != null) {
            return saslJaasConfFromEnv;
        } else {
            throw new KafkaException("sasl.jaas.config not set for Kafka Consumer");
        }
    }

    /**
     * @param message A message to Send
     * @param topicName The name of the topic to publish to
     * @return The status of the send request
     */
    public void send(String message, String topicName) throws KafkaException {
        ProducerRecord<String, String> kafkaMessagePayload = new ProducerRecord<>(topicName, "PartitionKey", message);
        kafkaProducer.send(kafkaMessagePayload);
    }

    /**
     * Kafka FLush operation
     */
    public void flush() throws KafkaException {
        log.info("SdcKafkaProducer - flush");
        kafkaProducer.flush();
    }
}
