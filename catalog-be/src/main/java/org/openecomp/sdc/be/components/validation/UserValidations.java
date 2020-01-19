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

import org.apache.commons.lang3.StringUtils;
import org.openecomp.sdc.be.components.impl.exceptions.ByActionStatusComponentException;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.utils.UserStatusEnum;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.user.Role;
import org.openecomp.sdc.be.user.UserBusinessLogic;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.openecomp.sdc.be.dao.api.ActionStatus.USER_INACTIVE;

@Component
public class UserValidations {

    private static final Logger log = Logger.getLogger(UserValidations.class);
	private final UserBusinessLogic userAdmin;

    public UserValidations(UserBusinessLogic userAdmin) {
		this.userAdmin = userAdmin;
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

	public ActionStatus validateUserExistsActionStatus(String userId) {
	    if (!userAdmin.hasActiveUser(userId)) {
			return ActionStatus.RESTRICTED_OPERATION;
		}
		return ActionStatus.OK;
	}

    public User validateUserNotEmpty(User user, String ecompErrorContext) {
		String userId = user.getUserId();
		if (StringUtils.isEmpty(userId)) {
			log.debug("User header is missing ");
			BeEcompErrorManager.getInstance().logBeUserMissingError(ecompErrorContext, user.getUserId());
            throw new ByActionStatusComponentException(ActionStatus.MISSING_USER_ID);
		}
        return user;
	}

	public User validateUserExists(String userId) {
		User user = userAdmin.getUser(userId);
		if (UserStatusEnum.INACTIVE == user.getStatus()) {
			throw new ByActionStatusComponentException(USER_INACTIVE, userId);
		}
		return user;
	}

	public User validateUserExists(User user) {
		return validateUserExists(user.getUserId());
	}


}
