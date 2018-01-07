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
import org.openecomp.sdc.logging.provider.LoggingContextService;
import org.testng.annotations.DataProvider;

import java.util.concurrent.Callable;

/**
 * @author EVITALIY
 * @since 08 Jan 18
 */
public abstract class BaseContextPropagationTest {

    // Disable if an old version of ctx implementation is being used.
    // ctxPropagationFactory should be used when ctx is not propagated to child threads.
    // See https://jira.qos.ch/browse/LOGBACK-422 and https://jira.qos.ch/browse/LOGBACK-624
    static final boolean ENABLED = false;
    static final String PROVIDER = "context";
    static final String KEY = "test-data";

    static final String EXPECT_PROPAGATED_TO_CHILD = "Expected the data to be propagated to the child thread's context";
    static final String EXPECT_RETAINED_IN_CURRENT = "Expected the data to be retained in this thread";
    static final String EXPECT_REPLACED_WITH_STORED = "Expected context data to be replaced with stored data";
    static final String EXPECT_INNER_RUN = "Expected the inner thread to run";
    static final String EXPECT_OUTER_RUN = "Expected the outer thread to run";
    static final String EXPECT_NOT_COPIED = "Expected context data not to be copied to this thread";
    static final String EXPECT_RETAINED_IN_PARENT = "Expected context data to be retained in parent thread";
    static final String EXPECT_POPULATED = "Expected context data to be populated in this thread";
    static final String EXPECT_EMPTY = "Expected context data to be empty";
    static final String EXPECT_REMAIN_EMPTY = "Expected context data to remain empty in this thread";
    static final String EXPECT_REVERTED_ON_EXCEPTION = "Expected context data to be reverted even in case of exception";
    static final String EXPECT_EXCEPTION_FROM_INNER = "Expected the inner class to throw exception";

    @DataProvider(name = PROVIDER)
    public static Object[][] contextServices() {
        // try both directly call the implementation and get it via the binding
        return new Object[][] {
                { new SLF4JLoggingServiceProvider() },
                { new LoggingContextAdaptor() }
        };
    }

    private static class LoggingContextAdaptor implements LoggingContextService {

        @Override
        public void put(String key, String value) {
            LoggingContext.put(key, value);
        }

        @Override
        public String get(String key) {
            return LoggingContext.get(key);
        }

        @Override
        public void remove(String key) {
            LoggingContext.remove(key);
        }

        @Override
        public void clear() {
            LoggingContext.clear();
        }

        @Override
        public Runnable toRunnable(Runnable runnable) {
            return LoggingContext.toRunnable(runnable);
        }

        @Override
        public <V> Callable<V> toCallable(Callable<V> callable) {
            return LoggingContext.toCallable(callable);
        }

        @Override
        public String toString() {
            return this.getClass().getName();
        }
    }
}
