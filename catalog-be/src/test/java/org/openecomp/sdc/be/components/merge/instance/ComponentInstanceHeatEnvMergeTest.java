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
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.be.components.impl.ArtifactsBusinessLogic;
import org.openecomp.sdc.be.components.merge.heat.HeatEnvArtifactsMergeBusinessLogic;
import org.openecomp.sdc.be.components.utils.ArtifactBuilder;
import org.openecomp.sdc.be.components.utils.ComponentInstanceBuilder;
import org.openecomp.sdc.be.components.utils.ResourceBuilder;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.common.api.ArtifactGroupTypeEnum;
import org.openecomp.sdc.common.api.ArtifactTypeEnum;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;

public class ComponentInstanceHeatEnvMergeTest {

    @InjectMocks
    private ComponentInstanceHeatEnvMerge testInstance;

    @Mock
    private ArtifactsBusinessLogic artifactsBusinessLogicMock;

    @Mock
    private HeatEnvArtifactsMergeBusinessLogic heatEnvArtifactsMergeBusinessLogicMock;

    @Mock
    private ComponentsUtils componentsUtils;

    private static final User USER = new User();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void mergeDataAfterCreate_mergeAndPersistArtifacts() throws Exception {
        Map<String, ArtifactDefinition> nodeTypeArtifactsByName = buildMapOfHeatArtifacts("artifact1", "artifact2");
        DataForMergeHolder dataHolder = new DataForMergeHolder();
        dataHolder.setOrigComponentDeploymentArtifactsCreatedOnTheInstance(nodeTypeArtifactsByName);
        String instanceId = "instance1";
        Resource resource = buildResourceWithHeatArtifacts(instanceId, "heatArtifact1", "heatArtifact2");
        List<ArtifactDefinition> mergedArtifacts = buildListOfArtifacts("artifact1, heatArtifact1");
        when(heatEnvArtifactsMergeBusinessLogicMock.mergeInstanceHeatEnvArtifacts(dataHolder.getOrigComponentInstanceHeatEnvArtifacts(), resource.safeGetComponentInstanceHeatArtifacts(instanceId)))
                                                   .thenReturn(mergedArtifacts);
        expectMergedArtifactsToBePersisted(mergedArtifacts, instanceId, resource);
        testInstance.mergeDataAfterCreate(USER, dataHolder, resource, instanceId);
    }

    private void expectMergedArtifactsToBePersisted(List<ArtifactDefinition> mergedArtifacts, String instanceId, Resource resource) {
        for (ArtifactDefinition mergedArtifact : mergedArtifacts) {
            Map<String, Object> json = new HashMap<>();
            when(artifactsBusinessLogicMock.buildJsonForUpdateArtifact(mergedArtifact, ArtifactGroupTypeEnum.DEPLOYMENT, null)).thenReturn(json);
            ArtifactsBusinessLogic.ArtifactOperationInfo artifactUpdateOperation = artifactsBusinessLogicMock.new ArtifactOperationInfo(false, false, ArtifactsBusinessLogic.ArtifactOperationEnum.UPDATE);
            when(artifactsBusinessLogicMock.updateResourceInstanceArtifactNoContent(Mockito.eq(instanceId), Mockito.eq(resource),
                                                                                    Mockito.eq(USER), Mockito.eq(json),
                                                                                    Mockito.refEq(artifactUpdateOperation),
                                                                                    isNull()))
                                           .thenReturn(Either.left(new ArtifactDefinition()));
        }
    }

    private Resource buildResourceWithHeatArtifacts(String instanceId, String ... artifacts) {
        ComponentInstanceBuilder componentInstanceBuilder = new ComponentInstanceBuilder().setId(instanceId);
        for (String artifact : artifacts) {
            ArtifactDefinition heatArtifact = new ArtifactBuilder().setType(ArtifactTypeEnum.HEAT_ARTIFACT.getType()).setName(artifact).build();
            componentInstanceBuilder.addDeploymentArtifact(heatArtifact);
        }
        return new ResourceBuilder().addComponentInstance(componentInstanceBuilder.build()).build();
    }

    private Map<String, ArtifactDefinition> buildMapOfHeatArtifacts(String ... artifacts) {
        Map<String, ArtifactDefinition> artifactsByName = new HashMap<>();
        for (String artifact : artifacts) {
            ArtifactDefinition heatArtifact = new ArtifactBuilder().setType(ArtifactTypeEnum.HEAT_ARTIFACT.getType()).setName(artifact).build();
            artifactsByName.put(artifact, heatArtifact);
        }
        return artifactsByName;
    }

    private List<ArtifactDefinition> buildListOfArtifacts(String ... artifacts) {
        return Stream.of(artifacts).map(artifact -> new ArtifactBuilder().setName(artifact).build()).collect(Collectors.toList());
    }

}
