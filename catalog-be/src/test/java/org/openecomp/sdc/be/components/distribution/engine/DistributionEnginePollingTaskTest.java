/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import fj.data.Either;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import mockit.Deencapsulation;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.openecomp.sdc.be.components.BeConfDependentTest;
import org.openecomp.sdc.be.components.distribution.engine.report.DistributionCompleteReporter;
import org.openecomp.sdc.be.components.kafka.KafkaHandler;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.config.DistributionEngineConfiguration;
import org.openecomp.sdc.be.distribution.api.client.CambriaOperationStatus;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.resources.data.OperationalEnvironmentEntry;
import org.openecomp.sdc.common.api.HealthCheckInfo.HealthCheckStatus;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.openecomp.sdc.common.impl.FSConfigurationSource;
import org.openecomp.sdc.common.log.wrappers.LoggerSdcAudit;

class DistributionEnginePollingTaskTest extends BeConfDependentTest {

    // TODO - remove this setup after migration to Junit5 BeConfDependentTest
    @BeforeAll
    static void setup() {
        configurationManager =
            new ConfigurationManager(new FSConfigurationSource(ExternalConfiguration.getChangeListener(), "src/test/resources/config/catalog-be"));
    }

    @Mock
    private ComponentsUtils componentsUtils;

    private static final String TOPIC = "StatusTopic";
    private static final String VALID_MESSAGE =
        "{\"distributionID\":\"dist-1\",\"consumerID\":\"consumer-1\",\"timestamp\":1,"
            + "\"artifactURL\":\"/url\",\"status\":\"DOWNLOAD_OK\"}";

    private final KafkaHandler kafkaHandler = Mockito.mock(KafkaHandler.class);
    private final DistributionEngineClusterHealth clusterHealth = new DistributionEngineClusterHealth();

    private DistributionEnginePollingTask createKafkaTestSubject() {
        componentsUtils = Mockito.mock(ComponentsUtils.class);
        when(kafkaHandler.isKafkaActive()).thenReturn(true);
        DistributionEnginePollingTask task = new DistributionEnginePollingTask(configurationManager.getDistributionEngineConfiguration(),
            new DistributionCompleteReporterMock(), componentsUtils, clusterHealth, new OperationalEnvironmentEntry(), kafkaHandler);
        Deencapsulation.setField(task, "topicName", TOPIC);
        return task;
    }

    private DistributionEnginePollingTask createTestSubject() {
        componentsUtils = Mockito.mock(ComponentsUtils.class);
        DistributionEngineConfiguration distributionEngineConfiguration = configurationManager
            .getDistributionEngineConfiguration();
        distributionEngineConfiguration.setDistributionNotifTopicName("StamName");
        distributionEngineConfiguration.setDistributionStatusTopicName("StamName");
        List uebList = new LinkedList<>();
        uebList.add("FirstUEBserver.com");
        distributionEngineConfiguration.setUebServers(uebList);

        OperationalEnvironmentEntry environmentEntry = new OperationalEnvironmentEntry();
        HashSet<String> dmaapUebAddress = new HashSet<>();
        dmaapUebAddress.add("STAM");
        environmentEntry.setDmaapUebAddress(dmaapUebAddress);
        return new DistributionEnginePollingTask(distributionEngineConfiguration,
            new DistributionCompleteReporterMock(), componentsUtils, new DistributionEngineClusterHealth(), environmentEntry);
    }

    @Test
    void testStartTask() throws Exception {
        DistributionEnginePollingTask testSubject;
        String topicName = "UEBTopic";

        // default test
        testSubject = createTestSubject();
        testSubject.startTask(topicName);
    }

    @Test
    void testStopTask() throws Exception {
        DistributionEnginePollingTask testSubject;

        // default test
        testSubject = createTestSubject();
        testSubject.stopTask();
    }

    @Test
    void testDestroy() throws Exception {
        DistributionEnginePollingTask testSubject;

        // default test
        testSubject = createTestSubject();
        testSubject.destroy();
    }

    @Test
    void testRun() throws Exception {
        DistributionEnginePollingTask testSubject;

        // default test
        testSubject = createTestSubject();
        testSubject.run();
    }

    @Test
    void testHandleDistributionNotificationMsg() throws Exception {
        DistributionEnginePollingTask testSubject;
        final DistributionStatusNotification notification = new DistributionStatusNotification();
        notification.setDistributionID("mock");
        notification.setConsumerID("mock");
        notification.setArtifactURL("mock");
        notification.setTimestamp(435435);
        notification.setStatus(DistributionStatusNotificationEnum.ALREADY_DEPLOYED);
        notification.setErrorReason("mock");

        // default test
        testSubject = createTestSubject();
        Mockito.doNothing().when(componentsUtils).auditDistributionStatusNotification(Mockito.anyString(),
            Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
            Mockito.anyString(), Mockito.anyString(), Mockito.isNull());

        final Class[] argClasses = {DistributionStatusNotification.class, LoggerSdcAudit.class};
        final Method method = DistributionEnginePollingTask.class.getDeclaredMethod("handleDistributionNotificationMsg", argClasses);
        method.setAccessible(true);
        method.invoke(testSubject, notification, new LoggerSdcAudit(DistributionEnginePollingTask.class));
    }

    @Test
    void testRunSkipsNonJsonMessageAndHandlesTheRest() {
        DistributionEnginePollingTask testSubject = createKafkaTestSubject();
        when(kafkaHandler.fetchFromTopic(TOPIC)).thenReturn(Either.left(Arrays.asList("not json {", VALID_MESSAGE)));

        assertDoesNotThrow(testSubject::run);

        verify(componentsUtils, times(1)).auditDistributionStatusNotification(eq("dist-1"), eq("consumer-1"), eq(TOPIC), eq("/url"),
            eq("1"), eq("DOWNLOAD_OK"), any(), any());
    }

    @Test
    void testRunSkipsMessageWithUnknownStatus() {
        DistributionEnginePollingTask testSubject = createKafkaTestSubject();
        when(kafkaHandler.fetchFromTopic(TOPIC)).thenReturn(Either.left(Arrays.asList(
            "{\"distributionID\":\"dist-1\",\"status\":\"NOT_A_STATUS\"}", "null", VALID_MESSAGE)));

        assertDoesNotThrow(testSubject::run);

        verify(componentsUtils, times(1)).auditDistributionStatusNotification(anyString(), any(), any(), any(), any(), anyString(), any(),
            any());
    }

    @Test
    void testRunSkipsMessageWithoutDistributionId() {
        DistributionEnginePollingTask testSubject = createKafkaTestSubject();
        when(kafkaHandler.fetchFromTopic(TOPIC)).thenReturn(Either.left(List.of("{\"status\":\"DOWNLOAD_OK\"}")));

        assertDoesNotThrow(testSubject::run);

        verify(componentsUtils, never()).auditDistributionStatusNotification(any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void testRunSurvivesUnexpectedFetchFailure() {
        DistributionEnginePollingTask testSubject = createKafkaTestSubject();
        when(kafkaHandler.fetchFromTopic(TOPIC)).thenThrow(new IllegalStateException("consumer closed"));

        assertDoesNotThrow(testSubject::run);
        assertEquals(HealthCheckStatus.DOWN, clusterHealth.getHealthCheckInfo().getHealthCheckStatus());
    }

    @Test
    void testRunMarksHealthDownOnFetchErrorAndUpOnNextSuccess() {
        DistributionEnginePollingTask testSubject = createKafkaTestSubject();
        when(kafkaHandler.fetchFromTopic(TOPIC))
            .thenReturn(Either.right(new CambriaErrorResponse(CambriaOperationStatus.INTERNAL_SERVER_ERROR, 500)))
            .thenReturn(Either.left(List.of()));

        testSubject.run();
        assertEquals(HealthCheckStatus.DOWN, clusterHealth.getHealthCheckInfo().getHealthCheckStatus());

        testSubject.run();
        assertEquals(HealthCheckStatus.UP, clusterHealth.getHealthCheckInfo().getHealthCheckStatus());
    }

    private class DistributionCompleteReporterMock implements DistributionCompleteReporter {

        @Override
        public void reportDistributionComplete(DistributionStatusNotification distributionStatusNotification) {
            // TODO Auto-generated method stub
        }

    }
}
