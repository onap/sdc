package org.openecomp.sdc.itempermissions.dao.impl;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.openecomp.sdc.common.session.SessionContextProviderFactory;
import org.openecomp.sdc.itempermissions.PermissionsRules;
import org.openecomp.sdc.itempermissions.dao.ItemPermissionsDao;
import org.openecomp.sdc.itempermissions.type.ItemPermissionsEntity;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by ayalaben on 7/9/2017
 */
public class PermissionsServicesImplTest {

  private static final String ITEM1_ID = "1";
  private static final String PERMISSION = "Owner";
  private static final String USER2_ID = "testUser2";
  private static final String USER1_ID = "testUser1";
  private static final String ACTION_SUBMIT = "Submit_Item";
  private static final String CHANGE_PERMISSIONS = "Change_Item_Permissions";

  static {
    SessionContextProviderFactory.getInstance().createInterface().create("testUser1");
  }

  @Mock
  private ItemPermissionsDao permissionsDaoMock;
  @Mock
  private PermissionsRules permissionsRules;
  @InjectMocks
  @Spy
  private PermissionsServicesImpl permissionsServices;


  @BeforeMethod
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void testListItemPermissionsWhenNone()  {
    Collection<ItemPermissionsEntity> permissions =
        permissionsServices.listItemPermissions(ITEM1_ID);
    Assert.assertEquals(permissions.size(), 0);
  }


  @Test
  public void testListItemPermissions(){
    doReturn(Arrays.asList(
        createPermissionEntity(ITEM1_ID, USER1_ID, PERMISSION),
        createPermissionEntity(ITEM1_ID, USER2_ID, PERMISSION)))
        .when(permissionsDaoMock).listItemPermissions(anyObject());

    Collection<ItemPermissionsEntity> actual =
        permissionsServices.listItemPermissions(ITEM1_ID);
    Assert.assertEquals(actual.size(), 2);
  }


  @Test
  public void testIsAllowed(){
    when(permissionsDaoMock.getUserItemPermiission(ITEM1_ID,USER1_ID)).thenReturn(PERMISSION);
    when(permissionsRules.isAllowed(PERMISSION,ACTION_SUBMIT)).thenReturn(true);

    Boolean result = permissionsServices.isAllowed(ITEM1_ID,USER1_ID,ACTION_SUBMIT);

    Assert.assertTrue(result);

  }
  @Test
  public void testUpdatePermissions(){

    Set<String> addedUsers = new HashSet<String>();
    addedUsers.add(USER2_ID);

    permissionsServices.updateItemPermissions(ITEM1_ID,PERMISSION,addedUsers,
        new HashSet<String>());

    verify(permissionsRules).executeAction(ITEM1_ID,USER1_ID,CHANGE_PERMISSIONS);
    verify(permissionsRules).updatePermission(ITEM1_ID,USER1_ID,PERMISSION,addedUsers,new HashSet<String>());
    verify(permissionsDaoMock).updateItemPermissions(ITEM1_ID,PERMISSION,addedUsers,new
        HashSet<String>());
  }


  public static ItemPermissionsEntity createPermissionEntity(String itemId, String
      userId, String permission) {
    ItemPermissionsEntity permissionsEntity = new ItemPermissionsEntity();
   permissionsEntity.setItemId(itemId);
   permissionsEntity.setUserId(userId);
   permissionsEntity.setPermission(permission);
    return permissionsEntity;
  }

}