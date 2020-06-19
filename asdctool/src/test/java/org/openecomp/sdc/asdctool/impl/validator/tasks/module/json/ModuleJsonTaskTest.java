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

package org.openecomp.sdc.asdctool.impl.validator.tasks.module.json;

import fj.data.Either;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.sdc.asdctool.impl.validator.report.Report;
import org.openecomp.sdc.asdctool.impl.validator.utils.VertexResult;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.datatypes.elements.ArtifactDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.GroupInstanceDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.MapArtifactDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.MapGroupsDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.GraphPropertyEnum;
import org.openecomp.sdc.be.model.ComponentParametersView;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.jsonjanusgraph.datamodel.TopologyTemplate;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.TopologyTemplateOperation;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ModuleJsonTaskTest {

    @InjectMocks
    private ModuleJsonTask test;
    @Mock
    private TopologyTemplateOperation topologyTemplateOperation;

    @Test
    public void testValidate() {
        GraphVertex vertex = new GraphVertex();
        vertex.setUniqueId("uniqueId");
        Map<GraphPropertyEnum, Object> hasProps1 = new HashMap<>();
        hasProps1.put(GraphPropertyEnum.STATE, LifecycleStateEnum.CERTIFIED.name());
        vertex.setMetadataProperties(hasProps1);

        Map<String, ArtifactDataDefinition> mapDataDefinition = new HashMap<>();
        ArtifactDataDefinition artifactDataDefinition = new ArtifactDataDefinition();
        artifactDataDefinition.setArtifactName("one_modules.json");
        mapDataDefinition.put("one", artifactDataDefinition);
        MapGroupsDataDefinition mapGroupsDataDefinition = new MapGroupsDataDefinition();
        Map<String, GroupInstanceDataDefinition> mapToscaDataDefinition = new HashMap<>();
        mapToscaDataDefinition.put("one", new GroupInstanceDataDefinition());
        mapGroupsDataDefinition.setMapToscaDataDefinition(mapToscaDataDefinition);

        Map<String, MapGroupsDataDefinition> instGroups = new HashMap<>();
        instGroups.put("one", mapGroupsDataDefinition);

        Map<String, MapArtifactDataDefinition> instDeploymentArtifacts = new HashMap<>();
        MapArtifactDataDefinition mapArtifactDataDefinition = new MapArtifactDataDefinition();

        mapArtifactDataDefinition.setMapToscaDataDefinition(mapDataDefinition);
        instDeploymentArtifacts.put("one", mapArtifactDataDefinition);

        TopologyTemplate topologyTemplate = new TopologyTemplate();
        topologyTemplate.setInstGroups(instGroups);
        topologyTemplate.setInstDeploymentArtifacts(instDeploymentArtifacts);
        when(topologyTemplateOperation.getToscaElement(ArgumentMatchers.eq(vertex.getUniqueId()),
            ArgumentMatchers.any(ComponentParametersView.class))).thenReturn(Either.left(topologyTemplate));

        // Initially no outputFilePath was passed to this function (hence it is set to null)
        // TODO: Fix this null and see if the argument is used by this function
        try {
            Report report = Report.make();
            VertexResult actual = test.validate(report, vertex, null);
            assertThat(actual.getStatus(), is(true));
        } catch (Exception e) {
            // TODO: Fix this test, as currently, any exception is ignored
            // This will be addressed in another change
        }
    }
}
