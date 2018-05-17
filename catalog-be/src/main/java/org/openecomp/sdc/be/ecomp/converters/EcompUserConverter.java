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

import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import fj.data.Either;
import org.openecomp.portalsdk.core.restful.domain.EcompRole;
import org.openecomp.portalsdk.core.restful.domain.EcompUser;
import org.openecomp.sdc.be.dao.utils.UserStatusEnum;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.user.Role;

import java.util.Iterator;
import java.util.Objects;

public final class EcompUserConverter {

    private EcompUserConverter() {
    }

    public static Either<EcompUser, String> convertUserToEcompUser(User asdcUser) {
        return (Objects.nonNull(asdcUser)) ? Either.left(convertToEcompUser(asdcUser)) : Either.right("User is null");
    }


    public static Either<User, String> convertEcompUserToUser(EcompUser ecompUser) {
        return (Objects.nonNull(ecompUser)) ? Either.left(convertToUser(ecompUser)) : Either.right("EcompUser is null");
    }

    private static User convertToUser(EcompUser ecompUser) {
        User convertedUser = new User();

        convertedUser.setFirstName(ecompUser.getFirstName());
        convertedUser.setLastName(ecompUser.getLastName());
        convertedUser.setUserId((!isLoginIdEmpty(ecompUser) ? ecompUser.getLoginId() : ecompUser.getOrgUserId()));

        convertedUser.setEmail(ecompUser.getEmail());

        if (Objects.nonNull(ecompUser.getRoles())) {
            Iterator<EcompRole> iter = ecompUser.getRoles().iterator();

            if (iter.hasNext()) {
                String updatedRole = EcompRoleConverter.convertEcompRoleToRole(iter.next());
                convertedUser.setRole(updatedRole);
            }
        }

        convertedUser.setStatus((ecompUser.isActive()) ? UserStatusEnum.ACTIVE : UserStatusEnum.INACTIVE);


        return convertedUser;
    }

    private static EcompUser convertToEcompUser(User asdcUser) {
        EcompUser convertedUser = new EcompUser();

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

        EcompRole convertedRole = getEcompRole(asdcUser);

        convertedUser.setRoles(Sets.newHashSet(convertedRole));

        return convertedUser;
    }


    private static boolean isLoginIdEmpty(EcompUser user) {
        return Strings.isNullOrEmpty(user.getLoginId());
    }

    private static EcompRole getEcompRole(User asdcUser) {
        EcompRole convertedRole = new EcompRole();
        for (Role role : Role.values()) {
            if (isRolesNamesEqual(asdcUser, role)) {
                convertedRole.setName(role.name());
                convertedRole.setId((long) role.ordinal());
                break;
            }
        }
        return convertedRole;
    }

    private static boolean isRolesNamesEqual(User asdcUser, Role role) {
        String asdcUserRole = asdcUser.getRole();
        return role.name().equals(asdcUserRole) || role.toString().equals(asdcUserRole);
    }

}
