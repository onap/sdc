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
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.ecomp.converters.EcompUserConverter;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.user.UserBusinessLogic;
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
    private static final String FAILED_TO_UPDATE_USER_ROLE = "Failed to update user role";
    private static final String FAILED_TO_DEACTIVATE_USER = "Failed to deactivate user {}";
    private static final String FAILED_TO_DEACTIVATE_USER2 = "Failed to deactivate user";
    private static final String FAILED_TO_EDIT_USER = "Failed to edit user";
    private static final String EDIT_USER = "EditUser";
    private static final String CHECK_ROLES = "checkIfSingleRoleProvided";
    private static final String RECEIVED_NULL_FOR_ARGUMENT_LOGIN_ID = "Received null for argument loginId";
    private static final String RECEIVED_NULL_ROLES = "Received null roles for user";
    private static final String RECEIVED_MULTIPLE_ROLES = "Received multiple roles for user {}";
    private static final String RECEIVED_MULTIPLE_ROLES2 = "Received multiple roles for user";
    private static final String NULL_POINTER_RETURNED_FROM_USER_CONVERTER = "NULL pointer returned from user converter";
    private static final String FAILED_TO_CREATE_USER = "Failed to create user {}";
    private static final String FAILED_TO_CONVERT_USER = "Failed to convert user";
    private static final String JH0003 = "jh0003";
    private static final String PUSH_USER = "PushUser";
    private static final String RECEIVED_NULL_FOR_ARGUMENT_USER = "Received null for argument user";
    private static final Logger log = Logger.getLogger(PortalRestAPICentralServiceImpl.class);
    private UserBusinessLogic userBusinessLogic;

    public PortalRestAPICentralServiceImpl() throws PortalAPIException {
        try {
            ApplicationContext ctx = ContextLoader.getCurrentWebApplicationContext();
            userBusinessLogic = (UserBusinessLogic) ctx.getBean("userBusinessLogic");
        } catch (Exception e) {
            log.debug("Failed to get user UserBusinessLogic", e);
            BeEcompErrorManager.getInstance().logInvalidInputError("constructor", "Exception thrown" + e.getMessage(), BeEcompErrorManager.ErrorSeverity.ERROR);
            throw new PortalAPIException("SDC Internal server error");
        }
        log.debug("PortalRestAPICentralServiceImpl Class Instantiated");
    }

    PortalRestAPICentralServiceImpl(UserBusinessLogic ubl) {
        this.userBusinessLogic = ubl;
    }

    @Override
    public Map<String, String> getAppCredentials() throws PortalAPIException {
        Map<String, String> credMap = new HashMap<>();
        String portal_user = PortalApiProperties.getProperty(PortalPropertiesEnum.PORTAL_USER.value);
        String password = PortalApiProperties.getProperty(PortalPropertiesEnum.PORTAL_PASS.value);
        String appName = PortalApiProperties.getProperty(PortalPropertiesEnum.PORTAL_APP_NAME.value);
        try {
            credMap.put(PortalPropertiesEnum.PORTAL_USER.value, CipherUtil.decryptPKC(portal_user));
            credMap.put(PortalPropertiesEnum.PORTAL_PASS.value, CipherUtil.decryptPKC(password));
            credMap.put(PortalPropertiesEnum.PORTAL_APP_NAME.value, CipherUtil.decryptPKC(appName));
        } catch (CipherUtilException e) {
            log.debug("User authentication failed - Decryption failed", e);
            throw new PortalAPIException("Failed to decrypt" + e.getMessage());
        }

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
        User modifier = new User();
        modifier.setUserId(modifierAttId);
        log.debug("modifier id is {}", modifierAttId);

        User convertedAsdcUser = EcompUserConverter.convertEcompUserToUser(user);
        if (convertedAsdcUser == null) {
            BeEcompErrorManager.getInstance().logInvalidInputError(PUSH_USER, FAILED_TO_CONVERT_USER, BeEcompErrorManager.ErrorSeverity.INFO);
            log.debug(FAILED_TO_CREATE_USER, user);
            throw new PortalAPIException("Failed to convert user " + user);
        }

        Either<User, ResponseFormat> createUserResponse = userBusinessLogic.createUser(modifier, convertedAsdcUser);

        // ALREADY EXIST ResponseFormat
        final String ALREADY_EXISTS_RESPONSE_ID = "SVC4006";

        if (createUserResponse.isRight()) {
            if (!createUserResponse.right().value().getMessageId().equals(ALREADY_EXISTS_RESPONSE_ID)) {
                log.debug(FAILED_TO_CREATE_USER, user);
                BeEcompErrorManager.getInstance().logInvalidInputError(PUSH_USER, FAILED_TO_CREATE_USER, BeEcompErrorManager.ErrorSeverity.ERROR);
                throw new PortalAPIException(FAILED_TO_CREATE_USER + createUserResponse.right().value());
            } else {
                log.debug("User already exist and will be updated and reactivated {}", user);
                Either<User, ResponseFormat> updateUserResp = userBusinessLogic.updateUserCredentials(convertedAsdcUser);
                if(updateUserResp.isRight()){
                    log.debug(FAILED_TO_UPDATE_USER_CREDENTIALS, user);
                    BeEcompErrorManager.getInstance().logInvalidInputError(PUSH_USER, FAILED_TO_UPDATE_USER_CREDENTIALS, BeEcompErrorManager.ErrorSeverity.ERROR);
                    throw new PortalAPIException(FAILED_TO_UPDATE_USER_CREDENTIALS + createUserResponse.right().value());
                }
                Either<User, ResponseFormat> updateUserRoleResp = userBusinessLogic.updateUserRole(modifier, convertedAsdcUser.getUserId(), convertedAsdcUser.getRole());
                if(updateUserRoleResp.isRight()){
                    log.debug(FAILED_TO_UPDATE_USER_ROLE, user);
                    BeEcompErrorManager.getInstance().logInvalidInputError(PUSH_USER, FAILED_TO_UPDATE_USER_ROLE, BeEcompErrorManager.ErrorSeverity.ERROR);
                    throw new PortalAPIException(FAILED_TO_UPDATE_USER_ROLE + createUserResponse.right().value());
                }
            }

        }
        log.debug("User created {}", user);
    }

    @Override
    public void editUser(String loginId, EcompUser user) throws PortalAPIException {
        log.debug("Start handle request of ECOMP editUser");

        if (user == null) {
            log.debug(RECEIVED_NULL_FOR_ARGUMENT_USER);
            BeEcompErrorManager.getInstance().logInvalidInputError(EDIT_USER, RECEIVED_NULL_FOR_ARGUMENT_USER, BeEcompErrorManager.ErrorSeverity.INFO);
            throw new PortalAPIException(RECEIVED_NULL_FOR_ARGUMENT_USER);
        } else if (loginId == null) {
            log.debug(RECEIVED_NULL_FOR_ARGUMENT_LOGIN_ID);
            BeEcompErrorManager.getInstance().logInvalidInputError(EDIT_USER, RECEIVED_NULL_FOR_ARGUMENT_LOGIN_ID, BeEcompErrorManager.ErrorSeverity.INFO);
            throw new PortalAPIException(RECEIVED_NULL_FOR_ARGUMENT_LOGIN_ID);
        }

        checkIfSingleRoleProvided(user);

        final String modifierAttId = JH0003;
        User modifier = new User();
        modifier.setUserId(modifierAttId);
        log.debug("modifier id is {}", modifierAttId);

        if (user.getLoginId() != null && !user.getLoginId().equals(loginId)) {
            log.debug("loginId and user loginId not equal");
            BeEcompErrorManager.getInstance().logInvalidInputError(EDIT_USER, "loginId and user loginId not equal", BeEcompErrorManager.ErrorSeverity.INFO);
            throw new PortalAPIException("loginId not equals to the user loginId field");
        } else if (user.getLoginId() == null) {
            user.setLoginId(loginId);
        }

        User asdcUser = EcompUserConverter.convertEcompUserToUser(user);
        if (asdcUser == null) {
            BeEcompErrorManager.getInstance().logInvalidInputError(PUSH_USER, FAILED_TO_CONVERT_USER, BeEcompErrorManager.ErrorSeverity.INFO);
            log.debug(FAILED_TO_CREATE_USER, user);
            throw new PortalAPIException("Failed to convert user " + user);
        }

        Either<User, ResponseFormat> updateUserCredentialsResponse = userBusinessLogic.updateUserCredentials(asdcUser);

        if (updateUserCredentialsResponse.isRight()) {
            log.debug(FAILED_TO_UPDATE_USER_CREDENTIALS);
            BeEcompErrorManager.getInstance().logInvalidInputError(EDIT_USER, FAILED_TO_UPDATE_USER_CREDENTIALS, BeEcompErrorManager.ErrorSeverity.ERROR);
            throw new PortalAPIException(FAILED_TO_EDIT_USER + updateUserCredentialsResponse.right().value());
        }

        Either<User, ResponseFormat> deActivateUser;

        if(asdcUser.getRole() == null || asdcUser.getRole().isEmpty()){
            deActivateUser = userBusinessLogic.deActivateUser(modifier, asdcUser.getUserId());
        } else {
            return;
        }

        if (deActivateUser.isRight()) {
            log.debug(FAILED_TO_DEACTIVATE_USER, asdcUser);
            BeEcompErrorManager.getInstance().logInvalidInputError(EDIT_USER, FAILED_TO_DEACTIVATE_USER2, BeEcompErrorManager.ErrorSeverity.ERROR);
            throw new PortalAPIException(FAILED_TO_DEACTIVATE_USER2 + deActivateUser.right().value());
        }
    }

    @Override
    public String getUserId(HttpServletRequest request) throws PortalAPIException {
        return request.getHeader(Constants.USER_ID_HEADER);
    }


    private void checkIfSingleRoleProvided(EcompUser user) throws PortalAPIException {
        if(user.getRoles() == null) {
            log.debug(RECEIVED_NULL_ROLES, user);
            BeEcompErrorManager.getInstance().logInvalidInputError(CHECK_ROLES, RECEIVED_NULL_ROLES, BeEcompErrorManager.ErrorSeverity.ERROR);
            throw new PortalAPIException(RECEIVED_NULL_ROLES + user);
        }else if(user.getRoles().size() > 1) {
            log.debug(RECEIVED_MULTIPLE_ROLES, user);
            BeEcompErrorManager.getInstance().logInvalidInputError(CHECK_ROLES, RECEIVED_MULTIPLE_ROLES2, BeEcompErrorManager.ErrorSeverity.ERROR);
            throw new PortalAPIException(FAILED_TO_DEACTIVATE_USER2 + user);
        }
    }

    public enum PortalPropertiesEnum{
        PORTAL_PASS ("portal_pass"),
        PORTAL_USER("portal_user"),
        PORTAL_APP_NAME("portal_app_name");

        private final String value;

        PortalPropertiesEnum(String value) {
            this.value = value;
        }

        public String value() {
            return value;
        }
    }
}
