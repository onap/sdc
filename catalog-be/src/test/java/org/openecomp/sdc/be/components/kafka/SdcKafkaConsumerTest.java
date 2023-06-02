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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.kafka.common.KafkaException;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.Collection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.jetbrains.annotations.NotNull;

import org.openecomp.sdc.be.config.DistributionEngineConfiguration;

public class SdcKafkaConsumerTest {

    @Test
    public void TestSubscribeSuccess(){
        KafkaConsumer<byte[], byte[]> mockKafkaConsumer = Mockito.mock(KafkaConsumer.class);
        SdcKafkaConsumer sdcKafkaConsumer = new SdcKafkaConsumer(mockKafkaConsumer, null);
        ArgumentCaptor<Collections> captor = ArgumentCaptor.forClass(Collections.class);

        String testTopics = "testTopic";
        sdcKafkaConsumer.subscribe(testTopics);
        verify(mockKafkaConsumer).subscribe((Collection<String>) captor.capture());
    }

    @Test
    public void TestSubscribeAlreadySubscribed(){
        KafkaConsumer<byte[], byte[]> mockKafkaConsumer = Mockito.mock(KafkaConsumer.class);
        SdcKafkaConsumer sdcKafkaConsumer = new SdcKafkaConsumer(mockKafkaConsumer, null);
        ArgumentCaptor<Collections> captor = ArgumentCaptor.forClass(Collections.class);


        String testTopics = "testTopic";
        Set<String> currentSubs = new HashSet<String>();
        currentSubs.add(testTopics);
        when(mockKafkaConsumer.subscription()).thenReturn(currentSubs);
        sdcKafkaConsumer.subscribe(testTopics);
        verify(mockKafkaConsumer, never()).subscribe((Collection<String>) captor.capture());
    }

    @Test
    public void TestPollForMessagesForSpecificTopicSuccess(){
        KafkaConsumer<byte[], byte[]> mockKafkaConsumer = Mockito.mock(KafkaConsumer.class);


        String testTopic = "testTopic";

        ConsumerRecords mockedPollResult = getTestConsumerRecords(testTopic);

        when(mockKafkaConsumer.poll(any())).thenReturn(mockedPollResult);

        DistributionEngineConfiguration config = getMockDistributionEngineConfiguration();

        SdcKafkaConsumer sdcKafkaConsumer = new SdcKafkaConsumer(mockKafkaConsumer, config);

        List<String> returned = sdcKafkaConsumer.poll(testTopic);
        assertTrue(returned.size()==1);
        assertTrue(returned.contains("testTopicValue"));
    }

    @Test
    public void testSaslJaasConfigNotFound(){
        assertThrows(
            KafkaException.class,
            () ->  new SdcKafkaConsumer(setTestDistributionEngineConfigs()),
            "Sasl Jaas Config should not be found, so expected a KafkaException"
        );
    }

    @NotNull
    private DistributionEngineConfiguration getMockDistributionEngineConfiguration() {
        DistributionEngineConfiguration config = new DistributionEngineConfiguration();
        DistributionEngineConfiguration.DistributionStatusTopicConfig mockStatusTopic = new DistributionEngineConfiguration.DistributionStatusTopicConfig();
        mockStatusTopic.setPollingIntervalSec(1);
        config.setDistributionStatusTopic(mockStatusTopic);
        return config;
    }

    @NotNull
    private ConsumerRecords getTestConsumerRecords(String testTopics) {
        Map map = new HashMap<Integer, ConsumerRecord>();

        ConsumerRecord consumerRecord = new ConsumerRecord(testTopics, 0, 0, "", "testTopicValue");

        List<ConsumerRecord> consumerRecordList = new ArrayList<>();
        consumerRecordList.add(consumerRecord);
        TopicPartition topicPartition = new TopicPartition(testTopics, 0);
        map.put(topicPartition, consumerRecordList);

        ConsumerRecords mockedPollResult = new ConsumerRecords(map);
        return mockedPollResult;
    }

    private DistributionEngineConfiguration setTestDistributionEngineConfigs(){
        DistributionEngineConfiguration.DistributionStatusTopicConfig dsTopic = new DistributionEngineConfiguration.DistributionStatusTopicConfig();
        DistributionEngineConfiguration deConfiguration = new DistributionEngineConfiguration();
        String testBootstrapServers = "TestBootstrapServer";
        dsTopic.setConsumerGroup("consumerGroup");
        dsTopic.setConsumerId("consumerId");

        deConfiguration.setKafkaBootStrapServers(testBootstrapServers);
        deConfiguration.setDistributionStatusTopic(dsTopic);
        return deConfiguration;
    }
}
