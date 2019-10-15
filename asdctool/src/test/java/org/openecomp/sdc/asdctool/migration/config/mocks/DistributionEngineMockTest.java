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

package org.openecomp.sdc.asdctool.migration.config.mocks;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.openecomp.sdc.be.components.distribution.engine.INotificationData;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.resources.data.OperationalEnvironmentEntry;

@Ignore("This class does not test anything, there is not a single assertion and the code with reflection fails")
public class DistributionEngineMockTest {

    private DistributionEngineMock createTestSubject() {
        return new DistributionEngineMock();
    }

    @Test
    public void testIsActive() throws Exception {
        DistributionEngineMock testSubject;
        boolean result;

        // default test
        testSubject = createTestSubject();
        result = testSubject.isActive();
        Assert.assertNotNull(result);
    }

    @Test
    public void testNotifyService() throws Exception {
        DistributionEngineMock testSubject;
        String distributionId = "";
        Service service = null;
        INotificationData notificationData = null;
        String envName = "";
        String userId = "";
        ActionStatus result;
        User modifierName = new User();

        // default test
        testSubject = createTestSubject();
        result = testSubject.notifyService(distributionId, service, notificationData, envName, userId, modifierName);
        Assert.assertNotNull(result);
    }

    @Test
    public void testNotifyService_1() throws Exception {
        DistributionEngineMock testSubject;
        String distributionId = "";
        Service service = null;
        INotificationData notificationData = null;
        String envId = "";
        String envName = "";
        String userId = "";
        User modifierName = new User();
        ActionStatus result;

        // default test
        testSubject = createTestSubject();
        result = testSubject.notifyService(distributionId, service, notificationData, envId, envName,
            modifierName);
        Assert.assertNotNull(result);
    }

    @Test
    public void testIsEnvironmentAvailable() throws Exception {
        DistributionEngineMock testSubject;
        String envName = "";
        StorageOperationStatus result;

        // default test
        testSubject = createTestSubject();
        result = testSubject.isEnvironmentAvailable(envName);
        Assert.assertNotNull(result);
    }

    @Test
    public void testIsEnvironmentAvailable_1() throws Exception {
        DistributionEngineMock testSubject;
        StorageOperationStatus result;

        // default test
        testSubject = createTestSubject();
        result = testSubject.isEnvironmentAvailable();
        Assert.assertNotNull(result);
    }

    @Test
    public void testDisableEnvironment() throws Exception {
        DistributionEngineMock testSubject;
        String envName = "";

        // default test
        testSubject = createTestSubject();
        testSubject.disableEnvironment(envName);
    }

    @Test
    public void testIsReadyForDistribution() throws Exception {
        DistributionEngineMock testSubject;
        Service service = null;
        String envName = "";
        StorageOperationStatus result;

        // default test
        testSubject = createTestSubject();
        result = testSubject.isReadyForDistribution(envName);
        Assert.assertNotNull(result);
    }

    @Test
    public void testBuildServiceForDistribution() throws Exception {
        DistributionEngineMock testSubject;
        Service service = null;
        String distributionId = "";
        String workloadContext = "";
        INotificationData result;

        // default test
        testSubject = createTestSubject();
        result = testSubject.buildServiceForDistribution(service, distributionId, workloadContext);
        Assert.assertNotNull(result);
    }

    @Test
    public void testGetEnvironmentById() throws Exception {
        DistributionEngineMock testSubject;
        String opEnvId = "";
        OperationalEnvironmentEntry result;

        // default test
        testSubject = createTestSubject();
        result = testSubject.getEnvironmentById(opEnvId);
        Assert.assertNotNull(result);
    }
}
