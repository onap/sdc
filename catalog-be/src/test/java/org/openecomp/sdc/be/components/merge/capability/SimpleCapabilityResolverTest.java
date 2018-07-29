package org.openecomp.sdc.be.components.merge.capability;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.sdc.be.components.merge.utils.MergeInstanceUtils;
import org.openecomp.sdc.be.components.utils.CapabilityDefinitionBuilder;
import org.openecomp.sdc.be.components.utils.ComponentInstanceBuilder;
import org.openecomp.sdc.be.components.utils.ResourceBuilder;
import org.openecomp.sdc.be.model.CapabilityDefinition;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.Resource;

import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SimpleCapabilityResolverTest {

    public static final String NEW_OWNER_ID1 = "owner1";
    public static final String NEW_OWNER_ID2 = "owner2";
    public static final String OLD_OWNER_ID1 = "oldOwner1";
    private static final String OLD_OWNER_ID2 = "oldOwner2";
    public static final String OLD_OWNER_ID3 = "oldOwner3";
    @InjectMocks
    private SimpleCapabilityResolver testInstance;
    @Mock
    private MergeInstanceUtils MergeInstanceUtils;

    private CapabilityDefinition capName1Owner1Type1;
    private CapabilityDefinition capName1Owner1Type2;
    private CapabilityDefinition capName1Owner2Type1;
    private CapabilityDefinition capName2Owner1Type2;

    private CapabilityDefinition prevCapName1Owner1Type1;
    private CapabilityDefinition prevCapName1Owner1Type2;
    private CapabilityDefinition prevCapName1Owner2Type1;
    private CapabilityDefinition prevCapName2Owner1Type2;
    private CapabilityDefinition prevCapName1Owner3Type1;
    private CapabilityDefinition prevCapName3Owner1Type1;
    private CapabilityDefinition prevCapName1Owner1Type3;
    CapabilityDefinition[] prevCapabilities;
    private ComponentInstance oldInstance;
    private ComponentInstance currInstance;


    @Before
    public void setUp() {
        capName1Owner1Type1 = new CapabilityDefinitionBuilder().setName("name1").setOwnerId(NEW_OWNER_ID1).setType("type1").build();
        prevCapName1Owner1Type1 = new CapabilityDefinition(capName1Owner1Type1);
        prevCapName1Owner1Type1.setOwnerId(OLD_OWNER_ID1);

        capName1Owner1Type2 = new CapabilityDefinitionBuilder().setName("name1").setOwnerId(NEW_OWNER_ID1).setType("type2").build();
        prevCapName1Owner1Type2 = new CapabilityDefinition(capName1Owner1Type2);
        prevCapName1Owner1Type2.setOwnerId(OLD_OWNER_ID1);

        capName1Owner2Type1 = new CapabilityDefinitionBuilder().setName("name1").setOwnerId(NEW_OWNER_ID2).setType("type1").build();
        prevCapName1Owner2Type1 = new CapabilityDefinition(capName1Owner2Type1);
        prevCapName1Owner2Type1.setOwnerId(OLD_OWNER_ID2);

        capName2Owner1Type2 = new CapabilityDefinitionBuilder().setName("name2").setOwnerId(NEW_OWNER_ID1).setType("type2").build();
        prevCapName2Owner1Type2 = new CapabilityDefinition(capName2Owner1Type2);
        prevCapName2Owner1Type2.setOwnerId(OLD_OWNER_ID1);

        //prev capabilities that are not mapped to any new capability
        prevCapName1Owner3Type1 = new CapabilityDefinitionBuilder().setName("name1").setOwnerId(OLD_OWNER_ID3).setType("type1").build();
        prevCapName3Owner1Type1 = new CapabilityDefinitionBuilder().setName("name3").setOwnerId(OLD_OWNER_ID1).setType("type1").build();
        prevCapName1Owner1Type3 = new CapabilityDefinitionBuilder().setName("name1").setOwnerId(OLD_OWNER_ID1).setType("type3").build();

        currInstance = new ComponentInstanceBuilder().setId("inst1").addCapabilities(capName1Owner1Type1, capName1Owner1Type2, capName1Owner2Type1, capName2Owner1Type2).build();
        prevCapabilities = new CapabilityDefinition[]{prevCapName1Owner1Type1, prevCapName1Owner1Type2, prevCapName1Owner2Type1, prevCapName2Owner1Type2, prevCapName1Owner3Type1, prevCapName3Owner1Type1, prevCapName1Owner1Type3};
        oldInstance = new ComponentInstanceBuilder().setId("inst1").addCapabilities(prevCapabilities).build();
    }

    @Test
    public void resolvePrevCapIdToNewCapability_resolveByTypeNameAndNewOwnerId() {
        when(MergeInstanceUtils.mapOldToNewCapabilitiesOwnerIds(oldInstance, currInstance)).thenReturn(ImmutableMap.of(OLD_OWNER_ID1, NEW_OWNER_ID1, OLD_OWNER_ID2, NEW_OWNER_ID2));
        Map<CapabilityDefinition, CapabilityDefinition> oldToNewMap = testInstance.resolvePrevCapIdToNewCapability(oldInstance, currInstance);
        assertThat(oldToNewMap).hasSize(4);
        assertThat(oldToNewMap).containsEntry(prevCapName1Owner1Type1, capName1Owner1Type1);
        assertThat(oldToNewMap).containsEntry(prevCapName1Owner1Type2, capName1Owner1Type2);
        assertThat(oldToNewMap).containsEntry(prevCapName1Owner2Type1, capName1Owner2Type1);
        assertThat(oldToNewMap).containsEntry(prevCapName2Owner1Type2, capName2Owner1Type2);
    }

    @Test
    public void resolvePrevCapIdToNewCapability_noMatchingNewOwnerIds() {
        when(MergeInstanceUtils.mapOldToNewCapabilitiesOwnerIds(oldInstance, currInstance)).thenReturn(emptyMap());
        Map<CapabilityDefinition, CapabilityDefinition> oldToNewMap = testInstance.resolvePrevCapIdToNewCapability(oldInstance, currInstance);
        assertThat(oldToNewMap).isEmpty();
    }

    @Test
    public void mapOldToNewInstanceCapabilitiesOwnerIds() {
        Resource container = new ResourceBuilder().addComponentInstance(currInstance).build();
        Resource prevInstanceOrigNode = new Resource();
        when(MergeInstanceUtils.mapOldToNewCapabilitiesOwnerIds(container, prevInstanceOrigNode, "inst1", asList(OLD_OWNER_ID1, OLD_OWNER_ID2, OLD_OWNER_ID3))).thenReturn(ImmutableMap.of(OLD_OWNER_ID1, NEW_OWNER_ID1, OLD_OWNER_ID2, NEW_OWNER_ID2));
        Map<CapabilityDefinition, CapabilityDefinition> oldToNewMap = testInstance.resolvePrevCapToNewCapability(container, prevInstanceOrigNode, "inst1", asList(prevCapabilities));
        assertThat(oldToNewMap).hasSize(4);
        assertThat(oldToNewMap).containsEntry(prevCapName1Owner1Type1, capName1Owner1Type1);
        assertThat(oldToNewMap).containsEntry(prevCapName1Owner1Type2, capName1Owner1Type2);
        assertThat(oldToNewMap).containsEntry(prevCapName1Owner2Type1, capName1Owner2Type1);
        assertThat(oldToNewMap).containsEntry(prevCapName2Owner1Type2, capName2Owner1Type2);
    }
}