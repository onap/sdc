package org.openecomp.sdc.common.log.elements;

import org.onap.logging.ref.slf4j.ONAPLogConstants;
import org.openecomp.sdc.common.log.api.ILogFieldsHandler;
import org.openecomp.sdc.common.log.enums.LogLevel;
import org.openecomp.sdc.common.log.enums.LogMarkers;
import org.slf4j.Logger;
import org.slf4j.MarkerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LoggerDebug extends LoggerBase {

    private static ArrayList<String> mandatoryFields = new ArrayList<>(Arrays.asList(ONAPLogConstants.MDCs.REQUEST_ID));

    LoggerDebug(ILogFieldsHandler ecompMdcWrapper, Logger logger) {
        super(ecompMdcWrapper, MarkerFactory.getMarker(LogMarkers.DEBUG_MARKER.text()), logger);
    }

    @Override
    public LoggerDebug clear() {
        //nothing to clean up
        return this;
    }

    @Override
    public void log(LogLevel logLevel, String message, Object...params){
        setKeyRequestIdIfNotSetYet();
        super.log(logLevel, message, params);
    }

    @Override
    public void log(LogLevel logLevel, String message, Throwable throwable){
        setKeyRequestIdIfNotSetYet();
        super.log(logLevel, message, throwable);
    }

    @Override
    public void log(LogLevel logLevel, String message){
        setKeyRequestIdIfNotSetYet();
        super.log(logLevel, message);
    }

    @Override
    public LoggerDebug startTimer() {
        return this;
    }

    @Override
    public List<String> getMandatoryFields() {
        return mandatoryFields;
    }
}
