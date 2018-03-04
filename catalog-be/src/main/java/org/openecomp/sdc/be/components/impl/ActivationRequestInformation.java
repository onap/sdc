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
