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

import static org.junit.jupiter.api.Assertions.assertEquals;

import mockit.Deencapsulation;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openecomp.sdc.be.components.BeConfDependentTest;
import org.openecomp.sdc.common.api.HealthCheckInfo.HealthCheckStatus;

class DistributionEngineClusterHealthTest extends BeConfDependentTest {

    private DistributionEngineClusterHealth createTestSubject() {
        return new DistributionEngineClusterHealth();
    }

    // TODO - move to BeConfDependentTest after migration to Junit 5
    @BeforeAll
    public static void setupBeforeClass() {
        componentName = "catalog-be";
        confPath = "src/test/resources/config";
        setUp();
    }

    @Test
    void testLogAlarm() throws Exception {
        DistributionEngineClusterHealth testSubject;
        boolean lastHealthState = false;

        // default test
        testSubject = createTestSubject();
        Deencapsulation.invoke(testSubject, "logAlarm", new Object[]{lastHealthState});
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
