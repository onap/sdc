package org.openecomp.sdc.be.auditing.impl.distribution;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.sdc.be.auditing.api.AuditEventFactory;
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
import org.openecomp.sdc.be.resources.data.auditing.model.DistributionTopicData;
import org.openecomp.sdc.common.util.ThreadLocalsHolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openecomp.sdc.be.auditing.impl.AuditTestUtils.*;

@RunWith(MockitoJUnitRunner.class)
public class AuditDistributionEngineFuncTest {
    private AuditingManager auditingManager;

    @Mock
    private static AuditCassandraDao cassandraDao;
    @Mock
    private static AuditingDao auditingDao;
    @Mock
    private static Configuration.ElasticSearchConfig esConfig;

    @Captor
    private ArgumentCaptor<DistributionEngineEvent> eventCaptor;

    @Before
    public void setUp() {
        init(esConfig);
        auditingManager = new AuditingManager(auditingDao, cassandraDao);
        ThreadLocalsHolder.setUuid(REQUEST_ID);
    }

    @Test
    public void testAddKeyEvent() {
        AuditEventFactory factory = new AuditAddRemoveKeyDistributionEngineEventFactory(
                AuditingActionEnum.ADD_KEY_TO_TOPIC_ACL,
                CommonAuditData.newBuilder()
                        .description(DESCRIPTION)
                        .status(STATUS_OK)
                        .requestId(REQUEST_ID)
                        .serviceInstanceId(SERVICE_INSTANCE_ID)
                        .build(),
                DistributionTopicData.newBuilder()
                        .statusTopic(DIST_STATUS_TOPIC)
                        .notificationTopic(DIST_NOTIFY_TOPIC)
                        .build(),
                DIST_API_KEY, DIST_ENV_NAME, DIST_ROLE);

        when(auditingDao.addRecord(any(AuditingGenericEvent.class), eq(AuditingActionEnum.ADD_KEY_TO_TOPIC_ACL.getAuditingEsType())))
                .thenReturn(ActionStatus.OK);
        when(cassandraDao.saveRecord(any(AuditingGenericEvent.class))).thenReturn(CassandraOperationStatus.OK);

        assertThat(auditingManager.auditEvent(factory)).isEqualTo(EXPECTED_DIST_ADD_KEY_ENGINE_LOG_STR);
        verifyEvent(AuditingActionEnum.ADD_KEY_TO_TOPIC_ACL.getName());
    }

    @Test
    public void testCreateTopicEvent() {
        AuditEventFactory factory = new AuditCreateTopicDistributionEngineEventFactory(
                CommonAuditData.newBuilder()
                        .description(DESCRIPTION)
                        .status(STATUS_OK)
                        .requestId(REQUEST_ID)
                        .serviceInstanceId(SERVICE_INSTANCE_ID)
                        .build(),
                DistributionTopicData.newBuilder()
                        .statusTopic(DIST_STATUS_TOPIC)
                        .notificationTopic(DIST_NOTIFY_TOPIC)
                        .build(),
                DIST_API_KEY, DIST_ENV_NAME, DIST_ROLE);

        when(auditingDao.addRecord(any(AuditingGenericEvent.class), eq(AuditingActionEnum.CREATE_DISTRIBUTION_TOPIC.getAuditingEsType())))
                .thenReturn(ActionStatus.OK);
        when(cassandraDao.saveRecord(any(AuditingGenericEvent.class))).thenReturn(CassandraOperationStatus.OK);

        assertThat(auditingManager.auditEvent(factory)).isEqualTo(EXPECTED_DIST_CREATE_TOPIC_ENGINE_LOG_STR);
        verifyEvent(AuditingActionEnum.CREATE_DISTRIBUTION_TOPIC.getName());
    }

    @Test
    public void testRegisterEvent() {
        AuditEventFactory factory = new AuditRegUnregDistributionEngineEventFactory(
                AuditingActionEnum.DISTRIBUTION_REGISTER,
                CommonAuditData.newBuilder()
                        .description(DESCRIPTION)
                        .status(STATUS_OK)
                        .requestId(REQUEST_ID)
                        .serviceInstanceId(SERVICE_INSTANCE_ID)
                        .build(),
                DistributionTopicData.newBuilder()
                        .statusTopic(DIST_STATUS_TOPIC)
                        .notificationTopic(DIST_NOTIFY_TOPIC)
                        .build(),
                DIST_CONSUMER_ID, DIST_API_KEY, DIST_ENV_NAME);

        when(auditingDao.addRecord(any(AuditingGenericEvent.class), eq(AuditingActionEnum.DISTRIBUTION_REGISTER.getAuditingEsType())))
                .thenReturn(ActionStatus.OK);
        when(cassandraDao.saveRecord(any(AuditingGenericEvent.class))).thenReturn(CassandraOperationStatus.OK);

        assertThat(auditingManager.auditEvent(factory)).isEqualTo(EXPECTED_DIST_REG_ENGINE_LOG_STR);
        verifyEvent(AuditingActionEnum.DISTRIBUTION_REGISTER.getName());
    }

    private void verifyEvent(String action) {
        verify(cassandraDao).saveRecord(eventCaptor.capture());
        DistributionEngineEvent storedEvent = eventCaptor.getValue();
        assertThat(storedEvent.getDnotifTopic()).isEqualTo(DIST_NOTIFY_TOPIC);
        assertThat(storedEvent.getDstatusTopic()).isEqualTo(DIST_STATUS_TOPIC);
        assertThat(storedEvent.getDesc()).isEqualTo(DESCRIPTION);
        assertThat(storedEvent.getStatus()).isEqualTo(STATUS_OK);
        assertThat(storedEvent.getRequestId()).isEqualTo(REQUEST_ID);
        assertThat(storedEvent.getServiceInstanceId()).isEqualTo(SERVICE_INSTANCE_ID);
        assertThat(storedEvent.getAction()).isEqualTo(action);
        assertThat(storedEvent.getEnvironmentName()).isEqualTo(DIST_ENV_NAME);
        assertThat(storedEvent.getApiKey()).isEqualTo(DIST_API_KEY);
        if (!action.equals(AuditingActionEnum.CREATE_DISTRIBUTION_TOPIC.getName()) &&
            !action.equals(AuditingActionEnum.ADD_KEY_TO_TOPIC_ACL.getName()) &&
            !action.equals(AuditingActionEnum.REMOVE_KEY_FROM_TOPIC_ACL.getName())) {
            assertThat(storedEvent.getConsumerId()).isEqualTo(DIST_CONSUMER_ID);
        }
        if (!action.equals(AuditingActionEnum.DISTRIBUTION_REGISTER.getName()) &&
                !action.equals(AuditingActionEnum.DISTRIBUTION_UN_REGISTER.getName())) {
            assertThat(storedEvent.getRole()).isEqualTo(DIST_ROLE);
        }
    }

}
