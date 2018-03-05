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

package org.openecomp.sdc.logging.api;

import static org.testng.Assert.*;

import org.testng.annotations.Test;

/**
 * @author EVITALIY
 * @since 04 Mar 18
 */
public class AuditDataTest {

    @Test
    public void allPropertiesReadWhenPopulated() {

        final long start = System.currentTimeMillis();
        final long end = start + 100;
        final String responseCode = "Response-Code";
        final String responseDescription = "Response-Description";
        final String ipAddress = "10.56.20.70";

        AuditData data = AuditData.builder().startTime(start).endTime(end).statusCode(StatusCode.COMPLETE)
            .responseCode(responseCode).responseDescription(responseDescription).clientIpAddress(ipAddress).build();

        assertEquals(data.getClientIpAddress(), ipAddress);
        assertEquals(data.getEndTime(), end);
        assertEquals(data.getStartTime(), start);
        assertEquals(data.getResponseCode(), responseCode);
        assertEquals(data.getResponseDescription(), responseDescription);
        assertEquals(data.getStatusCode(), StatusCode.COMPLETE);
    }

    @Test
    public void allPropertiesEmptyWhenUnpopulated() {
        AuditData data = AuditData.builder().build();
        assertEquals(data.getStartTime(), 0);
        assertEquals(data.getEndTime(), 0);
        assertNull(data.getClientIpAddress());
        assertNull(data.getResponseCode());
        assertNull(data.getResponseDescription());
        assertNull(data.getStatusCode());
    }
}