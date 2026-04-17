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
package org.openecomp.sdc.notification.dao.types;


import java.util.UUID;

import com.datastax.oss.driver.api.mapper.annotations.ClusteringColumn;
import com.datastax.oss.driver.api.mapper.annotations.CqlName;
import com.datastax.oss.driver.api.mapper.annotations.Entity;
import com.datastax.oss.driver.api.mapper.annotations.PartitionKey;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@CqlName("notifications")
public class NotificationEntity {

    public static final String ENTITY_TYPE = "Event Notification";
    @PartitionKey
    @CqlName("owner_id")
    private String ownerId;
    @CqlName("read")
    private boolean read;
    @ClusteringColumn
    @CqlName("event_id")
    private UUID eventId;
    @CqlName("event_type")
    private String eventType;
    @CqlName("event_attributes")
    private String eventAttributes;
    @CqlName("originator_id")
    private String originatorId;

    public NotificationEntity(String ownerId) {
        this.ownerId = ownerId;
    }

    public NotificationEntity(String ownerId, UUID eventId, String eventType, String originatorId) {
        this(ownerId, false, eventId, eventType, null, originatorId);
    }

    public NotificationEntity(String ownerId, UUID eventId) {
        this(ownerId, eventId, null, null);
    }
}
