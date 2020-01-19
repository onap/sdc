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
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.onap.portalsdk.core.onboarding.exception.PortalAPIException;
import org.onap.portalsdk.core.restful.domain.EcompRole;
import org.onap.portalsdk.core.restful.domain.EcompUser;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.user.UserBusinessLogic;
import org.openecomp.sdc.be.user.UserBusinessLogicExt;
import org.openecomp.sdc.exception.ResponseFormat;

import javax.servlet.http.HttpServletRequest;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PortalRestAPICentralServiceImplTest {

    private PortalRestAPICentralServiceImpl testSubject;
    private UserBusinessLogic ubl;
    private  UserBusinessLogicExt uble;

    @Before
    public void createTestSubject() {
        ubl = Mockito.mock(UserBusinessLogic.class);
        uble = Mockito.mock(UserBusinessLogicExt.class);
        testSubject = new PortalRestAPICentralServiceImpl(ubl, uble);

    }

    @Test
    public void testGetAppCredentials() throws Exception {
        Map<String, String> appCredentials = testSubject.getAppCredentials();
        Assert.assertTrue(appCredentials.get("appName").equals("sdc"));
        Assert.assertTrue(appCredentials.get("username").equals("sdc"));
        Assert.assertTrue(appCredentials.get("password").equals("asdc"));
    }

    @Test
    public void testPushUser() {
        EcompUser user = new EcompUser();
        Set<EcompRole> roleSet = new HashSet<>();
        EcompRole role = new EcompRole();
        role.setId(1L);
        role.setName("Designer");
        roleSet.add(role);
        user.setRoles(roleSet);
        try {
            testSubject.pushUser(user);
        } catch (PortalAPIException e) {
            Assert.fail();
        }
    }

    @Test
    public void testPushUserUBLError() {
        Mockito.when(ubl.createUser(Mockito.anyString(), Mockito.any(User.class))).thenThrow(RuntimeException.class);
        EcompUser user = new EcompUser();
        Set<EcompRole> roleSet = new HashSet<>();
        EcompRole role = new EcompRole();
        role.setId(1L);
        role.setName("Designer");
        roleSet.add(role);
        user.setRoles(roleSet);
        try {
            testSubject.pushUser(user);
        } catch (PortalAPIException e) {
            Assert.assertTrue(e.getMessage().startsWith("Failed to create user"));
            return;
        }
        Assert.fail();
    }

    @Test
    public void testPushUserMultipleRoles() {
        EcompUser user = new EcompUser();
        Set<EcompRole> roleSet = new HashSet<>();
        EcompRole role = new EcompRole();
        role.setId(1L);
        role.setName("Designer");
        roleSet.add(role);
        EcompRole role1 = new EcompRole();
        role.setId(2L);
        roleSet.add(role1);
        role.setName("Tester");
        user.setRoles(roleSet);
        try {
            testSubject.pushUser(user);
        } catch (PortalAPIException e) {
            Assert.assertTrue(e.getMessage().startsWith("Received multiple roles for user"));
            return;
        }
        Assert.fail();
    }

    @Test
    public void testPushUserNullRoles() {
        EcompUser user = new EcompUser();
        try{
            testSubject.pushUser(user);
        } catch (PortalAPIException e){
            Assert.assertTrue(e.getMessage().equals("Received null roles for user" + user));
            return;
        }
        Assert.fail();
    }

    @Test
    public void testPushUserUserNull() {
        try {
            testSubject.pushUser(null);
        } catch (PortalAPIException e) {
            Assert.assertTrue(e.getMessage().equals("Received null for argument user"));
            return;
        }
        Assert.fail();
    }

    @Test
    public void testEditUserUserNull() {
        try {
            testSubject.editUser(null, null);
        } catch (PortalAPIException e) {
            Assert.assertTrue(e.getMessage().equals("Received null for argument user"));
            return;
        }
        Assert.fail();
    }

    @Test
    public void testEditUserIdNull() {
        try {
            testSubject.editUser(null, new EcompUser());
        } catch (PortalAPIException e) {
            Assert.assertTrue(e.getMessage().equals("Received null for argument loginId"));
            return;
        }
        Assert.fail();
    }

    @Test
    public void testEditUserFailOnUpdate() {
        Mockito.when(ubl.updateUserCredentials(Mockito.any(User.class))).thenReturn(Either.right(new ResponseFormat()));
        Mockito.when(ubl.verifyNewUserForPortal(Mockito.anyString())).thenReturn(Either.left(new User()));
        EcompUser user = new EcompUser();
        Set<EcompRole> roleSet = new HashSet<>();
        EcompRole role = new EcompRole();
        role.setId(1L);
        role.setName("Designer");
        roleSet.add(role);
        user.setRoles(roleSet);
        try {
            testSubject.editUser("mock_id", user);
        } catch (PortalAPIException e) {
            Assert.assertTrue(e.getMessage().startsWith("Failed to edit user"));
        }
    }

    @Test
    public void testEditUserDeactivate() {
        Mockito.when(ubl.updateUserCredentials(Mockito.any(User.class))).thenReturn(Either.left(new User()));
        Mockito.when(ubl.verifyNewUserForPortal(Mockito.anyString())).thenReturn(Either.left(new User()));
        EcompUser user = new EcompUser();
        try {
            testSubject.editUser("mock_id", user);
        } catch (PortalAPIException e) {
            Assert.fail();
        }
    }

    @Test
    public void testEditUserFailUpdate() {
        Mockito.when(ubl.updateUserCredentials(Mockito.any(User.class))).thenReturn(Either.left(new User()));
        Mockito.when(ubl.updateUserRole(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenThrow(new RuntimeException());
        Mockito.when(ubl.verifyNewUserForPortal(Mockito.anyString())).thenReturn(Either.left(new User()));
        EcompUser user = new EcompUser();
        Set<EcompRole> roleSet = new HashSet<>();
        EcompRole role = new EcompRole();
        role.setId(1L);
        role.setName("Designer");
        roleSet.add(role);
        user.setRoles(roleSet);
        try {
            testSubject.editUser("mock_id", user);
        } catch (PortalAPIException e) {
            Assert.assertTrue(e.getMessage().startsWith("Error: Failed to update user role"));
            return;
        }
        Assert.fail();
    }

    @Test
    public void testEditUserFailDeactivate() {
        Mockito.when(ubl.updateUserCredentials(Mockito.any(User.class))).thenReturn(Either.left(new User()));
        Mockito.when(uble.deActivateUser(Mockito.anyString(), Mockito.anyString())).thenThrow(new RuntimeException());
        Mockito.when(ubl.verifyNewUserForPortal(Mockito.anyString())).thenReturn(Either.left(new User()));
        EcompUser user = new EcompUser();
        try {
            testSubject.editUser("mock_id", user);
        } catch (PortalAPIException e) {
            Assert.assertTrue(e.getMessage().startsWith("Error: Failed to deactivate user"));
            return;
        }
        Assert.fail();
    }

    @Test
    public void testEditUser() {
        Mockito.when(ubl.updateUserCredentials(Mockito.any(User.class))).thenReturn(Either.left(new User()));
        Mockito.when(ubl.verifyNewUserForPortal(Mockito.anyString())).thenReturn(Either.left(new User()));
        EcompUser user = new EcompUser();
        Set<EcompRole> roleSet = new HashSet<>();
        EcompRole role = new EcompRole();
        role.setId(1L);
        role.setName("Designer");
        roleSet.add(role);
        user.setRoles(roleSet);
        try {
            testSubject.editUser("mock_id", user);
        } catch (PortalAPIException e) {
            Assert.fail();
        }
    }

    @Test
    public void testGetUserId() {
        HttpServletRequest httpServletRequestMock = Mockito.mock(HttpServletRequest.class);
        String mockHeader = "MockHeader";
        Mockito.when(httpServletRequestMock.getHeader(Mockito.anyString())).thenReturn(mockHeader);
        try {
            String userId = testSubject.getUserId(httpServletRequestMock);
            Assert.assertTrue(userId.equals(mockHeader));
        } catch (PortalAPIException e){
            Assert.fail();
        }

    }

    @Test
    public void testGetUserIdException() {
        HttpServletRequest httpServletRequestMock = Mockito.mock(HttpServletRequest.class);
        try {
            testSubject.getUserId(httpServletRequestMock);
        } catch (PortalAPIException e){
            Assert.assertTrue(e.getMessage().equals("Failed to get user_id header"));
            return;
        }
        Assert.fail();
    }
}
