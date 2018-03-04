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
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.resources.data.auditing.AuditingGenericEvent;
import org.openecomp.sdc.be.resources.data.auditing.AuthEvent;
import org.openecomp.sdc.be.resources.data.auditing.GetUsersListEvent;
import org.openecomp.sdc.be.resources.data.auditing.UserAccessEvent;
import org.openecomp.sdc.be.resources.data.auditing.UserAdminEvent;
import org.openecomp.sdc.be.resources.data.auditing.model.CommonAuditData;
import org.openecomp.sdc.common.datastructure.AuditingFieldsKeysEnum;

import java.util.EnumMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openecomp.sdc.be.auditing.impl.AuditTestUtils.AUTH_STATUS;
import static org.openecomp.sdc.be.auditing.impl.AuditTestUtils.AUTH_URL;
import static org.openecomp.sdc.be.auditing.impl.AuditTestUtils.DESCRIPTION;
import static org.openecomp.sdc.be.auditing.impl.AuditTestUtils.DESIGNER_USER_ROLE;
import static org.openecomp.sdc.be.auditing.impl.AuditTestUtils.EXPECTED_ADD_USER_LOG_STR;
import static org.openecomp.sdc.be.auditing.impl.AuditTestUtils.EXPECTED_AUTH_REQUEST_LOG_STR;
import static org.openecomp.sdc.be.auditing.impl.AuditTestUtils.EXPECTED_DELETE_USER_LOG_STR;
import static org.openecomp.sdc.be.auditing.impl.AuditTestUtils.EXPECTED_GET_USER_LIST_LOG_STR;
import static org.openecomp.sdc.be.auditing.impl.AuditTestUtils.EXPECTED_UPDATE_USER_LOG_STR;
import static org.openecomp.sdc.be.auditing.impl.AuditTestUtils.EXPECTED_USER_ACCESS_LOG_STR;
import static org.openecomp.sdc.be.auditing.impl.AuditTestUtils.MODIFIER_UID;
import static org.openecomp.sdc.be.auditing.impl.AuditTestUtils.REALM;
import static org.openecomp.sdc.be.auditing.impl.AuditTestUtils.REQUEST_ID;
import static org.openecomp.sdc.be.auditing.impl.AuditTestUtils.STATUS_CREATED;
import static org.openecomp.sdc.be.auditing.impl.AuditTestUtils.STATUS_OK;
import static org.openecomp.sdc.be.auditing.impl.AuditTestUtils.TESTER_USER_ROLE;
import static org.openecomp.sdc.be.auditing.impl.AuditTestUtils.UPDATED_USER_EXTENDED_NAME;
import static org.openecomp.sdc.be.auditing.impl.AuditTestUtils.USER_DETAILS;
import static org.openecomp.sdc.be.auditing.impl.AuditTestUtils.USER_EMAIL;
import static org.openecomp.sdc.be.auditing.impl.AuditTestUtils.USER_EXTENDED_NAME;
import static org.openecomp.sdc.be.auditing.impl.AuditTestUtils.USER_ID;
import static org.openecomp.sdc.be.auditing.impl.AuditTestUtils.USER_UID;
import static org.openecomp.sdc.be.auditing.impl.AuditTestUtils.init;
import static org.openecomp.sdc.be.auditing.impl.AuditTestUtils.modifier;
import static org.openecomp.sdc.be.auditing.impl.AuditTestUtils.user;

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
        auditingManager = new AuditingManager(auditingDao, cassandraDao);
    }

    @Test
    public void testNewUserAccessEvent() {
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
    public void testOldUserAccessEvent() {
        EnumMap<AuditingFieldsKeysEnum, Object> auditingFields = new EnumMap<>(AuditingFieldsKeysEnum.class);
        auditingFields.put(AuditingFieldsKeysEnum.AUDIT_ACTION, AuditingActionEnum.USER_ACCESS.getName());
        auditingFields.put(AuditingFieldsKeysEnum.AUDIT_USER_UID, user.getFirstName() + " " + user.getLastName() + '(' + user.getUserId() + ')');
        auditingFields.put(AuditingFieldsKeysEnum.AUDIT_STATUS, STATUS_OK);
        auditingFields.put(AuditingFieldsKeysEnum.AUDIT_DESC, DESCRIPTION);
        auditingFields.put(AuditingFieldsKeysEnum.AUDIT_REQUEST_ID, REQUEST_ID);

        when(auditingDao.addRecord(anyMap(), eq(AuditingActionEnum.USER_ACCESS.getAuditingEsType())))
                .thenReturn(ActionStatus.OK);
        when(cassandraDao.saveRecord(any(AuditingGenericEvent.class))).thenReturn(CassandraOperationStatus.OK);

        assertThat(auditingManager.auditEvent(auditingFields)).isEqualTo(EXPECTED_USER_ACCESS_LOG_STR);
        verifyUserAccessEvent();
    }

    @Test
    public void testNewUserAdminEventForAddUser() {

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
    public void testOldUserAdminEventForAddUser() {
        user.setRole(TESTER_USER_ROLE);
        user.setEmail(USER_EMAIL);

        EnumMap<AuditingFieldsKeysEnum, Object> auditingFields = new EnumMap<>(AuditingFieldsKeysEnum.class);
        auditingFields.put(AuditingFieldsKeysEnum.AUDIT_ACTION, AuditingActionEnum.ADD_USER.getName());
        auditingFields.put(AuditingFieldsKeysEnum.AUDIT_MODIFIER_NAME, modifier.getFirstName() + " " + modifier.getLastName());
        auditingFields.put(AuditingFieldsKeysEnum.AUDIT_MODIFIER_UID, modifier.getUserId());
        auditingFields.put(AuditingFieldsKeysEnum.AUDIT_STATUS, STATUS_CREATED);
        auditingFields.put(AuditingFieldsKeysEnum.AUDIT_DESC, DESCRIPTION);
        auditingFields.put(AuditingFieldsKeysEnum.AUDIT_REQUEST_ID, REQUEST_ID);
        auditingFields.put(AuditingFieldsKeysEnum.AUDIT_USER_BEFORE, null);
        auditingFields.put(AuditingFieldsKeysEnum.AUDIT_USER_AFTER, USER_EXTENDED_NAME);

        when(auditingDao.addRecord(anyMap(), eq(AuditingActionEnum.ADD_USER.getAuditingEsType())))
                .thenReturn(ActionStatus.OK);
        when(cassandraDao.saveRecord(any(AuditingGenericEvent.class))).thenReturn(CassandraOperationStatus.OK);

        assertThat(auditingManager.auditEvent(auditingFields)).isEqualTo(EXPECTED_ADD_USER_LOG_STR);
        verifyUserEvent(AuditingActionEnum.ADD_USER.getName());
    }

    @Test
    public void testNewUserAdminEventForUpdateUser() {

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
    public void testOldUserAdminEventForUpdateUser() {
        user.setRole(DESIGNER_USER_ROLE);
        user.setEmail(USER_EMAIL);

        User updated = new User(user);
        updated.setRole(TESTER_USER_ROLE);

        EnumMap<AuditingFieldsKeysEnum, Object> auditingFields = new EnumMap<>(AuditingFieldsKeysEnum.class);
        auditingFields.put(AuditingFieldsKeysEnum.AUDIT_ACTION, AuditingActionEnum.UPDATE_USER.getName());
        auditingFields.put(AuditingFieldsKeysEnum.AUDIT_MODIFIER_NAME, modifier.getFirstName() + " " + modifier.getLastName());
        auditingFields.put(AuditingFieldsKeysEnum.AUDIT_MODIFIER_UID, modifier.getUserId());
        auditingFields.put(AuditingFieldsKeysEnum.AUDIT_STATUS, STATUS_OK);
        auditingFields.put(AuditingFieldsKeysEnum.AUDIT_DESC, DESCRIPTION);
        auditingFields.put(AuditingFieldsKeysEnum.AUDIT_REQUEST_ID, REQUEST_ID);
        auditingFields.put(AuditingFieldsKeysEnum.AUDIT_USER_BEFORE, USER_EXTENDED_NAME);
        auditingFields.put(AuditingFieldsKeysEnum.AUDIT_USER_AFTER, UPDATED_USER_EXTENDED_NAME);

        when(auditingDao.addRecord(anyMap(), eq(AuditingActionEnum.UPDATE_USER.getAuditingEsType())))
                .thenReturn(ActionStatus.OK);
        when(cassandraDao.saveRecord(any(AuditingGenericEvent.class))).thenReturn(CassandraOperationStatus.OK);

        assertThat(auditingManager.auditEvent(auditingFields)).isEqualTo(EXPECTED_UPDATE_USER_LOG_STR);
        verifyUserEvent(AuditingActionEnum.UPDATE_USER.getName());
    }

    @Test
    public void testNewUserAdminEventForDeleteUser() {

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
    public void testOldUserAdminEventForDeleteUser() {
        user.setRole(TESTER_USER_ROLE);
        user.setEmail(USER_EMAIL);

        EnumMap<AuditingFieldsKeysEnum, Object> auditingFields = new EnumMap<>(AuditingFieldsKeysEnum.class);
        auditingFields.put(AuditingFieldsKeysEnum.AUDIT_ACTION, AuditingActionEnum.DELETE_USER.getName());
        auditingFields.put(AuditingFieldsKeysEnum.AUDIT_MODIFIER_NAME, modifier.getFirstName() + " " + modifier.getLastName());
        auditingFields.put(AuditingFieldsKeysEnum.AUDIT_MODIFIER_UID, modifier.getUserId());
        auditingFields.put(AuditingFieldsKeysEnum.AUDIT_STATUS, STATUS_OK);
        auditingFields.put(AuditingFieldsKeysEnum.AUDIT_DESC, DESCRIPTION);
        auditingFields.put(AuditingFieldsKeysEnum.AUDIT_REQUEST_ID, REQUEST_ID);
        auditingFields.put(AuditingFieldsKeysEnum.AUDIT_USER_BEFORE, USER_EXTENDED_NAME);
        auditingFields.put(AuditingFieldsKeysEnum.AUDIT_USER_AFTER, null);

        when(auditingDao.addRecord(anyMap(), eq(AuditingActionEnum.DELETE_USER.getAuditingEsType())))
                .thenReturn(ActionStatus.OK);
        when(cassandraDao.saveRecord(any(AuditingGenericEvent.class))).thenReturn(CassandraOperationStatus.OK);

        assertThat(auditingManager.auditEvent(auditingFields)).isEqualTo(EXPECTED_DELETE_USER_LOG_STR);
        verifyUserEvent(AuditingActionEnum.DELETE_USER.getName());
    }

    @Test
    public void testNewGetUserListEvent() {

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
    public void testOldGetUserListEvent() {
        EnumMap<AuditingFieldsKeysEnum, Object> auditingFields = new EnumMap<>(AuditingFieldsKeysEnum.class);
        auditingFields.put(AuditingFieldsKeysEnum.AUDIT_ACTION, AuditingActionEnum.GET_USERS_LIST.getName());
        auditingFields.put(AuditingFieldsKeysEnum.AUDIT_MODIFIER_NAME, user.getFirstName() + " " + user.getLastName());
        auditingFields.put(AuditingFieldsKeysEnum.AUDIT_MODIFIER_UID, user.getUserId());
        auditingFields.put(AuditingFieldsKeysEnum.AUDIT_STATUS, STATUS_OK);
        auditingFields.put(AuditingFieldsKeysEnum.AUDIT_DESC, DESCRIPTION);
        auditingFields.put(AuditingFieldsKeysEnum.AUDIT_REQUEST_ID, REQUEST_ID);
        auditingFields.put(AuditingFieldsKeysEnum.AUDIT_USER_DETAILS, USER_DETAILS);

        when(auditingDao.addRecord(anyMap(), eq(AuditingActionEnum.GET_USERS_LIST.getAuditingEsType())))
                .thenReturn(ActionStatus.OK);
        when(cassandraDao.saveRecord(any(AuditingGenericEvent.class))).thenReturn(CassandraOperationStatus.OK);

        assertThat(auditingManager.auditEvent(auditingFields)).isEqualTo(EXPECTED_GET_USER_LIST_LOG_STR);
        verifyGetUserListEvent();
    }

    @Test
    public void testNewAuthRequestEvent() {

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

    @Test
    public void testOldAuthRequestEvent() {
        EnumMap<AuditingFieldsKeysEnum, Object> auditingFields = new EnumMap<>(AuditingFieldsKeysEnum.class);
        auditingFields.put(AuditingFieldsKeysEnum.AUDIT_ACTION, AuditingActionEnum.AUTH_REQUEST.getName());
        auditingFields.put(AuditingFieldsKeysEnum.AUDIT_AUTH_USER, USER_ID);
        auditingFields.put(AuditingFieldsKeysEnum.AUDIT_STATUS, STATUS_OK);
        auditingFields.put(AuditingFieldsKeysEnum.AUDIT_DESC, DESCRIPTION);
        auditingFields.put(AuditingFieldsKeysEnum.AUDIT_REQUEST_ID, REQUEST_ID);
        auditingFields.put(AuditingFieldsKeysEnum.AUDIT_AUTH_REALM, REALM);
        auditingFields.put(AuditingFieldsKeysEnum.AUDIT_AUTH_URL, AUTH_URL);
        auditingFields.put(AuditingFieldsKeysEnum.AUDIT_AUTH_STATUS, AUTH_STATUS);

        when(auditingDao.addRecord(anyMap(), eq(AuditingActionEnum.AUTH_REQUEST.getAuditingEsType())))
                .thenReturn(ActionStatus.OK);
        when(cassandraDao.saveRecord(any(AuditingGenericEvent.class))).thenReturn(CassandraOperationStatus.OK);

        assertThat(auditingManager.auditEvent(auditingFields)).isEqualTo(EXPECTED_AUTH_REQUEST_LOG_STR);
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
