package org.openecomp.sdc.asdctool.impl.validator.tasks.moduleJson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.openecomp.sdc.asdctool.impl.validator.tasks.ServiceValidationTask;
import org.openecomp.sdc.asdctool.impl.validator.utils.ReportManager;
import org.openecomp.sdc.asdctool.impl.validator.utils.VertexResult;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.datatypes.elements.ArtifactDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.MapArtifactDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.MapGroupsDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.GraphPropertyEnum;
import org.openecomp.sdc.be.model.ComponentParametersView;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.jsontitan.datamodel.TopologyTemplate;
import org.openecomp.sdc.be.model.jsontitan.datamodel.ToscaElement;
import org.openecomp.sdc.be.model.jsontitan.operations.TopologyTemplateOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.springframework.beans.factory.annotation.Autowired;

import fj.data.Either;

/**
 * Created by chaya on 7/18/2017.
 */
public class ModuleJsonTask extends ServiceValidationTask {

    @Autowired
    private TopologyTemplateOperation topologyTemplateOperation;

    public ModuleJsonTask() {
        this.name = "Service Module json Validation Task";
    }

    @Override
    public VertexResult validate(GraphVertex vertex) {
        if (!isAfterSubmitForTesting(vertex)) {
            return new VertexResult(true);
        }

        ComponentParametersView paramView = new ComponentParametersView();
        paramView.disableAll();
        paramView.setIgnoreArtifacts(false);
        paramView.setIgnoreGroups(false);
        paramView.setIgnoreComponentInstances(false);
        Either<ToscaElement, StorageOperationStatus> toscaElementEither = topologyTemplateOperation.getToscaElement(vertex.getUniqueId(), paramView);
        if (toscaElementEither.isRight()) {
            return new VertexResult(false);
        }
        TopologyTemplate element = (TopologyTemplate) toscaElementEither.left().value();
        Map<String, MapGroupsDataDefinition> instGroups = element.getInstGroups();
        Map<String, MapArtifactDataDefinition> instDeploymentArtifacts = element.getInstDeploymentArtifacts();

        for (Map.Entry<String, MapGroupsDataDefinition> pair : Optional.ofNullable(instGroups).orElse(Collections.emptyMap()).entrySet()) {
            MapGroupsDataDefinition groups = pair.getValue();
            if (groups != null && !groups.getMapToscaDataDefinition().isEmpty()) {
                return new VertexResult(findCoordinateModuleJson(pair, instDeploymentArtifacts, vertex));
            }
            return new VertexResult(true);
        }
        return new VertexResult(true);
    }

    private boolean findCoordinateModuleJson(Map.Entry<String, MapGroupsDataDefinition> pair, Map<String, MapArtifactDataDefinition> instDeploymentArtifacts, GraphVertex vertex) {
        String groupKey = pair.getKey();
        String[] split = groupKey.split("\\.");
        String instanceName = split[split.length-1];
        MapArtifactDataDefinition deploymentsArtifacts = instDeploymentArtifacts.get(groupKey);
        if (deploymentsArtifacts != null && !deploymentsArtifacts.getMapToscaDataDefinition().isEmpty()) {
            List<ArtifactDataDefinition> moduleJsonArtifacts = deploymentsArtifacts.getMapToscaDataDefinition().values().stream().filter(artifact -> {
                String artifactName = artifact.getArtifactName();
                if (artifactName.startsWith(instanceName) && artifactName.endsWith("modules.json")) {
                    return true;
                }
                return false;
            }).collect(Collectors.toList());
            if (moduleJsonArtifacts.size() > 0) {
                String status = "Instance "+instanceName+" has a corresponding modules.json file: "+moduleJsonArtifacts.get(0).getArtifactName();
                ReportManager.writeReportLineToFile(status);
                return true;
            }
        }
        String status = "Instance "+instanceName+" doesn't have a corresponding modules.json file";
        ReportManager.writeReportLineToFile(status);
        ReportManager.addFailedVertex(getTaskName(), vertex.getUniqueId());
        return false;
    }

    private boolean isAfterSubmitForTesting(GraphVertex vertex){
        List allowedStates = new ArrayList<>(Arrays.asList(LifecycleStateEnum.READY_FOR_CERTIFICATION.name(),
                LifecycleStateEnum.CERTIFICATION_IN_PROGRESS.name(), LifecycleStateEnum.CERTIFIED.name()));
        return allowedStates.contains(vertex.getMetadataProperty(GraphPropertyEnum.STATE));
    }
}
