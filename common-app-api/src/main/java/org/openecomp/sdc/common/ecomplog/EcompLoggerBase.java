package org.openecomp.sdc.common.ecomplog;

import static org.openecomp.sdc.common.ecomplog.api.IEcompLogConfiguration.MDC_SERVER_FQDN;
import static org.openecomp.sdc.common.ecomplog.api.IEcompLogConfiguration.MDC_SERVER_IP_ADDRESS;

import org.openecomp.sdc.common.ecomplog.Enums.LogLevel;
import org.openecomp.sdc.common.ecomplog.api.IEcompLogger;
import org.openecomp.sdc.common.ecomplog.api.IEcompMdcWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.slf4j.Marker;


/**
 * Created by mm288v on 12/27/2017.
 * This class holds the common behavior of all Loger-Typed classes.
 * The Concrete loggers shoudl derive from this one.
 */
public abstract class EcompLoggerBase implements IEcompLogger{
    private static Logger myLogger = LoggerFactory.getLogger(IEcompLogger.class.getName());
    private Marker myMarker;
    IEcompMdcWrapper ecompMdcWrapper;

     EcompLoggerBase(IEcompMdcWrapper ecompMdcWrapper, Marker marker) {
        this.ecompMdcWrapper = ecompMdcWrapper;
         initializeMandatoryFields();
         this.myMarker = marker;
    }

    @Override
    public void log(LogLevel errorLevel, String message) {

        ecompMdcWrapper.validateMandatoryFields();

        if (this instanceof EcompLoggerAudit || this instanceof EcompLoggerMetric) {
            MDC.put(MDC_SERVER_IP_ADDRESS, EcompMDCWrapper.getInstance().getHostAddress());
            MDC.put(MDC_SERVER_FQDN, EcompMDCWrapper.getInstance().getFqdn());
        }

        if (errorLevel.equals(LogLevel.ERROR)) {
            myLogger.error(myMarker, message);
            return;
        }

        if (errorLevel.equals(LogLevel.WARN)) {
            myLogger.warn(myMarker, message);
            return;
        }

        if (errorLevel.equals(LogLevel.INFO)) {
            myLogger.info(myMarker, message);
            return;
        }

        if (errorLevel.equals(LogLevel.DEBUG)) {
            myLogger.info(myMarker, message);
        }
    }

    @Override
    public IEcompLogger clear() {
        ecompMdcWrapper.clear();
        return this;
    }

    @Override
    public IEcompLogger startTimer() {
        ecompMdcWrapper.startTimer();
        return this;
    }

    @Override
    public IEcompLogger setKeyRequestId(String keyRequestId) {
        ecompMdcWrapper.setKeyRequestId(keyRequestId);
        return this;
    }

}
