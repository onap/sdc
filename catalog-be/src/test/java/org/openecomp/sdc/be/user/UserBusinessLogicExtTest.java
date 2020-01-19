/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2020 AT&T Intellectual Property. All rights reserved.
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


import fj.data.Either;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.sdc.be.auditing.impl.AuditingManager;
import org.openecomp.sdc.be.components.impl.exceptions.ComponentException;
import org.openecomp.sdc.be.components.lifecycle.LifecycleBusinessLogic;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.utils.UserStatusEnum;
import org.openecomp.sdc.be.exception.ComponentExceptionMatcher;
import org.openecomp.sdc.be.facade.operations.UserOperation;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentMetadataDefinition;
import org.openecomp.sdc.be.model.LifeCycleTransitionEnum;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.ResourceMetadataDefinition;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.operations.StorageException;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.UserAdminOperation;
import org.openecomp.sdc.common.api.ConfigurationSource;
import org.openecomp.sdc.common.api.UserRoleEnum;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.openecomp.sdc.common.impl.FSConfigurationSource;
import org.openecomp.sdc.exception.ResponseFormat;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UserBusinessLogicExtTest {

    private static final String MOCK_MODIFIER = "mockMod";
    private static final String ID1 = "A";
    private static final String ID2 = "B";
    private UserBusinessLogicExt testSubject;

    @Mock
    private UserAdminOperation userAdminOperation;

    @Mock
    private LifecycleBusinessLogic lifecycleBusinessLogic;
    @Mock
    private static UserOperation facadeUserOperation;


    @Before
    public void setUp() {
        // init Configuration
        String appConfigDir = "src/test/resources/config/catalog-be";
        ConfigurationSource configurationSource = new FSConfigurationSource(ExternalConfiguration.getChangeListener(),
                appConfigDir);
        @SuppressWarnings("unused")
        ConfigurationManager configurationManager = new ConfigurationManager(configurationSource);
        ComponentsUtils componentsUtils = new ComponentsUtils(Mockito.mock(AuditingManager.class));
        UserBusinessLogic userBusinessLogic = new UserBusinessLogic(userAdminOperation, componentsUtils, facadeUserOperation);
        testSubject = new UserBusinessLogicExt(userBusinessLogic, userAdminOperation, lifecycleBusinessLogic,
                componentsUtils);
    }

    @Test(expected = ComponentException.class)
    public void testDeActivateUserMissingID() {
        testSubject.deActivateUser(null, "");
        
        verify(facadeUserOperation, never()).updateUserCache(any(UserOperationEnum.class), anyString(), anyString());  
    }

    @Test(expected = ComponentException.class)
    public void testDeActivateUserModifierNotFound() {
        String userUniqueIdToDeactivate = "";
        when(userAdminOperation.getUserData(MOCK_MODIFIER, false)).thenReturn(Either.right(ActionStatus.USER_NOT_FOUND));
        testSubject.deActivateUser(MOCK_MODIFIER, userUniqueIdToDeactivate);
        
        verify(facadeUserOperation, never()).updateUserCache(any(UserOperationEnum.class), anyString(), anyString());
    }

    @Test(expected = ComponentException.class)
    public void testDeActivateUserModNotAdmin() {
        User modifier = new User(MOCK_MODIFIER);
        modifier.setRole(UserRoleEnum.DESIGNER.getName());
        String userUniqueIdToDeactivate = "";
        when(userAdminOperation.getUserData(MOCK_MODIFIER, false)).thenReturn(Either.left(modifier));
        testSubject.deActivateUser(MOCK_MODIFIER, userUniqueIdToDeactivate);
        
        verify(facadeUserOperation, never()).updateUserCache(any(UserOperationEnum.class), anyString(), anyString());  
    }

    @Test(expected = ComponentException.class)
    public void testDeActivateUserDeactivatedUserNotFound() {
        User modifier = new User(MOCK_MODIFIER);
        modifier.setRole(UserRoleEnum.ADMIN.getName());
        String userUniqueIdToDeactivate = "mockDU";

        when(userAdminOperation.getUserData(MOCK_MODIFIER, false)).thenReturn(Either.left(modifier));
        when(userAdminOperation.getUserData("mockDU", false)).thenReturn(Either.right(ActionStatus.USER_NOT_FOUND));
        testSubject.deActivateUser(MOCK_MODIFIER, userUniqueIdToDeactivate);
        
        verify(facadeUserOperation, never()).updateUserCache(any(UserOperationEnum.class), anyString(), anyString());  
    }

    @Test(expected = ComponentException.class)
    public void testDeActivateUser_DeactivatedAndModifierAreSame() {
        User modifier = new User(MOCK_MODIFIER);
        modifier.setRole(UserRoleEnum.ADMIN.getName());

        when(userAdminOperation.getUserData(MOCK_MODIFIER, false)).thenReturn(Either.left(modifier));
        testSubject.deActivateUser(MOCK_MODIFIER, MOCK_MODIFIER);
        
        verify(facadeUserOperation, never()).updateUserCache(any(UserOperationEnum.class), anyString(), anyString());  
    }

    @Test(expected = StorageException.class)
    public void testDeActivateUserFailToGetTasks() {
        User modifier = new User(MOCK_MODIFIER);
        modifier.setRole(UserRoleEnum.ADMIN.getName());
        String userUniqueIdToDeactivate = "mockDU";
        User deacUser = new User();
        deacUser.setStatus(UserStatusEnum.ACTIVE);
        deacUser.setRole(UserRoleEnum.DESIGNER.name());

        when(userAdminOperation.getUserData(MOCK_MODIFIER, false)).thenReturn(Either.left(modifier));
        when(userAdminOperation.getUserData("mockDU", false)).thenReturn(Either.left(deacUser));
        when(userAdminOperation.getUserActiveComponents(any(), any())).thenThrow(new StorageException(StorageOperationStatus.INCONSISTENCY));
        testSubject.deActivateUser(MOCK_MODIFIER, userUniqueIdToDeactivate);
        
        verify(facadeUserOperation, never()).updateUserCache(any(UserOperationEnum.class), anyString(), anyString());  
    }

    @Test
    public void testDeActivateUserWithPendingTasks_verifyActionsWereDone() {
        User modifier = new User();
        modifier.setUserId(MOCK_MODIFIER);
        modifier.setRole(UserRoleEnum.ADMIN.getName());
        String userUniqueIdToDeactivate = "mockDU";
        User userToDeactivate = new User();
        userToDeactivate.setStatus(UserStatusEnum.ACTIVE);
        userToDeactivate.setRole(UserRoleEnum.DESIGNER.name());

        when(userAdminOperation.getUserData(MOCK_MODIFIER, false)).thenReturn(Either.left(modifier));
        when(userAdminOperation.getUserData("mockDU", false)).thenReturn(Either.left(userToDeactivate));
        List<Component> components = new ArrayList<>();
        Resource componentCheckedOut = createComponent(ID1, LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
        components.add(componentCheckedOut);
        when(userAdminOperation.getUserActiveComponents(any(), any())).thenReturn(components);
        doReturn(Either.left(componentCheckedOut)).when(lifecycleBusinessLogic).changeComponentState(any(), eq(ID1), eq(userToDeactivate), eq(LifeCycleTransitionEnum.CHECKIN),
                any(), eq(false), eq(true));
        when(userAdminOperation.deActivateUser(userToDeactivate)).thenReturn(userToDeactivate);
        testSubject.deActivateUser(MOCK_MODIFIER, userUniqueIdToDeactivate);
        verify(userAdminOperation, times(1)).deActivateUser(userToDeactivate);
        
        verify(facadeUserOperation, never()).updateUserCache(any(UserOperationEnum.class), anyString(), anyString());  
    }

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testDeActivateUserWithPendingTasks_FailToCheckIn_shouldFail() {
        User modifier = new User();
        modifier.setUserId(MOCK_MODIFIER);
        modifier.setRole(UserRoleEnum.ADMIN.getName());
        String userUniqueIdToDeactivate = "mockDU";
        User deacUser = new User();
        deacUser.setStatus(UserStatusEnum.ACTIVE);
        deacUser.setRole(UserRoleEnum.DESIGNER.name());

        when(userAdminOperation.getUserData(MOCK_MODIFIER, false)).thenReturn(Either.left(modifier));
        when(userAdminOperation.getUserData("mockDU", false)).thenReturn(Either.left(deacUser));
        List<Component> components = new ArrayList<>();
        Component componentCheckedOut = createComponent(ID1, LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
        components.add(componentCheckedOut);
        when(userAdminOperation.getUserActiveComponents(any(), any())).thenReturn(components);
        when(lifecycleBusinessLogic.changeComponentState(any(), eq(ID1), eq(deacUser), eq(LifeCycleTransitionEnum.CHECKIN), any(), eq(false), eq(true)))
                .thenReturn(Either.right(new ResponseFormat()));
        thrown.expect(ComponentException.class);
        thrown.expect(ComponentExceptionMatcher.hasStatus("SVC4569"));
        testSubject.deActivateUser(MOCK_MODIFIER, userUniqueIdToDeactivate);
        
        verify(facadeUserOperation, never()).updateUserCache(any(UserOperationEnum.class), anyString(), anyString());  
    }

    @Test
    public void testDeActivateUserWithPendingTasks_FailToCertify_shouldFail() {
        User modifier = new User();
        modifier.setUserId(MOCK_MODIFIER);
        modifier.setRole(UserRoleEnum.ADMIN.getName());
        String userUniqueIdToDeactivate = "mockDU";
        User deacUser = new User();
        deacUser.setStatus(UserStatusEnum.ACTIVE);
        deacUser.setRole(UserRoleEnum.DESIGNER.name());

        when(userAdminOperation.getUserData(MOCK_MODIFIER, false)).thenReturn(Either.left(modifier));
        when(userAdminOperation.getUserData("mockDU", false)).thenReturn(Either.left(deacUser));
        List<Component> components = new ArrayList<>();
        Component componentCheckedOut = createComponent(ID1, LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
        components.add(componentCheckedOut);
        when(userAdminOperation.getUserActiveComponents(any(), any())).thenReturn(components);
        when(lifecycleBusinessLogic.changeComponentState(any(), eq(ID1), eq(deacUser), eq(LifeCycleTransitionEnum.CHECKIN), any(), eq(false), eq(true)))
                .thenReturn(Either.right(new ResponseFormat()));
        thrown.expect(ComponentException.class);
        thrown.expect(ComponentExceptionMatcher.hasStatus("SVC4569"));
        testSubject.deActivateUser(MOCK_MODIFIER, userUniqueIdToDeactivate);
        
        verify(facadeUserOperation, never()).updateUserCache(any(UserOperationEnum.class), anyString(), anyString());  
    }

    private Resource createComponent(String id, LifecycleStateEnum state) {
        ComponentMetadataDefinition componentMetadataDefinition = new ResourceMetadataDefinition();
        Resource resource = new Resource(componentMetadataDefinition);
        resource.setUniqueId(id);
        resource.setName(id);
        resource.setLifecycleState(state);
        return resource;
    }

    @Test(expected = StorageException.class)
    public void testDeActivateUserDeactivateFails() {
        User modifier = new User(MOCK_MODIFIER);
        modifier.setRole(UserRoleEnum.ADMIN.getName());
        String userUniqueIdToDeactivate = "mockDU";
        User deacUser = new User();
        deacUser.setStatus(UserStatusEnum.ACTIVE);
        deacUser.setRole(UserRoleEnum.DESIGNER.name());

        when(userAdminOperation.getUserData(MOCK_MODIFIER, false)).thenReturn(Either.left(modifier));
        when(userAdminOperation.getUserData("mockDU", false)).thenReturn(Either.left(deacUser));
        when(userAdminOperation.deActivateUser(deacUser)).thenThrow(new StorageException(StorageOperationStatus.BAD_REQUEST));
        testSubject.deActivateUser(MOCK_MODIFIER, userUniqueIdToDeactivate);
        
        verify(facadeUserOperation, never()).updateUserCache(any(UserOperationEnum.class), anyString(), anyString());  
    }

    @Test
    public void testDeActivateUser_noTasks_shouldSucceed() {

        User modifier = new User(MOCK_MODIFIER);
        modifier.setRole(UserRoleEnum.ADMIN.getName());
        String userUniqueIdToDeactivate = "mockDU";
        User deacUser = new User(userUniqueIdToDeactivate);
        deacUser.setStatus(UserStatusEnum.ACTIVE);
        deacUser.setRole(UserRoleEnum.DESIGNER.name());

        when(userAdminOperation.getUserData(MOCK_MODIFIER, false)).thenReturn(Either.left(modifier));
        when(userAdminOperation.getUserData("mockDU", false)).thenReturn(Either.left(deacUser));
        when(userAdminOperation.getUserActiveComponents(any(), any())).thenReturn(new LinkedList<>());
        when(userAdminOperation.deActivateUser(deacUser)).thenReturn(deacUser);
        User user = testSubject.deActivateUser(MOCK_MODIFIER, userUniqueIdToDeactivate);
        assertThat(user.getUserId()).isEqualTo(userUniqueIdToDeactivate);
        
        verify(facadeUserOperation).updateUserCache(UserOperationEnum.DEACTIVATE, deacUser.getUserId(), deacUser.getRole());        
    }

    @Test(expected = ComponentException.class)
    public void testDeActivateUser_AlreadyInactive()  {
        User modifier = new User(MOCK_MODIFIER);
        modifier.setRole(UserRoleEnum.ADMIN.getName());
        String userUniqueIdToDeactivate = "mockDU";
        User deacUser = new User();
        deacUser.setStatus(UserStatusEnum.INACTIVE);

        when(userAdminOperation.getUserData(MOCK_MODIFIER, false)).thenReturn(Either.left(modifier));
        when(userAdminOperation.getUserData("mockDU", false)).thenReturn(Either.left(deacUser));
        testSubject.deActivateUser(MOCK_MODIFIER, userUniqueIdToDeactivate);
        
        verify(facadeUserOperation, never()).updateUserCache(any(UserOperationEnum.class), anyString(), anyString());  
    }


}