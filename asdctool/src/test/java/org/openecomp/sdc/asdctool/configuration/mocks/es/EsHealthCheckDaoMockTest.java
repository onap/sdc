package org.openecomp.sdc.asdctool.configuration.mocks.es;

import org.junit.Test;
import org.openecomp.sdc.common.api.HealthCheckInfo.HealthCheckStatus;

public class EsHealthCheckDaoMockTest {

	private EsHealthCheckDaoMock createTestSubject() {
		return new EsHealthCheckDaoMock();
	}

	@Test
	public void testGetClusterHealthStatus() throws Exception {
		EsHealthCheckDaoMock testSubject;
		HealthCheckStatus result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getClusterHealthStatus();
	}
}