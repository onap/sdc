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
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.config.SaslConfigs;
import org.openecomp.sdc.be.config.DistributionEngineConfiguration;
import org.openecomp.sdc.common.log.wrappers.Logger;

/**
 * Utility class that provides a KafkaConsumer to communicate with a kafka cluster
 */
public class SdcKafkaConsumer {

    private static final Logger log = Logger.getLogger(SdcKafkaConsumer.class.getName());
    private final DistributionEngineConfiguration deConfiguration;
    private final KafkaConsumer<String, String> kafkaConsumer;

    /**
     * Constructor setting up the KafkaConsumer from a predefined set of configurations
     */
    public SdcKafkaConsumer(DistributionEngineConfiguration deConfiguration){
        log.info("Create SdcKafkaConsumer via constructor");

        Properties properties = new Properties();
        this.deConfiguration = deConfiguration;

        properties.put(ConsumerConfig.CLIENT_ID_CONFIG, deConfiguration.getDistributionStatusTopic().getConsumerId()+ "-consumer-" + UUID.randomUUID());
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,  "org.apache.kafka.common.serialization.StringDeserializer");
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, deConfiguration.getDistributionStatusTopic().getConsumerGroup());
        properties.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, deConfiguration.getKafkaBootStrapServers());
        properties.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_PLAINTEXT");
        properties.put(ConsumerConfig.ALLOW_AUTO_CREATE_TOPICS_CONFIG, false);
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        properties.put(SaslConfigs.SASL_MECHANISM, "SCRAM-SHA-512");

        properties.put(SaslConfigs.SASL_JAAS_CONFIG, getKafkaSaslJaasConfig());
        kafkaConsumer = new KafkaConsumer<>(properties);
    }

    /**
     *
     * @param kafkaConsumer a kafkaConsumer to use within the class
     * @param deConfiguration - Configuration to pass into the class
     */
    @VisibleForTesting
    SdcKafkaConsumer(KafkaConsumer kafkaConsumer, DistributionEngineConfiguration deConfiguration){
        this.kafkaConsumer = kafkaConsumer;
        this.deConfiguration = deConfiguration;
    }

    /**
     *
     * @return the Sasl Jass Config
     */
    private String getKafkaSaslJaasConfig() {
        String saslJaasConfFromEnv = System.getenv("SASL_JAAS_CONFIG");
        if(saslJaasConfFromEnv != null) {
            return saslJaasConfFromEnv;
        } else {
            throw new KafkaException("sasl.jaas.config not set for Kafka Consumer");
        }
    }

    /**
     *
     * @param topic Topic in which to subscribe
     */
    public void subscribe(String topic) {
        if (!kafkaConsumer.subscription().contains(topic)) {
            kafkaConsumer.subscribe(Collections.singleton(topic));
        }
    }

    /**
     *
     * @return The list of messages for a specified topic, returned from the poll
     */
    public List<String> poll(){
        log.info("SdcKafkaConsumer - polling for messages for Specific Topic");
        List<String> msgs = new ArrayList<>();
        ConsumerRecords<String, String> ConsumerRecordsForSpecificTopic = kafkaConsumer.poll(Duration.ofSeconds(deConfiguration.getDistributionStatusTopic().getPollingIntervalSec()));
        for(ConsumerRecord<String, String> record : ConsumerRecordsForSpecificTopic){
            msgs.add(record.value());
        }
        return msgs;
    }
}
