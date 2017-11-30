package org.openecomp.sdc.common.rest.api;

import org.junit.Test;


public class RestResponseTest {

	private RestResponse createTestSubject() {
		return new RestResponse("", "", 0);
	}

	
	@Test
	public void testGetResponse() throws Exception {
		RestResponse testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getResponse();
	}

	
	@Test
	public void testSetResponse() throws Exception {
		RestResponse testSubject;
		String response = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setResponse(response);
	}

	
	@Test
	public void testGetHttpStatusCode() throws Exception {
		RestResponse testSubject;
		int result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getHttpStatusCode();
	}

	
	@Test
	public void testSetHttpStatusCode() throws Exception {
		RestResponse testSubject;
		int httpStatusCode = 0;

		// default test
		testSubject = createTestSubject();
		testSubject.setHttpStatusCode(httpStatusCode);
	}

	
	@Test
	public void testGetStatusDescription() throws Exception {
		RestResponse testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getStatusDescription();
	}

	
	@Test
	public void testSetStatusDescription() throws Exception {
		RestResponse testSubject;
		String statusDescription = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setStatusDescription(statusDescription);
	}

	
	@Test
	public void testToString() throws Exception {
		RestResponse testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toString();
	}
}