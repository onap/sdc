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

import static org.apache.commons.collections4.CollectionUtils.isEmpty;

import fj.data.Either;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.openecomp.sdc.be.components.impl.ComponentInstanceBusinessLogic;
import org.openecomp.sdc.be.datatypes.elements.AttributeDataDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.ComponentInstanceOutput;
import org.openecomp.sdc.be.model.OutputDefinition;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.common.log.wrappers.Logger;

@org.springframework.stereotype.Component
public class ComponentInstanceOutputAttributeDeclarator extends DefaultAttributeDeclarator<ComponentInstance, ComponentInstanceOutput> {

    private static final Logger log = Logger.getLogger(ComponentInstanceOutputAttributeDeclarator.class);
    private final ToscaOperationFacade toscaOperationFacade;
    private final ComponentInstanceBusinessLogic componentInstanceBusinessLogic;

<<<<<<< HEAD   (0d13c9 Update SDC version)
    public ComponentInstanceOutputAttributeDeclarator(final ComponentsUtils componentsUtils,
                                                      final AttributeOperation attributeOperation,
                                                      final ToscaOperationFacade toscaOperationFacade,
=======
    public ComponentInstanceOutputAttributeDeclarator(final ToscaOperationFacade toscaOperationFacade,
>>>>>>> CHANGE (88a3a7 Fix 'Unable to delete declared outputs')
                                                      final ComponentInstanceBusinessLogic componentInstanceBusinessLogic) {
        this.toscaOperationFacade = toscaOperationFacade;
        this.componentInstanceBusinessLogic = componentInstanceBusinessLogic;
    }

    @Override
    public ComponentInstanceOutput createDeclaredAttribute(final AttributeDataDefinition attributeDataDefinition) {
        return new ComponentInstanceOutput(attributeDataDefinition);
    }

    @Override
    public Either<?, StorageOperationStatus> updateAttributesValues(final Component component,
                                                                    final String cmptInstanceId,
                                                                    final List<ComponentInstanceOutput> attributetypeList) {
        log.debug("#updateAttributesValues - updating component instance outputs for instance {} on component {}", cmptInstanceId,
            component.getUniqueId());
        Map<String, List<ComponentInstanceOutput>> instAttributes = Collections.singletonMap(cmptInstanceId, attributetypeList);
        return toscaOperationFacade.addComponentInstanceOutputsToComponent(component, instAttributes);
    }

    @Override
    public Optional<ComponentInstance> resolvePropertiesOwner(final Component component, final String propertiesOwnerId) {
        log.debug("#resolvePropertiesOwner - fetching component instance {} of component {}", propertiesOwnerId, component.getUniqueId());
        return component.getComponentInstanceById(propertiesOwnerId);
    }

    public StorageOperationStatus unDeclareAttributesAsOutputs(final Component component, final OutputDefinition output) {
        List<ComponentInstanceOutput> componentInstanceInputsByInputId = componentInstanceBusinessLogic
            .getComponentInstanceOutputsByOutputId(component, output
                .getUniqueId());
        if (isEmpty(componentInstanceInputsByInputId)) {
            return StorageOperationStatus.OK;
        }
        return toscaOperationFacade.updateComponentInstanceOutputs(component, componentInstanceInputsByInputId.get(0).getComponentInstanceId(),
            componentInstanceInputsByInputId);
    }

}
