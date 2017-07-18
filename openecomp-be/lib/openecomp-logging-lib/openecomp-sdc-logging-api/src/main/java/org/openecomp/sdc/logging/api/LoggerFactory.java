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

package org.openecomp.sdc.logging.api;


/**
 * <a>Factory to hide a concrete, framework-specific implementation of logger creation.</a>
 * <p>The service used by this factory must implement {@link LoggerCreationService}. If no
 * implementation has been configured or could not be instantiated, a <b>no-op logger</b> will be
 * used, and <b>no events</b> will be logged. This is done to prevent recursion if attempts are
 * being made to log exceptions that resulted from logger initialization. </p>
 *
 * @author evitaliy
 * @see BaseFactory
 * @see LoggerCreationService
 * @since 13/09/2016.
 */
@SuppressWarnings("ThrowableInstanceNeverThrown")
public class LoggerFactory extends BaseFactory {

  private static final LoggerCreationService SERVICE;

  static {
    LoggerCreationService service;

    try {
      service = locateService(LoggerCreationService.class);
    } catch (Exception ex) {
      new RuntimeException("Failed to instantiate logger factory", ex).printStackTrace();
      // use the no-op service to prevent recursion in case of an attempt to log an exception as a
      // result of a logger initialization error
      service = new NoOpLoggerCreationService();
    }

    SERVICE = service;
  }

  public static Logger getLogger(String clazzName) {
    return SERVICE.getLogger(clazzName);
  }

  public static Logger getLogger(Class<?> clazz) {
    return SERVICE.getLogger(clazz);
  }

  private static class NoOpLoggerCreationService implements LoggerCreationService {

    private static final Logger NO_OP_LOGGER = new Logger() {

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
      }

      @Override
      public void metrics(String msg, Object arg) {
      }

      @Override
      public void metrics(String msg, Object arg1, Object arg2) {
      }

      @Override
      public void metrics(String msg, Object... arguments) {
      }

      @Override
      public void metrics(String msg, Throwable t) {
      }

      @Override
      public boolean isAuditEnabled() {
        return false;
      }

      @Override
      public void audit(String msg) {
      }

      @Override
      public void audit(String msg, Object arg) {
      }

      @Override
      public void audit(String msg, Object arg1, Object arg2) {
      }

      @Override
      public void audit(String msg, Object... arguments) {
      }

      @Override
      public void audit(String msg, Throwable t) {
      }

      @Override
      public boolean isDebugEnabled() {
        return false;
      }

      @Override
      public void debug(String msg) {
      }

      @Override
      public void debug(String msg, Object arg) {
      }

      @Override
      public void debug(String msg, Object arg1, Object arg2) {
      }

      @Override
      public void debug(String msg, Object... arguments) {
      }

      @Override
      public void debug(String msg, Throwable t) {
      }

      @Override
      public boolean isInfoEnabled() {
        return false;
      }

      @Override
      public void info(String msg) {
      }

      @Override
      public void info(String msg, Object arg) {
      }

      @Override
      public void info(String msg, Object arg1, Object arg2) {
      }

      @Override
      public void info(String msg, Object... arguments) {
      }

      @Override
      public void info(String msg, Throwable t) {
      }

      @Override
      public boolean isWarnEnabled() {
        return false;
      }

      @Override
      public void warn(String msg) {
      }

      @Override
      public void warn(String msg, Object arg) {
      }

      @Override
      public void warn(String msg, Object... arguments) {
      }

      @Override
      public void warn(String msg, Object arg1, Object arg2) {
      }

      @Override
      public void warn(String msg, Throwable t) {
      }

      @Override
      public boolean isErrorEnabled() {
        return false;
      }

      @Override
      public void error(String msg) {
      }

      @Override
      public void error(String msg, Object arg) {
      }

      @Override
      public void error(String msg, Object arg1, Object arg2) {
      }

      @Override
      public void error(String msg, Object... arguments) {
      }

      @Override
      public void error(String msg, Throwable t) {
      }
    };

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
