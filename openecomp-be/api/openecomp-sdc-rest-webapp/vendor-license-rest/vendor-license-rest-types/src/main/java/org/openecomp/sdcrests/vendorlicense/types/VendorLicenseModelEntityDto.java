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

package org.openecomp.sdcrests.vendorlicense.types;

import io.swagger.annotations.ApiModel;
import org.openecomp.sdc.versioning.dao.types.VersionStatus;
import org.openecomp.sdcrests.common.types.VersionDto;

import java.util.List;

@ApiModel(value = "VendorLicenseModelEntity")
public class VendorLicenseModelEntityDto extends VendorLicenseModelRequestDto {
  private String id;
  private VersionDto version;
  private VersionStatus status;
  private String lockingUser;
  private List<VersionDto> viewableVersions;
  private List<VersionDto> finalVersions;

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
}
