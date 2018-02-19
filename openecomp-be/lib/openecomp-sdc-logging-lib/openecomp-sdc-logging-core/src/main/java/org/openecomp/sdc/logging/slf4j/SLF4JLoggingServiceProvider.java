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

import java.util.Objects;
import java.util.concurrent.Callable;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.spi.LoggingServiceProvider;
import org.slf4j.MDC;

/**
 * @author evitaliy
 * @since 13/09/2016.
 */
public class SLF4JLoggingServiceProvider implements LoggingServiceProvider {

    private static final String REQUEST_ID = "RequestId";
    private static final String SERVICE_NAME = "ServiceName";
    private static final String PARTNER_NAME = "PartnerName";

    private static final String[] ALL_FIELDS = { REQUEST_ID, SERVICE_NAME, PARTNER_NAME };

    @Override
    public Logger getLogger(String className) {
        Objects.requireNonNull(className, "Name cannot be null");
        return new SLF4JLoggerWrapper(className);
    }

    @Override
    public Logger getLogger(Class<?> clazz) {
        Objects.requireNonNull(clazz, "Class cannot be null");
        return new SLF4JLoggerWrapper(clazz);
    }

    @Override
    public void putRequestId(String value) {
        MDC.put(REQUEST_ID, value);
    }

    @Override
    public void putServiceName(String value) {
        MDC.put(SERVICE_NAME, value);
    }

    @Override
    public void putPartnerName(String value) {
        MDC.put(PARTNER_NAME, value);
    }

    @Override
    public void clear() {
        for (String f : ALL_FIELDS) {
            MDC.remove(f);
        }
    }

    @Override
    public Runnable copyToRunnable(Runnable runnable) {
        Objects.requireNonNull(runnable, "Runnable cannot be null");
        // TODO: Copy only the fields this service is responsible for
        return new MDCRunnableWrapper(runnable);
    }

    @Override
    public <V> Callable<V> copyToCallable(Callable<V> callable) {
        Objects.requireNonNull(callable, "Runnable cannot be null");
        // TODO: Copy only the fields this service is responsible for
        return new MDCCallableWrapper<>(callable);
    }

    @Override
    public String toString() {
        return this.getClass().getName();
    }
}
