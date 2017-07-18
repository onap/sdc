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

package org.openecomp.sdc.vendorlicense.dao.types;

import java.util.HashSet;
import java.util.Set;

public class FeatureGroupModel {
  private FeatureGroupEntity featureGroup;
  private Set<EntitlementPoolEntity> entitlementPools = new HashSet<>();

  private Set<LicenseKeyGroupEntity> licenseKeyGroups = new HashSet<>();

  public FeatureGroupEntity getFeatureGroup() {
    return featureGroup;
  }

  public void setFeatureGroup(FeatureGroupEntity featureGroup) {
    this.featureGroup = featureGroup;
  }

  public Set<EntitlementPoolEntity> getEntitlementPools() {
    return entitlementPools;
  }

  public void setEntitlementPools(Set<EntitlementPoolEntity> entitlementPools) {
    this.entitlementPools = entitlementPools;
  }

  public Set<LicenseKeyGroupEntity> getLicenseKeyGroups() {
    return licenseKeyGroups;
  }

  public void setLicenseKeyGroups(Set<LicenseKeyGroupEntity> licenseKeyGroups) {
    this.licenseKeyGroups = licenseKeyGroups;
  }

  //for XML Artifact
  public String getEntityName() {
    return featureGroup.getName();
  }

  public String getEntityDesc() {
    return featureGroup.getDescription();
  }

  public String getEntityId() {
    return featureGroup.getId();
  }

  public String getEntityPartNumber() {
    return featureGroup.getPartNumber();
  }

  public String getEntityManufacturerReferenceNumber(){
    return featureGroup.getManufacturerReferenceNumber();
  }


}
