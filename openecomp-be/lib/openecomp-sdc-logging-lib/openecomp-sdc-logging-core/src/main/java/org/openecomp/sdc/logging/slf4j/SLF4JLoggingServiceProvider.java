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
import org.openecomp.sdc.logging.api.ContextData;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.spi.LoggingServiceProvider;

/**
 * Uses SLF4J as backend for logging service.
 *
 * @author evitaliy
 * @since 13 Sep 2016
 */
public class SLF4JLoggingServiceProvider implements LoggingServiceProvider {

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
    public void put(ContextData contextData) {
        Objects.requireNonNull(contextData, "Context data cannot be null");
        MDCDelegate.put(RequestContextProvider.from(contextData), new GlobalContextProvider());
    }

    @Override
    public ContextData get() {
        return RequestContextProvider.to(MDCDelegate.get());
    }

    @Override
    public void clear() {
        MDCDelegate.clear();
    }

    @Override
    public Runnable copyToRunnable(Runnable runnable) {
        Objects.requireNonNull(runnable, "Runnable cannot be null");
        return new MDCRunnableWrapper(runnable);
    }

    @Override
    public <V> Callable<V> copyToCallable(Callable<V> callable) {
        Objects.requireNonNull(callable, "Runnable cannot be null");
        return new MDCCallableWrapper<>(callable);
    }

    @Override
    public String toString() {
        return this.getClass().getName();
    }
}
