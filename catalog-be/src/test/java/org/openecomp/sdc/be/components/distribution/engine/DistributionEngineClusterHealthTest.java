/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2026 Deutsche Telekom AG. All rights reserved.
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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openecomp.sdc.be.components.BeConfDependentTest;
import org.openecomp.sdc.be.components.kafka.KafkaClusterProbe;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.config.DistributionEngineConfiguration;
import org.openecomp.sdc.common.api.HealthCheckInfo.HealthCheckStatus;

class DistributionEngineClusterHealthTest extends BeConfDependentTest {

    private final KafkaClusterProbe probe = mock(KafkaClusterProbe.class);
    private final DistributionEngineClusterHealth health = new DistributionEngineClusterHealth(probe);
    private final BeEcompErrorManager errorManager = mock(BeEcompErrorManager.class);
    private BeEcompErrorManager realErrorManager;

    private DistributionEngineClusterHealth createTestSubject() {
        return new DistributionEngineClusterHealth(probe);
    }

    @BeforeEach
    void replaceErrorManager() throws ReflectiveOperationException {
        realErrorManager = swapErrorManager(errorManager);
    }

    @AfterEach
    void restoreErrorManager() throws ReflectiveOperationException {
        swapErrorManager(realErrorManager);
        health.destroy();
    }

    private static BeEcompErrorManager swapErrorManager(BeEcompErrorManager replacement) throws ReflectiveOperationException {
        Field instance = BeEcompErrorManager.class.getDeclaredField("instance");
        instance.setAccessible(true);
        BeEcompErrorManager previous = (BeEcompErrorManager) instance.get(null);
        instance.set(null, replacement);
        return previous;
    }

    @Test
    void probeFailureFlipsUpToDown() {
        when(probe.isClusterReachable(any())).thenReturn(true, false);
        health.runHealthCheck(() -> true);
        assertEquals(HealthCheckStatus.UP, health.getHealthCheckInfo().getHealthCheckStatus());
        verify(errorManager).logBeHealthCheckUebClusterRecovery(anyString());
        health.runHealthCheck(() -> true);
        assertEquals(HealthCheckStatus.DOWN, health.getHealthCheckInfo().getHealthCheckStatus());
        verify(errorManager).logBeHealthCheckUebClusterError(anyString());
    }

    @Test
    void probeOkButListenerNotRunningIsDown() {
        when(probe.isClusterReachable(any())).thenReturn(true);
        health.runHealthCheck(() -> false);
        assertEquals(HealthCheckStatus.DOWN, health.getHealthCheckInfo().getHealthCheckStatus());
        verify(errorManager, never()).logBeHealthCheckUebClusterRecovery(anyString());
    }

    @Test
    void repeatedProbeResultsRaiseOneAlarm() {
        when(probe.isClusterReachable(any())).thenReturn(false);
        health.runHealthCheck(() -> true);
        health.runHealthCheck(() -> true);
        verify(errorManager, times(1)).logBeHealthCheckUebClusterError(anyString());
    }

    @Test
    void probeExceptionIsDownAndDoesNotEscape() {
        when(probe.isClusterReachable(any())).thenThrow(new IllegalStateException("boom"));
        health.runHealthCheck(() -> true);
        assertEquals(HealthCheckStatus.DOWN, health.getHealthCheckInfo().getHealthCheckStatus());
    }

    @Test
    void scheduledTaskProbesWithTheConfiguredTimeout() {
        when(probe.isClusterReachable(any())).thenReturn(true);
        health.startHealthCheckTask(() -> true);
        verify(probe, timeout(5_000).atLeastOnce()).isClusterReachable(Duration.ofSeconds(10));
    }

    @Test
    void listenerCheckRunsEvenWhenTheProbeFails() {
        when(probe.isClusterReachable(any())).thenReturn(false);
        AtomicBoolean checked = new AtomicBoolean();
        health.runHealthCheck(() -> checked.getAndSet(true));
        assertTrue(checked.get());
    }

    @Test
    void nonPositiveIntervalAndTimeoutFallBackToTheDefaults() {
        DistributionEngineConfiguration config = ConfigurationManager.getConfigurationManager().getDistributionEngineConfiguration();
        Integer interval = config.getKafkaHealthCheckIntervalSec();
        Integer probeTimeout = config.getKafkaHealthCheckTimeoutSec();
        config.setKafkaHealthCheckIntervalSec(0);
        config.setKafkaHealthCheckTimeoutSec(-1);
        try {
            when(probe.isClusterReachable(any())).thenReturn(true);
            assertDoesNotThrow(() -> health.startHealthCheckTask(() -> true));
            verify(probe, timeout(5_000).atLeastOnce()).isClusterReachable(Duration.ofSeconds(10));
        } finally {
            config.setKafkaHealthCheckIntervalSec(interval);
            config.setKafkaHealthCheckTimeoutSec(probeTimeout);
        }
    }

    // TODO - move to BeConfDependentTest after migration to Junit 5
    @BeforeAll
    public static void setupBeforeClass() {
        componentName = "catalog-be";
        confPath = "src/test/resources/config";
        setUp();
    }

    @Test
    void testGetHealthCheckInfoIsUnknownUntilReported() {
        assertEquals(HealthCheckStatus.UNKNOWN, createTestSubject().getHealthCheckInfo().getHealthCheckStatus());
    }

    @Test
    void testSetHealthCheckUebIsDisabled() {
        DistributionEngineClusterHealth testSubject = createTestSubject();
        testSubject.setHealthCheckUebIsDisabled();
        assertEquals(HealthCheckStatus.DOWN, testSubject.getHealthCheckInfo().getHealthCheckStatus());
    }

    @Test
    void testSetHealthCheckUebConfigurationError() {
        DistributionEngineClusterHealth testSubject = createTestSubject();
        testSubject.setHealthCheckUebConfigurationError();
        assertEquals(HealthCheckStatus.DOWN, testSubject.getHealthCheckInfo().getHealthCheckStatus());
    }

    @Test
    void testSetHealthCheckOkAndReportInCaseLastStateIsDown() {
        DistributionEngineClusterHealth testSubject = createTestSubject();
        testSubject.setHealthCheckOkAndReportInCaseLastStateIsDown();
        assertEquals(HealthCheckStatus.UP, testSubject.getHealthCheckInfo().getHealthCheckStatus());
    }

    @Test
    void testSetHealthCheckUnavailableAfterOk() {
        DistributionEngineClusterHealth testSubject = createTestSubject();
        testSubject.setHealthCheckOkAndReportInCaseLastStateIsDown();
        testSubject.setHealthCheckUnavailableAndReportInCaseLastStateIsUp();
        assertEquals(HealthCheckStatus.DOWN, testSubject.getHealthCheckInfo().getHealthCheckStatus());
        testSubject.setHealthCheckOkAndReportInCaseLastStateIsDown();
        assertEquals(HealthCheckStatus.UP, testSubject.getHealthCheckInfo().getHealthCheckStatus());
    }
}
