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

package org.openecomp.sdc.common.util;

public class ThreadLocalsHolder {

    private static final ThreadLocal<String> UUID_THREAD_LOCAL = new ThreadLocal<>();
    private static final ThreadLocal<Long> REQUEST_START_TIME_THREAD_LOCAL = new ThreadLocal<>();
    private static final ThreadLocal<Boolean> IS_MDC_PROCESSED_THREAD_LOCAL = ThreadLocal.withInitial(() -> false);

    public static void setMdcProcessed(Boolean isMdcProcessed) {
        IS_MDC_PROCESSED_THREAD_LOCAL.set(isMdcProcessed);
    }

    public static void setUuid(String uuid) {
        UUID_THREAD_LOCAL.set(uuid);
    }

    public static void setRequestStartTime(Long requestStartTime) {
        REQUEST_START_TIME_THREAD_LOCAL.set(requestStartTime);
    }

    public static String getUuid() {
        return UUID_THREAD_LOCAL.get();
    }

    public static Long getRequestStartTime() {
        return REQUEST_START_TIME_THREAD_LOCAL.get();
    }

    public static Boolean isMdcProcessed() {
        return IS_MDC_PROCESSED_THREAD_LOCAL.get();
    }

    public static void cleanup() {
        UUID_THREAD_LOCAL.remove();
        REQUEST_START_TIME_THREAD_LOCAL.remove();
        IS_MDC_PROCESSED_THREAD_LOCAL.remove();
    }
}
