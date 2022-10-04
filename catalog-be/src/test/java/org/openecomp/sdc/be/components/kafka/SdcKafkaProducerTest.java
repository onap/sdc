package org.openecomp.sdc.be.components.kafka;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.KafkaException;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.openecomp.sdc.be.catalog.api.IStatus;

public class SdcKafkaProducerTest {

    @Test
    public void TestSendSuccess(){
        KafkaProducer<byte[], byte[]> mockKafkaProducer = Mockito.mock(KafkaProducer.class);
        SdcKafkaProducer sdcKafkaProducer = new SdcKafkaProducer(mockKafkaProducer);
        ArgumentCaptor<ProducerRecord> captor = ArgumentCaptor.forClass(ProducerRecord.class);
        IStatus response = sdcKafkaProducer.send("testMessage", "testTopic");


        verify(mockKafkaProducer).send(captor.capture());
        assertTrue(response.equals(IStatus.getSuccessStatus()));
    }

    @Test
    public void testSendFail(){
        KafkaProducer<byte[], byte[]> mockKafkaProducer = Mockito.mock(KafkaProducer.class);
        SdcKafkaProducer sdcKafkaProducer = new SdcKafkaProducer(mockKafkaProducer);

        when(mockKafkaProducer.send(any())).thenThrow(new KafkaException());
        IStatus response = sdcKafkaProducer.send("testMessage", "testTopic");
        assertTrue(response.equals(IStatus.getFailStatus()));
    }
}
