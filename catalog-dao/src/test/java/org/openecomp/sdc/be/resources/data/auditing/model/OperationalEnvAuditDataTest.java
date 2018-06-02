package org.openecomp.sdc.be.resources.data.auditing.model;

import org.junit.Test;

public class OperationalEnvAuditDataTest {

	private OperationalEnvAuditData createTestSubject() {
		return new OperationalEnvAuditData("", "", "");
	}

	
	@Test
	public void testGetEnvId() throws Exception {
		OperationalEnvAuditData testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getEnvId();
	}

	
	@Test
	public void testGetVnfWorkloadContext() throws Exception {
		OperationalEnvAuditData testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getVnfWorkloadContext();
	}

	
	@Test
	public void testGetTenant() throws Exception {
		OperationalEnvAuditData testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getTenant();
	}
}