/*
* ============LICENSE_START=======================================================
* SDC
* ================================================================================
* Copyright (C) 2022 Nordix Foundation. All rights reserved.
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
import java.util.List;
import java.util.ArrayList;
import org.openecomp.sdc.be.components.impl.exceptions.ByActionStatusComponentException;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.ComponentInstanceAttribute;
import org.openecomp.sdc.be.model.ComponentParametersView;
import org.openecomp.sdc.be.model.OutputDefinition;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.common.log.enums.EcompLoggerErrorCode;
import org.openecomp.sdc.common.log.wrappers.Logger;



import static org.apache.commons.collections.CollectionUtils.isNotEmpty;

@org.springframework.stereotype.Component("ComponentInstanceAttributesMerge")
public class ComponentInstanceAttributesAndOutputsMerge implements ComponentInstanceMergeInterface {

    private static final Logger log = Logger.getLogger(ComponentInstanceAttributesAndOutputsMerge.class);
    private final ToscaOperationFacade toscaOperationFacade;
    private final ComponentsUtils componentsUtils;
    private final ComponentInstanceAttributesMergeBL componentInstanceAttributesMergeBL;
    private final ComponentInstanceOutputsRedeclareHandler instanceOutputsRedeclareHandler;

    public ComponentInstanceAttributesAndOutputsMerge(ToscaOperationFacade toscaOperationFacade, ComponentsUtils componentsUtils,
                                                      ComponentInstanceAttributesMergeBL componentInstanceAttributesMergeBL,
                                                      ComponentInstanceOutputsRedeclareHandler instanceOutputsRedeclareHandler) {
        this.toscaOperationFacade = toscaOperationFacade;
        this.componentsUtils = componentsUtils;
        this.componentInstanceAttributesMergeBL = componentInstanceAttributesMergeBL;
        this.instanceOutputsRedeclareHandler = instanceOutputsRedeclareHandler;
    }


    @Override
    public void saveDataBeforeMerge(DataForMergeHolder dataHolder, Component containerComponent, ComponentInstance currentResourceInstance,
                                    Component originComponent) {
        dataHolder.setOrigComponentInstanceAttributes(containerComponent.safeGetComponentInstancesAttributes()
            .get(currentResourceInstance.getUniqueId()));
        dataHolder.setOrigComponentOutputs(containerComponent.getOutputs());
    }


    @Override
    public Component mergeDataAfterCreate(User user, DataForMergeHolder dataHolder, Component updatedContainerComponent, String newInstanceId) {
        Either<List<ComponentInstanceAttribute>, ActionStatus> attributesEither = mergeComponentInstanceAttributesIntoContainer(dataHolder,
            updatedContainerComponent, newInstanceId);
        if (attributesEither.isRight()) {
            ActionStatus actionStatus = attributesEither.right().value();
            throw new ByActionStatusComponentException(actionStatus);
        }
        Either<List<OutputDefinition>, ActionStatus> outputsEither = mergeComponentOutputsIntoContainer(dataHolder,
                updatedContainerComponent.getUniqueId(), newInstanceId);
        if (outputsEither.isRight()) {
            ActionStatus actionStatus = outputsEither.right().value();
            throw new ByActionStatusComponentException(actionStatus);
        }
        return updatedContainerComponent;
    }

    private Either<List<ComponentInstanceAttribute>, ActionStatus> mergeComponentInstanceAttributesIntoContainer(DataForMergeHolder dataHolder,
                                                                                                                 Component updatedComponent,
                                                                                                                 String instanceId) {
        List<ComponentInstanceAttribute> originComponentInstanceAttributes = dataHolder.getOrigComponentInstanceAttributes();
        List<ComponentInstanceAttribute> newComponentInstancesAttributes = updatedComponent.safeGetComponentInstanceAttributes(instanceId);
        ActionStatus actionStatus = componentInstanceAttributesMergeBL
            .mergeComponentInstanceAttributes(originComponentInstanceAttributes, updatedComponent, instanceId);
        if (actionStatus != ActionStatus.OK) {
            log.error(EcompLoggerErrorCode.BUSINESS_PROCESS_ERROR, updatedComponent.getName(),
                "Failed to update component " + updatedComponent.getName() + " " + instanceId
                    + " with instance attributes " + newComponentInstancesAttributes);
            return Either.right(actionStatus);
        }
        return Either.left(newComponentInstancesAttributes);
    }

    private Either<List<OutputDefinition>, ActionStatus> mergeComponentOutputsIntoContainer(DataForMergeHolder dataHolder,
                                                                                            String newContainerComponentId, String newInstanceId) {
        List<OutputDefinition> origComponentOutputs = dataHolder.getOrigComponentOutputs();
        List<OutputDefinition> outputsToAddToContainer = new ArrayList<>();
        if (isNotEmpty(origComponentOutputs)) {
            Either<Component, StorageOperationStatus> componentWithInstancesAttributesAndOutputs = getComponentOutputs(newContainerComponentId);
            if (componentWithInstancesAttributesAndOutputs.isRight()) {
                log.error(EcompLoggerErrorCode.DATA_ERROR, newContainerComponentId, "Component " + newContainerComponentId
                        + " was not found");
                return Either.right(componentsUtils.convertFromStorageResponse(componentWithInstancesAttributesAndOutputs.right().value()));
            }
            Component updatedContainerComponent = componentWithInstancesAttributesAndOutputs.left().value();
            ActionStatus redeclareStatus = instanceOutputsRedeclareHandler
                .redeclareComponentOutputsForInstance(updatedContainerComponent, newInstanceId, origComponentOutputs);

            if (redeclareStatus != ActionStatus.OK) {
                log.error(EcompLoggerErrorCode.BUSINESS_PROCESS_ERROR, updatedContainerComponent.getName(),
                    "Failed to update component " + updatedContainerComponent.getName() + " " + newContainerComponentId
                        + " with merged inputs " + outputsToAddToContainer);
                return Either.right(redeclareStatus);
            }
        }
        return Either.left(outputsToAddToContainer);
    }

    private Either<Component, StorageOperationStatus> getComponentOutputs(String containerComponentId) {
        ComponentParametersView filter = new ComponentParametersView(true);
        filter.setIgnoreComponentInstances(false);
        filter.setIgnoreOutputs(false);
        filter.setIgnoreComponentInstancesAttributes(false);
        filter.setIgnoreArtifacts(false);
        return toscaOperationFacade.getToscaElement(containerComponentId, filter);
    }
}
