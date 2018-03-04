package org.openecomp.sdc.common.ecomplog.Enums;

public enum StatusCode {
    ERROR("ERROR"),
    COMPLETE("COMPLETE");

    String statusCode;

    StatusCode(String statusCode){this.statusCode = statusCode;}

    public String getStatusCodeEnum(){
        return statusCode;
    }
}
