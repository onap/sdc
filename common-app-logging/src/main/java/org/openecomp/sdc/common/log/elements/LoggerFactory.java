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

        if (type.getName().equals(LoggerAudit.class.getName())) {
            return (V) new LoggerAudit(new LogFieldsMdcHandler(), logger);
        }

        if (type.getName().equals(LoggerDebug.class.getName())) {
            return (V) new LoggerDebug(new LogFieldsMdcHandler(), logger);
        }

        if (type.getName().equals(LoggerMetric.class.getName())) {
            return (V) new LoggerMetric(new LogFieldsMdcHandler(), logger);
        }

        if (type.getName().equals(LoggerError.class.getName())) {
            return (V) new LoggerError(new LogFieldsMdcHandler(), logger);
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    public static <T, V> V getMdcLogger(Class<T> type, Logger logger) {

        if (type.getName().equals(LoggerAudit.class.getName())) {
            return (V) new LoggerAudit(LogFieldsMdcHandler.getInstance(), logger);
        }

        if (type.getName().equals(LoggerDebug.class.getName())) {
            return (V) new LoggerDebug(LogFieldsMdcHandler.getInstance(), logger);
        }

        if (type.getName().equals(LoggerMetric.class.getName())) {
            return (V) new LoggerMetric(LogFieldsMdcHandler.getInstance(), logger);
        }

        if (type.getName().equals(LoggerError.class.getName())) {
            return (V) new LoggerError(LogFieldsMdcHandler.getInstance(), logger);
        }

        if (type.getName().equals(LoggerSupportability.class.getName())) {
            return (V) new LoggerSupportability(LogFieldsMdcHandler.getInstance(), logger);
        }

        return null;
    }
}
