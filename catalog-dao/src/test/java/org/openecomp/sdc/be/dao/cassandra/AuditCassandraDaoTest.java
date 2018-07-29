package org.openecomp.sdc.be.dao.cassandra;

import com.datastax.driver.core.Session;
import com.datastax.driver.mapping.MappingManager;
import com.datastax.driver.mapping.Result;
import fj.data.Either;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.resources.data.auditing.*;

import java.util.LinkedList;
import java.util.List;

public class AuditCassandraDaoTest {

	@InjectMocks
	AuditCassandraDao testSubject;

	@Mock
	AuditAccessor auditAccessor;

	@Mock
	CassandraClient client;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
	}

	@Test(expected = RuntimeException.class)
	public void testInit() throws Exception {
		Mockito.when(client.isConnected()).thenReturn(true);
		Either<ImmutablePair<Session, MappingManager>, CassandraOperationStatus> value = Either
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

		Result<DistributionStatusEvent> value = Mockito.mock(Result.class);
		LinkedList<DistributionStatusEvent> value2 = new LinkedList<>();
		value2.add(new DistributionStatusEvent());
		Mockito.when(value.all()).thenReturn(value2);
		Mockito.when(auditAccessor.getListOfDistributionStatuses(Mockito.anyString())).thenReturn(value);

		// default test
		result = testSubject.getListOfDistributionStatuses(did);
	}

	@Test
	public void testGetListOfDistributionStatusesException() throws Exception {
		String did = "";
		Either<List<DistributionStatusEvent>, ActionStatus> result;

		Mockito.when(auditAccessor.getListOfDistributionStatuses(Mockito.anyString()))
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

		Result<DistributionDeployEvent> value = Mockito.mock(Result.class);
		LinkedList<DistributionDeployEvent> value2 = new LinkedList<>();
		value2.add(new DistributionDeployEvent());
		Mockito.when(value.all()).thenReturn(value2);
		Mockito.when(auditAccessor.getDistributionDeployByStatus(Mockito.anyString(), Mockito.anyString(),
				Mockito.anyString())).thenReturn(value);

		// default test
		result = testSubject.getDistributionDeployByStatus(did, action, status);
	}

	@Test
	public void testGetDistributionDeployByStatusEmptyList() throws Exception {
		String did = "";
		String action = "";
		String status = "";
		Either<List<DistributionDeployEvent>, ActionStatus> result;

		Result<DistributionDeployEvent> value = Mockito.mock(Result.class);
		LinkedList<DistributionDeployEvent> value2 = new LinkedList<>();
		value2.add(new DistributionDeployEvent());
		Mockito.when(value.all()).thenReturn(value2);
		Mockito.when(auditAccessor.getDistributionDeployByStatus(Mockito.anyString(), Mockito.anyString(),
				Mockito.anyString())).thenReturn(null);

		// default test
		result = testSubject.getDistributionDeployByStatus(did, action, status);
	}

	@Test
	public void testGetDistributionDeployByStatusException() throws Exception {
		String did = "";
		String action = "";
		String status = "";
		Either<List<DistributionDeployEvent>, ActionStatus> result;

		Mockito.when(auditAccessor.getDistributionDeployByStatus(Mockito.anyString(), Mockito.anyString(),
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

		Result<ResourceAdminEvent> value = Mockito.mock(Result.class);
		List<ResourceAdminEvent> value2 = new LinkedList<>();
		value2.add(new ResourceAdminEvent());
		Mockito.when(value.all()).thenReturn(value2);
		Mockito.when(auditAccessor.getDistributionRequest(Mockito.anyString(), Mockito.anyString())).thenReturn(value);

		// default test
		result = testSubject.getDistributionRequest(did, action);
	}

	@Test
	public void testGetDistributionRequestException() throws Exception {
		String did = "";
		String action = "";
		Either<List<ResourceAdminEvent>, ActionStatus> result;

		Mockito.when(auditAccessor.getDistributionRequest(Mockito.anyString(), Mockito.anyString()))
				.thenThrow(RuntimeException.class);

		// default test
		result = testSubject.getDistributionRequest(did, action);
	}

	@Test
	public void testGetDistributionNotify() throws Exception {
		String did = "";
		String action = "";
		Either<List<DistributionNotificationEvent>, ActionStatus> result;

		Result<DistributionNotificationEvent> value = Mockito.mock(Result.class);
		List<DistributionNotificationEvent> value2 = new LinkedList<>();
		value2.add(new DistributionNotificationEvent());
		Mockito.when(value.all()).thenReturn(value2);

		Mockito.when(auditAccessor.getDistributionNotify(Mockito.anyString(), Mockito.anyString())).thenReturn(value);

		// default test
		result = testSubject.getDistributionNotify(did, action);
	}

	@Test
	public void testGetDistributionNotifyException() throws Exception {
		String did = "";
		String action = "";
		Either<List<DistributionNotificationEvent>, ActionStatus> result;

		Mockito.when(auditAccessor.getDistributionNotify(Mockito.anyString(), Mockito.anyString()))
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

		Result<ResourceAdminEvent> value = Mockito.mock(Result.class);
		List<ResourceAdminEvent> value2 = new LinkedList<>();
		value2.add(new ResourceAdminEvent());
		Mockito.when(value.all()).thenReturn(value2);
		Mockito.when(auditAccessor.getByServiceInstanceId(Mockito.anyString())).thenReturn(value);
		// default test
		result = testSubject.getByServiceInstanceId(serviceInstanceId);
	}

	@Test
	public void testGetByServiceInstanceIdException() throws Exception {
		String serviceInstanceId = "";
		Either<List<ResourceAdminEvent>, ActionStatus> result;

		Mockito.when(auditAccessor.getByServiceInstanceId(Mockito.anyString())).thenThrow(RuntimeException.class);
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

		Result<ResourceAdminEvent> value = Mockito.mock(Result.class);
		List<ResourceAdminEvent> value2 = new LinkedList<>();
		value2.add(new ResourceAdminEvent());
		Mockito.when(value.all()).thenReturn(value2);
		Mockito.when(auditAccessor.getServiceDistributionStatus(Mockito.anyString())).thenReturn(value);

		// default test
		result = testSubject.getServiceDistributionStatusesList(serviceInstanceId);
	}

	@Test
	public void testGetServiceDistributionStatusesList2() throws Exception {
		String serviceInstanceId = "";
		Either<List<? extends AuditingGenericEvent>, ActionStatus> result;

		Result<ResourceAdminEvent> value = Mockito.mock(Result.class);
		List<ResourceAdminEvent> value2 = new LinkedList<>();
		value2.add(new ResourceAdminEvent());
		Mockito.when(value.all()).thenReturn(value2);
		Mockito.when(auditAccessor.getServiceDistributionStatus(Mockito.anyString())).thenReturn(value);

		Result<DistributionDeployEvent> value3 = Mockito.mock(Result.class);
		List<DistributionDeployEvent> value4 = new LinkedList<>();
		value4.add(new DistributionDeployEvent());
		Mockito.when(value3.all()).thenReturn(value4);
		Mockito.when(auditAccessor.getServiceDistributionDeploy(Mockito.anyString())).thenReturn(value3);

		// default test
		result = testSubject.getServiceDistributionStatusesList(serviceInstanceId);
	}

	@Test
	public void testGetServiceDistributionStatusesList3() throws Exception {
		String serviceInstanceId = "";
		Either<List<? extends AuditingGenericEvent>, ActionStatus> result;

		Result<ResourceAdminEvent> value = Mockito.mock(Result.class);
		List<ResourceAdminEvent> value2 = new LinkedList<>();
		value2.add(new ResourceAdminEvent());
		Mockito.when(value.all()).thenReturn(value2);
		Mockito.when(auditAccessor.getServiceDistributionStatus(Mockito.anyString())).thenReturn(value);

		Result<DistributionDeployEvent> value3 = Mockito.mock(Result.class);
		List<DistributionDeployEvent> value4 = new LinkedList<>();
		value4.add(new DistributionDeployEvent());
		Mockito.when(value3.all()).thenReturn(value4);
		Mockito.when(auditAccessor.getServiceDistributionDeploy(Mockito.anyString())).thenReturn(value3);

		
		Result<DistributionNotificationEvent> value5 = Mockito.mock(Result.class);
		List<DistributionNotificationEvent> value6 = new LinkedList<>();
		value6.add(new DistributionNotificationEvent());
		Mockito.when(value5.all()).thenReturn(value6);
		Mockito.when(auditAccessor.getServiceDistributionNotify(Mockito.anyString())).thenReturn(value5);

		// default test
		result = testSubject.getServiceDistributionStatusesList(serviceInstanceId);
	}
	
	@Test
	public void testGetServiceDistributionStatusesListException3() throws Exception {
		String serviceInstanceId = "";
		Either<List<? extends AuditingGenericEvent>, ActionStatus> result;

		Result<ResourceAdminEvent> value = Mockito.mock(Result.class);
		List<ResourceAdminEvent> value2 = new LinkedList<>();
		value2.add(new ResourceAdminEvent());
		Mockito.when(value.all()).thenReturn(value2);
		Mockito.when(auditAccessor.getServiceDistributionStatus(Mockito.anyString())).thenReturn(value);

		Result<DistributionDeployEvent> value3 = Mockito.mock(Result.class);
		List<DistributionDeployEvent> value4 = new LinkedList<>();
		value4.add(new DistributionDeployEvent());
		Mockito.when(value3.all()).thenReturn(value4);
		Mockito.when(auditAccessor.getServiceDistributionDeploy(Mockito.anyString())).thenReturn(value3);

		Mockito.when(auditAccessor.getServiceDistributionNotify(Mockito.anyString())).thenThrow(RuntimeException.class);

		// default test
		result = testSubject.getServiceDistributionStatusesList(serviceInstanceId);
	}
	
	@Test
	public void testGetServiceDistributionStatusesListException2() throws Exception {
		String serviceInstanceId = "";
		Either<List<? extends AuditingGenericEvent>, ActionStatus> result;

		Result<ResourceAdminEvent> value = Mockito.mock(Result.class);
		List<ResourceAdminEvent> value2 = new LinkedList<>();
		value2.add(new ResourceAdminEvent());
		Mockito.when(value.all()).thenReturn(value2);
		Mockito.when(auditAccessor.getServiceDistributionStatus(Mockito.anyString())).thenReturn(value);

		Mockito.when(auditAccessor.getServiceDistributionDeploy(Mockito.anyString())).thenThrow(RuntimeException.class);

		// default test
		result = testSubject.getServiceDistributionStatusesList(serviceInstanceId);
	}

	@Test
	public void testGetServiceDistributionStatusesListException() throws Exception {
		String serviceInstanceId = "";
		Either<List<? extends AuditingGenericEvent>, ActionStatus> result;

		Mockito.when(auditAccessor.getServiceDistributionStatus(Mockito.anyString())).thenThrow(RuntimeException.class);

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
		
		Result<ResourceAdminEvent> value = Mockito.mock(Result.class);
		List<ResourceAdminEvent> value2 = new LinkedList<>();
		value2.add(new ResourceAdminEvent());
		Mockito.when(value.all()).thenReturn(value2);
		Mockito.when(auditAccessor.getAuditByServiceIdAndPrevVersion(Mockito.anyString(), Mockito.anyString())).thenReturn(value);
		
		// default test
		result = testSubject.getAuditByServiceIdAndPrevVersion(serviceInstanceId, prevVersion);
	}
	
	@Test
	public void testGetAuditByServiceIdAndPrevVersionException() throws Exception {
		String serviceInstanceId = "";
		String prevVersion = "";
		Either<List<ResourceAdminEvent>, ActionStatus> result;
		
		Mockito.when(auditAccessor.getAuditByServiceIdAndPrevVersion(Mockito.anyString(), Mockito.anyString())).thenThrow(RuntimeException.class);
		
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

		Result<ResourceAdminEvent> value = Mockito.mock(Result.class);
		List<ResourceAdminEvent> value2 = new LinkedList<>();
		value2.add(new ResourceAdminEvent());
		Mockito.when(value.all()).thenReturn(value2);
		Mockito.when(auditAccessor.getAuditByServiceIdAndCurrVersion(Mockito.anyString(), Mockito.anyString())).thenReturn(value);
		
		// default test
		result = testSubject.getAuditByServiceIdAndCurrVersion(serviceInstanceId, currVersion);
	}
	
	@Test
	public void testGetAuditByServiceIdAndCurrVersionException() throws Exception {
		String serviceInstanceId = "";
		String currVersion = "";
		Either<List<ResourceAdminEvent>, ActionStatus> result;

		Mockito.when(auditAccessor.getAuditByServiceIdAndCurrVersion(Mockito.anyString(), Mockito.anyString())).thenThrow(RuntimeException.class);
		
		// default test
		result = testSubject.getAuditByServiceIdAndCurrVersion(serviceInstanceId, currVersion);
	}
	
	@Test
	public void testIsTableEmpty() throws Exception {
		String tableName = "";
		Either<Boolean, CassandraOperationStatus> result;

		// default test
		result = testSubject.isTableEmpty(tableName);
	}

	@Test
	public void testDeleteAllAudit() throws Exception {
		CassandraOperationStatus result;

		// default test
		result = testSubject.deleteAllAudit();
	}
}