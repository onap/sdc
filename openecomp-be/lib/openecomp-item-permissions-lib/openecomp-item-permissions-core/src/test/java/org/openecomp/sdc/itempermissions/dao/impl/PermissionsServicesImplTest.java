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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.openecomp.sdc.common.session.SessionContextProviderFactory;
import org.openecomp.sdc.itempermissions.PermissionsRules;
import org.openecomp.sdc.itempermissions.dao.ItemPermissionsDao;
import org.openecomp.sdc.itempermissions.dao.UserPermissionsDao;
import org.openecomp.sdc.itempermissions.type.ItemPermissionsEntity;
import org.junit.Test;
import org.junit.Before;
import org.junit.Assert;


/**
 * Created by ayalaben on 7/9/2017
 */
public class PermissionsServicesImplTest {

    private static final String ITEM1_ID = "1";
    private static final String PERMISSION = "Owner";
    private static final String USER2_ID = "testUser2";
    private static final String USER1_ID = "onboarding";
    private static final String ACTION_SUBMIT = "Submit_Item";
    private static final String CHANGE_PERMISSIONS = "Change_Item_Permissions";

    static {
        SessionContextProviderFactory.getInstance().createInterface().create("testUser1", "dox");
    }

    @Mock
    private ItemPermissionsDao permissionsDaoMock;
    @Mock
    private UserPermissionsDao userPermissionsDao;  // do not delete. needed for permissionService
    @Mock
    private PermissionsRules permissionsRulesMock;
    @InjectMocks
    @Spy
    private PermissionsServicesImpl permissionsServices;


    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testListUserPermittedItems() {
        Set<String> userPermissionSet = Collections.emptySet();

        Mockito.when(userPermissionsDao.listUserPermittedItems(anyString(), anyString())).thenReturn(userPermissionSet);

        Set<String> permissionsSet = permissionsServices.listUserPermittedItems(USER1_ID, PERMISSION);

        Assert.assertEquals(0, permissionsSet.size());
    }

    @Test
    public void testExecute() {
        Mockito.doNothing().when(permissionsRulesMock).executeAction(anyString(), anyString(), anyString());

        permissionsServices.execute(ITEM1_ID, USER1_ID, PERMISSION);

        Mockito.verify(permissionsRulesMock, times(1)).executeAction(anyString(), anyString(), anyString());
    }

    @Test
    public void testDeleteItemPermissions() {
        Mockito.doNothing().when(permissionsDaoMock).deleteItemPermissions(anyString());

        permissionsServices.deleteItemPermissions(ITEM1_ID);

        Mockito.verify(permissionsDaoMock, times(1)).deleteItemPermissions(anyString());
    }

    @Test
    public void testGetUserItemPermission() {
        Mockito.when(permissionsDaoMock.getUserItemPermission(anyString(), anyString()))
               .thenReturn(Optional.of(PERMISSION));

        Optional<String> permission = permissionsServices.getUserItemPermission(ITEM1_ID, USER1_ID);

        Assert.assertTrue(permission.isPresent());
        Assert.assertEquals(PERMISSION, permission.get());
    }

    @Test
    public void testListItemPermissionsWhenNone() {
        Collection<ItemPermissionsEntity> permissions = permissionsServices.listItemPermissions(ITEM1_ID);
        Assert.assertEquals( 0, permissions.size());
    }


    @Test
    public void testListItemPermissions() {
        doReturn(Arrays.asList(createPermissionEntity(ITEM1_ID, USER1_ID, PERMISSION),
                createPermissionEntity(ITEM1_ID, USER2_ID, PERMISSION))).when(permissionsDaoMock)
                                                                        .listItemPermissions(any());

        Collection<ItemPermissionsEntity> actual = permissionsServices.listItemPermissions(ITEM1_ID);
        Assert.assertEquals(2, actual.size());
    }


    @Test
    public void testIsAllowed() {
        when(permissionsDaoMock.getUserItemPermission(ITEM1_ID, USER1_ID)).thenReturn(Optional.of(PERMISSION));
        when(permissionsRulesMock.isAllowed(PERMISSION, ACTION_SUBMIT)).thenReturn(true);

        boolean result = permissionsServices.isAllowed(ITEM1_ID, USER1_ID, ACTION_SUBMIT);

        Assert.assertTrue(result);

    }

    @Test
    public void shouldUpdatePermissions() {

        Set<String> addedUsers = new HashSet<>();
        addedUsers.add(USER2_ID);

        permissionsServices.updateItemPermissions(ITEM1_ID, PERMISSION, addedUsers, new HashSet<>());

        verify(permissionsRulesMock).executeAction(ITEM1_ID, USER1_ID, CHANGE_PERMISSIONS);
        verify(permissionsRulesMock).updatePermission(ITEM1_ID, USER1_ID, PERMISSION, addedUsers, new HashSet<>());
        verify(permissionsDaoMock).updateItemPermissions(ITEM1_ID, PERMISSION, addedUsers, new HashSet<>());
    }

    @Test
    public void shouldExecutePermissionRules() {
        permissionsServices.execute(ITEM1_ID, USER1_ID, ACTION_SUBMIT);
        verify(permissionsRulesMock).executeAction(ITEM1_ID, USER1_ID, ACTION_SUBMIT);
    }

    @Test
    public void shouldReturnUserItemPermission() {
        doReturn(Optional.of(PERMISSION)).when(permissionsDaoMock).getUserItemPermission(ITEM1_ID, USER1_ID);
        Optional<String> actual = permissionsServices.getUserItemPermission(ITEM1_ID, USER1_ID);
        Assert.assertTrue(actual.isPresent());
        Assert.assertEquals(PERMISSION, actual.get());
    }

    @Test
    public void shouldDeleteItemPermissions() {
        permissionsServices.deleteItemPermissions(ITEM1_ID);
        verify(permissionsDaoMock).deleteItemPermissions(ITEM1_ID);
    }


    private static ItemPermissionsEntity createPermissionEntity(String itemId, String userId, String permission) {
        ItemPermissionsEntity permissionsEntity = new ItemPermissionsEntity();
        permissionsEntity.setItemId(itemId);
        permissionsEntity.setUserId(userId);
        permissionsEntity.setPermission(permission);
        return permissionsEntity;
    }

}
