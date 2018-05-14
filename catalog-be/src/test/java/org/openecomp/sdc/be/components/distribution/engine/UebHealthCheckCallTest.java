package org.openecomp.sdc.be.components.distribution.engine;

import org.junit.Test;
import org.openecomp.sdc.be.components.BeConfDependentTest;

public class UebHealthCheckCallTest extends BeConfDependentTest {

	private UebHealthCheckCall createTestSubject() {
		return new UebHealthCheckCall("mock", "mock");
	}

	@Test
	public void testCall() throws Exception {
		UebHealthCheckCall testSubject;
		Boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.call();
	}

	@Test
	public void testGetServer() throws Exception {
		UebHealthCheckCall testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getServer();
	}

	@Test
	public void testGetCambriaHandler() throws Exception {
		UebHealthCheckCall testSubject;
		CambriaHandler result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getCambriaHandler();
	}

	@Test
	public void testSetCambriaHandler() throws Exception {
		UebHealthCheckCall testSubject;
		CambriaHandler cambriaHandler = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setCambriaHandler(cambriaHandler);
	}
}