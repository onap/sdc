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

package org.openecomp.sdc.be.components.impl.policy;

import org.apache.commons.collections.MapUtils;
import org.openecomp.sdc.be.datatypes.elements.PolicyTargetType;
import org.openecomp.sdc.be.datatypes.enums.PromoteVersionEnum;
import org.openecomp.sdc.be.model.PolicyDefinition;
import org.openecomp.sdc.be.model.utils.GroupUtils;
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
        policies.forEach(policy ->removePolicyTarget(policy, targetId, targetType));
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
        policyDefinition.setVersion(GroupUtils.updateVersion(PromoteVersionEnum.MINOR, policyDefinition.getVersion()));
    }

    private void removePolicyTarget(PolicyDefinition policy, String targetId, PolicyTargetType targetType) {
        List<String> policyTargets = getTargetsList(policy, targetType);
        if (isEmpty(policyTargets)) {
            return;
        }
        
        policyTargets.remove(targetId);
        policy.setVersion(GroupUtils.updateVersion(PromoteVersionEnum.MINOR, policy.getVersion()));
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
