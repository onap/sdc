package org.openecomp.sdc.be.resources.data.auditing;

import org.junit.Test;

import java.util.Date;

public class EcompOperationalEnvironmentEventTest {

	private EcompOperationalEnvironmentEvent createTestSubject() {
		return new EcompOperationalEnvironmentEvent();
	}

	@Test
	public void testCtor() throws Exception {
		new EcompOperationalEnvironmentEvent();
		new EcompOperationalEnvironmentEvent("mock", "mock", "mock", "mock", "mock", "mock");
	}
	
	@Test
	public void testGetOperationalEnvironmentId() throws Exception {
		EcompOperationalEnvironmentEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getOperationalEnvironmentId();
	}

	@Test
	public void testSetOperationalEnvironmentId() throws Exception {
		EcompOperationalEnvironmentEvent testSubject;
		String operationalEnvironmentId = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setOperationalEnvironmentId(operationalEnvironmentId);
	}

	@Test
	public void testGetOperationalEnvironmentAction() throws Exception {
		EcompOperationalEnvironmentEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getOperationalEnvironmentAction();
	}

	@Test
	public void testSetOperationalEnvironmentAction() throws Exception {
		EcompOperationalEnvironmentEvent testSubject;
		String operationalEnvironmentAction = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setOperationalEnvironmentAction(operationalEnvironmentAction);
	}

	@Test
	public void testGetOperationalEnvironmentName() throws Exception {
		EcompOperationalEnvironmentEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getOperationalEnvironmentName();
	}

	@Test
	public void testSetOperationalEnvironmentName() throws Exception {
		EcompOperationalEnvironmentEvent testSubject;
		String operationalEnvironmentName = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setOperationalEnvironmentName(operationalEnvironmentName);
	}

	@Test
	public void testGetOperationalEnvironmentType() throws Exception {
		EcompOperationalEnvironmentEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getOperationalEnvironmentType();
	}

	@Test
	public void testSetOperational_environment_type() throws Exception {
		EcompOperationalEnvironmentEvent testSubject;
		String operationalEnvironmentType = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setOperational_environment_type(operationalEnvironmentType);
	}

	@Test
	public void testGetTenantContext() throws Exception {
		EcompOperationalEnvironmentEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getTenantContext();
	}

	@Test
	public void testSetTenantContext() throws Exception {
		EcompOperationalEnvironmentEvent testSubject;
		String tenantContext = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setTenantContext(tenantContext);
	}

	@Test
	public void testGetTimestamp1() throws Exception {
		EcompOperationalEnvironmentEvent testSubject;
		Date result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getTimestamp1();
	}

	@Test
	public void testSetTimestamp1() throws Exception {
		EcompOperationalEnvironmentEvent testSubject;
		Date timestamp = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setTimestamp1(timestamp);
	}

	@Test
	public void testGetAction() throws Exception {
		EcompOperationalEnvironmentEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getAction();
	}

	@Test
	public void testSetAction() throws Exception {
		EcompOperationalEnvironmentEvent testSubject;
		String action = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setAction(action);
	}

	@Test
	public void testFillFields() throws Exception {
		EcompOperationalEnvironmentEvent testSubject;

		// default test
		testSubject = createTestSubject();
		testSubject.fillFields();
	}

	@Test
	public void testToString() throws Exception {
		EcompOperationalEnvironmentEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toString();
	}
}