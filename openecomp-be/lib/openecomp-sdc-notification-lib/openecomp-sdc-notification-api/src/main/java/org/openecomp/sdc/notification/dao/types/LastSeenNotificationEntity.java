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

import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;

import java.util.Objects;
import java.util.UUID;

@Table(keyspace = "dox", name = "last_notification")
public class LastSeenNotificationEntity {
  public static final String ENTITY_TYPE = "Event Notification";

  @PartitionKey
  @Column(name = "owner_id")
  private String ownerId;

  @Column(name = "event_id")
  private UUID lastEventId;

  /**
   * Every entity class must have a default constructor according to
   * <a href="http://docs.datastax.com/en/developer/java-driver/2.1/manual/object_mapper/creating/">
   * Definition of mapped classes</a>.
   */
  public LastSeenNotificationEntity() {
    // Don't delete! Default constructor is required by DataStax driver
  }

  /**
   * Instantiates a new Notification entity.
   *
   * @param ownerId      the owner id
   * @param lastEventId  the last event id
   */
  public LastSeenNotificationEntity(String ownerId, UUID lastEventId) {
    this.ownerId = ownerId;
    this.lastEventId = lastEventId;
  }

  public String getOwnerId() {
    return ownerId;
  }

  public void setOwnerId(String ownerId) {
    this.ownerId = ownerId;
  }

  public UUID getLastEventId() {
    return lastEventId;
  }

  public void setLastEventId(UUID lastEventId) {
    this.lastEventId = lastEventId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    LastSeenNotificationEntity that = (LastSeenNotificationEntity) o;
    return Objects.equals(ownerId, that.ownerId) &&
            Objects.equals(lastEventId, that.lastEventId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(ownerId, lastEventId);
  }

  @Override
  public String toString() {
    return "LastSeenNotificationEntity {"
        + "ownerId='" + ownerId + '\''
       + '}';
  }
}
