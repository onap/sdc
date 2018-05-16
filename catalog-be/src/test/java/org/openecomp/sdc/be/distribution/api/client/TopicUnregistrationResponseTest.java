package org.openecomp.sdc.be.distribution.api.client;

import org.junit.Test;

public class TopicUnregistrationResponseTest {

	private TopicUnregistrationResponse createTestSubject() {
		return new TopicUnregistrationResponse("", "", CambriaOperationStatus.AUTHENTICATION_ERROR, CambriaOperationStatus.AUTHENTICATION_ERROR);
	}

	@Test
	public void testGetDistrNotificationTopicName() throws Exception {
		TopicUnregistrationResponse testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDistrNotificationTopicName();
	}

	@Test
	public void testGetDistrStatusTopicName() throws Exception {
		TopicUnregistrationResponse testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDistrStatusTopicName();
	}

	@Test
	public void testGetNotificationUnregisterResult() throws Exception {
		TopicUnregistrationResponse testSubject;
		CambriaOperationStatus result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getNotificationUnregisterResult();
	}

	@Test
	public void testGetStatusUnregisterResult() throws Exception {
		TopicUnregistrationResponse testSubject;
		CambriaOperationStatus result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getStatusUnregisterResult();
	}
}