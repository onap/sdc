package org.openecomp.sdc.common.log.elements;


import org.onap.logging.ref.slf4j.ONAPLogConstants;
import org.openecomp.sdc.common.log.api.ILogConfiguration;
import org.openecomp.sdc.common.log.api.ILogFieldsHandler;
import org.openecomp.sdc.common.log.enums.EcompLoggerErrorCode;
import org.openecomp.sdc.common.log.enums.Severity;
import org.slf4j.Logger;
import org.slf4j.MDC;
import org.slf4j.MarkerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class LoggerAudit extends LoggerBase {
    private static ArrayList<String> mandatoryFields = new ArrayList<>(Arrays.asList(
            ONAPLogConstants.MDCs.ENTRY_TIMESTAMP,
            ILogConfiguration.MDC_END_TIMESTAMP,
            ONAPLogConstants.MDCs.REQUEST_ID,
            ONAPLogConstants.MDCs.SERVICE_NAME,
            ONAPLogConstants.MDCs.PARTNER_NAME,
            ONAPLogConstants.MDCs.RESPONSE_STATUS_CODE,
            ONAPLogConstants.MDCs.RESPONSE_CODE,
            ILogConfiguration.MDC_SERVICE_INSTANCE_ID,
            ONAPLogConstants.MDCs.RESPONSE_DESCRIPTION,
            ILogConfiguration.MDC_ELAPSED_TIME,
            ILogConfiguration.MDC_SERVER_IP_ADDRESS,
            ONAPLogConstants.MDCs.SERVER_FQDN));

    private static ArrayList<String> optionalFields = new ArrayList<>(Arrays.asList(
            ONAPLogConstants.MDCs.INSTANCE_UUID,
            ONAPLogConstants.MDCs.RESPONSE_SEVERITY,
            ILogConfiguration.MDC_REMOTE_HOST,
            ILogConfiguration.MDC_CLASS_NAME,
            ILogConfiguration.MDC_PROCESS_KEY,
            ILogConfiguration.MDC_OPT_FIELD1,
            ILogConfiguration.MDC_OPT_FIELD2,
            ILogConfiguration.MDC_OPT_FIELD3,
            ILogConfiguration.MDC_OPT_FIELD4));

    LoggerAudit(ILogFieldsHandler ecompMdcWrapper, Logger logger) {
        //TODO Andrey, set default marker
        super (ecompMdcWrapper, MarkerFactory.getMarker(ONAPLogConstants.Markers.ENTRY.getName()), logger);
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
        MDC.put(ILogConfiguration.MDC_CLASS_NAME, className);
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

    @Override
    public LoggerAudit setKeyRequestId(String keyRequestId) {
        return (LoggerAudit) super.setKeyRequestId(keyRequestId);
    }

    @Override
    public LoggerAudit setKeyInvocationId(String keyInvocationId) {
        ecompLogFieldsHandler.setKeyInvocationId(keyInvocationId);
        return this;
    }

    public LoggerAudit setRemoteHost(String remoteHost) {
        ecompLogFieldsHandler.setRemoteHost(remoteHost);
        return this;
    }

    public LoggerAudit setServiceName(String serviceName) {
        ecompLogFieldsHandler.setServiceName(serviceName);
        return this;
    }

    public LoggerAudit setStatusCodeByResponseCode(String responseCode) {
        String respStatus = Integer.parseInt(responseCode) / 100 == 2 ? ONAPLogConstants.ResponseStatus.COMPLETE.name() : ONAPLogConstants.ResponseStatus.ERROR.name();
        ecompLogFieldsHandler.setStatusCode(respStatus);
        return this;
    }

    public LoggerAudit setStatusCode(String statusCode) {
        ecompLogFieldsHandler.setStatusCode(statusCode);
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
