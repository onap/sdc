package org.openecomp.sdc.common.log.elements;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.sdc.common.config.EcompErrorCode;
import org.openecomp.sdc.common.log.enums.EcompLoggerErrorCode;
import org.openecomp.sdc.common.log.enums.LogLevel;
import org.openecomp.sdc.common.util.ThreadLocalsHolder;
import org.slf4j.Logger;
import org.slf4j.MDC;

import static org.junit.Assert.assertEquals;
import static org.openecomp.sdc.common.log.api.ILogConfiguration.*;

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
        ThreadLocalsHolder.setUuid("uuid");
        errorLog.log(LogLevel.ERROR, EcompLoggerErrorCode.AVAILABILITY_TIMEOUTS_ERROR, "service", "entity", "server error");

        Assert.assertEquals(MDC.get(MDC_ERROR_CODE), String.valueOf(EcompLoggerErrorCode.AVAILABILITY_TIMEOUTS_ERROR.getErrorCode()));
        Assert.assertEquals("uuid", MDC.get(MDC_KEY_REQUEST_ID));
        Assert.assertEquals("entity", MDC.get(MDC_TARGET_ENTITY));
        Assert.assertEquals("service", MDC.get(MDC_SERVICE_NAME));
    }

    @Test
    public void missingFieldsTest() {
        errorLog.clear()
                .log(LogLevel.ERROR,"some message");
    }

    @Test
    public void convertEcompErrorForLogging_correctName() {
        assertEquals(EcompLoggerErrorCode.AVAILABILITY_TIMEOUTS_ERROR, EcompLoggerErrorCode.getByValue(EcompErrorCode.E_210.name()));
    }

   @Test
    public void convertEcompErrorForLogging_correctName_2() {
        assertEquals(EcompLoggerErrorCode.DATA_ERROR, EcompLoggerErrorCode.getByValue(EcompErrorCode.E_399.name()));
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
