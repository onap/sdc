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

import com.google.gson.JsonSyntaxException;
import fj.data.Either;
import org.apache.kafka.common.KafkaException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openecomp.sdc.be.components.distribution.engine.CambriaErrorResponse;
import org.openecomp.sdc.be.components.distribution.engine.INotificationData;
import org.openecomp.sdc.be.components.distribution.engine.NotificationDataImpl;
import org.openecomp.sdc.be.distribution.api.client.CambriaOperationStatus;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KafkaHandlerTest {

    @Mock
    private SdcKafkaConsumer mockSdcKafkaConsumer;

    @Mock
    private SdcKafkaProducer mockSdcKafkaProducer;

    private KafkaHandler kafkaHandler;

    @BeforeEach
    void setup() {
        kafkaHandler = new KafkaHandler(mockSdcKafkaConsumer, mockSdcKafkaProducer, true);
    }

    @Test
    void testIsKafkaActiveTrue() {
        assertTrue(kafkaHandler.isKafkaActive());
    }

    @Test
    void testIsKafkaActiveFalse() {
        kafkaHandler.setKafkaActive(false);
        assertFalse(kafkaHandler.isKafkaActive());
    }

    @Test
    void testFetchFromTopicSuccess() {
        String testTopic = "testTopic";
        List<String> mockedReturnedMessages = new ArrayList<>();
        mockedReturnedMessages.add("message1");
        mockedReturnedMessages.add("message2");
        when(mockSdcKafkaConsumer.poll(any())).thenReturn(mockedReturnedMessages);
        Either<Iterable<String>, CambriaErrorResponse> response = kafkaHandler.fetchFromTopic(testTopic);
        Iterable<String> actualReturnedMessages = response.left().value();
        assertTrue(response.isLeft());
        assertEquals(actualReturnedMessages, mockedReturnedMessages);
    }

    @Test
    void testFetchFromTopicFail() {
        String testTopic = "testTopic";
        when(mockSdcKafkaConsumer.poll(any())).thenThrow(new KafkaException());
        Either<Iterable<String>, CambriaErrorResponse> response = kafkaHandler.fetchFromTopic(testTopic);
        CambriaErrorResponse responseValue = response.right().value();
        assertTrue(response.isRight());
        assertEquals(responseValue.getOperationStatus(), CambriaOperationStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void testSendNotificationSuccess() {
        String testTopic = "testTopic";
        INotificationData testData = new NotificationDataImpl();
        CambriaErrorResponse response = kafkaHandler.sendNotification(testTopic, testData);
        assertEquals(response.getOperationStatus(), CambriaOperationStatus.OK);
        assertEquals(response.getHttpCode(), 200);
    }

    @Test
    void testSendNotificationKafkaException() {
        String testTopic = "testTopic";
        INotificationData testData = new NotificationDataImpl();
        doThrow(KafkaException.class).when(mockSdcKafkaProducer).send(any(), any());
        CambriaErrorResponse response = kafkaHandler.sendNotification(testTopic, testData);
        assertEquals(response.getOperationStatus(), CambriaOperationStatus.INTERNAL_SERVER_ERROR);
        assertEquals(response.getHttpCode(), 500);
    }

    @Test
    void testSendNotificationJsonSyntaxException() {
        String testTopic = "testTopic";
        INotificationData testData = new NotificationDataImpl();
        doThrow(JsonSyntaxException.class).when(mockSdcKafkaProducer).send(any(), any());
        CambriaErrorResponse response = kafkaHandler.sendNotification(testTopic, testData);
        assertEquals(response.getOperationStatus(), CambriaOperationStatus.INTERNAL_SERVER_ERROR);
        assertEquals(response.getHttpCode(), 500);
    }

    @Test
    void testSendNotificationFlushException() {
        String testTopic = "testTopic";
        INotificationData testData = new NotificationDataImpl();
        doThrow(KafkaException.class).when(mockSdcKafkaProducer).flush();
        CambriaErrorResponse response = kafkaHandler.sendNotification(testTopic, testData);
        assertEquals(response.getOperationStatus(), CambriaOperationStatus.INTERNAL_SERVER_ERROR);
        assertEquals(response.getHttpCode(), 500);
    }
}
