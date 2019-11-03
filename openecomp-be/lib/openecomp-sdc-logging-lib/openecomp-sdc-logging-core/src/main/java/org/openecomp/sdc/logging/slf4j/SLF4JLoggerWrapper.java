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

import java.text.SimpleDateFormat;
import org.openecomp.sdc.logging.api.AuditData;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.MetricsData;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

/**
 * Delegates log calls to SLF4J API and MDC.
 *
 * @author evitaliy
 * @since 08 Jan 18
 */
class SLF4JLoggerWrapper implements Logger {

    //The specified format presents time in UTC formatted per ISO 8601, as required by ONAP logging guidelines
    private static final String DATE_TIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";

    private final org.slf4j.Logger logger;

    // May cause http://www.slf4j.org/codes.html#loggerNameMismatch
    SLF4JLoggerWrapper(Class<?> clazz) {
        this(LoggerFactory.getLogger(clazz));
    }

    SLF4JLoggerWrapper(org.slf4j.Logger delegate) {
        this.logger = delegate;
    }

    SLF4JLoggerWrapper(String className) {
        this(LoggerFactory.getLogger(className));
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
        // do nothing, left for backward compatibility
    }

    @Override
    public void metrics(MetricsData data) {

        if (data == null) {
            return; // not going to fail because of null
        }

        try {
            putMetricsOnMdc(data);
            logger.info(Markers.METRICS, "");
        } finally {
            clearMetricsFromMdc();
        }
    }

    private void putMetricsOnMdc(MetricsData metrics) {

        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_TIME_PATTERN);
        unsafePutOnMdc(MetricsField.BEGIN_TIMESTAMP, dateFormat.format(metrics.getStartTime()));
        unsafePutOnMdc(MetricsField.END_TIMESTAMP, dateFormat.format(metrics.getEndTime()));
        unsafePutOnMdc(MetricsField.ELAPSED_TIME, String.valueOf(metrics.getEndTime() - metrics.getStartTime()));
        safePutOnMdc(MetricsField.RESPONSE_CODE, metrics.getResponseCode());
        safePutOnMdc(MetricsField.RESPONSE_DESCRIPTION, metrics.getResponseDescription());
        safePutOnMdc(MetricsField.CLIENT_IP_ADDRESS, metrics.getClientIpAddress());
        safePutOnMdc(MetricsField.TARGET_ENTITY, metrics.getTargetEntity());
        safePutOnMdc(MetricsField.TARGET_VIRTUAL_ENTITY, metrics.getTargetVirtualEntity());

        if (metrics.getStatusCode() != null) {
            unsafePutOnMdc(MetricsField.STATUS_CODE, metrics.getStatusCode().name());
        }
    }

    private void clearMetricsFromMdc() {
        for (MetricsField f : MetricsField.values()) {
            MDC.remove(f.asKey());
        }
    }

    private static void unsafePutOnMdc(MDCField field, String value) {
        MDC.put(field.asKey(), value);
    }

    private static void safePutOnMdc(MDCField field, String value) {
        if (value != null) {
            unsafePutOnMdc(field, value);
        }
    }

    @Override
    public boolean isAuditEnabled() {
        return logger.isInfoEnabled(Markers.EXIT);
    }

    @Override
    public void auditEntry(AuditData data) {

        if (data == null) {
            return; // not failing if null
        }

        try {
            putAuditOnMdc(data);
            logger.info(Markers.ENTRY, "");
        } finally {
            clearAuditFromMdc();
        }
    }


    @Override
    public void auditExit(AuditData data) {

        if (data == null) {
            return; // not failing if null
        }

        try {
            putAuditOnMdc(data);
            logger.info(Markers.EXIT, "");
        } finally {
            clearAuditFromMdc();
        }
    }

    private void putAuditOnMdc(AuditData audit) {

        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_TIME_PATTERN);
        unsafePutOnMdc(AuditField.BEGIN_TIMESTAMP, dateFormat.format(audit.getStartTime()));
        unsafePutOnMdc(AuditField.END_TIMESTAMP, dateFormat.format(audit.getEndTime()));
        unsafePutOnMdc(AuditField.ELAPSED_TIME, String.valueOf(audit.getEndTime() - audit.getStartTime()));
        safePutOnMdc(AuditField.RESPONSE_CODE, audit.getResponseCode());
        safePutOnMdc(AuditField.RESPONSE_DESCRIPTION, audit.getResponseDescription());
        safePutOnMdc(AuditField.CLIENT_IP_ADDRESS, audit.getClientIpAddress());

        if (audit.getStatusCode() != null) {
            unsafePutOnMdc(AuditField.STATUS_CODE, audit.getStatusCode().name());
        }
    }

    private void clearAuditFromMdc() {
        for (AuditField f : AuditField.values()) {
            MDC.remove(f.asKey());
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
