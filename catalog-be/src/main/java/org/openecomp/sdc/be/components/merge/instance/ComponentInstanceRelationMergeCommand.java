/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2020 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.be.components.merge.instance;

import fj.data.Either;
import org.apache.commons.collections.CollectionUtils;
import org.openecomp.sdc.be.components.merge.VspComponentsMergeCommand;
import org.openecomp.sdc.be.components.merge.utils.MergeInstanceUtils;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.RequirementCapabilityRelDef;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.springframework.core.annotation.Order;

import java.util.List;

import static org.openecomp.sdc.be.components.merge.resource.ResourceDataMergeBusinessLogic.LAST_COMMAND;

@org.springframework.stereotype.Component
@Order(LAST_COMMAND)//must run after all merge commands
public class ComponentInstanceRelationMergeCommand implements VspComponentsMergeCommand {

    private final ToscaOperationFacade toscaOperationFacade;
    private final MergeInstanceUtils mergeInstanceUtils;
    private final ComponentsUtils componentsUtils;

    public ComponentInstanceRelationMergeCommand(ToscaOperationFacade toscaOperationFacade, MergeInstanceUtils mergeInstanceUtils, ComponentsUtils componentsUtils) {
        this.toscaOperationFacade = toscaOperationFacade;
        this.mergeInstanceUtils = mergeInstanceUtils;
        this.componentsUtils = componentsUtils;
    }
    @Override
    public ActionStatus mergeComponents(Component prevComponent, Component currentComponent) {
        List<RequirementCapabilityRelDef> updatedUiRelations = mergeInstanceUtils.getUpdatedUiRelations(prevComponent, currentComponent);
        if(CollectionUtils.isNotEmpty(updatedUiRelations)){
            return associateResourceInstances(currentComponent, updatedUiRelations);
        }
        return ActionStatus.OK;
    }

    private ActionStatus associateResourceInstances(Component currentComponent, List<RequirementCapabilityRelDef> updatedUiRelations) {
        Either<List<RequirementCapabilityRelDef>, StorageOperationStatus> listStorageOperationStatusEither = toscaOperationFacade.associateResourceInstances(null, currentComponent.getUniqueId(), updatedUiRelations);
        if (listStorageOperationStatusEither.isLeft()) {
            currentComponent.getComponentInstancesRelations().addAll(updatedUiRelations);
        } else {
            return componentsUtils.convertFromStorageResponse(listStorageOperationStatusEither.right().value());
        }
        return ActionStatus.OK;
    }

    @Override
    public String description() {
        return "merge component instances from old component to new component";
    }
}
