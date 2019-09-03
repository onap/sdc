/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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
 * Modifications copyright (c) 2019 Nokia
 * ================================================================================
 */

package org.openecomp.sdc.generator.datatypes.tosca;

@SuppressWarnings("CheckStyle")
public class VendorInfo {

  private String manufacturer_reference_number;
  private String vendor_model;

  public String getManufacturer_reference_number() {
    return manufacturer_reference_number;
  }

  public void setManufacturer_reference_number(String manufacturer_reference_number) {
    this.manufacturer_reference_number = manufacturer_reference_number;
  }

  public String getVendor_model() {
    return vendor_model;
  }

  public void setVendor_model(String vendor_model) {
    this.vendor_model = vendor_model;
  }

  @Override
  public String toString() {
    return "VendorInfo{manufacturer_reference_number='" + manufacturer_reference_number + '\''
        + ", vendor_model='" + vendor_model + '\'' + '}';
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null || getClass() != obj.getClass())
      return false;
    VendorInfo other = (VendorInfo) obj;

      if (this.manufacturer_reference_number == null) {
        if (other.manufacturer_reference_number != null)
          return false;
      } else if (!manufacturer_reference_number.equals(other.manufacturer_reference_number))
        return false;
      if (this.vendor_model == null) {
        if (other.vendor_model != null)
          return false;
      } else if (!vendor_model.equals(other.vendor_model))
        return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result =
        manufacturer_reference_number != null ? manufacturer_reference_number.hashCode() : 0;
    result = 31 * result + (vendor_model != null ? vendor_model.hashCode() : 0);
    return result;
  }
}
