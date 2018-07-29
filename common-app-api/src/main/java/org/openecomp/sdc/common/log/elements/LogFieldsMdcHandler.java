package org.openecomp.sdc.common.log.elements;

import org.apache.commons.lang3.StringUtils;
import org.openecomp.sdc.common.log.api.ILogConfiguration;
import org.openecomp.sdc.common.log.api.ILogFieldsHandler;
import org.openecomp.sdc.common.log.enums.Severity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

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

    private final static String dateFormatPattern = "yyyy-MM-dd HH:mm:ss.SSSz";
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(dateFormatPattern);
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
        if (StringUtils.isEmpty(MDC.get(ILogConfiguration.MDC_AUDIT_BEGIN_TIMESTAMP))) {
            MDC.put(ILogConfiguration.MDC_AUDIT_BEGIN_TIMESTAMP, generatedTimeNow());
        }
    }

    @Override
    public void startMetricTimer() {
        if (StringUtils.isEmpty(MDC.get(ILogConfiguration.MDC_METRIC_BEGIN_TIMESTAMP))) {
            MDC.put(ILogConfiguration.MDC_METRIC_BEGIN_TIMESTAMP, generatedTimeNow());
        }
    }

    @Override
    public void stopAuditTimer() {
        //set start time if it is not set yet
        startAuditTimer();
        MDC.put(ILogConfiguration.MDC_END_TIMESTAMP, generatedTimeNow());
        setElapsedTime(MDC.get(ILogConfiguration.MDC_AUDIT_BEGIN_TIMESTAMP));
    }

    @Override
    public void stopMetricTimer() {
        //set start time if it is not set yet
        startMetricTimer();
        MDC.put(ILogConfiguration.MDC_END_TIMESTAMP, generatedTimeNow());
        setElapsedTime(MDC.get(ILogConfiguration.MDC_METRIC_BEGIN_TIMESTAMP));
    }

    @Override
    public void setClassName(String className) {
        MDC.put(ILogConfiguration.MDC_CLASS_NAME, className);
    }

    @Override
    public void setServerFQDN(String serverFQDN) {
        MDC.put(ILogConfiguration.MDC_SERVER_FQDN, serverFQDN);
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
        MDC.put(ILogConfiguration.MDC_INSTANCE_UUID, instanceUUID);
    }

    @Override
    public void setProcessKey(String processKey) {
        MDC.put(ILogConfiguration.MDC_PROCESS_KEY, processKey);
    }

    @Override
    public void setAlertSeverity(Severity alertSeverity) {
        MDC.put(ILogConfiguration.MDC_ALERT_SEVERITY, String.valueOf(alertSeverity.getSeverityType()));
    }

    @Override
    public void setOptCustomField1(String customField1) {
        MDC.put(ILogConfiguration.MDC_OPT_FIELD1, customField1);
    }

    @Override
    public void setOptCustomField2(String customField2) {
        MDC.put(ILogConfiguration.MDC_OPT_FIELD2, customField2);
    }

    @Override
    public void setOptCustomField3(String customField3) {
        MDC.put(ILogConfiguration.MDC_OPT_FIELD3, customField3);
    }

    @Override
    public void setOptCustomField4(String customField4) {
        MDC.put(ILogConfiguration.MDC_OPT_FIELD4, customField4);
    }

    @Override
    public void setKeyRequestId(String keyRequestId) {
        MDC.put(ILogConfiguration.MDC_KEY_REQUEST_ID, keyRequestId); // eg. servletRequest.getSession().getId()
    }

    @Override
    public void setRemoteHost(String remoteHost) {
        MDC.put(ILogConfiguration.MDC_REMOTE_HOST, remoteHost);
    }

    @Override
    public void setServiceName(String serviceName) {
        MDC.put(ILogConfiguration.MDC_SERVICE_NAME, serviceName);
    }

    @Override
    public void setStatusCode(String statusCode) {
        MDC.put(ILogConfiguration.MDC_STATUS_CODE, statusCode);
    }

    @Override
    public void setPartnerName(String partnerName) {
        MDC.put(ILogConfiguration.MDC_PARTNER_NAME, partnerName);
    }

    @Override
    public void setResponseCode(int responseCode) {
        MDC.put(ILogConfiguration.MDC_RESPONSE_CODE, Integer.toString(responseCode));
    }

    @Override
    public void setResponseDesc(String responseDesc) {
        MDC.put(ILogConfiguration.MDC_RESPONSE_DESC, responseDesc);
    }

    @Override
    public void setServiceInstanceId(String serviceInstanceId) {
        MDC.put(ILogConfiguration.MDC_SERVICE_INSTANCE_ID, serviceInstanceId);
    }

    @Override
    public void setTargetEntity(String targetEntity) {
        MDC.put(ILogConfiguration.MDC_TARGET_ENTITY, targetEntity);
    }

    @Override
    public void setTargetServiceName(String targetServiceName) {
        MDC.put(ILogConfiguration.MDC_TARGET_SERVICE_NAME, targetServiceName);
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
        return MDC.get(ILogConfiguration.MDC_SERVICE_NAME);
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
        return MDC.get(ILogConfiguration.MDC_KEY_REQUEST_ID);
    }

    @Override
    public void removeStatusCode() {
        MDC.remove(ILogConfiguration.MDC_STATUS_CODE);
    }

    @Override
    public void removePartnerName(){
        MDC.remove(ILogConfiguration.MDC_PARTNER_NAME);
    }

    @Override
    public void removeResponseCode(){
        MDC.remove(ILogConfiguration.MDC_RESPONSE_CODE);
    }

    @Override
    public void removeResponseDesc(){
        MDC.remove(ILogConfiguration.MDC_RESPONSE_DESC);
    }

    @Override
    public void removeServiceInstanceId(){
        MDC.remove(ILogConfiguration.MDC_SERVICE_INSTANCE_ID);
    }

    @Override
    public void removeTargetEntity(){
        MDC.remove(ILogConfiguration.MDC_TARGET_ENTITY);
    }

    @Override
    public void removeTargetServiceName(){
        MDC.remove(ILogConfiguration.MDC_TARGET_SERVICE_NAME);
    }

    @Override
    public void removeTargetVirtualEntity(){
        MDC.remove(ILogConfiguration.MDC_TARGET_VIRTUAL_ENTITY);
    }

    @Override
    public void removeErrorCode(){
        MDC.remove(ILogConfiguration.MDC_ERROR_CODE);
    }

    @Override
    public void removeErrorCategory(){
        MDC.remove(ILogConfiguration.MDC_ERROR_CATEGORY);
    }

    @Override
    public void removeErrorDescription(){
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

    private void setElapsedTime(String beginTimestamp) {
        try {
            final LocalDateTime startTime = LocalDateTime.parse(beginTimestamp, dateTimeFormatter);
            final LocalDateTime endTime = LocalDateTime.parse(MDC.get(ILogConfiguration.MDC_END_TIMESTAMP), dateTimeFormatter);
            final Duration timeDifference = Duration.between(startTime, endTime);

            MDC.put(ILogConfiguration.MDC_ELAPSED_TIME, String.valueOf(timeDifference.toMillis()));

        } catch(Exception ex) {
            log.error("failed to calculate elapsed time",ex);
        }
    }

    private String generatedTimeNow() {
        return dateTimeFormatter
                .withZone(ZoneOffset.UTC)
                .format(Instant.now());
    }

}