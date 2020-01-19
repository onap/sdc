package org.openecomp.sdc.common.log.elements;

import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.lang3.StringUtils;
import org.onap.logging.ref.slf4j.ONAPLogConstants;
import org.openecomp.sdc.common.log.api.ILogFieldsHandler;
import org.openecomp.sdc.common.log.api.ILogger;
import org.openecomp.sdc.common.log.enums.ConstantsLogging;
import org.openecomp.sdc.common.log.enums.LogLevel;
import org.openecomp.sdc.common.log.enums.LogMarkers;
import org.openecomp.sdc.common.log.utils.LoggingThreadLocalsHolder;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;


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

    public static String generateKeyRequestId() {
        return UUID.randomUUID().toString();
    }

    public static String getRequestId(HttpServletRequest httpRequest) {
        String onapRequestId = httpRequest.getHeader(ONAPLogConstants.Headers.REQUEST_ID);
        String requestId = httpRequest.getHeader(ConstantsLogging.X_REQUEST_ID);
        String transactionReId = httpRequest.getHeader(ConstantsLogging.X_TRANSACTION_ID_HEADER);
        String ecompRequestId = httpRequest.getHeader(ConstantsLogging.X_ECOMP_REQUEST_ID_HEADER);
        return Arrays.asList(onapRequestId, requestId, transactionReId, ecompRequestId).stream()
                .filter(id -> !StringUtils.isEmpty(id)).findFirst().orElse(generateKeyRequestId());
    }

    public static String getPartnerName(HttpServletRequest httpRequest) {
        String userId = httpRequest.getHeader(ConstantsLogging.USER_ID_HEADER);
        String onapPartnerName = httpRequest.getHeader(ONAPLogConstants.Headers.PARTNER_NAME);
        String reqUri = httpRequest.getHeader(ConstantsLogging.USER_AGENT_HEADER);
        return Arrays.asList(userId, onapPartnerName, reqUri).stream()
                .filter(pn-> !StringUtils.isEmpty(pn)).findFirst().orElse(ConstantsLogging.PartnerName_Unknown);
    }

    protected void setKeyRequestIdIfNotSetYet() {
        if (StringUtils.isEmpty(ecompLogFieldsHandler.getKeyRequestId())) {
            setKeyRequestId(LoggingThreadLocalsHolder.getUuid());
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
    public void log(Marker marker, LogLevel logLevel, String message) {
        log(marker, logLevel, message, (Object) null);
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

    @Override
    public void log(Marker marker, LogLevel logLevel, String message, Object...params) {
        validateMandatoryFields(message);

        switch(logLevel) {
            case ERROR:
            case FATAL:  //TODO check how to log "FATAL" word
                myLogger.error(marker, message, params);
                break;
            case WARN:
                myLogger.warn(marker, message, params);
                break;
            case INFO:
                myLogger.info(marker, message, params);
                break;
            case DEBUG:
                myLogger.debug(marker, message, params);
                break;
            case TRACE:
                myLogger.trace(marker, message, params);
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


    @Override
    public ILogger setKeyInvocationId(String keyInvocationId) {
        ecompLogFieldsHandler.setKeyInvocationId(keyInvocationId);
        return this;
    }


}
