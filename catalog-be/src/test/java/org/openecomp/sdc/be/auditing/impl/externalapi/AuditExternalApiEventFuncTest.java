package org.openecomp.sdc.be.auditing.impl.externalapi;

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
import org.openecomp.sdc.be.resources.data.auditing.ExternalApiEvent;
import org.openecomp.sdc.be.resources.data.auditing.model.CommonAuditData;
import org.openecomp.sdc.be.resources.data.auditing.model.ResourceAuditData;
import org.openecomp.sdc.common.datastructure.AuditingFieldsKeysEnum;

import java.util.EnumMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openecomp.sdc.be.auditing.impl.AuditTestUtils.ARTIFACT_DATA;
import static org.openecomp.sdc.be.auditing.impl.AuditTestUtils.ARTIFACT_UUID;
import static org.openecomp.sdc.be.auditing.impl.AuditTestUtils.CURRENT_STATE;
import static org.openecomp.sdc.be.auditing.impl.AuditTestUtils.CURRENT_VERSION;
import static org.openecomp.sdc.be.auditing.impl.AuditTestUtils.DESCRIPTION;
import static org.openecomp.sdc.be.auditing.impl.AuditTestUtils.DIST_CONSUMER_ID;
import static org.openecomp.sdc.be.auditing.impl.AuditTestUtils.DIST_RESOURCE_URL;
import static org.openecomp.sdc.be.auditing.impl.AuditTestUtils.EXPECTED_ACTIVATE_SERVICE_API_LOG_STR;
import static org.openecomp.sdc.be.auditing.impl.AuditTestUtils.EXPECTED_CHANGE_LIFECYCLE_EXTERNAL_API_LOG_STR;
import static org.openecomp.sdc.be.auditing.impl.AuditTestUtils.EXPECTED_DELETE_ARTIFACT_EXTERNAL_API_LOG_STR;
import static org.openecomp.sdc.be.auditing.impl.AuditTestUtils.EXPECTED_DOWNLOAD_ARTIFACT_EXTERNAL_API_LOG_STR;
import static org.openecomp.sdc.be.auditing.impl.AuditTestUtils.EXPECTED_GET_ASSET_LIST_LOG_STR;
import static org.openecomp.sdc.be.auditing.impl.AuditTestUtils.EXPECTED_GET_TOSCA_MODEL_LOG_STR;
import static org.openecomp.sdc.be.auditing.impl.AuditTestUtils.INVARIANT_UUID;
import static org.openecomp.sdc.be.auditing.impl.AuditTestUtils.MODIFIER_UID;
import static org.openecomp.sdc.be.auditing.impl.AuditTestUtils.PREV_RESOURCE_STATE;
import static org.openecomp.sdc.be.auditing.impl.AuditTestUtils.PREV_RESOURCE_VERSION;
import static org.openecomp.sdc.be.auditing.impl.AuditTestUtils.REQUEST_ID;
import static org.openecomp.sdc.be.auditing.impl.AuditTestUtils.RESOURCE_NAME;
import static org.openecomp.sdc.be.auditing.impl.AuditTestUtils.RESOURCE_TYPE;
import static org.openecomp.sdc.be.auditing.impl.AuditTestUtils.SERVICE_INSTANCE_ID;
import static org.openecomp.sdc.be.auditing.impl.AuditTestUtils.STATUS_OK;
import static org.openecomp.sdc.be.auditing.impl.AuditTestUtils.init;
import static org.openecomp.sdc.be.auditing.impl.AuditTestUtils.modifier;

@RunWith(MockitoJUnitRunner.class)
public class AuditExternalApiEventFuncTest {

    private AuditingManager auditingManager;

    @Mock
    private static AuditCassandraDao cassandraDao;
    @Mock
    private static AuditingDao auditingDao;
    @Mock
    private static Configuration.ElasticSearchConfig esConfig;

    @Captor
    private ArgumentCaptor<AuditingGenericEvent> eventCaptor;

    @Before
    public void setUp() {
        init(esConfig);
        auditingManager = new AuditingManager(auditingDao, cassandraDao);
    }

    @Test
    public void testNewActivateServiceEvent() {
        AuditEventFactory builder = new AuditActivateServiceExternalApiEventFactory(
                CommonAuditData.newBuilder()
                        .description(DESCRIPTION)
                        .status(STATUS_OK)
                        .requestId(REQUEST_ID)
                        .serviceInstanceId(SERVICE_INSTANCE_ID)
                        .build(),
                RESOURCE_TYPE, RESOURCE_NAME, DIST_CONSUMER_ID, DIST_RESOURCE_URL,
                ResourceAuditData.newBuilder()
                        .artifactUuid(ARTIFACT_UUID)
                        .state(PREV_RESOURCE_STATE)
                        .version(PREV_RESOURCE_VERSION)
                        .build(),
                ResourceAuditData.newBuilder()
                        .artifactUuid(ARTIFACT_UUID)
                        .state(CURRENT_STATE)
                        .version(CURRENT_VERSION)
                        .build(),
                INVARIANT_UUID, modifier, ARTIFACT_DATA);

        when(auditingDao.addRecord(any(AuditingGenericEvent.class), eq(AuditingActionEnum.ACTIVATE_SERVICE_BY_API.getAuditingEsType())))
                .thenReturn(ActionStatus.OK);
        when(cassandraDao.saveRecord(any(AuditingGenericEvent.class))).thenReturn(CassandraOperationStatus.OK);

        assertThat(auditingManager.auditEvent(builder)).isEqualTo(EXPECTED_ACTIVATE_SERVICE_API_LOG_STR);
        verifyExternalApiEvent(AuditingActionEnum.ACTIVATE_SERVICE_BY_API.getName());
    }

    @Test
    public void testOldActivateServiceEvent() {
        when(auditingDao.addRecord(anyMap(), eq(AuditingActionEnum.ACTIVATE_SERVICE_BY_API.getAuditingEsType())))
                .thenReturn(ActionStatus.OK);
        when(cassandraDao.saveRecord(any(AuditingGenericEvent.class))).thenReturn(CassandraOperationStatus.OK);

        assertThat(auditingManager.auditEvent(fillMap(AuditingActionEnum.ACTIVATE_SERVICE_BY_API))).isEqualTo(EXPECTED_ACTIVATE_SERVICE_API_LOG_STR);
        verifyExternalApiEvent(AuditingActionEnum.ACTIVATE_SERVICE_BY_API.getName());

    }

    @Test
    public void testNewDownloadArtifactEvent() {
        AuditEventFactory builder = new AuditDownloadArtifactExternalApiEventFactory(
                CommonAuditData.newBuilder()
                        .description(DESCRIPTION)
                        .status(STATUS_OK)
                        .requestId(REQUEST_ID)
                        .serviceInstanceId(SERVICE_INSTANCE_ID)
                        .build(),
                RESOURCE_TYPE, RESOURCE_NAME, DIST_CONSUMER_ID, DIST_RESOURCE_URL,
                ResourceAuditData.newBuilder()
                        .artifactUuid(ARTIFACT_UUID)
                        .state(PREV_RESOURCE_STATE)
                        .version(PREV_RESOURCE_VERSION)
                        .build(),
                ResourceAuditData.newBuilder()
                        .artifactUuid(ARTIFACT_UUID)
                        .state(CURRENT_STATE)
                        .version(CURRENT_VERSION)
                        .build(),
                INVARIANT_UUID, modifier, ARTIFACT_DATA);

        when(auditingDao.addRecord(any(AuditingGenericEvent.class), eq(AuditingActionEnum.DOWNLOAD_ARTIFACT.getAuditingEsType())))
                .thenReturn(ActionStatus.OK);
        when(cassandraDao.saveRecord(any(AuditingGenericEvent.class))).thenReturn(CassandraOperationStatus.OK);

        assertThat(auditingManager.auditEvent(builder)).isEqualTo(EXPECTED_DOWNLOAD_ARTIFACT_EXTERNAL_API_LOG_STR);
        verifyExternalApiEvent(AuditingActionEnum.DOWNLOAD_ARTIFACT.getName());
    }

    @Test
    public void testOldDownloadArtifactEvent() {
        when(auditingDao.addRecord(anyMap(), eq(AuditingActionEnum.DOWNLOAD_ARTIFACT.getAuditingEsType())))
                .thenReturn(ActionStatus.OK);
        when(cassandraDao.saveRecord(any(AuditingGenericEvent.class))).thenReturn(CassandraOperationStatus.OK);

        assertThat(auditingManager.auditEvent(fillMap(AuditingActionEnum.DOWNLOAD_ARTIFACT))).isEqualTo(EXPECTED_DOWNLOAD_ARTIFACT_EXTERNAL_API_LOG_STR + " ");
        verifyExternalApiEvent(AuditingActionEnum.DOWNLOAD_ARTIFACT.getName());
    }

    @Test
    public void testNewChangeLifecycleEvent() {
        AuditEventFactory factory = new AuditChangeLifecycleExternalApiEventFactory(
                CommonAuditData.newBuilder()
                        .description(DESCRIPTION)
                        .status(STATUS_OK)
                        .requestId(REQUEST_ID)
                        .serviceInstanceId(SERVICE_INSTANCE_ID)
                        .build(),
                RESOURCE_TYPE, RESOURCE_NAME, DIST_CONSUMER_ID, DIST_RESOURCE_URL,
                ResourceAuditData.newBuilder()
                        .artifactUuid(ARTIFACT_UUID)
                        .state(PREV_RESOURCE_STATE)
                        .version(PREV_RESOURCE_VERSION)
                        .build(),
                ResourceAuditData.newBuilder()
                        .artifactUuid(ARTIFACT_UUID)
                        .state(CURRENT_STATE)
                        .version(CURRENT_VERSION)
                        .build(),
                INVARIANT_UUID, modifier, ARTIFACT_DATA);

        when(auditingDao.addRecord(any(AuditingGenericEvent.class), eq(AuditingActionEnum.CHANGE_LIFECYCLE_BY_API.getAuditingEsType())))
                .thenReturn(ActionStatus.OK);
        when(cassandraDao.saveRecord(any(AuditingGenericEvent.class))).thenReturn(CassandraOperationStatus.OK);

        assertThat(auditingManager.auditEvent(factory)).isEqualTo(EXPECTED_CHANGE_LIFECYCLE_EXTERNAL_API_LOG_STR);
        verifyExternalApiEvent(AuditingActionEnum.CHANGE_LIFECYCLE_BY_API.getName());
    }

    @Test
    public void testOldChangeLifecycleEvent() {
        when(auditingDao.addRecord(anyMap(), eq(AuditingActionEnum.CHANGE_LIFECYCLE_BY_API.getAuditingEsType())))
                .thenReturn(ActionStatus.OK);
        when(cassandraDao.saveRecord(any(AuditingGenericEvent.class))).thenReturn(CassandraOperationStatus.OK);

        assertThat(auditingManager.auditEvent(fillMap(AuditingActionEnum.CHANGE_LIFECYCLE_BY_API))).isEqualTo(EXPECTED_CHANGE_LIFECYCLE_EXTERNAL_API_LOG_STR);
        verifyExternalApiEvent(AuditingActionEnum.CHANGE_LIFECYCLE_BY_API.getName());

    }

    @Test
    public void testNewDeleteArtifactEvent() {
        AuditEventFactory factory = new AuditDeleteArtByApiCrudExternalApiEventFactory(
                CommonAuditData.newBuilder()
                        .description(DESCRIPTION)
                        .status(STATUS_OK)
                        .requestId(REQUEST_ID)
                        .serviceInstanceId(SERVICE_INSTANCE_ID)
                        .build(),
                RESOURCE_TYPE, RESOURCE_NAME, DIST_CONSUMER_ID, DIST_RESOURCE_URL,
                ResourceAuditData.newBuilder()
                        .artifactUuid(ARTIFACT_UUID)
                        .state(PREV_RESOURCE_STATE)
                        .version(PREV_RESOURCE_VERSION)
                        .build(),
                ResourceAuditData.newBuilder()
                        .artifactUuid(ARTIFACT_UUID)
                        .state(CURRENT_STATE)
                        .version(CURRENT_VERSION)
                        .build(),
                INVARIANT_UUID, modifier, ARTIFACT_DATA);

        when(auditingDao.addRecord(any(AuditingGenericEvent.class), eq(AuditingActionEnum.ARTIFACT_DELETE_BY_API.getAuditingEsType())))
                .thenReturn(ActionStatus.OK);
        when(cassandraDao.saveRecord(any(AuditingGenericEvent.class))).thenReturn(CassandraOperationStatus.OK);

        assertThat(auditingManager.auditEvent(factory)).isEqualTo(EXPECTED_DELETE_ARTIFACT_EXTERNAL_API_LOG_STR);
        verifyExternalApiEvent(AuditingActionEnum.ARTIFACT_DELETE_BY_API.getName());
    }

    @Test
    public void testOldDeleteArtifactEvent() {
        when(auditingDao.addRecord(anyMap(), eq(AuditingActionEnum.ARTIFACT_DELETE_BY_API.getAuditingEsType())))
                .thenReturn(ActionStatus.OK);
        when(cassandraDao.saveRecord(any(AuditingGenericEvent.class))).thenReturn(CassandraOperationStatus.OK);

        assertThat(auditingManager.auditEvent(fillMap(AuditingActionEnum.ARTIFACT_DELETE_BY_API))).isEqualTo(EXPECTED_DELETE_ARTIFACT_EXTERNAL_API_LOG_STR + " ");
        verifyExternalApiEvent(AuditingActionEnum.ARTIFACT_DELETE_BY_API.getName());

    }

    @Test
    public void testNewGetAssetsListEvent() {
        AuditEventFactory factory = new AuditGetAssetListExternalApiEventFactory(
                CommonAuditData.newBuilder()
                        .description(DESCRIPTION)
                        .status(STATUS_OK)
                        .requestId(REQUEST_ID)
                        .serviceInstanceId(SERVICE_INSTANCE_ID)
                        .build(),
                RESOURCE_TYPE, RESOURCE_NAME, DIST_CONSUMER_ID, DIST_RESOURCE_URL,
                ResourceAuditData.newBuilder()
                        .artifactUuid(ARTIFACT_UUID)
                        .state(PREV_RESOURCE_STATE)
                        .version(PREV_RESOURCE_VERSION)
                        .build(),
                ResourceAuditData.newBuilder()
                        .artifactUuid(ARTIFACT_UUID)
                        .state(CURRENT_STATE)
                        .version(CURRENT_VERSION)
                        .build(),
                INVARIANT_UUID, modifier, ARTIFACT_DATA);

        when(auditingDao.addRecord(any(AuditingGenericEvent.class), eq(AuditingActionEnum.GET_ASSET_LIST.getAuditingEsType())))
                .thenReturn(ActionStatus.OK);
        when(cassandraDao.saveRecord(any(AuditingGenericEvent.class))).thenReturn(CassandraOperationStatus.OK);

        assertThat(auditingManager.auditEvent(factory)).isEqualTo(EXPECTED_GET_ASSET_LIST_LOG_STR);
        verifyExternalApiEvent(AuditingActionEnum.GET_ASSET_LIST.getName());
    }

    @Test
    public void testOldGetAssetsListEvent() {
        when(auditingDao.addRecord(anyMap(), eq(AuditingActionEnum.GET_ASSET_LIST.getAuditingEsType())))
                .thenReturn(ActionStatus.OK);
        when(cassandraDao.saveRecord(any(AuditingGenericEvent.class))).thenReturn(CassandraOperationStatus.OK);

        assertThat(auditingManager.auditEvent(fillMap(AuditingActionEnum.GET_ASSET_LIST))).isEqualTo(EXPECTED_GET_ASSET_LIST_LOG_STR);
        verifyExternalApiEvent(AuditingActionEnum.GET_ASSET_LIST.getName());
    }

    @Test
    public void testNewGetToscaModelEvent() {
        AuditEventFactory factory = new AuditGetToscaModelExternalApiEventFactory(
                CommonAuditData.newBuilder()
                        .description(DESCRIPTION)
                        .status(STATUS_OK)
                        .requestId(REQUEST_ID)
                        .serviceInstanceId(SERVICE_INSTANCE_ID)
                        .build(),
                RESOURCE_TYPE, RESOURCE_NAME, DIST_CONSUMER_ID, DIST_RESOURCE_URL,
                ResourceAuditData.newBuilder()
                        .artifactUuid(ARTIFACT_UUID)
                        .state(PREV_RESOURCE_STATE)
                        .version(PREV_RESOURCE_VERSION)
                        .build(),
                ResourceAuditData.newBuilder()
                        .artifactUuid(ARTIFACT_UUID)
                        .state(CURRENT_STATE)
                        .version(CURRENT_VERSION)
                        .build(),
                INVARIANT_UUID, modifier, ARTIFACT_DATA);

        when(auditingDao.addRecord(any(AuditingGenericEvent.class), eq(AuditingActionEnum.GET_TOSCA_MODEL.getAuditingEsType())))
                .thenReturn(ActionStatus.OK);
        when(cassandraDao.saveRecord(any(AuditingGenericEvent.class))).thenReturn(CassandraOperationStatus.OK);

        assertThat(auditingManager.auditEvent(factory)).isEqualTo(EXPECTED_GET_TOSCA_MODEL_LOG_STR);
        verifyExternalApiEvent(AuditingActionEnum.GET_TOSCA_MODEL.getName());
    }

    @Test
    public void testOldGetToscaModelEvent() {
        when(auditingDao.addRecord(anyMap(), eq(AuditingActionEnum.GET_TOSCA_MODEL.getAuditingEsType())))
                .thenReturn(ActionStatus.OK);
        when(cassandraDao.saveRecord(any(AuditingGenericEvent.class))).thenReturn(CassandraOperationStatus.OK);

        assertThat(auditingManager.auditEvent(fillMap(AuditingActionEnum.GET_TOSCA_MODEL))).isEqualTo(EXPECTED_GET_TOSCA_MODEL_LOG_STR);
        verifyExternalApiEvent(AuditingActionEnum.GET_TOSCA_MODEL.getName());
    }

    private void verifyExternalApiEvent(String action) {
        verify(cassandraDao).saveRecord(eventCaptor.capture());
        ExternalApiEvent storedEvent = (ExternalApiEvent) eventCaptor.getValue();
        assertThat(storedEvent.getModifier()).isEqualTo(MODIFIER_UID);
        assertThat(storedEvent.getDesc()).isEqualTo(DESCRIPTION);
        assertThat(storedEvent.getStatus()).isEqualTo(STATUS_OK);
        assertThat(storedEvent.getServiceInstanceId()).isEqualTo(SERVICE_INSTANCE_ID);
        assertThat(storedEvent.getAction()).isEqualTo(action);
        assertThat(storedEvent.getArtifactData()).isEqualTo(ARTIFACT_DATA);
        assertThat(storedEvent.getCurrArtifactUuid()).isEqualTo(ARTIFACT_UUID);
        assertThat(storedEvent.getPrevArtifactUuid()).isEqualTo(ARTIFACT_UUID);
        assertThat(storedEvent.getPrevState()).isEqualTo(PREV_RESOURCE_STATE);
        assertThat(storedEvent.getCurrState()).isEqualTo(CURRENT_STATE);
        assertThat(storedEvent.getPrevVersion()).isEqualTo(PREV_RESOURCE_VERSION);
        assertThat(storedEvent.getCurrVersion()).isEqualTo(CURRENT_VERSION);
        assertThat(storedEvent.getInvariantUuid()).isEqualTo(INVARIANT_UUID);
        assertThat(storedEvent.getResourceName()).isEqualTo(RESOURCE_NAME);
        assertThat(storedEvent.getResourceType()).isEqualTo(RESOURCE_TYPE);

    }

    private EnumMap<AuditingFieldsKeysEnum, Object> fillMap(AuditingActionEnum action) {
        EnumMap<AuditingFieldsKeysEnum, Object> auditingFields = new EnumMap<>(AuditingFieldsKeysEnum.class);
        auditingFields.put(AuditingFieldsKeysEnum.AUDIT_ACTION, action.getName());
        auditingFields.put(AuditingFieldsKeysEnum.AUDIT_MODIFIER_NAME, modifier.getFirstName() + " " + modifier.getLastName());
        auditingFields.put(AuditingFieldsKeysEnum.AUDIT_MODIFIER_UID, modifier.getUserId());
        auditingFields.put(AuditingFieldsKeysEnum.AUDIT_STATUS, STATUS_OK);
        auditingFields.put(AuditingFieldsKeysEnum.AUDIT_DESC, DESCRIPTION);

        auditingFields.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_TYPE, RESOURCE_TYPE);
        auditingFields.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_PREV_VERSION, PREV_RESOURCE_VERSION);
        auditingFields.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_PREV_STATE, PREV_RESOURCE_STATE);
        auditingFields.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_NAME, RESOURCE_NAME);
        auditingFields.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_CURR_VERSION, CURRENT_VERSION);
        auditingFields.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_CURR_STATE, CURRENT_STATE);
        auditingFields.put(AuditingFieldsKeysEnum.AUDIT_SERVICE_INSTANCE_ID, SERVICE_INSTANCE_ID);
        auditingFields.put(AuditingFieldsKeysEnum.AUDIT_INVARIANT_UUID, INVARIANT_UUID);
        auditingFields.put(AuditingFieldsKeysEnum.AUDIT_ARTIFACT_DATA, ARTIFACT_DATA);
        auditingFields.put(AuditingFieldsKeysEnum.AUDIT_CURR_ARTIFACT_UUID, ARTIFACT_UUID);
        auditingFields.put(AuditingFieldsKeysEnum.AUDIT_PREV_ARTIFACT_UUID, ARTIFACT_UUID);
        auditingFields.put(AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_CONSUMER_ID, DIST_CONSUMER_ID);
        auditingFields.put(AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_RESOURCE_URL, DIST_RESOURCE_URL);
        return auditingFields;
    }



}
