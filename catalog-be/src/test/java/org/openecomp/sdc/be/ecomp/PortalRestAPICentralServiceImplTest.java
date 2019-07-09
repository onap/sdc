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
import org.openecomp.sdc.exception.ResponseFormat;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PortalRestAPICentralServiceImplTest {

    PortalRestAPICentralServiceImpl testSubject;
    UserBusinessLogic ubl;

    @Before
    public void setUp() throws Exception {
        ubl = Mockito.mock(UserBusinessLogic.class);
        testSubject = new PortalRestAPICentralServiceImpl(ubl);
    }

    @Test
    public void testGetAppCredentials() throws Exception {
        Map<String, String> appCredentials = testSubject.getAppCredentials();
        Assert.assertTrue(appCredentials.get(PortalRestAPICentralServiceImpl.PortalPropertiesEnum.PORTAL_APP_NAME.value()).equals("sdc"));
        Assert.assertTrue(appCredentials.get(PortalRestAPICentralServiceImpl.PortalPropertiesEnum.PORTAL_USER.value()).equals("sdc"));
        Assert.assertTrue(appCredentials.get(PortalRestAPICentralServiceImpl.PortalPropertiesEnum.PORTAL_PASS.value()).equals("asdc"));
    }

    @Test
    public void testPushUserGeneralError() throws Exception {
        ResponseFormat responseFormat = Mockito.mock(ResponseFormat.class);
        Mockito.when(responseFormat.getMessageId()).thenReturn("mock");
        Mockito.when(ubl.createUser(Mockito.any(), Mockito.any())).thenReturn(Either.right(responseFormat));
        EcompUser user = new EcompUser();
        Set<EcompRole> roleSet = new HashSet<>();
        EcompRole role = new EcompRole();
        role.setId(1L);
        role.setName("Designer");
        roleSet.add(role);
        user.setRoles(roleSet);
        try{
            testSubject.pushUser(user);
        }catch (PortalAPIException e) {
            System.out.println(e);
            Assert.assertTrue(e.getMessage().startsWith("Failed to create user {}"));
        }

    }

    @Test
    public void testPushUserSuccess() throws Exception {
        ResponseFormat responseFormat = Mockito.mock(ResponseFormat.class);
        Mockito.when(responseFormat.getMessageId()).thenReturn("SVC4006");
        Mockito.when(ubl.createUser(Mockito.any(), Mockito.any())).thenReturn(Either.left(new User()));
        EcompUser user = new EcompUser();
        Set<EcompRole> roleSet = new HashSet<>();
        EcompRole role = new EcompRole();
        role.setId(1L);
        role.setName("Designer");
        roleSet.add(role);
        user.setRoles(roleSet);
        testSubject.pushUser(user);
    }

    @Test
    public void testPushUserNullRoles() throws Exception {
        EcompUser user = new EcompUser();
        try{
            testSubject.pushUser(user);
        } catch (PortalAPIException e){
            Assert.assertTrue(e.getMessage().equals("Received null roles for user" + user));
        }

    }

    @Test
    public void testPushUserUserNull() throws Exception {
        try {
            testSubject.pushUser(null);
        } catch (PortalAPIException e) {
            Assert.assertTrue(e.getMessage().equals("Received null for argument user"));
        }

    }

    /**
    *
    * Method: editUser(String loginId, EcompUser user)
    *
    */
    @Test
    public void testEditUser() throws Exception {
    //TODO: Test goes here...
    }

    /**
    *
    * Method: getUserId(HttpServletRequest request)
    *
    */
    @Test
    public void testGetUserId() throws Exception {
    //TODO: Test goes here...
    }

    /**
    *
    * Method: value()
    *
    */
    @Test
    public void testValue() throws Exception {
    //TODO: Test goes here...
    }

}
