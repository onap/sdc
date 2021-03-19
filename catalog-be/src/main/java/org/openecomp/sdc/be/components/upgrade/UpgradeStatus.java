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
package org.openecomp.sdc.be.components.upgrade;

import java.util.ArrayList;
import java.util.List;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.exception.ResponseFormat;

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
        ServiceInfo info = new ServiceInfo(serviceId, status);
        componentToUpgradeStatus.add(info);
    }

    public void addServiceStatus(Component component, ActionStatus status) {
        checkAndCreate();
        ServiceInfo info = new ServiceInfo(component.getUniqueId(), status);
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
