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

package org.openecomp.sdc.be.components.merge.instance;

import fj.data.Either;
import org.openecomp.sdc.be.components.impl.exceptions.ByActionStatusComponentException;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.ComponentInstanceInput;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.ComponentParametersView;
import org.openecomp.sdc.be.model.InputDefinition;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.common.log.wrappers.Logger;

import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.collections.CollectionUtils.isNotEmpty;

/**
 * Created by chaya on 9/20/2017.
 */
@org.springframework.stereotype.Component("ComponentInstancePropsAndInputsMerge")
public class ComponentInstancePropsAndInputsMerge implements ComponentInstanceMergeInterface {

    private static final Logger log = Logger.getLogger(ComponentInstancePropsAndInputsMerge.class);

    private final ToscaOperationFacade toscaOperationFacade;
    private final ComponentsUtils componentsUtils;
    private final ComponentInstancePropertiesMergeBL componentInstancePropertiesMergeBL;
    private final ComponentInstanceInputsMergeBL resourceInstanceInputsMergeBL;
    private final ComponentInstanceInputsRedeclareHandler instanceInputsRedeclareHandler;

    public ComponentInstancePropsAndInputsMerge(ToscaOperationFacade toscaOperationFacade, ComponentsUtils componentsUtils, ComponentInstancePropertiesMergeBL componentInstancePropertiesMergeBL, ComponentInstanceInputsMergeBL resourceInstanceInputsMergeBL, ComponentInstanceInputsRedeclareHandler instanceInputsRedeclareHandler) {
        this.toscaOperationFacade = toscaOperationFacade;
        this.componentsUtils = componentsUtils;
        this.componentInstancePropertiesMergeBL = componentInstancePropertiesMergeBL;
        this.resourceInstanceInputsMergeBL = resourceInstanceInputsMergeBL;
        this.instanceInputsRedeclareHandler = instanceInputsRedeclareHandler;
    }

    @Override
    public void saveDataBeforeMerge(DataForMergeHolder dataHolder, Component containerComponent, ComponentInstance currentResourceInstance, Component originComponent) {
        dataHolder.setOrigComponentInstanceInputs(containerComponent.safeGetComponentInstanceInputsByName(currentResourceInstance.getName()));
        dataHolder.setOrigComponentInstanceProperties(containerComponent.safeGetComponentInstanceProperties(currentResourceInstance.getUniqueId()));
        dataHolder.setOrigComponentInputs(containerComponent.getInputs());
    }

    @Override
    public Component mergeDataAfterCreate(User user, DataForMergeHolder dataHolder, Component updatedContainerComponent, String newInstanceId) {
        Either<List<ComponentInstanceInput>, ActionStatus> instanceInputsEither = mergeComponentInstanceInputsIntoContainer(dataHolder, updatedContainerComponent, newInstanceId);
        if (instanceInputsEither.isRight()) {
            ActionStatus actionStatus = instanceInputsEither.right().value();
            throw new ByActionStatusComponentException(actionStatus);
        }
        Either<List<ComponentInstanceProperty>, ActionStatus> instancePropsEither = mergeComponentInstancePropsIntoContainer(dataHolder, updatedContainerComponent, newInstanceId);
        if (instancePropsEither.isRight()) {
            ActionStatus actionStatus = instancePropsEither.right().value();
            throw new ByActionStatusComponentException(actionStatus);
        }
        Either<List<InputDefinition>, ActionStatus> inputsEither = mergeComponentInputsIntoContainer(dataHolder, updatedContainerComponent.getUniqueId(), newInstanceId);
        if (inputsEither.isRight()) {
            ActionStatus actionStatus = inputsEither.right().value();
            throw new ByActionStatusComponentException(actionStatus);
        }
        return updatedContainerComponent;
    }

    private Either<List<ComponentInstanceProperty>, ActionStatus> mergeComponentInstancePropsIntoContainer(DataForMergeHolder dataHolder, Component updatedComponent, String instanceId) {
        List<ComponentInstanceProperty> originComponentInstanceProps = dataHolder.getOrigComponentInstanceProperties();
        List<InputDefinition> originComponentsInputs = dataHolder.getOrigComponentInputs();
        List<ComponentInstanceProperty> newComponentInstancesProps = updatedComponent.safeGetComponentInstanceProperties(instanceId);
        ActionStatus actionStatus = componentInstancePropertiesMergeBL.mergeComponentInstanceProperties(originComponentInstanceProps, originComponentsInputs, updatedComponent, instanceId);

        if (actionStatus != ActionStatus.OK) {
            log.error("Failed to update component {} with merged instance properties", updatedComponent.getUniqueId(), newComponentInstancesProps);
            return Either.right(actionStatus);
        }
        return Either.left(newComponentInstancesProps);
    }

    private Either<List<ComponentInstanceInput>, ActionStatus> mergeComponentInstanceInputsIntoContainer(DataForMergeHolder dataHolder, Component updatedComponent, String instanceId) {
        List<ComponentInstanceInput> originComponentInstanceInputs = dataHolder.getOrigComponentInstanceInputs();
        List<InputDefinition> originComponentsInputs = dataHolder.getOrigComponentInputs();
        List<ComponentInstanceInput> newComponentInstancesInputs = updatedComponent.safeGetComponentInstanceInput(instanceId);
        ActionStatus actionStatus = resourceInstanceInputsMergeBL.mergeComponentInstanceInputs(originComponentInstanceInputs, originComponentsInputs, updatedComponent, instanceId);
        if (actionStatus != ActionStatus.OK) {
            log.error("Failed to update component {} with merged instance properties", updatedComponent.getUniqueId(), newComponentInstancesInputs);
            return Either.right(actionStatus);
        }
        return Either.left(newComponentInstancesInputs);
    }

    private Either<List<InputDefinition>, ActionStatus> mergeComponentInputsIntoContainer(DataForMergeHolder dataHolder, String newContainerComponentId, String newInstanceId) {
        List<InputDefinition> origComponentInputs = dataHolder.getOrigComponentInputs();
        List<InputDefinition> inputsToAddToContainer = new ArrayList<>();
        if (isNotEmpty(origComponentInputs)) {
            // get  instance inputs and properties after merge
            Either<Component, StorageOperationStatus> componentWithInstancesInputsAndProperties = getComponentWithInstancesInputsAndProperties(newContainerComponentId);
            if (componentWithInstancesInputsAndProperties.isRight()) {
                log.error("Component %s was not found", newContainerComponentId);
                return Either.right(componentsUtils.convertFromStorageResponse(componentWithInstancesInputsAndProperties.right().value()));
            }
            Component updatedContainerComponent = componentWithInstancesInputsAndProperties.left().value();
            Component currInstanceOriginType = dataHolder.getCurrInstanceNode();
            ActionStatus redeclareStatus = instanceInputsRedeclareHandler.redeclareComponentInputsForInstance(updatedContainerComponent, newInstanceId, currInstanceOriginType, origComponentInputs);
            if (redeclareStatus != ActionStatus.OK) {
                log.error("Failed to update component {} with merged inputs {}", newContainerComponentId, inputsToAddToContainer);
                return Either.right(redeclareStatus);
            }
        }
        return Either.left(inputsToAddToContainer);
    }

    private Either<Component, StorageOperationStatus> getComponentWithInstancesInputsAndProperties(String containerComponentId) {
        ComponentParametersView filter = new ComponentParametersView(true);
        filter.setIgnoreComponentInstances(false);
        filter.setIgnoreComponentInstancesInputs(false);
        filter.setIgnoreComponentInstancesProperties(false);
        filter.setIgnoreArtifacts(false);
        return toscaOperationFacade.getToscaElement(containerComponentId, filter);
    }
}
