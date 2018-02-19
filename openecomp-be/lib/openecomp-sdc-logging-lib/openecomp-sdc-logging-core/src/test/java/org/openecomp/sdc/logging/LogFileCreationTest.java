/*
 * Copyright © 2016-2018 European Support Limited
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

package org.openecomp.sdc.logging;

import org.openecomp.sdc.logging.api.AuditData;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.testng.annotations.Test;
import static org.testng.Assert.assertTrue;

public class LogFileCreationTest {
    private static final boolean ENABLED = true; // for manual testing change to 'true'
    private static final Logger LOGGER = LoggerFactory.getLogger(LogFileCreationTest.class);

    @Test(enabled = ENABLED)
    public void testMetrics() {
        LOGGER.metrics("This is metrics");
    }

    @Test(enabled = ENABLED)
    public void testAudit() {
        SpyAuditData auditData = new SpyAuditData();
        LOGGER.audit(auditData);
    }

    @Test(enabled = ENABLED)
    public void testDebug() {
        LOGGER.debug("This is debug");
    }

    @Test(enabled = ENABLED)
    public void testInfo() {
        LOGGER.info("This is info");
    }

    @Test(enabled = ENABLED)
    public void testWarn() {
        LOGGER.warn("This is warning");
    }

    @Test(enabled = ENABLED)
    public void testError() {
        LOGGER.error("This is error");
    }
    private class SpyAuditData implements AuditData {
        @Override
        public long getStartTime() {return 0;}

        @Override
        public long getEndTime(){return 0;}

        @Override
        public StatusCode getStatusCode(){return null;}

        @Override
        public String getResponseCode(){return null;}

        @Override
        public String getResponseDescription(){return null;}

        @Override
        public String getClientIpAddress(){return null;}
    }
}
