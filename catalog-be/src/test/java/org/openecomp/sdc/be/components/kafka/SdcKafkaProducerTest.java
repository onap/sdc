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
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.common.KafkaException;

import org.openecomp.sdc.be.catalog.api.IStatus;
import org.openecomp.sdc.be.config.DistributionEngineConfiguration;

public class SdcKafkaProducerTest {

    @Test
    public void TestSendSuccess(){
        KafkaProducer<byte[], byte[]> mockKafkaProducer = Mockito.mock(KafkaProducer.class);
        SdcKafkaProducer sdcKafkaProducer = new SdcKafkaProducer(mockKafkaProducer);
        ArgumentCaptor<ProducerRecord> captor = ArgumentCaptor.forClass(ProducerRecord.class);
        IStatus response = sdcKafkaProducer.send("testMessage", "testTopic");


        verify(mockKafkaProducer).send(captor.capture());
        assertEquals(response, IStatus.getSuccessStatus());
    }

    @Test
    public void testFlushSuccess(){
        KafkaProducer<byte[], byte[]> mockKafkaProducer = Mockito.mock(KafkaProducer.class);
        SdcKafkaProducer sdcKafkaProducer = new SdcKafkaProducer(mockKafkaProducer);
        sdcKafkaProducer.flush();

        verify(mockKafkaProducer).flush();
    }

    @Test
    public void testSendFail(){
        KafkaProducer<byte[], byte[]> mockKafkaProducer = Mockito.mock(KafkaProducer.class);
        SdcKafkaProducer sdcKafkaProducer = new SdcKafkaProducer(mockKafkaProducer);

        when(mockKafkaProducer.send(any())).thenThrow(new KafkaException());
        IStatus response = sdcKafkaProducer.send("testMessage", "testTopic");
        assertEquals(response, IStatus.getFailStatus());
    }

    @Test
    public void testSaslJaasConfigNotFound(){
        assertThrows(
            KafkaException.class,
            () ->  new SdcKafkaProducer(setTestDistributionEngineConfigs()),
            "Sasl Jaas Config should not be found, so expected a KafkaException"
        );
    }

    private DistributionEngineConfiguration setTestDistributionEngineConfigs(){
        DistributionEngineConfiguration.DistributionStatusTopicConfig dStatusTopicConfig = new DistributionEngineConfiguration.DistributionStatusTopicConfig();
        DistributionEngineConfiguration deConfiguration = new DistributionEngineConfiguration();
        deConfiguration.setKafkaBootStrapServers("TestBootstrapServer");
        dStatusTopicConfig.setConsumerId("consumerId");

        deConfiguration.setDistributionStatusTopic(dStatusTopicConfig);
        deConfiguration.getDistributionStatusTopic().getConsumerId();
        return deConfiguration;
    }
}
