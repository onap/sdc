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

@Table(keyspace = "dox", name = "vsp_service_template")
public class ServiceTemplateEntity implements ServiceElementEntity {

  private static final String ENTITY_TYPE;

  static {
    ENTITY_TYPE = "Vendor Software Product Service model";
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

  @Column(name = "base_name")
  private String baseName;

  public ServiceTemplateEntity() {
  }

  /**
   * Instantiates a new Service template entity.
   *
   * @param entity the entity
   */
  public ServiceTemplateEntity(ServiceTemplate entity) {
    this.id = entity.getVspId();
    this.version = entity.getVersion();
    this.name = entity.getName();
    this.setBaseName(entity.getBaseName());
    try {
      this.contentData = ByteBuffer.wrap(ByteStreams.toByteArray(entity.getContent()));
    } catch (IOException ioException) {
      MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_DB,
          LoggerTragetServiceName.CREATE_SERVICE_TEMPLATE, ErrorLevel.ERROR.name(),
          LoggerErrorCode.DATA_ERROR.getErrorCode(),
          LoggerErrorDescription.CREATE_SERVICE_TEMPLATE);
      throw new RuntimeException(ioException);
    }

  }

  public String getBaseName() {
    return baseName;
  }

  public void setBaseName(String baseName) {
    this.baseName = baseName;
  }

  @Override
  public String getEntityType() {
    return ENTITY_TYPE;
  }

  @Override
  public String getFirstClassCitizenId() {
    return getId();
  }

  public String getId() {
    return id;
  }

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

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public ByteBuffer getContentData() {
    return contentData;
  }

  public void setContentData(ByteBuffer contentData) {
    this.contentData = contentData;
  }


  /**
   * Gets service template.
   *
   * @return the service template
   */
  public ServiceTemplate getServiceTemplate() {
    ServiceTemplate serviceTemplate = new ServiceTemplate();
    serviceTemplate.setName(this.getName());
    serviceTemplate.setVersion(this.getVersion());
    serviceTemplate.setContentData(this.getContentData().array());
    serviceTemplate.setVspId(this.getId());
    serviceTemplate.setBaseName(this.getBaseName());
    return serviceTemplate;

  }
}
