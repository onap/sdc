package org.openecomp.sdc.be.resources.data.auditing.model;

public class CommonAuditData {
    private String description;
    private String requestId;
    private String serviceInstanceId;
    private String status;

    private CommonAuditData() {
        //for builder
    }

    public String getStatus() {
        return status;
    }

    public String getDescription() {
        return description;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getRequestId() {
        return requestId;
    }

    public String getServiceInstanceId() {
        return serviceInstanceId;
    }

    public void setServiceInstanceId(String serviceInstanceId) {
        this.serviceInstanceId = serviceInstanceId ;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {
        private final CommonAuditData instance;

        private Builder() {
            instance = new CommonAuditData();
        }

        public Builder description(String description) {
            instance.description = description;
            return this;
        }

        public Builder status(int status) {
            instance.status = String.valueOf(status);
            return this;
        }

        public Builder status(String status) {
            instance.status = status;
            return this;
        }

        public Builder requestId(String requestId) {
            instance.requestId = requestId;
            return this;
        }

        public Builder serviceInstanceId(String serviceInstanceId) {
            instance.serviceInstanceId = serviceInstanceId;
            return this;
        }

        public CommonAuditData build() {
            return instance;
        }

    }

}
