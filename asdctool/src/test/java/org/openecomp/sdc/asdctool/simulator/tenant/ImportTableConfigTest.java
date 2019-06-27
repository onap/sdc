package org.openecomp.sdc.asdctool.simulator.tenant;

import org.junit.Test;
import org.openecomp.sdc.be.dao.cassandra.CassandraClient;
import org.openecomp.sdc.be.dao.cassandra.OperationalEnvironmentDao;

import static org.mockito.Mockito.mock;

public class ImportTableConfigTest {

	private ImportTableConfig createTestSubject() {
		return new ImportTableConfig();
	}

	@Test
	public void testCassandraClient() throws Exception {
		ImportTableConfig testSubject;
		CassandraClient result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.cassandraClient();
	}

	@Test
	public void testOperationalEnvironmentDao() throws Exception {
		ImportTableConfig testSubject;
		OperationalEnvironmentDao result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.operationalEnvironmentDao(mock(CassandraClient.class));
	}
}
