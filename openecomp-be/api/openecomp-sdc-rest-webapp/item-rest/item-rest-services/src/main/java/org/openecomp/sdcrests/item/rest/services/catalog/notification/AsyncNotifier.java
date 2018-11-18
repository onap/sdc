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

package org.openecomp.sdcrests.item.rest.services.catalog.notification;

import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.logging.api.LoggingContext;
import org.openecomp.sdcrests.item.types.ItemAction;

/**
 * Asynchronously runs a notification task.
 *
 * @author evitaliy
 * @since 22 Nov 2018
 */
public class AsyncNotifier implements Notifier {

    private static final ScheduledExecutorService EXECUTOR_SERVICE = Executors.newScheduledThreadPool(1);

    private static final int DEFAULT_NUM_OF_RETRIES = 2;
    private static final long DEFAULT_INTERVAL = 5000;

    private final BiFunction<Collection<String>, ItemAction, Callable<NextAction>> taskProducer;

    AsyncNotifier(BiFunction<Collection<String>, ItemAction, Callable<NextAction>> taskProducer) {
        this.taskProducer = taskProducer;
    }

    @Override
    public void execute(Collection<String> itemIds, ItemAction action) {

        Callable<AsyncNotifier.NextAction> worker = taskProducer.apply(itemIds, action);

        RetryingTask retryingTask =
                new RetryingTask(worker, DEFAULT_NUM_OF_RETRIES, DEFAULT_INTERVAL, EXECUTOR_SERVICE);

        EXECUTOR_SERVICE.submit(LoggingContext.copyToCallable(retryingTask));
    }

    public enum NextAction {
        RETRY, DONE
    }

    static class RetryingTask implements Callable<Void> {

        private static final Logger LOGGER = LoggerFactory.getLogger(RetryingTask.class);

        private final Callable<AsyncNotifier.NextAction> worker;
        private final long delay;
        private final ScheduledExecutorService scheduler;
        private volatile int retries;

        RetryingTask(Callable<AsyncNotifier.NextAction> worker, int numOfRetries, long delay,
                ScheduledExecutorService scheduler) {

            this.worker = worker;
            this.retries = numOfRetries;
            this.delay = delay;
            this.scheduler = scheduler;
        }

        @Override
        public synchronized Void call() throws Exception {

            NextAction next = worker.call();
            if (next == NextAction.DONE) {
                LOGGER.debug("Task successful: {}. Not going to retry", worker);
                return null;
            }

            if (retries == 0) {
                LOGGER.warn("Exhausted number of retries for task {}, exiting", worker);
                return null;
            }

            retries--;
            scheduler.schedule(this, delay, TimeUnit.MILLISECONDS);
            return null;
        }
    }
}
