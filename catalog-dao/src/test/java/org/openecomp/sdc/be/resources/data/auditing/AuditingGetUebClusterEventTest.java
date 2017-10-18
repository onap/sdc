package org.openecomp.sdc.be.resources.data.auditing;

import java.util.Date;
import java.util.UUID;

import org.junit.Test;

public class AuditingGetUebClusterEventTest {

	private AuditingGetUebClusterEvent createTestSubject() {
		return new AuditingGetUebClusterEvent();
	}

	@Test
	public void testFillFields() throws Exception {
		AuditingGetUebClusterEvent testSubject;

		// default test
		testSubject = createTestSubject();
		testSubject.fillFields();
	}

	@Test
	public void testGetConsumerId() throws Exception {
		AuditingGetUebClusterEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getConsumerId();
	}

	@Test
	public void testSetConsumerId() throws Exception {
		AuditingGetUebClusterEvent testSubject;
		String consumerId = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setConsumerId(consumerId);
	}

	@Test
	public void testGetTimebaseduuid() throws Exception {
		AuditingGetUebClusterEvent testSubject;
		UUID result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getTimebaseduuid();
	}

	@Test
	public void testSetTimebaseduuid() throws Exception {
		AuditingGetUebClusterEvent testSubject;
		UUID timebaseduuid = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setTimebaseduuid(timebaseduuid);
	}

	@Test
	public void testGetTimestamp1() throws Exception {
		AuditingGetUebClusterEvent testSubject;
		Date result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getTimestamp1();
	}

	@Test
	public void testSetTimestamp1() throws Exception {
		AuditingGetUebClusterEvent testSubject;
		Date timestamp1 = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setTimestamp1(timestamp1);
	}

	@Test
	public void testGetRequestId() throws Exception {
		AuditingGetUebClusterEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getRequestId();
	}

	@Test
	public void testSetRequestId() throws Exception {
		AuditingGetUebClusterEvent testSubject;
		String requestId = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setRequestId(requestId);
	}

	@Test
	public void testGetServiceInstanceId() throws Exception {
		AuditingGetUebClusterEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getServiceInstanceId();
	}

	@Test
	public void testSetServiceInstanceId() throws Exception {
		AuditingGetUebClusterEvent testSubject;
		String serviceInstanceId = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setServiceInstanceId(serviceInstanceId);
	}

	@Test
	public void testGetAction() throws Exception {
		AuditingGetUebClusterEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getAction();
	}

	@Test
	public void testSetAction() throws Exception {
		AuditingGetUebClusterEvent testSubject;
		String action = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setAction(action);
	}

	@Test
	public void testGetStatus() throws Exception {
		AuditingGetUebClusterEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getStatus();
	}

	@Test
	public void testSetStatus() throws Exception {
		AuditingGetUebClusterEvent testSubject;
		String status = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setStatus(status);
	}

	@Test
	public void testGetDesc() throws Exception {
		AuditingGetUebClusterEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDesc();
	}

	@Test
	public void testSetDesc() throws Exception {
		AuditingGetUebClusterEvent testSubject;
		String desc = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setDesc(desc);
	}

	@Test
	public void testToString() throws Exception {
		AuditingGetUebClusterEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toString();
	}
}