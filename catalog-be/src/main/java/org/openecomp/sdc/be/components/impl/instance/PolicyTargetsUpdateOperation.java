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
package org.openecomp.sdc.be.components.impl.instance;

import org.openecomp.sdc.be.components.impl.policy.PolicyTargetsUpdateHandler;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.elements.PolicyTargetType;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.common.log.wrappers.Logger;

@org.springframework.stereotype.Component
public class PolicyTargetsUpdateOperation implements OnComponentInstanceChangeOperation {

    private static final Logger log = Logger.getLogger(PolicyTargetsUpdateOperation.class);
    private final PolicyTargetsUpdateHandler policyTargetsUpdateHandler;

    public PolicyTargetsUpdateOperation(PolicyTargetsUpdateHandler policyTargetsUpdateHandler) {
        this.policyTargetsUpdateHandler = policyTargetsUpdateHandler;
    }

    @Override
    public ActionStatus onChangeVersion(Component container, ComponentInstance prevVersion, ComponentInstance newVersion) {
        log.debug("#onChangeVersion - replacing all policy targets referencing component instance {} with component instance {}",
            prevVersion.getUniqueId(), newVersion.getUniqueId());
        return policyTargetsUpdateHandler
            .replacePoliciesTargets(container, prevVersion.getUniqueId(), newVersion.getUniqueId(), PolicyTargetType.COMPONENT_INSTANCES);
    }

    @Override
    public ActionStatus onDelete(Component container, String deletedEntityId) {
        log.debug("#onDelete - removing all component {} policy targets referencing component instance {}", container.getUniqueId(), deletedEntityId);
        return policyTargetsUpdateHandler.removePoliciesTargets(container, deletedEntityId, PolicyTargetType.COMPONENT_INSTANCES);
    }
}
