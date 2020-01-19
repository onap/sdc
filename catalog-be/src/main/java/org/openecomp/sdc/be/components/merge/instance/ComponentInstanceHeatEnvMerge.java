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

package org.openecomp.sdc.be.components.merge.instance;

import fj.data.Either;
import org.openecomp.sdc.be.components.impl.ArtifactsBusinessLogic;
import org.openecomp.sdc.be.components.merge.heat.HeatEnvArtifactsMergeBusinessLogic;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.Operation;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.common.api.ArtifactGroupTypeEnum;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

/**
 * Created by chaya on 9/20/2017.
 */
@org.springframework.stereotype.Component("ComponentInstanceHeatEnvMerge")
public class ComponentInstanceHeatEnvMerge implements ComponentInstanceMergeInterface {

    private static final Logger log = Logger.getLogger(ComponentInstanceHeatEnvMerge.class);

    @Autowired
    private ArtifactsBusinessLogic artifactsBusinessLogic;

    @Autowired
    private HeatEnvArtifactsMergeBusinessLogic heatEnvArtifactsMergeBusinessLogic;

    @Autowired
    protected ComponentsUtils componentsUtils;


    @Override
    public void saveDataBeforeMerge(DataForMergeHolder dataHolder, Component containerComponent, ComponentInstance currentResourceInstance, Component originComponent) {
        dataHolder.setOrigComponentInstanceHeatEnvArtifacts(containerComponent.safeGetComponentInstanceHeatArtifacts(currentResourceInstance.getUniqueId()));
    }

    @Override
    public Component mergeDataAfterCreate(User user, DataForMergeHolder dataHolder, Component updatedContainerComponent, String newInstanceId) {
        List<ArtifactDefinition> origCompInstHeatEnvArtifacts = dataHolder.getOrigComponentInstanceHeatEnvArtifacts();
        List<ArtifactDefinition> newCompInstHeatEnvArtifacts = updatedContainerComponent.safeGetComponentInstanceHeatArtifacts(newInstanceId);
        List<ArtifactDefinition> artifactsToUpdate = heatEnvArtifactsMergeBusinessLogic.mergeInstanceHeatEnvArtifacts(origCompInstHeatEnvArtifacts, newCompInstHeatEnvArtifacts);

        for (ArtifactDefinition artifactInfo : artifactsToUpdate) {
            Map<String, Object> json = artifactsBusinessLogic.buildJsonForUpdateArtifact(artifactInfo, ArtifactGroupTypeEnum.DEPLOYMENT,  null);

            Either<ArtifactDefinition, Operation> uploadArtifactToService = artifactsBusinessLogic.updateResourceInstanceArtifactNoContent(newInstanceId, updatedContainerComponent, user, json,
                    artifactsBusinessLogic.new ArtifactOperationInfo(false, false, ArtifactsBusinessLogic.ArtifactOperationEnum.UPDATE), null);
        }
        return updatedContainerComponent;
    }
}
