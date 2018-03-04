package org.openecomp.sdc.be.components.impl.version;

import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.PolicyDefinition;
import org.openecomp.sdc.be.model.jsontitan.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.UnaryOperator;

import static org.apache.commons.collections.CollectionUtils.isEmpty;

@org.springframework.stereotype.Component
public class PolicyTargetsUpdateOperation implements PostChangeVersionOperation {

    private static final Logger log = LoggerFactory.getLogger(PolicyTargetsUpdateOperation.class);
    private final ToscaOperationFacade toscaOperationFacade;
    private final ComponentsUtils componentsUtils;

    public PolicyTargetsUpdateOperation(ToscaOperationFacade toscaOperationFacade, ComponentsUtils componentsUtils) {
        this.toscaOperationFacade = toscaOperationFacade;
        this.componentsUtils = componentsUtils;
    }

    @Override
    public ActionStatus onChangeVersion(Component container, ComponentInstance prevVersion, ComponentInstance newVersion) {
        return replacePolicyTargetsInstanceId(container, prevVersion, newVersion);
    }

    private ActionStatus replacePolicyTargetsInstanceId(Component container, ComponentInstance prevVersion, ComponentInstance newVersion) {
        log.debug("#replacePolicyTargetsInstanceId - replacing all policy targets for component instance {} with component instance {}", prevVersion.getUniqueId(), newVersion.getUniqueId());
        List<PolicyDefinition> policiesWithPrevInstanceAsTarget = container.resolvePoliciesByComponentInstanceTarget(prevVersion.getUniqueId());
        if (isEmpty(policiesWithPrevInstanceAsTarget)) {
            return ActionStatus.OK;
        }
        replaceTargetsPrevInstanceIdWithNewInstanceId(prevVersion, newVersion, policiesWithPrevInstanceAsTarget);
        return updatePolicies(container, policiesWithPrevInstanceAsTarget);
    }

    private ActionStatus updatePolicies(Component policiesContainer, List<PolicyDefinition> policiesToUpdate) {
        log.debug("#updatePolicies - updating {} policies for container {}", policiesToUpdate.size(), policiesContainer.getUniqueId());
        StorageOperationStatus updatePolicyResult = toscaOperationFacade.updatePoliciesOfComponent(policiesContainer.getUniqueId(), policiesToUpdate);
        return componentsUtils.convertFromStorageResponse(updatePolicyResult, policiesContainer.getComponentType());
    }

    private void replaceTargetsPrevInstanceIdWithNewInstanceId(ComponentInstance prevVersion, ComponentInstance newVersion, List<PolicyDefinition> policiesWithPrevInstanceAsTarget) {
        policiesWithPrevInstanceAsTarget.forEach(policy -> updatePolicyTargetWithNewInstanceVersion(policy, prevVersion.getUniqueId(), newVersion.getUniqueId()));
    }

    private void updatePolicyTargetWithNewInstanceVersion(PolicyDefinition policyDefinition, String prevInstanceId, String newInstanceId) {
        List<String> policyInstanceTargets = policyDefinition.resolveComponentInstanceTargets();
        if (isEmpty(policyInstanceTargets)) {
            return;
        }
        policyInstanceTargets.replaceAll(prevInstanceIdByNewInstanceId(prevInstanceId, newInstanceId));
    }

    private UnaryOperator<String> prevInstanceIdByNewInstanceId(String prevInstanceId, String newInstanceId) {
        return instId -> instId.equals(prevInstanceId) ? newInstanceId: instId;
    }

}
