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

import fj.data.Either;
import org.apache.commons.collections.CollectionUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.openecomp.sdc.be.components.kafka.KafkaHandler;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.config.DistributionEngineConfiguration;
import org.openecomp.sdc.be.config.DistributionEngineConfiguration.CreateTopicConfig;
import org.openecomp.sdc.be.distribution.api.client.CambriaOperationStatus;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.resources.data.OperationalEnvironmentEntry;
import org.openecomp.sdc.common.api.ConfigurationSource;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.openecomp.sdc.common.impl.FSConfigurationSource;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

class DistributionEngineInitTaskTest {

    private ComponentsUtils componentsUtils;

    private CambriaHandler cambriaHandler;

    private KafkaHandler kafkaHandler;

    @BeforeEach
    public void setup() {
        ExternalConfiguration.setAppName("catalog-be");
        ExternalConfiguration.setConfigDir("src/test/resources/config");
        ExternalConfiguration.listenForChanges();

        ConfigurationSource configurationSource = new FSConfigurationSource(ExternalConfiguration.getChangeListener(), ExternalConfiguration.getConfigDir() + File.separator + ExternalConfiguration.getAppName());
        new ConfigurationManager(configurationSource);

        componentsUtils = Mockito.mock(ComponentsUtils.class);
        cambriaHandler = Mockito.mock(CambriaHandler.class);
        kafkaHandler = Mockito.mock(KafkaHandler.class);
    }

    @Test
    void checkIncrement() {

        String envName = "PrOD";

        DistributionEngineConfiguration deConfiguration = new DistributionEngineConfiguration();
        int retry = 2;
        int maxRetry = 40;
        deConfiguration.setInitRetryIntervalSec(retry);
        deConfiguration.setInitMaxIntervalSec(maxRetry);
        DistributionEngineInitTask initTask = new DistributionEngineInitTask(0l, deConfiguration, envName, new AtomicBoolean(false), componentsUtils, null, readEnvFromConfig(deConfiguration));

        for (int i = 1; i < 5; i++) {
            initTask.incrementRetryInterval();
            assertEquals("check next retry interval", initTask.getCurrentRetryInterval(), retry * (long) Math.pow(2, i));
        }

        initTask.incrementRetryInterval();
        assertEquals("check next retry interval reach max retry interval", initTask.getCurrentRetryInterval(), maxRetry);

    }

    @Test
    void checkStartTask() {

        String envName = "PrOD";

        DistributionEngineConfiguration deConfiguration = new DistributionEngineConfiguration();
        int retry = 2;
        int maxRetry = 40;
        deConfiguration.setInitRetryIntervalSec(retry);
        deConfiguration.setInitMaxIntervalSec(maxRetry);
        DistributionEngineInitTask initTask = new DistributionEngineInitTask(0l, deConfiguration, envName, new AtomicBoolean(false), componentsUtils, null, readEnvFromConfig(deConfiguration));

        initTask.startTask();
    }

    @Test
    void checkRestartTask() {

        String envName = "PrOD";

        DistributionEngineConfiguration deConfiguration = new DistributionEngineConfiguration();
        int retry = 2;
        int maxRetry = 40;
        deConfiguration.setInitRetryIntervalSec(retry);
        deConfiguration.setInitMaxIntervalSec(maxRetry);
        DistributionEngineInitTask initTask = new DistributionEngineInitTask(0l, deConfiguration, envName, new AtomicBoolean(false), componentsUtils, null, readEnvFromConfig(deConfiguration));

        initTask.restartTask();
    }

    @Test
    void checkStopTask() {

        String envName = "PrOD";

        DistributionEngineConfiguration deConfiguration = new DistributionEngineConfiguration();
        int retry = 2;
        int maxRetry = 40;
        deConfiguration.setInitRetryIntervalSec(retry);
        deConfiguration.setInitMaxIntervalSec(maxRetry);
        DistributionEngineInitTask initTask = new DistributionEngineInitTask(0l, deConfiguration, envName, new AtomicBoolean(false), componentsUtils, null, readEnvFromConfig(deConfiguration));

        initTask.stopTask();
        initTask.startTask();
        initTask.stopTask();
    }

    @Test
    void checkDestroy() {

        String envName = "PrOD";

        DistributionEngineConfiguration deConfiguration = new DistributionEngineConfiguration();
        int retry = 2;
        int maxRetry = 40;
        deConfiguration.setInitRetryIntervalSec(retry);
        deConfiguration.setInitMaxIntervalSec(maxRetry);
        DistributionEngineInitTask initTask = new DistributionEngineInitTask(0l, deConfiguration, envName, new AtomicBoolean(false), componentsUtils, null, readEnvFromConfig(deConfiguration));

        initTask.destroy();
    }

    @Test
    void checkRun() {

    	String notifTopic = "notif";
        String statusTopic = "status";

        List<String> uebServers = new ArrayList<>();
        uebServers.add("server1");
        CambriaErrorResponse cambriaErrorResponse = new CambriaErrorResponse(CambriaOperationStatus.NOT_FOUND);
        Either<Set<String>, CambriaErrorResponse> right = Either.right(cambriaErrorResponse);
        when(cambriaHandler.getTopics(Mockito.any(List.class))).thenReturn(right);

        String envName = "PrOD";

        DistributionEngineConfiguration deConfiguration = new DistributionEngineConfiguration();
        deConfiguration.setUebServers(uebServers);
        int retry = 2;
        int maxRetry = 40;
        deConfiguration.setInitRetryIntervalSec(retry);
        deConfiguration.setInitMaxIntervalSec(maxRetry);
        deConfiguration.setDistributionNotifTopicName(notifTopic);
        deConfiguration.setDistributionStatusTopicName(statusTopic);
        CreateTopicConfig createTopic = new CreateTopicConfig();
        createTopic.setPartitionCount(1);
        createTopic.setReplicationCount(1);
        deConfiguration.setCreateTopic(createTopic);

        cambriaErrorResponse = new CambriaErrorResponse(CambriaOperationStatus.OK);

        String realNotifTopic = notifTopic + "-" + envName.toUpperCase();
        String realStatusTopic = statusTopic + "-" + envName.toUpperCase();
        when(cambriaHandler.createTopic(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.eq(realNotifTopic), Mockito.eq(1), Mockito.eq(1))).thenReturn(cambriaErrorResponse);
        when(cambriaHandler.createTopic(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.eq(realStatusTopic), Mockito.eq(1), Mockito.eq(1))).thenReturn(cambriaErrorResponse);

        cambriaErrorResponse = new CambriaErrorResponse(CambriaOperationStatus.OK);
        when(cambriaHandler.registerToTopic(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(cambriaErrorResponse);

        DistributionEngineInitTask initTask = new DistributionEngineInitTask(0l, deConfiguration, envName, new AtomicBoolean(false), componentsUtils, null, readEnvFromConfig(deConfiguration));
        initTask.setCambriaHandler(cambriaHandler);

        boolean initFlow = initTask.initFlow();

        initTask.run();
    }

    @Test
    void testInitFlowScenarioSuccess() {

        String notifTopic = "notif";
        String statusTopic = "status";

        List<String> uebServers = new ArrayList<>();
        uebServers.add("server1");
        CambriaErrorResponse cambriaErrorResponse = new CambriaErrorResponse(CambriaOperationStatus.NOT_FOUND);
        Either<Set<String>, CambriaErrorResponse> right = Either.right(cambriaErrorResponse);
        when(cambriaHandler.getTopics(Mockito.any(List.class))).thenReturn(right);

        String envName = "PrOD";

        DistributionEngineConfiguration deConfiguration = new DistributionEngineConfiguration();
        deConfiguration.setUebServers(uebServers);
        int retry = 2;
        int maxRetry = 40;
        deConfiguration.setInitRetryIntervalSec(retry);
        deConfiguration.setInitMaxIntervalSec(maxRetry);
        deConfiguration.setDistributionNotifTopicName(notifTopic);
        deConfiguration.setDistributionStatusTopicName(statusTopic);
        CreateTopicConfig createTopic = new CreateTopicConfig();
        createTopic.setPartitionCount(1);
        createTopic.setReplicationCount(1);
        deConfiguration.setCreateTopic(createTopic);

        cambriaErrorResponse = new CambriaErrorResponse(CambriaOperationStatus.OK);

        String realNotifTopic = notifTopic + "-" + envName.toUpperCase();
        String realStatusTopic = statusTopic + "-" + envName.toUpperCase();
        when(cambriaHandler.createTopic(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.eq(realNotifTopic), Mockito.eq(1), Mockito.eq(1))).thenReturn(cambriaErrorResponse);
        when(cambriaHandler.createTopic(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.eq(realStatusTopic), Mockito.eq(1), Mockito.eq(1))).thenReturn(cambriaErrorResponse);

        cambriaErrorResponse = new CambriaErrorResponse(CambriaOperationStatus.OK);
        when(cambriaHandler.registerToTopic(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(cambriaErrorResponse);

        DistributionEngineInitTask initTask = new DistributionEngineInitTask(0l, deConfiguration, envName, new AtomicBoolean(false), componentsUtils, null, readEnvFromConfig(deConfiguration));
        initTask.setCambriaHandler(cambriaHandler);

        boolean initFlow = initTask.initFlow();
        assertTrue("check init flow succeed", initFlow);

    }

    @Test
    void testInitFlowSuccessKafkaEnabled(){
        DistributionEngineConfiguration config = new DistributionEngineConfiguration();
        config.setInitRetryIntervalSec(1);
        config.setInitMaxIntervalSec(1);

        when(kafkaHandler.isKafkaActive()).thenReturn(true);
        DistributionEngineInitTask initTask = new DistributionEngineInitTask(0l, config, null, new AtomicBoolean(false), componentsUtils, null, null);
        initTask.setKafkaHandler(kafkaHandler);

        try {
            boolean initFlow = initTask.initFlow();
            assertTrue("check init flow succeed", initFlow);
        } catch (Exception e) {
            fail("Should not throw exception");
        }
    }

    @Test
    void testInitFlowScenarioSuccessTopicsAlreadyExists() {

        String envName = "PrOD";
        String notifTopic = "notif";
        String statusTopic = "status";

        String realNotifTopic = notifTopic + "-" + envName.toUpperCase();
        String realStatusTopic = statusTopic + "-" + envName.toUpperCase();

        Set<String> topics = new HashSet<>();
        topics.add(realNotifTopic);
        topics.add(realStatusTopic);

        List<String> uebServers = new ArrayList<>();
        uebServers.add("server1");
        Either<Set<String>, CambriaErrorResponse> left = Either.left(topics);

        when(cambriaHandler.getTopics(Mockito.any(List.class))).thenReturn(left);

        DistributionEngineConfiguration deConfiguration = new DistributionEngineConfiguration();
        deConfiguration.setUebServers(uebServers);
        int retry = 2;
        int maxRetry = 40;
        deConfiguration.setInitRetryIntervalSec(retry);
        deConfiguration.setInitMaxIntervalSec(maxRetry);
        deConfiguration.setDistributionNotifTopicName(notifTopic);
        deConfiguration.setDistributionStatusTopicName(statusTopic);
        CreateTopicConfig createTopic = new CreateTopicConfig();
        createTopic.setPartitionCount(1);
        createTopic.setReplicationCount(1);
        deConfiguration.setCreateTopic(createTopic);

        CambriaErrorResponse cambriaErrorResponse = new CambriaErrorResponse(CambriaOperationStatus.OK);
        when(cambriaHandler.registerToTopic(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(cambriaErrorResponse);

        DistributionEngineInitTask initTask = new DistributionEngineInitTask(0l, deConfiguration, envName, new AtomicBoolean(false), componentsUtils, null, readEnvFromConfig(deConfiguration));
        initTask.setCambriaHandler(cambriaHandler);

        try {
            boolean initFlow = initTask.initFlow();
            assertTrue("check init flow succeed", initFlow);
        } catch (Exception e) {
            fail("Should not throw exception");
        }

    }

    @Test
    void testInitFlowScenarioFailToRegister() {

        String notifTopic = "notif";
        String statusTopic = "status";

        List<String> uebServers = new ArrayList<>();
        uebServers.add("server1");
        CambriaErrorResponse cambriaErrorResponse = new CambriaErrorResponse(CambriaOperationStatus.NOT_FOUND);
        Either<Set<String>, CambriaErrorResponse> right = Either.right(cambriaErrorResponse);
        when(cambriaHandler.getTopics(Mockito.any(List.class))).thenReturn(right);

        String envName = "PrOD";

        DistributionEngineConfiguration deConfiguration = new DistributionEngineConfiguration();
        deConfiguration.setUebServers(uebServers);
        int retry = 2;
        int maxRetry = 40;
        deConfiguration.setInitRetryIntervalSec(retry);
        deConfiguration.setInitMaxIntervalSec(maxRetry);
        deConfiguration.setDistributionNotifTopicName(notifTopic);
        deConfiguration.setDistributionStatusTopicName(statusTopic);
        CreateTopicConfig createTopic = new CreateTopicConfig();
        createTopic.setPartitionCount(1);
        createTopic.setReplicationCount(1);
        deConfiguration.setCreateTopic(createTopic);

        cambriaErrorResponse = new CambriaErrorResponse(CambriaOperationStatus.OK);

        String realNotifTopic = notifTopic + "-" + envName.toUpperCase();
        String realStatusTopic = statusTopic + "-" + envName.toUpperCase();
        when(cambriaHandler.createTopic(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.eq(realNotifTopic), Mockito.eq(1), Mockito.eq(1))).thenReturn(cambriaErrorResponse);
        when(cambriaHandler.createTopic(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.eq(realStatusTopic), Mockito.eq(1), Mockito.eq(1))).thenReturn(cambriaErrorResponse);

        when(cambriaHandler.registerToTopic(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.eq(realNotifTopic)))
                .thenReturn(new CambriaErrorResponse(CambriaOperationStatus.OK));

        when(cambriaHandler.registerToTopic(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.eq(realStatusTopic)))
                .thenReturn(new CambriaErrorResponse(CambriaOperationStatus.CONNNECTION_ERROR));


        DistributionEngineInitTask initTask = new DistributionEngineInitTask(0l, deConfiguration, envName, new AtomicBoolean(false), componentsUtils, null, readEnvFromConfig(deConfiguration));
        initTask.setCambriaHandler(cambriaHandler);

        boolean initFlow = initTask.initFlow();
        assertFalse("check init flow failed", initFlow);

    }

    @Test
    void testInitFlowScenario1GetTopicsFailed() {

        List<String> uebServers = new ArrayList<>();
        uebServers.add("server1");
        CambriaErrorResponse cambriaErrorResponse = new CambriaErrorResponse(CambriaOperationStatus.CONNNECTION_ERROR);
        Either<Set<String>, CambriaErrorResponse> right = Either.right(cambriaErrorResponse);
        when(cambriaHandler.getTopics(Mockito.any(List.class))).thenReturn(right);

        String envName = "PrOD";

        DistributionEngineConfiguration deConfiguration = new DistributionEngineConfiguration();
        deConfiguration.setUebServers(uebServers);
        int retry = 2;
        int maxRetry = 40;
        deConfiguration.setInitRetryIntervalSec(retry);
        deConfiguration.setInitMaxIntervalSec(maxRetry);
        DistributionEngineInitTask initTask = new DistributionEngineInitTask(0l, deConfiguration, envName, new AtomicBoolean(false), componentsUtils, null, readEnvFromConfig(deConfiguration));
        initTask.setCambriaHandler(cambriaHandler);

        boolean initFlow = initTask.initFlow();
        assertFalse("check init flow failed", initFlow);

    }

    private OperationalEnvironmentEntry readEnvFromConfig(DistributionEngineConfiguration distributionEngineConfiguration) {
        OperationalEnvironmentEntry entry = new OperationalEnvironmentEntry();
        entry.setUebApikey(distributionEngineConfiguration.getUebPublicKey());
        entry.setUebSecretKey(distributionEngineConfiguration.getUebSecretKey());
        Set<String> puebEndpoints = new HashSet<>();
        if(distributionEngineConfiguration.getUebServers() != null)
            puebEndpoints.addAll(distributionEngineConfiguration.getUebServers());
        entry.setDmaapUebAddress(puebEndpoints);
        String envName = "UNKNOWN";
        if(CollectionUtils.isNotEmpty(distributionEngineConfiguration.getEnvironments()))
            envName = distributionEngineConfiguration.getEnvironments().get(0);
        entry.setEnvironmentId(envName);
        return entry;
    }

}
