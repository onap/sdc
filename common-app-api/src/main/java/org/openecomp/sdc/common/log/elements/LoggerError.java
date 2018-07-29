package org.openecomp.sdc.common.log.elements;

import org.apache.commons.lang3.StringUtils;
import org.openecomp.sdc.common.config.EcompErrorConfiguration;
import org.openecomp.sdc.common.log.api.ILogConfiguration;
import org.openecomp.sdc.common.log.api.ILogFieldsHandler;
import org.openecomp.sdc.common.log.enums.EcompLoggerErrorCode;
import org.openecomp.sdc.common.log.enums.LogLevel;
import org.openecomp.sdc.common.log.enums.LogMarkers;
import org.slf4j.Logger;
import org.slf4j.MarkerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class LoggerError extends LoggerBase {
    private static ArrayList<String> mandatoryFields = new ArrayList<>(Arrays.asList(
            ILogConfiguration.MDC_KEY_REQUEST_ID,
            ILogConfiguration.MDC_SERVICE_NAME,
            ILogConfiguration.MDC_ERROR_CATEGORY,
            ILogConfiguration.MDC_ERROR_CODE));

    public static final String defaultServiceName = "SDC catalog";

    LoggerError(ILogFieldsHandler ecompMdcWrapper, Logger logger) {
        super(ecompMdcWrapper, MarkerFactory.getMarker(LogMarkers.ERROR_MARKER.text()), logger);
    }

    @Override
    public List<String> getMandatoryFields() {
        return Collections.unmodifiableList(mandatoryFields);
    }

    @Override
    public LoggerError setKeyRequestId(String keyRequestId) {
        return (LoggerError) super.setKeyRequestId(keyRequestId);
    }

    @Override
    public LoggerError startTimer() {
        return this;
    }

    @Override
    public LoggerError clear() {
        ecompLogFieldsHandler.removeErrorCategory();
        ecompLogFieldsHandler.removeErrorDescription();
        ecompLogFieldsHandler.removeErrorCode();
        return this;
    }

    public void log(LogLevel logLevel,
                    EcompLoggerErrorCode errorCodeEnum,
                    String serviceName,
                    String targetEntity,
                    String message, Object...params) {
        fillFieldsBeforeLogging(logLevel, errorCodeEnum, serviceName, targetEntity);
        super.log(logLevel, message, params);
    }

    private void fillFieldsBeforeLogging(LogLevel logLevel, EcompLoggerErrorCode errorCodeEnum, String serviceName, String targetEntity) {
        clear();
        ecompLogFieldsHandler.setErrorCode(errorCodeEnum.getErrorCode());
        ecompLogFieldsHandler.setErrorCategory(logLevel.name());
        if (!StringUtils.isEmpty(targetEntity)) {
            //avoid overriding this parameter in MDC
            ecompLogFieldsHandler.setTargetEntity(targetEntity);
        }
        if (StringUtils.isEmpty(ecompLogFieldsHandler.getServiceName())) {
            ecompLogFieldsHandler.setServiceName(serviceName);
        }
        setKeyRequestIdIfNotSetYet();
    }

    public void log(EcompErrorConfiguration.EcompErrorSeverity errorSeverity,
                    EcompLoggerErrorCode errorCodeEnum,
                    String serviceName,
                    String targetEntity,
                    String message, Object... params) {
        log(convertFromSeverityErrorLevel(errorSeverity), errorCodeEnum, serviceName, targetEntity, message, params);
    }

    public void log(LogLevel logLevel,
                    EcompLoggerErrorCode errorCodeEnum,
                    String serviceName,
                    String message, Object...params) {
        log(logLevel, errorCodeEnum, serviceName, null, message, params);
    }

    public void log(LogLevel logLevel,
                    EcompLoggerErrorCode errorCodeEnum,
                    String serviceName,
                    String message) {
        log(logLevel, errorCodeEnum, serviceName, message);
    }

    @Override
    public void log(LogLevel logLevel, String message, Object...params) {
        log(logLevel, EcompLoggerErrorCode.BUSINESS_PROCESS_ERROR, defaultServiceName, null, message, params);
    }

    public void log(LogLevel logLevel, String message, Throwable throwable) {
        log(logLevel, createErrorMessage(message, throwable));
    }

    public void log(LogLevel logLevel, String message) {
        log(logLevel, EcompLoggerErrorCode.BUSINESS_PROCESS_ERROR, defaultServiceName, null, message);
    }

    public void logInfo(LogLevel logLevel, String message, Object... params) {
        log(logLevel, EcompLoggerErrorCode.SUCCESS, defaultServiceName, null, message, params);
    }

    private LogLevel convertFromSeverityErrorLevel(EcompErrorConfiguration.EcompErrorSeverity severityLevel) {
        switch(severityLevel) {
            case INFO:
                return LogLevel.INFO;
            case FATAL:
                return LogLevel.FATAL;
            case ERROR:
                return LogLevel.ERROR;
            case WARN:
                return LogLevel.WARN;
        }
        return LogLevel.ERROR;
    }

}
