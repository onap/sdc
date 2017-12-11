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

/**
 * This is only for manual testing - change ENABLE to 'true'
 *
 * @author evitaliy
 * @since 13/09/2016.
 */
public class LoggerTest {

  private static final boolean ENABLED = false;
  private static final Logger LOGGER = LoggerFactory.getLogger(LoggerTest.class);

  @Test(enabled = ENABLED)
  public void testMetrics() throws Exception {
    LOGGER.metrics("This is metrics");
  }

  @Test(enabled = ENABLED)
  public void testAudit() throws Exception {
    LOGGER.audit("This is audit");
  }

  @Test(enabled = ENABLED)
  public void testDebug() throws Exception {
    LOGGER.debug("This is debug");
  }

  @Test(enabled = ENABLED)
  public void testInfo() throws Exception {
    LOGGER.info("This is info");
  }

  @Test(enabled = ENABLED)
  public void testWarn() throws Exception {
    LOGGER.warn("This is warning");
  }

  @Test(enabled = ENABLED)
  public void testError() throws Exception {
    LOGGER.error("This is error");
  }
}
