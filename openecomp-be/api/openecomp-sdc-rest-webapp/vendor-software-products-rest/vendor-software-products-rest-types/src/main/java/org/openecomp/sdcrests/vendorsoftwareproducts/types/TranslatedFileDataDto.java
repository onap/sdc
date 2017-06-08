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

/**
 * Created by TALIO on 4/20/2016.
 */
public class TranslatedFileDataDto {

  private String displayName;
  private String version;
  private String category;
  private String subcategory;
  private String vendorName;
  private String vendorRelease;
  private String packageChecksum;
  private String packageType;

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public String getCategory() {
    return category;
  }

  public void setCategory(String category) {
    this.category = category;
  }

  public String getSubcategory() {
    return subcategory;
  }

  public void setSubcategory(String subcategory) {
    this.subcategory = subcategory;
  }

  public String getVandorName() {
    return vendorName;
  }

  public void setVandorName(String vandorName) {
    this.vendorName = vandorName;
  }

  public String getVendorRelease() {
    return vendorRelease;
  }

  public void setVendorRelease(String vendorRelease) {
    this.vendorRelease = vendorRelease;
  }

  public String getPackageChecksum() {
    return packageChecksum;
  }

  public void setPackageChecksum(String packageChecksum) {
    this.packageChecksum = packageChecksum;
  }

  public String getPackageType() {
    return packageType;
  }

  public void setPackageType(String packageType) {
    this.packageType = packageType;
  }
}
