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

import fj.data.Either;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.openecomp.sdc.be.components.impl.exceptions.ByActionStatusComponentException;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.utils.UserStatusEnum;
import org.openecomp.sdc.be.facade.operations.UserOperation;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.operations.impl.UserAdminOperation;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.common.api.UserRoleEnum;
import org.openecomp.sdc.common.datastructure.UserContext;
import org.openecomp.sdc.common.kpi.api.ASDCKpiApi;
import org.openecomp.sdc.common.log.enums.EcompLoggerErrorCode;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.common.util.ThreadLocalsHolder;
import org.openecomp.sdc.exception.ResponseFormat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum.ADD_USER;
import static org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum.GET_USERS_LIST;
import static org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum.UPDATE_USER;

@org.springframework.stereotype.Component
public class UserBusinessLogic {

    private static final Logger log = Logger.getLogger(UserBusinessLogic.class);
    private static final String IN_CERTIFICATION_CHECKED_OUT = "in-certification/checked-out";
    private static final String UNKNOWN = "UNKNOWN";
    private static UserAdminValidator userAdminValidator = UserAdminValidator.getInstance();

    private final UserAdminOperation userAdminOperation;
    private final ComponentsUtils componentsUtils;
    private final UserOperation facadeUserOperation;

    public UserBusinessLogic(UserAdminOperation userAdminOperation, ComponentsUtils componentsUtils, UserOperation facadeUserOperation) {
        this.userAdminOperation = userAdminOperation;
        this.componentsUtils = componentsUtils;
        this.facadeUserOperation = facadeUserOperation;
    }

    public User getUser(String userId, boolean inTransaction) {
        Either<User, ActionStatus> result = userAdminOperation.getUserData(userId, inTransaction);
        if (result.isRight()) {
            handleUserAccessAuditing(userId, result.right().value());
            throw new ByActionStatusComponentException(result.right().value(), userId);
        }
        User user = result.left().value();
        if (user == null) {
            handleUserAccessAuditing(userId, ActionStatus.GENERAL_ERROR);
            throw new ByActionStatusComponentException(ActionStatus.GENERAL_ERROR);
        }
        return user;
    }

    public User getUser(String userId) {
        UserContext userContext = ThreadLocalsHolder.getUserContext();
        if (Objects.isNull(userContext) || Objects.isNull(userContext.getUserId())) {
            log.info("USER_NOT_FOUND, user=" + userId);
            handleUserAccessAuditing(userId, ActionStatus.USER_NOT_FOUND);
            throw new ByActionStatusComponentException(ActionStatus.USER_NOT_FOUND, userId);
        }
        if (Objects.isNull(userContext.getUserRoles())){
            userContext.setUserRoles(new HashSet<>());
        }
        return convertUserContextToUser(userContext);
    }

    protected User convertUserContextToUser(UserContext userContext) {
        User user = new User();
        user.setUserId(userContext.getUserId());
        user.setFirstName(userContext.getFirstName());
        user.setLastName(userContext.getLastName());
        boolean userHasRoles = userContext.getUserRoles().iterator().hasNext();
        user.setRole(!userHasRoles ? null : userContext.getUserRoles().iterator().next());
        user.setStatus(userHasRoles ? UserStatusEnum.ACTIVE : UserStatusEnum.INACTIVE);
        return user;
    }

    public boolean hasActiveUser(String userId) {
        UserContext userContext = ThreadLocalsHolder.getUserContext();
        if (Objects.isNull(userContext) || Objects.isNull(userContext.getUserId()) ) {
            handleUserAccessAuditing(userId, ActionStatus.USER_NOT_FOUND);
            return false;
        }
        if (Objects.isNull(userContext.getUserRoles()) || userContext.getUserRoles().isEmpty()){
            handleUserAccessAuditing(userId, ActionStatus.USER_INACTIVE);
            return false;
        }
        return true;
    }

    public User createUser(String modifierUserId, User newUser) {

        User modifier = getValidModifier(modifierUserId, newUser.getUserId(), AuditingActionEnum.ADD_USER);

        // verify user not exist
        String newUserId = newUser.getUserId();
        Either<User, ActionStatus> eitherUserInDB = verifyNewUser(newUserId);
        newUser.setStatus(UserStatusEnum.ACTIVE);

        validateEmail(newUser);

        validateRole(newUser);

        // handle last login if user is import
        if (newUser.getLastLoginTime() == null) {
            newUser.setLastLoginTime(0L);
        }

        User createdUser;
        if (ActionStatus.USER_INACTIVE.equals(eitherUserInDB.right().value())) { // user inactive - update state                                                                                  // exist
            newUser.setLastLoginTime(0L);
            createdUser = userAdminOperation.updateUserData(newUser);
        } else { // user does not exist - create new user
            if (!userAdminValidator.validateUserId(newUserId)) {
                log.debug("createUser method - user has invalid userId = {}", newUser.getUserId());
                throw new ByActionStatusComponentException(ActionStatus.INVALID_USER_ID, newUserId);
            }
            createdUser = userAdminOperation.saveUserData(newUser);
        }
        ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.CREATED);
        handleAuditing(modifier, null, createdUser, responseFormat, AuditingActionEnum.ADD_USER);
        getFacadeUserOperation().updateUserCache(UserOperationEnum.CREATE, createdUser.getUserId(), createdUser.getRole());
        return createdUser;
    }

    private void validateRole(User newUser) {
        if (newUser.getRole() == null || newUser.getRole().length() == 0) {
            newUser.setRole(Role.DESIGNER.name());
        } else {
            if (!userAdminValidator.validateRole(newUser.getRole())) {
                log.debug("createUser method - user has invalid role = {}", newUser.getUserId());
                throw new ByActionStatusComponentException(ActionStatus.INVALID_ROLE, newUser.getRole());
            }
        }
    }

    private void validateEmail(User newUser) {
        if (newUser.getEmail() != null && !userAdminValidator.validateEmail(newUser.getEmail())) {
            log.debug("createUser method - user has invalid email = {}", newUser.getUserId());
            throw new ByActionStatusComponentException(ActionStatus.INVALID_EMAIL_ADDRESS, newUser.getEmail());
        }
    }

    private Either<User, ActionStatus> verifyNewUser(String newUserId) {
        Either<User, ActionStatus> eitherUserInDB = getUserData(newUserId);
        if (eitherUserInDB.isRight()) {
            ActionStatus status = eitherUserInDB.right().value();
            if (!ActionStatus.USER_NOT_FOUND.equals(status) && !ActionStatus.USER_INACTIVE.equals(status)) {
                componentsUtils.auditAdminUserActionAndThrowException(ADD_USER, null, null, null, status, newUserId);
            }
        } else {// User exist in DB
            User userFromDb = eitherUserInDB.left().value();
            if (userFromDb.getStatus() == UserStatusEnum.ACTIVE) {
                log.debug("createUser method - user with id {} already exist with id: {}", newUserId, userFromDb.getUserId());
                componentsUtils.auditAdminUserActionAndThrowException(ADD_USER, null, null, null, ActionStatus.USER_ALREADY_EXIST, newUserId);
            }
        }
        return eitherUserInDB;
    }

    public Either<User, ActionStatus> verifyNewUserForPortal(String newUserId) {
        Either<User, ActionStatus> eitherUserInDB = getUserData(newUserId);
        if (eitherUserInDB.isRight()) {
            ActionStatus status = eitherUserInDB.right().value();
            if (!ActionStatus.USER_NOT_FOUND.equals(status) && !ActionStatus.USER_INACTIVE.equals(status)) {
                componentsUtils.auditAdminUserActionAndThrowException(ADD_USER, null, null, null, status, newUserId);
            }
        }

        return eitherUserInDB;
    }

    private Either<User, ActionStatus> getUserData(String newUserId) {
        if (newUserId == null) {
            log.error(EcompLoggerErrorCode.DATA_ERROR, "", "","Create user - new user id is missing");
            throw new ByActionStatusComponentException(ActionStatus.MISSING_INFORMATION);
        }

        return userAdminOperation.getUserData(newUserId, false);
    }

    public User updateUserRole(String modifierUserId, String userIdToUpdate, String userRole) {

        User modifier = getValidModifier(modifierUserId, userIdToUpdate, UPDATE_USER);
        User userToUpdate = getUser(userIdToUpdate, false);
        validateChangeRoleToAllowedRoles(userRole);

        List<Edge> userPendingTasks = userAdminOperation.getUserPendingTasksList(userToUpdate, getChangeRoleStateLimitations(userToUpdate));
        if (!userPendingTasks.isEmpty()) {
            log.debug("updateUserRole method - User cannot be updated, user have pending projects userId {}", userIdToUpdate);
            String userInfo = userToUpdate.getFirstName() + " " + userToUpdate.getLastName() + '(' + userToUpdate.getUserId() + ')';
            componentsUtils.auditAdminUserActionAndThrowException(UPDATE_USER, modifier, userToUpdate, null, ActionStatus.CANNOT_UPDATE_USER_WITH_ACTIVE_ELEMENTS, userInfo, IN_CERTIFICATION_CHECKED_OUT);
        }

        Role newRole = Role.valueOf(userRole);
        User newUser = new User();
        newUser.setRole(newRole.name());
        newUser.setUserId(userIdToUpdate);

        User updatedUser = userAdminOperation.updateUserData(newUser);
        handleAuditing(modifier, userToUpdate, updatedUser, componentsUtils.getResponseFormat(ActionStatus.OK), UPDATE_USER);
        getFacadeUserOperation().updateUserCache(UserOperationEnum.CHANGE_ROLE, updatedUser.getUserId(), updatedUser.getRole());
        return updatedUser;
    }

    private void validateChangeRoleToAllowedRoles(String userRole) {
        List<String> allowedRoles = Arrays.asList(UserRoleEnum.DESIGNER.getName(), UserRoleEnum.ADMIN.getName());
        if (!allowedRoles.contains(userRole)){
            throw new ByActionStatusComponentException(ActionStatus.INVALID_ROLE, userRole);
        }
    }

    User getValidModifier(String modifierUserId, String userIdHandle, AuditingActionEnum actionEnum) {
        if (modifierUserId == null) {
            log.error(EcompLoggerErrorCode.DATA_ERROR, "", "", "user modifier is missing");
            throw new ByActionStatusComponentException(ActionStatus.MISSING_INFORMATION);
        }

        User modifier = getUser(modifierUserId, false);
        if (!modifier.getRole().equals(UserRoleEnum.ADMIN.getName())) {
            log.debug("user is not admin. Id = {}", modifier.getUserId());
            componentsUtils.auditAdminUserActionAndThrowException(actionEnum, modifier, null, null, ActionStatus.RESTRICTED_OPERATION);
        }

        if (modifier.getUserId().equals(userIdHandle)) {
            log.debug("admin user cannot act on self. Id = {}", modifier.getUserId());
            componentsUtils.auditAdminUserActionAndThrowException(actionEnum, modifier, null, null, ActionStatus.UPDATE_USER_ADMIN_CONFLICT);
        }
        return modifier;
    }

    public List<User> getAllAdminUsers() {
        Either<List<User>, ActionStatus> response = userAdminOperation.getAllUsersWithRole(Role.ADMIN.name(), null);
        if (response.isRight()) {
            throw new ByActionStatusComponentException(response.right().value());
        }
        return response.left().value();
    }

    public List<User> getUsersList(String modifierAttId, List<String> roles, String rolesStr) {
        if (modifierAttId == null) {
            throw new ByActionStatusComponentException(ActionStatus.MISSING_INFORMATION);
        }
        User user = getUser(modifierAttId, false);
        Either<List<User>, ResponseFormat> getResponse;
        List<User> userList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(roles)) {
            for (String role : roles) {
                if (!userAdminValidator.validateRole(role)) {
                    componentsUtils.auditAdminUserActionAndThrowException(GET_USERS_LIST, user, null, null, ActionStatus.INVALID_ROLE, role);
                }
                getResponse = getUsersPerRole(role, user, rolesStr);
                userList.addAll(getResponse.left().value());
            }
        } else {
            rolesStr = "All";
            getResponse = getUsersPerRole(null, user, rolesStr);
            userList.addAll(getResponse.left().value());
        }
        handleGetUsersListAuditing(user, componentsUtils.getResponseFormat(ActionStatus.OK), rolesStr);
        return userList;
    }

    Either<List<User>, ResponseFormat> getUsersPerRole(String role, User user, String rolesStr) {
        ResponseFormat responseFormat;
        Either<List<User>, ActionStatus> response = userAdminOperation.getAllUsersWithRole(role, UserStatusEnum.ACTIVE.name());
        if (response.isRight()) {
            responseFormat = componentsUtils.getResponseFormat(response.right().value());
            handleGetUsersListAuditing(user, responseFormat, rolesStr);
            return Either.right(responseFormat);
        }
        List<User> users = response.left().value()
                .stream()
                .filter(u-> StringUtils.isNotEmpty(u.getUserId()))
                .collect(Collectors.toList());
        return Either.left(users);
    }

    private void handleGetUsersListAuditing(User user, ResponseFormat responseFormat, String details) {
        componentsUtils.auditGetUsersList(user, details, responseFormat);
    }

    private void handleAuditing(User modifier, User userBefore, User userAfter, ResponseFormat responseFormat, AuditingActionEnum actionName) {
        componentsUtils.auditAdminUserAction(actionName, modifier, userBefore, userAfter, responseFormat);
    }

    private void handleUserAccessAuditing(User user, ResponseFormat responseFormat) {
        componentsUtils.auditUserAccess(user, responseFormat);
    }

    private void handleUserAccessAuditing(String userId, ActionStatus status, String... params) {
        componentsUtils.auditUserAccess(new User(userId), status, params);
    }

    public User authorize(User authUser) {
        String userId = authUser.getUserId();
        if (userId == null) {
            log.debug("authorize method -  user id is missing");
            throw new ByActionStatusComponentException(ActionStatus.MISSING_INFORMATION);
        }

        User user = getUser(userId, false);

        String firstName = authUser.getFirstName();
        if (firstName != null && !firstName.isEmpty() && !firstName.equals(user.getFirstName())) {
            user.setFirstName(firstName);
        }

        String lastName = authUser.getLastName();
        if (lastName != null && !lastName.isEmpty() && !lastName.equals(user.getLastName())) {
            user.setLastName(lastName);
        }

        String email = authUser.getEmail();
        if (email != null && !email.isEmpty() && !email.equals(user.getEmail())) {
            user.setEmail(email);
        }

        // last login time stamp handle
        user.setLastLoginTime();

        User updatedUser = userAdminOperation.updateUserData(user);
        Long lastLoginTime = user.getLastLoginTime();
        if (lastLoginTime != null) {
            updatedUser.setLastLoginTime(lastLoginTime);
        } else {
            updatedUser.setLastLoginTime(0L);
        }

        handleUserAccessAuditing(updatedUser.getUserId(), ActionStatus.OK);
        ASDCKpiApi.countUsersAuthorizations();
        return updatedUser;
    }

    /*
     * The method updates user credentials only, the role is neglected The role updated through updateRole method
     */
    public Either<User, ResponseFormat> updateUserCredentials(User updatedUserCred) {

        ResponseFormat responseFormat;

        String userId = updatedUserCred.getUserId();

        if (userId == null) {
            updatedUserCred.setUserId(UNKNOWN);
            log.debug("updateUserCredentials method - user header is missing");
            responseFormat = componentsUtils.getResponseFormat(ActionStatus.MISSING_INFORMATION);
            handleUserAccessAuditing(updatedUserCred, responseFormat);
            return Either.right(responseFormat);
        }

        User user = getUser(userId, false);
        String firstName = updatedUserCred.getFirstName();
        if (firstName != null && !firstName.isEmpty() && !firstName.equals(user.getFirstName())) {
            user.setFirstName(firstName);
        }

        String lastName = updatedUserCred.getLastName();
        if (lastName != null && !lastName.isEmpty() && !lastName.equals(user.getLastName())) {
            user.setLastName(lastName);
        }

        String email = updatedUserCred.getEmail();
        if (email != null && !email.isEmpty() && !email.equals(user.getEmail())) {
            user.setEmail(email);
        }

        if (updatedUserCred.getLastLoginTime() != null && user.getLastLoginTime() != null) {
            if (updatedUserCred.getLastLoginTime() > user.getLastLoginTime()) {
                user.setLastLoginTime(updatedUserCred.getLastLoginTime());
            }
        } else if (updatedUserCred.getLastLoginTime() != null && user.getLastLoginTime() == null) {
            user.setLastLoginTime(updatedUserCred.getLastLoginTime());
        }

        User updatedUser = userAdminOperation.updateUserData(user);
        responseFormat = componentsUtils.getResponseFormat(ActionStatus.OK);
        handleUserAccessAuditing(updatedUser, responseFormat);
        return Either.left(updatedUser);
    }

    private List<Object> getChangeRoleStateLimitations(User user) {
        UserRoleEnum role = UserRoleEnum.valueOf(user.getRole());
        List<Object> properties = new ArrayList<>();
        switch (role) {
            case DESIGNER:
            case PRODUCT_STRATEGIST:
            case PRODUCT_MANAGER:
            case ADMIN:
                properties.add(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT.name());
                break;
            case TESTER:
                // For tester we allow change role even if there are pending task (per US468155 in 1810)
            default:
        }
        return properties;
    }

    public UserOperation getFacadeUserOperation() {
        return facadeUserOperation;
    }

}
