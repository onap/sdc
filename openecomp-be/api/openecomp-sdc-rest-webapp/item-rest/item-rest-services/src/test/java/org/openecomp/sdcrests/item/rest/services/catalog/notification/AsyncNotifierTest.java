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

import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledExecutorService;
import org.apache.commons.lang.mutable.MutableInt;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * @author evitaliy
 * @since 22 Nov 2018
 */
public class AsyncNotifierTest {

    @Test
    public void taskRunsOnceWhenSuccessful() throws Exception {

        ScheduledExecutorService executorServiceMock = Mockito.mock(ScheduledExecutorService.class);
        Mockito.doAnswer(invocation -> {
            ((Callable<?>) invocation.getArgument(0)).call();
            return null;
        }).when(executorServiceMock).submit(any(Callable.class));

        MutableInt counter = new MutableInt(0);
        Callable<AsyncNotifier.NextAction> countingTask = () -> {
            counter.add(1);
            return AsyncNotifier.NextAction.DONE;
        };

        AsyncNotifier.RetryingTask retryingTask =
                new AsyncNotifier.RetryingTask(countingTask, 10, 10, executorServiceMock);
        retryingTask.call();
        assertEquals(1, counter.intValue());
    }

    @Test
    public void taskRunsTwiceWhenFailedFirstTime() {

    }

    @Test
    public void exhaustedAttemptsWhenTaskAlwaysFails() {

    }

}