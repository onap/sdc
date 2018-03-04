package org.openecomp.sdc.be.auditing.impl.resourceadmin;

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
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.resources.data.auditing.AuditingGenericEvent;
import org.openecomp.sdc.be.resources.data.auditing.ResourceAdminEvent;
import org.openecomp.sdc.be.resources.data.auditing.model.CommonAuditData;
import org.openecomp.sdc.be.resources.data.auditing.model.ResourceAuditData;
import org.openecomp.sdc.common.api.ConfigurationSource;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.datastructure.AuditingFieldsKeysEnum;
import org.openecomp.sdc.common.util.ThreadLocalsHolder;

import java.util.EnumMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openecomp.sdc.be.auditing.impl.AuditTestUtils.*;

@RunWith(MockitoJUnitRunner.class)
public class AuditResourceAdminEventFuncTest {
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
    public void testNewCheckInResourceAdminEvent() {
       Resource resource = new Resource();
        resource.setName(RESOURCE_NAME);
        resource.setUUID(SERVICE_INSTANCE_ID);
        resource.setInvariantUUID(INVARIANT_UUID);
        resource.setLifecycleState(LifecycleStateEnum.CERTIFIED);
        resource.setVersion(CURRENT_VERSION);

        AuditEventFactory factory = new AuditCertificationResourceAdminEventFactory(
                AuditingActionEnum.CHECKIN_RESOURCE,
                CommonAuditData.newBuilder()
                        .description(DESCRIPTION)
                        .status(STATUS_OK)
                        .requestId(REQUEST_ID)
                        .serviceInstanceId(SERVICE_INSTANCE_ID)
                        .build(),
                ResourceAuditData.newBuilder()
                        .artifactUuid(ARTIFACT_UUID)
                        .state(PREV_RESOURCE_STATE)
                        .version(PREV_RESOURCE_VERSION)
                        .distributionStatus(DPREV_STATUS)
                        .build(),
                ResourceAuditData.newBuilder()
                        .artifactUuid(ARTIFACT_UUID)
                        .state(CURRENT_STATE)
                        .version(CURRENT_VERSION)
                        .distributionStatus(DCURR_STATUS)
                        .build(),
                RESOURCE_TYPE_VFC, RESOURCE_NAME, INVARIANT_UUID, modifier,
                ARTIFACT_DATA, COMMENT, DIST_ID);

        when(auditingDao.addRecord(any(AuditingGenericEvent.class), eq(AuditingActionEnum.CHECKIN_RESOURCE.getAuditingEsType())))
                .thenReturn(ActionStatus.OK);
        when(cassandraDao.saveRecord(any(AuditingGenericEvent.class))).thenReturn(CassandraOperationStatus.OK);

        assertThat(auditingManager.auditEvent(factory)).isEqualTo(EXPECTED_CHECK_IN_RESOURCE_LOG_STR);
        verifyResourceAdminEvent(AuditingActionEnum.CHECKIN_RESOURCE.getName());
    }

    @Test
    public void testOldCheckInResourceAdminEvent() {
        when(auditingDao.addRecord(anyMap(), eq(AuditingActionEnum.CHECKIN_RESOURCE.getAuditingEsType())))
                .thenReturn(ActionStatus.OK);
        when(cassandraDao.saveRecord(any(AuditingGenericEvent.class))).thenReturn(CassandraOperationStatus.OK);

        assertThat(auditingManager.auditEvent(fillMap(AuditingActionEnum.CHECKIN_RESOURCE))).isEqualTo(EXPECTED_CHECK_IN_RESOURCE_LOG_STR + " ");
        verifyResourceAdminEvent(AuditingActionEnum.CHECKIN_RESOURCE.getName());

    }

    private EnumMap<AuditingFieldsKeysEnum, Object> fillMap(AuditingActionEnum action) {
        EnumMap<AuditingFieldsKeysEnum, Object> auditingFields = new EnumMap<>(AuditingFieldsKeysEnum.class);
        auditingFields.put(AuditingFieldsKeysEnum.AUDIT_ACTION, action.getName());
        auditingFields.put(AuditingFieldsKeysEnum.AUDIT_MODIFIER_NAME, modifier.getFirstName() + " " + modifier.getLastName());
        auditingFields.put(AuditingFieldsKeysEnum.AUDIT_MODIFIER_UID, modifier.getUserId());
        auditingFields.put(AuditingFieldsKeysEnum.AUDIT_STATUS, STATUS_OK);
        auditingFields.put(AuditingFieldsKeysEnum.AUDIT_DESC, DESCRIPTION);

        auditingFields.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_TYPE, RESOURCE_TYPE_VFC);
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
        auditingFields.put(AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_ID, DIST_ID);
        auditingFields.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_COMMENT, COMMENT);
        auditingFields.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_DCURR_STATUS, DCURR_STATUS);
        auditingFields.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_DPREV_STATUS, DPREV_STATUS);

        if (action == AuditingActionEnum.IMPORT_RESOURCE) {
            auditingFields.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_TOSCA_NODE_TYPE, TOSCA_NODE_TYPE);
        }
        else {
            auditingFields.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_TOSCA_NODE_TYPE, Constants.EMPTY_STRING);
        }

        return auditingFields;
    }

    @Test
    public void testNewCreateResourceAdminEvent() {

      AuditEventFactory factory = new AuditCreateUpdateResourceAdminEventFactory(
             AuditingActionEnum.CREATE_RESOURCE,
             CommonAuditData.newBuilder()
                     .description(DESCRIPTION)
                     .status(STATUS_OK)
                     .requestId(REQUEST_ID)
                     .serviceInstanceId(SERVICE_INSTANCE_ID)
                     .build(),
              ResourceAuditData.newBuilder()
                      .artifactUuid(ARTIFACT_UUID)
                      .state(PREV_RESOURCE_STATE)
                      .version(PREV_RESOURCE_VERSION)
                      .distributionStatus(DPREV_STATUS)
                      .build(),
              ResourceAuditData.newBuilder()
                      .artifactUuid(ARTIFACT_UUID)
                      .state(CURRENT_STATE)
                      .version(CURRENT_VERSION)
                      .distributionStatus(DCURR_STATUS)
                      .build(),
             RESOURCE_TYPE_VFC, RESOURCE_NAME, INVARIANT_UUID, modifier,
             ARTIFACT_DATA, COMMENT, DIST_ID, Constants.EMPTY_STRING);

        when(auditingDao.addRecord(any(AuditingGenericEvent.class), eq(AuditingActionEnum.CREATE_RESOURCE.getAuditingEsType())))
                .thenReturn(ActionStatus.OK);
        when(cassandraDao.saveRecord(any(AuditingGenericEvent.class))).thenReturn(CassandraOperationStatus.OK);

        assertThat(auditingManager.auditEvent(factory)).isEqualTo(EXPECTED_CREATE_RESOURCE_LOG_STR);
        verifyResourceAdminEvent(AuditingActionEnum.CREATE_RESOURCE.getName());
    }

    @Test
    public void testOldCreateResourceAdminEvent() {
        when(auditingDao.addRecord(anyMap(), eq(AuditingActionEnum.CREATE_RESOURCE.getAuditingEsType())))
                .thenReturn(ActionStatus.OK);
        when(cassandraDao.saveRecord(any(AuditingGenericEvent.class))).thenReturn(CassandraOperationStatus.OK);

        assertThat(auditingManager.auditEvent(fillMap(AuditingActionEnum.CREATE_RESOURCE))).isEqualTo(EXPECTED_CREATE_RESOURCE_LOG_STR + " ");
        verifyResourceAdminEvent(AuditingActionEnum.CREATE_RESOURCE.getName());

    }

    @Test
    public void testNewImportResourceAdminEvent() {

        Resource resource = new Resource();
        resource.setName(RESOURCE_NAME);
        resource.setVersion(CURRENT_VERSION);
        resource.setInvariantUUID(INVARIANT_UUID);
        resource.setUUID(SERVICE_INSTANCE_ID);
        resource.setState(LifecycleStateEnum.CERTIFIED);
        resource.setToscaType(TOSCA_NODE_TYPE);

        AuditEventFactory factory = new AuditImportResourceAdminEventFactory(
                CommonAuditData.newBuilder()
                        .description(DESCRIPTION)
                        .status(STATUS_OK)
                        .requestId(REQUEST_ID)
                        .serviceInstanceId(SERVICE_INSTANCE_ID)
                        .build(),
                ResourceAuditData.newBuilder()
                        .artifactUuid(ARTIFACT_UUID)
                        .state(PREV_RESOURCE_STATE)
                        .version(PREV_RESOURCE_VERSION)
                        .distributionStatus(DPREV_STATUS)
                        .build(),
                ResourceAuditData.newBuilder()
                        .artifactUuid(ARTIFACT_UUID)
                        .state(CURRENT_STATE)
                        .version(CURRENT_VERSION)
                        .distributionStatus(DCURR_STATUS)
                        .build(),
                RESOURCE_TYPE_VFC, RESOURCE_NAME, INVARIANT_UUID, modifier,
                ARTIFACT_DATA, COMMENT, DIST_ID, TOSCA_NODE_TYPE);

        when(auditingDao.addRecord(any(AuditingGenericEvent.class), eq(AuditingActionEnum.IMPORT_RESOURCE.getAuditingEsType())))
                .thenReturn(ActionStatus.OK);
        when(cassandraDao.saveRecord(any(AuditingGenericEvent.class))).thenReturn(CassandraOperationStatus.OK);

        assertThat(auditingManager.auditEvent(factory)).isEqualTo(EXPECTED_IMPORT_RESOURCE_LOG_STR);
        verifyResourceAdminEvent(AuditingActionEnum.IMPORT_RESOURCE.getName());
    }

    @Test
    public void testOldImportResourceAdminEvent() {
        when(auditingDao.addRecord(anyMap(), eq(AuditingActionEnum.IMPORT_RESOURCE.getAuditingEsType())))
                .thenReturn(ActionStatus.OK);
        when(cassandraDao.saveRecord(any(AuditingGenericEvent.class))).thenReturn(CassandraOperationStatus.OK);

        assertThat(auditingManager.auditEvent(fillMap(AuditingActionEnum.IMPORT_RESOURCE))).isEqualTo(EXPECTED_IMPORT_RESOURCE_LOG_STR + " ");
        verifyResourceAdminEvent(AuditingActionEnum.IMPORT_RESOURCE.getName());

    }

    @Test
    public void testNewArtifactUploadResourceAdminEvent() {

        AuditEventFactory factory = new AuditArtifactResourceAdminEventFactory(
                AuditingActionEnum.ARTIFACT_UPLOAD,
                CommonAuditData.newBuilder()
                        .description(DESCRIPTION)
                        .status(STATUS_OK)
                        .requestId(REQUEST_ID)
                        .serviceInstanceId(SERVICE_INSTANCE_ID)
                        .build(),
                ResourceAuditData.newBuilder()
                        .artifactUuid(ARTIFACT_UUID)
                        .state(PREV_RESOURCE_STATE)
                        .version(PREV_RESOURCE_VERSION)
                        .distributionStatus(DPREV_STATUS)
                        .build(),
                ResourceAuditData.newBuilder()
                        .artifactUuid(ARTIFACT_UUID)
                        .state(CURRENT_STATE)
                        .version(CURRENT_VERSION)
                        .distributionStatus(DCURR_STATUS)
                        .build(),
                RESOURCE_TYPE_VFC, RESOURCE_NAME, INVARIANT_UUID, modifier,
                ARTIFACT_DATA, COMMENT, DIST_ID);

        when(auditingDao.addRecord(any(AuditingGenericEvent.class), eq(AuditingActionEnum.ARTIFACT_UPLOAD.getAuditingEsType())))
                .thenReturn(ActionStatus.OK);
        when(cassandraDao.saveRecord(any(AuditingGenericEvent.class))).thenReturn(CassandraOperationStatus.OK);

        assertThat(auditingManager.auditEvent(factory)).isEqualTo(EXPECTED_ARTIFACT_UPLOAD_LOG_STR);
        verifyResourceAdminEvent(AuditingActionEnum.ARTIFACT_UPLOAD.getName());
    }

    @Test
    public void testOldArtifactUploadResourceAdminEvent() {
        when(auditingDao.addRecord(anyMap(), eq(AuditingActionEnum.IMPORT_RESOURCE.getAuditingEsType())))
                .thenReturn(ActionStatus.OK);
        when(cassandraDao.saveRecord(any(AuditingGenericEvent.class))).thenReturn(CassandraOperationStatus.OK);

        assertThat(auditingManager.auditEvent(fillMap(AuditingActionEnum.ARTIFACT_UPLOAD))).isEqualTo(EXPECTED_ARTIFACT_UPLOAD_LOG_STR + " ");
        verifyResourceAdminEvent(AuditingActionEnum.ARTIFACT_UPLOAD.getName());

    }

    @Test
    public void testNewDistStateChangeRequestResourceAdminEvent() {

        AuditEventFactory factory = new AuditDistStateChangeRequestResourceAdminEventFactory(
                CommonAuditData.newBuilder()
                        .description(DESCRIPTION)
                        .status(STATUS_OK)
                        .requestId(REQUEST_ID)
                        .serviceInstanceId(SERVICE_INSTANCE_ID)
                        .build(),
                ResourceAuditData.newBuilder()
                        .artifactUuid(ARTIFACT_UUID)
                        .state(PREV_RESOURCE_STATE)
                        .version(PREV_RESOURCE_VERSION)
                        .distributionStatus(DPREV_STATUS)
                        .build(),
                ResourceAuditData.newBuilder()
                        .artifactUuid(ARTIFACT_UUID)
                        .state(CURRENT_STATE)
                        .version(CURRENT_VERSION)
                        .distributionStatus(DCURR_STATUS)
                        .build(),
                RESOURCE_TYPE_VFC, RESOURCE_NAME, INVARIANT_UUID, modifier,
                ARTIFACT_DATA, COMMENT, DIST_ID);

        when(auditingDao.addRecord(any(AuditingGenericEvent.class), eq(AuditingActionEnum.DISTRIBUTION_STATE_CHANGE_REQUEST.getAuditingEsType())))
                .thenReturn(ActionStatus.OK);
        when(cassandraDao.saveRecord(any(AuditingGenericEvent.class))).thenReturn(CassandraOperationStatus.OK);

        assertThat(auditingManager.auditEvent(factory)).isEqualTo(EXPECTED_DIST_STATE_CHANGE_REQUEST);
        verifyResourceAdminEvent(AuditingActionEnum.DISTRIBUTION_STATE_CHANGE_REQUEST.getName());
    }

    @Test
    public void testOldDistStateChangeRequestResourceAdminEvent() {
        when(auditingDao.addRecord(anyMap(), eq(AuditingActionEnum.DISTRIBUTION_STATE_CHANGE_REQUEST.getAuditingEsType())))
                .thenReturn(ActionStatus.OK);
        when(cassandraDao.saveRecord(any(AuditingGenericEvent.class))).thenReturn(CassandraOperationStatus.OK);

        assertThat(auditingManager.auditEvent(fillMap(AuditingActionEnum.DISTRIBUTION_STATE_CHANGE_REQUEST))).isEqualTo(EXPECTED_DIST_STATE_CHANGE_REQUEST);
        verifyResourceAdminEvent(AuditingActionEnum.DISTRIBUTION_STATE_CHANGE_REQUEST.getName());
    }

    @Test
    public void testNewDistStateChangeApprovResourceAdminEvent() {

        AuditEventFactory factory = new AuditDistStateChangeResourceAdminEventFactory(
                AuditingActionEnum.DISTRIBUTION_STATE_CHANGE_APPROV,
                CommonAuditData.newBuilder()
                        .description(DESCRIPTION)
                        .status(STATUS_OK)
                        .requestId(REQUEST_ID)
                        .serviceInstanceId(SERVICE_INSTANCE_ID)
                        .build(),
                 ResourceAuditData.newBuilder()
                        .artifactUuid(ARTIFACT_UUID)
                        .state(PREV_RESOURCE_STATE)
                        .version(PREV_RESOURCE_VERSION)
                        .distributionStatus(DPREV_STATUS)
                        .build(),
                ResourceAuditData.newBuilder()
                        .artifactUuid(ARTIFACT_UUID)
                        .state(CURRENT_STATE)
                        .version(CURRENT_VERSION)
                        .distributionStatus(DCURR_STATUS)
                        .build(),
                RESOURCE_TYPE_VFC, RESOURCE_NAME, INVARIANT_UUID, modifier,
                ARTIFACT_DATA, COMMENT, DIST_ID);

        when(auditingDao.addRecord(any(AuditingGenericEvent.class), eq(AuditingActionEnum.DISTRIBUTION_STATE_CHANGE_REQUEST.getAuditingEsType())))
                .thenReturn(ActionStatus.OK);
        when(cassandraDao.saveRecord(any(AuditingGenericEvent.class))).thenReturn(CassandraOperationStatus.OK);

        assertThat(auditingManager.auditEvent(factory)).isEqualTo(EXPECTED_DIST_STATE_CHANGE_APPROV);
        verifyResourceAdminEvent(AuditingActionEnum.DISTRIBUTION_STATE_CHANGE_APPROV.getName());
    }

    @Test
    public void testOldDistStateChangeApprovResourceAdminEvent() {
        when(auditingDao.addRecord(anyMap(), eq(AuditingActionEnum.DISTRIBUTION_STATE_CHANGE_APPROV.getAuditingEsType())))
                .thenReturn(ActionStatus.OK);
        when(cassandraDao.saveRecord(any(AuditingGenericEvent.class))).thenReturn(CassandraOperationStatus.OK);

        assertThat(auditingManager.auditEvent(fillMap(AuditingActionEnum.DISTRIBUTION_STATE_CHANGE_APPROV))).isEqualTo(EXPECTED_DIST_STATE_CHANGE_APPROV);
        verifyResourceAdminEvent(AuditingActionEnum.DISTRIBUTION_STATE_CHANGE_APPROV.getName());
    }


    private void verifyResourceAdminEvent(String action) {
        verify(cassandraDao).saveRecord(eventCaptor.capture());
        ResourceAdminEvent storedEvent = (ResourceAdminEvent) eventCaptor.getValue();
        assertThat(storedEvent.getModifier()).isEqualTo(MODIFIER_UID);
        assertThat(storedEvent.getDesc()).isEqualTo(DESCRIPTION);
        assertThat(storedEvent.getRequestId()).isEqualTo(REQUEST_ID);
        assertThat(storedEvent.getServiceInstanceId()).isEqualTo(SERVICE_INSTANCE_ID);
        assertThat(storedEvent.getAction()).isEqualTo(action);
        assertThat(storedEvent.getArtifactData()).isEqualTo(ARTIFACT_DATA);
        assertThat(storedEvent.getComment()).isEqualTo(COMMENT);
        assertThat(storedEvent.getCurrArtifactUUID()).isEqualTo(ARTIFACT_UUID);
        assertThat(storedEvent.getPrevArtifactUUID()).isEqualTo(ARTIFACT_UUID);
        assertThat(storedEvent.getPrevState()).isEqualTo(PREV_RESOURCE_STATE);
        assertThat(storedEvent.getCurrState()).isEqualTo(CURRENT_STATE);
        assertThat(storedEvent.getPrevVersion()).isEqualTo(PREV_RESOURCE_VERSION);
        assertThat(storedEvent.getCurrVersion()).isEqualTo(CURRENT_VERSION);
        assertThat(storedEvent.getDcurrStatus()).isEqualTo(DCURR_STATUS);
        assertThat(storedEvent.getDprevStatus()).isEqualTo(DPREV_STATUS);
        assertThat(storedEvent.getDid()).isEqualTo(DIST_ID);
        assertThat(storedEvent.getInvariantUUID()).isEqualTo(INVARIANT_UUID);
        assertThat(storedEvent.getResourceName()).isEqualTo(RESOURCE_NAME);
        assertThat(storedEvent.getResourceType()).isEqualTo(RESOURCE_TYPE_VFC);

        if (action.equals(AuditingActionEnum.IMPORT_RESOURCE.getName())) {
            assertThat(storedEvent.getToscaNodeType()).isEqualTo(TOSCA_NODE_TYPE);
        } else {
            assertThat(storedEvent.getToscaNodeType()).isEmpty();
        }
    }
}
