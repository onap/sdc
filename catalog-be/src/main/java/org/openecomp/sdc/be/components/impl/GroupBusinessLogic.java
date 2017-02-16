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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.config.BeEcompErrorManager.ErrorSeverity;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.info.ArtifactDefinitionInfo;
import org.openecomp.sdc.be.info.ArtifactTemplateInfo;
import org.openecomp.sdc.be.info.GroupDefinitionInfo;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.ComponentParametersView;
import org.openecomp.sdc.be.model.GroupDefinition;
import org.openecomp.sdc.be.model.GroupProperty;
import org.openecomp.sdc.be.model.GroupTypeDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.ComponentOperation;
import org.openecomp.sdc.be.model.operations.impl.GroupOperation;
import org.openecomp.sdc.be.model.operations.impl.GroupTypeOperation;
import org.openecomp.sdc.be.model.operations.impl.ResourceOperation;
import org.openecomp.sdc.be.model.operations.impl.UniqueIdBuilder;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.config.EcompErrorName;
import org.openecomp.sdc.exception.ResponseFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fj.data.Either;

@org.springframework.stereotype.Component("groupBusinessLogic")
public class GroupBusinessLogic extends BaseBusinessLogic {

	public static String INITIAL_VERSION = "1";

	private static final String CREATE_GROUP = "CreateGroup";

	private static final String UPDATE_GROUP = "UpdateGroup";

	private static final String GET_GROUP = "GetGroup";

	private static Logger log = LoggerFactory.getLogger(GroupBusinessLogic.class.getName());

	public GroupBusinessLogic() {

	}

	@javax.annotation.Resource
	private GroupTypeOperation groupTypeOperation;

	@javax.annotation.Resource
	private GroupOperation groupOperation;

	/**
	 * 
	 * 1. validate user exist
	 * 
	 * 2. validate component can be edited
	 * 
	 * 3. verify group not already exist
	 * 
	 * 4. verify type of group exist
	 * 
	 * 5. verify Component instances exist under the component
	 * 
	 * 6. verify the component instances type are allowed according to the member types in the group type
	 * 
	 * 7. verify the artifacts belongs to the component
	 * 
	 * @param componentId
	 * @param userId
	 * @param componentType
	 * @param groupDefinition
	 * @param inTransaction
	 * @return
	 */
	public Either<GroupDefinition, ResponseFormat> createGroup(String componentId, String userId, ComponentTypeEnum componentType, GroupDefinition groupDefinition, boolean inTransaction) {

		Either<GroupDefinition, ResponseFormat> result = null;

		try {
			Either<User, ResponseFormat> validateUserExists = validateUserExists(userId, CREATE_GROUP, inTransaction);

			if (validateUserExists.isRight()) {
				result = Either.right(validateUserExists.right().value());
				return result;
			}

			User user = validateUserExists.left().value();
			// 5. check service/resource existence
			// 6. check service/resource check out
			// 7. user is owner of checkout state
			org.openecomp.sdc.be.model.Component component = null;

			// String realComponentId = componentType ==
			// ComponentTypeEnum.RESOURCE_INSTANCE ? parentId : componentId;
			String realComponentId = componentId;

			ComponentParametersView componentParametersView = new ComponentParametersView();
			componentParametersView.disableAll();
			componentParametersView.setIgnoreGroups(false);
			componentParametersView.setIgnoreArtifacts(false);
			componentParametersView.setIgnoreUsers(false);
			componentParametersView.setIgnoreComponentInstances(false);

			Either<? extends org.openecomp.sdc.be.model.Component, ResponseFormat> validateComponent = validateComponentExists(realComponentId, componentType, componentParametersView, userId, null, user);

			if (validateComponent.isRight()) {
				result = Either.right(validateComponent.right().value());
				return result;
			}
			component = validateComponent.left().value();
			Either<Boolean, ResponseFormat> canWork = validateCanWorkOnComponent(component, userId);
			if (canWork.isRight()) {
				result = Either.right(canWork.right().value());
				return result;
			}

			result = this.createGroup(component, user, componentType, groupDefinition, inTransaction);
			return result;

		} finally {

			if (false == inTransaction) {

				if (result == null || result.isRight()) {
					log.debug("Going to execute rollback on create group.");
					titanGenericDao.rollback();
				} else {
					log.debug("Going to execute commit on create group.");
					titanGenericDao.commit();
				}

			}

		}
	}

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

		if (artifacts == null || true == artifacts.isEmpty()) {
			return Either.left(true);
		}

		Map<String, ArtifactDefinition> deploymentArtifacts = component.getDeploymentArtifacts();
		if (deploymentArtifacts == null || true == deploymentArtifacts.isEmpty()) {
			BeEcompErrorManager.getInstance().logInvalidInputError(context, "No deployment artifact found under component " + component.getNormalizedName(), ErrorSeverity.INFO);
			return Either.right(componentsUtils.getResponseFormat(ActionStatus.INVALID_CONTENT));
		}

		List<String> currentArtifacts = deploymentArtifacts.values().stream().map(p -> p.getUniqueId()).collect(Collectors.toList());
		log.debug("The deployment artifacts of component {} are {}", component.getNormalizedName(), deploymentArtifacts);
		if (false == currentArtifacts.containsAll(artifacts)) {
			BeEcompErrorManager.getInstance().logInvalidInputError(context, "Not all artifacts belongs to component " + component.getNormalizedName(), ErrorSeverity.INFO);
			return Either.right(componentsUtils.getResponseFormat(ActionStatus.INVALID_CONTENT));
		}

		return Either.left(true);

	}

	/**
	 * Verify that the artifact members belongs to the component
	 * 
	 * @param component
	 * @param artifacts
	 * @return
	 */
	private Either<List<ArtifactDefinition>, ResponseFormat> getArtifactsBelongsToComponent(Component component, List<String> artifacts, String context) {

		/*
		 * if (artifacts == null || true == artifacts.isEmpty()) { return Either.left(true); }
		 */

		Map<String, ArtifactDefinition> deploymentArtifacts = component.getDeploymentArtifacts();
		if (deploymentArtifacts == null || true == deploymentArtifacts.isEmpty()) {
			BeEcompErrorManager.getInstance().logInvalidInputError(context, "No deployment artifact found under component " + component.getNormalizedName(), ErrorSeverity.INFO);
			return Either.right(componentsUtils.getResponseFormat(ActionStatus.INVALID_CONTENT));
		}
		List<ArtifactDefinition> resultList = new ArrayList();

		for (String artifactId : artifacts) {
			Optional<ArtifactDefinition> groupArtifactOp = deploymentArtifacts.values().stream().filter(p -> p.getUniqueId().equals(artifactId)).findAny();

			if (groupArtifactOp.isPresent()) {
				ArtifactDefinition groupArtifact = groupArtifactOp.get();
				resultList.add(groupArtifact);
			} else {
				BeEcompErrorManager.getInstance().logInvalidInputError(context, "Not all artifacts belongs to component " + component.getNormalizedName(), ErrorSeverity.INFO);
				return Either.right(componentsUtils.getResponseFormat(ActionStatus.INVALID_CONTENT));

			}
		}

		return Either.left(resultList);

	}

	/**
	 * verify that the members are component instances of the component
	 * 
	 * @param component
	 * @param componentType
	 * @param groupMembers
	 * @param memberToscaTypes
	 * @return
	 */
	private Either<Boolean, ResponseFormat> verifyComponentInstancesAreValidMembers(Component component, ComponentTypeEnum componentType, String groupName, String groupType, Map<String, String> groupMembers, List<String> memberToscaTypes) {

		if (groupMembers == null || true == groupMembers.isEmpty()) {
			return Either.left(true);
		}

		if (memberToscaTypes == null || true == memberToscaTypes.isEmpty()) {
			return Either.left(true);
		}

		List<ComponentInstance> componentInstances = component.getComponentInstances();
		if (componentInstances != null && false == componentInstances.isEmpty()) {
			Map<String, ComponentInstance> compInstUidToCompInstMap = componentInstances.stream().collect(Collectors.toMap(p -> p.getUniqueId(), p -> p));

			Set<String> allCompInstances = compInstUidToCompInstMap.keySet();

			for (Entry<String, String> groupMember : groupMembers.entrySet()) {
				String compName = groupMember.getKey();
				String compUid = groupMember.getValue();

				if (false == allCompInstances.contains(compUid)) {
					/*
					 * %1 - member name %2 - group name %3 - VF name %4 - component type [VF ]
					 */
					String componentTypeForResponse = getComponentTypeForResponse(component);

					BeEcompErrorManager.getInstance().logInvalidInputError(CREATE_GROUP, "Not all group members exists under the component", ErrorSeverity.INFO);
					return Either.right(componentsUtils.getResponseFormat(ActionStatus.GROUP_INVALID_COMPONENT_INSTANCE, compName, groupName, component.getNormalizedName(), componentTypeForResponse));
				}
			}

			ComponentOperation componentOperation = getComponentOperationByParentComponentType(componentType);
			if (componentOperation instanceof ResourceOperation) {
				ResourceOperation resourceOperation = (ResourceOperation) componentOperation;

				for (Entry<String, String> groupMember : groupMembers.entrySet()) {

					String componentInstName = groupMember.getKey();
					String componentInstUid = groupMember.getValue();

					ComponentInstance componentInstance = compInstUidToCompInstMap.get(componentInstUid);
					String componentUid = componentInstance.getComponentUid();
					List<String> componentToscaNames = new ArrayList<>();
					TitanOperationStatus status = resourceOperation.fillResourceDerivedListFromGraph(componentUid, componentToscaNames);
					if (status != TitanOperationStatus.OK) {
						BeEcompErrorManager.getInstance().logInternalFlowError(CREATE_GROUP, "Cannot find tosca list of component id " + componentUid, ErrorSeverity.ERROR);
						return Either.right(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR));
					}

					log.debug("The tosca names of component id {} are {}", componentUid, memberToscaTypes);

					boolean found = false;
					for (String memberToscaType : memberToscaTypes) {
						if (componentToscaNames.contains(memberToscaType)) {
							found = true;
							break;
						}
					}
					if (found == false) {
						BeEcompErrorManager.getInstance().logInvalidInputError(CREATE_GROUP,
								"No tosca types from " + memberToscaTypes + " can be found in the tosca list " + componentToscaNames + " of component " + componentInstance.getNormalizedName(), ErrorSeverity.INFO);
						/*
						 * # %1 - member name # %2 - group name # %3 - group type
						 */
						return Either.right(componentsUtils.getResponseFormat(ActionStatus.GROUP_INVALID_TOSCA_NAME_OF_COMPONENT_INSTANCE, componentInstName, groupName, groupType));
					} else {
						log.debug("Component instance {} fits to one of the required tosca types", componentInstance.getNormalizedName());
					}
				}
			} else {
				BeEcompErrorManager.getInstance().logInvalidInputError(CREATE_GROUP, "Cannot find tosca list since it is not supported for product", ErrorSeverity.ERROR);
				return Either.right(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR));
			}

		}

		return Either.left(true);
	}

	public ComponentOperation getComponentOperation(NodeTypeEnum componentType) {

		switch (componentType) {
		case Service:
		case ResourceInstance:
			return serviceOperation;
		case Resource:
			return resourceOperation;
		default:
			return null;
		}
	}

	/**
	 * Update specific group version
	 * 
	 * @param groupDefinition
	 * @param inTransaction
	 * @return
	 */
	public Either<GroupDefinition, StorageOperationStatus> updateGroupVersion(GroupDefinition groupDefinition, boolean inTransaction) {
		Either<GroupDefinition, StorageOperationStatus> result = null;
		List<String> groupIdsToUpdateVersion = new ArrayList<>();
		groupIdsToUpdateVersion.add(groupDefinition.getUniqueId());
		Either<List<GroupDefinition>, StorageOperationStatus> updateGroupVersion = updateGroupVersion(groupIdsToUpdateVersion, inTransaction);
		if (updateGroupVersion.isLeft()) {
			result = Either.left(updateGroupVersion.left().value().get(0));
		} else {
			log.debug("Failed to update group version. Status is {} ", updateGroupVersion.right().value());
			result = Either.right(updateGroupVersion.right().value());
		}
		return result;
	}

	/**
	 * Update list of groups versions
	 * 
	 * @param groupsUniqueId
	 * @param inTransaction
	 * @return
	 */
	public Either<List<GroupDefinition>, StorageOperationStatus> updateGroupVersion(List<String> groupsUniqueId, boolean inTransaction) {

		Either<List<GroupDefinition>, StorageOperationStatus> result = null;

		try {

			result = groupOperation.updateGroupVersion(groupsUniqueId, true);

			return result;

		} finally {

			if (false == inTransaction) {

				if (result == null || result.isRight()) {
					log.debug("Going to execute rollback on create group.");
					titanGenericDao.rollback();
				} else {
					log.debug("Going to execute commit on create group.");
					titanGenericDao.commit();
				}

			}

		}

	}

	/**
	 * Update GroupDefinition metadata
	 * 
	 * @param componentId
	 * @param user
	 * @param groupId
	 * @param componentType
	 * @param groupUpdate
	 * @param inTransaction
	 * @return
	 */
	public Either<GroupDefinition, ResponseFormat> updateGroupMetadata(String componentId, User user, String groupUniqueId, ComponentTypeEnum componentType, GroupDefinition groupUpdate, boolean inTransaction) {

		Either<GroupDefinition, ResponseFormat> result = null;

		// Validate user and validate group belongs to component
		List<GroupDefinition> groups = new ArrayList<>();
		groups.add(groupUpdate);
		Either<Component, ResponseFormat> validateGroupsBeforeUpdate = validateGroupsBeforeUpdate(componentId, user.getUserId(), componentType, groups, inTransaction);
		if (validateGroupsBeforeUpdate.isRight()) {
			result = Either.right(validateGroupsBeforeUpdate.right().value());
			return result;
		}
		Component component = validateGroupsBeforeUpdate.left().value();

		// Get the GroupDefinition object
		Either<GroupDefinition, StorageOperationStatus> groupStatus = groupOperation.getGroup(groupUniqueId);
		if (groupStatus.isRight()) {
			return Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(groupStatus.right().value(), ComponentTypeEnum.SERVICE), ""));
		}
		GroupDefinition currentGroup = groupStatus.left().value();

		// Validate group type is vfModule
		if (!currentGroup.getType().equals(Constants.DEFAULT_GROUP_VF_MODULE)) {
			log.error("Group update metadata: Group type is different then: {}", Constants.DEFAULT_GROUP_VF_MODULE);
			ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.INVALID_VF_MODULE_TYPE, groupUpdate.getType());
			return Either.right(responseFormat);
		}

		Either<GroupDefinition, ResponseFormat> validationRsponse = validateAndUpdateGroupMetadata(currentGroup, groupUpdate);
		if (validationRsponse.isRight()) {
			log.info("Group update metadata: validations field.");
			return validationRsponse;
		}
		GroupDefinition groupToUpdate = validationRsponse.left().value();

		// lock resource
		Either<Boolean, ResponseFormat> lockResult = lockComponent(componentId, component, "Update GroupDefinition Metadata");
		if (lockResult.isRight()) {
			return Either.right(lockResult.right().value());
		}
		try {
			Either<GroupDefinition, StorageOperationStatus> updateResponse = groupOperation.updateGroupName(groupUniqueId, groupUpdate.getName(), inTransaction);
			if (updateResponse.isRight()) {
				titanGenericDao.rollback();
				BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeSystemError, "Update GroupDefinition Metadata");
				BeEcompErrorManager.getInstance().logBeSystemError("Update GroupDefinition Metadata");
				log.debug("failed to update sevice {}", groupToUpdate.getUniqueId());
				return Either.right(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR));
			}
			titanGenericDao.commit();
			return Either.left(updateResponse.left().value());
		} finally {
			graphLockOperation.unlockComponent(componentId, componentType.getNodeType());
		}
	}

	/**
	 * Validate and update GroupDefinition metadata
	 * 
	 * @param user
	 * @param currentGroup
	 * @param groupUpdate
	 * @return
	 */
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
	 * @param user
	 * @param currentGroup
	 * @param groupUpdate
	 * @return
	 */
	private Either<Boolean, ResponseFormat> validateAndUpdateGroupName(GroupDefinition currentGroup, GroupDefinition groupUpdate) {
		String nameUpdated = groupUpdate.getName();
		String nameCurrent = currentGroup.getName();
		if (!nameCurrent.equals(nameUpdated)) {
			Either<Boolean, ResponseFormat> validatNameResponse = validateGroupName(currentGroup.getName(), groupUpdate.getName());
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
	private Either<Boolean, ResponseFormat> validateGroupName(String currentGroupName, String groupUpdateName) {
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
				String[] split1 = currentGroupName.split("\\.\\.");
				String currentResourceName = split1[0];
				String currentCounter = split1[2];

				String[] split2 = groupUpdateName.split("\\.\\.");
				String groupUpdateResourceName = split2[0];
				String groupUpdateCounter = split2[2];

				if (!currentResourceName.equals(groupUpdateResourceName)) {
					return Either.right(componentsUtils.getResponseFormat(ActionStatus.INVALID_VF_MODULE_NAME_MODIFICATION, currentResourceName));
				}

				if (!currentCounter.equals(groupUpdateCounter)) {
					return Either.right(componentsUtils.getResponseFormat(ActionStatus.INVALID_VF_MODULE_NAME_MODIFICATION, currentCounter));
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
	 * @param groups
	 * @param shouldLockComp
	 * @param inTransaction
	 * @return
	 */
	public Either<List<GroupDefinition>, ResponseFormat> associateArtifactsToGroup(String componentId, String userId, ComponentTypeEnum componentType, List<GroupDefinition> groups, boolean shouldLockComp, boolean inTransaction) {

		Either<List<GroupDefinition>, ResponseFormat> result = null;

		if (shouldLockComp == true && inTransaction == true) {
			BeEcompErrorManager.getInstance().logInternalFlowError("dissociateArtifactsFromGroup", "Cannot lock component since we are inside a transaction", ErrorSeverity.ERROR);
			// Cannot lock component since we are in a middle of another
			// transaction.
			ActionStatus actionStatus = ActionStatus.INVALID_CONTENT;
			result = Either.right(componentsUtils.getResponseFormat(actionStatus));
			return result;
		}

		Component component = null;
		try {

			if (groups == null || groups.isEmpty()) {
				return Either.right(componentsUtils.getResponseFormat(ActionStatus.OK));
			}

			Either<Component, ResponseFormat> validateGroupsBeforeUpdate = validateGroupsBeforeUpdate(componentId, userId, componentType, groups, inTransaction);
			if (validateGroupsBeforeUpdate.isRight()) {
				result = Either.right(validateGroupsBeforeUpdate.right().value());
				return result;
			}

			component = validateGroupsBeforeUpdate.left().value();

			if (shouldLockComp) {
				Either<Boolean, ResponseFormat> lockComponent = lockComponent(component, "Group - Associate Artifacts");
				if (lockComponent.isRight()) {
					return Either.right(lockComponent.right().value());
				}
			}

			List<GroupDefinition> updatedGroups = new ArrayList<>();

			List<GroupDefinition> componentGroups = component.getGroups();

			// per group, associate to it the artifacts
			for (GroupDefinition groupDefinition : groups) {

				GroupDefinition componentGroup = componentGroups.stream().filter(p -> p.getUniqueId().equals(groupDefinition.getUniqueId())).findFirst().orElse(null);
				if (componentGroup != null) {
					List<String> componentArtifacts = componentGroup.getArtifacts();
					int artifactsSizeInGroup = componentArtifacts == null ? 0 : componentArtifacts.size();
					if (artifactsSizeInGroup > 0) {
						List<String> artifactsToAssociate = groupDefinition.getArtifacts();

						// if no artifcats sent
						if (artifactsToAssociate == null || true == artifactsToAssociate.isEmpty()) {
							continue;
						}

						boolean isChanged = componentArtifacts.removeAll(artifactsToAssociate);
						if (isChanged) {// I.e. At least one artifact is already
										// associated to the group
							log.debug("Some of the artifacts already associated to group {}", groupDefinition.getUniqueId());
							return Either.right(componentsUtils.getResponseFormat(ActionStatus.GROUP_ARTIFACT_ALREADY_ASSOCIATED, componentGroup.getName()));
						}
					}
				}

				Either<GroupDefinition, StorageOperationStatus> associateArtifactsToGroup = groupOperation.associateArtifactsToGroup(groupDefinition.getUniqueId(), groupDefinition.getArtifacts(), true);

				if (associateArtifactsToGroup.isRight()) {
					ActionStatus actionStatus = componentsUtils.convertFromStorageResponse(associateArtifactsToGroup.right().value());
					result = Either.right(componentsUtils.getResponseFormat(actionStatus));
					log.debug("Failed to update group {} under component {}, error: {}", groupDefinition.getName(), component.getNormalizedName(), actionStatus.name());
					return result;
				}
				updatedGroups.add(associateArtifactsToGroup.left().value());

			}

			result = Either.left(updatedGroups);
			return result;

		} finally {

			if (false == inTransaction) {

				if (result == null || result.isRight()) {
					log.debug("Going to execute rollback on create group.");
					titanGenericDao.rollback();
				} else {
					log.debug("Going to execute commit on create group.");
					titanGenericDao.commit();
				}

			}

			// unlock resource
			if (shouldLockComp && component != null) {
				graphLockOperation.unlockComponent(componentId, componentType.getNodeType());
			}

		}
	}

	public Either<List<GroupDefinition>, ResponseFormat> associateMembersToGroup(String componentId, String userId, ComponentTypeEnum componentType, List<GroupDefinition> groups, boolean shouldLockComp, boolean inTransaction) {

		Either<List<GroupDefinition>, ResponseFormat> result = null;

		if (shouldLockComp == true && inTransaction == true) {
			BeEcompErrorManager.getInstance().logInternalFlowError("dissociateArtifactsFromGroup", "Cannot lock component since we are inside a transaction", ErrorSeverity.ERROR);
			// Cannot lock component since we are in a middle of another
			// transaction.
			ActionStatus actionStatus = ActionStatus.INVALID_CONTENT;
			result = Either.right(componentsUtils.getResponseFormat(actionStatus));
			return result;
		}

		Component component = null;
		try {

			if (groups == null || groups.isEmpty()) {
				return Either.right(componentsUtils.getResponseFormat(ActionStatus.OK));
			}

			Either<Component, ResponseFormat> validateGroupsBeforeUpdate = validateGroupsBeforeUpdate(componentId, userId, componentType, groups, inTransaction);
			if (validateGroupsBeforeUpdate.isRight()) {
				result = Either.right(validateGroupsBeforeUpdate.right().value());
				return result;
			}

			component = validateGroupsBeforeUpdate.left().value();

			if (shouldLockComp) {
				Either<Boolean, ResponseFormat> lockComponent = lockComponent(component, "Group - Associate Members");
				if (lockComponent.isRight()) {
					return Either.right(lockComponent.right().value());
				}
			}

			List<GroupDefinition> updatedGroups = new ArrayList<>();

			// per group, associate to it the members
			for (GroupDefinition groupDefinition : groups) {

				Either<GroupDefinition, StorageOperationStatus> associateMembersToGroup = groupOperation.associateMembersToGroup(groupDefinition.getUniqueId(), groupDefinition.getMembers(), true);

				if (associateMembersToGroup.isRight()) {
					ActionStatus actionStatus = componentsUtils.convertFromStorageResponse(associateMembersToGroup.right().value());
					result = Either.right(componentsUtils.getResponseFormat(actionStatus));
					log.debug("Failed to update group {} under component {}, error: {}", groupDefinition.getName(), component.getNormalizedName(), actionStatus.name());
					return result;
				} else {
					updatedGroups.add(associateMembersToGroup.left().value());
				}

			}

			result = Either.left(updatedGroups);
			return result;

		} finally {

			if (false == inTransaction) {

				if (result == null || result.isRight()) {
					log.debug("Going to execute rollback on create group.");
					titanGenericDao.rollback();
				} else {
					log.debug("Going to execute commit on create group.");
					titanGenericDao.commit();
				}

			}

			// unlock resource
			if (shouldLockComp && component != null) {
				graphLockOperation.unlockComponent(componentId, componentType.getNodeType());
			}

		}
	}

	/**
	 * associate artifacts to a given group
	 * 
	 * @param componentId
	 * @param userId
	 * @param componentType
	 * @param groups
	 * @param shouldLockComp
	 * @param inTransaction
	 * @return
	 */
	public Either<GroupDefinitionInfo, ResponseFormat> getGroupWithArtifactsById(ComponentTypeEnum componentType, String componentId, String groupId, String userId, boolean inTransaction) {

		Either<GroupDefinitionInfo, ResponseFormat> result = null;

		// Validate user exist
		Either<User, ResponseFormat> validateUserExists = validateUserExists(userId, UPDATE_GROUP, true);

		if (validateUserExists.isRight()) {
			result = Either.right(validateUserExists.right().value());
			return result;
		}

		User user = validateUserExists.left().value();

		// Validate component exist
		org.openecomp.sdc.be.model.Component component = null;
		String realComponentId = componentId;

		try {
			ComponentParametersView componentParametersView = new ComponentParametersView();
			componentParametersView.disableAll();
			componentParametersView.setIgnoreGroups(false);
			componentParametersView.setIgnoreArtifacts(false);
			componentParametersView.setIgnoreUsers(false);
			componentParametersView.setIgnoreComponentInstances(false);

			Either<? extends org.openecomp.sdc.be.model.Component, ResponseFormat> validateComponent = validateComponentExists(realComponentId, componentType, componentParametersView, userId, null, user);
			if (validateComponent.isRight()) {
				result = Either.right(validateComponent.right().value());
				return result;
			}
			component = validateComponent.left().value();

			// validate we can work on component
			/*
			 * Either<Boolean, ResponseFormat> canWork = validateCanWorkOnComponent( component, userId); if (canWork.isRight()) { result = Either.right(canWork.right().value()); return result; }
			 */
			List<GroupDefinition> groups = component.getGroups();
			Optional<GroupDefinition> findAny = groups.stream().filter(p -> p.getUniqueId().equals(groupId)).findAny();
			if (findAny.isPresent()) {
				GroupDefinition group = findAny.get();
				Boolean isBase = null;// Constants.IS_BASE;
				List<GroupProperty> props = group.getProperties();
				if (props != null && !props.isEmpty()) {
					Optional<GroupProperty> isBasePropOp = props.stream().filter(p -> p.getName().equals(Constants.IS_BASE)).findAny();
					if (isBasePropOp.isPresent()) {
						GroupProperty propIsBase = isBasePropOp.get();
						isBase = Boolean.parseBoolean(propIsBase.getValue());

					} else {
						BeEcompErrorManager.getInstance().logInvalidInputError(GET_GROUP, "failed to find prop isBase " + component.getNormalizedName(), ErrorSeverity.INFO);
						// return
						// Either.right(componentsUtils.getResponseFormat(ActionStatus.INVALID_CONTENT));

					}
				}

				List<ArtifactDefinitionInfo> artifacts = new ArrayList();
				List<String> artifactsIds = group.getArtifacts();
				if (artifactsIds != null && !artifactsIds.isEmpty()) {
					Either<List<ArtifactDefinition>, ResponseFormat> getArtifacts = getArtifactsBelongsToComponent(component, artifactsIds, GET_GROUP);
					if (getArtifacts.isRight()) {
						log.debug("Faild to find artifacts in group {} under component {}", groupId, component.getUniqueId());
						// result = Either.right(getArtifacts.right().value());
						// return result;
					} else {

						List<ArtifactDefinition> artifactsFromComponent = getArtifacts.left().value();
						if (artifactsFromComponent != null && !artifactsFromComponent.isEmpty()) {
							for (ArtifactDefinition artifactDefinition : artifactsFromComponent) {
								ArtifactDefinitionInfo artifactDefinitionInfo = new ArtifactDefinitionInfo(artifactDefinition);
								artifacts.add(artifactDefinitionInfo);
							}
						}
					}
				}
				GroupDefinitionInfo resultInfo = new GroupDefinitionInfo(group);
				resultInfo.setIsBase(isBase);
				if (!artifacts.isEmpty())
					resultInfo.setArtifacts(artifacts);

				result = Either.left(resultInfo);

				return result;

			} else {
				log.debug("Faild to find group {} under component {}", groupId, component.getUniqueId());
				BeEcompErrorManager.getInstance().logInvalidInputError(GET_GROUP, "group  " + groupId + " not found under component " + component.getUniqueId(), ErrorSeverity.INFO);
				String componentTypeForResponse = getComponentTypeForResponse(component);
				result = Either.right(componentsUtils.getResponseFormat(ActionStatus.GROUP_IS_MISSING, groupId, component.getSystemName(), componentTypeForResponse));
				return result;

			}
		} finally {

			if (false == inTransaction) {

				if (result == null || result.isRight()) {
					log.debug("Going to execute rollback on create group.");
					titanGenericDao.rollback();
				} else {
					log.debug("Going to execute commit on create group.");
					titanGenericDao.commit();
				}

			}

		}

	}

	/**
	 * @param componentId
	 * @param userId
	 * @param componentType
	 * @param groups
	 * @param inTransaction
	 * @return
	 */
	private Either<org.openecomp.sdc.be.model.Component, ResponseFormat> validateGroupsBeforeUpdate(String componentId, String userId, ComponentTypeEnum componentType, List<GroupDefinition> groups, boolean inTransaction) {

		Either<org.openecomp.sdc.be.model.Component, ResponseFormat> result;

		// Validate user exist
		Either<User, ResponseFormat> validateUserExists = validateUserExists(userId, UPDATE_GROUP, inTransaction);
		if (validateUserExists.isRight()) {
			result = Either.right(validateUserExists.right().value());
			return result;
		}
		User user = validateUserExists.left().value();

		// Validate component exist
		String realComponentId = componentId;

		ComponentParametersView componentParametersView = new ComponentParametersView();
		componentParametersView.disableAll();
		componentParametersView.setIgnoreGroups(false);
		componentParametersView.setIgnoreArtifacts(false);
		componentParametersView.setIgnoreUsers(false);
		componentParametersView.setIgnoreComponentInstances(false);

		Either<? extends org.openecomp.sdc.be.model.Component, ResponseFormat> validateComponent = validateComponentExists(realComponentId, componentType, componentParametersView, userId, null, user);

		if (validateComponent.isRight()) {
			result = Either.right(validateComponent.right().value());
			return result;
		}
		org.openecomp.sdc.be.model.Component component = validateComponent.left().value();

		// validate we can work on component
		Either<Boolean, ResponseFormat> canWork = validateCanWorkOnComponent(component, userId);
		if (canWork.isRight()) {
			result = Either.right(canWork.right().value());
			return result;
		}

		// Validate groups exists in the component
		ResponseFormat validateGroupsInComponent = validateGroupsInComponentByFunc(groups, component, p -> p.getUniqueId());
		if (validateGroupsInComponent != null) {
			result = Either.right(validateGroupsInComponent);
			return result;
		}

		Set<String> artifacts = new HashSet<>();
		groups.forEach(p -> {
			if (p.getArtifacts() != null) {
				artifacts.addAll(p.getArtifacts());
			}
		});
		// validate all artifacts belongs to the component
		Either<Boolean, ResponseFormat> verifyArtifactsBelongsToComponent = verifyArtifactsBelongsToComponent(component, new ArrayList<>(artifacts), UPDATE_GROUP);
		if (verifyArtifactsBelongsToComponent.isRight()) {
			result = Either.right(verifyArtifactsBelongsToComponent.right().value());
			return result;
		}

		return Either.left(component);
	}

	private ResponseFormat validateGroupsInComponent(List<GroupDefinition> groups, org.openecomp.sdc.be.model.Component component) {

		Function<GroupDefinition, String> getByName = s -> s.getName();

		return validateGroupsInComponentByFunc(groups, component, getByName);

	}

	/**
	 * @param groups
	 * @param component
	 * @param getByParam
	 *            - the method to fetch the key of the GroupDefinition(from groups) in order to compare to groups in the component
	 * @return
	 */
	private ResponseFormat validateGroupsInComponentByFunc(List<GroupDefinition> groups, org.openecomp.sdc.be.model.Component component, Function<GroupDefinition, String> getByParam) {
		ResponseFormat result = null;

		List<GroupDefinition> currentGroups = component.getGroups();

		boolean found = false;
		List<String> updatedGroupsName = groups.stream().map(getByParam).collect(Collectors.toList());

		List<String> missingGroupNames = updatedGroupsName;

		if (currentGroups != null && false == currentGroups.isEmpty()) {
			List<String> currentGroupsName = currentGroups.stream().map(getByParam).collect(Collectors.toList());

			if (currentGroupsName.containsAll(updatedGroupsName)) {
				found = true;
			} else {
				currentGroupsName.removeAll(currentGroupsName);
				missingGroupNames = currentGroupsName;
			}
		}
		if (false == found) {
			String componentTypeForResponse = getComponentTypeForResponse(component);
			String listOfGroups = getAsString(missingGroupNames);
			result = componentsUtils.getResponseFormat(ActionStatus.GROUP_IS_MISSING, listOfGroups, component.getSystemName(), componentTypeForResponse);
			return result;
		}

		return null;
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

	/**
	 * dissociate artifacts from a given group
	 * 
	 * @param componentId
	 * @param userId
	 * @param componentType
	 * @param groups
	 * @param shouldLockComp
	 * @param inTransaction
	 * @return
	 */
	public Either<List<GroupDefinition>, ResponseFormat> dissociateArtifactsFromGroup(String componentId, String userId, ComponentTypeEnum componentType, List<GroupDefinition> groups, boolean shouldLockComp, boolean inTransaction) {

		Either<List<GroupDefinition>, ResponseFormat> result = null;

		if (shouldLockComp == true && inTransaction == true) {
			BeEcompErrorManager.getInstance().logInternalFlowError("dissociateArtifactsFromGroup", "Cannot lock component since we are inside a transaction", ErrorSeverity.ERROR);
			// Cannot lock component since we are in a middle of another
			// transaction.
			ActionStatus actionStatus = ActionStatus.INVALID_CONTENT;
			result = Either.right(componentsUtils.getResponseFormat(actionStatus));
			return result;
		}

		Component component = null;

		try {

			if (groups == null || groups.isEmpty()) {
				return Either.right(componentsUtils.getResponseFormat(ActionStatus.OK));
			}

			Either<Component, ResponseFormat> validateGroupsBeforeUpdate = validateGroupsBeforeUpdate(componentId, userId, componentType, groups, inTransaction);
			if (validateGroupsBeforeUpdate.isRight()) {
				result = Either.right(validateGroupsBeforeUpdate.right().value());
				return result;
			}

			component = validateGroupsBeforeUpdate.left().value();

			if (shouldLockComp) {
				Either<Boolean, ResponseFormat> lockComponent = lockComponent(component, "Group - Dissociate Artifacts");
				if (lockComponent.isRight()) {
					return Either.right(lockComponent.right().value());
				}
			}

			List<GroupDefinition> updatedGroups = new ArrayList<>();

			List<GroupDefinition> componentGroups = component.getGroups();
			// per group, associate to it the artifacts
			for (GroupDefinition groupDefinition : groups) {

				GroupDefinition componentGroup = componentGroups.stream().filter(p -> p.getUniqueId().equals(groupDefinition.getUniqueId())).findFirst().orElse(null);
				if (componentGroup != null) {
					List<String> componentArtifacts = componentGroup.getArtifacts();
					int artifactsSizeInGroup = componentArtifacts == null ? 0 : componentArtifacts.size();
					List<String> artifactsToDissociate = groupDefinition.getArtifacts();

					// if no artifcats sent
					if (artifactsToDissociate == null || true == artifactsToDissociate.isEmpty()) {
						continue;
					}

					if (artifactsSizeInGroup > 0) {

						boolean containsAll = componentArtifacts.containsAll(artifactsToDissociate);
						if (false == containsAll) { // At least one artifact is
													// not associated to the
													// group
							log.debug("Some of the artifacts already dissociated to group {}", groupDefinition.getUniqueId());
							return Either.right(componentsUtils.getResponseFormat(ActionStatus.GROUP_ARTIFACT_ALREADY_DISSOCIATED, componentGroup.getName()));
						}
					} else {
						if (artifactsSizeInGroup == 0) {
							if (artifactsToDissociate != null && false == artifactsToDissociate.isEmpty()) {
								log.debug("No artifact is found under the group {}", groupDefinition.getUniqueId());
								return Either.right(componentsUtils.getResponseFormat(ActionStatus.GROUP_ARTIFACT_ALREADY_DISSOCIATED, componentGroup.getName()));
							}
						}
					}
				}

				Either<GroupDefinition, StorageOperationStatus> associateArtifactsToGroup = groupOperation.dissociateArtifactsFromGroup(groupDefinition.getUniqueId(), groupDefinition.getArtifacts(), true);

				if (associateArtifactsToGroup.isRight()) {
					ActionStatus actionStatus = componentsUtils.convertFromStorageResponse(associateArtifactsToGroup.right().value());
					result = Either.right(componentsUtils.getResponseFormat(actionStatus));
					log.debug("Failed to update group {} under component {}, error: {}", groupDefinition.getName(), component.getNormalizedName(), actionStatus.name());
					return result;
				}
				updatedGroups.add(associateArtifactsToGroup.left().value());

			}

			result = Either.left(updatedGroups);
			return result;

		} finally {

			if (false == inTransaction) {

				if (result == null || result.isRight()) {
					log.debug("Going to execute rollback on create group.");
					titanGenericDao.rollback();
				} else {
					log.debug("Going to execute commit on create group.");
					titanGenericDao.commit();
				}

			}
			// unlock resource
			if (shouldLockComp && component != null) {
				graphLockOperation.unlockComponent(componentId, componentType.getNodeType());
			}

		}

	}

	public Either<List<GroupDefinition>, ResponseFormat> createGroups(String componentId, String userId, ComponentTypeEnum componentType, List<GroupDefinition> groupDefinitions, boolean shouldLockComp, boolean inTransaction) {

		Either<List<GroupDefinition>, ResponseFormat> result = null;

		List<GroupDefinition> groups = new ArrayList<>();
		org.openecomp.sdc.be.model.Component component = null;
		try {

			if (groupDefinitions != null && false == groupDefinitions.isEmpty()) {

				if (shouldLockComp == true && inTransaction == true) {
					BeEcompErrorManager.getInstance().logInternalFlowError("createGroups", "Cannot lock component since we are inside a transaction", ErrorSeverity.ERROR);
					// Cannot lock component since we are in a middle of another
					// transaction.
					ActionStatus actionStatus = ActionStatus.INVALID_CONTENT;
					result = Either.right(componentsUtils.getResponseFormat(actionStatus));
					return result;
				}

				Either<User, ResponseFormat> validateUserExists = validateUserExists(userId, CREATE_GROUP, true);
				if (validateUserExists.isRight()) {
					result = Either.right(validateUserExists.right().value());
					return result;
				}

				User user = validateUserExists.left().value();

				ComponentParametersView componentParametersView = new ComponentParametersView();
				componentParametersView.disableAll();
				componentParametersView.setIgnoreGroups(false);
				componentParametersView.setIgnoreArtifacts(false);
				componentParametersView.setIgnoreUsers(false);
				componentParametersView.setIgnoreComponentInstances(false);

				Either<? extends org.openecomp.sdc.be.model.Component, ResponseFormat> validateComponent = validateComponentExists(componentId, componentType, componentParametersView, userId, null, user);

				if (validateComponent.isRight()) {
					result = Either.right(validateComponent.right().value());
					return result;
				}
				component = validateComponent.left().value();

				if (shouldLockComp) {
					Either<Boolean, ResponseFormat> lockComponent = lockComponent(component, "CreateGroups");
					if (lockComponent.isRight()) {
						return Either.right(lockComponent.right().value());
					}
				}

				Either<Boolean, ResponseFormat> canWork = validateCanWorkOnComponent(component, userId);
				if (canWork.isRight()) {
					result = Either.right(canWork.right().value());
					return result;
				}

				for (GroupDefinition groupDefinition : groupDefinitions) {
					Either<GroupDefinition, ResponseFormat> createGroup = this.createGroup(component, user, componentType, groupDefinition, true);
					if (createGroup.isRight()) {
						log.debug("Failed to create group {}.", groupDefinition);
						result = Either.right(createGroup.right().value());
						return result;
					}
					GroupDefinition createdGroup = createGroup.left().value();
					groups.add(createdGroup);
				}
			}

			result = Either.left(groups);
			return result;

		} finally {

			if (false == inTransaction) {

				if (result == null || result.isRight()) {
					log.debug("Going to execute rollback on create group.");
					titanGenericDao.rollback();
				} else {
					log.debug("Going to execute commit on create group.");
					titanGenericDao.commit();
				}

			}
			// unlock resource
			if (shouldLockComp && component != null) {
				graphLockOperation.unlockComponent(componentId, componentType.getNodeType());
			}

		}

	}

	public Either<GroupDefinition, ResponseFormat> createGroup(Component component, User user, ComponentTypeEnum componentType, GroupDefinition groupDefinition, boolean inTransaction) {

		Either<GroupDefinition, ResponseFormat> result = null;

		log.debug("Going to create group {}", groupDefinition);

		try {

			// 3. verify group not already exist
			List<GroupDefinition> groups = component.getGroups();
			boolean found = false;
			if (groups != null && false == groups.isEmpty()) {

				GroupDefinition existGroupDef = groups.stream().filter(p -> p.getName().equalsIgnoreCase(groupDefinition.getName())).findFirst().orElse(null);

				found = existGroupDef != null;
			}

			if (true == found) {
				String componentTypeForResponse = getComponentTypeForResponse(component);
				result = Either.right(componentsUtils.getResponseFormat(ActionStatus.GROUP_ALREADY_EXIST, groupDefinition.getName(), component.getNormalizedName(), componentTypeForResponse));
				return result;
			}

			// 4. verify type of group exist
			String groupType = groupDefinition.getType();
			if (groupType == null || groupType.isEmpty()) {
				result = Either.right(componentsUtils.getResponseFormat(ActionStatus.GROUP_MISSING_GROUP_TYPE, groupDefinition.getName()));
				return result;
			}
			Either<GroupTypeDefinition, StorageOperationStatus> getGroupType = groupTypeOperation.getLatestGroupTypeByType(groupType, true);
			if (getGroupType.isRight()) {
				StorageOperationStatus status = getGroupType.right().value();
				if (status == StorageOperationStatus.NOT_FOUND) {
					BeEcompErrorManager.getInstance().logInvalidInputError(CREATE_GROUP, "group type " + groupType + " cannot be found", ErrorSeverity.INFO);
					result = Either.right(componentsUtils.getResponseFormat(ActionStatus.GROUP_TYPE_IS_INVALID, groupType));
					return result;
				} else {
					result = Either.right(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR));
					return result;
				}
			}

			// 6. verify the component instances type are allowed according to
			// the member types in the group type
			GroupTypeDefinition groupTypeDefinition = getGroupType.left().value();

			Either<Boolean, ResponseFormat> areValidMembers = verifyComponentInstancesAreValidMembers(component, componentType, groupDefinition.getName(), groupType, groupDefinition.getMembers(), groupTypeDefinition.getMembers());

			if (areValidMembers.isRight()) {
				ResponseFormat responseFormat = areValidMembers.right().value();
				result = Either.right(responseFormat);
				return result;
			}

			// 7. verify the artifacts belongs to the component
			Either<Boolean, ResponseFormat> areValidArtifacts = verifyArtifactsBelongsToComponent(component, groupDefinition.getArtifacts(), CREATE_GROUP);
			if (areValidArtifacts.isRight()) {
				ResponseFormat responseFormat = areValidArtifacts.right().value();
				result = Either.right(responseFormat);
				return result;
			}

			NodeTypeEnum nodeTypeEnum = componentType.getNodeType();

			// add invariantUUID
			String invariantUUID = UniqueIdBuilder.buildInvariantUUID();
			groupDefinition.setInvariantUUID(invariantUUID);

			// add groupUUID
			String groupUUID = UniqueIdBuilder.generateUUID();
			groupDefinition.setGroupUUID(groupUUID);

			// add version
			groupDefinition.setVersion(INITIAL_VERSION);

			// set groupType uid
			groupDefinition.setTypeUid(groupTypeDefinition.getUniqueId());

			Either<GroupDefinition, StorageOperationStatus> addGroupToGraph = groupOperation.addGroup(nodeTypeEnum, component.getUniqueId(), groupDefinition, true);

			if (addGroupToGraph.isRight()) {
				StorageOperationStatus storageOperationStatus = addGroupToGraph.right().value();
				ActionStatus actionStatus = componentsUtils.convertFromStorageResponse(storageOperationStatus);
				result = Either.right(componentsUtils.getResponseFormat(actionStatus));
				log.debug("Failed to create group {} under component {}, error: {}", groupDefinition.getName(), component.getNormalizedName(), actionStatus.name());
			} else {
				GroupDefinition groupDefinitionCreated = addGroupToGraph.left().value();
				result = Either.left(groupDefinitionCreated);
			}

			return result;

		} finally {

			if (false == inTransaction) {

				if (result == null || result.isRight()) {
					log.debug("Going to execute rollback on create group.");
					titanGenericDao.rollback();
				} else {
					log.debug("Going to execute commit on create group.");
					titanGenericDao.commit();
				}

			}

		}

	}

	public Either<List<GroupDefinition>, ResponseFormat> updateVfModuleGroupNames(String resourceSystemName, List<GroupDefinition> groups, boolean inTransaction) {
		List<GroupDefinition> updatedGroups = new ArrayList<>();
		Either<List<GroupDefinition>, ResponseFormat> updateGroupNamesRes = Either.left(updatedGroups);
		Either<GroupDefinition, StorageOperationStatus> updateGroupNameRes;
		Either<String, ResponseFormat> validateGenerateGroupNameRes;
		int counter;
		for (GroupDefinition group : groups) {
			if (!group.getType().equals(Constants.DEFAULT_GROUP_VF_MODULE) && !Pattern.compile(Constants.MODULE_OLD_NAME_PATTERN).matcher(group.getName()).matches()) {
				continue;
			}
			counter = Integer.parseInt(group.getName().split(Constants.MODULE_NAME_DELIMITER)[1]);
			validateGenerateGroupNameRes = validateGenerateVfModuleGroupName(resourceSystemName, group.getDescription(), counter);
			if (validateGenerateGroupNameRes.isRight()) {
				updateGroupNamesRes = Either.right(validateGenerateGroupNameRes.right().value());
				break;
			}
			updateGroupNameRes = groupOperation.updateGroupName(group.getUniqueId(), validateGenerateGroupNameRes.left().value(), inTransaction);
			if (updateGroupNameRes.isRight()) {
				ActionStatus actionStatus = componentsUtils.convertFromStorageResponse(updateGroupNameRes.right().value());
				updateGroupNamesRes = Either.right(componentsUtils.getResponseFormat(actionStatus));
				break;
			}
			updatedGroups.add(updateGroupNameRes.left().value());
		}
		return updateGroupNamesRes;
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
			final String fileName = description.replaceAll("\\.\\.", "\\.");
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
					.map(group -> Integer.parseInt(group.getName().split(Constants.MODULE_NAME_DELIMITER)[1])).collect(Collectors.toList());
			counter = (counters == null || counters.isEmpty()) ? 0 : counters.stream().max((a, b) -> Integer.compare(a, b)).get() + 1;
		}
		return counter;
	}

	public Either<List<GroupDefinition>, ResponseFormat> validateUpdateVfGroupNamesOnGraph(List<GroupDefinition> groups, String resourceSystemName, boolean inTransaction) {
		List<GroupDefinition> updatedGroups = new ArrayList<>();
		Either<List<GroupDefinition>, ResponseFormat> result = Either.left(updatedGroups);

		for (GroupDefinition group : groups) {
			String groupType = group.getType();
			String oldGroupName = group.getName();
			String newGroupName;
			Either<String, ResponseFormat> newGroupNameRes;
			Either<GroupDefinition, StorageOperationStatus> updateGroupNameRes;
			int counter;
			if (groupType.equals(Constants.DEFAULT_GROUP_VF_MODULE) && Pattern.compile(Constants.MODULE_OLD_NAME_PATTERN).matcher(oldGroupName).matches()) {
				counter = Integer.parseInt(group.getName().split(Constants.MODULE_NAME_DELIMITER)[1]);
				newGroupNameRes = validateGenerateVfModuleGroupName(resourceSystemName, group.getDescription(), counter);
				if (newGroupNameRes.isRight()) {
					log.debug("Failed to generate new vf module group name. Status is {} ", newGroupNameRes.right().value());
					result = Either.right(newGroupNameRes.right().value());
					break;
				}
				newGroupName = newGroupNameRes.left().value();
				updateGroupNameRes = groupOperation.updateGroupName(group.getUniqueId(), newGroupName, inTransaction);
				if (updateGroupNameRes.isRight()) {
					log.debug("Failed to update vf module group name for group {} . Status is {} ", oldGroupName, updateGroupNameRes.right().value());
					ResponseFormat responseFormat = componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(updateGroupNameRes.right().value()));
					result = Either.right(responseFormat);
					break;
				}
			}
			updatedGroups.add(group);
		}
		return result;
	}

	public Either<List<GroupDefinition>, ResponseFormat> createGroups(Component component, User user, ComponentTypeEnum componentType, List<GroupDefinition> groupDefinitions, boolean inTransaction) {

		List<GroupDefinition> generatedGroups = new ArrayList<>();
		Either<List<GroupDefinition>, ResponseFormat> result = Either.left(generatedGroups);

		try {

			if (groupDefinitions != null && false == groupDefinitions.isEmpty()) {
				for (GroupDefinition groupDefinition : groupDefinitions) {
					Either<GroupDefinition, ResponseFormat> createGroup = this.createGroup(component, user, componentType, groupDefinition, true);
					if (createGroup.isRight()) {
						result = Either.right(createGroup.right().value());
						return result;
					}
					GroupDefinition generatedGroup = createGroup.left().value();
					generatedGroups.add(generatedGroup);
				}
			}

			return result;
		} finally {

			if (false == inTransaction) {

				if (result == null || result.isRight()) {
					log.debug("Going to execute rollback on create group.");
					titanGenericDao.rollback();
				} else {
					log.debug("Going to execute commit on create group.");
					titanGenericDao.commit();
				}

			}

		}

	}

}
