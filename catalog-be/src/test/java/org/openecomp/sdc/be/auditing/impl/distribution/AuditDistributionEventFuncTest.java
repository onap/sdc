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
package org.openecomp.sdc.be.auditing.impl.distribution;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.sdc.be.auditing.api.AuditEventFactory;
import org.openecomp.sdc.be.auditing.impl.AuditGetUebClusterEventFactory;
import org.openecomp.sdc.be.auditing.impl.AuditingManager;
import org.openecomp.sdc.be.dao.cassandra.AuditCassandraDao;
import org.openecomp.sdc.be.dao.cassandra.CassandraOperationStatus;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.resources.data.auditing.AuditingGenericEvent;
import org.openecomp.sdc.be.resources.data.auditing.AuditingGetUebClusterEvent;
import org.openecomp.sdc.be.resources.data.auditing.DistributionDeployEvent;
import org.openecomp.sdc.be.resources.data.auditing.DistributionDownloadEvent;
import org.openecomp.sdc.be.resources.data.auditing.DistributionNotificationEvent;
import org.openecomp.sdc.be.resources.data.auditing.DistributionStatusEvent;
import org.openecomp.sdc.be.resources.data.auditing.model.CommonAuditData;
import org.openecomp.sdc.be.resources.data.auditing.model.DistributionData;
import org.openecomp.sdc.be.resources.data.auditing.model.OperationalEnvAuditData;
import org.openecomp.sdc.be.resources.data.auditing.model.ResourceCommonInfo;
import org.openecomp.sdc.be.resources.data.auditing.model.ResourceVersionInfo;
import org.openecomp.sdc.common.util.ThreadLocalsHolder;
import org.openecomp.sdc.test.utils.TestConfigurationProvider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openecomp.sdc.be.auditing.impl.AuditTestUtils.CURRENT_STATE;
import static org.openecomp.sdc.be.auditing.impl.AuditTestUtils.CURRENT_VERSION;
import static org.openecomp.sdc.be.auditing.impl.AuditTestUtils.DESCRIPTION;
import static org.openecomp.sdc.be.auditing.impl.AuditTestUtils.DIST_CONSUMER_ID;
import static org.openecomp.sdc.be.auditing.impl.AuditTestUtils.DIST_ID;
import static org.openecomp.sdc.be.auditing.impl.AuditTestUtils.DIST_RESOURCE_URL;
import static org.openecomp.sdc.be.auditing.impl.AuditTestUtils.DIST_STATUS_TIME;
import static org.openecomp.sdc.be.auditing.impl.AuditTestUtils.EXPECTED_DISTRIB_DEPLOY_LOG_STR;
import static org.openecomp.sdc.be.auditing.impl.AuditTestUtils.EXPECTED_DISTRIB_NOTIFICATION_LOG_STR;
import static org.openecomp.sdc.be.auditing.impl.AuditTestUtils.EXPECTED_DIST_DOWNLOAD_LOG_STR;
import static org.openecomp.sdc.be.auditing.impl.AuditTestUtils.EXPECTED_DIST_STATUS_LOG_STR;
import static org.openecomp.sdc.be.auditing.impl.AuditTestUtils.EXPECTED_GET_UEB_CLUSTER_LOG_STR;
import static org.openecomp.sdc.be.auditing.impl.AuditTestUtils.OP_ENV_ID;
import static org.openecomp.sdc.be.auditing.impl.AuditTestUtils.REQUEST_ID;
import static org.openecomp.sdc.be.auditing.impl.AuditTestUtils.RESOURCE_NAME;
import static org.openecomp.sdc.be.auditing.impl.AuditTestUtils.RESOURCE_TYPE;
import static org.openecomp.sdc.be.auditing.impl.AuditTestUtils.SERVICE_INSTANCE_ID;
import static org.openecomp.sdc.be.auditing.impl.AuditTestUtils.STATUS_OK;
import static org.openecomp.sdc.be.auditing.impl.AuditTestUtils.TENANT_CONTEXT;
import static org.openecomp.sdc.be.auditing.impl.AuditTestUtils.TOPIC_NAME;
import static org.openecomp.sdc.be.auditing.impl.AuditTestUtils.USER_UID;
import static org.openecomp.sdc.be.auditing.impl.AuditTestUtils.VNF_WORKLOAD_CONTEXT;
import static org.openecomp.sdc.be.auditing.impl.AuditTestUtils.init;
import static org.openecomp.sdc.be.auditing.impl.AuditTestUtils.user;

@RunWith(MockitoJUnitRunner.class)
public class AuditDistributionEventFuncTest {

    private AuditingManager auditingManager;

    @Mock
    private static AuditCassandraDao cassandraDao;

    @Captor
    private ArgumentCaptor<AuditingGenericEvent> eventCaptor;

    @Before
    public void setUp() {
        init();
        auditingManager = new AuditingManager(cassandraDao, new TestConfigurationProvider());
        ThreadLocalsHolder.setUuid(REQUEST_ID);
    }

    @Test
    public void testNotifyEvent() {
        AuditEventFactory factory = new AuditDistributionNotificationEventFactory(
                CommonAuditData.newBuilder()
                    .description(DESCRIPTION)
                    .status(STATUS_OK)
                    .requestId(REQUEST_ID)
                    .serviceInstanceId(SERVICE_INSTANCE_ID)
                    .build(),
                new ResourceCommonInfo(RESOURCE_NAME,RESOURCE_TYPE),
                ResourceVersionInfo.newBuilder()
                    .state(CURRENT_STATE)
                    .version(CURRENT_VERSION)
                    .build(),
                DIST_ID, user, TOPIC_NAME,
                new OperationalEnvAuditData(OP_ENV_ID, VNF_WORKLOAD_CONTEXT, TENANT_CONTEXT));

        when(cassandraDao.saveRecord(any(AuditingGenericEvent.class))).thenReturn(CassandraOperationStatus.OK);

        assertThat(auditingManager.auditEvent(factory)).isEqualTo(EXPECTED_DISTRIB_NOTIFICATION_LOG_STR);
        verifyNotifyEvent();
    }

    @Test
    public void testStatusEvent() {
        AuditEventFactory factory = new AuditDistributionStatusEventFactory(
                CommonAuditData.newBuilder()
                        .description(DESCRIPTION)
                        .status(STATUS_OK)
                        .requestId(REQUEST_ID)
                        .serviceInstanceId(SERVICE_INSTANCE_ID)
                        .build(),
                new DistributionData(DIST_CONSUMER_ID, DIST_RESOURCE_URL),
                DIST_ID, TOPIC_NAME, DIST_STATUS_TIME);

        when(cassandraDao.saveRecord(any(AuditingGenericEvent.class))).thenReturn(CassandraOperationStatus.OK);

        assertThat(auditingManager.auditEvent(factory)).isEqualTo(EXPECTED_DIST_STATUS_LOG_STR);
        verifyStatusEvent();
    }

    @Test
    public void testDownloadEvent() {
        AuditEventFactory factory = new AuditDistributionDownloadEventFactory(
                CommonAuditData.newBuilder()
                        .description(DESCRIPTION)
                        .status(STATUS_OK)
                        .requestId(REQUEST_ID)
                        .serviceInstanceId(SERVICE_INSTANCE_ID)
                        .build(),
                new DistributionData(DIST_CONSUMER_ID, DIST_RESOURCE_URL));

        when(cassandraDao.saveRecord(any(AuditingGenericEvent.class))).thenReturn(CassandraOperationStatus.OK);

        assertThat(auditingManager.auditEvent(factory)).isEqualTo(EXPECTED_DIST_DOWNLOAD_LOG_STR);
        verifyDownloadsEvent();
    }

    @Test
    public void testDeployEvent() {
        AuditEventFactory factory = new AuditDistributionDeployEventFactory(
                CommonAuditData.newBuilder()
                        .description(DESCRIPTION)
                        .status(STATUS_OK)
                        .requestId(REQUEST_ID)
                        .serviceInstanceId(SERVICE_INSTANCE_ID)
                        .build(),
                new ResourceCommonInfo(RESOURCE_NAME,RESOURCE_TYPE),
                DIST_ID, user, CURRENT_VERSION);

        when(cassandraDao.saveRecord(any(AuditingGenericEvent.class))).thenReturn(CassandraOperationStatus.OK);

        assertThat(auditingManager.auditEvent(factory)).isEqualTo(EXPECTED_DISTRIB_DEPLOY_LOG_STR);
        verifyDeployEvent();
    }

    @Test
    public void testGetUebClusterEvent() {
        AuditEventFactory factory = new AuditGetUebClusterEventFactory(
                CommonAuditData.newBuilder()
                        .description(DESCRIPTION)
                        .status(STATUS_OK)
                        .requestId(REQUEST_ID)
                        .serviceInstanceId(SERVICE_INSTANCE_ID)
                        .build(),
                DIST_CONSUMER_ID);

        when(cassandraDao.saveRecord(any(AuditingGenericEvent.class))).thenReturn(CassandraOperationStatus.OK);

        assertThat(auditingManager.auditEvent(factory)).contains(EXPECTED_GET_UEB_CLUSTER_LOG_STR);
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

}
