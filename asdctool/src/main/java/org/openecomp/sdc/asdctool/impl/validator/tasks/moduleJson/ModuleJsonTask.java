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

package org.openecomp.sdc.asdctool.impl.validator.tasks.moduleJson;

import fj.data.Either;
import org.openecomp.sdc.asdctool.impl.validator.report.Report;
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
import org.openecomp.sdc.be.model.jsonjanusgraph.datamodel.TopologyTemplate;
import org.openecomp.sdc.be.model.jsonjanusgraph.datamodel.ToscaElement;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.TopologyTemplateOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class ModuleJsonTask extends ServiceValidationTask {

    private TopologyTemplateOperation topologyTemplateOperation;

    @Autowired
    public ModuleJsonTask(TopologyTemplateOperation topologyTemplateOperation) {
        this.topologyTemplateOperation = topologyTemplateOperation;
        this.name = "Service Module json Validation Task";
    }

    @Override
    public VertexResult validate(Report report, GraphVertex vertex, String outputFilePath) {
        if (!isAfterSubmitForTesting(vertex)) {
            return new VertexResult(true);
        }

        ComponentParametersView paramView = new ComponentParametersView();
        paramView.disableAll();
        paramView.setIgnoreArtifacts(false);
        paramView.setIgnoreGroups(false);
        paramView.setIgnoreComponentInstances(false);
        Either<ToscaElement, StorageOperationStatus> toscaElementEither = topologyTemplateOperation
            .getToscaElement(vertex.getUniqueId(), paramView);
        if (toscaElementEither.isRight()) {
            return new VertexResult(false);
        }
        TopologyTemplate element = (TopologyTemplate) toscaElementEither.left().value();
        Map<String, MapGroupsDataDefinition> instGroups = element.getInstGroups();
        Map<String, MapArtifactDataDefinition> instDeploymentArtifacts = element.getInstDeploymentArtifacts();

        for (Map.Entry<String, MapGroupsDataDefinition> pair : Optional.ofNullable(instGroups)
            .orElse(Collections.emptyMap()).entrySet()) {
            MapGroupsDataDefinition groups = pair.getValue();
            if (groups != null && !groups.getMapToscaDataDefinition().isEmpty()) {
                return new VertexResult(
                    findCoordinateModuleJson(report, pair, instDeploymentArtifacts, vertex, outputFilePath));
            }
            return new VertexResult(true);
        }
        return new VertexResult(true);
    }

    private boolean findCoordinateModuleJson(
        Report report,
        Map.Entry<String, MapGroupsDataDefinition> pair,
        Map<String, MapArtifactDataDefinition> instDeploymentArtifacts,
        GraphVertex vertex, String outputFilePath
    ) {
        String groupKey = pair.getKey();
        String[] split = groupKey.split("\\.");
        String instanceName = split[split.length - 1];
        MapArtifactDataDefinition deploymentsArtifacts = instDeploymentArtifacts.get(groupKey);
        if (deploymentsArtifacts != null && !deploymentsArtifacts.getMapToscaDataDefinition().isEmpty()) {
            List<ArtifactDataDefinition> moduleJsonArtifacts = deploymentsArtifacts.getMapToscaDataDefinition().values()
                .stream().filter(artifact -> {
                    String artifactName = artifact.getArtifactName();
                    if (artifactName.startsWith(instanceName) && artifactName.endsWith("modules.json")) {
                        return true;
                    }
                    return false;
                }).collect(Collectors.toList());
            if (moduleJsonArtifacts.size() > 0) {
                String status =
                    "Instance " + instanceName + " has a corresponding modules.json file: " + moduleJsonArtifacts.get(0)
                        .getArtifactName();
                ReportManager.writeReportLineToFile(status, outputFilePath);
                return true;
            }
        }
        String status = "Instance " + instanceName + " doesn't have a corresponding modules.json file";
        ReportManager.writeReportLineToFile(status, outputFilePath);
        report.addFailure(getTaskName(), vertex.getUniqueId());
        return false;
    }

    private boolean isAfterSubmitForTesting(GraphVertex vertex) {
        List<String> allowedStates = new ArrayList<>(Arrays.asList(LifecycleStateEnum.CERTIFIED.name()));
        return allowedStates.contains(vertex.getMetadataProperty(GraphPropertyEnum.STATE));
    }
}
