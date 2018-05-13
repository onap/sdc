package org.openecomp.sdc.be.components.distribution.engine;

import java.util.Properties;

import org.junit.Test;
import org.openecomp.sdc.be.components.BeConfDependentTest;

import mockit.Deencapsulation;

public class AaiRequestHandlerTest extends BeConfDependentTest {

	private AaiRequestHandler createTestSubject() {
		AaiRequestHandler testSubject = new AaiRequestHandler();
		testSubject.init();
		return testSubject;
	}

	@Test
	public void testInit() throws Exception {
		AaiRequestHandler testSubject;

		// default test
		testSubject = createTestSubject();
		testSubject.init();
	}

	@Test
	public void testGetOperationalEnvById() throws Exception {
		AaiRequestHandler testSubject;
		String id = "";

		// default test
		testSubject = createTestSubject();
		testSubject.getOperationalEnvById(id);
	}

	@Test
	public void testRetryOnException() throws Exception {
		AaiRequestHandler testSubject;
		Exception e = null;
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = Deencapsulation.invoke(testSubject, "retryOnException", new Object[] { Exception.class });
	}

	@Test
	public void testGetCause() throws Exception {
		AaiRequestHandler testSubject;
		Exception e = null;
		Throwable result;

		// default test
		testSubject = createTestSubject();
		result = Deencapsulation.invoke(testSubject, "getCause", new Object[] { Exception.class });
	}

	@Test
	public void testCreateHeaders() throws Exception {
		AaiRequestHandler testSubject;
		Properties result;

		// default test
		testSubject = createTestSubject();
		result = Deencapsulation.invoke(testSubject, "createHeaders");
	}
}