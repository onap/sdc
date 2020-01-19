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

package org.openecomp.sdc.be.components.merge.input;

import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.InputDefinition;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.common.log.wrappers.Logger;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static org.apache.commons.collections.CollectionUtils.isEmpty;

public abstract class InputsMergeCommand {

    private static final Logger log = Logger.getLogger(InputsMergeCommand.class);

    private InputsValuesMergingBusinessLogic inputsValuesMergingBusinessLogic;
    private DeclaredInputsResolver declaredInputsResolver;
    private ToscaOperationFacade toscaOperationFacade;
    private ComponentsUtils componentsUtils;

    public InputsMergeCommand(InputsValuesMergingBusinessLogic inputsValuesMergingBusinessLogic, DeclaredInputsResolver declaredInputsResolver, ToscaOperationFacade toscaOperationFacade, ComponentsUtils componentsUtils) {
        this.inputsValuesMergingBusinessLogic = inputsValuesMergingBusinessLogic;
        this.declaredInputsResolver = declaredInputsResolver;
        this.toscaOperationFacade = toscaOperationFacade;
        this.componentsUtils = componentsUtils;
    }

    abstract List<InputDefinition> getInputsToMerge(Component component);

    abstract Map<String, List<PropertyDataDefinition>> getProperties(Component component);

    ActionStatus redeclareAndMergeInputsValues(Component prevComponent, Component currComponent) {
        if (prevComponent == null || isEmpty(prevComponent.getInputs())) {
            return ActionStatus.OK;
        }
        List<InputDefinition> mergedInputs = mergeInputsValues(prevComponent, currComponent);
        List<InputDefinition> previouslyDeclaredInputsToMerge = getUniquePreviouslyDeclaredInputsToMerge(prevComponent, currComponent, mergedInputs);
        mergedInputs.addAll(previouslyDeclaredInputsToMerge);
        return updateInputs(currComponent.getUniqueId(), mergedInputs);
    }



    private List<InputDefinition> mergeInputsValues(Component prevComponent, Component currComponent) {
        log.debug("#mergeInputsValues - merge inputs values from previous component {} to current component {}", prevComponent.getUniqueId(), currComponent.getUniqueId());
        List<InputDefinition> inputsToMerge = getInputsToMerge(currComponent);
        List<InputDefinition> prevInputs = prevComponent.safeGetInputs();
        inputsValuesMergingBusinessLogic.mergeComponentInputs(prevInputs, inputsToMerge);
        return inputsToMerge;
    }

    private List<InputDefinition> getUniquePreviouslyDeclaredInputsToMerge(Component prevComponent, Component currComponent, List<InputDefinition> mergedInputs) {
        List<InputDefinition> previouslyDeclaredInputsToMerge = getPreviouslyDeclaredInputsToMerge(prevComponent, currComponent);
        return previouslyDeclaredInputsToMerge.stream()
                .filter(prev -> mergedInputs.stream()
                        .noneMatch(merged -> merged.getName().equals(prev.getName()))).collect(Collectors.toList());
    }


    private List<InputDefinition> getPreviouslyDeclaredInputsToMerge(Component prevComponent, Component currComponent) {
        log.debug("#getPreviouslyDeclaredInputsToMerge - getting inputs that were previously declared from previous component {} and setting on current component {}", prevComponent.getUniqueId(), currComponent.getUniqueId());
        if (isEmpty(prevComponent.getInputs())) {
            return emptyList();
        }
        Map<String, List<PropertyDataDefinition>> props = getProperties(currComponent);
        return declaredInputsResolver.getPreviouslyDeclaredInputsToMerge(prevComponent, currComponent, props);
    }

    private ActionStatus updateInputs(String containerId, List<InputDefinition> inputsToUpdate) {
        log.debug("#updateInputs - updating inputs for container {}", containerId);
        return toscaOperationFacade.updateInputsToComponent(inputsToUpdate, containerId)
                .either(updatedInputs -> ActionStatus.OK,
                        componentsUtils::convertFromStorageResponse);
    }

}
