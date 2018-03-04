package org.openecomp.sdc.common.ecomplog;

import static java.lang.Integer.valueOf;
import static org.openecomp.sdc.common.ecomplog.api.IEcompLogConfiguration.MDC_ALERT_SEVERITY;
import static org.openecomp.sdc.common.ecomplog.api.IEcompLogConfiguration.MDC_CLASS_NAME;
import static org.openecomp.sdc.common.ecomplog.api.IEcompLogConfiguration.MDC_ERROR_CODE;
import static org.openecomp.sdc.common.ecomplog.api.IEcompLogConfiguration.MDC_ERROR_DESC;
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

import java.net.InetAddress;
import java.util.ArrayList;

import org.openecomp.sdc.common.ecomplog.Enums.Severity;
import org.openecomp.sdc.common.ecomplog.api.IEcompMdcWrapper;
import org.openecomp.sdc.common.ecomplog.api.IStopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

public class EcompMDCWrapper implements IEcompMdcWrapper {

    private static EcompMDCWrapper instanceMdcWrapper = new EcompMDCWrapper(new Stopwatch());

    public static EcompMDCWrapper getInstance() {
        return instanceMdcWrapper;
    }

    private IStopWatch stopWatch;
    protected static Logger log = LoggerFactory.getLogger(EcompMDCWrapper.class.getName());
    protected ArrayList<String> mandatoryFields = new ArrayList<>();
    protected ArrayList<String> optionalFields = new ArrayList<>();
    protected static String hostAddress;
    protected static String fqdn;

    // in package classes can instantiate this class
    // to use directly from outside the package usr the getInstance() Method
    EcompMDCWrapper(IStopWatch stopwatch) {
        this.stopWatch = stopwatch;
    }

    static {
        try {
            hostAddress = InetAddress.getLocalHost().getHostAddress();
            fqdn = InetAddress.getByName(hostAddress).getCanonicalHostName();
        } catch (Exception ex) {
            log.error("failed to get machine parameters", ex);
        }
    }

    @Override
    public EcompMDCWrapper startTimer() {
        stopWatch.start();
        return this;
    }

    @Override
    public EcompMDCWrapper stopTimer() {
        try {
            stopWatch.stop();
        } catch (Exception ex) {
            log.error("StopWatch failed; probably start was not called before Stopwatch", ex);
        }
        return this;
    }

    @Override
    public EcompMDCWrapper setClassName(String className) {
        MDC.put(MDC_CLASS_NAME, className);
        return this;
    }

    // automatic parameter this is optional
    @Override
    public EcompMDCWrapper setAutoServerFQDN(String serverFQDN) {
        MDC.put(MDC_SERVER_FQDN, serverFQDN);
        return this;
    }

    // automatic parameter this is optional
    @Override
    public EcompMDCWrapper setAutoServerIPAddress(String serverIPAddress) {
        MDC.put(MDC_SERVER_IP_ADDRESS, serverIPAddress);
        return this;
    }

    @Override
    public EcompMDCWrapper setInstanceUUID(String instanceUUID) {
        MDC.put(MDC_INSTANCE_UUID, instanceUUID);
        return this;
    }

    @Override
    public EcompMDCWrapper setProcessKey(String processKey) {
        MDC.put(MDC_PROCESS_KEY, processKey);
        return this;
    }

    @Override
    public EcompMDCWrapper setAlertSeverity(Severity alertSeverity) {
        MDC.put(MDC_ALERT_SEVERITY, String.valueOf(alertSeverity.getSeverityType()));
        return this;
    }

    @Override
    public EcompMDCWrapper setOptCustomField1(String customField1) {
        MDC.put(MDC_OPT_FIELD1, customField1);
        return this;
    }

    @Override
    public EcompMDCWrapper setOptCustomField2(String customField2) {
        MDC.put(MDC_OPT_FIELD2, customField2);
        return this;
    }

    @Override
    public EcompMDCWrapper setOptCustomField3(String customField3) {
        MDC.put(MDC_OPT_FIELD3, customField3);
        return this;
    }

    @Override
    public EcompMDCWrapper setOptCustomField4(String customField4) {
        MDC.put(MDC_OPT_FIELD4, customField4);
        return this;
    }

    @Override
    public EcompMDCWrapper setKeyRequestId(String keyRequestId) {
        MDC.put(MDC_KEY_REQUEST_ID, keyRequestId); // eg. servletRequest.getSession().getId()
        return this;
    }

    @Override
    public EcompMDCWrapper setRemoteHost(String remoteHost) {
        MDC.put(MDC_REMOTE_HOST, remoteHost);
        return this;
    }

    @Override
    public EcompMDCWrapper setServiceName(String serviceName) {
        MDC.put(MDC_SERVICE_NAME, serviceName);
        return this;
    }

    @Override
    public EcompMDCWrapper setStatusCode(String statusCode) {
        MDC.put(MDC_STATUS_CODE, statusCode);
        return this;
    }

    @Override
    public EcompMDCWrapper setPartnerName(String partnerName) {
        MDC.put(MDC_PARTNER_NAME, partnerName);
        return this;
    }

    @Override
    public EcompMDCWrapper setResponseCode(int responseCode) {
        MDC.put(MDC_RESPONSE_CODE, Integer.toString(responseCode));
        return this;
    }

    @Override
    public EcompMDCWrapper setResponseDesc(String responseDesc) {
        MDC.put(MDC_RESPONSE_DESC, responseDesc);
        return this;
    }

    @Override
    public EcompMDCWrapper setServiceInstanceId(String serviceInstanceId) {
        MDC.put(MDC_SERVICE_INSTANCE_ID, serviceInstanceId);
        return this;
    }

    @Override
    public EcompMDCWrapper setTargetEntity(String targetEntity) {
        MDC.put(MDC_TARGET_ENTITY, targetEntity);
        return this;
    }

    @Override
    public EcompMDCWrapper setTargetServiceName(String targetServiceName) {
        MDC.put(MDC_TARGET_SERVICE_NAME, targetServiceName);
        return this;
    }

    @Override
    public EcompMDCWrapper setTargetVirtualEntity(String targetVirtualEntity) {
        MDC.put(MDC_TARGET_VIRTUAL_ENTITY, targetVirtualEntity);
        return this;
    }

    @Override
    public EcompMDCWrapper setErrorCode(int errorCode) {
        MDC.put(MDC_ERROR_CODE, valueOf(errorCode).toString());
        return this;
    }

    @Override
    public EcompMDCWrapper setErrorDescription(String errorDescription) {
        MDC.put(MDC_ERROR_DESC, errorDescription);
        return this;
    }

    @Override
    public void validateMandatoryFields() {
        // this method only checks if the mandatory fields have been initialized
        String filedNameThatHasNotBeenInitialized = checkMandatoryFieldsExistInMDC();

        if (MDC.getCopyOfContextMap() == null || MDC.getCopyOfContextMap().isEmpty()) {
            writeLogMDCEmptyError();
            return;
        }

        if (!"".equalsIgnoreCase(filedNameThatHasNotBeenInitialized)) {
            writeLogMissingFieldsError(filedNameThatHasNotBeenInitialized);
        }
    }

    protected void writeLogMissingFieldsError(String FiledNameThatHasNotBeenInitialized) {
        log.error("mandatory parameters for EELF logging, missing fields: %s", FiledNameThatHasNotBeenInitialized);
    }

    protected void writeLogMDCEmptyError() {
        log.error("write to log when MDC is empty error");
    }

    @Override
    public EcompMDCWrapper clear() {
        mandatoryFields.forEach(MDC::remove);
        optionalFields.forEach(MDC::remove);
        return this;
    }

    protected String checkMandatoryFieldsExistInMDC() {
        // this method returns a String of uninitialised fields
        StringBuilder missingFields = new StringBuilder();
        mandatoryFields.forEach(field -> {
            if (isMDCParamEmpty(field)) {
                missingFields.append(field).append(" ");
            }
        });
        return missingFields.toString();
    }

    @Override
    public void setMandatoryField(String parameterName) {
        mandatoryFields.add(parameterName);
    }

    @Override
    public void setOptionalField(String parameterName) {
        optionalFields.add(parameterName);
    }

    @Override
    public boolean isMDCParamEmpty(String mdcKeyName) {
        String val = MDC.get(mdcKeyName);
        return (val == null || val.trim().length() == 0);
    }

    @Override
    public String getFqdn() {
        return fqdn;
    }

    @Override
    public String getHostAddress() {
        return hostAddress;
    }
}