/*
 * Copyright © 2016-2017 European Support Limited
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

package org.openecomp.sdc.logging;

import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerCreationService;
import org.slf4j.LoggerFactory;

/**
 * @author evitaliy
 * @since 13/09/2016.
 */
public class SLF4JLoggerCreationService implements LoggerCreationService {

    @Override
    public Logger getLogger(String className) {
        return new SLF4JWrapper(className);
    }

    @Override
    public Logger getLogger(Class<?> clazz) {
        return new SLF4JWrapper(clazz);
    }

    private class SLF4JWrapper implements Logger {

        private final org.slf4j.Logger logger;

        SLF4JWrapper(Class<?> clazz) {
            logger = LoggerFactory.getLogger(clazz);
        }

        SLF4JWrapper(String className) {
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
        public void audit(String msg) {
            logger.info(Markers.AUDIT, msg);
        }

        @Override
        public void audit(String msg, Object arg) {
            logger.info(Markers.AUDIT, msg, arg);
        }

        @Override
        public void audit(String msg, Object arg1, Object arg2) {
            logger.info(Markers.AUDIT, msg, arg1, arg2);
        }

        @Override
        public void audit(String msg, Object... arguments) {
            logger.info(Markers.AUDIT, msg, arguments);
        }

        @Override
        public void audit(String msg, Throwable t) {
            logger.info(Markers.AUDIT, msg, t);
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
}
