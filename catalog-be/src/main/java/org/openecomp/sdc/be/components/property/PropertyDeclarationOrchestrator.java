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

package org.openecomp.sdc.be.components.property;

import fj.data.Either;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.openecomp.sdc.be.components.property.propertytopolicydeclarators.ComponentInstancePropertyToPolicyDeclarator;
import org.openecomp.sdc.be.components.property.propertytopolicydeclarators.ComponentPropertyToPolicyDeclarator;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstInputsMap;
import org.openecomp.sdc.be.model.ComponentInstancePropInput;
import org.openecomp.sdc.be.model.InputDefinition;
import org.openecomp.sdc.be.model.PolicyDefinition;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.common.log.wrappers.Logger;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.apache.commons.collections.MapUtils.isNotEmpty;

@org.springframework.stereotype.Component
public class PropertyDeclarationOrchestrator {

    private static final Logger log = Logger.getLogger(PropertyDeclarationOrchestrator.class);
    private ComponentInstanceInputPropertyDeclarator componentInstanceInputPropertyDeclarator;
    private ComponentInstancePropertyDeclarator componentInstancePropertyDeclarator;
    private PolicyPropertyDeclarator policyPropertyDeclarator;
    private GroupPropertyDeclarator groupPropertyDeclarator;
    private ComponentPropertyDeclarator servicePropertyDeclarator;
    private List<PropertyDeclarator> propertyDeclaratorsToInput;
    private List<PropertyDeclarator> propertyDeclaratorsToPolicy;
    private ComponentPropertyToPolicyDeclarator componentPropertyToPolicyDeclarator;
    private ComponentInstancePropertyToPolicyDeclarator componentInstancePropertyToPolicyDeclarator;

    public PropertyDeclarationOrchestrator(ComponentInstanceInputPropertyDeclarator componentInstanceInputPropertyDeclarator,
            ComponentInstancePropertyDeclarator componentInstancePropertyDeclarator, PolicyPropertyDeclarator policyPropertyDeclarator,
            GroupPropertyDeclarator groupPropertyDeclarator, ComponentPropertyDeclarator servicePropertyDeclarator,
            ComponentPropertyToPolicyDeclarator componentPropertyToPolicyDeclarator,
            ComponentInstancePropertyToPolicyDeclarator componentInstancePropertyToPolicyDeclarator) {
        this.componentInstanceInputPropertyDeclarator = componentInstanceInputPropertyDeclarator;
        this.componentInstancePropertyDeclarator = componentInstancePropertyDeclarator;
        this.policyPropertyDeclarator = policyPropertyDeclarator;
        this.groupPropertyDeclarator = groupPropertyDeclarator;
        this.servicePropertyDeclarator = servicePropertyDeclarator;
        this.componentPropertyToPolicyDeclarator = componentPropertyToPolicyDeclarator;
        this.componentInstancePropertyToPolicyDeclarator = componentInstancePropertyToPolicyDeclarator;
        propertyDeclaratorsToInput = Arrays.asList(componentInstanceInputPropertyDeclarator, componentInstancePropertyDeclarator, policyPropertyDeclarator, groupPropertyDeclarator, servicePropertyDeclarator);
        propertyDeclaratorsToPolicy = Arrays.asList(componentPropertyToPolicyDeclarator, componentInstancePropertyToPolicyDeclarator);
    }

    public Either<List<InputDefinition>, StorageOperationStatus> declarePropertiesToInputs(Component component, ComponentInstInputsMap componentInstInputsMap) {
        updatePropertiesConstraints(component, componentInstInputsMap);
        PropertyDeclarator propertyDeclarator = getPropertyDeclarator(componentInstInputsMap);
        Pair<String, List<ComponentInstancePropInput>> propsToDeclare = componentInstInputsMap.resolvePropertiesToDeclare();
        return propertyDeclarator.declarePropertiesAsInputs(component, propsToDeclare.getLeft(), propsToDeclare.getRight());
    }

    private void updatePropertiesConstraints(Component component, ComponentInstInputsMap componentInstInputsMap) {
        componentInstInputsMap.getComponentInstanceProperties().forEach((k, v) -> updatePropsConstraints(component.safeGetComponentInstancesProperties(), k, v));
        componentInstInputsMap.getComponentInstanceInputsMap().forEach((k, v) -> updatePropsConstraints(component.safeGetComponentInstancesInputs(), k, v));
        componentInstInputsMap.getGroupProperties().forEach((k, v) -> updatePropsConstraints(component.safeGetPolicyProperties(), k, v));
        componentInstInputsMap.getPolicyProperties().forEach((k, v) -> updatePropsConstraints(component.safeGetGroupsProperties(), k, v));
    }

    public Either<List<PolicyDefinition>, StorageOperationStatus> declarePropertiesToPolicies(Component component, ComponentInstInputsMap componentInstInputsMap) {
        PropertyDeclarator propertyDeclarator = getPropertyDeclarator(componentInstInputsMap);
        Pair<String, List<ComponentInstancePropInput>> propsToDeclare = componentInstInputsMap.resolvePropertiesToDeclare();
        return propertyDeclarator.declarePropertiesAsPolicies(component, propsToDeclare.getLeft(), propsToDeclare.getRight());
    }

    public Either<InputDefinition, StorageOperationStatus> declarePropertiesToListInput(Component component, ComponentInstInputsMap componentInstInputsMap, InputDefinition input) {
        PropertyDeclarator propertyDeclarator = getPropertyDeclarator(componentInstInputsMap);
        Pair<String, List<ComponentInstancePropInput>> propsToDeclare = componentInstInputsMap.resolvePropertiesToDeclare();
        log.debug("#declarePropertiesToInputs: componentId={}, propOwnerId={}", component.getUniqueId(), propsToDeclare.getLeft());
        return propertyDeclarator.declarePropertiesAsListInput(component, propsToDeclare.getLeft(), propsToDeclare.getRight(), input);
    }

    private <T extends PropertyDataDefinition> void updatePropsConstraints(Map<String, List<T>> instancesProperties , String ownerId, List<ComponentInstancePropInput> inputs) {
        Optional<List<T>> propertiesOpt = instancesProperties.entrySet()
                .stream()
                .filter(e -> e.getKey().equals(ownerId))
                .map(Map.Entry::getValue)
                .findFirst();
        if(propertiesOpt.isPresent()){
            Map<String, PropertyDataDefinition> instProps = propertiesOpt.get()
                    .stream()
                    .collect(Collectors.toMap(PropertyDataDefinition::getName, p->p));
            inputs.stream()
                    .filter(i->instProps.containsKey(i.getName()))
                    .forEach(i->updatePropConstraints(i, instProps.get(i.getName())));

        }
    }

    private void updatePropConstraints(PropertyDataDefinition input, PropertyDataDefinition property) {
        if(CollectionUtils.isNotEmpty(property.getPropertyConstraints())){
            input.setPropertyConstraints(property.getPropertyConstraints());
        } else if(property.getSchemaProperty() != null && CollectionUtils.isNotEmpty(property.getSchemaProperty().getPropertyConstraints())){
            input.setPropertyConstraints(property.getSchemaProperty().getPropertyConstraints());
        }
    }


    public StorageOperationStatus unDeclarePropertiesAsInputs(Component component, InputDefinition inputToDelete) {
        log.debug("#unDeclarePropertiesAsInputs - removing input declaration for input {} on component {}", inputToDelete.getName(), component.getUniqueId());
        for (PropertyDeclarator propertyDeclarator : propertyDeclaratorsToInput) {
            StorageOperationStatus storageOperationStatus = propertyDeclarator.unDeclarePropertiesAsInputs(component, inputToDelete);
            if (StorageOperationStatus.OK != storageOperationStatus) {
                log.debug("#unDeclarePropertiesAsInputs - failed to remove input declaration for input {} on component {}. reason {}", inputToDelete.getName(), component.getUniqueId(), storageOperationStatus);
                return storageOperationStatus;
            }
        }
        return StorageOperationStatus.OK;

    }
    /**
     * Un declare properties declared as list type input
     *
     * @param component
     * @param inputToDelete
     * @return
     */
    public StorageOperationStatus unDeclarePropertiesAsListInputs(Component component, InputDefinition inputToDelete) {
        log.debug("#unDeclarePropertiesAsListInputs - removing input declaration for input {} on component {}", inputToDelete.getName(), component.getUniqueId());
        for (PropertyDeclarator propertyDeclarator : propertyDeclaratorsToInput) {
            StorageOperationStatus storageOperationStatus = propertyDeclarator.unDeclarePropertiesAsListInputs(component, inputToDelete);
            if (StorageOperationStatus.OK != storageOperationStatus) {
                log.debug("#unDeclarePropertiesAsListInputs - failed to remove input declaration for input {} on component {}. reason {}", inputToDelete.getName(), component.getUniqueId(), storageOperationStatus);
                return storageOperationStatus;
            }
        }
        return StorageOperationStatus.OK;

    }

    /**
     * Get properties owner id
     *
     * @param componentInstInputsMap
     * @return
     */
    public String getPropOwnerId(ComponentInstInputsMap componentInstInputsMap) {
        Pair<String, List<ComponentInstancePropInput>> propsToDeclare = componentInstInputsMap.resolvePropertiesToDeclare();
        return propsToDeclare.getLeft();
    }

    public StorageOperationStatus unDeclarePropertiesAsPolicies(Component component, PolicyDefinition policyToDelete) {
        log.debug("#unDeclarePropertiesAsInputs - removing policy declaration for input {} on component {}", policyToDelete
                                                                                                                     .getName(), component.getUniqueId());
        for(PropertyDeclarator propertyDeclarator : propertyDeclaratorsToPolicy) {
            StorageOperationStatus storageOperationStatus =
                    propertyDeclarator.unDeclarePropertiesAsPolicies(component, policyToDelete);
            if (StorageOperationStatus.OK != storageOperationStatus) {
                log.debug("#unDeclarePropertiesAsInputs - failed to remove policy declaration for policy {} on component {}. reason {}", policyToDelete
                                                                                                                                                 .getName(), component.getUniqueId(), storageOperationStatus);
                return storageOperationStatus;
            }
        }

        return StorageOperationStatus.OK;

    }

    private PropertyDeclarator getPropertyDeclarator(ComponentInstInputsMap componentInstInputsMap) {
        if (isNotEmpty(componentInstInputsMap.getComponentInstanceInputsMap())) {
            return componentInstanceInputPropertyDeclarator;
        }
        if (isNotEmpty(componentInstInputsMap.getComponentInstanceProperties())) {
            return componentInstancePropertyDeclarator;
        }
        if (isNotEmpty(componentInstInputsMap.getPolicyProperties())) {
            return policyPropertyDeclarator;
        }
        if (isNotEmpty(componentInstInputsMap.getGroupProperties())) {
            return groupPropertyDeclarator;
        }
        if(isNotEmpty(componentInstInputsMap.getServiceProperties())) {
            return servicePropertyDeclarator;
        }
        if(isNotEmpty(componentInstInputsMap.getComponentPropertiesToPolicies())) {
            return componentPropertyToPolicyDeclarator;
        }
        if(isNotEmpty(componentInstInputsMap.getComponentInstancePropertiesToPolicies())) {
            return componentInstancePropertyToPolicyDeclarator;
        }
        throw new IllegalStateException("there are no properties selected for declaration");

    }

}
