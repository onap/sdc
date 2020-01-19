/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (c) 2019 Samsung
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

package org.openecomp.sdc.asdctool.impl.validator.tasks.artifacts;

import fj.data.Either;
import org.openecomp.sdc.asdctool.impl.validator.utils.ReportManager;
import org.openecomp.sdc.be.dao.cassandra.ArtifactCassandraDao;
import org.openecomp.sdc.be.dao.cassandra.CassandraOperationStatus;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.datatypes.elements.ArtifactDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.MapArtifactDataDefinition;
import org.openecomp.sdc.be.model.ComponentParametersView;
import org.openecomp.sdc.be.model.jsonjanusgraph.datamodel.TopologyTemplate;
import org.openecomp.sdc.be.model.jsonjanusgraph.datamodel.ToscaElement;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.TopologyTemplateOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Created by chaya on 7/6/2017.
 */
public class ArtifactValidationUtils {

    private static final Logger logger = Logger.getLogger(ArtifactValidationUtils.class);

    private ArtifactCassandraDao artifactCassandraDao;

    private TopologyTemplateOperation topologyTemplateOperation;

    @Autowired
    public ArtifactValidationUtils(ArtifactCassandraDao artifactCassandraDao,
        TopologyTemplateOperation topologyTemplateOperation) {
        this.artifactCassandraDao = artifactCassandraDao;
        this.topologyTemplateOperation = topologyTemplateOperation;
    }

    public ArtifactsVertexResult validateArtifactsAreInCassandra(GraphVertex vertex, String taskName, List<ArtifactDataDefinition> artifacts) {
        ArtifactsVertexResult result = new ArtifactsVertexResult(true);
        for(ArtifactDataDefinition artifact:artifacts) {
            boolean isArtifactExist = isArtifactInCassandra(artifact.getEsId());
            String status = isArtifactExist ? "Artifact " + artifact.getEsId() + " is in Cassandra" :
                    "Artifact " + artifact.getEsId() + " doesn't exist in Cassandra";
            ReportManager.writeReportLineToFile(status);
            if (!isArtifactExist) {
                ReportManager.addFailedVertex(taskName, vertex.getUniqueId());
                result.setStatus(false);
                result.addNotFoundArtifact(artifact.getUniqueId());
            }
        }
        return result;
    }

    public boolean isArtifactInCassandra(String uniqueId) {
        Either<Long, CassandraOperationStatus> countOfArtifactsEither =
            artifactCassandraDao.getCountOfArtifactById(uniqueId);
        if (countOfArtifactsEither.isRight()) {
            logger.debug("Failed to retrieve artifact with id: {} from Cassandra", uniqueId);
            return false;
        }
        Long count = countOfArtifactsEither.left().value();
        return count >= 1;
    }

    public List<ArtifactDataDefinition> addRelevantArtifacts(Map<String, ArtifactDataDefinition> artifactsMap) {
        List<ArtifactDataDefinition> artifacts = new ArrayList<>();
        Optional.ofNullable(artifactsMap).orElse(Collections.emptyMap()).forEach((key, dataDef) -> {
            if (dataDef.getEsId() != null && !dataDef.getEsId().isEmpty()) {
                artifacts.add(dataDef);
            }
        });
        return artifacts;
    }

    public ArtifactsVertexResult validateTopologyTemplateArtifacts(GraphVertex vertex, String taskName) {
        ArtifactsVertexResult result = new ArtifactsVertexResult();
        ComponentParametersView paramView = new ComponentParametersView();
        paramView.disableAll();
        paramView.setIgnoreArtifacts(false);
        paramView.setIgnoreComponentInstances(false);
        Either<ToscaElement, StorageOperationStatus> toscaElementEither = topologyTemplateOperation.getToscaElement(vertex.getUniqueId(), paramView);
        if (toscaElementEither.isRight()) {
            result.setStatus(false);
            return result;
        }
        TopologyTemplate element = (TopologyTemplate) toscaElementEither.left().value();
        Map<String, ArtifactDataDefinition> deploymentArtifacts = element.getDeploymentArtifacts();
        Map<String, ArtifactDataDefinition> artifacts = element.getArtifacts();
        Map<String, ArtifactDataDefinition> apiArtifacts = element.getServiceApiArtifacts();
        Map<String, MapArtifactDataDefinition> instanceArtifacts = element.getInstanceArtifacts();
        Map<String, MapArtifactDataDefinition> instanceDeploymentArtifacts = element.getInstDeploymentArtifacts();

        List<ArtifactDataDefinition> allArtifacts = new ArrayList<>();

        allArtifacts.addAll(addRelevantArtifacts(deploymentArtifacts));
        allArtifacts.addAll(addRelevantArtifacts(artifacts));
        allArtifacts.addAll(addRelevantArtifacts(apiArtifacts));

        if (instanceArtifacts != null) {
            instanceArtifacts.forEach((key, artifactMap) ->
                    allArtifacts.addAll(addRelevantArtifacts(artifactMap.getMapToscaDataDefinition())));
        }

        if (instanceDeploymentArtifacts != null) {
            instanceDeploymentArtifacts.forEach((key, artifactMap) ->
                    allArtifacts.addAll(addRelevantArtifacts(artifactMap.getMapToscaDataDefinition())));
        }

        return validateArtifactsAreInCassandra(vertex, taskName, allArtifacts);
    }
}
