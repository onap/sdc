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

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.apache.kafka.common.KafkaException;
import org.openecomp.sdc.be.components.distribution.engine.INotificationData;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Publishes distribution notifications and waits for the broker to acknowledge them.
 */
@Component
public class DistributionNotificationPublisher {

    static final String PARTITION_KEY = "PartitionKey";
    private static final Logger log = Logger.getLogger(DistributionNotificationPublisher.class.getName());
    private final Gson gson = new Gson();
    private final KafkaTemplate<String, String> kafkaTemplate;

    public DistributionNotificationPublisher(KafkaTemplate<String, String> distributionKafkaTemplate) {
        this.kafkaTemplate = distributionKafkaTemplate;
    }

    public PublishResult publish(String topic, INotificationData data, long timeoutSeconds) {
        try {
            kafkaTemplate.send(topic, PARTITION_KEY, gson.toJson(data)).get(timeoutSeconds, TimeUnit.SECONDS);
            return PublishResult.OK;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return failed(topic, e);
        } catch (ExecutionException | TimeoutException | KafkaException | JsonIOException | IllegalStateException | IllegalArgumentException e) {
            return failed(topic, e);
        }
    }

    private PublishResult failed(String topic, Exception e) {
        BeEcompErrorManager.getInstance().logBeDistributionEngineSystemError("sendNotification", e.getMessage());
        log.error("Failed to send message to topic {}", topic, e);
        return PublishResult.FAILED;
    }

    public enum PublishResult {
        OK(200, "OK"),
        FAILED(500, "Error: failed to publish to Kafka");

        private final int httpCode;
        private final String description;

        PublishResult(int httpCode, String description) {
            this.httpCode = httpCode;
            this.description = description;
        }

        public int httpCode() {
            return httpCode;
        }

        public String description() {
            return description;
        }
    }
}
