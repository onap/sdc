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

package org.openecomp.sdc.be.ecomp;

import fj.data.Either;
import org.onap.portalsdk.core.onboarding.crossapi.IPortalRestAPIService;
import org.onap.portalsdk.core.onboarding.exception.CipherUtilException;
import org.onap.portalsdk.core.onboarding.exception.PortalAPIException;
import org.onap.portalsdk.core.onboarding.util.CipherUtil;
import org.onap.portalsdk.core.onboarding.util.PortalApiProperties;
import org.onap.portalsdk.core.restful.domain.EcompRole;
import org.onap.portalsdk.core.restful.domain.EcompUser;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.config.BeEcompErrorManager.ErrorSeverity;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.ecomp.converters.EcompRoleConverter;
import org.openecomp.sdc.be.ecomp.converters.EcompUserConverter;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.user.Role;
import org.openecomp.sdc.be.user.UserBusinessLogic;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.exception.ResponseFormat;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.ContextLoader;

import javax.servlet.http.HttpServletRequest;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


public class EcompIntImpl implements IPortalRestAPIService {
	private static final String FAILED_TO_CONVERT_ROLES = "Failed to convert Roles";
	private static final String FAILED_TO_GET_ROLES = "Failed to get Roles";
	private static final String GET_USER_ROLES = "GetUserRoles";
	private static final String ERROR_FAILED_TO_GET_ROLES = "Error: Failed to get Roles";
	private static final String PUSH_USER_ROLE = "PushUserRole";
	private static final String FAILED_TO_FETCH_ROLES = "Failed to fetch roles";
	private static final String FAILED_TO_CONVERT_USER2 = "Failed to convert User {}";
	private static final String GET_USERS = "GetUsers";
	private static final String FAILED_TO_GET_USERS = "Failed to get Users";
	private static final String GET_USER = "GetUser";
	private static final String FAILED_TO_GET_USER = "Failed to get User";
	private static final String FAILED_TO_UPDATE_USER_CREDENTIALS = "Failed to updateUserCredentials";
	private static final String FAILED_TO_EDIT_USER = "Failed to edit user";
	private static final String EDIT_USER = "EditUser";
	private static final String RECEIVED_NULL_FOR_ARGUMENT_LOGIN_ID = "Received null for argument loginId";
	private static final String NULL_POINTER_RETURNED_FROM_USER_CONVERTER = "NULL pointer returned from user converter";
	private static final String FAILED_TO_CREATE_USER = "Failed to create user {}";
	private static final String FAILED_TO_CONVERT_USER = "Failed to convert user";
	private static final String JH0003 = "jh0003";
	private static final String PUSH_USER = "PushUser";
	private static final String RECEIVED_NULL_FOR_ARGUMENT_USER = "Received null for argument user";
	private static final Logger log = Logger.getLogger(EcompIntImpl.class.getName());

    public EcompIntImpl() {
        log.debug("EcompIntImpl Class Instantiated");
    }

    @Override
    public void pushUser(EcompUser user) throws PortalAPIException {
        log.debug("Start handle request of ECOMP pushUser");
        try {
            if (user == null) {
                BeEcompErrorManager.getInstance().logInvalidInputError(PUSH_USER, RECEIVED_NULL_FOR_ARGUMENT_USER, ErrorSeverity.INFO);
                log.debug(RECEIVED_NULL_FOR_ARGUMENT_USER);
                throw new PortalAPIException(RECEIVED_NULL_FOR_ARGUMENT_USER);
            }

            UserBusinessLogic userBusinessLogic = getUserBusinessLogic();

            final String modifierAttId = JH0003;
            User modifier = new User();
            modifier.setUserId(modifierAttId);
            log.debug("modifier id is {}", modifierAttId);

            User convertedAsdcUser = EcompUserConverter.convertEcompUserToUser(user);
            if (convertedAsdcUser == null) {
                BeEcompErrorManager.getInstance().logInvalidInputError(PUSH_USER, FAILED_TO_CONVERT_USER, ErrorSeverity.INFO);
                log.debug(FAILED_TO_CREATE_USER, user);
                throw new PortalAPIException("Failed to convert user " + user);
            }

            Either<User, ResponseFormat> createUserResponse = userBusinessLogic.createUser(modifier, convertedAsdcUser);

            // ALREADY EXIST ResponseFormat
            final String ALREADY_EXISTS_RESPONSE_ID = "SVC4006";

            if (createUserResponse.isRight()) {
                if (!createUserResponse.right().value().getMessageId().equals(ALREADY_EXISTS_RESPONSE_ID)) {
                    log.debug(FAILED_TO_CREATE_USER, user);
                    BeEcompErrorManager.getInstance().logInvalidInputError(PUSH_USER, FAILED_TO_CREATE_USER, ErrorSeverity.ERROR);
                    throw new PortalAPIException(FAILED_TO_CREATE_USER + createUserResponse.right());
                }
                log.debug("User already exist {}", user);
            }
            log.debug("User created {}", user);
        } catch (Exception e) {
            log.debug(FAILED_TO_CREATE_USER, user, e);
            BeEcompErrorManager.getInstance().logInvalidInputError(PUSH_USER, FAILED_TO_CREATE_USER, ErrorSeverity.ERROR);
            throw new PortalAPIException(FAILED_TO_CREATE_USER, e);
        }
    }

    /*
     * (non-Javadoc)
     *
     *
     * loginId - equals to userId
     *
     */
    @Override
    public void editUser(String loginId, EcompUser user) throws PortalAPIException {
        log.debug("Start handle request of ECOMP editUser");

        try {
            if (user == null) {
                log.debug(RECEIVED_NULL_FOR_ARGUMENT_USER);
                BeEcompErrorManager.getInstance().logInvalidInputError(EDIT_USER, RECEIVED_NULL_FOR_ARGUMENT_USER, ErrorSeverity.INFO);
                throw new PortalAPIException(RECEIVED_NULL_FOR_ARGUMENT_USER);
            } else if (loginId == null) {
                log.debug(RECEIVED_NULL_FOR_ARGUMENT_LOGIN_ID);
                BeEcompErrorManager.getInstance().logInvalidInputError(EDIT_USER, RECEIVED_NULL_FOR_ARGUMENT_LOGIN_ID, ErrorSeverity.INFO);
                throw new PortalAPIException(RECEIVED_NULL_FOR_ARGUMENT_LOGIN_ID);
            }

            UserBusinessLogic userBusinessLogic = getUserBusinessLogic();

            if (user.getLoginId() != null && !user.getLoginId().equals(loginId)) {
                log.debug("loginId and user loginId not equal");
                BeEcompErrorManager.getInstance().logInvalidInputError(EDIT_USER, "loginId and user loginId not equal", ErrorSeverity.INFO);
                throw new PortalAPIException("loginId not equals to the user loginId field");
            } else if (user.getLoginId() == null) {
                user.setLoginId(loginId);
            }

            User convertedAsdcUser = EcompUserConverter.convertEcompUserToUser(user);
            if (convertedAsdcUser == null) {
                BeEcompErrorManager.getInstance().logInvalidInputError(PUSH_USER, FAILED_TO_CONVERT_USER, ErrorSeverity.INFO);
                log.debug(FAILED_TO_CREATE_USER, user);
                throw new PortalAPIException("Failed to convert user " + user);
            }

            Either<User, ResponseFormat> updateUserCredentialsResponse = userBusinessLogic.updateUserCredentials(convertedAsdcUser);

            if (updateUserCredentialsResponse.isRight()) {
                log.debug(FAILED_TO_UPDATE_USER_CREDENTIALS);
                BeEcompErrorManager.getInstance().logInvalidInputError(EDIT_USER, FAILED_TO_UPDATE_USER_CREDENTIALS, ErrorSeverity.ERROR);
                throw new PortalAPIException(FAILED_TO_EDIT_USER + updateUserCredentialsResponse.right().value());
            }
        } catch (Exception e) {
            log.debug(FAILED_TO_UPDATE_USER_CREDENTIALS);
            throw new PortalAPIException(FAILED_TO_EDIT_USER, e);
        }

    }

    @Override
    public EcompUser getUser(String loginId) throws PortalAPIException {
        log.debug("Start handle request of ECOMP getUser");

        try {

            if (loginId == null) {
                log.debug(RECEIVED_NULL_FOR_ARGUMENT_LOGIN_ID);
                BeEcompErrorManager.getInstance().logInvalidInputError(GET_USER, RECEIVED_NULL_FOR_ARGUMENT_LOGIN_ID, ErrorSeverity.INFO);
                throw new PortalAPIException(RECEIVED_NULL_FOR_ARGUMENT_LOGIN_ID);
            }

            UserBusinessLogic userBusinessLogic = getUserBusinessLogic();

            Either<User, ActionStatus> getUserResponse = userBusinessLogic.getUser(loginId, false);

            if (getUserResponse.isRight()) {
                log.debug(FAILED_TO_GET_USER);
                BeEcompErrorManager.getInstance().logInvalidInputError(GET_USER, FAILED_TO_GET_USER, ErrorSeverity.INFO);
                throw new PortalAPIException(FAILED_TO_GET_USER + getUserResponse.right());
            } else {
                if (getUserResponse.left().value() != null) {
                    Either<EcompUser, String> ecompUser = EcompUserConverter.convertUserToEcompUser(getUserResponse.left().value());
                    if (ecompUser.isLeft() && ecompUser.left().value() != null) {
                        return ecompUser.left().value();
                    } else {
                        log.debug(FAILED_TO_GET_USER);
                        BeEcompErrorManager.getInstance().logInvalidInputError(GET_USER, FAILED_TO_GET_USER, ErrorSeverity.INFO);
                        throw new PortalAPIException(ecompUser.right().value());
                    }
                } else {
                    log.debug(FAILED_TO_GET_USER);
                    BeEcompErrorManager.getInstance().logInvalidInputError(GET_USER, FAILED_TO_GET_USER, ErrorSeverity.INFO);
                    throw new PortalAPIException(FAILED_TO_GET_USER + getUserResponse.right());
                }
            }
        } catch (Exception e) {
            log.debug(FAILED_TO_GET_USER);
            throw new PortalAPIException(FAILED_TO_GET_USER, e);
        }
    }

    @Override
    public List<EcompUser> getUsers() throws PortalAPIException {
        log.debug("Start handle request of ECOMP getUsers");

        try {
            UserBusinessLogic userBusinessLogic = getUserBusinessLogic();

            Either<List<User>, ResponseFormat> getUsersResponse = userBusinessLogic.getUsersList(JH0003, null, null);

            if (getUsersResponse.isRight()) {
                log.debug(FAILED_TO_GET_USERS);
                BeEcompErrorManager.getInstance().logInvalidInputError(GET_USERS, FAILED_TO_GET_USERS, ErrorSeverity.INFO);
                throw new PortalAPIException(FAILED_TO_GET_USERS + getUsersResponse.right());
            } else {
                if (getUsersResponse.left().value() != null) {
                    List<EcompUser> ecompUserList = new LinkedList<>();
                    for (User user : getUsersResponse.left().value()) {
                        Either<EcompUser, String> ecompUser = EcompUserConverter.convertUserToEcompUser(user);
                        if (ecompUser.isRight()) {
                            log.debug(FAILED_TO_CONVERT_USER2, user);
                            BeEcompErrorManager.getInstance().logInvalidInputError(GET_USERS, "Failed to convert User" + user.toString(), ErrorSeverity.WARNING);
                            continue;
                        } else if (ecompUser.left().value() == null) {
                            log.debug(FAILED_TO_CONVERT_USER2, user);
                            BeEcompErrorManager.getInstance().logInvalidInputError(GET_USERS, "Failed to convert User" + user.toString(), ErrorSeverity.WARNING);
                            continue;
                        }
                        ecompUserList.add(ecompUser.left().value());
                    }
                    return ecompUserList;
                } else {
                    log.debug(FAILED_TO_GET_USERS);
                    BeEcompErrorManager.getInstance().logInvalidInputError(GET_USERS, FAILED_TO_GET_USERS, ErrorSeverity.INFO);
                    throw new PortalAPIException(FAILED_TO_GET_USERS + getUsersResponse.right());
                }
            }
        } catch (Exception e) {
            log.debug(FAILED_TO_GET_USERS);
            BeEcompErrorManager.getInstance().logInvalidInputError(GET_USERS, FAILED_TO_GET_USERS, ErrorSeverity.INFO);
            throw new PortalAPIException(FAILED_TO_GET_USERS, e);
        }
    }

    @Override
    public List<EcompRole> getAvailableRoles(String requestedLoginId) throws PortalAPIException {
        log.debug("Start handle request of ECOMP getAvailableRoles");
        try {
            List<EcompRole> ecompRolesList = new LinkedList<>();
            for (Role role : Role.values()) {
                EcompRole ecompRole = new EcompRole();
                ecompRole.setId(new Long(role.ordinal()));
                ecompRole.setName(role.name());
                ecompRolesList.add(ecompRole);
            }

            if (ecompRolesList.isEmpty()) {
                throw new PortalAPIException();
            }

            return ecompRolesList;
        } catch (Exception e) {
            log.debug(FAILED_TO_FETCH_ROLES);
            BeEcompErrorManager.getInstance().logInvalidInputError("GetAvailableRoles", FAILED_TO_FETCH_ROLES, ErrorSeverity.INFO);
            throw new PortalAPIException("Roles fetching failed", e);
        }

    }

    /**
     * The user role updated through this method only
     */
    @Override
    public void pushUserRole(String loginId, List<EcompRole> roles) throws PortalAPIException {
        log.debug("Start handle request of ECOMP pushUserRole");

        final String modifierAttId = JH0003;
        User modifier = new User();
        modifier.setUserId(modifierAttId);
        log.debug("modifier id is {}", modifierAttId);

        UserBusinessLogic userBusinessLogic = getUserBusinessLogic();

        String updatedRole = null;

        if (roles == null) {
            throw new PortalAPIException("Error: Recieved null for roles");
        } else if (roles.iterator().hasNext()) {
            EcompRole ecompRole = roles.iterator().next();
            updatedRole = EcompRoleConverter.convertEcompRoleToRole(ecompRole);
            log.debug("pushing role: {} to user: {}", updatedRole, loginId);
            Either<User, ResponseFormat> updateUserRoleResponse = userBusinessLogic.updateUserRole(modifier, loginId, updatedRole);
            if (updateUserRoleResponse.isRight()) {
                log.debug("Error: Failed to update role");
                BeEcompErrorManager.getInstance().logInvalidInputError(PUSH_USER_ROLE, "Failed to update role", ErrorSeverity.INFO);
                throw new PortalAPIException("Failed to update role" + updateUserRoleResponse.right().value().toString());
            }
        } else {
            log.debug("Error: No roles in List");
            BeEcompErrorManager.getInstance().logInvalidInputError(PUSH_USER_ROLE, FAILED_TO_FETCH_ROLES, ErrorSeverity.INFO);
            //in this cases we want to deactivate the user
            Either<User, ResponseFormat> deActivateUserResponse = userBusinessLogic.deActivateUser(modifier, loginId);
            if (deActivateUserResponse.isRight()) {
                log.debug("Error: Failed to deactivate user {}",loginId);
                BeEcompErrorManager.getInstance().logInvalidInputError(PUSH_USER_ROLE, "Failed to deactivate user", ErrorSeverity.INFO);
                throw new PortalAPIException(deActivateUserResponse.right().value().getFormattedMessage());
            }
        }
    }

    @Override
    public List<EcompRole> getUserRoles(String loginId) throws PortalAPIException {
        try {
            log.debug("Start handle request of ECOMP getUserRoles");

            UserBusinessLogic userBusinessLogic = getUserBusinessLogic();

            Either<User, ActionStatus> getUserResponse = userBusinessLogic.getUser(loginId, false);

            if (getUserResponse.isRight()) {
                log.debug(ERROR_FAILED_TO_GET_ROLES);
                BeEcompErrorManager.getInstance().logInvalidInputError(GET_USER_ROLES, FAILED_TO_GET_ROLES, ErrorSeverity.INFO);
                throw new PortalAPIException(FAILED_TO_GET_ROLES + getUserResponse.right());
            } else {
                if (getUserResponse.left().value() != null) {
                    Either<EcompUser, String> ecompUser = EcompUserConverter.convertUserToEcompUser(getUserResponse.left().value());
                    if (ecompUser.isRight()) {
                        log.debug("Error: Failed to convert Roles");
                        BeEcompErrorManager.getInstance().logInvalidInputError(GET_USER_ROLES, FAILED_TO_CONVERT_ROLES, ErrorSeverity.ERROR);
                        throw new PortalAPIException(ecompUser.right().value());
                    } else if (ecompUser.left().value() == null) {
                        log.debug("Error: Failed to convert Roles");
                        BeEcompErrorManager.getInstance().logInvalidInputError(GET_USER_ROLES, FAILED_TO_CONVERT_ROLES, ErrorSeverity.ERROR);
                        throw new PortalAPIException();
                    }

                    return new LinkedList<>(ecompUser.left().value().getRoles());
                } else {
                    log.debug(ERROR_FAILED_TO_GET_ROLES);
                    BeEcompErrorManager.getInstance().logInvalidInputError(GET_USER_ROLES, FAILED_TO_GET_ROLES, ErrorSeverity.ERROR);
                    throw new PortalAPIException(FAILED_TO_GET_ROLES + getUserResponse.right());
                }
            }
        } catch (Exception e) {
            log.debug(ERROR_FAILED_TO_GET_ROLES);
            BeEcompErrorManager.getInstance().logInvalidInputError(GET_USER_ROLES, FAILED_TO_GET_ROLES, ErrorSeverity.INFO);
            throw new PortalAPIException(FAILED_TO_GET_ROLES, e);
        }
    }

    @Override
    public boolean isAppAuthenticated(HttpServletRequest request, Map<String,String> appCredentials) throws PortalAPIException {
        final String portal_key = PortalApiProperties.getProperty("portal_pass");
        final String portal_user = PortalApiProperties.getProperty("portal_user");
        final String USERNAME = request.getHeader("username");
        final String PASSWORD = request.getHeader("password");

        if (USERNAME != null && PASSWORD != null) {
            try {
                if (USERNAME.equals(CipherUtil.decryptPKC(portal_user)) &&
                        PASSWORD.equals(CipherUtil.decryptPKC(portal_key))) {
                    log.debug("User authenticated - Username: {}", USERNAME);
                    return true;
                }
            } catch (CipherUtilException e) {
                log.debug("User authentication failed - Decryption failed", e);
                return false;
            }
        }
        log.debug("User authentication failed");
        return false;
    }

    private UserBusinessLogic getUserBusinessLogic() {
        ApplicationContext ctx = ContextLoader.getCurrentWebApplicationContext();
        return (UserBusinessLogic) ctx.getBean("userBusinessLogic");
    }

    /**
     * Gets and returns the userId for the logged-in user based on the request.
     * If any error occurs, the method should throw PortalApiException with an
     * appropriate message. The FW library will catch the exception and send an
     * appropriate response to Portal.
     * 
      * As a guideline for AT&T specific implementation, see the sample apps
     * repository
     * https://codecloud.web.att.com/projects/EP_SDK/repos/ecomp_portal_sdk_third_party/
     * for a sample implementation for on-boarded applications using EPSDK-FW.
     * However, the app can always choose to have a custom implementation of
     * this method. For Open-source implementation, for example, the app will
     * have a totally different implementation for this method.
     * 
      * @param request
     * @return true if the request contains appropriate credentials, else false.
     * @throws PortalAPIException
     *             If an unexpected error occurs while processing the request.
     */
    @Override
    public String getUserId(HttpServletRequest request) throws PortalAPIException {
        return request.getHeader(Constants.USER_ID_HEADER);
    }

    //TODO for what the following method stands for
    @Override
    public Map<String, String> getCredentials() throws PortalAPIException {
        return null;
    }
}
