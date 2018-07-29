package org.openecomp.sdc.be.auditing.impl.ecompopenv;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.sdc.be.auditing.api.AuditEventFactory;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openecomp.sdc.be.auditing.impl.AuditTestUtils.*;

@RunWith(MockitoJUnitRunner.class)
public class AuditEcompOpEnvEventTest {

    private AuditingManager auditingManager;

    @Mock
    private static AuditCassandraDao cassandraDao;
    @Mock
    private static AuditingDao auditingDao;
    @Mock
    private static Configuration.ElasticSearchConfig esConfig;

    @Captor
    private ArgumentCaptor<EcompOperationalEnvironmentEvent> eventCaptor;

    @Before
    public void setUp() {
        init(esConfig);
        auditingManager = new AuditingManager(auditingDao, cassandraDao);
    }

    @Test
    public void testCreateOpEnvEvent() {
        AuditEventFactory factory = new AuditEcompOpEnvEventFactory(AuditingActionEnum.CREATE_ENVIRONMENT,
                OP_ENV_ID, OP_ENV_NAME, OP_ENV_TYPE, OP_ENV_ACTION, TENANT_CONTEXT);

        when(auditingDao.addRecord(any(AuditingGenericEvent.class), eq(AuditingActionEnum.CREATE_ENVIRONMENT.getAuditingEsType())))
                .thenReturn(ActionStatus.OK);
        when(cassandraDao.saveRecord(any(AuditingGenericEvent.class))).thenReturn(CassandraOperationStatus.OK);

        assertThat(auditingManager.auditEvent(factory)).isEqualTo(EXPECTED_CREATE_OP_ENV_LOG_STR);
        verifyEvent(AuditingActionEnum.CREATE_ENVIRONMENT.getName());
    }

    @Test
    public void testUnsupportedTypeOpEnvEvent() {
        AuditEventFactory factory = new AuditEcompOpEnvEventFactory(AuditingActionEnum.UNSUPPORTED_ENVIRONMENT_TYPE,
                OP_ENV_ID, OP_ENV_NAME, OP_ENV_TYPE, OP_ENV_ACTION, TENANT_CONTEXT);

        when(auditingDao.addRecord(any(AuditingGenericEvent.class), eq(AuditingActionEnum.UNSUPPORTED_ENVIRONMENT_TYPE.getAuditingEsType())))
                .thenReturn(ActionStatus.OK);
        when(cassandraDao.saveRecord(any(AuditingGenericEvent.class))).thenReturn(CassandraOperationStatus.OK);

        assertThat(auditingManager.auditEvent(factory)).isEqualTo(EXPECTED_UNSUPPORTED_TYPE_OP_ENV_LOG_STR);
        verifyEvent(AuditingActionEnum.UNSUPPORTED_ENVIRONMENT_TYPE.getName());
    }

    @Test
    public void testUnknownNotificationOpEnvEvent() {
        AuditEventFactory factory = new AuditEcompOpEnvEventFactory(AuditingActionEnum.UNKNOWN_ENVIRONMENT_NOTIFICATION,
                OP_ENV_ID, OP_ENV_NAME, OP_ENV_TYPE, OP_ENV_ACTION, TENANT_CONTEXT);

        when(auditingDao.addRecord(any(AuditingGenericEvent.class), eq(AuditingActionEnum.UNKNOWN_ENVIRONMENT_NOTIFICATION.getAuditingEsType())))
                .thenReturn(ActionStatus.OK);
        when(cassandraDao.saveRecord(any(AuditingGenericEvent.class))).thenReturn(CassandraOperationStatus.OK);

        assertThat(auditingManager.auditEvent(factory)).isEqualTo(EXPECTED_UNKNOWN_NOTIFICATION_OP_ENV_LOG_STR);
        verifyEvent(AuditingActionEnum.UNKNOWN_ENVIRONMENT_NOTIFICATION.getName());
    }

    private void verifyEvent(String action) {
        verify(cassandraDao).saveRecord(eventCaptor.capture());
        EcompOperationalEnvironmentEvent storedEvent = eventCaptor.getValue();
        assertThat(storedEvent.getAction()).isEqualTo(action);
        assertThat(storedEvent.getOperationalEnvironmentId()).isEqualTo(OP_ENV_ID);
        assertThat(storedEvent.getOperationalEnvironmentName()).isEqualTo(OP_ENV_NAME);
        assertThat(storedEvent.getOperationalEnvironmentType()).isEqualTo(OP_ENV_TYPE);
        assertThat(storedEvent.getTenantContext()).isEqualTo(TENANT_CONTEXT);
    }

}
