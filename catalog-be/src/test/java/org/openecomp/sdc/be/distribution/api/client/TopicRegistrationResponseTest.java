package org.openecomp.sdc.be.distribution.api.client;

import org.junit.Test;

public class TopicRegistrationResponseTest {

	private TopicRegistrationResponse createTestSubject() {
		return new TopicRegistrationResponse();
	}

	@Test
	public void testSetDistrNotificationTopicName() throws Exception {
		TopicRegistrationResponse testSubject;
		String distrNotificationTopicName = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setDistrNotificationTopicName(distrNotificationTopicName);
	}

	@Test
	public void testSetDistrStatusTopicName() throws Exception {
		TopicRegistrationResponse testSubject;
		String distrStatusTopicName = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setDistrStatusTopicName(distrStatusTopicName);
	}

	@Test
	public void testGetDistrNotificationTopicName() throws Exception {
		TopicRegistrationResponse testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDistrNotificationTopicName();
	}

	@Test
	public void testGetDistrStatusTopicName() throws Exception {
		TopicRegistrationResponse testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDistrStatusTopicName();
	}
}