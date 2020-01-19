package org.openecomp.sdc.common.log.elements;

public class ErrorLogOptionalData {
    private String targetEntity;
    private String targetServiceName;

    public ErrorLogOptionalData() {
    }

    String getTargetEntity() {
        return targetEntity;
    }

    private void setTargetEntity(String targetEntity) {
        this.targetEntity = targetEntity;
    }

    String getTargetServiceName() {
        return targetServiceName;
    }

    private void setTargetServiceName(String targetServiceName) {
        this.targetServiceName = targetServiceName;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {
        private final ErrorLogOptionalData instance;

        private Builder() {
            instance = new ErrorLogOptionalData();
        }

        public Builder targetEntity(String targetEntity) {
            instance.setTargetEntity(targetEntity);
            return this;
        }

        public Builder targetServiceName(String targetServiceName) {
            instance.setTargetServiceName(targetServiceName);
            return this;
        }

        public ErrorLogOptionalData build() {
            return instance;
        }
    }
}
