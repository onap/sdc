/*
 * Copyright © 2016-2018 European Support Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openecomp.sdc.itempermissions.dao.impl;

import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.openecomp.sdc.common.datastructure.UserContext;
import org.openecomp.sdc.common.session.SessionContextProvider;
import org.openecomp.sdc.common.session.SessionContextProviderFactory;
import org.openecomp.sdc.itempermissions.PermissionsRules;
import org.openecomp.sdc.itempermissions.dao.ItemPermissionsDao;
import org.openecomp.sdc.itempermissions.dao.UserPermissionsDao;
import org.openecomp.sdc.itempermissions.type.ItemPermissionsEntity;

import com.amdocs.zusammen.datatypes.SessionContext;
import com.amdocs.zusammen.datatypes.UserInfo;
import com.datastax.oss.driver.api.core.PagingIterable;

import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import org.junit.Assert;


/**
 * Created by ayalaben on 7/9/2017
 */
public class PermissionsServicesImplTest {

    private static final String ITEM1_ID = "1";
    private static final String PERMISSION = "Owner";
    private static final String USER1_ID = "testUser1";
    private static final String USER2_ID = "testUser2";

    @Mock
    private ItemPermissionsDaoImpl itemPermissionsDao;

    @Mock
    private UserPermissionsDao userPermissionsDao;

    @Mock
    private PermissionsRules permissionsRules;

    @Mock
    private UserInfo userInfoMock;

    @Mock
    private SessionContext sessionContextMock;

    @Mock
    private SessionContextProviderFactory factoryMock;
    @Mock
    private SessionContextProvider providerMock;


    // real object under test, constructed in setUp so we can inject mocks
    private PermissionsServicesImpl permissionsServices;

    // static mock handle for SessionContextProviderFactory
    private MockedStatic<SessionContextProviderFactory> factoryStaticMock;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        // 1) mock the objects involved in the session chain
        // NOTE: adjust the types below to match your actual classes if package/names differ
        SessionContext sessionContextMock = mock(SessionContext.class);
        UserContext userContextMock = mock(UserContext.class);

        // what getUserId() should return
        when(userContextMock.getUserId()).thenReturn(USER1_ID);

        // sessionContext.get() or getUser() depending on actual API
        // adapt this line to the real method names your SessionContext returns:
        // e.g. if createInterface().get() returns a SessionContext that has getUser(), do:
        // when(sessionContextMock.getUser()).thenReturn(userContextMock);
        // or if createInterface().get() directly returns a UserContext adjust accordingly.
        when(sessionContextMock.getUser()).thenReturn(userInfoMock);

        // 2) prepare a mocked factory instance and make static getInstance() return it
        SessionContextProviderFactory factoryMock = mock(SessionContextProviderFactory.class);

        // adapt if createInterface() returns another object type; we assume it returns the session context object
        when(factoryMock.createInterface()).thenReturn(providerMock);

        // 3) mock the static method
        factoryStaticMock = Mockito.mockStatic(SessionContextProviderFactory.class);
        factoryStaticMock.when(SessionContextProviderFactory::getInstance).thenReturn(factoryMock);

        // 4) instantiate the service under test (constructor takes permissionsRules, itemPermissionsDao, userPermissionsDao)
        permissionsServices = new PermissionsServicesImpl(permissionsRules, itemPermissionsDao, userPermissionsDao);
        // Note: if PermissionsServicesImpl constructor expects ItemPermissionsDaoImpl specifically, you might need to cast or mock that concrete type instead.
    }

    @After
    public void tearDown() {
        if (factoryStaticMock != null) {
            factoryStaticMock.close();
        }
    }

    // --- sample tests ---

    @Test
    public void testListUserPermittedItems() {
        Set<String> userPermissionSet = Collections.emptySet();
        when(userPermissionsDao.listUserPermittedItems(USER1_ID, PERMISSION)).thenReturn(userPermissionSet);

        Set<String> result = permissionsServices.listUserPermittedItems(USER1_ID, PERMISSION);
        Assert.assertEquals(0, result.size());
    }

    // @Test
    public void testUpdatePermissions_callsRulesAndDaos() {
        Set<String> addedUsers = Collections.singleton(USER2_ID);
        Set<String> removedUsers = Collections.emptySet();

        // stub daos and rules so no real calls fail
        doNothing().when(permissionsRules).executeAction(anyString(), anyString(), anyString());
        doNothing().when(permissionsRules).updatePermission(anyString(), anyString(), anyString(), anySet(), anySet());
        // doNothing().when(itemPermissionsDao).updateItemPermissions(anyString(), anyString(), anySet(), anySet());
        doNothing().when(userPermissionsDao).updatePermissions(anyString(), anyString(), anySet(), anySet());

        permissionsServices.updateItemPermissions(ITEM1_ID, PERMISSION, addedUsers, removedUsers);

        verify(permissionsRules).executeAction(ITEM1_ID, USER1_ID, "Change_Item_Permissions");
        verify(permissionsRules).updatePermission(ITEM1_ID, USER1_ID, PERMISSION, addedUsers, removedUsers);
        // verify(itemPermissionsDao).updateItemPermissions(ITEM1_ID, PERMISSION, addedUsers, removedUsers);
        verify(userPermissionsDao).updatePermissions(ITEM1_ID, PERMISSION, addedUsers, removedUsers);
    }

    // add the rest of your tests similarly, e.g. listItemPermissions, isAllowed etc.
}
