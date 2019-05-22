/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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
 * Modifications copyright (c) 2019 Nokia
 * ================================================================================
 */

package org.openecomp.sdc.be.components.validation;

import org.apache.commons.collections.CollectionUtils;
import org.openecomp.sdc.be.components.impl.exceptions.ByActionStatusComponentException;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields;
import org.openecomp.sdc.be.datatypes.tosca.ToscaDataDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.ComponentParametersView;
import org.openecomp.sdc.be.model.GroupDefinition;
import org.openecomp.sdc.be.model.jsontitan.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.StorageException;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.utils.ComponentValidationUtils;
import org.openecomp.sdc.common.util.ValidationUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.util.stream.Collectors.toList;

@org.springframework.stereotype.Component
public class ComponentValidations {

    private final ToscaOperationFacade toscaOperationFacade;

    public ComponentValidations(ToscaOperationFacade toscaOperationFacade) {
        this.toscaOperationFacade = toscaOperationFacade;
    }

    public Optional<ComponentInstance> getComponentInstance(Component component, String instanceId) {
        return component.getComponentInstances()
                .stream()
                .filter(ci -> ci.getUniqueId().equals(instanceId))
                .findFirst();
    }

    public static boolean validateComponentInstanceExist(Component component, String instanceId) {
        return Optional.ofNullable(component.getComponentInstances())
                       .map(componentInstances -> componentInstances.stream().map(ComponentInstance::getUniqueId).collect(toList()))
                       .filter(instancesIds -> instancesIds.contains(instanceId))
                       .isPresent();
    }

    public static String getNormalizedName(ToscaDataDefinition toscaDataDefinition) {
        String name = (String) toscaDataDefinition.getToscaPresentationValue(JsonPresentationFields.NAME);
        return org.openecomp.sdc.common.util.ValidationUtils.normalizeComponentInstanceName(name);
    }

    /**
     * The following logic is applied:
     * For each name new or existing name we look at the normalized name which is used in Tosca representation
     * @param currentName
     * @param newName
     * @param component
     * @return True is new name can be used in this component, false otherwise
     */
    public static boolean validateNameIsUniqueInComponent(String currentName, String newName, Component component) {
        String normalizedCurrentName = ValidationUtils.normalizeComponentInstanceName(currentName);
        String normalizedNewName = ValidationUtils.normalizeComponentInstanceName(newName);
        
        if (normalizedCurrentName.equals(normalizedNewName)) {
            return true;    //As it's same entity, still considered unique
        }
        List<GroupDefinition> groups = component.getGroups();
        List<ComponentInstance> componentInstances = component.getComponentInstances();
        Set<String> existingNames = new HashSet<>();
        if (CollectionUtils.isNotEmpty(groups)) {
            List<String> existingGroupNames = groups
                    .stream()
                    .map(ComponentValidations::getNormalizedName)
                    .collect(toList());
            existingNames.addAll(existingGroupNames);
        }
        if (CollectionUtils.isNotEmpty(componentInstances)) {
            List<String> existingInstanceNames = componentInstances
                    .stream()
                    .map(ComponentValidations::getNormalizedName)
                    .collect(toList());
            existingNames.addAll(existingInstanceNames);
        }
        return !existingNames.contains(normalizedNewName);
    }

    void validateComponentIsCheckedOutByUser(Component component, String userId) {
        if (!ComponentValidationUtils.canWorkOnComponent(component, userId)) {
            throw new ByActionStatusComponentException(ActionStatus.ILLEGAL_COMPONENT_STATE, component.getComponentType().name(), component.getName(), component.getLifecycleState().name());
        }
    }
    Component validateComponentIsCheckedOutByUser(String componentId, ComponentTypeEnum componentTypeEnum, String userId) {
        Component component = getComponent(componentId, componentTypeEnum);
        validateComponentIsCheckedOutByUser(component, userId);
        return component;
    }

    Component getComponent(String componentId, ComponentTypeEnum componentType) {
        Component component = toscaOperationFacade.getToscaElement(componentId, new ComponentParametersView())
                .left()
                .on(storageOperationStatus -> onToscaOperationError(storageOperationStatus, componentId));

        validateComponentType(component, componentType);

        return component;
    }

    private void validateComponentType(Component component, ComponentTypeEnum componentType) {
        if (componentType!=component.getComponentType()) {
            throw new ByActionStatusComponentException(ActionStatus.INVALID_RESOURCE_TYPE);
        }
    }

    private Component onToscaOperationError(StorageOperationStatus storageOperationStatus, String componentId) {
        throw new StorageException(storageOperationStatus, componentId);
    }

}
