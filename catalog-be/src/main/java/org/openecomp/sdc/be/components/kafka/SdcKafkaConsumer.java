/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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
import java.util.Map;
import java.util.Properties;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.config.SaslConfigs;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.config.DistributionEngineConfiguration;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.springframework.stereotype.Component;

public class SdcKafkaConsumer {

    private static final Logger log = Logger.getLogger(SdcKafkaConsumer.class.getName());
    private final DistributionEngineConfiguration deConfiguration;
    private final KafkaConsumer<String, String> kafkaConsumer;

    public SdcKafkaConsumer(){
        log.info("Create SdcKafkaConsumer via constructor");
        deConfiguration = ConfigurationManager.getConfigurationManager().getDistributionEngineConfiguration();

        Properties properties = new Properties();
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,  "org.apache.kafka.common.serialization.StringDeserializer");
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, deConfiguration.getDistributionStatusTopic().getConsumerGroup());
        properties.put(ConsumerConfig.CLIENT_ID_CONFIG, deConfiguration.getDistributionStatusTopic().getConsumerId());
        properties.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, deConfiguration.getKafkaBootStrapServers());
        properties.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_PLAINTEXT");
        properties.put(SaslConfigs.SASL_MECHANISM, "SCRAM-SHA-512");
        properties.put(SaslConfigs.SASL_JAAS_CONFIG, getKafkaSaslJaasConfig());
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        kafkaConsumer = new KafkaConsumer<>(properties);

    }

    @VisibleForTesting
    SdcKafkaConsumer(KafkaConsumer kafkaConsumer, DistributionEngineConfiguration deConfiguration){
        this.kafkaConsumer = kafkaConsumer;
        this.deConfiguration = deConfiguration;
    }

    private String getKafkaSaslJaasConfig() {
        String saslJaasConfFromEnv = System.getenv("SASL_JAAS_CONFIG");
        if(saslJaasConfFromEnv != null) {
            return saslJaasConfFromEnv;
        } else {
            throw new KafkaException("sasl.jaas.config not set for Kafka Consumer");
        }
    }

    public void subscribe(String topics) {
        log.info("SdcKafkaConsumer - Subscribing to topic {}", topics);
        if (!kafkaConsumer.subscription().contains(topics)) {
            kafkaConsumer.subscribe(Collections.singleton(topics));
        }
    }

    public ConsumerRecords<String, String> poll() {
        log.info("SdcKafkaConsumer - polling");
        return kafkaConsumer.poll(Duration.ofSeconds(deConfiguration.getDistributionStatusTopic().getPollingIntervalSec()));
    }

    public Iterable<ConsumerRecord<String, String>> pollForSpecificTopic(String topic){
        log.info("SdcKafkaConsumer - polling for Specific Topic");
        ConsumerRecords<String, String> records = poll();
        return records.records(topic);
    }

    public List<String> pollForMessagesForSpecificTopic(String topic){
        log.info("SdcKafkaConsumer - polling for messages for Specific Topic");
        List<String> msgs = new ArrayList<>();
        Iterable<ConsumerRecord<String, String>> recordsForSpecificTopic = pollForSpecificTopic(topic);
        for(ConsumerRecord<String, String> record : recordsForSpecificTopic){
            msgs.add(record.value());
        }
        return msgs;
    }

    public Map<String, List<PartitionInfo>> listTopics() {
        return kafkaConsumer.listTopics();
    }
}
