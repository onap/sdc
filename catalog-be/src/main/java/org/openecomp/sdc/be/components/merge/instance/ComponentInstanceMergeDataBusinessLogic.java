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
import java.util.List;
import org.openecomp.sdc.be.components.impl.exceptions.ByActionStatusComponentException;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.ComponentParametersView;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

/**
 * Created by chaya on 9/12/2017.
 */
@org.springframework.stereotype.Component("componentInstanceMergeDataBusinessLogic")
public class ComponentInstanceMergeDataBusinessLogic {

    private static final Logger log = Logger.getLogger(ComponentInstanceMergeDataBusinessLogic.class);
    @Autowired
    @Lazy
    private List<ComponentInstanceMergeInterface> componentInstancesMergeBLs;
    @Autowired
    private ToscaOperationFacade toscaOperationFacade;
    @Autowired
    private ComponentsUtils componentsUtils;

    //for testing only
    protected void setComponentInstancesMergeBLs(List<ComponentInstanceMergeInterface> componentInstancesMergeBLs) {
        this.componentInstancesMergeBLs = componentInstancesMergeBLs;
    }

    /**
     * Saves all containerComponents data before deleting, in order to merge once creating a new instance
     *
     * @param containerComponent
     * @param currentResourceInstance
     */
    public DataForMergeHolder saveAllDataBeforeDeleting(org.openecomp.sdc.be.model.Component containerComponent,
                                                        ComponentInstance currentResourceInstance, Component originComponent) {
        DataForMergeHolder dataHolder = new DataForMergeHolder();
        for (ComponentInstanceMergeInterface compInstMergeBL : componentInstancesMergeBLs) {
            compInstMergeBL.saveDataBeforeMerge(dataHolder, containerComponent, currentResourceInstance, originComponent);
        }
        return dataHolder;
    }

    /**
     * Merges inputs and instance inputs/props of the new Container component with the old container component data (before deleting)
     *
     * @param containerComponent
     * @param newContainerComponentId
     * @param newInstanceId
     * @return
     */
    public Component mergeComponentUserOrigData(User user, DataForMergeHolder dataHolder, org.openecomp.sdc.be.model.Component containerComponent,
                                                String newContainerComponentId, String newInstanceId) {
        Either<Component, StorageOperationStatus> componentWithInstancesInputsAndProperties = getComponentWithInstancesMergeEntities(
            newContainerComponentId);
        if (componentWithInstancesInputsAndProperties.isRight()) {
            log.error("Component with id {} was not found", newContainerComponentId);
            StorageOperationStatus storageOperationStatus = componentWithInstancesInputsAndProperties.right().value();
            ActionStatus actionStatus = componentsUtils.convertFromStorageResponse(storageOperationStatus, containerComponent.getComponentType());
            throw new ByActionStatusComponentException(actionStatus);
        }
        Component updatedContainerComponent = componentWithInstancesInputsAndProperties.left().value();
        componentInstancesMergeBLs.forEach(c -> c.mergeDataAfterCreate(user, dataHolder, updatedContainerComponent, newInstanceId));
        return updatedContainerComponent;
    }

    private Either<Component, StorageOperationStatus> getComponentWithInstancesMergeEntities(String containerComponentId) {
        ComponentParametersView filter = new ComponentParametersView(true);
        filter.setIgnoreComponentInstances(false);
        filter.setIgnoreComponentInstancesInputs(false);
        filter.setIgnoreInputs(false);//dr
        filter.setIgnoreComponentInstancesProperties(false);
        filter.setIgnoreCapabilities(false);
        filter.setIgnoreCapabiltyProperties(false);
        filter.setIgnoreArtifacts(false);
        filter.setIgnoreServicePath(false);
        filter.setIgnoreComponentInstancesInterfaces(false);
        filter.setIgnoreComponentInstancesAttributes(false);
        filter.setIgnoreOutputs(false);
        return toscaOperationFacade.getToscaElement(containerComponentId, filter);
    }
}
