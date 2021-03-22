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

import org.openecomp.sdc.vendorsoftwareproduct.dao.type.DeploymentFlavorEntity;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.DeploymentFlavor;
import org.openecomp.sdcrests.mapping.MappingBase;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.DeploymentFlavorListResponseDto;

public class MapDeploymentFlavorEntityDeploymentFlavorToListResponse extends MappingBase<DeploymentFlavorEntity, DeploymentFlavorListResponseDto> {

    @Override
    public void doMapping(DeploymentFlavorEntity source, DeploymentFlavorListResponseDto target) {
        target.setId(source.getId());
        DeploymentFlavor deploymentFlavor = source.getDeploymentFlavorCompositionData();
        if (deploymentFlavor != null) {
            target.setModel(deploymentFlavor.getModel());
            target.setDescription(deploymentFlavor.getDescription());
        }
    }
}
