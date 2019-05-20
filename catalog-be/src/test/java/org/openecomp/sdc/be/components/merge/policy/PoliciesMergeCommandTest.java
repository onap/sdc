package org.openecomp.sdc.be.components.merge.policy;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.sdc.be.components.utils.ComponentInstanceBuilder;
import org.openecomp.sdc.be.components.utils.GroupDefinitionBuilder;
import org.openecomp.sdc.be.components.utils.PolicyDefinitionBuilder;
import org.openecomp.sdc.be.components.utils.ResourceBuilder;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.elements.PolicyTargetType;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.GroupDefinition;
import org.openecomp.sdc.be.model.PolicyDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PoliciesMergeCommandTest {

    private static final String CONTAINER_ID = "containerId";
    private static final String GROUP1 = "group1";
    private static final String GROUP1_ORIG_ID = GROUP1 + "Id";
    private static final String GROUP1_UPDATED_ID = GROUP1 +"NewId";
    private static final String GROUP2_ORIG_ID = "group2Id";
    private static final String INSTANCE1 = "instance1";
    private static final String INSTANCE1_ORIG_ID = INSTANCE1 + "Id";
    private static final String INSTANCE1_UPDATED_ID = INSTANCE1+ "NewId";
    private static final String INSTANCE2_ORIG_ID = "instance2Id";
    private static final Resource DONT_CARE = new Resource();
    @InjectMocks
    private PoliciesMergeCommand testInstance;
    @Mock
    private ToscaOperationFacade toscaOperationFacade;
    @Mock
    private ComponentsUtils componentsUtils;
    @Captor
    private ArgumentCaptor<List<PolicyDefinition>> policiesToMerge;

    private PolicyDefinition noTargetsPolicy;
    private ComponentInstance instance1;
    private GroupDefinition group1;
    private Resource prevResource, newResource;
    private PolicyDefinition policy1;

    @Before
    public void setUp() throws Exception {
        instance1 = new ComponentInstanceBuilder()
                .setName(INSTANCE1)
                .setId(INSTANCE1_UPDATED_ID)
                .build();
        group1 = GroupDefinitionBuilder.create()
                .setName(GROUP1 + "newName")
                .setInvariantName(GROUP1)
                .setUniqueId(GROUP1_UPDATED_ID)
                .build();
        newResource = new ResourceBuilder()
                .addGroup(group1)
                .addComponentInstance(instance1)
                .setUniqueId(CONTAINER_ID)
                .build();

        ComponentInstance prevInstance1 = new ComponentInstanceBuilder()
                .setName(INSTANCE1)
                .setId(INSTANCE1_ORIG_ID)
                .build();

        ComponentInstance prevInstance2 = new ComponentInstanceBuilder()
                .setName(INSTANCE2_ORIG_ID)
                .setId(INSTANCE2_ORIG_ID)
                .build();

        GroupDefinition prevGroup1 = GroupDefinitionBuilder.create()
                .setName(GROUP1)
                .setInvariantName(GROUP1)
                .setUniqueId(GROUP1_ORIG_ID)
                .build();

        GroupDefinition prevGroup2 = GroupDefinitionBuilder.create()
                .setName(GROUP2_ORIG_ID)
                .setInvariantName(GROUP2_ORIG_ID)
                .setUniqueId(GROUP2_ORIG_ID)
                .build();

        policy1 = PolicyDefinitionBuilder.create()
                .addComponentInstanceTarget(prevInstance1.getUniqueId())
                .addComponentInstanceTarget(prevInstance2.getUniqueId())
                .addGroupTarget(prevGroup1.getUniqueId())
                .addGroupTarget(prevGroup2.getUniqueId())
                .setName("policy1")
                .setUniqueId("policy1")
                .build();

        noTargetsPolicy = PolicyDefinitionBuilder.create()
                .setUniqueId("policy2")
                .setName("policy2")
                .build();


        prevResource = new ResourceBuilder()
                .addGroup(prevGroup1)
                .addGroup(prevGroup2)
                .addComponentInstance(prevInstance1)
                .addComponentInstance(prevInstance2)
                .build();
    }

    @Test
    public void whenPreviousComponentHasNoPolicies_returnOk() {
        ActionStatus mergeResult = testInstance.mergeComponents(new Resource(), DONT_CARE);
        assertThat(mergeResult).isEqualTo(ActionStatus.OK);
        verifyZeroInteractions(toscaOperationFacade, componentsUtils);
    }

    @Test
    public void associatePrevPoliciesToNewComponent() {
        prevResource = new ResourceBuilder()
                .addPolicy(policy1)
                .addPolicy(noTargetsPolicy)
                .build();
        when(toscaOperationFacade.associatePoliciesToComponent(eq(CONTAINER_ID), policiesToMerge.capture())).thenReturn(StorageOperationStatus.OK);
        when(componentsUtils.convertFromStorageResponse(StorageOperationStatus.OK)).thenReturn(ActionStatus.OK);
        ActionStatus actionStatus = testInstance.mergeComponents(prevResource, newResource);
        assertThat(actionStatus).isEqualTo(ActionStatus.OK);
        List<PolicyDefinition> mergedPolicies = policiesToMerge.getValue();
        assertThat(mergedPolicies)
                .containsExactlyInAnyOrder(policy1, noTargetsPolicy);
    }

    @Test
    public void whenFailingToAssociatePolicies_propagateTheError() {
        prevResource = new ResourceBuilder()
                .addPolicy(policy1)
                .build();
        when(toscaOperationFacade.associatePoliciesToComponent(eq(CONTAINER_ID), anyList())).thenReturn(StorageOperationStatus.GENERAL_ERROR);
        when(componentsUtils.convertFromStorageResponse(StorageOperationStatus.GENERAL_ERROR)).thenReturn(ActionStatus.GENERAL_ERROR);
        ActionStatus actionStatus = testInstance.mergeComponents(prevResource, newResource);
        assertThat(actionStatus).isEqualTo(ActionStatus.GENERAL_ERROR);
    }

    @Test
    public void whenPreviousPolicyTargetsDoesNotExistInNewVersion_ignoreTargets() {
        Map<PolicyTargetType, List<String>> expectedTargetsMap = createTargetsMap(singletonList(instance1.getUniqueId()), singletonList(group1.getUniqueId()));
        mergePolicies_verifyPoliciesTargets(policy1, expectedTargetsMap);
    }

    @Test
    public void whenPrevPolicyGroupTargetNotExistInNewVersion_returnEmptyListInTargetMap() {
        PolicyDefinition policy = PolicyDefinitionBuilder.create()
                .setName("policy")
                .setUniqueId("policy")
                .addGroupTarget(GROUP2_ORIG_ID)
                .addComponentInstanceTarget(INSTANCE1_ORIG_ID)
                .build();
        Map<PolicyTargetType, List<String>> expectedTargetsMap = createTargetsMap(singletonList(INSTANCE1_UPDATED_ID), emptyList());
        mergePolicies_verifyPoliciesTargets(policy, expectedTargetsMap);
    }

    @Test
    public void whenPrevCapabilityInstanceTargetNotExistInNewVersion_returnEmptyListInTargetMap() {
        PolicyDefinition policy = PolicyDefinitionBuilder.create()
                .setName("policy")
                .setUniqueId("policy")
                .addGroupTarget(GROUP1_ORIG_ID)
                .addComponentInstanceTarget(INSTANCE2_ORIG_ID)
                .build();
        Map<PolicyTargetType, List<String>> expectedTargetsMap = createTargetsMap(emptyList(), singletonList(GROUP1_UPDATED_ID));
        mergePolicies_verifyPoliciesTargets(policy, expectedTargetsMap);
    }

    @Test
    public void whenPrevCapabilityInstanceAndGroupTargetsNotExistInNewVersion_returnTargetMapWithEmptyListsOfIds() {
        PolicyDefinition policy = PolicyDefinitionBuilder.create()
                .setName("policy")
                .setUniqueId("policy")
                .addGroupTarget(GROUP2_ORIG_ID)
                .addComponentInstanceTarget(INSTANCE2_ORIG_ID)
                .build();
        Map<PolicyTargetType, List<String>> expectedTargetsMap = createTargetsMap(emptyList(), emptyList());
        mergePolicies_verifyPoliciesTargets(policy, expectedTargetsMap);
    }

    @Test
    public void whenPrevCapabilityHasNoTargets_returnNullTargetsMap() {
        mergePolicies_verifyPoliciesTargets(noTargetsPolicy, null);
    }

    private void mergePolicies_verifyPoliciesTargets(PolicyDefinition prevPolicy, Map<PolicyTargetType, List<String>> expectedTargetsMap) {
        prevResource.setPolicies(new HashMap<>());
        prevResource.getPolicies().put(prevPolicy.getUniqueId(), prevPolicy);
        when(toscaOperationFacade.associatePoliciesToComponent(eq(CONTAINER_ID), policiesToMerge.capture())).thenReturn(StorageOperationStatus.OK);
        when(componentsUtils.convertFromStorageResponse(StorageOperationStatus.OK)).thenReturn(ActionStatus.OK);
        ActionStatus actionStatus = testInstance.mergeComponents(prevResource, newResource);
        assertThat(actionStatus).isEqualTo(ActionStatus.OK);
        List<PolicyDefinition> mergedPolicies = policiesToMerge.getValue();
        assertThat(mergedPolicies)
                .extracting("targets")
                .containsExactlyInAnyOrder(expectedTargetsMap);
    }
    
    private Map<PolicyTargetType, List<String>> createTargetsMap(List<String> cmptInstanceTargets, List<String> groupTargets) {
        return ImmutableMap.of(PolicyTargetType.COMPONENT_INSTANCES, cmptInstanceTargets, PolicyTargetType.GROUPS, groupTargets);
    }

}
