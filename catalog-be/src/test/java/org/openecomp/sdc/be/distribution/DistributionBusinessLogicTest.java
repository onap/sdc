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

package org.openecomp.sdc.be.distribution;

import fj.data.Either;
import org.junit.Test;
import org.openecomp.sdc.be.components.BeConfDependentTest;
import org.openecomp.sdc.be.config.DistributionEngineConfiguration;
import org.openecomp.sdc.be.distribution.api.client.KafkaDataResponse;
import org.openecomp.sdc.exception.ResponseFormat;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DistributionBusinessLogicTest extends BeConfDependentTest {

    private final DistributionBusinessLogic distributionBusinessLogic = new DistributionBusinessLogic();

    @Test
    public void getKafkaDataReturnsBootstrapServerAndTopicsOfTheFirstEnvironment() {
        DistributionEngineConfiguration config = configurationManager.getDistributionEngineConfiguration();
        String env = config.getEnvironments().get(0).toUpperCase();

        Either<KafkaDataResponse, ResponseFormat> result = distributionBusinessLogic.getKafkaData();

        assertTrue(result.isLeft());
        KafkaDataResponse kafkaData = result.left().value();
        assertEquals(config.getKafkaBootStrapServers(), kafkaData.getKafkaBootStrapServer());
        assertEquals(config.getDistributionNotifTopicName() + "-" + env, kafkaData.getDistrNotificationTopicName());
        assertEquals(config.getDistributionStatusTopicName() + "-" + env, kafkaData.getDistrStatusTopicName());
    }

    @Test
    public void getKafkaDataFailsWithoutBootstrapServers() {
        DistributionEngineConfiguration config = configurationManager.getDistributionEngineConfiguration();
        String bootstrapServers = config.getKafkaBootStrapServers();
        config.setKafkaBootStrapServers(null);
        try {
            Either<KafkaDataResponse, ResponseFormat> result = distributionBusinessLogic.getKafkaData();
            assertTrue(result.isRight());
            assertEquals(500, result.right().value().getStatus().intValue());
        } finally {
            config.setKafkaBootStrapServers(bootstrapServers);
        }
    }
}
