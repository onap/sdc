package org.openecomp.sdc.common.ecomplog;

import static org.openecomp.sdc.common.ecomplog.api.IEcompLogConfiguration.MDC_ALERT_SEVERITY;
import static org.openecomp.sdc.common.ecomplog.api.IEcompLogConfiguration.MDC_BEGIN_TIMESTAMP;
import static org.openecomp.sdc.common.ecomplog.api.IEcompLogConfiguration.MDC_CLASS_NAME;
import static org.openecomp.sdc.common.ecomplog.api.IEcompLogConfiguration.MDC_ELAPSED_TIME;
import static org.openecomp.sdc.common.ecomplog.api.IEcompLogConfiguration.MDC_END_TIMESTAMP;
import static org.openecomp.sdc.common.ecomplog.api.IEcompLogConfiguration.MDC_INSTANCE_UUID;
import static org.openecomp.sdc.common.ecomplog.api.IEcompLogConfiguration.MDC_KEY_REQUEST_ID;
import static org.openecomp.sdc.common.ecomplog.api.IEcompLogConfiguration.MDC_OPT_FIELD1;
import static org.openecomp.sdc.common.ecomplog.api.IEcompLogConfiguration.MDC_OPT_FIELD2;
import static org.openecomp.sdc.common.ecomplog.api.IEcompLogConfiguration.MDC_OPT_FIELD3;
import static org.openecomp.sdc.common.ecomplog.api.IEcompLogConfiguration.MDC_OPT_FIELD4;
import static org.openecomp.sdc.common.ecomplog.api.IEcompLogConfiguration.MDC_PARTNER_NAME;
import static org.openecomp.sdc.common.ecomplog.api.IEcompLogConfiguration.MDC_PROCESS_KEY;
import static org.openecomp.sdc.common.ecomplog.api.IEcompLogConfiguration.MDC_REMOTE_HOST;
import static org.openecomp.sdc.common.ecomplog.api.IEcompLogConfiguration.MDC_RESPONSE_CODE;
import static org.openecomp.sdc.common.ecomplog.api.IEcompLogConfiguration.MDC_RESPONSE_DESC;
import static org.openecomp.sdc.common.ecomplog.api.IEcompLogConfiguration.MDC_SERVER_FQDN;
import static org.openecomp.sdc.common.ecomplog.api.IEcompLogConfiguration.MDC_SERVER_IP_ADDRESS;
import static org.openecomp.sdc.common.ecomplog.api.IEcompLogConfiguration.MDC_SERVICE_INSTANCE_ID;
import static org.openecomp.sdc.common.ecomplog.api.IEcompLogConfiguration.MDC_SERVICE_NAME;
import static org.openecomp.sdc.common.ecomplog.api.IEcompLogConfiguration.MDC_STATUS_CODE;

import org.openecomp.sdc.common.ecomplog.Enums.EcompLoggerErrorCode;
import org.openecomp.sdc.common.ecomplog.Enums.LogMarkers;
import org.openecomp.sdc.common.ecomplog.Enums.Severity;
import org.openecomp.sdc.common.ecomplog.Enums.StatusCode;
import org.openecomp.sdc.common.ecomplog.api.IEcompMdcWrapper;
import org.slf4j.MDC;
import org.slf4j.MarkerFactory;

public class EcompLoggerAudit extends EcompLoggerBase{
    private static EcompLoggerAudit instanceLoggerAudit = EcompLoggerFactory.getLogger(EcompLoggerAudit.class);

    EcompLoggerAudit(IEcompMdcWrapper ecompMdcWrapper) {
        super (ecompMdcWrapper, MarkerFactory.getMarker(LogMarkers.AUDIT_MARKER.text()));
   }

    public static EcompLoggerAudit getInstance() {
        return instanceLoggerAudit;
    }

    @Override
    public EcompLoggerAudit startTimer() {
        return (EcompLoggerAudit) super.startTimer();
    }

    public EcompLoggerAudit stopTimer() {
        ecompMdcWrapper.stopTimer();
        return this;
    }

    // automatic parameter this is optional
    public EcompLoggerAudit setAutoServerFQDN(String serverFQDN) {
        ecompMdcWrapper.setAutoServerFQDN(serverFQDN);
        return this;
    }

    // automatic parameter this is optional
    public EcompLoggerAudit setAutoServerIPAddress(String serverIPAddress) {
        ecompMdcWrapper.setAutoServerIPAddress(serverIPAddress);
        return this;
    }

    public EcompLoggerAudit setInstanceUUID(String instanceUUID) {
        ecompMdcWrapper.setInstanceUUID(instanceUUID);
        return this;
    }

    public EcompLoggerAudit setOptClassName(String className) {
        MDC.put("ClassName", className);
        return this;
    }

    public EcompLoggerAudit setOptProcessKey(String processKey) {
        ecompMdcWrapper.setProcessKey(processKey);
        return this;
    }

    public EcompLoggerAudit setOptAlertSeverity(Severity alertSeverity) {
        ecompMdcWrapper.setAlertSeverity(alertSeverity);
        return this;
    }

    // ecomplog optional parameter
    public EcompLoggerAudit setOptCustomField1(String customField1) {
        ecompMdcWrapper.setOptCustomField1(customField1);
        return this;
    }

    // ecomplog optional parameter
    public EcompLoggerAudit setOptCustomField2(String customField2) {
        ecompMdcWrapper.setOptCustomField2(customField2);
        return this;
    }

    // ecomplog optional parameter
    public EcompLoggerAudit setOptCustomField3(String customField3) {
        ecompMdcWrapper.setOptCustomField3(customField3);
        return this;
    }

    public EcompLoggerAudit setOptCustomField4(String customField4) {
        ecompMdcWrapper.setOptCustomField4(customField4);
        return this;
    }

    @Override
    public EcompLoggerAudit setKeyRequestId(String keyRequestId) {
        return (EcompLoggerAudit) super.setKeyRequestId(keyRequestId);
    }

    public EcompLoggerAudit setRemoteHost(String remoteHost) {
        ecompMdcWrapper.setRemoteHost(remoteHost);
        return this;
    }

    public EcompLoggerAudit setServiceName(String serviceName) {
        ecompMdcWrapper.setServiceName(serviceName);
        return this;
    }

    public EcompLoggerAudit setStatusCode(String statusCode) {
        // status code is either success (COMPLETE) or failure (ERROR) of the request.
        String respStatus = Integer.parseInt(statusCode) / 100 == 2 ? StatusCode.COMPLETE.getStatusCodeEnum() : StatusCode.ERROR.getStatusCodeEnum();
        ecompMdcWrapper.setStatusCode(respStatus);
        return this;
    }

    public EcompLoggerAudit setPartnerName(String partnerName) {
        ecompMdcWrapper.setPartnerName(partnerName);
        return this;
    }

    public EcompLoggerAudit setResponseCode(EcompLoggerErrorCode responseCode) {
        ecompMdcWrapper.setResponseCode(responseCode.getErrorCode());
        return this;
    }

    public EcompLoggerAudit setResponseDesc(String responseDesc) {
        ecompMdcWrapper.setResponseDesc(responseDesc);
        return this;
    }

    public EcompLoggerAudit setOptServiceInstanceId(String serviceInstanceId) {
        ecompMdcWrapper.setServiceInstanceId(serviceInstanceId);
        return this;
    }

    @Override
    public EcompLoggerAudit clear() {
        return (EcompLoggerAudit) super.clear();
    }

    @Override
    public void initializeMandatoryFields() {

        ecompMdcWrapper.setMandatoryField(MDC_BEGIN_TIMESTAMP);
        ecompMdcWrapper.setMandatoryField(MDC_END_TIMESTAMP);
        ecompMdcWrapper.setMandatoryField(MDC_KEY_REQUEST_ID);
        ecompMdcWrapper.setMandatoryField(MDC_SERVICE_NAME);
        ecompMdcWrapper.setMandatoryField(MDC_PARTNER_NAME);
        ecompMdcWrapper.setMandatoryField(MDC_STATUS_CODE);
        ecompMdcWrapper.setMandatoryField(MDC_RESPONSE_CODE);
        ecompMdcWrapper.setMandatoryField(MDC_SERVICE_INSTANCE_ID);
        ecompMdcWrapper.setMandatoryField(MDC_RESPONSE_DESC);
        ecompMdcWrapper.setMandatoryField(MDC_ELAPSED_TIME);

        //Theoretically Optional, but practically Mandatory
        ecompMdcWrapper.setMandatoryField(MDC_SERVER_IP_ADDRESS);
        ecompMdcWrapper.setMandatoryField(MDC_SERVER_FQDN);

        ecompMdcWrapper.setOptionalField(MDC_INSTANCE_UUID);
        ecompMdcWrapper.setOptionalField(MDC_ALERT_SEVERITY);
        ecompMdcWrapper.setOptionalField(MDC_REMOTE_HOST);
        ecompMdcWrapper.setOptionalField(MDC_CLASS_NAME);
        ecompMdcWrapper.setOptionalField(MDC_PROCESS_KEY);
        ecompMdcWrapper.setOptionalField(MDC_OPT_FIELD1);
        ecompMdcWrapper.setOptionalField(MDC_OPT_FIELD2);
        ecompMdcWrapper.setOptionalField(MDC_OPT_FIELD3);
        ecompMdcWrapper.setOptionalField(MDC_OPT_FIELD4);
    }
}
