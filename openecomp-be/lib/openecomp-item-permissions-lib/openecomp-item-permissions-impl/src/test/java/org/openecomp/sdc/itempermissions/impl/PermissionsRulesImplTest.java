package org.openecomp.sdc.itempermissions.impl;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.itempermissions.dao.impl.PermissionsServicesImpl;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.HashSet;


/**
 * Created by ayalaben on 7/10/2017
 */
public class PermissionsRulesImplTest {

  private static final String ITEM1_ID = "1";
  private static final String USER1_ID = "testUser1";
  private static final String PERMISSION_OWNER = "Owner";
  private static final String PERMISSION_CONTRIBUTOR = "Contributor";
  private static final String INVALID_PERMISSION = "Invalid_Permission";
  private static final String SUBMIT_ACTION = "Submit_Item";
  private static final String EDIT_ACTION = "Edit_Item";
  private static final String CHANGE_PERMISSIONS_ACTION = "Change_Item_Permissions";
  private static final String INVALID_ACTION = "Invalid_Action";

  @Mock
  private PermissionsServicesImpl permissionsServices;

  @InjectMocks
  @Spy
  private PermissionsRulesImpl permissionsRules;


  @BeforeMethod
  public void setUp() throws Exception {

    MockitoAnnotations.initMocks(this);
  }

  @Test(expectedExceptions = CoreException.class,expectedExceptionsMessageRegExp =
      "Invalid permission type")
  public void testIsAllowedWhenInvalidPermission() {
      permissionsRules.isAllowed(INVALID_PERMISSION, EDIT_ACTION);
    }

  @Test(expectedExceptions = CoreException.class,expectedExceptionsMessageRegExp =
      "Invalid action type")
  public void testIsAllowedWhenInvalidAction() {
    permissionsRules.isAllowed(PERMISSION_CONTRIBUTOR, INVALID_ACTION);
  }

  @Test
  public void testIsAllowedCaseSubmitOwner(){
    Assert.assertTrue(permissionsRules.isAllowed(PERMISSION_OWNER,SUBMIT_ACTION));
  }

  @Test
  public void testIsAllowedCaseSubmitNotOwner(){
    Assert.assertFalse(permissionsRules.isAllowed(PERMISSION_CONTRIBUTOR,SUBMIT_ACTION));
  }

  @Test
  public void testIsAllowedCaseEditOwner(){
    Assert.assertTrue(permissionsRules.isAllowed(PERMISSION_OWNER,EDIT_ACTION));
  }

  @Test
  public void testIsAllowedCaseEditContributer(){
    Assert.assertTrue(permissionsRules.isAllowed(PERMISSION_CONTRIBUTOR,EDIT_ACTION));
  }

  @Test
  public void testIsAllowedCaseChangePermissionsContributer(){
    Assert.assertFalse(permissionsRules.isAllowed(PERMISSION_CONTRIBUTOR,CHANGE_PERMISSIONS_ACTION));
  }

  @Test
  public void testIsAllowedCaseChangePermissionsOwner(){
    Assert.assertTrue(permissionsRules.isAllowed(PERMISSION_OWNER,CHANGE_PERMISSIONS_ACTION));
  }

  @Test(expectedExceptions = CoreException.class,expectedExceptionsMessageRegExp =
      "Invalid permission type")
  public void testUpdatePermissionWhenInvalidPermission()  {
    permissionsRules.updatePermission(ITEM1_ID,USER1_ID,INVALID_PERMISSION,new HashSet<String>(),
        new HashSet<String>());
  }

  @Test(expectedExceptions = CoreException.class,expectedExceptionsMessageRegExp =
      "Invalid action type")
  public void testExecuteActionInvalidAction(){
    permissionsRules.executeAction(ITEM1_ID,USER1_ID,INVALID_ACTION);
  }


}