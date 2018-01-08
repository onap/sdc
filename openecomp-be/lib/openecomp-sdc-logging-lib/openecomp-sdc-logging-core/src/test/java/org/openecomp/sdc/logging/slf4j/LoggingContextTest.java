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

import org.openecomp.sdc.logging.api.LoggingContext;
import org.slf4j.MDC;
import org.testng.annotations.Test;

import java.util.UUID;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

/**
 * @author evitaliy
 * @since 12/09/2016.
 */
public class LoggingContextTest {

    @Test
    public void returnMdcWrapperWhenToRunnableCalled() {
        assertEquals(LoggingContext.toRunnable(() -> {}).getClass(), MDCRunnableWrapper.class);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void throwNpeWhenToRunnableWithNull() {
        LoggingContext.toRunnable(null);
    }

    @Test
    public void returnMdcWrapperWhenToCallableCalled() {
        assertEquals(LoggingContext.toCallable(() -> "").getClass(), MDCCallableWrapper.class);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void throwNpeWhenToCallableWithNull() {
        LoggingContext.toCallable(null);
    }

    @Test
    public void clearContextWhenClearCalled() {

        String random = UUID.randomUUID().toString();

        try {
            LoggingContext.put(random, random);
            LoggingContext.clear();
            assertNull(MDC.get(random));
            assertNull(LoggingContext.get(random));
        } finally {
            MDC.remove(random);
        }
    }

    @Test
    public void returnContextWhenGetCalled() {

        String random = UUID.randomUUID().toString();

        try {
            LoggingContext.put(random, random);
            assertEquals(random, MDC.get(random));
            assertEquals(random, LoggingContext.get(random));
        } finally {
            MDC.remove(random);
        }
    }

    @Test
    public void removeContextWhenRemoveCalled() {

        String random = UUID.randomUUID().toString();

        try {
            LoggingContext.put(random, random);
            LoggingContext.remove(random);
            assertNull(MDC.get(random));
            assertNull(LoggingContext.get(random));
        } finally {
            MDC.remove(random);
        }
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void throwNpeWhenPutWithKeyNull() {
        LoggingContext.put(null, "---");
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void throwNpeWhenGetWithKeyNull() {
        LoggingContext.get(null);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void throwNpeWhenRemoveWithKeyNull() {
        LoggingContext.remove(null);
    }
}
