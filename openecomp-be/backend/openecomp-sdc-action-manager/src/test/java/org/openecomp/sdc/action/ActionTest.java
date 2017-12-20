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

package org.openecomp.sdc.action;

@SuppressWarnings("Duplicates")
public class ActionTest {

  /*
  Logger logger = LoggerFactory.getLogger(ActionTest.class);
  private static final Version VERSION01 = new Version(0, 1);
  private static final String USER1 = "actionTestUser1";
  private static final String USER2 = "actionTestUser2";
  private static final String ACTION_1 =
      "{\"name\":\"Test_Action1_name\", \"endpointUri\":\"/test/action/uri\"}";
  private static final String ACTION_2 =
      "{\"name\":\"Test_Action2_list\", \"endpointUri\":\"/test/action/uri\", \"categoryList\":[\"Cat-test\", \"Cat-2\"], \"supportedModels\":[{\"versionId\" : \"Model-test\"}], \"supportedComponents\":[{\"Id\":\"APP-C\"}]}";
  private static final String ACTION_3 =
      "{\"name\":\"Test_Action3_list\", \"endpointUri\":\"/test/action/uri\", \"vendorList\":[\"Vendor-test\", \"Vendor-2\"], \"supportedModels\":[{\"versionId\" : \"Model-2\"}], \"supportedComponents\":[{\"Id\":\"MSO\"}]}";
  private static final String ACTION_4 =
      "{\"name\":\"Test_Action4_list\", \"endpointUri\":\"/test/action/uri\", \"categoryList\":[\"Cat-test\", \"Cat-2\"], \"supportedModels\":[{\"versionId\" : \"Model-test\"}], \"supportedComponents\":[{\"Id\":\"APP-C\"}]}";
  private static final String ACTION_5 =
      "{\"name\":\"Test_Action5_list\", \"endpointUri\":\"/test/action/uri\", \"vendorList\":[\"Vendor-test\", \"Vendor-2\"], \"supportedModels\":[{\"versionId\" : \"Model-2\"}], \"supportedComponents\":[{\"Id\":\"MSO\"}]}";
  private static final String ACTION_6 =
      "{\"name\":\"Test_Action6_name\", \"endpointUri\":\"/test/action/uri\"}";
  private static final String ARTIFACT_TEST_ACTION =
      "{\"name\":\"Test_Artifact_Action\", \"endpointUri\":\"/test/artifact/action/uri\", \"vendorList\":[\"Vendor-test\", \"Vendor-2\"], \"supportedModels\":[{\"versionId\" : \"Model-2\"}], \"supportedComponents\":[{\"Id\":\"MSO\"}]}";
  private static final String ACTION_TEST_DELETE =
      "{\"name\":\"Test_Delete_Action\", \"endpointUri\":\"/test/delete/action/uri\", \"categoryList\":[\"Cat-Delete-test\"], \"vendorList\":[\"Vendor-Delete\"], \"supportedModels\":[{\"versionId\" : \"Model-Delete\"}], \"supportedComponents\":[{\"Id\":\"MSO-Delete\"}]}";
  private static final String ACTION_TEST_ARTIFACT_FILE_NAME = "test_artifact_file.txt";
  private static final String ACTION_TEST_UPDATE_ARTIFACT_FILE_NAME =
      "test_artifact_update_file.txt";
  private static ActionManager actionManager = new ActionManagerImpl();
  private static ActionDao actionDao = ActionDaoFactory.getInstance().createInterface();

  private static NoSqlDb noSqlDb;

  private static String action1Id;
  private static String action2Id;

  private static String actionUUId;
  private static Action testArtifactAction;
  private static String expectedArtifactUUID;
  private static ActionArtifact actionArtifact;
  private Action deleteAction;

  private static String testCreate() {
    Action action1 = createAction(ACTION_1);
    Action actionCreated = actionManager.createAction(action1, USER1);
    action1Id = actionCreated.getActionInvariantUuId();
    actionUUId = actionCreated.getActionUuId();
    action1.setVersion(VERSION01.toString());
    ActionEntity loadedAction = actionDao.get(action1.toEntity());
    assertActionEquals(actionCreated, loadedAction.toDto());
    return action1Id;
  }

  private static Action createAction(String requestJSON) {
    Action action = JsonUtil.json2Object(requestJSON, Action.class);
    action.setData(requestJSON);
    return action;
  }

  private static void assertActionEquals(Action actual, Action expected) {
    Assert.assertEquals(actual.getActionUuId(), expected.getActionUuId());
    Assert.assertEquals(actual.getVersion(), expected.getVersion());
    Assert.assertEquals(actual.getName(), expected.getName());
    //Assert.assertEquals(actual.getDescription(), expected.getDescription());
    Assert.assertEquals(actual.getData(), expected.getData());
    Assert.assertEquals(actual.getActionInvariantUuId(), expected.getActionInvariantUuId());
    //Assert.assertEquals(actual.getEndpointUri(), expected.getEndpointUri());
    Assert.assertEquals(actual.getStatus(), expected.getStatus());
    Assert.assertEquals(actual.getSupportedComponents(), expected.getSupportedComponents());
    Assert.assertEquals(actual.getSupportedModels(), expected.getSupportedModels());
  }

  @BeforeTest
  private void init() {
    this.noSqlDb = NoSqlDbFactory.getInstance().createInterface();
    this.noSqlDb.execute("TRUNCATE dox.action;");
    this.noSqlDb.execute("TRUNCATE dox.ecompcomponent;");
    this.noSqlDb.execute("TRUNCATE dox.unique_value;");
    this.noSqlDb.execute("TRUNCATE dox.action_artifact;");
    this.noSqlDb.execute("insert into dox.ecompcomponent(id, name) values ('COMP-1','MSO');");
    this.noSqlDb.execute("insert into dox.ecompcomponent(id, name) values ('COMP-2','APP-C');");
  }

  @Test
  public void createTest() {
    action1Id = testCreate();
  }

  @Test
  public void createTestWithoutActionDetails() {
    final String ACTION_7 =
            "{\"name\":\"Test_Action7_name\"}";
    Action action = createAction(ACTION_7);
    Action actionCreated = actionManager.createAction(action, USER1);
    action1Id = actionCreated.getActionInvariantUuId();
    actionUUId = actionCreated.getActionUuId();
    action.setVersion(VERSION01.toString());
    ActionEntity loadedAction = actionDao.get(action.toEntity());
    assertActionEquals(actionCreated, loadedAction.toDto());
  }

  @Test
  public void createTestWithActionDetailsWithoutEndpointUri() {
    final String ACTION_8 =
            "{\"name\":\"test_action8_name\",\"actionDetails\":[{\"actionType\":\"DMaaP\"}]}";
    Action action = createAction(ACTION_8);
    Action actionCreated = actionManager.createAction(action, USER1);
    action1Id = actionCreated.getActionInvariantUuId();
    actionUUId = actionCreated.getActionUuId();
    action.setVersion(VERSION01.toString());
    ActionEntity loadedAction = actionDao.get(action.toEntity());
    assertActionEquals(actionCreated, loadedAction.toDto());
  }

  @Test
  public void createTestWithActionDetailsWithEndpointUri() {
  final String ACTION_9 =
            "{\"name\":\"test_action9_name\",\"actionDetails\":[{\"actionType\":\"DMaaP\", \"endpointUri\":\"/test/action/uri\"}]}";
    Action action = createAction(ACTION_9);
    Action actionCreated = actionManager.createAction(action, USER1);
    action1Id = actionCreated.getActionInvariantUuId();
    actionUUId = actionCreated.getActionUuId();
    action.setVersion(VERSION01.toString());
    ActionEntity loadedAction = actionDao.get(action.toEntity());
    assertActionEquals(actionCreated, loadedAction.toDto());
  }

  @Test
  public void testGetByInvIdOnCreate() {
    String input =
        "{\"name\":\"Action_2.0\",\"endpointUri\":\"new/action/uri\",\"categoryList\":[\"Cat-1\", \"Cat-2\"],\"displayName\":\"Updated Action\",\"vendorList\":[\"Vendor-1\", \"Vendor-2\"]," +
            "\"supportedModels\":[{\"versionId\":\"AA56B177-9383-4934-8543-0F91A7A04971\",\"invariantID\":\"CC87B177-9383-4934-8543-0F91A7A07193\", \"name\":\"vSBC\",\"version\":\"2.1\",\"vendor\":\"cisco\"}]," +
            "\"supportedComponents\":[{\"Id\":\"BB47B177-9383-4934-8543-0F91A7A06448\", \"name\":\"appc\"}]}";
    Action action1 = createAction(input);
    Action action = actionManager.createAction(action1, USER1);
    action2Id = action.getActionInvariantUuId();
    List<Action> actions =
        actionManager.getActionsByActionInvariantUuId(action.getActionInvariantUuId());
    Assert.assertEquals(1, actions.size());
    Assert.assertEquals("0.1", actions.get(0).getVersion());
  }

  @Test(dependsOnMethods = {"testGetByInvIdOnCreate"})
  public void testGetByIgnoreCaseName() {
    List<Action> actions =
        actionManager.getFilteredActions(ActionConstants.FILTER_TYPE_NAME, "acTion_2.0");
    List<String> actualVersionList = new ArrayList<String>();
    List<String> expectedVersionList = new ArrayList<String>();
    expectedVersionList.add("0.1");
    for (Action action : actions) {
      System.out.println("action by testGetByIgnoreCaseName is::::");
      System.out.println(action.getActionInvariantUuId() + " " + action.getVersion());
      actualVersionList.add(action.getVersion());
    }
    Assert.assertEquals(1, actions.size());
    Assert.assertEquals(expectedVersionList, actualVersionList);
  }

  @Test(dependsOnMethods = {"testGetByInvIdOnCreate"})
  public void testGetByInvIdManyVersionWithoutSubmit() {
    for (int i = 0; i < 11; i++) {
      actionManager.checkin(action2Id, USER1);
      actionManager.checkout(action2Id, USER1);
    }

    List<Action> actions = actionManager.getActionsByActionInvariantUuId(action2Id);
    List<String> actualVersionList = new ArrayList<String>();
    List<String> expectedVersionList = new ArrayList<String>();
    expectedVersionList.add("0.11");
    expectedVersionList.add("0.12");
    System.out.println(actions.size());
    for (Action action : actions) {
      System.out.println("testGetByInvIdManyVersionWithoutSubmit is::::");
      System.out.println(action.getActionInvariantUuId() + " " + action.getVersion());
      actualVersionList.add(action.getVersion());
    }
    Assert.assertEquals(2, actions.size());
    Assert.assertEquals(expectedVersionList, actualVersionList);
  }

  @Test(dependsOnMethods = {"testGetByInvIdManyVersionWithoutSubmit"})
  public void testGetByInvIdManyVersionWithFirstSubmit() {
    actionManager.checkin(action2Id, USER1);//Checkin 0.12
    actionManager.submit(action2Id, USER1); //1.0
    for (int i = 0; i < 11; i++) {
      actionManager.checkout(action2Id, USER1);
      actionManager.checkin(action2Id, USER1);
    }

    List<Action> actions = actionManager.getActionsByActionInvariantUuId(action2Id);
    List<String> actualVersionList = new ArrayList<String>();
    List<String> expectedVersionList = new ArrayList<String>();
    expectedVersionList.add("1.0");
    expectedVersionList.add("1.11");
    System.out.println(actions.size());
    for (Action action : actions) {
      System.out.println("testGetByInvIdManyVersionWithFirstSubmit is::::");
      System.out.println(action.getActionInvariantUuId() + " " + action.getVersion());
      actualVersionList.add(action.getVersion());
    }
    Assert.assertEquals(2, actions.size());
    Assert.assertEquals(expectedVersionList, actualVersionList);
  }

  @Test(dependsOnMethods = {"testGetByInvIdManyVersionWithFirstSubmit"})
  public void testGetByInvIdManyVersionWithMultSubmit() {
    actionManager.submit(action2Id, USER1); //2.0
    for (int i = 0; i < 11; i++) {
      actionManager.checkout(action2Id, USER1);
      actionManager.checkin(action2Id, USER1);
    }
    actionManager.checkout(action2Id, USER1); //2.12

    List<Action> actions = actionManager.getActionsByActionInvariantUuId(action2Id);
    List<String> actualVersionList = new ArrayList<String>();
    List<String> expectedVersionList = new ArrayList<String>();
    expectedVersionList.add("1.0");
    expectedVersionList.add("2.0");
    expectedVersionList.add("2.11");
    expectedVersionList.add("2.12");
    System.out.println(actions.size());
    for (Action action : actions) {
      System.out.println("testGetByInvIdManyVersionWithMultSubmit is::::");
      System.out.println(action.getActionInvariantUuId() + " " + action.getVersion());
      actualVersionList.add(action.getVersion());
    }
    Assert.assertEquals(4, actions.size());
    Assert.assertEquals(expectedVersionList, actualVersionList);
  }

  @Test(dependsOnMethods = {"testGetByInvIdManyVersionWithMultSubmit"})
  public void testGetByInvIdOnName() {
    for (int i = 0; i < 9; i++) {
      actionManager.checkin(action2Id, USER1);
      actionManager.checkout(action2Id, USER1); //2.21
    }

    List<Action> actions =
        actionManager.getFilteredActions(ActionConstants.FILTER_TYPE_NAME, "Action_2.0");
    List<String> actualVersionList = new ArrayList<String>();
    List<String> expectedVersionList = new ArrayList<String>();
    expectedVersionList.add("1.0");
    expectedVersionList.add("2.0");
    expectedVersionList.add("2.20");
    expectedVersionList.add("2.21");
    for (Action action : actions) {
      System.out.println("action by testGetByInvIdOnName is::::");
      System.out.println(action.getActionInvariantUuId() + " " + action.getVersion());
      actualVersionList.add(action.getVersion());
    }
    Assert.assertEquals(4, actions.size());
    Assert.assertEquals(expectedVersionList, actualVersionList);
  }

  @Test(dependsOnMethods = {"createTest"})
  public void testCreateWithExistingActionName_negative() {
    try {
      actionManager.createAction(createAction(ACTION_1), USER1);
      Assert.fail();
    } catch (ActionException exception) {
      logger.error(exception.getMessage());
      Assert.assertEquals(exception.getErrorCode(), ActionErrorConstants.ACTION_ENTITY_UNIQUE_VALUE_ERROR);
    }
  }

  @Test(groups = "updateTestGroup",
      dependsOnMethods = {"testCreateWithExistingActionName_negative", "createTest"})
  public void updateTest() {
    List<String> newSupportedComponents = new LinkedList<>();
    newSupportedComponents.add("Updated MSO");
    newSupportedComponents.add("Updated APPC");

    List<String> newSupportedModels = new LinkedList<>();
    newSupportedModels.add("Updated Model-1");
    newSupportedModels.add("Updated Model-2");

    Action action = new Action();
    action.setActionInvariantUuId(action1Id);
    action.setVersion(VERSION01.toString());
    ActionEntity existingActionEntity = actionDao.get(action.toEntity());
    existingActionEntity
        .setSupportedComponents(newSupportedComponents);    //Updating Supported components
    existingActionEntity.setSupportedModels(newSupportedModels);    //Updating supported models
    //Persisting the updated entity
    Action updatedAction = actionManager.updateAction(existingActionEntity.toDto(), USER1);

    //Create expected response template
    ActionEntity expectedActionEntity = new ActionEntity(action1Id, VERSION01);
    expectedActionEntity.setName(existingActionEntity.getName());
    expectedActionEntity.setActionUuId(existingActionEntity.getActionUuId());
    expectedActionEntity.setActionInvariantUuId(existingActionEntity.getActionInvariantUuId());
    expectedActionEntity.setData(existingActionEntity.getData());
    expectedActionEntity.setStatus(ActionStatus.Locked.name());
    expectedActionEntity.setSupportedComponents(newSupportedComponents);
    expectedActionEntity.setSupportedModels(newSupportedModels);
    Action expectedAction = updateData(expectedActionEntity.toDto());

    assertActionEquals(updatedAction, expectedAction);
  }

  @Test(groups = "updateTestGroup", dependsOnMethods = {"updateTest"})
  public void testUpdateName_negative() {
    try {
      Action action = new Action();
      action.setActionInvariantUuId(action1Id);
      action.setVersion(VERSION01.toString());
      ActionEntity existingActionEntity = actionDao.get(action.toEntity());
      action = existingActionEntity.toDto();
      action.setName("Update - New Action Name");
      //Persisting the updated entity
      actionManager.updateAction(action, USER1);
      Assert.fail();
    } catch (ActionException exception) {
      logger.error(exception.getMessage());
      Assert
          .assertEquals(exception.getErrorCode(), ActionErrorConstants.ACTION_UPDATE_NOT_ALLOWED_CODE_NAME);
    }
  }

  @Test(groups = "updateTestGroup", dependsOnMethods = {"updateTest"})
  public void testUpdateVersion_negative() {
    try {
      Action action = new Action();
      action.setActionInvariantUuId(action1Id);
      action.setVersion(VERSION01.toString());
      ActionEntity existingActionEntity = actionDao.get(action.toEntity());
      action = existingActionEntity.toDto();
      action.setVersion("0.3");
      //Persisting the updated entity
      actionManager.updateAction(action, USER1);
      Assert.fail();
    } catch (ActionException exception) {
      logger.error(exception.getMessage());
      Assert.assertEquals(exception.getErrorCode(), ActionErrorConstants.ACTION_UPDATE_INVALID_VERSION);
    }
  }

  @Test(groups = "updateTestGroup", dependsOnMethods = {"updateTest"})
  public void testUpdateInvalidVersion_negative() {
    try {
      Action action = new Action();
      action.setActionInvariantUuId(action1Id);
      action.setVersion(VERSION01.toString());
      ActionEntity existingActionEntity = actionDao.get(action.toEntity());
      //existingActionEntity.setDisplayName("Display Name Updated");
      Action updatedAction = existingActionEntity.toDto();
      updatedAction.setVersion("invalid_version_format");
      //Persisting the updated entity
      actionManager.updateAction(updatedAction, USER1);
      Assert.fail();
    } catch (ActionException exception) {
      logger.error(exception.getMessage());
      Assert.assertEquals(exception.getErrorCode(), ActionErrorConstants.ACTION_UPDATE_NOT_ALLOWED_CODE);
    }
  }

    /*@Test(groups = "updateTestGroup", dependsOnMethods = {"updateTest"})
    public void testUpdateStatusInvalidEnum_negative() {
        try {
            Action action = new Action();
            action.setActionInvariantUuId(action1Id);
            action.setVersion(VERSION01.toString());
            ActionEntity existingActionEntity = actionDao.get(action.toEntity());
            existingActionEntity.setStatus("invalid_status_string");
            //Persisting the updated entity
            actionManager.updateAction(existingActionEntity.toDto(),USER1);
            Assert.fail();
        } catch (ActionException exception) {
            Assert.assertEquals(exception.getErrorCode(), ActionErrorConstants.ACTION_UPDATE_NOT_ALLOWED_CODE);
        } catch (IllegalArgumentException ie){
            String message = ie.getMessage();
            boolean result = message.contains("No enum constant");
            Assert.assertEquals(true, result);
        }
    }

  @Test(groups = "updateTestGroup", dependsOnMethods = {"updateTest"})
  public void testUpdateInvariantId_negative() {
    try {
      Action action = new Action();
      action.setActionInvariantUuId(action1Id);
      action.setVersion(VERSION01.toString());
      ActionEntity existingActionEntity = actionDao.get(action.toEntity());
      action = existingActionEntity.toDto();
      action.setActionInvariantUuId(UUID.randomUUID().toString());
      //Persisting the updated entity
      actionManager.updateAction(action, USER1);
      Assert.fail();
    } catch (ActionException exception) {
      logger.error(exception.getMessage());
      Assert.assertEquals(exception.getErrorCode(), ActionErrorConstants.ACTION_ENTITY_NOT_EXIST_CODE);
    }
  }

  @Test(groups = "updateTestGroup", dependsOnMethods = {"updateTest"})
  public void testUpdateUniqueId_negative() {
    try {
      Action action = new Action();
      action.setActionInvariantUuId(action1Id);
      action.setVersion(VERSION01.toString());
      ActionEntity existingActionEntity = actionDao.get(action.toEntity());
      //existingActionEntity.setActionUuId(UUID.randomUUID().toString());

      action = existingActionEntity.toDto();
      action.setActionUuId(UUID.randomUUID().toString());
      //Persisting the updated entity
      //actionManager.updateAction(existingActionEntity.toDto(),USER1);
      actionManager.updateAction(action, USER1);
      Assert.fail();
    } catch (ActionException exception) {
      logger.error(exception.getMessage());
      Assert.assertEquals(exception.getErrorCode(), ActionErrorConstants.ACTION_UPDATE_NOT_ALLOWED_CODE);
    }
  }

  @Test(groups = "updateTestGroup", dependsOnMethods = {"updateTest"})
  public void testUpdateStatus_negative() {
    try {
      Action action = new Action();
      action.setActionInvariantUuId(action1Id);
      action.setVersion(VERSION01.toString());
      ActionEntity existingActionEntity = actionDao.get(action.toEntity());
      action = existingActionEntity.toDto();
      action.setStatus(ActionStatus.Final);
      //Persisting the updated entity
      actionManager.updateAction(action, USER1);
      Assert.fail();
    } catch (ActionException exception) {
      logger.error(exception.getMessage());
      Assert.assertEquals(exception.getErrorCode(), ActionErrorConstants.ACTION_UPDATE_NOT_ALLOWED_CODE);
    } catch (IllegalArgumentException ie) {
      logger.error(ie.getMessage());
      String message = ie.getMessage();
      boolean result = message.contains("No enum constant");
      Assert.assertEquals(true, result);
    }
  }

  @Test(groups = "updateTestGroup", dependsOnMethods = {"updateTest"})
  public void testUpdateOtherUser_negative() {
    try {
      Action action = new Action();
      action.setActionInvariantUuId(action1Id);
      action.setVersion(VERSION01.toString());
      ActionEntity existingActionEntity = actionDao.get(action.toEntity());
      action = existingActionEntity.toDto();
      //existingActionEntity.setDescription("Testing Update using other user");
      //Persisting the updated entity
      actionManager.updateAction(action, USER2);
      Assert.fail();
    } catch (ActionException exception) {
      logger.error(exception.getMessage());
      Assert.assertEquals(exception.getErrorCode(),
          ActionErrorConstants.ACTION_EDIT_ON_ENTITY_LOCKED_BY_OTHER_USER);
    }
  }

  @Test(dependsOnMethods = {"createTest"})
  public void testCheckOutOnCheckOut() {
    try {
      actionManager.checkout(action1Id, USER1);
    } catch (ActionException wae) {
      logger.error(wae.getMessage());
      Assert
          .assertEquals(wae.getErrorCode(), ActionErrorConstants.ACTION_CHECKOUT_ON_LOCKED_ENTITY);
      Assert.assertEquals(wae.getDescription(),
          "Can not check out versionable entity Action with id " + action1Id +
              " since it is checked out by other user: " + USER1 + ".");
    }
  }

  @Test(dependsOnMethods = {"createTest"})
  public void testCheckOutOnCheckOutWithOtherUser() {
    try {
      actionManager.checkout(action1Id, "invlaiduser");
    } catch (ActionException wae) {
      logger.error(wae.getMessage());
      Assert.assertEquals(wae.getErrorCode(),
          ActionErrorConstants.ACTION_CHECKOUT_ON_LOCKED_ENTITY_OTHER_USER);
      Assert.assertEquals(wae.getDescription(),
          "Can not check out versionable entity Action with id " + action1Id +
              " since it is checked out by other user: " + USER1 + ".");
    }
  }

  @Test(dependsOnGroups = {"updateTestGroup"})
  public void testCheckIn() {
    Action action = actionManager.checkin(action1Id, USER1);
    Assert.assertEquals(action.getActionInvariantUuId(), action1Id);
    Assert.assertEquals(action.getStatus(), ActionStatus.Available);
    Assert.assertEquals(action.getVersion(), VERSION01.toString());
    Assert.assertNotNull(action.getActionUuId());
  }

  @Test(dependsOnMethods = {"testCheckIn"})
  public void testUpdateOnCheckedInAction_negative() {
    try {
      Action action = new Action();
      action.setActionInvariantUuId(action1Id);
      action.setVersion(VERSION01.toString());
      ActionEntity existingActionEntity = actionDao.get(action.toEntity());
      //existingActionEntity.setDescription("Testing Update On Checked In Action");
      //Persisting the updated entity
      actionManager.updateAction(existingActionEntity.toDto(), USER1);
      Assert.fail();
    } catch (ActionException exception) {
      logger.error(exception.getMessage());
      Assert.assertEquals(exception.getErrorCode(), ActionErrorConstants.ACTION_UPDATE_ON_UNLOCKED_ENTITY);
    }
  }

  @Test(dependsOnMethods = {"testUpdateOnCheckedInAction_negative"})
  public void testSubmit() {
    Action action = actionManager.submit(action1Id, USER1);
    ActionEntity loadedAction = actionDao.get(action.toEntity());
    assertActionEquals(action, loadedAction.toDto());
  }

  @Test(dependsOnMethods = {"testSubmit"})
  public void testCheckInWithoutCheckout() {
    try {
      actionManager.checkin(action1Id, "invaliduser");
    } catch (ActionException wae) {
      logger.error(wae.getMessage());
      Assert
          .assertEquals(wae.getErrorCode(), ActionErrorConstants.ACTION_CHECKIN_ON_UNLOCKED_ENTITY);
      Assert.assertEquals(wae.getDescription(),
          "Can not check in versionable entity Action with id " + action1Id +
              " since it is not checked out.");
    }
  }

  @Test(dependsOnMethods = {"testSubmit"})
  public void testCheckOut() {
    final Version VERSION02 = new Version(1, 1);
    Action action = null;
    action = actionManager.checkout(action1Id, USER1);
    ActionEntity loadedAction = actionDao.get(action.toEntity());
    assertActionEquals(action, loadedAction.toDto());
  }

  @Test(dependsOnMethods = {"testCheckOut"})
  public void testCheckInWithOtherUser() {
    try {
      actionManager.checkin(action1Id, "invaliduser");
    } catch (ActionException wae) {
      logger.error(wae.getMessage());
      Assert.assertEquals(wae.getErrorCode(),
          ActionErrorConstants.ACTION_CHECKIN_ON_ENTITY_LOCKED_BY_OTHER_USER);
      Assert.assertEquals(wae.getDescription(),
          "Can not check in versionable entity Action with id " + action1Id +
              " since it is checked out by other user: " + USER1 + ".");
    }
  }

  @Test(dependsOnMethods = {"testCheckOut"})
  public void testSubmitOnCheckout() {
    try {
      actionManager.submit(action1Id, USER1);
    } catch (ActionException wae) {
      logger.error(wae.getMessage());
      Assert.assertEquals(wae.getErrorCode(),
          ActionErrorConstants.ACTION_SUBMIT_LOCKED_ENTITY_NOT_ALLOWED);
      Assert.assertEquals(wae.getDescription(), "Versionable entity Action with id " + action1Id +
          " can not be submitted since it is currently locked by user " + USER1 + ".");
    }
  }

  @Test(dependsOnMethods = {"testCheckOut"})
  public void testUndoCheckout() {
    final Version VERSION11 = new Version(1, 1);
    actionManager.undoCheckout(action1Id, USER1);
    Action action = new Action();
    action.setActionInvariantUuId(action1Id);
    action.setVersion(VERSION11.toString());
    ActionEntity existingActionEntity = actionDao.get(action.toEntity());
    Assert.assertNull(existingActionEntity);
  }

  @Test
  public void testUndoCheckoutOnCreate() {
    Action action = actionManager.createAction(createAction(ACTION_6), USER1);
    actionManager.undoCheckout(action.getActionInvariantUuId(), USER1);
    ActionEntity existingActionEntity = actionDao.get(action.toEntity());
    Assert.assertNull(existingActionEntity);
  }

  @Test
  public void testGetOpenECOMPComponents() {
    List<OpenEcompComponent> componentList = actionManager.getOpenEcompComponents();
    List<OpenEcompComponent> expectedComponentList = new ArrayList<>();
    expectedComponentList.add(new OpenEcompComponent("MSO", "COMP-1"));
    expectedComponentList.add(new OpenEcompComponent("APP-C", "COMP-2"));
    for (OpenEcompComponent exception : componentList) {
      boolean res = expectedComponentList.contains(exception);
      Assert.assertEquals(res, true);
    }
  }

  @Test
  public void testgetActionsByActionUUID_Negative() {
    try {
      Action action = actionManager.getActionsByActionUuId("");
      Assert.fail();
    } catch (ActionException exception) {
      logger.error(exception.getMessage());
      Assert.assertEquals(exception.getErrorCode(), ActionErrorConstants.ACTION_ENTITY_NOT_EXIST_CODE);
    }
  }

  @Test(dependsOnMethods = {"createTest"})
  public void testgetActionsByActionUUID() {
    Action action = actionManager.getActionsByActionUuId(actionUUId);
    Assert.assertNotNull(action.getData());
  }

  @Test
  public void testGetByCategory() {
    createActionVersions(ACTION_2);
    createActionVersions(ACTION_3);
    createActionVersions(ACTION_4);
    createActionVersions(ACTION_5);
    List<Action> actions =
        actionManager.getFilteredActions(ActionConstants.FILTER_TYPE_CATEGORY, "CAT-teSt");

    List<String> actualNameVersionList = new ArrayList<String>();
    List<String> expectedNameVersionList = new ArrayList<String>();
    expectedNameVersionList.add("Test_Action4_list:2.2");
    expectedNameVersionList.add("Test_Action4_list:2.0");
    expectedNameVersionList.add("Test_Action2_list:2.2");
    expectedNameVersionList.add("Test_Action2_list:2.0");
    for (Action action : actions) {
      System.out.println("action by category is::::");
      System.out.println(action.getName() + " " + action.getVersion());
      actualNameVersionList.add(action.getName() + ":" + action.getVersion());
    }
    Assert.assertEquals(4, actions.size());
    Assert.assertEquals(expectedNameVersionList, actualNameVersionList);
  }

  @Test(dependsOnMethods = {"testGetByCategory"})
  public void testGetByVendor() {
    List<Action> actions =
        actionManager.getFilteredActions(ActionConstants.FILTER_TYPE_VENDOR, "VendOr-tESt");

    List<String> actualNameVersionList = new ArrayList<String>();
    List<String> expectedNameVersionList = new ArrayList<String>();
    expectedNameVersionList.add("Test_Action5_list:2.2");
    expectedNameVersionList.add("Test_Action5_list:2.0");
    expectedNameVersionList.add("Test_Action3_list:2.2");
    expectedNameVersionList.add("Test_Action3_list:2.0");
    for (Action action : actions) {
      System.out.println("action by category is::::");
      System.out.println(action.getName() + " " + action.getVersion());
      actualNameVersionList.add(action.getName() + ":" + action.getVersion());
    }
    Assert.assertEquals(4, actions.size());
    Assert.assertEquals(expectedNameVersionList, actualNameVersionList);
  }

  @Test(dependsOnMethods = {"testGetByCategory"})
  public void testGetBySupportedModel() {
    List<Action> actions =
        actionManager.getFilteredActions(ActionConstants.FILTER_TYPE_MODEL, "MODEL-tEst");

    List<String> actualNameVersionList = new ArrayList<>();
    List<String> expectedNameVersionList = new ArrayList<>();
    expectedNameVersionList.add("Test_Action4_list:2.2");
    expectedNameVersionList.add("Test_Action4_list:2.0");
    expectedNameVersionList.add("Test_Action2_list:2.2");
    expectedNameVersionList.add("Test_Action2_list:2.0");
    for (Action action : actions) {
      actualNameVersionList.add(action.getName() + ":" + action.getVersion());
    }
    Assert.assertEquals(4, actions.size());
    Assert.assertEquals(expectedNameVersionList, actualNameVersionList);
  }

  @Test(dependsOnMethods = {"testGetByCategory"})
  public void testGetBySupportedComponent() {
    List<Action> actions =
        actionManager.getFilteredActions(ActionConstants.FILTER_TYPE_OPEN_ECOMP_COMPONENT, "mso");

    List<String> actualNameVersionList = new ArrayList<>();
    List<String> expectedNameVersionList = new ArrayList<>();
    expectedNameVersionList.add("Test_Action5_list:2.2");
    expectedNameVersionList.add("Test_Action5_list:2.0");
    expectedNameVersionList.add("Test_Action3_list:2.2");
    expectedNameVersionList.add("Test_Action3_list:2.0");
    for (Action action : actions) {
      actualNameVersionList.add(action.getName() + ":" + action.getVersion());
    }
    Assert.assertEquals(4, actions.size());
    Assert.assertEquals(expectedNameVersionList, actualNameVersionList);
  }

  @Test(dependsOnMethods = {"testGetByCategory"})
  public void testGetAllActions() {
    List<Action> actions =
        actionManager.getFilteredActions(ActionConstants.FILTER_TYPE_NONE, "MSO");

    List<String> actualNameVersionList = new ArrayList<>();
    List<String> expectedNameVersionList = new ArrayList<>();

    expectedNameVersionList.add("Test_Action5_list:2.2");
    expectedNameVersionList.add("Test_Action5_list:2.0");
    expectedNameVersionList.add("Test_Action3_list:2.2");
    expectedNameVersionList.add("Test_Action3_list:2.0");
    expectedNameVersionList.add("Test_Action4_list:2.2");
    expectedNameVersionList.add("Test_Action4_list:2.0");
    expectedNameVersionList.add("Test_Action2_list:2.2");
    expectedNameVersionList.add("Test_Action2_list:2.0");
    for (Action action : actions) {
      actualNameVersionList.add(action.getName() + ":" + action.getVersion());
    }
    Assert.assertEquals(8, actions.size());

    for (String s : actualNameVersionList) {
      boolean res = expectedNameVersionList.contains(s);
      Assert.assertEquals(res, true);
    }
  }

  @Test(dependsOnMethods = {"testGetAllActions"})
  public void testDeleteCheckedOutAction_Negative() {
    try {
      initDeleteActionTest();
      String deleteActionInvariantId = deleteAction.getActionInvariantUuId();
      actionManager.deleteAction(deleteActionInvariantId, USER1);
      Assert.fail();
    } catch (ActionException exception) {
      logger.error(exception.getMessage());
      Assert.assertEquals(exception.getErrorCode(), ACTION_DELETE_ON_LOCKED_ENTITY_CODE);
      Assert.assertEquals(exception.getDescription(), String.format(
          "Can not delete versionable entity Action with id %s since it is checked out by other user: %s",
          deleteAction.getActionInvariantUuId(), USER1 + "."));
    }
  }

  @Test(dependsOnMethods = {"testDeleteCheckedOutAction_Negative"})
  public void testDeleteAction() {
    try {
      String deleteActionInvariantId = deleteAction.getActionInvariantUuId();
      actionManager.checkin(deleteActionInvariantId, USER1);
      actionManager.deleteAction(deleteActionInvariantId, USER1);
    } catch (ActionException exception) {
      logger.error(exception.getMessage());
      Assert.fail("Delete action test failed with exception : " + exception.getDescription());
    }
  }

  @Test(dependsOnMethods = {"testDeleteAction"})
  public void testDeletedActionVersioningOperations_Negative() {
    String deleteActionInvariantId = deleteAction.getActionInvariantUuId();
    try {
      actionManager.checkout(deleteActionInvariantId, USER1);
      Assert.fail();
    } catch (ActionException exception) {
      logger.error(exception.getMessage());
      Assert.assertEquals(exception.getErrorCode(), ACTION_ENTITY_NOT_EXIST_CODE);
      Assert.assertEquals(exception.getDescription(), ACTION_ENTITY_NOT_EXIST);
    }
    try {
      actionManager.checkin(deleteActionInvariantId, USER1);
      Assert.fail();
    } catch (ActionException exception) {
      logger.error(exception.getMessage());
      Assert.assertEquals(exception.getErrorCode(), ACTION_ENTITY_NOT_EXIST_CODE);
      Assert.assertEquals(exception.getDescription(), ACTION_ENTITY_NOT_EXIST);
    }
    try {
      actionManager.submit(deleteActionInvariantId, USER1);
      Assert.fail();
    } catch (ActionException exception) {
      logger.error(exception.getMessage());
      Assert.assertEquals(exception.getErrorCode(), ACTION_ENTITY_NOT_EXIST_CODE);
      Assert.assertEquals(exception.getDescription(), ACTION_ENTITY_NOT_EXIST);
    }
    try {
      actionManager.undoCheckout(deleteActionInvariantId, USER1);
      Assert.fail();
    } catch (ActionException exception) {
      logger.error(exception.getMessage());
      Assert.assertEquals(exception.getErrorCode(), ACTION_ENTITY_NOT_EXIST_CODE);
      Assert.assertEquals(exception.getDescription(), ACTION_ENTITY_NOT_EXIST);
    }
    try {
      actionManager.deleteAction(deleteActionInvariantId, USER1);
      Assert.fail();
    } catch (ActionException exception) {
      logger.error(exception.getMessage());
      Assert.assertEquals(exception.getErrorCode(), ACTION_ENTITY_NOT_EXIST_CODE);
      Assert.assertEquals(exception.getDescription(), ACTION_ENTITY_NOT_EXIST);
    }
  }

  @Test(dependsOnMethods = {"testDeleteAction"})
  public void testCreateActionWithDeletedActionName_Negative() {
    try {
      actionManager.createAction(createAction(ACTION_TEST_DELETE), USER1);
      Assert.fail();
    } catch (ActionException exception) {
      logger.error(exception.getMessage());
      Assert.assertEquals(exception.getErrorCode(), ACTION_ENTITY_UNIQUE_VALUE_ERROR);
      Assert.assertEquals(exception.getDescription(), String
          .format(ACTION_ENTITY_UNIQUE_VALUE_MSG, ActionConstants.UniqueValues.ACTION_NAME,
              deleteAction.getName()));
    }
  }

  @Test(dependsOnMethods = {"testDeleteAction"})
  public void testDeletedActionGetQueries() {
    String deleteActionInvariantId = deleteAction.getActionInvariantUuId();
    List<Action> invariantFetchResults =
        actionManager.getActionsByActionInvariantUuId(deleteActionInvariantId);
    Assert.assertEquals(invariantFetchResults.size(), 3);
    for (Action a : invariantFetchResults) {
      Assert.assertEquals(a.getStatus(), ActionStatus.Deleted);
    }

    Action actionUUIDFetchResult =
        actionManager.getActionsByActionUuId(deleteAction.getActionUuId());
    Assert.assertEquals(actionUUIDFetchResult.getStatus(), ActionStatus.Deleted);

    List<Action> nameFetchResults =
        actionManager.getFilteredActions(FILTER_TYPE_NAME, "Test_Delete_Action");
    Assert.assertEquals(nameFetchResults.size(), 3);
    for (Action a : nameFetchResults) {
      Assert.assertEquals(a.getStatus(), ActionStatus.Deleted);
    }

    List<Action> filteredActions =
        actionManager.getFilteredActions(ActionConstants.FILTER_TYPE_VENDOR, "Vendor-Delete");
    Assert.assertEquals(filteredActions.size(), 0);
    filteredActions =
        actionManager.getFilteredActions(ActionConstants.FILTER_TYPE_CATEGORY, "Cat-Delete-test");
    Assert.assertEquals(filteredActions.size(), 0);
    filteredActions =
        actionManager.getFilteredActions(ActionConstants.FILTER_TYPE_OPEN_ECOMP_COMPONENT, "MSO-delete");
    Assert.assertEquals(filteredActions.size(), 0);
    filteredActions =
        actionManager.getFilteredActions(ActionConstants.FILTER_TYPE_MODEL, "Model-Delete");
    Assert.assertEquals(filteredActions.size(), 0);
  }

  /***
   * ACTION ARTIFACT OPERATION TEST CASES
   ***/
/*
  @Test
  public void testUploadArtifact() {
    actionArtifact = new ActionArtifact();
    File resourceFile = new File(
        this.getClass().getClassLoader().getResource(ACTION_TEST_ARTIFACT_FILE_NAME).getPath());
    FileInputStream fileInputStream;
    //Create payload from the test resource file
    byte[] payload = new byte[(int) resourceFile.length()];
    try {
      fileInputStream = new FileInputStream(resourceFile);
      fileInputStream.read(payload);
      fileInputStream.close();
      actionArtifact.setArtifact(payload);
      actionArtifact.setArtifactName(ACTION_TEST_ARTIFACT_FILE_NAME);
      actionArtifact.setArtifactLabel("Test Artifact Label");
      actionArtifact.setArtifactDescription("Test Artifact Description");
      actionArtifact.setArtifactProtection(ActionArtifactProtection.readWrite.name());
    } catch (IOException exception) {
      logger.error(exception.getMessage());
      exception.printStackTrace();
    }

    //Create action for artifact upload test
    testArtifactAction = actionManager.createAction(createAction(ARTIFACT_TEST_ACTION), USER1);
    //Generate Expected artifact UUID
    expectedArtifactUUID =
        generateActionArtifactUUID(testArtifactAction, ACTION_TEST_ARTIFACT_FILE_NAME);
    //Upload the artifact
    ActionArtifact response = actionManager
        .uploadArtifact(actionArtifact, testArtifactAction.getActionInvariantUuId(), USER1);
    //Validate if generated and the expected artifact UUID is same
    Assert.assertEquals(expectedArtifactUUID, response.getArtifactUuId());
    //Fetch the data field of the updated action version
    Action updatedAction = actionManager.getActionsByActionUuId(testArtifactAction.getActionUuId());
    List<ActionArtifact> updatedArtifactList = updatedAction.getArtifacts();
    for (ActionArtifact artifact : updatedArtifactList) {
      //Validate the artifact metadata
      Assert.assertEquals(artifact.getArtifactName(), actionArtifact.getArtifactName());
      Assert.assertEquals(artifact.getArtifactLabel(), actionArtifact.getArtifactLabel());
      Assert
          .assertEquals(artifact.getArtifactDescription(), actionArtifact.getArtifactDescription());
      Assert.assertEquals(artifact.getArtifactProtection(), actionArtifact.getArtifactProtection());
    }
  }

  @Test(dependsOnMethods = {"testUploadArtifact"})
  public void testUploadArtifactInvalidActionInvId_negative() {
    ActionArtifact testArtifact = new ActionArtifact();
    testArtifact.setArtifact("testData".getBytes());
    testArtifact.setArtifactName(ACTION_TEST_ARTIFACT_FILE_NAME);
    try {
      actionManager.uploadArtifact(testArtifact, "INVALID_UUID", USER1);
    } catch (ActionException ae) {
      logger.error(ae.getMessage());
      Assert.assertEquals(ae.getErrorCode(), ACTION_ENTITY_NOT_EXIST_CODE);
      Assert.assertEquals(ae.getDescription(), ACTION_ENTITY_NOT_EXIST);
    }
  }

  @Test(dependsOnMethods = {"testUploadArtifact"})
  public void testUploadArtifactSameName_negative() {
    try {
      actionManager
          .uploadArtifact(actionArtifact, testArtifactAction.getActionInvariantUuId(), USER1);
    } catch (ActionException ae) {
      logger.error(ae.getMessage());
      Assert.assertEquals(ae.getErrorCode(), ACTION_ARTIFACT_ALREADY_EXISTS_CODE);
      Assert.assertEquals(ae.getDescription(), String
          .format(ACTION_ARTIFACT_ALREADY_EXISTS, testArtifactAction.getActionInvariantUuId()));
    }
  }

  @Test(dependsOnMethods = {"testUploadArtifact"})
  public void testUploadArtifactCheckedOutOtherUser_negative() {
    try {
      actionManager
          .uploadArtifact(actionArtifact, testArtifactAction.getActionInvariantUuId(), USER2);
    } catch (ActionException ae) {
      logger.error(ae.getMessage());
      Assert.assertEquals(ae.getErrorCode(), ACTION_EDIT_ON_ENTITY_LOCKED_BY_OTHER_USER);
      Assert.assertEquals(ae.getDescription(),
          "Versionable entity Action with id " + testArtifactAction.getActionInvariantUuId() +
              " can not be updated since it is locked by other user " + USER1 + ".");
    }
  }

  @Test(dependsOnMethods = {"testUploadArtifact"})
  public void testUploadArtifactUnlockedAction_negative() {
    try {
      testArtifactAction =
          actionManager.checkin(testArtifactAction.getActionInvariantUuId(), USER1);
      actionManager
          .uploadArtifact(actionArtifact, testArtifactAction.getActionInvariantUuId(), USER1);
    } catch (ActionException ae) {
      logger.error(ae.getMessage());
      Assert.assertEquals(ae.getErrorCode(), ACTION_UPDATE_ON_UNLOCKED_ENTITY);
      Assert.assertEquals(ae.getDescription(), "Can not update versionable entity Action with id " +
          testArtifactAction.getActionInvariantUuId() + " since it is not checked out.");
    }
  }

  @Test(dependsOnMethods = {"testUploadArtifact"})
  public void testDownloadArtifact() {
    String actionUUID = testArtifactAction.getActionUuId();
    ActionArtifact response = actionManager.downloadArtifact(actionUUID, expectedArtifactUUID);
    Assert.assertEquals(actionArtifact.getArtifactName(), response.getArtifactName());
    Assert.assertEquals(actionArtifact.getArtifact(), response.getArtifact());
  }

  @Test(dependsOnMethods = {"testUploadArtifact"})
  public void testDownloadArtifactNegativeInvalidArtifact() {
    String actionUUID = testArtifactAction.getActionUuId();
    String artifactUUID = "negativeArtifact";
    try {
      ActionArtifact response = actionManager.downloadArtifact(actionUUID, artifactUUID);
    } catch (ActionException ae) {
      logger.error(ae.getMessage());
      Assert.assertEquals(ae.getErrorCode(), ACTION_ARTIFACT_ENTITY_NOT_EXIST_CODE);
    }

  }

  @Test
  public void testDownloadArtifactNegativeInvalidAction() {
    String actionUUID = "NegativeAction";
    try {
      ActionArtifact response = actionManager.downloadArtifact(actionUUID, expectedArtifactUUID);
    } catch (ActionException ae) {
      logger.error(ae.getMessage());
      Assert.assertEquals(ae.getErrorCode(), ACTION_ENTITY_NOT_EXIST_CODE);
    }

  }

  @Test
  public void testDeleteArtifactInvalidActInvId() {
    try {
      actionManager.deleteArtifact("action2Id", "1234", USER1);
    } catch (ActionException exception) {
      logger.error(exception.getMessage());
      Assert.assertEquals(exception.getErrorCode(), ActionErrorConstants.ACTION_ENTITY_NOT_EXIST_CODE);
      Assert.assertEquals(exception.getDescription(), ActionErrorConstants.ACTION_ENTITY_NOT_EXIST);
    }
  }

  @Test(dependsOnMethods = {"testGetByInvIdOnCreate"})
  public void testDeleteArtifactInvalidArtifactUUID() {
    try {
      actionManager.deleteArtifact(action2Id, "1234", USER1);
    } catch (ActionException exception) {
      logger.error(exception.getMessage());
      Assert.assertEquals(exception.getErrorCode(),
          ActionErrorConstants.ACTION_ARTIFACT_ENTITY_NOT_EXIST_CODE);
      Assert
          .assertEquals(exception.getDescription(), ActionErrorConstants.ACTION_ARTIFACT_ENTITY_NOT_EXIST);
    }
  }

  @Test(dependsOnMethods = {"testUploadArtifact"})
  public void testDeleteReadOnlyArtifact() {
    ActionArtifact testArtifact = null;
    String artifactUUID = null;
    try {
      testArtifact = new ActionArtifact();
      testArtifact.setArtifact("testData".getBytes());
      testArtifact.setArtifactProtection(ActionArtifactProtection.readOnly.name());
      testArtifact.setArtifactName("TestRO.txt");
      actionManager
          .uploadArtifact(testArtifact, testArtifactAction.getActionInvariantUuId(), USER1);
      artifactUUID = testArtifact.getArtifactUuId();
      actionManager.deleteArtifact(testArtifactAction.getActionInvariantUuId(),
          testArtifact.getArtifactUuId(), USER1);

    } catch (ActionException exception) {
      logger.error(exception.getMessage());
      Assert.assertEquals(exception.getErrorCode(), ActionErrorConstants.ACTION_ARTIFACT_DELETE_READ_ONLY);
      Assert.assertEquals(exception.getDescription(),
          ActionErrorConstants.ACTION_ARTIFACT_DELETE_READ_ONLY_MSG);
    }

    //cleanup uploaded document after test
    testArtifact = new ActionArtifact();
    testArtifact.setArtifactUuId(artifactUUID);
    testArtifact.setArtifactProtection(ActionArtifactProtection.readWrite.name());
    actionManager.updateArtifact(testArtifact, testArtifactAction.getActionInvariantUuId(), USER1);
    actionManager
        .deleteArtifact(testArtifactAction.getActionInvariantUuId(), testArtifact.getArtifactUuId(),
            USER1);
  }

  @Test(dependsOnMethods = {"testUploadArtifact"})
  public void testDeleteArtifactLockedByOtherUser() {
    try {
      actionManager.deleteArtifact(testArtifactAction.getActionInvariantUuId(),
          actionArtifact.getArtifactUuId(), USER2);
    } catch (ActionException ae) {
      logger.error(ae.getMessage());
      Assert.assertEquals(ae.getErrorCode(), ACTION_ARTIFACT_DEL_LOCKED_OTHER_USER_CODE);
      Assert.assertEquals(ae.getDescription(),
          String.format(ACTION_ARTIFACT_DEL_LOCKED_OTHER_USER, USER1));
    }
  }

  @Test(dependsOnMethods = {"testUploadArtifactUnlockedAction_negative"})
  public void testDeleteArtifactOnUnlockedAction() {
    try {
      actionManager.deleteArtifact(testArtifactAction.getActionInvariantUuId(),
          actionArtifact.getArtifactUuId(), USER1);
    } catch (ActionException ae) {
      logger.error(ae.getMessage());
      Assert.assertEquals(ae.getErrorCode(), ACTION_NOT_LOCKED_CODE);
      Assert.assertEquals(ae.getDescription(), ACTION_NOT_LOCKED_MSG);
    }
  }

  @Test(dependsOnMethods = {"testUploadArtifact"})
  public void testDeleteArtifact() {
    try {
      ActionArtifact testArtifact = new ActionArtifact();
      testArtifact.setArtifact("testData".getBytes());
      testArtifact.setArtifactName("Test_ToBeDel.txt");
      testArtifact.setArtifactProtection(ActionArtifactProtection.readWrite.name());
      actionManager
          .uploadArtifact(testArtifact, testArtifactAction.getActionInvariantUuId(), USER1);
      actionManager.deleteArtifact(testArtifactAction.getActionInvariantUuId(),
          testArtifact.getArtifactUuId(), USER1);
      ActionArtifact response = actionManager
          .downloadArtifact(testArtifactAction.getActionUuId(), testArtifact.getArtifactUuId());
    } catch (ActionException exception) {
      logger.error(exception.getMessage());
      Assert.assertEquals(exception.getErrorCode(),
          ActionErrorConstants.ACTION_ARTIFACT_ENTITY_NOT_EXIST_CODE);
      Assert
          .assertEquals(exception.getDescription(), ActionErrorConstants.ACTION_ARTIFACT_ENTITY_NOT_EXIST);
    }
  }

  @Test(dependsOnMethods = {"testUploadArtifact"})
  public void testUpdateArtifact() {
    ActionArtifact updatedArtifact = new ActionArtifact();
    File resourceFile = new File(
        this.getClass().getClassLoader().getResource(ACTION_TEST_UPDATE_ARTIFACT_FILE_NAME)
            .getPath());
    FileInputStream fileInputStream;
    //Create payload from the test resource file
    byte[] payload = new byte[(int) resourceFile.length()];
    try {
      fileInputStream = new FileInputStream(resourceFile);
      fileInputStream.read(payload);
      fileInputStream.close();
      updatedArtifact.setArtifactUuId(
          generateActionArtifactUUID(testArtifactAction, ACTION_TEST_ARTIFACT_FILE_NAME));
      updatedArtifact.setArtifact(payload);
      updatedArtifact.setArtifactName(ACTION_TEST_ARTIFACT_FILE_NAME);
      updatedArtifact.setArtifactLabel("Test Artifact Update Label");
      updatedArtifact.setArtifactDescription("Test Artifact Update Description");
      updatedArtifact.setArtifactProtection(ActionArtifactProtection.readWrite.name());
    } catch (IOException exception) {
      logger.error(exception.getMessage());
      exception.printStackTrace();
    }

    String actionInvarientUUID = testArtifactAction.getActionInvariantUuId();
    actionManager.updateArtifact(updatedArtifact, actionInvarientUUID, USER1);

    String actionUUID = testArtifactAction.getActionUuId();
    Action action = actionManager.getActionsByActionUuId(actionUUID);
    List<ActionArtifact> artifacts = action.getArtifacts();
    for (ActionArtifact actionArtifact : artifacts) {
      Assert.assertEquals(actionArtifact.getArtifactName(), updatedArtifact.getArtifactName());
      Assert.assertEquals(actionArtifact.getArtifactLabel(), updatedArtifact.getArtifactLabel());
      Assert.assertEquals(actionArtifact.getArtifactDescription(),
          updatedArtifact.getArtifactDescription());
      Assert.assertEquals(actionArtifact.getArtifactProtection(),
          updatedArtifact.getArtifactProtection());
    }
  }

  @Test(dependsOnMethods = {"testUploadArtifact"})
  public void testUpdateArtifact_ArtifactNotPresent_Negative() {
    ActionArtifact invalidActionArtifact = new ActionArtifact();
    String artifactUUID = generateActionArtifactUUID(testArtifactAction, "ArtifactNotPresent");
    invalidActionArtifact.setArtifactUuId(artifactUUID);
    try {
      actionManager
          .updateArtifact(invalidActionArtifact, testArtifactAction.getActionInvariantUuId(),
              USER1);
    } catch (ActionException actionException) {
      logger.error(actionException.getMessage());
      Assert.assertEquals(actionException.getDescription(), ACTION_ARTIFACT_ENTITY_NOT_EXIST);
    }
  }

  @Test(dependsOnMethods = {"testUploadArtifact"})
  public void testUpdateArtifact_ArtifactNameUpdate_Negative() {
    String invariantUUID = testArtifactAction.getActionInvariantUuId();
    ActionArtifact artifactToUpdate = new ActionArtifact();
    artifactToUpdate.setArtifactUuId(actionArtifact.getArtifactUuId());
    artifactToUpdate.setArtifactName("UpdatingName");

    try {
      actionManager.updateArtifact(artifactToUpdate, invariantUUID, USER1);
    } catch (ActionException actionException) {
      Assert.assertEquals(actionException.getDescription(), ACTION_ARTIFACT_UPDATE_NAME_INVALID);
    }
  }

  @Test(dependsOnMethods = {"testUploadArtifact"})
  void testUpdateArtifact_CheckoutByOtherUser_Negative() {
    String invariantUUID = testArtifactAction.getActionInvariantUuId();
    ActionArtifact artifactToUpdate = new ActionArtifact();
    artifactToUpdate.setArtifactUuId(actionArtifact.getArtifactUuId());
    artifactToUpdate.setArtifactLabel("CheckoutbyOtherUser label");

    try {
      actionManager.updateArtifact(artifactToUpdate, invariantUUID, USER2);
    } catch (ActionException actionException) {
      Assert
          .assertEquals(actionException.getErrorCode(), ACTION_EDIT_ON_ENTITY_LOCKED_BY_OTHER_USER);
      Assert.assertEquals(actionException.getDescription(),
          "Versionable entity Action with id " + invariantUUID +
              " can not be updated since it is locked by other user " + USER1 + ".");
    }
    System.out.println("asdf");
  }

  @Test(dependsOnMethods = {"testUploadArtifact"})
  void testUpdateArtifact_ArtifactProtectionReadOnly_CanNotUpdate_Negative() {
    String invariantUUID = testArtifactAction.getActionInvariantUuId();
    ActionArtifact artifactToUpdate = new ActionArtifact();
    artifactToUpdate.setArtifactUuId(actionArtifact.getArtifactUuId());
    artifactToUpdate.setArtifactProtection(ActionArtifactProtection.readOnly.name());
    actionManager.updateArtifact(artifactToUpdate, invariantUUID, USER1);

    artifactToUpdate.setArtifactLabel("test label");
    artifactToUpdate.setArtifactDescription("test description");
    artifactToUpdate.setArtifactProtection(ActionArtifactProtection.readWrite.name());
    try {
      actionManager.updateArtifact(artifactToUpdate, invariantUUID, USER1);
    } catch (ActionException actionExecption) {
      Assert.assertEquals(actionExecption.getDescription(), ACTION_ARTIFACT_UPDATE_READ_ONLY_MSG);
    }
  }

  // Function which will take action as input string and create action
  // After create multiple versions of same action
  // Final versions :1.0, 2.0
  // Last minor version :2.2
  // Candidate version :2.3
  private void createActionVersions(String input) {
    Action action1 = createAction(input);
    Action action = actionManager.createAction(action1, USER1);
    String Id = action.getActionInvariantUuId();

    actionManager.checkin(Id, USER1);
    actionManager.submit(Id, USER1); // 1.0
    actionManager.checkout(Id, USER1);
    actionManager.checkin(Id, USER1);
    actionManager.submit(Id, USER1);//2.0
    actionManager.checkout(Id, USER1);
    actionManager.checkin(Id, USER1);
    actionManager.checkout(Id, USER1);
    actionManager.checkin(Id, USER1); //2.2
    actionManager.checkout(Id, USER1); //2.3 candidate
  }

  private Action updateData(Action action) {
    Map<String, String> dataMap = new LinkedHashMap<>();
    dataMap.put(ActionConstants.UNIQUE_ID, action.getActionUuId());
    dataMap.put(ActionConstants.VERSION, action.getVersion());
    dataMap.put(ActionConstants.INVARIANTUUID, action.getActionInvariantUuId());
    dataMap.put(ActionConstants.STATUS, ActionStatus.Locked.name());

    String data = action.getData();
    Map<String, String> currentDataMap = JsonUtil.json2Object(data, LinkedHashMap.class);
    dataMap.putAll(currentDataMap);
    data = JsonUtil.object2Json(dataMap);
    action.setData(data);
    return action;
  }

  private void initDeleteActionTest() {
    deleteAction = actionManager.createAction(createAction(ACTION_TEST_DELETE), USER1);
    String deleteActionInvariantId = deleteAction.getActionInvariantUuId();
    actionManager.checkin(deleteActionInvariantId, USER1);
    actionManager.submit(deleteActionInvariantId, USER1); // 1.0
    actionManager.checkout(deleteActionInvariantId, USER1);
    actionManager.checkin(deleteActionInvariantId, USER1);
    actionManager.submit(deleteActionInvariantId, USER1);//2.0
    actionManager.checkout(deleteActionInvariantId, USER1);
  }

  private int getEffectiveVersion(String actionVersion) {
    Version version = Version.valueOf(actionVersion);
    return version.getMajor() * 10000 + version.getMinor();
  }

  private String generateActionArtifactUUID(Action action, String artifactName) {
    int effectiveVersion = getEffectiveVersion(action.getVersion());
    //Upper case for maintaining case-insensitive behavior for the artifact names
    String artifactUUIDString =
        action.getName().toUpperCase() + effectiveVersion + artifactName.toUpperCase();
    String generateArtifactUUID =
        UUID.nameUUIDFromBytes((artifactUUIDString).getBytes()).toString();
    String artifactUUID = generateArtifactUUID.replace("-", "");
    return artifactUUID.toUpperCase();
  }

  */

}
