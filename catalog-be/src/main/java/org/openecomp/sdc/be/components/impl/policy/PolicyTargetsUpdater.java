package org.openecomp.sdc.be.components.impl.policy;

import org.apache.commons.collections.MapUtils;
import org.openecomp.sdc.be.datatypes.elements.PolicyTargetType;
import org.openecomp.sdc.be.model.PolicyDefinition;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;

import static java.util.Collections.emptyList;
import static org.apache.commons.collections.CollectionUtils.isEmpty;

/**
 * A Helper class which handles altering the targets state of a policy
 */
@Component
public class PolicyTargetsUpdater {

    public void removeTarget(List<PolicyDefinition> policies, String targetId, PolicyTargetType targetType) {
        policies.forEach(policy -> removePolicyTarget(policy, targetId, targetType));
    }

    public void replaceTarget(List<PolicyDefinition> policies, String oldTargetId, String newTargetId, PolicyTargetType targetType) {
        policies.forEach(policy -> replacePolicyTarget(policy, targetType, oldTargetId, newTargetId));
    }

    private void replacePolicyTarget(PolicyDefinition policyDefinition, PolicyTargetType targetType, String oldTargetId, String newTargetId) {
        List<String> policyTargets = getTargetsList(policyDefinition, targetType);
        if (isEmpty(policyTargets)) {
            return;
        }
        policyTargets.replaceAll(prevInstanceIdByNewInstanceId(oldTargetId, newTargetId));
    }

    private void removePolicyTarget(PolicyDefinition policy, String targetId, PolicyTargetType targetType) {
        List<String> policyTargets = getTargetsList(policy, targetType);
        if (isEmpty(policyTargets)) {
            return;
        }
        policyTargets.remove(targetId);
    }

    private List<String> getTargetsList(PolicyDefinition policyDefinition, PolicyTargetType targetType) {
        Map<PolicyTargetType, List<String>> targets = policyDefinition.getTargets();
        if(MapUtils.isEmpty(targets) || isEmpty(targets.get(targetType))) {
            return emptyList();
        }
        return targets.get(targetType);
    }

    private UnaryOperator<String> prevInstanceIdByNewInstanceId(String prevInstanceId, String newInstanceId) {
        return instId -> instId.equals(prevInstanceId) ? newInstanceId: instId;
    }

}
