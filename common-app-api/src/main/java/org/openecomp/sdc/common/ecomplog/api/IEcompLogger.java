package org.openecomp.sdc.common.ecomplog.api;

import org.openecomp.sdc.common.ecomplog.Enums.LogLevel;

/**
 * Created by dd4296 on 12/24/2017.
 */
public interface IEcompLogger {
    void log(LogLevel errorLevel, String message);
    void initializeMandatoryFields();
    IEcompLogger clear();
    IEcompLogger startTimer();
    IEcompLogger setKeyRequestId(String keyRequestId);
}
