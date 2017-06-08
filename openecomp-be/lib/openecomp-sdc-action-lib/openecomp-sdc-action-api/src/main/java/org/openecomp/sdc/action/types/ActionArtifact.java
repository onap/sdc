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

package org.openecomp.sdc.action.types;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.openecomp.sdc.action.dao.types.ActionArtifactEntity;

import java.nio.ByteBuffer;
import java.util.Date;


public class ActionArtifact {
  private String artifactUuId;

  @JsonIgnore
  private Integer effectiveVersion;

  private String artifactName;
  private String artifactLabel;
  private String artifactCategory;
  private String artifactDescription;
  private String artifactProtection;
  private Date timestamp;
  private byte[] artifact;

  public String getArtifactUuId() {
    return artifactUuId;
  }

  public void setArtifactUuId(String artifactUuId) {
    this.artifactUuId = artifactUuId;
  }

  public Integer getEffectiveVersion() {
    return effectiveVersion;
  }

  public void setEffectiveVersion(Integer effectiveVersion) {
    this.effectiveVersion = effectiveVersion;
  }

  public String getArtifactName() {
    return artifactName;
  }

  public void setArtifactName(String artifactName) {
    this.artifactName = artifactName;
  }

  public String getArtifactLabel() {
    return artifactLabel;
  }

  public void setArtifactLabel(String artifactLabel) {
    this.artifactLabel = artifactLabel;
  }

  public String getArtifactCategory() {
    return artifactCategory;
  }

  public void setArtifactCategory(String artifactCategory) {
    this.artifactCategory = artifactCategory;
  }

  public String getArtifactDescription() {
    return artifactDescription;
  }

  public void setArtifactDescription(String artifactDescription) {
    this.artifactDescription = artifactDescription;
  }

  public String getArtifactProtection() {
    return artifactProtection;
  }

  public void setArtifactProtection(String artifactProtection) {
    this.artifactProtection = artifactProtection;
  }

  public Date getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(Date timestamp) {
    this.timestamp = timestamp;
  }

  public byte[] getArtifact() {
    return artifact;
  }

  public void setArtifact(byte[] artifact) {
    this.artifact = artifact;
  }

  /**
   * To entity action artifact entity.
   *
   * @return the action artifact entity
   */
  public ActionArtifactEntity toEntity() {
    ActionArtifactEntity destination = new ActionArtifactEntity();
    destination.setArtifactUuId(this.getArtifactUuId());
    destination.setEffectiveVersion(this.getEffectiveVersion());
    destination.setArtifact(ByteBuffer.wrap(this.getArtifact()));

    return destination;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof ActionArtifact) {
      ActionArtifact temp = (ActionArtifact) obj;
      if (artifactUuId.equals(temp.getArtifactUuId())) {
        return true;
      }
    }
    return false;
  }
}
