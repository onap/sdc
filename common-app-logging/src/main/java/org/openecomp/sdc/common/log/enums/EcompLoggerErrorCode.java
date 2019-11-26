package org.openecomp.sdc.common.log.enums;

import java.util.Arrays;
import java.util.Optional;

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

    private int errorCode;

    EcompLoggerErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public static EcompLoggerErrorCode getByValue(String ecompErrorCode) {
        String errorPrefix = parseCode(ecompErrorCode);
        Optional<EcompLoggerErrorCode> optionalCode = Arrays.stream(values()).filter(v->isCode(v, errorPrefix)).findFirst();
        return optionalCode.orElse(UNKNOWN_ERROR);
    }

    private static boolean isCode(EcompLoggerErrorCode ecompLoggerErrorCode, String errorPrefix) {
        return String.valueOf(ecompLoggerErrorCode.getErrorCode()).contains(errorPrefix);
    }

    private static String parseCode(String errorCode) {
        try {
            return errorCode.substring("E_".length(), 3);
        } catch (StringIndexOutOfBoundsException ex) {
            return UNKNOWN_ERROR.name();
        }
    }


}
