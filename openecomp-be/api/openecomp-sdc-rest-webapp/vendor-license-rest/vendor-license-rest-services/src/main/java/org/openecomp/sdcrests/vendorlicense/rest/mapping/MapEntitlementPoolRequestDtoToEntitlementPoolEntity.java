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

import org.openecomp.sdc.vendorlicense.dao.types.EntitlementPoolEntity;
import org.openecomp.sdc.vendorlicense.dao.types.MultiChoiceOrOther;
import org.openecomp.sdcrests.mapping.MappingBase;
import org.openecomp.sdcrests.vendorlicense.types.EntitlementPoolRequestDto;

public class MapEntitlementPoolRequestDtoToEntitlementPoolEntity
    extends MappingBase<EntitlementPoolRequestDto, EntitlementPoolEntity> {
  @Override
  public void doMapping(EntitlementPoolRequestDto source, EntitlementPoolEntity target) {
    target.setName(source.getName());
    target.setManufacturerReferenceNumber(source.getManufacturerReferenceNumber());
    target.setDescription(source.getDescription());
    target.setType(source.getType());
    target.setThresholdValue(source.getThresholdValue());
    target.setThresholdUnit(source.getThresholdUnits());
    target.setIncrements(source.getIncrements());
    target.setOperationalScope(new MapMultiChoiceOrOtherDtoToMultiChoiceOrOther()
        .applyMapping(source.getOperationalScope(), MultiChoiceOrOther.class));

    target.setStartDate(source.getStartDate());
    target.setExpiryDate(source.getExpiryDate());
  }
}
