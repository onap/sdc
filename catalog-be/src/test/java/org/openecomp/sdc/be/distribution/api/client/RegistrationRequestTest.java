package org.openecomp.sdc.be.distribution.api.client;

import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

public class RegistrationRequestTest {

	private RegistrationRequest createTestSubject() {
		return new RegistrationRequest("", "", false);
	}
	
	@Test
	public void testConstructor() throws Exception {
		List<String> distEnvEndPoints = new LinkedList<>();
		new RegistrationRequest("mock", "mock", distEnvEndPoints , false);
	}
	
	@Test
	public void testGetApiPublicKey() throws Exception {
		RegistrationRequest testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getApiPublicKey();
	}

	@Test
	public void testGetDistrEnvName() throws Exception {
		RegistrationRequest testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDistrEnvName();
	}

	@Test
	public void testGetIsConsumerToSdcDistrStatusTopic() throws Exception {
		RegistrationRequest testSubject;
		Boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getIsConsumerToSdcDistrStatusTopic();
	}

	@Test
	public void testGetDistEnvEndPoints() throws Exception {
		RegistrationRequest testSubject;
		List<String> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDistEnvEndPoints();
	}

	@Test
	public void testSetDistEnvEndPoints() throws Exception {
		RegistrationRequest testSubject;
		List<String> distEnvEndPoints = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setDistEnvEndPoints(distEnvEndPoints);
	}
}