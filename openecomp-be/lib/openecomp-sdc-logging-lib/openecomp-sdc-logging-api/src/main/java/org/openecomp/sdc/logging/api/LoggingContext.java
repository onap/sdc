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

import org.openecomp.sdc.logging.provider.LoggingContextService;

import java.util.Objects;
import java.util.concurrent.Callable;

/**
 * <a>Factory to hide a concrete, framework-specific implementation of diagnostic context.</a>
 * <p>The service used by this factory must implement {@link LoggingContextService}. If no
 * implementation has been configured or could be instantiated, a <b>no-op context service</b> will be
 * used, and <b>no context</b> will be stored or propagated. No errors will be generated, so that the application can
 * still work (albeit without proper logging).</p>
 *
 * @author evitaliy
 * @since 07/01/2018.
 *
 * @see ServiceBinder
 * @see LoggingContextService
 */
public class LoggingContext {

    private static final LoggingContextService SERVICE = ServiceBinder.getContextServiceBinding().orElse(
            new NoOpLoggingContextService());

    private LoggingContext() {
        // prevent instantiation
    }

    public static void put(String key, String value) {
        SERVICE.put(key, value);
    }

    public static String get(String key) {
        return SERVICE.get(key);
    }

    public static void remove(String key) {
        SERVICE.remove(key);
    }

    public static void clear() {
        SERVICE.clear();
    }

    public static Runnable toRunnable(Runnable runnable) {
        return SERVICE.toRunnable(runnable);
    }

    public static <V> Callable<V> toCallable(Callable<V> callable) {
        return SERVICE.toCallable(callable);
    }

    private static class NoOpLoggingContextService implements LoggingContextService {

        private static final String KEY_CANNOT_BE_NULL = "Key cannot be null";

        @Override
        public void put(String key, String value) {
            Objects.requireNonNull(key, KEY_CANNOT_BE_NULL);
            // no-op
        }

        @Override
        public String get(String key) {
            Objects.requireNonNull(key, KEY_CANNOT_BE_NULL);
            return null;
        }

        @Override
        public void remove(String key) {
            Objects.requireNonNull(key, KEY_CANNOT_BE_NULL);
            // no-op
        }

        @Override
        public void clear() {
            // no-op
        }

        @Override
        public Runnable toRunnable(Runnable runnable) {
            Objects.requireNonNull(runnable, "Runnable cannot be null");
            return runnable;
        }

        @Override
        public <V> Callable<V> toCallable(Callable<V> callable) {
            Objects.requireNonNull(callable, "Callable cannot be null");
            return callable;
        }
    }
}
