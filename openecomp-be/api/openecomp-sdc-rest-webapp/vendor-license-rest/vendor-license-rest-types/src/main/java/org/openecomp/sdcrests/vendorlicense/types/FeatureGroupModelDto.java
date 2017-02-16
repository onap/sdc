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

import java.util.Collection;
import java.util.Set;

@ApiModel(value = "FeatureGroupModel")
public class FeatureGroupModelDto extends FeatureGroupDescriptorDto {
  private String id;
  private Set<String> referencingLicenseAgreements;
  private Collection<LicenseKeyGroupEntityDto> licenseKeyGroups;
  private Collection<EntitlementPoolEntityDto> entitlementPools;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public Set<String> getReferencingLicenseAgreements() {
    return referencingLicenseAgreements;
  }

  public void setReferencingLicenseAgreements(Set<String> referencingLicenseAgreements) {
    this.referencingLicenseAgreements = referencingLicenseAgreements;
  }

  public Collection<LicenseKeyGroupEntityDto> getLicenseKeyGroups() {
    return licenseKeyGroups;
  }

  public void setLicenseKeyGroups(Collection<LicenseKeyGroupEntityDto> licenseKeyGroups) {
    this.licenseKeyGroups = licenseKeyGroups;
  }

  public Collection<EntitlementPoolEntityDto> getEntitlementPools() {
    return entitlementPools;
  }

  public void setEntitlementPools(Collection<EntitlementPoolEntityDto> entitlementPools) {
    this.entitlementPools = entitlementPools;
  }
}
