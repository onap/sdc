/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2021, Nordix Foundation. All rights reserved.
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

package org.openecomp.sdc.be.components.attribute;

import static org.apache.commons.collections.MapUtils.isNotEmpty;

import fj.data.Either;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstOutputsMap;
import org.openecomp.sdc.be.model.ComponentInstanceAttribOutput;
import org.openecomp.sdc.be.model.OutputDefinition;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.common.log.wrappers.Logger;

@org.springframework.stereotype.Component("attributeDeclarationOrchestrator")
public class AttributeDeclarationOrchestrator {

    private static final Logger log = Logger.getLogger(AttributeDeclarationOrchestrator.class);
    private final ComponentInstanceOutputAttributeDeclarator componentInstanceOutputAttributeDeclarator;
    private final ComponentInstanceAttributeDeclarator componentInstanceAttributeDeclarator;
    private final List<AttributeDeclarator> attributeDeclaratorsToOutput;

    public AttributeDeclarationOrchestrator(final ComponentInstanceOutputAttributeDeclarator componentInstanceOutputAttributeDeclarator,
                                            final ComponentInstanceAttributeDeclarator componentInstanceAttributeDeclarator) {
        this.componentInstanceOutputAttributeDeclarator = componentInstanceOutputAttributeDeclarator;
        this.componentInstanceAttributeDeclarator = componentInstanceAttributeDeclarator;
        attributeDeclaratorsToOutput = Arrays.asList(componentInstanceOutputAttributeDeclarator, componentInstanceAttributeDeclarator);
    }

    public Either<List<OutputDefinition>, StorageOperationStatus> declareAttributesToOutputs(final Component component,
                                                                                             final ComponentInstOutputsMap componentInstOutputsMap) {
        AttributeDeclarator attributeDeclarator = getAttributeDeclarator(componentInstOutputsMap);
        Pair<String, List<ComponentInstanceAttribOutput>> attributesToDeclare = componentInstOutputsMap.resolveAttributesToDeclare();
        return attributeDeclarator.declareAttributesAsOutputs(component, attributesToDeclare.getLeft(), attributesToDeclare.getRight());
    }

    public StorageOperationStatus unDeclareAttributesAsOutputs(final Component component,
                                                               final OutputDefinition outputToDelete) {
        log.debug("#unDeclareAttributesAsOutputs - removing output declaration for output {} on component {}", outputToDelete.getName(),
            component.getUniqueId());
        for (final AttributeDeclarator attributeDeclarator : attributeDeclaratorsToOutput) {
            final StorageOperationStatus storageOperationStatus = attributeDeclarator.unDeclareAttributesAsOutputs(component, outputToDelete);
            if (StorageOperationStatus.OK != storageOperationStatus) {
                log.debug("#unDeclareAttributesAsOutputs - failed to remove output declaration for output {} on component {}. reason {}",
                    outputToDelete.getName(), component.getUniqueId(), storageOperationStatus);
                return storageOperationStatus;
            }
        }
        return StorageOperationStatus.OK;

    }

    private AttributeDeclarator getAttributeDeclarator(final ComponentInstOutputsMap componentInstOutputsMap) {
        if (isNotEmpty(componentInstOutputsMap.getComponentInstanceOutputsMap())) {
            return componentInstanceOutputAttributeDeclarator;
        }
        if (isNotEmpty(componentInstOutputsMap.getComponentInstanceAttributes())) {
            return componentInstanceAttributeDeclarator;
        }
        throw new IllegalStateException("there are no properties selected for declaration");

    }

}
