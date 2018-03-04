package org.openecomp.sdc.common.ecomplog.api;

import org.openecomp.sdc.common.ecomplog.EcompMDCWrapper;
import org.openecomp.sdc.common.ecomplog.Enums.Severity;

/**
 * Created by dd4296 on 12/25/2017.
 */
public interface IEcompMdcWrapper {
    EcompMDCWrapper startTimer();

    EcompMDCWrapper stopTimer();

    EcompMDCWrapper setClassName(String className);

    // automatic parameter this is optional
    EcompMDCWrapper setAutoServerFQDN(String serverFQDN);

    // automatic parameter this is optional
    EcompMDCWrapper setAutoServerIPAddress(String serverIPAddress);

    EcompMDCWrapper setInstanceUUID(String instanceUUID);

    EcompMDCWrapper setProcessKey(String processKey);

    EcompMDCWrapper setAlertSeverity(Severity alertSeverity);

    EcompMDCWrapper setOptCustomField1(String customField1);

    EcompMDCWrapper setOptCustomField2(String customField2);

    EcompMDCWrapper setOptCustomField3(String customField3);

    EcompMDCWrapper setOptCustomField4(String customField4);

    EcompMDCWrapper setKeyRequestId(String keyRequestId);

    EcompMDCWrapper setRemoteHost(String remoteHost);

    EcompMDCWrapper setServiceName(String serviceName);

    EcompMDCWrapper setStatusCode(String statusCode);

    EcompMDCWrapper setPartnerName(String partnerName);

    EcompMDCWrapper setResponseCode(int responseCode);

    EcompMDCWrapper setResponseDesc(String responseDesc);

    EcompMDCWrapper setServiceInstanceId(String serviceInstanceId);

    EcompMDCWrapper setTargetEntity(String targetEntity);

    EcompMDCWrapper setTargetServiceName(String targetServiceName);

    EcompMDCWrapper setTargetVirtualEntity(String targetVirtualEntity);

    EcompMDCWrapper setErrorCode(int errorCode);

    EcompMDCWrapper setErrorDescription(String errorDescription);

    EcompMDCWrapper clear();

    void validateMandatoryFields();

    void setMandatoryField(String mdcKeyRequestId);

    void setOptionalField(String mdcKeyRequestId);

    boolean isMDCParamEmpty(String mdcKeyName);

    String getFqdn();

    String getHostAddress();
}
