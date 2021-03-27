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

    void setOutgoingInvocationId(String outgoingInvocationId);

    void setStatusCode(String statusCode);

    void setResponseCode(int responseCode);

    void setResponseDesc(String responseDesc);

    void setServiceInstanceId(String serviceInstanceId);

    void setTargetVirtualEntity(String targetVirtualEntity);

    String getErrorCode();

    void setErrorCode(int errorCode);

    String getServiceName();

    void setServiceName(String serviceName);

    String getTargetEntity();

    void setTargetEntity(String targetEntity);

    String getTargetServiceName();

    void setTargetServiceName(String targetServiceName);

    String getErrorCategory();

    void setErrorCategory(String errorCategory);

    void clear();

    boolean isMDCParamEmpty(String mdcKeyName);

    String getFqdn();

    String getHostAddress();

    String getKeyRequestId();

    void setKeyRequestId(String keyRequestId);

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

    String getAuditMessage();

    void setAuditMessage(String message);

    //service supportability [US 496441]
    String getSupportablityAction();

    void setSupportablityAction(String action);

    String getSupportablityCsarUUID();

    void setSupportablityCsarUUID(String uuid);

    String getSupportablityCsarVersion();

    void setSupportablityCsarVersion(String version);

    String getSupportablityComponentName();

    void setSupportablityComponentName(String name);

    String getSupportablityComponentUUID();

    void setSupportablityComponentUUID(String UUID);

    String getSupportablityComponentVersion();

    void setSupportablityComponentVersion(String version);

    String getSupportablityStatusCode();

    void setSupportablityStatusCode(String statusCode);

    void removeSupportablityAction();

    void removeSupportablityComponentName();

    void removeSupportablityComponentUUID();

    void removeSupportablityComponentVersion();

    void removeSupportablityCsarUUID();

    void removeSupportablityCsarVersion();

    void removeSupportablityStatusCode();

    String getPartnerName();

    void setPartnerName(String partnerName);

    String getRemoteHost();

    void setRemoteHost(String remoteHost);

    String getServerIpAddress();

    CharSequence getKeyInvocationId();

    void setKeyInvocationId(String invocationId);
}
