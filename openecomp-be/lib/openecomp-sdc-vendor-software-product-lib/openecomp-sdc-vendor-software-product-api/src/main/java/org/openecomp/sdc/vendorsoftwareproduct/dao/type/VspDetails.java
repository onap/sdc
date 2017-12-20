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

import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdc.versioning.dao.types.VersionableEntity;

import java.util.List;


public class VspDetails implements VersionableEntity {
  public static final String ENTITY_TYPE = "Vendor Software Product";

  private String id;

  private Version version;

  private String name;
  private String description;

  private String category;

  private String subCategory;

  private String icon;

  private String vendorName;

  private String vendorId;

  private Version vlmVersion;

  private String licenseAgreement;

  private List<String> featureGroups;

  private Long writetimeMicroSeconds;

  private String onboardingMethod;

  public VspDetails() {
  }

  public VspDetails(String id, Version version) {
    this.id = id;
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

  public Long getWritetimeMicroSeconds() {
    return this.writetimeMicroSeconds;
  }

  public void setWritetimeMicroSeconds(Long writetimeMicroSeconds) {
    this.writetimeMicroSeconds = writetimeMicroSeconds;
  }

  /*public boolean isOldVersion() {
    return this.oldVersion;
  }*/

  public String getOnboardingMethod() {
    return onboardingMethod;
  }

  public void setOnboardingMethod(String onboardingMethod) {
    this.onboardingMethod = onboardingMethod;
  }


  @Override
  public String toString() {
    return String.format(
        "Vsp id = '%s', Version = %s', Name = %s', Category = %s', Description = %s', Vendor = %s'",
        this.id, this.version, this.name, this.category, this.description, this.vendorName);
  }
}
