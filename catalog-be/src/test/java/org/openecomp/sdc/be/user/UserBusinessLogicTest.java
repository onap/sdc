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

package org.openecomp.sdc.be.user;

import com.google.common.collect.Lists;
import fj.data.Either;
import org.apache.commons.lang3.StringUtils;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.util.detached.DetachedEdge;
import org.javatuples.Pair;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.sdc.be.components.impl.ResponseFormatManager;
import org.openecomp.sdc.be.components.impl.exceptions.ByActionStatusComponentException;
import org.openecomp.sdc.be.components.impl.exceptions.ComponentException;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.utils.UserStatusEnum;
import org.openecomp.sdc.be.facade.operations.UserOperation;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.operations.StorageException;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.UserAdminOperation;
import org.openecomp.sdc.common.api.ConfigurationSource;
import org.openecomp.sdc.common.api.UserRoleEnum;
import org.openecomp.sdc.common.datastructure.UserContext;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.openecomp.sdc.common.impl.FSConfigurationSource;
import org.openecomp.sdc.common.util.ThreadLocalsHolder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UserBusinessLogicTest {

    private static final String MOCK_MAIL = "mock@mock.mock";
    private static final String MOCK_MODIFIER = "mockModif";
    private static final String MOCK_NEW_USER = "mockNewUs";
    @Mock
    private UserAdminOperation userAdminOperation;
    @Mock
    private ComponentsUtils componentsUtils;
    @Mock
    private UserOperation facadeUserOperation;
    @Mock
    private User user;
    @Mock
    private User userNull;
    @InjectMocks
    private UserBusinessLogic testSubject;

    static ResponseFormatManager responseFormatManager = new ResponseFormatManager();
    private static ConfigurationSource configurationSource = new FSConfigurationSource(ExternalConfiguration.getChangeListener(), "src/test/resources/config/catalog-be");
    static ConfigurationManager configurationManager = new ConfigurationManager(configurationSource);

    @Before
    public void setUp() {
        doThrow(new ByActionStatusComponentException(ActionStatus.GENERAL_ERROR)).when(componentsUtils).auditAdminUserActionAndThrowException(any(), any(), any(), any(), any(), any());
    }

    @Test(expected = ComponentException.class)
    public void testCreateUserErrorGetUser() {
        User newUser = new User();
        when(userAdminOperation.getUserData(Mockito.anyString(), Mockito.anyBoolean())).thenReturn(Either.right(ActionStatus.USER_NOT_FOUND));
        // default test
        testSubject.createUser("mock", newUser);

        verify(facadeUserOperation, never()).updateUserCache(any(UserOperationEnum.class), anyString(), anyString());
    }

//    @Test(expected = ComponentException.class)
//    public void testGetUserContextNull() {
//        testSubject.getUser("userId");
//    }

    @Test(expected = ComponentException.class)
    public void testGetUserContextIdEmpty() {
        UserContext userContext = new UserContext(null);
        ThreadLocalsHolder.setUserContext(userContext);

        testSubject.getUser(null);
    }

    @Test
    public void testGetUserContext() {
        UserContext originalUserContext = ThreadLocalsHolder.getUserContext();
        String userId = "userId";
        Set<String> userRoles = new HashSet<>();
        userRoles.add(Role.DESIGNER.name());
        UserContext userContext = new UserContext(userId, userRoles, "test" ,"User");

        User user = new User();
        user.setUserId(userId);
        user.setRole(Role.DESIGNER.name());
        user.setStatus(UserStatusEnum.ACTIVE);
        user.setFirstName("test");
        user.setLastName("User");

        getAndValidateUser(originalUserContext, userId, userContext, user);
    }

    private void getAndValidateUser(UserContext originalUserContext, String userId, UserContext userContext, User user) {
        try {
            ThreadLocalsHolder.setUserContext(userContext);
            User convertedUser = testSubject.getUser(userId);
            assertThat(convertedUser).isEqualTo(user);
        }
        finally {
            ThreadLocalsHolder.setUserContext(originalUserContext);
        }
    }

    @Test
    public void testGetUserContextInActive() {
        UserContext originalUserContext = ThreadLocalsHolder.getUserContext();
        String userId = "userId";
        //Set<String> userRoles = new HashSet<>();
        //userRoles.add(Role.DESIGNER.name());
        UserContext userContext = new UserContext(userId, null, "test" ,"User");

        User user = new User();
        user.setUserId(userId);
        user.setRole(null);
        user.setStatus(UserStatusEnum.INACTIVE);
        user.setFirstName("test");
        user.setLastName("User");

        getAndValidateUser(originalUserContext, userId, userContext, user);
    }

    @Test(expected = ComponentException.class)
    public void testCreateUserErrorUserNotAdmin() {
        User newUser = new User();
        User userFromDb = new User();
        userFromDb.setRole(UserRoleEnum.DESIGNER.getName());
        when(userAdminOperation.getUserData(Mockito.anyString(), Mockito.anyBoolean())).thenReturn(Either.left(userFromDb));
        testSubject.createUser("mock", newUser);

        verify(facadeUserOperation, never()).updateUserCache(any(UserOperationEnum.class), anyString(), anyString());
    }

    @Test(expected = ComponentException.class)
    public void testCreateErrorCheckingNewUser() {
        User newUser = new User(MOCK_NEW_USER);
        User modifierFromDb = new User(MOCK_MODIFIER);
        modifierFromDb.setRole(UserRoleEnum.ADMIN.getName());
        when(userAdminOperation.getUserData(MOCK_MODIFIER, false)).thenReturn(Either.left(modifierFromDb));
        when(userAdminOperation.getUserData(MOCK_NEW_USER, false)).thenReturn(Either.right(ActionStatus.AUTH_REQUIRED));

        // default test
        testSubject.createUser(MOCK_MODIFIER, newUser);

        verify(facadeUserOperation, never()).updateUserCache(any(UserOperationEnum.class), anyString(), anyString());
    }

    @Test(expected = ComponentException.class)
    public void testCreateErrorCheckingNewUser2() {
        User newUser = new User(MOCK_NEW_USER);
        User modifierFromDb = new User(MOCK_MODIFIER);
        modifierFromDb.setRole(UserRoleEnum.ADMIN.getName());
        when(userAdminOperation.getUserData(MOCK_MODIFIER, false)).thenReturn(Either.left(modifierFromDb));
        when(userAdminOperation.getUserData(MOCK_NEW_USER, false)).thenReturn(Either.right(ActionStatus.USER_ALREADY_EXIST));

        // default test
        testSubject.createUser(MOCK_MODIFIER, newUser);

        verify(facadeUserOperation, never()).updateUserCache(any(UserOperationEnum.class), anyString(), anyString());
    }

    @Test(expected = ComponentException.class)
    public void testCreate_userExists_fails() {
        User newUser = new User(MOCK_NEW_USER);
        User modifierFromDb = new User(MOCK_MODIFIER);
        modifierFromDb.setRole(UserRoleEnum.ADMIN.getName());
        User userFromDb2 = new User();
        when(userAdminOperation.getUserData(MOCK_MODIFIER, false)).thenReturn(Either.left(modifierFromDb));
        when(userAdminOperation.getUserData(MOCK_NEW_USER, false)).thenReturn(Either.left(userFromDb2));
        testSubject.createUser(MOCK_MODIFIER, newUser);

        verify(facadeUserOperation, never()).updateUserCache(any(UserOperationEnum.class), anyString(), anyString());
    }

    @Test(expected = ComponentException.class)
    public void testCreateInvalidMail() {
        User newUser = new User(MOCK_NEW_USER);
        newUser.setEmail("mock");

        User userFromDbAdmin = new User(MOCK_MODIFIER);
        userFromDbAdmin.setRole(UserRoleEnum.ADMIN.getName());

        User userFromDbNew = new User();
        userFromDbNew.setStatus(UserStatusEnum.INACTIVE);
        when(userAdminOperation.getUserData(MOCK_MODIFIER, false)).thenReturn(Either.left(userFromDbAdmin));
        when(userAdminOperation.getUserData(MOCK_NEW_USER, false)).thenReturn(Either.left(userFromDbNew));
        testSubject.createUser(MOCK_MODIFIER, newUser);

        verify(facadeUserOperation, never()).updateUserCache(any(UserOperationEnum.class), anyString(), anyString());
    }

    @Test(expected = ComponentException.class)
    public void testCreateInvalidRole() {
        User newUser = new User(MOCK_NEW_USER);
        newUser.setEmail(MOCK_MAIL);
        newUser.setRole("mock");

        User userFromDbAdmin = new User(MOCK_MODIFIER);
        userFromDbAdmin.setRole(UserRoleEnum.ADMIN.getName());
        User userFromDbNew = new User();
        userFromDbNew.setStatus(UserStatusEnum.INACTIVE);
        when(userAdminOperation.getUserData(MOCK_MODIFIER, false)).thenReturn(Either.left(userFromDbAdmin));
        when(userAdminOperation.getUserData(MOCK_NEW_USER, false)).thenReturn(Either.left(userFromDbNew));
        testSubject.createUser(MOCK_MODIFIER, newUser);

        verify(facadeUserOperation, never()).updateUserCache(any(UserOperationEnum.class), anyString(), anyString());
    }

    @Test
    public void testCreateValidUser() throws Exception {
        User newUser = new User();
        newUser.setUserId(MOCK_NEW_USER);
        newUser.setEmail(MOCK_MAIL);
        newUser.setRole(UserRoleEnum.DESIGNER.name());

        User userFromDbAdmin = new User();
        userFromDbAdmin.setUserId(MOCK_MODIFIER);
        userFromDbAdmin.setRole(UserRoleEnum.ADMIN.getName());
        Either<User, ActionStatus> value = Either.left(userFromDbAdmin);

        User userFromDbNew = new User();
        userFromDbNew.setStatus(UserStatusEnum.INACTIVE);
        Either<User, ActionStatus> value2 = Either.right(ActionStatus.USER_NOT_FOUND);
        when(userAdminOperation.getUserData(MOCK_MODIFIER, false)).thenReturn(value);
        when(userAdminOperation.getUserData(MOCK_NEW_USER, false)).thenReturn(value2);
        when(userAdminOperation.saveUserData(newUser)).thenReturn(newUser);

        // test
        User resultUser = testSubject.createUser(MOCK_MODIFIER, newUser);
        assertThat(resultUser).isEqualTo(newUser);

        verify(facadeUserOperation).updateUserCache(UserOperationEnum.CREATE, newUser.getUserId(), newUser.getRole());
    }

    @Test(expected =  ComponentException.class)
    public void testUpdateUserRoleNotFound() {
        User modifier = new User(MOCK_MODIFIER);
        String userIdToUpdate = "";
        String userRole = "";

        when(userAdminOperation.getUserData(modifier.getUserId(), false)).thenReturn(Either.right(ActionStatus.USER_NOT_FOUND));
        testSubject.updateUserRole(MOCK_MODIFIER, userIdToUpdate, userRole);

        verify(facadeUserOperation, never()).updateUserCache(any(UserOperationEnum.class), anyString(), anyString());
    }

    @Test(expected =  ComponentException.class)
    public void testUpdateUserRoleModifierWrongRole() {
        User modifier = new User(MOCK_MODIFIER);
        modifier.setRole(UserRoleEnum.DESIGNER.getName());
        String userIdToUpdate = "";
        String userRole = "";

        when(userAdminOperation.getUserData(modifier.getUserId(), false)).thenReturn(Either.left(modifier));
        testSubject.updateUserRole(MOCK_MODIFIER, userIdToUpdate, userRole);

        verify(facadeUserOperation, never()).updateUserCache(any(UserOperationEnum.class), anyString(), anyString());
    }

    @Test(expected =  ComponentException.class)
    public void testUpdateUserRoleSameId() {
        User modifier = new User(MOCK_MODIFIER);
        modifier.setRole(UserRoleEnum.ADMIN.getName());
        String userRole = "";

        when(userAdminOperation.getUserData(modifier.getUserId(), false)).thenReturn(Either.left(modifier));
        testSubject.updateUserRole(MOCK_MODIFIER, MOCK_MODIFIER, userRole);

        verify(facadeUserOperation, never()).updateUserCache(any(UserOperationEnum.class), anyString(), anyString());
    }

    @Test(expected =  ComponentException.class)
    public void testUpdateUserRoleUpdatedNotFound() {
        User modifier = new User(MOCK_MODIFIER);
        modifier.setRole(UserRoleEnum.ADMIN.getName());
        String userIdToUpdate = "mock1";
        String userRole = "";

        when(userAdminOperation.getUserData(modifier.getUserId(), false)).thenReturn(Either.left(modifier));
        when(userAdminOperation.getUserData(userIdToUpdate, false)).thenReturn(Either.right(ActionStatus.ECOMP_USER_NOT_FOUND));

        testSubject.updateUserRole(MOCK_MODIFIER, userIdToUpdate, userRole);

        verify(facadeUserOperation, never()).updateUserCache(any(UserOperationEnum.class), anyString(), anyString());
    }

    @Test(expected =  ComponentException.class)
    public void testUpdateUserRoleUpdatedToInvalidRole() {
        User modifier = new User(MOCK_MODIFIER);
        modifier.setRole(UserRoleEnum.ADMIN.getName());
        String userIdToUpdate = "mock1";
        String userRole = "";

        when(userAdminOperation.getUserData(modifier.getUserId(), false)).thenReturn(Either.left(modifier));
        when(userAdminOperation.getUserData(userIdToUpdate, false)).thenReturn(Either.left(modifier));

        testSubject.updateUserRole(MOCK_MODIFIER, userIdToUpdate, userRole);

        verify(facadeUserOperation, never()).updateUserCache(any(UserOperationEnum.class), anyString(), anyString());
    }

    @Test(expected =  StorageException.class)
    public void testUpdateUserRolePendingTaskFetchFailed() {
        User modifier = new User(MOCK_MODIFIER);
        modifier.setRole(UserRoleEnum.ADMIN.getName());
        String userIdToUpdate = "mock1";
        String userRole = UserRoleEnum.DESIGNER.getName();

        User updatedUser = new User();
        updatedUser.setUserId(userIdToUpdate);
        updatedUser.setRole(UserRoleEnum.TESTER.getName());

        when(userAdminOperation.getUserData(modifier.getUserId(), false)).thenReturn(Either.left(modifier));
        when(userAdminOperation.getUserData(userIdToUpdate, false)).thenReturn(Either.left(updatedUser));
        when(userAdminOperation.getUserPendingTasksList(Mockito.any(), Mockito.any())).thenThrow(new StorageException(StorageOperationStatus.INCONSISTENCY));

        // default test
        testSubject.updateUserRole(MOCK_MODIFIER, userIdToUpdate, userRole);

        verify(facadeUserOperation, never()).updateUserCache(any(UserOperationEnum.class), anyString(), anyString());
    }

    @Test
    public void testUpdateTesterRole_taskStateCriteriaShouldBeEmpty_shouldSucceed() {
        User modifier = new User(MOCK_MODIFIER);
        modifier.setRole(UserRoleEnum.ADMIN.getName());
        String userIdToUpdate = "mock1";
        String userRole = UserRoleEnum.DESIGNER.getName();

        User updatedUser = new User(userIdToUpdate);
        updatedUser.setRole(UserRoleEnum.TESTER.getName());

        User newUser = new User();
        newUser.setUserId(userIdToUpdate);
        newUser.setRole(UserRoleEnum.DESIGNER.getName());
        List<Object> testerState = new ArrayList<>();
        when(userAdminOperation.getUserData(modifier.getUserId(), false)).thenReturn(Either.left(modifier));
        when(userAdminOperation.getUserData(userIdToUpdate, false)).thenReturn(Either.left(updatedUser));
        when(userAdminOperation.getUserPendingTasksList(eq(updatedUser), eq(testerState))).thenReturn(new LinkedList<>());
        when(userAdminOperation.updateUserData(newUser)).thenReturn(updatedUser);

        // default test
        User user = testSubject.updateUserRole(MOCK_MODIFIER, userIdToUpdate, userRole);
        assertThat(user).isEqualToComparingFieldByField(updatedUser);

        verify(facadeUserOperation).updateUserCache(UserOperationEnum.CHANGE_ROLE, userIdToUpdate, UserRoleEnum.TESTER.name());
    }

    @Test(expected =  ComponentException.class)
    public void testUpdateDesignerRoleListOfTasksNotEmpty_shouldFail() {
        User modifier = new User(MOCK_MODIFIER);
        modifier.setRole(UserRoleEnum.ADMIN.getName());
        String userIdToUpdate = "mock1";
        String userRole = UserRoleEnum.TESTER.getName();

        User updatedUser = new User();
        updatedUser.setUserId(userIdToUpdate);
        updatedUser.setRole(UserRoleEnum.DESIGNER.getName());

        List<Object> designerState = new ArrayList<>();
        designerState.add(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT.name());

        when(userAdminOperation.getUserData(modifier.getUserId(), false)).thenReturn(Either.left(modifier));
        when(userAdminOperation.getUserData(userIdToUpdate, false)).thenReturn(Either.left(updatedUser));
        List<Edge> list = new LinkedList<>();
        list.add(new DetachedEdge("sdas", "fdfs", new HashMap<>(), Pair.with("sadas", "sadasd"), "",
                Pair.with("sadas", "sadasd"), ""));
        testSubject.updateUserRole(MOCK_MODIFIER, userIdToUpdate, userRole);

        verify(facadeUserOperation, never()).updateUserCache(any(UserOperationEnum.class), anyString(), anyString());
    }

    @Test(expected =  StorageException.class)
    public void testUpdateUserRoleStorageError_shouldFail() {
        User modifier = new User(MOCK_MODIFIER);
        modifier.setRole(UserRoleEnum.ADMIN.getName());
        String userIdToUpdate = "mock1";
        String userRole = UserRoleEnum.DESIGNER.getName();

        User updatedUser = new User(userIdToUpdate);
        updatedUser.setRole(UserRoleEnum.TESTER.getName());

        when(userAdminOperation.getUserData(modifier.getUserId(), false)).thenReturn(Either.left(modifier));
        when(userAdminOperation.getUserData(userIdToUpdate, false)).thenReturn(Either.left(updatedUser));
        when(userAdminOperation.getUserPendingTasksList(Mockito.any(), Mockito.any())).thenReturn(new LinkedList<>());
        when(userAdminOperation.updateUserData(Mockito.any())).thenThrow(new StorageException(StorageOperationStatus.INCONSISTENCY));
        // default test
        testSubject.updateUserRole(MOCK_MODIFIER, userIdToUpdate, userRole);

        verify(facadeUserOperation, never()).updateUserCache(any(UserOperationEnum.class), anyString(), anyString());
    }

    @Test
    public void testUpdateUserRoleEmptyTaskList_shouldSucceed() {
        User modifier = new User(MOCK_MODIFIER);
        modifier.setRole(UserRoleEnum.ADMIN.getName());
        String userIdToUpdate = "mock1";
        String userRole = UserRoleEnum.DESIGNER.getName();

        User updatedUser = new User();
        updatedUser.setUserId(userIdToUpdate);
        updatedUser.setRole(UserRoleEnum.TESTER.getName());

        when(userAdminOperation.getUserData(modifier.getUserId(), false)).thenReturn(Either.left(modifier));
        when(userAdminOperation.getUserData(userIdToUpdate, false)).thenReturn(Either.left(updatedUser));
        when(userAdminOperation.getUserPendingTasksList(Mockito.any(), Mockito.any())).thenReturn(new LinkedList<>());
        when(userAdminOperation.updateUserData(Mockito.any())).thenReturn(updatedUser);
        testSubject.updateUserRole(MOCK_MODIFIER, userIdToUpdate, userRole);

        verify(facadeUserOperation).updateUserCache(UserOperationEnum.CHANGE_ROLE, userIdToUpdate, UserRoleEnum.TESTER.name());
    }

    @Test(expected = ComponentException.class)
    public void testUpdateRoleToTester_shouldFail() {
        User modifier = new User(MOCK_MODIFIER);
        modifier.setRole(UserRoleEnum.ADMIN.getName());
        String userIdToUpdate = "mock1";
        String userRole = UserRoleEnum.TESTER.getName();

        User updatedUser = new User();
        updatedUser.setUserId(userIdToUpdate);
        updatedUser.setRole(UserRoleEnum.TESTER.getName());

        when(userAdminOperation.getUserData(modifier.getUserId(), false)).thenReturn(Either.left(modifier));
        when(userAdminOperation.getUserData(userIdToUpdate, false)).thenReturn(Either.left(updatedUser));
        testSubject.updateUserRole(MOCK_MODIFIER, userIdToUpdate, userRole);
    }

    @Test
    public void testGetAllAdminUsers() {
        Either<List<User>, ActionStatus> response = Either.left(new LinkedList<>());
        when(userAdminOperation.getAllUsersWithRole(anyString(), Mockito.nullable(String.class)))
                .thenReturn(response);
        assertEquals(0, testSubject.getAllAdminUsers().size());
    }

    @Test(expected = ComponentException.class)
    public void testGetAllAdminUsersFail() {
        Either<List<User>, ActionStatus> response = Either.right(ActionStatus.NOT_ALLOWED);
        when(userAdminOperation.getAllUsersWithRole(anyString(), Mockito.nullable(String.class)))
                .thenReturn(response);
        testSubject.getAllAdminUsers();
    }

    @Test(expected = ComponentException.class)
    public void testGetUsersList1() {
        // test 1
        testSubject.getUsersList(null, null, "");
    }

    @Test(expected = ComponentException.class)
    public void testGetUsersListFail() {
        String modifierAttId = "mockMod";
        String rolesStr = "";

        Either<User, ActionStatus> value3 = Either.right(ActionStatus.ILLEGAL_COMPONENT_STATE);
        when(userAdminOperation.getUserData(modifierAttId, false)).thenReturn(value3);
        testSubject.getUsersList(modifierAttId, null, rolesStr);
    }

    @Test(expected = ComponentException.class)
    public void testGetUsersListFail2() {
        String modifierAttId = "mockMod";
        String rolesStr = "";

        Either<User, ActionStatus> value3 = Either.right(ActionStatus.USER_NOT_FOUND);
        when(userAdminOperation.getUserData(modifierAttId, false)).thenReturn(value3);
        testSubject.getUsersList(modifierAttId, null, rolesStr);
    }


    @Test
    public void testGetUsersList() {
        String modifierAttId = "mockMod";
        List<String> roles = new LinkedList<>();
        String rolesStr = "";

        User a = new User();
        Either<User, ActionStatus> value3 = Either.left(a);
        when(userAdminOperation.getUserData(modifierAttId, false)).thenReturn(value3);
        Either<List<User>, ActionStatus> value = Either.left(new LinkedList<>());
        when(userAdminOperation.getAllUsersWithRole(Mockito.nullable(String.class), anyString()))
                .thenReturn(value);

        assertEquals(0, testSubject.getUsersList(modifierAttId, roles, rolesStr).size());
    }

    @Test(expected = ComponentException.class)
    public void testGetUsersListInvalidRole() {
        String modifierAttId = "mockMod";
        List<String> roles = new LinkedList<>();
        roles.add("mock");
        String rolesStr = "";

        User a = new User();
        Either<User, ActionStatus> value3 = Either.left(a);
        when(userAdminOperation.getUserData(modifierAttId, false)).thenReturn(value3);
        testSubject.getUsersList(modifierAttId, roles, rolesStr);
    }

    @Test(expected = ComponentException.class)
    public void testAuthorizeMissingId() {
        User authUser = new User();
        testSubject.authorize(authUser);
    }

    @Test
    public void testGetUsersList2() {
        String modifierAttId = "mockMod";
        List<String> roles = new LinkedList<>();
        roles.add(UserRoleEnum.DESIGNER.name());
        String rolesStr = "";

        User a = new User();
        Either<User, ActionStatus> value3 = Either.left(a);
        when(userAdminOperation.getUserData(modifierAttId, false)).thenReturn(value3);
        Either<List<User>, ActionStatus> value = Either.left(new LinkedList<>());
        when(userAdminOperation.getAllUsersWithRole(Mockito.nullable(String.class), anyString()))
                .thenReturn(value);

        assertEquals(0, testSubject.getUsersList(modifierAttId, roles, rolesStr).size());
    }


    @Test(expected = ComponentException.class)
    public void testAuthorizeFail1() {
        User authUser = new User();
        authUser.setUserId("mockAU");

        Either<User, ActionStatus> value = Either.right(ActionStatus.USER_NOT_FOUND);
        when(userAdminOperation.getUserData("mockAU", false)).thenReturn(value);
        testSubject.authorize(authUser);
    }

    @Test(expected = ComponentException.class)
    public void testAuthorizeFail2() {
        User authUser = new User();
        authUser.setUserId("mockAU");

        Either<User, ActionStatus> value = Either.right(ActionStatus.AAI_ARTIFACT_GENERATION_FAILED);
        when(userAdminOperation.getUserData("mockAU", false)).thenReturn(value);
        testSubject.authorize(authUser);
    }

    @Test(expected = ComponentException.class)
    public void testAuthorizeFail3() {
        User authUser = new User();
        authUser.setUserId("mockAU");

        Either<User, ActionStatus> value = Either.left(null);
        when(userAdminOperation.getUserData("mockAU", false)).thenReturn(value);
        testSubject.authorize(authUser);
    }


    @Test
    public void testAuthorize5() {
        User authUser = new User();
        authUser.setUserId("mockAU");

        Either<User, ActionStatus> value = Either.left(authUser);
        when(userAdminOperation.getUserData("mockAU", false)).thenReturn(value);
        when(userAdminOperation.updateUserData(Mockito.any(User.class))).thenReturn(authUser);
        assertEquals(authUser.getUserId(), testSubject.authorize(authUser).getUserId());
    }

    @Test
    public void testUpdateUserCredentialsMissingId() {
        User updatedUserCred = new User();
        updatedUserCred.setUserId(null);
        assertTrue(testSubject.updateUserCredentials(updatedUserCred).isRight());
    }

    @Test(expected = ComponentException.class)
    public void testUpdateUserCredentialsFailedToGet() {
        User updatedUserCred = new User();
        updatedUserCred.setUserId("mock");

        Either<User, ActionStatus> value = Either.right(ActionStatus.USER_NOT_FOUND);
        when(userAdminOperation.getUserData("mock", false)).thenReturn(value);
        testSubject.updateUserCredentials(updatedUserCred);
    }

    @Test(expected = ComponentException.class)
    public void testUpdateUserCredentialsFailedToGet2() {
        User updatedUserCred = new User();
        updatedUserCred.setUserId("mock");

        Either<User, ActionStatus> value = Either.right(ActionStatus.ADDITIONAL_INFORMATION_ALREADY_EXISTS);
        when(userAdminOperation.getUserData("mock", false)).thenReturn(value);
        testSubject.updateUserCredentials(updatedUserCred);
    }

    @Test(expected = ComponentException.class)
    public void testUpdateUserCredentialsFailedToGet3() {
        User updatedUserCred = new User();
        updatedUserCred.setUserId("mock");

        Either<User, ActionStatus> value = Either.left(null);
        when(userAdminOperation.getUserData("mock", false)).thenReturn(value);
        testSubject.updateUserCredentials(updatedUserCred);
    }

    @Test
    public void testUpdateUserCredentials() {
        User updatedUserCred = new User();
        updatedUserCred.setUserId("mock");

        Either<User, ActionStatus> value = Either.left(updatedUserCred);
        when(userAdminOperation.getUserData("mock", false)).thenReturn(value);

        when(userAdminOperation.updateUserData(Mockito.any(User.class))).thenReturn(updatedUserCred);
        assertEquals(updatedUserCred.getUserId(),
                testSubject.updateUserCredentials(updatedUserCred).left().value().getUserId());
    }

    @Test
    public void getUsersPerRoleWhenListIsEmpty() {
        when(userAdminOperation.getAllUsersWithRole(any(), any()))
                .thenReturn(Either.left(Lists.newArrayList()));
        assertEquals(0, testSubject.getUsersPerRole("all", user, "").left().value().size());
    }

    @Test
    public void getUsersPerRoleWhenListHasMixedElements() {
        List<User> users = Lists.newArrayList(user, userNull);
        when(user.getUserId()).thenReturn("123");
        when(userNull.getUserId()).thenReturn(null);
        when(userAdminOperation.getAllUsersWithRole(any(), any()))
                .thenReturn(Either.left(users));
        List<User> result = testSubject.getUsersPerRole("all", user, "").left().value();

        assertEquals(1, result.size());
        assertTrue(StringUtils.isNotEmpty(result.get(0).getUserId()));
    }

    @Test
    public void getUsersPerRoleWhenListHasNoneNullElements() {
        List<User> users = Lists.newArrayList(user, user);
        when(user.getUserId()).thenReturn("123");
        when(userAdminOperation.getAllUsersWithRole(any(), any()))
                .thenReturn(Either.left(users));
        List<User> result = testSubject.getUsersPerRole("all", user, "").left().value();

        assertEquals(2, result.size());
        assertTrue(StringUtils.isNotEmpty(result.get(0).getUserId()) && StringUtils.isNotEmpty(result.get(1).getUserId()));
    }

    @Test
    public void getUsersPerRoleWhenListHasNullElements() {
        List<User> users = Lists.newArrayList(userNull);
        when(userNull.getUserId()).thenReturn(null);
        when(userAdminOperation.getAllUsersWithRole(any(), any()))
                .thenReturn(Either.left(users));
        List<User> result = testSubject.getUsersPerRole("all", user, "").left().value();

        assertEquals(0, result.size());
    }

    @Test
    public void testHasActiveUserTrue() {
        UserContext originalUserContext = null;
        try {
            originalUserContext = ThreadLocalsHolder.getUserContext();
            String userId = "mock";
            Set<String> userRoles = new HashSet<>();
            userRoles.add(Role.DESIGNER.name());
            UserContext userContext = new UserContext(userId, userRoles, "test" ,"User");
            ThreadLocalsHolder.setUserContext(userContext);

            assertThat(testSubject.hasActiveUser(userId)).isTrue();
        } finally {
            ThreadLocalsHolder.setUserContext(originalUserContext);
        }
    }

    @Test
    public void testHasActiveUserFalseNoRoles() {
        UserContext originalUserContext = null;
        try {
            originalUserContext = ThreadLocalsHolder.getUserContext();
            String userId = "mock";
            Set<String> userRoles = new HashSet<>();
            UserContext userContext = new UserContext(userId, userRoles, "test" ,"User");
            ThreadLocalsHolder.setUserContext(userContext);

            assertThat(testSubject.hasActiveUser(userId)).isFalse();
        } finally {
            ThreadLocalsHolder.setUserContext(originalUserContext);
        }
    }

    @Test
    public void testHasActiveUserFalseNullUserContext() {
        UserContext originalUserContext = null;
        try {
            originalUserContext = ThreadLocalsHolder.getUserContext();
            ThreadLocalsHolder.setUserContext(null);

            assertThat(testSubject.hasActiveUser(null)).isFalse();
        } finally {
            ThreadLocalsHolder.setUserContext(originalUserContext);
        }
    }
}
