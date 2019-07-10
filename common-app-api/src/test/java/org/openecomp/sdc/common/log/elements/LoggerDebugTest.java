/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

/**
 * Created by dd4296 on 12/25/2017.
 */

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.sdc.common.log.enums.LogLevel;
import org.openecomp.sdc.common.util.ThreadLocalsHolder;
import org.slf4j.Logger;
import org.slf4j.MDC;

import java.net.UnknownHostException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.openecomp.sdc.common.log.api.ILogConfiguration.*;

@RunWith(MockitoJUnitRunner.class)
public class LoggerDebugTest {

    @Mock
    private Logger logger;

    private LoggerDebug debugLog;

     @Before
    public void init() {
        debugLog = new LoggerDebug(LogFieldsMdcHandler.getInstance(), logger);
        ThreadLocalsHolder.setUuid(null);
        MDC.clear();
    }

    @Test
    public void whenNoFieldsIsPopulated_RequestedMdcFieldsAreEmpty() {
        debugLog.clear()
                .log(LogLevel.DEBUG, "some error code");
        assertNull(MDC.get(MDC_KEY_REQUEST_ID));
    }

    @Test
    public void debugLogCheckValidationValidFieldsTest() {
        debugLog.clear()
                .startTimer()
                .setKeyRequestId("uuid")
                .log(LogLevel.DEBUG, "some error code");

        Assert.assertEquals(MDC.get(MDC_KEY_REQUEST_ID), "uuid");
    }

    @Test
    public void whenOnlyDebugUUIDFieldsIsPopulated_ShouldReturnAssertTrue_onUUIDFieldCheck() {
        debugLog.clear()
                .setKeyRequestId("uuid")
                .log(LogLevel.DEBUG, "some error code");

        Assert.assertEquals(MDC.get(MDC_KEY_REQUEST_ID), "uuid");
    }

    @Test
    public void whenAllDebugFieldsArePopulated_ShouldReturnAssertTrue_onEachMACFieldCheck() throws UnknownHostException {
        debugLog.clear()
                .startTimer()
                .setKeyRequestId(MDC_KEY_REQUEST_ID)
                .log(LogLevel.DEBUG, "some message");

        Assert.assertTrue(LogFieldsMdcHandler.getInstance().isMDCParamEmpty(MDC_END_TIMESTAMP));
        Assert.assertTrue(LogFieldsMdcHandler.getInstance().isMDCParamEmpty(MDC_ELAPSED_TIME));
        Assert.assertTrue(LogFieldsMdcHandler.getInstance().isMDCParamEmpty(MDC_STATUS_CODE));
    }


    @Test
    public void validateMandatoryFields(){
        assertEquals(MDC_KEY_REQUEST_ID, debugLog.checkMandatoryFieldsExistInMDC().trim());
    }

    @Test
    public void validateMandatoryFieldsWhenFieldIsSet(){
        debugLog.clear()
                .setKeyRequestId("1234");
        assertEquals("", debugLog.checkMandatoryFieldsExistInMDC());
    }
}
