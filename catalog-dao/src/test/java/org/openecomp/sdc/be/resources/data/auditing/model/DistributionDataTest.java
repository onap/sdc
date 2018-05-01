package org.openecomp.sdc.be.resources.data.auditing.model;

import javax.annotation.Generated;

import org.junit.Test;

@Generated(value = "org.junit-tools-1.0.6")
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