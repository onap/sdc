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
import static org.openecomp.sdc.common.ecomplog.api.IEcompLogConfiguration.MDC_TARGET_ENTITY;
import static org.openecomp.sdc.common.ecomplog.api.IEcompLogConfiguration.MDC_TARGET_SERVICE_NAME;
import static org.openecomp.sdc.common.ecomplog.api.IEcompLogConfiguration.MDC_TARGET_VIRTUAL_ENTITY;

import org.openecomp.sdc.common.ecomplog.Enums.LogMarkers;
import org.openecomp.sdc.common.ecomplog.Enums.Severity;
import org.openecomp.sdc.common.ecomplog.api.IEcompMdcWrapper;
import org.slf4j.MarkerFactory;


public class EcompLoggerMetric extends EcompLoggerBase{
    private static EcompLoggerMetric instanceLoggerMetric = (EcompLoggerMetric) EcompLoggerFactory.getLogger(EcompLoggerMetric.class);

    EcompLoggerMetric(IEcompMdcWrapper ecompMdcWrapper) {
        super(ecompMdcWrapper, MarkerFactory.getMarker(LogMarkers.METRIC_MARKER.text()));
    }

    public static EcompLoggerMetric getInstance() {
        return instanceLoggerMetric;
    }

    @Override
    public EcompLoggerMetric startTimer() {
        return (EcompLoggerMetric) super.startTimer();
    }

    @Override
    public EcompLoggerMetric setKeyRequestId(String keyRequestId) {
        return (EcompLoggerMetric) super.setKeyRequestId(keyRequestId);
    }

    public EcompLoggerMetric stopTimer() {
        ecompMdcWrapper.stopTimer();
        return this;
    }

    // automatic parameter this is optional
    public EcompLoggerMetric setAutoServerFQDN(String serverFQDN) {
        ecompMdcWrapper.setAutoServerFQDN(serverFQDN);
        return this;
    }

    // automatic parameter this is optional
    public EcompLoggerMetric setAutoServerIPAddress(String serverIPAddress) {
        ecompMdcWrapper.setAutoServerIPAddress(serverIPAddress);
        return this;
    }

    public EcompLoggerMetric setInstanceUUID(String instanceUUID) {
        ecompMdcWrapper.setInstanceUUID(instanceUUID);
        return this;
    }

    // ecomplog optional parameter
    public EcompLoggerMetric setOptProcessKey(String processKey) {
        ecompMdcWrapper.setProcessKey(processKey);
        return this;
    }

    // ecomplog optional parameter
    public EcompLoggerMetric setOptAlertSeverity(Severity alertSeverity) {
        ecompMdcWrapper.setAlertSeverity(alertSeverity);
        return this;
    }

    // ecomplog optional parameter
    public EcompLoggerMetric setOptCustomField1(String customField1) {
        ecompMdcWrapper.setOptCustomField1(customField1);
        return this;
    }

    // ecomplog optional parameter
    public EcompLoggerMetric setOptCustomField2(String customField2) {
        ecompMdcWrapper.setOptCustomField2(customField2);
        return this;
    }

    // ecomplog optional parameter
    public EcompLoggerMetric setOptCustomField3(String customField3) {
        ecompMdcWrapper.setOptCustomField3(customField3);
        return this;
    }

    // ecomplog optional parameter
    public EcompLoggerMetric setOptCustomField4(String customField4) {
        ecompMdcWrapper.setOptCustomField4(customField4);
        return this;
    }

    public EcompLoggerMetric setRemoteHost(String remoteHost) {
        ecompMdcWrapper.setRemoteHost(remoteHost);
        return this;
    }

    public EcompLoggerMetric setServiceName(String serviceName) {
        ecompMdcWrapper.setServiceName(serviceName);
        return this;
    }

    public EcompLoggerMetric setStatusCode(String statusCode) {
        ecompMdcWrapper.setStatusCode(statusCode);
        return this;
    }

    public EcompLoggerMetric setPartnerName(String partnerName) {
        ecompMdcWrapper.setPartnerName(partnerName);
        return this;
    }

    public EcompLoggerMetric setResponseCode(int responseCode) {
        ecompMdcWrapper.setResponseCode(responseCode);
        return this;
    }

    public EcompLoggerMetric setResponseDesc(String responseDesc) {
        ecompMdcWrapper.setResponseDesc(responseDesc);
        return this;
    }

    public EcompLoggerMetric setOptServiceInstanceId(String serviceInstanceId) {
        ecompMdcWrapper.setServiceInstanceId(serviceInstanceId);
        return this;
    }

    public EcompLoggerMetric setOptClassName(String className) {
        ecompMdcWrapper.setClassName(className);
        return this;
    }

    public EcompLoggerMetric setTargetEntity(String targetEntity) {
        ecompMdcWrapper.setTargetEntity(targetEntity);
        return this;
    }

    public EcompLoggerMetric setTargetServiceName(String targetServiceName) {
        ecompMdcWrapper.setTargetServiceName(targetServiceName);
        return this;
    }

    public EcompLoggerMetric setTargetVirtualEntity(String targetVirtualEntity) {
        ecompMdcWrapper.setTargetVirtualEntity(targetVirtualEntity);
        return this;
    }

    @Override
    public EcompLoggerMetric clear () {
        return (EcompLoggerMetric) super.clear();
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
        ecompMdcWrapper.setMandatoryField(MDC_TARGET_ENTITY);
        ecompMdcWrapper.setMandatoryField(MDC_TARGET_SERVICE_NAME);
        ecompMdcWrapper.setMandatoryField(MDC_TARGET_VIRTUAL_ENTITY);

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
