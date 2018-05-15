/*
 * Copyright © 2016-2018 European Support Limited
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

package org.openecomp.sdc.vendorsoftwareproduct.dao.type;

import org.openecomp.core.utilities.json.JsonUtil;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.CompositionEntityId;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.CompositionEntityType;
import org.openecomp.sdc.versioning.dao.types.Version;

import java.util.List;

public class VspDataEntity implements CompositionEntity  {

  private Version vlmVersion;

  private String licenseAgreement;

  private List<String> featureGroups;

  private String compositionData;

  public Version getVlmVersion() {
    return vlmVersion;
  }

  public void setVlmVersion(Version vlmVersion) {
    this.vlmVersion = vlmVersion;
  }

  public String getLicenseAgreement() {
    return licenseAgreement;
  }

  public void setLicenseAgreement(String licenseAgreement) {
    this.licenseAgreement = licenseAgreement;
  }

  public List<String> getFeatureGroups() {
    return featureGroups;
  }

  public void setFeatureGroups(List<String> featureGroups) {
    this.featureGroups = featureGroups;
  }

  @Override
  public CompositionEntityType getType() {
    return CompositionEntityType.vsp;
  }

  @Override
  public CompositionEntityId getCompositionEntityId() {
    return null;
  }

  public String getCompositionData() {
    return compositionData;
  }

  public void setCompositionData(String compositionData) {
    this.compositionData = compositionData;
  }

  @Override
  public String getQuestionnaireData() {
    return null;
  }

  @Override
  public void setQuestionnaireData(String questionnaireData) {

  }

  public void setCompositionDataVspDataEntity(VspDataEntity vspDataEntity) {
    this.compositionData = vspDataEntity == null ? null : JsonUtil.object2Json(vspDataEntity);
  }

  @Override
  public String getEntityType() {
    return null;
  }

  @Override
  public String getFirstClassCitizenId() {
    return null;
  }

  @Override
  public String getId() {
    return null;
  }

  @Override
  public void setId(String id) {

  }

  @Override
  public Version getVersion() {
    return null;
  }

  @Override
  public void setVersion(Version version) {

  }
}
