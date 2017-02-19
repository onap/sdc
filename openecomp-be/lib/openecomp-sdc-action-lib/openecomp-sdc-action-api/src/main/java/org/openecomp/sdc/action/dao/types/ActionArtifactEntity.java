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

package org.openecomp.sdc.action.dao.types;

import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;
import org.openecomp.sdc.action.types.ActionArtifact;

import java.nio.ByteBuffer;

@Table(keyspace = "dox", name = "action_artifact")
public class ActionArtifactEntity {

  @PartitionKey
  @Column(name = "artifactuuid")
  private String artifactUuId;

  @PartitionKey(value = 1)
  @Column(name = "effective_version")
  private int effectiveVersion;

  @Column(name = "artifact")
  private ByteBuffer artifact;

  public ActionArtifactEntity() {
    //Default constructor implementation
  }

  public ActionArtifactEntity(String artifactUuId, int effectiveVersion) {
    this.artifactUuId = artifactUuId;
    this.effectiveVersion = effectiveVersion;
  }

  public String getArtifactUuId() {
    return artifactUuId;
  }

  public void setArtifactUuId(String artifactUuId) {
    this.artifactUuId = artifactUuId;
  }

  public int getEffectiveVersion() {
    return effectiveVersion;
  }

  public void setEffectiveVersion(int effectiveVersion) {
    this.effectiveVersion = effectiveVersion;
  }

  public ByteBuffer getArtifact() {
    return artifact;
  }

  public void setArtifact(ByteBuffer artifact) {
    this.artifact = artifact;
  }

  /**
   * To dto action artifact.
   *
   * @return the action artifact
   */
  public ActionArtifact toDto() {
    ActionArtifact destination = new ActionArtifact();
    destination.setArtifactUuId(this.getArtifactUuId());
    destination.setEffectiveVersion(this.getEffectiveVersion());
    destination.setArtifact(this.getArtifact().array());
    return destination;
  }
}
