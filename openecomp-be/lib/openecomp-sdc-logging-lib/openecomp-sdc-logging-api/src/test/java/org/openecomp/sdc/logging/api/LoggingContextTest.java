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

package org.openecomp.sdc.logging.api;

import org.testng.annotations.Test;

import java.lang.reflect.Field;
import java.util.concurrent.Callable;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

/**
 * @author EVITALIY
 * @since 08 Jan 18
 */
public class LoggingContextTest {

    @Test
    public void shouldHoldNoOpWhenNoBinding() throws Exception {
        Field factory = LoggingContext.class.getDeclaredField("SERVICE");
        factory.setAccessible(true);
        Object impl = factory.get(null);
        assertEquals(impl.getClass().getName(),
                "org.openecomp.sdc.logging.api.LoggingContext$NoOpLoggingContextService");
    }

    @Test
    public void putDoesNotHaveEffectWhenNoBinding() {
        final String key = "Key";
        LoggingContext.put(key, "Dummy");
        assertNull(LoggingContext.get(key));
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void throwNpeWhenPutWithKeyNull() {
        LoggingContext.put(null, "value");
    }

    @Test
    public void getAlwaysReturnsNull() {
        assertNull(LoggingContext.get("GetKey"));
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void throwNpeWhenGetWithKeyNull() {
        LoggingContext.get(null);
    }

    @Test
    public void removeDoesNotFail() {
        LoggingContext.remove("RemoveKey");
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void throwNpWhenRemoveWithKeyNull() {
        LoggingContext.remove(null);
    }

    @Test
    public void clearDoesNotFail() {
        LoggingContext.clear();
    }

    @Test
    public void toRunnableReturnsSameInstance() {
        Runnable test = () -> { /* do nothing */ };
        assertTrue(test == LoggingContext.copyToRunnable(test));
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void throwNpeWhenToRunnableWithNull() {
        LoggingContext.copyToRunnable(null);
    }

    @Test
    public void toCallableReturnsSameInstance() {
        Callable<String> test = () -> "";
        assertTrue(test == LoggingContext.copyToCallable(test));
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void throwNpeWhenToCallableWithNull() {
        LoggingContext.copyToCallable(null);
    }
}