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

package org.openecomp.sdc.logging.api;

/**
 * <a>Factory to hide a concrete, framework-specific implementation of logger creation.</a>
 * <p>The service used by this factory must implement {@link LoggerCreationService}. If no
 * implementation has been configured or could be instantiated, a <b>no-op logger</b> will be
 * used, and <b>no events</b> will be logged. This is done to prevent recursion if attempts are
 * being made to log exceptions that resulted from logger initialization. </p>
 *
 * @author evitaliy
 * @since 13/09/2016.
 *
 * @see ServiceBinder
 * @see LoggerCreationService
 */
public class LoggerFactory {

    // use the no-op service to prevent recursion in case of an attempt to log an exception as a
    // result of a logger initialization error
    private static final LoggerCreationService SERVICE = ServiceBinder.getCreationServiceBinding().orElse(
            new NoOpLoggerCreationService());

    private LoggerFactory() {
        // prevent instantiation
    }

    public static Logger getLogger(String clazzName) {
        return SERVICE.getLogger(clazzName);
    }

    public static Logger getLogger(Class<?> clazz) {
        return SERVICE.getLogger(clazz);
    }

    private static class NoOpLoggerCreationService implements LoggerCreationService {

        private static final Logger NO_OP_LOGGER = new NoOpLogger();

        private static class NoOpLogger implements Logger {

            @Override
            public String getName() {
                return "No-Op Logger";
            }

            @Override
            public boolean isMetricsEnabled() {
                return false;
            }

            @Override
            public void metrics(String msg) {
                // no-op
            }

            @Override
            public void metrics(String msg, Object arg) {
                // no-op
            }

            @Override
            public void metrics(String msg, Object arg1, Object arg2) {
                // no-op
            }

            @Override
            public void metrics(String msg, Object... arguments) {
                // no-op
            }

            @Override
            public void metrics(String msg, Throwable t) {
                // no-op
            }

            @Override
            public boolean isAuditEnabled() {
                return false;
            }

            @Override
            public void audit(String msg) {
                // no-op
            }

            @Override
            public void audit(String msg, Object arg) {
                // no-op
            }

            @Override
            public void audit(String msg, Object arg1, Object arg2) {
                // no-op
            }

            @Override
            public void audit(String msg, Object... arguments) {
                // no-op
            }

            @Override
            public void audit(String msg, Throwable t) {
                // no-op
            }

            @Override
            public boolean isDebugEnabled() {
                return false;
            }

            @Override
            public void debug(String msg) {
                // no-op
            }

            @Override
            public void debug(String msg, Object arg) {
                // no-op
            }

            @Override
            public void debug(String msg, Object arg1, Object arg2) {
                // no-op
            }

            @Override
            public void debug(String msg, Object... arguments) {
                // no-op
            }

            @Override
            public void debug(String msg, Throwable t) {
                // no-op
            }

            @Override
            public boolean isInfoEnabled() {
                return false;
            }

            @Override
            public void info(String msg) {
                // no-op
            }

            @Override
            public void info(String msg, Object arg) {
                // no-op
            }

            @Override
            public void info(String msg, Object arg1, Object arg2) {
                // no-op
            }

            @Override
            public void info(String msg, Object... arguments) {
                // no-op
            }

            @Override
            public void info(String msg, Throwable t) {
                // no-op
            }

            @Override
            public boolean isWarnEnabled() {
                return false;
            }

            @Override
            public void warn(String msg) {
                // no-op
            }

            @Override
            public void warn(String msg, Object arg) {
                // no-op
            }

            @Override
            public void warn(String msg, Object... arguments) {
                // no-op
            }

            @Override
            public void warn(String msg, Object arg1, Object arg2) {
                // no-op
            }

            @Override
            public void warn(String msg, Throwable t) {
                // no-op
            }

            @Override
            public boolean isErrorEnabled() {
                return false;
            }

            @Override
            public void error(String msg) {
                // no-op
            }

            @Override
            public void error(String msg, Object arg) {
                // no-op
            }

            @Override
            public void error(String msg, Object arg1, Object arg2) {
                // no-op
            }

            @Override
            public void error(String msg, Object... arguments) {
                // no-op
            }

            @Override
            public void error(String msg, Throwable t) {
                // no-op
            }
        }

        @Override
        public Logger getLogger(String className) {
            return NO_OP_LOGGER;
        }

        @Override
        public Logger getLogger(Class<?> clazz) {
            return NO_OP_LOGGER;
        }
    }
}
