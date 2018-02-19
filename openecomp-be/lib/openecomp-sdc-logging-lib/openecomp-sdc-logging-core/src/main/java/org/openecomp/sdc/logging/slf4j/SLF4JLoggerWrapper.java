/*
 * Copyright © 2016-2018 European Support Limited
 *
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
 */

package org.openecomp.sdc.logging.slf4j;

import org.openecomp.sdc.logging.api.AuditData;
import org.openecomp.sdc.logging.api.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.text.SimpleDateFormat;
import java.util.Date;

import static org.openecomp.sdc.logging.slf4j.SLF4JLoggingServiceProvider.PREFIX;

/**
 * @author EVITALIY
 * @since 08 Jan 18
 */
class SLF4JLoggerWrapper implements Logger {

    private static final String BEGIN_TIMESTAMP = PREFIX + "BeginTimestamp";
    private static final String END_TIMESTAMP = PREFIX + "EndTimestamp";
    private static final String ELAPSED_TIME = PREFIX + "ElapsedTime";
    private static final String STATUS_CODE = PREFIX + "StatusCode";
    private static final String RESPONSE_CODE = PREFIX + "ResponseCode";
    private static final String RESPONSE_DESCRIPTION = PREFIX + "ResponsDescription";
    private static final String CLIENT_IP_ADDRESS = PREFIX + "ClientIpAddress";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
    private final org.slf4j.Logger logger;

    SLF4JLoggerWrapper(Class<?> clazz) {
        logger = LoggerFactory.getLogger(clazz);
    }

    SLF4JLoggerWrapper(String className) {
        logger = LoggerFactory.getLogger(className);
    }

    @Override
    public String getName() {
        return logger.getName();
    }

    @Override
    public boolean isMetricsEnabled() {
        return logger.isInfoEnabled(Markers.METRICS);
    }

    @Override
    public void metrics(String msg) {
        logger.info(Markers.METRICS, msg);
    }

    @Override
    public void metrics(String msg, Object arg) {
        logger.info(Markers.METRICS, msg, arg);
    }

    @Override
    public void metrics(String msg, Object arg1, Object arg2) {
        logger.info(Markers.METRICS, msg, arg1, arg2);
    }

    @Override
    public void metrics(String msg, Object... arguments) {
        logger.info(Markers.METRICS, msg, arguments);
    }

    @Override
    public void metrics(String msg, Throwable t) {
        logger.info(Markers.METRICS, msg, t);
    }

    @Override
    public boolean isAuditEnabled() {
        return logger.isInfoEnabled(Markers.AUDIT);
    }

    @Override
    public void audit(AuditData data) {
        if (data == null) {
            return;
        }

        MDC.put(BEGIN_TIMESTAMP, DATE_FORMAT.format(new Date(data.getStartTime())));
        MDC.put(END_TIMESTAMP,   DATE_FORMAT.format(new Date(data.getEndTime())));

        if ((data.getEndTime() > data.getStartTime()) && (data.getStartTime() > 0)) {
            MDC.put(ELAPSED_TIME, String.valueOf(data.getEndTime() - data.getStartTime()));
        }
        if (data.getStatusCode() != null) {
            MDC.put(STATUS_CODE, data.getStatusCode().getValue());
        }
        if (data.getResponseCode() != null) {
            MDC.put(RESPONSE_CODE, data.getResponseCode());
        }
        if (data.getResponseDescription() != null) {
            MDC.put(RESPONSE_DESCRIPTION, data.getResponseDescription());
        }
        if (data.getClientIpAddress() != null) {
            MDC.put(CLIENT_IP_ADDRESS, data.getClientIpAddress());
        }

        try {
            logger.info(Markers.AUDIT, "");
        } finally {
            MDC.remove(BEGIN_TIMESTAMP);
            MDC.remove(END_TIMESTAMP);
            MDC.remove(ELAPSED_TIME);
            MDC.remove(STATUS_CODE);
            MDC.remove(RESPONSE_CODE);
            MDC.remove(RESPONSE_DESCRIPTION);
            MDC.remove(CLIENT_IP_ADDRESS);
        }
    }

    @Override
    public boolean isDebugEnabled() {
        return logger.isDebugEnabled();
    }

    @Override
    public void debug(String msg) {
        logger.debug(msg);
    }

    @Override
    public void debug(String format, Object arg) {
        logger.debug(format, arg);
    }

    @Override
    public void debug(String format, Object arg1, Object arg2) {
        logger.debug(format, arg1, arg2);
    }

    @Override
    public void debug(String format, Object... arguments) {
        logger.debug(format, arguments);
    }

    @Override
    public void debug(String msg, Throwable t) {
        logger.debug(msg, t);
    }

    @Override
    public boolean isInfoEnabled() {
        return logger.isInfoEnabled();
    }

    @Override
    public void info(String msg) {
        logger.info(msg);
    }

    @Override
    public void info(String format, Object arg) {
        logger.info(format, arg);
    }

    @Override
    public void info(String format, Object arg1, Object arg2) {
        logger.info(format, arg1, arg2);
    }

    @Override
    public void info(String format, Object... arguments) {
        logger.info(format, arguments);
    }

    @Override
    public void info(String msg, Throwable t) {
        logger.info(msg, t);
    }

    @Override
    public boolean isWarnEnabled() {
        return logger.isWarnEnabled();
    }

    @Override
    public void warn(String msg) {
        logger.warn(msg);
    }

    @Override
    public void warn(String format, Object arg) {
        logger.warn(format, arg);
    }

    @Override
    public void warn(String format, Object... arguments) {
        logger.warn(format, arguments);
    }

    @Override
    public void warn(String format, Object arg1, Object arg2) {
        logger.warn(format, arg1, arg2);
    }

    @Override
    public void warn(String msg, Throwable t) {
        logger.warn(msg, t);
    }

    @Override
    public boolean isErrorEnabled() {
        return logger.isErrorEnabled();
    }

    @Override
    public void error(String msg) {
        logger.error(msg);
    }

    @Override
    public void error(String format, Object arg) {
        logger.error(format, arg);
    }

    @Override
    public void error(String format, Object arg1, Object arg2) {
        logger.error(format, arg1, arg2);
    }

    @Override
    public void error(String format, Object... arguments) {
        logger.error(format, arguments);
    }

    @Override
    public void error(String msg, Throwable t) {
        logger.error(msg, t);
    }
}
