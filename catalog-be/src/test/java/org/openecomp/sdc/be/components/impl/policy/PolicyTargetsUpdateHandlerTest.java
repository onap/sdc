package org.openecomp.sdc.be.components.impl.policy;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.sdc.be.components.impl.exceptions.ComponentException;
import org.openecomp.sdc.be.components.utils.PolicyDefinitionBuilder;
import org.openecomp.sdc.be.components.utils.ResourceBuilder;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.elements.PolicyTargetType;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.PolicyDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.openecomp.sdc.be.components.impl.utils.TestGenerationUtils.getComponentsUtils;

@RunWith(MockitoJUnitRunner.class)
//note that this class only tests group targets, the tests for instance targets are under PolicyTargetsUpdateOperationTest
public class PolicyTargetsUpdateHandlerTest {

    private static final String CONTAINER_ID = "containerId";

    private PolicyTargetsUpdateHandler testInstance;
    @Mock
    private ToscaOperationFacade toscaOperationFacade;
    @Captor
    private ArgumentCaptor<List<PolicyDefinition>> updatedPoliciesCaptor;
    private PolicyDefinition policyWithGroupTarget1, policyWithGroupTarget2, policyWithGroupTarget3;
    private Resource container;

    @Before
    public void setUp() throws Exception {
        policyWithGroupTarget1 = createPolicyWithGroupsAsTargets("policy1", "group1", "group2");
        policyWithGroupTarget2 = createPolicyWithGroupsAsTargets("policy2", "group1", "group2", "group3");
        policyWithGroupTarget3 = createPolicyWithGroupsAsTargets("policy3", "group1", "group3");
        container = new ResourceBuilder()
                .addPolicy(policyWithGroupTarget1)
                .addPolicy(policyWithGroupTarget2)
                .addPolicy(policyWithGroupTarget3)
                .setUniqueId(CONTAINER_ID)
                .build();
        testInstance = new PolicyTargetsUpdateHandler(toscaOperationFacade, getComponentsUtils(), new PolicyTargetsUpdater());
    }


    @Test
    public void onDeleteInstance_whenNoPolicies_returnActionOk() {
        Component container = new Resource();
        testInstance.removePoliciesTargets(container, "groupToDel", PolicyTargetType.GROUPS);
        verifyZeroInteractions(toscaOperationFacade);
    }

    @Test
    public void onDeleteInstance_whenNoPoliciesWithGroupsAsTargets_returnOk() {
        PolicyDefinition policy = PolicyDefinitionBuilder.create().addComponentInstanceTarget("someInst").build();
        Component container = new ResourceBuilder().addPolicy(policy).build();
        testInstance.removePoliciesTargets(container, "groupToDel", PolicyTargetType.GROUPS);
        verifyZeroInteractions(toscaOperationFacade);
    }

    @Test
    public void onDeleteInstance_whenNoPoliciesWithGivenGroupAsTarget_returnActionOk() {
        testInstance.removePoliciesTargets(container, "groupToDel", PolicyTargetType.GROUPS);
        verifyZeroInteractions(toscaOperationFacade);
    }

    @Test
    public void onDeleteInstance_removeDeletedTargetFromPolicies() {
        when(toscaOperationFacade.updatePoliciesOfComponent(eq(CONTAINER_ID), updatedPoliciesCaptor.capture())).thenReturn(StorageOperationStatus.OK);
        testInstance.removePoliciesTargets(container, "group2", PolicyTargetType.GROUPS);
        List<PolicyDefinition> updatedPolicies = updatedPoliciesCaptor.getValue();
        verifyUpdatedPolicies(updatedPolicies, policyWithGroupTarget1, policyWithGroupTarget2);
        verifyUpdatedPolicyTargets(policyWithGroupTarget1, "group1");
        verifyUpdatedPolicyTargets(policyWithGroupTarget2, "group1", "group3");
    }

    @Test
    public void onDeleteInstance_whenFailingToUpdatePolicies_throwException() {
        when(toscaOperationFacade.updatePoliciesOfComponent(eq(CONTAINER_ID), anyList())).thenReturn(StorageOperationStatus.GENERAL_ERROR);
        try {
            testInstance.removePoliciesTargets(container, "group2", PolicyTargetType.GROUPS);
        } catch (ComponentException e) {
            assertThat(e.getActionStatus()).isEqualTo(ActionStatus.GENERAL_ERROR);
        }
    }

    private void verifyUpdatedPolicyTargets(PolicyDefinition updatedPolicy, String ... expectedCmptInstanceTargetIds) {
        assertThat(updatedPolicy.resolveGroupTargets())
                .containsExactlyInAnyOrder(expectedCmptInstanceTargetIds);
    }

    private void verifyUpdatedPolicies(List<PolicyDefinition> updatedPolicies, PolicyDefinition ... expectedUpdatedPolicies) {
        assertThat(updatedPolicies)
                .usingElementComparatorOnFields("targets")
                .containsExactlyInAnyOrder(expectedUpdatedPolicies);
    }

    private PolicyDefinition createPolicyWithGroupsAsTargets(String uniqueId, String ... groupId) {
        PolicyDefinitionBuilder policyDefinitionBuilder = PolicyDefinitionBuilder.create();
        Stream.of(groupId).forEach(policyDefinitionBuilder::addGroupTarget);
        return policyDefinitionBuilder.setUniqueId(uniqueId).build();
    }
}