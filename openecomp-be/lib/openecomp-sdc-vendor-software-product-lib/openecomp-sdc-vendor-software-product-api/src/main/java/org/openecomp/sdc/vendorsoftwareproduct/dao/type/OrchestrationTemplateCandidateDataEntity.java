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

import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.Frozen;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdc.versioning.dao.types.VersionableEntity;

import java.nio.ByteBuffer;

@Table(keyspace = "dox", name = "vsp_orchestration_template_candidate")
public class OrchestrationTemplateCandidateDataEntity implements VersionableEntity {
  private static final String ENTITY_TYPE =
      "Vendor Software Product Upload Orchestration_candidate data";

  @PartitionKey
  @Column(name = "vsp_id")
  private String id;

  @PartitionKey(value = 1)
  @Frozen
  private Version version;

  @Column(name = "content_data")
  private ByteBuffer contentData;

  @Column(name = "files_data_structure")
  private String filesDataStructure;

  public OrchestrationTemplateCandidateDataEntity() {
  }

  /**
   * Instantiates a new OrchestrationTemplateCandidateDataEntity.
   *
   * @param id       the id
   * @param version     the Version

   */
  public OrchestrationTemplateCandidateDataEntity(String id, Version version) {
    this.id = id;
    this.version = version;
  }

  /**
   * Instantiates a new OrchestrationTemplateCandidateDataEntity.
   *
   * @param filesDataStructure       the files data Structure
   * @param version     the version
   * @param contentData the contentData
   * @param id          the id
   */
  public OrchestrationTemplateCandidateDataEntity(String id, Version version,
                                                  ByteBuffer contentData,
                                                  String filesDataStructure) {
    this.id = id;
    this.contentData = contentData;
    this.filesDataStructure = filesDataStructure;
    this.version = version;
  }

  @Override
  public String getEntityType() {
    return ENTITY_TYPE;
  }

  @Override
  public String getFirstClassCitizenId() {
    return null;
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

  public ByteBuffer getContentData() {
    return contentData;
  }

  public void setContentData(ByteBuffer contentData) {
    this.contentData = contentData;
  }

  public String getFilesDataStructure() {
    return filesDataStructure;
  }

  public void setFilesDataStructure(String filesDataStructure) {
    this.filesDataStructure = filesDataStructure;
  }
}
