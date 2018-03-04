package org.openecomp.sdc.be.auditing.impl.ecompopenv;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.sdc.be.auditing.api.AuditEventFactory;
import org.openecomp.sdc.be.auditing.api.IAuditingManager;
import org.openecomp.sdc.be.auditing.impl.AuditEcompOpEnvEventFactory;
import org.openecomp.sdc.be.auditing.impl.AuditingManager;
import org.openecomp.sdc.be.config.Configuration;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.cassandra.AuditCassandraDao;
import org.openecomp.sdc.be.dao.cassandra.CassandraOperationStatus;
import org.openecomp.sdc.be.dao.impl.AuditingDao;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.resources.data.auditing.AuditingGenericEvent;
import org.openecomp.sdc.be.resources.data.auditing.EcompOperationalEnvironmentEvent;
import org.openecomp.sdc.common.api.ConfigurationSource;
import org.openecomp.sdc.common.datastructure.AuditingFieldsKeysEnum;

import java.util.EnumMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openecomp.sdc.be.auditing.impl.AuditTestUtils.*;

@RunWith(MockitoJUnitRunner.class)
public class AuditEcompOpEnvEventTest {

    private IAuditingManager auditingManager;

    @Mock
    private static AuditCassandraDao cassandraDao;
    @Mock
    private static ConfigurationSource configurationSource;
    @Mock
    private static AuditingDao auditingDao;
    @Mock
    private static Configuration.ElasticSearchConfig esConfig;

    @Captor
    private ArgumentCaptor<AuditingGenericEvent> eventCaptor;

    @Before
    public void setUp() {
        init(configurationSource, esConfig);
        auditingManager = new AuditingManager(auditingDao, cassandraDao);
    }

    @Test
    public void testNewCreateOpEnvEvent() {
        AuditEventFactory factory = new AuditEcompOpEnvEventFactory(AuditingActionEnum.CREATE_ENVIRONMENT,
                OP_ENV_ID, OP_ENV_NAME, OP_ENV_TYPE, OP_ENV_ACTION, TENANT_CONTEXT);

        when(auditingDao.addRecord(any(AuditingGenericEvent.class), eq(AuditingActionEnum.CREATE_ENVIRONMENT.getAuditingEsType())))
                .thenReturn(ActionStatus.OK);
        when(cassandraDao.saveRecord(any(AuditingGenericEvent.class))).thenReturn(CassandraOperationStatus.OK);

        assertThat(auditingManager.auditEvent(factory)).isEqualTo(EXPECTED_CREATE_OP_ENV_LOG_STR);
        verifyEvent(AuditingActionEnum.CREATE_ENVIRONMENT.getName());
    }

    @Test
    public void testOldCreateOpEnvEvent() {
        when(auditingDao.addRecord(anyMap(), eq(AuditingActionEnum.CREATE_ENVIRONMENT.getAuditingEsType())))
                .thenReturn(ActionStatus.OK);
        when(cassandraDao.saveRecord(any(AuditingGenericEvent.class))).thenReturn(CassandraOperationStatus.OK);

        assertThat(auditingManager.auditEvent(fillMap(AuditingActionEnum.CREATE_ENVIRONMENT))).isEqualTo(EXPECTED_CREATE_OP_ENV_LOG_STR);
        verifyEvent(AuditingActionEnum.CREATE_ENVIRONMENT.getName());

    }

    @Test
    public void testNewUnsupportedTypeOpEnvEvent() {
        AuditEventFactory factory = new AuditEcompOpEnvEventFactory(AuditingActionEnum.UNSUPPORTED_ENVIRONMENT_TYPE,
                OP_ENV_ID, OP_ENV_NAME, OP_ENV_TYPE, OP_ENV_ACTION, TENANT_CONTEXT);

        when(auditingDao.addRecord(any(AuditingGenericEvent.class), eq(AuditingActionEnum.UNSUPPORTED_ENVIRONMENT_TYPE.getAuditingEsType())))
                .thenReturn(ActionStatus.OK);
        when(cassandraDao.saveRecord(any(AuditingGenericEvent.class))).thenReturn(CassandraOperationStatus.OK);

        assertThat(auditingManager.auditEvent(factory)).isEqualTo(EXPECTED_UNSUPPORTED_TYPE_OP_ENV_LOG_STR);
        verifyEvent(AuditingActionEnum.UNSUPPORTED_ENVIRONMENT_TYPE.getName());
    }

    @Test
    public void testOldUnsupportedTypeOpEnvEvent() {
        when(auditingDao.addRecord(anyMap(), eq(AuditingActionEnum.UNSUPPORTED_ENVIRONMENT_TYPE.getAuditingEsType())))
                .thenReturn(ActionStatus.OK);
        when(cassandraDao.saveRecord(any(AuditingGenericEvent.class))).thenReturn(CassandraOperationStatus.OK);

        assertThat(auditingManager.auditEvent(fillMap(AuditingActionEnum.UNSUPPORTED_ENVIRONMENT_TYPE))).isEqualTo(EXPECTED_UNSUPPORTED_TYPE_OP_ENV_LOG_STR);
        verifyEvent(AuditingActionEnum.UNSUPPORTED_ENVIRONMENT_TYPE.getName());
    }

    @Test
    public void testNewUnknownNotificationOpEnvEvent() {
        AuditEventFactory factory = new AuditEcompOpEnvEventFactory(AuditingActionEnum.UNKNOWN_ENVIRONMENT_NOTIFICATION,
                OP_ENV_ID, OP_ENV_NAME, OP_ENV_TYPE, OP_ENV_ACTION, TENANT_CONTEXT);

        when(auditingDao.addRecord(any(AuditingGenericEvent.class), eq(AuditingActionEnum.UNKNOWN_ENVIRONMENT_NOTIFICATION.getAuditingEsType())))
                .thenReturn(ActionStatus.OK);
        when(cassandraDao.saveRecord(any(AuditingGenericEvent.class))).thenReturn(CassandraOperationStatus.OK);

        assertThat(auditingManager.auditEvent(factory)).isEqualTo(EXPECTED_UNKNOWN_NOTIFICATION_OP_ENV_LOG_STR);
        verifyEvent(AuditingActionEnum.UNKNOWN_ENVIRONMENT_NOTIFICATION.getName());
    }

    @Test
    public void testOldUnknownNotificationOpEnvEvent() {
        when(auditingDao.addRecord(anyMap(), eq(AuditingActionEnum.UNSUPPORTED_ENVIRONMENT_TYPE.getAuditingEsType())))
                .thenReturn(ActionStatus.OK);
        when(cassandraDao.saveRecord(any(AuditingGenericEvent.class))).thenReturn(CassandraOperationStatus.OK);

        assertThat(auditingManager.auditEvent(fillMap(AuditingActionEnum.UNKNOWN_ENVIRONMENT_NOTIFICATION))).isEqualTo(EXPECTED_UNKNOWN_NOTIFICATION_OP_ENV_LOG_STR);
        verifyEvent(AuditingActionEnum.UNKNOWN_ENVIRONMENT_NOTIFICATION.getName());
    }

    private EnumMap<AuditingFieldsKeysEnum, Object> fillMap(AuditingActionEnum action) {
        EnumMap<AuditingFieldsKeysEnum, Object> auditingFields = new EnumMap<>(AuditingFieldsKeysEnum.class);
        auditingFields.put(AuditingFieldsKeysEnum.AUDIT_ACTION, action.getName());
        auditingFields.put(AuditingFieldsKeysEnum.AUDIT_OPERATIONAL_ENVIRONMENT_ID, OP_ENV_ID);
        auditingFields.put(AuditingFieldsKeysEnum.AUDIT_OPERATIONAL_ENVIRONMENT_TYPE, OP_ENV_TYPE);
        auditingFields.put(AuditingFieldsKeysEnum.AUDIT_OPERATIONAL_ENVIRONMENT_NAME, OP_ENV_NAME);
        auditingFields.put(AuditingFieldsKeysEnum.AUDIT_OPERATIONAL_ENVIRONMENT_ACTION, OP_ENV_ACTION);
        auditingFields.put(AuditingFieldsKeysEnum.AUDIT_TENANT_CONTEXT, TENANT_CONTEXT);

        return auditingFields;
    }

    private void verifyEvent(String action) {
        verify(cassandraDao).saveRecord(eventCaptor.capture());
        EcompOperationalEnvironmentEvent storedEvent = (EcompOperationalEnvironmentEvent) eventCaptor.getValue();
        assertThat(storedEvent.getAction()).isEqualTo(action);
        assertThat(storedEvent.getOperationalEnvironmentId()).isEqualTo(OP_ENV_ID);
        assertThat(storedEvent.getOperationalEnvironmentName()).isEqualTo(OP_ENV_NAME);
        assertThat(storedEvent.getOperationalEnvironmentType()).isEqualTo(OP_ENV_TYPE);
        assertThat(storedEvent.getTenantContext()).isEqualTo(TENANT_CONTEXT);


    }

}
