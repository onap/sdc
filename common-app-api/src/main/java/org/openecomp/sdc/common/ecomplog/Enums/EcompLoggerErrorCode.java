package org.openecomp.sdc.common.ecomplog.Enums;

/**
 * Created by dd4296 on 12/26/2017.
 */
public enum EcompLoggerErrorCode {
    SUCCESS(0),
    PERMISSION_ERROR(100),
    AVAILABILITY_TIMEOUTS_ERROR(200),
    DATA_ERROR(300),
    SCHEMA_ERROR(400),
    BUSINESS_PROCESS_ERROR(500),
    UNKNOWN_ERROR(900);

    int errorCode;

    EcompLoggerErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public int getErrorCode() {
        return errorCode;
    }
}
