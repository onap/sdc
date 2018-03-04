package org.openecomp.sdc.be.externalapi.servlet.representation;

/**
 * Created by chaya on 10/26/2017.
 */
public class ServiceDistributionRespInfo {

    private String distributionId;

    public ServiceDistributionRespInfo() {
    }

    public ServiceDistributionRespInfo(String distributionId) {
        this.distributionId = distributionId;
    }

    public String getDistributionId() {
        return distributionId;
    }

    public void setDistributionId(String distributionId) {
        this.distributionId = distributionId;
    }
}
