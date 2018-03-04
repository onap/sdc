package org.openecomp.sdc.be.resources.data.auditing.model;

public class OperationalEnvAuditData {
    private String envId;
    private String vnfWorkloadContext;
    private String tenant;

    public String getEnvId() {
        return envId;
    }

    public String getVnfWorkloadContext() {
        return vnfWorkloadContext;
    }

    public String getTenant() {
        return tenant;
    }

    public OperationalEnvAuditData(String envId, String vnfWorkloadContext, String tenant) {
        this.envId = envId;
        this.vnfWorkloadContext = vnfWorkloadContext;
        this.tenant = tenant;
   }

}
