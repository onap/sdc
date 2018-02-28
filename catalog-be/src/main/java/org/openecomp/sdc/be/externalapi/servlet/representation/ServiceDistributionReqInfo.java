package org.openecomp.sdc.be.externalapi.servlet.representation;

/**
 * Created by chaya on 10/26/2017.
 */
public class ServiceDistributionReqInfo {

    private String workloadContext;

    public ServiceDistributionReqInfo() {
    }
    public ServiceDistributionReqInfo(String workloadContext) {
        this.workloadContext = workloadContext;
    }

    public String getWorkloadContext() {
        return workloadContext;
    }

    public void setWorkloadContext(String workloadContext) {
        this.workloadContext = workloadContext;
    }
}
