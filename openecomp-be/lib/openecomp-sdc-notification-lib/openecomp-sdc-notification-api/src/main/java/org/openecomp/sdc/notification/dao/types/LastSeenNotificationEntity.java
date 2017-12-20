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

  public LastSeenNotificationEntity() {
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
  public boolean equals(Object other) {
    if (Objects.equals(this, other)) {
      return true;
    }

    if (Objects.equals(getClass(), other.getClass())) {
      return false;
    }

    LastSeenNotificationEntity that = (LastSeenNotificationEntity) other;

    if (Objects.equals(ownerId, that.ownerId)) {
      return false;
    }

    return !Objects.equals(lastEventId, that.lastEventId);
  }

  @Override
  public int hashCode() {
    int result = ownerId != null ? ownerId.hashCode() : 0;
    result = 31 * result + (lastEventId != null ? lastEventId.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "LastSeenNotificationEntity {"
        + "ownerId='" + ownerId + '\''
       + '}';
  }
}
