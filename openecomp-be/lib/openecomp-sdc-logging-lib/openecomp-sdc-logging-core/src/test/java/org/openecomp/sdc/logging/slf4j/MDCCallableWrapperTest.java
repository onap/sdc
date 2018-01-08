package org.openecomp.sdc.logging.slf4j;

import org.openecomp.sdc.logging.provider.LoggingContextService;
import org.testng.annotations.Test;

import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

/**
 * @author EVITALIY
 * @since 08 Jan 18
 */
public class MDCCallableWrapperTest extends BaseContextPropagationTest {

    @Test(enabled = ENABLED, dataProvider = PROVIDER)
    public void testContextPropagated(LoggingContextService ctx) throws Exception {

        String uuid = UUID.randomUUID().toString();
        ctx.put(KEY, uuid);

        AtomicBoolean complete = new AtomicBoolean(false);

        // pass the callable to the context service first
        execute(ctx.toCallable(() -> {
            assertEquals(ctx.get(KEY), uuid, EXPECT_PROPAGATED_TO_CHILD);
            complete.set(true);
            return null;
        }));

        assertEquals(ctx.get(KEY), uuid, EXPECT_RETAINED_IN_CURRENT);
        assertTrue(complete.get(), EXPECT_INNER_RUN);
    }

    @Test(enabled = ENABLED, dataProvider = PROVIDER)
    public void testContextReplacement(LoggingContextService ctx) throws Exception {

        String innerRandom = UUID.randomUUID().toString();
        ctx.put(KEY, innerRandom);

        AtomicBoolean innerComplete = new AtomicBoolean(false);

        // should run with the context of main thread
        Callable inner = ctx.toCallable(() -> {
            assertEquals(ctx.get(KEY), innerRandom, EXPECT_PROPAGATED_TO_CHILD);
            innerComplete.set(true);
            return null;
        });

        // pushes its own context, but the inner must run with its own context
        AtomicBoolean outerComplete = new AtomicBoolean(false);
        execute(() -> {
            String outerUuid = UUID.randomUUID().toString();
            ctx.put(KEY, outerUuid);
            inner.call();
            assertEquals(ctx.get(KEY), outerUuid, EXPECT_REPLACED_WITH_STORED);
            outerComplete.set(true);
            return null;
        });

        assertEquals(ctx.get(KEY), innerRandom, EXPECT_RETAINED_IN_CURRENT);
        assertTrue(outerComplete.get(), EXPECT_OUTER_RUN);
        assertTrue(innerComplete.get(), EXPECT_INNER_RUN);
    }

    @Test(enabled = ENABLED, dataProvider = PROVIDER)
    public void testContextRemainsEmpty(LoggingContextService ctx) throws Exception {

        ctx.remove(KEY);
        assertNull(ctx.get(KEY), EXPECT_EMPTY);

        final AtomicBoolean complete = new AtomicBoolean(false);
        execute(ctx.toCallable(() -> {
            assertNull(ctx.get(KEY), EXPECT_EMPTY);
            complete.set(true);
            return null;
        }));

        assertNull(ctx.get(KEY), EXPECT_EMPTY);
        assertTrue(complete.get(), EXPECT_INNER_RUN);
    }

    @Test(enabled = ENABLED, dataProvider = PROVIDER)
    public void testContextCleanedUp(LoggingContextService ctx) throws Exception {

        String innerRandom = UUID.randomUUID().toString();
        ctx.put(KEY, innerRandom);

        AtomicBoolean innerComplete = new AtomicBoolean(false);
        // should run with the context of main thread
        Callable inner = ctx.toCallable((() -> {
            assertEquals(ctx.get(KEY), innerRandom, EXPECT_PROPAGATED_TO_CHILD);
            innerComplete.set(true);
            return null;
        }));

        // pushes its own context, but runs the inner
        AtomicBoolean outerComplete = new AtomicBoolean(false);
        execute(() -> {
            assertNull(ctx.get(KEY), EXPECT_NOT_COPIED);
            inner.call();
            assertNull(ctx.get(KEY), EXPECT_REMAIN_EMPTY);
            outerComplete.set(true);
            return null;
        });

        assertEquals(ctx.get(KEY), innerRandom, EXPECT_RETAINED_IN_PARENT);
        assertTrue(outerComplete.get(), EXPECT_OUTER_RUN);
        assertTrue(innerComplete.get(), EXPECT_INNER_RUN);
    }

    @Test(enabled = ENABLED, dataProvider = PROVIDER)
    public void testCleanupAfterError(LoggingContextService ctx) throws Exception {

        String innerRandom = UUID.randomUUID().toString();
        ctx.put(KEY, innerRandom);

        // should run with the context of main thread
        AtomicBoolean innerComplete = new AtomicBoolean(false);
        Callable inner = ctx.toCallable(() -> {
            assertEquals(ctx.get(KEY), innerRandom, EXPECT_PROPAGATED_TO_CHILD);
            innerComplete.set(true);
            throw new IllegalArgumentException();
        });

        // pushes its own context, but runs the inner callable
        AtomicBoolean outerComplete = new AtomicBoolean(false);
        AtomicBoolean exceptionThrown = new AtomicBoolean(false);
        execute(() -> {

            String outerUuid = UUID.randomUUID().toString();
            ctx.put(KEY, outerUuid);
            assertEquals(ctx.get(KEY), outerUuid, EXPECT_POPULATED);

            try {
                inner.call();
            } catch (IllegalArgumentException e) {
                exceptionThrown.set(true);
            } finally {
                assertEquals(ctx.get(KEY), outerUuid, EXPECT_REVERTED_ON_EXCEPTION);
                outerComplete.set(true);
            }

            return null;
        });

        assertEquals(ctx.get(KEY), innerRandom, EXPECT_RETAINED_IN_PARENT);
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