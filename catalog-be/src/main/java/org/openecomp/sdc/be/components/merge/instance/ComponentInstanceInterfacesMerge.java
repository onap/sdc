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

import java.util.List;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.openecomp.sdc.be.components.impl.exceptions.ByResponseFormatComponentException;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.elements.ListDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.OperationInputDefinition;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.ComponentInstanceInterface;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.springframework.beans.factory.annotation.Autowired;

@org.springframework.stereotype.Component("ComponentInstanceInterfacesMerge")
public class ComponentInstanceInterfacesMerge implements ComponentInstanceMergeInterface {

    @Autowired
    private ComponentsUtils componentsUtils;
    @Autowired
    private ToscaOperationFacade toscaOperationFacade;

    @Override
    public void saveDataBeforeMerge(DataForMergeHolder dataHolder, Component containerComponent, ComponentInstance currentResourceInstance,
                                    Component originComponent) {
        dataHolder.setOrigInstanceNode(originComponent);
        dataHolder.setOrigComponentInstanceInterfaces(containerComponent.safeGetComponentInstanceInterfaces(currentResourceInstance.getUniqueId()));
    }

    @Override
    public Component mergeDataAfterCreate(User user, DataForMergeHolder dataHolder, Component updatedContainerComponent, String newInstanceId) {
        List<ComponentInstanceInterface> origInstanceInterfaces = dataHolder.getOrigComponentInstanceInterfaces();
        ActionStatus mergeStatus = mergeComponentInstanceInterfaces(updatedContainerComponent, newInstanceId, origInstanceInterfaces);
        if (!ActionStatus.OK.equals(mergeStatus)) {
            throw new ByResponseFormatComponentException(componentsUtils.getResponseFormat(mergeStatus));
        } else {
            return updatedContainerComponent;
        }
    }

    private ActionStatus mergeComponentInstanceInterfaces(Component currentComponent, String instanceId,
                                                          List<ComponentInstanceInterface> prevInstanceInterfaces) {
        if (CollectionUtils.isEmpty(prevInstanceInterfaces) || MapUtils.isEmpty(currentComponent.getComponentInstancesInterfaces())) {
            return ActionStatus.OK;
        }
        if (CollectionUtils.isEmpty(currentComponent.getComponentInstancesInterfaces().get(instanceId))) {
            return ActionStatus.OK;
        }
        currentComponent.getComponentInstancesInterfaces().get(instanceId).stream().forEach(
            newInterfaceDef -> newInterfaceDef.getOperationsMap().values().forEach(
                newOperationDef -> prevInstanceInterfaces.stream().filter(in -> in.getUniqueId().equals(newInterfaceDef.getUniqueId())).forEach(
                    prevInterfaceDef -> prevInterfaceDef.getOperationsMap().values().stream()
                        .filter(in1 -> in1.getUniqueId().equals(newOperationDef.getUniqueId()))
                        .forEach(oldOperationDef -> {
                            newOperationDef.setDescription(oldOperationDef.getDescription());
                            newOperationDef.setImplementation(oldOperationDef.getImplementation());
                            if(oldOperationDef.getInputs() != null) {
                                if(newOperationDef.getInputs() == null) {
                                    newOperationDef.setInputs(new ListDataDefinition<>());
                                }
                                mergeOperationInputDefinitions(oldOperationDef.getInputs(), newOperationDef.getInputs());
                            }
                        }))));
        StorageOperationStatus updateStatus = toscaOperationFacade.updateComponentInstanceInterfaces(currentComponent, instanceId);
        return componentsUtils.convertFromStorageResponse(updateStatus);
    }

    private void mergeOperationInputDefinitions(ListDataDefinition<OperationInputDefinition> origInputs,
                                                ListDataDefinition<OperationInputDefinition> newInputs) {
            newInputs.getListToscaDataDefinition().
                forEach(inp -> origInputs.getListToscaDataDefinition().stream().filter(in -> in.getInputId().equals(inp.getInputId())).
                    forEach(in -> {
                        inp.setSourceProperty(in.getSourceProperty());
                        inp.setSource(in.getSource());
                        inp.setValue(in.getValue());
                    }));
            origInputs.getListToscaDataDefinition().stream().
                    filter(inp -> newInputs.getListToscaDataDefinition().stream().noneMatch(in -> in.getInputId().equals(inp.getInputId()))).
                    forEach(inp -> newInputs.getListToscaDataDefinition().add(inp));
    }
}
