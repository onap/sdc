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
import org.openecomp.sdc.be.resources.data.auditing.model.DistributionData;
import org.openecomp.sdc.be.resources.data.auditing.model.ResourceCommonInfo;
import org.openecomp.sdc.be.resources.data.auditing.model.ResourceVersionInfo;
import org.openecomp.sdc.test.utils.TestConfigurationProvider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openecomp.sdc.be.auditing.impl.AuditTestUtils.*;


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
    private ArgumentCaptor<ExternalApiEvent> eventCaptor;

    @Before
    public void setUp() {
        init(esConfig);
        auditingManager = new AuditingManager(auditingDao, cassandraDao, new TestConfigurationProvider());
    }

    @Test
    public void testActivateServiceEvent() {
        AuditEventFactory builder = new AuditActivateServiceExternalApiEventFactory(
                CommonAuditData.newBuilder()
                        .description(DESCRIPTION)
                        .status(STATUS_OK)
                        .requestId(REQUEST_ID)
                        .serviceInstanceId(SERVICE_INSTANCE_ID)
                        .build(),
                new ResourceCommonInfo(RESOURCE_NAME, RESOURCE_TYPE),
                new DistributionData(DIST_CONSUMER_ID, DIST_RESOURCE_URL),
                INVARIANT_UUID, modifier);

        when(auditingDao.addRecord(any(AuditingGenericEvent.class), eq(AuditingActionEnum.ACTIVATE_SERVICE_BY_API.getAuditingEsType())))
                .thenReturn(ActionStatus.OK);
        when(cassandraDao.saveRecord(any(AuditingGenericEvent.class))).thenReturn(CassandraOperationStatus.OK);

        assertThat(auditingManager.auditEvent(builder)).isEqualTo(EXPECTED_ACTIVATE_SERVICE_API_LOG_STR);
        verifyExternalApiEventWithoutVersionInfo(AuditingActionEnum.ACTIVATE_SERVICE_BY_API.getName());

    }

    @Test
    public void testDownloadArtifactEvent() {
        AuditEventFactory builder = new AuditDownloadArtifactExternalApiEventFactory(
                CommonAuditData.newBuilder()
                        .description(DESCRIPTION)
                        .status(STATUS_OK)
                        .requestId(REQUEST_ID)
                        .serviceInstanceId(SERVICE_INSTANCE_ID)
                        .build(),
                new ResourceCommonInfo(RESOURCE_NAME, RESOURCE_TYPE),
                new DistributionData(DIST_CONSUMER_ID, DIST_RESOURCE_URL),
                ResourceVersionInfo.newBuilder()
                        .version(CURRENT_VERSION)
                        .state(CURRENT_STATE)
                        .artifactUuid(ARTIFACT_UUID)
                        .build(),
                modifier);

        when(auditingDao.addRecord(any(AuditingGenericEvent.class), eq(AuditingActionEnum.DOWNLOAD_ARTIFACT.getAuditingEsType())))
                .thenReturn(ActionStatus.OK);
        when(cassandraDao.saveRecord(any(AuditingGenericEvent.class))).thenReturn(CassandraOperationStatus.OK);

        assertThat(auditingManager.auditEvent(builder)).isEqualTo(EXPECTED_DOWNLOAD_ARTIFACT_EXTERNAL_API_LOG_STR);
        verifyExternalApiEvent(AuditingActionEnum.DOWNLOAD_ARTIFACT.getName());
    }

    @Test
    public void testChangeLifecycleEvent() {
        AuditEventFactory factory = new AuditChangeLifecycleExternalApiEventFactory(
                CommonAuditData.newBuilder()
                        .description(DESCRIPTION)
                        .status(STATUS_OK)
                        .requestId(REQUEST_ID)
                        .serviceInstanceId(SERVICE_INSTANCE_ID)
                        .build(),
                new ResourceCommonInfo(RESOURCE_NAME, RESOURCE_TYPE),
                new DistributionData(DIST_CONSUMER_ID, DIST_RESOURCE_URL),
                ResourceVersionInfo.newBuilder()
                        .artifactUuid(ARTIFACT_UUID)
                        .state(PREV_RESOURCE_STATE)
                        .version(PREV_RESOURCE_VERSION)
                        .build(),
                ResourceVersionInfo.newBuilder()
                        .artifactUuid(ARTIFACT_UUID)
                        .state(CURRENT_STATE)
                        .version(CURRENT_VERSION)
                        .build(),
                INVARIANT_UUID, modifier);

        when(auditingDao.addRecord(any(AuditingGenericEvent.class), eq(AuditingActionEnum.CHANGE_LIFECYCLE_BY_API.getAuditingEsType())))
                .thenReturn(ActionStatus.OK);
        when(cassandraDao.saveRecord(any(AuditingGenericEvent.class))).thenReturn(CassandraOperationStatus.OK);

        assertThat(auditingManager.auditEvent(factory)).isEqualTo(EXPECTED_CHANGE_LIFECYCLE_EXTERNAL_API_LOG_STR);
        verifyExternalApiEvent(AuditingActionEnum.CHANGE_LIFECYCLE_BY_API.getName());
    }

    @Test
    public void testDeleteArtifactEvent() {
        AuditEventFactory factory = new AuditCrudExternalApiArtifactEventFactory(
                AuditingActionEnum.ARTIFACT_DELETE_BY_API,
                CommonAuditData.newBuilder()
                        .description(DESCRIPTION)
                        .status(STATUS_OK)
                        .requestId(REQUEST_ID)
                        .serviceInstanceId(SERVICE_INSTANCE_ID)
                        .build(),
                new ResourceCommonInfo(RESOURCE_NAME, RESOURCE_TYPE),
                new DistributionData(DIST_CONSUMER_ID, DIST_RESOURCE_URL),
                ResourceVersionInfo.newBuilder()
                        .artifactUuid(ARTIFACT_UUID)
                        .state(PREV_RESOURCE_STATE)
                        .version(PREV_RESOURCE_VERSION)
                        .build(),
                ResourceVersionInfo.newBuilder()
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
    public void testGetAssetEvent() {
        AuditEventFactory factory = new AuditAssetExternalApiEventFactory(AuditingActionEnum.GET_ASSET_METADATA,
                CommonAuditData.newBuilder()
                        .status(STATUS_OK)
                        .description(DESCRIPTION)
                        .requestId(REQUEST_ID)
                        .serviceInstanceId(SERVICE_INSTANCE_ID)
                        .build(),
                new ResourceCommonInfo(RESOURCE_NAME, RESOURCE_TYPE),
                new DistributionData(DIST_CONSUMER_ID, DIST_RESOURCE_URL));
        when(auditingDao.addRecord(any(AuditingGenericEvent.class), eq(AuditingActionEnum.GET_ASSET_METADATA.getAuditingEsType())))
                .thenReturn(ActionStatus.OK);
        when(cassandraDao.saveRecord(any(AuditingGenericEvent.class))).thenReturn(CassandraOperationStatus.OK);

        assertThat(auditingManager.auditEvent(factory)).isEqualTo(EXPECTED_EXTERNAL_ASSET_LOG_STR);
        verifyExternalApiEventWithoutVersionInfo(AuditingActionEnum.GET_ASSET_METADATA.getName());

    }

    @Test
    public void testGetAssetsListEvent() {
        AuditEventFactory factory = new AuditAssetListExternalApiEventFactory(
                AuditingActionEnum.GET_ASSET_LIST,
                CommonAuditData.newBuilder()
                        .description(DESCRIPTION)
                        .status(STATUS_OK)
                        .requestId(REQUEST_ID)
                        .serviceInstanceId(SERVICE_INSTANCE_ID)
                        .build(),
                new DistributionData(DIST_CONSUMER_ID, DIST_RESOURCE_URL));

        when(auditingDao.addRecord(any(AuditingGenericEvent.class), eq(AuditingActionEnum.GET_ASSET_LIST.getAuditingEsType())))
                .thenReturn(ActionStatus.OK);
        when(cassandraDao.saveRecord(any(AuditingGenericEvent.class))).thenReturn(CassandraOperationStatus.OK);

        assertThat(auditingManager.auditEvent(factory)).isEqualTo(EXPECTED_GET_ASSET_LIST_LOG_STR);
        verifyExternalApiEventWithoutResourceInfo(AuditingActionEnum.GET_ASSET_LIST.getName());
    }

    @Test
    public void testGetToscaModelEvent() {
        AuditEventFactory factory = new AuditAssetExternalApiEventFactory(
                AuditingActionEnum.GET_TOSCA_MODEL,
                CommonAuditData.newBuilder()
                        .description(DESCRIPTION)
                        .status(STATUS_OK)
                        .requestId(REQUEST_ID)
                        .serviceInstanceId(SERVICE_INSTANCE_ID)
                        .build(),
                new ResourceCommonInfo(RESOURCE_NAME, RESOURCE_TYPE),
                new DistributionData(DIST_CONSUMER_ID, DIST_RESOURCE_URL));

        when(auditingDao.addRecord(any(AuditingGenericEvent.class), eq(AuditingActionEnum.GET_TOSCA_MODEL.getAuditingEsType())))
                .thenReturn(ActionStatus.OK);
        when(cassandraDao.saveRecord(any(AuditingGenericEvent.class))).thenReturn(CassandraOperationStatus.OK);

        assertThat(auditingManager.auditEvent(factory)).isEqualTo(EXPECTED_GET_TOSCA_MODEL_LOG_STR);
        verifyExternalApiEventWithoutVersionInfo(AuditingActionEnum.GET_TOSCA_MODEL.getName());
    }

    @Test
    public void testCreateResourceEvent() {
        AuditEventFactory factory = new AuditCreateResourceExternalApiEventFactory(
                CommonAuditData.newBuilder()
                        .description(DESCRIPTION)
                        .status(STATUS_OK)
                        .requestId(REQUEST_ID)
                        .serviceInstanceId(SERVICE_INSTANCE_ID)
                        .build(),
                new ResourceCommonInfo(RESOURCE_NAME, RESOURCE_TYPE),
                new DistributionData(DIST_CONSUMER_ID, DIST_RESOURCE_URL),
                ResourceVersionInfo.newBuilder()
                        .artifactUuid(ARTIFACT_UUID)
                        .state(CURRENT_STATE)
                        .version(CURRENT_VERSION)
                        .build(),
                INVARIANT_UUID, modifier);

        when(auditingDao.addRecord(any(AuditingGenericEvent.class), eq(AuditingActionEnum.CREATE_RESOURCE_BY_API.getAuditingEsType())))
                .thenReturn(ActionStatus.OK);
        when(cassandraDao.saveRecord(any(AuditingGenericEvent.class))).thenReturn(CassandraOperationStatus.OK);

        assertThat(auditingManager.auditEvent(factory)).isEqualTo(EXPECTED_EXTERNAL_CREATE_RESOURCE_LOG_STR);
        verifyExternalApiEvent(AuditingActionEnum.CREATE_RESOURCE_BY_API.getName());
    }

    private void verifyExternalApiEvent(String action) {
        verify(cassandraDao).saveRecord(eventCaptor.capture());
        ExternalApiEvent storedEvent = eventCaptor.getValue();
        assertThat(storedEvent.getModifier()).isEqualTo(MODIFIER_UID);
        assertThat(storedEvent.getDesc()).isEqualTo(DESCRIPTION);
        assertThat(storedEvent.getStatus()).isEqualTo(STATUS_OK);
        assertThat(storedEvent.getServiceInstanceId()).isEqualTo(SERVICE_INSTANCE_ID);
        assertThat(storedEvent.getAction()).isEqualTo(action);

        assertThat(storedEvent.getResourceName()).isEqualTo(RESOURCE_NAME);
        assertThat(storedEvent.getResourceType()).isEqualTo(RESOURCE_TYPE);
        assertThat(storedEvent.getCurrArtifactUuid()).isEqualTo(ARTIFACT_UUID);

        if (!action.equals(AuditingActionEnum.DOWNLOAD_ARTIFACT.getName())
            && !action.equals(AuditingActionEnum.CREATE_RESOURCE_BY_API.getName())) {
            assertThat(storedEvent.getPrevArtifactUuid()).isEqualTo(ARTIFACT_UUID);
            assertThat(storedEvent.getCurrVersion()).isEqualTo(CURRENT_VERSION);
            assertThat(storedEvent.getCurrState()).isEqualTo(CURRENT_STATE);
            assertThat(storedEvent.getPrevState()).isEqualTo(PREV_RESOURCE_STATE);
            assertThat(storedEvent.getPrevVersion()).isEqualTo(PREV_RESOURCE_VERSION);
        }
        else {
            assertThat(storedEvent.getPrevArtifactUuid()).isNull();
            assertThat(storedEvent.getCurrVersion()).isEqualTo(CURRENT_VERSION);
            assertThat(storedEvent.getCurrState()).isEqualTo(CURRENT_STATE);
            assertThat(storedEvent.getPrevState()).isNull();
            assertThat(storedEvent.getPrevVersion()).isNull();
            if (action.equals(AuditingActionEnum.DOWNLOAD_ARTIFACT.getName())) {
                assertThat(storedEvent.getInvariantUuid()).isNull();
            }
            else {
                assertThat(storedEvent.getInvariantUuid()).isEqualTo(INVARIANT_UUID);
            }
        }
        if (!action.equals(AuditingActionEnum.CHANGE_LIFECYCLE_BY_API.getName())
                && !action.equals(AuditingActionEnum.DOWNLOAD_ARTIFACT.getName())
                && !action.equals(AuditingActionEnum.CREATE_RESOURCE_BY_API.getName())) {
            assertThat(storedEvent.getArtifactData()).isEqualTo(ARTIFACT_DATA);
        }
        else {
            assertThat(storedEvent.getArtifactData()).isNull();
        }

    }

    private void verifyExternalApiEventWithoutVersionInfo(String action) {
        verify(cassandraDao).saveRecord(eventCaptor.capture());
        ExternalApiEvent storedEvent = eventCaptor.getValue();
        if (action.equals(AuditingActionEnum.ACTIVATE_SERVICE_BY_API.getName())) {
            assertThat(storedEvent.getModifier()).isEqualTo(MODIFIER_UID);
            assertThat(storedEvent.getInvariantUuid()).isEqualTo(INVARIANT_UUID);
        }
        else {
            assertThat(storedEvent.getModifier()).isEmpty();
            assertThat(storedEvent.getInvariantUuid()).isNull();
        }
        assertThat(storedEvent.getDesc()).isEqualTo(DESCRIPTION);
        assertThat(storedEvent.getStatus()).isEqualTo(STATUS_OK);
        assertThat(storedEvent.getServiceInstanceId()).isEqualTo(SERVICE_INSTANCE_ID);
        assertThat(storedEvent.getAction()).isEqualTo(action);
        assertThat(storedEvent.getArtifactData()).isNull();
        assertThat(storedEvent.getCurrArtifactUuid()).isNull();
        assertThat(storedEvent.getPrevArtifactUuid()).isNull();
        assertThat(storedEvent.getPrevState()).isNull();
        assertThat(storedEvent.getCurrState()).isNull();
        assertThat(storedEvent.getPrevVersion()).isNull();
        assertThat(storedEvent.getCurrVersion()).isNull();

        assertThat(storedEvent.getResourceName()).isEqualTo(RESOURCE_NAME);
        assertThat(storedEvent.getResourceType()).isEqualTo(RESOURCE_TYPE);

    }

    private void verifyExternalApiEventWithoutResourceInfo(String action) {
        verify(cassandraDao).saveRecord(eventCaptor.capture());
        ExternalApiEvent storedEvent = eventCaptor.getValue();
        assertThat(storedEvent.getModifier()).isEmpty();
        assertThat(storedEvent.getDesc()).isEqualTo(DESCRIPTION);
        assertThat(storedEvent.getStatus()).isEqualTo(STATUS_OK);
        assertThat(storedEvent.getServiceInstanceId()).isEqualTo(SERVICE_INSTANCE_ID);
        assertThat(storedEvent.getAction()).isEqualTo(action);
        assertThat(storedEvent.getArtifactData()).isNull();
        assertThat(storedEvent.getCurrArtifactUuid()).isNull();
        assertThat(storedEvent.getPrevArtifactUuid()).isNull();
        assertThat(storedEvent.getPrevState()).isNull();
        assertThat(storedEvent.getCurrState()).isNull();
        assertThat(storedEvent.getPrevVersion()).isNull();
        assertThat(storedEvent.getCurrVersion()).isNull();
        assertThat(storedEvent.getInvariantUuid()).isNull();
        assertThat(storedEvent.getResourceName()).isNull();
        assertThat(storedEvent.getResourceType()).isNull();
    }

}