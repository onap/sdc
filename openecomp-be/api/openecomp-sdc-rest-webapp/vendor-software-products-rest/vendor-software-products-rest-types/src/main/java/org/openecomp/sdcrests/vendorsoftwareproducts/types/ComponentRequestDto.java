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

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class ComponentRequestDto {

  private String name;
  @NotNull(message = "VFC displayName is mandatory.")
  @Size(min = 1, max = 30,message = "VFC displayName length should be between 1 and 30.")
  private String displayName;
  @Size(min = 0, max = 1000,message = "description length should not exceed 1000.")
  private String vfcCode;
  private String nfcCode;
  private String nfcFunction;
  private String description;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getVfcCode() {
    return vfcCode;
  }

  public void setVfcCode(String vfcCode) {
    this.vfcCode = vfcCode;
  }

  public String getNfcCode() {
    return nfcCode;
  }

  public void setNfcCode(String nfcCode) {
    this.nfcCode = nfcCode;
  }

  public String getNfcFunction() {
    return nfcFunction;
  }

  public void setNfcFunction(String nfcFunction) {
    this.nfcFunction = nfcFunction;
  }
}
