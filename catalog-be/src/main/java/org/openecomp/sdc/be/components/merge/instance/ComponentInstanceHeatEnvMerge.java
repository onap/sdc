package org.openecomp.sdc.be.components.merge.instance;

import java.util.List;
import java.util.Map;

import org.openecomp.sdc.be.components.impl.ArtifactsBusinessLogic;
import org.openecomp.sdc.be.components.merge.heat.HeatEnvArtifactsMergeBusinessLogic;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.Operation;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.common.api.ArtifactGroupTypeEnum;
import org.openecomp.sdc.exception.ResponseFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import fj.data.Either;

/**
 * Created by chaya on 9/20/2017.
 */
@org.springframework.stereotype.Component("ComponentInstanceHeatEnvMerge")
public class ComponentInstanceHeatEnvMerge implements ComponentInstanceMergeInterface {

    private static Logger log = LoggerFactory.getLogger(ComponentInstanceHeatEnvMerge.class.getName());

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
    public Either<Component, ResponseFormat> mergeDataAfterCreate(User user, DataForMergeHolder dataHolder, Component updatedContainerComponent, String newInstanceId) {
        List<ArtifactDefinition> origCompInstHeatEnvArtifacts = dataHolder.getOrigComponentInstanceHeatEnvArtifacts();
        List<ArtifactDefinition> newCompInstHeatEnvArtifacts = updatedContainerComponent.safeGetComponentInstanceHeatArtifacts(newInstanceId);
        List<ArtifactDefinition> artifactsToUpdate = heatEnvArtifactsMergeBusinessLogic.mergeInstanceHeatEnvArtifacts(origCompInstHeatEnvArtifacts, newCompInstHeatEnvArtifacts);

        for (ArtifactDefinition artifactInfo : artifactsToUpdate) {
            Map<String, Object> json = artifactsBusinessLogic.buildJsonForUpdateArtifact(artifactInfo, ArtifactGroupTypeEnum.DEPLOYMENT,  null);

            Either<Either<ArtifactDefinition, Operation>, ResponseFormat> uploadArtifactToService = artifactsBusinessLogic.updateResourceInstanceArtifactNoContent(newInstanceId, updatedContainerComponent, user, json,
                    artifactsBusinessLogic.new ArtifactOperationInfo(false, false, ArtifactsBusinessLogic.ArtifactOperationEnum.Update), null);
            if (uploadArtifactToService.isRight()) {
                log.error("Failed to update artifact {} on resourceInstance {}", artifactInfo.getArtifactLabel(), newInstanceId);
                return Either.right(uploadArtifactToService.right().value());
            }
        }
        return Either.left(updatedContainerComponent);
    }
}
