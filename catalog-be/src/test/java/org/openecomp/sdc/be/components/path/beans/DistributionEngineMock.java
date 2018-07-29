package org.openecomp.sdc.be.components.path.beans;

import org.openecomp.sdc.be.components.distribution.engine.IDistributionEngine;
import org.openecomp.sdc.be.components.distribution.engine.INotificationData;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.resources.data.OperationalEnvironmentEntry;

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

}
