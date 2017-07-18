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
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdcrests.common.types.VersionDto;
import org.openecomp.sdcrests.mapping.MappingBase;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.VspDescriptionDto;

public class MapVspDescriptionDtoToVspDetails extends MappingBase<VspDescriptionDto, VspDetails> {

  @Override
  public void doMapping(VspDescriptionDto source, VspDetails target) {
    target.setName(source.getName());
    target.setDescription(source.getDescription());
    target.setCategory(source.getCategory());
    target.setSubCategory(source.getSubCategory());
    target.setIcon(source.getIcon());
    target.setVendorName(source.getVendorName());
    target.setVendorId(source.getVendorId());
    target.setOnboardingMethod(source.getOnboardingMethod());

    VersionDto vlmVersion = source.getLicensingVersion();
    if (vlmVersion != null) {
      Version version = Version.valueOf(vlmVersion.getId());
      target.setVlmVersion(version);
//      target.setVlmVersion(
//          Pattern.compile(Version.VERSION_REGEX).matcher(vlmVersion.getId()).find() ? Version
//              .valueOf(vlmVersion.getId()) : null);
    }

    LicensingData licensingData = source.getLicensingData();
    if (licensingData != null) {
      target.setLicenseAgreement(licensingData.getLicenseAgreement());
      target.setFeatureGroups(licensingData.getFeatureGroups());
    }
  }
}
