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

package org.openecomp.sdc.be.components.impl;

import fj.data.Either;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.openecomp.sdc.be.components.impl.exceptions.ComponentException;
import org.openecomp.sdc.be.components.utils.Utils;
import org.openecomp.sdc.be.components.validation.AccessValidations;
import org.openecomp.sdc.be.components.validation.ComponentValidations;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.config.BeEcompErrorManager.ErrorSeverity;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.datatypes.elements.GroupDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.info.ArtifactDefinitionInfo;
import org.openecomp.sdc.be.info.ArtifactTemplateInfo;
import org.openecomp.sdc.be.info.GroupDefinitionInfo;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.ComponentParametersView;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.GroupDefinition;
import org.openecomp.sdc.be.model.GroupInstance;
import org.openecomp.sdc.be.model.GroupInstanceProperty;
import org.openecomp.sdc.be.model.GroupProperty;
import org.openecomp.sdc.be.model.GroupTypeDefinition;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.PropertyDefinition.GroupInstancePropertyValueUpdateBehavior;
import org.openecomp.sdc.be.model.PropertyDefinition.PropertyNames;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.cache.ApplicationDataTypeCache;
import org.openecomp.sdc.be.model.jsontitan.operations.ArtifactsOperations;
import org.openecomp.sdc.be.model.jsontitan.operations.GroupsOperation;
import org.openecomp.sdc.be.model.jsontitan.operations.TopologyTemplateOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.DaoStatusConverter;
import org.openecomp.sdc.be.model.operations.impl.GroupTypeOperation;
import org.openecomp.sdc.be.model.operations.impl.UniqueIdBuilder;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.util.ValidationUtils;
import org.openecomp.sdc.exception.ResponseFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@org.springframework.stereotype.Component("groupBusinessLogic")
public class GroupBusinessLogic extends BaseBusinessLogic {
    public static final String GROUP_DELIMITER_REGEX = "\\.\\.";
    private static String ADDING_GROUP = "AddingGroup";

    public static final String INITIAL_VERSION = "1";

    private static final String CREATE_GROUP = "CreateGroup";

    private static final String UPDATE_GROUP = "UpdateGroup";

    private static final String GET_GROUP = "GetGroup";

    private static final String DELETE_GROUP = "GetGroup";

    private static final Logger log = LoggerFactory.getLogger(GroupBusinessLogic.class);

    @javax.annotation.Resource
    private AccessValidations accessValidations;

    @javax.annotation.Resource
    private GroupTypeOperation groupTypeOperation;

    @Autowired
    private ArtifactsOperations artifactsOperation;

    @Autowired
    private GroupsOperation groupsOperation;
    @Autowired
    private ApplicationDataTypeCache dataTypeCache;

    private String getComponentTypeForResponse(org.openecomp.sdc.be.model.Component component) {
        String componentTypeForResponse = "SERVICE";
        if (component instanceof Resource) {
            componentTypeForResponse = ((Resource) component).getResourceType().name();
        }
        return componentTypeForResponse;
    }

    /**
     * Verify that the artifact members belongs to the component
     *
     * @param component
     * @param artifacts
     * @return
     */
    private Either<Boolean, ResponseFormat> verifyArtifactsBelongsToComponent(Component component, List<String> artifacts, String context) {

        if (CollectionUtils.isEmpty(artifacts)) {
            return Either.left(true);
        }

        Map<String, ArtifactDefinition> deploymentArtifacts = component.getDeploymentArtifacts();
        if (MapUtils.isEmpty(deploymentArtifacts)) {
            BeEcompErrorManager.getInstance().logInvalidInputError(context, "No deployment artifact found under component " + component.getNormalizedName(), ErrorSeverity.INFO);
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.INVALID_CONTENT));
        }

        List<String> currentArtifacts = deploymentArtifacts.values().stream().map(p -> p.getUniqueId()).collect(toList());
        log.debug("The deployment artifacts of component {} are {}", component.getNormalizedName(), deploymentArtifacts);
        if (!currentArtifacts.containsAll(artifacts)) {
            BeEcompErrorManager.getInstance().logInvalidInputError(context, "Not all artifacts belongs to component " + component.getNormalizedName(), ErrorSeverity.INFO);
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.INVALID_CONTENT));
        }

        return Either.left(true);

    }

    /**
     * verify that the members are component instances of the component
     *
     * @param component
     * @param groupMembers
     * @param memberToscaTypes
     * @return
     */
    private Either<Boolean, ResponseFormat> verifyComponentInstancesAreValidMembers(Component component, String groupName, Map<String, String> groupMembers, List<String> memberToscaTypes) {

        if (MapUtils.isEmpty(groupMembers)) {
            return Either.left(true);
        }

        if (CollectionUtils.isEmpty(memberToscaTypes)) {
            return Either.left(true);
        }

        List<ComponentInstance> componentInstances = component.getComponentInstances();
        if (CollectionUtils.isNotEmpty(componentInstances)) {
            Map<String, ComponentInstance> compInstUidToCompInstMap = componentInstances.stream().collect(Collectors.toMap(p -> p.getUniqueId(), p -> p));

            Set<String> allCompInstances = compInstUidToCompInstMap.keySet();

            for (Entry<String, String> groupMember : groupMembers.entrySet()) {
                String compName = groupMember.getKey();
                String compUid = groupMember.getValue();

                if (!allCompInstances.contains(compUid)) {
                    /*
                     * %1 - member name %2 - group name %3 - VF name %4 - component type [VF ]
                     */
                    String componentTypeForResponse = getComponentTypeForResponse(component);

                    BeEcompErrorManager.getInstance().logInvalidInputError(CREATE_GROUP, "Not all group members exists under the component", ErrorSeverity.INFO);
                    return Either.right(componentsUtils.getResponseFormat(ActionStatus.GROUP_INVALID_COMPONENT_INSTANCE, compName, groupName, component.getNormalizedName(), componentTypeForResponse));
                }
            }
        }

        return Either.left(true);
    }



    /**
     * Update GroupDefinition metadata
     *
     * @param componentId
     * @param user
     * @param componentType
     * @param updatedGroup
     * @param inTransaction
     * @return
     */
    public Either<GroupDefinition, ResponseFormat> validateAndUpdateGroupMetadata(String componentId, User user, ComponentTypeEnum componentType, GroupDefinition updatedGroup, boolean inTransaction , boolean shouldLock) {

        Either<GroupDefinition, ResponseFormat> result = null;
        try {
            // Validate user exist
            Either<User, ResponseFormat> validateUserExists = validateUserExists(user.getUserId(), UPDATE_GROUP, inTransaction);
            if (validateUserExists.isRight()) {
                result = Either.right(validateUserExists.right().value());
                return result;
            }
            // Validate component exist
            Either<? extends org.openecomp.sdc.be.model.Component, ResponseFormat> validateComponent = validateComponentExists(componentId, componentType, null);
            if (validateComponent.isRight()) {
                result = Either.right(validateComponent.right().value());
                return result;
            }
            org.openecomp.sdc.be.model.Component component = validateComponent.left().value();
            // validate we can work on component
            Either<Boolean, ResponseFormat> canWork = validateCanWorkOnComponent(component, user.getUserId());
            if (canWork.isRight()) {
                result = Either.right(canWork.right().value());
                return result;
            }
            List<GroupDefinition> currentGroups = component.getGroups();
            if (CollectionUtils.isEmpty(currentGroups)) {
                log.error("Failed to update the metadata of group {} on component {}. The status is {}. ", updatedGroup.getName(), component.getName(), ActionStatus.GROUP_IS_MISSING);
                result = Either.right(componentsUtils.getResponseFormat(ActionStatus.GROUP_IS_MISSING, updatedGroup.getName(), component.getName(), component.getComponentType().getValue()));
                return result;
            }
            // Validate groups exists in the component
            Optional<GroupDefinition> currentGroupOpt = currentGroups.stream().filter(g -> g.getUniqueId().equals(updatedGroup.getUniqueId())).findAny();
            if (!currentGroupOpt.isPresent()) {
                log.error("Failed to update the metadata of group {} on component {}. The status is {}. ", updatedGroup.getName(), component.getName(), ActionStatus.GROUP_IS_MISSING);
                result = Either.right(componentsUtils.getResponseFormat(ActionStatus.GROUP_IS_MISSING, updatedGroup.getName(), component.getName(), component.getComponentType().getValue()));
                return result;
            }
            GroupDefinition currentGroup = currentGroupOpt.get();
            if ( shouldLock ){
                Either<Boolean, ResponseFormat> lockResult = lockComponent(componentId, component, "Update GroupDefinition Metadata");
                if (lockResult.isRight()) {
                    result = Either.right(lockResult.right().value());
                    return result;
                }
            }
            // Validate group type is vfModule
            if (currentGroup.getType().equals(Constants.GROUP_TOSCA_HEAT)) {
                log.error("Failed to update the metadata of group {}. Group type is {} and cannot be updated", currentGroup.getName(), currentGroup.getType());
                ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.GROUP_TYPE_IS_INVALID, updatedGroup.getType());
                result = Either.right(responseFormat);
                return result;
            }
            result = updateGroupMetadata(component, currentGroup, updatedGroup);
            return result;

        } finally {
            if (result.isLeft()) {
                titanDao.commit();
            } else {
                titanDao.rollback();
            }
            if (shouldLock) {
                graphLockOperation.unlockComponent(componentId, componentType.getNodeType());
            }
        }
    }

    private Either<GroupDefinition, ResponseFormat> updateGroupMetadata(Component component, GroupDefinition currentGroup, GroupDefinition updatedGroup) {
        String currentGroupName = currentGroup.getName();
        Either<GroupDefinition, ResponseFormat> result = validateAndUpdateGroupMetadata(currentGroup, updatedGroup);

        if (result.isRight()) {
            log.debug("Failed to validate a metadata of the group {} on component {}. ", updatedGroup.getName(), component.getName());
        }
        if (result.isLeft()) {
            result = updateGroup(component, currentGroup, currentGroupName);
        }
        return result;
    }

    private Either<GroupDefinition, ResponseFormat> updateGroup(Component component, GroupDefinition updatedGroup, String currentGroupName) {
        Either<GroupDefinition, StorageOperationStatus> handleGroupRes;
        Either<GroupDefinition, ResponseFormat> result = null;
        if (updatedGroup.getName().equals(currentGroupName)) {
            handleGroupRes = groupsOperation.updateGroup(component, updatedGroup);
            if (handleGroupRes.isRight()) {
                log.debug("Failed to update a metadata of the group {} on component {}. ", updatedGroup.getName(), component.getName());
                result = Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(handleGroupRes.right().value())));
            }
        } else {
            StorageOperationStatus deleteStatus = groupsOperation.deleteGroup(component, currentGroupName);
            if (deleteStatus != StorageOperationStatus.OK) {
                log.debug("Failed to delete the group {} from component {}. ", updatedGroup.getName(), component.getName());
                result = Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(deleteStatus)));
            }
            handleGroupRes = groupsOperation.addGroup(component, updatedGroup);
            if (handleGroupRes.isRight()) {
                log.debug("Failed to add the group {} to component {}. ", updatedGroup.getName(), component.getName());
                result = Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(handleGroupRes.right().value())));
            }
        }
        if (result == null) {
            result = Either.left(updatedGroup);
        }
        return result;
    }

    /**
     * Validate and Update Group Property
     *
     * @param componentId
     * @param groupUniqueId
     * @param user
     * @param componentType
     * @param groupPropertiesToUpdate
     * @param inTransaction
     * @return
     */
    public Either<List<GroupProperty>, ResponseFormat> validateAndUpdateGroupProperties(String componentId, String groupUniqueId, User user, ComponentTypeEnum componentType, List<GroupProperty> groupPropertiesToUpdate, boolean inTransaction) {

        Either<List<GroupProperty>, ResponseFormat> result = Either.left(groupPropertiesToUpdate);
        try {
            Optional<GroupDefinition> optionalGroupConnectedToVf = null;
            GroupDefinition currentGroup = null;
            StorageOperationStatus lockResult = graphLockOperation.lockComponent(componentId, componentType.getNodeType());
            if (lockResult != StorageOperationStatus.OK) {
                result = Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(lockResult, componentType), componentId));
            }
            if (result.isLeft()) {
                // VF exist because lock succedded
                Resource vf = (Resource) toscaOperationFacade.getToscaElement(componentId).left().value();
                optionalGroupConnectedToVf =
                        // All groups on resource
                        vf.getGroups().stream().
                        // Filter in group sent is part of VF groups
                                filter(e -> e.getUniqueId().equals(groupUniqueId)).
                                // Collect
                                findAny();
                if (!optionalGroupConnectedToVf.isPresent()) {
                    result = Either.right(componentsUtils.getResponseFormat(ActionStatus.GROUP_IS_MISSING, groupUniqueId, vf.getName(), ComponentTypeEnum.RESOURCE.getValue()));
                }
            }

            if (result.isLeft()) {
                currentGroup = optionalGroupConnectedToVf.get();
                result = validateGroupPropertyAndResetEmptyValue(currentGroup, groupPropertiesToUpdate);
            }
            if (result.isLeft()) {
                result = updateGroupPropertiesValue(componentId, currentGroup, groupPropertiesToUpdate, inTransaction);
                if (result.isRight()) {
                    BeEcompErrorManager.getInstance().logBeSystemError("Update GroupProperties");
                    log.debug("failed to update Vf {}", componentId);
                }
            }

        } catch (Exception e) {
            log.debug("Error in validateAndUpdateGroupProperty {}", e);
            result = Either.right(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR));
        } finally {
            graphLockOperation.unlockComponent(componentId, componentType.getNodeType());
        }
        return result;
    }

    private void resetEmptyValueWithDefaults(List<GroupProperty> groupPropertiesToUpdate, GroupDefinition originalGroup) {
        Map<String, GroupProperty> originalProperties =
                // Stream of original properties from group
                originalGroup.convertToGroupProperties().stream().
                // Collecting to map with name as key
                        collect(Collectors.toMap(e -> e.getName(), e -> e));
        for (GroupProperty gp : groupPropertiesToUpdate) {
            if (StringUtils.isEmpty(gp.getValue())) {
                gp.setValue(originalProperties.get(gp.getName()).getDefaultValue());
            }
        }

    }

    private Either<List<GroupProperty>, ResponseFormat> validateGroupPropertyAndResetEmptyValue(GroupDefinition originalGroup, List<GroupProperty> groupPropertiesToUpdate) {

        Either<List<GroupProperty>, ResponseFormat> ret = validateOnlyValueChanged(groupPropertiesToUpdate, originalGroup);
        if (ret.isLeft()) {
            resetEmptyValueWithDefaults(groupPropertiesToUpdate, originalGroup);
        }
        if (ret.isLeft()) {
            // Validate Type Match Value
            Optional<StorageOperationStatus> optionalError =
                    // Stream of group properties
                    groupPropertiesToUpdate.stream().
                    // Validate each and map to returned Strorage status value
                            map(e -> groupOperation.validateAndUpdatePropertyValue(e)).
                            // Keep only failed result if there is such
                            filter(e -> e != StorageOperationStatus.OK).
                            // collect
                            findFirst();
            if (optionalError.isPresent()) {
                ActionStatus actionStatus = componentsUtils.convertFromStorageResponse(optionalError.get());
                ret = Either.right(componentsUtils.getResponseFormat(actionStatus));
            }

        }
        if (ret.isLeft()) {
            // Validate min max ect...
            ret = validatePropertyBusinessLogic(groupPropertiesToUpdate, originalGroup);
        }

        return ret;
    }

    private Either<List<GroupProperty>, ResponseFormat> validatePropertyBusinessLogic(List<GroupProperty> groupPropertiesToUpdate, GroupDefinition originalGroup) {

        Either<List<GroupProperty>, ResponseFormat> ret = Either.left(groupPropertiesToUpdate);

        Map<PropertyNames, String> nameValueMap = new HashMap<>();
        for (GroupProperty gp : groupPropertiesToUpdate) {
            // Filter out non special properties which does not have Enum
            final PropertyNames gpEnum = PropertyNames.findName(gp.getName());
            if (gpEnum != null) {
                nameValueMap.put(gpEnum, gp.getValue());
            }
        }

        if (!MapUtils.isEmpty(nameValueMap)) {

            if (nameValueMap.containsKey(PropertyNames.INITIAL_COUNT) || nameValueMap.containsKey(PropertyNames.MAX_INSTANCES) || nameValueMap.containsKey(PropertyNames.MIN_INSTANCES)) {

                Map<PropertyNames, String> oldValueMap = prepareMapWithOriginalProperties(originalGroup);

                Either<Boolean, ResponseFormat> eitherValid = validateMinMaxAndInitialCountPropertyLogicVF(nameValueMap, oldValueMap);
                if (eitherValid.isRight()) {
                    ret = Either.right(eitherValid.right().value());
                }
            }
            if (ret.isLeft() && (nameValueMap.containsKey(PropertyNames.VF_MODULE_DESCRIPTION) || nameValueMap.containsKey(PropertyNames.VF_MODULE_LABEL))) {

                Optional<ResponseFormat> optionalError =
                        // Stream of group Properties
                        groupPropertiesToUpdate.stream().
                        // Filter in only properties that needs text validation
                                filter(e -> enumHasValueFilter(e.getName(), enumName -> PropertyNames.findName(enumName), PropertyNames.VF_MODULE_DESCRIPTION, PropertyNames.VF_MODULE_LABEL)).
                                // validate text properties
                                map(e -> validateFreeText(e)).
                                // filter in only errors if exist
                                filter(e -> e.isRight()).
                                // map the Either value to the Error
                                map(e -> e.right().value())
                                // collect
                                .findFirst();
                if (optionalError.isPresent()) {
                    ret = Either.right(optionalError.get());
                }

            }
        }

        return ret;
    }

    private Map<PropertyNames, String> prepareMapWithOriginalProperties(GroupDefinition originalGroup) {
        Map<PropertyNames, String> oldValueMap = new HashMap<>();
        PropertyNames[] propertiesToCheck = new PropertyNames[] { PropertyNames.INITIAL_COUNT, PropertyNames.MAX_INSTANCES, PropertyNames.MIN_INSTANCES };

        for (GroupProperty gp : originalGroup.convertToGroupProperties()) {
            if (enumHasValueFilter(gp.getName(), PropertyNames::findName, propertiesToCheck)) {
                oldValueMap.put(PropertyNames.findName(gp.getName()), gp.getValue());
            }
        }
        if (StringUtils.isEmpty(oldValueMap.get(PropertyNames.MAX_INSTANCES))) {
            oldValueMap.put(PropertyNames.MAX_INSTANCES, String.valueOf(Integer.MAX_VALUE));
        }
        return oldValueMap;
    }

    private Either<List<GroupProperty>, ResponseFormat> validateOnlyValueChanged(List<GroupProperty> groupPropertiesToUpdate, GroupDefinition originalGroup) {

        Either<List<GroupProperty>, ResponseFormat> ret = Either.left(groupPropertiesToUpdate);
        if (CollectionUtils.isEmpty(groupPropertiesToUpdate)) {
            ret = Either.right(componentsUtils.getResponseFormat(ActionStatus.PROPERTY_NOT_FOUND, StringUtils.EMPTY));
        } else if (CollectionUtils.isEmpty(originalGroup.getProperties())) {
            ret = Either.right(componentsUtils.getResponseFormat(ActionStatus.PROPERTY_NOT_FOUND, groupPropertiesToUpdate.get(NumberUtils.INTEGER_ZERO).getName()));
        } else {
            Map<String, GroupProperty> namePropertyMap =
                    // Original Group Properties Stream
                    originalGroup.convertToGroupProperties().stream().
                    // Collect to map with name as key
                            collect(Collectors.toMap(e -> e.getName(), e -> e));

            Optional<GroupProperty> optionalMissingProperty =
                    // Group Properties to be updated Stream
                    groupPropertiesToUpdate.stream().
                    // Filter in property that is not contained in original if there is such
                            filter(e -> !namePropertyMap.containsKey(e.getName())).
                            // collect
                            findFirst();

            if (optionalMissingProperty.isPresent()) {
                ret = Either.right(componentsUtils.getResponseFormat(ActionStatus.PROPERTY_NOT_FOUND, optionalMissingProperty.get().getName()));
            } else {
                Optional<GroupProperty> optionalNonValueChange =
                        // groups to be updated stream
                        groupPropertiesToUpdate.stream().
                        // filter in only properties with non-value (illegal) change
                                filter(e -> !isOnlyGroupPropertyValueChanged(e, namePropertyMap.get(e.getName()))).
                                // Collect
                                findFirst();
                if (optionalNonValueChange.isPresent()) {
                    ret = Either.right(componentsUtils.getResponseFormat(ActionStatus.INVALID_PROPERTY, optionalNonValueChange.get().getName()));

                }
            }

        }
        return ret;
    }

    /**
     * if groupProperty are the same or if only value is different returns true, otherwise returns false.
     *
     * @param groupProperty
     * @param groupProperty2
     * @return
     */
    private boolean isOnlyGroupPropertyValueChanged(GroupProperty groupProperty, GroupProperty groupProperty2) {
        // Create 2 duplicates for groupPropery and reset their values
        try {
            GroupProperty groupPropertyDuplicate = new GroupProperty(groupProperty);
            groupPropertyDuplicate.setValue(null);
            groupPropertyDuplicate.setSchema(null);
            groupPropertyDuplicate.setParentUniqueId(null);
            GroupProperty groupProperty2Duplicate = new GroupProperty(groupProperty2);
            groupProperty2Duplicate.setValue(null);
            groupProperty2Duplicate.setSchema(null);
            groupProperty2Duplicate.setParentUniqueId(null);
            return groupPropertyDuplicate.equals(groupProperty2Duplicate) && StringUtils.equals(groupPropertyDuplicate.getValueUniqueUid(), groupProperty2Duplicate.getValueUniqueUid());
        } catch (Exception e) {
            log.debug("Failed validate group properties. ", e);
            return false;
        }
    }

    /**
     * Validate and update GroupDefinition metadata
     *
     * @param currentGroup
     * @param groupUpdate
     * @return
     **/
    private Either<GroupDefinition, ResponseFormat> validateAndUpdateGroupMetadata(GroupDefinition currentGroup, GroupDefinition groupUpdate) {
        // Check if to update, and update GroupDefinition name.
        Either<Boolean, ResponseFormat> response = validateAndUpdateGroupName(currentGroup, groupUpdate);
        if (response.isRight()) {
            ResponseFormat errorResponse = response.right().value();
            return Either.right(errorResponse);
        }

        // Do not allow to update GroupDefinition version directly.
        String versionUpdated = groupUpdate.getVersion();
        String versionCurrent = currentGroup.getVersion();
        if (versionUpdated != null && !versionCurrent.equals(versionUpdated)) {
            log.info("update Group: recived request to update version to {} the field is not updatable ignoring.", versionUpdated);
        }

        return Either.left(currentGroup);
    }

    /**
     * Validate and update GroupDefinition name
     *
     * @param currentGroup
     * @param groupUpdate
     * @return
     */
    private Either<Boolean, ResponseFormat> validateAndUpdateGroupName(GroupDefinition currentGroup, GroupDefinition groupUpdate) {
        String nameUpdated = groupUpdate.getName();
        String nameCurrent = currentGroup.getName();
        if (!nameCurrent.equals(nameUpdated)) {
            Either<Boolean, ResponseFormat> validatNameResponse = validateGroupName(currentGroup.getName(), groupUpdate.getName() ,true);
            if (validatNameResponse.isRight()) {
                ResponseFormat errorRespons = validatNameResponse.right().value();
                return Either.right(errorRespons);
            }
            currentGroup.setName(groupUpdate.getName());
        }
        return Either.left(true);
    }

    /**
     * Validate that group name to update is valid (same as current group name except for middle part). For example: Current group name: MyResource..MyDesc..Module-1 Group to update: MyResource..MyDesc2..Module-1 Verify that only the second part
     * MyDesc was changed.
     *
     * @param currentGroupName
     * @param groupUpdateName
     * @return
     */
    private Either<Boolean, ResponseFormat> validateGroupName(String currentGroupName, String groupUpdateName , boolean isforceNameModification) {
        try {
            // Check if the group name is in old format.
            if (Pattern.compile(Constants.MODULE_OLD_NAME_PATTERN).matcher(groupUpdateName).matches()) {
                log.error("Group name {} is in old format", groupUpdateName);
                return Either.right(componentsUtils.getResponseFormat(ActionStatus.INVALID_VF_MODULE_NAME, groupUpdateName));
            }

            // Check that name pats 1 and 3 did not changed (only the second
            // part can be changed)
            // But verify before that the current group format is the new one
            if (!Pattern.compile(Constants.MODULE_OLD_NAME_PATTERN).matcher(currentGroupName).matches()) {
                String[] split1 = currentGroupName.split(GROUP_DELIMITER_REGEX);
                String currentResourceName = split1[0];
                String currentCounter = split1[2];

                String[] split2 = groupUpdateName.split(GROUP_DELIMITER_REGEX);
                String groupUpdateResourceName = split2[0];
                String groupUpdateCounter = split2[2];
                if (!isforceNameModification){            //if not forced ,allow name prefix&suffix validation [no changes]
                    if (!currentResourceName.equals(groupUpdateResourceName)) {
                        return Either.right(componentsUtils.getResponseFormat(ActionStatus.INVALID_VF_MODULE_NAME_MODIFICATION, currentResourceName));
                    }

                    if (!currentCounter.equals(groupUpdateCounter)) {
                        return Either.right(componentsUtils.getResponseFormat(ActionStatus.INVALID_VF_MODULE_NAME_MODIFICATION, currentCounter));
                    }
                }

            }

            return Either.left(true);
        } catch (Exception e) {
            log.error("Error valiadting group name", e);
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.INVALID_CONTENT));
        }
    }


    /**
     * associate artifacts to a given group
     *
     * @param componentId
     * @param userId
     * @param componentType
     * @param inTransaction
     * @return
     */
    public Either<GroupDefinitionInfo, ResponseFormat> getGroupWithArtifactsById(ComponentTypeEnum componentType, String componentId, String groupId, String userId, boolean inTransaction) {

        Either<GroupDefinitionInfo, ResponseFormat> result = null;

        // Validate user exist
        Either<User, ResponseFormat> validateUserExists = validateUserExists(userId, GET_GROUP, true);

        if (validateUserExists.isRight()) {
            result = Either.right(validateUserExists.right().value());
            return result;
        }

        // Validate component exist
        org.openecomp.sdc.be.model.Component component = null;
        String realComponentId = componentId;

        try {
            ComponentParametersView componentParametersView = new ComponentParametersView();
            componentParametersView.disableAll();
            componentParametersView.setIgnoreGroups(false);
            componentParametersView.setIgnoreArtifacts(false);
            componentParametersView.setIgnoreUsers(false);

            Either<? extends org.openecomp.sdc.be.model.Component, ResponseFormat> validateComponent = validateComponentExists(realComponentId, componentType, componentParametersView);
            if (validateComponent.isRight()) {
                result = Either.right(validateComponent.right().value());
                return result;
            }
            component = validateComponent.left().value();

            Either<GroupDefinition, StorageOperationStatus> groupEither = findGroupOnComponent(component, groupId);

            if (groupEither.isRight()) {
                log.debug("Faild to find group {} under component {}", groupId, component.getUniqueId());
                BeEcompErrorManager.getInstance().logInvalidInputError(GET_GROUP, "group  " + groupId + " not found under component " + component.getUniqueId(), ErrorSeverity.INFO);
                String componentTypeForResponse = getComponentTypeForResponse(component);
                result = Either.right(componentsUtils.getResponseFormat(ActionStatus.GROUP_IS_MISSING, groupId, component.getSystemName(), componentTypeForResponse));
                return result;
            }
            GroupDefinition group = groupEither.left().value();

            Boolean isBase = null;
            List<GroupProperty> props = group.convertToGroupProperties();
            if (props != null && !props.isEmpty()) {
                Optional<GroupProperty> isBasePropOp = props.stream().filter(p -> p.getName().equals(Constants.IS_BASE)).findAny();
                if (isBasePropOp.isPresent()) {
                    GroupProperty propIsBase = isBasePropOp.get();
                    isBase = Boolean.parseBoolean(propIsBase.getValue());

                } else {
                    BeEcompErrorManager.getInstance().logInvalidInputError(GET_GROUP, "failed to find prop isBase " + component.getNormalizedName(), ErrorSeverity.INFO);
                }
            }

            List<ArtifactDefinitionInfo> artifacts = new ArrayList<>();
            List<ArtifactDefinition> artifactsFromComponent = new ArrayList<>();
            List<String> artifactsIds = group.getArtifacts();

            Map<String, ArtifactDefinition> deploymentArtifacts = null;
            if (MapUtils.isNotEmpty(component.getDeploymentArtifacts())) {
                deploymentArtifacts = component.getDeploymentArtifacts().values().stream().collect(Collectors.toMap(a -> a.getUniqueId(), a -> a));
            }

            if (artifactsIds != null && !artifactsIds.isEmpty()) {
                for (String id : artifactsIds) {
                    if (MapUtils.isEmpty(deploymentArtifacts) || !deploymentArtifacts.containsKey(id)) {
                        log.debug("Failed to get artifact {} . Status is {} ", id, StorageOperationStatus.NOT_FOUND);
                        ResponseFormat responseFormat = componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(StorageOperationStatus.NOT_FOUND));
                        result = Either.right(responseFormat);
                        return result;
                    }
                    artifactsFromComponent.add(deploymentArtifacts.get(id));
                }
                if (!artifactsFromComponent.isEmpty()) {
                    for (ArtifactDefinition artifactDefinition : artifactsFromComponent) {
                        ArtifactDefinitionInfo artifactDefinitionInfo = new ArtifactDefinitionInfo(artifactDefinition);
                        artifacts.add(artifactDefinitionInfo);
                    }
                }

            }
            GroupDefinitionInfo resultInfo = new GroupDefinitionInfo(group);
            resultInfo.setIsBase(isBase);
            if (!artifacts.isEmpty()) {
                resultInfo.setArtifacts(artifacts);
            }
            result = Either.left(resultInfo);

            return result;

        } finally {

            if (!inTransaction) {

                if (result == null || result.isRight()) {
                    log.debug("Going to execute rollback on create group.");
                    titanDao.rollback();
                } else {
                    log.debug("Going to execute commit on create group.");
                    titanDao.commit();
                }

            }

        }

    }

    private Either<GroupDefinition, StorageOperationStatus> findGroupOnComponent(Component component, String groupId) {

        Either<GroupDefinition, StorageOperationStatus> result = null;
        if (CollectionUtils.isNotEmpty(component.getGroups())) {
            Optional<GroupDefinition> foundGroup = component.getGroups().stream().filter(g -> g.getUniqueId().equals(groupId)).findFirst();
            if (foundGroup.isPresent()) {
                result = Either.left(foundGroup.get());
            }
        }
        if (result == null) {
            result = Either.right(StorageOperationStatus.NOT_FOUND);
        }
        return result;
    }

    public String getAsString(List<String> list) {

        if (list == null || list.isEmpty()) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        list.forEach(p -> builder.append(p + ","));

        String result = builder.toString();
        return result.substring(0, result.length());

    }


    private Either<List<GroupProperty>, ResponseFormat> updateGroupPropertiesValue(String componentId, GroupDefinition currentGroup, List<GroupProperty> groupPropertyToUpdate, boolean inTransaction) {
        Either<List<GroupProperty>, ResponseFormat> result;

        Either<List<GroupProperty>, StorageOperationStatus> eitherUpdate = groupsOperation.updateGroupPropertiesOnComponent(componentId, currentGroup, groupPropertyToUpdate);
        if (eitherUpdate.isRight()) {
            ActionStatus actionStatus = componentsUtils.convertFromStorageResponse(eitherUpdate.right().value());
            result = Either.right(componentsUtils.getResponseFormat(actionStatus));
        } else {
            result = Either.left(eitherUpdate.left().value());
        }
        return result;
    }

    public Either<Boolean, ResponseFormat> validateGenerateVfModuleGroupNames(List<ArtifactTemplateInfo> allGroups, String resourceSystemName, int startGroupCounter) {
        Either<Boolean, ResponseFormat> validateGenerateGroupNamesRes = Either.left(true);
        Collections.sort(allGroups, (art1, art2) -> ArtifactTemplateInfo.compareByGroupName(art1, art2));
        for (ArtifactTemplateInfo group : allGroups) {
            Either<String, ResponseFormat> validateGenerateGroupNameRes = validateGenerateVfModuleGroupName(resourceSystemName, group.getDescription(), startGroupCounter++);
            if (validateGenerateGroupNameRes.isRight()) {
                validateGenerateGroupNamesRes = Either.right(validateGenerateGroupNameRes.right().value());
                break;
            }
            group.setGroupName(validateGenerateGroupNameRes.left().value());
        }
        return validateGenerateGroupNamesRes;
    }

    /**
     * Generate module name from resourceName, description and counter
     *
     * @param resourceSystemName
     * @param description
     * @param groupCounter
     * @return
     */
    private Either<String, ResponseFormat> validateGenerateVfModuleGroupName(String resourceSystemName, String description, int groupCounter) {
        Either<String, ResponseFormat> validateGenerateGroupNameRes;
        if (resourceSystemName != null && description != null && Pattern.compile(Constants.MODULE_DESC_PATTERN).matcher(description).matches()) {
            final String fileName = description.replaceAll(GROUP_DELIMITER_REGEX, "\\.");
            validateGenerateGroupNameRes = Either.left(String.format(Constants.MODULE_NAME_FORMAT, resourceSystemName, FilenameUtils.removeExtension(fileName), groupCounter));
        } else {
            validateGenerateGroupNameRes = Either.right(componentsUtils.getResponseFormat(ActionStatus.INVALID_VF_MODULE_NAME));
        }
        return validateGenerateGroupNameRes;
    }

    public Either<Map<String, GroupDefinition>, ResponseFormat> validateUpdateVfGroupNames(Map<String, GroupDefinition> groups, String resourceSystemName) {

        Map<String, GroupDefinition> updatedNamesGroups = new HashMap<>();
        Either<Map<String, GroupDefinition>, ResponseFormat> result = Either.left(updatedNamesGroups);
        for (Entry<String, GroupDefinition> groupEntry : groups.entrySet()) {
            GroupDefinition curGroup = groupEntry.getValue();
            String groupType = curGroup.getType();
            String groupName = groupEntry.getKey();
            int counter;
            String description;
            Either<String, ResponseFormat> newGroupNameRes;
            if (groupType.equals(Constants.DEFAULT_GROUP_VF_MODULE) && !Pattern.compile(Constants.MODULE_NEW_NAME_PATTERN).matcher(groupName).matches()) {

                if (Pattern.compile(Constants.MODULE_OLD_NAME_PATTERN).matcher(groupEntry.getKey()).matches()) {
                    counter = Integer.parseInt(groupEntry.getKey().split(Constants.MODULE_NAME_DELIMITER)[1]);
                    description = curGroup.getDescription();
                } else {
                    counter = getNextVfModuleNameCounter(updatedNamesGroups);
                    description = groupName;
                }
                newGroupNameRes = validateGenerateVfModuleGroupName(resourceSystemName, description, counter);
                if (newGroupNameRes.isRight()) {
                    log.debug("Failed to generate new vf module group name. Status is {} ", newGroupNameRes.right().value());
                    result = Either.right(newGroupNameRes.right().value());
                    break;
                }
                groupName = newGroupNameRes.left().value();
                curGroup.setName(groupName);
            }
            updatedNamesGroups.put(groupName, curGroup);
        }
        return result;
    }

    public int getNextVfModuleNameCounter(Map<String, GroupDefinition> groups) {
        int counter = 0;
        if (groups != null && !groups.isEmpty()) {
            counter = getNextVfModuleNameCounter(groups.values());
        }
        return counter;
    }

    public int getNextVfModuleNameCounter(Collection<GroupDefinition> groups) {
        int counter = 0;
        if (groups != null && !groups.isEmpty()) {
            List<Integer> counters = groups.stream().filter(group -> Pattern.compile(Constants.MODULE_NEW_NAME_PATTERN).matcher(group.getName()).matches() || Pattern.compile(Constants.MODULE_OLD_NAME_PATTERN).matcher(group.getName()).matches())
                    .map(group -> Integer.parseInt(group.getName().split(Constants.MODULE_NAME_DELIMITER)[1])).collect(toList());
            counter = (counters == null || counters.isEmpty()) ? 0 : counters.stream().max((a, b) -> Integer.compare(a, b)).get() + 1;
        }
        return counter;
    }

    public Either<List<GroupDefinition>, ResponseFormat> validateUpdateVfGroupNamesOnGraph(List<GroupDefinition> groups, Component component, boolean inTransaction) {
        List<GroupDefinition> updatedGroups = new ArrayList<>();
        Either<List<GroupDefinition>, ResponseFormat> result = Either.left(updatedGroups);

        for (GroupDefinition group : groups) {
            String groupType = group.getType();
            String oldGroupName = group.getName();
            String newGroupName;
            Either<String, ResponseFormat> newGroupNameRes;
            int counter;
            if (groupType.equals(Constants.DEFAULT_GROUP_VF_MODULE) && Pattern.compile(Constants.MODULE_OLD_NAME_PATTERN).matcher(oldGroupName).matches()) {
                counter = Integer.parseInt(group.getName().split(Constants.MODULE_NAME_DELIMITER)[1]);
                newGroupNameRes = validateGenerateVfModuleGroupName(component.getSystemName(), group.getDescription(), counter);
                if (newGroupNameRes.isRight()) {
                    log.debug("Failed to generate new vf module group name. Status is {} ", newGroupNameRes.right().value());
                    result = Either.right(newGroupNameRes.right().value());
                    break;
                }
                newGroupName = newGroupNameRes.left().value();
                group.setName(newGroupName);

            }
            updatedGroups.add(group);

        }

        result = Either.left(updatedGroups);
        return result;
    }


    public Either<GroupDefinitionInfo, ResponseFormat> getGroupInstWithArtifactsById(ComponentTypeEnum componentType, String componentId, String componentInstanceId, String groupInstId, String userId, boolean inTransaction) {
        Either<GroupDefinitionInfo, ResponseFormat> result = null;

        // Validate user exist
        Either<User, ResponseFormat> validateUserExists = validateUserExists(userId, UPDATE_GROUP, true);

        if (validateUserExists.isRight()) {
            result = Either.right(validateUserExists.right().value());
            return result;
        }

        // Validate component exist
        org.openecomp.sdc.be.model.Component component = null;
        String realComponentId = componentId;

        try {
            ComponentParametersView componentParametersView = new ComponentParametersView();
            componentParametersView.disableAll();
            componentParametersView.setIgnoreUsers(false);
            componentParametersView.setIgnoreComponentInstances(false);
            componentParametersView.setIgnoreArtifacts(false);

            Either<? extends org.openecomp.sdc.be.model.Component, ResponseFormat> validateComponent = validateComponentExists(realComponentId, componentType, componentParametersView);
            if (validateComponent.isRight()) {
                result = Either.right(validateComponent.right().value());
                return result;
            }
            component = validateComponent.left().value();
            Either<ImmutablePair<ComponentInstance, GroupInstance>, StorageOperationStatus> findComponentInstanceAndGroupInstanceRes = findComponentInstanceAndGroupInstanceOnComponent(component, componentInstanceId, groupInstId);

            if (findComponentInstanceAndGroupInstanceRes.isRight()) {
                log.debug("Failed to get group {} . Status is {} ", groupInstId, findComponentInstanceAndGroupInstanceRes.right().value());
                ResponseFormat responseFormat = componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(findComponentInstanceAndGroupInstanceRes.right().value()));
                result = Either.right(responseFormat);
                return result;
            }

            GroupInstance group = findComponentInstanceAndGroupInstanceRes.left().value().getRight();

            Boolean isBase = null;
            List<? extends GroupProperty> props = group.convertToGroupInstancesProperties();
            if (props != null && !props.isEmpty()) {
                Optional<? extends GroupProperty> isBasePropOp = props.stream().filter(p -> p.getName().equals(Constants.IS_BASE)).findAny();
                if (isBasePropOp.isPresent()) {
                    GroupProperty propIsBase = isBasePropOp.get();
                    isBase = Boolean.parseBoolean(propIsBase.getValue());

                } else {
                    BeEcompErrorManager.getInstance().logInvalidInputError(GET_GROUP, "failed to find prop isBase " + component.getNormalizedName(), ErrorSeverity.INFO);
                }
            }

            List<ArtifactDefinitionInfo> artifacts = new ArrayList<>();
            List<String> artifactsIds = group.getArtifacts();
            if (artifactsIds != null && !artifactsIds.isEmpty()) {

                List<ComponentInstance> instances = component.getComponentInstances();
                if (instances != null) {
                    Optional<ComponentInstance> findFirst = instances.stream().filter(i -> i.getUniqueId().equals(componentInstanceId)).findFirst();
                    if (findFirst.isPresent()) {
                        ComponentInstance ci = findFirst.get();
                        Map<String, ArtifactDefinition> deploymentArtifacts = ci.getDeploymentArtifacts();
                        for (String id : artifactsIds) {
                            Optional<ArtifactDefinition> artOp = deploymentArtifacts.values().stream().filter(a -> a.getUniqueId().equals(id)).findFirst();
                            if (artOp.isPresent()) {
                                artifacts.add(new ArtifactDefinitionInfo(artOp.get()));
                            }
                        }
                        List<String> instArtifactsIds = group.getGroupInstanceArtifacts();
                        for (String id : instArtifactsIds) {
                            Optional<ArtifactDefinition> artOp = deploymentArtifacts.values().stream().filter(a -> a.getUniqueId().equals(id)).findFirst();
                            if (artOp.isPresent()) {
                                artifacts.add(new ArtifactDefinitionInfo(artOp.get()));
                            }
                        }
                    }

                }
            }
            GroupDefinitionInfo resultInfo = new GroupDefinitionInfo(group);
            resultInfo.setIsBase(isBase);
            if (!artifacts.isEmpty()) {
                resultInfo.setArtifacts(artifacts);
            }
            result = Either.left(resultInfo);

            return result;

        } finally {

            if (!inTransaction) {

                if (result == null || result.isRight()) {
                    log.debug("Going to execute rollback on create group.");
                    titanDao.rollback();
                } else {
                    log.debug("Going to execute commit on create group.");
                    titanDao.commit();
                }

            }

        }
    }

    private Either<ImmutablePair<ComponentInstance, GroupInstance>, StorageOperationStatus> findComponentInstanceAndGroupInstanceOnComponent(Component component, String componentInstanceId, String groupInstId) {

        Either<ImmutablePair<ComponentInstance, GroupInstance>, StorageOperationStatus> result = null;
        if (CollectionUtils.isNotEmpty(component.getComponentInstances())) {
            Optional<GroupInstance> foundGroup;
            Optional<ComponentInstance> foundComponent = component.getComponentInstances().stream().filter(ci -> ci.getUniqueId().equals(componentInstanceId)).findFirst();
            if (foundComponent.isPresent() && CollectionUtils.isNotEmpty(foundComponent.get().getGroupInstances())) {
                foundGroup = foundComponent.get().getGroupInstances().stream().filter(gi -> gi.getUniqueId().equals(groupInstId)).findFirst();
                if (foundGroup.isPresent()) {
                    result = Either.left(new ImmutablePair<>(foundComponent.get(), foundGroup.get()));
                }
            }
        }
        if (result == null) {
            result = Either.right(StorageOperationStatus.NOT_FOUND);
        }
        return result;
    }

    private int getLatestIntProperty(Map<PropertyNames, String> newValues, Map<PropertyNames, String> parentValues, PropertyNames propertyKey) {
        String value;
        if (newValues.containsKey(propertyKey)) {
            value = newValues.get(propertyKey);
        } else {
            value = parentValues.get(propertyKey);
        }
        return Integer.valueOf(value);
    }

    private boolean isPropertyChanged(Map<PropertyNames, String> newValues, Map<PropertyNames, String> parentValues, final PropertyNames minInstances) {
        return newValues.containsKey(minInstances) && !newValues.get(minInstances).equals(parentValues.get(minInstances));
    }

    private Either<Boolean, ResponseFormat> validateMinMaxAndInitialCountPropertyLogicVF(Map<PropertyNames, String> newValues, Map<PropertyNames, String> parentValues) {

        int latestMaxInstances = getLatestIntProperty(newValues, parentValues, PropertyNames.MAX_INSTANCES);
        int latestInitialCount = getLatestIntProperty(newValues, parentValues, PropertyNames.INITIAL_COUNT);
        int latestMinInstances = getLatestIntProperty(newValues, parentValues, PropertyNames.MIN_INSTANCES);
        Either<Boolean, ResponseFormat> result = Either.left(true);

        if (isPropertyChanged(newValues, parentValues, PropertyNames.INITIAL_COUNT) && result.isLeft()
                && (latestInitialCount > latestMaxInstances || latestInitialCount < latestMinInstances)) {
            result = Either.right(componentsUtils.getResponseFormat(ActionStatus.INVALID_GROUP_INITIAL_COUNT_PROPERTY_VALUE, PropertyNames.INITIAL_COUNT.getPropertyName(), String.valueOf(latestMinInstances), String.valueOf(latestMaxInstances)));
        }
        if (isPropertyChanged(newValues, parentValues, PropertyNames.MAX_INSTANCES) && result.isLeft() &&
                latestMaxInstances < latestInitialCount) {
            result = Either.right(componentsUtils.getResponseFormat(ActionStatus.INVALID_GROUP_PROPERTY_VALUE_LOWER_HIGHER, PropertyNames.MAX_INSTANCES.getPropertyName(), "higher", String.valueOf(latestInitialCount)));
        }
        if (isPropertyChanged(newValues, parentValues, PropertyNames.MIN_INSTANCES) &&
                result.isLeft() && latestMinInstances > latestInitialCount) {
            result = Either.right(componentsUtils.getResponseFormat(ActionStatus.INVALID_GROUP_PROPERTY_VALUE_LOWER_HIGHER, PropertyNames.MIN_INSTANCES.getPropertyName(), "lower", String.valueOf(latestInitialCount)));
        }
        return result;
    }

    private Either<Boolean, ResponseFormat> validateMinMaxAndInitialCountPropertyLogic(Map<PropertyNames, String> newValues, Map<PropertyNames, String> currValues, Map<PropertyNames, String> parentValues) {

        Either<Boolean, ResponseFormat> result;
        for (Entry<PropertyNames, String> entry : newValues.entrySet()) {
            PropertyNames currPropertyName = entry.getKey();
            if (currPropertyName == PropertyNames.MIN_INSTANCES) {
                String minValue = parentValues.get(PropertyNames.MIN_INSTANCES);
                String maxValue = newValues.containsKey(PropertyNames.INITIAL_COUNT) ? newValues.get(PropertyNames.MAX_INSTANCES) : currValues.get(PropertyNames.INITIAL_COUNT);
                result = validateValueInRange(new ImmutablePair<PropertyNames, String>(currPropertyName, entry.getValue()), new ImmutablePair<PropertyNames, String>(PropertyNames.MIN_INSTANCES, minValue),
                        new ImmutablePair<PropertyNames, String>(PropertyNames.MAX_INSTANCES, maxValue));
                if (result.isRight()) {
                    return result;
                }
            } else if (currPropertyName == PropertyNames.INITIAL_COUNT) {
                String minValue = newValues.containsKey(PropertyNames.MIN_INSTANCES) ? newValues.get(PropertyNames.MIN_INSTANCES) : currValues.get(PropertyNames.MIN_INSTANCES);
                String maxValue = newValues.containsKey(PropertyNames.MAX_INSTANCES) ? newValues.get(PropertyNames.MAX_INSTANCES) : currValues.get(PropertyNames.MAX_INSTANCES);
                result = validateValueInRange(new ImmutablePair<PropertyNames, String>(currPropertyName, entry.getValue()), new ImmutablePair<PropertyNames, String>(PropertyNames.MIN_INSTANCES, minValue),
                        new ImmutablePair<PropertyNames, String>(PropertyNames.MAX_INSTANCES, maxValue));
                if (result.isRight()) {
                    return result;
                }
            } else if (currPropertyName == PropertyNames.MAX_INSTANCES) {
                String minValue = newValues.containsKey(PropertyNames.INITIAL_COUNT) ? newValues.get(PropertyNames.MIN_INSTANCES) : currValues.get(PropertyNames.INITIAL_COUNT);
                String maxValue = parentValues.get(PropertyNames.MAX_INSTANCES);
                result = validateValueInRange(new ImmutablePair<PropertyNames, String>(currPropertyName, entry.getValue()), new ImmutablePair<PropertyNames, String>(PropertyNames.MIN_INSTANCES, minValue),
                        new ImmutablePair<PropertyNames, String>(PropertyNames.MAX_INSTANCES, maxValue));
                if (result.isRight()) {
                    return result;
                }
            }
        }
        return Either.left(true);
    }

    private Either<Boolean, ResponseFormat> validateValueInRange(ImmutablePair<PropertyNames, String> newValue, ImmutablePair<PropertyNames, String> min, ImmutablePair<PropertyNames, String> max) {
        Either<Boolean, ResponseFormat> result;
        final String warnMessage = "Failed to validate {} as property value of {}. It must be not higher than {}, and not lower than {}.";
        int newValueInt = parseIntValue(newValue.getValue(), newValue.getKey());
        int minInt = parseIntValue(min.getValue(), min.getKey());
        int maxInt = parseIntValue(max.getValue(), max.getKey());
        if (newValueInt < 0 || minInt < 0 || maxInt < 0) {
            result = Either.right(componentsUtils.getResponseFormat(ActionStatus.INVALID_PROPERTY));
        } else if (newValueInt < minInt || newValueInt > maxInt) {
            log.debug(warnMessage, newValue.getValue(), newValue.getKey().getPropertyName(), min.getValue(), max.getValue());
            result = Either
                    .right(componentsUtils.getResponseFormat(ActionStatus.INVALID_GROUP_MIN_MAX_INSTANCES_PROPERTY_VALUE, newValue.getKey().getPropertyName(), maxInt == Integer.MAX_VALUE ? Constants.UNBOUNDED : max.getValue(), min.getValue()));
        } else {
            result = Either.left(true);
        }
        return result;
    }

    private int parseIntValue(String value, PropertyNames propertyName) {
        int result;
        if (propertyName == PropertyNames.MAX_INSTANCES) {
            result = convertIfUnboundMax(value);
        } else if (NumberUtils.isNumber(value)) {
            result = Integer.parseInt(value);
        } else {
            result = -1;
        }
        return result;
    }

    /**
     * validates received new property values and updates group instance in case of success
     *
     * @param oldGroupInstance
     * @param newProperties
     * @param inTransaction
     * @return
     */
    public Either<GroupInstance, ResponseFormat> validateAndUpdateGroupInstancePropertyValues(String componentId, String instanceId, GroupInstance oldGroupInstance, List<GroupInstanceProperty> newProperties, boolean inTransaction) {

        Either<GroupInstance, ResponseFormat> actionResult = null;
        Either<GroupInstance, StorageOperationStatus> updateGroupInstanceResult = null;
        Either<List<GroupInstanceProperty>, ResponseFormat> validateRes = validateReduceGroupInstancePropertiesBeforeUpdate(oldGroupInstance, newProperties);
        if (validateRes.isRight()) {
            log.debug("Failed to validate group instance {} properties before update. ", oldGroupInstance.getName());
            actionResult = Either.right(validateRes.right().value());
        }
        if (actionResult == null) {
            List<GroupInstanceProperty> validatedReducedNewProperties = validateRes.left().value();
            updateGroupInstanceResult = groupsOperation.updateGroupInstancePropertyValuesOnGraph(componentId, instanceId, oldGroupInstance, validatedReducedNewProperties);
            if (updateGroupInstanceResult.isRight()) {
                log.debug("Failed to update group instance {} property values. ", oldGroupInstance.getName());
                actionResult = Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(updateGroupInstanceResult.right().value())));
            }
        }
        if (actionResult == null) {
            actionResult = Either.left(updateGroupInstanceResult.left().value());
        }
        return actionResult;
    }

    private Either<List<GroupInstanceProperty>, ResponseFormat> validateReduceGroupInstancePropertiesBeforeUpdate(GroupInstance oldGroupInstance, List<GroupInstanceProperty> newProperties) {

        Either<Boolean, ResponseFormat> validationRes = null;
        Either<List<GroupInstanceProperty>, ResponseFormat> actionResult;
        Map<String, GroupInstanceProperty> existingProperties = oldGroupInstance.convertToGroupInstancesProperties().stream().collect(Collectors.toMap(p -> p.getName(), p -> p));
        Map<PropertyNames, String> newPropertyValues = new EnumMap<>(PropertyNames.class);
        List<GroupInstanceProperty> reducedProperties = new ArrayList<>();
        String currPropertyName;
        try {
            for (GroupInstanceProperty currNewProperty : newProperties) {
                currPropertyName = currNewProperty.getName();
                validationRes = handleAndAddProperty(reducedProperties, newPropertyValues, currNewProperty, existingProperties.get(currPropertyName));
                if (validationRes.isRight()) {
                    log.debug("Failed to handle property {} of group instance {}. ", currPropertyName, oldGroupInstance.getName());
                    break;
                }
            }
            if (validationRes == null || validationRes.isLeft()) {
                Map<PropertyNames, String> existingPropertyValues = new EnumMap<>(PropertyNames.class);
                Map<PropertyNames, String> parentPropertyValues = new EnumMap<>(PropertyNames.class);
                fillValuesAndParentValuesFromExistingProperties(existingProperties, existingPropertyValues, parentPropertyValues);
                validationRes = validateMinMaxAndInitialCountPropertyLogic(newPropertyValues, existingPropertyValues, parentPropertyValues);
            }
            if (validationRes.isLeft()) {
                actionResult = Either.left(reducedProperties);
            } else {
                actionResult = Either.right(validationRes.right().value());
            }
        } catch (Exception e) {
            log.error("Exception occured during validation and reducing group instance properties. The message is {}", e.getMessage(), e);
            actionResult = Either.right(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR));
        }
        return actionResult;
    }

    private void fillValuesAndParentValuesFromExistingProperties(Map<String, GroupInstanceProperty> existingProperties, Map<PropertyNames, String> propertyValues, Map<PropertyNames, String> parentPropertyValues) {
        PropertyNames[] allPropertyNames = PropertyNames.values();
        for (PropertyNames name : allPropertyNames) {
            if (isUpdatable(name)) {
                propertyValues.put(name, String.valueOf(existingProperties.get(name.getPropertyName()).getValue()));
                parentPropertyValues.put(name, String.valueOf(existingProperties.get(name.getPropertyName()).getParentValue()));
            }
        }
    }

    private Either<Boolean, ResponseFormat> handleAndAddProperty(List<GroupInstanceProperty> reducedProperties, Map<PropertyNames, String> newPropertyValues, GroupInstanceProperty currNewProperty, GroupInstanceProperty currExistingProperty) {

        Either<Boolean, ResponseFormat> validationRes = null;
        String currPropertyName = currNewProperty.getName();
        PropertyNames propertyName = PropertyNames.findName(currPropertyName);
        try {
            if (currExistingProperty == null) {
                log.warn("The value of property with the name {} cannot be updated. The property not found on group instance. ", currPropertyName);
            } else if (isUpdatable(propertyName)) {
                validationRes = validateAndUpdatePropertyValue(currNewProperty, currExistingProperty);
                if (validationRes.isRight()) {
                    log.debug("Failed to validate property value {} of property {}. ", currNewProperty.getValue(), currPropertyName);
                } else {
                    addPropertyUpdatedValues(reducedProperties, propertyName, newPropertyValues, currNewProperty, currExistingProperty);
                }
            } else {
                validateImmutableProperty(currExistingProperty, currNewProperty);
            }
            if (validationRes == null) {
                validationRes = Either.left(true);
            }
        } catch (Exception e) {
            log.error("Exception occured during handle and adding property. The message is {}", e.getMessage(), e);
            validationRes = Either.right(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR));
        }
        return validationRes;
    }

    private boolean isUpdatable(PropertyNames updatablePropertyName) {
        return updatablePropertyName != null && updatablePropertyName.getUpdateBehavior().getLevelNumber() >= GroupInstancePropertyValueUpdateBehavior.UPDATABLE_ON_SERVICE_LEVEL.getLevelNumber();
    }

    private void addPropertyUpdatedValues(List<GroupInstanceProperty> reducedProperties, PropertyNames propertyName, Map<PropertyNames, String> newPropertyValues, GroupInstanceProperty newProperty, GroupInstanceProperty existingProperty) {

        String newValue = newProperty.getValue();
        if (!newValue.equals(String.valueOf(existingProperty.getValue()))) {
            newProperty.setValueUniqueUid(existingProperty.getValueUniqueUid());
            reducedProperties.add(newProperty);
        }
        if (!isEmptyMinInitialCountValue(propertyName, newValue)) {
            newPropertyValues.put(propertyName, newValue);
        }
    }

    private boolean isEmptyMinInitialCountValue(PropertyNames propertyName, String newValue) {
        boolean result = false;
        if ((propertyName == PropertyNames.MIN_INSTANCES || propertyName == PropertyNames.INITIAL_COUNT) && !NumberUtils.isNumber(newValue)) {
            result = true;
        }
        return result;
    }

    private int convertIfUnboundMax(String value) {

        int result;
        if (!NumberUtils.isNumber(value)) {
            result = Integer.MAX_VALUE;
        } else {
            result = Integer.parseInt(value);
        }
        return result;
    }

    private Either<Boolean, ResponseFormat> validateAndUpdatePropertyValue(GroupInstanceProperty newProperty, GroupInstanceProperty existingProperty) {

        Either<Boolean, ResponseFormat> validationRes = null;
        String parentValue = existingProperty.getParentValue();

        newProperty.setParentValue(parentValue);
        if (StringUtils.isEmpty(newProperty.getValue())) {
            newProperty.setValue(parentValue);
        }
        if (StringUtils.isEmpty(existingProperty.getValue())) {
            existingProperty.setValue(parentValue);
        }
        StorageOperationStatus status = groupOperation.validateAndUpdatePropertyValue(newProperty);
        if (status != StorageOperationStatus.OK) {
            log.debug("Failed to validate property value {} of property with name {}. Status is {}. ", newProperty.getValue(), newProperty.getName(), status);
            validationRes = Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(status)));
        }
        if (validationRes == null) {
            validationRes = Either.left(true);
        }
        return validationRes;
    }

    private void validateImmutableProperty(GroupProperty oldProperty, GroupProperty newProperty) {
        if (oldProperty.getValue() == null && newProperty.getValue() != null || oldProperty.getValue() != null && !oldProperty.getValue().equals(newProperty.getValue())) {
            log.warn("The value of property with the name {} cannot be updated on service level. Going to ignore new property value {}. ", oldProperty.getName(), newProperty.getValue());
        }
    }

    public GroupDefinition createGroup(String groupType, ComponentTypeEnum componentTypeEnum, String componentId,
                                       String userId) {

        try {
            Component component = accessValidations.validateUserCanWorkOnComponentAndLockIt(componentTypeEnum, componentId, userId, CREATE_GROUP);

            validateGroupTypePerComponent(groupType, component);

            GroupTypeDefinition groupTypeDefinition = groupTypeOperation.getLatestGroupTypeByType(groupType, false)
                    .left()
                    .on(se -> onGroupTypeNotFound(component));

            boolean isFirstGroup = component.getGroups() == null;
            GroupDefinition groupDefinition = new GroupDefinition();
            groupDefinition.setType(groupType);

            //find next valid counter
            int nextCounter = 0;
            if (!isFirstGroup) {
                nextCounter = getNewGroupCounter(component);
            }
            String name = TopologyTemplateOperation.buildSubComponentName(component.getName(), groupType, nextCounter);
            groupDefinition.setName(name);

            //Add default type properties
            List<PropertyDefinition> groupTypeProperties = groupTypeDefinition.getProperties();
            List<GroupProperty> properties = groupTypeProperties.stream()
                    .map(GroupProperty::new)
                    .collect(toList());
            groupDefinition.convertFromGroupProperties(properties);

            List<GroupDefinition> gdList;
            if (isFirstGroup) {
                gdList = createGroups(component, Arrays.asList(groupDefinition))
                        .left()
                        .on(this::onFailedGroupDBOperation);
            } else {
                gdList = addGroups(component, Arrays.asList(groupDefinition))
                        .left()
                        .on(this::onFailedGroupDBOperation);
            }
            return gdList.get(0);
        } finally {
            titanDao.commit();
            graphLockOperation.unlockComponent(componentId, componentTypeEnum.getNodeType());
        }
    }

    private void validateGroupTypePerComponent(String groupType, Component component) {
        String specificType = component.getComponentMetadataDefinition().getMetadataDataDefinition().getActualComponentType();
        if (!component.isTopologyTemplate()) {
            throw new ComponentException(ActionStatus.GROUP_TYPE_ILLEGAL_PER_COMPONENT, groupType,
                    specificType);
        }
        Map<String, Set<String>> excludedGroupTypesMap = ConfigurationManager.getConfigurationManager().getConfiguration()
                .getExcludedGroupTypesMapping();

        if (MapUtils.isNotEmpty(excludedGroupTypesMap) && StringUtils.isNotEmpty(specificType)) {
            Set<String> excludedGroupTypesPerComponent = excludedGroupTypesMap.get(specificType);
            if (excludedGroupTypesPerComponent!=null && excludedGroupTypesPerComponent.contains(groupType)) {
                throw new ComponentException(ActionStatus.GROUP_TYPE_ILLEGAL_PER_COMPONENT, groupType, specificType);
            }
        }
    }

    private int getNewGroupCounter(Component component) {
        List<String> existingNames = component.getGroups()
                .stream()
                .map(GroupDataDefinition::getName)
                .collect(toList());
        List<String> existingIds = component.getGroups()
                .stream()
                .map(GroupDataDefinition::getUniqueId)
                .collect(toList());
        existingIds.addAll(existingNames);

        return Utils.getNextCounter(existingIds);
    }

    public GroupDefinition updateGroup(ComponentTypeEnum componentTypeEnum, String componentId, String groupId,
                                       String userId, GroupDefinition updatedGroup) {
        try {
            Component component = accessValidations.validateUserCanWorkOnComponentAndLockIt(componentTypeEnum, componentId, userId, UPDATE_GROUP);

            GroupDefinition existingGroup = findGroupOnComponent(component, groupId)
                    .left()
                    .on(se -> onGroupNotFoundInComponentError(component, groupId));

            String existingGroupName = existingGroup.getName();
            String updatedGroupName = updatedGroup.getName();
            assertNewNameIsValidAndUnique(existingGroupName, updatedGroupName, component);
            existingGroup.setName(updatedGroupName);

            return updateGroup(component, existingGroup, existingGroupName)
                    .left()
                    .on(this::onFailedUpdateGroupDBOperation);
        } finally {
            titanDao.commit();
            graphLockOperation.unlockComponent(componentId, componentTypeEnum.getNodeType());
        }

    }

    private void assertNewNameIsValidAndUnique(String currentGroupName, String updatedGroupName, Component component) {
        if (!ValidationUtils.validateResourceInstanceNameLength(updatedGroupName)) {
            throw new ComponentException(ActionStatus.EXCEEDS_LIMIT, "Group Name", ValidationUtils.RSI_NAME_MAX_LENGTH.toString());
        }
        if (!ValidationUtils.validateResourceInstanceName(updatedGroupName)) {
            throw new ComponentException(ActionStatus.INVALID_VF_MODULE_NAME, updatedGroupName);
        }
        if (!ComponentValidations.validateNameIsUniqueInComponent(currentGroupName, updatedGroupName, component)) {
            throw new ComponentException(ActionStatus.COMPONENT_NAME_ALREADY_EXIST, "Group", updatedGroupName);
        }
    }

    public GroupDefinition deleteGroup(ComponentTypeEnum componentTypeEnum, String componentId, String groupId,
                                                                     String userId) {
        try {
            Component component = accessValidations.validateUserCanWorkOnComponentAndLockIt(componentTypeEnum, componentId, userId, DELETE_GROUP);

            GroupDefinition groupDefinition = findGroupOnComponent(component, groupId)
                    .left()
                    .on(se -> onGroupNotFoundInComponentError(component, groupId));

            List<GroupDefinition> gdList = deleteGroups(component, java.util.Arrays.asList(groupDefinition))
                    .left()
                    .on(this::onFailedGroupDBOperation);
            return gdList.get(0);
        } finally {
            titanDao.commit();
            graphLockOperation.unlockComponent(componentId, componentTypeEnum.getNodeType());
        }
    }

    private List<GroupDefinition> onFailedGroupDBOperation(ResponseFormat responseFormat) {
        titanDao.rollback();
        throw new ComponentException(responseFormat);
    }

    private GroupDefinition onFailedUpdateGroupDBOperation(ResponseFormat responseFormat) {
        titanDao.rollback();
        throw new ComponentException(responseFormat);
    }

    private GroupDefinition onGroupNotFoundInComponentError(Component component, String groupId) {
        throw new ComponentException(ActionStatus.GROUP_IS_MISSING, groupId,
                component.getSystemName(), getComponentTypeForResponse(component));
    }

    private GroupTypeDefinition onGroupTypeNotFound(Component component) {
        throw new ComponentException(ActionStatus.GROUP_TYPE_IS_INVALID, component.getSystemName(),
                component.getComponentType().toString());
    }

    private Boolean onFailedToLockComponent(ResponseFormat responseFormat) {
        throw new ComponentException(responseFormat);
    }


    public Either<List<GroupDefinition>, ResponseFormat> createGroups(Component component, final List<GroupDefinition> groupDefinitions) {

        Map<String, GroupDataDefinition> groups = new HashMap<>();
        Either<List<GroupDefinition>, ResponseFormat> result = null;
        Either<List<GroupDefinition>, StorageOperationStatus> createGroupsResult = null;
        Either<Map<String, DataTypeDefinition>, TitanOperationStatus> allDataTypes = dataTypeCache.getAll();
        if (allDataTypes.isRight()) {
            TitanOperationStatus status = allDataTypes.right().value();
            BeEcompErrorManager.getInstance().logInternalFlowError("AddPropertyToGroup", "Failed to add property to group. Status is " + status, ErrorSeverity.ERROR);
            return Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(DaoStatusConverter.convertTitanStatusToStorageStatus(status))));

        }

        // handle groups and convert to tosca data
        if (groupDefinitions != null && !groupDefinitions.isEmpty()) {
            for (GroupDefinition groupDefinition : groupDefinitions) {
                Either<GroupDefinition, ResponseFormat> handleGroupRes = handleGroup(component, groupDefinition, allDataTypes.left().value());
                if (handleGroupRes.isRight()) {
                    result = Either.right(handleGroupRes.right().value());
                    break;
                }
                GroupDefinition handledGroup = handleGroupRes.left().value();
                groups.put(handledGroup.getName(), new GroupDataDefinition(handledGroup));

            }
        }
        if (result == null) {
            createGroupsResult = groupsOperation.createGroups(component, groups);
            if (createGroupsResult.isRight()) {
                result = Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(createGroupsResult.right().value())));
            }
        }
        if (result == null) {
            result = Either.left(createGroupsResult.left().value());
        }
        return result;
    }

    public Either<List<GroupDefinition>, ResponseFormat> addGroups(Component component, final List<GroupDefinition> groupDefinitions) {

        Either<List<GroupDefinition>, ResponseFormat> result = null;
        Either<List<GroupDefinition>, StorageOperationStatus> createGroupsResult = null;
        List<GroupDataDefinition> groups = new ArrayList<>();

        Either<Map<String, DataTypeDefinition>, TitanOperationStatus> allDataTypes = dataTypeCache.getAll();
        if (allDataTypes.isRight()) {
            TitanOperationStatus status = allDataTypes.right().value();
            BeEcompErrorManager.getInstance().logInternalFlowError("AddPropertyToGroup", "Failed to add property to group. Status is " + status, ErrorSeverity.ERROR);
            return Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(DaoStatusConverter.convertTitanStatusToStorageStatus(status))));

        }

        // handle groups and convert to tosca data
        if (groupDefinitions != null && !groupDefinitions.isEmpty()) {
            for (GroupDefinition groupDefinition : groupDefinitions) {
                Either<GroupDefinition, ResponseFormat> handleGroupRes = handleGroup(component, groupDefinition, allDataTypes.left().value());
                if (handleGroupRes.isRight()) {
                    result = Either.right(handleGroupRes.right().value());
                    break;
                }
                GroupDefinition handledGroup = handleGroupRes.left().value();
                groups.add(new GroupDataDefinition(handledGroup));
            }
        }
        if (result == null) {
            createGroupsResult = groupsOperation.addGroups(component, groups);
            if (createGroupsResult.isRight()) {
                result = Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(createGroupsResult.right().value())));
            }
        }
        if (result == null) {
            result = Either.left(createGroupsResult.left().value());
        }
        return result;
    }

    public Either<List<GroupDefinition>, ResponseFormat> deleteGroups(Component component, List<GroupDefinition> groupDefinitions) {

        Either<List<GroupDefinition>, StorageOperationStatus> deleteGroupsResult;

        deleteGroupsResult = groupsOperation.deleteGroups(component, groupDefinitions.stream().map(x -> new GroupDataDefinition(x)).collect(toList()));
        if (deleteGroupsResult.isRight()) {
            return Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(deleteGroupsResult.right().value())));
        }
        return Either.left(deleteGroupsResult.left().value());
    }

    /**
     * Update specific group version
     *
     */
    public Either<List<GroupDefinition>, ResponseFormat> updateGroups(Component component, List<GroupDefinition> groupDefinitions) {

        Either<List<GroupDefinition>, ResponseFormat> result = null;
        Either<List<GroupDefinition>, StorageOperationStatus> createGroupsResult;

        createGroupsResult = groupsOperation.updateGroups(component, groupDefinitions.stream().map(GroupDataDefinition::new).collect(toList()));
        if (createGroupsResult.isRight()) {
            result = Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(createGroupsResult.right().value())));
        }

        if (result == null) {
            result = Either.left(createGroupsResult.left().value());
        }
        return result;
    }

    public Either<GroupDefinition, ResponseFormat> handleGroup(Component component, GroupDefinition groupDefinition, Map<String, DataTypeDefinition> allDAtaTypes) {

        log.trace("Going to create group {}", groupDefinition);
        // 3. verify group not already exist
        String groupDefinitionName = groupDefinition.getName();
        if (groupExistsInComponent(groupDefinitionName, component)) {
            String componentTypeForResponse = getComponentTypeForResponse(component);
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.GROUP_ALREADY_EXIST, groupDefinitionName, component.getNormalizedName(), componentTypeForResponse));
        }
        // 4. verify type of group exist
        String groupType = groupDefinition.getType();
        if (StringUtils.isEmpty(groupType)) {
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.GROUP_MISSING_GROUP_TYPE, groupDefinitionName));
        }
        Either<GroupTypeDefinition, StorageOperationStatus> getGroupType = groupTypeOperation.getLatestGroupTypeByType(groupType, true);
        if (getGroupType.isRight()) {
            StorageOperationStatus status = getGroupType.right().value();
            if (status == StorageOperationStatus.NOT_FOUND) {
                BeEcompErrorManager.getInstance().logInvalidInputError(CREATE_GROUP, "group type " + groupType + " cannot be found", ErrorSeverity.INFO);
                return Either.right(componentsUtils.getResponseFormat(ActionStatus.GROUP_TYPE_IS_INVALID, groupType));
            } else {
                return Either.right(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR));
            }
        }
        // 6. verify the component instances type are allowed according to
        // the member types in the group type
        GroupTypeDefinition groupTypeDefinition = getGroupType.left().value();

        Either<Boolean, ResponseFormat> areValidMembers = verifyComponentInstancesAreValidMembers(component, groupDefinitionName, groupDefinition.getMembers(), groupTypeDefinition.getMembers());

        if (areValidMembers.isRight()) {
            ResponseFormat responseFormat = areValidMembers.right().value();
            return Either.right(responseFormat);
        }
        // 7. verify the artifacts belongs to the component
        Either<Boolean, ResponseFormat> areValidArtifacts = verifyArtifactsBelongsToComponent(component, groupDefinition.getArtifacts(), CREATE_GROUP);
        if (areValidArtifacts.isRight()) {
            ResponseFormat responseFormat = areValidArtifacts.right().value();
            return Either.right(responseFormat);
        }
        List<PropertyDefinition> groupTypeProperties = groupTypeDefinition.getProperties();

        List<GroupProperty> properties = groupDefinition.convertToGroupProperties();
        List<GroupProperty> updatedGroupTypeProperties = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(properties)) {
            if (CollectionUtils.isEmpty(groupTypeProperties)) {
                BeEcompErrorManager.getInstance().logInvalidInputError(ADDING_GROUP, "group type does not have properties", ErrorSeverity.INFO);
                return Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(DaoStatusConverter.convertTitanStatusToStorageStatus(TitanOperationStatus.MATCH_NOT_FOUND))));
            }

            Map<String, PropertyDefinition> groupTypePropertiesMap = groupTypeProperties.stream().collect(Collectors.toMap(p -> p.getName(), p -> p));

            Either<GroupProperty, TitanOperationStatus> addPropertyResult;
            int i = 1;
            for (GroupProperty prop : properties) {
                addPropertyResult = handleProperty(prop, groupTypePropertiesMap.get(prop.getName()), i, allDAtaTypes, groupType);
                if (addPropertyResult.isRight()) {
                    BeEcompErrorManager.getInstance().logInvalidInputError(ADDING_GROUP, "failed to validate property", ErrorSeverity.INFO);
                    return Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(DaoStatusConverter.convertTitanStatusToStorageStatus(addPropertyResult.right().value()))));
                }
                updatedGroupTypeProperties.add(addPropertyResult.left().value());

                i++;
            }
        }
        if (groupDefinition.getUniqueId() == null) {
            String uid = UniqueIdBuilder.buildGroupingUid(component.getUniqueId(), groupDefinitionName);
            groupDefinition.setUniqueId(uid);
        }
        groupDefinition.convertFromGroupProperties(updatedGroupTypeProperties);
        groupDefinition.setInvariantUUID(UniqueIdBuilder.buildInvariantUUID());
        groupDefinition.setGroupUUID(UniqueIdBuilder.generateUUID());
        groupDefinition.setVersion(INITIAL_VERSION);
        groupDefinition.setTypeUid(groupTypeDefinition.getUniqueId());

        return Either.left(groupDefinition);
    }

    private static boolean groupExistsInComponent(String groupDefinitionName, Component component) {
        boolean found = false;
        List<GroupDefinition> groups = component.getGroups();
        if (CollectionUtils.isNotEmpty(groups)) {
            found = groups.stream().filter(p -> p.getName().equalsIgnoreCase(groupDefinitionName)).findFirst().orElse(null) != null;
        }
        return found;
    }

    private Either<GroupProperty, TitanOperationStatus> handleProperty(GroupProperty groupProperty, PropertyDefinition prop, Integer index, Map<String, DataTypeDefinition> allDataTypes, String groupType) {

        if (prop == null) {
            return Either.right(TitanOperationStatus.ILLEGAL_ARGUMENT);
        }

        PropertyDataDefinition propDataDef = prop;
        String propertyType = propDataDef.getType();
        String value = groupProperty.getValue();

        Either<String, TitanOperationStatus> checkInnerType = propertyOperation.checkInnerType(propDataDef);
        if (checkInnerType.isRight()) {
            TitanOperationStatus status = checkInnerType.right().value();
            return Either.right(status);
        }

        String innerType = checkInnerType.left().value();

        log.debug("Before validateAndUpdatePropertyValue");
        Either<Object, Boolean> isValid = propertyOperation.validateAndUpdatePropertyValue(propertyType, value, innerType, allDataTypes);
        log.debug("After validateAndUpdatePropertyValue. isValid = {}", isValid);

        String newValue = value;
        if (isValid.isRight()) {
            Boolean res = isValid.right().value();
            if (!res) {
                return Either.right(TitanOperationStatus.ILLEGAL_ARGUMENT);
            }
        } else {
            Object object = isValid.left().value();
            if (object != null) {
                newValue = object.toString();
            }
        }

        String uniqueId = shouldReconstructUniqueId(groupType) ? UniqueIdBuilder.buildGroupPropertyValueUid(prop.getUniqueId(), index)
                : prop.getUniqueId();

        groupProperty.setUniqueId(uniqueId);
        groupProperty.setValue(newValue);
        groupProperty.setType(prop.getType());
        groupProperty.setDefaultValue(prop.getDefaultValue());
        groupProperty.setDescription(prop.getDescription());
        groupProperty.setSchema(prop.getSchema());
        groupProperty.setPassword(prop.isPassword());
        groupProperty.setParentUniqueId(prop.getUniqueId());

        log.debug("Before adding property value to graph {}", groupProperty);

        return Either.left(groupProperty);
    }

    // For old groups we want to leave indexing of property
    // For new groups we just need the types
    private boolean shouldReconstructUniqueId(String groupType) {
        return Constants.GROUP_TOSCA_HEAT.equals(groupType) || Constants.DEFAULT_GROUP_VF_MODULE.equals(groupType);
    }

}
