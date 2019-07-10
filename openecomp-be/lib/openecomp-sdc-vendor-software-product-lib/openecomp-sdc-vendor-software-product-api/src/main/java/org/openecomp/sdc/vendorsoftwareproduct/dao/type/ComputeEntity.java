/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

import com.datastax.driver.mapping.annotations.*;
import org.openecomp.core.utilities.json.JsonUtil;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.CompositionEntityId;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.CompositionEntityType;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.ComputeData;
import org.openecomp.sdc.versioning.dao.types.Version;

@Table(keyspace = "dox", name = "vsp_component_compute")
public class ComputeEntity implements CompositionEntity {
  public static final String ENTITY_TYPE = "Vendor Software Product Component Compute Flavor";

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
  @Column(name = "compute_id")
  private String id;
  @Column(name = "composition_data")
  private String compositionData;
  @Column(name = "questionnaire_data")
  private String questionnaireData;

  /**
   * Every entity class must have a default constructor according to
   * <a href="http://docs.datastax.com/en/developer/java-driver/2.1/manual/object_mapper/creating/">
   * Definition of mapped classes</a>.
   */
  public ComputeEntity() {
    // Don't delete! Default constructor is required by DataStax driver
  }

  public ComputeEntity(String vspId, Version version, String componentId, String id) {
    this.vspId = vspId;
    this.version = version;
    this.componentId = componentId;
    this.id = id;
  }

  @Override
  public String getEntityType() {
    return ENTITY_TYPE;
  }

  @Override
  public String getFirstClassCitizenId() {
    return getVspId();
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
  public CompositionEntityType getType() {
    return CompositionEntityType.compute;
  }

  @Override
  public CompositionEntityId getCompositionEntityId() {
    return new CompositionEntityId(getId(),
        new CompositionEntityId(getComponentId(), new CompositionEntityId(getVspId(), null)));
  }

  @Override
  public String getCompositionData() {
    return compositionData;
  }

  @Override
  public void setCompositionData(String compositionData) {
    this.compositionData = compositionData;
  }

  @Override
  public String getQuestionnaireData() {
    return questionnaireData;
  }

  @Override
  public void setQuestionnaireData(String questionnaireData) {
    this.questionnaireData = questionnaireData;
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

  public ComputeData getComputeCompositionData() {
    return compositionData == null ? null
        : JsonUtil.json2Object(compositionData, ComputeData.class);
  }

  public void setComputeCompositionData(ComputeData computeData){
    this.compositionData = computeData == null ? null : JsonUtil.object2Json(computeData);
  }


}
