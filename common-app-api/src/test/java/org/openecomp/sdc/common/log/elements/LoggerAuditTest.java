package org.openecomp.sdc.common.log.elements;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.sdc.common.log.enums.EcompLoggerErrorCode;
import org.openecomp.sdc.common.log.enums.LogLevel;
import org.openecomp.sdc.common.log.enums.Severity;
import org.openecomp.sdc.common.log.enums.StatusCode;
import org.slf4j.Logger;
import org.slf4j.MDC;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.UUID;

import static org.openecomp.sdc.common.log.api.ILogConfiguration.*;
import static org.openecomp.sdc.common.log.elements.LogFieldsMdcHandler.hostAddress;

@RunWith(MockitoJUnitRunner.class)
public class LoggerAuditTest {
    @Mock
    private Logger logger;

    private LoggerAudit auditLog;

    @Before
    public void init() {
        auditLog = new LoggerAudit(LogFieldsMdcHandler.getInstance(), logger);
    }

    @After
    public void tearDown() {
        MDC.clear();
    }

    @Test
    public void whenNoAuditFieldsArePopulated_ShouldReturnAssertTrue_onMdcMap() {
        auditLog.clear()
                .log(LogLevel.INFO, "some error code");
        Assert.assertNotNull(MDC.get(MDC_SERVER_FQDN));
        Assert.assertNotNull(MDC.get(MDC_SERVER_IP_ADDRESS));
    }

    @Test
    public void whenAllAuditFieldsArePopulated_ShouldReturnAssertTrue_onEachMACField() throws UnknownHostException {

        String uuid = UUID.randomUUID().toString();
        String hostName = InetAddress.getByName(hostAddress).getCanonicalHostName();
        String hostAddress = InetAddress.getLocalHost().getHostAddress();

        auditLog.clear()
                .startTimer()
                .stopTimer()
                .setKeyRequestId(uuid)
                .setInstanceUUID(MDC_INSTANCE_UUID)
                .setRemoteHost(MDC_REMOTE_HOST)
                .setServiceName(MDC_SERVICE_NAME)
                .setResponseCode(EcompLoggerErrorCode.DATA_ERROR)
                .setStatusCode("201")
                .setResponseDesc(MDC_RESPONSE_DESC)
                .setPartnerName(MDC_PARTNER_NAME)

                .setOptClassName(LoggerAuditTest.class.toString())
                .setOptAlertSeverity(Severity.CRITICAL)
                .setOptProcessKey(MDC_PROCESS_KEY)
                .setOptServiceInstanceId(MDC_SERVICE_INSTANCE_ID)
                .log(LogLevel.DEBUG, "");


        Assert.assertFalse(LogFieldsMdcHandler.getInstance().isMDCParamEmpty(MDC_AUDIT_BEGIN_TIMESTAMP));
        Assert.assertFalse(LogFieldsMdcHandler.getInstance().isMDCParamEmpty(MDC_END_TIMESTAMP));
        Assert.assertFalse(LogFieldsMdcHandler.getInstance().isMDCParamEmpty(MDC_ELAPSED_TIME));

        Assert.assertEquals(MDC.get(MDC_SERVER_IP_ADDRESS), hostAddress);
        Assert.assertEquals(MDC.get(MDC_SERVER_FQDN), hostName);
        Assert.assertEquals(MDC.get(MDC_REMOTE_HOST), MDC_REMOTE_HOST);
        Assert.assertEquals(MDC.get(MDC_STATUS_CODE), StatusCode.COMPLETE.getStatusCodeEnum());

        Assert.assertEquals(MDC.get(MDC_KEY_REQUEST_ID), uuid);
        Assert.assertEquals(MDC.get(MDC_SERVICE_NAME), MDC_SERVICE_NAME);
        Assert.assertEquals(MDC.get(MDC_PARTNER_NAME), MDC_PARTNER_NAME);
        Assert.assertEquals(MDC.get(MDC_RESPONSE_CODE), String.valueOf(EcompLoggerErrorCode.DATA_ERROR.getErrorCode()));
        Assert.assertEquals(MDC.get(MDC_RESPONSE_DESC), MDC_RESPONSE_DESC);
        Assert.assertEquals(MDC.get(MDC_INSTANCE_UUID), MDC_INSTANCE_UUID);
        Assert.assertEquals(MDC.get(MDC_CLASS_NAME), LoggerAuditTest.class.toString());

        Assert.assertEquals(MDC.get(MDC_ALERT_SEVERITY), String.valueOf(Severity.CRITICAL.getSeverityType()));
        Assert.assertEquals(MDC.get(MDC_PROCESS_KEY), MDC_PROCESS_KEY);
        Assert.assertEquals(MDC.get(MDC_SERVICE_INSTANCE_ID), MDC_SERVICE_INSTANCE_ID);
    }
}
