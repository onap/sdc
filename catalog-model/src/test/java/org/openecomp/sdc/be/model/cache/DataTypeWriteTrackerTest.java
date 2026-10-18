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
package org.openecomp.sdc.be.model.cache;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;

class DataTypeWriteTrackerTest {

    private final AtomicLong now = new AtomicLong(Long.MAX_VALUE - Duration.ofSeconds(30).toNanos());
    private final DataTypeWriteTracker tracker = new DataTypeWriteTracker(now::get);

    @Test
    void notWrittenBeforeAnyWrite() {
        assertFalse(tracker.wasWrittenWithin(Duration.ofDays(365)));
    }

    @Test
    void writtenWithinWindow() {
        tracker.markWritten();
        now.addAndGet(Duration.ofSeconds(60).toNanos());
        assertTrue(tracker.wasWrittenWithin(Duration.ofSeconds(60)));
    }

    @Test
    void notWrittenOnceWindowHasPassed() {
        tracker.markWritten();
        now.addAndGet(Duration.ofSeconds(61).toNanos());
        assertFalse(tracker.wasWrittenWithin(Duration.ofSeconds(60)));
    }

    @Test
    void laterWriteRestartsWindow() {
        tracker.markWritten();
        now.addAndGet(Duration.ofSeconds(50).toNanos());
        tracker.markWritten();
        now.addAndGet(Duration.ofSeconds(50).toNanos());
        assertTrue(tracker.wasWrittenWithin(Duration.ofSeconds(60)));
    }
}
