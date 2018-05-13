package org.openecomp.sdc.be.components.impl.version;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.openecomp.sdc.be.components.impl.utils.TestGenerationUtils.getComponentsUtils;

import java.util.List;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.sdc.be.components.utils.ComponentInstanceBuilder;
import org.openecomp.sdc.be.components.utils.PolicyDefinitionBuilder;
import org.openecomp.sdc.be.components.utils.ResourceBuilder;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.PolicyDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.jsontitan.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;

@RunWith(MockitoJUnitRunner.class)
public class PolicyTargetsUpdateOperationTest {

    private static final String CONTAINER_ID = "containerId";

    private PolicyTargetsUpdateOperation policyTargetsUpdateOperation;
    @Mock
    private ToscaOperationFacade toscaOperationFacade;

    @Captor
    private ArgumentCaptor<List<PolicyDefinition>> updatedPoliciesCaptor;
    private PolicyDefinition policy1, policy2, policy3;
    private Resource container;

    @Before
    public void setUp() throws Exception {
        policy1 = createPolicyWithCmptInstAsTargets("policy1", "inst1", "inst2");
        policy2 = createPolicyWithCmptInstAsTargets("policy2", "inst1", "inst2", "inst3");
        policy3 = createPolicyWithCmptInstAsTargets("policy3", "inst1", "inst3");
        container = new ResourceBuilder()
                .addPolicy(policy1)
                .addPolicy(policy2)
                .addPolicy(policy3)
                .setUniqueId(CONTAINER_ID)
                .build();
        policyTargetsUpdateOperation = new PolicyTargetsUpdateOperation(toscaOperationFacade, getComponentsUtils());
    }

    @Test
    public void whenNoPolicies_returnActionOk() {
        Component container = new Resource();
        ComponentInstance prevVersion = new ComponentInstanceBuilder().setId("prevVersion").build();
        ComponentInstance newVersion = new ComponentInstanceBuilder().setId("newVersion").build();
        ActionStatus operationStatus = policyTargetsUpdateOperation.onChangeVersion(container, prevVersion, newVersion);
        assertThat(operationStatus).isEqualTo(ActionStatus.OK);
        verifyZeroInteractions(toscaOperationFacade);
    }

    @Test
    public void whenNoCmptInstancePolicies_returnOk() {
        PolicyDefinition policy = PolicyDefinitionBuilder.create().addGroupTarget("someGroup").build();
        Component container = new ResourceBuilder().addPolicy(policy).build();
        ComponentInstance prevVersion = new ComponentInstanceBuilder().setId("prevVersion").build();
        ComponentInstance newVersion = new ComponentInstanceBuilder().setId("newVersion").build();
        ActionStatus operationStatus = policyTargetsUpdateOperation.onChangeVersion(container, prevVersion, newVersion);
        assertThat(operationStatus).isEqualTo(ActionStatus.OK);
        verifyZeroInteractions(toscaOperationFacade);
    }

    @Test
    public void whenNoPoliciesWithPrevInstanceAsTarget_returnActionOk() {
        ComponentInstance prevVersion = new ComponentInstanceBuilder().setId("prevVersion").build();
        ComponentInstance newVersion = new ComponentInstanceBuilder().setId("newVersion").build();
        ActionStatus operationStatus = policyTargetsUpdateOperation.onChangeVersion(container, prevVersion, newVersion);
        assertThat(operationStatus).isEqualTo(ActionStatus.OK);
        verifyZeroInteractions(toscaOperationFacade);
    }

    @Test
    public void replacePrevCmptInstanceIdWithNewInstanceIdAndUpdatePolicies() {
        ComponentInstance prevVersion = new ComponentInstanceBuilder().setId("inst2").build();
        ComponentInstance newVersion = new ComponentInstanceBuilder().setId("inst2New").build();
        when(toscaOperationFacade.updatePoliciesOfComponent(eq(CONTAINER_ID), updatedPoliciesCaptor.capture())).thenReturn(StorageOperationStatus.OK);
        ActionStatus updatePoliciesRes = policyTargetsUpdateOperation.onChangeVersion(container, prevVersion, newVersion);
        assertThat(updatePoliciesRes).isEqualTo(ActionStatus.OK);
        List<PolicyDefinition> updatedPolicies = updatedPoliciesCaptor.getValue();
        verifyUpdatedPolicies(updatedPolicies, policy1, policy2);//policy3 does not have "inst2" as target, no update needed
        verifyUpdatedPolicyTargets(policy1, "inst1", "inst2New");
        verifyUpdatedPolicyTargets(policy2, "inst1", "inst2New", "inst3");
    }

    @Test
    public void whenUpdateOfPoliciesFails_propagateTheFailure() {
        ComponentInstance prevVersion = new ComponentInstanceBuilder().setId("inst2").build();
        ComponentInstance newVersion = new ComponentInstanceBuilder().setId("inst2New").build();
        when(toscaOperationFacade.updatePoliciesOfComponent(eq(CONTAINER_ID), anyList())).thenReturn(StorageOperationStatus.GENERAL_ERROR);
        ActionStatus updatePoliciesRes = policyTargetsUpdateOperation.onChangeVersion(container, prevVersion, newVersion);
        assertThat(updatePoliciesRes).isEqualTo(ActionStatus.GENERAL_ERROR);
    }

    private void verifyUpdatedPolicyTargets(PolicyDefinition updatedPolicy, String ... expectedCmptInstanceTargetIds) {
        assertThat(updatedPolicy.resolveComponentInstanceTargets())
                .containsExactlyInAnyOrder(expectedCmptInstanceTargetIds);
    }

    private void verifyUpdatedPolicies(List<PolicyDefinition> updatedPolicies, PolicyDefinition ... expectedUpdatedPolicies) {
        assertThat(updatedPolicies)
                .usingElementComparatorOnFields("targets")
                .containsExactlyInAnyOrder(expectedUpdatedPolicies);
    }

    private PolicyDefinition createPolicyWithCmptInstAsTargets(String uniqueId, String ... instId) {
        PolicyDefinitionBuilder policyDefinitionBuilder = PolicyDefinitionBuilder.create();
        Stream.of(instId).forEach(policyDefinitionBuilder::addComponentInstanceTarget);
        return policyDefinitionBuilder.setUniqueId(uniqueId).build();
    }
}