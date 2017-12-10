package org.openecomp.sdc.common.rest.api;

import org.junit.Test;


public class RestResponseAsByteArrayTest {

	private RestResponseAsByteArray createTestSubject() {
		return new RestResponseAsByteArray(null, "", 0);
	}

	
	@Test
	public void testGetResponse() throws Exception {
		RestResponseAsByteArray testSubject;
		byte[] result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getResponse();
	}

	
	@Test
	public void testSetResponse() throws Exception {
		RestResponseAsByteArray testSubject;
		byte[] response = new byte[] { ' ' };

		// default test
		testSubject = createTestSubject();
		testSubject.setResponse(response);
	}

	
	@Test
	public void testGetHttpStatusCode() throws Exception {
		RestResponseAsByteArray testSubject;
		int result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getHttpStatusCode();
	}

	
	@Test
	public void testSetHttpStatusCode() throws Exception {
		RestResponseAsByteArray testSubject;
		int httpStatusCode = 0;

		// default test
		testSubject = createTestSubject();
		testSubject.setHttpStatusCode(httpStatusCode);
	}

	
	@Test
	public void testGetStatusDescription() throws Exception {
		RestResponseAsByteArray testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getStatusDescription();
	}

	
	@Test
	public void testSetStatusDescription() throws Exception {
		RestResponseAsByteArray testSubject;
		String statusDescription = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setStatusDescription(statusDescription);
	}

	
	@Test
	public void testToString() throws Exception {
		RestResponseAsByteArray testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toString();
	}

	
	@Test
	public void testToPrettyString() throws Exception {
		RestResponseAsByteArray testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toPrettyString();
	}
}