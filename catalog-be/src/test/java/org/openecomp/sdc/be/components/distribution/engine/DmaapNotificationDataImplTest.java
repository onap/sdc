package org.openecomp.sdc.be.components.distribution.engine;

import org.junit.Test;

public class DmaapNotificationDataImplTest {

	private DmaapNotificationDataImpl createTestSubject() {
		return new DmaapNotificationDataImpl();
	}

	@Test
	public void testGetOperationalEnvironmentId() throws Exception {
		DmaapNotificationDataImpl testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getOperationalEnvironmentId();
	}

	@Test
	public void testGetOperationalEnvironmentName() throws Exception {
		DmaapNotificationDataImpl testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getOperationalEnvironmentName();
	}

	@Test
	public void testGetTenantContext() throws Exception {
		DmaapNotificationDataImpl testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getTenantContext();
	}
}