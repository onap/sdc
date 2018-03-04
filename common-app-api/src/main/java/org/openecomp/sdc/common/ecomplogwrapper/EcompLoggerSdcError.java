package org.openecomp.sdc.common.ecomplogwrapper;

import org.openecomp.sdc.common.ecomplog.EcompLoggerError;
import org.openecomp.sdc.common.ecomplog.Enums.EcompLoggerErrorCode;
import org.openecomp.sdc.common.ecomplog.Enums.LogLevel;

public class EcompLoggerSdcError {

    public void log(LogLevel errorLevel,
                    EcompLoggerErrorCode errorCodeEnum,
                    String uuid,
                    String errorDescription,
                    String partnerName,
                    String targetEntity,
                    String message) {

        EcompLoggerError.getInstance()
                .clear()
                .startTimer()
                .setErrorDescription(errorDescription)
                .setErrorCode(errorCodeEnum)
                .setKeyRequestId(uuid)
                .setServiceName(partnerName)
                .setTargetEntity(targetEntity)
                .log(errorLevel, message);
    }
}
