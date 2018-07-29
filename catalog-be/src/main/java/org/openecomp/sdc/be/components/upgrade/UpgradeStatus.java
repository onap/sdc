package org.openecomp.sdc.be.components.upgrade;

import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.exception.ResponseFormat;

import java.util.ArrayList;
import java.util.List;

public class UpgradeStatus {
    ActionStatus status;
    ResponseFormat error;
    
    
    List<ServiceInfo> componentToUpgradeStatus;

    public ActionStatus getStatus() {
        return status;
    }

    public void setStatus(ActionStatus status) {
        this.status = status;
    }

    public List<ServiceInfo> getComponentToUpgradeStatus() {
        return componentToUpgradeStatus;
    }

    public void setComponentToUpgradeStatus(List<ServiceInfo> componentToUpgradeStatus) {
        this.componentToUpgradeStatus = componentToUpgradeStatus;
    }

    public void addServiceStatus(ServiceInfo info) {
        checkAndCreate();
        componentToUpgradeStatus.add(info);
    }
    public void addServiceStatus(String serviceId, ActionStatus status) {
        checkAndCreate();
        ServiceInfo info = new ServiceInfo(serviceId, status );
        componentToUpgradeStatus.add(info);
    }
    public void addServiceStatus(Component component, ActionStatus status) {
        checkAndCreate();
        ServiceInfo info = new ServiceInfo(component.getUniqueId(), status );
        info.setName(component.getName());
        info.setVersion(component.getVersion());
        componentToUpgradeStatus.add(info);
    }

    private void checkAndCreate() {
        if (componentToUpgradeStatus == null) {
            componentToUpgradeStatus = new ArrayList<>();
        }
    }

    public ResponseFormat getError() {
        return error;
    }

    public void setError(ResponseFormat error) {
        this.error = error;
    }
    
}
