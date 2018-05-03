package org.openecomp.sdc.asdctool.migration.config.mocks;

import org.junit.Test;
import org.openecomp.sdc.be.components.distribution.engine.DmaapHealth;

public class DmaapHealthCheckMockTest {

	private DmaapHealthCheckMock createTestSubject() {
		return new DmaapHealthCheckMock();
	}

	@Test
	public void testInit() throws Exception {
		DmaapHealthCheckMock testSubject;
		DmaapHealth result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.init();
	}
}