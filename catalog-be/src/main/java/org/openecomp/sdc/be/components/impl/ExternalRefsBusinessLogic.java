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

package org.openecomp.sdc.be.components.impl;

import fj.data.Either;
import org.openecomp.sdc.be.components.impl.exceptions.ByActionStatusComponentException;
import org.openecomp.sdc.be.components.impl.lock.LockingTransactional;
import org.openecomp.sdc.be.components.validation.AccessValidations;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.GraphPropertyEnum;
import org.openecomp.sdc.be.dto.ExternalRefDTO;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ExternalReferencesOperation;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.common.log.wrappers.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by yavivi on 04/02/2018.
 */
@org.springframework.stereotype.Component
public class ExternalRefsBusinessLogic {

    private static final Logger log = Logger.getLogger(ExternalRefsBusinessLogic.class);

    private final ExternalReferencesOperation externalReferencesOperation;
    private final ToscaOperationFacade toscaOperationFacade;
    private final AccessValidations accessValidations;
    private final ComponentLocker componentLocker;

    public ExternalRefsBusinessLogic(ExternalReferencesOperation externalReferencesOperation, ToscaOperationFacade toscaOperationFacade, AccessValidations accessValidations, ComponentLocker componentLocker) {
        this.externalReferencesOperation = externalReferencesOperation;
        this.toscaOperationFacade = toscaOperationFacade;
        this.accessValidations = accessValidations;
        this.componentLocker = componentLocker;
    }

    public Either<List<String>, ActionStatus> getExternalReferences(String assetUuid, String version, String componentInstanceName, String objectType){
        Either<Component, StorageOperationStatus> componentsResult = toscaOperationFacade.getComponentByUuidAndVersion(assetUuid, version);
        if (componentsResult == null || componentsResult.isRight()) {
            return Either.right(ActionStatus.RESOURCE_NOT_FOUND);
        }
        Component component = componentsResult.left().value();
        return this.externalReferencesOperation.getExternalReferences(component.getUniqueId(), componentInstanceName, objectType);
    }

    public Either<Map<String, List<String>>, ActionStatus> getExternalReferences(String assetUuid, String version, String objectType){
        Either<Component, StorageOperationStatus> componentsResult = toscaOperationFacade.getComponentByUuidAndVersion(assetUuid, version);
        if (componentsResult == null || componentsResult.isRight()) {
            return Either.right(ActionStatus.RESOURCE_NOT_FOUND);
        }

        Component component = componentsResult.left().value();

        Either<Map<String, List<String>>, ActionStatus> externalReferencesResult = this.externalReferencesOperation.getExternalReferences(component.getUniqueId(), objectType);
        if (externalReferencesResult.isRight()){
            return Either.right(externalReferencesResult.right().value());
        } else {
            return Either.left(externalReferencesResult.left().value());
        }
    }

    @LockingTransactional
    public Either<String, ActionStatus> addExternalReference(String componentId, ComponentTypeEnum componentType, String userId, String componentInstanceName, String objectType, ExternalRefDTO ref) {
        return this.doAction(componentId, componentType, userId, "POST", componentId, componentInstanceName, objectType, ref.getReferenceUUID(), "");
    }

    @LockingTransactional
    public Either<String, ActionStatus> deleteExternalReference(String componentId, ComponentTypeEnum componentType, String userId, String componentInstanceName, String objectType, String reference) {
        return this.doAction(componentId, componentType, userId, "DELETE", componentId, componentInstanceName, objectType, reference, "");
    }

    @LockingTransactional
    public Either<String, ActionStatus> updateExternalReference(String componentId, ComponentTypeEnum componentType, String userId, String componentInstanceName, String objectType, String oldRefValue, String newRefValue) {
        return this.doAction(componentId, componentType, userId, "PUT", componentId, componentInstanceName, objectType, oldRefValue, newRefValue);
    }

    public String fetchComponentUniqueIdByUuid(String uuid, ComponentTypeEnum componentType){
        Either<Component, StorageOperationStatus> latestServiceByUuid = toscaOperationFacade.getLatestComponentByUuid(uuid, createPropsToMatch(componentType));
        if (latestServiceByUuid == null || latestServiceByUuid.isRight()){
            throw new ByActionStatusComponentException(ActionStatus.RESOURCE_NOT_FOUND, uuid);
        }

        //Get Component Unique ID
        Component component = latestServiceByUuid.left().value();
        return component.getUniqueId();
    }


    public Either<String, ActionStatus> doAction(String componentId, ComponentTypeEnum componentType, String userId, String action, String uuid, String componentInstanceName, String objectType, String ref1, String ref2){

        accessValidations.validateUserCanWorkOnComponent(componentId, componentType, userId, action + " EXTERNAL REF");

        switch (action) {
            case "POST":
                return this.externalReferencesOperation.addExternalReferenceWithCommit(componentId, componentInstanceName, objectType, ref1);
            case "PUT":
                return this.externalReferencesOperation.updateExternalReferenceWithCommit(componentId, componentInstanceName, objectType, ref1, ref2);
            case "DELETE":
                return this.externalReferencesOperation.deleteExternalReferenceWithCommit(componentId, componentInstanceName, objectType, ref1);
            default:
                return Either.right(ActionStatus.GENERAL_ERROR);
        }

    }

    private Map<GraphPropertyEnum, Object> createPropsToMatch(ComponentTypeEnum componentType) {
        Map<GraphPropertyEnum, Object> propertiesToMatch = new HashMap<>();
        propertiesToMatch.put(GraphPropertyEnum.COMPONENT_TYPE, componentType.name());
        return propertiesToMatch;
    }

}
