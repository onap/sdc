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
 */
package org.openecomp.sdcrests.vendorlicense.rest.mapping;

import org.openecomp.sdc.vendorlicense.dao.types.LimitEntity;
import org.openecomp.sdcrests.mapping.MappingBase;
import org.openecomp.sdcrests.vendorlicense.types.LimitEntityDto;

public class MapLimitEntityToLimitDto extends MappingBase<LimitEntity, LimitEntityDto> {

    @Override
    public void doMapping(LimitEntity source, LimitEntityDto target) {
        target.setId(source.getId());
        target.setName(source.getName());
        target.setDescription(source.getDescription());
        target.setMetric(source.getMetric());
        target.setAggregationFunction(source.getAggregationFunction() != null ? source.getAggregationFunction().name() : null);
        target.setTime(source.getTime());
        target.setType(source.getType() != null ? source.getType().name() : null);
        target.setUnit(source.getUnit());
        target.setValue(source.getValue());
    }
}
