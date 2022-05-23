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

import static java.util.stream.Collectors.toList;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.openecomp.sdc.be.components.merge.output.DeclaredOutputsResolver;
import org.openecomp.sdc.be.components.merge.output.OutputsValuesMergingBusinessLogic;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.elements.AttributeDataDefinition;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.OutputDefinition;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.common.log.wrappers.Logger;

@org.springframework.stereotype.Component
public class ComponentInstanceOutputsRedeclareHandler {

    private static final Logger log = Logger.getLogger(ComponentInstanceOutputsRedeclareHandler.class);
    private final DeclaredOutputsResolver declaredOutputsResolver;
    private final ToscaOperationFacade toscaOperationFacade;
    private final ComponentsUtils componentsUtils;
    private final OutputsValuesMergingBusinessLogic outputsValuesMergingBusinessLogic;

    public ComponentInstanceOutputsRedeclareHandler(DeclaredOutputsResolver declaredOutputsResolver, ToscaOperationFacade toscaOperationFacade,
                                                    ComponentsUtils componentsUtils,
                                                    OutputsValuesMergingBusinessLogic outputsValuesMergingBusinessLogic) {
        this.declaredOutputsResolver = declaredOutputsResolver;
        this.toscaOperationFacade = toscaOperationFacade;
        this.componentsUtils = componentsUtils;
        this.outputsValuesMergingBusinessLogic = outputsValuesMergingBusinessLogic;
    }

    public ActionStatus redeclareComponentOutputsForInstance(Component container, String newInstanceId, List<OutputDefinition> oldOutputs) {
        log.debug(
            "#redeclareComponentOutputsForInstance - getting outputs that were previously declared from instance {} and setting on current component {}",
            newInstanceId, container.getUniqueId());
        List<AttributeDataDefinition> allAttributesForInstance = getAllGetAttributesForInstance(container, newInstanceId);
        List<OutputDefinition> previouslyDeclaredOutputs = declaredOutputsResolver
            .getPreviouslyDeclaredOutputsToMerge(oldOutputs, allAttributesForInstance, newInstanceId);
        outputsValuesMergingBusinessLogic.mergeComponentOutputs(oldOutputs, previouslyDeclaredOutputs);
        return updateOutputs(container.getUniqueId(), previouslyDeclaredOutputs);
    }

    private List<AttributeDataDefinition> getAllGetAttributesForInstance(Component newComponent, String instanceId) {
        return Stream
            .of(newComponent.safeGetComponentInstanceAttributes(instanceId))
            .flatMap(Collection::stream).map(AttributeDataDefinition::new).collect(toList());
    }

    private ActionStatus updateOutputs(String containerId, List<OutputDefinition> outputsToUpdate) {
        log.debug("#updateInputs - updating inputs for container {}", containerId);
        return toscaOperationFacade.updateOutputsToComponent(outputsToUpdate, containerId)
            .either(updatedInputs -> ActionStatus.OK, componentsUtils::convertFromStorageResponse);
    }
}
