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
import com.google.gson.Gson;
import fj.data.Either;
import org.apache.http.HttpStatus;
import org.openecomp.sdc.be.components.distribution.engine.CambriaErrorResponse;
import org.openecomp.sdc.be.components.distribution.engine.INotificationData;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.distribution.api.client.CambriaOperationStatus;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class KafkaHandler {

    private static final Logger log = Logger.getLogger(KafkaHandler.class.getName());
    private final Gson gson = new Gson();

    @Autowired
    private SdcKafkaConsumer sdcKafkaConsumer;

    @Autowired
    private SdcKafkaProducer sdcKafkaProducer;

    private boolean isKafkaActive;

    @VisibleForTesting
    KafkaHandler(SdcKafkaConsumer sdcKafkaConsumer, SdcKafkaProducer sdcKafkaProducer, boolean isKafkaActive) {
        this.sdcKafkaConsumer = sdcKafkaConsumer;
        this.sdcKafkaProducer = sdcKafkaProducer;
        this.isKafkaActive = isKafkaActive;
    }

    public KafkaHandler(){
        log.info("Creating Kafka Handler");
        isKafkaActive = Boolean.parseBoolean(System.getenv().getOrDefault("USE_KAFKA", "false"));
        if (isKafkaActive && sdcKafkaProducer == null && sdcKafkaConsumer == null) {
            log.info("Creating new kafka producer and consumer");
            sdcKafkaProducer = new SdcKafkaProducer();
            sdcKafkaConsumer = new SdcKafkaConsumer();
        }

    }

    public Boolean isKafkaActive(){
        return isKafkaActive;
    }

    public Either<Iterable<String>, CambriaErrorResponse> fetchFromTopic(String topicName) {
        try {
            log.info("KafkaHandler: Subscribing to topic: {}", topicName);
            sdcKafkaConsumer.subscribe(topicName);
            Iterable<String> messages = sdcKafkaConsumer.pollForMessagesForSpecificTopic(topicName);
            log.info("Returning messages from topic {}", topicName);
            return Either.left(messages);
        }
        catch (Exception e) {
            log.info("Failed to fetch from kafka topic", e);
            BeEcompErrorManager.getInstance().logBeDistributionEngineSystemError("fetchFromTopic", e.getMessage());
            CambriaErrorResponse cambriaErrorResponse = new CambriaErrorResponse(CambriaOperationStatus.INTERNAL_SERVER_ERROR,
                HttpStatus.SC_INTERNAL_SERVER_ERROR);
            return Either.right(cambriaErrorResponse);
        }
    }

    /**
     * Publish notification message to a given topic
     *
     * @param topicName
     * @param data
     * @return
     */
    public CambriaErrorResponse sendNotification(String topicName, INotificationData data) {
        String json = gson.toJson(data);
        log.info("Before sending notification data {} to topic {}", json, topicName);
        sdcKafkaProducer.send(json, topicName);
        return new CambriaErrorResponse(CambriaOperationStatus.OK, 200);
    }

    public CambriaErrorResponse sendNotificationAndClose(String topicName, INotificationData data) {
        String json = gson.toJson(data);
        CambriaErrorResponse response;
        log.info("Before sending notification data {} to topic {}", json, topicName);
        try {
            sdcKafkaProducer.send(json, topicName);
            response = new CambriaErrorResponse(CambriaOperationStatus.OK, 200);
        } catch (Exception e) {
            log.info("Failed to send notification {} to topic {} ", data, topicName, e);
            response = new CambriaErrorResponse(CambriaOperationStatus.INTERNAL_SERVER_ERROR, 500);
        }

        return response;
    }

}
