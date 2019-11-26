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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.logging.ref.slf4j.ONAPLogConstants;
import org.openecomp.sdc.common.log.enums.EcompLoggerErrorCode;
import org.openecomp.sdc.common.log.enums.LogLevel;
import org.openecomp.sdc.common.log.utils.EcompLogErrorCode;
import org.openecomp.sdc.common.log.utils.LoggingThreadLocalsHolder;
import org.slf4j.Logger;
import org.slf4j.MDC;

import static org.junit.Assert.assertEquals;
import static org.openecomp.sdc.common.log.api.LogConfigurationConstants.MDC_ERROR_CODE;

@RunWith(MockitoJUnitRunner.class)
public class LoggerErrorTest {
    private LoggerError errorLog;

    @Mock
    private Logger logger;
    @Before
    public void init() {
       errorLog = LoggerFactory.getMdcLogger(LoggerError.class, logger);
       MDC.clear();
    }

    @Test
    public void allFieldsArePresentTest() {
        LoggingThreadLocalsHolder.setUuid("uuid");
        errorLog.log(LogLevel.ERROR, EcompLoggerErrorCode.AVAILABILITY_TIMEOUTS_ERROR, "service", "entity", "server error");

        assertEquals(MDC.get(MDC_ERROR_CODE), String.valueOf(EcompLoggerErrorCode.AVAILABILITY_TIMEOUTS_ERROR.getErrorCode()));
        assertEquals("uuid", MDC.get(ONAPLogConstants.MDCs.REQUEST_ID));
        assertEquals("entity", MDC.get(ONAPLogConstants.MDCs.TARGET_ENTITY));
        assertEquals("service", MDC.get(ONAPLogConstants.MDCs.SERVICE_NAME));
    }

    @Test
    public void missingFieldsTest() {
        errorLog.clear()
                .log(LogLevel.ERROR,"some message");
    }

    @Test
    public void convertEcompErrorForLogging_correctName() {
        assertEquals(EcompLoggerErrorCode.AVAILABILITY_TIMEOUTS_ERROR, EcompLoggerErrorCode.getByValue(EcompLogErrorCode.E_210.name()));
    }

   @Test
    public void convertEcompErrorForLogging_correctName_2() {
        assertEquals(EcompLoggerErrorCode.DATA_ERROR, EcompLoggerErrorCode.getByValue(EcompLogErrorCode.E_399.name()));
    }

    @Test
    public void convertEcompErrorForLogging_NotConvertable() {
        assertEquals(EcompLoggerErrorCode.UNKNOWN_ERROR, EcompLoggerErrorCode.getByValue("ABC"));
    }

    @Test
    public void convertEcompErrorForLogging_NotConvertable_2() {
        assertEquals(EcompLoggerErrorCode.UNKNOWN_ERROR, EcompLoggerErrorCode.getByValue("E_ABC"));
    }

    @Test
    public void convertEcompErrorForLogging_Success() {
        assertEquals(EcompLoggerErrorCode.SUCCESS, EcompLoggerErrorCode.getByValue("E_0"));
    }
}
