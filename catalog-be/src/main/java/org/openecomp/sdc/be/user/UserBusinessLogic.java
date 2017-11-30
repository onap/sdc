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

package org.openecomp.sdc.be.user;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.ServletContext;

import org.apache.tinkerpop.gremlin.structure.Edge;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.neo4j.GraphPropertiesDictionary;
import org.openecomp.sdc.be.dao.titan.TitanGenericDao;
import org.openecomp.sdc.be.dao.utils.UserStatusEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.operations.api.IUserAdminOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.common.api.UserRoleEnum;
import org.openecomp.sdc.common.config.EcompErrorName;
import org.openecomp.sdc.common.kpi.api.ASDCKpiApi;
import org.openecomp.sdc.exception.ResponseFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fj.data.Either;

@Component("userBusinessLogic")
public class UserBusinessLogic implements IUserBusinessLogic {

	private static Logger log = LoggerFactory.getLogger(UserBusinessLogic.class.getName());
	private static UserAdminValidator userAdminValidator = UserAdminValidator.getInstance();

	@Resource
	private IUserAdminOperation userAdminOperation;
	@Resource
	private ComponentsUtils componentsUtils;
	@Autowired
	private TitanGenericDao titanDao;

	@Override
	public Either<User, ActionStatus> getUser(String userId, boolean inTransaction) {
		return userAdminOperation.getUserData(userId, inTransaction);
	}

	@Override
	public Either<User, ResponseFormat> createUser(User modifier, User newUser) {

		ResponseFormat responseFormat;
		String modifierUserId = modifier.getUserId();

		if (modifierUserId == null) {
			modifier.setUserId("UNKNOWN");
			log.debug("createUser method -  user header is missing");
			responseFormat = componentsUtils.getResponseFormat(ActionStatus.MISSING_INFORMATION);
			handleAuditing(modifier, null, null, responseFormat, AuditingActionEnum.ADD_USER);
			return Either.right(responseFormat);
		}

		Either<User, ActionStatus> eitherCreator = getUser(modifierUserId, false);
		if (eitherCreator.isRight() || eitherCreator.left().value() == null) {
			log.debug("createUser method - user is not listed. userId = {}", modifier.getUserId());
			responseFormat = componentsUtils.getResponseFormat(ActionStatus.RESTRICTED_OPERATION);
			handleAuditing(modifier, null, null, responseFormat, AuditingActionEnum.ADD_USER);
			return Either.right(responseFormat);
		}

		modifier = eitherCreator.left().value();
		if (!modifier.getRole().equals(UserRoleEnum.ADMIN.getName())) {
			log.debug("createUser method - user is not admin = {}", modifier.getUserId());
			responseFormat = componentsUtils.getResponseFormat(ActionStatus.RESTRICTED_OPERATION);
			handleAuditing(modifier, null, null, responseFormat, AuditingActionEnum.ADD_USER);
			return Either.right(responseFormat);
		}

		// verify user not exist
		User userFromDb = new User();
		// boolean isUserAlreadyExist = false;
		Either<User, ActionStatus> eitherUserInDB = getUser(newUser.getUserId(), false);
		if (eitherUserInDB.isRight()) {
			ActionStatus status = eitherUserInDB.right().value();
			if (!ActionStatus.USER_NOT_FOUND.equals(status) && !ActionStatus.USER_INACTIVE.equals(status)) {
				responseFormat = componentsUtils.getResponseFormat(eitherUserInDB.right().value(), newUser.getUserId());
				handleAuditing(modifier, null, null, responseFormat, AuditingActionEnum.ADD_USER);
				return Either.right(responseFormat);
			}
		} else {// User exist in DB
			userFromDb = eitherUserInDB.left().value();
			// isUserAlreadyExist = true;
			if (userFromDb.getStatus() == UserStatusEnum.ACTIVE) {
				responseFormat = componentsUtils.getResponseFormatByUserId(ActionStatus.USER_ALREADY_EXIST, newUser.getUserId());
				log.debug("createUser method - user with id {} already exist with id: {}", modifier.getUserId(), userFromDb.getUserId());
				handleAuditing(modifier, null, null, responseFormat, AuditingActionEnum.ADD_USER);
				return Either.right(responseFormat);
			}
		}

		newUser.setStatus(UserStatusEnum.ACTIVE);

		// validate Email
		if (newUser.getEmail() != null && !userAdminValidator.validateEmail(newUser.getEmail())) {
			log.debug("createUser method - user has invalid email = {}", modifier.getUserId());
			responseFormat = componentsUtils.getResponseFormat(ActionStatus.INVALID_EMAIL_ADDRESS, newUser.getEmail());
			handleAuditing(modifier, null, null, responseFormat, AuditingActionEnum.ADD_USER);
			return Either.right(responseFormat);
		}

		// validate Role
		if (newUser.getRole() == null || newUser.getRole().length() == 0) {
			newUser.setRole(Role.DESIGNER.name());
		} else {
			if (!userAdminValidator.validateRole(newUser.getRole())) {
				log.debug("createUser method - user has invalid role = {}", modifier.getUserId());
				responseFormat = componentsUtils.getResponseFormat(ActionStatus.INVALID_ROLE, newUser.getRole());
				handleAuditing(modifier, null, null, responseFormat, AuditingActionEnum.ADD_USER);
				return Either.right(responseFormat);
			}
		}

		// handle last login if user is import
		if (newUser.getLastLoginTime() == null) {
			newUser.setLastLoginTime(0L);
		}

		Either<User, StorageOperationStatus> addOrUpdateUserReq;

		if (ActionStatus.USER_INACTIVE.equals(eitherUserInDB.right().value())) { // user
																					// exist
																					// with
																					// inactive
																					// state
																					// -
																					// update
																					// user
																					// data
			newUser.setLastLoginTime(0L);
			addOrUpdateUserReq = userAdminOperation.updateUserData(newUser);

		} else { // user not exist - create new user

			if (newUser.getUserId() != null && !userAdminValidator.validateUserId(newUser.getUserId())) {
				log.debug("createUser method - user has invalid userId = {}", modifier.getUserId());
				responseFormat = componentsUtils.getResponseFormat(ActionStatus.INVALID_USER_ID, newUser.getUserId());
				handleAuditing(modifier, null, null, responseFormat, AuditingActionEnum.ADD_USER);
				return Either.right(responseFormat);
			}
			addOrUpdateUserReq = userAdminOperation.saveUserData(newUser);
		}

		if (addOrUpdateUserReq.isRight() || addOrUpdateUserReq.left().value() == null) {
			log.debug("createUser method - failed to create user");
			Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(addOrUpdateUserReq.right().value())));
		}
		log.debug("createUser method - user created");
		User createdUser = addOrUpdateUserReq.left().value();
		responseFormat = componentsUtils.getResponseFormat(ActionStatus.CREATED);
		handleAuditing(modifier, null, createdUser, responseFormat, AuditingActionEnum.ADD_USER);
		return Either.left(createdUser);
	}

	@Override
	public Either<User, ResponseFormat> updateUserRole(User modifier, String userIdToUpdate, String userRole) {

		ResponseFormat responseFormat;
		String modifierUserId = modifier.getUserId();

		if (modifierUserId == null) {
			modifier.setUserId("UNKNOWN");
			log.debug("updateUserRole method -  user header is missing");
			responseFormat = componentsUtils.getResponseFormat(ActionStatus.MISSING_INFORMATION);
			handleAuditing(modifier, null, null, responseFormat, AuditingActionEnum.UPDATE_USER);
			return Either.right(responseFormat);
		}

		Either<User, ActionStatus> eitherCreator = getUser(modifierUserId, false);
		if (eitherCreator.isRight() || eitherCreator.left().value() == null) {
			log.debug("updateUserRole method - user is not listed. userId = {}", modifier.getUserId());
			responseFormat = componentsUtils.getResponseFormat(ActionStatus.RESTRICTED_OPERATION);
			handleAuditing(modifier, null, null, responseFormat, AuditingActionEnum.UPDATE_USER);
			return Either.right(responseFormat);
		}

		modifier = eitherCreator.left().value();
		if (!modifier.getRole().equals(UserRoleEnum.ADMIN.getName())) {
			log.debug("updateUserRole method - user is not admin. userId = {}", modifier.getUserId());
			responseFormat = componentsUtils.getResponseFormat(ActionStatus.RESTRICTED_OPERATION);
			handleAuditing(modifier, null, null, responseFormat, AuditingActionEnum.UPDATE_USER);
			return Either.right(responseFormat);
		}

		if (modifier.getUserId().equals(userIdToUpdate)) {
			log.debug("updateUserRole method - admin role can only be updated by other admin. userId = {}", modifier.getUserId());
			responseFormat = componentsUtils.getResponseFormat(ActionStatus.UPDATE_USER_ADMIN_CONFLICT);
			handleAuditing(modifier, null, null, responseFormat, AuditingActionEnum.UPDATE_USER);
			return Either.right(responseFormat);
		}

		Either<User, ActionStatus> userToUpdateReq = getUser(userIdToUpdate, false);
		if (userToUpdateReq.isRight() || userToUpdateReq.left().value() == null) {
			log.debug("updateUserRole method - user not found. userId = {}", modifier.getUserId());
			responseFormat = componentsUtils.getResponseFormat(ActionStatus.USER_NOT_FOUND, userIdToUpdate);
			handleAuditing(modifier, null, null, responseFormat, AuditingActionEnum.UPDATE_USER);
			return Either.right(responseFormat);
		}

		if (!userAdminValidator.validateRole(userRole)) {
			log.debug("updateUserRole method - user has invalid role = {}", modifier.getUserId());
			responseFormat = componentsUtils.getResponseFormat(ActionStatus.INVALID_ROLE, userRole);
			handleAuditing(modifier, null, null, responseFormat, AuditingActionEnum.UPDATE_USER);
			return Either.right(responseFormat);
		}

		User newUser = new User();
		newUser.setRole(userRole);
		newUser.setUserId(userIdToUpdate);
		User userToUpdate = userToUpdateReq.left().value();
		// if(!userRole.equals(UserRoleEnum.ADMIN.getName())){ //this is in
		// comment until admin will be able to do do check-in/check-out from the
		// UI

		Either<List<Edge>, StorageOperationStatus> userPendingTasksReq = getPendingUserPendingTasksWithCommit(userToUpdate);
		if (userPendingTasksReq.isRight()) {
			log.debug("updateUserRole method - failed to get user pending tasks list userId {}", userIdToUpdate);
			return Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(userPendingTasksReq.right().value())));
		}

		List<Edge> userPendingTasks = userPendingTasksReq.left().value();
		if (!userPendingTasks.isEmpty()) {
			log.debug("updateUserRole method - User canot be updated, user have pending projects userId {}", userIdToUpdate);
			
			String userTasksStatusForErrorMessage = getUserPendingTaskStatusByRole(UserRoleEnum.valueOf(userToUpdate.getRole()));
			String userInfo = userToUpdate.getFirstName() + " " + userToUpdate.getLastName() + '(' + userToUpdate.getUserId() + ')';
			responseFormat = componentsUtils.getResponseFormat(ActionStatus.CANNOT_UPDATE_USER_WITH_ACTIVE_ELEMENTS, userInfo, userTasksStatusForErrorMessage);
			handleAuditing(modifier, userToUpdate, userToUpdate, responseFormat, AuditingActionEnum.UPDATE_USER);
			return Either.right(responseFormat);
		}
		// }
		Either<User, StorageOperationStatus> updateUserReq = userAdminOperation.updateUserData(newUser);

		if (updateUserReq.isRight() || updateUserReq.left().value() == null) {
			log.debug("updateUser method - failed to update user data. userId = {}", modifier.getUserId());
			return Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(updateUserReq.right().value())));
		}

		responseFormat = componentsUtils.getResponseFormat(ActionStatus.OK);
		User updatedUser = updateUserReq.left().value();
		handleAuditing(modifier, userToUpdate, updatedUser, responseFormat, AuditingActionEnum.UPDATE_USER);
		return Either.left(updatedUser);
	}

	@Override
	public Either<List<User>, ResponseFormat> getAllAdminUsers(ServletContext context) {
		Either<List<User>, ActionStatus> response = userAdminOperation.getAllUsersWithRole(Role.ADMIN.name(), null);

		if (response.isRight()) {
			ResponseFormat responseFormat = componentsUtils.getResponseFormat(response.right().value());
			return Either.right(responseFormat);
		}
		return Either.left(response.left().value());
	}

	@Override
	public Either<List<User>, ResponseFormat> getUsersList(String modifierAttId, List<String> roles, String rolesStr) {
		ResponseFormat responseFormat;
		User user = new User();
		if (modifierAttId == null) {
			user.setUserId("UNKNOWN");
			responseFormat = componentsUtils.getResponseFormat(ActionStatus.MISSING_INFORMATION);
			handleGetUsersListAuditing(user, responseFormat, rolesStr);
			return Either.right(responseFormat);
		}
		Either<User, ActionStatus> userResult = getUser(modifierAttId, false);
		if (userResult.isRight()) {
			user.setUserId(modifierAttId);
			if (userResult.right().value().equals(ActionStatus.USER_NOT_FOUND)) {
				responseFormat = componentsUtils.getResponseFormat(ActionStatus.RESTRICTED_OPERATION);
			} else {
				responseFormat = componentsUtils.getResponseFormat(userResult.right().value());
			}
			BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeUserMissingError, "Get users per roles", modifierAttId);
			BeEcompErrorManager.getInstance().logBeUserMissingError("Get users per roles", modifierAttId);

			handleGetUsersListAuditing(user, responseFormat, rolesStr);
			return Either.right(responseFormat);
		}
		user = userResult.left().value();
		Either<List<User>, ResponseFormat> getResponse = null;
		List<User> resultList = new ArrayList<>();
		if (roles != null && !roles.isEmpty()) {
			for (String role : roles) {
				if (!userAdminValidator.validateRole(role)) {
					responseFormat = componentsUtils.getResponseFormat(ActionStatus.INVALID_ROLE, role);
					handleGetUsersListAuditing(user, responseFormat, rolesStr);
					return Either.right(responseFormat);
				}
				getResponse = getUsersPerRole(role, user, rolesStr);
				resultList.addAll(getResponse.left().value());
			}
		} else {
			rolesStr = "All";
			getResponse = getUsersPerRole(null, user, rolesStr);
			resultList.addAll(getResponse.left().value());
		}
		responseFormat = componentsUtils.getResponseFormat(ActionStatus.OK);
		handleGetUsersListAuditing(user, responseFormat, rolesStr);
		return Either.left(resultList);
	}

	private Either<List<User>, ResponseFormat> getUsersPerRole(String role, User user, String rolesStr) {
		ResponseFormat responseFormat;
		Either<List<User>, ActionStatus> response = userAdminOperation.getAllUsersWithRole(role, UserStatusEnum.ACTIVE.name());
		if (response.isRight()) {
			responseFormat = componentsUtils.getResponseFormat(response.right().value());
			handleGetUsersListAuditing(user, responseFormat, rolesStr);
			return Either.right(responseFormat);
		}
		return Either.left(response.left().value());
	}

	private void handleGetUsersListAuditing(User user, ResponseFormat responseFormat, String details) {
		componentsUtils.auditGetUsersList(AuditingActionEnum.GET_USERS_LIST, user, details, responseFormat);
	}

	private void handleAuditing(User modifier, User userBefor, User userAfter, ResponseFormat responseFormat, AuditingActionEnum actionName) {
		componentsUtils.auditAdminUserAction(actionName, modifier, userBefor, userAfter, responseFormat);
	}

	private void handleUserAccessAuditing(User user, ResponseFormat responseFormat, AuditingActionEnum actionName) {
		componentsUtils.auditUserAccess(actionName, user, responseFormat);
	}

	@Override
	public Either<User, ResponseFormat> deActivateUser(User modifier, String userUniuqeIdToDeactive) {

		ResponseFormat responseFormat;
		String userId = modifier.getUserId();

		if (userId == null) {
			modifier.setUserId("UNKNOWN");
			log.debug("deActivateUser method -  user header is missing");
			responseFormat = componentsUtils.getResponseFormat(ActionStatus.MISSING_INFORMATION);
			handleAuditing(modifier, null, null, responseFormat, AuditingActionEnum.DELETE_USER);
			return Either.right(responseFormat);
		}

		Either<User, ActionStatus> eitherCreator = getUser(userId, false);
		if (eitherCreator.isRight() || eitherCreator.left().value() == null) {
			log.debug("deActivateUser method - user is not listed. userId = {}", modifier.getUserId());
			responseFormat = componentsUtils.getResponseFormat(ActionStatus.RESTRICTED_OPERATION);
			handleAuditing(modifier, null, null, responseFormat, AuditingActionEnum.DELETE_USER);
			return Either.right(responseFormat);
		}

		modifier = eitherCreator.left().value();

		if (!modifier.getRole().equals(UserRoleEnum.ADMIN.getName())) {
			log.debug("deActivateUser method - user is not admin. userId = {}", modifier.getUserId());
			responseFormat = componentsUtils.getResponseFormat(ActionStatus.RESTRICTED_OPERATION);
			handleAuditing(modifier, null, null, responseFormat, AuditingActionEnum.DELETE_USER);
			return Either.right(responseFormat);
		}

		if (modifier.getUserId().equals(userUniuqeIdToDeactive)) {
			log.debug("deActivateUser deActivateUser - admin can only be deactivate by other admin. userId = {}", modifier.getUserId());
			responseFormat = componentsUtils.getResponseFormat(ActionStatus.DELETE_USER_ADMIN_CONFLICT);
			handleAuditing(modifier, null, null, responseFormat, AuditingActionEnum.DELETE_USER);
			return Either.right(responseFormat);
		}

		Either<User, ActionStatus> getUserToDeleteResponse = getUser(userUniuqeIdToDeactive, false);
		if (getUserToDeleteResponse.isRight() || getUserToDeleteResponse.left().value() == null) {
			log.debug("deActivateUser method - failed to get user by id {}", userUniuqeIdToDeactive);
			responseFormat = componentsUtils.getResponseFormat(ActionStatus.USER_NOT_FOUND, userUniuqeIdToDeactive);
			handleAuditing(modifier, null, null, responseFormat, AuditingActionEnum.DELETE_USER);
			return Either.right(componentsUtils.getResponseFormat(getUserToDeleteResponse.right().value(), userUniuqeIdToDeactive));
		}

		User userToDeactivate = getUserToDeleteResponse.left().value();
		if (userToDeactivate.getStatus().equals(UserStatusEnum.INACTIVE)) {
			log.debug("deActivateUser method - User already inactive", userUniuqeIdToDeactive);
			responseFormat = componentsUtils.getResponseFormat(ActionStatus.USER_NOT_FOUND, userUniuqeIdToDeactive);
			handleAuditing(modifier, null, null, responseFormat, AuditingActionEnum.DELETE_USER);
			return Either.right(responseFormat);
		}

		Either<List<Edge>, StorageOperationStatus> userPendingTasksReq = getPendingUserPendingTasksWithCommit(userToDeactivate);
		if (userPendingTasksReq.isRight()) {
			log.debug("deActivateUser method - failed to get user pending tasks list", userUniuqeIdToDeactive);
			return Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(userPendingTasksReq.right().value())));
		}

		List<Edge> userPendingTasks = userPendingTasksReq.left().value();
		if (userPendingTasks.size() > 0) {
			log.debug("deActivateUser method - User canot be deleted, user have pending projects", userUniuqeIdToDeactive);

			String userTasksStatusForErrorMessage = getUserPendingTaskStatusByRole(UserRoleEnum.valueOf(userToDeactivate.getRole()));
			String userInfo = userToDeactivate.getFirstName() + " " + userToDeactivate.getLastName() + '(' + userToDeactivate.getUserId() + ')';
			responseFormat = componentsUtils.getResponseFormat(ActionStatus.CANNOT_DELETE_USER_WITH_ACTIVE_ELEMENTS, userInfo, userTasksStatusForErrorMessage);
			handleAuditing(modifier, userToDeactivate, userToDeactivate, responseFormat, AuditingActionEnum.DELETE_USER);
			return Either.right(responseFormat);
		}

		Either<User, StorageOperationStatus> deactivateUserReq = userAdminOperation.deActivateUser(userToDeactivate);
		if (deactivateUserReq.isRight()) {
			log.debug("deActivateUser method - failed to deactivate user", userUniuqeIdToDeactive);
			return Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(deactivateUserReq.right().value())));
		}
		User deactivateUser = deactivateUserReq.left().value();
		responseFormat = componentsUtils.getResponseFormat(ActionStatus.OK);
		handleAuditing(modifier, userToDeactivate, null, responseFormat, AuditingActionEnum.DELETE_USER);
		return Either.left(deactivateUser);
	}

	@Override
	public Either<User, ResponseFormat> authorize(User authUser) {

		ResponseFormat responseFormat;

		String userId = authUser.getUserId();

		if (userId == null) {
			authUser.setUserId("UNKNOWN");
			log.debug("deActivateUser method -  user header is missing");
			responseFormat = componentsUtils.getResponseFormat(ActionStatus.MISSING_INFORMATION);
			handleUserAccessAuditing(authUser, responseFormat, AuditingActionEnum.USER_ACCESS);
			return Either.right(responseFormat);
		}

		Either<User, ActionStatus> eitherCreator = getUser(userId, false);
		if (eitherCreator.isRight()) {
			if (eitherCreator.right().value() == ActionStatus.USER_NOT_FOUND || eitherCreator.right().value() == ActionStatus.USER_INACTIVE) {
				responseFormat = componentsUtils.getResponseFormat(ActionStatus.RESTRICTED_ACCESS);
				handleUserAccessAuditing(authUser, responseFormat, AuditingActionEnum.USER_ACCESS);
				return Either.right(responseFormat);
			} else {
				return Either.right(componentsUtils.getResponseFormatByUser(eitherCreator.right().value(), authUser));
			}
		} else {
			if (eitherCreator.left().value() == null) {
				responseFormat = componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR);
				return Either.right(responseFormat);
			}
		}

		User user = eitherCreator.left().value();

		String firstName = authUser.getFirstName();
		if (firstName != null && firstName.isEmpty() == false && !firstName.equals(user.getFirstName())) {
			user.setFirstName(firstName);
		}

		String lastName = authUser.getLastName();
		if (lastName != null && lastName.isEmpty() == false && !lastName.equals(user.getLastName())) {
			user.setLastName(lastName);
		}

		String email = authUser.getEmail();
		if (email != null && false == email.isEmpty() && !email.equals(user.getEmail())) {
			user.setEmail(email);
		}

		// last login time stamp handle
		user.setLastLoginTime();

		Either<User, StorageOperationStatus> updateUserReq = userAdminOperation.updateUserData(user);

		if (updateUserReq.isRight()) {
			responseFormat = componentsUtils.getResponseFormatByUser(eitherCreator.right().value(), user);
			handleUserAccessAuditing(user, responseFormat, AuditingActionEnum.USER_ACCESS);
			return Either.right(componentsUtils.getResponseFormatByUser(eitherCreator.right().value(), user));
		}

		User updatedUser = updateUserReq.left().value();

		Long lastLoginTime = user.getLastLoginTime();
		if (lastLoginTime != null) {
			updatedUser.setLastLoginTime(lastLoginTime);
		} else {
			updatedUser.setLastLoginTime(new Long(0));
		}

		responseFormat = componentsUtils.getResponseFormat(ActionStatus.OK);
		handleUserAccessAuditing(updatedUser, responseFormat, AuditingActionEnum.USER_ACCESS);
		ASDCKpiApi.countUsersAuthorizations();
		return Either.left(updatedUser);
	}

	/*
	 * The method updates user credentials only, the role is neglected The role updated through updateRole method
	 */
	public Either<User, ResponseFormat> updateUserCredentials(User updatedUserCred) {

		ResponseFormat responseFormat;

		String userId = updatedUserCred.getUserId();

		if (userId == null) {
			updatedUserCred.setUserId("UNKNOWN");
			log.debug("updateUserCredentials method - user header is missing");
			responseFormat = componentsUtils.getResponseFormat(ActionStatus.MISSING_INFORMATION);
			handleUserAccessAuditing(updatedUserCred, responseFormat, AuditingActionEnum.USER_ACCESS);
			return Either.right(responseFormat);
		}

		Either<User, ActionStatus> eitherCreator = getUser(userId, false);
		if (eitherCreator.isRight()) {
			ActionStatus status = eitherCreator.right().value();
			if (status == ActionStatus.USER_NOT_FOUND || status == ActionStatus.USER_INACTIVE) {
				responseFormat = componentsUtils.getResponseFormat(ActionStatus.RESTRICTED_ACCESS);
				handleUserAccessAuditing(updatedUserCred, responseFormat, AuditingActionEnum.USER_ACCESS);
				return Either.right(responseFormat);
			} else {
				return Either.right(componentsUtils.getResponseFormatByUser(status, updatedUserCred));
			}
		} else {
			if (eitherCreator.left().value() == null) {
				responseFormat = componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR);
				return Either.right(responseFormat);
			}
		}

		User user = eitherCreator.left().value();

		String firstName = updatedUserCred.getFirstName();
		if (firstName != null && firstName.isEmpty() == false && !firstName.equals(user.getFirstName())) {
			user.setFirstName(firstName);
		}

		String lastName = updatedUserCred.getLastName();
		if (lastName != null && lastName.isEmpty() == false && !lastName.equals(user.getLastName())) {
			user.setLastName(lastName);
		}

		String email = updatedUserCred.getEmail();
		if (email != null && false == email.isEmpty() && !email.equals(user.getEmail())) {
			user.setEmail(email);
		}

		if (updatedUserCred.getLastLoginTime() != null && user.getLastLoginTime() != null) {
			if (updatedUserCred.getLastLoginTime() > user.getLastLoginTime()) {
				user.setLastLoginTime(updatedUserCred.getLastLoginTime());
			}
		} else if (updatedUserCred.getLastLoginTime() != null && user.getLastLoginTime() == null) {
			user.setLastLoginTime(updatedUserCred.getLastLoginTime());
		}

		Either<User, StorageOperationStatus> updateUserReq = userAdminOperation.updateUserData(user);

		if (updateUserReq.isRight()) {
			responseFormat = componentsUtils.getResponseFormatByUser(eitherCreator.right().value(), user);
			handleUserAccessAuditing(user, responseFormat, AuditingActionEnum.USER_ACCESS);
			return Either.right(componentsUtils.getResponseFormatByUser(eitherCreator.right().value(), user));
		}

		User updatedUser = updateUserReq.left().value();

		responseFormat = componentsUtils.getResponseFormat(ActionStatus.OK);
		handleUserAccessAuditing(updatedUser, responseFormat, AuditingActionEnum.USER_ACCESS);
		return Either.left(updatedUser);
	}

	private Either<List<Edge>, StorageOperationStatus> getPendingUserPendingTasksWithCommit(User user) {

		Either<List<Edge>, StorageOperationStatus> result = null;

		try {
			UserRoleEnum userRole = UserRoleEnum.valueOf(user.getRole());
			Map<String, Object> properties = new HashMap<String, Object>();
			switch (userRole) {
			case DESIGNER:
			case PRODUCT_STRATEGIST:
			case PRODUCT_MANAGER:
				properties.put(GraphPropertiesDictionary.STATE.getProperty(), LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT.name());
				return userAdminOperation.getUserPendingTasksList(user, properties);
			case TESTER:
				properties.put(GraphPropertiesDictionary.STATE.getProperty(), LifecycleStateEnum.CERTIFICATION_IN_PROGRESS.name());
				return userAdminOperation.getUserPendingTasksList(user, properties);
			case ADMIN:
				properties.put(GraphPropertiesDictionary.STATE.getProperty(), LifecycleStateEnum.CERTIFICATION_IN_PROGRESS.name());
				properties.put(GraphPropertiesDictionary.STATE.getProperty(), LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT.name());
				return userAdminOperation.getUserPendingTasksList(user, properties);
			default:
				return Either.left(new ArrayList<>());
			}
		} finally {
			// commit will be perform outside!!!
			if (result == null || result.isRight()) {
				log.debug("getUserPendingTasksList failed to perform fetching");
				titanDao.rollback();
			} else {
				titanDao.commit();
			}
		}
	}

	private String getUserPendingTaskStatusByRole(UserRoleEnum role) {

		switch (role) {
		case DESIGNER:
		case PRODUCT_STRATEGIST:
		case PRODUCT_MANAGER:
			return "checked-out";

		case TESTER:
			return "in-certification";
		case ADMIN:
			return "in-certification/checked-out";
		default:
			return "";
		}
	}
}
