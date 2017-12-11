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
 * <p>This interface defines logging as specified by Open OPENECOMP logging requirements.</p>
 *
 * <p>Formatted messages must follow the <a href="http://www.slf4j.org/api/org/slf4j/helpers/MessageFormatter.html>SLF4J
 * format</a>.</p>
 *
 * @author evitaliy
 * @since 13/09/2016.
 */
public interface Logger {

    String getName();

    boolean isMetricsEnabled();

    void metrics(String msg);

    void metrics(String msg, Object arg);

    void metrics(String msg, Object arg1, Object arg2);

    void metrics(String msg, Object... arguments);

    void metrics(String msg, Throwable t);

    boolean isAuditEnabled();

    void audit(String msg);

    void audit(String msg, Object arg);

    void audit(String msg, Object arg1, Object arg2);

    void audit(String msg, Object... arguments);

    void audit(String msg, Throwable t);

    boolean isDebugEnabled();

    void debug(String msg);

    void debug(String msg, Object arg);

    void debug(String msg, Object arg1, Object arg2);

    void debug(String msg, Object... arguments);

    void debug(String msg, Throwable t);

    boolean isInfoEnabled();

    void info(String msg);

    void info(String msg, Object arg);

    void info(String msg, Object arg1, Object arg2);

    void info(String msg, Object... arguments);

    void info(String msg, Throwable t);

    boolean isWarnEnabled();

    void warn(String msg);

    void warn(String msg, Object arg);

    void warn(String msg, Object... arguments);

    void warn(String msg, Object arg1, Object arg2);

    void warn(String msg, Throwable t);

    boolean isErrorEnabled();

    void error(String msg);

    void error(String msg, Object arg);

    void error(String msg, Object arg1, Object arg2);

    void error(String msg, Object... arguments);

    void error(String msg, Throwable t);
}
