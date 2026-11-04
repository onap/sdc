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

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
@EnableKafka
public class KafkaConfig {

    // without it spring-kafka treats an authentication or authorization failure as fatal and stops the container for good
    static final Duration AUTH_EXCEPTION_RETRY_INTERVAL = Duration.ofSeconds(10);

    @Bean
    public ConsumerFactory<String, String> distributionStatusConsumerFactory(KafkaCommonConfig kafkaCommonConfig) {
        return new DefaultKafkaConsumerFactory<>(toMap(kafkaCommonConfig.getConsumerProperties()));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory(
        ConsumerFactory<String, String> distributionStatusConsumerFactory) {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(distributionStatusConsumerFactory);
        // started by DistributionEngine once configuration is validated, so a broken Kafka setup cannot fail context startup
        factory.setAutoStartup(false);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.RECORD);
        factory.getContainerProperties().setAuthExceptionRetryInterval(AUTH_EXCEPTION_RETRY_INTERVAL);
        factory.setCommonErrorHandler(new DefaultErrorHandler(new FixedBackOff(0L, 0L)));
        return factory;
    }

    @Bean
    public ProducerFactory<String, String> distributionProducerFactory(KafkaCommonConfig kafkaCommonConfig) {
        return new DefaultKafkaProducerFactory<>(toMap(kafkaCommonConfig.getProducerProperties()));
    }

    @Bean
    public KafkaTemplate<String, String> distributionKafkaTemplate(ProducerFactory<String, String> distributionProducerFactory) {
        return new KafkaTemplate<>(distributionProducerFactory);
    }

    private static Map<String, Object> toMap(Properties properties) {
        Map<String, Object> map = new HashMap<>();
        properties.forEach((k, v) -> map.put((String) k, v));
        return map;
    }
}
