package org.openecomp.sdc.common.log.api;

import org.openecomp.sdc.common.log.enums.LogLevel;
import org.slf4j.Marker;

import java.util.List;

/**
 * Created by dd4296 on 12/24/2017.
 */
public interface ILogger {
    void log(LogLevel logLevel, String message);
    void log(Marker marker, LogLevel logLevel, String message);
    void log(LogLevel logLevel, String message, Object... params);
    void log(Marker marker, LogLevel logLevel, String message, Object... params);
    void log(LogLevel logLevel, String message, Throwable throwable);
    List<String> getMandatoryFields();
    ILogger clear();
    ILogger startTimer();
    ILogger setKeyRequestId(String keyRequestId);

    ILogger setKeyInvocationId(String keyInvocationId);
}
