package org.openecomp.sdc.be.components.merge.instance;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.openecomp.sdc.be.components.impl.ArtifactsBusinessLogic;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.Operation;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.jsontitan.operations.ToscaOperationFacade;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.exception.ResponseFormat;
import org.springframework.beans.factory.annotation.Autowired;

import fj.data.Either;

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
                .collect(Collectors.toMap(p -> p.getKey(), p -> p.getValue()));

        dataHolder.setOrigComponentDeploymentArtifactsCreatedOnTheInstance(deploymentArtifactsCreatedOnTheInstance);

        Map<String, ArtifactDefinition> componentInstancesInformationalArtifacts = currentResourceInstance.safeGetArtifacts();
        Map<String, ArtifactDefinition> originalComponentInformationalArtifacts = originComponent.getArtifacts();
        Map<String, ArtifactDefinition> informationalArtifactsCreatedOnTheInstance = componentInstancesInformationalArtifacts.entrySet()
                .stream()
                .filter(i -> !originalComponentInformationalArtifacts.containsKey(i.getKey()))
                .collect(Collectors.toMap(p -> p.getKey(), p -> p.getValue()));
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
                .collect(Collectors.toMap(p -> p.getKey(), q -> q.getValue()));
        Map<String, ArtifactDefinition> origInstanceInformationalArtifactsCreatedOnTheInstance = dataHolder.getOrigComponentInformationalArtifactsCreatedOnTheInstance();
        Map<String, ArtifactDefinition> currentInstanceInformationalArtifacts = updatedContainerComponent.safeGetComponentInstanceInformationalArtifacts(newInstanceId);
        Map<String, ArtifactDefinition> filteredInformationalArtifactsToAdd = Optional.ofNullable(origInstanceInformationalArtifactsCreatedOnTheInstance).orElse(new HashMap<>()).entrySet().stream()
                .filter(artifact -> noArtifactWithTheSameLabel(artifact.getValue().getArtifactLabel(), currentInstanceInformationalArtifacts))
                .collect(Collectors.toMap(p -> p.getKey(), q -> q.getValue()));
        Map<String, ArtifactDefinition> allFilteredArtifactsToAdd = new HashMap<>();
        allFilteredArtifactsToAdd.putAll(filteredDeploymentArtifactsToAdd);
        allFilteredArtifactsToAdd.putAll(filteredInformationalArtifactsToAdd);

        for (Map.Entry<String, ArtifactDefinition> currentArtifactDefinition :  allFilteredArtifactsToAdd.entrySet()) {
            Map<String, Object> jsonForUpdateArtifact = artifactsBusinessLogic.buildJsonForUpdateArtifact(
                    currentArtifactDefinition.getValue().getUniqueId(), currentArtifactDefinition.getValue().getArtifactName(),
                    currentArtifactDefinition.getValue().getArtifactType(), currentArtifactDefinition.getValue().getArtifactGroupType(),
                    currentArtifactDefinition.getValue().getArtifactLabel(), currentArtifactDefinition.getValue().getArtifactDisplayName(),
                    currentArtifactDefinition.getValue().getDescription(), currentArtifactDefinition.getValue().getPayloadData(),
                    null, currentArtifactDefinition.getValue().getListHeatParameters());
            addEsIdToArtifactJson(jsonForUpdateArtifact, currentArtifactDefinition.getValue().getEsId());
            Either<Either<ArtifactDefinition, Operation>, ResponseFormat> uploadArtifactToService =
                    artifactsBusinessLogic.updateResourceInstanceArtifactNoContent(newInstanceId, updatedContainerComponent,
                            user, jsonForUpdateArtifact, artifactsBusinessLogic.new ArtifactOperationInfo(
                                    false, false, ArtifactsBusinessLogic.ArtifactOperationEnum.Link), currentArtifactDefinition.getValue());
            if (uploadArtifactToService.isRight()) {
                return Either.right(uploadArtifactToService.right().value());
            }
            toscaOperationFacade.commit();
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
