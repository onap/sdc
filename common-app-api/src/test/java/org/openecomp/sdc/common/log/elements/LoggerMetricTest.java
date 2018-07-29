package org.openecomp.sdc.common.log.elements;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.sdc.common.log.enums.LogLevel;
import org.openecomp.sdc.common.log.enums.Severity;
import org.openecomp.sdc.common.util.ThreadLocalsHolder;
import org.slf4j.Logger;
import org.slf4j.MDC;

import javax.ws.rs.core.Response;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.UUID;

import static org.openecomp.sdc.common.log.api.ILogConfiguration.*;
import static org.openecomp.sdc.common.log.elements.LogFieldsMdcHandler.hostAddress;

/**
 * Created by dd4296 on 12/31/2017.
 */
@RunWith(MockitoJUnitRunner.class)
public class LoggerMetricTest {
    @Mock
    private Logger logger;

    @Mock
    Response.StatusType statusType;

    private LoggerMetric metricLog;

    @Before
    public void init() {
        metricLog = new LoggerMetric(LogFieldsMdcHandler.getInstance(), logger);
    }

    @After
    public void tearDown() {
        MDC.clear();
        ThreadLocalsHolder.setUuid(null);
    }

    @Test
    public void whenNoMetricFieldsArePopulated_ShouldReturnassertEquals_onMdcMap() {
        metricLog.clear()
                .log(LogLevel.DEBUG, "some error code");
        Assert.assertNotNull(MDC.get(MDC_SERVER_FQDN));
        Assert.assertNotNull(MDC.get(MDC_SERVER_IP_ADDRESS));
    }

    @Test
    public void whenAllMetricFieldsArePopulated_ShouldReturnassertEquals_onEachMACField() throws UnknownHostException {
        String uuid = UUID.randomUUID().toString();
        ThreadLocalsHolder.setUuid(uuid);

        String hostName = InetAddress.getByName(hostAddress).getCanonicalHostName();
        String hostAddress = InetAddress.getLocalHost().getHostAddress();

        metricLog.clear()
                .startTimer()
                .stopTimer()
                .setInstanceUUID(MDC_INSTANCE_UUID)
                .setRemoteHost(MDC_REMOTE_HOST)
                .setServiceName(MDC_SERVICE_NAME)
                .setResponseCode(500)
                .setStatusCode("201")
                .setResponseDesc(MDC_RESPONSE_DESC)
                .setPartnerName(MDC_PARTNER_NAME)

                .setOptClassName(LoggerMetricTest.class.toString())
                .setOptAlertSeverity(Severity.CRITICAL)
                .setOptProcessKey(MDC_PROCESS_KEY)
                .setOptServiceInstanceId(MDC_SERVICE_INSTANCE_ID)

                .setTargetEntity(MDC_TARGET_ENTITY)
                .setTargetServiceName(MDC_TARGET_SERVICE_NAME)
                .setTargetVirtualEntity(MDC_TARGET_VIRTUAL_ENTITY)

                .log(LogLevel.DEBUG, "");


        Assert.assertFalse(LogFieldsMdcHandler.getInstance().isMDCParamEmpty(MDC_METRIC_BEGIN_TIMESTAMP));
        Assert.assertFalse(LogFieldsMdcHandler.getInstance().isMDCParamEmpty(MDC_END_TIMESTAMP));
        Assert.assertFalse(LogFieldsMdcHandler.getInstance().isMDCParamEmpty(MDC_ELAPSED_TIME));

        Assert.assertEquals(MDC.get(MDC_SERVER_IP_ADDRESS),hostAddress);
        Assert.assertEquals(MDC.get(MDC_SERVER_FQDN), hostName);
        Assert.assertEquals(MDC.get(MDC_REMOTE_HOST), MDC_REMOTE_HOST);
        Assert.assertEquals(MDC.get(MDC_STATUS_CODE) ,"201");

        Assert.assertEquals(MDC.get(MDC_KEY_REQUEST_ID), uuid);
        Assert.assertEquals(MDC.get(MDC_SERVICE_NAME) ,MDC_SERVICE_NAME);
        Assert.assertEquals(MDC.get(MDC_PARTNER_NAME) ,MDC_PARTNER_NAME);
        Assert.assertEquals(MDC.get(MDC_RESPONSE_CODE) ,"500");
        Assert.assertEquals(MDC.get(MDC_RESPONSE_DESC) ,MDC_RESPONSE_DESC);
        Assert.assertEquals(MDC.get(MDC_INSTANCE_UUID) ,MDC_INSTANCE_UUID);
        Assert.assertEquals(MDC.get(MDC_CLASS_NAME) ,LoggerMetricTest.class.toString());

        Assert.assertEquals(MDC.get(MDC_ALERT_SEVERITY) ,String.valueOf(Severity.CRITICAL.getSeverityType()));
        Assert.assertEquals(MDC.get(MDC_PROCESS_KEY) ,MDC_PROCESS_KEY);
        Assert.assertEquals(MDC.get(MDC_SERVICE_INSTANCE_ID) ,MDC_SERVICE_INSTANCE_ID);

        Assert.assertEquals(MDC.get(MDC_TARGET_ENTITY) ,MDC_TARGET_ENTITY);
        Assert.assertEquals(MDC.get(MDC_TARGET_SERVICE_NAME) ,MDC_TARGET_SERVICE_NAME);
        Assert.assertEquals(MDC.get(MDC_TARGET_VIRTUAL_ENTITY) ,MDC_TARGET_VIRTUAL_ENTITY);
    }
}
