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
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.Image;
import org.openecomp.sdc.versioning.dao.types.Version;

@Table(keyspace = "dox", name = "vsp_component_image")
public class ImageEntity implements CompositionEntity {

  private static final String ENTITY_TYPE = "Vendor Software Product Component Image";

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
  @Column(name = "image_id")
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
  public ImageEntity() {
    // Don't delete! Default constructor is required by DataStax driver
  }

  /**
   * Instantiates a new Image entity.
   *
   * @param vspId       the vsp id
   * @param version     the version
   * @param id          the id
   */
  public ImageEntity(String vspId, Version version, String componentId, String id) {
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

  public Version getVersion() {
    return version;
  }

  public void setVersion(Version version) {
    this.version = version;
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

  public String getQuestionnaireData() {
    return questionnaireData;
  }

  public void setQuestionnaireData(String questionnaireData) {
    this.questionnaireData = questionnaireData;
  }

  @Override
  public CompositionEntityType getType() {
    return CompositionEntityType.image;
  }

  @Override
  public CompositionEntityId getCompositionEntityId() {
    return new CompositionEntityId(getId(),
        new CompositionEntityId(getComponentId(), new CompositionEntityId(getVspId(), null)));
  }

  public String getCompositionData() {
    return compositionData;
  }

  public void setCompositionData(String compositionData) {
    this.compositionData = compositionData;
  }

  public Image getImageCompositionData() {
    return compositionData == null ? null : JsonUtil.json2Object(compositionData, Image.class);
  }

  public void setImageCompositionData(Image image) {
    this.compositionData = image == null ? null : JsonUtil.object2Json(image);
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

    ImageEntity imageEntity = (ImageEntity) object;

    if (vspId != null ? !vspId.equals(imageEntity.vspId) : imageEntity.vspId != null) {
      return false;
    }
    if (version != null ? !version.equals(imageEntity.version) : imageEntity.version != null) {
      return false;
    }
    if (componentId != null ? !componentId.equals(imageEntity.componentId)
        : imageEntity.componentId != null) {
      return false;
    }
    if (id != null ? !id.equals(imageEntity.id) : imageEntity.id != null) {
      return false;
    }
    if (compositionData != null ? !compositionData.equals(imageEntity.compositionData)
        : imageEntity.compositionData != null) {
      return false;
    }
    return questionnaireData != null ? questionnaireData.equals(imageEntity.questionnaireData)
        : imageEntity.questionnaireData == null;

  }
}
