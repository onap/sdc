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

import org.openecomp.sdc.vendorsoftwareproduct.types.LicensingData;

import javax.validation.constraints.NotNull;

public class VspDescriptionDto {
  @NotNull
  private String name;
  @NotNull
  private String description;
  private String icon;
  @NotNull
  private String category;
  @NotNull
  private String subCategory;
  @NotNull
  private String vendorName;
  @NotNull
  private String vendorId;            // this will be populated with vlm id
  private String licensingVersion;    // this will be populated with vlm version
  private LicensingData licensingData;

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

  public String getIcon() {
    return this.icon;
  }

  public void setIcon(String icon) {
    this.icon = icon;
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

  public String getLicensingVersion() {
    return licensingVersion;
  }

  public void setLicensingVersion(String licensingVersion) {
    this.licensingVersion = licensingVersion;
  }

  public LicensingData getLicensingData() {
    return licensingData;
  }

  public void setLicensingData(LicensingData licensingData) {
    this.licensingData = licensingData;
  }
}
