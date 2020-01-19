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

package org.openecomp.sdc.be.ecomp;

import fj.data.Either;
import org.onap.portalsdk.core.onboarding.crossapi.IPortalRestCentralService;
import org.onap.portalsdk.core.onboarding.exception.CipherUtilException;
import org.onap.portalsdk.core.onboarding.exception.PortalAPIException;
import org.onap.portalsdk.core.onboarding.util.CipherUtil;
import org.onap.portalsdk.core.onboarding.util.PortalApiProperties;
import org.onap.portalsdk.core.restful.domain.EcompUser;
import org.openecomp.sdc.be.components.impl.exceptions.ComponentException;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.ecomp.converters.EcompUserConverter;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.user.UserBusinessLogic;
import org.openecomp.sdc.be.user.UserBusinessLogicExt;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.exception.ResponseFormat;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.ContextLoader;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

public final class PortalRestAPICentralServiceImpl implements IPortalRestCentralService {
    private static final String FAILED_TO_UPDATE_USER_CREDENTIALS = "Failed to update user credentials";
    private static final String FAILED_TO_DEACTIVATE_USER = "Failed to deactivate user {}";
    private static final String FAILED_TO_EDIT_USER = "Failed to edit user";
    private static final String EDIT_USER = "EditUser";
    private static final String CHECK_ROLES = "checkIfSingleRoleProvided";
    private static final String RECEIVED_NULL_FOR_ARGUMENT_LOGIN_ID = "Received null for argument loginId";
    private static final String RECEIVED_NULL_ROLES = "Received null roles for user";
    private static final String RECEIVED_MULTIPLE_ROLES = "Received multiple roles for user {}";
    private static final String RECEIVED_MULTIPLE_ROLES2 = "Received multiple roles for user";
    private static final String FAILED_TO_CREATE_USER = "Failed to create user {}";
    private static final String FAILED_TO_GET_USER_ID_HEADER = "Failed to get user_id header";
    private static final String JH0003 = "jh0003";
    private static final String PUSH_USER = "PushUser";
    private static final String RECEIVED_NULL_FOR_ARGUMENT_USER = "Received null for argument user";
    private static final Logger log = Logger.getLogger(PortalRestAPICentralServiceImpl.class);
    private UserBusinessLogic userBusinessLogic;
    private UserBusinessLogicExt userBusinessLogicExt;

    public PortalRestAPICentralServiceImpl() throws PortalAPIException {
        try {
            ApplicationContext ctx = ContextLoader.getCurrentWebApplicationContext();
            userBusinessLogic = (UserBusinessLogic) ctx.getBean("userBusinessLogic");
            userBusinessLogicExt = (UserBusinessLogicExt) ctx.getBean("userBusinessLogicExt");
        } catch (Exception e) {
            log.debug("Failed to get user UserBusinessLogic", e);
            BeEcompErrorManager.getInstance().logInvalidInputError("constructor", "Exception thrown" + e.getMessage(), BeEcompErrorManager.ErrorSeverity.ERROR);
            throw new PortalAPIException("SDC Internal server error");
        }
        log.debug("PortalRestAPICentralServiceImpl Class Instantiated");
    }

    //For testing purposes
    PortalRestAPICentralServiceImpl(UserBusinessLogic ubl, UserBusinessLogicExt uble) {
        this.userBusinessLogic = ubl;
        this.userBusinessLogicExt = uble;
    }

    @Override
    public Map<String, String> getAppCredentials() throws PortalAPIException {
        Map<String, String> credMap = new HashMap<>();
        String portal_user = PortalApiProperties.getProperty(PortalPropertiesEnum.USER.value());
        String password = PortalApiProperties.getProperty(PortalPropertiesEnum.PASSWORD.value());
        String appName = PortalApiProperties.getProperty(PortalPropertiesEnum.APP_NAME.value());
        try {
            credMap.put("username", CipherUtil.decryptPKC(portal_user));
            credMap.put("password", CipherUtil.decryptPKC(password));
            credMap.put("appName", CipherUtil.decryptPKC(appName));
        } catch (CipherUtilException e) {
            log.debug("User authentication failed - Decryption failed", e);
            throw new PortalAPIException("Failed to decrypt" + e.getMessage());
        }
        log.debug("the credentials map for portal is {}", credMap);
        return credMap;
    }

    @Override
    public void pushUser(EcompUser user) throws PortalAPIException {
        log.debug("Start handle request of ECOMP pushUser");

        if (user == null) {
            BeEcompErrorManager.getInstance().logInvalidInputError(PUSH_USER, RECEIVED_NULL_FOR_ARGUMENT_USER, BeEcompErrorManager.ErrorSeverity.INFO);
            log.debug(RECEIVED_NULL_FOR_ARGUMENT_USER);
            throw new PortalAPIException(RECEIVED_NULL_FOR_ARGUMENT_USER);
        }
        checkIfSingleRoleProvided(user);

        final String modifierAttId = JH0003;
        log.debug("modifier id is {}", modifierAttId);

        User convertedAsdcUser = EcompUserConverter.convertEcompUserToUser(user);

        try{
            log.debug("Before creating ecomp user {} sdc user {}", user, convertedAsdcUser);
            userBusinessLogic.createUser(modifierAttId, convertedAsdcUser);
        }catch (Exception e) {
            log.debug(FAILED_TO_CREATE_USER, user, e);
            BeEcompErrorManager.getInstance().logInvalidInputError(PUSH_USER, FAILED_TO_CREATE_USER, BeEcompErrorManager.ErrorSeverity.ERROR);
            throw new PortalAPIException(FAILED_TO_CREATE_USER + e.getMessage());
        }

        log.debug("User created ecomp user {} sdc user {}", user, convertedAsdcUser);
    }

    @Override
    public void editUser(String loginId, EcompUser user) throws PortalAPIException {
        if (user == null) {
            log.debug(RECEIVED_NULL_FOR_ARGUMENT_USER);
            BeEcompErrorManager.getInstance().logInvalidInputError(EDIT_USER, RECEIVED_NULL_FOR_ARGUMENT_USER, BeEcompErrorManager.ErrorSeverity.INFO);
            throw new PortalAPIException(RECEIVED_NULL_FOR_ARGUMENT_USER);
        } else if (loginId == null) {
            log.debug(RECEIVED_NULL_FOR_ARGUMENT_LOGIN_ID);
            BeEcompErrorManager.getInstance().logInvalidInputError(EDIT_USER, RECEIVED_NULL_FOR_ARGUMENT_LOGIN_ID, BeEcompErrorManager.ErrorSeverity.INFO);
            throw new PortalAPIException(RECEIVED_NULL_FOR_ARGUMENT_LOGIN_ID);
        }

        log.debug("Start handle request of ECOMP editUser {} with loginId {} with follopwing roles {}", user, loginId, user.getRoles());

        final String modifierAttId = JH0003;
        log.debug("modifier id is {}", modifierAttId);

        if (user.getLoginId() != null && !user.getLoginId().equals(loginId)) {
            log.debug("loginId and user loginId not equal");
            BeEcompErrorManager.getInstance().logInvalidInputError(EDIT_USER, "loginId and user loginId not equal", BeEcompErrorManager.ErrorSeverity.INFO);
            throw new PortalAPIException("loginId not equals to the user loginId field");
        } else if (user.getLoginId() == null) {
            user.setLoginId(loginId);
        }

        Either<User, ActionStatus> verifyNewUser;
        try{
            verifyNewUser = userBusinessLogic.verifyNewUserForPortal(user.getLoginId());
        } catch (ComponentException e){
            log.debug("Failed to verify new user", e);
            throw new PortalAPIException(e.getCause());
        }

        if(verifyNewUser.isRight() &&
                (ActionStatus.USER_NOT_FOUND.equals(verifyNewUser.right().value()) ||
                        ActionStatus.USER_INACTIVE.equals(verifyNewUser.right().value()))){
            log.debug("Edit user for user that not exist in DB, executing push user flow {}", user);
            pushUser(user);
            return;
        }

        User asdcUser = EcompUserConverter.convertEcompUserToUser(user);
        log.debug("Before editing ecomp user {} sdc user {}", user, asdcUser);
        Either<User, ResponseFormat> updateUserCredentialsResponse = userBusinessLogic.updateUserCredentials(asdcUser);

        if (updateUserCredentialsResponse.isRight()) {
            log.debug(FAILED_TO_UPDATE_USER_CREDENTIALS);
            BeEcompErrorManager.getInstance().logInvalidInputError(EDIT_USER, FAILED_TO_UPDATE_USER_CREDENTIALS, BeEcompErrorManager.ErrorSeverity.ERROR);
            throw new PortalAPIException(FAILED_TO_EDIT_USER + updateUserCredentialsResponse.right().value());
        }

        if(user.getRoles() == null || user.getRoles().isEmpty()){
            try {
                log.debug("Before deactivating ecomp user {} sdc user {}", user, asdcUser);
                userBusinessLogicExt.deActivateUser(modifierAttId, loginId);
            }
            catch (Exception e) {
                log.debug("Error: Failed to deactivate user {}", loginId);
                BeEcompErrorManager.getInstance().logInvalidInputError(FAILED_TO_DEACTIVATE_USER, "Failed to deactivate user", BeEcompErrorManager.ErrorSeverity.INFO);
                throw new PortalAPIException("Error: Failed to deactivate user" + e);
            }
        } else {
            checkIfSingleRoleProvided(user);
            try {
                log.debug("Before updating ecomp user {} sdc user {}", user, asdcUser);
                userBusinessLogic.updateUserRole(modifierAttId, loginId, asdcUser.getRole());
            }catch (Exception e) {
                log.debug("Error: Failed to update user role {}", loginId);
                BeEcompErrorManager.getInstance().logInvalidInputError(FAILED_TO_EDIT_USER, "Failed to update user role", BeEcompErrorManager.ErrorSeverity.INFO);
                throw new PortalAPIException("Error: Failed to update user role" + e);
            }
        }
        log.debug("user updated ecomp user {} sdc user {}", user, asdcUser);
    }

    @Override
    public String getUserId(HttpServletRequest request) throws PortalAPIException {
        String header = request.getHeader(Constants.USER_ID_HEADER);
        if (header == null) {
            log.debug(FAILED_TO_GET_USER_ID_HEADER);
            BeEcompErrorManager.getInstance().logInvalidInputError("getUserId", FAILED_TO_GET_USER_ID_HEADER, BeEcompErrorManager.ErrorSeverity.ERROR);
            throw new PortalAPIException(FAILED_TO_GET_USER_ID_HEADER);
        }
        return header;
    }


    public static void checkIfSingleRoleProvided(EcompUser user) throws PortalAPIException {
        if(user.getRoles() == null) {
            log.debug(RECEIVED_NULL_ROLES, user);
            BeEcompErrorManager.getInstance().logInvalidInputError(CHECK_ROLES, RECEIVED_NULL_ROLES, BeEcompErrorManager.ErrorSeverity.ERROR);
            throw new PortalAPIException(RECEIVED_NULL_ROLES + user);
        }else if(user.getRoles().size() > 1) {
            log.debug(RECEIVED_MULTIPLE_ROLES, user);
            BeEcompErrorManager.getInstance().logInvalidInputError(CHECK_ROLES, RECEIVED_MULTIPLE_ROLES2, BeEcompErrorManager.ErrorSeverity.ERROR);
            throw new PortalAPIException(RECEIVED_MULTIPLE_ROLES2 + user);
        }
    }


}
