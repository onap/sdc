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
package org.openecomp.sdc.be.components.distribution.engine;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListenerConfigurer;
import org.springframework.kafka.config.KafkaListenerEndpointRegistrar;
import org.springframework.kafka.config.MethodKafkaListenerEndpoint;
import org.springframework.messaging.handler.annotation.support.DefaultMessageHandlerMethodFactory;
import org.springframework.stereotype.Component;

/**
 * Registered programmatically rather than with {@code @KafkaListener}: spring-kafka refuses a listener without topics and
 * would fail the context when no environment is configured. DistributionEngine reports that configuration error instead.
 */
@Component
public class DistributionStatusListener implements KafkaListenerConfigurer {

    public static final String LISTENER_ID = "distributionStatus";
    private final DistributionStatusNotificationHandler handler;
    private final DistributionTopics distributionTopics;

    public DistributionStatusListener(DistributionStatusNotificationHandler handler, DistributionTopics distributionTopics) {
        this.handler = handler;
        this.distributionTopics = distributionTopics;
    }

    @Override
    public void configureKafkaListeners(KafkaListenerEndpointRegistrar registrar) {
        String[] topics = distributionTopics.statusTopics();
        if (topics.length == 0) {
            return;
        }
        DefaultMessageHandlerMethodFactory handlerMethodFactory = new DefaultMessageHandlerMethodFactory();
        handlerMethodFactory.afterPropertiesSet();
        MethodKafkaListenerEndpoint<String, String> endpoint = new MethodKafkaListenerEndpoint<>();
        endpoint.setId(LISTENER_ID);
        endpoint.setTopics(topics);
        endpoint.setBean(this);
        try {
            endpoint.setMethod(DistributionStatusListener.class.getMethod("onStatus", ConsumerRecord.class));
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(e);
        }
        endpoint.setMessageHandlerMethodFactory(handlerMethodFactory);
        registrar.registerEndpoint(endpoint);
    }

    public void onStatus(ConsumerRecord<String, String> consumerRecord) {
        handler.handle(consumerRecord.topic(), consumerRecord.value());
    }
}
