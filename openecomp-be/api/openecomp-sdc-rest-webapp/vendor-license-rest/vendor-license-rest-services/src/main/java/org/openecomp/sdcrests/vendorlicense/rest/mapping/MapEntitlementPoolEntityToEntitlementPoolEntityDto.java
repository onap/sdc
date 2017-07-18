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
import org.openecomp.sdcrests.mapping.MappingBase;
import org.openecomp.sdcrests.vendorlicense.types.ChoiceOrOtherDto;
import org.openecomp.sdcrests.vendorlicense.types.EntitlementPoolEntityDto;
import org.openecomp.sdcrests.vendorlicense.types.MultiChoiceOrOtherDto;

public class MapEntitlementPoolEntityToEntitlementPoolEntityDto
    extends MappingBase<EntitlementPoolEntity, EntitlementPoolEntityDto> {
  @Override
  public void doMapping(EntitlementPoolEntity source, EntitlementPoolEntityDto target) {
    target.setId(source.getId());
    target.setName(source.getName());
    target.setDescription(source.getDescription());
    target.setThresholdValue(source.getThresholdValue());
    target.setThresholdUnits(source.getThresholdUnit());
    target.setIncrements(source.getIncrements());

    MapChoiceOrOtherToChoiceOrOtherDto choiceOrOtherMapper =
        new MapChoiceOrOtherToChoiceOrOtherDto();
    target.setEntitlementMetric(
        choiceOrOtherMapper.applyMapping(source.getEntitlementMetric(), ChoiceOrOtherDto.class));
    target.setAggregationFunction(
        choiceOrOtherMapper.applyMapping(source.getAggregationFunction(), ChoiceOrOtherDto.class));
    target.setOperationalScope(new MapMultiChoiceOrOtherToMultiChoiceOrOtherDto()
        .applyMapping(source.getOperationalScope(), MultiChoiceOrOtherDto.class));
    target.setTime(choiceOrOtherMapper.applyMapping(source.getTime(), ChoiceOrOtherDto.class));
    target.setManufacturerReferenceNumber(source.getManufacturerReferenceNumber());
    target.setReferencingFeatureGroups(source.getReferencingFeatureGroups());

    target.setStartDate(source.getStartDate());
    target.setExpiryDate(source.getExpiryDate());
  }
}
