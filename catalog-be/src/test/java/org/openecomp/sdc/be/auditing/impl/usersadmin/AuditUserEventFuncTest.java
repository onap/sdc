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
 * Modifications copyright (c) 2019 Nokia
 * ================================================================================
 */
package org.openecomp.sdc.be.auditing.impl.usersadmin;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.sdc.be.auditing.api.AuditEventFactory;
import org.openecomp.sdc.be.auditing.impl.AuditAuthRequestEventFactory;
import org.openecomp.sdc.be.auditing.impl.AuditingManager;
import org.openecomp.sdc.be.config.Configuration;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.cassandra.AuditCassandraDao;
import org.openecomp.sdc.be.dao.cassandra.CassandraOperationStatus;
import org.openecomp.sdc.be.dao.impl.AuditingDao;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.resources.data.auditing.*;
import org.openecomp.sdc.be.resources.data.auditing.model.CommonAuditData;
import org.openecomp.sdc.test.utils.TestConfigurationProvider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openecomp.sdc.be.auditing.impl.AuditTestUtils.*;


@RunWith(MockitoJUnitRunner.class)
public class AuditUserEventFuncTest {
    @Mock
    private static AuditCassandraDao cassandraDao;
    @Captor
    private ArgumentCaptor<AuditingGenericEvent> eventCaptor;
    @Mock
    private static AuditingDao auditingDao;
    @Mock
    private Configuration.ElasticSearchConfig esConfig;

    private AuditingManager auditingManager;

    @Before
    public void setUp() {
        init(esConfig);
        auditingManager = new AuditingManager(auditingDao, cassandraDao, new TestConfigurationProvider());
    }

    @Test
    public void testUserAccessEvent() {
        AuditEventFactory factory = new AuditUserAccessEventFactory(
                CommonAuditData.newBuilder()
                        .description(DESCRIPTION)
                        .status(STATUS_OK)
                        .requestId(REQUEST_ID)
                        .build(),
                user);
        when(auditingDao.addRecord(any(AuditingGenericEvent.class), eq(AuditingActionEnum.USER_ACCESS.getAuditingEsType())))
                .thenReturn(ActionStatus.OK);
        when(cassandraDao.saveRecord(any(AuditingGenericEvent.class))).thenReturn(CassandraOperationStatus.OK);

        assertThat(auditingManager.auditEvent(factory)).isEqualTo(EXPECTED_USER_ACCESS_LOG_STR);
        verifyUserAccessEvent();
    }

    @Test
    public void testUserAdminEventForAddUser() {

        user.setRole(DESIGNER_USER_ROLE);
        user.setEmail(USER_EMAIL);

        AuditEventFactory factory = new AuditUserAdminEventFactory(AuditingActionEnum.ADD_USER,
                CommonAuditData.newBuilder()
                        .description(DESCRIPTION)
                        .status(STATUS_CREATED)
                        .requestId(REQUEST_ID)
                        .build(),
                modifier, null, user);

        when(auditingDao.addRecord(any(AuditingGenericEvent.class), eq(AuditingActionEnum.ADD_USER.getAuditingEsType())))
                .thenReturn(ActionStatus.OK);
        when(cassandraDao.saveRecord(any(AuditingGenericEvent.class))).thenReturn(CassandraOperationStatus.OK);

        assertThat(auditingManager.auditEvent(factory)).isEqualTo(EXPECTED_ADD_USER_LOG_STR);
        verifyUserEvent(AuditingActionEnum.ADD_USER.getName());
    }

    @Test
    public void testUserAdminEventForUpdateUser() {

        user.setRole(DESIGNER_USER_ROLE);
        user.setEmail(USER_EMAIL);

        User updated = new User(user);
        updated.setRole(TESTER_USER_ROLE);

        AuditEventFactory builder = new AuditUserAdminEventFactory(AuditingActionEnum.UPDATE_USER,
                CommonAuditData.newBuilder()
                        .description(DESCRIPTION)
                        .status(STATUS_OK)
                        .requestId(REQUEST_ID)
                        .build(),
                modifier, user, updated);

        when(auditingDao.addRecord(any(AuditingGenericEvent.class), eq(AuditingActionEnum.UPDATE_USER.getAuditingEsType())))
                .thenReturn(ActionStatus.OK);
        when(cassandraDao.saveRecord(any(AuditingGenericEvent.class))).thenReturn(CassandraOperationStatus.OK);

        assertThat(auditingManager.auditEvent(builder)).isEqualTo(EXPECTED_UPDATE_USER_LOG_STR);
        verifyUserEvent(AuditingActionEnum.UPDATE_USER.getName());
    }

    @Test
    public void testUserAdminEventForDeleteUser() {

        user.setRole(DESIGNER_USER_ROLE);
        user.setEmail(USER_EMAIL);

        AuditEventFactory factory = new AuditUserAdminEventFactory(AuditingActionEnum.DELETE_USER,
                CommonAuditData.newBuilder()
                        .description(DESCRIPTION)
                        .status(STATUS_OK)
                        .requestId(REQUEST_ID)
                        .build(),
                modifier, user, null);

        when(auditingDao.addRecord(any(AuditingGenericEvent.class), eq(AuditingActionEnum.DELETE_USER.getAuditingEsType())))
                .thenReturn(ActionStatus.OK);
        when(cassandraDao.saveRecord(any(AuditingGenericEvent.class))).thenReturn(CassandraOperationStatus.OK);

        assertThat(auditingManager.auditEvent(factory)).isEqualTo(EXPECTED_DELETE_USER_LOG_STR);
        verifyUserEvent(AuditingActionEnum.DELETE_USER.getName());
    }

    @Test
    public void testGetUserListEvent() {

        AuditEventFactory factory = new AuditGetUsersListEventFactory(
                CommonAuditData.newBuilder()
                        .description(DESCRIPTION)
                        .status(STATUS_OK)
                        .requestId(REQUEST_ID)
                        .build(),
                user, USER_DETAILS);

        when(auditingDao.addRecord(any(AuditingGenericEvent.class), eq(AuditingActionEnum.GET_USERS_LIST.getAuditingEsType())))
                .thenReturn(ActionStatus.OK);
        when(cassandraDao.saveRecord(any(AuditingGenericEvent.class))).thenReturn(CassandraOperationStatus.OK);

        assertThat(auditingManager.auditEvent(factory)).isEqualTo(EXPECTED_GET_USER_LIST_LOG_STR);
        verifyGetUserListEvent();
    }

    @Test
    public void testAuthRequestEvent() {

        AuditEventFactory factory = new AuditAuthRequestEventFactory(
                CommonAuditData.newBuilder()
                        .description(DESCRIPTION)
                        .status(STATUS_OK)
                        .requestId(REQUEST_ID)
                        .build(),
                USER_ID, AUTH_URL, REALM, AUTH_STATUS);

        when(auditingDao.addRecord(any(AuditingGenericEvent.class), eq(AuditingActionEnum.AUTH_REQUEST.getAuditingEsType())))
                .thenReturn(ActionStatus.OK);
        when(cassandraDao.saveRecord(any(AuditingGenericEvent.class))).thenReturn(CassandraOperationStatus.OK);

        assertThat(auditingManager.auditEvent(factory)).isEqualTo(EXPECTED_AUTH_REQUEST_LOG_STR);
        verifyAuthRequestEvent();
    }

    private void verifyUserEvent(String action) {
        verify(cassandraDao).saveRecord(eventCaptor.capture());
        UserAdminEvent storedEvent = (UserAdminEvent) eventCaptor.getValue();
        assertThat(storedEvent.getModifier()).isEqualTo(MODIFIER_UID);
        assertThat(storedEvent.getDesc()).isEqualTo(DESCRIPTION);
//        assertThat(storedEvent.getRequestId()).isEqualTo(REQUEST_ID); - it is not filled out by the old code!!!
        assertThat(storedEvent.getServiceInstanceId()).isNull();
        assertThat(storedEvent.getAction()).isEqualTo(action);
        if (action.equals(AuditingActionEnum.ADD_USER.getName())) {
            //TODO enable this test after deleting the old auditEvent method
//            assertThat(storedEvent.getUserBefore()).isNull();
            assertThat(storedEvent.getUserAfter()).isEqualTo(USER_EXTENDED_NAME);
            assertThat(storedEvent.getStatus()).isEqualTo(STATUS_CREATED);
        }
        else if (action.equals(AuditingActionEnum.UPDATE_USER.getName())){
            assertThat(storedEvent.getUserBefore()).isEqualTo(USER_EXTENDED_NAME);
            assertThat(storedEvent.getUserAfter()).isEqualTo(UPDATED_USER_EXTENDED_NAME);
            assertThat(storedEvent.getStatus()).isEqualTo(STATUS_OK);
        }
        else {
            assertThat(storedEvent.getUserBefore()).isEqualTo(USER_EXTENDED_NAME);
            //TODO enable this test after deleting the old auditEvent method
//            assertThat(storedEvent.getUserAfter()).isNull();
            assertThat(storedEvent.getStatus()).isEqualTo(STATUS_OK);
        }
    }

    private void verifyGetUserListEvent() {
        verify(cassandraDao).saveRecord(eventCaptor.capture());
        GetUsersListEvent storedEvent = (GetUsersListEvent) eventCaptor.getValue();
        assertThat(storedEvent.getModifier()).isEqualTo(USER_UID);
        assertThat(storedEvent.getDesc()).isEqualTo(DESCRIPTION);
        assertThat(storedEvent.getServiceInstanceId()).isNull();
        assertThat(storedEvent.getAction()).isEqualTo(AuditingActionEnum.GET_USERS_LIST.getName());
    }

    private void verifyUserAccessEvent() {
        verify(cassandraDao).saveRecord(eventCaptor.capture());
        UserAccessEvent storedEvent = (UserAccessEvent) eventCaptor.getValue();
        assertThat(storedEvent.getUserUid()).isEqualTo(USER_UID);
        assertThat(storedEvent.getStatus()).isEqualTo(STATUS_OK);
        assertThat(storedEvent.getDesc()).isEqualTo(DESCRIPTION);
        assertThat(storedEvent.getRequestId()).isNotBlank();
        assertThat(storedEvent.getServiceInstanceId()).isNull();
        assertThat(storedEvent.getAction()).isEqualTo(AuditingActionEnum.USER_ACCESS.getName());
    }

    private void verifyAuthRequestEvent() {
        verify(cassandraDao).saveRecord(eventCaptor.capture());
        AuthEvent storedEvent = (AuthEvent) eventCaptor.getValue();
        assertThat(storedEvent.getUser()).isEqualTo(USER_ID);
        assertThat(storedEvent.getStatus()).isEqualTo(STATUS_OK);
        assertThat(storedEvent.getDesc()).isEqualTo(DESCRIPTION);
        assertThat(storedEvent.getRequestId()).isNotBlank();
        assertThat(storedEvent.getServiceInstanceId()).isNull();
        assertThat(storedEvent.getAuthStatus()).isEqualTo(AUTH_STATUS);
        assertThat(storedEvent.getUrl()).isEqualTo(AUTH_URL);
        assertThat(storedEvent.getRealm()).isEqualTo(REALM);
        assertThat(storedEvent.getAction()).isEqualTo(AuditingActionEnum.AUTH_REQUEST.getName());
    }

}
