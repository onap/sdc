/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2020 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.be.components.health;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.LinkedList;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.onap.portalsdk.core.onboarding.exception.CipherUtilException;
import org.openecomp.sdc.be.catalog.impl.DmaapProducerHealth;
import org.openecomp.sdc.be.components.BeConfDependentTest;
import org.openecomp.sdc.be.components.distribution.engine.DistributionEngineClusterHealth;
import org.openecomp.sdc.be.components.distribution.engine.DmaapHealth;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.switchover.detector.SwitchoverDetector;
import org.openecomp.sdc.common.api.HealthCheckInfo;
import org.openecomp.sdc.common.http.client.api.HttpExecuteException;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.openecomp.sdc.common.impl.FSConfigurationSource;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class HealthCheckBusinessLogicHealthTest extends BeConfDependentTest {

    // TODO - remove this setup after migration to Junit5 BeConfDependentTest
    @BeforeAll
    private static void setup() {
        configurationManager =
            new ConfigurationManager(new FSConfigurationSource(ExternalConfiguration.getChangeListener(), "src/test/resources/config/catalog-be"));
    }

    private final DmaapProducerHealth dmaapProducerHealth = mock(DmaapProducerHealth.class);
    private final HealthCheckInfo dmaapProducerHealthCheckInfo = mock(HealthCheckInfo.class);

    private HealthCheckBusinessLogic createTestSubject() {

        HealthCheckBusinessLogic healthCheckBusinessLogic = new HealthCheckBusinessLogic();
        DmaapHealth dmaapHealth = new DmaapHealth();
        ReflectionTestUtils.setField(healthCheckBusinessLogic, "dmaapHealth", dmaapHealth);
        PortalHealthCheckBuilder portalHealthCheckBuilder = new PortalHealthCheckBuilder();
        ReflectionTestUtils.setField(healthCheckBusinessLogic, "portalHealthCheck", portalHealthCheckBuilder);
        DistributionEngineClusterHealth distributionEngineClusterHealth = new DistributionEngineClusterHealth();
        ReflectionTestUtils.setField(healthCheckBusinessLogic, "distributionEngineClusterHealth",
            distributionEngineClusterHealth);
        SwitchoverDetector switchoverDetector = new SwitchoverDetector();
        ReflectionTestUtils.setField(healthCheckBusinessLogic, "switchoverDetector", switchoverDetector);
        List<HealthCheckInfo> prevBeHealthCheckInfos = new LinkedList<>();
        ReflectionTestUtils.setField(healthCheckBusinessLogic, "prevBeHealthCheckInfos", prevBeHealthCheckInfos);
        ReflectionTestUtils.setField(healthCheckBusinessLogic, "dmaapProducerHealth", dmaapProducerHealth);
        return healthCheckBusinessLogic;
    }

    @BeforeEach
    private void beforeTest() {
        when(dmaapProducerHealth.getHealthCheckInfo())
            .thenReturn(dmaapProducerHealthCheckInfo);
    }

    @Test
    void testInit() throws Exception {
        HealthCheckBusinessLogic testSubject = createTestSubject();
        testSubject.init();
    }

    @Test
    void testIsDistributionEngineUp() throws Exception {
        HealthCheckBusinessLogic testSubject;
        // default test
        testSubject = createTestSubject();
        testSubject.isDistributionEngineUp();
    }

    @Test
    void testGetBeHealthCheckInfosStatus() throws Exception {
        HealthCheckBusinessLogic testSubject;

        // default test
        testSubject = createTestSubject();
        testSubject.getBeHealthCheckInfosStatus();
    }

    @Test
    void testGetJanusGraphHealthCheck() throws Exception {
        HealthCheckBusinessLogic testSubject;
        List<HealthCheckInfo> healthCheckInfos = new LinkedList<>();

        // default test
        testSubject = createTestSubject();
        healthCheckInfos.add(testSubject.getJanusGraphHealthCheck());
    }

    @Test
    void testGetPortalHealthCheckSuccess() throws Exception {
        PortalHealthCheckBuilder testSubject = spy(PortalHealthCheckBuilder.class);
        String healthCheckURL = testSubject.buildPortalHealthCheckUrl();
        int timeout = 3000;
        doReturn(200).when(testSubject).getStatusCode(healthCheckURL, timeout);
        testSubject.init();
        testSubject.runTask();
        HealthCheckInfo hci = testSubject.getHealthCheckInfo();
        Assertions.assertEquals("PORTAL", hci.getHealthCheckComponent());
        Assertions.assertEquals(HealthCheckInfo.HealthCheckStatus.UP, hci.getHealthCheckStatus());
        Assertions.assertEquals("OK", hci.getDescription());
    }

    @Test
    void testGetPortalHealthCheckFailureMissingConfig() throws Exception {
        PortalHealthCheckBuilder testSubject = new PortalHealthCheckBuilder();
        testSubject.init(null);
        HealthCheckInfo hci = testSubject.getHealthCheckInfo();
        Assertions.assertEquals("PORTAL", hci.getHealthCheckComponent());
        Assertions.assertEquals(HealthCheckInfo.HealthCheckStatus.DOWN, hci.getHealthCheckStatus());
        Assertions.assertEquals("PORTAL health check configuration is missing", hci.getDescription());
    }

    @Test
    void testGetPortalHealthCheckFailureErrorResponse() throws HttpExecuteException, CipherUtilException {
        PortalHealthCheckBuilder testSubject = spy(PortalHealthCheckBuilder.class);
        String healthCheckURL = testSubject.buildPortalHealthCheckUrl();
        int timeout = 3000;
        doReturn(404).when(testSubject).getStatusCode(healthCheckURL, timeout);
        testSubject.init(testSubject.getConfiguration());
        testSubject.runTask();
        HealthCheckInfo hci = testSubject.getHealthCheckInfo();
        Assertions.assertEquals("PORTAL", hci.getHealthCheckComponent());
        Assertions.assertEquals(HealthCheckInfo.HealthCheckStatus.DOWN, hci.getHealthCheckStatus());
        Assertions.assertEquals("PORTAL responded with 404 status code", hci.getDescription());
    }

    @Test
    void testGetPortalHealthCheckFailureNoResponse() throws HttpExecuteException, CipherUtilException {
        PortalHealthCheckBuilder testSubject = spy(PortalHealthCheckBuilder.class);
        String healthCheckURL = testSubject.buildPortalHealthCheckUrl();
        int timeout = 3000;
        doThrow(HttpExecuteException.class).when(testSubject).getStatusCode(healthCheckURL, timeout);
        testSubject.init(testSubject.getConfiguration());
        testSubject.runTask();
        HealthCheckInfo hci = testSubject.getHealthCheckInfo();
        Assertions.assertEquals("PORTAL", hci.getHealthCheckComponent());
        Assertions.assertEquals(HealthCheckInfo.HealthCheckStatus.DOWN, hci.getHealthCheckStatus());
        Assertions.assertEquals("PORTAL is not available", hci.getDescription());
    }

    @Test
    void testDestroy() throws Exception {
        HealthCheckBusinessLogic testSubject;

        // default test
        testSubject = createTestSubject();
        testSubject.init();
        testSubject.destroy();
    }

    @Test
    void testGetSiteMode() throws Exception {
        HealthCheckBusinessLogic testSubject;

        // default test
        testSubject = createTestSubject();
        testSubject.getSiteMode();
    }

    @Test
    void testAnyStatusChanged() throws Exception {
        HealthCheckBusinessLogic testSubject;
        List<HealthCheckInfo> beHealthCheckInfos = null;
        List<HealthCheckInfo> prevBeHealthCheckInfos = null;
        boolean result;

        // test 1
        testSubject = createTestSubject();
        beHealthCheckInfos = null;
        prevBeHealthCheckInfos = null;
        result = testSubject.anyStatusChanged(beHealthCheckInfos, prevBeHealthCheckInfos);
        Assertions.assertEquals(false, result);

        // test 2
        testSubject = createTestSubject();
        prevBeHealthCheckInfos = null;
        beHealthCheckInfos = null;
        result = testSubject.anyStatusChanged(beHealthCheckInfos, prevBeHealthCheckInfos);
        Assertions.assertEquals(false, result);

        // test 3
        testSubject = createTestSubject();
        beHealthCheckInfos = null;
        prevBeHealthCheckInfos = null;
        result = testSubject.anyStatusChanged(beHealthCheckInfos, prevBeHealthCheckInfos);
        Assertions.assertEquals(false, result);

        // test 4
        testSubject = createTestSubject();
        prevBeHealthCheckInfos = null;
        beHealthCheckInfos = null;
        result = testSubject.anyStatusChanged(beHealthCheckInfos, prevBeHealthCheckInfos);
        Assertions.assertEquals(false, result);
    }

}
