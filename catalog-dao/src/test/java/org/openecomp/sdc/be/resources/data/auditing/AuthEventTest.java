package org.openecomp.sdc.be.resources.data.auditing;

import java.util.Date;
import java.util.UUID;

import javax.annotation.Generated;

import org.junit.Test;


public class AuthEventTest {

	private AuthEvent createTestSubject() {
		return new AuthEvent();
	}

	
	@Test
	public void testGetUrl() throws Exception {
		AuthEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getUrl();
	}

	
	@Test
	public void testSetUrl() throws Exception {
		AuthEvent testSubject;
		String url = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setUrl(url);
	}

	
	@Test
	public void testGetUser() throws Exception {
		AuthEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getUser();
	}

	
	@Test
	public void testSetUser() throws Exception {
		AuthEvent testSubject;
		String user = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setUser(user);
	}

	
	@Test
	public void testGetAuthStatus() throws Exception {
		AuthEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getAuthStatus();
	}

	
	@Test
	public void testSetAuthStatus() throws Exception {
		AuthEvent testSubject;
		String authStatus = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setAuthStatus(authStatus);
	}

	
	@Test
	public void testGetRealm() throws Exception {
		AuthEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getRealm();
	}

	
	@Test
	public void testSetRealm() throws Exception {
		AuthEvent testSubject;
		String realm = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setRealm(realm);
	}

	
	@Test
	public void testGetTimebaseduuid() throws Exception {
		AuthEvent testSubject;
		UUID result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getTimebaseduuid();
	}

	
	@Test
	public void testSetTimebaseduuid() throws Exception {
		AuthEvent testSubject;
		UUID timebaseduuid = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setTimebaseduuid(timebaseduuid);
	}

	
	@Test
	public void testGetTimestamp1() throws Exception {
		AuthEvent testSubject;
		Date result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getTimestamp1();
	}

	
	@Test
	public void testSetTimestamp1() throws Exception {
		AuthEvent testSubject;
		Date timestamp1 = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setTimestamp1(timestamp1);
	}

	
	@Test
	public void testGetAction() throws Exception {
		AuthEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getAction();
	}

	
	@Test
	public void testSetAction() throws Exception {
		AuthEvent testSubject;
		String action = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setAction(action);
	}

	
	@Test
	public void testGetStatus() throws Exception {
		AuthEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getStatus();
	}

	
	@Test
	public void testSetStatus() throws Exception {
		AuthEvent testSubject;
		String status = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setStatus(status);
	}

	
	@Test
	public void testGetDesc() throws Exception {
		AuthEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDesc();
	}

	
	@Test
	public void testSetDesc() throws Exception {
		AuthEvent testSubject;
		String desc = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setDesc(desc);
	}

	
	@Test
	public void testGetRequestId() throws Exception {
		AuthEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getRequestId();
	}

	
	@Test
	public void testSetRequestId() throws Exception {
		AuthEvent testSubject;
		String requestId = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setRequestId(requestId);
	}

	
	@Test
	public void testFillFields() throws Exception {
		AuthEvent testSubject;

		// default test
		testSubject = createTestSubject();
		testSubject.fillFields();
	}
}