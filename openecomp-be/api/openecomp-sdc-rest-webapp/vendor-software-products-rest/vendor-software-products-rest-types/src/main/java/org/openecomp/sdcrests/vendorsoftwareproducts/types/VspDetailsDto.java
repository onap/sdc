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

package org.openecomp.sdcrests.vendorsoftwareproducts.types;

import io.swagger.annotations.ApiModel;
import org.openecomp.sdc.heat.datatypes.structure.ValidationStructureList;
import org.openecomp.sdc.versioning.dao.types.VersionStatus;
import org.openecomp.sdcrests.common.types.VersionDto;

import java.util.List;

/**
 * Created by TALIO on 4/25/2016.
 */
@ApiModel(value = "VspDetails")
public class VspDetailsDto extends VspDescriptionDto {

  private String id;
  private VersionDto version;
  private List<VersionDto> viewableVersions;
  private List<VersionDto> finalVersions;
  private VersionStatus status;
  private String lockingUser;
  private ValidationStructureList validationData;
  private String isOldVersion;
  private String onboardingOrigin;
  private String networkPackageName;
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public VersionDto getVersion() {
    return version;
  }

  public void setVersion(VersionDto version) {
    this.version = version;
  }

  public List<VersionDto> getViewableVersions() {
    return viewableVersions;
  }

  public void setViewableVersions(List<VersionDto> viewableVersions) {
    this.viewableVersions = viewableVersions;
  }

  public List<VersionDto> getFinalVersions() {
    return finalVersions;
  }

  public void setFinalVersions(List<VersionDto> finalVersions) {
    this.finalVersions = finalVersions;
  }

  public VersionStatus getStatus() {
    return status;
  }

  public void setStatus(VersionStatus status) {
    this.status = status;
  }

  public String getLockingUser() {
    return lockingUser;
  }

  public void setLockingUser(String lockingUser) {
    this.lockingUser = lockingUser;
  }

  public ValidationStructureList getValidationData() {
    return validationData;
  }

  public String getIsOldVersion() {
    return isOldVersion;
  }

  public void setIsOldVersion(String isOldVersion) {
    this.isOldVersion = isOldVersion;
  }

  public void setValidationData(ValidationStructureList validationData) {
    this.validationData = validationData;
  }

  public String getOnboardingOrigin() {
    return onboardingOrigin;
  }

  public void setOnboardingOrigin(String onboardingOrigin) {
    this.onboardingOrigin = onboardingOrigin;
  }

  public String getNetworkPackageName() {
    return networkPackageName;
  }

  public void setNetworkPackageName(String networkPackageName) {
    this.networkPackageName = networkPackageName;
  }
}
