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
import org.openecomp.sdc.be.resources.data.auditing.*;
import org.openecomp.sdc.be.resources.data.auditing.model.CommonAuditData;
import org.openecomp.sdc.be.resources.data.auditing.model.DistributionData;
import org.openecomp.sdc.be.resources.data.auditing.model.OperationalEnvAuditData;
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
public class AuditDistrEventFuncTest {

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
    public void testNewNotifyEvent() {
        AuditEventFactory factory = new AuditDistribNotificationEventFactory(
                CommonAuditData.newBuilder()
                        .description(DESCRIPTION)
                        .status(STATUS_OK)
                        .requestId(REQUEST_ID)
                        .serviceInstanceId(SERVICE_INSTANCE_ID)
                        .build(),
                CURRENT_STATE, CURRENT_VERSION, DIST_ID, user,
                RESOURCE_NAME, RESOURCE_TYPE, TOPIC_NAME,
                new OperationalEnvAuditData(OP_ENV_ID, VNF_WORKLOAD_CONTEXT, TENANT_CONTEXT));

        when(auditingDao.addRecord(any(AuditingGenericEvent.class), eq(AuditingActionEnum.DISTRIBUTION_NOTIFY.getAuditingEsType())))
                .thenReturn(ActionStatus.OK);
        when(cassandraDao.saveRecord(any(AuditingGenericEvent.class))).thenReturn(CassandraOperationStatus.OK);

        assertThat(auditingManager.auditEvent(factory)).isEqualTo(EXPECTED_DISTRIB_NOTIFICATION_LOG_STR);
        verifyNotifyEvent();
    }

    @Test
    public void testOldNotifyEvent() {
        when(auditingDao.addRecord(anyMap(), eq(AuditingActionEnum.DISTRIBUTION_NOTIFY.getAuditingEsType())))
                .thenReturn(ActionStatus.OK);
        when(cassandraDao.saveRecord(any(AuditingGenericEvent.class))).thenReturn(CassandraOperationStatus.OK);

        assertThat(auditingManager.auditEvent(fillNotifyMap())).isEqualTo(EXPECTED_DISTRIB_NOTIFICATION_LOG_STR);
        verifyNotifyEvent();

    }

    @Test
    public void testNewStatusEvent() {
        AuditEventFactory factory = new AuditDistribStatusEventFactory(
                CommonAuditData.newBuilder()
                        .description(DESCRIPTION)
                        .status(STATUS_OK)
                        .requestId(REQUEST_ID)
                        .serviceInstanceId(SERVICE_INSTANCE_ID)
                        .build(),
                DIST_ID, DIST_CONSUMER_ID, TOPIC_NAME, DIST_RESOURCE_URL, DIST_STATUS_TIME);

        when(auditingDao.addRecord(any(AuditingGenericEvent.class), eq(AuditingActionEnum.DISTRIBUTION_STATUS.getAuditingEsType())))
                .thenReturn(ActionStatus.OK);
        when(cassandraDao.saveRecord(any(AuditingGenericEvent.class))).thenReturn(CassandraOperationStatus.OK);

        assertThat(auditingManager.auditEvent(factory)).isEqualTo(EXPECTED_DIST_STATUS_LOG_STR);
        verifyStatusEvent();
    }

    @Test
    public void testOldStatusEvent() {
        when(auditingDao.addRecord(anyMap(), eq(AuditingActionEnum.DISTRIBUTION_STATUS.getAuditingEsType())))
                .thenReturn(ActionStatus.OK);
        when(cassandraDao.saveRecord(any(AuditingGenericEvent.class))).thenReturn(CassandraOperationStatus.OK);

        assertThat(auditingManager.auditEvent(fillStatusMap())).isEqualTo(EXPECTED_DIST_STATUS_LOG_STR);
        verifyStatusEvent();
    }

    @Test
    public void testNewDownloadEvent() {
        AuditEventFactory factory = new AuditDistribDownloadEventFactory(
                CommonAuditData.newBuilder()
                        .description(DESCRIPTION)
                        .status(STATUS_OK)
                        .requestId(REQUEST_ID)
                        .serviceInstanceId(SERVICE_INSTANCE_ID)
                        .build(),
                new DistributionData(DIST_CONSUMER_ID, DIST_RESOURCE_URL));

        when(auditingDao.addRecord(any(AuditingGenericEvent.class), eq(AuditingActionEnum.DISTRIBUTION_ARTIFACT_DOWNLOAD.getAuditingEsType())))
                .thenReturn(ActionStatus.OK);
        when(cassandraDao.saveRecord(any(AuditingGenericEvent.class))).thenReturn(CassandraOperationStatus.OK);

        assertThat(auditingManager.auditEvent(factory)).isEqualTo(EXPECTED_DIST_DOWNLOAD_LOG_STR);
        verifyDownloadsEvent();
    }

    @Test
    public void testOldDownloadEvent() {
        when(auditingDao.addRecord(anyMap(), eq(AuditingActionEnum.DISTRIBUTION_ARTIFACT_DOWNLOAD.getAuditingEsType())))
                .thenReturn(ActionStatus.OK);
        when(cassandraDao.saveRecord(any(AuditingGenericEvent.class))).thenReturn(CassandraOperationStatus.OK);

        assertThat(auditingManager.auditEvent(fillDownloadMap())).isEqualTo(EXPECTED_DIST_DOWNLOAD_LOG_STR);
        verifyDownloadsEvent();
    }

    @Test
    public void testNewDeployEvent() {
        AuditEventFactory factory = new AuditDistribDeployEventFactory(
                CommonAuditData.newBuilder()
                        .description(DESCRIPTION)
                        .status(STATUS_OK)
                        .requestId(REQUEST_ID)
                        .serviceInstanceId(SERVICE_INSTANCE_ID)
                        .build(),
                CURRENT_VERSION,
                DIST_ID, user, RESOURCE_NAME, RESOURCE_TYPE);

        when(auditingDao.addRecord(any(AuditingGenericEvent.class), eq(AuditingActionEnum.DISTRIBUTION_DEPLOY.getAuditingEsType())))
                .thenReturn(ActionStatus.OK);
        when(cassandraDao.saveRecord(any(AuditingGenericEvent.class))).thenReturn(CassandraOperationStatus.OK);

        assertThat(auditingManager.auditEvent(factory)).isEqualTo(EXPECTED_DISTRIB_DEPLOY_LOG_STR);
        verifyDeployEvent();
    }

    @Test
    public void testOldDeployEvent() {
        when(auditingDao.addRecord(anyMap(), eq(AuditingActionEnum.DISTRIBUTION_DEPLOY.getAuditingEsType())))
                .thenReturn(ActionStatus.OK);
        when(cassandraDao.saveRecord(any(AuditingGenericEvent.class))).thenReturn(CassandraOperationStatus.OK);

        assertThat(auditingManager.auditEvent(fillDeployMap())).isEqualTo(EXPECTED_DISTRIB_DEPLOY_LOG_STR);
        verifyDeployEvent();
    }

    @Test
    public void testNewGetUebClusterEvent() {
        AuditEventFactory factory = new AuditGetUebClusterEventFactory(
                CommonAuditData.newBuilder()
                        .description(DESCRIPTION)
                        .status(STATUS_OK)
                        .requestId(REQUEST_ID)
                        .serviceInstanceId(SERVICE_INSTANCE_ID)
                        .build(),
                DIST_CONSUMER_ID);

        when(auditingDao.addRecord(any(AuditingGenericEvent.class), eq(AuditingActionEnum.GET_UEB_CLUSTER.getAuditingEsType())))
                .thenReturn(ActionStatus.OK);
        when(cassandraDao.saveRecord(any(AuditingGenericEvent.class))).thenReturn(CassandraOperationStatus.OK);

        assertThat(auditingManager.auditEvent(factory)).isEqualTo(EXPECTED_GET_UEB_CLUSTER_LOG_STR);
        verifyGetUebClusterEvent();
    }

    @Test
    public void testOldGetUebClusterEvent() {
        when(auditingDao.addRecord(anyMap(), eq(AuditingActionEnum.GET_UEB_CLUSTER.getAuditingEsType())))
                .thenReturn(ActionStatus.OK);
        when(cassandraDao.saveRecord(any(AuditingGenericEvent.class))).thenReturn(CassandraOperationStatus.OK);

        assertThat(auditingManager.auditEvent(fillGetUebClusterMap())).isEqualTo(EXPECTED_GET_UEB_CLUSTER_LOG_STR);
        verifyGetUebClusterEvent();
    }

    private void verifyNotifyEvent() {
        verify(cassandraDao).saveRecord(eventCaptor.capture());
        DistributionNotificationEvent storedEvent = (DistributionNotificationEvent) eventCaptor.getValue();
        assertThat(storedEvent.getCurrState()).isEqualTo(CURRENT_STATE);
        assertThat(storedEvent.getCurrVersion()).isEqualTo(CURRENT_VERSION);
        assertThat(storedEvent.getDesc()).isEqualTo(DESCRIPTION);
        assertThat(storedEvent.getStatus()).isEqualTo(STATUS_OK);
        assertThat(storedEvent.getRequestId()).isEqualTo(REQUEST_ID);
        assertThat(storedEvent.getServiceInstanceId()).isEqualTo(SERVICE_INSTANCE_ID);
        assertThat(storedEvent.getAction()).isEqualTo(AuditingActionEnum.DISTRIBUTION_NOTIFY.getName());
        assertThat(storedEvent.getDid()).isEqualTo(DIST_ID);
        assertThat(storedEvent.getModifier()).isEqualTo(USER_UID);
        assertThat(storedEvent.getResourceName()).isEqualTo(RESOURCE_NAME);
        assertThat(storedEvent.getResourceType()).isEqualTo(RESOURCE_TYPE);
        assertThat(storedEvent.getTopicName()).isEqualTo(TOPIC_NAME);
        assertThat(storedEvent.getVnfWorkloadContext()).isEqualTo(VNF_WORKLOAD_CONTEXT);
        assertThat(storedEvent.getEnvId()).isEqualTo(OP_ENV_ID);
        assertThat(storedEvent.getTenant()).isEqualTo(TENANT_CONTEXT);
    }

    private void verifyStatusEvent() {
        verify(cassandraDao).saveRecord(eventCaptor.capture());
        DistributionStatusEvent storedEvent = (DistributionStatusEvent) eventCaptor.getValue();
        assertThat(storedEvent.getConsumerId()).isEqualTo(DIST_CONSUMER_ID);
        assertThat(storedEvent.getDid()).isEqualTo(DIST_ID);
        assertThat(storedEvent.getDesc()).isEqualTo(DESCRIPTION);
        assertThat(storedEvent.getStatus()).isEqualTo(STATUS_OK);
        assertThat(storedEvent.getRequestId()).isEqualTo(REQUEST_ID);
        assertThat(storedEvent.getServiceInstanceId()).isEqualTo(SERVICE_INSTANCE_ID);
        assertThat(storedEvent.getAction()).isEqualTo(AuditingActionEnum.DISTRIBUTION_STATUS.getName());
        assertThat(storedEvent.getStatusTime()).isEqualTo(DIST_STATUS_TIME);
        assertThat(storedEvent.getResoureURL()).isEqualTo(DIST_RESOURCE_URL);
        assertThat(storedEvent.getTopicName()).isEqualTo(TOPIC_NAME);
    }

    private void verifyDownloadsEvent() {
        verify(cassandraDao).saveRecord(eventCaptor.capture());
        DistributionDownloadEvent storedEvent = (DistributionDownloadEvent) eventCaptor.getValue();
        assertThat(storedEvent.getConsumerId()).isEqualTo(DIST_CONSUMER_ID);
        assertThat(storedEvent.getDesc()).isEqualTo(DESCRIPTION);
        assertThat(storedEvent.getStatus()).isEqualTo(STATUS_OK);
        assertThat(storedEvent.getRequestId()).isEqualTo(REQUEST_ID);
        assertThat(storedEvent.getServiceInstanceId()).isEqualTo(SERVICE_INSTANCE_ID);
        assertThat(storedEvent.getResourceUrl()).isEqualTo(DIST_RESOURCE_URL);
    }

    private void verifyDeployEvent() {
        verify(cassandraDao).saveRecord(eventCaptor.capture());
        DistributionDeployEvent storedEvent = (DistributionDeployEvent) eventCaptor.getValue();
        assertThat(storedEvent.getCurrVersion()).isEqualTo(CURRENT_VERSION);
        assertThat(storedEvent.getDesc()).isEqualTo(DESCRIPTION);
        assertThat(storedEvent.getStatus()).isEqualTo(STATUS_OK);
        assertThat(storedEvent.getRequestId()).isEqualTo(REQUEST_ID);
        assertThat(storedEvent.getServiceInstanceId()).isEqualTo(SERVICE_INSTANCE_ID);
        assertThat(storedEvent.getDid()).isEqualTo(DIST_ID);
        assertThat(storedEvent.getModifier()).isEqualTo(USER_UID);
        assertThat(storedEvent.getResourceName()).isEqualTo(RESOURCE_NAME);
        assertThat(storedEvent.getResourceType()).isEqualTo(RESOURCE_TYPE);
    }

    private void verifyGetUebClusterEvent() {
        verify(cassandraDao).saveRecord(eventCaptor.capture());
        AuditingGetUebClusterEvent storedEvent = (AuditingGetUebClusterEvent) eventCaptor.getValue();
        assertThat(storedEvent.getConsumerId()).isEqualTo(DIST_CONSUMER_ID);
        assertThat(storedEvent.getDesc()).isEqualTo(DESCRIPTION);
        assertThat(storedEvent.getStatus()).isEqualTo(STATUS_OK);
        assertThat(storedEvent.getRequestId()).isEqualTo(REQUEST_ID);
        assertThat(storedEvent.getServiceInstanceId()).isEqualTo(SERVICE_INSTANCE_ID);
     }

    private EnumMap<AuditingFieldsKeysEnum, Object> fillNotifyMap() {
        EnumMap<AuditingFieldsKeysEnum, Object> auditingFields = new EnumMap<>(AuditingFieldsKeysEnum.class);
        auditingFields.put(AuditingFieldsKeysEnum.AUDIT_ACTION, AuditingActionEnum.DISTRIBUTION_NOTIFY.getName());
        auditingFields.put(AuditingFieldsKeysEnum.AUDIT_STATUS, STATUS_OK);
        auditingFields.put(AuditingFieldsKeysEnum.AUDIT_DESC, DESCRIPTION);
        auditingFields.put(AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_ID, DIST_ID);
        auditingFields.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_TYPE, RESOURCE_TYPE);
        auditingFields.put(AuditingFieldsKeysEnum.AUDIT_SERVICE_INSTANCE_ID, SERVICE_INSTANCE_ID);
        auditingFields.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_NAME, RESOURCE_NAME);
        auditingFields.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_CURR_STATE, CURRENT_STATE);
        auditingFields.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_CURR_VERSION, CURRENT_VERSION);
        auditingFields.put(AuditingFieldsKeysEnum.AUDIT_MODIFIER_UID, USER_ID);
        auditingFields.put(AuditingFieldsKeysEnum.AUDIT_MODIFIER_NAME, USER_FIRST_NAME + " " + USER_LAST_NAME);
        auditingFields.put(AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_TOPIC_NAME, TOPIC_NAME);
        auditingFields.put(AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_VNF_WORKLOAD_CONTEXT, VNF_WORKLOAD_CONTEXT);
        auditingFields.put(AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_TENANT, TENANT_CONTEXT);
        auditingFields.put(AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_ENVIRONMENT_ID, OP_ENV_ID);

        return auditingFields;
    }

    private EnumMap<AuditingFieldsKeysEnum, Object> fillStatusMap() {
       EnumMap<AuditingFieldsKeysEnum, Object> auditingFields = new EnumMap<>(AuditingFieldsKeysEnum.class);
        auditingFields.put(AuditingFieldsKeysEnum.AUDIT_ACTION, AuditingActionEnum.DISTRIBUTION_STATUS.getName());
        auditingFields.put(AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_TOPIC_NAME, TOPIC_NAME);
        auditingFields.put(AuditingFieldsKeysEnum.AUDIT_STATUS, STATUS_OK);
        auditingFields.put(AuditingFieldsKeysEnum.AUDIT_DESC, DESCRIPTION);
        auditingFields.put(AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_ID, DIST_ID);
        auditingFields.put(AuditingFieldsKeysEnum.AUDIT_SERVICE_INSTANCE_ID, SERVICE_INSTANCE_ID);
        auditingFields.put(AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_RESOURCE_URL, DIST_RESOURCE_URL);
        auditingFields.put(AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_STATUS_TIME, DIST_STATUS_TIME);
        auditingFields.put(AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_CONSUMER_ID, DIST_CONSUMER_ID);

        return auditingFields;
    }

    private EnumMap<AuditingFieldsKeysEnum, Object> fillDownloadMap() {
        EnumMap<AuditingFieldsKeysEnum, Object> auditingFields = new EnumMap<>(AuditingFieldsKeysEnum.class);
        auditingFields.put(AuditingFieldsKeysEnum.AUDIT_ACTION, AuditingActionEnum.DISTRIBUTION_ARTIFACT_DOWNLOAD.getName());
        auditingFields.put(AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_CONSUMER_ID, DIST_CONSUMER_ID);
        auditingFields.put(AuditingFieldsKeysEnum.AUDIT_STATUS, STATUS_OK);
        auditingFields.put(AuditingFieldsKeysEnum.AUDIT_DESC, DESCRIPTION);
        auditingFields.put(AuditingFieldsKeysEnum.AUDIT_REQUEST_ID, REQUEST_ID);
        auditingFields.put(AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_RESOURCE_URL, DIST_RESOURCE_URL);
        auditingFields.put(AuditingFieldsKeysEnum.AUDIT_SERVICE_INSTANCE_ID, SERVICE_INSTANCE_ID);

        return auditingFields;
    }

    private EnumMap<AuditingFieldsKeysEnum, Object> fillDeployMap() {
        EnumMap<AuditingFieldsKeysEnum, Object> auditingFields = new EnumMap<>(AuditingFieldsKeysEnum.class);
        auditingFields.put(AuditingFieldsKeysEnum.AUDIT_ACTION, AuditingActionEnum.DISTRIBUTION_DEPLOY.getName());
        auditingFields.put(AuditingFieldsKeysEnum.AUDIT_STATUS, STATUS_OK);
        auditingFields.put(AuditingFieldsKeysEnum.AUDIT_DESC, DESCRIPTION);
        auditingFields.put(AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_ID, DIST_ID);
        auditingFields.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_TYPE, RESOURCE_TYPE);
        auditingFields.put(AuditingFieldsKeysEnum.AUDIT_SERVICE_INSTANCE_ID, SERVICE_INSTANCE_ID);
        auditingFields.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_NAME, RESOURCE_NAME);
        auditingFields.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_CURR_VERSION, CURRENT_VERSION);
        auditingFields.put(AuditingFieldsKeysEnum.AUDIT_MODIFIER_UID, USER_ID);
        auditingFields.put(AuditingFieldsKeysEnum.AUDIT_MODIFIER_NAME, USER_FIRST_NAME + " " + USER_LAST_NAME);

        return auditingFields;
    }

    private EnumMap<AuditingFieldsKeysEnum, Object> fillGetUebClusterMap() {
        EnumMap<AuditingFieldsKeysEnum, Object> auditingFields = new EnumMap<>(AuditingFieldsKeysEnum.class);
        auditingFields.put(AuditingFieldsKeysEnum.AUDIT_ACTION, AuditingActionEnum.GET_UEB_CLUSTER.getName());
        auditingFields.put(AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_CONSUMER_ID, DIST_CONSUMER_ID);
        auditingFields.put(AuditingFieldsKeysEnum.AUDIT_STATUS, STATUS_OK);
        auditingFields.put(AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_STATUS_DESC, DESCRIPTION);
        auditingFields.put(AuditingFieldsKeysEnum.AUDIT_REQUEST_ID, REQUEST_ID);
        auditingFields.put(AuditingFieldsKeysEnum.AUDIT_SERVICE_INSTANCE_ID, SERVICE_INSTANCE_ID);

        return auditingFields;
    }




}
