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
import org.openecomp.sdc.be.model.*;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.exception.ResponseFormat;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by chaya on 9/20/2017.
 */
@org.springframework.stereotype.Component("ComponentInstanceArtifactsMerge")
public class ComponentInstanceArtifactsMerge implements ComponentInstanceMergeInterface {

    @Autowired
    ToscaOperationFacade toscaOperationFacade;

    @Autowired
    ArtifactsBusinessLogic artifactsBusinessLogic;

    @Override
    public void saveDataBeforeMerge(DataForMergeHolder dataHolder, Component containerComponent, ComponentInstance currentResourceInstance, Component originComponent) {
        Map<String, ArtifactDefinition> componentInstancesDeploymentArtifacts = currentResourceInstance.safeGetDeploymentArtifacts();
        Map<String, ArtifactDefinition> originalComponentDeploymentArtifacts = originComponent.getDeploymentArtifacts();
        Map<String, ArtifactDefinition> deploymentArtifactsCreatedOnTheInstance = componentInstancesDeploymentArtifacts.entrySet()
                .stream()
                .filter(i -> !originalComponentDeploymentArtifacts.containsKey(i.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        dataHolder.setOrigComponentDeploymentArtifactsCreatedOnTheInstance(deploymentArtifactsCreatedOnTheInstance);

        Map<String, ArtifactDefinition> componentInstancesInformationalArtifacts = currentResourceInstance.safeGetArtifacts();
        Map<String, ArtifactDefinition> originalComponentInformationalArtifacts = originComponent.getArtifacts();
        Map<String, ArtifactDefinition> informationalArtifactsCreatedOnTheInstance = componentInstancesInformationalArtifacts.entrySet()
                .stream()
                .filter(i -> !originalComponentInformationalArtifacts.containsKey(i.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        dataHolder.setOrigComponentInformationalArtifactsCreatedOnTheInstance(informationalArtifactsCreatedOnTheInstance);
    }

    private void addEsIdToArtifactJson(Map<String, Object> artifactJson, String origEsId) {
        artifactJson.put(Constants.ARTIFACT_ES_ID, origEsId);
    }

    @Override
    public Either<Component, ResponseFormat> mergeDataAfterCreate(User user, DataForMergeHolder dataHolder, Component updatedContainerComponent, String newInstanceId) {
        Map<String, ArtifactDefinition> origInstanceDeploymentArtifactsCreatedOnTheInstance = dataHolder.getOrigComponentDeploymentArtifactsCreatedOnTheInstance();
        Map<String, ArtifactDefinition> currentInstanceDeploymentArtifacts = updatedContainerComponent.safeGetComponentInstanceDeploymentArtifacts(newInstanceId);
        Map<String, ArtifactDefinition> filteredDeploymentArtifactsToAdd = Optional.ofNullable(origInstanceDeploymentArtifactsCreatedOnTheInstance).orElse(new HashMap<>()).entrySet().stream()
                .filter(artifact -> noArtifactWithTheSameLabel(artifact.getValue().getArtifactLabel(), currentInstanceDeploymentArtifacts))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        Map<String, ArtifactDefinition> origInstanceInformationalArtifactsCreatedOnTheInstance = dataHolder.getOrigComponentInformationalArtifactsCreatedOnTheInstance();
        Map<String, ArtifactDefinition> currentInstanceInformationalArtifacts = updatedContainerComponent.safeGetComponentInstanceInformationalArtifacts(newInstanceId);
        Map<String, ArtifactDefinition> filteredInformationalArtifactsToAdd = Optional.ofNullable(origInstanceInformationalArtifactsCreatedOnTheInstance).orElse(new HashMap<>()).entrySet().stream()
                .filter(artifact -> noArtifactWithTheSameLabel(artifact.getValue().getArtifactLabel(), currentInstanceInformationalArtifacts))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        Map<String, ArtifactDefinition> allFilteredArtifactsToAdd = new HashMap<>();
        allFilteredArtifactsToAdd.putAll(filteredDeploymentArtifactsToAdd);
        allFilteredArtifactsToAdd.putAll(filteredInformationalArtifactsToAdd);

        for (Map.Entry<String, ArtifactDefinition> currentArtifactDefinition :  allFilteredArtifactsToAdd.entrySet()) {
            Map<String, Object> jsonForUpdateArtifact = artifactsBusinessLogic.buildJsonForUpdateArtifact(
                    currentArtifactDefinition.getValue().getUniqueId(), 
                    currentArtifactDefinition.getValue().getArtifactName(),
                    currentArtifactDefinition.getValue().getArtifactType(), 
                    currentArtifactDefinition.getValue().getArtifactGroupType(),
                    currentArtifactDefinition.getValue().getArtifactLabel(), 
                    currentArtifactDefinition.getValue().getArtifactDisplayName(),
                    currentArtifactDefinition.getValue().getDescription(), 
                    currentArtifactDefinition.getValue().getPayloadData(),
                    null, currentArtifactDefinition.getValue().getListHeatParameters());
            addEsIdToArtifactJson(jsonForUpdateArtifact, currentArtifactDefinition.getValue().getEsId());
            Either<Either<ArtifactDefinition, Operation>, ResponseFormat> uploadArtifactToService =
                    artifactsBusinessLogic.updateResourceInstanceArtifactNoContent(newInstanceId, updatedContainerComponent,
                            user, jsonForUpdateArtifact, artifactsBusinessLogic.new ArtifactOperationInfo(
                                    false, false, ArtifactsBusinessLogic.ArtifactOperationEnum.LINK), currentArtifactDefinition.getValue());
            if (uploadArtifactToService.isRight()) {
                return Either.right(uploadArtifactToService.right().value());
            }
        }
        return Either.left(updatedContainerComponent);
    }

    private boolean noArtifactWithTheSameLabel(String artifactLabel, Map<String, ArtifactDefinition> currDeploymentArtifacts) {
        for (Map.Entry<String, ArtifactDefinition> artifact : currDeploymentArtifacts.entrySet()) {
            if (artifact.getValue().getArtifactLabel().equals(artifactLabel)) {
                return false;
            }
        }
        return true;
    }
}
