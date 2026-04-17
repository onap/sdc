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

package org.openecomp.sdc.be.dao.cassandra;


import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.PagingIterable;
import com.datastax.oss.driver.api.core.cql.ColumnDefinitions;
import com.datastax.oss.driver.api.core.cql.ExecutionInfo;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;

import fj.data.Either;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.resources.data.auditing.*;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class AuditCassandraDaoTest {

	@InjectMocks
	AuditCassandraDao testSubject;

	@Mock
	AuditDao auditDao;

	@Mock
	CassandraClient client;

	
	@Mock
    private CqlSession session;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.openMocks(this);
		ReflectionTestUtils.setField(testSubject, "auditDao", auditDao);
	}

	private static <T> PagingIterable<T> asPagingIterable(final List<T> list) {
		return new PagingIterable<T>() {
			@Override
			public ColumnDefinitions getColumnDefinitions() {
				return null;
			}

			@Override
			public List<ExecutionInfo> getExecutionInfos() {
				return Collections.emptyList();
			}

			@Override
			public boolean isFullyFetched() {
				return true;
			}

			@Override
			public int getAvailableWithoutFetching() {
				return list.size();
			}

			@Override
			public boolean wasApplied() {
				return true;
			}

			@Override
			public Iterator<T> iterator() {
				return list.iterator();
			}
		};
	}

	@Test(expected = RuntimeException.class)
	public void testInit() throws Exception {
		Mockito.when(client.isConnected()).thenReturn(true);
		Either <CqlSession, CassandraOperationStatus> value = Either
				.right(CassandraOperationStatus.CLUSTER_NOT_CONNECTED);
		Mockito.when(client.connect(Mockito.anyString())).thenReturn(value);
		testSubject.init();
	}

	@Test
	public void testInitFail2() throws Exception {
		Mockito.when(client.isConnected()).thenReturn(false);
		testSubject.init();
	}

	@Test
	public void testGetListOfDistributionStatuses() throws Exception {
		String did = "";
		Either<List<DistributionStatusEvent>, ActionStatus> result;

		List<DistributionStatusEvent> events = new ArrayList<>();

		events.add(new DistributionStatusEvent());
		Mockito.when(auditDao.getListOfDistributionStatuses(Mockito.anyString()))
           .thenReturn(asPagingIterable(events));
		result = testSubject.getListOfDistributionStatuses(did);
		
		
	}

	@Test
	public void testGetListOfDistributionStatusesException() throws Exception {
		String did = "";
		Either<List<DistributionStatusEvent>, ActionStatus> result;

		Mockito.when(auditDao.getListOfDistributionStatuses(Mockito.anyString()))
				.thenThrow(RuntimeException.class);

		// default test
		result = testSubject.getListOfDistributionStatuses(did);
	}

	@Test
	public void testGetListOfDistributionStatusesEmptyList() throws Exception {
		String did = "";
		Either<List<DistributionStatusEvent>, ActionStatus> result;

		// default test
		result = testSubject.getListOfDistributionStatuses(did);
	}

	@Test
	public void testGetDistributionDeployByStatus() throws Exception {
		String did = "";
		String action = "";
		String status = "";
		Either<List<DistributionDeployEvent>, ActionStatus> result;

		List<DistributionDeployEvent> mockedList = new LinkedList<>();
		mockedList.add(new DistributionDeployEvent());

			Mockito.when(auditDao.getDistributionDeployByStatus(
	            Mockito.anyString(),
	            Mockito.anyString(),
	            Mockito.anyString()))
	        .thenReturn(asPagingIterable(mockedList));
			// default test
			result = testSubject.getDistributionDeployByStatus(did, action, status);

		
	}

	@Test
	public void testGetDistributionDeployByStatusEmptyList() throws Exception {
		String did = "";
		String action = "";
		String status = "";
		Either<List<DistributionDeployEvent>, ActionStatus> result;

		Mockito.when(auditDao.getDistributionDeployByStatus(
            Mockito.anyString(),
            Mockito.anyString(),
            Mockito.anyString()))
        .thenReturn(null);

		// default test
		result = testSubject.getDistributionDeployByStatus(did, action, status);

	}

	@Test
	public void testGetDistributionDeployByStatusException() throws Exception {
		String did = "";
		String action = "";
		String status = "";
		Either<List<DistributionDeployEvent>, ActionStatus> result;

		Mockito.when(auditDao.getDistributionDeployByStatus(Mockito.anyString(), Mockito.anyString(),
				Mockito.anyString())).thenThrow(RuntimeException.class);

		// default test
		result = testSubject.getDistributionDeployByStatus(did, action, status);
	}

	@Test
	public void testGetDistributionRequest() throws Exception {
		String did = "";
		String action = "";
		Either<List<ResourceAdminEvent>, ActionStatus> result;

		// default test
		result = testSubject.getDistributionRequest(did, action);
	}

	@Test
	public void testGetDistributionRequestList() throws Exception {
		String did = "";
		String action = "";
		Either<List<ResourceAdminEvent>, ActionStatus> result;

			List<ResourceAdminEvent> value2 = new LinkedList<>();
	    	value2.add(new ResourceAdminEvent());
			Mockito.when(auditDao.getDistributionRequest(Mockito.anyString(), Mockito.anyString()))
	           .thenReturn(asPagingIterable(value2));
			
			// default test
			result = testSubject.getDistributionRequest(did, action);

	}

	@Test
	public void testGetDistributionRequestException() throws Exception {
		String did = "";
		String action = "";
		Either<List<ResourceAdminEvent>, ActionStatus> result;

		Mockito.when(auditDao.getDistributionRequest(Mockito.anyString(), Mockito.anyString()))
				.thenThrow(RuntimeException.class);

		// default test
		result = testSubject.getDistributionRequest(did, action);
	}

	@Test
	public void testGetDistributionNotify() throws Exception {
		String did = "";
		String action = "";
		Either<List<DistributionNotificationEvent>, ActionStatus> result;

			List<DistributionNotificationEvent> value2 = new LinkedList<>();
	    	value2.add(new DistributionNotificationEvent());

			Mockito.when(auditDao.getDistributionNotify(Mockito.anyString(), Mockito.anyString()))
	           .thenReturn(asPagingIterable(value2));
			// default test
			result = testSubject.getDistributionNotify(did, action);
	}

	@Test
	public void testGetDistributionNotifyException() throws Exception {
		String did = "";
		String action = "";
		Either<List<DistributionNotificationEvent>, ActionStatus> result;

		Mockito.when(auditDao.getDistributionNotify(Mockito.anyString(), Mockito.anyString()))
				.thenThrow(RuntimeException.class);

		// default test
		result = testSubject.getDistributionNotify(did, action);
	}

	@Test
	public void testGetDistributionNotifyNull() throws Exception {
		String did = "";
		String action = "";
		Either<List<DistributionNotificationEvent>, ActionStatus> result;

		// default test
		result = testSubject.getDistributionNotify(did, action);
	}

	@Test
	public void testGetByServiceInstanceId() throws Exception {
		String serviceInstanceId = "";
		Either<List<ResourceAdminEvent>, ActionStatus> result;

			List<ResourceAdminEvent> value2 = new LinkedList<>();
	    	value2.add(new ResourceAdminEvent());		
			Mockito.when(auditDao.getByServiceInstanceId(Mockito.anyString())).thenReturn(asPagingIterable(value2));

		// default test
		result = testSubject.getByServiceInstanceId(serviceInstanceId);
	}

	@Test
	public void testGetByServiceInstanceIdException() throws Exception {
		String serviceInstanceId = "";
		Either<List<ResourceAdminEvent>, ActionStatus> result;

		Mockito.when(auditDao.getByServiceInstanceId(Mockito.anyString())).thenThrow(RuntimeException.class);
		// default test
		result = testSubject.getByServiceInstanceId(serviceInstanceId);
	}

	@Test
	public void testGetByServiceInstanceIdNull() throws Exception {
		String serviceInstanceId = "";
		Either<List<ResourceAdminEvent>, ActionStatus> result;

		// default test
		result = testSubject.getByServiceInstanceId(serviceInstanceId);
	}

	@Test
	public void testGetServiceDistributionStatusesList() throws Exception {
		String serviceInstanceId = "";
		Either<List<? extends AuditingGenericEvent>, ActionStatus> result;

			List<ResourceAdminEvent> value2 = new LinkedList<>();
			value2.add(new ResourceAdminEvent());
			Mockito.when(auditDao.getServiceDistributionStatus(Mockito.anyString())).thenReturn(asPagingIterable(value2));

		// default test
		result = testSubject.getServiceDistributionStatusesList(serviceInstanceId);
	}

	@Test
	public void testGetServiceDistributionStatusesList2() throws Exception {
		String serviceInstanceId = "";
		Either<List<? extends AuditingGenericEvent>, ActionStatus> result;

		
			List<ResourceAdminEvent> value2 = new LinkedList<>();
			value2.add(new ResourceAdminEvent());
			
			Mockito.when(auditDao.getServiceDistributionStatus(Mockito.anyString())).thenReturn(asPagingIterable(value2));

	
			List<DistributionDeployEvent> value4 = new LinkedList<>();
			value4.add(new DistributionDeployEvent());
		
			Mockito.when(auditDao.getServiceDistributionDeploy(Mockito.anyString())).thenReturn(asPagingIterable(value4));

		// default test
		result = testSubject.getServiceDistributionStatusesList(serviceInstanceId);
	}

	@Test
	public void testGetServiceDistributionStatusesList3() throws Exception {
		String serviceInstanceId = "";
		Either<List<? extends AuditingGenericEvent>, ActionStatus> result;

		
			List<ResourceAdminEvent> value2 = new LinkedList<>();
			value2.add(new ResourceAdminEvent());
		
			Mockito.when(auditDao.getServiceDistributionStatus(Mockito.anyString())).thenReturn(asPagingIterable(value2));


			List<DistributionDeployEvent> value4 = new LinkedList<>();
			value4.add(new DistributionDeployEvent());

			Mockito.when(auditDao.getServiceDistributionDeploy(Mockito.anyString())).thenReturn(asPagingIterable(value4));

		
	
			List<DistributionNotificationEvent> value6 = new LinkedList<>();
			value6.add(new DistributionNotificationEvent());

			Mockito.when(auditDao.getServiceDistributionNotify(Mockito.anyString())).thenReturn(asPagingIterable(value6));

		// default test
		result = testSubject.getServiceDistributionStatusesList(serviceInstanceId);
	}
	
	@Test
	public void testGetServiceDistributionStatusesListException3() throws Exception {
		String serviceInstanceId = "";
		Either<List<? extends AuditingGenericEvent>, ActionStatus> result;

		
			List<ResourceAdminEvent> value2 = new LinkedList<>();
			value2.add(new ResourceAdminEvent());
			Mockito.when(auditDao.getServiceDistributionStatus(Mockito.anyString())).thenReturn(asPagingIterable(value2));

		
			List<DistributionDeployEvent> value4 = new LinkedList<>();
			value4.add(new DistributionDeployEvent());

			Mockito.when(auditDao.getServiceDistributionDeploy(Mockito.anyString())).thenReturn(asPagingIterable(value4));

		Mockito.when(auditDao.getServiceDistributionNotify(Mockito.anyString())).thenThrow(RuntimeException.class);

		// default test
		result = testSubject.getServiceDistributionStatusesList(serviceInstanceId);
	}
	
	@Test
	public void testGetServiceDistributionStatusesListException2() throws Exception {
		String serviceInstanceId = "";
		Either<List<? extends AuditingGenericEvent>, ActionStatus> result;


			List<ResourceAdminEvent> value2 = new LinkedList<>();
			value2.add(new ResourceAdminEvent());

			Mockito.when(auditDao.getServiceDistributionStatus(Mockito.anyString())).thenReturn(asPagingIterable(value2));

		Mockito.when(auditDao.getServiceDistributionDeploy(Mockito.anyString())).thenThrow(RuntimeException.class);

		// default test
		result = testSubject.getServiceDistributionStatusesList(serviceInstanceId);
	}

	@Test
	public void testGetServiceDistributionStatusesListException() throws Exception {
		String serviceInstanceId = "";
		Either<List<? extends AuditingGenericEvent>, ActionStatus> result;

		Mockito.when(auditDao.getServiceDistributionStatus(Mockito.anyString())).thenThrow(RuntimeException.class);

		// default test
		result = testSubject.getServiceDistributionStatusesList(serviceInstanceId);
	}

	@Test
	public void testGetServiceDistributionStatusesListNull() throws Exception {
		String serviceInstanceId = "";
		Either<List<? extends AuditingGenericEvent>, ActionStatus> result;

		// default test
		result = testSubject.getServiceDistributionStatusesList(serviceInstanceId);
	}

	@Test
	public void testGetAuditByServiceIdAndPrevVersionNull() throws Exception {
		String serviceInstanceId = "";
		String prevVersion = "";
		Either<List<ResourceAdminEvent>, ActionStatus> result;

		// default test
		result = testSubject.getAuditByServiceIdAndPrevVersion(serviceInstanceId, prevVersion);
	}
	
	@Test
	public void testGetAuditByServiceIdAndPrevVersion() throws Exception {
		String serviceInstanceId = "";
		String prevVersion = "";
		Either<List<ResourceAdminEvent>, ActionStatus> result;
		

		List<ResourceAdminEvent> value2 = new LinkedList<>();
		value2.add(new ResourceAdminEvent());

			Mockito.when(auditDao.getAuditByServiceIdAndPrevVersion(Mockito.anyString(), Mockito.anyString())).thenReturn(asPagingIterable(value2));
		
		// default test
		result = testSubject.getAuditByServiceIdAndPrevVersion(serviceInstanceId, prevVersion);
	}
	
	@Test
	public void testGetAuditByServiceIdAndPrevVersionException() throws Exception {
		String serviceInstanceId = "";
		String prevVersion = "";
		Either<List<ResourceAdminEvent>, ActionStatus> result;
		
		Mockito.when(auditDao.getAuditByServiceIdAndPrevVersion(Mockito.anyString(), Mockito.anyString())).thenThrow(RuntimeException.class);
		
		// default test
		result = testSubject.getAuditByServiceIdAndPrevVersion(serviceInstanceId, prevVersion);
	}
	
	@Test
	public void testGetAuditByServiceIdAndCurrVersionNull() throws Exception {
		String serviceInstanceId = "";
		String currVersion = "";
		Either<List<ResourceAdminEvent>, ActionStatus> result;

		// default test
		result = testSubject.getAuditByServiceIdAndCurrVersion(serviceInstanceId, currVersion);
	}

	@Test
	public void testGetAuditByServiceIdAndCurrVersion() throws Exception {
		String serviceInstanceId = "";
		String currVersion = "";
		Either<List<ResourceAdminEvent>, ActionStatus> result;

		
		List<ResourceAdminEvent> value2 = new LinkedList<>();
		value2.add(new ResourceAdminEvent());

			Mockito.when(auditDao.getAuditByServiceIdAndCurrVersion(Mockito.anyString(), Mockito.anyString())).thenReturn(asPagingIterable(value2));
		
		// default test
		result = testSubject.getAuditByServiceIdAndCurrVersion(serviceInstanceId, currVersion);
	}
	
	@Test
	public void testGetAuditByServiceIdAndCurrVersionException() throws Exception {
		String serviceInstanceId = "";
		String currVersion = "";
		Either<List<ResourceAdminEvent>, ActionStatus> result;

		Mockito.when(auditDao.getAuditByServiceIdAndCurrVersion(Mockito.anyString(), Mockito.anyString())).thenThrow(RuntimeException.class);
		
		// default test
		result = testSubject.getAuditByServiceIdAndCurrVersion(serviceInstanceId, currVersion);
	}
	
	@Test
	public void testIsTableEmpty() throws Exception {
		Mockito.when(client.isConnected()).thenReturn(true);
    Mockito.when(client.connect(AuditingTypesConstants.ARTIFACT_KEYSPACE))
           .thenReturn(Either.left(session));

    // Mock ResultSet and Row
    ResultSet mockResultSet = Mockito.mock(ResultSet.class);
    Row mockRow = Mockito.mock(Row.class);

    Mockito.when(mockRow.getLong("count")).thenReturn(0L); // table is empty
    Mockito.when(mockResultSet.one()).thenReturn(mockRow);
   Mockito.when(session.execute(Mockito.any(SimpleStatement.class)))
       .thenReturn(mockResultSet);

    // testSubject.init();

    Either<Boolean, CassandraOperationStatus> result =
            testSubject.isTableEmpty("artifacts");

	}

	@Test
	public void testDeleteAllAudit() throws Exception {
		CassandraOperationStatus result;

		// default test
		result = testSubject.deleteAllAudit();
	}
}
