package org.openecomp.sdc.be.components.impl.instance;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.sdc.be.components.impl.policy.PolicyTargetsUpdateHandler;
import org.openecomp.sdc.be.components.impl.policy.PolicyTargetsUpdater;
import org.openecomp.sdc.be.components.utils.ComponentInstanceBuilder;
import org.openecomp.sdc.be.components.utils.PolicyDefinitionBuilder;
import org.openecomp.sdc.be.components.utils.ResourceBuilder;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
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
public class PolicyTargetsUpdateOperationTest {

    private static final String CONTAINER_ID = "containerId";

    private PolicyTargetsUpdateOperation policyTargetsUpdateOperation;
    @Mock
    private ToscaOperationFacade toscaOperationFacade;

    @Captor
    private ArgumentCaptor<List<PolicyDefinition>> updatedPoliciesCaptor;
    private PolicyDefinition policyWithInstanceTarget1, policyWithInstanceTarget2, policyWithInstanceTarget3;
    private Resource container;

    @Before
    public void setUp() throws Exception {
        policyWithInstanceTarget1 = createPolicyWithCmptInstAsTargets("policy1", "inst1", "inst2");
        policyWithInstanceTarget2 = createPolicyWithCmptInstAsTargets("policy2", "inst1", "inst2", "inst3");
        policyWithInstanceTarget3 = createPolicyWithCmptInstAsTargets("policy3", "inst1", "inst3");
        container = new ResourceBuilder()
                .addPolicy(policyWithInstanceTarget1)
                .addPolicy(policyWithInstanceTarget2)
                .addPolicy(policyWithInstanceTarget3)
                .setUniqueId(CONTAINER_ID)
                .build();
        PolicyTargetsUpdateHandler policyTargetsUpdateHandler = new PolicyTargetsUpdateHandler(toscaOperationFacade, getComponentsUtils(), new PolicyTargetsUpdater());
        policyTargetsUpdateOperation = new PolicyTargetsUpdateOperation(policyTargetsUpdateHandler);
    }

    @Test
    public void onChangeVersion_whenNoPolicies_returnActionOk() {
        Component container = new Resource();
        ComponentInstance prevVersion = new ComponentInstanceBuilder().setId("prevVersion").build();
        ComponentInstance newVersion = new ComponentInstanceBuilder().setId("newVersion").build();
        ActionStatus operationStatus = policyTargetsUpdateOperation.onChangeVersion(container, prevVersion, newVersion);
        assertThat(operationStatus).isEqualTo(ActionStatus.OK);
        verifyZeroInteractions(toscaOperationFacade);
    }

    @Test
    public void onChangeVersion_whenNoCmptInstancePolicies_returnOk() {
        PolicyDefinition policy = PolicyDefinitionBuilder.create().addGroupTarget("someGroup").build();
        Component container = new ResourceBuilder().addPolicy(policy).build();
        ComponentInstance prevVersion = new ComponentInstanceBuilder().setId("prevVersion").build();
        ComponentInstance newVersion = new ComponentInstanceBuilder().setId("newVersion").build();
        ActionStatus operationStatus = policyTargetsUpdateOperation.onChangeVersion(container, prevVersion, newVersion);
        assertThat(operationStatus).isEqualTo(ActionStatus.OK);
        verifyZeroInteractions(toscaOperationFacade);
    }

    @Test
    public void onChangeVersion_whenNoPoliciesWithPrevInstanceAsTarget_returnActionOk() {
        ComponentInstance prevVersion = new ComponentInstanceBuilder().setId("prevVersion").build();
        ComponentInstance newVersion = new ComponentInstanceBuilder().setId("newVersion").build();
        ActionStatus operationStatus = policyTargetsUpdateOperation.onChangeVersion(container, prevVersion, newVersion);
        assertThat(operationStatus).isEqualTo(ActionStatus.OK);
        verifyZeroInteractions(toscaOperationFacade);
    }

    @Test
    public void onChangeVersion_replacePrevCmptInstanceIdWithNewInstanceIdAndUpdatePolicies() {
        ComponentInstance prevVersion = new ComponentInstanceBuilder().setId("inst2").build();
        ComponentInstance newVersion = new ComponentInstanceBuilder().setId("inst2New").build();
        when(toscaOperationFacade.updatePoliciesOfComponent(eq(CONTAINER_ID), updatedPoliciesCaptor.capture())).thenReturn(StorageOperationStatus.OK);
        ActionStatus updatePoliciesRes = policyTargetsUpdateOperation.onChangeVersion(container, prevVersion, newVersion);
        assertThat(updatePoliciesRes).isEqualTo(ActionStatus.OK);
        List<PolicyDefinition> updatedPolicies = updatedPoliciesCaptor.getValue();
        verifyUpdatedPolicies(updatedPolicies, policyWithInstanceTarget1, policyWithInstanceTarget2);//policy3 does not have "inst2" as target, no update needed
        verifyUpdatedPolicyTargets(policyWithInstanceTarget1, "inst1", "inst2New");
        verifyUpdatedPolicyTargets(policyWithInstanceTarget2, "inst1", "inst2New", "inst3");
    }

    @Test
    public void onChangeVersion_whenUpdateOfPoliciesFails_propagateTheFailure() {
        ComponentInstance prevVersion = new ComponentInstanceBuilder().setId("inst2").build();
        ComponentInstance newVersion = new ComponentInstanceBuilder().setId("inst2New").build();
        when(toscaOperationFacade.updatePoliciesOfComponent(eq(CONTAINER_ID), anyList())).thenReturn(StorageOperationStatus.GENERAL_ERROR);
        ActionStatus updatePoliciesRes = policyTargetsUpdateOperation.onChangeVersion(container, prevVersion, newVersion);
        assertThat(updatePoliciesRes).isEqualTo(ActionStatus.GENERAL_ERROR);
    }

    @Test
    public void onDeleteInstance_whenNoPolicies_returnActionOk() {
        Component container = new Resource();
        ActionStatus operationStatus = policyTargetsUpdateOperation.onDelete(container, "instToDel");
        assertThat(operationStatus).isEqualTo(ActionStatus.OK);
        verifyZeroInteractions(toscaOperationFacade);
    }

    @Test
    public void onDeleteInstance_whenNoCmptInstancePolicies_returnOk() {
        PolicyDefinition policy = PolicyDefinitionBuilder.create().addGroupTarget("someGroup").build();
        Component container = new ResourceBuilder().addPolicy(policy).build();
        ActionStatus operationStatus = policyTargetsUpdateOperation.onDelete(container, "instToDel");
        assertThat(operationStatus).isEqualTo(ActionStatus.OK);
        verifyZeroInteractions(toscaOperationFacade);
    }

    @Test
    public void onDeleteInstance_whenNoPoliciesWithInstanceAsTarget_returnActionOk() {
        ActionStatus operationStatus = policyTargetsUpdateOperation.onDelete(container, "instToDel");
        assertThat(operationStatus).isEqualTo(ActionStatus.OK);
        verifyZeroInteractions(toscaOperationFacade);
    }

    @Test
    public void onDeleteInstance_removeDeletedTargetFromPolicies() {
        when(toscaOperationFacade.updatePoliciesOfComponent(eq(CONTAINER_ID), updatedPoliciesCaptor.capture())).thenReturn(StorageOperationStatus.OK);
        ActionStatus operationStatus = policyTargetsUpdateOperation.onDelete(container, "inst2");
        assertThat(operationStatus).isEqualTo(ActionStatus.OK);
        List<PolicyDefinition> updatedPolicies = updatedPoliciesCaptor.getValue();
        verifyUpdatedPolicies(updatedPolicies, policyWithInstanceTarget1, policyWithInstanceTarget2);
        verifyUpdatedPolicyTargets(policyWithInstanceTarget1, "inst1");
        verifyUpdatedPolicyTargets(policyWithInstanceTarget2, "inst1", "inst3");
    }

    @Test
    public void onDeleteInstance_whenFailingToUpdatePolicies_propagateTheError() {
        when(toscaOperationFacade.updatePoliciesOfComponent(eq(CONTAINER_ID), anyList())).thenReturn(StorageOperationStatus.GENERAL_ERROR);
        ActionStatus operationStatus = policyTargetsUpdateOperation.onDelete(container, "inst2");
        assertThat(operationStatus).isEqualTo(ActionStatus.GENERAL_ERROR);
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