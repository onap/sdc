package org.openecomp.sdc.common.ecomplogwrapper;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.ecomplog.EcompLoggerAudit;
import org.openecomp.sdc.common.ecomplog.Enums.LogLevel;
import org.openecomp.sdc.common.ecomplog.Enums.Severity;
import org.slf4j.MDC;


/**
 * Created by dd4296 on 12/19/2017.
 *
 * audit log for asdc using the ecomplog library
 * this is adapted for filter classes
 */
public class EcompLoggerSdcAudit extends EcompLoggerSdcUtilBase {

    private static String AUDIT_ON = "auditOn";
    private String className;

    public EcompLoggerSdcAudit(String className) {
        this.className = className;
    }

    public void startLog() {
        EcompLoggerAudit
                .getInstance()
                .clear()
                .startTimer();
        MDC.put(AUDIT_ON, "true");
    }

    public static boolean isFlowBeingTakenCare(){
        try
        {
            if (MDC.get(AUDIT_ON).equals("true")){
                return true;
            }
            return false;
        }
        catch (Exception E)
        {
            return false;
        }
    }

    public void log(HttpServletRequest sr,
                    ContainerRequestContext requestContext,
                    Response.StatusType statusInfo,
                    MultivaluedMap<String, Object> responseHeaders,
                    LogLevel errorLevel,
                    Severity securityLevel,
                    String message) {

        try {
            EcompLoggerAudit.getInstance()
                    .stopTimer()
                    .setRemoteHost(sr.getRemoteAddr())
                    .setServiceName(getServiceName(requestContext))
                    .setResponseCode(convertHttpCodeToErrorCode(statusInfo.getStatusCode()))
                    .setStatusCode(Integer.toString(statusInfo.getStatusCode()))
                    .setResponseDesc(statusInfo.getReasonPhrase())
                    .setKeyRequestId(getRequestIDfromHeaders(responseHeaders.get(Constants.X_ECOMP_REQUEST_ID_HEADER)))

                    .setPartnerName(getPartnerName(
                            requestContext.getHeaderString("user-agent"),
                            requestContext.getHeaderString("USER_ID"),
                            getUrl(requestContext)))

                    .setInstanceUUID(requestContext.getHeaderString(Constants.X_ECOMP_INSTANCE_ID_HEADER))

                    .setOptServiceInstanceId(requestContext.getHeaderString(Constants.X_ECOMP_SERVICE_ID_HEADER))
                    .setOptClassName(className)
                    .setOptAlertSeverity(securityLevel)
                    .setOptCustomField1(requestContext.getMethod() + ": " + getUrl(requestContext))
                    .setOptCustomField2(Integer.toString(statusInfo.getStatusCode()))
                    .log(errorLevel, message);
        }
        catch (Exception E)
        {
            log.warn("Faild to write to Audit Log. Original Message: {}", message);
        }
        finally {
            MDC.put(AUDIT_ON,"false");
        }
    }
}
