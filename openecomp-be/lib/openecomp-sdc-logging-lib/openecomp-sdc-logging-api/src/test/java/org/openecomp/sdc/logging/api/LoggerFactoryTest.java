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
import java.util.ServiceLoader;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;

/**
 * @author evitaliy
 * @since 14/09/2016.
 */
public class LoggerFactoryTest {

  @Test
  public void testNoOpLoggerService() throws Exception {

    assertFalse(ServiceLoader.load(LoggerCreationService.class).iterator().hasNext());

    LoggerFactory.getLogger(LoggerFactoryTest.class);
    Field factory = LoggerFactory.class.getDeclaredField("SERVICE");
    factory.setAccessible(true);
    Object impl = factory.get(null);
    assertEquals("org.openecomp.sdc.logging.api.LoggerFactory$NoOpLoggerCreationService",
        impl.getClass().getName());
  }

  @Test
  public void testNoOpLoggerByClass() throws Exception {
    Logger logger = LoggerFactory.getLogger(LoggerFactoryTest.class);
    verifyLogger(logger);
  }

  @Test
  public void testNoOpLoggerByName() throws Exception {
    Logger logger = LoggerFactory.getLogger(LoggerFactoryTest.class.getName());
    verifyLogger(logger);
  }

  private void verifyLogger(Logger logger) {
    assertNotNull(logger);

    // make sure no exceptions are thrown
    logger.error("");
    logger.warn("");
    logger.info("");
    logger.debug("");
    logger.audit("");
    logger.metrics("");
  }
}
