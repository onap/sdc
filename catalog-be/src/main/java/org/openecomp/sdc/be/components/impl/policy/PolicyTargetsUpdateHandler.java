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

import static org.apache.commons.collections.CollectionUtils.isEmpty;

import java.util.List;
import java.util.function.Consumer;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.elements.PolicyTargetType;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.PolicyDefinition;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.common.log.wrappers.Logger;

@org.springframework.stereotype.Component
public class PolicyTargetsUpdateHandler {

    private static final Logger log = Logger.getLogger(PolicyTargetsUpdateHandler.class);
    private final ToscaOperationFacade toscaOperationFacade;
    private final ComponentsUtils componentsUtils;
    private final PolicyTargetsUpdater policyTargetsUpdater;

    public PolicyTargetsUpdateHandler(ToscaOperationFacade toscaOperationFacade, ComponentsUtils componentsUtils,
                                      PolicyTargetsUpdater policyTargetsUpdater) {
        this.toscaOperationFacade = toscaOperationFacade;
        this.componentsUtils = componentsUtils;
        this.policyTargetsUpdater = policyTargetsUpdater;
    }

    public ActionStatus removePoliciesTargets(Component container, String targetId, PolicyTargetType targetType) {
        log.debug("#removePoliciesTargets - removing all component {} policy targets referencing target {}", container.getUniqueId(), targetId);
        Consumer<List<PolicyDefinition>> removeTarget = policies -> policyTargetsUpdater.removeTarget(policies, targetId, targetType);
        return updatePolicyTargets(container, targetId, targetType, removeTarget);
    }

    public ActionStatus replacePoliciesTargets(Component container, String prevTargetId, String newTargetId, PolicyTargetType targetType) {
        log.debug("#replacePoliciesTargets - replacing all policy targets referencing target {} with target {}", prevTargetId, newTargetId);
        Consumer<List<PolicyDefinition>> replaceTarget = policies -> policyTargetsUpdater
            .replaceTarget(policies, prevTargetId, newTargetId, targetType);
        return updatePolicyTargets(container, prevTargetId, targetType, replaceTarget);
    }

    private ActionStatus updatePolicyTargets(Component container, String targetId, PolicyTargetType targetType,
                                             Consumer<List<PolicyDefinition>> updatePolicyTargetTaskRunner) {
        List<PolicyDefinition> policiesWithPrevInstanceAsTarget = container.resolvePoliciesContainingTarget(targetId, targetType);
        if (isEmpty(policiesWithPrevInstanceAsTarget)) {
            return ActionStatus.OK;
        }
        updatePolicyTargetTaskRunner.accept(policiesWithPrevInstanceAsTarget);
        return updatePolicies(container, policiesWithPrevInstanceAsTarget);
    }

    private ActionStatus updatePolicies(Component policiesContainer, List<PolicyDefinition> policiesToUpdate) {
        log.debug("#updatePolicies - updating {} policies for container {}", policiesToUpdate.size(), policiesContainer.getUniqueId());
        StorageOperationStatus updatePolicyResult = toscaOperationFacade.updatePoliciesOfComponent(policiesContainer.getUniqueId(), policiesToUpdate);
        return componentsUtils.convertFromStorageResponse(updatePolicyResult, policiesContainer.getComponentType());
    }
}
