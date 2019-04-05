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
package org.openecomp.sdc.be.auditing.impl.resourceadmin;

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
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.resources.data.auditing.AuditingGenericEvent;
import org.openecomp.sdc.be.resources.data.auditing.ResourceAdminEvent;
import org.openecomp.sdc.be.resources.data.auditing.model.CommonAuditData;
import org.openecomp.sdc.be.resources.data.auditing.model.ResourceCommonInfo;
import org.openecomp.sdc.be.resources.data.auditing.model.ResourceVersionInfo;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.util.ThreadLocalsHolder;
import org.openecomp.sdc.test.utils.TestConfigurationProvider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openecomp.sdc.be.auditing.impl.AuditTestUtils.*;

@RunWith(MockitoJUnitRunner.class)
public class AuditResourceAdminEventFuncTest {
    private AuditingManager auditingManager;

    @Mock
    private static AuditCassandraDao cassandraDao;
    @Mock
    private static AuditingDao auditingDao;
    @Mock
    private static Configuration.ElasticSearchConfig esConfig;

    @Captor
    private ArgumentCaptor<ResourceAdminEvent> eventCaptor;

    @Before
    public void setUp() {
        init(esConfig);
        auditingManager = new AuditingManager(auditingDao, cassandraDao, new TestConfigurationProvider());
        ThreadLocalsHolder.setUuid(REQUEST_ID);
    }

    @Test
    public void testCheckInResourceAdminEvent() {
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
                new ResourceCommonInfo(RESOURCE_NAME, RESOURCE_TYPE_VFC),
                ResourceVersionInfo.newBuilder()
                        .artifactUuid(ARTIFACT_UUID)
                        .state(PREV_RESOURCE_STATE)
                        .version(PREV_RESOURCE_VERSION)
                        .distributionStatus(DPREV_STATUS)
                        .build(),
                ResourceVersionInfo.newBuilder()
                        .artifactUuid(ARTIFACT_UUID)
                        .state(CURRENT_STATE)
                        .version(CURRENT_VERSION)
                        .distributionStatus(DCURR_STATUS)
                        .build(),
                INVARIANT_UUID, modifier,
                ARTIFACT_DATA, COMMENT, DIST_ID);

        when(auditingDao.addRecord(any(AuditingGenericEvent.class), eq(AuditingActionEnum.CHECKIN_RESOURCE.getAuditingEsType())))
                .thenReturn(ActionStatus.OK);
        when(cassandraDao.saveRecord(any(AuditingGenericEvent.class))).thenReturn(CassandraOperationStatus.OK);

        assertThat(auditingManager.auditEvent(factory)).isEqualTo(EXPECTED_CHECK_IN_RESOURCE_LOG_STR);
        verifyResourceAdminEvent(AuditingActionEnum.CHECKIN_RESOURCE.getName());
    }

    @Test
    public void testCreateResourceAdminEvent() {

      AuditEventFactory factory = new AuditCreateUpdateResourceAdminEventFactory(
             AuditingActionEnum.CREATE_RESOURCE,
             CommonAuditData.newBuilder()
                     .description(DESCRIPTION)
                     .status(STATUS_OK)
                     .requestId(REQUEST_ID)
                     .serviceInstanceId(SERVICE_INSTANCE_ID)
                     .build(),
              new ResourceCommonInfo(RESOURCE_NAME, RESOURCE_TYPE_VFC),
              ResourceVersionInfo.newBuilder()
                      .artifactUuid(ARTIFACT_UUID)
                      .state(PREV_RESOURCE_STATE)
                      .version(PREV_RESOURCE_VERSION)
                      .distributionStatus(DPREV_STATUS)
                      .build(),
              ResourceVersionInfo.newBuilder()
                      .artifactUuid(ARTIFACT_UUID)
                      .state(CURRENT_STATE)
                      .version(CURRENT_VERSION)
                      .distributionStatus(DCURR_STATUS)
                      .build(),
             INVARIANT_UUID, modifier,
             ARTIFACT_DATA, COMMENT, DIST_ID, Constants.EMPTY_STRING);

        when(auditingDao.addRecord(any(AuditingGenericEvent.class), eq(AuditingActionEnum.CREATE_RESOURCE.getAuditingEsType())))
                .thenReturn(ActionStatus.OK);
        when(cassandraDao.saveRecord(any(AuditingGenericEvent.class))).thenReturn(CassandraOperationStatus.OK);

        assertThat(auditingManager.auditEvent(factory)).isEqualTo(EXPECTED_CREATE_RESOURCE_LOG_STR);
        verifyResourceAdminEvent(AuditingActionEnum.CREATE_RESOURCE.getName());
    }

    @Test
    public void testImportResourceAdminEvent() {

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
                new ResourceCommonInfo(RESOURCE_NAME, RESOURCE_TYPE_VFC),
                ResourceVersionInfo.newBuilder()
                        .artifactUuid(ARTIFACT_UUID)
                        .state(PREV_RESOURCE_STATE)
                        .version(PREV_RESOURCE_VERSION)
                        .distributionStatus(DPREV_STATUS)
                        .build(),
                ResourceVersionInfo.newBuilder()
                        .artifactUuid(ARTIFACT_UUID)
                        .state(CURRENT_STATE)
                        .version(CURRENT_VERSION)
                        .distributionStatus(DCURR_STATUS)
                        .build(),
                INVARIANT_UUID, modifier,
                ARTIFACT_DATA, COMMENT, DIST_ID, TOSCA_NODE_TYPE);

        when(auditingDao.addRecord(any(AuditingGenericEvent.class), eq(AuditingActionEnum.IMPORT_RESOURCE.getAuditingEsType())))
                .thenReturn(ActionStatus.OK);
        when(cassandraDao.saveRecord(any(AuditingGenericEvent.class))).thenReturn(CassandraOperationStatus.OK);

        assertThat(auditingManager.auditEvent(factory)).isEqualTo(EXPECTED_IMPORT_RESOURCE_LOG_STR);
        verifyResourceAdminEvent(AuditingActionEnum.IMPORT_RESOURCE.getName());
    }

    @Test
    public void testArtifactUploadResourceAdminEvent() {

        AuditEventFactory factory = new AuditArtifactResourceAdminEventFactory(
                AuditingActionEnum.ARTIFACT_UPLOAD,
                CommonAuditData.newBuilder()
                        .description(DESCRIPTION)
                        .status(STATUS_OK)
                        .requestId(REQUEST_ID)
                        .serviceInstanceId(SERVICE_INSTANCE_ID)
                        .build(),
                new ResourceCommonInfo(RESOURCE_NAME, RESOURCE_TYPE_VFC),
                ResourceVersionInfo.newBuilder()
                        .artifactUuid(ARTIFACT_UUID)
                        .state(PREV_RESOURCE_STATE)
                        .version(PREV_RESOURCE_VERSION)
                        .distributionStatus(DPREV_STATUS)
                        .build(),
                ResourceVersionInfo.newBuilder()
                        .artifactUuid(ARTIFACT_UUID)
                        .state(CURRENT_STATE)
                        .version(CURRENT_VERSION)
                        .distributionStatus(DCURR_STATUS)
                        .build(),
                INVARIANT_UUID, modifier,
                ARTIFACT_DATA, COMMENT, DIST_ID);

        when(auditingDao.addRecord(any(AuditingGenericEvent.class), eq(AuditingActionEnum.ARTIFACT_UPLOAD.getAuditingEsType())))
                .thenReturn(ActionStatus.OK);
        when(cassandraDao.saveRecord(any(AuditingGenericEvent.class))).thenReturn(CassandraOperationStatus.OK);

        assertThat(auditingManager.auditEvent(factory)).isEqualTo(EXPECTED_ARTIFACT_UPLOAD_LOG_STR);
        verifyResourceAdminEvent(AuditingActionEnum.ARTIFACT_UPLOAD.getName());
    }

    @Test
    public void testDistStateChangeRequestResourceAdminEvent() {

        AuditEventFactory factory = new AuditDistStateChangeRequestResourceAdminEventFactory(
                CommonAuditData.newBuilder()
                        .description(DESCRIPTION)
                        .status(STATUS_OK)
                        .requestId(REQUEST_ID)
                        .serviceInstanceId(SERVICE_INSTANCE_ID)
                        .build(),
                new ResourceCommonInfo(RESOURCE_NAME, RESOURCE_TYPE_VFC),
                ResourceVersionInfo.newBuilder()
                        .artifactUuid(ARTIFACT_UUID)
                        .state(PREV_RESOURCE_STATE)
                        .version(PREV_RESOURCE_VERSION)
                        .distributionStatus(DPREV_STATUS)
                        .build(),
                ResourceVersionInfo.newBuilder()
                        .artifactUuid(ARTIFACT_UUID)
                        .state(CURRENT_STATE)
                        .version(CURRENT_VERSION)
                        .distributionStatus(DCURR_STATUS)
                        .build(),
                INVARIANT_UUID, modifier,
                ARTIFACT_DATA, COMMENT, DIST_ID);

        when(auditingDao.addRecord(any(AuditingGenericEvent.class), eq(AuditingActionEnum.DISTRIBUTION_STATE_CHANGE_REQUEST.getAuditingEsType())))
                .thenReturn(ActionStatus.OK);
        when(cassandraDao.saveRecord(any(AuditingGenericEvent.class))).thenReturn(CassandraOperationStatus.OK);

        assertThat(auditingManager.auditEvent(factory)).isEqualTo(EXPECTED_DIST_STATE_CHANGE_REQUEST);
        verifyResourceAdminEvent(AuditingActionEnum.DISTRIBUTION_STATE_CHANGE_REQUEST.getName());
    }

    @Test
    public void testDistStateChangeApprovResourceAdminEvent() {

        AuditEventFactory factory = new AuditDistStateChangeResourceAdminEventFactory(
                AuditingActionEnum.DISTRIBUTION_STATE_CHANGE_APPROV,
                CommonAuditData.newBuilder()
                        .description(DESCRIPTION)
                        .status(STATUS_OK)
                        .requestId(REQUEST_ID)
                        .serviceInstanceId(SERVICE_INSTANCE_ID)
                        .build(),
                new ResourceCommonInfo(RESOURCE_NAME, RESOURCE_TYPE_VFC),
                 ResourceVersionInfo.newBuilder()
                        .artifactUuid(ARTIFACT_UUID)
                        .state(PREV_RESOURCE_STATE)
                        .version(PREV_RESOURCE_VERSION)
                        .distributionStatus(DPREV_STATUS)
                        .build(),
                ResourceVersionInfo.newBuilder()
                        .artifactUuid(ARTIFACT_UUID)
                        .state(CURRENT_STATE)
                        .version(CURRENT_VERSION)
                        .distributionStatus(DCURR_STATUS)
                        .build(),
                INVARIANT_UUID, modifier,
                ARTIFACT_DATA, COMMENT, DIST_ID);

        when(auditingDao.addRecord(any(AuditingGenericEvent.class), eq(AuditingActionEnum.DISTRIBUTION_STATE_CHANGE_REQUEST.getAuditingEsType())))
                .thenReturn(ActionStatus.OK);
        when(cassandraDao.saveRecord(any(AuditingGenericEvent.class))).thenReturn(CassandraOperationStatus.OK);

        assertThat(auditingManager.auditEvent(factory)).isEqualTo(EXPECTED_DIST_STATE_CHANGE_APPROV);
        verifyResourceAdminEvent(AuditingActionEnum.DISTRIBUTION_STATE_CHANGE_APPROV.getName());
    }

    private void verifyResourceAdminEvent(String action) {
        verify(cassandraDao).saveRecord(eventCaptor.capture());
        ResourceAdminEvent storedEvent = eventCaptor.getValue();
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
