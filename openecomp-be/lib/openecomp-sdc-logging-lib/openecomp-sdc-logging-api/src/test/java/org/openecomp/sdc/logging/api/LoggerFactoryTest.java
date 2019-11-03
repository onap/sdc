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
import static org.junit.Assert.assertNotNull;

import java.lang.reflect.Field;
import org.junit.Test;

/**
 * Unit-test creation of a logger via factory, assuming not default binding.
 *
 * @author evitaliy
 * @since 14/09/2016.
 */
public class LoggerFactoryTest {

    @Test
    public void shouldHoldNoOpWhenNoBinding() throws NoSuchFieldException, IllegalAccessException {

        // set up to access the private static field
        Field factory = LoggerFactory.class.getDeclaredField("SERVICE");
        factory.setAccessible(true);
        Object impl = factory.get(null);

        assertEquals("org.openecomp.sdc.logging.api.LoggerFactory$NoOpLoggerCreationService",
                impl.getClass().getName());
    }

    @Test
    public void verifyNoOpLoggerWorksWhenGotByClass() {
        Logger logger = LoggerFactory.getLogger(LoggerFactoryTest.class);
        verifyLoggerWorks(logger);
    }

    @Test
    public void verifyNoOpLoggerWorksWhenGotByName() {
        Logger logger = LoggerFactory.getLogger(LoggerFactoryTest.class.getName());
        verifyLoggerWorks(logger);
    }

    @Test(expected = NullPointerException.class)
    public void throwNpeWhenGetByNameWithNull() {
        LoggerFactory.getLogger((String) null);
    }

    @Test(expected = NullPointerException.class)
    public void throwNpeWhenGetByClassWithNull() {
        LoggerFactory.getLogger((Class<LoggerFactoryTest>) null);
    }

    private void verifyLoggerWorks(Logger logger) {
        assertNotNull(logger);
        // make sure no exceptions are thrown
        logger.error("");
        logger.warn("");
        logger.info("");
        logger.debug("");
        logger.auditExit(null);
        logger.metrics(MetricsData.builder().build());
    }
}
