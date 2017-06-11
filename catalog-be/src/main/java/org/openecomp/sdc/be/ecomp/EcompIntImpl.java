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

import java.util.LinkedList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.openecomp.portalsdk.core.onboarding.crossapi.IPortalRestAPIService;
import org.openecomp.portalsdk.core.onboarding.exception.PortalAPIException;
import org.openecomp.portalsdk.core.restful.domain.EcompRole;
import org.openecomp.portalsdk.core.restful.domain.EcompUser;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.config.BeEcompErrorManager.ErrorSeverity;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.ecomp.converters.EcompRoleConverter;
import org.openecomp.sdc.be.ecomp.converters.EcompUserConverter;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.user.Role;
import org.openecomp.sdc.be.user.UserBusinessLogic;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.exception.ResponseFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.ContextLoader;

import fj.data.Either;

/*
 * PortalAPIException(String message, Throwable cause);
 */
public class EcompIntImpl implements IPortalRestAPIService {
	private static Logger log = LoggerFactory.getLogger(EcompIntImpl.class.getName());

	public EcompIntImpl() {
		log.debug("EcompIntImpl Class Instantiated");
	}

	@Override
	public void pushUser(EcompUser user) throws PortalAPIException {
		log.debug("Start handle request of ECOMP pushUser");
		try {
			if (user == null) {
				BeEcompErrorManager.getInstance().logInvalidInputError("PushUser", "Recieved null for argument user", ErrorSeverity.INFO);
				log.debug("Recieved null for argument user");
				throw new PortalAPIException("Recieved null for argument user");
			}

			UserBusinessLogic userBusinessLogic = getUserBusinessLogic();

			final String modifierAttId = "jh0003";
			User modifier = new User();
			modifier.setUserId(modifierAttId);
			log.debug("modifier id is {}", modifierAttId);

			Either<User, String> newASDCUser = EcompUserConverter.convertEcompUserToUser(user);
			if (newASDCUser.isRight()) {
				BeEcompErrorManager.getInstance().logInvalidInputError("PushUser", "Failed to convert user", ErrorSeverity.INFO);
				log.debug("Failed to create user {}", user.toString());
				throw new PortalAPIException("Failed to create user " + newASDCUser.right().value());
			} else if (newASDCUser.left().value() == null) {
				BeEcompErrorManager.getInstance().logInvalidInputError("PushUser", "NULL pointer returned from user converter", ErrorSeverity.INFO);
				log.debug("Failed to create user {}", user.toString());
				throw new PortalAPIException("Failed to create user " + newASDCUser.right().value());
			}

			User convertedAsdcUser = newASDCUser.left().value();
			Either<User, ResponseFormat> createUserResponse = userBusinessLogic.createUser(modifier, convertedAsdcUser);

			// ALREADY EXIST ResponseFormat
			final String ALREADY_EXISTS_RESPONSE_ID = "SVC4006";

			if (createUserResponse.isRight()) {
				if (!createUserResponse.right().value().getMessageId().equals(ALREADY_EXISTS_RESPONSE_ID)) {
					log.debug("Failed to create user {}", user.toString());
					BeEcompErrorManager.getInstance().logInvalidInputError("PushUser", "Failed to create user", ErrorSeverity.ERROR);
					throw new PortalAPIException("Failed to create user" + createUserResponse.right());
				}
				log.debug("User already exist {}", user.toString());
			}
			log.debug("User created {}", user.toString());
		} catch (Exception e) {
			log.debug("Failed to create user {}", user, e);
			BeEcompErrorManager.getInstance().logInvalidInputError("PushUser", "Failed to create user", ErrorSeverity.ERROR);
			throw new PortalAPIException("Failed to create user", e);
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
				log.debug("Recieved null for argument user");
				BeEcompErrorManager.getInstance().logInvalidInputError("EditUser", "Recieved null for argument user", ErrorSeverity.INFO);
				throw new PortalAPIException("Recieved null for argument user");
			} else if (loginId == null) {
				log.debug("Recieved null for argument loginId");
				BeEcompErrorManager.getInstance().logInvalidInputError("EditUser", "Recieved null for argument loginId", ErrorSeverity.INFO);
				throw new PortalAPIException("Recieved null for argument loginId");
			}

			UserBusinessLogic userBusinessLogic = getUserBusinessLogic();

			if (user.getLoginId() != null && !user.getLoginId().equals(loginId)) {
				log.debug("loginId and user loginId not equal");
				BeEcompErrorManager.getInstance().logInvalidInputError("EditUser", "loginId and user loginId not equal", ErrorSeverity.INFO);
				throw new PortalAPIException("loginId not equals to the user loginId field");
			} else if (user.getLoginId() == null) {
				user.setLoginId(loginId);
			}

			Either<User, String> asdcUser = EcompUserConverter.convertEcompUserToUser(user);
			if (asdcUser.isRight()) {
				log.debug("Failed to convert user");
				BeEcompErrorManager.getInstance().logInvalidInputError("EditUser", "Failed to convert user", ErrorSeverity.INFO);
				throw new PortalAPIException(asdcUser.right().value());
			} else if (asdcUser.left().value() == null) {
				log.debug("NULL pointer returned from user converter");
				BeEcompErrorManager.getInstance().logInvalidInputError("EditUser", "NULL pointer returned from user converter", ErrorSeverity.INFO);
				throw new PortalAPIException("Failed to edit user");
			}

			Either<User, ResponseFormat> updateUserCredentialsResponse = userBusinessLogic.updateUserCredentials(asdcUser.left().value());

			if (updateUserCredentialsResponse.isRight()) {
				log.debug("Failed to updateUserCredentials");
				BeEcompErrorManager.getInstance().logInvalidInputError("EditUser", "Failed to updateUserCredentials", ErrorSeverity.ERROR);
				throw new PortalAPIException("Failed to edit user" + updateUserCredentialsResponse.right().value());
			}
		} catch (Exception e) {
			log.debug("Failed to updateUserCredentials");
			throw new PortalAPIException("Failed to edit user", e);
		}

	}

	@Override
	public EcompUser getUser(String loginId) throws PortalAPIException {
		log.debug("Start handle request of ECOMP getUser");

		try {

			if (loginId == null) {
				log.debug("Recieved null for argument loginId");
				BeEcompErrorManager.getInstance().logInvalidInputError("GetUser", "Recieved null for argument loginId", ErrorSeverity.INFO);
				throw new PortalAPIException("Recieved null for argument loginId");
			}

			UserBusinessLogic userBusinessLogic = getUserBusinessLogic();

			Either<User, ActionStatus> getUserResponse = userBusinessLogic.getUser(loginId, false);

			if (getUserResponse.isRight()) {
				log.debug("Failed to get User");
				BeEcompErrorManager.getInstance().logInvalidInputError("GetUser", "Failed to get User", ErrorSeverity.INFO);
				throw new PortalAPIException("Failed to get User" + getUserResponse.right());
			} else {
				if (getUserResponse.left().value() != null) {
					Either<EcompUser, String> ecompUser = EcompUserConverter.convertUserToEcompUser(getUserResponse.left().value());
					if (ecompUser.isLeft() && ecompUser.left().value() != null) {
						return ecompUser.left().value();
					} else {
						log.debug("Failed to get User");
						BeEcompErrorManager.getInstance().logInvalidInputError("GetUser", "Failed to get User", ErrorSeverity.INFO);
						throw new PortalAPIException(ecompUser.right().value());
					}
				} else {
					log.debug("Failed to get User");
					BeEcompErrorManager.getInstance().logInvalidInputError("GetUser", "Failed to get User", ErrorSeverity.INFO);
					throw new PortalAPIException("Failed to get User" + getUserResponse.right());
				}
			}
		} catch (Exception e) {
			log.debug("Failed to get User");
			throw new PortalAPIException("Failed to get User", e);
		}
	}

	@Override
	public List<EcompUser> getUsers() throws PortalAPIException {
		log.debug("Start handle request of ECOMP getUsers");

		try {
			UserBusinessLogic userBusinessLogic = getUserBusinessLogic();

			final String modifierAttId = "jh0003";

			Either<List<User>, ResponseFormat> getUsersResponse = userBusinessLogic.getUsersList(modifierAttId, null, null);

			if (getUsersResponse.isRight()) {
				log.debug("Failed to get Users");
				BeEcompErrorManager.getInstance().logInvalidInputError("GetUsers", "Failed to get users", ErrorSeverity.INFO);
				throw new PortalAPIException("Failed to get Users" + getUsersResponse.right());
			} else {
				if (getUsersResponse.left().value() != null) {
					List<EcompUser> ecompUserList = new LinkedList<>();
					for (User user : getUsersResponse.left().value()) {
						Either<EcompUser, String> ecompUser = EcompUserConverter.convertUserToEcompUser(user);
						if (ecompUser.isRight()) {
							log.debug("Failed to convert User {}", user);
							BeEcompErrorManager.getInstance().logInvalidInputError("GetUsers", "Failed to convert User" + user.toString(), ErrorSeverity.WARNING);
							continue;
						} else if (ecompUser.left().value() == null) {
							log.debug("Failed to convert User {}", user);
							BeEcompErrorManager.getInstance().logInvalidInputError("GetUsers", "Failed to convert User" + user.toString(), ErrorSeverity.WARNING);
							continue;
						}
						ecompUserList.add(ecompUser.left().value());
					}
					return ecompUserList;
				} else {
					log.debug("Failed to get users");
					BeEcompErrorManager.getInstance().logInvalidInputError("GetUsers", "Failed to get users", ErrorSeverity.INFO);
					throw new PortalAPIException("Failed to get Users" + getUsersResponse.right());
				}
			}
		} catch (Exception e) {
			log.debug("Failed to get users");
			BeEcompErrorManager.getInstance().logInvalidInputError("GetUsers", "Failed to get users", ErrorSeverity.INFO);
			throw new PortalAPIException("Failed to get Users", e);
		}
	}

	@Override
	public List<EcompRole> getAvailableRoles() throws PortalAPIException {
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
			log.debug("Failed to fetch roles");
			BeEcompErrorManager.getInstance().logInvalidInputError("GetAvailableRoles", "Failed to fetch roles", ErrorSeverity.INFO);
			throw new PortalAPIException("Roles fetching failed", e);
		}

	}

	/**
	 * The user role updated through this method only
	 */
	@Override
	public void pushUserRole(String loginId, List<EcompRole> roles) throws PortalAPIException {
		log.debug("Start handle request of ECOMP pushUserRole");

		final String modifierAttId = "jh0003";
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
				BeEcompErrorManager.getInstance().logInvalidInputError("PushUserRole", "Failed to update role", ErrorSeverity.INFO);
				throw new PortalAPIException("Failed to update role" + updateUserRoleResponse.right().value().toString());
			}
		} else {
			log.debug("Error: No roles in List");
			BeEcompErrorManager.getInstance().logInvalidInputError("PushUserRole", "Failed to fetch roles", ErrorSeverity.INFO);
			//throw new PortalAPIException("Error: No roles in List");
			//in this cases we want to deactivate the user
			Either<User, ResponseFormat> deActivateUserResponse = userBusinessLogic.deActivateUser(modifier, loginId);
			if (deActivateUserResponse.isRight()) {
				log.debug("Error: Failed to deactivate user {}",loginId);
				BeEcompErrorManager.getInstance().logInvalidInputError("PushUserRole", "Failed to deactivate user", ErrorSeverity.INFO);
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
				log.debug("Error: Failed to get Roles");
				BeEcompErrorManager.getInstance().logInvalidInputError("GetUserRoles", "Failed to get Roles", ErrorSeverity.INFO);
				throw new PortalAPIException("Failed to get Roles" + getUserResponse.right());
			} else {
				if (getUserResponse.left().value() != null) {
					Either<EcompUser, String> ecompUser = EcompUserConverter.convertUserToEcompUser(getUserResponse.left().value());
					if (ecompUser.isRight()) {
						log.debug("Error: Failed to convert Roles");
						BeEcompErrorManager.getInstance().logInvalidInputError("GetUserRoles", "Failed to convert Roles", ErrorSeverity.ERROR);
						throw new PortalAPIException(ecompUser.right().value());
					} else if (ecompUser.left().value() == null) {
						log.debug("Error: Failed to convert Roles");
						BeEcompErrorManager.getInstance().logInvalidInputError("GetUserRoles", "Failed to convert Roles", ErrorSeverity.ERROR);
						throw new PortalAPIException();
					}

					return new LinkedList<>(ecompUser.left().value().getRoles());
				} else {
					log.debug("Error: Failed to get Roles");
					BeEcompErrorManager.getInstance().logInvalidInputError("GetUserRoles", "Failed to get Roles", ErrorSeverity.ERROR);
					throw new PortalAPIException("Failed to get Roles" + getUserResponse.right());
				}
			}
		} catch (Exception e) {
			log.debug("Error: Failed to get Roles");
			BeEcompErrorManager.getInstance().logInvalidInputError("GetUserRoles", "Failed to get Roles", ErrorSeverity.INFO);
			throw new PortalAPIException("Failed to get Roles", e);
		}
	}

	@Override
	public boolean isAppAuthenticated(HttpServletRequest request) throws PortalAPIException {
		// TODO Validation should be changed completely
		final String USERNAME = request.getHeader("username");
		final String PASSWORD = request.getHeader("password");

		if (USERNAME != null && PASSWORD != null) {
			if (!USERNAME.equals("") && !PASSWORD.equals("")) {
				log.debug("User authenticated - Username: ,Password: {}", USERNAME, PASSWORD);
				return true;
			}
		}

		log.debug("User authentication failed");
		return false;
	}

	private UserBusinessLogic getUserBusinessLogic() {
		ApplicationContext ctx = ContextLoader.getCurrentWebApplicationContext();
		UserBusinessLogic userBusinessLogic = (UserBusinessLogic) ctx.getBean("userBusinessLogic");
		return userBusinessLogic;
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
}
