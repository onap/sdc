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

package org.openecomp.sdc.be.auditing.impl;

import org.junit.Test;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.common.api.Constants;

import static org.junit.Assert.assertEquals;
import static org.openecomp.sdc.be.auditing.impl.AuditTestUtils.DESIGNER_USER_ROLE;
import static org.openecomp.sdc.be.auditing.impl.AuditTestUtils.USER_EMAIL;
import static org.openecomp.sdc.be.auditing.impl.AuditTestUtils.USER_EXTENDED_NAME;
import static org.openecomp.sdc.be.auditing.impl.AuditTestUtils.USER_FIRST_NAME;
import static org.openecomp.sdc.be.auditing.impl.AuditTestUtils.USER_ID;
import static org.openecomp.sdc.be.auditing.impl.AuditTestUtils.USER_LAST_NAME;
import static org.openecomp.sdc.be.auditing.impl.AuditTestUtils.USER_UID;

public class AuditBaseEventFactoryTest {

    private User user = new User();

    @Test
    public void buildUserNameWhenFullNameAndUserIdNotSet() {
        assertEquals(Constants.EMPTY_STRING, AuditBaseEventFactory.buildUserName(user));
    }

    @Test
    public void buildUserNameWhenFullNameIsNotSetAndUserIdIsSet() {
        user.setUserId(USER_ID);
        assertEquals("(" + USER_ID + ")", AuditBaseEventFactory.buildUserName(user));
    }

    @Test
    public void buildUserNameWhenFullNameIsNullNullAndUserIdSet() {
        user.setUserId(USER_ID);
        user.setFirstName(Constants.NULL_STRING);
        user.setLastName(Constants.NULL_STRING);
        assertEquals("(" + USER_ID + ")", AuditBaseEventFactory.buildUserName(user));
    }

    @Test
    public void buildUserNameWhenFirtsNameIsNotSetAndLastNameIsNull() {
        user.setUserId(USER_ID);
        user.setLastName(Constants.NULL_STRING);
        assertEquals("(" + USER_ID + ")", AuditBaseEventFactory.buildUserName(user));
    }

    @Test
    public void buildUserNameWhenLastNameIsNull() {
        user.setUserId(USER_ID);
        user.setFirstName(USER_FIRST_NAME);
        user.setLastName(Constants.NULL_STRING);
        assertEquals(USER_FIRST_NAME + " (" + USER_ID + ")", AuditBaseEventFactory.buildUserName(user));
    }

    @Test
    public void buildUserNameWhenFirstNameIsNullAndLastNameIsSet() {
        user.setUserId(USER_ID);
        user.setFirstName(Constants.NULL_STRING);
        user.setLastName(USER_LAST_NAME);
        assertEquals(USER_LAST_NAME + "(" + USER_ID + ")", AuditBaseEventFactory.buildUserName(user));
    }

    @Test
    public void buildUserNameWhenFullNameIsSetAndUserIdSet() {
        user.setUserId(USER_ID);
        user.setFirstName(USER_FIRST_NAME);
        user.setLastName(USER_LAST_NAME);
        assertEquals(USER_UID, AuditBaseEventFactory.buildUserName(user));
    }

    @Test
    public void buildUserNameWhenFirstNameIsSetAndUserIdSet() {
        user.setUserId(USER_ID);
        user.setFirstName(USER_FIRST_NAME);
        assertEquals(USER_FIRST_NAME + " (" + USER_ID + ")", AuditBaseEventFactory.buildUserName(user));
    }

    @Test
    public void buildExtendedUserNameWhenNothingIsSet() {
        assertEquals(Constants.EMPTY_STRING, AuditBaseEventFactory.buildUserNameExtended(user));
    }

    @Test
    public void buildExtendedUserNameWhenOnlyUserIdIsSet() {
        user.setUserId(USER_ID);
        assertEquals(USER_ID, AuditBaseEventFactory.buildUserNameExtended(user));
    }

    @Test
    public void buildExtendedUserNameAllSet() {
        user.setUserId(USER_ID);
        user.setFirstName(USER_FIRST_NAME);
        user.setLastName(USER_LAST_NAME);
        user.setEmail(USER_EMAIL);
        user.setRole(DESIGNER_USER_ROLE);
        assertEquals(USER_EXTENDED_NAME, AuditBaseEventFactory.buildUserNameExtended(user));
    }

    @Test
    public void buildExtendedUserNameWhenFirstNameAndUserIdAreSet() {
        user.setUserId(USER_ID);
        user.setFirstName(USER_FIRST_NAME);
        assertEquals(USER_ID + ", " + USER_FIRST_NAME, AuditBaseEventFactory.buildUserNameExtended(user));
    }

    @Test
    public void buildExtendedUserNameWhenLastNameAndUserIdAreSet() {
        user.setUserId(USER_ID);
        user.setLastName(USER_LAST_NAME);
        assertEquals(USER_ID + ", " + USER_LAST_NAME, AuditBaseEventFactory.buildUserNameExtended(user));
    }

    @Test
    public void buildExtendedUserNameWhenOnlyEmailAndRoleAreSet() {
        user.setEmail(USER_EMAIL);
        user.setRole(DESIGNER_USER_ROLE);
        assertEquals(USER_EMAIL + ", " + DESIGNER_USER_ROLE, AuditBaseEventFactory.buildUserNameExtended(user));
    }

    @Test
    public void buildExtendedUserNameWhenOnlyNameIsSet() {
        user.setFirstName(USER_FIRST_NAME);
        user.setLastName(USER_LAST_NAME);
        assertEquals(USER_FIRST_NAME + " " + USER_LAST_NAME, AuditBaseEventFactory.buildUserNameExtended(user));
    }

    @Test
    public void buildExtendedUserNameWhenOnlyRoleIsSet() {
        user.setRole(DESIGNER_USER_ROLE);
        assertEquals(DESIGNER_USER_ROLE, AuditBaseEventFactory.buildUserNameExtended(user));
    }

}
