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

package org.openecomp.sdc.vendorsoftwareproduct.dao.type;

import com.datastax.driver.mapping.annotations.ClusteringColumn;
import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.Frozen;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;
import org.openecomp.core.enrichment.types.ArtifactType;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdc.versioning.dao.types.VersionableEntity;

import java.io.Serializable;
import java.nio.ByteBuffer;

@Table(keyspace = "dox", name = "vsp_component_artifact")
public class MibEntity implements VersionableEntity, Serializable {
  public static final String ENTITY_TYPE = "Vendor Software Product Component Artifact";
  @PartitionKey
  @Column(name = "vsp_id")
  private String vspId;
  @PartitionKey(value = 1)
  @Frozen
  private Version version;
  @ClusteringColumn
  @Column(name = "component_id")
  private String componentId;
  @ClusteringColumn(value = 1)
  @Column(name = "artifact_type")
  private ArtifactType type;
  @ClusteringColumn(value = 2)
  @Column(name = "artifact_id")
  private String id;
  @Column(name = "name")
  private String artifactName;
  private ByteBuffer artifact;

  public MibEntity() {

  }

  /**
   * Instantiates a new Component artifact entity.
   *
   * @param vspId       the vsp id
   * @param version     the version
   * @param componentId the component id
   * @param id          the id
   */
  public MibEntity(String vspId, Version version, String componentId, String id) {
    this.vspId = vspId;
    this.version = version;
    this.componentId = componentId;
    this.id = id;
  }

  public String getVspId() {
    return vspId;
  }

  public void setVspId(String vspId) {
    this.vspId = vspId;
  }

  public String getComponentId() {
    return componentId;
  }

  public void setComponentId(String componentId) {
    this.componentId = componentId;
  }

  @Override
  public String getEntityType() {
    return ENTITY_TYPE;
  }

  @Override
  public String getFirstClassCitizenId() {
    return getVspId();
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public Version getVersion() {
    return version;
  }

  public void setVersion(Version version) {
    this.version = version;
  }

  public String getArtifactName() {
    return artifactName;
  }

  public void setArtifactName(String artifactName) {
    this.artifactName = artifactName;
  }

  public ByteBuffer getArtifact() {
    return artifact;
  }

  public void setArtifact(ByteBuffer artifact) {
    this.artifact = artifact;
  }

  public ArtifactType getType() {
    return type;
  }

  public void setType(ArtifactType type) {
    this.type = type;
  }

  @Override
  public String toString() {
    return "MibEntity{" +
        "vspId='" + vspId + '\'' +
        ", version=" + version +
        ", componentId='" + componentId + '\'' +
        ", type=" + type +
        ", id='" + id + '\'' +
        ", artifactName='" + artifactName + '\'' +
        '}';
  }
}
