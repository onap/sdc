package org.openecomp.sdc.be.resources.data.auditing;

import org.junit.Test;

public class AuditingGenericEventTest {

	private AuditingGenericEvent createTestSubject() {
		return new AuditingGenericEvent();
	}

	@Test
	public void testGetRequestId() throws Exception {
		AuditingGenericEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getRequestId();
	}

	@Test
	public void testSetRequestId() throws Exception {
		AuditingGenericEvent testSubject;
		String requestId = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setRequestId(requestId);
	}

	@Test
	public void testGetServiceInstanceId() throws Exception {
		AuditingGenericEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getServiceInstanceId();
	}

	@Test
	public void testSetServiceInstanceId() throws Exception {
		AuditingGenericEvent testSubject;
		String serviceInstanceId = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setServiceInstanceId(serviceInstanceId);
	}

	@Test
	public void testGetAction() throws Exception {
		AuditingGenericEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getAction();
	}

	@Test
	public void testSetAction() throws Exception {
		AuditingGenericEvent testSubject;
		String action = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setAction(action);
	}

	@Test
	public void testGetStatus() throws Exception {
		AuditingGenericEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getStatus();
	}

	@Test
	public void testSetStatus() throws Exception {
		AuditingGenericEvent testSubject;
		String status = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setStatus(status);
	}

	@Test
	public void testGetDesc() throws Exception {
		AuditingGenericEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDesc();
	}

	@Test
	public void testSetDesc() throws Exception {
		AuditingGenericEvent testSubject;
		String desc = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setDesc(desc);
	}

	@Test
	public void testFillFields() throws Exception {
		AuditingGenericEvent testSubject;

		// default test
		testSubject = createTestSubject();
		testSubject.fillFields();
	}
}