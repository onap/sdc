package org.openecomp.sdc.common.ecomplog;

/**
 * Created by dd4296 on 12/26/2017.
 * this factory helps decouple the classes for Stopwatch and EcompMDCWrapper from
 * the EcompLogger classes
 */
public class EcompLoggerFactory {

    private EcompLoggerFactory() {
    }

    @SuppressWarnings("unchecked")
    static public <T, V> V getLogger(Class<T> type) {

        if (type.getName().equals(EcompLoggerAudit.class.getName())) {
            return (V) new EcompLoggerAudit(new EcompMDCWrapper(new Stopwatch()));
        }

        if (type.getName().equals(EcompLoggerDebug.class.getName())) {
            return (V) new EcompLoggerDebug(new EcompMDCWrapper(new Stopwatch()));
        }

        if (type.getName().equals(EcompLoggerMetric.class.getName())) {
            return (V) new EcompLoggerMetric(new EcompMDCWrapper(new Stopwatch()));
        }

        if (type.getName().equals(EcompLoggerError.class.getName())) {
            return (V) new EcompLoggerError(new EcompMDCWrapper(new Stopwatch()));
        }

        return null;
    }
}
