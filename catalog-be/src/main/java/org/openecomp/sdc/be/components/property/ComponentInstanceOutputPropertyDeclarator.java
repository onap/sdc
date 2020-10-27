/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2020 Nordix Foundation
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================
 */

package org.openecomp.sdc.be.components.property;

import static org.apache.commons.collections.CollectionUtils.isEmpty;
import static org.openecomp.sdc.be.model.utils.ComponentUtilities.getOutputAnnotations;

import fj.data.Either;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.openecomp.sdc.be.components.impl.ComponentInstanceBusinessLogic;
import org.openecomp.sdc.be.components.impl.utils.ExceptionUtils;
import org.openecomp.sdc.be.datatypes.elements.Annotation;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.ComponentInstanceOutput;
import org.openecomp.sdc.be.model.ComponentInstancePropOutput;
import org.openecomp.sdc.be.model.ComponentParametersView;
import org.openecomp.sdc.be.model.InputDefinition;
import org.openecomp.sdc.be.model.OutputDefinition;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.PropertyOperation;
import org.openecomp.sdc.common.log.wrappers.Logger;

@org.springframework.stereotype.Component
public class ComponentInstanceOutputPropertyDeclarator extends
    DefaultPropertyDeclarator<ComponentInstance, ComponentInstanceOutput> {

    private static final Logger log = Logger.getLogger(ComponentInstanceOutputPropertyDeclarator.class);
    private final ToscaOperationFacade toscaOperationFacade;
    private final ComponentInstanceBusinessLogic componentInstanceBusinessLogic;
    private final ExceptionUtils exceptionUtils;

    public ComponentInstanceOutputPropertyDeclarator(final ComponentsUtils componentsUtils,
                                                     final PropertyOperation propertyOperation,
                                                     final ToscaOperationFacade toscaOperationFacade,
                                                     final ComponentInstanceBusinessLogic componentInstanceBusinessLogic,
                                                     final ExceptionUtils exceptionUtils) {
        super(componentsUtils, propertyOperation);
        this.toscaOperationFacade = toscaOperationFacade;
        this.componentInstanceBusinessLogic = componentInstanceBusinessLogic;
        this.exceptionUtils = exceptionUtils;
    }

    @Override
    public ComponentInstanceOutput createDeclaredProperty(final PropertyDataDefinition propertyDataDefinition) {
        return new ComponentInstanceOutput(propertyDataDefinition);
    }

    @Override
    public Either<?, StorageOperationStatus> updatePropertiesValues(final Component component,
                                                                    final String componentInstanceId,
                                                                    final List<ComponentInstanceOutput> properties) {
        log.debug("#updatePropertiesValues - updating component instance outputs for instance {} on component {}",
            componentInstanceId, component.getUniqueId());
        final Map<String, List<ComponentInstanceOutput>> instProperties = Collections
            .singletonMap(componentInstanceId, properties);
        return toscaOperationFacade.addComponentInstanceOutputsToComponent(component, instProperties);
    }

    @Override
    public Optional<ComponentInstance> resolvePropertiesOwner(final Component component,
                                                              final String propertiesOwnerId) {
        log.debug("#resolvePropertiesOwner - fetching component instance {} of component {}", propertiesOwnerId,
            component.getUniqueId());
        return component.getComponentInstanceById(propertiesOwnerId);
    }

    @Override
    OutputDefinition createOutputFromProperty(final String componentId, final ComponentInstance propertiesOwner,
                                              final String generateOutputName, final List<String> outputName,
                                              final ComponentInstancePropOutput componentInstancePropOutput,
                                              final PropertyDataDefinition propertyDataDefinition) {
        final OutputDefinition outputFromProperty = super
            .createOutputFromProperty(componentId, propertiesOwner, generateOutputName, outputName,
                componentInstancePropOutput, propertyDataDefinition);
        final Component propertiesOwnerNodeType = getInstanceOriginType(propertiesOwner);
        enrichOutputWithAnnotations(propertyDataDefinition, outputFromProperty, propertiesOwnerNodeType);
        return outputFromProperty;
    }

    private void enrichOutputWithAnnotations(final PropertyDataDefinition propertyDataDefinition,
                                             final OutputDefinition outputDefinition,
                                             final Component component) {
        final List<Annotation> outputAnnotations =
            getOutputAnnotations(component, propertyDataDefinition.getName());
        if (!isEmpty(outputAnnotations)) {
            outputDefinition.setAnnotations(outputAnnotations);
        }
    }

    private Component getInstanceOriginType(final ComponentInstance componentInstance) {
        return toscaOperationFacade
            .getToscaElement(componentInstance.getActualComponentUid(), getFilterComponentOutputs())
            .left()
            .on(err -> exceptionUtils.rollBackAndThrow(err, componentInstance.getActualComponentUid()));
    }

    private ComponentParametersView getFilterComponentOutputs() {
        final ComponentParametersView filterOutputs = new ComponentParametersView(true);
        filterOutputs.setIgnoreOutputs(false);
        return filterOutputs;
    }

    @Override
    public StorageOperationStatus unDeclarePropertiesAsOutputs(final Component component,
                                                               final OutputDefinition outputDefinition) {
        return unDeclareOutputProperty(component, outputDefinition);
    }

    private StorageOperationStatus unDeclareOutputProperty(final Component component,
                                                           final OutputDefinition outputDefinition) {
        final List<ComponentInstanceOutput> componentInstanceOutputs = componentInstanceBusinessLogic
            .getComponentInstanceOutputsByOutputId(component, outputDefinition.getUniqueId());
        if (isEmpty(componentInstanceOutputs)) {
            return StorageOperationStatus.OK;
        }
        componentInstanceOutputs.forEach(
            componentInstanceOutput -> prepareOutputValueBeforeDelete(outputDefinition, componentInstanceOutput,
                componentInstanceOutput.getPath()));
        return toscaOperationFacade.updateComponentInstanceOutputs(component,
            componentInstanceOutputs.get(0).getComponentInstanceId(), componentInstanceOutputs);
    }


    @Override
    protected void addPropertiesListToInput(final ComponentInstanceOutput componentInstanceOutput,
                                            final InputDefinition input) {
        throw new UnsupportedOperationException();
    }

    @Override
    public StorageOperationStatus unDeclarePropertiesAsInputs(final Component component,
                                                              final InputDefinition inputDefinition) {
        throw new UnsupportedOperationException();
    }

    @Override
    public StorageOperationStatus unDeclarePropertiesAsListInputs(final Component component,
                                                                  final InputDefinition inputDefinition) {
        throw new UnsupportedOperationException();
    }

}
