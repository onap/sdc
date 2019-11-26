/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.openecomp.sdc.common.log.elements;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.sdc.common.log.enums.LogLevel;
import org.openecomp.sdc.common.log.utils.LoggingThreadLocalsHolder;
import org.slf4j.Logger;
import org.slf4j.MDC;

import javax.ws.rs.core.Response;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.UUID;

import static org.onap.logging.ref.slf4j.ONAPLogConstants.MDCs.*;
import static org.onap.logging.ref.slf4j.ONAPLogConstants.ResponseStatus.COMPLETE;
import static org.openecomp.sdc.common.log.api.LogConfigurationConstants.*;
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
        LoggingThreadLocalsHolder.setUuid(null);
    }

    @Test
    public void whenNoMetricFieldsArePopulated_ShouldReturnassertEquals_onMdcMap() {
        metricLog.clear()
                .log(LogLevel.DEBUG, "some error code");
        Assert.assertNotNull(MDC.get(SERVER_FQDN));
        Assert.assertNotNull(MDC.get(MDC_SERVER_IP_ADDRESS));
    }

    @Test
    public void whenAllMetricFieldsArePopulated_ShouldReturnassertEquals_onEachMACField() throws UnknownHostException {
        String uuid = UUID.randomUUID().toString();
        LoggingThreadLocalsHolder.setUuid(uuid);

        String hostName = InetAddress.getByName(hostAddress).getCanonicalHostName();
        String hostAddress = InetAddress.getLocalHost().getHostAddress();

        metricLog.clear()
                .startTimer()
                .stopTimer()
                .setInstanceUUID(INSTANCE_UUID)
                .setRemoteHost(MDC_REMOTE_HOST)
                .setServiceName(SERVICE_NAME)
                .setResponseCode(500)
                .setStatusCode(COMPLETE.name())
                .setResponseDesc(RESPONSE_DESCRIPTION)
                .setPartnerName(PARTNER_NAME)
                .setOptClassName(LoggerMetricTest.class.toString())
                .setOptServiceInstanceId(MDC_SERVICE_INSTANCE_ID)
                .setTargetEntity(TARGET_ENTITY)
                .setTargetServiceName(TARGET_SERVICE_NAME)
                .setTargetVirtualEntity(MDC_TARGET_VIRTUAL_ENTITY)
                .log(LogLevel.DEBUG, "");


        Assert.assertFalse(LogFieldsMdcHandler.getInstance().isMDCParamEmpty(INVOKE_TIMESTAMP));
        Assert.assertFalse(LogFieldsMdcHandler.getInstance().isMDCParamEmpty(MDC_END_TIMESTAMP));
        Assert.assertFalse(LogFieldsMdcHandler.getInstance().isMDCParamEmpty(MDC_ELAPSED_TIME));
        Assert.assertEquals(MDC.get(MDC_SERVER_IP_ADDRESS),hostAddress);
        Assert.assertEquals(MDC.get(SERVER_FQDN), hostName);
        Assert.assertEquals(MDC.get(MDC_REMOTE_HOST), MDC_REMOTE_HOST);
        Assert.assertEquals(MDC.get(RESPONSE_STATUS_CODE) , "COMPLETE");
        Assert.assertEquals(MDC.get(REQUEST_ID), uuid);
        Assert.assertEquals(MDC.get(SERVICE_NAME) , SERVICE_NAME);
        Assert.assertEquals(MDC.get(PARTNER_NAME) , PARTNER_NAME);
        Assert.assertEquals(MDC.get(RESPONSE_CODE) ,"500");
        Assert.assertEquals(MDC.get(RESPONSE_DESCRIPTION) , RESPONSE_DESCRIPTION);
        Assert.assertEquals(MDC.get(INSTANCE_UUID) , INSTANCE_UUID);
        Assert.assertEquals(MDC.get(MDC_CLASS_NAME) ,LoggerMetricTest.class.toString());
        Assert.assertEquals(MDC.get(MDC_SERVICE_INSTANCE_ID) ,MDC_SERVICE_INSTANCE_ID);
        Assert.assertEquals(MDC.get(TARGET_ENTITY) , TARGET_ENTITY);
        Assert.assertEquals(MDC.get(TARGET_SERVICE_NAME) , TARGET_SERVICE_NAME);
        Assert.assertEquals(MDC.get(MDC_TARGET_VIRTUAL_ENTITY) ,MDC_TARGET_VIRTUAL_ENTITY);
    }
}
