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

package org.openecomp.sdcrests.vendorlicense.rest.mapping;

import org.openecomp.core.utilities.CommonMethods;
import org.openecomp.sdc.vendorlicense.dao.types.VendorLicenseModelEntity;
import org.openecomp.sdc.vendorlicense.types.VersionedVendorLicenseModel;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdc.versioning.types.VersionInfo;
import org.openecomp.sdcrests.common.types.VersionDto;
import org.openecomp.sdcrests.mapping.MappingBase;
import org.openecomp.sdcrests.vendorlicense.types.VendorLicenseModelEntityDto;

import java.util.stream.Collectors;

public class MapVersionedVendorLicenseModelToVendorLicenseModelEntityDto
    extends MappingBase<VersionedVendorLicenseModel, VendorLicenseModelEntityDto> {
  @Override
  public void doMapping(VersionedVendorLicenseModel source, VendorLicenseModelEntityDto target) {
    VendorLicenseModelEntity sourceVlm = source.getVendorLicenseModel();
    target.setId(sourceVlm.getId());
    target.setVendorName(sourceVlm.getVendorName());
    target.setDescription(sourceVlm.getDescription());
    target.setIconRef(sourceVlm.getIconRef());

    VersionInfo versionInfo = source.getVersionInfo();
    if (versionInfo != null) {
      target.setVersion(new VersionDto(versionInfo.getActiveVersion().toString(),versionInfo
          .getActiveVersion().toString()));
      target.setStatus(versionInfo.getStatus());
      target.setLockingUser(versionInfo.getLockingUser());

      if (!CommonMethods.isEmpty(versionInfo.getViewableVersions())) {
        target.setViewableVersions(versionInfo.getViewableVersions().stream().map(version->new
            VersionDto(version.toString(),version.toString()))
            .collect(Collectors.toList()));
      }

      if (!CommonMethods.isEmpty(versionInfo.getFinalVersions())) {
        target.setFinalVersions(versionInfo.getFinalVersions().stream().map(version->new
            VersionDto(version.toString(),version.toString()))
            .collect(Collectors.toList()));
      }
    }
  }
}
