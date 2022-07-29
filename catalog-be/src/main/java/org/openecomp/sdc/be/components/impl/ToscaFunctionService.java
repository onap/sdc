/*
 * -
 *  ============LICENSE_START=======================================================
 *  Copyright (C) 2022 Nordix Foundation.
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================
 */

package org.openecomp.sdc.be.components.impl;

import java.util.List;
import java.util.Map;
import org.openecomp.sdc.be.datatypes.elements.ToscaConcatFunction;
import org.openecomp.sdc.be.datatypes.elements.ToscaFunction;
import org.openecomp.sdc.be.datatypes.elements.ToscaFunctionType;
import org.openecomp.sdc.be.datatypes.elements.ToscaGetFunctionDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.PropertySource;
import org.openecomp.sdc.be.model.AttributeDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;

@org.springframework.stereotype.Service
public class ToscaFunctionService {

    final List<ToscaFunctionType> functionTypesToUpdateList =
        List.of(ToscaFunctionType.GET_INPUT, ToscaFunctionType.GET_ATTRIBUTE, ToscaFunctionType.GET_PROPERTY, ToscaFunctionType.CONCAT);

    /**
     * Updates the given TOSCA function with information from the SELF component.
     *
     * @param toscaFunctionToUpdate the TOSCA function to update
     * @param selfComponent         the SELF Component
     * @param instancePropertyMap   the SELF Component instances properties
     * @param instanceAttributeMap  the SELF Component instances attributes
     */
    public void updateFunctionWithDataFromSelfComponent(final ToscaFunction toscaFunctionToUpdate, final Component selfComponent,
                                                        final Map<String, List<ComponentInstanceProperty>> instancePropertyMap,
                                                        final Map<String, List<AttributeDefinition>> instanceAttributeMap) {
        switch (toscaFunctionToUpdate.getType()) {
            case GET_INPUT: {
                updateGetInputFunction((ToscaGetFunctionDataDefinition) toscaFunctionToUpdate, selfComponent);
                break;
            }
            case GET_PROPERTY:
            case GET_ATTRIBUTE: {
                updateGetPropertyFunction((ToscaGetFunctionDataDefinition) toscaFunctionToUpdate, selfComponent, instancePropertyMap,
                    instanceAttributeMap);
                break;
            }
            case CONCAT:
                updateConcatFunction((ToscaConcatFunction) toscaFunctionToUpdate, selfComponent, instancePropertyMap, instanceAttributeMap);
                break;
        }
    }

    /**
     * Updates the TOSCA concat function parameters, where the parameter is a TOSCA function.
     *
     * @param concatFunction       the TOSCA concat function to update
     * @param selfComponent        the SELF component
     * @param instancePropertyMap  the component instances properties
     * @param instanceAttributeMap the component instances attributes
     */
    private void updateConcatFunction(final ToscaConcatFunction concatFunction, final Component selfComponent,
                                      final Map<String, List<ComponentInstanceProperty>> instancePropertyMap,
                                      final Map<String, List<AttributeDefinition>> instanceAttributeMap) {
        concatFunction.getParameters().stream()
            .filter(ToscaFunction.class::isInstance)
            .filter(functionParameter -> functionTypesToUpdateList.contains(functionParameter.getType()))
            .forEach(functionParameter ->
                updateFunctionWithDataFromSelfComponent((ToscaFunction) functionParameter, selfComponent, instancePropertyMap, instanceAttributeMap));
    }

    /**
     * Updates the Source Unique Id, the Source Name and the Property Unique Id of the TOSCA get_input function.
     *
     * @param toscaGetFunction the TOSCA get_input function to update
     * @param selfComponent    the SELF component
     */
    private void updateGetInputFunction(final ToscaGetFunctionDataDefinition toscaGetFunction, final Component selfComponent) {
        toscaGetFunction.setSourceUniqueId(selfComponent.getUniqueId());
        toscaGetFunction.setSourceName(selfComponent.getName());
        selfComponent.getInputs().stream()
            .filter(inputDefinition -> inputDefinition.getName().equals(toscaGetFunction.getPropertyName()))
            .findAny().ifPresent(input ->
                toscaGetFunction.setPropertyUniqueId(input.getUniqueId())
            );
    }

    /**
     * Updates the Source Unique Id, the Source Name and the Property Unique Id of the TOSCA get function.
     *
     * @param toscaGetFunction     the TOSCA get function to update
     * @param selfComponent        the SELF component
     * @param instancePropertyMap  the component instances properties
     * @param instanceAttributeMap the component instances attributes
     */
    private void updateGetPropertyFunction(final ToscaGetFunctionDataDefinition toscaGetFunction, final Component selfComponent,
                                           final Map<String, List<ComponentInstanceProperty>> instancePropertyMap,
                                           final Map<String, List<AttributeDefinition>> instanceAttributeMap) {
        if (toscaGetFunction.getPropertySource() == PropertySource.SELF) {
            toscaGetFunction.setSourceUniqueId(selfComponent.getUniqueId());
            toscaGetFunction.setSourceName(selfComponent.getName());
            if (toscaGetFunction.getType() == ToscaFunctionType.GET_PROPERTY) {
                selfComponent.getProperties().stream()
                    .filter(property -> property.getName().equals(toscaGetFunction.getPropertyPathFromSource().get(0)))
                    .findAny()
                    .ifPresent(property ->
                        toscaGetFunction.setPropertyUniqueId(property.getUniqueId())
                    );
            } else {
                selfComponent.getAttributes().stream()
                    .filter(attribute -> attribute.getName().equals(toscaGetFunction.getPropertyPathFromSource().get(0)))
                    .findAny()
                    .ifPresent(attribute ->
                        toscaGetFunction.setPropertyUniqueId(attribute.getUniqueId())
                    );
            }
        } else if (toscaGetFunction.getPropertySource() == PropertySource.INSTANCE) {
            selfComponent.getComponentInstances().stream()
                .filter(componentInstance -> toscaGetFunction.getSourceName().equals(componentInstance.getName()))
                .findAny()
                .ifPresent(componentInstance -> toscaGetFunction.setSourceUniqueId(componentInstance.getUniqueId()));
            if (toscaGetFunction.getType() == ToscaFunctionType.GET_PROPERTY) {
                final List<ComponentInstanceProperty> instanceProperties = instancePropertyMap.get(toscaGetFunction.getSourceUniqueId());
                instanceProperties.stream()
                    .filter(property -> property.getName().equals(toscaGetFunction.getPropertyPathFromSource().get(0)))
                    .findAny()
                    .ifPresent(property ->
                        toscaGetFunction.setPropertyUniqueId(property.getUniqueId())
                    );
            } else {
                final List<AttributeDefinition> instanceAttributes = instanceAttributeMap.get(toscaGetFunction.getSourceUniqueId());
                instanceAttributes.stream()
                    .filter(attribute -> attribute.getName().equals(toscaGetFunction.getPropertyPathFromSource().get(0)))
                    .findAny()
                    .ifPresent(attribute ->
                        toscaGetFunction.setPropertyUniqueId(attribute.getUniqueId())
                    );
            }
        }
    }

}
