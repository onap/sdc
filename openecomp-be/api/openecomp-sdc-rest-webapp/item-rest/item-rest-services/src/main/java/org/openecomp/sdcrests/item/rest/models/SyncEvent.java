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

import java.util.Map;
import org.openecomp.sdc.notification.dtos.Event;

public class SyncEvent implements Event {

    private String eventType;
    private String originatorId;
    private Map<String, Object> attributes;
    private String entityId;

    public SyncEvent(String eventType, String originatorId, Map<String, Object> attributes, String entityId) {
        this.eventType = eventType;
        this.originatorId = originatorId;
        this.attributes = attributes;
        this.entityId = entityId;
    }

    @Override
    public String getEventType() {
        return eventType;
    }

    @Override
    public String getOriginatorId() {
        return originatorId;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public String getEntityId() {
        return entityId;
    }
}
