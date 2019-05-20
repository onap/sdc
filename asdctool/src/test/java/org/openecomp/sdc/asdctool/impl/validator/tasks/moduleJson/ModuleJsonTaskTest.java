package org.openecomp.sdc.asdctool.impl.validator.tasks.moduleJson;

import fj.data.Either;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
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
        when(topologyTemplateOperation.getToscaElement(ArgumentMatchers.eq(vertex.getUniqueId()), ArgumentMatchers.any(ComponentParametersView.class))).thenReturn(Either.left(topologyTemplate));
        try {
            test.validate(vertex);
        } catch (Exception e) {

        }
    }
}