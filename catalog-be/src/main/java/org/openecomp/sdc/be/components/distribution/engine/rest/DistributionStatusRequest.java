package org.openecomp.sdc.be.components.distribution.engine.rest;

/**
 * a class which represents an MSO distribution status rest request body
 */
public class DistributionStatusRequest {

    private String status;
    private String errorReason;

    public DistributionStatusRequest(String status, String errorReason) {
        this.status = status;
        this.errorReason = errorReason;
    }

    public String getStatus() {
        return status;
    }

    public String getErrorReason() {
        return errorReason;
    }
}
