package org.openecomp.sdc.be.components.distribution.engine;

import org.junit.Test;

public class DistributionStatusNotificationTest {

	private DistributionStatusNotification createTestSubject() {
		return new DistributionStatusNotification();
	}

	@Test
	public void testGetDistributionID() throws Exception {
		DistributionStatusNotification testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDistributionID();
	}

	@Test
	public void testSetDistributionID() throws Exception {
		DistributionStatusNotification testSubject;
		String distributionId = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setDistributionID(distributionId);
	}

	@Test
	public void testGetConsumerID() throws Exception {
		DistributionStatusNotification testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getConsumerID();
	}

	@Test
	public void testSetConsumerID() throws Exception {
		DistributionStatusNotification testSubject;
		String consumerId = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setConsumerID(consumerId);
	}

	@Test
	public void testGetTimestamp() throws Exception {
		DistributionStatusNotification testSubject;
		long result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getTimestamp();
	}

	@Test
	public void testSetTimestamp() throws Exception {
		DistributionStatusNotification testSubject;
		long timestamp = 4354;

		// default test
		testSubject = createTestSubject();
		testSubject.setTimestamp(timestamp);
	}

	@Test
	public void testGetArtifactURL() throws Exception {
		DistributionStatusNotification testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getArtifactURL();
	}

	@Test
	public void testSetArtifactURL() throws Exception {
		DistributionStatusNotification testSubject;
		String artifactURL = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setArtifactURL(artifactURL);
	}

	@Test
	public void testGetStatus() throws Exception {
		DistributionStatusNotification testSubject;
		DistributionStatusNotificationEnum result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getStatus();
	}

	@Test
	public void testSetStatus() throws Exception {
		DistributionStatusNotification testSubject;
		DistributionStatusNotificationEnum status = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setStatus(status);
	}

	@Test
	public void testGetErrorReason() throws Exception {
		DistributionStatusNotification testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getErrorReason();
	}

	@Test
	public void testSetErrorReason() throws Exception {
		DistributionStatusNotification testSubject;
		String errorReason = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setErrorReason(errorReason);
	}

	@Test
	public void testIsDistributionCompleteNotification() throws Exception {
		DistributionStatusNotification testSubject;
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.isDistributionCompleteNotification();
		
		testSubject.status = DistributionStatusNotificationEnum.DISTRIBUTION_COMPLETE_OK;
		result = testSubject.isDistributionCompleteNotification();
		
		testSubject.status = DistributionStatusNotificationEnum.DISTRIBUTION_COMPLETE_ERROR;
		result = testSubject.isDistributionCompleteNotification();
	}

	@Test
	public void testToString() throws Exception {
		DistributionStatusNotification testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toString();
	}
}