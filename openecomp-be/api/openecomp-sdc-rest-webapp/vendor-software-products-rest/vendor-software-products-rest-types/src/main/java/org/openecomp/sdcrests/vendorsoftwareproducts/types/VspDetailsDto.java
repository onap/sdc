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

/**
 * Created by TALIO on 4/25/2016.
 */
@ApiModel(value = "VspDetails")
public class VspDetailsDto extends VspRequestDto {

  private String id;
  private String version;
  private ValidationStructureList validationData;
  private String candidateOnboardingOrigin;
  private String onboardingOrigin;
  private String networkPackageName;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public ValidationStructureList getValidationData() {
    return validationData;
  }

  public void setValidationData(ValidationStructureList validationData) {
    this.validationData = validationData;
  }

  public String getCandidateOnboardingOrigin() {
    return candidateOnboardingOrigin;
  }

  public void setCandidateOnboardingOrigin(String candidateOnboardingOrigin) {
    this.candidateOnboardingOrigin = candidateOnboardingOrigin;
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
