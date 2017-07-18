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
import com.datastax.driver.mapping.annotations.Transient;
import org.openecomp.core.utilities.json.JsonUtil;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.ComponentData;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.CompositionEntityId;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.CompositionEntityType;
import org.openecomp.sdc.versioning.dao.types.Version;

import java.util.ArrayList;
import java.util.List;


@Table(keyspace = "dox", name = "vsp_component")
public class ComponentEntity implements CompositionEntity {
  public static final String ENTITY_TYPE = "Vendor Software Product Component";

  @PartitionKey
  @Column(name = "vsp_id")
  private String vspId;
  @PartitionKey(value = 1)
  @Frozen
  private Version version;
  @ClusteringColumn
  @Column(name = "component_id")
  private String id;
  @Column(name = "composition_data")
  private String compositionData;
  @Column(name = "questionnaire_data")
  private String questionnaireData;
  @Transient
  private List<NicEntity> nics = new ArrayList<>();

  public ComponentEntity() {

  }

  /**
   * Instantiates a new Component entity.
   *
   * @param vspId   the vsp id
   * @param version the version
   * @param id      the id
   */
  public ComponentEntity(String vspId, Version version, String id) {
    this.vspId = vspId;
    this.version = version;
    this.id = id;
  }

  @Override
  public CompositionEntityType getType() {
    return CompositionEntityType.component;
  }

  @Override
  public CompositionEntityId getCompositionEntityId() {
    return new CompositionEntityId(getId(), new CompositionEntityId(getVspId(), null));
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

  public ComponentData getComponentCompositionData() {
    return compositionData == null ? null
        : JsonUtil.json2Object(compositionData, ComponentData.class);
  }

  public void setComponentCompositionData(ComponentData component) {
    this.compositionData = component == null ? null : JsonUtil.object2Json(component);
  }

  public List<NicEntity> getNics() {
    return nics;
  }

  public void setNics(List<NicEntity> nics) {
    this.nics = nics;
  }

  @Override
  public int hashCode() {
    int result = vspId != null ? vspId.hashCode() : 0;
    result = 31 * result + (version != null ? version.hashCode() : 0);
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

    ComponentEntity that = (ComponentEntity) object;

    if (vspId != null ? !vspId.equals(that.vspId) : that.vspId != null) {
      return false;
    }
    if (version != null ? !version.equals(that.version) : that.version != null) {
      return false;
    }
    if (id != null ? !id.equals(that.id) : that.id != null) {
      return false;
    }
    if (compositionData != null ? !compositionData.equals(that.compositionData)
        : that.compositionData != null) {
      return false;
    }
    return questionnaireData != null ? questionnaireData.equals(that.questionnaireData)
        : that.questionnaireData == null;

  }
}
