package org.openecomp.sdc.logging.api;

import org.testng.annotations.Test;

import java.lang.reflect.Field;
import java.util.concurrent.Callable;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

/**
 * @author EVITALIY
 * @since 08 Jan 18
 */
public class LoggingContextTest {

    @Test
    public void testNoOpContextService() throws Exception {
        Field factory = LoggingContext.class.getDeclaredField("SERVICE");
        factory.setAccessible(true);
        Object impl = factory.get(null);
        assertEquals(impl.getClass().getName(),
                "org.openecomp.sdc.logging.api.LoggingContext$NoOpLoggingContextService");
    }

    @Test
    public void testPut() {
        final String key = "Key";
        LoggingContext.put(key, "Dummy");
        assertNull(LoggingContext.get(key));
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void testPutNull() {
        LoggingContext.put(null, "value");
    }

    @Test
    public void testGet() {
        assertNull(LoggingContext.get("GetKey"));
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void testGetNull() {
        LoggingContext.get(null);
    }

    @Test
    public void testRemove() {
        LoggingContext.remove("RemoveKey");
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void testRemoveNull() {
        LoggingContext.remove(null);
    }

    @Test
    public void testClear() {
        LoggingContext.clear();
    }

    @Test
    public void testToRunnable() {
        Runnable test = () -> { /* do nothing */ };
        assertEquals(test, LoggingContext.toRunnable(test));
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void testToRunnableNull() {
        LoggingContext.toRunnable(null);
    }

    @Test
    public void testToCallable() {
        Callable<String> test = () -> "";
        assertEquals(test, LoggingContext.toCallable(test));
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void testToCallableNull() {
        LoggingContext.toCallable(null);
    }

}