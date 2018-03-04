package org.openecomp.sdc.be.resources.data.auditing.model;

public class DistributionData {

    private final String consumerId;
    private final String resourceUrl;

    public DistributionData(String consumerId, String resourceUrl) {
        this.consumerId = consumerId;
        this.resourceUrl = resourceUrl;
    }

    public String getConsumerId() {
        return consumerId;
    }

    public String getResourceUrl() {
        return resourceUrl;
    }
}
