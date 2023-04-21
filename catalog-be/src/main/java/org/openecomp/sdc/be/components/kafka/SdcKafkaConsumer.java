/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2022-2023 Nordix Foundation. All rights reserved.
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
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.KafkaException;
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
        KafkaCommonConfig kafkaCommonConfig = new KafkaCommonConfig(deConfiguration);
        Properties properties = kafkaCommonConfig.getConsumerProperties();
        this.deConfiguration = deConfiguration;
        kafkaConsumer = new KafkaConsumer<>(properties);
    }

    /**
     *
     * @param kafkaConsumer a kafkaConsumer to use within the class
     * @param deConfiguration - Configuration to pass into the class
     */
    @VisibleForTesting
    SdcKafkaConsumer(KafkaConsumer kafkaConsumer, DistributionEngineConfiguration deConfiguration){
        this.deConfiguration = deConfiguration;
        this.kafkaConsumer = kafkaConsumer;
    }

    /**
     *
     * @param topic Topic in which to subscribe
     */
    public void subscribe(String topic) throws KafkaException {
        if (!kafkaConsumer.subscription().contains(topic)) {
            kafkaConsumer.subscribe(Collections.singleton(topic));
        }
    }

    /**
     *
     * @return The list of messages for a specified topic, returned from the poll
     */
    public List<String> poll(String topicName) throws KafkaException {
        log.info("SdcKafkaConsumer - polling for messages from Topic: {}", topicName);
        List<String> msgs = new ArrayList<>();
        ConsumerRecords<String, String> consumerRecordsForSpecificTopic = kafkaConsumer.poll(Duration.ofSeconds(deConfiguration.getDistributionStatusTopic().getPollingIntervalSec()));
        for(ConsumerRecord<String, String> rec : consumerRecordsForSpecificTopic){
            msgs.add(rec.value());
        }
        return msgs;
    }
}
