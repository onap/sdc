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
package org.openecomp.sdcrests.vsp.rest.mapping;

import org.openecomp.core.utilities.json.JsonUtil;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComputeEntity;
import org.openecomp.sdcrests.mapping.MappingBase;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.ComputeDescription;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.VspComputeDto;

public class MapComputeEntityToVspComputeDto extends MappingBase<ComputeEntity, VspComputeDto> {

    @Override
    public void doMapping(ComputeEntity source, VspComputeDto target) {
        target.setComputeFlavorId(source.getId());
        if (source.getCompositionData() != null) {
            ComputeDescription desc = JsonUtil.json2Object(source.getCompositionData(), ComputeDescription.class);
            target.setName(desc.getName());
            target.setComponentId(source.getComponentId());
        }
    }
}
