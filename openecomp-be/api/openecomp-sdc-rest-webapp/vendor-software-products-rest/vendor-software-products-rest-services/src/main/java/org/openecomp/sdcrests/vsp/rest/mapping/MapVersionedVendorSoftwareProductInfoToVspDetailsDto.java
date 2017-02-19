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

package org.openecomp.sdcrests.vsp.rest.mapping;

import org.openecomp.core.utilities.CommonMethods;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspDetails;
import org.openecomp.sdc.vendorsoftwareproduct.types.LicensingData;
import org.openecomp.sdc.vendorsoftwareproduct.types.VersionedVendorSoftwareProductInfo;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdcrests.mapping.MappingBase;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.VspDetailsDto;

import java.util.stream.Collectors;

public class MapVersionedVendorSoftwareProductInfoToVspDetailsDto
    extends MappingBase<VersionedVendorSoftwareProductInfo, VspDetailsDto> {

  @Override
  public void doMapping(VersionedVendorSoftwareProductInfo source, VspDetailsDto target) {
    VspDetails vsp = source.getVspDetails();

    target.setId(vsp.getId());
    target.setVersion(vsp.getVersion().toString());
    target.setName(vsp.getName());
    target.setDescription(vsp.getDescription());
    target.setCategory(vsp.getCategory());
    target.setSubCategory(vsp.getSubCategory());
    target.setVendorId(vsp.getVendorId());
    target.setVendorName(vsp.getVendorName());
    target.setLicensingVersion(vsp.getVlmVersion() == null ? null : vsp.getVlmVersion().toString());

    if (vsp.getLicenseAgreement() != null || vsp.getFeatureGroups() != null) {
      LicensingData licensingData = new LicensingData();
      licensingData.setLicenseAgreement(vsp.getLicenseAgreement());
      licensingData.setFeatureGroups(vsp.getFeatureGroups());
      target.setLicensingData(licensingData);
    }

    target.setValidationData(vsp.getValidationDataStructure());

    target.setStatus(source.getVersionInfo().getStatus());
    target.setLockingUser(source.getVersionInfo().getLockingUser());

    if (!CommonMethods.isEmpty(source.getVersionInfo().getViewableVersions())) {
      target.setViewableVersions(
          source.getVersionInfo().getViewableVersions().stream().map(Version::toString)
              .collect(Collectors.toList()));
    }

    if (!CommonMethods.isEmpty(source.getVersionInfo().getFinalVersions())) {
      target.setFinalVersions(
          source.getVersionInfo().getFinalVersions().stream().map(Version::toString)
              .collect(Collectors.toList()));
    }
  }
}
