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

import javax.validation.constraints.Size;
import java.util.Set;

@ApiModel(value = "FeatureGroupEntity")
public class FeatureGroupEntityDto extends FeatureGroupDescriptorDto {
  private String id;
  private Set<String> licenseKeyGroupsIds;
  @Size(min = 1)
  private Set<String> entitlementPoolsIds;
  private Set<String> referencingLicenseAgreements;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public Set<String> getLicenseKeyGroupsIds() {
    return licenseKeyGroupsIds;
  }

  public void setLicenseKeyGroupsIds(Set<String> licenseKeyGroupsIds) {
    this.licenseKeyGroupsIds = licenseKeyGroupsIds;
  }

  public Set<String> getEntitlementPoolsIds() {
    return entitlementPoolsIds;
  }

  public void setEntitlementPoolsIds(Set<String> entitlementPoolsIds) {
    this.entitlementPoolsIds = entitlementPoolsIds;
  }

  public Set<String> getReferencingLicenseAgreements() {
    return referencingLicenseAgreements;
  }

  public void setReferencingLicenseAgreements(Set<String> referencingLicenseAgreements) {
    this.referencingLicenseAgreements = referencingLicenseAgreements;
  }
}
