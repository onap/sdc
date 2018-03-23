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

import static org.openecomp.sdc.logging.slf4j.ContextPropagationTestHelper.EXPECT_EMPTY;
import static org.openecomp.sdc.logging.slf4j.ContextPropagationTestHelper.EXPECT_EXCEPTION_FROM_INNER;
import static org.openecomp.sdc.logging.slf4j.ContextPropagationTestHelper.EXPECT_INNER_RUN;
import static org.openecomp.sdc.logging.slf4j.ContextPropagationTestHelper.EXPECT_NOT_COPIED;
import static org.openecomp.sdc.logging.slf4j.ContextPropagationTestHelper.EXPECT_OUTER_RUN;
import static org.openecomp.sdc.logging.slf4j.ContextPropagationTestHelper.EXPECT_POPULATED;
import static org.openecomp.sdc.logging.slf4j.ContextPropagationTestHelper.EXPECT_PROPAGATED_TO_CHILD;
import static org.openecomp.sdc.logging.slf4j.ContextPropagationTestHelper.EXPECT_REMAIN_EMPTY;
import static org.openecomp.sdc.logging.slf4j.ContextPropagationTestHelper.EXPECT_REPLACED_WITH_STORED;
import static org.openecomp.sdc.logging.slf4j.ContextPropagationTestHelper.EXPECT_RETAINED_IN_CURRENT;
import static org.openecomp.sdc.logging.slf4j.ContextPropagationTestHelper.EXPECT_RETAINED_IN_PARENT;
import static org.openecomp.sdc.logging.slf4j.ContextPropagationTestHelper.EXPECT_REVERTED_ON_EXCEPTION;
import static org.openecomp.sdc.logging.slf4j.ContextPropagationTestHelper.IS_SUITABLE_LOGBACK_VERSION;
import static org.openecomp.sdc.logging.slf4j.ContextPropagationTestHelper.assertContextEmpty;
import static org.openecomp.sdc.logging.slf4j.ContextPropagationTestHelper.assertContextFields;
import static org.openecomp.sdc.logging.slf4j.ContextPropagationTestHelper.putUniqueValues;
import static org.testng.Assert.assertTrue;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.openecomp.sdc.logging.spi.LoggingContextService;
import org.testng.annotations.Test;

/**
 * Tests propagation of logging fields to Callable via the logging service.
 * 
 * @author evitaliy
 * @since 08 Jan 18
 */
@SuppressWarnings("DefaultAnnotationParam") // see the comment to ENABLED
public class CallableContextPropagationTest {

    private final LoggingContextService ctxService = new SLF4JLoggingServiceProvider();

    @Test(enabled = IS_SUITABLE_LOGBACK_VERSION)
    public void testContextPropagated() throws Exception {

        Map<ContextField, String> values = putUniqueValues();
        AtomicBoolean complete = new AtomicBoolean(false);

        // pass the callable to the context service first
        execute(ctxService.copyToCallable(() -> {
            assertContextFields(values, EXPECT_PROPAGATED_TO_CHILD);
            complete.set(true);
            return null;
        }));

        assertContextFields(values, EXPECT_RETAINED_IN_CURRENT);
        assertTrue(complete.get(), EXPECT_INNER_RUN);
    }

    @Test(enabled = IS_SUITABLE_LOGBACK_VERSION)
    public void testContextReplacement() throws Exception {

        Map<ContextField, String> innerValues = putUniqueValues();
        AtomicBoolean innerComplete = new AtomicBoolean(false);

        // should run with the context of main thread
        Callable inner = ctxService.copyToCallable(() -> {
            assertContextFields(innerValues, EXPECT_PROPAGATED_TO_CHILD);
            innerComplete.set(true);
            return null;
        });

        // pushes its own context, but the inner must run with its own context
        AtomicBoolean outerComplete = new AtomicBoolean(false);
        execute(() -> {
            Map<ContextField, String> outerValues = putUniqueValues();
            inner.call();
            assertContextFields(outerValues, EXPECT_REPLACED_WITH_STORED);
            outerComplete.set(true);
            return null;
        });

        assertContextFields(innerValues, EXPECT_RETAINED_IN_CURRENT);
        assertTrue(outerComplete.get(), EXPECT_OUTER_RUN);
        assertTrue(innerComplete.get(), EXPECT_INNER_RUN);
    }

    @Test(enabled = IS_SUITABLE_LOGBACK_VERSION)
    public void testContextRemainsEmpty() throws Exception {

        ctxService.clear();
        assertContextEmpty(EXPECT_EMPTY);

        final AtomicBoolean complete = new AtomicBoolean(false);
        execute(ctxService.copyToCallable(() -> {
            assertContextEmpty(EXPECT_EMPTY);
            complete.set(true);
            return null;
        }));

        assertContextEmpty(EXPECT_EMPTY);
        assertTrue(complete.get(), EXPECT_INNER_RUN);
    }

    @Test(enabled = IS_SUITABLE_LOGBACK_VERSION)
    public void testContextCleanedUp() throws Exception {

        Map<ContextField, String> innerValues = putUniqueValues();

        AtomicBoolean innerComplete = new AtomicBoolean(false);
        // should run with the context of main thread
        Callable inner = ctxService.copyToCallable((() -> {
            assertContextFields(innerValues, EXPECT_PROPAGATED_TO_CHILD);
            innerComplete.set(true);
            return null;
        }));

        // pushes its own context, but runs the inner
        AtomicBoolean outerComplete = new AtomicBoolean(false);
        execute(() -> {
            assertContextEmpty(EXPECT_NOT_COPIED);
            inner.call();
            assertContextEmpty(EXPECT_REMAIN_EMPTY);
            outerComplete.set(true);
            return null;
        });

        assertContextFields(innerValues, EXPECT_RETAINED_IN_PARENT);
        assertTrue(outerComplete.get(), EXPECT_OUTER_RUN);
        assertTrue(innerComplete.get(), EXPECT_INNER_RUN);
    }

    @Test(enabled = IS_SUITABLE_LOGBACK_VERSION)
    public void testCleanupAfterError() throws Exception {

        Map<ContextField, String> innerValues = putUniqueValues();

        // should run with the context of main thread
        AtomicBoolean innerComplete = new AtomicBoolean(false);
        Callable inner = ctxService.copyToCallable(() -> {
            assertContextFields(innerValues, EXPECT_PROPAGATED_TO_CHILD);
            innerComplete.set(true);
            throw new IllegalArgumentException();
        });

        // pushes its own context, but runs the inner callable
        AtomicBoolean outerComplete = new AtomicBoolean(false);
        AtomicBoolean exceptionThrown = new AtomicBoolean(false);
        execute(() -> {

            Map<ContextField, String> outerValues = putUniqueValues();
            assertContextFields(outerValues, EXPECT_POPULATED);

            try {
                inner.call();
            } catch (IllegalArgumentException e) {
                exceptionThrown.set(true);
            } finally {
                assertContextFields(outerValues, EXPECT_REVERTED_ON_EXCEPTION);
                outerComplete.set(true);
            }

            return null;
        });

        assertContextFields(innerValues, EXPECT_RETAINED_IN_PARENT);
        assertTrue(outerComplete.get(), EXPECT_OUTER_RUN);
        assertTrue(innerComplete.get(), EXPECT_INNER_RUN);
        assertTrue(exceptionThrown.get(), EXPECT_EXCEPTION_FROM_INNER);
    }

    private void execute(Callable<Object> callable) throws Exception {

        ExecutorService executor = Executors.newSingleThreadExecutor();

        try {
            Future<Object> future = executor.submit(callable);
            future.get(10, TimeUnit.SECONDS);
        } finally {
            executor.shutdown();
        }
    }
}