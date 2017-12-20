/*
 * Copyright © 2016-2017 European Support Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openecomp.core.zusammen.plugin.dao.types;

import com.amdocs.zusammen.datatypes.Id;

import java.util.Date;

/**
 * Synchronization state of an entity:
 * <ul>
 * <li>On private entity edit (create/update/delete): marked as dirty</li>
 * <li>On entity publication:
 * <ul>
 * <li>if the private entity exists - updated with the publish time, marked as not dirty</li>
 * <li>Otherwise - deleted</li>
 * </ul>
 * </li>
 * </ul>
 */
public class SynchronizationStateEntity {
  private Id id;
  private Id revisionId;
  private Date publishTime;
  private boolean dirty;
  private String user;
  private String message;

  public SynchronizationStateEntity(Id id,Id revisionId) {
    this.id = id;
    this.revisionId = revisionId;
  }

  public SynchronizationStateEntity(Id id,Id revisionId, Date publishTime, boolean dirty) {
    this(id,revisionId);
    this.publishTime = publishTime;
    this.dirty = dirty;
  }

  public Id getId() {
    return id;
  }

  public Date getPublishTime() {
    return publishTime;
  }

  public void setPublishTime(Date publishTime) {
    this.publishTime = publishTime;
  }

  public boolean isDirty() {
    return dirty;
  }

  public void setDirty(boolean dirty) {
    this.dirty = dirty;
  }

  public Id getRevisionId() {
    return revisionId;
  }

  public void setRevisionId(Id revisionId) {
    this.revisionId = revisionId;
  }

  public String getUser() {
    return user;
  }

  public void setUser(String user) {
    this.user = user;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    SynchronizationStateEntity that = (SynchronizationStateEntity) o;

    return id.equals(that.id);
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }
}
