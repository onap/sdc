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

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.openecomp.sdcrests.item.rest.services.catalog.notification.AsyncNotifier.NextAction.DONE;
import static org.openecomp.sdcrests.item.rest.services.catalog.notification.AsyncNotifier.NextAction.RETRY;

import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang.mutable.MutableInt;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

/**
 * @author evitaliy
 * @since 22 Nov 2018
 */
public class AsyncNotifierTest {

    @Test
    public void taskRunsOnceWhenSuccessful() throws Exception {

        ScheduledExecutorService executorServiceMock = createMockScheduledExecutor();

        MutableInt counter = new MutableInt(0);
        Callable<AsyncNotifier.NextAction> countingTask = () -> {
            counter.increment();
            return DONE;
        };

        AsyncNotifier.RetryingTask retryingTask =
                new AsyncNotifier.RetryingTask(countingTask, 10, 10, executorServiceMock);
        retryingTask.call();
        assertEquals(1, counter.intValue());
    }

    @Test
    public void taskRunsTwiceWhenFailedFirstTime() throws Exception {

        ScheduledExecutorService executorServiceMock = createMockScheduledExecutor();

        MutableInt counter = new MutableInt(0);
        Callable<AsyncNotifier.NextAction> countingTask = () -> {
            counter.increment();
            return counter.intValue() < 2 ? RETRY : DONE;
        };

        AsyncNotifier.RetryingTask retryingTask =
                new AsyncNotifier.RetryingTask(countingTask, 10, 10, executorServiceMock);
        retryingTask.call();
        assertEquals(2, counter.intValue());
    }

    @Test
    public void exhaustedAttemptsWhenTaskAlwaysFails() throws Exception {

        ScheduledExecutorService executorServiceMock = createMockScheduledExecutor();

        MutableInt counter = new MutableInt(0);
        Callable<AsyncNotifier.NextAction> countingTask = () -> {
            counter.increment();
            return RETRY;
        };

        final int numOfRetries = 10;
        AsyncNotifier.RetryingTask retryingTask =
                new AsyncNotifier.RetryingTask(countingTask, numOfRetries, 10, executorServiceMock);
        retryingTask.call();
        assertEquals(numOfRetries, counter.intValue());
    }

    private ScheduledExecutorService createMockScheduledExecutor() {

        ScheduledExecutorService executorServiceMock = Mockito.mock(ScheduledExecutorService.class);
        Answer passThrough = invocation -> {
            ((Callable<?>) invocation.getArgument(0)).call();
            return null;
        };

        Mockito.doAnswer(passThrough).when(executorServiceMock).submit(any(Callable.class));
        Mockito.doAnswer(passThrough).when(executorServiceMock)
                .schedule(any(Callable.class), anyLong(), any(TimeUnit.class));
        return executorServiceMock;
    }
}