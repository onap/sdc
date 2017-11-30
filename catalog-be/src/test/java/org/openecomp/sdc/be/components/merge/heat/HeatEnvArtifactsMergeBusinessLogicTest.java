package org.openecomp.sdc.be.components.merge.heat;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.openecomp.sdc.be.components.utils.ArtifactBuilder;
import org.openecomp.sdc.be.components.utils.HeatParameterBuilder;
import org.openecomp.sdc.be.datatypes.elements.HeatParameterDataDefinition;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.HeatParameterDefinition;

public class HeatEnvArtifactsMergeBusinessLogicTest {

    private HeatEnvArtifactsMergeBusinessLogic testInstance;

    @Before
    public void setUp() throws Exception {
        testInstance = new HeatEnvArtifactsMergeBusinessLogic();
    }

    @Test
    public void mergeHeatEnvParameter_diffArtifactLabel_noMerging() throws Exception {
        HeatParameterDefinition newHeatParam1 = buildHeatParameterDefinition("param1", "type1", "param1NewValue");
        HeatParameterDefinition prevHeatParam1 = buildHeatParameterDefinition("param1", "type1", "param1PrevValue");

        ArtifactDefinition oldArtifact = buildHeatArtifact("artifact1", prevHeatParam1);
        ArtifactDefinition newArtifact = buildHeatArtifact("artifact2", newHeatParam1);
        List<ArtifactDefinition> mergedArtifacts = testInstance.mergeInstanceHeatEnvArtifacts(Collections.singletonList(oldArtifact), Collections.singletonList(newArtifact));
        assertEquals(0, mergedArtifacts.size());
        assertEquals("param1NewValue", newArtifact.getHeatParameters().get(0).getCurrentValue());
    }

    @Test
    public void mergeHeatEnvParameter_diffHeatParamName_noMerge() throws Exception {
        HeatParameterDefinition newHeatParam1 = buildHeatParameterDefinition("param1", "type1", "param1NewValue");
        HeatParameterDefinition prevHeatParam1 = buildHeatParameterDefinition("param2", "type1", "param1PrevValue");
        ArtifactDefinition oldArtifact = buildHeatArtifact("artifact1", prevHeatParam1);
        ArtifactDefinition newArtifact = buildHeatArtifact("artifact1", newHeatParam1);
        List<ArtifactDefinition> mergedArtifacts = testInstance.mergeInstanceHeatEnvArtifacts(Collections.singletonList(oldArtifact), Collections.singletonList(newArtifact));
        assertEquals(0, mergedArtifacts.size());
        assertEquals("param1NewValue", newArtifact.getHeatParameters().get(0).getCurrentValue());
    }

    @Test
    public void mergeHeatEnvParameter_diffHeatParamType_noMerge() throws Exception {
        HeatParameterDefinition newHeatParam1 = buildHeatParameterDefinition("param1", "type1", "param1NewValue");
        HeatParameterDefinition prevHeatParam1 = buildHeatParameterDefinition("param1", "type2", "param1PrevValue");
        ArtifactDefinition oldArtifact = buildHeatArtifact("artifact1", prevHeatParam1);
        ArtifactDefinition newArtifact = buildHeatArtifact("artifact1", newHeatParam1);
        List<ArtifactDefinition> mergedArtifacts = testInstance.mergeInstanceHeatEnvArtifacts(Collections.singletonList(oldArtifact), Collections.singletonList(newArtifact));
        assertEquals(0, mergedArtifacts.size());
        assertEquals("param1NewValue", newArtifact.getHeatParameters().get(0).getCurrentValue());
    }

    @Test
    public void mergeHeatEnvParameter__diffOldAndNewValues_overrideNewValueWithOldValue() throws Exception {
        HeatParameterDefinition newHeatParam1 = buildHeatParameterDefinition("param1", "type1", null);
        HeatParameterDefinition newHeatParam2 = buildHeatParameterDefinition("param2", "type1", "param2value");

        HeatParameterDefinition prevHeatParam1 = buildHeatParameterDefinition("param1", "type1", "param1PrevValue");
        HeatParameterDefinition prevHeatParam2 = buildHeatParameterDefinition("param2", "type1", "param2PrevValue");

        ArtifactDefinition oldArtifact = buildHeatArtifact("artifact1", prevHeatParam1, prevHeatParam2);
        ArtifactDefinition newArtifact = buildHeatArtifact("artifact1", newHeatParam1, newHeatParam2);

        List<ArtifactDefinition> mergedArtifacts = testInstance.mergeInstanceHeatEnvArtifacts(Collections.singletonList(oldArtifact), Collections.singletonList(newArtifact));
        assertEquals(1, mergedArtifacts.size());
        List<HeatParameterDataDefinition> heatParameters = mergedArtifacts.get(0).getHeatParameters();
        assertEquals("param1PrevValue", heatParameters.get(0).getCurrentValue());
        assertEquals("param2PrevValue", heatParameters.get(1).getCurrentValue());
    }

    @Test
    public void mergeHeatEnvParameter_multipleArtifacts() throws Exception {
        HeatParameterDefinition newHeatParam1 = buildHeatParameterDefinition("param1", "type1", "param1Newvalue");
        HeatParameterDefinition newHeatParam2 = buildHeatParameterDefinition("param2", "type1", "param2Newvalue");
        HeatParameterDefinition newHeatParam3 = buildHeatParameterDefinition("param2", "type1", "param3Newvalue");

        HeatParameterDefinition prevHeatParam1 = buildHeatParameterDefinition("param1", "type1", "param1PrevValue");
        HeatParameterDefinition prevHeatParam2 = buildHeatParameterDefinition("param2", "type1", "param2PrevValue");
        HeatParameterDefinition prevHeatParam3 = buildHeatParameterDefinition("param3", "type2", "param3PrevValue");

        ArtifactDefinition oldArtifact1 = buildHeatArtifact("artifact1", prevHeatParam1);
        ArtifactDefinition oldArtifact2 = buildHeatArtifact("artifact2", prevHeatParam2);
        ArtifactDefinition oldArtifact3 = buildHeatArtifact("artifact3", prevHeatParam3);

        ArtifactDefinition newArtifact1 = buildHeatArtifact("artifact1", newHeatParam1);
        ArtifactDefinition newArtifact2 = buildHeatArtifact("artifact2New", newHeatParam2);
        ArtifactDefinition newArtifact3 = buildHeatArtifact("artifact3", newHeatParam3);

        List<ArtifactDefinition> mergedArtifacts = testInstance.mergeInstanceHeatEnvArtifacts(Arrays.asList(oldArtifact1, oldArtifact2, oldArtifact3), Arrays.asList(newArtifact1, newArtifact2, newArtifact3));
        assertEquals(1, mergedArtifacts.size());//artifact 2 not merged as it has different label, artifact 3 not merged as the heat parameter has diff types
        assertEquals("artifact1", mergedArtifacts.get(0).getArtifactLabel());
    }

    private HeatParameterDefinition buildHeatParameterDefinition(String name, String type, String val) {
        return new HeatParameterBuilder().setName(name).setType(type).setCurrentValue(val).build();
    }

    private ArtifactDefinition buildHeatArtifact(String label, HeatParameterDefinition ... heatParameterDefinitions) {
        ArtifactBuilder artifactBuilder = new ArtifactBuilder().setLabel(label);
        Stream.of(heatParameterDefinitions).forEach(artifactBuilder::addHeatParam);
        return artifactBuilder.build();
    }

}