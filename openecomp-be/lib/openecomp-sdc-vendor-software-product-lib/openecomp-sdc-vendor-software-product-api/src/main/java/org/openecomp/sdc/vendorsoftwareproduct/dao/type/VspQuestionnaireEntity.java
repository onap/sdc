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

import org.openecomp.sdc.vendorsoftwareproduct.types.composition.CompositionEntityId;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.CompositionEntityType;
import org.openecomp.sdc.versioning.dao.types.Version;



public class VspQuestionnaireEntity implements CompositionEntity {
  private static final String ENTITY_TYPE = "Vendor Software Product";

  private String id;
  private Version version;
  private String questionnaireData;

  public VspQuestionnaireEntity() {

  }

  public VspQuestionnaireEntity(String vspId, Version version) {
    this.id = vspId;
    this.version = version;
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
  public CompositionEntityType getType() {
    return CompositionEntityType.vsp;
  }

  @Override
  public CompositionEntityId getCompositionEntityId() {
    return new CompositionEntityId(getId(), null);
  }

  @Override
  public String getCompositionData() {
    return null; //none
  }

  @Override
  public void setCompositionData(String compositionData) {
    //none
  }

  public String getQuestionnaireData() {
    return questionnaireData;
  }

  public void setQuestionnaireData(String questionnaireData) {
    this.questionnaireData = questionnaireData;
  }

}
