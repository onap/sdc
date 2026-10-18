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

import java.time.Duration;
import java.util.function.LongSupplier;
import org.springframework.stereotype.Component;

/**
 * Records when this instance last wrote a data type vertex, so that {@link ApplicationDataTypeCache} knows when a staleness check on the
 * request path is worth its cost. A separate bean because the operations that write data types are themselves dependencies of the cache.
 */
@Component
public class DataTypeWriteTracker {

    private final LongSupplier nanoClock;
    private volatile long lastWriteNanos;
    private volatile boolean written;

    public DataTypeWriteTracker() {
        this(System::nanoTime);
    }

    DataTypeWriteTracker(final LongSupplier nanoClock) {
        this.nanoClock = nanoClock;
    }

    public void markWritten() {
        lastWriteNanos = nanoClock.getAsLong();
        written = true;
    }

    public boolean wasWrittenWithin(final Duration window) {
        return written && nanoClock.getAsLong() - lastWriteNanos <= window.toNanos();
    }
}
