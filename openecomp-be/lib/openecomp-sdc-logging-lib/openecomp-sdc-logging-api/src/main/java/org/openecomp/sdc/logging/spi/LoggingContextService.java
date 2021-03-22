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
package org.openecomp.sdc.logging.spi;

import java.util.concurrent.Callable;
import org.openecomp.sdc.logging.api.ContextData;

/**
 * Should be used to implement a framework-specific mechanism of managing a per-thread diagnostic context (for instance
 * <a href="http://www.slf4j.org/manual.html#mdc">MDC</a>), and propagating it to child threads if needed. Context
 * propagation should be used when creating a child thread directly, or submitting tasks for potentially postponed execution via an <a
 * href="http://docs.oracle.com/javase/8/docs/api/java/util/concurrent/Executor.html">Executor</a> (including any of the <a
 * href="http://docs.oracle.com/javase/8/docs/api/java/util/concurrent/ExecutorService.html"> executor services</a> and <a
 * href="http://docs.oracle.com/javase/8/docs/api/java/util/concurrent/ForkJoinPool.html"> ForkJoinPool</a>).
 *
 * @author evitaliy
 * @since 07 Jan 2018
 */
public interface LoggingContextService {

    /**
     * Put logging data on the context.
     */
    void put(ContextData contextData);

    /**
     * Return logging context's data.
     */
    ContextData get();

    /**
     * Clear logging thread context.
     */
    void clear();

    /**
     * Copies logging context of current thread onto a {@link Runnable}, so that the context is available when this {@link Runnable} runs in another
     * thread.
     */
    Runnable copyToRunnable(Runnable runnable);

    /**
     * Copies logging context of current thread onto a {@link Callable}, so that the context is available when this {@link Callable} runs in another
     * thread
     */
    <V> Callable<V> copyToCallable(Callable<V> callable);
}
