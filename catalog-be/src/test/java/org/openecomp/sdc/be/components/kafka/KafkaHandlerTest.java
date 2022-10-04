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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import org.apache.kafka.common.KafkaException;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.ArrayList;
import fj.data.Either;
import java.util.List;

import org.openecomp.sdc.be.components.distribution.engine.CambriaErrorResponse;
import org.openecomp.sdc.be.components.distribution.engine.NotificationDataImpl;
import org.openecomp.sdc.be.components.distribution.engine.INotificationData;
import org.openecomp.sdc.be.distribution.api.client.CambriaOperationStatus;


@ExtendWith(MockitoExtension.class)
public class KafkaHandlerTest {

    @Mock
    private SdcKafkaConsumer mockSdcKafkaConsumer;

    @Mock
    private SdcKafkaProducer mockSdcKafkaProducer;

    private KafkaHandler kafkaHandler;

    @Test
    public void testIsKafkaActiveFalseByDefault(){
        kafkaHandler = new KafkaHandler();
        assertFalse(kafkaHandler.isKafkaActive());
    }

    @Test
    public void testIsKafkaActiveTrue(){
        KafkaHandler kafkaHandler = new KafkaHandler(mockSdcKafkaConsumer, mockSdcKafkaProducer, true);
        assertTrue(kafkaHandler.isKafkaActive());
    }

    @Test
    public void testFetchFromTopicSuccess(){
        String testTopic = "testTopic";
        List<String> mockedReturnedMessages = new ArrayList<>();
        mockedReturnedMessages.add("message1");
        mockedReturnedMessages.add("message2");
        KafkaHandler kafkaHandler = new KafkaHandler(mockSdcKafkaConsumer, mockSdcKafkaProducer, true);
        when(mockSdcKafkaConsumer.pollForMessagesForSpecificTopic(testTopic)).thenReturn(mockedReturnedMessages);
        Either<Iterable<String>, CambriaErrorResponse> response = kafkaHandler.fetchFromTopic(testTopic);
        Iterable<String> actualReturnedMessages = response.left().value();
        assertTrue(response.isLeft());
        assertEquals(actualReturnedMessages, mockedReturnedMessages);
    }

    @Test
    public void testFetchFromTopicFail(){
        String testTopic = "testTopic";
        KafkaHandler kafkaHandler = new KafkaHandler(mockSdcKafkaConsumer, mockSdcKafkaProducer, true);
        when(mockSdcKafkaConsumer.pollForMessagesForSpecificTopic(testTopic)).thenThrow(new RuntimeException());
        Either<Iterable<String>, CambriaErrorResponse> response = kafkaHandler.fetchFromTopic(testTopic);
        CambriaErrorResponse responseValue = response.right().value();
        assertTrue(response.isRight());
        assertEquals(responseValue.getOperationStatus(), CambriaOperationStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    public void testSendNotificationSuccess(){
        String testTopic = "testTopic";
        KafkaHandler kafkaHandler = new KafkaHandler(mockSdcKafkaConsumer, mockSdcKafkaProducer, true);
        INotificationData testData = new NotificationDataImpl();
        CambriaErrorResponse response = kafkaHandler.sendNotification(testTopic, testData);
        assertEquals(response.getOperationStatus(), CambriaOperationStatus.OK);
        assertEquals(response.getHttpCode(), 200);
    }

    @Test
    public void testSendNotificationException(){
        String testTopic = "testTopic";
        KafkaHandler kafkaHandler = new KafkaHandler(mockSdcKafkaConsumer, mockSdcKafkaProducer, true);
        INotificationData testData = new NotificationDataImpl();
        when(mockSdcKafkaProducer.send(any(), any())).thenThrow(new KafkaException());
        CambriaErrorResponse response = kafkaHandler.sendNotification(testTopic, testData);
        assertEquals(response.getOperationStatus(), CambriaOperationStatus.INTERNAL_SERVER_ERROR);
        assertEquals(response.getHttpCode(), 500);
    }

    @Test
    public void testSendNotificationFlushException(){
        String testTopic = "testTopic";
        KafkaHandler kafkaHandler = new KafkaHandler(mockSdcKafkaConsumer, mockSdcKafkaProducer, true);
        INotificationData testData = new NotificationDataImpl();
        doThrow(KafkaException.class).when(mockSdcKafkaProducer).flush();
        CambriaErrorResponse response = kafkaHandler.sendNotification(testTopic, testData);
        assertEquals(response.getOperationStatus(), CambriaOperationStatus.INTERNAL_SERVER_ERROR);
        assertEquals(response.getHttpCode(), 500);
    }
}
