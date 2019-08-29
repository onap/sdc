/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 Nokia. All rights reserved.
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
package org.openecomp.sdcrests.item.rest.models;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.Map;
import org.junit.Test;

public class SyncEventTest {

    private static final String EVENT = "EVENT";
    private static final String ORIGINATOR = "ORIGINATOR";
    private static final String ENTITY_ID = "ENTITY";
    private static final Map<String, Object> ATTRIBUTES = Collections.emptyMap();

    @Test
    public void shouldHaveValidGetters() {
        SyncEvent syncEvent = new SyncEvent(EVENT, ORIGINATOR, ATTRIBUTES, ENTITY_ID);
        assertEquals(syncEvent.getAttributes(), ATTRIBUTES);
        assertEquals(syncEvent.getEntityId(), ENTITY_ID);
        assertEquals(syncEvent.getEventType(), EVENT);
        assertEquals(syncEvent.getOriginatorId(), ORIGINATOR);
    }
}