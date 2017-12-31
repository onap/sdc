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

  /**
   * Every entity class must have a default constructor according to
   * <a href="http://docs.datastax.com/en/developer/java-driver/2.1/manual/object_mapper/creating/">
   * Definition of mapped classes</a>.
   */
  public NotificationEntity() {
    // Don't delete! Default constructor is required by DataStax driver
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
  public NotificationEntity(String ownerId, UUID eventId, String eventType, String originatorId, boolean read,
                            String eventAttributes) {
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
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    NotificationEntity that = (NotificationEntity) o;
    return read == that.read &&
            Objects.equals(ownerId, that.ownerId) &&
            Objects.equals(eventId, that.eventId) &&
            Objects.equals(eventType, that.eventType) &&
            Objects.equals(eventAttributes, that.eventAttributes) &&
            Objects.equals(originatorId, that.originatorId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(ownerId, read, eventId, eventType, eventAttributes, originatorId);
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
