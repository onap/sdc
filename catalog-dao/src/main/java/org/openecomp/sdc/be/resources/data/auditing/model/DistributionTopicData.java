package org.openecomp.sdc.be.resources.data.auditing.model;

public class DistributionTopicData {
    private String statusTopic;
    private String notificationTopic;

    private DistributionTopicData() {
    }

    public String getStatusTopic() {
        return statusTopic;
    }

    public String getNotificationTopic() {
        return notificationTopic;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {

        private final DistributionTopicData instance;

        public Builder() {
            instance = new DistributionTopicData();
        }

        public Builder statusTopic(String statusTopic) {
            this.instance.statusTopic = statusTopic;
            return this;
        }

        public Builder notificationTopic(String notificationTopic) {
            this.instance.notificationTopic = notificationTopic;
            return this;
        }

        public DistributionTopicData build() {
            return instance;
        }

    }
}
