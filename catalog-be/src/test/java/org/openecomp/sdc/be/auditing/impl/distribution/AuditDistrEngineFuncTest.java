package org.openecomp.sdc.be.auditing.impl.distribution;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.sdc.be.auditing.api.AuditEventFactory;
import org.openecomp.sdc.be.auditing.api.IAuditingManager;
import org.openecomp.sdc.be.auditing.impl.AuditingManager;
import org.openecomp.sdc.be.config.Configuration;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.cassandra.AuditCassandraDao;
import org.openecomp.sdc.be.dao.cassandra.CassandraOperationStatus;
import org.openecomp.sdc.be.dao.impl.AuditingDao;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.resources.data.auditing.AuditingGenericEvent;
import org.openecomp.sdc.be.resources.data.auditing.DistributionEngineEvent;
import org.openecomp.sdc.be.resources.data.auditing.model.CommonAuditData;
import org.openecomp.sdc.common.api.ConfigurationSource;
import org.openecomp.sdc.common.datastructure.AuditingFieldsKeysEnum;
import org.openecomp.sdc.common.util.ThreadLocalsHolder;

import java.util.EnumMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openecomp.sdc.be.auditing.impl.AuditTestUtils.*;

@RunWith(MockitoJUnitRunner.class)
public class AuditDistrEngineFuncTest {
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
        ThreadLocalsHolder.setUuid(REQUEST_ID);
    }

    @Test
    public void testNewAddKeyEvent() {
        AuditEventFactory factory = new AuditAddKeyDistribEngineEventFactory(
                CommonAuditData.newBuilder()
                        .description(DESCRIPTION)
                        .status(STATUS_OK)
                        .requestId(REQUEST_ID)
                        .serviceInstanceId(SERVICE_INSTANCE_ID)
                        .build(),
                DIST_CONSUMER_ID, DIST_STATUS_TOPIC, DIST_NOTIFY_TOPIC, DIST_API_KEY, DIST_ENV_NAME, DIST_ROLE);

        when(auditingDao.addRecord(any(AuditingGenericEvent.class), eq(AuditingActionEnum.ADD_KEY_TO_TOPIC_ACL.getAuditingEsType())))
                .thenReturn(ActionStatus.OK);
        when(cassandraDao.saveRecord(any(AuditingGenericEvent.class))).thenReturn(CassandraOperationStatus.OK);

        assertThat(auditingManager.auditEvent(factory)).isEqualTo(EXPECTED_DIST_ADD_KEY_ENGINE_LOG_STR);
        verifyEvent(AuditingActionEnum.ADD_KEY_TO_TOPIC_ACL.getName());
    }

    @Test
    public void testOldAddKeyEvent() {
        when(auditingDao.addRecord(anyMap(), eq(AuditingActionEnum.ADD_KEY_TO_TOPIC_ACL.getAuditingEsType())))
                .thenReturn(ActionStatus.OK);
        when(cassandraDao.saveRecord(any(AuditingGenericEvent.class))).thenReturn(CassandraOperationStatus.OK);

        assertThat(auditingManager.auditEvent(fillMap(AuditingActionEnum.ADD_KEY_TO_TOPIC_ACL))).isEqualTo(EXPECTED_DIST_ADD_KEY_ENGINE_LOG_STR);
        verifyEvent(AuditingActionEnum.ADD_KEY_TO_TOPIC_ACL.getName());

    }

    @Test
    public void testNewCreateTopicEvent() {
        AuditEventFactory factory = new AuditCreateTopicDistribEngineEventFactory(
                CommonAuditData.newBuilder()
                        .description(DESCRIPTION)
                        .status(STATUS_OK)
                        .requestId(REQUEST_ID)
                        .serviceInstanceId(SERVICE_INSTANCE_ID)
                        .build(),
                DIST_CONSUMER_ID, DIST_STATUS_TOPIC, DIST_NOTIFY_TOPIC, DIST_API_KEY, DIST_ENV_NAME, DIST_ROLE);

        when(auditingDao.addRecord(any(AuditingGenericEvent.class), eq(AuditingActionEnum.CREATE_DISTRIBUTION_TOPIC.getAuditingEsType())))
                .thenReturn(ActionStatus.OK);
        when(cassandraDao.saveRecord(any(AuditingGenericEvent.class))).thenReturn(CassandraOperationStatus.OK);

        assertThat(auditingManager.auditEvent(factory)).isEqualTo(EXPECTED_DIST_CREATE_TOPIC_ENGINE_LOG_STR);
        verifyEvent(AuditingActionEnum.CREATE_DISTRIBUTION_TOPIC.getName());
    }

    @Test
    public void testOldCreateTopicEvent() {
        when(auditingDao.addRecord(anyMap(), eq(AuditingActionEnum.CREATE_DISTRIBUTION_TOPIC.getAuditingEsType())))
                .thenReturn(ActionStatus.OK);
        when(cassandraDao.saveRecord(any(AuditingGenericEvent.class))).thenReturn(CassandraOperationStatus.OK);

        assertThat(auditingManager.auditEvent(fillMap(AuditingActionEnum.CREATE_DISTRIBUTION_TOPIC))).isEqualTo(EXPECTED_DIST_CREATE_TOPIC_ENGINE_LOG_STR);
        verifyEvent(AuditingActionEnum.CREATE_DISTRIBUTION_TOPIC.getName());
    }

    @Test
    public void testNewRegisterEvent() {
        AuditEventFactory factory = new AuditRegisterDistribEngineEventFactory(
                CommonAuditData.newBuilder()
                        .description(DESCRIPTION)
                        .status(STATUS_OK)
                        .requestId(REQUEST_ID)
                        .serviceInstanceId(SERVICE_INSTANCE_ID)
                        .build(),
                DIST_CONSUMER_ID, DIST_STATUS_TOPIC, DIST_NOTIFY_TOPIC, DIST_API_KEY, DIST_ENV_NAME, DIST_ROLE);

        when(auditingDao.addRecord(any(AuditingGenericEvent.class), eq(AuditingActionEnum.DISTRIBUTION_REGISTER.getAuditingEsType())))
                .thenReturn(ActionStatus.OK);
        when(cassandraDao.saveRecord(any(AuditingGenericEvent.class))).thenReturn(CassandraOperationStatus.OK);

        assertThat(auditingManager.auditEvent(factory)).isEqualTo(EXPECTED_DIST_REG_ENGINE_LOG_STR);
        verifyEvent(AuditingActionEnum.DISTRIBUTION_REGISTER.getName());
    }

    @Test
    public void testOldRegisterEvent() {
        when(auditingDao.addRecord(anyMap(), eq(AuditingActionEnum.DISTRIBUTION_REGISTER.getAuditingEsType())))
                .thenReturn(ActionStatus.OK);
        when(cassandraDao.saveRecord(any(AuditingGenericEvent.class))).thenReturn(CassandraOperationStatus.OK);

        assertThat(auditingManager.auditEvent(fillMap(AuditingActionEnum.DISTRIBUTION_REGISTER))).isEqualTo(EXPECTED_DIST_REG_ENGINE_LOG_STR);
        verifyEvent(AuditingActionEnum.DISTRIBUTION_REGISTER.getName());
    }

    private void verifyEvent(String action) {
        verify(cassandraDao).saveRecord(eventCaptor.capture());
        DistributionEngineEvent storedEvent = (DistributionEngineEvent) eventCaptor.getValue();
        assertThat(storedEvent.getDnotifTopic()).isEqualTo(DIST_NOTIFY_TOPIC);
        assertThat(storedEvent.getDstatusTopic()).isEqualTo(DIST_STATUS_TOPIC);
        assertThat(storedEvent.getDesc()).isEqualTo(DESCRIPTION);
        assertThat(storedEvent.getStatus()).isEqualTo(STATUS_OK);
        assertThat(storedEvent.getRequestId()).isEqualTo(REQUEST_ID);
        assertThat(storedEvent.getServiceInstanceId()).isEqualTo(SERVICE_INSTANCE_ID);
        assertThat(storedEvent.getAction()).isEqualTo(action);
        assertThat(storedEvent.getEnvironmentName()).isEqualTo(DIST_ENV_NAME);
        assertThat(storedEvent.getApiKey()).isEqualTo(DIST_API_KEY);
        assertThat(storedEvent.getConsumerId()).isEqualTo(DIST_CONSUMER_ID);
        assertThat(storedEvent.getRole()).isEqualTo(DIST_ROLE);
    }

    private EnumMap<AuditingFieldsKeysEnum, Object> fillMap(AuditingActionEnum action) {
        EnumMap<AuditingFieldsKeysEnum, Object> auditingFields = new EnumMap<>(AuditingFieldsKeysEnum.class);
        auditingFields.put(AuditingFieldsKeysEnum.AUDIT_ACTION, action.getName());
        auditingFields.put(AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_CONSUMER_ID, DIST_CONSUMER_ID);
        auditingFields.put(AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_ENVRIONMENT_NAME, DIST_ENV_NAME);
        auditingFields.put(AuditingFieldsKeysEnum.AUDIT_STATUS, STATUS_OK);
        auditingFields.put(AuditingFieldsKeysEnum.AUDIT_DESC, DESCRIPTION);
        auditingFields.put(AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_STATUS_TOPIC_NAME, DIST_STATUS_TOPIC);
        auditingFields.put(AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_NOTIFICATION_TOPIC_NAME, DIST_NOTIFY_TOPIC);
        auditingFields.put(AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_TOPIC_NAME, DIST_NOTIFY_TOPIC);
        auditingFields.put(AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_API_KEY, DIST_API_KEY);
        auditingFields.put(AuditingFieldsKeysEnum.AUDIT_REQUEST_ID, REQUEST_ID);
        auditingFields.put(AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_ROLE, DIST_ROLE);
        auditingFields.put(AuditingFieldsKeysEnum.AUDIT_SERVICE_INSTANCE_ID, SERVICE_INSTANCE_ID);

        return auditingFields;
    }


}
