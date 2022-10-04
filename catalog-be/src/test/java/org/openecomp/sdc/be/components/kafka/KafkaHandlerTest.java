package org.openecomp.sdc.be.components.kafka;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import fj.data.Either;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.sdc.be.components.distribution.engine.CambriaErrorResponse;
import org.openecomp.sdc.be.components.distribution.engine.INotificationData;
import org.openecomp.sdc.be.components.distribution.engine.NotificationDataImpl;
import org.openecomp.sdc.be.distribution.api.client.CambriaOperationStatus;


@RunWith(MockitoJUnitRunner.class)
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
        assertTrue(actualReturnedMessages.equals(mockedReturnedMessages));
    }

    @Test
    public void testFetchFromTopicFail(){
        String testTopic = "testTopic";
        KafkaHandler kafkaHandler = new KafkaHandler(mockSdcKafkaConsumer, mockSdcKafkaProducer, true);
        when(mockSdcKafkaConsumer.pollForMessagesForSpecificTopic(testTopic)).thenThrow(new RuntimeException());
        Either<Iterable<String>, CambriaErrorResponse> response = kafkaHandler.fetchFromTopic(testTopic);
        CambriaErrorResponse responseValue = response.right().value();
        assertTrue(response.isRight());
        assertTrue(responseValue.getOperationStatus().equals(CambriaOperationStatus.INTERNAL_SERVER_ERROR));
    }

    @Test
    public void testSendNotificationSuccess(){
        String testTopic = "testTopic";
        KafkaHandler kafkaHandler = new KafkaHandler(mockSdcKafkaConsumer, mockSdcKafkaProducer, true);
        INotificationData testData = new NotificationDataImpl();
        CambriaErrorResponse response = kafkaHandler.sendNotification(testTopic, testData);
        assertTrue(response.getOperationStatus().equals(CambriaOperationStatus.OK));
        assertTrue(response.getHttpCode().equals(200));
    }

    @Test
    public void testSendNotificationAndCloseSuccess(){
        String testTopic = "testTopic";
        KafkaHandler kafkaHandler = new KafkaHandler(mockSdcKafkaConsumer, mockSdcKafkaProducer, true);
        INotificationData testData = new NotificationDataImpl();
        CambriaErrorResponse response = kafkaHandler.sendNotificationAndClose(testTopic, testData);
        assertTrue(response.getOperationStatus().equals(CambriaOperationStatus.OK));
        assertTrue(response.getHttpCode().equals(200));
    }

    @Test
    public void testSendNotificationAndCloseException(){
        String testTopic = "testTopic";
        KafkaHandler kafkaHandler = new KafkaHandler(mockSdcKafkaConsumer, mockSdcKafkaProducer, true);
        INotificationData testData = new NotificationDataImpl();
        when(mockSdcKafkaProducer.send(any(), any())).thenThrow(new RuntimeException());
        CambriaErrorResponse response = kafkaHandler.sendNotificationAndClose(testTopic, testData);
        assertTrue(response.getOperationStatus().equals(CambriaOperationStatus.INTERNAL_SERVER_ERROR));
        assertTrue(response.getHttpCode().equals(500));
    }
}
