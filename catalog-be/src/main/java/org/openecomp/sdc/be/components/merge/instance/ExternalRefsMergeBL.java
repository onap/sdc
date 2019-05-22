/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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
 * Modifications copyright (c) 2019 Nokia
 * ================================================================================
 */
package org.openecomp.sdc.be.components.merge.instance;

import fj.data.Either;
import org.openecomp.sdc.be.components.impl.exceptions.ByActionStatusComponentException;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.jsontitan.operations.ExternalReferencesOperation;
import org.openecomp.sdc.exception.ResponseFormat;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.apache.commons.collections.MapUtils.isEmpty;

@org.springframework.stereotype.Component
public class ExternalRefsMergeBL implements ComponentInstanceMergeInterface {

    private final ExternalReferencesOperation externalReferencesOperation;

    ExternalRefsMergeBL(ExternalReferencesOperation externalReferencesOperation) {
        this.externalReferencesOperation = externalReferencesOperation;
    }

    @Override
    public void saveDataBeforeMerge(DataForMergeHolder dataHolder, Component containerComponent,
                                    ComponentInstance currentResourceInstance, Component originComponent) {
        Map<String, List<String>> externalRefs = externalReferencesOperation.getAllExternalReferences(containerComponent.getUniqueId(),
                currentResourceInstance.getUniqueId());
        dataHolder.setOrigComponentInstanceExternalRefs(externalRefs);
    }

    @Override
    public Either<Component, ResponseFormat> mergeDataAfterCreate(User user, DataForMergeHolder dataHolder, Component updatedContainerComponent, String newInstanceId) {
        Optional<ComponentInstance> componentInstance = updatedContainerComponent.getComponentInstanceById(newInstanceId);
        if (!componentInstance.isPresent()) {
            throw new ByActionStatusComponentException(ActionStatus.COMPONENT_INSTANCE_NOT_FOUND,
                    newInstanceId);
        }
        Map<String, List<String>>  savedExternalRefs = dataHolder.getOrigCompInstExternalRefs();
        if (!isEmpty(savedExternalRefs)) {
            externalReferencesOperation.addAllExternalReferences(updatedContainerComponent.getUniqueId(),
                    componentInstance.get().getUniqueId(), savedExternalRefs);
        }
        return Either.left(updatedContainerComponent);
    }
}
