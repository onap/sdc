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

package org.openecomp.sdc.versioning.dao.types;

import com.datastax.driver.mapping.annotations.ClusteringColumn;
import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.Enumerated;
import com.datastax.driver.mapping.annotations.Frozen;
import com.datastax.driver.mapping.annotations.FrozenValue;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;

import java.util.HashSet;
import java.util.Set;

@Table(keyspace = "dox", name = "version_info")
public class VersionInfoEntity {

  @PartitionKey
  @Column(name = "entity_type")
  private String entityType;

  @ClusteringColumn
  @Column(name = "entity_id")
  private String entityId;

  @Column(name = "active_version")
  @Frozen
  private Version activeVersion;

  @Enumerated
  private VersionStatus status;

  @Frozen
  private UserCandidateVersion candidate;

  @Column(name = "viewable_versions")
  @FrozenValue
  private Set<Version> viewableVersions = new HashSet<>();

  @Column(name = "latest_final_version")
  @Frozen
  private Version latestFinalVersion;

  public VersionInfoEntity() {
  }

  public VersionInfoEntity(String entityType, String entityId) {
    this.entityType = entityType;
    this.entityId = entityId;
  }

  public String getEntityType() {
    return entityType;
  }

  public void setEntityType(String entityType) {
    this.entityType = entityType;
  }

  public String getEntityId() {
    return entityId;
  }

  public void setEntityId(String entityId) {
    this.entityId = entityId;
  }

  public Version getActiveVersion() {
    return activeVersion;
  }

  public void setActiveVersion(Version activeVersion) {
    this.activeVersion = activeVersion;
  }

  public VersionStatus getStatus() {
    return status;
  }

  public void setStatus(VersionStatus status) {
    this.status = status;
  }

  public UserCandidateVersion getCandidate() {
    return candidate;
  }

  public void setCandidate(UserCandidateVersion candidate) {
    this.candidate = candidate;
  }

  public Set<Version> getViewableVersions() {
    return viewableVersions;
  }

  public void setViewableVersions(Set<Version> viewableVersions) {
    this.viewableVersions = viewableVersions;
  }

  public Version getLatestFinalVersion() {
    return latestFinalVersion;
  }

  public void setLatestFinalVersion(Version latestFinalVersion) {
    this.latestFinalVersion = latestFinalVersion;
  }
}
