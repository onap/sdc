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

package org.openecomp.sdc.logging.slf4j;

import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.provider.LoggingServiceProvider;
import org.slf4j.MDC;

import java.util.Objects;
import java.util.concurrent.Callable;

/**
 * @author evitaliy
 * @since 13/09/2016.
 */
public class SLF4JLoggingServiceProvider implements LoggingServiceProvider {

    private static final String KEY_CANNOT_BE_NULL = "Key cannot be null";

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
    public void put(String key, String value) {
        Objects.requireNonNull(key, KEY_CANNOT_BE_NULL);
        MDC.put(key, value);
    }

    @Override
    public String get(String key) {
        Objects.requireNonNull(key, KEY_CANNOT_BE_NULL);
        return MDC.get(key);
    }

    @Override
    public void remove(String key) {
        Objects.requireNonNull(key, KEY_CANNOT_BE_NULL);
        MDC.remove(key);
    }

    @Override
    public void clear() {
        MDC.clear();
    }

    @Override
    public Runnable toRunnable(Runnable runnable) {
        Objects.requireNonNull(runnable, "Runnable cannot be null");
        return new MDCRunnableWrapper(runnable);
    }

    @Override
    public <V> Callable<V> toCallable(Callable<V> callable) {
        Objects.requireNonNull(callable, "Runnable cannot be null");
        return new MDCCallableWrapper<>(callable);
    }

    @Override
    public String toString() {
        return this.getClass().getName();
    }
}
