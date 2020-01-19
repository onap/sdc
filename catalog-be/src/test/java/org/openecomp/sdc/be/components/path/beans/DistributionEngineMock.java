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

package org.openecomp.sdc.be.components.path.beans;

import org.openecomp.sdc.be.components.distribution.engine.IDistributionEngine;
import org.openecomp.sdc.be.components.distribution.engine.INotificationData;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.resources.data.OperationalEnvironmentEntry;

import java.util.List;

public class DistributionEngineMock implements IDistributionEngine {
    @Override
    public boolean isActive() {
        return false;
    }

    @Override
    public ActionStatus notifyService(String distributionId, Service service, INotificationData notificationData, String envName, User modifier) {
        return null;
    }

    @Override
    public ActionStatus notifyService(String distributionId, Service service, INotificationData notificationData, String envId, String envName, User modifier) {
        return null;
    }

    @Override
    public StorageOperationStatus isEnvironmentAvailable(String envName) {
        return null;
    }

    @Override
    public StorageOperationStatus isEnvironmentAvailable() {
        return null;
    }

    @Override
    public void disableEnvironment(String envName) {

    }

    @Override
    public StorageOperationStatus isReadyForDistribution(String envName) {
        return null;
    }

    @Override
    public INotificationData buildServiceForDistribution(Service service, String distributionId, String workloadContext) {
        return null;
    }

    @Override
    public OperationalEnvironmentEntry getEnvironmentById(String opEnvId) {
        return null;
    }

    @Override
    public OperationalEnvironmentEntry getEnvironmentByDmaapUebAddress(List<String> dmaapUebAddress) {
        return null;
    }

}
