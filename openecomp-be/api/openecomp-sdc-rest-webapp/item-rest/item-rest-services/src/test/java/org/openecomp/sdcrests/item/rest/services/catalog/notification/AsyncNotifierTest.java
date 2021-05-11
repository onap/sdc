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
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.openecomp.sdcrests.item.rest.services.catalog.notification.AsyncNotifier.NextAction.DONE;
import static org.openecomp.sdcrests.item.rest.services.catalog.notification.AsyncNotifier.NextAction.RETRY;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.BiFunction;
import org.apache.commons.lang3.mutable.MutableInt;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.openecomp.sdcrests.item.types.ItemAction;

/**
 * @author evitaliy
 * @since 22 Nov 2018
 */
public class AsyncNotifierTest {

    private static final String NUMBER_OF_RETRIES_MESSAGE = "Number of retries must be positive";
    private static final String DELAY_MESSAGE = "Delay must be positive";

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test(expected = NullPointerException.class)
    public void errorWhenWorkerNull() {
        new AsyncNotifier.RetryingTask(null, 10, 10, Mockito.mock(ScheduledExecutorService.class));
    }

    @Test(expected = NullPointerException.class)
    public void errorWhenSchedulerServiceNull() {
        new AsyncNotifier.RetryingTask(() -> DONE, 10, 10, null);
    }

    @Test
    public void errorWhenNumberOfRetriesNegative() {
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage(NUMBER_OF_RETRIES_MESSAGE);
        new AsyncNotifier.RetryingTask(() -> DONE, -12, 10, Mockito.mock(ScheduledExecutorService.class));
    }

    @Test
    public void errorWhenNumberOfRetriesZero() {
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage(NUMBER_OF_RETRIES_MESSAGE);
        new AsyncNotifier.RetryingTask(() -> DONE, 0, 10, Mockito.mock(ScheduledExecutorService.class));
    }

    @Test
    public void errorWhenDelayNegative() {
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage(DELAY_MESSAGE);
        new AsyncNotifier.RetryingTask(() -> DONE, 1, -77, Mockito.mock(ScheduledExecutorService.class));
    }

    @Test
    public void errorWhenDelayZero() {
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage(DELAY_MESSAGE);
        new AsyncNotifier.RetryingTask(() -> DONE, 1, 0, Mockito.mock(ScheduledExecutorService.class));
    }

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

    @Test
    public void workerExecutedWithGivenItemIdsAndAction()
            throws InterruptedException, ExecutionException, TimeoutException {

        CompletableFuture<Boolean> completed = new CompletableFuture<>();
        Callable<AsyncNotifier.NextAction> mockTask = () -> {
            completed.complete(true);
            return DONE;
        };

        final Collection<String> itemIds = Collections.singleton(UUID.randomUUID().toString());
        final ItemAction action = ItemAction.RESTORE;

        BiFunction<Collection<String>, ItemAction, Callable<AsyncNotifier.NextAction>> mockProducer = (i, a) -> {
            assertEquals(itemIds, i);
            assertEquals(action, a);
            return mockTask;
        };

        new AsyncNotifier(mockProducer, 1, 1).execute(itemIds, action);
        assertTrue(completed.get(5, TimeUnit.SECONDS));
    }
}
