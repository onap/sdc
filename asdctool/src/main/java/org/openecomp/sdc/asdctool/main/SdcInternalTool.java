package org.openecomp.sdc.asdctool.main;

import org.openecomp.sdc.common.log.wrappers.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.core.Appender;

public abstract class SdcInternalTool {
    protected static void disableConsole() {
        org.slf4j.Logger rootLogger = LoggerFactory.getILoggerFactory().getLogger(Logger.ROOT_LOGGER_NAME);
        Appender appender = ((ch.qos.logback.classic.Logger) rootLogger).getAppender("STDOUT");
        if (appender != null) {
            appender.stop();
        }
    }
}
