package org.openecomp.sdc.common.log.elements;

import org.slf4j.Logger;

/**
 * Created by dd4296 on 12/26/2017.
 * this factory helps decouple the classes for Stopwatch and LogFieldsMdcHandler from
 * the EcompLogger classes
 */
public class LoggerFactory {

    private LoggerFactory() {
    }

    @SuppressWarnings("unchecked")
    public static <T, V> V getLogger(Class<T> type, Logger logger) {

        if (type.isAssignableFrom(LoggerAudit.class)) {
            return (V) new LoggerAudit(new LogFieldsMdcHandler(), logger);
        }

        if (type.isAssignableFrom(LoggerDebug.class)) {
            return (V) new LoggerDebug(new LogFieldsMdcHandler(), logger);
        }

        if (type.isAssignableFrom(LoggerMetric.class)) {
            return (V) new LoggerMetric(new LogFieldsMdcHandler(), logger);
        }

        if (type.isAssignableFrom(LoggerError.class)) {
            return (V) new LoggerError(new LogFieldsMdcHandler(), logger);
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    public static <T, V> V getMdcLogger(Class<T> type, Logger logger) {

        if (type.isAssignableFrom(LoggerAudit.class)) {
            return (V) new LoggerAudit(LogFieldsMdcHandler.getInstance(), logger);
        }

        if (type.isAssignableFrom(LoggerDebug.class)) {
            return (V) new LoggerDebug(LogFieldsMdcHandler.getInstance(), logger);
        }

        if (type.isAssignableFrom(LoggerMetric.class)) {
            return (V) new LoggerMetric(LogFieldsMdcHandler.getInstance(), logger);
        }

        if (type.isAssignableFrom(LoggerError.class)) {
            return (V) new LoggerError(LogFieldsMdcHandler.getInstance(), logger);
        }

        if (type.getName().equals(LoggerSupportability.class.getName())) {
            return (V) new LoggerSupportability(LogFieldsMdcHandler.getInstance(), logger);
        }

        return null;
    }
}
