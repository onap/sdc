package org.openecomp.sdc.be.info;

import javax.annotation.Generated;

import org.junit.Test;


public class DistributionStatusOfServiceInfoTest {

	private DistributionStatusOfServiceInfo createTestSubject() {
		return new DistributionStatusOfServiceInfo();
	}

	
	@Test
	public void testGetDistributionID() throws Exception {
		DistributionStatusOfServiceInfo testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDistributionID();
	}

	
	@Test
	public void testSetDistributionID() throws Exception {
		DistributionStatusOfServiceInfo testSubject;
		String distributionID = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setDistributionID(distributionID);
	}

	
	@Test
	public void testGetTimestamp() throws Exception {
		DistributionStatusOfServiceInfo testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getTimestamp();
	}

	
	@Test
	public void testSetTimestamp() throws Exception {
		DistributionStatusOfServiceInfo testSubject;
		String timestamp = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setTimestamp(timestamp);
	}

	
	@Test
	public void testGetUserId() throws Exception {
		DistributionStatusOfServiceInfo testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getUserId();
	}

	
	@Test
	public void testSetUserId() throws Exception {
		DistributionStatusOfServiceInfo testSubject;
		String userId = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setUserId(userId);
	}

	
	@Test
	public void testGetDeployementStatus() throws Exception {
		DistributionStatusOfServiceInfo testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDeployementStatus();
	}

	
	@Test
	public void testSetDeployementStatus() throws Exception {
		DistributionStatusOfServiceInfo testSubject;
		String deployementStatus = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setDeployementStatus(deployementStatus);
	}
}