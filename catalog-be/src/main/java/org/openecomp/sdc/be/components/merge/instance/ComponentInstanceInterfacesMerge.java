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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
import org.openecomp.sdc.be.model.Operation;
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
        ActionStatus mergeStatus = mergeComponentInstanceInterfaces(updatedContainerComponent, newInstanceId, origInstanceInterfaces,
            dataHolder.getOrigInstanceNode());
        if (!ActionStatus.OK.equals(mergeStatus)) {
            throw new ByResponseFormatComponentException(componentsUtils.getResponseFormat(mergeStatus));
        } else {
            return updatedContainerComponent;
        }
    }

    private ActionStatus mergeComponentInstanceInterfaces(Component currentComponent, String instanceId,
                                                          List<ComponentInstanceInterface> prevInstanceInterfaces,
                                                          Component prevInstanceOrigComponent) {
        if (CollectionUtils.isEmpty(prevInstanceInterfaces) || MapUtils.isEmpty(currentComponent.getComponentInstancesInterfaces())) {
            return ActionStatus.OK;
        }
        if (CollectionUtils.isEmpty(currentComponent.getComponentInstancesInterfaces().get(instanceId))) {
            return ActionStatus.OK;
        }
        currentComponent.getComponentInstancesInterfaces().get(instanceId).forEach(
            newInterfaceDef -> {
                Map<String, Operation> newInterfaceDefOperationMap = new HashMap<>();
                newInterfaceDef.getOperationsMap().forEach(
                    (newOperationDefKey, newOperationDefKeyValue) -> prevInstanceInterfaces.stream()
                        .filter(in -> in.getUniqueId().equals(newInterfaceDef.getUniqueId())).forEach(
                            prevInterfaceDef -> prevInterfaceDef.getOperationsMap().values().stream()
                            .filter(in1 -> in1.getUniqueId().equals(newOperationDefKeyValue.getUniqueId()))
                            .forEach(oldOperationDef -> {
                                Operation originalOperationDef = prevInstanceOrigComponent.getInterfaces().get(newInterfaceDef.getInterfaceId())
                                        .getOperationsMap().get(newOperationDefKey);
                                if (oldOperationDef.getInputs() != null) {
                                    if (newOperationDefKeyValue.getInputs() == null) {
                                        newOperationDefKeyValue.setInputs(new ListDataDefinition<>());
                                    }
                                    mergeOperationInputDefinitions(oldOperationDef.getInputs(), newOperationDefKeyValue.getInputs(),
                                        originalOperationDef.getInputs());
                                }
                                if (originalValueOverwritten(originalOperationDef.getImplementation(), oldOperationDef.getImplementation()) ) {
                                    newOperationDefKeyValue.setImplementation(oldOperationDef.getImplementation());
                                }
                                if (originalValueOverwritten(originalOperationDef.getDescription(), oldOperationDef.getDescription())) {
                                    newOperationDefKeyValue.setDescription(oldOperationDef.getDescription());
                                }
                                newInterfaceDefOperationMap.put(newOperationDefKey, newOperationDefKeyValue);
                            })));
                newInterfaceDef.setOperationsMap(newInterfaceDefOperationMap);
            });
        StorageOperationStatus updateStatus = toscaOperationFacade.updateComponentInstanceInterfaces(currentComponent, instanceId);
        return componentsUtils.convertFromStorageResponse(updateStatus);
    }
    
    private <T> boolean originalValueOverwritten(final T originalValue, final T oldValue) {
        if (originalValue == null) {
            return oldValue != null;
        }
        return !originalValue.equals(oldValue);
    }

    private void mergeOperationInputDefinitions(ListDataDefinition<OperationInputDefinition> oldInputs,
                                                ListDataDefinition<OperationInputDefinition> newInputs,
                                                ListDataDefinition<OperationInputDefinition> origInputs) {
        newInputs.getListToscaDataDefinition()
            .forEach(newInput -> oldInputs.getListToscaDataDefinition().stream().filter(oldInput -> oldInput.getName().equals(newInput.getName()))
                .forEach(oldInput -> {
                    Optional<OperationInputDefinition> origInput =
                        origInputs.getListToscaDataDefinition().stream().filter(input -> input.getName().equals(oldInput.getName()))
                            .findFirst();
                    newInput.setSourceProperty(oldInput.getSourceProperty());
                    newInput.setSource(oldInput.getSource());
                    if (origInput.isPresent() && !origInput.get().getValue().equals(oldInput.getValue())) {
                        newInput.setValue(oldInput.getValue());
                    }
                }));
        oldInputs.getListToscaDataDefinition().stream()
        .filter(oldInput -> newInputs.getListToscaDataDefinition().stream().noneMatch(newInput -> newInput.getName().equals(oldInput.getName())))
        .forEach(oldInput -> newInputs.getListToscaDataDefinition().add(oldInput));
    }
}
