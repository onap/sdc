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
package org.openecomp.sdc.be.components.validation;

import fj.data.Either;
import org.apache.commons.lang3.StringUtils;
import org.openecomp.sdc.be.components.impl.exceptions.ByActionStatusComponentException;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.user.IUserBusinessLogic;
import org.openecomp.sdc.be.user.Role;
import org.openecomp.sdc.common.log.wrappers.Logger;

import java.util.List;

@org.springframework.stereotype.Component
public class UserValidations {

    private static final Logger log = Logger.getLogger(UserValidations.class);
	private final IUserBusinessLogic userAdmin;
	private final ComponentsUtils componentsUtils;

	public UserValidations(IUserBusinessLogic userAdmin, ComponentsUtils componentsUtils) {
		this.userAdmin = userAdmin;
		this.componentsUtils = componentsUtils;
	}

	public User validateUserExists(String userId, String ecompErrorContext, boolean inTransaction) {
        Either<User, ActionStatus> eitherCreator = userAdmin.getUser(userId, inTransaction);
        if (eitherCreator.isRight() || eitherCreator.left().value() == null) {
            ActionStatus status;
            if (eitherCreator.right().value().equals(ActionStatus.USER_NOT_FOUND)) {
                log.debug("validateUserExists - not authorized user, userId {}", userId);
                status = ActionStatus.AUTH_FAILED;
            } else {
                log.debug("validateUserExists - failed to authorize user, userId {}", userId);
                status = eitherCreator.right().value();
            }
            log.debug("User is not listed. userId {}", userId);
            BeEcompErrorManager.getInstance().logBeUserMissingError(ecompErrorContext, userId);
            throw new ByActionStatusComponentException(status);
        }
        return eitherCreator.left().value();
    }

    public void validateUserRole(User user, List<Role> roles) {
		Role userRole = Role.valueOf(user.getRole());
		if (roles != null) {
			if (!roles.contains(userRole)) {
				log.debug("user is not in appropriate role to perform action");
                throw new ByActionStatusComponentException(ActionStatus.RESTRICTED_OPERATION);
			}
		}
	}

	public Either<User, ActionStatus> validateUserExistsActionStatus(String userId, String ecompErrorContext) {
		Either<User, ActionStatus> eitherCreator = userAdmin.getUser(userId, false);
		if (eitherCreator.isRight() || eitherCreator.left().value() == null) {
			if (eitherCreator.right().value().equals(ActionStatus.USER_NOT_FOUND)) {
				log.debug("validateUserExists - not authorized user, userId {}", userId);
				Either.right(ActionStatus.RESTRICTED_OPERATION);
			} else {
				log.debug("validateUserExists - failed to authorize user, userId {}", userId);
			}
			log.debug("User is not listed. userId {}", userId);
			BeEcompErrorManager.getInstance().logBeUserMissingError(ecompErrorContext, userId);
			return Either.right(eitherCreator.right().value());
		}
		return Either.left(eitherCreator.left().value());
	}

    public User validateUserNotEmpty(User user, String ecompErrorContext) {
		String userId = user.getUserId();
		if (StringUtils.isEmpty(userId)) {
			log.debug("User header is missing ");
			BeEcompErrorManager.getInstance().logBeUserMissingError(ecompErrorContext, user.getUserId());
            throw new ByActionStatusComponentException(ActionStatus.MISSING_INFORMATION);
		}
        return user;
	}

    public User validateUserExists(User user, String ecompErrorContext, boolean inTransaction) {
		return validateUserExists(user.getUserId(), ecompErrorContext, inTransaction);
	}

    public void validateUserExist(String userId, String ecompErrorContext) {
        validateUserExists(userId, ecompErrorContext, false);
	}

}
