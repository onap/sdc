package org.openecomp.sdc.common.log.wrappers;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.logging.ref.slf4j.ONAPLogConstants;
import org.openecomp.sdc.common.log.api.ILogConfiguration;
import org.openecomp.sdc.common.log.elements.ErrorLogOptionalData;
import org.openecomp.sdc.common.log.elements.LoggerError;
import org.openecomp.sdc.common.log.enums.EcompLoggerErrorCode;
import org.openecomp.sdc.common.log.enums.LogLevel;
import org.openecomp.sdc.common.log.enums.Severity;
import org.openecomp.sdc.common.log.utils.LoggingThreadLocalsHolder;
import org.slf4j.MDC;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.onap.logging.ref.slf4j.ONAPLogConstants.MDCs.REQUEST_ID;
import static org.onap.logging.ref.slf4j.ONAPLogConstants.MDCs.SERVICE_NAME;
import static org.openecomp.sdc.common.log.api.ILogConfiguration.*;

@RunWith(MockitoJUnitRunner.class)
public class LoggerTest {

    private final static String targetEntity = "DCEA";
    private final static String targetServiceName = "test target name";
    private final static String serviceName = "testService";
    private final static String message = "Logger message";
    private final static String exceptionMsg= "Exception testing";
    private final static String missingFieldsMessageFragment = "mandatory parameters for ECOMP logging";

    @Mock
    private org.slf4j.Logger logger;
    @Mock
    private ContainerRequestContext requestContext;
    @Mock
    private UriInfo uriInfo;
    @Mock
    private Response.StatusType statusType;

    @InjectMocks
    private Logger commonLogger;

    @Captor
    private ArgumentCaptor<String> captor;

    @Before
    public void setUp() {
        MDC.clear();
    }

    @Test
    public void validateErrorLogWhenErrorSettingsProvided() {
        when(logger.isErrorEnabled()).thenReturn(true);
        commonLogger.error(EcompLoggerErrorCode.PERMISSION_ERROR, serviceName, targetEntity, message);

        verify(logger).error(any(Marker.class), captor.capture(), any(Object[].class));
        assertEquals(message, captor.getValue());
        assertEquals(String.valueOf(EcompLoggerErrorCode.PERMISSION_ERROR.getErrorCode()), MDC.get(ILogConfiguration.MDC_ERROR_CODE));
        assertEquals(LogLevel.ERROR.name(), MDC.get(ILogConfiguration.MDC_ERROR_CATEGORY));
        assertEquals(targetEntity, MDC.get(ONAPLogConstants.MDCs.TARGET_ENTITY));
        assertEquals(serviceName, MDC.get(SERVICE_NAME));
    }

    @Test
    public void validateWarnMessageIsLoggedWhenAllErrorSettingsProvided() {
        when(logger.isErrorEnabled()).thenReturn(true);
        commonLogger.error(EcompLoggerErrorCode.AVAILABILITY_TIMEOUTS_ERROR, serviceName, targetEntity, message);

        verify(logger).error(any(Marker.class), captor.capture(), any(Object[].class));
        assertEquals(message, captor.getValue());
        assertEquals(String.valueOf(EcompLoggerErrorCode.AVAILABILITY_TIMEOUTS_ERROR.getErrorCode()), MDC.get(ILogConfiguration.MDC_ERROR_CODE));
        assertEquals(LogLevel.ERROR.name(), MDC.get(ILogConfiguration.MDC_ERROR_CATEGORY));
        assertEquals(targetEntity, MDC.get(ONAPLogConstants.MDCs.TARGET_ENTITY));
        assertEquals(serviceName, MDC.get(SERVICE_NAME));
    }

    @Test
    public void validateFatalMessageIsLoggedWhenAllErrorSettingsProvided() {
        when(logger.isErrorEnabled()).thenReturn(true);
        commonLogger.fatal(EcompLoggerErrorCode.PERMISSION_ERROR, serviceName, targetEntity, message);

        verify(logger).error(any(Marker.class), captor.capture(), any(Object[].class));
        assertEquals(message, captor.getValue());
        assertEquals(String.valueOf(EcompLoggerErrorCode.PERMISSION_ERROR.getErrorCode()), MDC.get(ILogConfiguration.MDC_ERROR_CODE));
        assertEquals(LogLevel.FATAL.name(), MDC.get(ILogConfiguration.MDC_ERROR_CATEGORY));
        assertEquals(targetEntity, MDC.get(ONAPLogConstants.MDCs.TARGET_ENTITY));
        assertEquals(serviceName, MDC.get(SERVICE_NAME));
    }

    @Test
    public void validateWarnMessage_AllErrorElements() {
        when(logger.isWarnEnabled()).thenReturn(true);
        ErrorLogOptionalData errorLogOptionalData = ErrorLogOptionalData.newBuilder()
                .targetEntity(targetEntity)
                .targetServiceName(targetServiceName).build();
        commonLogger.warn(EcompLoggerErrorCode.BUSINESS_PROCESS_ERROR, serviceName,errorLogOptionalData, message);
        verify(logger).warn(any(Marker.class), captor.capture(), any(Object[].class));
        assertEquals(message, captor.getValue());
        assertEquals(String.valueOf(EcompLoggerErrorCode.BUSINESS_PROCESS_ERROR.getErrorCode()), MDC.get(ILogConfiguration.MDC_ERROR_CODE));
        assertEquals(LogLevel.WARN.name(), MDC.get(ILogConfiguration.MDC_ERROR_CATEGORY));
        assertEquals(targetEntity, MDC.get(ONAPLogConstants.MDCs.TARGET_ENTITY));
        assertEquals(serviceName, MDC.get(SERVICE_NAME));
        assertEquals(targetServiceName, MDC.get(ONAPLogConstants.MDCs.TARGET_SERVICE_NAME));
    }

    @Test
    public void validateWarnMessage_emptyTargetEntity() {
        when(logger.isWarnEnabled()).thenReturn(true);
        ErrorLogOptionalData errorLogOptionalData = ErrorLogOptionalData.newBuilder()
                .targetServiceName(targetServiceName).build();
        commonLogger.warn(EcompLoggerErrorCode.BUSINESS_PROCESS_ERROR, serviceName,errorLogOptionalData, message);
        verify(logger).warn(any(Marker.class), captor.capture(), any(Object[].class));
        assertEquals(message, captor.getValue());
        assertEquals(String.valueOf(EcompLoggerErrorCode.BUSINESS_PROCESS_ERROR.getErrorCode()), MDC.get(ILogConfiguration.MDC_ERROR_CODE));
        assertEquals(LogLevel.WARN.name(), MDC.get(ILogConfiguration.MDC_ERROR_CATEGORY));
        assertEquals(null, MDC.get(ONAPLogConstants.MDCs.TARGET_ENTITY));
        assertEquals(serviceName, MDC.get(SERVICE_NAME));
        assertEquals(targetServiceName, MDC.get(ONAPLogConstants.MDCs.TARGET_SERVICE_NAME));
    }

    @Test
    public void validateWarnMessage_emptyTargetServiceName() {
        when(logger.isWarnEnabled()).thenReturn(true);
        ErrorLogOptionalData errorLogOptionalData = ErrorLogOptionalData.newBuilder()
                .targetEntity(targetEntity).build();
        commonLogger.warn(EcompLoggerErrorCode.BUSINESS_PROCESS_ERROR, serviceName,errorLogOptionalData, message);
        verify(logger).warn(any(Marker.class), captor.capture(), any(Object[].class));
        assertEquals(message, captor.getValue());
        assertEquals(String.valueOf(EcompLoggerErrorCode.BUSINESS_PROCESS_ERROR.getErrorCode()), MDC.get(ILogConfiguration.MDC_ERROR_CODE));
        assertEquals(LogLevel.WARN.name(), MDC.get(ILogConfiguration.MDC_ERROR_CATEGORY));
        assertEquals(targetEntity, MDC.get(ONAPLogConstants.MDCs.TARGET_ENTITY));
        assertEquals(serviceName, MDC.get(SERVICE_NAME));
        assertEquals(null, MDC.get(ONAPLogConstants.MDCs.TARGET_SERVICE_NAME));
    }


    @Test
    public void validateErrorMessage_AllErrorElements() {
        when(logger.isErrorEnabled()).thenReturn(true);
        ErrorLogOptionalData errorLogOptionalData = ErrorLogOptionalData.newBuilder()
                .targetEntity(targetEntity)
                .targetServiceName(targetServiceName).build();
        commonLogger.error(EcompLoggerErrorCode.BUSINESS_PROCESS_ERROR, serviceName,errorLogOptionalData, message);
        verify(logger).error(any(Marker.class), captor.capture(), any(Object[].class));
        assertEquals(message, captor.getValue());
        assertEquals(String.valueOf(EcompLoggerErrorCode.BUSINESS_PROCESS_ERROR.getErrorCode()), MDC.get(ILogConfiguration.MDC_ERROR_CODE));
        assertEquals(LogLevel.ERROR.name(), MDC.get(ILogConfiguration.MDC_ERROR_CATEGORY));
        assertEquals(targetEntity, MDC.get(ONAPLogConstants.MDCs.TARGET_ENTITY));
        assertEquals(serviceName, MDC.get(SERVICE_NAME));
        assertEquals(targetServiceName, MDC.get(ONAPLogConstants.MDCs.TARGET_SERVICE_NAME));
    }

    @Test
    public void validateErrorMessage_emptyTargetEntity() {
        when(logger.isErrorEnabled()).thenReturn(true);
        ErrorLogOptionalData errorLogOptionalData = ErrorLogOptionalData.newBuilder()
                .targetServiceName(targetServiceName).build();
        commonLogger.error(EcompLoggerErrorCode.BUSINESS_PROCESS_ERROR, serviceName,errorLogOptionalData, message);
        verify(logger).error(any(Marker.class), captor.capture(), any(Object[].class));
        assertEquals(message, captor.getValue());
        assertEquals(String.valueOf(EcompLoggerErrorCode.BUSINESS_PROCESS_ERROR.getErrorCode()), MDC.get(ILogConfiguration.MDC_ERROR_CODE));
        assertEquals(LogLevel.ERROR.name(), MDC.get(ILogConfiguration.MDC_ERROR_CATEGORY));
        assertEquals(null, MDC.get(ONAPLogConstants.MDCs.TARGET_ENTITY));
        assertEquals(serviceName, MDC.get(SERVICE_NAME));
        assertEquals(targetServiceName, MDC.get(ONAPLogConstants.MDCs.TARGET_SERVICE_NAME));
    }

    @Test
    public void validateErrorMessage_emptyTargetServiceName() {
        when(logger.isErrorEnabled()).thenReturn(true);
        ErrorLogOptionalData errorLogOptionalData = ErrorLogOptionalData.newBuilder()
                .targetEntity(targetEntity).build();
        commonLogger.error(EcompLoggerErrorCode.BUSINESS_PROCESS_ERROR, serviceName,errorLogOptionalData, message);
        verify(logger).error(any(Marker.class), captor.capture(), any(Object[].class));
        assertEquals(message, captor.getValue());
        assertEquals(String.valueOf(EcompLoggerErrorCode.BUSINESS_PROCESS_ERROR.getErrorCode()), MDC.get(ILogConfiguration.MDC_ERROR_CODE));
        assertEquals(LogLevel.ERROR.name(), MDC.get(ILogConfiguration.MDC_ERROR_CATEGORY));
        assertEquals(targetEntity, MDC.get(ONAPLogConstants.MDCs.TARGET_ENTITY));
        assertEquals(serviceName, MDC.get(SERVICE_NAME));
        assertEquals(null, MDC.get(ONAPLogConstants.MDCs.TARGET_SERVICE_NAME));
    }



    @Test
    public void validateErrorMessageIsNotLoggedWhenErrorLevelIsDisabledEvenIfErrorSettingsProvided() {
        commonLogger.error(EcompLoggerErrorCode.PERMISSION_ERROR, serviceName, targetEntity, message);
        verify(logger, never()).error(any(Marker.class), any(String.class));
    }

    @Test
    public void validateErrorLogWhenErrorSettingsProvidedPartially() {
        when(logger.isErrorEnabled()).thenReturn(true);
        commonLogger.error(message);

        verify(logger).error(any(Marker.class), eq(message), any(Object[].class));
        assertEquals(String.valueOf(EcompLoggerErrorCode.BUSINESS_PROCESS_ERROR.getErrorCode()), MDC.get(ILogConfiguration.MDC_ERROR_CODE));
        assertEquals(LogLevel.ERROR.name(), MDC.get(ILogConfiguration.MDC_ERROR_CATEGORY));
        assertNull(MDC.get(ONAPLogConstants.MDCs.TARGET_ENTITY));
        assertEquals(LoggerError.defaultServiceName, MDC.get(SERVICE_NAME));
    }

    @Test
    public void errorMessageIsNotLoggedWhenErrorLevelIsDisabled() {
        commonLogger.error(message);
        verify(logger, times(0)).error(any(Marker.class), anyString());
    }

    @Test
    public void traceMessageWithExceptionIsNotLoggedWhenTraceLevelIsDisabled() {
        commonLogger.trace(message, new UnsupportedOperationException());
        verify(logger, times(0)).trace(any(Marker.class), anyString());
    }

    @Test
    public void verifyInfoMessage() {
        final String msg = "Info message";
        when(logger.isInfoEnabled()).thenReturn(true);
        commonLogger.info(msg, new RuntimeException());
        verify(logger).info(any(Marker.class), anyString());
    }

    @Test
    public void verifyWarnMessage() {
        when(logger.isWarnEnabled()).thenReturn(true);
        commonLogger.warn("Text");
        assertEquals(LogLevel.WARN.name(), MDC.get(MDC_ERROR_CATEGORY));
        assertEquals(String.valueOf(EcompLoggerErrorCode.BUSINESS_PROCESS_ERROR.getErrorCode()), MDC.get(MDC_ERROR_CODE));
        assertEquals(LoggerError.defaultServiceName, MDC.get(SERVICE_NAME));
    }

    @Test
    public void validateErrorLogWithExceptionWhenErrorSettingsProvidedPartially() {
        LoggingThreadLocalsHolder.setUuid("uuid");
        final String logFieldsNotProvidedMsg = "mandatory parameters for ECOMP logging, missing fields: ServiceName PartnerName";
        when(logger.isWarnEnabled()).thenReturn(true);
        commonLogger.warn(message, new NullPointerException(exceptionMsg));

        //the expected warn message
        verify(logger).warn(any(Marker.class), contains(message), any(Object[].class));
        assertEquals(String.valueOf(EcompLoggerErrorCode.BUSINESS_PROCESS_ERROR.getErrorCode()), MDC.get(ILogConfiguration.MDC_ERROR_CODE));
        assertEquals(LogLevel.WARN.name(), MDC.get(ILogConfiguration.MDC_ERROR_CATEGORY));
        assertEquals("uuid", MDC.get(REQUEST_ID));
        assertNull(MDC.get(ONAPLogConstants.MDCs.TARGET_ENTITY));
        assertEquals(LoggerError.defaultServiceName, MDC.get(SERVICE_NAME));
    }


    @Test
    public void validateDebugLogWithException() {
        final String msg = "Debug message";
        LoggingThreadLocalsHolder.setUuid("uuid");
        when(logger.isDebugEnabled()).thenReturn(true);
        commonLogger.debug(msg, new RuntimeException());

        verify(logger).debug(any(Marker.class), eq(msg), any(RuntimeException.class));
    }

    @Test
    public void validateTraceLogWithExceptionAndPartialParamsAndDebugLevelDisabled() {
        final String msg = "Debug message";
        when(logger.isTraceEnabled()).thenReturn(true);
        commonLogger.trace(msg, new RuntimeException());

        verify(logger).trace(any(Marker.class), eq(msg), any(RuntimeException.class));
    }

    @Test
    public void warnMessageWithParameterIsNotLoggedIfWarnLevelIsDisabled() {
        commonLogger.warn("msg", "param");
        verify(logger, times(0)).warn(any(Marker.class),
                                    anyString(), any(Object.class));
    }

    @Test
    public void verifyMdcValuesAreStoredWhenAuditAndErrorLoggersAreInvokedSequentially() throws URISyntaxException {
        final String uuid = "12345";
        final String message = "message";
        when(requestContext.getHeaderString(anyString())).thenReturn("ab2222");
        when(requestContext.getUriInfo()).thenReturn(uriInfo);
        when(logger.isErrorEnabled()).thenReturn(true);

        URI uri = new URI("http:/abc.com/getId");
        when(uriInfo.getRequestUri()).thenReturn(uri);
        when(uriInfo.getBaseUri()).thenReturn(uri);
        when(statusType.getStatusCode()).thenReturn(200);
        when(statusType.getReasonPhrase()).thenReturn("OK");
        LoggerSdcAudit audit = new LoggerSdcAudit(this.getClass());
        LoggingThreadLocalsHolder.setUuid(uuid);
        audit.startLog(requestContext);
        audit.logExit("abc.log.com", requestContext, statusType, LogLevel.INFO, Severity.OK, message,
                MarkerFactory.getMarker(ONAPLogConstants.Markers.EXIT.getName()));

        commonLogger.error(message);
        verify(logger).error(any(Marker.class), eq(message), any(Object[].class));
        assertEquals(uuid, MDC.get(REQUEST_ID));
        assertEquals("/", MDC.get(SERVICE_NAME));
        assertEquals(LogLevel.ERROR.name(), MDC.get(MDC_ERROR_CATEGORY));
        assertEquals(String.valueOf(EcompLoggerErrorCode.BUSINESS_PROCESS_ERROR.getErrorCode()), MDC.get(MDC_ERROR_CODE));
    }

    @Test
    public void verifyLoggerDoesNothingWhenTheLevelIsNotSet() {
        if (commonLogger.isDebugEnabled()) {
            commonLogger.debug("text");
        }
        verify(logger, times(0)).debug(any(Marker.class), anyString(), eq((Object[])null));
    }

    @Test
    public void verifyLoggerTraceMethodIsCalledWhenTheLevelIsSet() {
        LoggingThreadLocalsHolder.setUuid("1234");
        when(logger.isTraceEnabled()).thenReturn(true);
        if (commonLogger.isTraceEnabled()) {
            commonLogger.trace("text");
        }
        verify(logger, times(1)).trace(any(Marker.class), anyString(), eq((Object[])null));
    }


    @Test
    public void verifyMdcValuesAreStoredWhenTraceLoggerIsInvokedAfterAuditStart() throws URISyntaxException {
        final String uuid = "12345";
        final String message = "message";
        when(requestContext.getHeaderString(anyString())).thenReturn("ab2222");
        when(requestContext.getUriInfo()).thenReturn(uriInfo);
        when(logger.isTraceEnabled()).thenReturn(true);

        URI uri = new URI("http:/abc.com/getId");
        when(uriInfo.getRequestUri()).thenReturn(uri);
        when(uriInfo.getBaseUri()).thenReturn(uri);
        LoggerSdcAudit audit = new LoggerSdcAudit(this.getClass());
        LoggingThreadLocalsHolder.setUuid(uuid);
        audit.startLog(requestContext);

        commonLogger.trace(message);
        verify(logger).trace(any(Marker.class), captor.capture(), eq((Object[])null));
        assertEquals(message, captor.getValue());
        assertEquals(uuid, MDC.get(REQUEST_ID));
    }


}
