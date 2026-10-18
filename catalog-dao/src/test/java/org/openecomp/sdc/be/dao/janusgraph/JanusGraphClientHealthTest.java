/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2026 Deutsche Telekom AG. All rights reserved.
 * ================================================================================
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
 * ============LICENSE_END=========================================================
 */

package org.openecomp.sdc.be.dao.janusgraph;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class JanusGraphClientHealthTest {

    private final AtomicLong now = new AtomicLong(TimeUnit.HOURS.toNanos(1));
    private JanusGraphClient client;

    @BeforeEach
    void setUp() {
        client = new JanusGraphClient();
        client.nanoClock = now::get;
    }

    @Test
    void healthIsUpWhileTheLastCheckIsFresh() {
        markHealthyAt(now.get());
        now.addAndGet(TimeUnit.SECONDS.toNanos(5));

        assertTrue(client.getHealth());
    }

    @Test
    void healthIsDownOnceTheLastCheckIsStale() {
        markHealthyAt(now.get());
        now.addAndGet(TimeUnit.MINUTES.toNanos(1));

        assertFalse(client.getHealth());
    }

    @Test
    void healthCheckTaskSurvivesAThrowable() {
        client.nanoClock = () -> {
            throw new NoClassDefFoundError("simulated");
        };
        Runnable task = (Runnable) ReflectionTestUtils.getField(client, "healthCheckScheduledTask");

        assertDoesNotThrow(task::run);
    }

    @Test
    void healthCheckTaskRefreshesTheTimestampOnEveryRun() {
        markHealthyAt(0);
        Runnable task = (Runnable) ReflectionTestUtils.getField(client, "healthCheckScheduledTask");

        task.run();

        assertFalse(client.getHealth(), "no graph, so the run itself reports down");
        assertEquals(now.get(), ReflectionTestUtils.getField(client, "lastHealthCheckNanos"));
    }

    private void markHealthyAt(long nanos) {
        client.lastHealthState = true;
        ReflectionTestUtils.setField(client, "lastHealthCheckNanos", nanos);
    }
}
