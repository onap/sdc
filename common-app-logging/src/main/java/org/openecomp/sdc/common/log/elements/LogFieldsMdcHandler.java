package org.openecomp.sdc.common.log.elements;

import org.apache.commons.lang3.StringUtils;
import org.onap.logging.ref.slf4j.ONAPLogConstants;
import org.openecomp.sdc.common.log.enums.ConstantsLogging;
import org.openecomp.sdc.common.log.api.ILogConfiguration;
import org.openecomp.sdc.common.log.api.ILogFieldsHandler;
import org.openecomp.sdc.common.log.enums.Severity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import static java.lang.Integer.valueOf;

public class LogFieldsMdcHandler implements ILogFieldsHandler {

  private static LogFieldsMdcHandler instanceMdcWrapper = new LogFieldsMdcHandler();

  public static LogFieldsMdcHandler getInstance() {
    return instanceMdcWrapper;
  }

  private final static String dateFormatPattern = "yyyy-MM-dd'T'HH:mm:ss.SSSz";
  private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter
      .ofPattern(dateFormatPattern);
  protected static Logger log = LoggerFactory.getLogger(LogFieldsMdcHandler.class.getName());
  protected static String hostAddress;
  protected static String fqdn;

  static {
    try {
      hostAddress = InetAddress.getLocalHost().getHostAddress();
      fqdn = InetAddress.getByName(hostAddress).getCanonicalHostName();
    } catch (Exception ex) {
      log.error("failed to get machine parameters", ex);
    }
  }

  @Override
  public void startAuditTimer() {
    if (StringUtils.isEmpty(MDC.get(ONAPLogConstants.MDCs.ENTRY_TIMESTAMP))) {
      MDC.put(ONAPLogConstants.MDCs.ENTRY_TIMESTAMP, generatedTimeNow());
    }
  }

  @Override
  public void startMetricTimer() {
    if (StringUtils.isEmpty(MDC.get(ONAPLogConstants.MDCs.INVOKE_TIMESTAMP))) {
      MDC.put(ONAPLogConstants.MDCs.INVOKE_TIMESTAMP, generatedTimeNow());
    }
  }

  @Override
  public void stopAuditTimer() {
    //set start time if it is not set yet
    startAuditTimer();
    MDC.put(ILogConfiguration.MDC_END_TIMESTAMP, generatedTimeNow());
    setElapsedTime(MDC.get(ONAPLogConstants.MDCs.ENTRY_TIMESTAMP));
  }

  @Override
  public void stopMetricTimer() {
    //set start time if it is not set yet
    startMetricTimer();
    MDC.put(ILogConfiguration.MDC_END_TIMESTAMP, generatedTimeNow());
    setElapsedTime(MDC.get(ONAPLogConstants.MDCs.INVOKE_TIMESTAMP));
  }

  @Override
  public void setClassName(String className) {
    MDC.put(ILogConfiguration.MDC_CLASS_NAME, className);
  }

  @Override
  public void setServerFQDN(String serverFQDN) {
    MDC.put(ONAPLogConstants.MDCs.SERVER_FQDN, serverFQDN);
  }

  @Override
  public void setServerIPAddress(String serverIPAddress) {
    MDC.put(ILogConfiguration.MDC_SERVER_IP_ADDRESS, serverIPAddress);
  }

  @Override
  public void setServerFQDNInternally() {
    setServerFQDN(fqdn);
  }

  @Override
  public void setServerIPAddressInternally() {
    setServerIPAddress(hostAddress);
  }

  @Override
  public void setInstanceUUID(String instanceUUID) {
    MDC.put(ONAPLogConstants.MDCs.INSTANCE_UUID, instanceUUID);
  }

  @Override
  public void setProcessKey(String processKey) {
    MDC.put(ILogConfiguration.MDC_PROCESS_KEY, processKey);
  }

  @Override
  public void setAlertSeverity(Severity alertSeverity) {
    MDC.put(ONAPLogConstants.MDCs.RESPONSE_SEVERITY, String.valueOf(alertSeverity.getSeverityType()));
  }

  @Override
  public void setOptCustomField1(String customField1) {
    MDC.put(ILogConfiguration.MDC_OPT_FIELD1, customField1);
  }

  @Override
  public void setOutgoingInvocationId(String outgoingInvocationId) {
    MDC.put(ILogConfiguration.MDC_OUTGOING_INVOCATION_ID, outgoingInvocationId);
  }

  @Override
  public void setKeyRequestId(String keyRequestId) {
    MDC.put(ONAPLogConstants.MDCs.REQUEST_ID,
        keyRequestId); // eg. servletRequest.getSession().getId()
  }

  @Override
  public void setKeyInvocationId(String invocationId ) {
    MDC.put(ONAPLogConstants.MDCs.INVOCATION_ID,
            invocationId);
  }

  @Override
  public void setRemoteHost(String remoteHost) {
    MDC.put(ILogConfiguration.MDC_REMOTE_HOST, remoteHost);
  }

  @Override
  public void setServiceName(String serviceName) {
    MDC.put(ONAPLogConstants.MDCs.SERVICE_NAME, serviceName);
  }

  @Override
  public void setStatusCode(String statusCode) {
    MDC.put(ONAPLogConstants.MDCs.RESPONSE_STATUS_CODE, statusCode);
  }

  @Override
  public void setPartnerName(String partnerName) {
    MDC.put(ONAPLogConstants.MDCs.PARTNER_NAME, partnerName);
  }

  @Override
  public void setResponseCode(int responseCode) {
    MDC.put(ONAPLogConstants.MDCs.RESPONSE_CODE, Integer.toString(responseCode));
  }

  @Override
  public void setResponseDesc(String responseDesc) {
    MDC.put(ONAPLogConstants.MDCs.RESPONSE_DESCRIPTION, responseDesc);
  }

  @Override
  public void setServiceInstanceId(String serviceInstanceId) {
    MDC.put(ILogConfiguration.MDC_SERVICE_INSTANCE_ID, serviceInstanceId);
  }

  @Override
  public void setTargetEntity(String targetEntity) {
    MDC.put(ONAPLogConstants.MDCs.TARGET_ENTITY, targetEntity);
  }

  @Override
  public void setTargetServiceName(String targetServiceName) {
    MDC.put(ONAPLogConstants.MDCs.TARGET_SERVICE_NAME, targetServiceName);
  }

  @Override
  public void setTargetVirtualEntity(String targetVirtualEntity) {
    MDC.put(ILogConfiguration.MDC_TARGET_VIRTUAL_ENTITY, targetVirtualEntity);
  }

  @Override
  public void setErrorCode(int errorCode) {
    MDC.put(ILogConfiguration.MDC_ERROR_CODE, valueOf(errorCode).toString());
  }

  @Override
  public void setErrorCategory(String errorCategory) {
    MDC.put(ILogConfiguration.MDC_ERROR_CATEGORY, errorCategory);
  }

  @Override
  public String getErrorCode() {
    return MDC.get(ILogConfiguration.MDC_ERROR_CODE);
  }

  @Override
  public String getServiceName() {
    return MDC.get(ONAPLogConstants.MDCs.SERVICE_NAME);
  }

  @Override
  public String getErrorCategory() {
    return MDC.get(ILogConfiguration.MDC_ERROR_CATEGORY);
  }

  @Override
  public void clear() {
    MDC.clear();
  }

  @Override
  public boolean isMDCParamEmpty(String mdcKeyName) {
    return StringUtils.isEmpty(MDC.get(mdcKeyName));
  }

  @Override
  public String getFqdn() {
    return fqdn;
  }

  @Override
  public String getHostAddress() {
    return hostAddress;
  }

  @Override
  public String getKeyRequestId() {
    return MDC.get(ONAPLogConstants.MDCs.REQUEST_ID);
  }

  @Override
  public String getTargetEntity() {
    return MDC.get(ONAPLogConstants.MDCs.TARGET_ENTITY);
  }

  @Override
  public String getTargetServiceName() {
    return MDC.get(ONAPLogConstants.MDCs.TARGET_SERVICE_NAME);
  }

  @Override
  public void removeStatusCode() {
    MDC.remove(ONAPLogConstants.MDCs.RESPONSE_STATUS_CODE);
  }

  @Override
  public void removePartnerName() {
    MDC.remove(ONAPLogConstants.MDCs.PARTNER_NAME);
  }

  @Override
  public void removeResponseCode() {
    MDC.remove(ONAPLogConstants.MDCs.RESPONSE_CODE);
  }

  @Override
  public void removeResponseDesc() {
    MDC.remove(ONAPLogConstants.MDCs.RESPONSE_DESCRIPTION);
  }

  @Override
  public void removeServiceInstanceId() {
    MDC.remove(ILogConfiguration.MDC_SERVICE_INSTANCE_ID);
  }

  @Override
  public void removeTargetEntity() {
    MDC.remove(ONAPLogConstants.MDCs.TARGET_ENTITY);
  }

  @Override
  public void removeTargetServiceName() {
    MDC.remove(ONAPLogConstants.MDCs.TARGET_SERVICE_NAME);
  }

  @Override
  public void removeTargetVirtualEntity() {
    MDC.remove(ILogConfiguration.MDC_TARGET_VIRTUAL_ENTITY);
  }

  @Override
  public void removeErrorCode() {
    MDC.remove(ILogConfiguration.MDC_ERROR_CODE);
  }

  @Override
  public void removeErrorCategory() {
    MDC.remove(ILogConfiguration.MDC_ERROR_CATEGORY);
  }

  @Override
  public void removeErrorDescription() {
    MDC.remove(ILogConfiguration.MDC_ERROR_DESC);
  }

  @Override
  public void setAuditMessage(String message) {
    MDC.put(ILogConfiguration.MDC_AUDIT_MESSAGE, message);
  }

  @Override
  public String getAuditMessage() {
    return MDC.get(ILogConfiguration.MDC_AUDIT_MESSAGE);
  }


  @Override
  public String getSupportablityStatusCode() {
    return MDC.get(ILogConfiguration.MDC_SUPPORTABLITY_STATUS_CODE);
  }

  @Override
  public String getSupportablityAction() {
    return MDC.get(ILogConfiguration.MDC_SUPPORTABLITY_ACTION);

  }


  @Override
  public String getRemoteHost() {
    return MDC.get(ILogConfiguration.MDC_REMOTE_HOST);
  }

  @Override
  public String getServerIpAddress() {
    return MDC.get(ILogConfiguration.MDC_SERVER_IP_ADDRESS);
  }

//  @Override
//  public String getSupportablityCsarName() {
//    return MDC.get(ILogConfiguration.MDC_SUPPORTABLITY_CSAR_NAME);
//  }

  @Override
  public String getSupportablityCsarUUID() {
    return MDC.get(ILogConfiguration.MDC_SUPPORTABLITY_CSAR_UUID);
  }

  @Override
  public String getSupportablityCsarVersion() {
    return MDC.get(ILogConfiguration.MDC_SUPPORTABLITY_CSAR_VERSION);

  }

  @Override
  public String getSupportablityComponentName() {
    return MDC.get(ILogConfiguration.MDC_SUPPORTABLITY_COMPONENT_NAME);
  }

  @Override
  public String getSupportablityComponentUUID() {
    return MDC.get(ILogConfiguration.MDC_SUPPORTABLITY_COMPONENT_UUID);

  }

  @Override
  public String getSupportablityComponentVersion() {
    return MDC.get(ILogConfiguration.MDC_SUPPORTABLITY_COMPONENT_VERSION);
  }

  @Override
  public String getKeyInvocationId() {
    return MDC.get(ONAPLogConstants.MDCs.INVOCATION_ID);
  }

  @Override
  public void setSupportablityStatusCode(String statusCode) {
    MDC.put(ILogConfiguration.MDC_SUPPORTABLITY_STATUS_CODE, statusCode);
  }

  @Override
  public void setSupportablityAction(String action) {
    MDC.put(ILogConfiguration.MDC_SUPPORTABLITY_ACTION, action);
  }

  @Override
  public void setSupportablityCsarUUID(String uuid) {
    MDC.put(ILogConfiguration.MDC_SUPPORTABLITY_CSAR_UUID, uuid);
  }

  @Override
  public void setSupportablityCsarVersion(String version) {
    MDC.put(ILogConfiguration.MDC_SUPPORTABLITY_CSAR_VERSION, version);
  }

  @Override
  public void setSupportablityComponentName(String name) {
    MDC.put(ILogConfiguration.MDC_SUPPORTABLITY_COMPONENT_NAME, name);
  }

  @Override
  public void setSupportablityComponentUUID(String uuid) {
    MDC.put(ILogConfiguration.MDC_SUPPORTABLITY_COMPONENT_UUID, uuid);
  }

  @Override
  public void setSupportablityComponentVersion(String version) {
    MDC.put(ILogConfiguration.MDC_SUPPORTABLITY_COMPONENT_VERSION, version);
  }

  @Override
  public void removeSupportablityAction() {
    MDC.remove(ILogConfiguration.MDC_SUPPORTABLITY_ACTION);
  }

  @Override
  public void removeSupportablityComponentName() {
    MDC.remove(ILogConfiguration.MDC_SUPPORTABLITY_COMPONENT_NAME);
  }

  @Override
  public void removeSupportablityComponentUUID() {
    MDC.remove(ILogConfiguration.MDC_SUPPORTABLITY_COMPONENT_UUID);
  }

  @Override
  public void removeSupportablityComponentVersion() {
    MDC.remove(ILogConfiguration.MDC_SUPPORTABLITY_COMPONENT_VERSION);
  }

//  @Override
//  public void removeSupportablityCsarName() {
//    MDC.remove(ILogConfiguration.MDC_SUPPORTABLITY_CSAR_NAME);
//  }

  @Override
  public void removeSupportablityCsarUUID() {
    MDC.remove(ILogConfiguration.MDC_SUPPORTABLITY_CSAR_UUID);
  }

  @Override
  public void removeSupportablityCsarVersion() {
    MDC.remove(ILogConfiguration.MDC_SUPPORTABLITY_CSAR_VERSION);
  }

  @Override
  public void removeSupportablityStatusCode() {
    MDC.remove(ILogConfiguration.MDC_SUPPORTABLITY_STATUS_CODE);
  }

  @Override
  public String getPartnerName() {
    return MDC.get(ONAPLogConstants.MDCs.PARTNER_NAME);
  }

  private void setElapsedTime(String beginTimestamp) {
    try {
      final LocalDateTime startTime = LocalDateTime.parse(beginTimestamp, dateTimeFormatter);
      final LocalDateTime endTime = LocalDateTime
          .parse(MDC.get(ILogConfiguration.MDC_END_TIMESTAMP), dateTimeFormatter);
      final Duration timeDifference = Duration.between(startTime, endTime);

      MDC.put(ILogConfiguration.MDC_ELAPSED_TIME, String.valueOf(timeDifference.toMillis()));

    } catch (Exception ex) {
      log.error("failed to calculate elapsed time", ex);
    }
  }

  private String generatedTimeNow() {
    return dateTimeFormatter
        .withZone(ZoneOffset.UTC)
        .format(Instant.now());
  }

  public void collectRequestInfoForErrorAndDebugLogging(HttpServletRequest httpRequest) {
    LogFieldsMdcHandler.getInstance().clear();
    String partnerName = LoggerBase.getPartnerName(httpRequest);
    LogFieldsMdcHandler.getInstance().setPartnerName(partnerName);

    String serviceInstanceID = httpRequest.getHeader(ConstantsLogging.X_ECOMP_SERVICE_ID_HEADER);
    LogFieldsMdcHandler.getInstance().setServiceInstanceId(serviceInstanceID);

    LogFieldsMdcHandler.getInstance().setRemoteHost(httpRequest.getRemoteHost());
    LogFieldsMdcHandler.getInstance().setServerIPAddress(httpRequest.getLocalAddr());

    String requestId = LoggerBase.getRequestId(httpRequest);
    LogFieldsMdcHandler.getInstance().setKeyRequestId(requestId);

    LogFieldsMdcHandler.getInstance().setServiceName(httpRequest.getRequestURI());
  }

  public void addInfoForErrorAndDebugLogging(String partnerName){
    LogFieldsMdcHandler.getInstance().clear();
    LogFieldsMdcHandler.getInstance().setPartnerName(partnerName);

    String requestId = LoggerBase.generateKeyRequestId();
    LogFieldsMdcHandler.getInstance().setKeyRequestId(requestId);
  }
}
