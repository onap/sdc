package org.openecomp.sdc.common.log.api;

public interface ILogConfiguration {
    String MDC_KEY_REQUEST_ID = "RequestId";
    String MDC_SERVICE_INSTANCE_ID = "ServiceInstanceId";
    String MDC_SERVICE_NAME = "ServiceName";
    String MDC_INSTANCE_UUID = "InstanceUUID";
    String MDC_SERVER_IP_ADDRESS = "ServerIPAddress";
    String MDC_SERVER_FQDN = "ServerFQDN";
    String MDC_REMOTE_HOST = "RemoteHost";
    String MDC_AUDIT_MESSAGE = "AuditMessage";
    String MDC_ALERT_SEVERITY = "AlertSeverity";
    String MDC_AUDIT_BEGIN_TIMESTAMP = "AuditBeginTimestamp";
    String MDC_METRIC_BEGIN_TIMESTAMP = "MetricBeginTimestamp";
    String MDC_END_TIMESTAMP = "EndTimestamp";
    String MDC_PARTNER_NAME = "PartnerName";
    String MDC_STATUS_CODE = "StatusCode";
    String MDC_RESPONSE_CODE = "ResponseCode";
    String MDC_RESPONSE_DESC = "ResponseDescription";
    String MDC_ELAPSED_TIME = "ElapsedTime";
    String MDC_PROCESS_KEY = "ProcessKey";
    String MDC_TARGET_ENTITY = "TargetEntity";
    String MDC_TARGET_SERVICE_NAME = "TargetServiceName";
    String MDC_TARGET_VIRTUAL_ENTITY = "TargetVirtualEntity";
    String MDC_ERROR_CATEGORY = "ErrorCategory";
    String MDC_ERROR_CODE = "ErrorCode";
    String MDC_ERROR_DESC = "ErrorDescription";
    String MDC_CLASS_NAME = "ClassName";
    String MDC_OPT_FIELD1 = "CustomField1";
    String MDC_OPT_FIELD2 = "CustomField2";
    String MDC_OPT_FIELD3 = "CustomField3";
    String MDC_OPT_FIELD4 = "CustomField4";
}