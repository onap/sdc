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

import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspDetails;
import org.openecomp.sdc.vendorsoftwareproduct.types.LicensingData;
import org.openecomp.sdcrests.mapping.MappingBase;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.VspDetailsDto;

public class MapVspDetailsToDto extends MappingBase<VspDetails, VspDetailsDto> {

  @Override
  public void doMapping(VspDetails source, VspDetailsDto target) {
    target.setId(source.getId());
    target.setVersion(source.getVersion() == null ? null : source.getVersion().getId());
    target.setName(source.getName());
    target.setDescription(source.getDescription());
    target.setIcon(source.getIcon());
    target.setCategory(source.getCategory());
    target.setSubCategory(source.getSubCategory());
    target.setVendorId(source.getVendorId());
    target.setVendorName(source.getVendorName());
    target.setLicensingVersion(
        source.getVlmVersion() == null ? null : source.getVlmVersion().getId());

    if (source.getLicenseAgreement() != null || source.getFeatureGroups() != null) {
      LicensingData licensingData = new LicensingData();
      licensingData.setLicenseAgreement(source.getLicenseAgreement());
      licensingData.setFeatureGroups(source.getFeatureGroups());
      target.setLicensingData(licensingData);
    }

    target.setOnboardingMethod(source.getOnboardingMethod());
  }
}
