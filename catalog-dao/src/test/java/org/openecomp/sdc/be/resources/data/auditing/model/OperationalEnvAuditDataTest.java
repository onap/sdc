package org.openecomp.sdc.be.resources.data.auditing.model;

import javax.annotation.Generated;

import org.junit.Test;

@Generated(value = "org.junit-tools-1.0.6")
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