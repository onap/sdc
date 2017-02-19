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

import org.openecomp.sdc.vendorlicense.dao.types.LicenseKeyGroupEntity;
import org.openecomp.sdcrests.mapping.MappingBase;
import org.openecomp.sdcrests.vendorlicense.types.LicenseKeyGroupEntityDto;
import org.openecomp.sdcrests.vendorlicense.types.MultiChoiceOrOtherDto;

public class MapLicenseKeyGroupEntityToLicenseKeyGroupEntityDto
    extends MappingBase<LicenseKeyGroupEntity, LicenseKeyGroupEntityDto> {
  @Override
  public void doMapping(LicenseKeyGroupEntity source, LicenseKeyGroupEntityDto target) {
    target.setId(source.getId());
    target.setName(source.getName());
    target.setDescription(source.getDescription());
    target.setType(source.getType());
    target.setOperationalScope(new MapMultiChoiceOrOtherToMultiChoiceOrOtherDto()
        .applyMapping(source.getOperationalScope(), MultiChoiceOrOtherDto.class));
    target.setReferencingFeatureGroups(source.getReferencingFeatureGroups());
  }
}
