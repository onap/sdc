package org.openecomp.sdc.common.log.api;

import org.openecomp.sdc.common.log.enums.Severity;

/**
 * Created by dd4296 on 12/25/2017.
 */
public interface ILogFieldsHandler {
    void startAuditTimer();

    void startMetricTimer();

    void stopAuditTimer();

    void stopMetricTimer();

    void setClassName(String className);

    void setServerFQDN(String serverFQDN);

    void setServerIPAddress(String serverIPAddress);

    // intended for setting this parameter in a given thread
    void setServerFQDNInternally();

    // intended for setting this parameter in a given thread
    void setServerIPAddressInternally();

    void setInstanceUUID(String instanceUUID);

    void setProcessKey(String processKey);

    void setAlertSeverity(Severity alertSeverity);

    void setOptCustomField1(String customField1);

    void setOptCustomField2(String customField2);

    void setOptCustomField3(String customField3);

    void setOptCustomField4(String customField4);

    void setKeyRequestId(String keyRequestId);

    void setRemoteHost(String remoteHost);

    void setServiceName(String serviceName);

    void setStatusCode(String statusCode);

    void setPartnerName(String partnerName);

    void setResponseCode(int responseCode);

    void setResponseDesc(String responseDesc);

    void setServiceInstanceId(String serviceInstanceId);

    void setTargetEntity(String targetEntity);

    void setTargetServiceName(String targetServiceName);

    void setTargetVirtualEntity(String targetVirtualEntity);

    void setErrorCode(int errorCode);

    void setErrorCategory(String errorCategory);

    String getErrorCode();

    String getServiceName();

    String getErrorCategory();

    void clear();

    boolean isMDCParamEmpty(String mdcKeyName);

    String getFqdn();

    String getHostAddress();

    String getKeyRequestId();

    void removeStatusCode();

    void removePartnerName();

    void removeResponseCode();

    void removeResponseDesc();

    void removeServiceInstanceId();

    void removeTargetEntity();

    void removeTargetServiceName();

    void removeTargetVirtualEntity();

    void removeErrorCode();

    void removeErrorCategory();

    void removeErrorDescription();

    void setAuditMessage(String message);

    String getAuditMessage();

}
