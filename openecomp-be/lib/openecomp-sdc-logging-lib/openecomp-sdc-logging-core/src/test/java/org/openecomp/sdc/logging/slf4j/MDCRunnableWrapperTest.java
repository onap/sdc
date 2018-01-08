package org.openecomp.sdc.logging.slf4j;

import org.openecomp.sdc.logging.provider.LoggingContextService;
import org.testng.annotations.Test;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

/**
 * @author EVITALIY
 * @since 08 Jan 18
 */
public class MDCRunnableWrapperTest extends BaseContextPropagationTest {

    @Test(enabled = ENABLED, dataProvider = PROVIDER)
    public void testContextNotPropagated(LoggingContextService ctx) throws InterruptedException {

        String random = UUID.randomUUID().toString();
        ctx.put(KEY, random);

        AtomicBoolean complete = new AtomicBoolean(false);

        // create thread right away without copying context
        Thread thread = new Thread(() -> {
            assertNull(ctx.get(KEY), "Data unexpectedly copied to a child thread. " +
                    "Are you using an old version of SLF4J diagnostic context implementation (e.g. logback)?");
            complete.set(true);
        });

        thread.start();
        thread.join();

        assertEquals(ctx.get(KEY), random, EXPECT_RETAINED_IN_CURRENT);
        assertTrue(complete.get(), EXPECT_INNER_RUN);
    }

    @Test(enabled = ENABLED, dataProvider = PROVIDER)
    public void testContextPropagated(LoggingContextService ctx) throws InterruptedException {

        String uuid = UUID.randomUUID().toString();
        ctx.put(KEY, uuid);

        AtomicBoolean complete = new AtomicBoolean(false);

        // pass the runnable to the context service first
        Thread thread = new Thread(ctx.toRunnable(() -> {
            assertEquals(ctx.get(KEY), uuid, EXPECT_PROPAGATED_TO_CHILD);
            complete.set(true);
        }));

        thread.start();
        thread.join();

        assertEquals(ctx.get(KEY), uuid, EXPECT_RETAINED_IN_CURRENT);
        assertTrue(complete.get(), EXPECT_INNER_RUN);
    }

    @Test(enabled = ENABLED, dataProvider = PROVIDER)
    public void testContextReplacement(LoggingContextService ctx) throws InterruptedException {

        String innerRandom = UUID.randomUUID().toString();
        ctx.put(KEY, innerRandom);

        AtomicBoolean innerComplete = new AtomicBoolean(false);

        // should run with the context of main thread
        Runnable inner = ctx.toRunnable(() -> {
            assertEquals(ctx.get(KEY), innerRandom, EXPECT_PROPAGATED_TO_CHILD);
            innerComplete.set(true);
        });

        // pushes its own context, but the inner must run with its own context
        AtomicBoolean outerComplete = new AtomicBoolean(false);
        Thread outer = new Thread(() -> {
            String outerUuid = UUID.randomUUID().toString();
            ctx.put(KEY, outerUuid);
            inner.run();
            assertEquals(ctx.get(KEY), outerUuid, EXPECT_REPLACED_WITH_STORED);
            outerComplete.set(true);
        });

        outer.start();
        outer.join();

        assertEquals(ctx.get(KEY), innerRandom, EXPECT_RETAINED_IN_CURRENT);
        assertTrue(outerComplete.get(), EXPECT_OUTER_RUN);
        assertTrue(innerComplete.get(), EXPECT_INNER_RUN);
    }

    @Test(enabled = ENABLED, dataProvider = PROVIDER)
    public void testContextRemainsEmpty(LoggingContextService ctx) throws InterruptedException {

        ctx.remove(KEY);
        assertNull(ctx.get(KEY), EXPECT_EMPTY);

        final AtomicBoolean complete = new AtomicBoolean(false);
        Runnable runnable = ctx.toRunnable(() -> {
            assertNull(ctx.get(KEY), EXPECT_EMPTY);
            complete.set(true);
        });

        Thread thread = new Thread(runnable);
        thread.start();
        thread.join();

        assertNull(ctx.get(KEY), EXPECT_EMPTY);
        assertTrue(complete.get(), EXPECT_INNER_RUN);
    }

    @Test(enabled = ENABLED, dataProvider = PROVIDER)
    public void testContextCleanedUp(LoggingContextService ctx) throws Exception {

        String innerRandom = UUID.randomUUID().toString();
        ctx.put(KEY, innerRandom);

        AtomicBoolean innerComplete = new AtomicBoolean(false);
        // should run with the context of main thread
        Runnable inner = ctx.toRunnable(() -> {
            assertEquals(ctx.get(KEY), innerRandom, EXPECT_PROPAGATED_TO_CHILD);
            innerComplete.set(true);
        });

        // pushes its own context, but runs the inner
        AtomicBoolean outerComplete = new AtomicBoolean(false);
        Thread outer = new Thread(() -> {
            assertNull(ctx.get(KEY), EXPECT_NOT_COPIED);
            inner.run();
            assertNull(ctx.get(KEY), EXPECT_REMAIN_EMPTY);
            outerComplete.set(true);
        });

        outer.start();
        outer.join();

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
        Runnable inner = ctx.toRunnable(() -> {
            assertEquals(ctx.get(KEY), innerRandom, EXPECT_PROPAGATED_TO_CHILD);
            innerComplete.set(true);
            throw new IllegalArgumentException();
        });

        // pushes its own context, but runs the inner runnable
        AtomicBoolean outerComplete = new AtomicBoolean(false);
        AtomicBoolean exceptionThrown = new AtomicBoolean(false);
        Thread outer = new Thread(() -> {

            String outerUuid = UUID.randomUUID().toString();
            ctx.put(KEY, outerUuid);
            assertEquals(ctx.get(KEY), outerUuid, EXPECT_POPULATED);

            try {
                inner.run();
            } catch (IllegalArgumentException e) {
                exceptionThrown.set(true);
            } finally {
                assertEquals(ctx.get(KEY), outerUuid, EXPECT_REVERTED_ON_EXCEPTION);
                outerComplete.set(true);
            }
        });

        outer.start();
        outer.join();

        assertEquals(ctx.get(KEY), innerRandom, EXPECT_RETAINED_IN_PARENT);
        assertTrue(outerComplete.get(), EXPECT_OUTER_RUN);
        assertTrue(innerComplete.get(), EXPECT_INNER_RUN);
        assertTrue(exceptionThrown.get(), EXPECT_EXCEPTION_FROM_INNER);
    }


}