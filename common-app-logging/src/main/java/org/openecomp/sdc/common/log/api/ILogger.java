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
package org.openecomp.sdc.common.log.api;

import org.openecomp.sdc.common.log.enums.LogLevel;
import org.slf4j.Marker;

import java.util.List;

public interface ILogger {
    void log(LogLevel logLevel, String message);
    void log(Marker marker, LogLevel logLevel, String message);
    void log(LogLevel logLevel, String message, Object... params);
    void log(Marker marker, LogLevel logLevel, String message, Object... params);
    void log(LogLevel logLevel, String message, Throwable throwable);
    List<String> getMandatoryFields();
    ILogger clear();
    ILogger startTimer();
    ILogger setKeyRequestId(String keyRequestId);

    ILogger setKeyInvocationId(String keyInvocationId);
}
