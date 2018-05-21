package org.openecomp.sdc.be.info;

import org.junit.Test;

public class DistributionStatusTest {

	private DistributionStatus createTestSubject() {
		return DistributionStatus.DEPLOYED;
	}

	@Test
	public void testGetName() throws Exception {
		DistributionStatus testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getName();
	}

	@Test
	public void testGetAuditingStatus() throws Exception {
		DistributionStatus testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getAuditingStatus();
	}

	@Test
	public void testGetStatusByAuditingStatusName() throws Exception {
		String auditingStatus = "";
		DistributionStatus result;

		// default test
		result = DistributionStatus.getStatusByAuditingStatusName(auditingStatus);
	}
}