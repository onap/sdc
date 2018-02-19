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
import org.openecomp.sdc.logging.slf4j.SLF4JLoggingServiceProvider.ContextField;
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
        assertEquals(LoggingContext.copyToRunnable(() -> {}).getClass(), MDCRunnableWrapper.class);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void throwNpeWhenToRunnableWithNull() {
        LoggingContext.copyToRunnable(null);
    }

    @Test
    public void returnMdcWrapperWhenToCallableCalled() {
        assertEquals(LoggingContext.copyToCallable(() -> "").getClass(), MDCCallableWrapper.class);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void throwNpeWhenToCallableWithNull() {
        LoggingContext.copyToCallable(null);
    }

    @Test
    public void keysClearedWhenContextCleared() {

        String value = UUID.randomUUID().toString();

        try {
            LoggingContext.putPartnerName(value);
            LoggingContext.putServiceName(value);
            LoggingContext.putRequestId(value);
            LoggingContext.clear();

            for (ContextField field : ContextField.values()) {
                assertNull(MDC.get(field.asKey()));
            }

        } finally {
            MDC.clear();
        }
    }

    @Test
    public void unrelatedKeysRemainWhenContextCleared() {

        String randomValue = UUID.randomUUID().toString();
        String randomKey = "Key-" + randomValue;

        try {

            MDC.put(randomKey, randomValue);
            LoggingContext.clear();
            assertEquals(MDC.get(randomKey), randomValue);

        } finally {
            MDC.clear();
        }
    }

    @Test
    public void contextHasServiceNameWhenPut() {

        String random = UUID.randomUUID().toString();

        try {
            LoggingContext.putServiceName(random);
            assertEquals(random, MDC.get(ContextField.SERVICE_NAME.asKey()));
        } finally {
            MDC.clear();
        }
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void throwNpeWhenServiceNameNull() {
        LoggingContext.putServiceName(null);
    }

    @Test
    public void contextHasRequestIdWhenPut() {

        String random = UUID.randomUUID().toString();

        try {
            LoggingContext.putRequestId(random);
            assertEquals(random, MDC.get(ContextField.REQUEST_ID.asKey()));
        } finally {
            MDC.clear();
        }
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void throwNpeWhenRequestIdNull() {
        LoggingContext.putRequestId(null);
    }

    @Test
    public void contextHasPartnerNameWhenPut() {

        String random = UUID.randomUUID().toString();

        try {
            LoggingContext.putPartnerName(random);
            assertEquals(random, MDC.get(ContextField.PARTNER_NAME.asKey()));
        } finally {
            MDC.clear();
        }
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void throwNpeWhenPartnerNameNull() {
        LoggingContext.putPartnerName(null);
    }
}
