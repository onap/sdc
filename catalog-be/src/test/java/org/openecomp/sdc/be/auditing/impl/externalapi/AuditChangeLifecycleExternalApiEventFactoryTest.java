/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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
 */

package org.openecomp.sdc.be.auditing.impl.externalapi;

import org.junit.Before;
import org.junit.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openecomp.sdc.be.auditing.impl.AuditTestUtils.*;

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

@RunWith(MockitoJUnitRunner.class)
public class AuditChangeLifecycleExternalApiEventFactoryTest {

	private AuditChangeLifecycleExternalApiEventFactory createTestSubject() {
		CommonAuditData.Builder newBuilder = CommonAuditData.newBuilder()
				.description(DESCRIPTION)
				.status(STATUS_OK)
				.requestId(REQUEST_ID)
				.serviceInstanceId(SERVICE_INSTANCE_ID);
		CommonAuditData commonAuData = newBuilder.build();
		ResourceVersionInfo.Builder newBuilder2 = ResourceVersionInfo.newBuilder()
				.artifactUuid(ARTIFACT_UUID)
				.state(PREV_RESOURCE_STATE)
				.version(PREV_RESOURCE_VERSION);
		ResourceVersionInfo resAuData1 = newBuilder2.build();
		ResourceVersionInfo.Builder newBuilder3 = ResourceVersionInfo.newBuilder()
				.artifactUuid(ARTIFACT_UUID)
				.state(CURRENT_STATE)
				.version(CURRENT_VERSION);
		ResourceVersionInfo resAuData2 = newBuilder3.build();
		return new AuditChangeLifecycleExternalApiEventFactory(commonAuData,
				new ResourceCommonInfo(RESOURCE_NAME, RESOURCE_TYPE),
				new DistributionData(DIST_CONSUMER_ID,DIST_RESOURCE_URL),
				resAuData1,
				resAuData2,
				INVARIANT_UUID,
				modifier);
	}

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
	public void testGetLogMessage() throws Exception {
		AuditChangeLifecycleExternalApiEventFactory testSubject;

		// default test
		testSubject = createTestSubject();
		assertThat(testSubject.getLogMessage()).isNotBlank();
		assertThat(testSubject.getLogMessage()).isEqualTo(EXPECTED_CHANGE_LIFECYCLE_EXTERNAL_API_LOG_STR);
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

	private void verifyExternalApiEvent(String action) {
		verify(cassandraDao).saveRecord(eventCaptor.capture());
		ExternalApiEvent storedEvent = eventCaptor.getValue();
		assertThat(storedEvent.getModifier()).isEqualTo(MODIFIER_UID);
		assertThat(storedEvent.getDesc()).isEqualTo(DESCRIPTION);
		assertThat(storedEvent.getStatus()).isEqualTo(STATUS_OK);
		assertThat(storedEvent.getServiceInstanceId()).isEqualTo(SERVICE_INSTANCE_ID);
		assertThat(storedEvent.getAction()).isEqualTo(action);
		assertThat(storedEvent.getResourceType()).isEqualTo(RESOURCE_TYPE);
	}
}
