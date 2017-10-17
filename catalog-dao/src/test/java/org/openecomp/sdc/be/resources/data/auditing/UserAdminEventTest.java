package org.openecomp.sdc.be.resources.data.auditing;

import java.util.Date;
import java.util.UUID;

import javax.annotation.Generated;

import org.junit.Test;


public class UserAdminEventTest {

	private UserAdminEvent createTestSubject() {
		return new UserAdminEvent();
	}

	
	@Test
	public void testFillFields() throws Exception {
		UserAdminEvent testSubject;

		// default test
		testSubject = createTestSubject();
		testSubject.fillFields();
	}

	
	@Test
	public void testGetModifier() throws Exception {
		UserAdminEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getModifier();
	}

	
	@Test
	public void testSetModifier() throws Exception {
		UserAdminEvent testSubject;
		String modifier = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setModifier(modifier);
	}

	
	@Test
	public void testGetUserBefore() throws Exception {
		UserAdminEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getUserBefore();
	}

	
	@Test
	public void testSetUserBefore() throws Exception {
		UserAdminEvent testSubject;
		String userBeforeName = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setUserBefore(userBeforeName);
	}

	
	@Test
	public void testGetUserAfter() throws Exception {
		UserAdminEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getUserAfter();
	}

	
	@Test
	public void testSetUserAfter() throws Exception {
		UserAdminEvent testSubject;
		String userAfterName = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setUserAfter(userAfterName);
	}

	
	@Test
	public void testGetRequestId() throws Exception {
		UserAdminEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getRequestId();
	}

	
	@Test
	public void testSetRequestId() throws Exception {
		UserAdminEvent testSubject;
		String requestId = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setRequestId(requestId);
	}

	
	@Test
	public void testGetServiceInstanceId() throws Exception {
		UserAdminEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getServiceInstanceId();
	}

	
	@Test
	public void testSetServiceInstanceId() throws Exception {
		UserAdminEvent testSubject;
		String serviceInstanceId = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setServiceInstanceId(serviceInstanceId);
	}

	
	@Test
	public void testGetAction() throws Exception {
		UserAdminEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getAction();
	}

	
	@Test
	public void testSetAction() throws Exception {
		UserAdminEvent testSubject;
		String action = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setAction(action);
	}

	
	@Test
	public void testGetStatus() throws Exception {
		UserAdminEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getStatus();
	}

	
	@Test
	public void testSetStatus() throws Exception {
		UserAdminEvent testSubject;
		String status = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setStatus(status);
	}

	
	@Test
	public void testGetDesc() throws Exception {
		UserAdminEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDesc();
	}

	
	@Test
	public void testSetDesc() throws Exception {
		UserAdminEvent testSubject;
		String desc = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setDesc(desc);
	}

	
	@Test
	public void testGetTimebaseduuid() throws Exception {
		UserAdminEvent testSubject;
		UUID result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getTimebaseduuid();
	}

	
	@Test
	public void testSetTimebaseduuid() throws Exception {
		UserAdminEvent testSubject;
		UUID timebaseduuid = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setTimebaseduuid(timebaseduuid);
	}

	
	@Test
	public void testGetTimestamp1() throws Exception {
		UserAdminEvent testSubject;
		Date result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getTimestamp1();
	}

	
	@Test
	public void testSetTimestamp1() throws Exception {
		UserAdminEvent testSubject;
		Date timestamp = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setTimestamp1(timestamp);
	}

	
	@Test
	public void testToString() throws Exception {
		UserAdminEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toString();
	}
}