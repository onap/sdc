package org.openecomp.sdc.common.ecomplogwrapper;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Response;

import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.ecomplog.EcompLoggerAudit;
import org.openecomp.sdc.common.ecomplog.EcompLoggerMetric;
import org.openecomp.sdc.common.ecomplog.Enums.LogLevel;
import org.openecomp.sdc.common.ecomplog.Enums.Severity;
import org.openecomp.sdc.common.util.ThreadLocalsHolder;

/**
 * Created by dd4296 on 12/20/2017.
 *
 * METRIC log for asdc using the ecomplog library
 */
public class EcompLoggerSdcMetric extends EcompLoggerSdcUtilBase {

    private String className;

    public EcompLoggerSdcMetric(String className) {
        this.className = className;
    }

    public void startLog() {
        EcompLoggerAudit
                .getInstance()
                .clear()
                .startTimer();
    }

    public void writeToLog(HttpServletRequest sr,
                           ContainerRequestContext requestContext,
                           Response.StatusType statusInfo,
                           LogLevel errorLevel,
                           Severity securityLevel,
                           String targetEntity,
                           String targetServiceName,
                           String targetVirtualEntity,
                           String message) {

        EcompLoggerMetric.getInstance()
                .stopTimer()
                .setRemoteHost(sr.getRemoteAddr())
                .setServiceName(getServiceName(requestContext))
                .setResponseCode(statusInfo.getStatusCode())
                .setStatusCode(Integer.toString(statusInfo.getStatusCode()))
                .setResponseDesc(statusInfo.getReasonPhrase())
                .setKeyRequestId(ThreadLocalsHolder.getUuid())

                .setPartnerName(getPartnerName(
                        requestContext.getHeaderString("user-agent"),
                        requestContext.getHeaderString("USER_ID"),
                        getUrl(requestContext)))

                .setInstanceUUID(requestContext.getHeaderString(Constants.X_ECOMP_INSTANCE_ID_HEADER))

                .setOptServiceInstanceId(requestContext.getHeaderString(Constants.X_ECOMP_SERVICE_ID_HEADER))
                .setOptClassName(className)
                .setOptAlertSeverity(securityLevel)
                .setOptCustomField1(getUrl(requestContext) + "/" + requestContext.getMethod())

                .setTargetEntity(targetEntity)
                .setTargetServiceName(targetServiceName)
                .setTargetVirtualEntity(targetVirtualEntity)

                .log(errorLevel, message);
    }
}
