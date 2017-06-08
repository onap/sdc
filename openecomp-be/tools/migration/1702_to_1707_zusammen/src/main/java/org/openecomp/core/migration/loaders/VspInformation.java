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

package org.openecomp.core.migration.loaders;

import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.Computed;
import com.datastax.driver.mapping.annotations.Frozen;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;
import org.openecomp.core.utilities.json.JsonUtil;
import org.openecomp.sdc.heat.datatypes.structure.ValidationStructureList;
import org.openecomp.sdc.versioning.dao.types.Version;

import java.nio.ByteBuffer;
import java.util.List;

@Table(keyspace = "dox", name = "vsp_information")
public class VspInformation {
  public static final String ENTITY_TYPE = "Vendor Software Product";

  @PartitionKey
  @Column(name = "vsp_id")
  private String id;

  @PartitionKey(value = 1)
  @Frozen
  private Version version;

  private String name;
  private String description;

  private String category;

  @Column(name = "sub_category")
  private String subCategory;

  private String icon;

  @Column(name = "vendor_name")
  private String vendorName;

  @Column(name = "vendor_id")
  private String vendorId;

  @Column(name = "vlm_version")
  @Frozen
  private Version vlmVersion;

  @Column(name = "license_agreement")
  private String licenseAgreement;

  @Column(name = "feature_groups")
  private List<String> featureGroups;

  @Column(name = "package_name")
  private String packageName;

  @Column(name = "package_version")
  private String packageVersion;

  @Column(name = "validation_data")
  private String validationData;

  @Column(name = "is_old_version")
  private String isOldVersion;

  @Column(name = "questionnaire_data")
  private String questionnaireData;

  @Column(name = "content_data")
  private ByteBuffer contentData;


  @Computed("writetime(name)")
  private Long writetimeMicroSeconds;


  public VspInformation() {
  }

  public VspInformation(String id, Version version) {
    this.id = id;
    this.version = version;
  }


  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }


  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getCategory() {
    return category;
  }

  public void setCategory(String category) {
    this.category = category;
  }

  public String getSubCategory() {
    return subCategory;
  }

  public void setSubCategory(String subCategory) {
    this.subCategory = subCategory;
  }

  public String getIcon() {
    return icon;
  }

  public void setIcon(String icon) {
    this.icon = icon;
  }

  public String getVendorName() {
    return vendorName;
  }

  public void setVendorName(String vendorName) {
    this.vendorName = vendorName;
  }

  public String getVendorId() {
    return vendorId;
  }

  public void setVendorId(String vendorId) {
    this.vendorId = vendorId;
  }

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

  public String getPackageName() {
    return packageName;
  }

  public void setPackageName(String packageName) {
    this.packageName = packageName;
  }

  public String getPackageVersion() {
    return packageVersion;
  }

  public void setPackageVersion(String packageVersion) {
    this.packageVersion = packageVersion;
  }

  public String getValidationData() {
    return validationData;
  }

  public void setValidationData(String validationData) {
    this.validationData = validationData;
  }

  public ValidationStructureList getValidationDataStructure() {
    return validationData == null ? null
        : JsonUtil.json2Object(validationData, ValidationStructureList.class);
  }

  public void setValidationDataStructure(ValidationStructureList validationData) {
    this.validationData = validationData == null ? null
        : JsonUtil.object2Json(validationData);
  }

  public Long getWritetimeMicroSeconds() {
    return this.writetimeMicroSeconds;
  }

  public void setWritetimeMicroSeconds(Long writetimeMicroSeconds) {
    this.writetimeMicroSeconds = writetimeMicroSeconds;
  }

  public String getIsOldVersion() {
    return this.isOldVersion;
  }

  public void setIsOldVersion(String oldVersion) {
    this.isOldVersion = oldVersion;
  }

  public void setVersion(Version version) {
    this.version = version;
  }

  public Version getVersion() {
    return version;
  }

  public String getQuestionnaireData() {
    return questionnaireData;
  }

  public void setQuestionnaireData(String questionnaireData) {
    this.questionnaireData = questionnaireData;
  }

  public ByteBuffer getContentData() {
    return contentData;
  }

  public void setContentData(ByteBuffer contentData) {
    this.contentData = contentData;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("Vsp id = ").append(this.id);
    sb.append("Version = ").append(this.version);

    return sb.toString();
  }
}
