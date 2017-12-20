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

package org.openecomp.core.model.types;

import com.datastax.driver.mapping.annotations.ClusteringColumn;
import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.Frozen;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;
import com.google.common.io.ByteStreams;
import org.openecomp.sdc.datatypes.error.ErrorLevel;
import org.openecomp.sdc.logging.context.impl.MdcDataErrorMessage;
import org.openecomp.sdc.logging.types.LoggerConstants;
import org.openecomp.sdc.logging.types.LoggerErrorCode;
import org.openecomp.sdc.logging.types.LoggerErrorDescription;
import org.openecomp.sdc.logging.types.LoggerTragetServiceName;
import org.openecomp.sdc.versioning.dao.types.Version;

import java.io.IOException;
import java.nio.ByteBuffer;

@Table(keyspace = "dox", name = "vsp_enriched_service_artifact")
public class EnrichedServiceArtifactEntity implements ServiceElementEntity {
  private static final String ENTITY_TYPE;

  static {
    ENTITY_TYPE = "Vendor Software Product Service artifact";
  }

  @PartitionKey
  @Column(name = "vsp_id")
  public String id;

  @PartitionKey(value = 1)
  @Frozen
  public Version version;

  @ClusteringColumn
  @Column(name = "name")
  public String name;

  @Column(name = "content_data")
  public ByteBuffer contentData;

  /**
   * Instantiates a new Enriched service artifact entity.
   *
   * @param entity the entity
   */
  public EnrichedServiceArtifactEntity(ServiceArtifact entity) {
    this.id = entity.getVspId();
    this.version = entity.getVersion();
    this.name = entity.getName();

    try {
      this.contentData = ByteBuffer.wrap(ByteStreams.toByteArray(entity.getContent()));
    } catch (IOException ioException) { //todo fix typo in Traget
      MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_DB,
          LoggerTragetServiceName.CREATE_SERVICE_ENRICH_ARTIFACT, ErrorLevel.ERROR.name(),
          LoggerErrorCode.DATA_ERROR.getErrorCode(),
          LoggerErrorDescription.CREATE_ENRICH_SERVICE_ARTIFACT);
      throw new RuntimeException(ioException);
    }

  }

  @Override
  public String getEntityType() {
    return ENTITY_TYPE;
  }

  @Override
  public String getFirstClassCitizenId() {
    return getId();
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public void setId(String id) {
    this.id = id;
  }

  @Override
  public Version getVersion() {
    return version;
  }

  @Override
  public void setVersion(Version version) {
    this.version = version;
  }

  @Override
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public ByteBuffer getContentData() {
    return contentData;
  }

  public void setContentData(ByteBuffer contentData) {
    this.contentData = contentData;
  }

  /**
   * Gets service artifact.
   *
   * @return the service artifact
   */
  public ServiceArtifact getServiceArtifact() {
    ServiceArtifact serviceArtifact = new ServiceArtifact();
    serviceArtifact.setName(this.getName());
    serviceArtifact.setVersion(this.getVersion());
    serviceArtifact.setContentData(this.getContentData().array());
    serviceArtifact.setVspId(this.getId());
    return serviceArtifact;
  }
}
