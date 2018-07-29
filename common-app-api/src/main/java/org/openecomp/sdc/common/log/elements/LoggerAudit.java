package org.openecomp.sdc.common.log.elements;


import org.openecomp.sdc.common.log.api.ILogConfiguration;
import org.openecomp.sdc.common.log.api.ILogFieldsHandler;
import org.openecomp.sdc.common.log.enums.EcompLoggerErrorCode;
import org.openecomp.sdc.common.log.enums.LogMarkers;
import org.openecomp.sdc.common.log.enums.Severity;
import org.openecomp.sdc.common.log.enums.StatusCode;
import org.slf4j.Logger;
import org.slf4j.MDC;
import org.slf4j.MarkerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class LoggerAudit extends LoggerBase {
    private static ArrayList<String> mandatoryFields = new ArrayList<>(Arrays.asList(
            ILogConfiguration.MDC_AUDIT_BEGIN_TIMESTAMP,
            ILogConfiguration.MDC_END_TIMESTAMP,
            ILogConfiguration.MDC_KEY_REQUEST_ID,
            ILogConfiguration.MDC_SERVICE_NAME,
            ILogConfiguration.MDC_PARTNER_NAME,
            ILogConfiguration.MDC_STATUS_CODE,
            ILogConfiguration.MDC_RESPONSE_CODE,
            ILogConfiguration.MDC_SERVICE_INSTANCE_ID,
            ILogConfiguration.MDC_RESPONSE_DESC,
            ILogConfiguration.MDC_ELAPSED_TIME,
            ILogConfiguration.MDC_SERVER_IP_ADDRESS,
            ILogConfiguration.MDC_SERVER_FQDN));

    private static ArrayList<String> optionalFields = new ArrayList<>(Arrays.asList(
            ILogConfiguration.MDC_INSTANCE_UUID,
            ILogConfiguration.MDC_ALERT_SEVERITY,
            ILogConfiguration.MDC_REMOTE_HOST,
            ILogConfiguration.MDC_CLASS_NAME,
            ILogConfiguration.MDC_PROCESS_KEY,
            ILogConfiguration.MDC_OPT_FIELD1,
            ILogConfiguration.MDC_OPT_FIELD2,
            ILogConfiguration.MDC_OPT_FIELD3,
            ILogConfiguration.MDC_OPT_FIELD4));

    LoggerAudit(ILogFieldsHandler ecompMdcWrapper, Logger logger) {
        super (ecompMdcWrapper, MarkerFactory.getMarker(LogMarkers.AUDIT_MARKER.text()), logger);
        //put the remote host and FQDN values from another thread if they are set
        ecompMdcWrapper.setServerIPAddressInternally();
        ecompMdcWrapper.setServerFQDNInternally();
    }

    @Override
    public LoggerAudit startTimer() {
        ecompLogFieldsHandler.startAuditTimer();
        return this;
    }

    public LoggerAudit stopTimer() {
        ecompLogFieldsHandler.stopAuditTimer();
        return this;
    }

    public LoggerAudit setInstanceUUID(String instanceUUID) {
        ecompLogFieldsHandler.setInstanceUUID(instanceUUID);
        return this;
    }

    public LoggerAudit setOptClassName(String className) {
        MDC.put("ClassName", className);
        return this;
    }

    public LoggerAudit setOptProcessKey(String processKey) {
        ecompLogFieldsHandler.setProcessKey(processKey);
        return this;
    }

    public LoggerAudit setOptAlertSeverity(Severity alertSeverity) {
        ecompLogFieldsHandler.setAlertSeverity(alertSeverity);
        return this;
    }

    // log optional parameter
    public LoggerAudit setOptCustomField1(String customField1) {
        ecompLogFieldsHandler.setOptCustomField1(customField1);
        return this;
    }

    // log optional parameter
    public LoggerAudit setOptCustomField2(String customField2) {
        ecompLogFieldsHandler.setOptCustomField2(customField2);
        return this;
    }

    // log optional parameter
    public LoggerAudit setOptCustomField3(String customField3) {
        ecompLogFieldsHandler.setOptCustomField3(customField3);
        return this;
    }

    public LoggerAudit setOptCustomField4(String customField4) {
        ecompLogFieldsHandler.setOptCustomField4(customField4);
        return this;
    }

    @Override
    public LoggerAudit setKeyRequestId(String keyRequestId) {
        return (LoggerAudit) super.setKeyRequestId(keyRequestId);
    }

    public LoggerAudit setRemoteHost(String remoteHost) {
        ecompLogFieldsHandler.setRemoteHost(remoteHost);
        return this;
    }

    public LoggerAudit setServiceName(String serviceName) {
        ecompLogFieldsHandler.setServiceName(serviceName);
        return this;
    }

    public LoggerAudit setStatusCode(String statusCode) {
        // status code is either success (COMPLETE) or failure (ERROR) of the request.
        String respStatus = Integer.parseInt(statusCode) / 100 == 2 ? StatusCode.COMPLETE.getStatusCodeEnum() : StatusCode.ERROR.getStatusCodeEnum();
        ecompLogFieldsHandler.setStatusCode(respStatus);
        return this;
    }

    public LoggerAudit setPartnerName(String partnerName) {
        ecompLogFieldsHandler.setPartnerName(partnerName);
        return this;
    }

    public LoggerAudit setResponseCode(EcompLoggerErrorCode responseCode) {
        ecompLogFieldsHandler.setResponseCode(responseCode.getErrorCode());
        return this;
    }

    public LoggerAudit setResponseDesc(String responseDesc) {
        ecompLogFieldsHandler.setResponseDesc(responseDesc);
        return this;
    }

    public LoggerAudit setOptServiceInstanceId(String serviceInstanceId) {
        ecompLogFieldsHandler.setServiceInstanceId(serviceInstanceId);
        return this;
    }

    public String getAuditMessage() {
        return ecompLogFieldsHandler.getAuditMessage();
    }


    @Override
    public List<String> getMandatoryFields() {
        return Collections.unmodifiableList(mandatoryFields);
    }

    @Override
    public LoggerAudit clear() {
        super.clear();
        ecompLogFieldsHandler.setServerFQDNInternally();
        ecompLogFieldsHandler.setServerIPAddressInternally();
        return this;
    }

}
