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

package org.openecomp.sdc.be.components.impl;

import org.openecomp.sdc.be.model.Service;

/**
 * Created by chaya on 10/22/2017.
 */
public class ActivationRequestInformation {
    private Service serviceToActivate;
    private String workloadContext;
    private String tenant;

    public ActivationRequestInformation(Service serviceToActivate, String workloadContext, String tenant) {
        this.serviceToActivate = serviceToActivate;
        this.workloadContext = workloadContext;
        this.tenant = tenant;
    }

    public String getTenant() {
        return tenant;
    }

    public void setTenant(String tenant) {
        this.tenant = tenant;
    }

    public Service getServiceToActivate() {
        return serviceToActivate;
    }

    public void setServiceToActivate(Service serviceToActivate) {
        this.serviceToActivate = serviceToActivate;
    }

    public String getWorkloadContext() {
        return workloadContext;
    }

    public void setWorkloadContext(String workloadContext) {
        this.workloadContext = workloadContext;
    }
}
