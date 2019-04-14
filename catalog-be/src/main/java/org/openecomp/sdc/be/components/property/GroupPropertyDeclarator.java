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
 */

package org.openecomp.sdc.be.components.property;

import static java.util.stream.Collectors.toList;
import static org.apache.commons.collections.CollectionUtils.isEmpty;
import static org.openecomp.sdc.be.components.property.GetInputUtils.isGetInputValueForInput;

import fj.data.Either;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.apache.commons.collections.CollectionUtils;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.GroupDefinition;
import org.openecomp.sdc.be.model.InputDefinition;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.GroupOperation;
import org.openecomp.sdc.be.model.operations.impl.PropertyOperation;
import org.openecomp.sdc.common.log.wrappers.Logger;

@org.springframework.stereotype.Component
public class GroupPropertyDeclarator extends DefaultPropertyDeclarator<GroupDefinition, PropertyDataDefinition> {

    private static final Logger log = Logger.getLogger(GroupPropertyDeclarator.class);
    private GroupOperation groupOperation;

    public GroupPropertyDeclarator(ComponentsUtils componentsUtils, PropertyOperation propertyOperation, GroupOperation groupOperation) {
        super(componentsUtils, propertyOperation);
        this.groupOperation = groupOperation;
    }

    @Override
    public PropertyDataDefinition createDeclaredProperty(PropertyDataDefinition prop) {
        return new PropertyDataDefinition(prop);
    }

    @Override
    public Either<?, StorageOperationStatus> updatePropertiesValues(Component component, String groupId, List<PropertyDataDefinition> properties) {
        log.debug("#updatePropertiesValues - updating group properties for group {} on component {}", groupId, component.getUniqueId());
        StorageOperationStatus updateStatus = groupOperation.updateGroupProperties(component, groupId, properties);
        return updateStatus == StorageOperationStatus.OK ? Either.left(updateStatus) : Either.right(updateStatus);
    }

    @Override
    public Optional<GroupDefinition> resolvePropertiesOwner(Component component, String groupId) {
        log.debug("#resolvePropertiesOwner - fetching group {} of component {}", groupId, component.getUniqueId());
        return component.getGroupById(groupId);
    }

    @Override
    public void addPropertiesListToInput(PropertyDataDefinition declaredProp, InputDefinition input) {
        List<ComponentInstanceProperty> propertiesList = input.getProperties();
        if(propertiesList == null) {
            propertiesList = new ArrayList<>(); // adding the property with the new value for UI
        }
        propertiesList.add(new ComponentInstanceProperty(declaredProp));
        input.setProperties(propertiesList);

    }

    @Override
    public StorageOperationStatus unDeclarePropertiesAsInputs(Component component, InputDefinition inputForDelete) {
        return getGroupPropertiesDeclaredAsInput(component, inputForDelete.getUniqueId())
                .map(groupProperties -> unDeclareGroupProperties(component, inputForDelete, groupProperties))
                .orElse(StorageOperationStatus.OK);
    }

    private StorageOperationStatus unDeclareGroupProperties(Component container, InputDefinition input, GroupProperties groupProperties) {
        String groupId = groupProperties.getGroupId();
        List<PropertyDataDefinition> propsDeclaredAsInput = groupProperties.getProperties();
        propsDeclaredAsInput.forEach(groupProp -> prepareValueBeforeDelete(input, groupProp, Collections.emptyList()));
        return groupOperation.updateGroupProperties(container, groupId, propsDeclaredAsInput);
    }

    private Optional<GroupProperties> getGroupPropertiesDeclaredAsInput(Component container, String inputId) {
        if (container.getGroups() == null) {
            return Optional.empty();
        }
        return container.getGroups()
                .stream()
                .filter(group -> Objects.nonNull(group.getProperties()))
                .map(grp -> getGroupPropertiesDeclaredAsInput(grp, inputId))
                .filter(GroupProperties::isNotEmpty)
                .findFirst();
    }


    private GroupProperties getGroupPropertiesDeclaredAsInput(GroupDefinition group, String inputId) {
        List<PropertyDataDefinition> propertyDataDefinitions = group.getProperties()
                .stream()
                .filter(prop -> isPropertyDeclaredAsInputByInputId(prop, inputId))
                .collect(toList());
        return new GroupProperties(group.getUniqueId(), propertyDataDefinitions);
    }

    private boolean isPropertyDeclaredAsInputByInputId(PropertyDataDefinition property, String inputId) {
        if (isEmpty(property.getGetInputValues())) {
            return false;
        }
        return property.getGetInputValues().stream()
                .filter(Objects::nonNull)
                .anyMatch(getInputVal -> isGetInputValueForInput(getInputVal, inputId));
    }


    private class GroupProperties {
        private String groupId;
        private List<PropertyDataDefinition> properties;

        GroupProperties(String groupId, List<PropertyDataDefinition> properties) {
            this.groupId = groupId;
            this.properties = (properties == null)? null :new ArrayList<>(properties);
        }

        String getGroupId() {
            return groupId;
        }

        public List<PropertyDataDefinition> getProperties() {
            return new ArrayList<>(properties);
        }

        boolean isNotEmpty() {
            return CollectionUtils.isNotEmpty(properties);
        }
    }
}
