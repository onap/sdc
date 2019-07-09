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

package org.openecomp.sdc.be.ecomp.converters;

import fj.data.Either;
import org.junit.Assert;
import org.junit.Test;
import org.onap.portalsdk.core.restful.domain.EcompRole;
import org.onap.portalsdk.core.restful.domain.EcompUser;
import org.openecomp.sdc.be.model.User;

import java.util.Collections;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
public class EcompUserConverterTest {
    private static final String FIRST_NAME = "firstname";
    private static final String LAST_NAME = "lastname";
    private static final String USER_ID = "sample id";
    private static final String EMAIL_ADDRESS = "sample@sample.com";
    private static final String ROLE = "ADMIN";

    private static final Long LAST_LOGIN_TIME = 12345L;
    private static final User TEST_USER = new User(FIRST_NAME, LAST_NAME, USER_ID, EMAIL_ADDRESS, ROLE, LAST_LOGIN_TIME);


    @Test
    public void shouldProperlyConvertEcompUserToUser() {
        Either<EcompUser, String> convertedUser = EcompUserConverter.convertUserToEcompUser(TEST_USER);

        assertThat(convertedUser.isLeft()).isTrue();

        EcompUser user = convertedUser.left().value();

        assertThat(EMAIL_ADDRESS).isEqualTo(user.getEmail());
        assertThat(FIRST_NAME).isEqualTo(user.getFirstName());
        assertThat(LAST_NAME).isEqualTo(user.getLastName());
        assertThat(USER_ID).isEqualTo(user.getLoginId());
        assertThat(user.getRoles().stream().anyMatch((x) -> ROLE.equals(x.getName()))).isTrue();
    }

    @Test
    public void shouldNotCrashWhenUserIsNotProvided() {
        Either<EcompUser, String> convertedUser = EcompUserConverter.convertUserToEcompUser(null);

        assertThat(convertedUser.isRight()).isTrue();
    }

    @Test
    public void shouldNotCrashWhenEcompUserIsNotProvided() {
        User convertedUser = EcompUserConverter.convertEcompUserToUser(null);

        Assert.assertTrue(convertedUser != null);
    }

    @Test
    public void shouldProperlyConvertUserToEcompUser() {
        User convertedUser = EcompUserConverter.convertEcompUserToUser(createEcompUser());

        assertThat(EMAIL_ADDRESS).isEqualTo(convertedUser.getEmail());
        assertThat(FIRST_NAME).isEqualTo(convertedUser.getFirstName());
        assertThat(LAST_NAME).isEqualTo(convertedUser.getLastName());
        assertThat(USER_ID).isEqualTo(convertedUser.getUserId());
        assertThat(ROLE).isEqualTo(convertedUser.getRole());
    }

    private EcompUser createEcompUser() {
        EcompUser user = new EcompUser();

        EcompRole role = new EcompRole();

        role.setName(ROLE);
        role.setId(0L);

        user.setRoles(Collections.singleton(role));
        user.setEmail(EMAIL_ADDRESS);
        user.setLastName(LAST_NAME);
        user.setFirstName(FIRST_NAME);
        user.setOrgUserId(USER_ID);
        return user;
    }
}
