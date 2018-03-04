package org.openecomp.sdc.common.ecomplogwrapper;

import java.util.Arrays;

import org.openecomp.sdc.common.ecomplog.EcompLoggerDebug;
import org.openecomp.sdc.common.ecomplog.Enums.LogLevel;

public class EcompLoggerSdcDebug {

    public void log(LogLevel errorLevel,
                    String uuid,
                    String message,
                    Exception ex) {

        StringBuilder stackTrack = new StringBuilder();
        Arrays.asList(ex.getStackTrace()).forEach(item -> stackTrack.append(item.toString()).append("\n"));

        EcompLoggerDebug.getInstance()
                .clear()
                .startTimer()
                .setKeyRequestId(uuid)
                .log(errorLevel, String.format("%s %s", message, stackTrack.toString()));
    }

    public void log(LogLevel errorLevel,
                    String uuid,
                    String message) {

        EcompLoggerDebug.getInstance()
                .clear()
                .startTimer()
                .setKeyRequestId(uuid)
                .log(errorLevel, message);
    }
}
