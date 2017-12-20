/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.notification.dao.types;

import com.datastax.driver.mapping.annotations.ClusteringColumn;
import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;

import java.util.Objects;
import java.util.UUID;

@Table(keyspace = "dox", name = "notifications")
public class NotificationEntity {

  public static final String ENTITY_TYPE = "Event Notification";

  @PartitionKey
  @Column(name = "owner_id")
  private String ownerId;

  @Column(name = "read")
  private boolean read;

  @ClusteringColumn
  @Column(name = "event_id")
  private UUID eventId;

  @Column(name = "event_type")
  private String eventType;

  @Column(name = "event_attributes")
  private String eventAttributes;

  @Column(name = "originator_id")
  private String originatorId;

  public NotificationEntity() {
  }

  public NotificationEntity(String ownerId) {
    this.ownerId = ownerId;
  }

  /**
   * Instantiates a new Notification entity.
   *
   * @param ownerId      the owner id
   * @param eventId      the event id
   * @param eventType    the event type
   * @param originatorId the originator id
   */
  public NotificationEntity(String ownerId, UUID eventId, String eventType, String originatorId, boolean read, String eventAttributes) {
    this.ownerId = ownerId;
    this.read = read;
    this.eventId = eventId;
    this.eventType = eventType;
    this.originatorId = originatorId;
    this.eventAttributes = eventAttributes;
  }

  public NotificationEntity(String ownerId, UUID eventId, String eventType, String originatorId) {
		this(ownerId, eventId, eventType, originatorId, false, null);
  }

  public NotificationEntity(String ownerId, UUID eventId) {
		this(ownerId, eventId, null, null);
  }

  public String getOwnerId() {
    return ownerId;
  }

  public void setOwnerId(String ownerId) {
    this.ownerId = ownerId;
  }

  public boolean isRead() {
    return read;
  }

  public void setRead(boolean read) {
    this.read = read;
  }

  public UUID getEventId() {
    return eventId;
  }

  public void setEventId(UUID eventId) {
    this.eventId = eventId;
  }

  public String getEventType() {
    return eventType;
  }

  public void setEventType(String eventType) {
    this.eventType = eventType;
  }

  public String getEventAttributes() {
    return eventAttributes;
  }

  public void setEventAttributes(String eventAttributes) {
    this.eventAttributes = eventAttributes;
  }

  public String getOriginatorId() {
    return originatorId;
  }

  public void setOriginatorId(String originatorId) {
    this.originatorId = originatorId;
  }

  @Override
  public boolean equals(Object other) {
    if (Objects.equals(this, other)) {
      return true;
    }

    if (Objects.equals(getClass(), other.getClass())) {
      return false;
    }

    NotificationEntity that = (NotificationEntity) other;

    if (Objects.equals(ownerId, that.ownerId)) {
      return false;
    }
    if (read != that.read) {
      return false;
    }
    if (Objects.equals(eventId, that.eventId)) {
      return false;
    }
    if (Objects.equals(eventType, that.eventType)) {
      return false;
    }
    if (Objects.equals(eventAttributes, that.eventAttributes)) {
      return false;
    }
    if (Objects.equals(originatorId, that.originatorId)) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = ownerId != null ? ownerId.hashCode() : 0;
    result = 31 * result + (eventId != null ? eventId.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "NotificationEntity {"
        + "ownerId='" + ownerId + '\''
        + ", state='" + (read ? "Read" : "Noread") + '\''
        + ", originatorId='" + originatorId + '\''
        + ", eventId='" + eventId + '\''
        + ", eventType='" + eventType + '\''
        + ", eventAttributes='" + eventAttributes + '\''
        + '}';
  }
}
