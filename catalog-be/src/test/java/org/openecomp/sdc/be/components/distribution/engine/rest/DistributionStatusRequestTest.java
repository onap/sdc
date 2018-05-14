package org.openecomp.sdc.be.components.distribution.engine.rest;

import org.junit.Test;

public class DistributionStatusRequestTest {

	private DistributionStatusRequest createTestSubject() {
		return new DistributionStatusRequest("", "");
	}

	@Test
	public void testGetStatus() throws Exception {
		DistributionStatusRequest testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getStatus();
	}

	@Test
	public void testGetErrorReason() throws Exception {
		DistributionStatusRequest testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getErrorReason();
	}
}