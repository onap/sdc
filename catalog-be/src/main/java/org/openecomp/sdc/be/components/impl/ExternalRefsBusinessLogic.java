package org.openecomp.sdc.be.components.impl;

import fj.data.Either;
import org.openecomp.sdc.be.components.validation.AccessValidations;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.GraphPropertyEnum;
import org.openecomp.sdc.be.dto.ExternalRefDTO;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
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

    public Either<String, ActionStatus> addExternalReference(ComponentTypeEnum componentType, String userId, String uuid, String componentInstanceName, String objectType, ExternalRefDTO ref) {
        return this.doAction(componentType, userId, "POST", uuid, componentInstanceName, objectType, ref.getReferenceUUID(), "");
    }

    public Either<String, ActionStatus> deleteExternalReference(ComponentTypeEnum componentType, String userId, String uuid, String componentInstanceName, String objectType, String reference) {
        return this.doAction(componentType, userId, "DELETE", uuid, componentInstanceName, objectType, reference, "");
    }

    public Either<String, ActionStatus> updateExternalReference(ComponentTypeEnum componentType, String userId, String uuid, String componentInstanceName, String objectType, String oldRefValue, String newRefValue) {
        return this.doAction(componentType, userId, "PUT", uuid, componentInstanceName, objectType, oldRefValue, newRefValue);
    }

    private Either<String, ActionStatus> doAction(ComponentTypeEnum componentType, String userId, String action, String uuid, String componentInstanceName, String objectType, String ref1, String ref2){
        Either<Component, StorageOperationStatus> latestServiceByUuid = toscaOperationFacade.getLatestComponentByUuid(uuid, createPropsToMatch(componentType));
        if (latestServiceByUuid == null || latestServiceByUuid.isRight()){
            return Either.right(ActionStatus.RESOURCE_NOT_FOUND);
        }

        //Get Component Unique ID
        Component component = latestServiceByUuid.left().value();
        String uniqueId = component.getUniqueId();

        //Lock Asset
        this.componentLocker.lock(component);
        this.accessValidations.validateUserCanWorkOnComponent(component, userId, action + " EXTERNAL REF");

        Either<String, ActionStatus> opResult = Either.right(ActionStatus.GENERAL_ERROR);
        try {
            switch (action) {
                case "POST":
                    opResult = this.externalReferencesOperation.addExternalReferenceWithCommit(uniqueId, componentInstanceName, objectType, ref1);
                    break;
                case "PUT":
                    opResult = this.externalReferencesOperation.updateExternalReferenceWithCommit(uniqueId, componentInstanceName, objectType, ref1, ref2);
                    break;
                case "DELETE":
                    opResult = this.externalReferencesOperation.deleteExternalReferenceWithCommit(uniqueId, componentInstanceName, objectType, ref1);
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            opResult = Either.right(ActionStatus.GENERAL_ERROR);
            log.error("Failed to execute external ref action:{} on asset:{} component:{} objectType:{}", action, uuid, componentInstanceName, objectType);
            log.error("Cause is:" , e);
        } finally {
            //Unlock Asset
            this.componentLocker.unlock(uniqueId, componentType);
        }
        return opResult;
    }

    private Map<GraphPropertyEnum, Object> createPropsToMatch(ComponentTypeEnum componentType) {
        Map<GraphPropertyEnum, Object> propertiesToMatch = new HashMap<>();
        propertiesToMatch.put(GraphPropertyEnum.COMPONENT_TYPE, componentType.name());
        propertiesToMatch.put(GraphPropertyEnum.STATE, LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
        return propertiesToMatch;
    }

}
