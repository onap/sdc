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

package org.openecomp.sdc.be.components.impl;

import static org.openecomp.sdc.be.components.impl.BaseBusinessLogic.enumHasValueFilter;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.openecomp.sdc.be.components.impl.exceptions.ByActionStatusComponentException;
import org.openecomp.sdc.be.components.impl.lock.LockingTransactional;
import org.openecomp.sdc.be.components.validation.AccessValidations;
import org.openecomp.sdc.be.components.validation.ComponentValidations;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.PromoteVersionEnum;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.GroupDefinition;
import org.openecomp.sdc.be.model.GroupProperty;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.GroupsOperation;
import org.openecomp.sdc.be.model.operations.StorageException;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.GroupOperation;
import org.openecomp.sdc.common.util.ValidationUtils;
import org.springframework.transaction.annotation.Transactional;

@org.springframework.stereotype.Component
public class GroupBusinessLogicNew {

    private final AccessValidations accessValidations;
    private final ComponentValidations componentValidations;
    private final GroupsOperation groupsOperation;
    private final GroupOperation groupOperation;

    public GroupBusinessLogicNew(AccessValidations accessValidations, ComponentValidations componentValidations, GroupsOperation groupsOperation,
                                 GroupOperation groupOperation) {
        this.accessValidations = accessValidations;
        this.componentValidations = componentValidations;
        this.groupsOperation = groupsOperation;
        this.groupOperation = groupOperation;
    }

    @LockingTransactional
    public List<String> updateMembers(String componentId, ComponentTypeEnum componentType, String userId, String groupUniqueId,
                                      List<String> members) {
        Component component = accessValidations.validateUserCanWorkOnComponent(componentId, componentType, userId, "UPDATE GROUP MEMBERS");
        GroupDefinition groupDefinition = getGroup(component, groupUniqueId);
        groupDefinition.setMembers(buildMembersMap(component, members));
        groupsOperation.updateGroupOnComponent(componentId, groupDefinition, PromoteVersionEnum.MINOR);
        return new ArrayList<>(groupDefinition.getMembers().values());
    }

    @LockingTransactional
    public List<GroupProperty> updateProperties(String componentId, ComponentTypeEnum componentType, String userId, String groupUniqueId,
                                                List<GroupProperty> newProperties) {
        Component component = accessValidations.validateUserCanWorkOnComponent(componentId, componentType, userId, "UPDATE GROUP PROPERTIES");
        GroupDefinition currentGroup = getGroup(component, groupUniqueId);
        validateUpdatedPropertiesAndSetEmptyValues(component, currentGroup, newProperties);
        return groupsOperation.updateGroupPropertiesOnComponent(componentId, currentGroup, newProperties, PromoteVersionEnum.MINOR).left()
            .on(this::onUpdatePropertyError);
    }

    @Transactional
    public List<PropertyDataDefinition> getProperties(String componentType, String userId, String componentId, String groupUniqueId) {
        Component component = accessValidations.validateUserCanRetrieveComponentData(componentId, componentType, userId, "GET GROUP PROPERTIES");
        GroupDefinition currentGroup = getGroup(component, groupUniqueId);
        return currentGroup.getProperties();
    }

    private List<GroupProperty> onUpdatePropertyError(StorageOperationStatus storageOperationStatus) {
        throw new StorageException(storageOperationStatus);
    }

    private Map<String, String> buildMembersMap(Component component, List<String> newMemberUniqueIds) {
        Map<String, String> nameToUniqueId = new HashMap<>();
        for (String memberUniqueId : newMemberUniqueIds) {
            ComponentInstance componentInstance = getComponentInstance(component, memberUniqueId);
            nameToUniqueId.put(componentInstance.getName(), componentInstance.getUniqueId());
        }
        return nameToUniqueId;
    }

    private ComponentInstance getComponentInstance(Component component, String memberUniqueId) {
        return componentValidations.getComponentInstance(component, memberUniqueId).orElseThrow(
            () -> new ByActionStatusComponentException(ActionStatus.COMPONENT_INSTANCE_NOT_FOUND_ON_CONTAINER, memberUniqueId, "",
                component.getActualComponentType(), component.getSystemName()));
    }

    private GroupDefinition getGroup(Component component, String groupUniqueId) {
        return component.getGroupById(groupUniqueId).orElseThrow(
            () -> new ByActionStatusComponentException(ActionStatus.GROUP_IS_MISSING, component.getSystemName(), component.getActualComponentType()));
    }

    private void validateUpdatedPropertiesAndSetEmptyValues(Component groupOwner, GroupDefinition originalGroup, List<GroupProperty> groupPropertiesToUpdate) {
        if (CollectionUtils.isEmpty(groupPropertiesToUpdate)) {
            throw new ByActionStatusComponentException(ActionStatus.PROPERTY_NOT_FOUND, StringUtils.EMPTY);
        }
        if (CollectionUtils.isEmpty(originalGroup.getProperties())) {
            throw new ByActionStatusComponentException(ActionStatus.PROPERTY_NOT_FOUND,
                groupPropertiesToUpdate.get(NumberUtils.INTEGER_ZERO).getName());
        }
        Map<String, GroupProperty> originalProperties = originalGroup.convertToGroupProperties().stream()
            .collect(Collectors.toMap(PropertyDataDefinition::getName, p -> p));
        for (GroupProperty gp : groupPropertiesToUpdate) {
            String updatedPropertyName = gp.getName();
            if (!originalProperties.containsKey(updatedPropertyName)) {
                throw new ByActionStatusComponentException(ActionStatus.PROPERTY_NOT_FOUND, updatedPropertyName);
            }
            if (!isOnlyGroupPropertyValueChanged(gp, originalProperties.get(updatedPropertyName))) {
                throw new ByActionStatusComponentException(ActionStatus.INVALID_PROPERTY, updatedPropertyName);
            }
            if (StringUtils.isEmpty(gp.getValue())) {
                gp.setValue(originalProperties.get(updatedPropertyName).getDefaultValue());
            }
            StorageOperationStatus sos = groupOperation.validateAndUpdatePropertyValue(groupOwner, gp);
            if (StorageOperationStatus.OK != sos) {
                throw new StorageException(sos, updatedPropertyName);
            }
        }
        validatePropertyBusinessLogic(groupPropertiesToUpdate, originalGroup);
    }

    private void validatePropertyBusinessLogic(List<GroupProperty> groupPropertiesToUpdate, GroupDefinition originalGroup) {
        Map<PropertyDefinition.PropertyNames, String> enumValueMap = new EnumMap<>(PropertyDefinition.PropertyNames.class);
        for (GroupProperty gp : groupPropertiesToUpdate) {
            // Filter out non special properties which does not have Enum
            final PropertyDefinition.PropertyNames gpEnum = PropertyDefinition.PropertyNames.findName(gp.getName());
            if (gpEnum != null) {
                enumValueMap.put(gpEnum, gp.getValue());
            }
        }
        if (MapUtils.isEmpty(enumValueMap)) {
            return;
        }
        validateVFInstancesLogic(enumValueMap, prepareMapWithOriginalProperties(originalGroup));
        if (enumValueMap.containsKey(PropertyDefinition.PropertyNames.VF_MODULE_DESCRIPTION) || enumValueMap
            .containsKey(PropertyDefinition.PropertyNames.VF_MODULE_LABEL)) {
            groupPropertiesToUpdate.stream().filter(e -> enumHasValueFilter(e.getName(), PropertyDefinition.PropertyNames::findName,
                PropertyDefinition.PropertyNames.VF_MODULE_DESCRIPTION, PropertyDefinition.PropertyNames.VF_MODULE_LABEL))
                .forEach(this::validateFreeText);
        }
    }

    private Map<PropertyDefinition.PropertyNames, String> prepareMapWithOriginalProperties(GroupDefinition originalGroup) {
        Map<PropertyDefinition.PropertyNames, String> oldValueMap = new EnumMap<>(PropertyDefinition.PropertyNames.class);
        PropertyDefinition.PropertyNames[] propertiesToCheck = new PropertyDefinition.PropertyNames[]{PropertyDefinition.PropertyNames.INITIAL_COUNT,
            PropertyDefinition.PropertyNames.MAX_INSTANCES, PropertyDefinition.PropertyNames.MIN_INSTANCES};
        for (GroupProperty gp : originalGroup.convertToGroupProperties()) {
            if (enumHasValueFilter(gp.getName(), PropertyDefinition.PropertyNames::findName, propertiesToCheck)) {
                oldValueMap.put(PropertyDefinition.PropertyNames.findName(gp.getName()), gp.getValue());
            }
        }
        if (StringUtils.isEmpty(oldValueMap.get(PropertyDefinition.PropertyNames.MAX_INSTANCES))) {
            oldValueMap.put(PropertyDefinition.PropertyNames.MAX_INSTANCES, String.valueOf(Integer.MAX_VALUE));
        }
        return oldValueMap;
    }

    private void validateVFInstancesLogic(Map<PropertyDefinition.PropertyNames, String> newValues,
                                          Map<PropertyDefinition.PropertyNames, String> parentValues) {
        if (!newValues.containsKey(PropertyDefinition.PropertyNames.INITIAL_COUNT) && !newValues
            .containsKey(PropertyDefinition.PropertyNames.MAX_INSTANCES) && !newValues.containsKey(PropertyDefinition.PropertyNames.MIN_INSTANCES)) {
            return;
        }
        int latestMaxInstances = getLatestIntProperty(newValues, parentValues, PropertyDefinition.PropertyNames.MAX_INSTANCES);
        int latestInitialCount = getLatestIntProperty(newValues, parentValues, PropertyDefinition.PropertyNames.INITIAL_COUNT);
        int latestMinInstances = getLatestIntProperty(newValues, parentValues, PropertyDefinition.PropertyNames.MIN_INSTANCES);
        if (isPropertyChanged(newValues, parentValues, PropertyDefinition.PropertyNames.INITIAL_COUNT) && (latestInitialCount > latestMaxInstances
            || latestInitialCount < latestMinInstances)) {
            throw new ByActionStatusComponentException(ActionStatus.INVALID_GROUP_INITIAL_COUNT_PROPERTY_VALUE,
                PropertyDefinition.PropertyNames.INITIAL_COUNT.getPropertyName(), String.valueOf(latestMinInstances),
                String.valueOf(latestMaxInstances));
        }
        if (isPropertyChanged(newValues, parentValues, PropertyDefinition.PropertyNames.MAX_INSTANCES) && latestMaxInstances < latestInitialCount) {
            throw new ByActionStatusComponentException(ActionStatus.INVALID_GROUP_PROPERTY_VALUE_LOWER_HIGHER,
                PropertyDefinition.PropertyNames.MAX_INSTANCES.getPropertyName(), "higher", String.valueOf(latestInitialCount));
        }
        if (isPropertyChanged(newValues, parentValues, PropertyDefinition.PropertyNames.MIN_INSTANCES) && latestMinInstances > latestInitialCount) {
            throw new ByActionStatusComponentException(ActionStatus.INVALID_GROUP_PROPERTY_VALUE_LOWER_HIGHER,
                PropertyDefinition.PropertyNames.MIN_INSTANCES.getPropertyName(), "lower", String.valueOf(latestInitialCount));
        }
    }

    private boolean isPropertyChanged(Map<PropertyDefinition.PropertyNames, String> newValues,
                                      Map<PropertyDefinition.PropertyNames, String> parentValues,
                                      final PropertyDefinition.PropertyNames minInstances) {
        return newValues.containsKey(minInstances) && !newValues.get(minInstances).equals(parentValues.get(minInstances));
    }

    private int getLatestIntProperty(Map<PropertyDefinition.PropertyNames, String> newValues,
                                     Map<PropertyDefinition.PropertyNames, String> parentValues, PropertyDefinition.PropertyNames propertyKey) {
        String value;
        if (newValues.containsKey(propertyKey)) {
            value = newValues.get(propertyKey);
        } else {
            value = parentValues.get(propertyKey);
        }
        return Integer.parseInt(value);
    }

    private boolean isOnlyGroupPropertyValueChanged(GroupProperty groupProperty1, GroupProperty groupProperty2) {
        GroupProperty groupProperty1Duplicate = new GroupProperty(groupProperty1);
        groupProperty1Duplicate.setValue(null);
        groupProperty1Duplicate.setSchema(null);
        groupProperty1Duplicate.setParentUniqueId(null);
        groupProperty1Duplicate.setToscaFunction(null);
        groupProperty1Duplicate.setToscaGetFunctionType(null);
        GroupProperty groupProperty2Duplicate = new GroupProperty(groupProperty2);
        groupProperty2Duplicate.setValue(null);
        groupProperty2Duplicate.setSchema(null);
        groupProperty2Duplicate.setParentUniqueId(null);
        groupProperty2Duplicate.setToscaFunction(null);
        groupProperty2Duplicate.setToscaGetFunctionType(null);
        return StringUtils.equals(groupProperty1Duplicate.getValueUniqueUid(), groupProperty2Duplicate.getValueUniqueUid()) && groupProperty1Duplicate
            .equals(groupProperty2Duplicate);
    }

    private void validateFreeText(GroupProperty groupPropertyToUpdate) {
        final String groupTypeValue = groupPropertyToUpdate.getValue();
        if (!org.apache.commons.lang3.StringUtils.isEmpty(groupTypeValue)) {
            if (!ValidationUtils.validateDescriptionLength(groupTypeValue)) {
                throw new ByActionStatusComponentException(ActionStatus.COMPONENT_DESCRIPTION_EXCEEDS_LIMIT, NodeTypeEnum.Property.getName(),
                    String.valueOf(ValidationUtils.COMPONENT_DESCRIPTION_MAX_LENGTH));
            } else if (!ValidationUtils.validateIsEnglish(groupTypeValue)) {
                throw new ByActionStatusComponentException(ActionStatus.COMPONENT_INVALID_DESCRIPTION, NodeTypeEnum.Property.getName());
            }
        }
    }
}
