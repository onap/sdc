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

package org.openecomp.sdc.be.components.distribution.engine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.be.components.kafka.KafkaHandler;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.config.DistributionEngineConfiguration;
import org.openecomp.sdc.be.config.DistributionEngineConfiguration.ComponentArtifactTypesConfig;
import org.openecomp.sdc.be.config.DistributionEngineConfiguration.CreateTopicConfig;
import org.openecomp.sdc.be.config.DistributionEngineConfiguration.DistributionStatusTopicConfig;
import org.openecomp.sdc.common.api.ConfigurationSource;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.openecomp.sdc.common.impl.FSConfigurationSource;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

class DistributionEngineConfigTest {

    @Mock
    private KafkaHandler kafkaHandler;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        ExternalConfiguration.setAppName("catalog-be");
        ExternalConfiguration.setConfigDir("src/test/resources/config");
        ExternalConfiguration.listenForChanges();

        new ConfigurationManager(new FSConfigurationSource(ExternalConfiguration.getChangeListener(), ExternalConfiguration.getConfigDir() + File.separator + ExternalConfiguration.getAppName()));
    }

    @Test
    void validateMissingEnvironments() {

        when(kafkaHandler.isKafkaActive()).thenReturn(false);

        DistributionEngineConfiguration deConfiguration = new DistributionEngineConfiguration();

        String uebPublicKey = "uebPublicKey";
        String uebSecretKey = "uebSecretKey";

        DistributionEngine distributionEngine = new DistributionEngine();
        distributionEngine.setKafkaHandler(kafkaHandler);
        List<String> environments = new ArrayList<>();
        environments.add("PROD");
        deConfiguration.setEnvironments(environments);

        List<String> servers = new ArrayList<>();
        servers.add("server1:80");
        servers.add("server2:8080");

        CreateTopicConfig createTopic = new CreateTopicConfig();
        createTopic.setPartitionCount(1);
        createTopic.setReplicationCount(1);
        deConfiguration.setCreateTopic(createTopic);

        ComponentArtifactTypesConfig distribNotifResourceArtifactTypes = new ComponentArtifactTypesConfig();
        deConfiguration.setDistribNotifResourceArtifactTypes(distribNotifResourceArtifactTypes);

        ComponentArtifactTypesConfig distribNotifServiceArtifactTypes = new ComponentArtifactTypesConfig();
        deConfiguration.setDistribNotifServiceArtifactTypes(distribNotifServiceArtifactTypes);

        deConfiguration.setDistributionNotifTopicName("distributionNotifTopicName");
        deConfiguration.setDistributionStatusTopicName("statusTopic");
        deConfiguration.setDistributionDeleteTopicName("distributionDeleteTopicName");

        DistributionStatusTopicConfig distributionStatusTopic = new DistributionStatusTopicConfig();
        distributionStatusTopic.setConsumerGroup("asdc-group");
        distributionStatusTopic.setConsumerId("asdc-id");
        distributionStatusTopic.setFetchTimeSec(20);
        distributionStatusTopic.setPollingIntervalSec(20);
        deConfiguration.setDistributionStatusTopic(distributionStatusTopic);

        deConfiguration.setUebServers(servers);
        deConfiguration.setUebPublicKey(uebPublicKey);
        deConfiguration.setUebSecretKey(uebSecretKey);
        deConfiguration.setInitMaxIntervalSec(8);
        deConfiguration.setInitRetryIntervalSec(3);

        boolean isValid = distributionEngine.validateConfiguration(deConfiguration);
        assertTrue(isValid, "check empty configuration");

        deConfiguration.setUebServers(null);
        isValid = distributionEngine.validateConfiguration(deConfiguration);
        assertFalse(isValid, "check empty configuration");

        deConfiguration.setUebServers(servers);
        isValid = distributionEngine.validateConfiguration(deConfiguration);
        assertTrue(isValid, "check empty configuration");

        deConfiguration.setEnvironments(null);
        isValid = distributionEngine.validateConfiguration(deConfiguration);
        assertFalse(isValid, "check empty configuration");

        deConfiguration.setEnvironments(environments);
        isValid = distributionEngine.validateConfiguration(deConfiguration);
        assertTrue(isValid, "check empty configuration");

        deConfiguration.setUebPublicKey(null);
        isValid = distributionEngine.validateConfiguration(deConfiguration);
        assertFalse(isValid, "check empty configuration");

        deConfiguration.setUebPublicKey(uebPublicKey);
        isValid = distributionEngine.validateConfiguration(deConfiguration);
        assertTrue(isValid, "check empty configuration");

        deConfiguration.setUebSecretKey(null);
        isValid = distributionEngine.validateConfiguration(deConfiguration);
        assertFalse(isValid, "check empty configuration");

        deConfiguration.setUebSecretKey(uebPublicKey);
        isValid = distributionEngine.validateConfiguration(deConfiguration);
        assertTrue(isValid, "check empty configuration");

        deConfiguration.setDistributionNotifTopicName(null);
        isValid = distributionEngine.validateConfiguration(deConfiguration);
        assertFalse(isValid, "check empty configuration");

        deConfiguration.setDistributionNotifTopicName(uebPublicKey);
        isValid = distributionEngine.validateConfiguration(deConfiguration);
        assertTrue(isValid, "check empty configuration");

        deConfiguration.setDistributionStatusTopicName(null);
        isValid = distributionEngine.validateConfiguration(deConfiguration);
        assertFalse(isValid, "check empty configuration");

        deConfiguration.setDistributionStatusTopicName(uebPublicKey);
        isValid = distributionEngine.validateConfiguration(deConfiguration);
        assertTrue(isValid, "check empty configuration");

        deConfiguration.setInitMaxIntervalSec(null);
        isValid = distributionEngine.validateConfiguration(deConfiguration);
        assertFalse(isValid, "check empty configuration");

        deConfiguration.setInitMaxIntervalSec(8);
        isValid = distributionEngine.validateConfiguration(deConfiguration);
        assertTrue(isValid, "check empty configuration");

        deConfiguration.setInitRetryIntervalSec(null);
        isValid = distributionEngine.validateConfiguration(deConfiguration);
        assertFalse(isValid, "check empty configuration");

        deConfiguration.setInitRetryIntervalSec(8);
        isValid = distributionEngine.validateConfiguration(deConfiguration);
        assertTrue(isValid, "check empty configuration");

    }

}
