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
import org.openecomp.core.utilities.json.JsonUtil;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.CompositionEntityId;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.CompositionEntityType;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.Nic;
import org.openecomp.sdc.versioning.dao.types.Version;


@Table(keyspace = "dox", name = "vsp_component_nic")
public class NicEntity implements CompositionEntity {
  private static final String ENTITY_TYPE = "Vendor Software Product NIC";

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
  @Column(name = "nic_id")
  private String id;
  @Column(name = "composition_data")
  private String compositionData;
  @Column(name = "questionnaire_data")
  private String questionnaireData;

  public NicEntity() {

  }

  /**
   * Instantiates a new Nic entity.
   *
   * @param vspId       the vsp id
   * @param version     the version
   * @param componentId the component id
   * @param id          the id
   */
  public NicEntity(String vspId, Version version, String componentId, String id) {
    this.vspId = vspId;
    this.version = version;
    this.componentId = componentId;
    this.id = id;
  }

  @Override
  public CompositionEntityType getType() {
    return CompositionEntityType.nic;
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

  public String getComponentId() {
    return componentId;
  }

  public void setComponentId(String componentId) {
    this.componentId = componentId;
  }

  public Nic getNicCompositionData() {
    return compositionData == null ? null : JsonUtil.json2Object(compositionData, Nic.class);
  }

  public void setNicCompositionData(Nic nic) {
    this.compositionData = nic == null ? null : JsonUtil.object2Json(nic);
  }

  @Override
  public int hashCode() {
    int result = vspId != null ? vspId.hashCode() : 0;
    result = 31 * result + (version != null ? version.hashCode() : 0);
    result = 31 * result + (componentId != null ? componentId.hashCode() : 0);
    result = 31 * result + (id != null ? id.hashCode() : 0);
    result = 31 * result + (compositionData != null ? compositionData.hashCode() : 0);
    result = 31 * result + (questionnaireData != null ? questionnaireData.hashCode() : 0);
    return result;
  }

  @Override
  public boolean equals(Object object) {
    if (this == object) {
      return true;
    }
    if (object == null || getClass() != object.getClass()) {
      return false;
    }

    NicEntity nicEntity = (NicEntity) object;

    if (vspId != null ? !vspId.equals(nicEntity.vspId) : nicEntity.vspId != null) {
      return false;
    }
    if (version != null ? !version.equals(nicEntity.version) : nicEntity.version != null) {
      return false;
    }
    if (componentId != null ? !componentId.equals(nicEntity.componentId)
        : nicEntity.componentId != null) {
      return false;
    }
    if (id != null ? !id.equals(nicEntity.id) : nicEntity.id != null) {
      return false;
    }
    if (compositionData != null ? !compositionData.equals(nicEntity.compositionData)
        : nicEntity.compositionData != null) {
      return false;
    }
    return questionnaireData != null ? questionnaireData.equals(nicEntity.questionnaireData)
        : nicEntity.questionnaireData == null;

  }
}
