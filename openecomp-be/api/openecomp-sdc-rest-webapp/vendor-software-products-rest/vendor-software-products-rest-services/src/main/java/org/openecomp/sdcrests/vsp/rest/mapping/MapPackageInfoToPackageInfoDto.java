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

import org.openecomp.sdc.vendorsoftwareproduct.dao.type.PackageInfo;
import org.openecomp.sdcrests.mapping.MappingBase;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.PackageInfoDto;

/**
 * Created by TALIO on 4/25/2016.
 */
public class MapPackageInfoToPackageInfoDto extends MappingBase<PackageInfo, PackageInfoDto> {
  @Override
  public void doMapping(PackageInfo source, PackageInfoDto target) {
    target.setVspName(source.getVspName());
    target.setPackageId(source.getVspId());
    target.setDescription(source.getVspDescription());
    target.setVersion(source.getVersion());
    target.setPackageType(source.getPackageType());
    target.setCategory(source.getCategory());
    target.setSubCategory(source.getSubCategory());
    target.setPackageChecksum(source.getPackageChecksum());
    target.setVendorRelease(source.getVendorRelease());
    target.setVendorName(source.getVendorName());
  }
}
