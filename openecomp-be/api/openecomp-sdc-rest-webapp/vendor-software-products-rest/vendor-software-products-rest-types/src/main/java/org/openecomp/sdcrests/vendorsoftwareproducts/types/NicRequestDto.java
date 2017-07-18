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

import org.hibernate.validator.constraints.NotBlank;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.validation.ValidateString;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

public class NicRequestDto {

  @NotBlank(message = "is mandatory and should not be empty")
  @Pattern(regexp = "^[a-zA-Z 0-9._-]*$", message = "must match \"^[a-zA-Z 0-9._-]*$\"")
  private String name;
  private String description;
  private String networkId;
  @NotNull
  @ValidateString(acceptedValues = {"External", "Internal"}, message =   "doesn't "
      +  "meet the expected attribute value.", isCaseSensitive = true)
  private String networkType;

  private String networkDescription;

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

  public String getNetworkId() {
    return networkId;
  }

  public void setNetworkId(String networkId) {
    this.networkId = networkId;
  }

  public String getNetworkType(){
    return networkType;
  }

  public void setNetworkType(String networkType){
    this.networkType = networkType;
  }

  public String getNetworkDescription(){
    return networkDescription;
  }

  public void setNetworkDescription(String networkDescription){
    this.networkDescription = networkDescription;
  }
}
