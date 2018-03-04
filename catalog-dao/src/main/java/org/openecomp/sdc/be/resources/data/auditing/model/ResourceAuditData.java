package org.openecomp.sdc.be.resources.data.auditing.model;

public class ResourceAuditData {
    private String artifactUuid;
    private String state;
    private String version;
    private String distributionStatus;

    private ResourceAuditData() {
        //for builder
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public String getArtifactUuid() {
        return artifactUuid;
    }
    public String getState() {
        return state;
    }
    public String getVersion() {
        return version;
    }
    public String getDistributionStatus() { return distributionStatus; }


    public static class Builder {
        private final ResourceAuditData instance;

        private Builder() {
            instance = new ResourceAuditData();
        }

        public Builder artifactUuid(String artifactUuid) {
            instance.artifactUuid = artifactUuid;
            return this;
        }

        public Builder state(String state) {
            instance.state = state;
            return this;
        }

        public Builder version(String version) {
            instance.version = version;
            return this;
        }

        public Builder distributionStatus(String distributionStatus) {
            instance.distributionStatus = distributionStatus;
            return this;
        }

        public ResourceAuditData build() {
            return instance;
        }
    }
}
