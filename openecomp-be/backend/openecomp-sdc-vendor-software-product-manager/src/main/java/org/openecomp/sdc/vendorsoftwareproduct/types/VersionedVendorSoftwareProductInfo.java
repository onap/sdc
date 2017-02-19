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

package org.openecomp.sdc.vendorsoftwareproduct.types;

import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspDetails;

public class VersionedVendorSoftwareProductInfo {
  private VspDetails vspDetails;
  private org.openecomp.sdc.versioning.types.VersionInfo versionInfo;

  public VersionedVendorSoftwareProductInfo() {
  }

  public VersionedVendorSoftwareProductInfo(VspDetails vspDetails,
                                   org.openecomp.sdc.versioning.types.VersionInfo versionInfo) {
    this.vspDetails = vspDetails;
    this.versionInfo = versionInfo;
  }

  public VspDetails getVspDetails() {
    return vspDetails;
  }

  public void setVspDetails(VspDetails vspDetails) {
    this.vspDetails = vspDetails;
  }

  public org.openecomp.sdc.versioning.types.VersionInfo getVersionInfo() {
    return versionInfo;
  }

  public void setVersionInfo(org.openecomp.sdc.versioning.types.VersionInfo versionInfo) {
    this.versionInfo = versionInfo;
  }
}
