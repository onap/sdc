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

/*
 * Copyright © 2016-2018 European Support Limited
 *
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
 */

package org.openecomp.sdcrests.vsp.rest.mapping;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.List;
import org.junit.Test;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.DeploymentFlavorEntity;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.ComponentComputeAssociation;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.DeploymentFlavor;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.DeploymentFlavorRequestDto;

/**
 * This class was generated.
 */
public class MapDeploymentFlavorRequestDtoToDeploymentFlavorEntityTest {

    @Test()
    public void testConversion() {

        final DeploymentFlavorRequestDto source = new DeploymentFlavorRequestDto();

        final String model = "a4bea41c-d14a-4249-841a-31668485ced7";
        source.setModel(model);

        final String description = "1ec317d3-58e7-47ef-9c9c-00afa0a5414d";
        source.setDescription(description);

        final String featureGroupId = "356f3c57-4531-4e22-a988-bfc8571d3c91";
        source.setFeatureGroupId(featureGroupId);

        final List<ComponentComputeAssociation> componentComputeAssociations = Collections.emptyList();
        source.setComponentComputeAssociations(componentComputeAssociations);

        final DeploymentFlavorEntity target = new DeploymentFlavorEntity();
        final MapDeploymentFlavorRequestDtoToDeploymentFlavorEntity mapper = new MapDeploymentFlavorRequestDtoToDeploymentFlavorEntity();
        mapper.doMapping(source, target);

        DeploymentFlavor deploymentFlavor = target.getDeploymentFlavorCompositionData();
        assertEquals(model, deploymentFlavor.getModel());
        assertEquals(description, deploymentFlavor.getDescription());
        assertEquals(featureGroupId, deploymentFlavor.getFeatureGroupId());
        assertEquals(componentComputeAssociations, deploymentFlavor.getComponentComputeAssociations());
    }
}
