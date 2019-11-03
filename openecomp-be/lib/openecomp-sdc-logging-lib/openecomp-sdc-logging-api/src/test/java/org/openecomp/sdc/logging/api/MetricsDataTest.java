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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.onap.logging.ref.slf4j.ONAPLogConstants.ResponseStatus.COMPLETE;

import org.junit.Test;

/**
 * Unit-testing metrics builder and structure.
 *
 * @author evitaliy
 * @since 04 Mar 18
 */
public class MetricsDataTest {

    @Test
    public void allMetricsPropertiesReadWhenPopulated() {

        final long start = System.currentTimeMillis();
        final long end = start + 1000;
        final String responseCode = "Metrics-Response-Code";
        final String responseDescription = "Metrics-Response-Description";
        final String ipAddress = "IP72";
        final String targetEntity = "Metrics-Target-Entity";
        final String targetVirtualEntity = "Metrics-Target-Virtual-Entity";

        MetricsData data = MetricsData.builder().startTime(start).endTime(end).statusCode(COMPLETE)
                                      .responseCode(responseCode).responseDescription(responseDescription)
                                      .clientIpAddress(ipAddress).targetEntity(targetEntity)
                                      .targetVirtualEntity(targetVirtualEntity).build();

        assertEquals(ipAddress, data.getClientIpAddress());
        assertEquals(end, data.getEndTime());
        assertEquals(start, data.getStartTime());
        assertEquals(responseCode, data.getResponseCode());
        assertEquals(responseDescription, data.getResponseDescription());
        assertEquals(COMPLETE, data.getStatusCode());
        assertEquals(targetEntity, data.getTargetEntity());
        assertEquals(targetVirtualEntity, data.getTargetVirtualEntity());

    }

    @Test
    public void allMetricsPropertiesEmptyWhenUnpopulated() {
        MetricsData data = MetricsData.builder().build();
        assertEquals(0, data.getStartTime());
        assertEquals(0, data.getEndTime());
        assertNull(data.getClientIpAddress());
        assertNull(data.getResponseCode());
        assertNull(data.getResponseDescription());
        assertNull(data.getStatusCode());
        assertNull(data.getTargetEntity());
        assertNull(data.getTargetVirtualEntity());
    }
}