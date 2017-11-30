package org.openecomp.sdc.be.dao.cassandra;

import java.util.List;

import org.junit.Test;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.resources.data.auditing.AuditingGenericEvent;
import org.openecomp.sdc.be.resources.data.auditing.DistributionDeployEvent;
import org.openecomp.sdc.be.resources.data.auditing.DistributionNotificationEvent;
import org.openecomp.sdc.be.resources.data.auditing.DistributionStatusEvent;
import org.openecomp.sdc.be.resources.data.auditing.ResourceAdminEvent;

import fj.data.Either;


public class AuditCassandraDaoTest {

	private AuditCassandraDao createTestSubject() {
		return new AuditCassandraDao();
	}

	

	


	
	@Test
	public void testGetListOfDistributionStatuses() throws Exception {
		AuditCassandraDao testSubject;
		String did = "";
		Either<List<DistributionStatusEvent>, ActionStatus> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getListOfDistributionStatuses(did);
	}

	
	@Test
	public void testGetDistributionDeployByStatus() throws Exception {
		AuditCassandraDao testSubject;
		String did = "";
		String action = "";
		String status = "";
		Either<List<DistributionDeployEvent>, ActionStatus> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDistributionDeployByStatus(did, action, status);
	}

	
	@Test
	public void testGetDistributionRequest() throws Exception {
		AuditCassandraDao testSubject;
		String did = "";
		String action = "";
		Either<List<ResourceAdminEvent>, ActionStatus> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDistributionRequest(did, action);
	}

	
	@Test
	public void testGetDistributionNotify() throws Exception {
		AuditCassandraDao testSubject;
		String did = "";
		String action = "";
		Either<List<DistributionNotificationEvent>, ActionStatus> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDistributionNotify(did, action);
	}

	
	@Test
	public void testGetByServiceInstanceId() throws Exception {
		AuditCassandraDao testSubject;
		String serviceInstanceId = "";
		Either<List<ResourceAdminEvent>, ActionStatus> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getByServiceInstanceId(serviceInstanceId);
	}

	
	@Test
	public void testGetServiceDistributionStatusesList() throws Exception {
		AuditCassandraDao testSubject;
		String serviceInstanceId = "";
		Either<List<? extends AuditingGenericEvent>, ActionStatus> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getServiceDistributionStatusesList(serviceInstanceId);
	}

	
	@Test
	public void testGetAuditByServiceIdAndPrevVersion() throws Exception {
		AuditCassandraDao testSubject;
		String serviceInstanceId = "";
		String prevVersion = "";
		Either<List<ResourceAdminEvent>, ActionStatus> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getAuditByServiceIdAndPrevVersion(serviceInstanceId, prevVersion);
	}

	
	@Test
	public void testGetAuditByServiceIdAndCurrVersion() throws Exception {
		AuditCassandraDao testSubject;
		String serviceInstanceId = "";
		String currVersion = "";
		Either<List<ResourceAdminEvent>, ActionStatus> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getAuditByServiceIdAndCurrVersion(serviceInstanceId, currVersion);
	}

	
	@Test
	public void testIsTableEmpty() throws Exception {
		AuditCassandraDao testSubject;
		String tableName = "";
		Either<Boolean, CassandraOperationStatus> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.isTableEmpty(tableName);
	}

	
	@Test
	public void testDeleteAllAudit() throws Exception {
		AuditCassandraDao testSubject;
		CassandraOperationStatus result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.deleteAllAudit();
	}
}