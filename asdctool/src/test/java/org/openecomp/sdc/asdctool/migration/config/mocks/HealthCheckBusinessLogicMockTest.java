package org.openecomp.sdc.asdctool.migration.config.mocks;

import org.junit.Test;

public class HealthCheckBusinessLogicMockTest {

	private HealthCheckBusinessLogicMock createTestSubject() {
		return new HealthCheckBusinessLogicMock();
	}

	@Test
	public void testInit() throws Exception {
		HealthCheckBusinessLogicMock testSubject;

		// default test
		testSubject = createTestSubject();
		testSubject.init();
	}
}