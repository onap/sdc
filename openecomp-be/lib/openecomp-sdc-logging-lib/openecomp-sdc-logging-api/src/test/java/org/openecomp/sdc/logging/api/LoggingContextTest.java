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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.lang.reflect.Field;
import java.util.concurrent.Callable;
import org.junit.Test;

/**
 * Unit-testing default context service implementation.
 *
 * @author evitaliy
 * @since 08 Jan 18
 */
public class LoggingContextTest {

    @Test
    public void shouldHoldNoOpWhenNoBinding() throws NoSuchFieldException, IllegalAccessException {
        Field factory = LoggingContext.class.getDeclaredField("SERVICE");
        factory.setAccessible(true);
        Object impl = factory.get(null);
        assertEquals("org.openecomp.sdc.logging.api.LoggingContext$NoOpLoggingContextService",
                impl.getClass().getName());
    }

    @Test(expected = NullPointerException.class)
    public void throwNpeWhenContextIsNull() {
        LoggingContext.put(null);
    }

    @Test
    public void clearDoesNotFail() {
        LoggingContext.clear();
    }

    @Test
    public void toRunnableReturnsSameInstance() {
        Runnable test = () -> { /* do nothing */ };
        assertSame(test, LoggingContext.copyToRunnable(test));
    }

    @Test(expected = NullPointerException.class)
    public void throwNpeWhenToRunnableWithNull() {
        LoggingContext.copyToRunnable(null);
    }

    @Test
    public void toCallableReturnsSameInstance() {
        Callable<String> test = () -> "";
        assertSame(test, LoggingContext.copyToCallable(test));
    }

    @Test(expected = NullPointerException.class)
    public void throwNpeWhenToCallableWithNull() {
        LoggingContext.copyToCallable(null);
    }
}