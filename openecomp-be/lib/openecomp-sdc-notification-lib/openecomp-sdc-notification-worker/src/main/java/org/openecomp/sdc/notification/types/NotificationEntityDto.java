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

package org.openecomp.sdc.notification.types;

import java.util.Map;
import java.util.UUID;

public class NotificationEntityDto {
  private boolean read;
  private UUID eventId;
  private String dateTime;
  private String eventType;
  private Map<String, Object> eventAttributes;

  public NotificationEntityDto() {
  }
  public NotificationEntityDto(boolean read, UUID eventId, String eventType,
                               Map<String, Object> eventAttributes) {
    this.read = read;
    this.eventId = eventId;
    this.eventType = eventType;
    this.eventAttributes = eventAttributes;
  }

  public NotificationEntityDto(boolean read, UUID eventId,String eventType,
                               Map<String, Object> eventAttributes, String dateTime) {
    this.read = read;
    this.eventId = eventId;
    this.dateTime = dateTime;
    this.eventType = eventType;
    this.eventAttributes = eventAttributes;
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

  public Map<String, Object> getEventAttributes() {
    return eventAttributes;
  }

  public void setEventAttributes(Map<String, Object> eventAttributes) {
    this.eventAttributes = eventAttributes;
  }

  public String getDateTime() {
    return dateTime;
  }

  public void setDateTime(String dateTime) {
    this.dateTime = dateTime;
  }

  @Override
  public String toString() {
    return "NotificationEntityDto {"
        + ", state='" + (read ? "Read" : "Noread") + '\''
        + ", dateTime='" + dateTime + '\''
        + ", eventId='" + eventId + '\''
        + ", eventType='" + eventType + '\''
        + ", eventAttributes='" + eventAttributes + '\''
        + '}';
  }

}
