package org.openecomp.sdc.common.log.api;

public interface ILogConfiguration {

  String MDC_SERVICE_INSTANCE_ID = "ServiceInstanceID";
  String MDC_SERVER_IP_ADDRESS = "ServerIPAddress";
  String MDC_REMOTE_HOST = "RemoteHost";
  String MDC_AUDIT_MESSAGE = "AuditMessage";
  String MDC_END_TIMESTAMP = "EndTimestamp";
  String MDC_ELAPSED_TIME = "ElapsedTime";
  String MDC_PROCESS_KEY = "ProcessKey";
  String MDC_TARGET_VIRTUAL_ENTITY = "TargetVirtualEntity";
  String MDC_ERROR_CATEGORY = "ErrorCategory";
  String MDC_ERROR_CODE = "ErrorCode";
  String MDC_ERROR_DESC = "ErrorDescription";
  String MDC_CLASS_NAME = "ClassName";
  String MDC_OPT_FIELD1 = "CustomField1";
  String MDC_OPT_FIELD2 = "CustomField2";
  String MDC_OPT_FIELD3 = "CustomField3";
  String MDC_OPT_FIELD4 = "CustomField4";
  String MDC_OUTGOING_INVOCATION_ID = "OutgoingInvocationId";
  String MDC_SUPPORTABLITY_ACTION = "SupportablityAction";
  String MDC_SUPPORTABLITY_CSAR_UUID="SupportablityCsarUUID";
  String MDC_SUPPORTABLITY_CSAR_VERSION="SupportablityCsarVersion";
  String MDC_SUPPORTABLITY_COMPONENT_NAME = "SupportablityComponentName";
  String MDC_SUPPORTABLITY_COMPONENT_UUID = "SupportablityComponentUUID";
  String MDC_SUPPORTABLITY_COMPONENT_VERSION="SupportablityComponentVersion";
  String MDC_SUPPORTABLITY_STATUS_CODE = "SupportablityStatus";
}