package org.openecomp.sdc.common.log.enums;

public enum StatusCode {
    ERROR("ERROR"),
    COMPLETE("COMPLETE");

    String statusCode;

    StatusCode(String statusCode){this.statusCode = statusCode;}

    public String getStatusCodeEnum(){
        return statusCode;
    }
}
