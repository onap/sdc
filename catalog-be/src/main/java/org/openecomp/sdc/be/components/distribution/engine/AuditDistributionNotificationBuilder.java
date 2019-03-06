package org.openecomp.sdc.be.components.distribution.engine;

import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;

public class AuditDistributionNotificationBuilder {
    
    private String topicName; 
    private String distributionId; 
    private CambriaErrorResponse status; 
    private Service service; 
    private String envId; 
    private User modifier    ; 
    private String workloadContext; 
    private String tenant;

    public String getTopicName() {
        return topicName;
    }

    public AuditDistributionNotificationBuilder setTopicName(String topicName) {
        this.topicName = topicName;
        return this;
    }

    public String getDistributionId() {
        return distributionId;
    }

    public AuditDistributionNotificationBuilder setDistributionId(String distributionId) {
        this.distributionId = distributionId;
        return this;
    }

    public CambriaErrorResponse getStatus() {
        return status;
    }

    public AuditDistributionNotificationBuilder setStatus(CambriaErrorResponse status) {
        this.status = status;
        return this;
    }

    public Service getService() {
        return service;
    }

    public AuditDistributionNotificationBuilder setService(Service service) {
        this.service = service;
        return this;
    }

    public String getEnvId() {
        return envId;
    }

    public AuditDistributionNotificationBuilder setEnvId(String envId) {
        this.envId = envId;
        return this;
    }

    public User getModifier() {
        return modifier;
    }

    public AuditDistributionNotificationBuilder setModifier(User modifier) {
        this.modifier = modifier;
        return this;
    }

    public String getWorkloadContext() {
        return workloadContext;
    }

    public AuditDistributionNotificationBuilder setWorkloadContext(String workloadContext) {
        this.workloadContext = workloadContext;
        return this;
    }

    public String getTenant() {
        return tenant;
    }

    public AuditDistributionNotificationBuilder setTenant(String tenant) {
        this.tenant = tenant;
        return this;
    }
}
