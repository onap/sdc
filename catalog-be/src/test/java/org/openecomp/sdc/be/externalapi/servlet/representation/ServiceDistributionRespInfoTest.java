package org.openecomp.sdc.be.externalapi.servlet.representation;

import org.junit.Test;

public class ServiceDistributionRespInfoTest {

	private ServiceDistributionRespInfo createTestSubject() {
		return new ServiceDistributionRespInfo();
	}

	@Test
	public void testCtr() throws Exception {
		new ServiceDistributionRespInfo("mock");
	}
	
	@Test
	public void testGetDistributionId() throws Exception {
		ServiceDistributionRespInfo testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDistributionId();
	}

	@Test
	public void testSetDistributionId() throws Exception {
		ServiceDistributionRespInfo testSubject;
		String distributionId = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setDistributionId(distributionId);
	}
}