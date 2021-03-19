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

import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.common.log.wrappers.Logger;

@org.springframework.stereotype.Component
public class ComponentInstanceChangeOperationOrchestrator {

    private static final Logger log = Logger.getLogger(ComponentInstanceChangeOperationOrchestrator.class);
    private final List<OnComponentInstanceChangeOperation> onInstanceChangeOperations;

    public ComponentInstanceChangeOperationOrchestrator(List<OnComponentInstanceChangeOperation> onInstanceChangeOperations) {
        this.onInstanceChangeOperations = onInstanceChangeOperations;
    }

    public ActionStatus doPostChangeVersionOperations(Component container, ComponentInstance prevVersion, ComponentInstance newVersion) {
        log.debug("#doPostChangeVersionOperations - starting post change version operations for component {}. from instance {} to instance {}",
            container.getUniqueId(), prevVersion.getUniqueId(), newVersion.getUniqueId());
        Function<OnComponentInstanceChangeOperation, ActionStatus> instanceChangeTaskRunner = operation -> operation
            .onChangeVersion(container, prevVersion, newVersion);
        return doOnChangeInstanceOperations(instanceChangeTaskRunner);
    }

    public ActionStatus doOnDeleteInstanceOperations(Component container, String deletedInstanceId) {
        log.debug("#doPostChangeVersionOperations - starting on delete instance operations for component {} and instance {}.",
            container.getUniqueId(), deletedInstanceId);
        Function<OnComponentInstanceChangeOperation, ActionStatus> instanceChangeTaskRunner = operation -> operation
            .onDelete(container, deletedInstanceId);
        return doOnChangeInstanceOperations(instanceChangeTaskRunner);
    }

    private ActionStatus doOnChangeInstanceOperations(Function<OnComponentInstanceChangeOperation, ActionStatus> instanceChangeTaskRunner) {
        ActionStatus onDeleteInstanceResult = ActionStatus.OK;
        Iterator<OnComponentInstanceChangeOperation> onDeleteInstIter = onInstanceChangeOperations.iterator();
        while (onDeleteInstIter.hasNext() && onDeleteInstanceResult == ActionStatus.OK) {
            onDeleteInstanceResult = instanceChangeTaskRunner.apply(onDeleteInstIter.next());
        }
        return onDeleteInstanceResult;
    }
}
