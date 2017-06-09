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

package org.openecomp.sdc.be.ecomp.converters;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.openecomp.portalsdk.core.restful.domain.EcompRole;
import org.openecomp.portalsdk.core.restful.domain.EcompUser;
import org.openecomp.sdc.be.dao.utils.UserStatusEnum;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.user.Role;

import fj.data.Either;

public final class EcompUserConverter {

	private EcompUserConverter() {
	}

	public static Either<EcompUser, String> convertUserToEcompUser(User asdcUser) {
		EcompUser convertedUser = new EcompUser();

		if (asdcUser == null) {
			return Either.right("User is null");
		}

		convertedUser.setFirstName(asdcUser.getFirstName());
		convertedUser.setLastName(asdcUser.getLastName());
		convertedUser.setLoginId(asdcUser.getUserId());
		convertedUser.setOrgUserId(asdcUser.getUserId());
		convertedUser.setEmail(asdcUser.getEmail());

		if (asdcUser.getStatus().equals(UserStatusEnum.ACTIVE)) {
			convertedUser.setActive(true);
		} else if (asdcUser.getStatus().equals(UserStatusEnum.INACTIVE)) {
			convertedUser.setActive(false);
		}

		EcompRole convertedRole = new EcompRole();
		for (Role role : Role.values()) {
			if (role.name().equals(asdcUser.getRole()) || role.toString().equals(asdcUser.getRole())) {
				convertedRole.setName(role.name());
				convertedRole.setId(new Long(role.ordinal()));
				break;
			}
		}

		Set<EcompRole> convertedRoleSet = new HashSet<>();
		convertedRoleSet.add(convertedRole);
		convertedUser.setRoles(convertedRoleSet);

		return Either.left(convertedUser);
	}

	public static Either<User, String> convertEcompUserToUser(EcompUser ecompUser) {
		User convertedUser = new User();

		if (ecompUser == null) {
			return Either.right("EcompUser is null");
		}

		convertedUser.setFirstName(ecompUser.getFirstName());
		convertedUser.setLastName(ecompUser.getLastName());

		if (!ecompUser.getLoginId().isEmpty()) {
			convertedUser.setUserId(ecompUser.getLoginId());
		} else {
			convertedUser.setUserId(ecompUser.getOrgUserId());
		}

		convertedUser.setEmail(ecompUser.getEmail());

		if (ecompUser.getRoles() != null) {
			Iterator<EcompRole> iter = ecompUser.getRoles().iterator();

			if (iter.hasNext()) {
				String updatedRole = EcompRoleConverter.convertEcompRoleToRole(iter.next());
				convertedUser.setRole(updatedRole);
			}
		}

		if (ecompUser.isActive()) {
			convertedUser.setStatus(UserStatusEnum.ACTIVE);
		} else {
			convertedUser.setStatus(UserStatusEnum.INACTIVE);
		}

		return Either.left(convertedUser);
	}
}
