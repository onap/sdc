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

package org.openecomp.sdc.logging.api;

import org.openecomp.sdc.logging.spi.LoggingContextService;

import java.util.Objects;
import java.util.concurrent.Callable;

/**
 * <p>Factory to hide a concrete, framework-specific implementation of diagnostic context.</p>
 *
 * <p>The service used by this factory must implement {@link LoggingContextService}. If no implementation has been
 * configured or could be instantiated, a <b>no-op context service</b> will be used, and <b>no context</b> will be
 * stored or propagated. No errors will be generated, so that the application can still work (albeit without proper
 * logging).</p>
 *
 * @author evitaliy
 * @see ServiceBinder
 * @see LoggingContextService
 * @since 07 Jan 2018
 */
public class LoggingContext {

    private static final LoggingContextService SERVICE =
            ServiceBinder.getContextServiceBinding().orElseGet(NoOpLoggingContextService::new);

    private LoggingContext() {
        // prevent instantiation
    }

    public static void put(ContextData contextData) {
        SERVICE.put(contextData);
    }

    public static ContextData get() {
        return SERVICE.get();
    }

    public static void clear() {
        SERVICE.clear();
    }

    public static Runnable copyToRunnable(Runnable runnable) {
        return SERVICE.copyToRunnable(runnable);
    }

    public static <V> Callable<V> copyToCallable(Callable<V> callable) {
        return SERVICE.copyToCallable(callable);
    }

    private static class NoOpLoggingContextService implements LoggingContextService {

        @Override
        public void put(ContextData contextData) {
            Objects.requireNonNull(contextData, "Context data cannot be null");
        }

        @Override
        public ContextData get() {
            return EmptyContextData.INSTANCE;
        }

        @Override
        public void clear() {
            // no-op
        }

        @Override
        public Runnable copyToRunnable(Runnable runnable) {
            Objects.requireNonNull(runnable, "Runnable cannot be null");
            return runnable;
        }

        @Override
        public <V> Callable<V> copyToCallable(Callable<V> callable) {
            Objects.requireNonNull(callable, "Callable cannot be null");
            return callable;
        }

        private static class EmptyContextData {

            private static final ContextData INSTANCE = ContextData.builder().build();

            private EmptyContextData() {
                // prevent instantiation
            }
        }
    }
}
