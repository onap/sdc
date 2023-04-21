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
import java.util.Properties;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.KafkaException;
import org.openecomp.sdc.be.config.DistributionEngineConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class that provides a KafkaProducer to communicate with a kafka cluster
 */
public class SdcKafkaProducer {
    private static final Logger log = LoggerFactory.getLogger(SdcKafkaProducer.class.getName());

    private final KafkaProducer<String, String> kafkaProducer;

    /**
     * Constructor setting up the KafkaProducer from a predefined set of configurations
     */
    public SdcKafkaProducer(DistributionEngineConfiguration deConfiguration) {
        log.info("Create SdcKafkaProducer via constructor");
        KafkaCommonConfig kafkaCommonConfig = new KafkaCommonConfig(deConfiguration);
        Properties properties = kafkaCommonConfig.getProducerProperties();
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
     * @param message A message to Send
     * @param topicName The name of the topic to publish to
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
