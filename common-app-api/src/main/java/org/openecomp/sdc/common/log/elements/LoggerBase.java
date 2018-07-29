package org.openecomp.sdc.common.log.elements;

import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.lang3.StringUtils;
import org.openecomp.sdc.common.log.api.ILogFieldsHandler;
import org.openecomp.sdc.common.log.api.ILogger;
import org.openecomp.sdc.common.log.enums.LogLevel;
import org.openecomp.sdc.common.log.enums.LogMarkers;
import org.openecomp.sdc.common.util.ThreadLocalsHolder;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.util.Arrays;
import java.util.List;


/**
 * Created by mm288v on 12/27/2017.
 * This class holds the common behavior of all Loger-Typed classes.
 * The Concrete loggers shoudl derive from this one.
 */
public abstract class LoggerBase implements ILogger {
    private final Logger myLogger;
    private final Marker myMarker;
    protected final ILogFieldsHandler ecompLogFieldsHandler;
    private final static String missingLogFieldsMsg = "mandatory parameters for ECOMP logging, missing fields: %s, original message: %s";

    LoggerBase(ILogFieldsHandler ecompLogFieldsHandler, Marker marker, Logger logger) {
        this.ecompLogFieldsHandler = ecompLogFieldsHandler;
        this.myMarker = marker;
        this.myLogger = logger;
        setKeyRequestIdIfNotSetYet();
    }

    protected void setKeyRequestIdIfNotSetYet() {
        if (StringUtils.isEmpty(ecompLogFieldsHandler.getKeyRequestId())) {
            setKeyRequestId(ThreadLocalsHolder.getUuid());
        }
    }
    private void validateMandatoryFields(String originMsg) {
        // this method only checks if the mandatory fields have been initialized
        String filedNameThatHasNotBeenInitialized = checkMandatoryFieldsExistInMDC();

        if (myLogger.isDebugEnabled() && !"".equalsIgnoreCase(filedNameThatHasNotBeenInitialized)) {
            myLogger.debug(MarkerFactory.getMarker(LogMarkers.DEBUG_MARKER.text()),
                    String.format(missingLogFieldsMsg, filedNameThatHasNotBeenInitialized, originMsg));
        }
    }

    @VisibleForTesting
    String checkMandatoryFieldsExistInMDC() {
        // this method returns a String of uninitialised fields
        StringBuilder missingFields = new StringBuilder();
        getMandatoryFields().forEach(field -> {
            if (ecompLogFieldsHandler.isMDCParamEmpty(field)) {
                missingFields.append(field).append(" ");
            }
        });
        return missingFields.toString();
    }

    public abstract List<String> getMandatoryFields();

    protected String convertExceptionStackToString(Exception ex) {
        StringBuilder stackTrack = new StringBuilder();
        Arrays.asList(ex.getStackTrace()).forEach(item -> stackTrack.append(item.toString()).append("\n"));
        return stackTrack.toString();
    }

    @Override
    public void log(LogLevel logLevel, String message) {
        log(logLevel, message, (Object) null);
    }

    @Override
    public void log(LogLevel logLevel, String message, Object...params) {
        validateMandatoryFields(message);

        switch(logLevel) {
            case ERROR:
            case FATAL:  //TODO check how to log "FATAL" word
                myLogger.error(myMarker, message, params);
                break;
            case WARN:
                myLogger.warn(myMarker, message, params);
                break;
            case INFO:
                myLogger.info(myMarker, message, params);
                break;
            case DEBUG:
                myLogger.debug(myMarker, message, params);
                break;
            case TRACE:
                myLogger.trace(myMarker, message, params);
                break;
            default:
                break;
        }
    }

    @Override
    public void log(LogLevel logLevel, String message, Throwable throwable) {
        validateMandatoryFields(message);

        switch(logLevel) {
            case ERROR:
            case FATAL:  //TODO check how to log "FATAL" word
                myLogger.error(myMarker, createErrorMessage(message, throwable));
                break;
            case WARN:
                myLogger.warn(myMarker, createErrorMessage(message, throwable));
                break;
            case INFO:
                myLogger.info(myMarker, createErrorMessage(message, throwable));
                break;
            case DEBUG:
                myLogger.debug(myMarker, message, throwable);
                break;
            case TRACE:
                myLogger.trace(myMarker, message, throwable);
                break;
            default:
                break;
        }
    }

    protected String createErrorMessage(String message, Throwable throwable) {
        return String.format("%s: %s", message, throwable.getLocalizedMessage());
    }


    @Override
    public ILogger clear() {
        ecompLogFieldsHandler.clear();
        return this;
    }

    @Override
    public ILogger setKeyRequestId(String keyRequestId) {
        ecompLogFieldsHandler.setKeyRequestId(keyRequestId);
        return this;
    }

}
