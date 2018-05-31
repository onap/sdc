package org.openecomp.sdc.be.resources.data.auditing.model;

import org.junit.Test;

public class DistributionDataTest {

	private DistributionData createTestSubject() {
		return new DistributionData("", "");
	}

	
	@Test
	public void testGetConsumerId() throws Exception {
		DistributionData testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getConsumerId();
	}

	
	@Test
	public void testGetResourceUrl() throws Exception {
		DistributionData testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getResourceUrl();
	}
}